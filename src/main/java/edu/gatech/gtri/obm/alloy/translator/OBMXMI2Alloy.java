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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.internal.impl.OpaqueExpressionImpl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.EMFUtil;
import edu.umd.omgutil.UMLModelErrorException;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;
import edu.umd.omgutil.uml.OpaqueExpression;


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
   * omgutil SysMLAdapter - Adapter for SysML from omgutil
   */
  SysMLAdapter sysmladapter;
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
   * A set of string representing the type of transfer fields (ie., Integer)
   */
  Set<String> transferingTypeSig;

  /**
   * A map where key is sig name string and value is a set of field name strings. This includes inherited fields/properties and used in closure facts.
   */
  Map<String, Set<String>> stepPropertiesBySig;

  /**
   * A set of connectors redefined by children so that the connectors are ignored by the parent.
   */
  Set<Connector> redefinedConnectors;

  /**
   * A dictionary contains signature name as key and a set of transfer field names as value.
   */
  Map<String, Set<String>> sigToTransferFieldMap;

  /**
   * A dictionary contains signature name as key and a set of fact expression as value.
   */
  Map<String, Set<Expr>> sigToFactsMap;

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
  private static String STEREOTYPE_ITEMFLOW = "Model::OBM::ItemFlow";
  private static String STEREOTYPE_OBJECTFLOW = "Model::OBM::ObjectFlow";
  private static String STEREOTYPE_BINDDINGCONNECTOR = "SysML::BindingConnector";

  /**
   * An absolute path name string for the required library folder containing Transfer.als and utilities(folder) necessary for creating own OBMUtil alloy object.
   */
  private String alloyLibPath;

  private enum CONNECTOR_TYPE {
    HAPPENS_BEFORE, HAPPENS_DURING, TRANSFER;
  }

  /**
   * A constructor to set the given alloyLibPath as an instance variable.
   * 
   * @param alloyLibPath the abstract pathname.
   */
  public OBMXMI2Alloy(String _alloyLibPath) {
    this.alloyLibPath = _alloyLibPath;
  }

  /**
   * initialize the translator. Called by createAlloyFile method.
   */
  private void reset() {
    toAlloy = new ToAlloy(alloyLibPath);
    redefinedConnectors = new HashSet<Connector>();
    stepPropertiesBySig = new HashMap<>();
    sigToTransferFieldMap = new HashMap<>();
    sigToFactsMap = new HashMap<>();
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
   * @return errorMessage
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
    transferingTypeSig = new HashSet<>();
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
    // key = Signame, values = propertyNames
    HashMap<String, Set<String>> inputs = new HashMap<>(); // collect field type Sig having a
                                                           // transfer connector
    // with transferTarget "Customer"
    HashMap<String, Set<String>> outputs = new HashMap<>(); // collect field type Sig having a
                                                            // transfer connector


    // used to collect sig with fields with Transfer or TransferBefore type. The set is used to
    // create like fact fact {all x: SimpleSequence | no y: Transfer | y in x.steps} if the sig is
    // not in the set
    Set<Sig> sigWithTransferFields = new HashSet<>();
    allClassesConnectedToMainSigByFields = propertiesByClass.keySet();
    // used later for not adding equal inputs/outputs fact like {all x: B1 | x.vin in x.inputs} and {all x: B1 | x.inputs in x.vin}
    Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType = new HashSet<>(); // ie., BehaviorWithParameter

    // from child to parent so that redefinedconnector is not created by parents
    // during handlClassForProcessConnector this.sigToTransferFieldMap is created
    for (int i = classInHierarchy.size() - 1; i >= 0; i--) {
      Class ne = classInHierarchy.get(i);
      sigNameWithTransferConnectorWithSameInputOutputFieldType
          .addAll(handleClassForProecessConnector(ne, inputs, outputs, sigWithTransferFields));
      // removing ne
      allClassesConnectedToMainSigByFields.remove(ne);
    }
    // after handling connectors for Signatures(hierarchy of main Signature), handle others.
    for (NamedElement ne : allClassesConnectedToMainSigByFields) {
      if (ne instanceof Class) {// no connector in primitiveType (Real, Integer)
        sigNameWithTransferConnectorWithSameInputOutputFieldType
            .addAll(handleClassForProecessConnector(ne, inputs, outputs, sigWithTransferFields));
      }
    }

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
    toAlloy.handleNoInputsOutputs(inputs, outputs, allClassNames,
        sigNameWithTransferConnectorWithSameInputOutputFieldType,
        leafSigs);

    // adding no steps.x
    // fact {all x: Integer | no steps.x}, fact {all x: Real | no steps.x} or {all x: Product | no
    // steps.x}
    toAlloy.addStepClosureFact(this.transferingTypeSig, leafSigs);
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
   * 
   * 
   * @param ne - NamedElement mapped to Signature.
   * @param inputs
   * @param outputs
   * @param sigWithTransferFields
   * @return a Set of
   * 
   */
  private Set<String> handleClassForProecessConnector(NamedElement ne,
      HashMap<String, Set<String>> inputs, HashMap<String, Set<String>> outputs,
      Set<Sig> sigWithTransferFields) {

    PrimSig ownerSig = toAlloy.getSig(ne.getName());

    Map<Field, Set<Field>> fieldWithInputs = new HashMap<>(); // key = prepare, value=
                                                              // [preparedFoodItem,prepareDestination]
    Map<Field, Set<Field>> fieldWithOutputs = new HashMap<>(); // key = order,
                                                               // value=[orderAmount,
                                                               // orderDestination,
                                                               // orderedFoodItem]


    // transfer fields created is adding to stepPropertiesBySig passing in processConnector
    // Signature names which has at least one transfer connector having the same field type of sourceOutputProperty and targetInputProperty's BehaviorWithParameter
    Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType =
        processConnector((Class) ne, inputs, outputs,
            sigWithTransferFields, fieldWithInputs, fieldWithOutputs);


    // any of connector ends owned field types are the same (p1, p2:
    // BehaviorWithParameter)
    // 4.1.5 Multiple Execution Step2 = MultiplObjectFlow=[BehaviorWithParameter]
    if (sigNameWithTransferConnectorWithSameInputOutputFieldType.size() > 0) {
      Set<String> nonTransferFieldNames = stepPropertiesBySig.get(ne.getName()).stream()
          .filter(f -> !f.startsWith("transfer")).collect(Collectors.toSet());
      // no inputs
      for (String fieldName : nonTransferFieldNames) {
        if (!AlloyUtils.fieldsLabels(fieldWithInputs.keySet()).contains(fieldName)) {
          // fact {all x: MultipleObjectFlow |no x.p1.inputs}
          toAlloy.addNoInputsOrOutputsFieldFact(ownerSig, fieldName, Alloy.oinputs);
        }
        if (!AlloyUtils.fieldsLabels(fieldWithOutputs.keySet()).contains(fieldName)) {
          toAlloy.addNoInputsOrOutputsFieldFact(ownerSig, fieldName, Alloy.ooutputs);
        }
      }

    }
    // 4/10
    // fact {all x: OFSingleFoodService | x.prepare.inputs in x.prepare.preparedFoodItem +
    // x.prepare.prepareDestination}
    for (Field field : fieldWithInputs.keySet()) {
      // The following fact is NOT included because x.p1.vout is <<Parmeter>> field
      // fact {all x: MultipleObjectFlowAlt | x.p1.outputs in x.p1.vout}
      Set<Field> fields = removeParameterFields(fieldWithInputs.get(field));
      if (fields.size() > 0)
        toAlloy.addInOutClosureFact(ownerSig, field, fields, Alloy.oinputs);
    }
    for (Field field : fieldWithOutputs.keySet()) {
      Set<Field> fields = removeParameterFields(fieldWithOutputs.get(field));
      if (fields.size() > 0)
        toAlloy.addInOutClosureFact(ownerSig, field, fields, Alloy.ooutputs);
    }

    return sigNameWithTransferConnectorWithSameInputOutputFieldType;

  }

  /**
   * Remove fields with Parameter stereotype from the given fields.
   * 
   * @param original - the set of fields before removing Parameter stereotype removed.
   * @return set of fields after removing Parameter streotype fields
   */
  private Set<Field> removeParameterFields(Set<Field> original) {
    return original.stream().filter(f -> !parameterFields.contains(f)).collect(Collectors.toSet());
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

  /**
   * Create a equal or greater than equal to the property's lower fact constraint to sig's field.
   * 
   * @param p Property that maps to field
   * @param sig the owner of field
   * @param field having the constraint fact //
   */
  // private void addCardinalityFact(Property p, PrimSig sig, Field field) {
  // if (p.getLower() == p.getUpper())
  // toAlloy.addCardinalityEqualConstraintToField(field, sig, p.getLower());
  // else if (p.getUpper() == -1 && p.getLower() >= 1) {
  // toAlloy.addCardinalityGreaterThanEqualConstraintToFieldFact(field, sig, p.getLower());
  // }
  // }
  //
  // /**
  // * Create a equal or greater than equal to the property's lower fact constraint to sign's field.
  // *
  // * @param p Property that maps to field having lower and upper values
  // * @param sig the owner of field
  // * @param fieldName name of the sig's field having the constraint fact
  // */
  // private void addCardinalityFact(Property p, PrimSig sig, String fieldName) {
  // if (p.getLower() == p.getUpper())
  // toAlloy.addCardinalityEqualConstraintToField(fieldName, sig, p.getLower());
  // else if (p.getUpper() == -1 && p.getLower() >= 1) {
  // toAlloy.addCardinalityGreaterThanEqualConstraintToFieldFact(fieldName, sig, p.getLower());
  // }
  // }

  /**
   * Add facts by analyzing connectors for a Signature(NamedElement/Class).
   * 
   * @param ne - A NamedElement(Class) mapped to a signature.
   * @param inputs
   * @param outputs
   * @param sigWithTransferFields
   * @param fieldsWithInputs
   * @param fieldsWithOutputs
   * @param connectorendsFieldTypeOwnerFieldTypeSig - connector's property having same owner sig (ie., BehaviorWithParameter in 4.1.5 MultiObjectFlow)
   * 
   * 
   *        // For 4.1.5 MultipleObjectFlow // a connector sourceOutputProperty(i) and targetInputProperty (i)'s owner field is // p1:BehaviorWithParameter, p2:BehaviorParameter // the type
   *        (BehaviorParameter) is the same so put in connectorendsFieldTypeOwnerFieldTypeSig
   * 
   * @return Set<String> The name of Signature that has at least one transfer or transferbefore connector having the same field type of sourceInputProperty and targetOutputProperty (i.e., 4.1.5.
   *         MultipleObjectFlow's BehaviorWithParameter).
   */
  private Set<String> processConnector(Class ne, HashMap<String, Set<String>> inputs,
      HashMap<String, Set<String>> outputs, Set<Sig> sigWithTransferFields,
      Map<Field, Set<Field>> fieldsWithInputs, Map<Field, Set<Field>> fieldsWithOutputs) {

    Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType = new HashSet<>();
    PrimSig sigOfNamedElement = toAlloy.getSig(ne.getName());

    //
    // start handling one of connectors
    //
    // find sig's constraint
    Set<Constraint> constraints = sysMLUtil.getAllRules(ne);
    Set<EList<Element>> oneOfSets = getOneOfRules(constraints); // EList<ConnectorEnd> [ [start, eat] or [order, end]]

    Set<org.eclipse.uml2.uml.Connector> connectors = sysMLUtil.getOwnedConnectors(ne);

    // finding connectors with oneof
    Set<Connector> oneOfConnectors = new HashSet<>();
    for (Connector cn : connectors) {
      for (ConnectorEnd ce : cn.getEnds()) {
        for (EList<Element> oneOfSet : oneOfSets) {
          Optional<Element> found = oneOfSet.stream().filter(e -> e == ce).findFirst();
          if (!found.isEmpty()) {
            oneOfConnectors.add(cn);
          }
        }
      }
    }

    ConnectableElement s, t;
    Map<ConnectableElement, Integer> sourceEndRolesFrequency = new HashMap<>(); // [eat, 2], [start, 1]
    Map<ConnectableElement, Integer> targetEndRolesFrequency = new HashMap<>(); // [order, 2],[end, 1]
    for (Connector oneOfConnector : oneOfConnectors) {
      EList<ConnectorEnd> cends = oneOfConnector.getEnds();
      s = cends.get(0).getRole();
      t = cends.get(1).getRole();
      Integer sFreq = sourceEndRolesFrequency.get(s);
      sourceEndRolesFrequency.put(s, sFreq == null ? 1 : sFreq + 1);
      Integer tFreq = targetEndRolesFrequency.get(t);
      targetEndRolesFrequency.put(t, tFreq == null ? 1 : tFreq + 1);
    }
    // [start]
    Set<ConnectableElement> oneSourceProperties = sourceEndRolesFrequency.entrySet().stream()
        .filter(e -> e.getValue() == 1).map(e -> e.getKey()).collect(Collectors.toSet());
    // [end]
    Set<ConnectableElement> oneTargetProperties = targetEndRolesFrequency.entrySet().stream()
        .filter(e -> e.getValue() == 1).map(e -> e.getKey()).collect(Collectors.toSet());


    for (EList<Element> oneOfSet : oneOfSets) {
      handleOneOfConnectors(oneOfSet, sigOfNamedElement, oneOfConnectors, oneSourceProperties,
          oneTargetProperties);
    }
    //
    // end of handling one of connectors
    //

    // process remaining of connectors
    for (org.eclipse.uml2.uml.Connector cn : connectors) {
      if (oneOfConnectors.contains(cn))
        continue; // oneof connectors are already handled so skip here
      if (ne.getInheritedMembers().contains(cn))
        continue;// ignore inherited

      // while translating IFSingleFoolService and processing connectors for IFFoodService,
      // connectors creating "transferPrepareServe" and "transferServeEat" should be ignored because
      // it they are redefined in IFSingleFoodService
      if (this.redefinedConnectors.contains(cn))
        continue; // ignore


      CONNECTOR_TYPE connector_type = null;
      edu.umd.omgutil.uml.Element omgE = sysmladapter.mapObject(cn);
      if (omgE instanceof edu.umd.omgutil.uml.Connector) {
        edu.umd.omgutil.uml.Connector omgConnector = (edu.umd.omgutil.uml.Connector) omgE;

        edu.umd.omgutil.uml.Type owner = omgConnector.getFeaturingType();
        String source = null;
        String target = null;

        String sourceTypeName = null; // used in Transfer
        String targetTypeName = null;// used in Transfer
        boolean isBindingConnector = false;

        for (ConnectorEnd ce : ((Connector) cn).getEnds()) {

          if (ce.getDefiningEnd() == null) {
            if (cn.getAppliedStereotype(STEREOTYPE_BINDDINGCONNECTOR) != null) {
              if (isBindingConnector == false) {
                source = ce.getRole().getLabel();
                isBindingConnector = true;
              } else {
                target = ce.getRole().getLabel();
                isBindingConnector = false;
              }
              if (source != null && target != null)
                toAlloy.addEqualFact(sigOfNamedElement, source, target);
            }
          } else {

            String definingEndName = ce.getDefiningEnd().getName();
            edu.umd.omgutil.uml.ConnectorEnd end =
                (edu.umd.omgutil.uml.ConnectorEnd) sysmladapter.mapObject(ce);
            List<String> endsFeatureNames = end.getCorrectedFeaturePath(owner).stream()
                .map(f -> f.getName()).collect(Collectors.toList());

            if (definingEndName.equals("happensAfter")) {
              connector_type = CONNECTOR_TYPE.HAPPENS_BEFORE;
              source = endsFeatureNames.get(0);
              // sourceCN = ce;
            } else if (definingEndName.equals("happensBefore")) {
              connector_type = CONNECTOR_TYPE.HAPPENS_BEFORE;
              target = endsFeatureNames.get(0);
              // targetCN = ce;
            } else if (definingEndName.equals("happensDuring-1")) {
              connector_type = CONNECTOR_TYPE.HAPPENS_DURING;
              source = endsFeatureNames.get(0);
            } else if (definingEndName.equals("happensDuring")) {
              connector_type = CONNECTOR_TYPE.HAPPENS_DURING;
              target = endsFeatureNames.get(0);
            } else if (definingEndName.equals("transferSource")) {
              connector_type = CONNECTOR_TYPE.TRANSFER;
              source = endsFeatureNames.get(0);
              sourceTypeName = ce.getRole().getType().getName();

            } else if (definingEndName.equals("transferTarget")) {
              connector_type = CONNECTOR_TYPE.TRANSFER;
              target = endsFeatureNames.get(0);
              targetTypeName = ce.getRole().getType().getName();
            }

            if (source == null || target == null)
              continue;

            Field sourceField = AlloyUtils.getFieldFromSigOrItsParents(source, sigOfNamedElement);
            Field targetField = AlloyUtils.getFieldFromSigOrItsParents(target, sigOfNamedElement);

            if (connector_type == CONNECTOR_TYPE.HAPPENS_BEFORE) {
              toAlloy.createBijectionFilteredHappensBefore(sigOfNamedElement, sourceField,
                  targetField);
            } else if (connector_type == CONNECTOR_TYPE.HAPPENS_DURING)
              toAlloy.createBijectionFilteredHappensDuring(sigOfNamedElement, sourceField,
                  targetField);

            else if (connector_type == CONNECTOR_TYPE.TRANSFER) {

              for (Connector redefinedConnector : cn.getRedefinedConnectors()) {
                for (ConnectorEnd cce : ((Connector) redefinedConnector).getEnds()) {

                  if (cce.getDefiningEnd().getName().equals("transferSource")
                      || cce.getDefiningEnd().getName().equals("transferTarget")) {
                    // while processing child sig connectors, find parent connector that child is
                    // redefining.
                    redefinedConnectors.add(redefinedConnector);
                    break;
                  }
                }
              }
              Association type = cn.getType();
              List<Set<String>> sourceOutputAndTargetInputProperties =
                  handleTransferAndTransferBeforeInputsAndOutputs(cn);
              sigWithTransferFields.add(sigOfNamedElement);

              Utils.addToHashMap(inputs, targetTypeName,
                  sourceOutputAndTargetInputProperties.get(1)); // "targetInputProperty"
              Utils.addToHashMap(outputs, sourceTypeName,
                  sourceOutputAndTargetInputProperties.get(0));// "sourceOutputProperty",

              boolean addEquals = false;
              if (targetTypeName.equals(sourceTypeName)) { // ie., targetTypeName = sourceTypeName
                                                           // is "BehaviorWithParemeter" for 4.1.5
                                                           // Multiple Execution Steps2 - Multiple
                                                           // Object Flow
                sigNameWithTransferConnectorWithSameInputOutputFieldType.add(targetTypeName);
                addEquals = true;
              }

              boolean toBeInherited = false;


              // only leafSig
              List<Set<Field>> targetInputsSourceOutputsFields = null;
              if (leafSigs.contains(sigOfNamedElement)) {
                targetInputsSourceOutputsFields =
                    processConnectorInputsOutputs(sigOfNamedElement, sourceField, targetField,
                        sourceTypeName,
                        targetTypeName, sourceOutputAndTargetInputProperties, fieldsWithInputs,
                        fieldsWithOutputs, addEquals);

              } else { // non leaf
                targetInputsSourceOutputsFields = processConnectorInputsOutputs(sigOfNamedElement,
                    sourceField, targetField, sourceTypeName, targetTypeName,
                    sourceOutputAndTargetInputProperties, null, null, addEquals);
                // findInputAndOutputsFields(sigOfNamedElement, source, target, sourceTypeName,
                // targetTypeName, sourceOutputAndTargetInputProperties);
                toBeInherited = true;
              }
              if (type.getName().equals("Transfer")) {
                Sig.Field transferField =
                    handTransferFieldAndFnPrep(sigOfNamedElement, source, target);
                addToSigToFactsMap(sigOfNamedElement.label,
                    toAlloy.addTransferFacts(sigOfNamedElement, transferField, source, target,
                        targetInputsSourceOutputsFields, toBeInherited));
              } else if (type.getName().equals("TransferBefore")) {
                Sig.Field transferField =
                    handTransferFieldAndFnPrep(sigOfNamedElement, source, target);
                addToSigToFactsMap(sigOfNamedElement.label,
                    toAlloy.addTransferBeforeFacts(sigOfNamedElement, transferField, source, target,
                        targetInputsSourceOutputsFields, toBeInherited));
              }
            } // end of connector_type == CONNECTOR_TYPE.TRANSFER
          }
        } // end of connectorEnd
      } // end of Connector
    } // org.eclipse.uml2.uml.Connector
    return sigNameWithTransferConnectorWithSameInputOutputFieldType;
  }



  /**
   * adding facts inputs and outputs for bijection like below for leaf sig 1) if addequal is true - fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs} 2) - fact {all x: IFSingleFoodService
   * | bijectionFiltered[outputs, x.order, x.order.orderedFoodItem]}
   * 
   * @param sig
   * @param source in the connector
   * @param target in the connector
   * @param sourceTypeName type of connector source(output)
   * @param targetTypeName type of connector target(input)
   * @param sourceOutputAndTargetInputProperties
   * @param fieldsWithInputs null or non-leaf sig
   * @param fieldsWithOutputs null or non-leaf sig
   * @return List<Set<Field>> [0] = targetInputFields [1] = sourceOutputFields
   */
  private List<Set<Field>> processConnectorInputsOutputs(PrimSig sig, Field sourceField,
      Field targetField,
      String sourceTypeName, String targetTypeName,
      List<Set<String>> sourceOutputAndTargetInputProperties,
      Map<Field, Set<Field>> fieldsWithInputs, Map<Field, Set<Field>> fieldsWithOutputs,
      boolean addEquals) {

    // if addEquals are true add the fact like below:
    // targetTypeName = sourceTypeName (ie., BehaviorWithParameter)
    // source => outputs
    // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}

    // p1, order(sigOfNamedElement= IFSIngleFoodService)

    Set<Field> addOutputToFields = new HashSet<>();
    Set<Field> addInputToFields = new HashSet<>();
    if (sourceField != null) {
      PrimSig typeSig = toAlloy.getSig(sourceTypeName);// sourceTypeName =IFCustomerOrder

      for (String sourceOutput : sourceOutputAndTargetInputProperties.get(0)) {
        // orderedFoodItem
        Field outputTo = AlloyUtils.getFieldFromSigOrItsParents(
            // [orderFoodItem, prepareFoodItem],
            sourceOutput, typeSig);// i
        // fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p1, x.p1.i]}
        if (!parameterFields.contains(outputTo))
          addOutputToFields.add(outputTo);

        // only for leaf-sig
        if (fieldsWithOutputs != null
            && !AlloyUtils.containsBothKeyAndValue(fieldsWithOutputs, sourceField, outputTo)) {
          fieldsWithOutputs.computeIfAbsent(sourceField, v -> new HashSet<Field>()).add(outputTo);
          // addToHashMap(fieldsWithOutputs, sourceField, outputTo);
          toAlloy.createBijectionFilteredOutputs(sig, sourceField, sourceField.join(outputTo));
          if (addEquals)
            toAlloy.createInField(sig, sourceField, sourceField.join(outputTo),
                outputTo, Alloy.ooutputs);

        }
      }
    }

    // target => inputs
    // fact {all x: MultipleObjectFlow | all p: x.p2 | p.i = p.inputs}


    if (targetField != null) {
      PrimSig typeSig = toAlloy.getSig(targetTypeName);// IFCustomPrepare
      for (String targetInputProperties : sourceOutputAndTargetInputProperties.get(1)) {
        Field inputTo = AlloyUtils.getFieldFromSigOrItsParents(targetInputProperties, // i
            typeSig);
        if (!parameterFields.contains(inputTo))
          addInputToFields.add(inputTo);
        // fact {all x: MultipleObjectFlow | bijectionFiltered[inputs, x.p2, x.p2.i]}
        // fact {all x: IFSingleFoodService | bijectionFiltered[inputs, x.prepare,
        // x.prepare.preparedFoodItem]}
        /* addToSigToFactsMap(sigOfNamedElement.label, */
        // only for leaf sig
        if (fieldsWithInputs != null
            && !AlloyUtils.containsBothKeyAndValue(fieldsWithInputs, targetField, inputTo)) {
          // addToHashMap(fieldsWithInputs, targetField, inputTo);
          fieldsWithInputs.computeIfAbsent(targetField, v -> new HashSet<Field>()).add(inputTo);
          toAlloy.createBijectionFilteredInputs(sig, targetField, targetField.join(inputTo));
          if (addEquals) {
            toAlloy.createInField(sig, targetField, targetField.join(inputTo),
                inputTo, Alloy.oinputs);// );
          }
        }
      }
    }
    return List.of(addOutputToFields, addInputToFields);
  }

  private Sig.Field handTransferFieldAndFnPrep(PrimSig sig, String source, String target) {
    String fieldName = "transfer" + Utils.firstCharUpper(source) + Utils.firstCharUpper(target);
    // adding transferFields in stepProperties
    stepPropertiesBySig.get(sig.label).add(fieldName);
    addSigToTransferFieldsMap(sig, fieldName);
    Sig.Field transferField = AlloyUtils.addTransferField(fieldName, sig);
    return transferField;
  }

  /**
   * 
   * @param sig
   * @param fieldName
   */
  private void addSigToTransferFieldsMap(PrimSig sig, String fieldName) {
    Set<String> tFields;
    if ((tFields = sigToTransferFieldMap.get(sig.label)) == null) {
      tFields = new HashSet<>();
      tFields.add(fieldName);
      sigToTransferFieldMap.put(sig.label, tFields);
    } else
      tFields.add(fieldName);
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
   * Add bijection happensBefore or function and inverseFunction happensBefore facts.
   * 
   * 
   * For example, oneOfSet is [order, end], with 3 one of connectors [c1 [eat,order],c2 [eat, end], c3[start, order]], c1 and c2 are the connector to be get information from. Then sources = [eat, eat]
   * and targets = [order, end]. Since end is in the given oneTargetProperties and target-side has oneOf constraint, two facts: functionFiltered[happensBefore, eat, end+order} and
   * inverseFunctionFiltered[happensBefore, eat, end] are added (order <-- eat --> end).
   * 
   * 
   * @param oneOfSet List of ConnectableElement both having oneOf constraint. Both should be source-side or target-side.
   * @param ownerSig the owner sig of one of connectors
   * @param oneOfConnectors the one of connectors if the sig .
   * @param oneSourceProperties list of connectableElements of the sig's one of connectors which has only one outgoing.
   * @param oneTargetProperties list of connectableElements of the sig's one of connectors which has only one incoming.
   */
  private void handleOneOfConnectors(List<Element> oneOfSet, PrimSig ownerSig,
      Set<Connector> oneOfConnectors,
      Set<ConnectableElement> oneSourceProperties,
      Set<ConnectableElement> oneTargetProperties) {

    List<ConnectableElement> sources = new ArrayList<>();
    List<ConnectableElement> targets = new ArrayList<>();

    boolean isSourceSideOneOf = false;
    for (org.eclipse.uml2.uml.Connector cn : oneOfConnectors) {
      for (ConnectorEnd ce : cn.getEnds()) {
        Optional<Element> found = oneOfSet.stream().filter(e -> e == ce).findFirst();
        if (!found.isEmpty()) {
          List<ConnectableElement> ces = MDUtils.getEndRolesForCEFirst(cn, ce);
          if (ces == null) { // this should not happens
            this.messages.add("A connector " + cn.getQualifiedName()
                + " does not have two connector ends, so ignored.");
            return;
          }
          String definingEndName = ce.getDefiningEnd().getName();
          if (definingEndName.equals("happensAfter")) {
            isSourceSideOneOf = true; // source-sides is oneOf
            sources.add(ces.get(0));
            targets.add(ces.get(1));


          } else if (definingEndName.equals("happensBefore")) {
            isSourceSideOneOf = false; // target-side is oneOf
            sources.add(ces.get(1));// [eat, eat]
            targets.add(ces.get(0)); // [order,end]
          }
        }
      }
    }

    Expr beforeExpr = null;
    Expr afterExpr = null;
    if (isSourceSideOneOf) { // sourceSide needs to be combined
      afterExpr = AlloyUtils.getFieldFromSigOrItsParents(targets.get(0).getName(), ownerSig);
      List<String> sourceNames = // sorting source names alphabetically = how to be write out
          sources.stream().map(e -> e.getName()).sorted().collect(Collectors.toList());
      for (String sourceName : sourceNames) {
        beforeExpr = beforeExpr == null
            ? AlloyUtils.getFieldFromSigOrItsParents(sourceName,
                ownerSig)
            : beforeExpr.plus(AlloyUtils
                .getFieldFromSigOrItsParents(sourceName, ownerSig));
      }

    } else { // targetSide needs to be combined
      List<String> targetNames = // sorting target names alphabetically = to be write out
          targets.stream().map(e -> e.getName()).sorted().collect(Collectors.toList());
      for (String targetName : targetNames) {
        afterExpr = afterExpr == null
            ? AlloyUtils.getFieldFromSigOrItsParents(targetName,
                ownerSig)
            : afterExpr.plus(AlloyUtils
                .getFieldFromSigOrItsParents(targetName, ownerSig));
      }
      beforeExpr = AlloyUtils
          .getFieldFromSigOrItsParents(sources.get(0).getName(), ownerSig);
    }

    if (isSourceSideOneOf) {
      boolean allSourceOneOf = true;
      if (oneSourceProperties.size() == sources.size()) {
        for (ConnectableElement ce : oneSourceProperties) {
          if (!sources.contains(ce)) {
            allSourceOneOf = false;
            break;
          }
        }
      } else
        allSourceOneOf = false;

      // if both are one sourceProperties
      if (allSourceOneOf) { // merge a + b -> c
        toAlloy.createBijectionFilteredHappensBefore(ownerSig, beforeExpr,
            afterExpr);
      } else {
        for (ConnectableElement ce : oneSourceProperties) {
          // need fn a -> b or a ->c
          Expr beforeExpr_modified = AlloyUtils.getFieldFromSigOrItsParents(ce.getName(),
              ownerSig); // start
          toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig,
              beforeExpr_modified,
              afterExpr);// order
        }
        // inverse fn a + b -> c
        // fact {all x: OFControlLoopFoodService | inverseFunctionFiltered[happensBefore, x.a + x.b, x.c]}
        toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr, // a + b
            afterExpr);// order
      }
    } else { // targetSide has OneOf

      boolean allTargetOneOf = true;
      if (oneTargetProperties.size() == targets.size()) {
        for (ConnectableElement ce : oneTargetProperties) {
          if (!targets.contains(ce)) {
            allTargetOneOf = false;
            break;
          }
        }
      } else
        allTargetOneOf = false;

      // if both are one targetProperties
      if (allTargetOneOf) { // decision a -> b + c
        toAlloy.createBijectionFilteredHappensBefore(ownerSig, beforeExpr,
            afterExpr);
      } else {
        // fn a -> b + c
        // fact {all x: OFControlLoopFoodService | functionFiltered[happensBefore, x.a, x.b + x.c]}
        toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr,
            afterExpr);

        // inversefn a -> b or a -> c
        for (ConnectableElement ce : oneTargetProperties) {
          Expr afterExpr_modified = AlloyUtils.getFieldFromSigOrItsParents(ce.getName(),
              ownerSig);// end
          toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig,
              beforeExpr,
              afterExpr_modified);
        }
      }
    }

  }

  /**
   * Find sourceOutputProperty and targetInputProperty of the given connector with <<ItemFlow>> or <<ObjectFlow>> Stereotype
   * 
   * @param cn - a connector having the properties
   * @return List<Set<String>> 1st in the list is sourceOutputProperty names and 2nd in the list is the targetInputProperty names
   */
  private List<Set<String>> handleTransferAndTransferBeforeInputsAndOutputs(
      org.eclipse.uml2.uml.Connector cn) {


    List<Set<String>> sourceOutputAndTargetInputProperties = new ArrayList<>();
    String[] stTagNames = {"sourceOutputProperty", "targetInputProperty"};// , "itemType"}; this
                                                                          // property value is class
                                                                          // not property
    Map<String, List<Property>> stTagItemFlowValues =
        getStreotypePropertyValues(cn, STEREOTYPE_ITEMFLOW, stTagNames);
    Map<String, List<Property>> stTagObjectFlowValues =
        getStreotypePropertyValues(cn, STEREOTYPE_OBJECTFLOW, stTagNames);

    List<Property> sos = null;
    List<Property> tis = null;

    if (stTagObjectFlowValues != null) { // ObjectFlow
      sos = stTagObjectFlowValues.get(stTagNames[0]); // sourceOutputProperty = x.outputs
      tis = stTagObjectFlowValues.get(stTagNames[1]); // targetInputProperty = x.inputs
    }

    if (stTagItemFlowValues != null) { // ItemFlow
      sos = stTagItemFlowValues.get(stTagNames[0]); // sourceOutputProperty - name is suppliedProduct
      tis = stTagItemFlowValues.get(stTagNames[1]); // targetInputProperty - name is "receivedProduct"
    }
    Class sosOwner = null;
    Class tisOwner = null;
    Set<String> sourceOutput = new HashSet<>();
    Set<String> targetInput = new HashSet<>();
    if (sos != null && tis != null) {
      for (Property p : sos) {
        sosOwner = ((org.eclipse.uml2.uml.Class) p.getOwner());
        sourceOutput.add(p.getName()); // p.getName() is vout. so create {B2(owner) | x.vout= x.output}
        transferingTypeSig.add(p.getType().getName());
      }
      for (Property p : tis) {
        tisOwner = ((org.eclipse.uml2.uml.Class) p.getOwner());
        transferingTypeSig.add(p.getType().getName());
        targetInput.add(p.getName()); // = x.inputs
      }
      // 4.1.4 Transfers and Parameters
      // preventing fact {all x:B| x.vin = x.output}} to be generated from <<ItemFlow>> between
      // B.vin and b1(B1).vin - b1 is a field of B
      // but having fact {all x: B1| x.vin = x.inputs} is ok
      EList<Property> atts = sosOwner.getAttributes();
      for (Property att : atts) // b1
        if (att.getType() == tisOwner) { // b1.getType() == B1 and tisOwner == B1
          sourceOutput = null;
          break;
        }
      // preventing fact {all x: B |x.vout = x.inputs} - to be generated from <<ItemFlow>> between
      // B.vout and b2(B2).vout - b2 is a field of B
      // but having fact {all x: B2|x.vout = x.outputs} is ok
      atts = tisOwner.getAttributes(); // tisOwner is B
      for (Property att : atts) // b2
        if (att.getType() == sosOwner) { // b2.getType() == B2 and sosOwner == B2
          targetInput = null;
          break;
        }
    }
    sourceOutputAndTargetInputProperties.add(sourceOutput);
    sourceOutputAndTargetInputProperties.add(targetInput);
    return sourceOutputAndTargetInputProperties;
  }

  /**
   * Find a stereotype of element of the given streotypeName and return map of its tagName(string) and values(Properties)
   *
   * @param element - element whose stereotype properties to be found
   * @param streotypeName - stereotype name in string
   * @param tagNames -stereotype property names
   * @return Map (key = tag/property name string, value = properties) or null if the element does not have stereotype applied.
   */
  private Map<String, List<Property>> getStreotypePropertyValues(Element element,
      String streotypeName, String[] tagNames) {

    Map<String, List<Property>> propertysByTagNames = new HashMap<>();
    Stereotype st = null;
    if ((st = element.getAppliedStereotype(streotypeName)) != null) {
      for (String propertyName : tagNames) {
        List<Property> results = new ArrayList<>();
        Object pObject = (element.getValue(st, propertyName));
        if (pObject instanceof List) {
          @SuppressWarnings("unchecked")
          List<Object> properties = (List<Object>) pObject;
          for (Object property : properties) {
            if (property instanceof Property) {
              results.add((Property) property);
            } else {
              this.messages.add(
                  propertyName + " is not an instance of Property but "
                      + property.getClass().getSimpleName() + ". so ignored.");
            }
          }
          propertysByTagNames.put(propertyName, results);
        }
      }
      return propertysByTagNames;
    }
    return null;
  }

  /**
   * Get two rules (ConnectorEnds) of each oneof constraint and return as set.
   * 
   * @param cs a set of Constraints
   * @return set of oneof constraint's two rules (ConnectorEnd)
   */
  private Set<EList<Element>> getOneOfRules(Set<Constraint> cs) {
    Set<EList<Element>> oneOfSet = new HashSet<>();
    for (Constraint c : cs) {
      ValueSpecification vs = c.getSpecification();
      if (vs instanceof OpaqueExpressionImpl) {
        edu.umd.omgutil.uml.OpaqueExpression omgE = (OpaqueExpression) sysmladapter.mapObject(vs);
        if (omgE.getBodies().contains("OneOf")) {
          EList<Element> es = c.getConstrainedElements(); // list of connectorEnds
          oneOfSet.add(es);
        }
      }
    }
    return oneOfSet;
  }



  // private static void addToHashMap(Map<Field, Set<Field>> map, Field key, Field value) {
  // map.computeIfAbsent(key, k -> new HashSet<Field>()).add(value);
  //
  // // if (map.containsKey(key))
  // // map.get(key).add(value);
  // // else {
  // // Set<Field> vs = new HashSet<Field>();
  // // vs.add(value);
  // // map.put(key, vs);
  // // }
  // }



  /**
   * Add to sigToFactsMap instance variable if facts is not null
   * 
   * @param sigName - the signature name that is a key of sigToFactsMap
   * @param facts - Set of Expr facts to be added to sigToFactsMap
   */
  private void addToSigToFactsMap(String sigName, Set<Expr> facts) {
    if (facts == null)
      return;
    sigToFactsMap.computeIfAbsent(sigName, v -> new HashSet<Expr>()).addAll(facts);

    // Set<Expr> allFacts = sigToFactsMap.get(sigName);
    // if (allFacts == null)
    // sigToFactsMap.put(sigName, facts);
    // else
    // allFacts.addAll(facts);
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


