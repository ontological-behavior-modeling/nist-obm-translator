package edu.gatech.gtri.obm.alloy.translator;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.NamedElement;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;

public class ConnectorHandler {

  SysMLAdapter sysmladapter;
  SysMLUtil sysmlUtil;

  /**
   * A set of connectors redefined by children so that the connectors are ignored by the parent.
   */
  Set<Connector> redefinedConnectors;
  Set<PrimSig> leafSigs;
  ToAlloy toAlloy;
  /**
   * A dictionary contains signature name as key and a set of transfer field names as value.
   */
  Map<String, Set<String>> sigToTransferFieldMap;
  Map<String, Set<String>> stepPropertiesBySig;

  List<String> messages;
  Set<Field> parameterFields;

  /**
   * A dictionary contains signature name as key and a set of fact expression as value.
   */
  Map<String, Set<Expr>> sigToFactsMap;


  Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType;
  // connectors
  // key = Signame, values = propertyNames
  HashMap<String, Set<String>> connectorTargetInputPropertyNamesByClassName; // collect field type Sig having a
  // transfer connector
  // with transferTarget "Customer"
  HashMap<String, Set<String>> connectorSourceOutputPrpertyNamesByClassName; // collect field type Sig having a
  // transfer connector
  Set<Sig> sigWithTransferFields;
  /**
   * A set of string representing the type of transfer fields (ie., Integer)
   */
  Set<String> transferingTypeSig;

  public ConnectorHandler(SysMLAdapter _sysmladapter,
      SysMLUtil _sysmlUtil, /* Set<Connector> _redefinedConnectors, */Set<PrimSig> _leafSigs,
      ToAlloy _toAlloy, /* Map<String, Set<Expr>> _sigToFactsMap, */
      Set<Field> _parameterFields, /* Map<String, Set<String>> _sigToTransferFieldMap, */
      Map<String, Set<String>> _stepPropertiesBySig) {
    this.sysmladapter = _sysmladapter;
    this.sysmlUtil = _sysmlUtil;
    // redefinedConnectors = _redefinedConnectors;
    redefinedConnectors = new HashSet<Connector>(); // pass to each instance of pcm and used and updated
    leafSigs = _leafSigs; // pass from OBMXMI2Alloy and used in each instance of pcm
    toAlloy = _toAlloy; // pass from OBMXMI2Alloy and used in each instance of pcm
    parameterFields = _parameterFields; // pass from OBMXMI2Alloy and used in each instance of pcm

    sigToTransferFieldMap = new HashMap<>(); // instance variable updated in pcm used by OBMXMI2Alloy
    // sigToTransferFieldMap = _sigToTransferFieldMap;
    stepPropertiesBySig = _stepPropertiesBySig; // pass to each instance of pcm and used and also updated and used later at OBMXMI2Alloy
    // sigToFactsMap = _sigToFactsMap;
    sigToFactsMap = new HashMap<>();// instance variable updated in pcm used by OBMXMI2Alloy

    messages = new ArrayList<String>();
    sigNameWithTransferConnectorWithSameInputOutputFieldType = new HashSet<>();
    connectorTargetInputPropertyNamesByClassName = new HashMap<>();
    connectorSourceOutputPrpertyNamesByClassName = new HashMap<>();
    sigWithTransferFields = new HashSet<>();
    transferingTypeSig = new HashSet<String>();
  }

  public void process(List<Class> classInHierarchy,
      Set<NamedElement> allClassesConnectedToMainSigByFields) {

    // from child to parent so that redefinedconnector is not created by parents
    // during handlClassForProcessConnector this.sigToTransferFieldMap is created
    for (int i = classInHierarchy.size() - 1; i >= 0; i--) {
      Class ne = classInHierarchy.get(i);

      // ProcessConnectorMethod pcm =
      // new ProcessConnectorMethod(ne, this.redefinedConnectors, this.leafSigs, toAlloy,
      // sigToFactsMap, this.parameterFields, this.sigToTransferFieldMap,
      // this.stepPropertiesBySig);

      handleClassForProecessConnector(ne);

      /*
       * sigNameWithTransferConnectorWithSameInputOutputFieldType .addAll(pcm.getSigNameWithTransferConnectorWithSameInputOutputFieldType()); connectorTargetInputPropertyNamesByClassName
       * .putAll(pcm.getConnectorTargetInputPropertyNamesByClassName()); connectorSourceOutputPrpertyNamesByClassName .putAll(pcm.getConnectorSourceOutputPrpertyNamesByClassName());
       * sigWithTransferFields.addAll(pcm.getSigWithTransferField());
       * 
       * this.messages.addAll(pcm.getMessages()); this.transferingTypeSig.addAll(pcm.getTransferingTypeSig());
       */
      // sigNameWithTransferConnectorWithSameInputOutputFieldType
      // .addAll(handleClassForProecessConnector(ne, connectorTargetInputPropertyNamesByClassName,
      // connectorSourceInputPrpertyNamesByClassName, sigWithTransferFields));

      // removing ne
      allClassesConnectedToMainSigByFields.remove(ne);
    }
    // after handling connectors for Signatures(hierarchy of main Signature), handle others.
    for (

    NamedElement ne : allClassesConnectedToMainSigByFields) {
      if (ne instanceof Class) {// no connector in primitiveType (Real, Integer)
        // sigNameWithTransferConnectorWithSameInputOutputFieldType
        // .addAll(
        // handleClassForProecessConnector(ne, connectorTargetInputPropertyNamesByClassName,
        // connectorSourceInputPrpertyNamesByClassName, sigWithTransferFields));

        //
        // ProcessConnectorMethod pcm =
        // new ProcessConnectorMethod((org.eclipse.uml2.uml.Class) ne, this.redefinedConnectors,
        // this.leafSigs, toAlloy,
        // sigToFactsMap, this.parameterFields, this.sigToTransferFieldMap,
        // this.stepPropertiesBySig);

        handleClassForProecessConnector((org.eclipse.uml2.uml.Class) ne);

        /*
         * sigNameWithTransferConnectorWithSameInputOutputFieldType .addAll(pcm.getSigNameWithTransferConnectorWithSameInputOutputFieldType()); connectorTargetInputPropertyNamesByClassName
         * .putAll(pcm.getConnectorTargetInputPropertyNamesByClassName()); connectorSourceOutputPrpertyNamesByClassName .putAll(pcm.getConnectorSourceOutputPrpertyNamesByClassName());
         * sigWithTransferFields.addAll(pcm.getSigWithTransferField());
         * 
         * this.messages.addAll(pcm.getMessages()); this.transferingTypeSig.addAll(pcm.getTransferingTypeSig());
         */

      }
    }
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
  private void handleClassForProecessConnector(
      Class ne) {



    ProcessConnectorMethod pcm =
        new ProcessConnectorMethod(ne, this.redefinedConnectors, this.leafSigs, toAlloy,
            sigToFactsMap, this.parameterFields, this.sigToTransferFieldMap,
            this.stepPropertiesBySig);


    pcm.processConnector(this.sysmladapter, this.sysmlUtil);


    sigNameWithTransferConnectorWithSameInputOutputFieldType
        .addAll(pcm.getSigNameWithTransferConnectorWithSameInputOutputFieldType());
    connectorTargetInputPropertyNamesByClassName
        .putAll(pcm.getConnectorTargetInputPropertyNamesByClassName());
    connectorSourceOutputPrpertyNamesByClassName
        .putAll(pcm.getConnectorSourceOutputPrpertyNamesByClassName());
    sigWithTransferFields.addAll(pcm.getSigWithTransferField());
    this.messages.addAll(pcm.getMessages());
    this.transferingTypeSig.addAll(pcm.getTransferingTypeSig());



    PrimSig ownerSig = pcm.getSigOfNamedElement();

    Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType =
        pcm.getSigNameWithTransferConnectorWithSameInputOutputFieldType();

    Map<Field, Set<Field>> fieldWithInputs = pcm.getFieldWithInputs();
    Map<Field, Set<Field>> fieldWithOutputs = pcm.getFieldWithOutputs();

    // any of connector ends owned field types are the same (p1, p2:
    // BehaviorWithParameter)
    // 4.1.5 Multiple Execution Step2 = MultiplObjectFlow=[BehaviorWithParameter]
    if (sigNameWithTransferConnectorWithSameInputOutputFieldType.size() > 0) {
      Set<String> nonTransferFieldNames =
          stepPropertiesBySig.get(ownerSig.label).stream()
              .filter(f -> !f.startsWith("transfer")).collect(Collectors.toSet());
      // no inputs
      for (String fieldName : nonTransferFieldNames) {
        if (!AlloyUtils.fieldsLabels(fieldWithInputs.keySet()).contains(fieldName)) {
          // fact {all x: MultipleObjectFlow |no x.p1.inputs}
          toAlloy.addNoInputsOrOutputsFieldFact(ownerSig, fieldName,
              Alloy.oinputs);
        }
        if (!AlloyUtils.fieldsLabels(fieldWithOutputs.keySet()).contains(fieldName)) {
          toAlloy.addNoInputsOrOutputsFieldFact(ownerSig, fieldName,
              Alloy.ooutputs);
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
    return sigWithTransferFields;
  }

  protected Set<String> getSigNameWithTransferConnectorWithSameInputOutputFieldType() {
    return sigNameWithTransferConnectorWithSameInputOutputFieldType;
  }


  protected HashMap<String, Set<String>> getConnectorTargetInputPropertyNamesByClassName() {
    return connectorTargetInputPropertyNamesByClassName;
  }


  protected HashMap<String, Set<String>> getConnectorSourceOutputPrpertyNamesByClassName() {
    return connectorSourceOutputPrpertyNamesByClassName;
  }


  protected Set<String> getTransferingTypeSig() {
    return transferingTypeSig;
  }

  protected Map<String, Set<String>> getSigToTransferFieldMap() {
    return this.sigToTransferFieldMap;
  }

  protected Map<String, Set<Expr>> getSigToFactsMap() {
    return this.sigToFactsMap;
  }

}
