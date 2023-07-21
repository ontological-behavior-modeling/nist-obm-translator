package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Property;
import org.graphstream.graph.Edge;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.graph.Graph2AlloyExpr;
import edu.gatech.gtri.obm.translator.alloy.graph.IObject;
import edu.gatech.gtri.obm.translator.alloy.graph.OListAND;
import edu.gatech.gtri.obm.translator.alloy.graph.OListOR;
import edu.gatech.gtri.obm.translator.alloy.graph.ONode;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.umd.omgutil.EMFUtil;
import edu.umd.omgutil.UMLModelErrorException;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;



public class OBMXMI2Alloy {

  ToAlloy toAlloy;

  public static void main(String[] args) throws Exception {

    // using C:\Users\mw107\AppData\Local\Temp\Transfer.als
    // System.setProperty(("java.io.tmpdir"), System.getProperty("user.dir"));
    System.setProperty(("java.io.tmpdir"),
        "C:/Users/mw107/Documents/Projects/NIST OBM/info/obm-alloy-code_2023-05-26/obm");
    OBMXMI2Alloy test = new OBMXMI2Alloy();
    File xmiFile = new File(OBMXMI2Alloy.class.getResource("/OBMModel_MW.xmi").getFile());

    // String className = "Model::Basic::BehaviorFork";
    // String className = "Model::Basic::BehaviorJoin";
    // String className = "Model::Basic::ControlFlowBehavior";
    // String className = "Model::Basic::BehaviorDecision";
    // String className = "Model::Basic::Loop";
    // String className = "Model::Basic::ComplexBehavior";
    // String className = "Model::Basic::ComplexBehavior_MW";
    String className = "Model::Basic::ComposedBehavior";
    test.createAlloyFile(xmiFile, className);
  }

  public Expr getOverallFacts() {
    return toAlloy.getOverallFacts();
  }

  public Map<String, Sig> getSigMap() {
    return toAlloy.getSigMap();
  }

  public OBMXMI2Alloy() throws FileNotFoundException, UMLModelErrorException {
    toAlloy = new ToAlloy();


  }

  public void createAlloyFile(File xmiFile, String className)
      throws FileNotFoundException, UMLModelErrorException {
    if (!xmiFile.exists() || !xmiFile.canRead())
      System.err.println("File " + xmiFile.getAbsolutePath() + " does not exist or read.");
    else {
      this.loadOBMAndCreateAlloy(xmiFile, className);
      String outputFileName = toAlloy.createAlloyFile();
      System.out.println(outputFileName + " is created");
    }
  }

  private Map<org.eclipse.uml2.uml.Class, Map<org.eclipse.uml2.uml.Class, List<Property>>> addClasses(
      org.eclipse.uml2.uml.Class umlClass, SysMLUtil sysMLUtil,
      Map<org.eclipse.uml2.uml.Class, Map<org.eclipse.uml2.uml.Class, List<Property>>> propertiesByClass) {
    Set<org.eclipse.uml2.uml.Property> atts = sysMLUtil.getAllCorrectedAttributes(umlClass);
    // find property having the same type
    Map<org.eclipse.uml2.uml.Class, List<Property>> propertiesByTheirType = new HashMap<>();
    for (Property p : atts) {
      org.eclipse.uml2.uml.Type eType = p.getType();
      List<Property> ps = null;
      if ((ps = propertiesByTheirType.get(eType)) == null) {
        ps = new ArrayList<>();
        propertiesByTheirType.put((org.eclipse.uml2.uml.Class) eType, ps);
      }
      propertiesByTheirType.get(eType).add(p);

      if (eType instanceof org.eclipse.uml2.uml.Class) {
        toAlloy.addAlloySig(eType.getName(), "parent not used for now");
        addClasses((org.eclipse.uml2.uml.Class) eType, sysMLUtil, propertiesByClass);
      }
      propertiesByClass.put(umlClass, propertiesByTheirType);
    }
    return propertiesByClass;
  }

  /**
   * @xmiFile xmi file containing activity
   * @param _className QualifiedName of Class containing activities
   * @param startNodeName name of initial node.
   * @throws FileNotFoundException
   * @throws UMLModelErrorException
   * @throws Exception
   */
  public void loadOBMAndCreateAlloy(File xmiFile, String _className)
      throws FileNotFoundException, UMLModelErrorException {

    ResourceSet rs = EMFUtil.createResourceSet();
    Resource r = EMFUtil.loadResourceWithDependencies(rs,
        URI.createFileURI(xmiFile.getAbsolutePath()), null);
    SysMLUtil sysMLUtil = new SysMLUtil(rs);
    // soneof =
    // com.engisis.xmiutil.SysMLUtil.loadStereotype(OBMXMI2Alloy.class.getResource("/OBM.xmi"),
    // URI.createURI("http://www.nist.gov/sid/obm"), "OneOf");


    SysMLAdapter sysmladapter = new SysMLAdapter(xmiFile, null);
    org.eclipse.uml2.uml.NamedElement mainClass = EMFUtil.getNamedElement(r, _className);
    Graph2AlloyExpr ge = new Graph2AlloyExpr();
    Map<org.eclipse.uml2.uml.Class, Map<org.eclipse.uml2.uml.Class, List<Property>>> propertiesByClass =
        new HashMap<>();

    if (mainClass == null)
      return;
    else if (mainClass instanceof org.eclipse.uml2.uml.Class) {
      Sig mainSig = toAlloy.addAlloySig(mainClass.getName(), "parent not used for now", true);
      propertiesByClass =
          addClasses((org.eclipse.uml2.uml.Class) mainClass, sysMLUtil, propertiesByClass);
    }

    for (org.eclipse.uml2.uml.Class c : propertiesByClass.keySet()) {
      Map<org.eclipse.uml2.uml.Class, List<Property>> propertiesByType = propertiesByClass.get(c);
      for (org.eclipse.uml2.uml.Class propertyType : propertiesByType.keySet()) {
        List<Property> ps = propertiesByType.get(propertyType);
        String[] fsa = new String[ps.size()];
        int i = 0;
        for (Property p : ps) {
          fsa[i] = p.getName();
          ge.addNode(p.getName());
          i++;
        }
        Sig.Field[] fields = toAlloy.addDisjAlloyFields(fsa, propertyType.getName(), c.getName());
        i = 0;
        for (Property p : ps) {
          if (p.getLower() == 1 && p.getUpper() == 1)
            toAlloy.addOneConstraintToField(fields[i], c.getName());
          i++;
        }

      }
      Sig thisSig = toAlloy.getSigMap().get(c.getName());
      // }//end of class



      // if (mainClass instanceof org.eclipse.uml2.uml.Class) {
      // org.eclipse.uml2.uml.Class c = (org.eclipse.uml2.uml.Class) mainClass;
      // mainSig = toAlloy.addAlloySig(mainClass.getName(), "parent not used for now", true);
      // Set<org.eclipse.uml2.uml.Property> atts = sysMLUtil.getAllCorrectedAttributes(c);
      //
      // Map<Sig, List<Property>> fieldNameByTypeSig = new HashMap<>();
      // for (Property p : atts) {
      // System.out.println(p.getName());
      // ge.addNode(p.getName());
      // org.eclipse.uml2.uml.Type eType = p.getType();
      // if (eType instanceof org.eclipse.uml2.uml.Class) {
      // Sig typeSig = toAlloy.addAlloySig(eType.getName(), "parent not used for now");
      // List<Property> fieldNames = fieldNameByTypeSig.get(typeSig);
      // System.out.println(fieldNames);
      // if (fieldNames == null) {
      // fieldNames = new ArrayList<>();
      // fieldNames.add(p);
      // fieldNameByTypeSig.put(typeSig, fieldNames);
      // } else {
      // fieldNames.add(p);
      // // fieldNameByTypeSig.put(typeSig, fieldNames);
      // }
      // }
      // }
      //
      // for (Sig keysig : fieldNameByTypeSig.keySet()) {
      // List<Property> fs = fieldNameByTypeSig.get(keysig);
      // System.out.println(keysig.label);
      // System.out.println(fs.size());
      // String[] fsa = new String[fs.size()];
      // for (int i = 0; i < fs.size(); i++)
      // fsa[i] = fs.get(i).getName();
      // Sig.Field[] fields = toAlloy.addDisjAlloyFields(fsa, keysig.label, mainClass.getName());
      // for (int i = 0; i < fs.size(); i++) {
      // if (fs.get(i).getLower() == 1 && fs.get(i).getUpper() == 1)
      // toAlloy.addOneConstraintToField(fields[i], mainClass.getName());
      // }
      // }



      Set<Constraint> constraints = sysMLUtil.getAllRules(c);
      Set<org.eclipse.uml2.uml.Connector> connectors = sysMLUtil.getAllConnectors(c);

      for (org.eclipse.uml2.uml.Connector cn : connectors) {
        edu.umd.omgutil.uml.Element omgE = sysmladapter.mapObject(cn);

        if (omgE instanceof edu.umd.omgutil.uml.Connector) {
          edu.umd.omgutil.uml.Connector omgConnector = (edu.umd.omgutil.uml.Connector) omgE;

          // sysMLUtil.getAllRules(c).stream()
          // .forEach(rule -> System.out.println(rule.getSpecification()));
          // Constraint oneof = sysMLUtil.getAllRules(c).stream()
          // .filter(c -> c.isStereotypeApplied(obmutil.getOneOf())).findAny().orElse(null);
          // if (oneof != null) {
          //
          // }

          edu.umd.omgutil.uml.Type owner = omgConnector.getFeaturingType();
          String source = null;
          String target = null;
          ConnectorEnd sourceCN = null;
          ConnectorEnd targetCN = null;
          for (ConnectorEnd ce : ((Connector) cn).getEnds()) {
            if (ce.getDefiningEnd() != null) {
              // System.out.println(ce.getDefiningEnd().getName());
              String definingEndName = ce.getDefiningEnd().getName();
              edu.umd.omgutil.uml.ConnectorEnd end =
                  (edu.umd.omgutil.uml.ConnectorEnd) sysmladapter.mapObject(ce);
              List<String> endsFeatureNames = end.getCorrectedFeaturePath(owner).stream()
                  .map(f -> f.getName()).collect(Collectors.toList());

              if (definingEndName.equals("happensBefore-1")) {
                source = endsFeatureNames.get(0);
                sourceCN = ce;
              } else if (definingEndName.equals("happensBefore")) {
                target = endsFeatureNames.get(0);
                targetCN = ce;
              }

            }
            // System.out.println("=================Graph creating Edge ========" + source + "->"
            // + target + "=======================");
            if (source != null && target != null) {
              System.out.println("Adding edge: " + source + target);
              Edge edge = ge.addEdge(source + target, source, target);
              if (isOneof(constraints, sourceCN, targetCN)) {
                edge.setAttribute("oneof", true);
                System.out.println("Yes one of");
              }
            }
          } // end of connectorEnd
        } // end of Connector
      } // org.eclipse.uml2.uml.Connector



      System.out.println("HB Function: ");
      Map<IObject, IObject> happensBeforeFnInfo = ge.getHappensBeforeFunction(); // before, after
      Graph2AlloyExpr.print(happensBeforeFnInfo);
      for (IObject before : happensBeforeFnInfo.keySet()) {
        List<Expr> lbefore = toExprs(before, thisSig);
        List<Expr> lafter = toExprs(happensBeforeFnInfo.get(before), thisSig);

        for (int i = 0; i < lbefore.size(); i++) {
          Expr beforeExpr = lbefore.get(i);
          for (int j = 0; j < lafter.size(); j++) {
            Expr afterExpr = lafter.get(j);
            toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(thisSig, beforeExpr,
                afterExpr);
          }
        }
      } // end iterator

      System.out.println("HB Inverse Function: ");
      Map<IObject, IObject> happensBeforeInvFnInfo = ge.getHappensBeforeInvFunction(); // before,
      Graph2AlloyExpr.print(happensBeforeInvFnInfo); // after

      for (IObject before : happensBeforeInvFnInfo.keySet()) {
        List<Expr> lbefore = toExprs(before, thisSig);
        List<Expr> lafter = toExprs(happensBeforeInvFnInfo.get(before), thisSig);

        for (int i = 0; i < lbefore.size(); i++) {
          Expr beforeExpr = lbefore.get(i);
          for (int j = 0; j < lafter.size(); j++) {
            Expr afterExpr = lafter.get(j);

            // toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(mainSig,
            // beforeExpr,
            // afterExpr);
            toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(thisSig,
                beforeExpr, afterExpr);
          }
        }
      } // end iterator

    } // end of class
    toAlloy.addRemainingFactAndPredicate();
    ge.display();

  }

  // if any of connector end is startCN or endCN then specify connector is one of
  private boolean isOneof(Set<Constraint> constraints, ConnectorEnd startCN, ConnectorEnd endCN) {
    for (Constraint ct : constraints) {
      EList<Element> constraintedElements = ct.getConstrainedElements();
      if (constraintedElements.size() == 2) {
        if (constraintedElements.get(0) == startCN || constraintedElements.get(0) == endCN
            || constraintedElements.get(1) == startCN || constraintedElements.get(1) == endCN)
          return true;
      }
    }
    return false;


    // for (Element ce : ct.getConstrainedElements()) {
    // if (ce instanceof ConnectorEnd) {
    // ConnectorEnd cend = (ConnectorEnd) ce;
    //
    // // ConnectorEnd defining End
    // // Property cendProperty = cend.getDefiningEnd();
    // // System.out.println(cendProperty.getQualifiedName());
    // // // rule
    // // ConnectableElement rule = cend.getRole();
    // // System.out.println(rule);
    // // if (rule instanceof Property) {
    // // System.out.println("Rule... Property is");
    // // if (start == null)
    // // start = (Property) rule;
    // // else
    // // end = (Property) rule;
    // // System.out.println(rule.getQualifiedName());
    // // }
    // // if (start != null && end != null)
    // // ge.addOneOf(start.getName(), end.getName());
    // //
    // }
    // }
    // }
  }

  // assume if 1st list field in _aSig, all fields are also in _aSig
  public List<Expr> toExprs(IObject _o, Sig _aSig) {
    List<Expr> exprs = new ArrayList<>();
    if (_o instanceof OListOR) {
      ONode onode = (ONode) ((OListOR) _o).get(0);
      Sig.Field f = toAlloy.getField(onode.getName());
      if (f.sig == _aSig) {
        Expr expr = _aSig.domain(f);
        for (int i = 1; i < ((OListOR) _o).size(); i++) {
          onode = (ONode) ((OListOR) _o).get(i);
          expr = expr.plus(_aSig.domain(toAlloy.getField(onode.getName())));
        }
        exprs.add(expr);
      }

    } else if (_o instanceof OListAND) {
      ONode onode = (ONode) ((OListAND) _o).get(0);
      Sig.Field f = toAlloy.getField(onode.getName());
      if (f.sig == _aSig) {
        exprs.add(_aSig.domain(f));
        for (int i = 1; i < ((OListAND) _o).size(); i++) {
          onode = (ONode) ((OListAND) _o).get(i);
          f = toAlloy.getField(onode.getName());
          exprs.add(_aSig.domain(f));
        }
      }
    } else {
      ONode onode = (ONode) _o;
      Sig.Field f = toAlloy.getField(onode.getName());
      if (f.sig == _aSig)
        exprs.add(_aSig.domain(f));
    }
    return exprs;
  }



  public static Object getObjectByName(Resource r, String lookingForName) {

    TreeIterator<Object> iter = EcoreUtil.getAllContents(r, true);
    while (iter.hasNext()) {
      Object o = iter.next();
      if (o instanceof org.eclipse.uml2.uml.NamedElement) {
        String name = ((org.eclipse.uml2.uml.NamedElement) o).getName();
        if (name != null && name.equals(lookingForName))
          return o;
      }
    }
    return null;
  }

  public static List<Alloy> getAllActivities(Resource r) {
    List<Alloy> activities = new ArrayList<>();

    TreeIterator<Object> iter = EcoreUtil.getAllContents(r, false);
    while (iter.hasNext()) {
      Object o = iter.next();
      if (o instanceof Alloy) {
        activities.add((Alloy) o);
      }

    }

    return activities;
  }



}


