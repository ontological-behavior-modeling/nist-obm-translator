package edu.gatech.gtri.obm.alloy.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;

/**
 * A class to handle a connector
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 *
 */
public class ConnectorHandler {

  private enum CONNECTOR_TYPE {
    HAPPENS_BEFORE, HAPPENS_DURING, TRANSFER;
  }

  /** Stereotype qualified names */
  private static String STEREOTYPE_BINDDINGCONNECTOR = "SysML::BindingConnector";

  Map<Field, Set<Field>> fieldWithInputs;
  // key = prepare, value= [preparedFoodItem,prepareDestination]
  Map<Field, Set<Field>> fieldWithOutputs;
  // key = order, value=[orderAmount, orderDestination, orderedFoodItem]

  /**
   * A dictionary contains signature name as key and a set of fact expression as value.
   */
  Map<String, Set<Expr>> sigToFactsMap;
  /**
   * A set of connectors redefined by children so that the connectors are ignored by the parent. This variable is initialized in ConnectorsHandler and updated and used by a ConnectorHandler while
   * handling each connector at a time.
   */
  Set<Connector> redefinedConnectors;

  /**
   * A set of Alloy fields created for Properties with <<Parameter>> stereotype
   */
  Set<Field> parameterFields;
  /**
   * A class connect this connector class and Alloy class
   */
  ToAlloy toAlloy;

  /**
   * A list of messages to collect during this class
   */
  List<String> messages;


  /**
   * A dictionary contains signature name as key and a set of transfer field names as value.
   */
  Map<String, Set<String>> sigToTransferFieldMap;
  /**
   * A dictionary contains signature name as key and a set of step property/field names as value
   */
  Map<String, Set<String>> stepPropertiesBySig;

  /**
   * A set of transfer field names
   */
  protected Set<String> transferFieldNames;

  /** omgutils sysmladapter **/
  SysMLAdapter sysmladapter;
  /** omgutils sysmlutl **/
  SysMLUtil sysmlUtil;

  /** A transfer connecter handler if this connector is transfer connector **/
  ConnectorHandler_Transfer tch;

  /**
   * A constructor
   * 
   * @param _redefinedConnectors
   * @param _toAlloy
   * @param _sigToFactsMap
   * @param _parameterFields
   * @param _sigToTransferFieldMap
   * @param _sysmladapter
   * @param _sysmlUtil
   */
  protected ConnectorHandler(Set<Connector> _redefinedConnectors,
      ToAlloy _toAlloy, Map<String, Set<Expr>> _sigToFactsMap,
      Set<Field> _parameterFields, Map<String, Set<String>> _sigToTransferFieldMap,
      SysMLAdapter _sysmladapter,
      SysMLUtil _sysmlUtil) {

    this.sysmladapter = _sysmladapter;
    this.sysmlUtil = _sysmlUtil;

    // passed by reference from ConnenctorHandler used and updated during in instance of this class
    redefinedConnectors = _redefinedConnectors;

    // pass from OBMXMI2Alloy -> ConnectorHandler to this class used to check if field is parameterfield or not
    parameterFields = _parameterFields;
    toAlloy = _toAlloy;

    sigToTransferFieldMap = _sigToTransferFieldMap;
    sigToFactsMap = _sigToFactsMap;


    messages = new ArrayList<String>();

    tch = new ConnectorHandler_Transfer(toAlloy, sigToFactsMap,
        sigToTransferFieldMap, redefinedConnectors,
        parameterFields, messages);
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
  protected void processConnectorsForASig(Class _classOfSig, boolean isSigLeaf) {

    tch.reset(); // transferFieldNames requires to reset for each class. The transferFiledNames used to define stepProperties.

    PrimSig sigOfClass = this.toAlloy.getSig(_classOfSig.getName());
    Set<org.eclipse.uml2.uml.Connector> connectors = sysmlUtil.getOwnedConnectors(_classOfSig);

    // handle one of connectors
    ConnectorsHandler_OneOf och =
        new ConnectorsHandler_OneOf(sysmlUtil, sysmladapter, toAlloy, this.messages);
    Set<Connector> oneOfConnectors = och.handleOneOfConnectors(sigOfClass, _classOfSig, connectors);

    // process remaining of connectors
    for (org.eclipse.uml2.uml.Connector cn : connectors) {
      if (oneOfConnectors.contains(cn))
        continue; // oneof connectors are already handled so skip here
      if (_classOfSig.getInheritedMembers().contains(cn))
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
                this.toAlloy.addEqualFact(sigOfClass, source, target);
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

            // targetField is null
            // i.e., For 4.1.4 Transfers and Parameters - a connector <<ItemFlow>> for b(B).vout and b2(B2).vout
            // source="b2" target="b", sigOfClass= "B" -- "b" is not a field of "B"
            Field sourceField = AlloyUtils.getFieldFromSigOrItsParents(source, sigOfClass);
            Field targetField = AlloyUtils.getFieldFromSigOrItsParents(target, sigOfClass);

            if (connector_type == CONNECTOR_TYPE.HAPPENS_BEFORE) {
              this.toAlloy.createBijectionFilteredHappensBefore(sigOfClass, sourceField,
                  targetField);
            } else if (connector_type == CONNECTOR_TYPE.HAPPENS_DURING)
              this.toAlloy.createBijectionFilteredHappensDuring(sigOfClass, sourceField,
                  targetField);

            else if (connector_type == CONNECTOR_TYPE.TRANSFER) {
              tch.handleTransferConnector(cn, sigOfClass, isSigLeaf,
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

  // Get methods to pass back to connectors handler
  protected HashMap<String, Set<String>> getConnectorTargetInputPropertyNamesByClassName() {
    return tch.getConnectorTargetInputPropertyNamesByClassName();
  }

  protected HashMap<String, Set<String>> getConnectorSourceOutputPrpertyNamesByClassName() {
    return tch.getConnectorSourceOutputPrpertyNamesByClassName();
  }

  protected Map<Field, Set<Field>> getFieldWithInputs() {
    return tch.getFieldWithInputs();
  }

  protected Map<Field, Set<Field>> getFieldWithOutputs() {
    return tch.getFieldWithOutputs();
  }

  protected Set<String> getTransferFieldNames() {
    return tch.getTransferFieldNames();
  }

  protected Set<String> getSigNameWithTransferConnectorWithSameInputOutputFieldType() {
    return tch.getSigNameWithTransferConnectorWithSameInputOutputFieldType();
  }

  protected Set<Sig> getSigWithTransferField() {
    return tch.getSigWithTransferField();
  }

  protected Set<String> getTransferingTypeSig() {
    return tch.getTransferingTypeSig();
  }

  protected List<String> getMessages() {
    return this.messages;
  }
}
