package edu.gatech.gtri.obm.alloy.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;

public class ProcessConnectorMethod {

  private enum CONNECTOR_TYPE {
    HAPPENS_BEFORE, HAPPENS_DURING, TRANSFER;
  }

  private static String STEREOTYPE_BINDDINGCONNECTOR = "SysML::BindingConnector";
  private static String STEREOTYPE_ITEMFLOW = "Model::OBM::ItemFlow";
  private static String STEREOTYPE_OBJECTFLOW = "Model::OBM::ObjectFlow";

  // connectors
  // key = Signame, values = propertyNames
  HashMap<String, Set<String>> connectorTargetInputPropertyNamesByClassName; // collect field type Sig having a
  // transfer connector
  // with transferTarget "Customer"
  HashMap<String, Set<String>> connectorSourceOutputPrpertyNamesByClassName; // collect field type Sig having a
  // transfer connector
  Set<Sig> sigWithTransferField;

  Map<Field, Set<Field>> fieldWithInputs; // key = prepare, value=
  // [preparedFoodItem,prepareDestination]
  Map<Field, Set<Field>> fieldWithOutputs; // key = order,
  // value=[orderAmount,
  // orderDestination,
  // orderedFoodItem]

  Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType;

  /**
   * A dictionary contains signature name as key and a set of fact expression as value.
   */
  Map<String, Set<Expr>> sigToFactsMap;

  /**
   * A set of connectors redefined by children so that the connectors are ignored by the parent.
   */
  Set<Connector> redefinedConnectors;
  Class classMapToSignature;
  Set<PrimSig> leafSigs;
  ToAlloy toAlloy;
  List<String> messages;
  Set<Field> parameterFields;

  /**
   * A set of string representing the type of transfer fields (ie., Integer)
   */
  Set<String> transferingTypeSig;


  /**
   * A dictionary contains signature name as key and a set of transfer field names as value.
   */
  Map<String, Set<String>> sigToTransferFieldMap;

  Map<String, Set<String>> stepPropertiesBySig;

  protected PrimSig sigOfNamedElement;

  protected ProcessConnectorMethod(Class _c, Set<Connector> _redefinedConnectors,
      Set<PrimSig> _leafSigs, ToAlloy _toAlloy, Map<String, Set<Expr>> _sigToFactsMap,
      Set<Field> _parameterFields, Map<String, Set<String>> _sigToTransferFieldMap,
      Map<String, Set<String>> _stepPropertiesBySig) {

    // passed by reference from XXX used and updated during in instance of this class
    redefinedConnectors = _redefinedConnectors;

    // pass from xxx and used during each instance of this class
    parameterFields = _parameterFields;
    leafSigs = _leafSigs;
    toAlloy = _toAlloy;

    classMapToSignature = _c;


    sigToTransferFieldMap = _sigToTransferFieldMap;
    stepPropertiesBySig = _stepPropertiesBySig;
    sigToFactsMap = _sigToFactsMap;

    sigOfNamedElement = this.toAlloy.getSig(_c.getName());

    connectorTargetInputPropertyNamesByClassName = new HashMap<>();
    connectorSourceOutputPrpertyNamesByClassName = new HashMap<>();
    sigWithTransferField = new HashSet<>();
    fieldWithInputs = new HashMap<>();
    fieldWithOutputs = new HashMap<>();
    sigNameWithTransferConnectorWithSameInputOutputFieldType = new HashSet<>();
    messages = new ArrayList<String>();
    transferingTypeSig = new HashSet<String>();
  }

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
  protected void processConnector(
      SysMLAdapter _sysmladapter,
      SysMLUtil _sysmlUtil) {


    // Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType = new HashSet<>();
    //
    // start handling one of connectors
    //
    // find sig's constraint
    Set<Constraint> constraints = _sysmlUtil.getAllRules(this.classMapToSignature);
    Set<EList<Element>> oneOfSets = SysMLAdapterUtils.getOneOfRules(_sysmladapter, constraints); // EList<ConnectorEnd> [ [start, eat] or [order, end]]

    Set<org.eclipse.uml2.uml.Connector> connectors =
        _sysmlUtil.getOwnedConnectors(this.classMapToSignature);

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
      handleOneOfConnectors(oneOfSet, oneOfConnectors, oneSourceProperties,
          oneTargetProperties);
    }
    //
    // end of handling one of connectors
    //

    // process remaining of connectors
    for (org.eclipse.uml2.uml.Connector cn : connectors) {
      if (oneOfConnectors.contains(cn))
        continue; // oneof connectors are already handled so skip here
      if (this.classMapToSignature.getInheritedMembers().contains(cn))
        continue;// ignore inherited

      // while translating IFSingleFoolService and processing connectors for IFFoodService,
      // connectors creating "transferPrepareServe" and "transferServeEat" should be ignored because
      // it they are redefined in IFSingleFoodService
      if (this.redefinedConnectors.contains(cn))
        continue; // ignore


      CONNECTOR_TYPE connector_type = null;
      edu.umd.omgutil.uml.Element omgE = _sysmladapter.mapObject(cn);
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
                this.toAlloy.addEqualFact(sigOfNamedElement, source, target);
            }
          } else {

            String definingEndName = ce.getDefiningEnd().getName();
            edu.umd.omgutil.uml.ConnectorEnd end =
                (edu.umd.omgutil.uml.ConnectorEnd) _sysmladapter.mapObject(ce);
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
              this.toAlloy.createBijectionFilteredHappensBefore(sigOfNamedElement, sourceField,
                  targetField);
            } else if (connector_type == CONNECTOR_TYPE.HAPPENS_DURING)
              this.toAlloy.createBijectionFilteredHappensDuring(sigOfNamedElement, sourceField,
                  targetField);

            else if (connector_type == CONNECTOR_TYPE.TRANSFER) {
              handleTransferConnector(cn,
                  sourceTypeName,
                  targetTypeName,
                  sourceField,
                  targetField, source, target);
            }
          }
        } // end of connectorEnd
      } // end of Connector
    } // org.eclipse.uml2.uml.Connector

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
  private void handleOneOfConnectors(List<Element> oneOfSet,
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
          // if (ces == null) { // this should not happens
          // this.messages.add("A connector " + cn.getQualifiedName()
          // + " does not have two connector ends, so ignored.");
          // return;
          // }
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
      afterExpr =
          AlloyUtils.getFieldFromSigOrItsParents(targets.get(0).getName(), this.sigOfNamedElement);
      List<String> sourceNames = // sorting source names alphabetically = how to be write out
          sources.stream().map(e -> e.getName()).sorted().collect(Collectors.toList());
      for (String sourceName : sourceNames) {
        beforeExpr = beforeExpr == null
            ? AlloyUtils.getFieldFromSigOrItsParents(sourceName,
                this.sigOfNamedElement)
            : beforeExpr.plus(AlloyUtils
                .getFieldFromSigOrItsParents(sourceName, this.sigOfNamedElement));
      }

    } else { // targetSide needs to be combined
      List<String> targetNames = // sorting target names alphabetically = to be write out
          targets.stream().map(e -> e.getName()).sorted().collect(Collectors.toList());
      for (String targetName : targetNames) {
        afterExpr = afterExpr == null
            ? AlloyUtils.getFieldFromSigOrItsParents(targetName,
                this.sigOfNamedElement)
            : afterExpr.plus(AlloyUtils
                .getFieldFromSigOrItsParents(targetName, this.sigOfNamedElement));
      }
      beforeExpr = AlloyUtils
          .getFieldFromSigOrItsParents(sources.get(0).getName(), this.sigOfNamedElement);
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
        this.toAlloy.createBijectionFilteredHappensBefore(this.sigOfNamedElement, beforeExpr,
            afterExpr);
      } else {
        for (ConnectableElement ce : oneSourceProperties) {
          // need fn a -> b or a ->c
          Expr beforeExpr_modified = AlloyUtils.getFieldFromSigOrItsParents(ce.getName(),
              this.sigOfNamedElement); // start
          this.toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(
              this.sigOfNamedElement,
              beforeExpr_modified,
              afterExpr);// order
        }
        // inverse fn a + b -> c
        // fact {all x: OFControlLoopFoodService | inverseFunctionFiltered[happensBefore, x.a + x.b, x.c]}
        this.toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(
            this.sigOfNamedElement,
            beforeExpr, // a + b
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
        this.toAlloy.createBijectionFilteredHappensBefore(this.sigOfNamedElement, beforeExpr,
            afterExpr);
      } else {
        // fn a -> b + c
        // fact {all x: OFControlLoopFoodService | functionFiltered[happensBefore, x.a, x.b + x.c]}
        this.toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(this.sigOfNamedElement,
            beforeExpr,
            afterExpr);

        // inversefn a -> b or a -> c
        for (ConnectableElement ce : oneTargetProperties) {
          Expr afterExpr_modified = AlloyUtils.getFieldFromSigOrItsParents(ce.getName(),
              this.sigOfNamedElement);// end
          this.toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(
              this.sigOfNamedElement,
              beforeExpr,
              afterExpr_modified);
        }
      }
    }

  }



  private void handleTransferConnector(Connector cn,
      String sourceTypeName, String targetTypeName,
      Field sourceField,
      Field targetField, String source, String target) {// , Map<Field, Set<Field>> fieldsWithInputs,
    // Map<Field, Set<Field>> fieldsWithOutputs) {
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

    List<Set<String>> sourceOutputAndTargetInputProperties =
        handleTransferAndTransferBeforeInputsAndOutputs(cn);
    this.sigWithTransferField.add(sigOfNamedElement);

    // add to inputs map where key = tragetTypeName and values = targetInputProperty names
    Utils.addToHashMap(this.connectorTargetInputPropertyNamesByClassName, targetTypeName,
        sourceOutputAndTargetInputProperties.get(1));
    // add to output map where key = sourceTypeName and values = sourceOutputProperty names
    Utils.addToHashMap(this.connectorSourceOutputPrpertyNamesByClassName, sourceTypeName,
        sourceOutputAndTargetInputProperties.get(0));

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
          processConnectorInputsOutputs(sourceField, targetField,
              sourceTypeName,
              targetTypeName, sourceOutputAndTargetInputProperties, addEquals, true);

    } else { // non leaf
      targetInputsSourceOutputsFields = processConnectorInputsOutputs(
          sourceField, targetField, sourceTypeName, targetTypeName,
          sourceOutputAndTargetInputProperties, addEquals, false);
      // findInputAndOutputsFields(sigOfNamedElement, source, target, sourceTypeName,
      // targetTypeName, sourceOutputAndTargetInputProperties);
      toBeInherited = true;
    }
    Association type = cn.getType();
    if (type.getName().equals("Transfer")) {
      Sig.Field transferField =
          handTransferFieldAndFnPrep(source, target);
      addToSigToFactsMap(sigOfNamedElement.label,
          this.toAlloy.addTransferFacts(sigOfNamedElement, transferField, source, target,
              targetInputsSourceOutputsFields, toBeInherited));
    } else if (type.getName().equals("TransferBefore")) {
      Sig.Field transferField =
          handTransferFieldAndFnPrep(source, target);
      addToSigToFactsMap(sigOfNamedElement.label,
          this.toAlloy.addTransferBeforeFacts(sigOfNamedElement, transferField, source, target,
              targetInputsSourceOutputsFields, toBeInherited));
    }
  }

  private Sig.Field handTransferFieldAndFnPrep(String source, String target) {
    String fieldName = "transfer" + Utils.firstCharUpper(source) + Utils.firstCharUpper(target);
    // adding transferFields in stepProperties
    stepPropertiesBySig.get(this.sigOfNamedElement.label).add(fieldName);
    addSigToTransferFieldsMap(fieldName);
    Sig.Field transferField = AlloyUtils.addTransferField(fieldName, this.sigOfNamedElement);
    return transferField;
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
  private List<Set<Field>> processConnectorInputsOutputs(Field sourceField,
      Field targetField,
      String sourceTypeName, String targetTypeName,
      List<Set<String>> sourceOutputAndTargetInputProperties,
      boolean addEquals, boolean isLeaf) {



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
        if (isLeaf
            && !AlloyUtils.containsBothKeyAndValue(this.fieldWithOutputs, sourceField, outputTo)) {
          this.fieldWithOutputs.computeIfAbsent(sourceField, v -> new HashSet<Field>())
              .add(outputTo);
          // addToHashMap(fieldsWithOutputs, sourceField, outputTo);
          toAlloy.createBijectionFilteredOutputs(this.sigOfNamedElement, sourceField,
              sourceField.join(outputTo));
          if (addEquals)
            toAlloy.createInField(this.sigOfNamedElement, sourceField, sourceField.join(outputTo),
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
        if (isLeaf
            && !AlloyUtils.containsBothKeyAndValue(this.fieldWithInputs, targetField, inputTo)) {
          // addToHashMap(fieldsWithInputs, targetField, inputTo);
          this.fieldWithInputs.computeIfAbsent(targetField, v -> new HashSet<Field>()).add(inputTo);
          toAlloy.createBijectionFilteredInputs(this.sigOfNamedElement, targetField,
              targetField.join(inputTo));
          if (addEquals) {
            toAlloy.createInField(this.sigOfNamedElement, targetField, targetField.join(inputTo),
                inputTo, Alloy.oinputs);// );
          }
        }
      }
    }
    return List.of(addOutputToFields, addInputToFields);
  }

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
   * 
   * @param sig
   * @param fieldName
   */
  private void addSigToTransferFieldsMap(String fieldName) {
    Set<String> tFields;
    if ((tFields = sigToTransferFieldMap.get(this.sigOfNamedElement.label)) == null) {
      tFields = new HashSet<>();
      tFields.add(fieldName);
      sigToTransferFieldMap.put(this.sigOfNamedElement.label, tFields);
    } else
      tFields.add(fieldName);
  }


  protected HashMap<String, Set<String>> getConnectorTargetInputPropertyNamesByClassName() {
    return connectorTargetInputPropertyNamesByClassName;
  }


  protected HashMap<String, Set<String>> getConnectorSourceOutputPrpertyNamesByClassName() {
    return connectorSourceOutputPrpertyNamesByClassName;
  }

  protected Map<Field, Set<Field>> getFieldWithInputs() {
    return fieldWithInputs;
  }


  protected Map<Field, Set<Field>> getFieldWithOutputs() {
    return fieldWithOutputs;
  }

  protected Set<String> getSigNameWithTransferConnectorWithSameInputOutputFieldType() {
    return sigNameWithTransferConnectorWithSameInputOutputFieldType;
  }

  protected Set<Sig> getSigWithTransferField() {
    return sigWithTransferField;
  }


  protected List<String> getMessages() {
    return messages;
  }


  protected Set<String> getTransferingTypeSig() {
    return transferingTypeSig;
  }

  protected PrimSig getSigOfNamedElement() {
    return sigOfNamedElement;
  }



}
