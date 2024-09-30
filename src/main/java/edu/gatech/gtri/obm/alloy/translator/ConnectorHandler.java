package edu.gatech.gtri.obm.alloy.translator;

import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;

/**
 * A class to handle a connector
 *
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 */
public class ConnectorHandler {

  private enum CONNECTOR_TYPE {
    HAPPENS_BEFORE,
    HAPPENS_DURING,
    TRANSFER;
  }

  /** Stereotype qualified names */
  private static String STEREOTYPE_BINDDINGCONNECTOR = "SysML::BindingConnector";

  Map<Field, Set<Field>> fieldWithInputs;
  // key = prepare, value= [preparedFoodItem,prepareDestination]
  Map<Field, Set<Field>> fieldWithOutputs;
  // key = order, value=[orderAmount, orderDestination, orderedFoodItem]

  /** A dictionary contains signature name as key and a set of fact expression as value. */
  Map<String, Set<Expr>> sigToFactsMap;
  /**
   * A set of connectors redefined by children so that the connectors are ignored by the parent.
   * This variable is initialized in ConnectorsHandler and updated and used by a ConnectorHandler
   * while handling each connector at a time.
   */
  Set<Connector> redefinedConnectors;

  /** A set of Alloy fields created for Properties with <<Parameter>> stereotype */
  Set<Field> parameterFields;
  /** A class connect this connector class and Alloy class */
  ToAlloy toAlloy;

  /** A list of messages to collect during this class */
  List<String> messages;

  /** A dictionary contains signature name as key and a set of transfer field names as value. */
  Map<String, Set<String>> sigToTransferFieldMap;
  /** A dictionary contains signature name as key and a set of step property/field names as value */
  Map<String, Set<String>> stepPropertiesBySig;

  /** A set of transfer field names */
  protected Set<String> transferFieldNames;

  /** omgutils sysmladapter * */
  SysMLAdapter sysmladapter;
  /** omgutils sysmlutl * */
  SysMLUtil sysmlUtil;

  /** A transfer connecter handler if this connector is transfer connector * */
  ConnectorHandler_Transfer transfer_connectorHandler;

  /**
   * A constructor
   *
   * @param _redefinedConnectors (Set<Connector>)
   * @param _toAlloy (ToAlloy)
   * @param _sigToFactsMap (Map<String, Set<Expr>)
   * @param _parameterFields (Set<Field>)
   * @param _sigToTransferFieldMap (Map<String, Set<String>>)
   * @param _sysmladapter (SysMLAdapter)
   * @param _sysmlUtil (SysMLUtil)
   */
  protected ConnectorHandler(
      Set<Connector> _redefinedConnectors,
      ToAlloy _toAlloy,
      Map<String, Set<Expr>> _sigToFactsMap,
      Set<Field> _parameterFields,
      Map<String, Set<String>> _sigToTransferFieldMap,
      SysMLAdapter _sysmladapter,
      SysMLUtil _sysmlUtil) {

    this.sysmladapter = _sysmladapter;
    this.sysmlUtil = _sysmlUtil;

    // passed by reference from ConnenctorHandler used and updated during in instance of this class
    redefinedConnectors = _redefinedConnectors;

    // pass from OBMXMI2Alloy -> ConnectorHandler to this class used to check if field is
    // parameterfield or not
    parameterFields = _parameterFields;
    toAlloy = _toAlloy;

    sigToTransferFieldMap = _sigToTransferFieldMap;
    sigToFactsMap = _sigToFactsMap;

    messages = new ArrayList<String>();

    transfer_connectorHandler =
        new ConnectorHandler_Transfer(
            toAlloy,
            sigToFactsMap,
            sigToTransferFieldMap,
            redefinedConnectors,
            parameterFields,
            messages);
  }

  /**
   * Create facts for a signature by observing own connectors.
   *
   * @param _sigOgClass(PrimSig) - the owner of the connector
   * @param _isSigLeaf(boolean) - true if this signature is leaf, otherwise false
   */
  protected void processConnectorsForASig(Class _sigOgClass, boolean _isSigLeaf) {

    transfer_connectorHandler
        .reset(); // transferFieldNames requires to reset for each class. The transferFiledNames
    // used to define stepProperties.

    PrimSig sigOfClass = this.toAlloy.getSig(_sigOgClass.getName());
    Set<org.eclipse.uml2.uml.Connector> connectors = sysmlUtil.getOwnedConnectors(_sigOgClass);

    // handle one of connectors
    ConnectorsHandler_OneOf och =
        new ConnectorsHandler_OneOf(sysmlUtil, sysmladapter, toAlloy, this.messages);
    Set<Connector> oneOfConnectors = och.handleOneOfConnectors(sigOfClass, _sigOgClass, connectors);

    // process remaining of connectors
    for (org.eclipse.uml2.uml.Connector cn : connectors) {
      if (oneOfConnectors.contains(cn))
        continue; // oneof connectors are already handled above so skip here
      if (_sigOgClass.getInheritedMembers().contains(cn)) continue; // ignore inherited

      // for example) while translating IFSingleFoolService and processing connectors for
      // IFFoodService,
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
              if (source != null && target != null)
                this.toAlloy.addEqualFact(sigOfClass, source, target);
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

            } else if (definingEndName.equals("transferTarget")) {
              connector_type = CONNECTOR_TYPE.TRANSFER;
              target = endsFeatureNames.get(0);
              targetTypeName = ce.getRole().getType().getName();
            }

            if (source == null || target == null) continue;

            // targetField is null
            // i.e., For 4.1.4 Transfers and Parameters - a connector <<ItemFlow>> for b(B).vout and
            // b2(B2).vout
            // source="b2" target="b", sigOfClass= "B" -- "b" is not a field of "B"
            Field sourceField = AlloyUtils.getFieldFromSigOrItsParents(source, sigOfClass);
            Field targetField = AlloyUtils.getFieldFromSigOrItsParents(target, sigOfClass);

            if (connector_type == CONNECTOR_TYPE.HAPPENS_BEFORE) {
              this.toAlloy.addBijectionFilteredHappensBeforeFact(
                  sigOfClass, sourceField, targetField);
            } else if (connector_type == CONNECTOR_TYPE.HAPPENS_DURING)
              this.toAlloy.addBijectionFilteredHappensDuringFact(
                  sigOfClass, sourceField, targetField);
            else if (connector_type == CONNECTOR_TYPE.TRANSFER) {
              transfer_connectorHandler.handleTransferConnector(
                  cn,
                  sigOfClass,
                  _isSigLeaf,
                  sourceTypeName,
                  targetTypeName,
                  sourceField,
                  targetField,
                  source,
                  target);
            }
          }
        } // end of connectorEnd
      } // end of Connector
    } // org.eclipse.uml2.uml.Connector
  }

  // Get methods to pass back to connectors handler
  /**
   * Get method for transfer_connectorHandler(ConnectorHandler_Transfer) instance variable's
   * connectorTargetInputPropertyNamesByClassName instance variable
   *
   * @return Map<String, Set<String>>
   */
  protected Map<String, Set<String>> getConnectorsTargetInputPropertyNamesByClassName() {
    return transfer_connectorHandler.getConnectorsTargetInputPropertyNamesByClassName();
  }

  /**
   * Get method for transfer_connectorHandler(ConnectorHandler_Transfer) instance variable's
   * connectorSourceOutputPrpertyNamesByClassName instance variable
   *
   * @return Map<String, Set<String>>
   */
  protected Map<String, Set<String>> getConnectorsSourceOutputPrpertyNamesByClassName() {
    return transfer_connectorHandler.getConnectorsSourceOutputPrpertyNamesByClassName();
  }

  /**
   * Get method for transfer_connectorHandler(ConnectorHandler_Transfer) instance variable's
   * fieldWithInputs instance variable
   *
   * @return Map<Field, Set<Field>>
   */
  protected Map<Field, Set<Field>> getFieldWithInputs() {
    return transfer_connectorHandler.getFieldWithInputs();
  }

  /**
   * Get method for transfer_connectorHandler(ConnectorHandler_Transfer) instance variable's
   * fieldWithOutputs instance variable
   *
   * @return Map<Field, Set<Field>>
   */
  protected Map<Field, Set<Field>> getFieldWithOutputs() {
    return transfer_connectorHandler.getFieldWithOutputs();
  }

  /**
   * Get method for transfer_connectorHandler(ConnectorHandler_Transfer) instance variable's
   * transferFieldNames instance variable
   *
   * @return Set<String>
   */
  protected Set<String> getTransferFieldNames() {
    return transfer_connectorHandler.getTransferFieldNames();
  }

  /**
   * Get method for transfer_connectorHandler(ConnectorHandler_Transfer) instance variable's
   * sigNamesWithTransferConnectorWithSameInputOutputFieldType instance variable
   *
   * @return Set<String>
   */
  protected Set<String> getSigNameWithTransferConnectorWithSameInputOutputFieldType() {
    return transfer_connectorHandler.getSigNameWithTransferConnectorWithSameInputOutputFieldType();
  }

  /**
   * Get method for transfer_connectorHandler(ConnectorHandler_Transfer) instance variable's
   * sigNamesWithTransferConnectorWithSameInputOutputFieldType instance variable
   *
   * @return Set<Sig>
   */
  protected Set<Sig> getSigWithTransferField() {
    return transfer_connectorHandler.getSigWithTransferField();
  }

  /**
   * Get method for transfer_connectorHandler(ConnectorHandler_Transfer) instance variable's
   * transferingTypeSig instance variable
   *
   * @return Set<String>
   */
  protected Set<String> getTransferingTypeSig() {
    return transfer_connectorHandler.getTransferingTypeSig();
  }

  /**
   * Get method for messages collected while executing this processConnectorsForASig method
   *
   * @return List<String>
   */
  protected List<String> getMessages() {
    return this.messages;
  }
}
