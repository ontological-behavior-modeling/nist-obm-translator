package edu.gatech.gtri.obm.alloy.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Property;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;


/**
 * A handler for a transfer connector.
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 *
 */
public class ConnectorHandler_Transfer {

  private static String STEREOTYPE_ITEMFLOW = "Model::OBM::ItemFlow";
  private static String STEREOTYPE_OBJECTFLOW = "Model::OBM::ObjectFlow";


  ToAlloy toAlloy;
  // transfer connector
  Set<Sig> sigWithTransferField;
  /**
   * A dictionary contains signature name as key and a set of transfer field names as value.
   */
  Map<String, Set<String>> sigToTransferFieldMap;
  /**
   * A set of connectors redefined by children so that the connectors are ignored by the parent. This variable is initialized in ConnectorsHandler and updated and used by a ConnectorHandler while
   * handling each connector at a time.
   */
  Set<Connector> redefinedConnectors;
  Map<Field, Set<Field>> fieldWithInputs; // key = prepare, value=
  // [preparedFoodItem,prepareDestination]
  Map<Field, Set<Field>> fieldWithOutputs; // key = order,
  // value=[orderAmount,
  // orderDestination,
  // orderedFoodItem]
  Set<Field> parameterFields;

  // connectors
  // key = Signame, values = propertyNames
  HashMap<String, Set<String>> connectorTargetInputPropertyNamesByClassName; // collect field type Sig having a
  // transfer connector
  // with transferTarget "Customer"
  HashMap<String, Set<String>> connectorSourceOutputPrpertyNamesByClassName; // collect field type Sig having a


  Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType;

  /**
   * A dictionary contains signature name as key and a set of fact expression as value.
   */
  Map<String, Set<Expr>> sigToFactsMap;
  /**
   * A set of string representing the type of transfer fields (ie., Integer)
   */
  Set<String> transferingTypeSig;

  protected Set<String> transferFieldNames;

  List<String> messages;


  /**
   * Constructor
   * 
   * @param _toAlloy
   * @param _sigToFactsMap
   * @param _sigToTransferFieldMap
   * @param _redefinedConnectors
   * @param _parameterFields
   * @param _messages
   */
  protected ConnectorHandler_Transfer(ToAlloy _toAlloy, Map<String, Set<Expr>> _sigToFactsMap,
      Map<String, Set<String>> _sigToTransferFieldMap, Set<Connector> _redefinedConnectors,
      Set<Field> _parameterFields, List<String> _messages) {
    toAlloy = _toAlloy;
    sigToFactsMap = _sigToFactsMap;
    sigToTransferFieldMap = _sigToTransferFieldMap;
    redefinedConnectors = _redefinedConnectors;
    parameterFields = _parameterFields;
    messages = _messages;

    // initialize
    connectorTargetInputPropertyNamesByClassName = new HashMap<>();
    connectorSourceOutputPrpertyNamesByClassName = new HashMap<>();
    sigWithTransferField = new HashSet<>();
    sigNameWithTransferConnectorWithSameInputOutputFieldType = new HashSet<>();
    transferingTypeSig = new HashSet<String>();
  }

  /**
   * Reset required instance variables for new signature.
   */
  protected void reset() {
    transferFieldNames = new HashSet<>();
    fieldWithInputs = new HashMap<>();
    fieldWithOutputs = new HashMap<>();
  }

  /**
   * Add transfer transfer or transferbefore facts to alloy.
   * 
   * @param cn (Connector) - a connector to process
   * @param sigOfClass(PrimSig) - the owner of the connector
   * @param isSigLeaf(boolean) - boolean true if the signature is leaf, otherwise false
   * @param sourceTypeName(String) - the source connector end's type name
   * @param targetTypeName(String) - the target connector end's type name
   * @param sourceField(Field) - the source field if belong to sigOfClass, possible to be null
   * @param targetField(Field) - the target field if belong to sigOfClass, possible to be null
   * @param source(String) - the sourceOutputProperty name
   * @param target(String) - the targetInputProperty name
   */
  protected void handleTransferConnector(Connector cn, PrimSig sigOfClass, boolean isSigLeaf,
      String sourceTypeName, String targetTypeName,
      Field sourceField,
      Field targetField, String source, String target) {


    this.sigWithTransferField.add(sigOfClass);

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

    // add to inputs map where key = tragetTypeName and values = targetInputProperty names
    if (sourceOutputAndTargetInputProperties.get(1) != null) {
      // && sourceOutputAndTargetInputProperties.get(1).size() != 0) {
      this.connectorTargetInputPropertyNamesByClassName
          .computeIfAbsent(targetTypeName, v -> new HashSet<String>())
          .addAll(sourceOutputAndTargetInputProperties.get(1));
    }

    // add to output map where key = sourceTypeName and values = sourceOutputProperty names
    if (sourceOutputAndTargetInputProperties.get(0) != null) {
      // && sourceOutputAndTargetInputProperties.get(0).size() != 0) {
      this.connectorSourceOutputPrpertyNamesByClassName
          .computeIfAbsent(sourceTypeName, v -> new HashSet<String>())
          .addAll(sourceOutputAndTargetInputProperties.get(0));
    }

    boolean addEquals = false;
    if (targetTypeName.equals(sourceTypeName)) { // ie., targetTypeName = sourceTypeName
                                                 // is "BehaviorWithParemeter" for 4.1.5
                                                 // Multiple Execution Steps2 - Multiple
                                                 // Object Flow
      sigNameWithTransferConnectorWithSameInputOutputFieldType.add(targetTypeName);
      addEquals = true;
    }

    // only leafSig
    List<Set<Field>> targetInputsSourceOutputsFields =
        processConnectorInputsOutputs(sigOfClass, sourceField, targetField,
            sourceTypeName, targetTypeName, sourceOutputAndTargetInputProperties, addEquals,
            isSigLeaf);


    Association type = cn.getType();
    if (type.getName().equals("Transfer") || type.getName().equals("TransferBefore")) {
      Sig.Field transferField =
          handTransferFieldAndFnPrep(sigOfClass, source, target);
      Set<Expr> exprs = null;
      if (type.getName().equals("Transfer"))
        exprs = this.toAlloy.addTransferFacts(sigOfClass, transferField, source, target,
            targetInputsSourceOutputsFields, !isSigLeaf);
      else // TransferBefore
        exprs = this.toAlloy.addTransferBeforeFacts(sigOfClass, transferField, source, target,
            targetInputsSourceOutputsFields, !isSigLeaf);
      sigToFactsMap.computeIfAbsent(sigOfClass.label, v -> new HashSet<Expr>())
          .addAll(exprs);
    }
  }

  /**
   * Create a transfer field named with suffix with source and target and return.
   * 
   * @param sigOfClass(PrimSig) - the connector owner to have the transfer field
   * @param source(String) - the source name
   * @param target(String) - the target name
   * @return a created field (Field)
   */
  private Sig.Field handTransferFieldAndFnPrep(PrimSig sigOfClass, String source, String target) {
    String fieldName = "transfer" + firstCharUpper(source) + firstCharUpper(target);
    this.transferFieldNames.add(fieldName);
    sigToTransferFieldMap.computeIfAbsent(sigOfClass.label, v -> new HashSet<>()).add(fieldName);
    Sig.Field transferField = AlloyUtils.addTransferField(fieldName, sigOfClass);
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
        UML2Utils.getStreotypePropertyValues(cn, STEREOTYPE_ITEMFLOW, stTagNames, this.messages);
    Map<String, List<Property>> stTagObjectFlowValues =
        UML2Utils.getStreotypePropertyValues(cn, STEREOTYPE_OBJECTFLOW, stTagNames, this.messages);

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
   * adding facts inputs and outputs for bijection like below for leaf sig 1) if addequal is true - fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs} 2) - fact {all x: IFSingleFoodService
   * | bijectionFiltered[outputs, x.order, x.order.orderedFoodItem]}
   * 
   * @param sig
   * @param sourceField A field. When this field is null, sourceOutputAndTargetInputProperties.get(0) is also null
   * @param targetField A field when this field is null, sourceOutputAndTargetInputProperties.get(1) is also null
   * @param sourceTypeName type of connector source(output)
   * @param targetTypeName type of connector target(input)
   * @param sourceOutputAndTargetInputProperties
   * @param fieldsWithInputs null or non-leaf sig
   * @param fieldsWithOutputs null or non-leaf sig
   * @return List<Set<Field>> [0] = targetInputFields [1] = sourceOutputFields
   */
  private List<Set<Field>> processConnectorInputsOutputs(PrimSig sigOfClass, Field sourceField,
      Field targetField,
      String sourceTypeName, String targetTypeName,
      List<Set<String>> sourceOutputAndTargetInputProperties,
      boolean addEquals, boolean isLeaf) {

    // if addEquals is true add the fact like below:
    // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}

    Set<Field> addOutputToFields = new HashSet<>();
    Set<Field> addInputToFields = new HashSet<>();
    if (sourceField != null) {
      PrimSig typeSig = toAlloy.getSig(sourceTypeName);// sourceTypeName =IFCustomerOrder
      for (String sourceOutput : sourceOutputAndTargetInputProperties.get(0)) {
        // orderedFoodItem
        Field outputTo = AlloyUtils.getFieldFromSigOrItsParents(sourceOutput, typeSig);// i
        // fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p1, x.p1.i]}
        if (!parameterFields.contains(outputTo))
          addOutputToFields.add(outputTo);

        // only for leaf-sig
        if (isLeaf
            && AlloyUtils.notContainBothKeyAndValue(this.fieldWithOutputs, sourceField, outputTo)) {
          this.fieldWithOutputs.computeIfAbsent(sourceField, v -> new HashSet<Field>())
              .add(outputTo);
          toAlloy.createBijectionFilteredOutputs(sigOfClass, sourceField,
              sourceField.join(outputTo));
          if (addEquals)
            toAlloy.createInField(sigOfClass, sourceField, outputTo, Alloy.ooutputs);

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
        // only for leaf sig
        if (isLeaf
            && AlloyUtils.notContainBothKeyAndValue(this.fieldWithInputs, targetField, inputTo)) {
          this.fieldWithInputs.computeIfAbsent(targetField, v -> new HashSet<Field>()).add(inputTo);
          toAlloy.createBijectionFilteredInputs(sigOfClass, targetField,
              targetField.join(inputTo));
          if (addEquals) {
            toAlloy.createInField(sigOfClass, targetField, inputTo, Alloy.oinputs);
          }
        }
      }
    }
    return List.of(addOutputToFields, addInputToFields);
  }

  // Get method for instance variables
  protected Set<String> getSigNameWithTransferConnectorWithSameInputOutputFieldType() {
    return this.sigNameWithTransferConnectorWithSameInputOutputFieldType;
  }

  protected Set<Sig> getSigWithTransferField() {
    return sigWithTransferField;
  }


  protected Set<String> getTransferingTypeSig() {
    return transferingTypeSig;
  }

  protected Set<String> getTransferFieldNames() {
    return this.transferFieldNames;
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

  // Utility methods
  /**
   * Convert a given string with 1st letter upper case and others to be lower case.
   * 
   * @param s - the input string
   * @return the converted string
   */
  private static String firstCharUpper(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }

}
