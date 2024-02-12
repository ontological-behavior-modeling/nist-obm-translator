package edu.gatech.gtri.obm.translator.alloy.fromxmi;

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



public class OBMXMI2Alloy {

  ToAlloy toAlloy;
  SysMLAdapter sysmladapter;
  SysMLUtil sysMLUtil;// omgutil created from ResourceSet used through out the translator
  Set<Field> parameterFields; // A set of Field mapped from a Property with <<Parameter>> stereotype
  // Each connector with <<ObjectFlow>>, get sourceOutputProperty and targetInputProperty,
  // find the property type name and put in this Set
  Set<String> transferingTypeSig; // ie., [Integer] for Model::Basic::MultipleObjectFlowAlt
  String message = "";

  private static String STEREOTYPE_STEP = "Model::OBM::Step";
  private static String STEREOTYPE_PATICIPANT = "SysML::ParticipantProperty";
  public static String STEREOTYPE_PAREMETER = "Model::OBM::Parameter";// property
  private static String STEREOTYPE_ITEMFLOW = "Model::OBM::ItemFlow";
  private static String STEREOTYPE_OBJECTFLOW = "Model::OBM::ObjectFlow";
  private static String STEREOTYPE_BINDDINGCONNECTOR = "SysML::BindingConnector";

  private enum CONNECTOR_TYPE {
    HAPPENS_BEFORE, HAPPENS_DURING, TRANSFER;
  }

  /**
   * 
   * @param working_dir where required alloy library (Transfer) is locating
   */
  public OBMXMI2Alloy(String working_dir) throws FileNotFoundException, UMLModelErrorException {
    toAlloy = new ToAlloy(working_dir);
  }

  public boolean createAlloyFile(File xmiFile, String className, File outputFile)
      throws FileNotFoundException, UMLModelErrorException {

    if (!xmiFile.exists() || !xmiFile.canRead()) {
      System.err.println("File " + xmiFile.getAbsolutePath() + " does not exist or read.");
      return false;
    }
    if (loadOBMAndCreateAlloy(xmiFile, className)) {
      String outputFileName = toAlloy.createAlloyFile(outputFile, this.parameterFields);
      System.out.println(outputFileName + " is created");
      return true;
    }
    System.err.println(this.message);
    return false;
  }

  /**
   * @xmiFile xmi file containing activity
   * @param _className QualifiedName of Class containing activities
   * @return boolean true if success otherwise false
   * @throws FileNotFoundException
   * @throws UMLModelErrorException
   * @throws Exception
   */
  private boolean loadOBMAndCreateAlloy(File xmiFile, String _className) {

    parameterFields = new HashSet<>();
    transferingTypeSig = new HashSet<>();
    ResourceSet rs;
    try {
      rs = EMFUtil.createResourceSet();
    } catch (FileNotFoundException e1) {
      this.message = "Failed to initialize EMFUtil.";
      return false;
    }
    Resource r = EMFUtil.loadResourceWithDependencies(rs,
        URI.createFileURI(xmiFile.getAbsolutePath()), null);

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
      this.message = "Failed to load SysML in EMFUtil.";
      return false;
    } catch (FileNotFoundException e) {
      this.message = xmiFile.getAbsolutePath() + " does not exist.";
      return false;
    }

    org.eclipse.uml2.uml.NamedElement mainClass = EMFUtil.getNamedElement(r, _className);

    if (mainClass == null) {
      this.message = _className + " not found in " + xmiFile.getAbsolutePath();
      return false;
    }

    // key = NamedElement(Class or org.eclipse.uml2.uml.PrimitiveType(Integer, Real etc.))
    Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass =
        new HashMap<>();
    if (mainClass instanceof Class) {
      // The mainclass will be the last in the list
      List<org.eclipse.uml2.uml.Class> classInHierarchy =
          MDUtils.createListIncludeSelfAndParents((Class) mainClass);
      PrimSig parentSig = null;
      for (Class aClass : classInHierarchy) {
        boolean isMainSig = (aClass == mainClass) ? true : false;
        // returned parentSig will be the next aClass sig's parent
        parentSig = toAlloy.createSigOrReturnSig(aClass.getName(), parentSig, isMainSig);
        processClassToSig(aClass, propertiesByClass);
      }
    }
    // key = sigName, value = field names
    Map<String, Set<String>> stepPropertiesBySig = new HashMap<>();
    Set<NamedElement> allClassesConnectedToMainSigByFields = propertiesByClass.keySet(); // SimpleSequence,
    // AtomicBehavior
    List<Class> classInHierarchy = MDUtils.createListIncludeSelfAndParents((Class) mainClass); // SimpleSequence

    // go throw each sigs in classInHierarchy and allClassesConnectedToMainSigByFields
    for (Class ne : classInHierarchy) {
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      Set<String> stepProperties = processSigPropertiesToFields(ne, propertiesByType);
      stepPropertiesBySig.put(ne.getName(), stepProperties);
      // allClasses MAY contain a class in hierarchy so remove it
      allClassesConnectedToMainSigByFields.remove(ne);
    }
    for (NamedElement ne : allClassesConnectedToMainSigByFields) {

      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      Set<String> stepProperties = processSigPropertiesToFields(ne, propertiesByType);
      stepPropertiesBySig.put(ne.getName(), stepProperties);
    }

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
    allClassesConnectedToMainSigByFields = propertiesByClass.keySet();// Suppler, Customer
    Set<String> sigNameOfSharedFieldType = new HashSet<>(); // ie., BehaviorWithParameter
    for (Class ne : classInHierarchy) {// ParticipantTransfer/TransferProduct? Customer and Supplier
                                       // are children of Product
      sigNameOfSharedFieldType.addAll(handleClassForProecessConnector(ne, stepPropertiesBySig,
          inputs, outputs, sigWithTransferFields));
      // removing ne
      allClassesConnectedToMainSigByFields.remove(ne);
    }
    for (NamedElement ne : allClassesConnectedToMainSigByFields) {
      if (ne instanceof Class) {// no connector in primitiveType (Real, Integer)

        sigNameOfSharedFieldType.addAll(handleClassForProecessConnector(ne, stepPropertiesBySig,
            inputs, outputs, sigWithTransferFields));

      }
    }
    Set<Sig> noStepsSigs = toAlloy.handleSteps(stepPropertiesBySig, parameterFields);
    // if "no x.steps" and sig with fields with type Transfer should not have below:
    // fact {all x: BehaviorWithParameterOut | no y: Transfer | y in x.steps}
    sigWithTransferFields.addAll(noStepsSigs);
    toAlloy.handleNoTransfer(sigWithTransferFields);


    // allClassNames are used to create no inputs and no outputs later created from Sigs in
    // propertiesByClass.keySet() and classInHierarchy
    Set<String> allClassNames = Stream
        .of(propertiesByClass.keySet().stream().map(c -> c.getName()).collect(Collectors.toSet()),
            classInHierarchy.stream().map(c -> c.getName()).collect(Collectors.toSet()))
        .flatMap(x -> x.stream()).collect(Collectors.toSet());

    // may be can assume this???????????
    // assume classsInHiearchy does not have primitiveType class
    // Set<String> primitiveTypeSigNames =
    // propertiesByClass.keySet().stream().filter((c) -> (c instanceof PrimitiveType))
    // .map((c) -> c.getName()).collect(Collectors.toSet());

    // Set<String> primitiveTypeSigNames =
    // Stream
    // .of(propertiesByClass.keySet().stream().filter(c -> (c instanceof PrimitiveType))
    // .map((c) -> c.getName()).collect(Collectors.toSet()),
    // classInHierarchy.stream().filter(c -> (c instanceof PrimitiveType))
    // .map((c) -> c.getName()).collect(Collectors.toSet()))
    // .flatMap(x -> x.stream()).collect(Collectors.toSet());

    // remove sharedTransferSigType from allClassNames so the 4 facts not in BehaviorWithParameter
    // (4.1.5 MultiObjectFlow)
    // fact {all x: BehaviorWithParameter | x.i in x.inputs}
    // fact {all x: BehaviorWithParameter | x.inputs in x.i}
    // fact {all x: BehaviorWithParameter | x.i in x.outputs}
    // fact {all x: BehaviorWithParameter | x.outputs in x.i}

    // !!!!!!!!!!!!!!
    toAlloy.handleNoInputsOutputs(inputs, outputs, allClassNames, sigNameOfSharedFieldType);

    toAlloy.addBijectionInputsOutputsToContainer(inputs, outputs, allClassNames);


    // adding no steps.x
    // fact {all x: Integer | no steps.x}, fact {all x: Real | no steps.x} or {all x: Product | no
    // steps.x}
    toAlloy.handleStepClosure(this.transferingTypeSig);
    return true;

  }

  /**
   * 
   * @param ne
   * @param stepPropertiesBySig
   * @param inputs
   * @param outputs
   * @param sigWithTransferFields
   * 
   */
  private Set<String> handleClassForProecessConnector(NamedElement ne,
      Map<String, Set<String>> stepPropertiesBySig, HashMap<String, Set<String>> inputs,
      HashMap<String, Set<String>> outputs, Set<Sig> sigWithTransferFields) {

    Set<String> fieldNamesWithInputs = new HashSet<>();
    Set<String> fieldNamesWithOutputs = new HashSet<>();

    // ie., BehaviorWithParameter
    Set<String> sigNameOfSharedFieldType =
        processConnector((Class) ne, stepPropertiesBySig.get(ne.getName()), inputs, outputs,
            sigWithTransferFields, fieldNamesWithInputs, fieldNamesWithOutputs);


    // any of connectorends owned field types are the same (p1, p2:
    // BehaviorWithParameter)
    if (sigNameOfSharedFieldType.size() > 0) {
      Set<String> allfieldNames = stepPropertiesBySig.get(ne.getName()).stream()
          .filter(f -> !f.startsWith("transfer")).collect(Collectors.toSet());
      // no inputs
      for (String fieldName : allfieldNames) {
        if (!fieldNamesWithInputs.contains(fieldName)) {
          toAlloy.createNoInputsField(ne.getName(), fieldName);// fact {all x: MultipleObjectFlow |
                                                               // no
          // x.p1.inputs}
        }
        if (!fieldNamesWithOutputs.contains(fieldName)) {
          toAlloy.createNoOutputsField(ne.getName(), fieldName);
        }
      }

    }
    return sigNameOfSharedFieldType;
  }

  /**
   * 
   * @param ne Class that map to Sig
   * @param propertiesByType - Map<Type, List<Property>> map of properties by type
   * @return Set of String - field names in this sig which are having Step or Paticipant stereotypes
   */
  private Set<String> processSigPropertiesToFields(NamedElement ne,
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType) {

    PrimSig sigOfNamedElement = toAlloy.getSig(ne.getName());

    Set<String> stepProperties = new HashSet<>();
    if (propertiesByType != null) {
      for (org.eclipse.uml2.uml.Type propertyType : propertiesByType.keySet()) {
        // find property by type (ie., propetyType = Order, List<Property> = [order]);
        List<Property> propertiesSortedByType = propertiesByType.get(propertyType);

        // sort property in alphabetical order, also remove redefined properties from the sorted
        // list.
        List<String> nonRedefinedPropertyInAlphabeticalOrderPerType = new ArrayList<>();
        Set<String> parameterProperty = new HashSet<>();
        for (Property p : propertiesSortedByType) {
          if (p.getName() != null) { // Since MD allow having no name.
            if (p.getRedefinedProperties().size() == 0) {
              System.out.println("Parameter: " + ne.getName() + "." + p.getName());
              System.out.println("Step: " + p.getAppliedStereotype(STEREOTYPE_STEP));
              System.out
                  .println("ParticipantProperty: " + p.getAppliedStereotype(STEREOTYPE_PATICIPANT));
              System.out.println("Parameter: " + p.getAppliedStereotype(STEREOTYPE_PAREMETER));

              nonRedefinedPropertyInAlphabeticalOrderPerType.add(p.getName());
              if (p.getAppliedStereotype(STEREOTYPE_STEP) != null
                  || p.getAppliedStereotype(STEREOTYPE_PATICIPANT) != null) {
                stepProperties.add(p.getName());
              } else if (p.getAppliedStereotype(STEREOTYPE_PAREMETER) != null) {
                parameterProperty.add(p.getName());

                System.out.println("");
              }

            }
          } else {
            System.err.println(p.getQualifiedName()
                + "has no name, so ignored.  Please defined the name to be included");
          }
        }
        Collections.sort(nonRedefinedPropertyInAlphabeticalOrderPerType);

        if (nonRedefinedPropertyInAlphabeticalOrderPerType.size() > 0) {
          Sig.Field[] fields =
              toAlloy.addDisjAlloyFields(nonRedefinedPropertyInAlphabeticalOrderPerType,
                  propertyType.getName(), sigOfNamedElement);
          // server, Serve, SinglFooeService
          if (fields != null) { // this should not happens
            for (int j = 0; j < propertiesSortedByType.size(); j++) {
              addCardinality(propertiesSortedByType.get(j), sigOfNamedElement, fields[j].label);
              if (parameterProperty.contains(fields[j].label)) {
                this.parameterFields.add(fields[j]);
              }
            }
          }
        } else {
          for (Property p : propertiesSortedByType) {
            addCardinality(p, sigOfNamedElement, p.getName());
          }
        }
      }
    } // end processing property
    return stepProperties;
  }

  private void addCardinality(Property p, PrimSig sigOfNamedElement, String fieldName) {
    if (p.getLower() == p.getUpper())
      toAlloy.addCardinalityEqualConstraintToField(fieldName, sigOfNamedElement, p.getLower());
    else if (p.getUpper() == -1 && p.getLower() >= 1) {
      toAlloy.addCardinalityGreaterThanEqualConstraintToField(fieldName, sigOfNamedElement,
          p.getLower());
    }
  }

  /**
   * 
   * @param ne
   * @param stepFieldNames
   * @param inputs
   * @param outputs
   * @param sigWithTransferFields
   * @param fieldsWithInputs
   * @param fieldsWithOutputs
   * @param connectorendsFieldTypeOwnerFieldTypeSig - connector's property having same owner sig
   *        (ie., BehaviorWithParameter in 4.1.5 MultiObjectFlow)
   * 
   * 
   *        // For 4.1.5 MultipleObjectFlow // a connector sourceOutputProperty(i) and
   *        targetInputProperty (i)'s owner field is // p1:BehaviorWithParameter,
   *        p2:BehaviorParameter // the type (BehaviorParameter) is the same so put in
   *        connectorendsFieldTypeOwnerFieldTypeSig
   * 
   * @return Set<String> signame whose (BehaviorWithParameter) connectorend's belong to have the
   *         same type (ie., p1, p2: BehaviorWithParameter)
   */
  private Set<String> processConnector(Class ne, /* PrimSig sigOfNamedElement, */
      Set<String> stepFieldNames, HashMap<String, Set<String>> inputs,
      HashMap<String, Set<String>> outputs, Set<Sig> sigWithTransferFields,
      Set<String> fieldsWithInputs, Set<String> fieldsWithOutputs) {

    Set<String> sigNameOfSharedFieldType = new HashSet<>();

    PrimSig sigOfNamedElement = toAlloy.getSig(ne.getName());

    Set<Constraint> constraints = sysMLUtil.getAllRules(ne);
    Set<EList<Element>> oneOfSets = getOneOfRules(constraints);
    Set<org.eclipse.uml2.uml.Connector> connectors = sysMLUtil.getAllConnectors(ne);

    // process connectors with oneof first
    Set<Connector> processedConnectors = new HashSet<>();
    for (EList<Element> oneOfSet : oneOfSets) {
      handleOneOfConnectors(sigOfNamedElement, connectors, oneOfSet, processedConnectors);
    }

    // connector between p1 and p2 having the same BehaviorWithParameter, then this is set as true
    // boolean oneofConnectorsOwnerFieldTypeAreThesame = false; // default

    // boolean isOneofConnectorSourceAndTargetPropertyOwnerFieldTypeAreTheSame = false;

    // process remaining of connectors
    for (org.eclipse.uml2.uml.Connector cn : connectors) {
      if (processedConnectors.contains(cn))
        continue; // oneof connectors so not need to process
      if (ne.getInheritedMembers().contains(cn))
        continue;// ignore inherited


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
              if (source != null && target != null) {
                toAlloy.addEqual(sigOfNamedElement, source, target);
              }
            }
          } else {
            // System.out.println(ce.getDefiningEnd().getName());// transferSource
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
              // System.out.println("s: " + source + ce.getRole().getType().getName());
              sourceTypeName = ce.getRole().getType().getName();

              List<Property> ps = sysMLUtil.getPropertyPath(ce);
              // System.out.println(ps);

            } else if (definingEndName.equals("transferTarget")) {
              connector_type = CONNECTOR_TYPE.TRANSFER;
              target = endsFeatureNames.get(0);
              // System.out.println("t: " + target + " " + ce.getRole().getType().getName());
              targetTypeName = ce.getRole().getType().getName();
            }

            if (source == null || target == null)
              continue;
            if (connector_type == CONNECTOR_TYPE.HAPPENS_BEFORE) {
              handleHappensBefore(sigOfNamedElement, source, target);
            } else if (connector_type == CONNECTOR_TYPE.HAPPENS_DURING)
              handleHappensDuring(sigOfNamedElement, source, target);

            // using graph to find out what is one of
            // if (sourceCN != null && targetCN != null) {
            // Edge edge = ge.addEdge(source + target, source, target);
            // if (isOneof(constraints, sourceCN, targetCN)) {
            // edge.setAttribute("oneof", true);
            // }
            else if (connector_type == CONNECTOR_TYPE.TRANSFER) {
              Association type = cn.getType();
              String[] sourceOutputAndTargetInputProperties =
                  handleTransferAndTransferBeforeInputsAndOutputs(cn);
              sigWithTransferFields.add(sigOfNamedElement);
              addToHashMap(inputs, targetTypeName, sourceOutputAndTargetInputProperties[1]); // "targetInputProperty"
              addToHashMap(outputs, sourceTypeName, sourceOutputAndTargetInputProperties[0]);// "sourceOutputProperty",

              System.out.println(targetTypeName + " = " + sourceTypeName);
              boolean addEquals = false;
              if (targetTypeName.equals(sourceTypeName)) {
                sigNameOfSharedFieldType.add(targetTypeName);
                addEquals = true;
              }
              processConnectorInputsOutputs(sigOfNamedElement, source, target, sourceTypeName,
                  targetTypeName, sourceOutputAndTargetInputProperties, fieldsWithInputs,
                  fieldsWithOutputs, addEquals);
              // isOneofConnectorSourceAndTargetPropertyOwnerFieldTypeAreTheSame = true;


              if (type.getName().equals("Transfer")) {
                handleTransferFieldAndFn(sigOfNamedElement, source, target, stepFieldNames);
              } else if (type.getName().equals("TransferBefore")) {
                // String[] sourceOutputAndTargetInputProperties =
                // handleTransferAndTransferBeforeInputsAndOutputs(cn);
                handleTransferBeforeFieldAndFn(sigOfNamedElement, source, target, stepFieldNames);
                // sigWithTransferFields.add(sigOfNamedElement);
                // addToHashMap(inputs, targetTypeName, sourceOutputAndTargetInputProperties[1]);
                // addToHashMap(outputs, sourceTypeName, sourceOutputAndTargetInputProperties[0]);
              }
            }
          }
        } // end of connectorEnd
      } // end of Connector
    } // org.eclipse.uml2.uml.Connector
    // handleHappensBefore(ge, thisSig); //getting happens before using graph
    // return isOneofConnectorSourceAndTargetPropertyOwnerFieldTypeAreTheSame == true
    // ? sigOfNamedElement
    // : null;
    return sigNameOfSharedFieldType;
  }

  /**
   * only should be called sourceTypeName == targetTypeName
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
  private void processConnectorInputsOutputs(PrimSig sigOfNamedElement, String source,
      String target, String sourceTypeName, String targetTypeName,
      String[] sourceOutputAndTargetInputProperties, Set<String> fieldsWithInputs,
      Set<String> fieldsWithOutputs, boolean addEquals) {

    // targetTypeName = sourceTypeName (ie., BehaviorWithParameter)

    // source => outputs
    // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}
    System.out.println(
        source + ": " + sourceTypeName + " with " + sourceOutputAndTargetInputProperties[0]);

    Field outputFrom = AlloyUtils.getFieldFromSig(source, sigOfNamedElement); // p1
    if (outputFrom != null) {
      if (!fieldsWithOutputs.contains(outputFrom.label)) {// handle duplicate p1 having two outputs
        fieldsWithOutputs.add(outputFrom.label);
        Field outputTo = AlloyUtils.getFieldFromSig(sourceOutputAndTargetInputProperties[0],
            toAlloy.getSig(sourceTypeName));// i
        // fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p1, x.p1.i]}
        toAlloy.createBijectionFilteredOutputsAndAddToOverallFact(sigOfNamedElement, outputFrom,
            outputFrom, outputFrom.join(outputTo), outputTo, addEquals);
      }
    }

    // target => inputs
    // fact {all x: MultipleObjectFlow | all p: x.p2 | p.i = p.inputs}
    System.out.println(
        target + ": " + targetTypeName + " with " + sourceOutputAndTargetInputProperties[1]);

    Field inputFrom = AlloyUtils.getFieldFromSig(target, sigOfNamedElement);// p2
    if (inputFrom != null) {
      if (!fieldsWithInputs.contains(inputFrom.label)) { // handle duplicate p4 is having two inputs
        fieldsWithInputs.add(inputFrom.label);
        Field inputTo = AlloyUtils.getFieldFromSig(sourceOutputAndTargetInputProperties[1], // i
            toAlloy.getSig(targetTypeName));
        // fact {all x: MultipleObjectFlow | bijectionFiltered[inputs, x.p2, x.p2.i]}
        toAlloy.createBijectionFilteredInputsAndAddToOverallFact(sigOfNamedElement, inputFrom,
            inputFrom, inputFrom.join(inputTo), inputTo, addEquals);
      }
    }

  }

  private Sig.Field handTransferFieldAndFnPrep(PrimSig sig, String source, String target,
      Set<String> stepFieldNames) {
    String fieldName = "transfer" + firstCharUpper(source) + firstCharUpper(target);
    stepFieldNames.add(fieldName);
    Sig.Field transferField = toAlloy.addAlloyTransferField(fieldName, sig);
    return transferField;
  }

  private void handleTransferFieldAndFn(PrimSig sig, String source, String target,
      Set<String> stepFieldNames) {
    // String fieldName = "transfer" + firstCharUpper(source) + firstCharUpper(target);
    // stepFieldNames.add(fieldName);
    // Sig.Field transferField = toAlloy.addAlloyTransferField(fieldName, sig);

    Sig.Field transferField = handTransferFieldAndFnPrep(sig, source, target, stepFieldNames);
    toAlloy.createFnForTransferAndAddToOverallFact(sig, transferField, source, target);
  }

  /**
   * 
   * @param sig
   * @param source
   * @param target
   * @param stepFieldNames the transferField to be added (ie., p1, p2, p3, p4 and add transferP1P2)
   */
  private void handleTransferBeforeFieldAndFn(PrimSig sig, String source, String target,
      Set<String> stepFieldNames) {
    // String fieldName = "transfer" + firstCharUpper(source) + firstCharUpper(target);
    // stepFieldNames.add(fieldName);
    // // Sig.Field transferField = toAlloy.addAlloyTransferBeforeField(fieldName, sig);
    // Sig.Field transferField = toAlloy.addAlloyTransferField(fieldName, sig);
    Sig.Field transferField = handTransferFieldAndFnPrep(sig, source, target, stepFieldNames);
    toAlloy.createFnForTransferBeforeAndAddToOverallFact(sig, transferField, source, target);
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
  private void processClassToSig(NamedElement umlElement,
      Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass) {

    if (umlElement instanceof org.eclipse.uml2.uml.Class) {
      Set<org.eclipse.uml2.uml.Property> atts =
          sysMLUtil.getOwnedAttributes((org.eclipse.uml2.uml.Class) umlElement);
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
          toAlloy.createAlloySig(eType.getName(),
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
    List<String> endsFeatureNames = end.getCorrectedFeaturePath(owner).stream()
        .map(f -> f.getName()).collect(Collectors.toList());
    String name = endsFeatureNames.get(0);
    return name;

  }

  private void handleOneOfConnectors(PrimSig ownerSig, Set<Connector> connectors,
      List<Element> oneOfSet, Set<Connector> processedConnectors) {

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

      Expr afterExpr =
          /* ownerSig.domain( */AlloyUtils.getFieldFromSig(targetNames.get(0), ownerSig)/* ) */;
      for (String sourceName : sourceNames) {
        if (!sourceInTarget.contains(sourceName)) {
          beforeExpr_filtered = beforeExpr_filtered == null
              ? /* ownerSig.domain( */AlloyUtils.getFieldFromSig(sourceName, ownerSig)// )
              : beforeExpr_filtered.plus(
                  /* ownerSig.domain( */AlloyUtils.getFieldFromSig(sourceName, ownerSig))/* ) */;
        }
      }
      for (String sourceName : sourceNames) {
        beforeExpr_all = beforeExpr_all == null
            ? /* ownerSig.domain( */AlloyUtils.getFieldFromSig(sourceName, ownerSig)// )
            : beforeExpr_all.plus(
                /* ownerSig.domain( */AlloyUtils.getFieldFromSig(sourceName, ownerSig))/* ) */;
      }

      toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr_filtered,
          afterExpr); // not include source in source
      toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig,
          beforeExpr_all, afterExpr);
    } else if (targetInSource.size() > 0 && !isSourceSideOneOf) {
      Expr afterExpr_filtered = null; // p3
      Expr afterExpr_all = null; // p2 + p3

      Expr beforeExpr =
          /* ownerSig.domain( */AlloyUtils.getFieldFromSig(sourceNames.get(0), ownerSig)/* ) */;
      for (String targetName : targetNames) {
        if (!targetInSource.contains(targetName)) {
          afterExpr_filtered = afterExpr_filtered == null
              ? /* ownerSig.domain( */AlloyUtils.getFieldFromSig(targetName, ownerSig)// )
              : afterExpr_filtered.plus(
                  /* ownerSig.domain( */AlloyUtils.getFieldFromSig(targetName, ownerSig))/* ) */;
        }
      }
      for (String targetName : targetNames) {
        afterExpr_all = afterExpr_all == null
            ? /* ownerSig.domain( */AlloyUtils.getFieldFromSig(targetName, ownerSig)// )
            : afterExpr_all.plus(
                /* ownerSig.domain( */AlloyUtils.getFieldFromSig(targetName, ownerSig))/* ) */;
      }

      toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr,
          afterExpr_all); // not include target in source
      toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr,
          afterExpr_filtered);

    } // non self-loop
    else {
      Expr beforeExpr = null;
      Expr afterExpr = null;
      if (isSourceSideOneOf) { // sourceSide need to be combined
        afterExpr =
            /* ownerSig.domain( */AlloyUtils.getFieldFromSig(targetNames.get(0), ownerSig)/* ) */;
        for (String sourceName : sourceNames) {
          beforeExpr = beforeExpr == null
              ? /* ownerSig.domain( */AlloyUtils.getFieldFromSig(sourceName, ownerSig)// )
              : beforeExpr.plus(
                  /* ownerSig.domain( */AlloyUtils.getFieldFromSig(sourceName, ownerSig))/* ) */;
        }

      } else {
        for (String targetName : targetNames) {
          afterExpr = afterExpr == null
              ? /* ownerSig.domain( */AlloyUtils.getFieldFromSig(targetName, ownerSig)// )
              : afterExpr.plus(
                  /* ownerSig.domain( */AlloyUtils.getFieldFromSig(targetName, ownerSig))/* ) */;
        }
        beforeExpr =
            /* ownerSig.domain( */AlloyUtils.getFieldFromSig(sourceNames.get(0), ownerSig)/* ) */;
      }

      toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr,
          afterExpr);
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
      if (b.contains(s))
        contained.add(s);
    }
    return contained;
  }


  // sig BuffetService
  private void handleHappensBefore(PrimSig sig, String source, String target) {

    System.out.println("looking for source" + source + " in " + sig.label);
    System.out.println("looking for target" + target + " in " + sig.label);

    Field sourceField = AlloyUtils.getFieldFromSig(source, sig); // FoodService <: prepare
    Field targetField = AlloyUtils.getFieldFromSig(target, sig);

    if (sourceField != null && targetField != null) {
      // Expr sexpr = sig.domain(sourceField);
      // Expr texpr = sig.domain(targetField);
      // toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(sig, sexpr, texpr);

      toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(sig, sourceField,
          targetField);
    } else
      System.err.println("source or target for HappensBefore not in " + sig.label);
  }

  private void handleHappensDuring(PrimSig sig, String source, String target) {

    Field sourceField = AlloyUtils.getFieldFromSig(source, sig);
    Field targetField = AlloyUtils.getFieldFromSig(target, sig);

    if (sourceField != null && targetField != null) {
      toAlloy.createBijectionFilteredHappensDuringAndAddToOverallFact(sig, sourceField,
          targetField);
    } else
      System.err.println("source or target for handleHappensDuring not in " + sig.label);

  }



  private String[] handleTransferAndTransferBeforeInputsAndOutputs(org.eclipse.uml2.uml.Connector cn
  /* PrimSig sig , String source, String target, String sourceTypeName, String targetTypeName */) {


    String[] sourceOutputAndTargetInputProperties = {"", ""};
    String[] stTagNames = {"sourceOutputProperty", "targetInputProperty"};// , "itemType"}; this
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
    if (sos != null && tis != null) {
      for (Property p : sos) {
        sosOwner = ((org.eclipse.uml2.uml.Class) p.getOwner());
        String owner = ((org.eclipse.uml2.uml.Class) p.getOwner()).getName();
        System.out.println(owner + " | x." + p.getName() + "= x.output");
        // B2 | x.vout= x.output
        // toAlloy.addOutputs(owner, /* "Supplier" */ p.getName() /* "suppliedProduct" */);
        sourceOutputAndTargetInputProperties[0] = p.getName(); // = x.outputs
        transferingTypeSig.add(p.getType().getName());
        break; // assumption is having only one
      }
      for (Property p : tis) {
        tisOwner = ((org.eclipse.uml2.uml.Class) p.getOwner());
        String owner = ((org.eclipse.uml2.uml.Class) p.getOwner()).getName();
        System.out.println(owner + " | x." + p.getName() + "= x.input: " + p.getName());
        // B | x.voutzzzzz= x.input
        // toAlloy.addInputs(owner, /* "Customer" */p.getName() /* "receivedProduct" */);
        transferingTypeSig.add(p.getType().getName());
        sourceOutputAndTargetInputProperties[1] = p.getName(); // = x.inputs
        break; // assumption is having only one
      }
      // preventing fact {all x:B| x.vin = x.output}} to be generated from <<ItemFlow>> between
      // B.vin and b1.vin.
      // but having fact {all x: B1| x.vin = x.inputs} is ok
      EList<Property> atts = sosOwner.getAttributes();
      for (Property att : atts)
        if (att.getType() == tisOwner) {
          sourceOutputAndTargetInputProperties[0] = null; // overwrite
          break;
        }
      // preventing fact {all x: B |x.vout = x.inputs} - to be generated from <<ItemFlow>> between
      // B.vout and b2.vout
      // but having fact {all x: B2|x.vout = x.outputs} is ok
      atts = tisOwner.getAttributes();
      for (Property att : atts)
        if (att.getType() == sosOwner) {
          sourceOutputAndTargetInputProperties[1] = null; // overwrite
          break;
        }

    }
    return sourceOutputAndTargetInputProperties;
  }



  /**
   * 
   * @param element
   * @param streotypeName
   * @param tagNames
   * @return null is not Stereotype with streotypeName applied to the element
   */
  private Map<String, List<Property>> getStreotypePropertyValues(Element element,
      String streotypeName, String[] tagNames) {

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

  public static void addToHashMap(HashMap<String, Set<String>> map, String key, String value) {
    if (value == null)
      return;
    Set<String> vs;
    if (map.containsKey(key))
      vs = map.get(key);
    else {
      vs = new HashSet<String>();
      map.put(key, vs);
    }
    vs.add(value);


  }


  /**
   * 
   * @param element (ie., Connector)
   * @param streotypeName (ie,. Model::OBM::ItemFlow)
   * @param propertyName (ie., sourceOutputProperty)
   * @return null if no Stereotype is applied to element or List<Property>
   */
  // private List<Property> getStreotypePropertyValues(Element element, String streotypeName,
  // String propertyName) {
  // List<Property> results = new ArrayList<>();
  // Stereotype st = null;
  // if ((st = element.getAppliedStereotype(streotypeName)) != null) {
  // List<Object> properties = (List<Object>) (element.getValue(st, propertyName));
  // for (Object property : properties)
  // results.add((Property) property);
  // return results;
  // }
  // return null;
  // }

  // commented out getting happens before using graph

  // private boolean isOneOf(Constraint c) {
  // ValueSpecification vs = c.getSpecification();
  // if (vs instanceof OpaqueExpressionImpl) {
  // edu.umd.omgutil.uml.OpaqueExpression omgE = (OpaqueExpression) sysmladapter.mapObject(vs);
  // if (omgE.getBodies().contains("OneOf"))
  // return true;
  // }
  // return false;
  // }

  // if any of connector end is startCN or endCN then specify connector is one of
  // private boolean isOneof(Set<Constraint> constraints, ConnectorEnd startCN, ConnectorEnd endCN)
  // {
  // for (Constraint ct : constraints) {
  // if (isOneOf(ct)) {
  // EList<Element> constraintedElements = ct.getConstrainedElements();
  // if (constraintedElements.size() == 2) {
  // if (constraintedElements.get(0) == startCN || constraintedElements.get(0) == endCN
  // || constraintedElements.get(1) == startCN || constraintedElements.get(1) == endCN)
  // return true;
  // }
  // }
  // }
  // return false;
  // }

  /*
   * private void handleHappensBefore(Graph2AlloyExpr ge, Sig thisSig) {
   * System.out.println("HB Function: "); Map<IObject, IObject> happensBeforeFnInfo =
   * ge.getHappensBeforeFunction(); // before, after // Graph2AlloyExpr.print(happensBeforeFnInfo);
   * for (IObject before : happensBeforeFnInfo.keySet()) { before.sort(); List<Expr> lbefore =
   * toExprs(before, thisSig); IObject after = happensBeforeFnInfo.get(before); after.sort();
   * List<Expr> lafter = toExprs(after, thisSig);
   * 
   * for (int i = 0; i < lbefore.size(); i++) { Expr beforeExpr = lbefore.get(i); for (int j = 0; j
   * < lafter.size(); j++) { Expr afterExpr = lafter.get(j);
   * toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(thisSig, beforeExpr, afterExpr);
   * } } } // end iterator
   * 
   * System.out.println("HB Inverse Function: "); Map<IObject, IObject> happensBeforeInvFnInfo =
   * ge.getHappensBeforeInvFunction(); // before, Graph2AlloyExpr.print(happensBeforeInvFnInfo); //
   * after
   * 
   * for (IObject before : happensBeforeInvFnInfo.keySet()) { before.sort(); List<Expr> lbefore =
   * toExprs(before, thisSig); IObject after = happensBeforeInvFnInfo.get(before); after.sort();
   * List<Expr> lafter = toExprs(after, thisSig);
   * 
   * for (int i = 0; i < lbefore.size(); i++) { Expr beforeExpr = lbefore.get(i); for (int j = 0; j
   * < lafter.size(); j++) { Expr afterExpr = lafter.get(j);
   * 
   * // toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(mainSig, // beforeExpr, //
   * afterExpr); toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(thisSig,
   * beforeExpr, afterExpr); } } } // end iterator }
   * 
   * // assume if 1st list field in _aSig, all fields are also in _aSig public List<Expr>
   * toExprs(IObject _o, Sig _aSig) { List<Expr> exprs = new ArrayList<>(); if (_o instanceof
   * OListOR) { ONode onode = (ONode) ((OListOR) _o).get(0); Sig.Field f =
   * toAlloy.getField(onode.getName()); if (f.sig == _aSig) { Expr expr = _aSig.domain(f); for (int
   * i = 1; i < ((OListOR) _o).size(); i++) { onode = (ONode) ((OListOR) _o).get(i); expr =
   * expr.plus(_aSig.domain(toAlloy.getField(onode.getName()))); } exprs.add(expr); }
   * 
   * } else if (_o instanceof OListAND) { ONode onode = (ONode) ((OListAND) _o).get(0); Sig.Field f
   * = toAlloy.getField(onode.getName()); if (f.sig == _aSig) { exprs.add(_aSig.domain(f)); for (int
   * i = 1; i < ((OListAND) _o).size(); i++) { onode = (ONode) ((OListAND) _o).get(i); f =
   * toAlloy.getField(onode.getName()); exprs.add(_aSig.domain(f)); } } } else { ONode onode =
   * (ONode) _o; Sig.Field f = toAlloy.getField(onode.getName()); if (f.sig == _aSig)
   * exprs.add(_aSig.domain(f)); } return exprs; }
   */

}


