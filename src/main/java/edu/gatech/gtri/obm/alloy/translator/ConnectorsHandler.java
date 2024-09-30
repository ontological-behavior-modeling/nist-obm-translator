package edu.gatech.gtri.obm.alloy.translator;

import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.NamedElement;

/**
 * Connectors handler
 *
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 */
public class ConnectorsHandler {

  /** omgutil SysMLUtil - Util method from omgutil */
  SysMLUtil sysmlUtil;

  /**
   * A set of connectors redefined by children so that the connectors are ignored by the parent.
   * This variable is initialized in this class and updated and used while analyzing all the
   * connectors by ConnectorHandler.
   */
  Set<Connector> redefinedConnectors;
  /**
   * A set of leaf signatures used to determine the given signature of process method is leaf or not
   */
  Set<PrimSig> leafSigs;
  /** A class connect this and Alloy class */
  ToAlloy toAlloy;
  /** A dictionary contains signature name as key and a set of transfer field names as value. */
  Map<String, Set<String>> sigToTransferFieldMap;
  /**
   * A map where key is signature name string and value is a set of field name strings. This
   * includes inherited fields/properties and used in closure facts.
   */
  Map<String, Set<String>> stepPropertiesBySig;

  /** A set of Alloy fields created for Properties with <<Parameter>> stereotype */
  Set<Field> parameterFields;

  /** A dictionary contains signature name as key and a set of fact expression as value. */
  Map<String, Set<Expr>> sigToFactsMap;

  ConnectorHandler connectorHandler;

  protected ConnectorsHandler(
      SysMLAdapter sysMLAdapter,
      SysMLUtil _sysmlUtil,
      Set<PrimSig> _leafSigs,
      ToAlloy _toAlloy,
      Set<Field> _parameterFields,
      Map<String, Set<String>> _stepPropertiesBySig) {

    this.sysmlUtil = _sysmlUtil;

    this.redefinedConnectors =
        new HashSet<Connector>(); // pass to each instance of pcm and used and updated
    this.leafSigs = _leafSigs; // pass from OBMXMI2Alloy and used in each instance of pcm
    this.toAlloy = _toAlloy; // pass from OBMXMI2Alloy and used in each instance of pcm
    this.parameterFields =
        _parameterFields; // pass from OBMXMI2Alloy and used during process method
    this.stepPropertiesBySig = _stepPropertiesBySig;

    this.sigToTransferFieldMap =
        new HashMap<>(); // instance variable updated in pcm.process method and used later by
    // OBMXMI2Alloy
    this.sigToFactsMap =
        new HashMap<>(); // instance variable updated in pcm.process method and used later by
    // OBMXMI2Alloy

    connectorHandler =
        new ConnectorHandler(
            this.redefinedConnectors,
            this.toAlloy,
            this.sigToFactsMap,
            this.parameterFields,
            this.sigToTransferFieldMap,
            sysMLAdapter,
            this.sysmlUtil);
  }

  /**
   * Go processing all connectors in the correct order.
   *
   * @param _classInHierarchy List of Class from oldest to youngest where the youngest is main class
   *     you tried to translate.
   * @param _allNamedElementsConnectedToMainSigByFields NamedElements to be converted to Signatures
   *     (Class and PrimitiveType(Real, Integer etc...)
   */
  protected void process(
      List<Class> _classInHierarchy,
      Set<NamedElement> _allNamedElementsConnectedToMainSigByFields) {

    // go through from child to parent so that facts generated from redefined connectors will not be
    // created by parent.
    for (int i = _classInHierarchy.size() - 1; i >= 0; i--) {
      Class classOfSig = _classInHierarchy.get(i);
      processConnectorsForAClass(classOfSig);
      // removing ne
      // _allClassesConnectedToMainSigByFields.remove(classOfSig);
    }
    // after handling connectors for Signatures(hierarchy of main Signature), handle others classes.
    for (NamedElement ne : _allNamedElementsConnectedToMainSigByFields) {

      if (!_classInHierarchy.contains(ne)
          && ne instanceof Class) { // no connector processing in PrimitiveType (Real, Integer)
        processConnectorsForAClass((Class) ne);
      }
    }
  }

  /**
   * @param ne - a namedElement transfer to a signature.
   * @param outConnectorInputPropertiesBySig - a map data to be collected where values are
   *     connectors' targetInputProperty tag values (property names) and key the property owner
   *     (namedElement) name
   * @param outconnectorOutputPropertiesBySig - a map data to be collected where values are
   *     connectors' sourceOutputProperty tag values (property names) and key of the property owner
   *     (namedElement) name
   * @param outSigWithTransferFields - a set of signatures having transfer fields collected
   * @return a set of signature string names having transfer field(s) of the same input and output
   *     types
   */
  private void processConnectorsForAClass(Class ne) {

    PrimSig ownerSig = this.toAlloy.getSig(ne.getName()); // pcm.getSigOfNamedElement();
    connectorHandler.processConnectorsForASig(ne, leafSigs.contains(ownerSig));

    Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType =
        connectorHandler.getSigNameWithTransferConnectorWithSameInputOutputFieldType();

    Map<Field, Set<Field>> fieldWithInputs = connectorHandler.getFieldWithInputs();
    Map<Field, Set<Field>> fieldWithOutputs = connectorHandler.getFieldWithOutputs();

    // any of connector ends owned field types are the same (p1, p2:
    // BehaviorWithParameter)
    // 4.1.5 Multiple Execution Step2 = MultiplObjectFlow=[BehaviorWithParameter]
    if (sigNameWithTransferConnectorWithSameInputOutputFieldType.size() > 0) {
      Set<String> nonTransferFieldNames = this.stepPropertiesBySig.get(ne.getName());

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
      if (fields.size() > 0) toAlloy.addInOutClosureFact(ownerSig, field, fields, Alloy.oinputs);
    }
    for (Field field : fieldWithOutputs.keySet()) {
      Set<Field> fields = removeParameterFields(fieldWithOutputs.get(field));
      if (fields.size() > 0) toAlloy.addInOutClosureFact(ownerSig, field, fields, Alloy.ooutputs);
    }
    // add transferFieldNames for this Class(ne), now stepPorpertiedBySig has transfer fields later
    // used in OBMXMI1Alloy
    this.stepPropertiesBySig.get(ne.getName()).addAll(connectorHandler.getTransferFieldNames());
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

  protected Set<Sig> getSigWithTransferFields() {
    return connectorHandler.getSigWithTransferField();
  }

  protected Set<String> getSigNameWithTransferConnectorWithSameInputOutputFieldType() {
    return connectorHandler.getSigNameWithTransferConnectorWithSameInputOutputFieldType();
  }

  protected Map<String, Set<String>> getConnectorsTargetInputPropertyNamesByClassName() {
    return connectorHandler.getConnectorsTargetInputPropertyNamesByClassName();
  }

  protected Map<String, Set<String>> getConnectorsSourceOutputPrpertyNamesByClassName() {
    return connectorHandler.getConnectorsSourceOutputPrpertyNamesByClassName();
  }

  protected Set<String> getTransferingTypeSig() {
    return connectorHandler.getTransferingTypeSig();
  }

  protected Map<String, Set<String>> getSigToTransferFieldMap() {
    return this.sigToTransferFieldMap;
  }

  protected Map<String, Set<Expr>> getSigToFactsMap() {
    return this.sigToFactsMap;
  }

  protected List<String> getMessages() {
    return this.connectorHandler.getMessages();
  }
}
