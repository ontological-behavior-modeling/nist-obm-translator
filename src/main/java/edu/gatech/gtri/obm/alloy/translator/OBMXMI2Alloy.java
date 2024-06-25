package edu.gatech.gtri.obm.alloy.translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.EMFUtil;
import edu.umd.omgutil.UMLModelErrorException;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;


/**
 * Translate SysML Behavior Model in a xmi file into an Alloy file.
 * 
 * Usage:
 * 
 * OBMXMI2Alloy translator = new OBMXMI2Alloy("C:\\temp\\OBMModel.xmi"); translator.createAlloyFile(new File("C:\\OBMModel.xmi"), "Model::4.1 Basic Examples::4.1.1 Time Orderings::SimpleSequence", new
 * File("C:\\output.als"));
 * 
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 *
 */
public class OBMXMI2Alloy {
  /**
   * errorMessages collected during the translation. Resetting by each createAlloyFile method call.
   */
  List<String> errorMessages;
  /**
   * messages collected during the translation. Resetting by each createAlloyFile method call.
   */
  List<String> messages;
  /**
   * A class connect this and Alloy class
   */
  ToAlloy toAlloy;

  /**
   * omgutil SysMLUtil - used to create the omgutil ResourceSet used during the translation
   */
  SysMLUtil sysMLUtil;
  /**
   * A set of Alloy fields created for Properties with <<Parameter>> stereotype
   */
  Set<Field> parameterFields;

  /**
   * A set of Alloy fields mapped from <<ObjectFlow>> connectors's sourceOutputProperty and targetInputProperty.
   */
  Set<Field> valueTypeFields;
  /**
   * A map where key is sig name string and value is a set of field name strings. This includes inherited fields/properties and used in closure facts.
   */
  Map<String, Set<String>> stepPropertiesBySig;
  /**
   * A dictionary contains signature as key and a set of properties as value.
   */
  Map<PrimSig, Set<Property>> redefinedPropertiesBySig;

  /**
   * A set of leaf signatures
   */
  Set<PrimSig> leafSigs;

  /** Stereotype qualified names */
  private static String STEREOTYPE_STEP = "Model::OBM::Step";
  private static String STEREOTYPE_PATICIPANT = "SysML::ParticipantProperty";
  private static String STEREOTYPE_PAREMETER = "Model::OBM::Parameter";// property
  private static String STEREOTYPE_VALUETYPE = "SysML::Blocks::ValueType";


  /**
   * An absolute path name string for the required library folder containing Transfer.als and utilities(folder) necessary for creating own OBMUtil alloy object.
   */
  private String alloyLibPath;


  /**
   * A constructor to set the given alloyLibPath as an instance variable.
   * 
   * @param alloyLibPath the abstract pathname.
   */
  public OBMXMI2Alloy(String _alloyLibPath) {
    this.alloyLibPath = _alloyLibPath;
  }

  /**
   * Initialize the translator. Called by createAlloyFile method.
   */
  private void reset() {
    toAlloy = new ToAlloy(alloyLibPath);
    stepPropertiesBySig = new HashMap<>();
    this.errorMessages = new ArrayList<>();
    this.messages = new ArrayList<>();
  }

  /**
   * Initialize the translator and create an alloy output file of the qualifideName class/behavior model in the xml file. If this method return false, you may use getErrorMessages() to know why failed.
   * 
   * @param xmiFile - the xmi file contain a class to be translated to an alloy file.
   * @param qualifiedName of a UML:Class for translation (ie., Model::FoodService::OFSingleFoodService)
   * @param outputFile - the output alloy file
   * @return true if the given outputFile is created from the given xmlFile and the qualifiedName; false if fails.
   */
  public boolean createAlloyFile(File xmiFile, String qualifiedName, File outputFile) {
    reset();
    if (!xmiFile.exists() || !xmiFile.canRead()) {
      this.errorMessages.add(
          "A file " + xmiFile.getAbsolutePath() + " does not exist or no permission to read.");
      return false;
    }
    try {
      if (loadOBMAndCreateAlloy(xmiFile, qualifiedName)) {
        try {
          boolean success = toAlloy.createAlloyFile(outputFile, this.parameterFields);
          if (success)
            this.messages.add(outputFile.getAbsolutePath() + " is created");
          else
            this.errorMessages.add("Failed to create the alloy file as "
                + outputFile.getAbsolutePath() + ". May not have write permission.");
          return success;
        } catch (IOException e) {
          this.errorMessages.add("Failed to translator the alloy file: " + e.getMessage());
        }
      }
    } catch (FileNotFoundException e) {
      // error message is set in createAlloyFile method
      // happens the xmi file having behavior model does not exist.
    } catch (UMLModelErrorException e) {
      // error message is set in createAlloyFile method
      // happens when the alloy library resource could not initialized.
    }
    return false;
  }

  /**
   * Get errorMessages collected while the translation.
   * 
   * @return errorMessage - list of error message strings
   */
  public List<String> getErrorMessages() {
    return this.errorMessages;
  }

  /**
   * Load the given xmi file and find the given class and create alloy objects in memory.
   * 
   * @xmiFile - the xml file contains a class to be converted to an alloy file
   * @param - the qualifiedName of a class contained in the xml file
   * @return boolean true if successfully created as alloy objects otherwise false
   */
  private boolean loadOBMAndCreateAlloy(File xmiFile, String className)
      throws FileNotFoundException, UMLModelErrorException {

    parameterFields = new HashSet<>();
    valueTypeFields = new HashSet<>();
    // transferingTypeSig = new HashSet<>();
    ResourceSet rs;
    try {
      rs = EMFUtil.createResourceSet();
    } catch (FileNotFoundException e1) {
      this.errorMessages.add("Failed to initialize OmgUtil.");
      return false;
    }
    Resource r = EMFUtil.loadResourceWithDependencies(rs,
        URI.createFileURI(xmiFile.getAbsolutePath()), null);

    try {
      while (!r.isLoaded()) {
        this.messages.add("XMI Resource not loaded yet wait 1 milli sec...");
        Thread.sleep(1000);
      }
    } catch (Exception e) {
    }

    try {
      sysMLUtil = new SysMLUtil(rs);
    } catch (UMLModelErrorException e) {
      this.errorMessages.add("Failed to initialize OmgUtil.");
      throw e;
    }
    SysMLAdapter sysmladapter;
    try {
      sysmladapter = new SysMLAdapter(xmiFile, null);
    } catch (FileNotFoundException e) {
      this.errorMessages.add(xmiFile.getAbsolutePath() + " does not exist.");
      throw e;
    } catch (UMLModelErrorException e) {
      this.errorMessages.add("Failed to load " + xmiFile + " into OmgUtil");
      throw e;
    }

    org.eclipse.uml2.uml.NamedElement mainClass = EMFUtil.getNamedElement(r, className);

    if (mainClass == null) {
      this.errorMessages.add(className + " not found in " + xmiFile.getAbsolutePath());
      return false;
    }

    // key = NamedElement(Class or org.eclipse.uml2.uml.PrimitiveType(Integer, Real etc.)) value =
    // property used to create disj fields
    Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass =
        new HashMap<>();
    if (mainClass instanceof Class) {
      // The main class will be the last in this list
      List<org.eclipse.uml2.uml.Class> classInHierarchyForMain =
          MDUtils.createListIncludeSelfAndParents((Class) mainClass);
      PrimSig parentSig = Alloy.occSig; // oldest's parent is always Occurrence
      for (Class aClass : classInHierarchyForMain) { // loop through oldest to youngest(main
                                                     // is the youngest)
        boolean isMainSig = (aClass == mainClass) ? true : false;
        // create Signature - returned parentSig will be the next aClass(Signature)'s parent
        parentSig = toAlloy.createSig(aClass.getName(), parentSig, isMainSig);
        if (parentSig == null) {
          this.errorMessages.add(
              "Signature named \"" + aClass.getName()
                  + "\" already existed (possibly in the required library).  The name must be unique.");
          return false;
        }
        processClassToSig(aClass, propertiesByClass);
      }
    }

    Set<NamedElement> allClassesConnectedToMainSigByFields = propertiesByClass.keySet(); // SimpleSequence,
    // [0]=grand parent [1]=parent [2]=child
    List<Class> classInHierarchy = MDUtils.createListIncludeSelfAndParents((Class) mainClass); // SimpleSequence

    /* Map<PrimSig, Set<Property>> */ redefinedPropertiesBySig = new HashMap<>();
    // go throw each sigs in classInHierarchy and allClassesConnectedToMainSigByFields
    for (Class ne : classInHierarchy) {
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      addFieldsToSig(ne, propertiesByType);
      addToStepPropertiesBySig(ne);

      // allClasses MAY contain a class in hierarchy so remove it
      allClassesConnectedToMainSigByFields.remove(ne);
    }

    for (NamedElement ne : allClassesConnectedToMainSigByFields) {
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      addFieldsToSig(ne, propertiesByType);
      addToStepPropertiesBySig(ne);
    }

    for (PrimSig sig : redefinedPropertiesBySig.keySet()) {
      // fact {all x: OFFoodService | x.eat in OFEat }
      // fact {all x: IFSingleFoodService | x.order in IFCustomerOrder}
      toAlloy.addRedefinedSubsettingAsFacts(sig,
          MDUtils.toNameAndType(redefinedPropertiesBySig.get(sig)));

    }

    Set<NamedElement> leafClasses = MDUtils.findLeafClass(allClassesConnectedToMainSigByFields);
    leafClasses.add(classInHierarchy.get(classInHierarchy.size() - 1));
    leafSigs = toSigs(leafClasses);


    // connectors
    ConnectorsHandler connectorsHandler =
        new ConnectorsHandler(sysmladapter, this.sysMLUtil, this.leafSigs,
            this.toAlloy, this.parameterFields, this.stepPropertiesBySig);

    connectorsHandler.process(classInHierarchy, allClassesConnectedToMainSigByFields);

    Set<Sig> sigWithTransferFields = connectorsHandler.getSigWithTransferFields();
    Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType =
        connectorsHandler.getSigNameWithTransferConnectorWithSameInputOutputFieldType();
    HashMap<String, Set<String>> connectorTargetInputPropertyNamesByClassName =
        connectorsHandler.getConnectorTargetInputPropertyNamesByClassName();
    HashMap<String, Set<String>> connectorSourceOutputPrpertyNamesByClassName =
        connectorsHandler.getConnectorSourceOutputPrpertyNamesByClassName();

    Set<String> transferingTypeSig = connectorsHandler.getTransferingTypeSig();
    Map<String, Set<String>> sigToTransferFieldMap = connectorsHandler.getSigToTransferFieldMap();
    Map<String, Set<Expr>> sigToFactsMap = connectorsHandler.getSigToFactsMap();
    this.messages.addAll(connectorsHandler.getMessages());


    // using sigToFactsMap and sigToTransferFieldMap to add transferFields to mainSig.
    // loop through mainSig's parent to collect transferSigs and added to mainSig as its fields
    // Note: Only supporting transferFields inherited to mainSig.
    // If maingSig has a parent who inherited tranferFields, currently the translator is not
    // supported to handle the inherited transfer fields for non mainSig.
    Set<String> mainSigInheritingTransferFields = new HashSet<>();
    // i.e.,
    // o/isBeforeTarget[x . (IFFoodService <: transferOrderServe)]
    // o/bijectionFiltered[o/outputs, x . (FoodService <: order), x . (FoodService <: order) .
    // (IFOrder <: orderedFoodItem)]
    Set<Expr> mainSigInheritingTransferRelatedFacts = new HashSet<>();
    // classInHiearchy = [0: FoodService, 1: IFoodService, 2: IFSingleFoodServive]
    for (int i = 0; i < classInHierarchy.size() - 1; i++) {
      Set<String> possibleMainSigInheritingTransferFields =
          sigToTransferFieldMap.get(classInHierarchy.get(i).getName());
      if (possibleMainSigInheritingTransferFields != null) {
        // transferOrderServe - OFSingleFoodService
        mainSigInheritingTransferFields.addAll(possibleMainSigInheritingTransferFields);
      }
      Set<Expr> possibleMainSigInheritingTransferRelatedFacts =
          sigToFactsMap.get(classInHierarchy.get(i).getName());
      if (possibleMainSigInheritingTransferRelatedFacts != null)
        mainSigInheritingTransferRelatedFacts.addAll(possibleMainSigInheritingTransferRelatedFacts);
    }
    // add mainSig's inherited transfer fields to step Properties for the mainSig
    stepPropertiesBySig.get(mainClass.getName()).addAll(mainSigInheritingTransferFields);

    toAlloy.addFacts(mainClass.getName(), mainSigInheritingTransferRelatedFacts);


    Set<Sig> noStepsSigs = toAlloy.addStepsFacts(stepPropertiesBySig, leafSigs);
    // if "no x.steps" and sig with fields with type Transfer should not have below:
    // fact {all x: BehaviorWithParameterOut | no y: Transfer | y in x.steps}
    sigWithTransferFields.addAll(noStepsSigs);
    toAlloy.addNoTransferInXStepsFact(sigWithTransferFields, leafSigs);


    // allClassNames are used to create no inputs and no outputs later created from Sigs in
    // propertiesByClass.keySet() and classInHierarchy
    Set<String> allClassNames = Stream
        .of(propertiesByClass.keySet().stream().map(c -> c.getName()).collect(Collectors.toSet()),
            classInHierarchy.stream().map(c -> c.getName()).collect(Collectors.toSet()))
        .flatMap(x -> x.stream()).collect(Collectors.toSet());


    // if the name of signatures is in sigNameOfShardFieldType, then equal input/output facts (ie., {all x: B1 | x.vin in x.inputs} and {all x: B1 | x.inputs in x.vin}}
    // are not be added
    toAlloy.handleNoInputsOutputs(connectorTargetInputPropertyNamesByClassName,
        connectorSourceOutputPrpertyNamesByClassName, allClassNames,
        sigNameWithTransferConnectorWithSameInputOutputFieldType, leafSigs);

    // adding no steps.x
    // fact {all x: Integer | no steps.x}, fact {all x: Real | no steps.x} or {all x: Product | no
    // steps.x}
    toAlloy.addStepClosureFact(transferingTypeSig, leafSigs);
    return true;

  }



  /**
   * Add to property(including inherited ones) name strings of the given NamedElement to stepPropertiesBySig instance variable if the property has STEROTYPE_STEP or STREOTYPE_PATICIPANT stereotype.
   * 
   * @param ne - A NamedElement which has properties to be filtered.
   */
  private void addToStepPropertiesBySig(NamedElement ne) {

    Set<String> stepProperties_all = new HashSet<>();
    if (ne instanceof org.eclipse.uml2.uml.Class) { // ne can be PrimitiveType
      Set<org.eclipse.uml2.uml.Property> atts =
          sysMLUtil.getAllAttributes((org.eclipse.uml2.uml.Class) ne);
      for (Property p : atts) {
        if (p.getAppliedStereotype(STEREOTYPE_STEP) != null
            || p.getAppliedStereotype(STEREOTYPE_PATICIPANT) != null) {
          stepProperties_all.add(p.getName());
        }
      }
    }
    // for PrimitiveType (ie., Real, Integer), empty Set is added
    stepPropertiesBySig.put(ne.getName(), stepProperties_all);
  }

  /**
   * Add fields in Sig (non redefined attributes only), Add cardinality facts (ie., abc = 1) and return set of strings to be used later.
   * 
   * this.parameterFields and this.valueTypeFields are updated.
   * 
   * @param NamedElement ne that map to a Sig
   * @param propertiesByType - Map<Type, List<Property>> map of properties by type
   * @return
   */
  private void addFieldsToSig(NamedElement ne,
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType) {

    PrimSig sigOfNamedElement = toAlloy.getSig(ne.getName());

    Set<Property> redefinedProperties = new HashSet<>();
    if (propertiesByType != null) {
      for (org.eclipse.uml2.uml.Type propertyType : propertiesByType.keySet()) {
        // find property by type (ie., propetyType = Order, List<Property> = [order]);
        List<Property> propertiesSortedByType = propertiesByType.get(propertyType);

        // sort property in alphabetical order, also remove redefined properties from the sorted
        // list.
        List<String> nonRedefinedPropertyInAlphabeticalOrderPerType = new ArrayList<>();
        Set<String> parameterProperties = new HashSet<>();
        Set<String> valueTypeProperties = new HashSet<>();
        for (Property p : propertiesSortedByType) {
          if (p.getName() != null) { // Since MD allow having no name.
            if (p.getRedefinedProperties().size() == 0) {
              nonRedefinedPropertyInAlphabeticalOrderPerType.add(p.getName());
            } else {
              redefinedProperties.add(p);
              // fact {all x: OFFoodService | x.eat in OFEat }
              // toAlloy.addRedefinedSubsettingAsFacts(sigOfNamedElement, field, )
            }
            if (p.getAppliedStereotype(STEREOTYPE_PAREMETER) != null) {
              parameterProperties.add(p.getName());
            } else if (p.getApplicableStereotype(STEREOTYPE_VALUETYPE) != null) {
              valueTypeProperties.add(p.getName());
            }

          } else {
            this.errorMessages.add(p.getQualifiedName()
                + "has no name, so ignored.  Please defined the name to be included");
          }
        }

        redefinedPropertiesBySig.put(sigOfNamedElement, redefinedProperties);

        Collections.sort(nonRedefinedPropertyInAlphabeticalOrderPerType);

        if (nonRedefinedPropertyInAlphabeticalOrderPerType.size() > 0) {
          Sig.Field[] fields =
              toAlloy.addDisjAlloyFields(nonRedefinedPropertyInAlphabeticalOrderPerType,
                  propertyType.getName(), sigOfNamedElement);
          // server, Serve, SinglFooeService
          if (fields != null) { // this should not happens
            for (int j = 0; j < propertiesSortedByType.size(); j++) {
              toAlloy.addCardinalityFact(sigOfNamedElement, fields[j],
                  propertiesSortedByType.get(j).getLower(),
                  propertiesSortedByType.get(j).getUpper());
              if (parameterProperties.contains(fields[j].label)) {
                this.parameterFields.add(fields[j]);
              } else if (valueTypeProperties.contains(fields[j].label)) {
                this.valueTypeFields.add(fields[j]);
              }
            }
          }
        } else { // cardinality only when no redefined properties
          for (Property p : propertiesSortedByType) {
            boolean added = toAlloy.addCardinalityFact(sigOfNamedElement, p.getName(), p.getLower(),
                p.getUpper());
            if (!added)
              this.errorMessages.add("A field \"" + p.getName() + " not found in Sig "
                  + sigOfNamedElement.label + ".  Failed to add cadinality constraint.");
          }
        }
      }
    } // end processing property

  }


  /*
   *
   * go through each class, class to its properties, a property to its type recursively to complete propertiesByClass (Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>>)
   * 
   * For example, this processClassToSig method is called from outside of this method : with umlElement = FoodService, then called recursively(internally) umlElement as Prepare -> Order -> Serve -> Eat
   * -> Pay.
   * 
   * @param umlElement NamedElement either org.eclipse.uml2.uml.Class or org.eclipse.uml2.uml.PrimitiveType to be analyzed to complete propertiesByClass
   * 
   * @param propertiesByClass Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> where the key NamedElement to be mapped to Sig (class or PrimitiveType like Integer and Real) and value is
   * Map<org.eclipse.uml2.uml.Type, List<Property>. The map's key type is property/field's type and List<Property> is property/fields having the same type.
   * 
   * For example, sig SimpleSequence extends Occurrence { disj p1,p2: set AtomicBehavior } propertiesByClass's key = SimpleSequence and value = (key = AtomicBehavior, value =[p1,p2])
   * 
   */
  private void processClassToSig(NamedElement umlElement,
      Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass) {

    if (umlElement instanceof org.eclipse.uml2.uml.Class) {
      Set<Property> atts = sysMLUtil.getOwnedAttributes((org.eclipse.uml2.uml.Class) umlElement);

      if (atts.size() == 0) {
        propertiesByClass.put(umlElement, null);
        return;
      }

      // find property having the same type
      // for example) sig MultipleObjectFlowAlt extends Occurrence
      // { p1: set BehaviorWithParameterOut, ....
      // key = BehaviorWithParameterOut, value = [p1]
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByTheirType = new HashMap<>();
      for (Property p : atts) {
        org.eclipse.uml2.uml.Type eType = p.getType();
        List<Property> ps = null;
        if ((ps = propertiesByTheirType.get(eType)) == null) {
          ps = new ArrayList<>();
          ps.add(p);
          propertiesByTheirType.put(eType, ps);
        } else
          ps.add(p);

        if (eType instanceof org.eclipse.uml2.uml.Class
            || eType instanceof org.eclipse.uml2.uml.PrimitiveType) {

          EList<Classifier> parents = null;
          if (eType instanceof org.eclipse.uml2.uml.Class) {
            parents = ((org.eclipse.uml2.uml.Class) eType).getGenerals();
          }
          // alloy allows only one parent
          // create Sig of property type with or without parent
          // parent should already exists
          toAlloy.createSigOrReturnSig(eType.getName(),
              parents == null || parents.size() == 0 ? null : parents.get(0).getName());
          // process both eType(Class or PrimitiveType) recursively
          processClassToSig(eType, propertiesByClass);
        }
        propertiesByClass.put(umlElement, propertiesByTheirType);
      }
    }
    // like Integer and Real - assume no properties, thus put null as the value of propertiesByClass
    else if (umlElement instanceof org.eclipse.uml2.uml.PrimitiveType) {
      propertiesByClass.put(umlElement, null);
    }
  }

  /**
   * Return Set of Signatures that is stored in toAlloy for the given NamedElement
   * 
   * @param nes - Set of NamedElements
   * @return Set of Signatures
   */
  private Set<PrimSig> toSigs(Set<NamedElement> nes) {
    Set<PrimSig> sigs = new HashSet<>();
    for (NamedElement ne : nes) {
      sigs.add(toAlloy.getSig(ne.getName()));
    }
    return sigs;
  }
}


