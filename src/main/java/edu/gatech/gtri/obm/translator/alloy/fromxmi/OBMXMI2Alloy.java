package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Property;
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

  public static void main(String[] args) throws FileNotFoundException, UMLModelErrorException {

    OBMXMI2Alloy test = new OBMXMI2Alloy();
    File xmiFile = new File(OBMXMI2Alloy.class.getResource("/OBMModel.xmi").getFile());
    // String className = "Model::Basic::BehaviorFork";
    // String className = "Model::Basic::BehaviorJoin";
    // String className = "Model::Basic::ComplexBehavior";
    String className = "Model::Basic::ControlFlowBehavior";
    // String className = "Model::Basic::BehaviorDecision";
    test.createAlloyFile(xmiFile, className);
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
      toAlloy.createAlloyFile();
    }
  }


  /**
   * @xmiFile xmi file containing activity
   * @param _className QualifiedName of Class containing activities
   * @param startNodeName name of initial node.
   * @throws FileNotFoundException
   * @throws UMLModelErrorException
   */
  public void loadOBMAndCreateAlloy(File xmiFile, String _className)
      throws FileNotFoundException, UMLModelErrorException {

    ResourceSet rs = EMFUtil.createResourceSet();
    Resource r = EMFUtil.loadResourceWithDependencies(rs,
        URI.createFileURI(xmiFile.getAbsolutePath()), null);
    SysMLUtil sysMLUtil = new SysMLUtil(rs);
    SysMLAdapter sysmladapter = new SysMLAdapter(xmiFile, null);
    org.eclipse.uml2.uml.NamedElement mainClass = EMFUtil.getNamedElement(r, _className);
    Graph2AlloyExpr ge = new Graph2AlloyExpr();
    Sig mainSig;
    if (mainClass instanceof org.eclipse.uml2.uml.Class) {
      Class c = (org.eclipse.uml2.uml.Class) mainClass;
      mainSig = toAlloy.addAlloySig(mainClass.getName(), "parent not used for now", true);
      Set<org.eclipse.uml2.uml.Property> atts = sysMLUtil.getAllCorrectedAttributes(c);
      for (Property p : atts) {
        // System.out.println("\t" + p.getName() + " " + p.getLower() + " " + p.getUpper());
        ge.addNode(p.getName());
        org.eclipse.uml2.uml.Type eType = p.getType();
        edu.umd.omgutil.uml.Element omgE = sysmladapter.mapObject(eType);
        // System.out.println("type: " + omgE + " " + omgE.getClass());
        // System.out.println("type's general....");
        if (eType instanceof org.eclipse.uml2.uml.Class) {

          // Set<org.eclipse.uml2.uml.Classifier> generals =
          // sysMLUtil.getGenerals((org.eclipse.uml2.uml.Class) eType, false, false);
          // System.out.println("#of general: " + generals.size());
          // // for (Classifier general : generals) {
          // System.out.println("General: " + general);
          // System.out
          // .println("================Alloy create Sig based for field's ================");
          // addAlloySig(eType.getName(), general.getName());
          toAlloy.addAlloySig(eType.getName(), "???");
          // }
          Sig.Field f = toAlloy.addAlloyField(p.getName(),
              ((org.eclipse.uml2.uml.Class) eType).getName(), mainClass.getName());
          if (p.getLower() == 1 && p.getUpper() == 1)
            toAlloy.addOneConstraintToField(f, mainClass.getName());
        }


      }
      // System.out.println("==============Alloy field for " + sa.getName() + "==================");
      // for (Iterator<Entry<String, String>> iter = fieldNamefieldType.entrySet().iterator(); iter
      // .hasNext();) {
      // Entry<String, String> entry = iter.next();
      // System.out.println(entry);
      // addAlloyField(entry.getKey(), entry.getValue(), sa.getName());
      // }

      Set<org.eclipse.uml2.uml.Connector> connectors = sysMLUtil.getAllConnectors(c);

      for (org.eclipse.uml2.uml.Connector cn : connectors) {
        edu.umd.omgutil.uml.Element omgE = sysmladapter.mapObject(cn);

        if (omgE instanceof edu.umd.omgutil.uml.Connector) {
          edu.umd.omgutil.uml.Connector omgConnector = (edu.umd.omgutil.uml.Connector) omgE;

          edu.umd.omgutil.uml.Type owner = omgConnector.getFeaturingType();
          String source = null;
          String target = null;
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
              } else if (definingEndName.equals("happensBefore")) {
                target = endsFeatureNames.get(0);
              }

            }
            // System.out.println("=================Graph creating Edge ========" + source + "->"
            // + target + "=======================");
            if (source != null && target != null) {
              ge.addEdge(source + target, source, target);
            }
          } // end of connectorEnd
        } // end of Connector
      } // org.eclipse.uml2.uml.Connector

      System.out.println("HB Function: ");
      Map<IObject, IObject> happensBeforeFnInfo = ge.getHappensBeforeFunction(); // before, after
      Graph2AlloyExpr.print(happensBeforeFnInfo);
      for (IObject before : happensBeforeFnInfo.keySet()) {
        List<Expr> lbefore = toExprs(before, mainSig);
        List<Expr> lafter = toExprs(happensBeforeFnInfo.get(before), mainSig);

        for (int i = 0; i < lbefore.size(); i++) {
          Expr beforeExpr = lbefore.get(i);
          for (int j = 0; j < lafter.size(); j++) {
            Expr afterExpr = lafter.get(j);
            toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(mainSig, beforeExpr,
                afterExpr);
          }
        }
      } // end iterator

      System.out.println("HB Inverse Function: ");
      Map<IObject, IObject> happensBeforeInvFnInfo = ge.getHappensBeforeInvFunction(); // before,
      Graph2AlloyExpr.print(happensBeforeInvFnInfo); // after

      for (IObject before : happensBeforeInvFnInfo.keySet()) {
        List<Expr> lbefore = toExprs(before, mainSig);
        List<Expr> lafter = toExprs(happensBeforeInvFnInfo.get(before), mainSig);

        for (int i = 0; i < lbefore.size(); i++) {
          Expr beforeExpr = lbefore.get(i);
          for (int j = 0; j < lafter.size(); j++) {
            Expr afterExpr = lafter.get(j);

            // toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(mainSig, beforeExpr,
            // afterExpr);
            toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(mainSig,
                beforeExpr, afterExpr);
          }
        }
      } // end iterator

    } // end of class
    toAlloy.addRemainingFactAndPredicate();
  }

  public List<Expr> toExprs(IObject _o, Sig _mainSig) {
    List<Expr> exprs = new ArrayList<>();
    if (_o instanceof OListOR) {
      ONode onode = (ONode) ((OListOR) _o).get(0);
      Expr expr = _mainSig.domain(toAlloy.getFieldByName().get(onode.getName()));
      for (int i = 1; i < ((OListOR) _o).size(); i++) {
        onode = (ONode) ((OListOR) _o).get(i);
        expr = expr.plus(_mainSig.domain(toAlloy.getFieldByName().get(onode.getName())));
      }
      exprs.add(expr);
    } else if (_o instanceof OListAND) {
      ONode onode = (ONode) ((OListAND) _o).get(0);
      exprs.add(_mainSig.domain(toAlloy.getFieldByName().get(onode.getName())));
      for (int i = 1; i < ((OListAND) _o).size(); i++) {
        onode = (ONode) ((OListAND) _o).get(i);
        exprs.add(_mainSig.domain(toAlloy.getFieldByName().get(onode.getName())));
      }
    } else {
      ONode onode = (ONode) _o;
      exprs.add(_mainSig.domain(toAlloy.getFieldByName().get(onode.getName())));
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


