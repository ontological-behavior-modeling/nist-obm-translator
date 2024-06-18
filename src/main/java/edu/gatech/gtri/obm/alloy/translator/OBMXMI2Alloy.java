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
  // Set<String> transferingTypeSig;

  /**
   * A map where key is sig name string and value is a set of field name strings. This includes inherited fields/properties and used in closure facts.
   */
  Map<String, Set<String>> stepPropertiesBySig;

  /**
   * A set of connectors redefined by children so that the connectors are ignored by the parent.
   */
  // Set<Connector> redefinedConnectors;

  /**
   * A dictionary contains signature name as key and a set of transfer field names as value.
   */
  // Map<String, Set<String>> sigToTransferFieldMap;

  /**
   * A dictionary contains signature name as key and a set of fact expression as value.
   */
  // Map<String, Set<Expr>> sigToFactsMap;

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
    // redefinedConnectors = new HashSet<Connector>();
    stepPropertiesBySig = new HashMap<>();
    // sigToTransferFieldMap = new HashMap<>();
    // sigToFactsMap = new HashMap<>();
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

    ConnectorHandler connectorHandler =
        new ConnectorHandler(sysmladapter, this.sysMLUtil, /* redefinedConnectors, */ leafSigs, this.toAlloy,
            /* this.sigToFactsMap, */ this.parameterFields, /* this.sigToTransferFieldMap, */
            this.stepPropertiesBySig);
    connectorHandler.process(classInHierarchy, allClassesConnectedToMainSigByFields);

    Set<Sig> sigWithTransferFields = connectorHandler.getSigWithTransferFields();
    Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType =
        connectorHandler.getSigNameWithTransferConnectorWithSameInputOutputFieldType();
    HashMap<String, Set<String>> connectorTargetInputPropertyNamesByClassName =
        connectorHandler.getConnectorTargetInputPropertyNamesByClassName();
    HashMap<String, Set<String>> connectorSourceOutputPrpertyNamesByClassName =
        connectorHandler.getConnectorSourceOutputPrpertyNamesByClassName();

    Set<String> transferingTypeSig = connectorHandler.getTransferingTypeSig();
    Map<String, Set<String>> sigToTransferFieldMap = connectorHandler.getSigToTransferFieldMap();
    Map<String, Set<Expr>> sigToFactsMap = connectorHandler.getSigToFactsMap();


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
   * 
   * 
   * @param ne - a namedElement transfer to a signature.
   * @param outConnectorInputPropertiesBySig - a map data to be collected where values are connectors' targetInputProperty tag values (property names) and key the property owner (namedElement) name
   * @param outconnectorOutputPropertiesBySig - a map data to be collected where values are connectors' sourceOutputProperty tag values (property names) and key of the property owner (namedElement) name
   * @param outSigWithTransferFields - a set of signatures having transfer fields collected
   * @return a set of signature string names having transfer field(s) of the same input and output types
   * 
   */
  /*
   * private void handleClassForProecessConnector( ProcessConnectorMethod pcm) {
   * 
   * // HashMap<String, Set<String>> outConnectorInputPropertiesBySig, // HashMap<String, Set<String>> outconnectorOutputPropertiesBySig, // Set<Sig> outSigWithTransferFields) {
   * 
   * // PrimSig ownerSig = toAlloy.getSig(ne.getName());
   * 
   * // Map<Field, Set<Field>> fieldWithInputs = new HashMap<>(); // key = prepare, value= // [preparedFoodItem,prepareDestination] // Map<Field, Set<Field>> fieldWithOutputs = new HashMap<>(); // key =
   * order, // value=[orderAmount, // orderDestination, // orderedFoodItem]
   * 
   * 
   * // transfer fields created is adding to stepPropertiesBySig passing in processConnector // Signature names which has at least one transfer connector having the same field type of
   * sourceOutputProperty and targetInputProperty's BehaviorWithParameter // Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType = // processConnector((Class) ne,
   * outConnectorInputPropertiesBySig, // outconnectorOutputPropertiesBySig, // outSigWithTransferFields, fieldWithInputs, fieldWithOutputs);
   * 
   * pcm.processConnector(sysmladapter, sysMLUtil);
   * 
   * PrimSig ownerSig = pcm.getSigOfNamedElement();
   * 
   * Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType = pcm.getSigNameWithTransferConnectorWithSameInputOutputFieldType();
   * 
   * Map<Field, Set<Field>> fieldWithInputs = pcm.getFieldWithInputs(); Map<Field, Set<Field>> fieldWithOutputs = pcm.getFieldWithOutputs();
   * 
   * // any of connector ends owned field types are the same (p1, p2: // BehaviorWithParameter) // 4.1.5 Multiple Execution Step2 = MultiplObjectFlow=[BehaviorWithParameter] if
   * (sigNameWithTransferConnectorWithSameInputOutputFieldType.size() > 0) { Set<String> nonTransferFieldNames = stepPropertiesBySig.get(ownerSig.label).stream() .filter(f ->
   * !f.startsWith("transfer")).collect(Collectors.toSet()); // no inputs for (String fieldName : nonTransferFieldNames) { if (!AlloyUtils.fieldsLabels(fieldWithInputs.keySet()).contains(fieldName)) {
   * // fact {all x: MultipleObjectFlow |no x.p1.inputs} toAlloy.addNoInputsOrOutputsFieldFact(ownerSig, fieldName, Alloy.oinputs); } if
   * (!AlloyUtils.fieldsLabels(fieldWithOutputs.keySet()).contains(fieldName)) { toAlloy.addNoInputsOrOutputsFieldFact(ownerSig, fieldName, Alloy.ooutputs); } }
   * 
   * } // 4/10 // fact {all x: OFSingleFoodService | x.prepare.inputs in x.prepare.preparedFoodItem + // x.prepare.prepareDestination} for (Field field : fieldWithInputs.keySet()) { // The following
   * fact is NOT included because x.p1.vout is <<Parmeter>> field // fact {all x: MultipleObjectFlowAlt | x.p1.outputs in x.p1.vout} Set<Field> fields =
   * removeParameterFields(fieldWithInputs.get(field)); if (fields.size() > 0) toAlloy.addInOutClosureFact(ownerSig, field, fields, Alloy.oinputs); } for (Field field : fieldWithOutputs.keySet()) {
   * Set<Field> fields = removeParameterFields(fieldWithOutputs.get(field)); if (fields.size() > 0) toAlloy.addInOutClosureFact(ownerSig, field, fields, Alloy.ooutputs); }
   * 
   * 
   * 
   * }
   */

  /**
   * Remove fields with Parameter stereotype from the given fields.
   * 
   * @param original - the set of fields before removing Parameter stereotype removed.
   * @return set of fields after removing Parameter streotype fields
   */
  // private Set<Field> removeParameterFields(Set<Field> original) {
  // return original.stream().filter(f -> !parameterFields.contains(f)).collect(Collectors.toSet());
  // }

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
   * @param connectorInputPropertiesBySig
   * @param connectorOutputPropertiesBySig
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
  // private Set<String> processConnector(Class ne,
  // HashMap<String, Set<String>> connectorInputPropertiesBySig,
  // HashMap<String, Set<String>> connectorOutputPropertiesBySig, Set<Sig> sigWithTransferFields,
  // Map<Field, Set<Field>> fieldsWithInputs, Map<Field, Set<Field>> fieldsWithOutputs) {
  //
  // Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType = new HashSet<>();
  // PrimSig sigOfNamedElement = toAlloy.getSig(ne.getName());
  //
  // //
  // // start handling one of connectors
  // //
  // // find sig's constraint
  // Set<Constraint> constraints = sysMLUtil.getAllRules(ne);
  // Set<EList<Element>> oneOfSets = SysMLAdapterUtils.getOneOfRules(sysmladapter, constraints); // EList<ConnectorEnd> [ [start, eat] or [order, end]]
  //
  // Set<org.eclipse.uml2.uml.Connector> connectors = sysMLUtil.getOwnedConnectors(ne);
  //
  // // finding connectors with oneof
  // Set<Connector> oneOfConnectors = new HashSet<>();
  // for (Connector cn : connectors) {
  // for (ConnectorEnd ce : cn.getEnds()) {
  // for (EList<Element> oneOfSet : oneOfSets) {
  // Optional<Element> found = oneOfSet.stream().filter(e -> e == ce).findFirst();
  // if (!found.isEmpty()) {
  // oneOfConnectors.add(cn);
  // }
  // }
  // }
  // }
  //
  // ConnectableElement s, t;
  // Map<ConnectableElement, Integer> sourceEndRolesFrequency = new HashMap<>(); // [eat, 2], [start, 1]
  // Map<ConnectableElement, Integer> targetEndRolesFrequency = new HashMap<>(); // [order, 2],[end, 1]
  // for (Connector oneOfConnector : oneOfConnectors) {
  // EList<ConnectorEnd> cends = oneOfConnector.getEnds();
  // s = cends.get(0).getRole();
  // t = cends.get(1).getRole();
  // Integer sFreq = sourceEndRolesFrequency.get(s);
  // sourceEndRolesFrequency.put(s, sFreq == null ? 1 : sFreq + 1);
  // Integer tFreq = targetEndRolesFrequency.get(t);
  // targetEndRolesFrequency.put(t, tFreq == null ? 1 : tFreq + 1);
  // }
  // // [start]
  // Set<ConnectableElement> oneSourceProperties = sourceEndRolesFrequency.entrySet().stream()
  // .filter(e -> e.getValue() == 1).map(e -> e.getKey()).collect(Collectors.toSet());
  // // [end]
  // Set<ConnectableElement> oneTargetProperties = targetEndRolesFrequency.entrySet().stream()
  // .filter(e -> e.getValue() == 1).map(e -> e.getKey()).collect(Collectors.toSet());
  //
  //
  // for (EList<Element> oneOfSet : oneOfSets) {
  // handleOneOfConnectors(oneOfSet, sigOfNamedElement, oneOfConnectors, oneSourceProperties,
  // oneTargetProperties);
  // }
  // //
  // // end of handling one of connectors
  // //
  //
  // // process remaining of connectors
  // for (org.eclipse.uml2.uml.Connector cn : connectors) {
  // if (oneOfConnectors.contains(cn))
  // continue; // oneof connectors are already handled so skip here
  // if (ne.getInheritedMembers().contains(cn))
  // continue;// ignore inherited
  //
  // // while translating IFSingleFoolService and processing connectors for IFFoodService,
  // // connectors creating "transferPrepareServe" and "transferServeEat" should be ignored because
  // // it they are redefined in IFSingleFoodService
  // if (this.redefinedConnectors.contains(cn))
  // continue; // ignore
  //
  //
  // CONNECTOR_TYPE connector_type = null;
  // edu.umd.omgutil.uml.Element omgE = sysmladapter.mapObject(cn);
  // if (omgE instanceof edu.umd.omgutil.uml.Connector) {
  // edu.umd.omgutil.uml.Connector omgConnector = (edu.umd.omgutil.uml.Connector) omgE;
  //
  // edu.umd.omgutil.uml.Type owner = omgConnector.getFeaturingType();
  // String source = null;
  // String target = null;
  //
  // String sourceTypeName = null; // used in Transfer
  // String targetTypeName = null;// used in Transfer
  // boolean isBindingConnector = false;
  //
  // for (ConnectorEnd ce : ((Connector) cn).getEnds()) {
  //
  // if (ce.getDefiningEnd() == null) {
  // if (cn.getAppliedStereotype(STEREOTYPE_BINDDINGCONNECTOR) != null) {
  // if (isBindingConnector == false) {
  // source = ce.getRole().getLabel();
  // isBindingConnector = true;
  // } else {
  // target = ce.getRole().getLabel();
  // isBindingConnector = false;
  // }
  // if (source != null && target != null)
  // toAlloy.addEqualFact(sigOfNamedElement, source, target);
  // }
  // } else {
  //
  // String definingEndName = ce.getDefiningEnd().getName();
  // edu.umd.omgutil.uml.ConnectorEnd end =
  // (edu.umd.omgutil.uml.ConnectorEnd) sysmladapter.mapObject(ce);
  // List<String> endsFeatureNames = end.getCorrectedFeaturePath(owner).stream()
  // .map(f -> f.getName()).collect(Collectors.toList());
  //
  // if (definingEndName.equals("happensAfter")) {
  // connector_type = CONNECTOR_TYPE.HAPPENS_BEFORE;
  // source = endsFeatureNames.get(0);
  // // sourceCN = ce;
  // } else if (definingEndName.equals("happensBefore")) {
  // connector_type = CONNECTOR_TYPE.HAPPENS_BEFORE;
  // target = endsFeatureNames.get(0);
  // // targetCN = ce;
  // } else if (definingEndName.equals("happensDuring-1")) {
  // connector_type = CONNECTOR_TYPE.HAPPENS_DURING;
  // source = endsFeatureNames.get(0);
  // } else if (definingEndName.equals("happensDuring")) {
  // connector_type = CONNECTOR_TYPE.HAPPENS_DURING;
  // target = endsFeatureNames.get(0);
  // } else if (definingEndName.equals("transferSource")) {
  // connector_type = CONNECTOR_TYPE.TRANSFER;
  // source = endsFeatureNames.get(0);
  // sourceTypeName = ce.getRole().getType().getName();
  //
  // } else if (definingEndName.equals("transferTarget")) {
  // connector_type = CONNECTOR_TYPE.TRANSFER;
  // target = endsFeatureNames.get(0);
  // targetTypeName = ce.getRole().getType().getName();
  // }
  //
  // if (source == null || target == null)
  // continue;
  //
  // Field sourceField = AlloyUtils.getFieldFromSigOrItsParents(source, sigOfNamedElement);
  // Field targetField = AlloyUtils.getFieldFromSigOrItsParents(target, sigOfNamedElement);
  //
  // if (connector_type == CONNECTOR_TYPE.HAPPENS_BEFORE) {
  // toAlloy.createBijectionFilteredHappensBefore(sigOfNamedElement, sourceField,
  // targetField);
  // } else if (connector_type == CONNECTOR_TYPE.HAPPENS_DURING)
  // toAlloy.createBijectionFilteredHappensDuring(sigOfNamedElement, sourceField,
  // targetField);
  //
  // else if (connector_type == CONNECTOR_TYPE.TRANSFER) {
  // handleTransferConnector(cn, sigOfNamedElement, connectorInputPropertiesBySig,
  // connectorOutputPropertiesBySig, sigWithTransferFields, sourceTypeName,
  // targetTypeName,
  //
  // sigNameWithTransferConnectorWithSameInputOutputFieldType, sourceField,
  // targetField, source, target, fieldsWithInputs,
  // fieldsWithOutputs);
  // }
  // }
  // } // end of connectorEnd
  // } // end of Connector
  // } // org.eclipse.uml2.uml.Connector
  // return sigNameWithTransferConnectorWithSameInputOutputFieldType;
  // }
  //



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


