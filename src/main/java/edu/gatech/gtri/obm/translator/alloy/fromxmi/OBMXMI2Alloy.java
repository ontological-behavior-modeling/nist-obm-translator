package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.MDUtils;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.EMFUtil;
import edu.umd.omgutil.UMLModelErrorException;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;
import edu.umd.omgutil.uml.OpaqueExpression;
import java.io.File;
import java.io.FileNotFoundException;
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
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.internal.impl.OpaqueExpressionImpl;

public class OBMXMI2Alloy {

  ToAlloy toAlloy;
  SysMLAdapter sysmladapter;
  SysMLUtil sysMLUtil; // omgutil created from ResourceSet used through out the translator
  Set<Field> parameterFields; // A set of Field mapped from a Property with <<Parameter>> stereotype
  // Each connector with <<ObjectFlow>>, get sourceOutputProperty and targetInputProperty,
  // find the property type name and put in this Set
  Set<Field> valueTypeFields;
  Set<String> transferingTypeSig; // ie., [Integer] for Model::Basic::MultipleObjectFlowAlt
  String errorMessage = "";

  // key = sigName, value = field names
  Map<String, Set<String>> stepPropertiesBySig_all; // including inherited properties - used in
  // closure
  // (x.steps in ....)
  Map<String, Set<String>> stepPropertiesBySig; // not include inherited properties - used in all
  // (.....
  // in
  // x.steps)
  Set<Connector> redefinedConnectors; // Set of Connector used in processConnector method so
  // connector not to be processed by parents
  Map<String, Set<String>> sigToTransferFieldMap; // key = signame of parent having transfer fields,
  // value = Set<String> transferfields names
  Map<String, Set<Expr>> sigToFactsMap;
  Map<PrimSig, Set<Property>> redefinedPropertiesBySig;
  Set<PrimSig> leafSigs;

  private static String STEREOTYPE_STEP = "Model::OBM::Step";
  private static String STEREOTYPE_PATICIPANT = "SysML::ParticipantProperty";
  public static String STEREOTYPE_PAREMETER = "Model::OBM::Parameter"; // property
  public static String STEREOTYPE_VALUETYPE = "SysML::Blocks::ValueType";
  private static String STEREOTYPE_ITEMFLOW = "Model::OBM::ItemFlow";
  private static String STEREOTYPE_OBJECTFLOW = "Model::OBM::ObjectFlow";
  private static String STEREOTYPE_BINDDINGCONNECTOR = "SysML::BindingConnector";

  private enum CONNECTOR_TYPE {
    HAPPENS_BEFORE,
    HAPPENS_DURING,
    TRANSFER;
  }

  /**
   * initializing a new alloy translator
   *
   * @param working_dir where required alloy library (Transfer.als and utilities directory) is
   *     locating.
   */
  public OBMXMI2Alloy(String working_dir) throws FileNotFoundException, UMLModelErrorException {
    toAlloy = new ToAlloy(working_dir);
    redefinedConnectors = new HashSet<Connector>();
    // key = sigName, value = field names
    stepPropertiesBySig_all = new HashMap<>();
    stepPropertiesBySig = new HashMap<>();
    sigToTransferFieldMap = new HashMap<>();
    sigToFactsMap = new HashMap<>();
  }

  /**
   * Create an alloy (the given outputFile) of the qualifideName class in the xml file.
   *
   * @param xmiFile - the xmi file contain a class to be translated to an alloy file.
   * @param qualifiedName of a UML:Class for translation (ie.,
   *     Model::FoodService::OFSingleFoodService)
   * @param outputFile - the output alloy file
   * @return true if the given outputFile is created from the given xmlFile and the qualifiedName
   * @throws UMLModelErrorException
   * @throws FileNotFoundException
   */
  public boolean createAlloyFile(File xmiFile, String qualifiedName, File outputFile)
      throws FileNotFoundException, UMLModelErrorException {
    if (!xmiFile.exists() || !xmiFile.canRead()) {
      System.err.println("File " + xmiFile.getAbsolutePath() + " does not exist or read.");
      return false;
    }
    if (loadOBMAndCreateAlloy(xmiFile, qualifiedName)) {
      boolean success = toAlloy.createAlloyFile(outputFile, this.parameterFields);
      if (success) System.out.println(outputFile.getAbsolutePath() + " is created");
      else System.out.println("Failed to create the alloy file as " + outputFile.getAbsolutePath());
      return success;
    }
    System.err.println(this.errorMessage);
    return false;
  }

  /**
   * Get a errorMessage collected while translating.
   *
   * @return errorMessage
   */
  public String getErrorMessage() {
    return this.errorMessage;
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
      this.errorMessage = "Failed to initialize EMFUtil.";
      return false;
    }
    Resource r =
        EMFUtil.loadResourceWithDependencies(
            rs, URI.createFileURI(xmiFile.getAbsolutePath()), null);

    try {
      while (!r.isLoaded()) {
        System.out.println("not loaded yet wait 1 milli sec...");
        Thread.sleep(1000);
      }
    } catch (Exception e) {
    }

    try {
      sysMLUtil = new SysMLUtil(rs);
      sysmladapter = new SysMLAdapter(xmiFile, null);
    } catch (UMLModelErrorException e1) {
      this.errorMessage = "Failed to load SysML in EMFUtil.";
      throw e1;
    } catch (FileNotFoundException e) {
      this.errorMessage = xmiFile.getAbsolutePath() + " does not exist.";
      throw e;
    }

    org.eclipse.uml2.uml.NamedElement mainClass = EMFUtil.getNamedElement(r, className);

    if (mainClass == null) {
      this.errorMessage = className + " not found in " + xmiFile.getAbsolutePath();
      return false;
    }

    // key = NamedElement(Class or org.eclipse.uml2.uml.PrimitiveType(Integer, Real etc.)) value =
    // property used to create disj fields
    Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass =
        new HashMap<>();
    if (mainClass instanceof Class) {
      // The mainclass will be the last in the list
      List<org.eclipse.uml2.uml.Class> classInHierarchyForMain =
          MDUtils.createListIncludeSelfAndParents((Class) mainClass);
      PrimSig parentSig = null;
      for (Class aClass : classInHierarchyForMain) {
        boolean isMainSig = (aClass == mainClass) ? true : false;
        // create Sig - returned parentSig will be the next aClass(sig)'s parent
        parentSig = toAlloy.createSigOrReturnSig(aClass.getName(), parentSig, isMainSig);
        processClassToSig(aClass, propertiesByClass);
      }
    }

    Set<NamedElement> allClassesConnectedToMainSigByFields =
        propertiesByClass.keySet(); // SimpleSequence,
    // [0]=grand parent [1]=parent [2]=child
    List<Class> classInHierarchy =
        MDUtils.createListIncludeSelfAndParents((Class) mainClass); // SimpleSequence

    /* Map<PrimSig, Set<Property>> */ redefinedPropertiesBySig = new HashMap<>();
    // go throw each sigs in classInHierarchy and allClassesConnectedToMainSigByFields
    for (Class ne : classInHierarchy) {
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      addFieldsToSig(ne, propertiesByType, redefinedPropertiesBySig);
      addToStepPropertiesBySig(ne);

      // allClasses MAY contain a class in hierarchy so remove it
      allClassesConnectedToMainSigByFields.remove(ne);
    }

    for (NamedElement ne : allClassesConnectedToMainSigByFields) {
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      addFieldsToSig(ne, propertiesByType, redefinedPropertiesBySig);
      addToStepPropertiesBySig(ne);
    }

    for (PrimSig sig : redefinedPropertiesBySig.keySet()) {
      // fact {all x: OFFoodService | x.eat in OFEat }
      // fact {all x: IFSingleFoodService | x.order in IFCustomerOrder}
      toAlloy.addRedefinedSubsettingAsFacts(sig, redefinedPropertiesBySig.get(sig));
    }

    Set<NamedElement> leafClasses = MDUtils.findLeafClass(allClassesConnectedToMainSigByFields);
    leafClasses.add(classInHierarchy.get(classInHierarchy.size() - 1));
    leafSigs = MDUtils.toSigs(leafClasses, toAlloy);

    // connectors
    // key = Signame, values = propertyNames
    HashMap<String, Set<String>> inputs = new HashMap<>(); // collect field type Sig having a
    // transfer connector
    // with transferTarget "Customer"
    HashMap<String, Set<String>> outputs = new HashMap<>(); // collect field type Sig having a
    // transfer connector

    // used to collect sig with fields with Transfer or TransferBefore type. The set is used to
    // create like fact {all x: BehaviorWithParameter | no y: Transfer | y in x.steps} if the sig is
    // not in the set
    Set<Sig> sigWithTransferFields = new HashSet<>();
    allClassesConnectedToMainSigByFields = propertiesByClass.keySet(); // Suppler,
    // Customer
    Set<String> sigNameOfSharedFieldType = new HashSet<>(); // ie., BehaviorWithParameter

    // from child to parent so that redefinedconnector is not created by parents
    // during handlClassForProcessConnector this.sigToTransferFieldMap is created
    for (int i = classInHierarchy.size() - 1; i >= 0; i--) {
      Class ne = classInHierarchy.get(i);
      sigNameOfSharedFieldType.addAll(
          handleClassForProecessConnector(ne, inputs, outputs, sigWithTransferFields));
      // removing ne
      allClassesConnectedToMainSigByFields.remove(ne);
    }
    for (NamedElement ne : allClassesConnectedToMainSigByFields) {
      if (ne instanceof Class) { // no connector in primitiveType (Real, Integer)
        sigNameOfSharedFieldType.addAll(
            handleClassForProecessConnector(ne, inputs, outputs, sigWithTransferFields));
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
        mainSigInheritingTransferFields.addAll(possibleMainSigInheritingTransferFields);
      }
      Set<Expr> possibleMainSigInheritingTransferRelatedFacts =
          sigToFactsMap.get(classInHierarchy.get(i).getName());
      if (possibleMainSigInheritingTransferRelatedFacts != null)
        mainSigInheritingTransferRelatedFacts.addAll(possibleMainSigInheritingTransferRelatedFacts);
    }
    // add mainSig's inherited transfer fields to step Properties for the mainSig
    stepPropertiesBySig_all.get(mainClass.getName()).addAll(mainSigInheritingTransferFields);

    toAlloy.addFacts(mainClass.getName(), mainSigInheritingTransferRelatedFacts);

    Set<Sig> noStepsSigs = toAlloy.addSteps(stepPropertiesBySig_all, leafSigs);
    // if "no x.steps" and sig with fields with type Transfer should not have below:
    // fact {all x: BehaviorWithParameterOut | no y: Transfer | y in x.steps}
    sigWithTransferFields.addAll(noStepsSigs);
    toAlloy.handleNoTransfer(sigWithTransferFields, leafSigs);

    // allClassNames are used to create no inputs and no outputs later created from Sigs in
    // propertiesByClass.keySet() and classInHierarchy
    Set<String> allClassNames =
        Stream.of(
                propertiesByClass.keySet().stream()
                    .map(c -> c.getName())
                    .collect(Collectors.toSet()),
                classInHierarchy.stream().map(c -> c.getName()).collect(Collectors.toSet()))
            .flatMap(x -> x.stream())
            .collect(Collectors.toSet());

    Map<String, NamedElement> namedElementsBySigName =
        propertiesByClass.keySet().stream()
            .collect(Collectors.toMap(NamedElement::getName, e -> e));

    toAlloy.handleNoInputsOutputs(
        inputs, outputs, allClassNames, sigNameOfSharedFieldType, leafSigs, namedElementsBySigName);

    // adding no steps.x
    // fact {all x: Integer | no steps.x}, fact {all x: Real | no steps.x} or {all x: Product | no
    // steps.x}
    toAlloy.handleStepClosure(this.transferingTypeSig, leafSigs);
    return true;
  }

  /**
   * Add to stepPropertiesBySig properties including inherited attributes
   *
   * @param ne
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
    System.out.println("====" + ne.getName());
    System.out.println(stepProperties_all);
    stepPropertiesBySig_all.put(ne.getName(), stepProperties_all);

    Set<String> stepProperties = new HashSet<>();
    if (ne instanceof org.eclipse.uml2.uml.Class) { // ne can be PrimitiveType
      Set<org.eclipse.uml2.uml.Property> atts =
          sysMLUtil.getOwnedAttributes((org.eclipse.uml2.uml.Class) ne);
      for (Property p : atts) {
        if (p.getAppliedStereotype(STEREOTYPE_STEP) != null
            || p.getAppliedStereotype(STEREOTYPE_PATICIPANT) != null) {
          stepProperties.add(p.getName());
        }
      }
    }
    System.out.println("====own" + ne.getName());
    System.out.println(stepProperties);
    stepPropertiesBySig.put(ne.getName(), stepProperties);
  }

  /**
   * @param ne
   * @param stepPropertiesBySig_all
   * @param inputs
   * @param outputs
   * @param sigWithTransferFields
   */
  private Set<String> handleClassForProecessConnector(
      NamedElement ne,
      HashMap<String, Set<String>> inputs,
      HashMap<String, Set<String>> outputs,
      Set<Sig> sigWithTransferFields) {

    PrimSig ownerSig = toAlloy.getSig(ne.getName());

    Map<Field, Set<Field>> fieldWithInputs = new HashMap<>(); // key = prepare, value=
    // [preparedFoodItem,prepareDestination]
    Map<Field, Set<Field>> fieldWithOutputs = new HashMap<>(); // key = order,
    // value=[orderAmount,
    // orderDestination,
    // orderedFoodItem]

    // transfer fields created is adding to stepPropertiesBySig passing in processConnector
    // ie., BehaviorWithParameter
    Set<String> sigNameOfSharedFieldType =
        processConnector(
            (Class) ne, inputs, outputs, sigWithTransferFields, fieldWithInputs, fieldWithOutputs);

    // any of connectorends owned field types are the same (p1, p2:
    // BehaviorWithParameter)
    // 4.1.5 Multiple Execution Step2 = MultiplObjectFlow=[BehaviorWithParameter]
    if (sigNameOfSharedFieldType.size() > 0) {
      Set<String> nonTransferFieldNames =
          stepPropertiesBySig_all.get(ne.getName()).stream()
              .filter(f -> !f.startsWith("transfer"))
              .collect(Collectors.toSet());
      // no inputs
      for (String fieldName : nonTransferFieldNames) {
        if (!AlloyUtils.fieldsLabels(fieldWithInputs.keySet()).contains(fieldName)) {
          // fact {all x: MultipleObjectFlow |no x.p1.inputs}
          toAlloy.createNoInputsOrOutputsField(ownerSig, fieldName, Alloy.oinputs);
        }
        if (!AlloyUtils.fieldsLabels(fieldWithOutputs.keySet()).contains(fieldName)) {
          toAlloy.createNoInputsOrOutputsField(ownerSig, fieldName, Alloy.ooutputs);
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
      if (fields.size() > 0) toAlloy.createInOutClosure(ownerSig, field, fields, Alloy.oinputs);
    }
    for (Field field : fieldWithOutputs.keySet()) {
      Set<Field> fields = removeParameterFields(fieldWithOutputs.get(field));
      if (fields.size() > 0) toAlloy.createInOutClosure(ownerSig, field, fields, Alloy.ooutputs);
    }

    return sigNameOfSharedFieldType;
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
   * Add fields in Sig (non redefined attributes only), Add cardinality facts (ie., abc = 1) and
   * return set of strings to be used later.
   *
   * <p>this.parameterFields and this.valueTypeFields are updated.
   *
   * @param NamedElement ne that map to a Sig
   * @param propertiesByType - Map<Type, List<Property>> map of properties by type
   * @return
   */
  private void addFieldsToSig(
      NamedElement ne,
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType,
      Map<PrimSig, Set<Property>> redefinedPropertiesPerSig) {

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
            System.err.println(
                p.getQualifiedName()
                    + "has no name, so ignored.  Please defined the name to be included");
          }
        }

        redefinedPropertiesPerSig.put(sigOfNamedElement, redefinedProperties);

        Collections.sort(nonRedefinedPropertyInAlphabeticalOrderPerType);

        if (nonRedefinedPropertyInAlphabeticalOrderPerType.size() > 0) {
          Sig.Field[] fields =
              toAlloy.addDisjAlloyFields(
                  nonRedefinedPropertyInAlphabeticalOrderPerType,
                  propertyType.getName(),
                  sigOfNamedElement);
          // server, Serve, SinglFooeService
          if (fields != null) { // this should not happens
            for (int j = 0; j < propertiesSortedByType.size(); j++) {
              addCardinality(propertiesSortedByType.get(j), sigOfNamedElement, fields[j]);
              if (parameterProperties.contains(fields[j].label)) {
                this.parameterFields.add(fields[j]);
              } else if (valueTypeProperties.contains(fields[j].label)) {
                this.valueTypeFields.add(fields[j]);
              }
            }
          }
        } else { // cardinality only when no redefined properties
          for (Property p : propertiesSortedByType) {
            addCardinality(p, sigOfNamedElement, p.getName());
          }
        }
      }
    } // end processing property
  }

  private void addCardinality(Property p, PrimSig sigOfNamedElement, Field field) {
    if (p.getLower() == p.getUpper())
      toAlloy.addCardinalityEqualConstraintToField(field, sigOfNamedElement, p.getLower());
    else if (p.getUpper() == -1 && p.getLower() >= 1) {
      toAlloy.addCardinalityGreaterThanEqualConstraintToField(
          field, sigOfNamedElement, p.getLower());
    }
  }

  private void addCardinality(Property p, PrimSig sigOfNamedElement, String fieldName) {
    if (p.getLower() == p.getUpper())
      toAlloy.addCardinalityEqualConstraintToField(fieldName, sigOfNamedElement, p.getLower());
    else if (p.getUpper() == -1 && p.getLower() >= 1) {
      toAlloy.addCardinalityGreaterThanEqualConstraintToField(
          fieldName, sigOfNamedElement, p.getLower());
    }
  }

  /**
   * @param ne
   * @param stepFieldNames
   * @param inputs
   * @param outputs
   * @param sigWithTransferFields
   * @param fieldsWithInputs
   * @param fieldsWithOutputs
   * @param connectorendsFieldTypeOwnerFieldTypeSig - connector's property having same owner sig
   *     (ie., BehaviorWithParameter in 4.1.5 MultiObjectFlow)
   *     <p>// For 4.1.5 MultipleObjectFlow // a connector sourceOutputProperty(i) and
   *     targetInputProperty (i)'s owner field is // p1:BehaviorWithParameter, p2:BehaviorParameter
   *     // the type (BehaviorParameter) is the same so put in
   *     connectorendsFieldTypeOwnerFieldTypeSig
   * @return Set<String> signame whose (BehaviorWithParameter) connectorend's belong to have the
   *     same type (ie., p1, p2: BehaviorWithParameter)
   */
  private Set<String> processConnector(
      Class ne,
      HashMap<String, Set<String>> inputs,
      HashMap<String, Set<String>> outputs,
      Set<Sig> sigWithTransferFields,
      Map<Field, Set<Field>> fieldsWithInputs,
      Map<Field, Set<Field>> fieldsWithOutputs) {

    Set<String> sigNameOfSharedFieldType = new HashSet<>();
    PrimSig sigOfNamedElement = toAlloy.getSig(ne.getName());

    Set<Constraint> constraints = sysMLUtil.getAllRules(ne);
    Set<EList<Element>> oneOfSets = getOneOfRules(constraints);

    // Set<org.eclipse.uml2.uml.Connector> connectors = sysMLUtil.getAllConnectors(ne);
    Set<org.eclipse.uml2.uml.Connector> connectors = sysMLUtil.getOwnedConnectors(ne);

    // process connectors with oneof first
    Set<Connector> processedConnectors = new HashSet<>();
    for (EList<Element> oneOfSet : oneOfSets) {
      handleOneOfConnectors(sigOfNamedElement, connectors, oneOfSet, processedConnectors);
    }

    // process remaining of connectors
    for (org.eclipse.uml2.uml.Connector cn : connectors) {
      if (processedConnectors.contains(cn)) continue; // oneof connectors so not need to process
      if (ne.getInheritedMembers().contains(cn)) continue; // ignore inherited

      // while translating IFSingleFoolService and processing connectors for IFFoodService,
      // connectors creating "transferPrepareServe" and "transferServeEat" should be ignored because
      // it they are redefined in IFSingleFoodService
      if (this.redefinedConnectors.contains(cn)) continue; // ignore

      CONNECTOR_TYPE connector_type = null;
      edu.umd.omgutil.uml.Element omgE = sysmladapter.mapObject(cn);
      if (omgE instanceof edu.umd.omgutil.uml.Connector) {
        edu.umd.omgutil.uml.Connector omgConnector = (edu.umd.omgutil.uml.Connector) omgE;

        edu.umd.omgutil.uml.Type owner = omgConnector.getFeaturingType();
        String source = null;
        String target = null;

        String sourceTypeName = null; // used in Transfer
        String targetTypeName = null; // used in Transfer
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
              if (source != null && target != null) {
                toAlloy.addEqual(sigOfNamedElement, source, target);
              }
            }
          } else {

            String definingEndName = ce.getDefiningEnd().getName();
            edu.umd.omgutil.uml.ConnectorEnd end =
                (edu.umd.omgutil.uml.ConnectorEnd) sysmladapter.mapObject(ce);
            List<String> endsFeatureNames =
                end.getCorrectedFeaturePath(owner).stream()
                    .map(f -> f.getName())
                    .collect(Collectors.toList());

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

              List<Property> ps = sysMLUtil.getPropertyPath(ce);

            } else if (definingEndName.equals("transferTarget")) {
              connector_type = CONNECTOR_TYPE.TRANSFER;
              target = endsFeatureNames.get(0);
              targetTypeName = ce.getRole().getType().getName();
            }

            if (source == null || target == null) continue;
            if (connector_type == CONNECTOR_TYPE.HAPPENS_BEFORE) {
              handleHappensBefore(sigOfNamedElement, source, target);
            } else if (connector_type == CONNECTOR_TYPE.HAPPENS_DURING)
              handleHappensDuring(sigOfNamedElement, source, target);
            else if (connector_type == CONNECTOR_TYPE.TRANSFER) {

              // System.out.println(
              // "=========== redefined connectors:" + cn.getRedefinedConnectors().size());
              for (Connector redefinedConnector : cn.getRedefinedConnectors()) {
                // System.out.println("QName: " + redefinedConnector.getQualifiedName());
                for (ConnectorEnd cce : ((Connector) redefinedConnector).getEnds()) {
                  // System.out.println(cce.getDefiningEnd().getName());
                  // if (cce.getDefiningEnd().getName().equals("transferSource")) {
                  // String xxsourceTypeName = cce.getRole().getType().getName();
                  // System.out.println(xxsourceTypeName);
                  // } else if (cce.getDefiningEnd().getName().equals("transferTarget")) {
                  // String xxtargetTypeName = cce.getRole().getType().getName();
                  // System.out.println(xxtargetTypeName);
                  // }
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
              addToHashMap(
                  inputs,
                  targetTypeName,
                  sourceOutputAndTargetInputProperties.get(1)); // "targetInputProperty"
              addToHashMap(
                  outputs,
                  sourceTypeName,
                  sourceOutputAndTargetInputProperties.get(0)); // "sourceOutputProperty",

              boolean addEquals = false;
              if (targetTypeName.equals(sourceTypeName)) { // ie., targetTypeName = sourceTypeName
                // is "BehaviorWithParemeter" for 4.1.5
                // Multiple Execution Steps2 - Multiple
                // Object Flow
                sigNameOfSharedFieldType.add(targetTypeName);
                addEquals = true;
              }

              boolean toBeInherited = false;
              // only leafSig
              List<Set<Field>> targetInputsSourceOutputsFields = null;
              if (leafSigs.contains(sigOfNamedElement)) {
                targetInputsSourceOutputsFields =
                    processConnectorInputsOutputs(
                        sigOfNamedElement,
                        source,
                        target,
                        sourceTypeName,
                        targetTypeName,
                        sourceOutputAndTargetInputProperties,
                        fieldsWithInputs,
                        fieldsWithOutputs,
                        addEquals);

              } else { // non leaf
                targetInputsSourceOutputsFields =
                    findInputAndOutputsFields(
                        sigOfNamedElement,
                        source,
                        target,
                        sourceTypeName,
                        targetTypeName,
                        sourceOutputAndTargetInputProperties);
                toBeInherited = true;
              }
              if (type.getName().equals("Transfer")) {
                handleTransferFieldAndFn(
                    sigOfNamedElement,
                    source,
                    target,
                    targetInputsSourceOutputsFields,
                    toBeInherited);
              } else if (type.getName().equals("TransferBefore")) {
                handleTransferBeforeFieldAndFn(
                    sigOfNamedElement,
                    source,
                    target,
                    targetInputsSourceOutputsFields,
                    toBeInherited);
              }
            }
          }
        } // end of connectorEnd
      } // end of Connector
    } // org.eclipse.uml2.uml.Connector
    return sigNameOfSharedFieldType;
  }

  /**
   * Find sourceOutputProperty field and targetInputProperty field with the given connector
   * information. Return List of fields [0] = sourceOutputProperty field, [1]= targetInputProperty
   * field
   *
   * @param sigOfNamedElement - PrimSig (ie., OFFoodService)
   * @param source - Source property name (i.e. order)
   * @param target - Target property name (ie., serve)
   * @param sourceTypeName - Source property type (ie., OFOrder)
   * @param targetTypeName - Target property type (ie., OFServe)
   * @param sourceOutputAndTargetInputProperties - List of String [0] = sourceOutputProperty [1] =
   *     tragetInputProperty (i.e., [orderedFoodIterm, servedFoodItem])
   * @return List of fields (i.e., [field (OFOrder <: orderedFoodItem),[field (OFServe <:
   *     servedFoodItem)]])
   */
  private List<Set<Field>> findInputAndOutputsFields(
      PrimSig sigOfNamedElement,
      String source,
      String target,
      String sourceTypeName,
      String targetTypeName,
      List<Set<String>> sourceOutputAndTargetInputProperties) {

    Field outputFrom = AlloyUtils.getFieldFromSigOrItsParents(source, sigOfNamedElement);
    Set<Field> addOutputToFields = new HashSet<>();
    Set<Field> addInputToFields = new HashSet<>();
    if (outputFrom != null) {
      PrimSig typeSig = toAlloy.getSig(sourceTypeName); // sourceTypeName =IFCustomerOrder
      for (String sourceOutput : sourceOutputAndTargetInputProperties.get(0)) {
        // orderedFoodItem
        Field outputTo = AlloyUtils.getFieldFromSigOrItsParents(sourceOutput, typeSig); // i
        // fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p1, x.p1.i]}
        if (!parameterFields.contains(outputTo)) addOutputToFields.add(outputTo);
        // System.out.println(sigOfNamedElement.label); // IFSingleFoodService
        // System.out.println(outputFrom.label); // order
        // System.out.println(outputTo.label); // orderedFoodItem
      }
    }

    // target => inputs
    // fact {all x: MultipleObjectFlow | all p: x.p2 | p.i = p.inputs}
    // System.out.println(
    // target + ": " + targetTypeName + " with " + sourceOutputAndTargetInputProperties[1]);

    Field inputFrom = AlloyUtils.getFieldFromSigOrItsParents(target, sigOfNamedElement); // p2
    if (inputFrom != null) {
      PrimSig typeSig = toAlloy.getSig(targetTypeName); // IFCustomPrepare
      for (String targetInputProperties : sourceOutputAndTargetInputProperties.get(1)) {
        Field inputTo =
            AlloyUtils.getFieldFromSigOrItsParents(
                targetInputProperties, // i
                typeSig);
        if (!parameterFields.contains(inputTo)) addInputToFields.add(inputTo);
      }
    }
    return List.of(addOutputToFields, addInputToFields);
  }

  /**
   * adding facts inputs and outputs for bijection like below
   *
   * <p>- fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}
   *
   * <p>- fact {all x: IFSingleFoodService | bijectionFiltered[outputs, x.order,
   * x.order.orderedFoodItem]}
   *
   * @param sigOfNamedElement
   * @param source
   * @param target
   * @param sourceTypeName
   * @param targetTypeName
   * @param sourceOutputAndTargetInputProperties
   * @param fieldsWithInputs
   * @param fieldsWithOutputs
   */
  private List<Set<Field>> processConnectorInputsOutputs(
      PrimSig sigOfNamedElement,
      String source,
      String target,
      String sourceTypeName,
      String targetTypeName,
      List<Set<String>> sourceOutputAndTargetInputProperties,
      Map<Field, Set<Field>> fieldsWithInputs,
      Map<Field, Set<Field>> fieldsWithOutputs,
      boolean addEquals) {

    // if addEquals are true add the fact like below:
    // targetTypeName = sourceTypeName (ie., BehaviorWithParameter)
    // source => outputs
    // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}

    Field outputFrom = AlloyUtils.getFieldFromSigOrItsParents(source, sigOfNamedElement); // p1,
    // order
    // (sigOfNamedElement
    // =
    // IFSIngleFoodService)

    Set<Field> addOutputToFields = new HashSet<>();
    Set<Field> addInputToFields = new HashSet<>();
    if (outputFrom != null) {
      // if (!fieldsWithOutputs.contains(outputFrom.label)) {// handle duplicate p1 having two
      // outputs

      // fieldsWithOutputs.add(outputFrom.label);

      PrimSig typeSig = toAlloy.getSig(sourceTypeName); // sourceTypeName =IFCustomerOrder

      for (String sourceOutput : sourceOutputAndTargetInputProperties.get(0)) {
        // orderedFoodItem
        Field outputTo =
            AlloyUtils.getFieldFromSigOrItsParents(
                // [orderFoodItem, prepareFoodItem],
                sourceOutput, typeSig); // i
        // fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p1, x.p1.i]}
        if (!parameterFields.contains(outputTo)) addOutputToFields.add(outputTo);
        // System.out.println(sigOfNamedElement.label); // IFSingleFoodService
        // System.out.println(outputFrom.label); // order
        // System.out.println(outputTo.label); // orderedFoodItem

        if (!contains(fieldsWithOutputs, outputFrom, outputTo)) {
          addToHashMap(fieldsWithOutputs, outputFrom, outputTo);
          // fact {all x: IFSingleFoodService | bijectionFiltered[outputs, x.order,
          // x.order.orderedFoodItem]}

          // not need to add in this.sigToFactMap because its special field type (ie., order:
          // IFCustomOrder) are handled below using redefined Property
          /* addToSigToFactsMap(sigOfNamedElement.label, */
          toAlloy.createBijectionFilteredInputsOrOutputs(
              sigOfNamedElement,
              outputFrom,
              outputFrom.join(outputTo),
              outputTo,
              addEquals,
              Alloy.ooutputs); // );
        }

        // if redefined and not in addField, create fact bijection outputs
        // Optional<Property> op_redefinedProperty =
        // redefinedPropertiesPerSig.get(sigOfNamedElement)
        // .stream().filter(p -> p.getName().equals(outputFrom.label)).findFirst();
        // if (op_redefinedProperty.isPresent()) {
        // Property redefinedProperty = op_redefinedProperty.get();
        // Type type = redefinedProperty.getType();
        //
        // if (type.getName().equals(sourceTypeName)) { // IFCustomerOrder
        // SafeList<Field> typeSigFields = typeSig.getFields();
        // for (Field typeSigField : typeSigFields) { // orderDestination, orderAmount
        // // (IFCustomerOrder's fields)
        // if (!addedField.contains(typeSigField)) {
        // // fact {all x: IFSingleFoodService | bijectionFiltered[outputs, x.order,
        // // x.order.orderDestination]}
        // // fact {all x: IFSingleFoodService | bijectionFiltered[outputs, x.order,
        // // x.order.orderAmount]}
        // /* addToSigToFactsMap(sigOfNamedElement.label, */
        // System.out.println(typeSigField.label);
        // toAlloy.createBijectionFilteredOutputsAndAddToOverallFact(sigOfNamedElement,
        // outputFrom, outputFrom.join(typeSigField), typeSigField, false);// );
        //
        // }
        // }
        // }
        // }

      }
    }

    // target => inputs
    // fact {all x: MultipleObjectFlow | all p: x.p2 | p.i = p.inputs}
    // System.out.println(
    // target + ": " + targetTypeName + " with " + sourceOutputAndTargetInputProperties[1]);

    Field inputFrom = AlloyUtils.getFieldFromSigOrItsParents(target, sigOfNamedElement); // p2
    if (inputFrom != null) {
      // if (!fieldsWithInputs.contains(inputFrom.label)) { // handle duplicate p4 is having two
      // inputs
      // fieldsWithInputs.add(inputFrom.label);
      PrimSig typeSig = toAlloy.getSig(targetTypeName); // IFCustomPrepare
      for (String targetInputProperties : sourceOutputAndTargetInputProperties.get(1)) {
        Field inputTo =
            AlloyUtils.getFieldFromSigOrItsParents(
                targetInputProperties, // i
                typeSig);
        if (!parameterFields.contains(inputTo)) addInputToFields.add(inputTo);
        // fact {all x: MultipleObjectFlow | bijectionFiltered[inputs, x.p2, x.p2.i]}
        // fact {all x: IFSingleFoodService | bijectionFiltered[inputs, x.prepare,
        // x.prepare.preparedFoodItem]}
        /* addToSigToFactsMap(sigOfNamedElement.label, */
        if (!contains(fieldsWithInputs, inputFrom, inputTo)) {
          addToHashMap(fieldsWithInputs, inputFrom, inputTo);
          toAlloy.createBijectionFilteredInputsOrOutputs(
              sigOfNamedElement,
              inputFrom,
              inputFrom.join(inputTo),
              inputTo,
              addEquals,
              Alloy.oinputs); // );
        }
      }
      // Optional<Property> op_redefinedProperty =
      // redefinedPropertiesPerSig.get(sigOfNamedElement)
      // .stream().filter(p -> p.getName().equals(inputFrom.label)).findFirst();
      // if (op_redefinedProperty.isPresent()) {
      // Property redefinedProperty = op_redefinedProperty.get();
      // Type type = redefinedProperty.getType();
      // if (type.getName().equals(targetTypeName)) {// IFCustomPrepare
      // SafeList<Field> typeSigFields = typeSig.getFields();
      // for (Field typeSigField : typeSigFields) {// [prepareDestination]
      // if (!addedField.contains(typeSigField)) {
      // // fact {all x: IFSingleFoodService | bijectionFiltered[inputs, x.prepare,
      // // x.prepare.prepareDestination]}
      // /* addToSigToFactsMap(sigOfNamedElement.label, */
      // toAlloy.createBijectionFilteredInputsAndAddToOverallFact(sigOfNamedElement,
      // inputFrom, inputFrom.join(typeSigField), typeSigField, false);// );
      // }
      // }
      // }
      // }

      // }
    }
    return List.of(addOutputToFields, addInputToFields);
  }

  private Sig.Field handTransferFieldAndFnPrep(PrimSig sig, String source, String target) {
    String fieldName = "transfer" + firstCharUpper(source) + firstCharUpper(target);
    // adding transferFields in stepProperties
    stepPropertiesBySig_all.get(sig.label).add(fieldName);
    addSigToTransferFieldsMap(sig, fieldName);
    Sig.Field transferField = ToAlloy.addAlloyTransferField(fieldName, sig);
    return transferField;
  }

  /**
   * used to includes facts in sig's subclass bijections inputs or outputs are not added because
   * handled in
   *
   * @param sig
   * @param source
   * @param target
   * @param stepFieldNames
   */
  private void handleTransferFieldAndFn(
      PrimSig sig,
      String source,
      String target,
      List<Set<Field>> targetInputsSourceOutputsFields,
      boolean toBeInherited) {
    Sig.Field transferField = handTransferFieldAndFnPrep(sig, source, target);
    addToSigToFactsMap(
        sig.label,
        toAlloy.createFnForTransfer(
            sig, transferField, source, target, targetInputsSourceOutputsFields, toBeInherited));
  }

  /**
   * @param sig
   * @param source
   * @param target
   * @param stepFieldNames is passed here to add transfer fields created while processing connector.
   */
  private void handleTransferBeforeFieldAndFn(
      PrimSig sig,
      String source,
      String target,
      List<Set<Field>> targetInputsSourceOutputsFields,
      boolean toBeInherited) {
    Sig.Field transferField = handTransferFieldAndFnPrep(sig, source, target);
    addToSigToFactsMap(
        sig.label,
        toAlloy.createFnForTransferBefore(
            sig, transferField, source, target, targetInputsSourceOutputsFields, toBeInherited));
  }

  /**
   * @param sig
   * @param fieldName
   */
  private void addSigToTransferFieldsMap(PrimSig sig, String fieldName) {
    Set<String> tFields;
    if ((tFields = sigToTransferFieldMap.get(sig)) == null) {
      tFields = new HashSet<>();
      tFields.add(fieldName);
      sigToTransferFieldMap.put(sig.label, tFields);
    } else tFields.add(fieldName);
  }

  /*
   *
   * go through each class, class to its properties, a property to its type recursively to complete
   * propertiesByClass (Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>>)
   *
   * @param umlElement NamedElement either org.eclipse.uml2.uml.Class or
   * org.eclipse.uml2.uml.PrimitiveType to be analyzed to complete propertiesByClass
   *
   * @param propertiesByClass Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>>
   * where the key NamedElement to be mapped to Sig (class or PrimitiveType like Integer and Real)
   * and value is Map<org.eclipse.uml2.uml.Type, List<Property>. The map's key type is
   * property/field's type and List<Property> is property/fields having the same type.
   *
   * For example,
   *
   * sig SimpleSequence extends Occurrence { disj p1,p2: set AtomicBehavior }
   *
   * propertiesByClass's key = SimpleSequence and value = (key = AtomicBehavior, value =[p1,p2])
   *
   */
  private void processClassToSig(
      NamedElement umlElement,
      Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass) {

    if (umlElement instanceof org.eclipse.uml2.uml.Class) {

      // umlElements are for IFFoodService
      // processClassToSig from outside of this method : (FoodService) -> then called
      // recursively(internally) Prepare -> Order -> Serve -> Eat -> Pay
      // processClassToSig from outside of this method (IFFoorService) -> then called
      // recursively(internally) IFPrepare -> FoodItem -> IFServe -> FoodItem -> IFEat -> FoodItem->
      // IFPay-> FoodItem

      // Set<org.eclipse.uml2.uml.Property> atts =
      // sysMLUtil.getOwnedAttributes((org.eclipse.uml2.uml.Class) umlElement);
      // sysMLUtil.getAllAttributes((org.eclipse.uml2.uml.Class) umlElement);

      Set<Property> atts = sysMLUtil.getOwnedAttributes((org.eclipse.uml2.uml.Class) umlElement);

      System.out.println(umlElement.getName() + "================");
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
        System.out.println(p.getName() + ": " + eType.getName());
        List<Property> ps = null;
        if ((ps = propertiesByTheirType.get(eType)) == null) {
          ps = new ArrayList<>();
          ps.add(p);
          propertiesByTheirType.put(eType, ps);
        } else ps.add(p);

        if (eType instanceof org.eclipse.uml2.uml.Class
            || eType instanceof org.eclipse.uml2.uml.PrimitiveType) {

          EList<Classifier> parents = null;
          if (eType instanceof org.eclipse.uml2.uml.Class) {
            parents = ((org.eclipse.uml2.uml.Class) eType).getGenerals();
          }
          // alloy allows only one parent
          // create Sig of property type with or without parent
          toAlloy.createAlloySig(
              eType.getName(),
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
   * Find property names for connector ends. One connector has two connector end. return string with
   * index = 0
   *
   * @param cn
   * @param ce
   * @return
   */
  private String[] getEndPropertyNames(Connector cn, ConnectorEnd ce) {
    String[] names = new String[2];
    edu.umd.omgutil.uml.Connector omgE = (edu.umd.omgutil.uml.Connector) sysmladapter.mapObject(cn);
    edu.umd.omgutil.uml.Type owner = omgE.getFeaturingType();
    names[0] = getConnecterEndPropertyName(ce, owner);

    for (ConnectorEnd ce1 : cn.getEnds())
      if (ce1 != ce) {
        names[1] = getConnecterEndPropertyName(ce1, owner);
        break;
      }
    return names;
  }

  private String getConnecterEndPropertyName(ConnectorEnd ce, edu.umd.omgutil.uml.Type owner) {
    edu.umd.omgutil.uml.ConnectorEnd end =
        (edu.umd.omgutil.uml.ConnectorEnd) sysmladapter.mapObject(ce);
    List<String> endsFeatureNames =
        end.getCorrectedFeaturePath(owner).stream()
            .map(f -> f.getName())
            .collect(Collectors.toList());
    String name = endsFeatureNames.get(0);
    return name;
  }

  private void handleOneOfConnectors(
      PrimSig ownerSig,
      Set<Connector> connectors,
      List<Element> oneOfSet,
      Set<Connector> processedConnectors) {

    List<String> sourceNames = new ArrayList<>(); // for each connector
    List<String> targetNames = new ArrayList<>();
    boolean isSourceSideOneOf = false;
    for (org.eclipse.uml2.uml.Connector cn : connectors) {
      for (ConnectorEnd ce : cn.getEnds()) {

        Optional<Element> found = oneOfSet.stream().filter(e -> e == ce).findFirst();
        if (!found.isEmpty()) {
          processedConnectors.add(cn);
          String[] names = getEndPropertyNames(cn, ce);

          String definingEndName = ce.getDefiningEnd().getName();
          if (definingEndName.equals("happensAfter")) {
            isSourceSideOneOf = true; // source-sides have oneof
            sourceNames.add(names[0]);
            targetNames.add(names[1]);
          } else if (definingEndName.equals("happensBefore")) {
            isSourceSideOneOf = false; // target-side have oneof
            targetNames.add(names[0]);
            sourceNames.add(names[1]);
          }
        }
      }
    }

    // sort so not x.p2 + x.p1 but be x.p1 + x.p2
    Collections.sort(sourceNames);
    Collections.sort(targetNames);

    // handling case of self-loop
    List<String> sourceInTarget = getAsContainInBs(sourceNames, targetNames);
    List<String> targetInSource = getAsContainInBs(targetNames, sourceNames);
    if (sourceInTarget.size() > 0 && isSourceSideOneOf) { // sourceSide
      Expr beforeExpr_filtered = null; // p1
      Expr beforeExpr_all = null; // p1 plus p2

      Expr afterExpr = /* ownerSig.domain( */
          AlloyUtils.getFieldFromSigOrItsParents(targetNames.get(0), ownerSig) /* ) */;
      for (String sourceName : sourceNames) {
        if (!sourceInTarget.contains(sourceName)) {
          beforeExpr_filtered =
              beforeExpr_filtered == null
                  ? /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                      sourceName, ownerSig) // )
                  : beforeExpr_filtered.plus(
                      /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                          sourceName, ownerSig)) /* ) */;
        }
      }
      for (String sourceName : sourceNames) {
        beforeExpr_all =
            beforeExpr_all == null
                ? /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                    sourceName, ownerSig) // )
                : beforeExpr_all.plus(
                    /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                        sourceName, ownerSig)) /* ) */;
      }

      toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(
          ownerSig, beforeExpr_filtered, afterExpr); // not include source in source
      toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(
          ownerSig, beforeExpr_all, afterExpr);
    } else if (targetInSource.size() > 0 && !isSourceSideOneOf) {
      Expr afterExpr_filtered = null; // p3
      Expr afterExpr_all = null; // p2 + p3

      Expr beforeExpr = /* ownerSig.domain( */
          AlloyUtils.getFieldFromSigOrItsParents(sourceNames.get(0), ownerSig) /* ) */;
      for (String targetName : targetNames) {
        if (!targetInSource.contains(targetName)) {
          afterExpr_filtered =
              afterExpr_filtered == null
                  ? /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                      targetName, ownerSig) // )
                  : afterExpr_filtered.plus(
                      /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                          targetName, ownerSig)) /* ) */;
        }
      }
      for (String targetName : targetNames) {
        afterExpr_all =
            afterExpr_all == null
                ? /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                    targetName, ownerSig) // )
                : afterExpr_all.plus(
                    /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                        targetName, ownerSig)) /* ) */;
      }

      toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(
          ownerSig, beforeExpr, afterExpr_all); // not include target in source
      toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(
          ownerSig, beforeExpr, afterExpr_filtered);

    } // non self-loop
    else {
      Expr beforeExpr = null;
      Expr afterExpr = null;
      if (isSourceSideOneOf) { // sourceSide need to be combined
        afterExpr = /* ownerSig.domain( */
            AlloyUtils.getFieldFromSigOrItsParents(targetNames.get(0), ownerSig) /* ) */;
        for (String sourceName : sourceNames) {
          beforeExpr =
              beforeExpr == null
                  ? /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                      sourceName, ownerSig) // )
                  : beforeExpr.plus(
                      /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                          sourceName, ownerSig)) /* ) */;
        }

      } else {
        for (String targetName : targetNames) {
          afterExpr =
              afterExpr == null
                  ? /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                      targetName, ownerSig) // )
                  : afterExpr.plus(
                      /* ownerSig.domain( */ AlloyUtils.getFieldFromSigOrItsParents(
                          targetName, ownerSig)) /* ) */;
        }
        beforeExpr = /* ownerSig.domain( */
            AlloyUtils.getFieldFromSigOrItsParents(sourceNames.get(0), ownerSig) /* ) */;
      }

      toAlloy.createBijectionFiltered(ownerSig, beforeExpr, afterExpr, Alloy.happensBefore);
    }
  }

  /**
   * Find any a contained in b as List of String
   *
   * @param a
   * @param b
   * @return
   */
  private List<String> getAsContainInBs(List<String> a, List<String> b) {
    List<String> contained = new ArrayList<String>();
    for (String s : a) {
      if (b.contains(s)) contained.add(s);
    }
    return contained;
  }

  // sig BuffetService
  private void handleHappensBefore(PrimSig sig, String source, String target) {

    System.out.println("looking for source" + source + " in " + sig.label);
    System.out.println("looking for target" + target + " in " + sig.label);

    Field sourceField = AlloyUtils.getFieldFromSigOrItsParents(source, sig); // FoodService <:
    // prepare
    Field targetField = AlloyUtils.getFieldFromSigOrItsParents(target, sig);

    if (sourceField != null && targetField != null) {
      // Expr sexpr = sig.domain(sourceField);
      // Expr texpr = sig.domain(targetField);
      // toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(sig, sexpr, texpr);

      toAlloy.createBijectionFiltered(sig, sourceField, targetField, Alloy.happensBefore);
    } else System.err.println("source or target for HappensBefore not in " + sig.label);
  }

  private void handleHappensDuring(PrimSig sig, String source, String target) {

    Field sourceField = AlloyUtils.getFieldFromSigOrItsParents(source, sig);
    Field targetField = AlloyUtils.getFieldFromSigOrItsParents(target, sig);

    if (sourceField != null && targetField != null) {
      toAlloy.createBijectionFiltered(sig, sourceField, targetField, Alloy.happensDuring);
    } else System.err.println("source or target for handleHappensDuring not in " + sig.label);
  }

  private List<Set<String>> handleTransferAndTransferBeforeInputsAndOutputs(
      org.eclipse.uml2.uml.Connector cn
      /* PrimSig sig , String source, String target, String sourceTypeName, String targetTypeName */ ) {

    List<Set<String>> sourceOutputAndTargetInputProperties = new ArrayList<>();
    String[] stTagNames = {"sourceOutputProperty", "targetInputProperty"}; // , "itemType"}; this
    // property value is class
    // not property
    Map<String, List<Property>> stTagItemFlowValues =
        getStreotypePropertyValues(cn, STEREOTYPE_ITEMFLOW, stTagNames);
    Map<String, List<Property>> stTagObjectFlowValues =
        getStreotypePropertyValues(cn, STEREOTYPE_OBJECTFLOW, stTagNames);

    List<Property> sos = null;
    List<Property> tis = null;

    if (stTagObjectFlowValues != null) {
      sos = stTagObjectFlowValues.get(stTagNames[0]); // sourceOutputProperty
      tis = stTagObjectFlowValues.get(stTagNames[1]); // targetInputProperty
      System.out.println("ObjectFlow");
      System.out.println(sos); // = x.outputs
      System.out.println(tis); // = x.inputs
    }

    if (stTagItemFlowValues != null) {
      sos = stTagItemFlowValues.get(stTagNames[0]); // sourceOutputProperty
      tis = stTagItemFlowValues.get(stTagNames[1]); // targetInputProperty - name is
      // "receivedProduct"
      System.out.println("ItemFlow");
      System.out.println(sos);
      System.out.println(tis);
    }
    Class sosOwner = null;
    Class tisOwner = null;
    Set<String> sourceOutput = new HashSet<>();
    Set<String> targetInput = new HashSet<>();
    if (sos != null && tis != null) {
      for (Property p : sos) {
        sosOwner = ((org.eclipse.uml2.uml.Class) p.getOwner());
        String owner = ((org.eclipse.uml2.uml.Class) p.getOwner()).getName();
        System.out.println(owner + " | x." + p.getName() + "= x.output");
        // B2 | x.vout= x.output
        // toAlloy.addOutputs(owner, /* "Supplier" */ p.getName() /* "suppliedProduct" */);
        sourceOutput.add(p.getName()); // = x.outputs
        transferingTypeSig.add(p.getType().getName());
        // break; // assumption is having only one
      }
      for (Property p : tis) {
        tisOwner = ((org.eclipse.uml2.uml.Class) p.getOwner());
        String owner = ((org.eclipse.uml2.uml.Class) p.getOwner()).getName();
        System.out.println(owner + " | x." + p.getName() + "= x.input: " + p.getName());
        // B | x.voutzzzzz= x.input
        // toAlloy.addInputs(owner, /* "Customer" */p.getName() /* "receivedProduct" */);
        transferingTypeSig.add(p.getType().getName());
        targetInput.add(p.getName()); // = x.inputs
        // break; // assumption is having only one
      }
      // preventing fact {all x:B| x.vin = x.output}} to be generated from <<ItemFlow>> between
      // B.vin and b1.vin. //b1 isa field of B
      // but having fact {all x: B1| x.vin = x.inputs} is ok
      EList<Property> atts = sosOwner.getAttributes();
      for (Property att : atts)
        if (att.getType() == tisOwner) { // owner is B
          sourceOutput = null;
          // sourceOutput.remove(att.getName());
          // sourceOutputAndTargetInputProperties[0] = null
          break;
        }
      // preventing fact {all x: B |x.vout = x.inputs} - to be generated from <<ItemFlow>> between
      // B.vout and b2(B2).vout - b2 is a field of B
      // but having fact {all x: B2|x.vout = x.outputs} is ok
      atts = tisOwner.getAttributes(); // tisOwner is B
      for (Property att : atts) // b2
      if (att.getType() == sosOwner) { // b2.getType() == B2 and sosOwner = B2
          targetInput = null;
          // sourceOutputAndTargetInputProperties[1] = null; // overwrite
          break;
        }
    }
    sourceOutputAndTargetInputProperties.add(sourceOutput);
    sourceOutputAndTargetInputProperties.add(targetInput);
    return sourceOutputAndTargetInputProperties;
  }

  /**
   * @param element
   * @param streotypeName
   * @param tagNames
   * @return null is not Stereotype with streotypeName applied to the element
   */
  private Map<String, List<Property>> getStreotypePropertyValues(
      Element element, String streotypeName, String[] tagNames) {

    Map<String, List<Property>> propertysByTagNames = new HashMap<>();
    Stereotype st = null;
    if ((st = element.getAppliedStereotype(streotypeName)) != null) {

      for (String propertyName : tagNames) {
        List<Property> results = new ArrayList<>();
        List<Object> properties = (List<Object>) (element.getValue(st, propertyName));
        for (Object property : properties) {
          if (property instanceof Property) {
            results.add((Property) property);
          } else {
            System.out.println(propertyName + " is not Property but " + property);
          }
        }
        propertysByTagNames.put(propertyName, results);
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

  private static String firstCharUpper(String o) {
    return o.substring(0, 1).toUpperCase() + o.substring(1).toLowerCase();
  }

  public static void addToHashMap(Map<String, Set<String>> map, String key, Set<String> values) {
    if (values == null || values.size() == 0) return;
    Set<String> vs;
    if (map.containsKey(key)) vs = map.get(key);
    else {
      vs = new HashSet<String>();
      map.put(key, vs);
    }
    vs.addAll(values);
  }

  public static void addToHashMap(Map<Field, Set<Field>> map, Field key, Field value) {
    Set<Field> vs;
    if (map.containsKey(key)) vs = map.get(key);
    else {
      vs = new HashSet<Field>();
      map.put(key, vs);
    }
    vs.add(value);
  }

  public static boolean contains(Map<Field, Set<Field>> map, Field key, Field value) {
    if (!map.containsKey(key)) return false;
    else {
      if (!map.get(key).contains(value)) return false;
    }
    return true;
  }

  public void addToSigToFactsMap(String sigName, Set<Expr> facts) {
    if (facts == null) return;
    Set<Expr> allFacts = sigToFactsMap.get(sigName);
    if (allFacts == null) sigToFactsMap.put(sigName, facts);
    else allFacts.addAll(facts);
  }
}
