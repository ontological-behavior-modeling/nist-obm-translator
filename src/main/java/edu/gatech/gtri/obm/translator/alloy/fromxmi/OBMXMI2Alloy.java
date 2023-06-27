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
import edu.gatech.gtri.obm.translator.alloy.graph.Graph2AlloyExpr2;
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
    String className = "Model::Basic::BehaviorFork";
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

    System.out.println(mainClass);

    System.out.println("=================Graph================================");
    Graph2AlloyExpr2 ge = new Graph2AlloyExpr2();

    System.out.println("===============end graph=================================");


    Sig mainSig;
    // Map<String, String> fieldNamefieldType = new HashMap<>();
    if (mainClass instanceof org.eclipse.uml2.uml.Class) {
      Class c = (org.eclipse.uml2.uml.Class) mainClass;
      System.out.println("================Alloy create mainSig ================");
      mainSig = toAlloy.addAlloySig(mainClass.getName(), "parent not used for now", true);
      // List<Element> elm = sa.getOwnedElements();
      // System.out.println(elm);
      // System.out.println("or=====================");

      // System.out.println("properties.....");
      Set<org.eclipse.uml2.uml.Property> atts = sysMLUtil.getAllCorrectedAttributes(c);

      for (Property p : atts) {
        System.out.println("\t" + p.getName() + " " + p.getLower() + " " + p.getUpper());
        edu.umd.omgutil.uml.Element pElement = sysmladapter.mapObject(p);

        System.out.println("=================Graph================================");
        ge.addNode(p.getName());
        System.out.println("===============end graph=================================");

        org.eclipse.uml2.uml.Type eType = p.getType();
        edu.umd.omgutil.uml.Element omgE = sysmladapter.mapObject(eType);
        System.out.println("type: " + omgE + " " + omgE.getClass());
        System.out.println("type's general....");
        if (eType instanceof org.eclipse.uml2.uml.Class) {

          Set<org.eclipse.uml2.uml.Classifier> generals =
              sysMLUtil.getGenerals((org.eclipse.uml2.uml.Class) eType, false, false);
          System.out.println("#of general: " + generals.size());
          // for (Classifier general : generals) {
          // System.out.println("General: " + general);
          // System.out
          // .println("================Alloy create Sig based for field's ================");
          // addAlloySig(eType.getName(), general.getName());
          toAlloy.addAlloySig(eType.getName(), "???");
          // }
          System.out.println("================Alloy create Field====" + p.getName() + " "
              + ((org.eclipse.uml2.uml.Class) eType).getName() + "============");
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

      System.out.println("connectors....");
      Set<org.eclipse.uml2.uml.Connector> connectors = sysMLUtil.getAllConnectors(c);

      for (org.eclipse.uml2.uml.Connector cn : connectors) {
        System.out.println(cn);
        System.out.println("\tname: " + cn.getName());
        System.out.println("\tkind: " + cn.getKind());
        System.out.println("\ttype: " + cn.getType());
        System.out.println("\tlabel: " + cn.getLabel());
        edu.umd.omgutil.uml.Element omgE = sysmladapter.mapObject(cn);

        if (omgE instanceof edu.umd.omgutil.uml.Connector) {
          edu.umd.omgutil.uml.Connector omgConnector = (edu.umd.omgutil.uml.Connector) omgE;

          edu.umd.omgutil.uml.Type owner = omgConnector.getFeaturingType();
          String source = null;
          String target = null;
          for (ConnectorEnd ce : ((Connector) cn).getEnds()) {
            if (ce.getDefiningEnd() != null) {
              System.out.println(ce.getDefiningEnd().getName());
              String definingEndName = ce.getDefiningEnd().getName();
              edu.umd.omgutil.uml.ConnectorEnd end =
                  (edu.umd.omgutil.uml.ConnectorEnd) sysmladapter.mapObject(ce);
              List<String> zz = end.getCorrectedFeaturePath(owner).stream().map(f -> f.getName())
                  .collect(Collectors.toList());

              if (definingEndName.equals("happensBefore-1")) {
                source = zz.get(0);
              } else if (definingEndName.equals("happensBefore")) {
                target = zz.get(0);
              }

            }
            System.out.println("=================Graph creating Edge ========" + source + "->"
                + target + "=======================");
            if (source != null && target != null) {
              ge.addEdge(source + target, source, target);
            }
          } // end of connectorEnd
        } // end of Connector
      } // org.eclipse.uml2.uml.Connector


      Map<IObject, IObject> happensBeforeInfo = ge.getHappensBeforeFunction();
      // for (Iterator<IObject> iter = happensBeforeInfo.keySet().iterator(); iter.hasNext();) {
      // IObject key = iter.next();
      for (IObject key : happensBeforeInfo.keySet()) {
        List<Expr> before = toExprs(key, mainSig);
        List<Expr> after = toExprs(happensBeforeInfo.get(key), mainSig);

        for (int i = 0; i < before.size(); i++) {
          Expr beforeExpr = before.get(i);
          for (int j = 0; j < after.size(); j++) {
            Expr afterExpr = after.get(j);

            toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(mainSig, beforeExpr,
                afterExpr);
            // toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(mainSig, beforeExpr,
            // afterExpr);
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

  // public static List<Expr> toExprsxxx(IObject _o, Sig _mainSig) {
  // List<Expr> exprs = new ArrayList<>();
  // if (_o instanceof OListOR) {
  // ONode onode = (ONode) ((OListOR) _o).get(0);
  // Expr expr = _mainSig.join(fieldByName.get(onode.getName()));
  // for (int i = 1; i < ((OListOR) _o).size(); i++) {
  // onode = (ONode) ((OListOR) _o).get(i);
  // expr = expr.plus(_mainSig.join(fieldByName.get(onode.getName())));
  // }
  // exprs.add(expr);
  // } else if (_o instanceof OListAND) {
  // ONode onode = (ONode) ((OListAND) _o).get(0);
  // exprs.add(_mainSig.join(fieldByName.get(onode.getName())));
  // for (int i = 1; i < ((OListAND) _o).size(); i++) {
  // onode = (ONode) ((OListAND) _o).get(i);
  // exprs.add(_mainSig.join(fieldByName.get(onode.getName())));
  // }
  // } else {
  // ONode onode = (ONode) _o;
  // exprs.add(_mainSig.join(fieldByName.get(onode.getName())));
  // }
  // return exprs;
  // }
  //


  public static Object getObjectByName(Resource r, String lookingForName) {

    TreeIterator<Object> iter = EcoreUtil.getAllContents(r, true);
    while (iter.hasNext()) {
      Object o = iter.next();
      if (o instanceof org.eclipse.uml2.uml.NamedElement) {

        String name = ((org.eclipse.uml2.uml.NamedElement) o).getName();
        System.out.println(name);
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
    // for (EObject eo : r.getContents()) {
    // if ( eo instanceof org.eclipse.uml2.uml.Namespace) {
    // EList<NamedElement> members = ((org.eclipse.uml2.uml.Namespace)eo).getOwnedMembers();
    // for (NamedElement ne: members) {
    // if ( ne instanceof Activity ) {
    // activities.add((Activity) ne);
    // }
    // }
    // }
    //
    // }
    return activities;
  }

  /**
   * Tests loading a basic SysML file using Eclipse UML2.
   * 
   * @throws FileNotFoundException
   * @throws UMLModelErrorException
   */
  // https://www.omg.org/spec/UML/20161101/UML.xmi
  // public static void loadActivity() throws FileNotFoundException, UMLModelErrorException {
  // ResourceSet rs = EMFUtil.createResourceSet();
  // Resource r = EMFUtil.loadResourceWithDependencies(rs,
  // URI.createFileURI(SYSMLMODEL.getAbsolutePath()), null);
  //
  // org.eclipse.uml2.uml.NamedElement sa =
  // EMFUtil.getNamedElement(r, "Model::Basic::ActivityDecision");
  // if (sa instanceof Activity) {
  // EList<Element> es = sa.getOwnedElements();
  // for (Element e : es) {
  // System.out.println(es);
  // }
  // EList<ActivityNode> anodes = ((Activity) sa).getOwnedNodes();
  // for (ActivityNode anode : anodes) {
  // System.out.println("Node.....");
  // System.out.println(anode);
  // System.out.println("===incomings");
  //
  // EList<ActivityEdge> incomingEdges = anode.getIncomings();
  // for (ActivityEdge edge : incomingEdges) {
  // System.out.println(edge);
  // }
  // System.out.println("===outgoings");
  // EList<ActivityEdge> getOutgoings = anode.getOutgoings();
  // for (ActivityEdge edge : getOutgoings) {
  // System.out.println(edge);
  //
  // }
  // }
  // }

  // assertTrue(
  // rootblock instanceof org.eclipse.uml2.uml.Class &&
  // rootblock.isStereotypeApplied(sysmlutil.getBlock()));

  // org.eclipse.uml2.uml.NamedElement modelx = EMFUtil.getNamedElement(r, "Model");
  // System.out.println(modelx);
  //
  // for (EObject eo : r.getContents())
  // if (eo instanceof Namespace && ((Namespace) eo).getName().equals("Model")) {
  // org.eclipse.uml2.uml.NamedElement model = (Namespace) eo;
  // System.out.println(model);
  // System.out.println("om");
  // for (NamedElement ne : ((Namespace) model).getOwnedMembers())
  // {
  // System.out.println(ne);
  // }
  // break;
  // }
  // }

  /**
   * Tests loading a basic SysML file using the SysML adapter.
   * 
   * @throws FileNotFoundException
   * @throws UMLModelErrorException
   */

  // public void loadSysMLAdapter() throws FileNotFoundException, UMLModelErrorException
  // {
  // SysMLAdapter sysmladapter = new SysMLAdapter(SYSMLMODEL, null);
  // Class rootblock = sysmladapter.getClassFromString(ROOTBLOCK);
  // // assertNotNull(rootblock);
  // }
  //

}

// System.out.println("\tedu.umd.omgutil.uml.Connector: " + omgConnector);
// List<? extends edu.umd.omgutil.uml.ConnectorEnd> ends = omgConnector.getEnds();
// System.out.println("\tconnectorEnds.....");
// for (edu.umd.omgutil.uml.ConnectorEnd end : ends) {
// List<String> zz = end.getCorrectedFeaturePath(owner).stream().map(f -> f.getName())
// .collect(Collectors.toList());
//
//
//
// System.out.println("Connector End");
// System.out.println("\t\t" + end);
// System.out.println("\t\t id" + end.getID());
// System.out.println("\t\t lower: " + end.getEndLower());
// System.out.println("\t\t upper: " + end.getEndUpper());
// System.out.println("\t\t connectedEnd feature?");
// System.out.println("\t\t\t UnlimitedNatural?");
//
//
//
// List<? extends Feature> fs =
// end.getCorrectedFeaturePath(sysmladapter.getUnlimitedNatural());
// for (Feature f : fs) {
// System.out.println(
// "\t\t\t " + f + " " + f.getName() + " " + f.getClass() + " " + f.getID());
// }
// System.out.println("\t\t\t Integer?");
// List<? extends Feature> fs2 = end.getCorrectedFeaturePath(sysmladapter.getInteger());
// for (Feature f2 : fs2) {
// System.out.println("\t\t\t " + f2 + " " + f2.getName() + " " + f2.getClass());
// }
// // System.out.println("\t\t" +
// // ((org.eclipse.uml2.uml.ConnectorEnd)end).getDefiningEnd());
// }
// System.out.println("\t connector type = Association....");
// Set<? extends edu.umd.omgutil.uml.Association> types = omgConnector.getType();
//
// if (types.size() != 1) {
// System.err.println("!!!!!!!!!!!!! more than one type");
// } else {
// edu.umd.omgutil.uml.Association assoiciation = types.iterator().next();;
//
// for (Iterator<? extends edu.umd.omgutil.uml.Property> iter =
// assoiciation.getOwnedEnds().iterator(); iter.hasNext();) {
// edu.umd.omgutil.uml.Property p = iter.next();
// System.out.println(p);
// System.out.println(p.getName());
// System.out.println(p.getClass() + " " + p.getID());
// p.getAllRedefinedFeatures();
// p.getAllRedefinedProperties();
// p.getAllSubsettedFeatures();
// p.getAllSubsettedProperties();
// if (p.getName().equals("source")) {
// System.out.println(p.getFeaturingType());
//
// } else if (p.getName().equals("target")) {
// System.out.println(p.getFeaturingType());
//
// }
//
// }
//
//
// }
//
// for (edu.umd.omgutil.uml.Association type : types) {
// System.out.println("\t\t" + type);
// System.out.println("\t\t" + type.getQualifiedName());
//
//
// System.out.println("\t\t\t Features...");
// List<? extends Feature> fs = type.getAllFeatures();
// for (Feature f : fs) {
// System.out.println("\t\t\t" + f + " " + f.getClass());
// }
// }
//
// }
// }
// System.out.println("constraints.....");
// Set<Constraint> constraints = sysMLUtil.getAllRules(c);
// for (Constraint constraint : constraints) {
// System.out.println(constraint);
// System.out.println("\tconstraint elements: ");
// EList<Element> constraintElements = constraint.getConstrainedElements();
// for (Element constraintElement : constraintElements) {
// System.out.println(constraintElement);
// }
// }
// System.out.println("generals.....recursive = true");
// Set<org.eclipse.uml2.uml.Classifier> generals = sysMLUtil.getGenerals(c, true, false);
// for (Classifier general : generals) {
// System.out.println(general);
// }
// System.out.println("generals.....recursive = false"); // just immediate parent
// Set<org.eclipse.uml2.uml.Classifier> generals2 = sysMLUtil.getGenerals(c, false, false);
// for (Classifier general : generals2) {
// System.out.println(general);
// addAlloySig(sa.getName(), general.getName());
// }
//


