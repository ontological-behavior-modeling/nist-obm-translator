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
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;

/**
 * A class to handle "Oneof" connector
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 */
public class ConnectorsHandler_OneOf {

  /** omgutil's SysMLAdapter **/
  SysMLAdapter sysmladapter;
  /** omgutil's SysMLUtil **/
  SysMLUtil sysmlUtil;
  /** a class to connect this class to Alloy for creation of facts **/
  ToAlloy toAlloy;
  /** a set of messages collected during handlOneOfConnectors method **/
  List<String> messages;

  /**
   * A constructor
   * 
   * @param _sysmlUtil
   * @param _sysmladapter
   * @param _toAlloy
   * @param _messages
   */
  protected ConnectorsHandler_OneOf(SysMLUtil _sysmlUtil, SysMLAdapter _sysmladapter,
      ToAlloy _toAlloy, List<String> _messages) {
    sysmlUtil = _sysmlUtil;
    sysmladapter = _sysmladapter;
    toAlloy = _toAlloy;
    messages = _messages;
  }

  /**
   * Find "OneOf" connectors from the given connectors and facts: bijectionFiltered, functionFiltered, or inverseFunctionFiltered.
   * 
   * @param _sigOfClass(PrimSig) - the owner signature of "OneOf" connector to be processed
   * @param _classOfSig(Class) - the class created the owner signature
   * @param connectors (A set of Connectors) - the all connectors filtered by the given signature
   * @return (A set of Connectors) - the "OneOf" connectors for the given _sigOfClass signature
   */
  protected Set<Connector> handleOneOfConnectors(PrimSig _sigOfClass, Class _classOfSig,
      Set<org.eclipse.uml2.uml.Connector> connectors) {

    // start handling one of connectors
    // find sig's constraint
    Set<Constraint> constraints = sysmlUtil.getAllRules(_classOfSig);
    Set<EList<Element>> oneOfSets = AlloyUtils.getOneOfRules(sysmladapter, constraints); // EList<ConnectorEnd> [ [start, eat] or [order, end]]


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
      addFacts(_sigOfClass, oneOfSet, oneOfConnectors, oneSourceProperties,
          oneTargetProperties);
    }
    return oneOfConnectors;
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
  private void addFacts(PrimSig sigOfClass, List<Element> _oneOfSet,
      Set<Connector> _oneOfConnectors,
      Set<ConnectableElement> _oneSourceProperties,
      Set<ConnectableElement> _oneTargetProperties) {

    List<ConnectableElement> sources = new ArrayList<>();
    List<ConnectableElement> targets = new ArrayList<>();

    boolean isSourceSideOneOf = false;
    for (org.eclipse.uml2.uml.Connector cn : _oneOfConnectors) {
      for (ConnectorEnd ce : cn.getEnds()) {
        Optional<Element> found = _oneOfSet.stream().filter(e -> e == ce).findFirst();
        if (!found.isEmpty()) {
          List<ConnectableElement> ces = UML2Utils.getEndRolesForCEFirst(cn, ce);
          if (ces == null) { // this should not happens
            this.messages.add("A connector " + cn.getQualifiedName()
                + " does not have two connector ends, so ignored.");
            return;
          }
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

    if (isSourceSideOneOf)  // sourceSide has One Of - all targets have the same name
      handleSourceSideOneOf(sources, targets.get(0).getName(), sigOfClass, _oneSourceProperties);
    else  // targetSide has OneOf - all sources have the same name
      handleTargetSideOneOf(targets, sources.get(0).getName(), sigOfClass, _oneTargetProperties);
  }

  /**
   * A method to handle a connector when source-side connector is "OneOf"
   * 
   * @param sources
   * @param targetName
   * @param _sigOfClass
   * @param _oneSourceProperties
   */
  private void handleSourceSideOneOf(List<ConnectableElement> sources,
      String targetName, PrimSig _sigOfClass,
      Set<ConnectableElement> _oneSourceProperties) {
    Expr beforeExpr = null;
    Expr afterExpr = null;

    afterExpr =
        AlloyUtils.getFieldFromSigOrItsParents(targetName, _sigOfClass);
    List<String> sourceNames = // sorting source names alphabetically = how to be write out
        sources.stream().map(e -> e.getName()).sorted().collect(Collectors.toList());
    for (String sourceName : sourceNames) {
      beforeExpr = beforeExpr == null
          ? AlloyUtils.getFieldFromSigOrItsParents(sourceName,
              _sigOfClass)
          : beforeExpr.plus(AlloyUtils
              .getFieldFromSigOrItsParents(sourceName, _sigOfClass));
    }

    boolean allSourceOneOf = true;
    if (_oneSourceProperties.size() == sources.size()) {
      for (ConnectableElement ce : _oneSourceProperties) {
        if (!sources.contains(ce)) {
          allSourceOneOf = false;
          break;
        }
      }
    } else
      allSourceOneOf = false;

    // if both are one sourceProperties
    if (allSourceOneOf) {  // merge a + b -> c
      this.toAlloy.createBijectionFilteredHappensBefore(_sigOfClass, beforeExpr,
          afterExpr);
    } else {
      for (ConnectableElement ce : _oneSourceProperties) {
        // need fn a -> b or a ->c
        Expr beforeExpr_modified = AlloyUtils.getFieldFromSigOrItsParents(ce.getName(),
            _sigOfClass); // start
        this.toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(
            _sigOfClass,
            beforeExpr_modified,
            afterExpr);// order
      }
      // inverse fn a + b -> c
      // fact {all x: OFControlLoopFoodService | inverseFunctionFiltered[happensBefore, x.a + x.b, x.c]}
      this.toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(
          _sigOfClass,
          beforeExpr, // a + b
          afterExpr);// order
    }
  }

  /**
   * A method to handle a connector when target-side connector is "OneOf"
   * 
   * @param targets
   * @param sourceName
   * @param _sigOfClass
   * @param _oneTargetProperties
   */
  private void handleTargetSideOneOf(List<ConnectableElement> targets,
      String sourceName, PrimSig _sigOfClass,
      Set<ConnectableElement> _oneTargetProperties) {// , Expr beforeExpr, Expr afterExpr) {

    Expr beforeExpr = null;
    Expr afterExpr = null;
    List<String> targetNames = // sorting target names alphabetically = to be write out
        targets.stream().map(e -> e.getName()).sorted().collect(Collectors.toList());
    for (String targetName : targetNames) {
      afterExpr = afterExpr == null
          ? AlloyUtils.getFieldFromSigOrItsParents(targetName,
              _sigOfClass)
          : afterExpr.plus(AlloyUtils
              .getFieldFromSigOrItsParents(targetName, _sigOfClass));
    }
    beforeExpr = AlloyUtils
        .getFieldFromSigOrItsParents(sourceName, _sigOfClass);

    boolean allTargetOneOf = true; // default
    if (_oneTargetProperties.size() == targets.size()) {
      for (ConnectableElement ce : _oneTargetProperties) {
        if (!targets.contains(ce)) {
          allTargetOneOf = false;
          break;
        }
      }
    } else
      allTargetOneOf = false;

    // if both are one targetProperties
    if (allTargetOneOf) { // decision a -> b + c
      this.toAlloy.createBijectionFilteredHappensBefore(_sigOfClass, beforeExpr,
          afterExpr);
    } else {
      // fn a -> b + c
      // fact {all x: OFControlLoopFoodService | functionFiltered[happensBefore, x.a, x.b + x.c]}
      this.toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(_sigOfClass,
          beforeExpr,
          afterExpr);

      // inversefn a -> b or a -> c
      for (ConnectableElement ce : _oneTargetProperties) {
        Expr afterExpr_modified = AlloyUtils.getFieldFromSigOrItsParents(ce.getName(),
            _sigOfClass);// end
        this.toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(
            _sigOfClass,
            beforeExpr,
            afterExpr_modified);
      }
    }
  }

}
