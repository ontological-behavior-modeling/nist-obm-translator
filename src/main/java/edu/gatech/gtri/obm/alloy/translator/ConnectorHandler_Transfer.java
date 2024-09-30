package edu.gatech.gtri.obm.alloy.translator;

import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
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

/**
 * A handler for a transfer connector
 *
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 */
public class ConnectorHandler_Transfer {

  /** Stereotype qualified names for item flow connector */
  private static String STEREOTYPE_ITEMFLOW = "Model::OBM::ItemFlow";
  /** Stereotype qualified name for object flow connector */
  private static String STEREOTYPE_OBJECTFLOW = "Model::OBM::ObjectFlow";

  /** A class that connects XMI model and the Alloy data model */
  ToAlloy toAlloy;
  /** A set of signatures with transfer fields */
  Set<Sig> sigWithTransferField;
  /** A dictionary contains signature name as a key and a set of transfer field names as a value. */
  Map<String, Set<String>> sigToTransferFieldMap;
  /**
   * A set of connectors redefined by children so that the connectors are ignored by the parent.
   * This variable is initialized in ConnectorsHandler and updated and used by a ConnectorHandler
   * while handling each connector at a time.
   */
  Set<Connector> redefinedConnectors;

  /** A set of field created from properties having ClassHandler.STEREOTYPE_PAREMETER stereotype. */
  Set<Field> parameterFields;

  /**
   * A dictionary where a key is a field and a value is a set of fields. The dictionary value is
   * targetInputProperty (fields) of connectors. The dictionary key is a field each connector is
   * connected to as input.
   *
   * <p>(i.e., 4.2.2 FoodService ObjectFlow::SingleFoodService (key = prepare, value =
   * [preparedFoodItem,prepareDestination])
   */
  Map<Field, Set<Field>> fieldWithInputs;
  /**
   * A dictionary where key is a field and a value is a set of fields. The dictionary value is
   * sourceOutputProperty (fields) of connectors. The dictionary key is a a field each connector is
   * connected as output.
   *
   * <p>(i.e., 4.2.2 FoodService ObjectFlow::SingleFoodService (key = order, value = [orderAmount,
   * orderDestination, orderedFoodItem])
   */
  Map<Field, Set<Field>> fieldWithOutputs;
  /**
   * A dictionary where a key is a class/signature name and a value is a set of property/field names
   * for connectors of targetInputProperty.
   */
  Map<String, Set<String>> connectorsTargetInputPropertyNamesByClassName;
  /**
   * A dictionary where a key is a class/signature name and a value is a set of property/field names
   * for connectors of sourceOutputProperty.
   */
  Map<String, Set<String>> connectorsSourceOutputPrpertyNamesByClassName;

  /**
   * A set of class/signature names which are types(class/signature) of a connector having the both
   * ends (property/field) having the same type(class/signature).
   *
   * <p>For example 4.1.5 Steps With Multiple Executions::MultipleObjectFlow, a connector's two ends
   * p1 and p2 having the same type(class/signature) "BehaviorWithParameter".
   */
  Set<String> sigNamesWithTransferConnectorWithSameInputOutputFieldType;
  /** A dictionary contains signature name as a key and a set of fact expression as a value. */
  Map<String, Set<Expr>> sigToFactsMap;
  /** A set of string representing the type(signature) of transfer fields (ie., Integer) */
  Set<String> transferingTypeSig;

  /** A set of transfer field names */
  protected Set<String> transferFieldNames;

  /** */
  List<String> messages;

  /**
   * Constructor - set the given variables as instance variables
   *
   * @param _toAlloy(ToAlloy)
   * @param _sigToFactsMap (Map<String>, Set<Expr>>)
   * @param _sigToTransferFieldMap (Map<String>, Set<String>>) -
   * @param _redefinedConnectors (Set<Connector>)
   * @param _parameterFields (Set<Field>)
   * @param _messages (List<String>)
   */
  protected ConnectorHandler_Transfer(
      ToAlloy _toAlloy,
      Map<String, Set<Expr>> _sigToFactsMap,
      Map<String, Set<String>> _sigToTransferFieldMap,
      Set<Connector> _redefinedConnectors,
      Set<Field> _parameterFields,
      List<String> _messages) {
    toAlloy = _toAlloy;
    sigToFactsMap = _sigToFactsMap;
    sigToTransferFieldMap = _sigToTransferFieldMap;
    redefinedConnectors = _redefinedConnectors;
    parameterFields = _parameterFields;
    messages = _messages;

    // initialize
    connectorsTargetInputPropertyNamesByClassName = new HashMap<>();
    connectorsSourceOutputPrpertyNamesByClassName = new HashMap<>();
    sigWithTransferField = new HashSet<>();
    sigNamesWithTransferConnectorWithSameInputOutputFieldType = new HashSet<>();
    transferingTypeSig = new HashSet<String>();
  }

  /** Reset required instance variables for new signature. */
  protected void reset() {
    transferFieldNames = new HashSet<>();
    fieldWithInputs = new HashMap<>();
    fieldWithOutputs = new HashMap<>();
  }

  /**
   * Add transfer transfer or TransferBefore facts to alloy.
   *
   * @param _connector (Connector) - a connector to process
   * @param _sigOfClass(PrimSig) - the owner of the connector
   * @param _isSigLeaf(boolean) - boolean true if the signature is leaf, otherwise false
   * @param _sourceTypeName(String) - the source connector end's type name
   * @param _targetTypeName(String) - the target connector end's type name
   * @param _sourceField(Field) - the source field if belong to sigOfClass, possible to be null
   * @param _targetField(Field) - the target field if belong to sigOfClass, possible to be null
   * @param _source(String) - the sourceOutputProperty name
   * @param _target(String) - the targetInputProperty name
   */
  protected void handleTransferConnector(
      Connector _connector,
      PrimSig _sigOfClass,
      boolean _isSigLeaf,
      String _sourceTypeName,
      String _targetTypeName,
      Field _sourceField,
      Field _targetField,
      String _source,
      String _target) {

    this.sigWithTransferField.add(_sigOfClass);
    for (Connector redefinedConnector : _connector.getRedefinedConnectors()) {
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
        handleTransferAndTransferBeforeInputsAndOutputs(_connector);

    // add to inputs map where key = tragetTypeName and values = targetInputProperty names
    if (sourceOutputAndTargetInputProperties.get(1) != null) {
      // && sourceOutputAndTargetInputProperties.get(1).size() != 0) {
      this.connectorsTargetInputPropertyNamesByClassName
          .computeIfAbsent(_targetTypeName, v -> new HashSet<String>())
          .addAll(sourceOutputAndTargetInputProperties.get(1));
    }

    // add to output map where key = sourceTypeName and values = sourceOutputProperty names
    if (sourceOutputAndTargetInputProperties.get(0) != null) {
      // && sourceOutputAndTargetInputProperties.get(0).size() != 0) {
      this.connectorsSourceOutputPrpertyNamesByClassName
          .computeIfAbsent(_sourceTypeName, v -> new HashSet<String>())
          .addAll(sourceOutputAndTargetInputProperties.get(0));
    }

    boolean addEquals = false;
    if (_targetTypeName.equals(_sourceTypeName)) { // ie., targetTypeName = sourceTypeName
      // is "BehaviorWithParemeter" for 4.1.5
      // Multiple Execution Steps2 - Multiple
      // Object Flow
      sigNamesWithTransferConnectorWithSameInputOutputFieldType.add(_targetTypeName);
      addEquals = true;
    }

    // only for leafSig
    List<Set<Field>> sourceOutputPropertyAndtargetInputPropertyFields =
        processConnectorInputsOutputs(
            _sigOfClass,
            _sourceField,
            _targetField,
            _sourceTypeName,
            _targetTypeName,
            sourceOutputAndTargetInputProperties,
            addEquals,
            _isSigLeaf);

    Association type = _connector.getType();
    if (type.getName().equals("Transfer") || type.getName().equals("TransferBefore")) {
      Sig.Field transferField = handTransferFieldAndFnPrep(_sigOfClass, _source, _target);
      Set<Expr> exprs = null;
      if (type.getName().equals("Transfer"))
        exprs =
            this.toAlloy.addTransferFacts(
                _sigOfClass,
                transferField,
                _source,
                _target,
                sourceOutputPropertyAndtargetInputPropertyFields,
                !_isSigLeaf);
      else // TransferBefore
      exprs =
            this.toAlloy.addTransferBeforeFacts(
                _sigOfClass,
                transferField,
                _source,
                _target,
                sourceOutputPropertyAndtargetInputPropertyFields,
                !_isSigLeaf);
      sigToFactsMap.computeIfAbsent(_sigOfClass.label, v -> new HashSet<Expr>()).addAll(exprs);
    }
  }

  /**
   * Create a transfer field named with suffix with source and target and return.
   *
   * @param _sigOfClass(PrimSig) - the connector owner to have the transfer field
   * @param _source(String) - the source name
   * @param _target(String) - the target name
   * @return (Field) - a created field
   */
  private Sig.Field handTransferFieldAndFnPrep(
      PrimSig _sigOfClass, String _source, String _target) {
    String fieldName = "transfer" + firstCharUpper(_source) + firstCharUpper(_target);
    this.transferFieldNames.add(fieldName);
    sigToTransferFieldMap.computeIfAbsent(_sigOfClass.label, v -> new HashSet<>()).add(fieldName);
    Sig.Field transferField = AlloyUtils.addTransferField(fieldName, _sigOfClass);
    return transferField;
  }

  /**
   * Find sourceOutputProperty and targetInputProperty of the given connector with the <<ItemFlow>>
   * or <<ObjectFlow>> Stereotype
   *
   * @param _connector (Connector) - a connector having the properties
   * @return List<Set<String>> - the 1st in the list is a set of sourceOutputProperty names and the
   *     2nd in the list is a set of the targetInputProperty names
   */
  private List<Set<String>> handleTransferAndTransferBeforeInputsAndOutputs(
      org.eclipse.uml2.uml.Connector _connector) {

    List<Set<String>> sourceOutputAndTargetInputProperties = new ArrayList<>();
    String[] stTagNames = {"sourceOutputProperty", "targetInputProperty"}; // , "itemType"}; this
    // property value is class
    // not property
    Map<String, List<Property>> stTagItemFlowValues =
        UML2Utils.getStreotypePropertyValues(
            _connector, STEREOTYPE_ITEMFLOW, stTagNames, this.messages);
    Map<String, List<Property>> stTagObjectFlowValues =
        UML2Utils.getStreotypePropertyValues(
            _connector, STEREOTYPE_OBJECTFLOW, stTagNames, this.messages);

    List<Property> sos = null;
    List<Property> tis = null;

    if (stTagObjectFlowValues != null) { // ObjectFlow
      sos = stTagObjectFlowValues.get(stTagNames[0]); // sourceOutputProperty = x.outputs
      tis = stTagObjectFlowValues.get(stTagNames[1]); // targetInputProperty = x.inputs
    }

    if (stTagItemFlowValues != null) { // ItemFlow
      sos =
          stTagItemFlowValues.get(stTagNames[0]); // sourceOutputProperty - name is suppliedProduct
      tis =
          stTagItemFlowValues.get(stTagNames[1]); // targetInputProperty - name is "receivedProduct"
    }
    Class sosOwner = null;
    Class tisOwner = null;
    Set<String> sourceOutput = new HashSet<>();
    Set<String> targetInput = new HashSet<>();
    if (sos != null && tis != null) {
      for (Property p : sos) {
        sosOwner = ((org.eclipse.uml2.uml.Class) p.getOwner());
        sourceOutput.add(
            p.getName()); // p.getName() is vout. so create {B2(owner) | x.vout= x.output}
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
   * With the given information of a transfer connector adding facts inputs and outputs for
   * bijection like below for leaf sig 1) if addequal is true - fact {all x: MultipleObjectFlow |
   * all p: x.p1 | p.i = p.outputs} 2) - fact {all x: IFSingleFoodService |
   * bijectionFiltered[outputs, x.order, x.order.orderedFoodItem]} and return a list of
   * sourceOUtputProperty field set and targetInputProperty field set to be used to create fact like
   * below.
   *
   * <pre>
   * {all x: ownerSig| transferOrderPay.items in x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount}
   * {all x: ownerSig| x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount in x.transferOrderPay.items}
   * or
   * fact {all x: TransferProduct | x.transferSupplierCustomer.items in x.transferSupplierCustomer.targets.receivedProduct}
   * fact {all x: TransferProduct | x.transferSupplierCustomer.targets.receivedProduct in x.transferSupplierCustomer.items}
   * </pre>
   *
   * @param _sigOfClass (PrimSig) - the connector owner to have the transfer field
   * @param _sourceField (Field) - A source field of the connector. When this field is null,
   *     _sourceOutputAndTargetInputProperties.get(0) is also null
   * @param _targetField (Field) - A target field of the connector. When this field is null,
   *     _sourceOutputAndTargetInputProperties.get(1) is also null
   * @param _sourceTypeName (String) - A source field type (signature) name of the connector
   * @param _targetTypeName (String) - A target field type (signature) name of the connector
   * @param _sourceOutputAndTargetInputProperties
   * @param fieldsWithInputs null or non-leaf sig
   * @param fieldsWithOutputs null or non-leaf sig
   * @return List<Set<Field>> - a list with 0 index for a set of fields for sourceOutputProperty of
   *     the transfer connector and 1st index for a sef of field for targetInputProperty of the
   *     transfer connector
   */
  private List<Set<Field>> processConnectorInputsOutputs(
      PrimSig _sigOfClass,
      Field _sourceField,
      Field _targetField,
      String _sourceTypeName,
      String _targetTypeName,
      List<Set<String>> _sourceOutputAndTargetInputProperties,
      boolean _addEquals,
      boolean _isLeaf) {

    // if addEquals is true add the fact like below:
    // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}

    Set<Field> sourceOutputPropertyFields = new HashSet<>();
    Set<Field> targetInputPropertyFields = new HashSet<>();
    if (_sourceField != null) {
      PrimSig typeSig = toAlloy.getSig(_sourceTypeName); // sourceTypeName =IFCustomerOrder
      for (String sourceOutput : _sourceOutputAndTargetInputProperties.get(0)) {
        // orderedFoodItem
        Field outputTo = AlloyUtils.getFieldFromSigOrItsParents(sourceOutput, typeSig); // i
        // fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p1, x.p1.i]}
        if (!parameterFields.contains(outputTo)) sourceOutputPropertyFields.add(outputTo);

        // only for leaf-sig
        if (_isLeaf
            && AlloyUtils.notContainBothKeyAndValue(
                this.fieldWithOutputs, _sourceField, outputTo)) {
          this.fieldWithOutputs
              .computeIfAbsent(_sourceField, v -> new HashSet<Field>())
              .add(outputTo);
          toAlloy.addBijectionFilteredOutputsFact(
              _sigOfClass, _sourceField, _sourceField.join(outputTo));
          if (_addEquals)
            toAlloy.createInFieldExpression(_sigOfClass, _sourceField, outputTo, Alloy.ooutputs);
        }
      }
    }

    // target => inputs
    // fact {all x: MultipleObjectFlow | all p: x.p2 | p.i = p.inputs}
    if (_targetField != null) {
      PrimSig typeSig = toAlloy.getSig(_targetTypeName); // IFCustomPrepare
      for (String targetInputProperties : _sourceOutputAndTargetInputProperties.get(1)) {
        Field inputTo =
            AlloyUtils.getFieldFromSigOrItsParents(
                targetInputProperties, // i
                typeSig);
        if (!parameterFields.contains(inputTo)) targetInputPropertyFields.add(inputTo);
        // fact {all x: MultipleObjectFlow | bijectionFiltered[inputs, x.p2, x.p2.i]}
        // fact {all x: IFSingleFoodService | bijectionFiltered[inputs, x.prepare,
        // x.prepare.preparedFoodItem]}
        // only for leaf sig
        if (_isLeaf
            && AlloyUtils.notContainBothKeyAndValue(this.fieldWithInputs, _targetField, inputTo)) {
          this.fieldWithInputs
              .computeIfAbsent(_targetField, v -> new HashSet<Field>())
              .add(inputTo);
          toAlloy.addBijectionFilteredInputsFact(
              _sigOfClass, _targetField, _targetField.join(inputTo));
          if (_addEquals) {
            toAlloy.createInFieldExpression(_sigOfClass, _targetField, inputTo, Alloy.oinputs);
          }
        }
      }
    }
    return List.of(sourceOutputPropertyFields, targetInputPropertyFields);
  }

  /**
   * Get method for sigNamesWithTransferConnectorWithSameInputOutputFieldType instance variable
   *
   * @return Set<String>
   */
  protected Set<String> getSigNameWithTransferConnectorWithSameInputOutputFieldType() {
    return this.sigNamesWithTransferConnectorWithSameInputOutputFieldType;
  }

  /**
   * Get method for sigWithTransferField instance variable
   *
   * @return Set<Sig>
   */
  protected Set<Sig> getSigWithTransferField() {
    return sigWithTransferField;
  }

  /**
   * Get method for transferingTypeSig instance variable
   *
   * @return Set<String>
   */
  protected Set<String> getTransferingTypeSig() {
    return transferingTypeSig;
  }

  /**
   * Get method for transferFieldNames instance variable
   *
   * @return Set<String>
   */
  protected Set<String> getTransferFieldNames() {
    return this.transferFieldNames;
  }

  /**
   * Get method for connectorsTargetInputPropertyNamesByClassName instance variable
   *
   * @return Map<String, Set<String>>
   */
  protected Map<String, Set<String>> getConnectorsTargetInputPropertyNamesByClassName() {
    return connectorsTargetInputPropertyNamesByClassName;
  }

  /**
   * Get method for connectorsSourceOutputPrpertyNamesByClassName instance variable
   *
   * @return Map<String, Set<String>>
   */
  protected Map<String, Set<String>> getConnectorsSourceOutputPrpertyNamesByClassName() {
    return connectorsSourceOutputPrpertyNamesByClassName;
  }

  /**
   * Get method for fieldWithInputs instance variable
   *
   * @return Map<Field, Set<Field>>
   */
  protected Map<Field, Set<Field>> getFieldWithInputs() {
    return fieldWithInputs;
  }

  /**
   * Get method for fieldWithOutputs instance variable
   *
   * @return Map<Field, Set<Field>>
   */
  protected Map<Field, Set<Field>> getFieldWithOutputs() {
    return fieldWithOutputs;
  }

  // Utility methods
  /**
   * Convert a given string to 1st letter upper case and others to lower case.
   *
   * @param s (String) - the input string
   * @return String - the converted string
   */
  private static String firstCharUpper(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }
}
