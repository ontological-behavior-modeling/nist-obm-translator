package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.internal.impl.OpaqueExpressionImpl;
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
import edu.umd.omgutil.uml.OpaqueExpression;



public class OBMXMI2Alloy {

  ToAlloy toAlloy;
  SysMLAdapter sysmladapter;

  public static void main(String[] args) throws Exception {

    // // using C:\Users\mw107\AppData\Local\Temp\Transfer.als
    // // System.setProperty(("java.io.tmpdir"), System.getProperty("user.dir"));
    // System.setProperty(("java.io.tmpdir"), "src/test/resources");
    // OBMXMI2Alloy test = new OBMXMI2Alloy();
    //
    // File xmiFile = new File("src/test/resources/OBMModel_R.xmi");
    // // File xmiFile = new File(OBMXMI2Alloy.class.getResource("/OBMModel_MW.xmi").getFile());
    // String className = "Model::Basic::BehaviorDecision";
    // test.createAlloyFile(xmiFile, className);
    // className = "Model::Basic::BehaviorFork";
    // test.createAlloyFile(xmiFile, className);
    // // String className = "Model::Basic::BehaviorJoin";
    // // String className = "Model::Basic::ControlFlowBehavior";
    // // String className = "Model::Basic::BehaviorDecision";
    // // String className = "Model::Basic::Loop";
    // // String className = "Model::Basic::ComplexBehavior";
    // // String className = "Model::Basic::ComplexBehavior_MW";
    // // String className = "Model::Basic::ComposedBehavior";
    // // String className = "Model::Basic::UnsatisfiableComposition2";
    // className = "Model::Basic::TransferProduct";
    // test.createAlloyFile(xmiFile, className);
  }

  public Expr getOverallFacts() {
    return toAlloy.getOverallFacts();
  }

  // public List<Sig> getAllSigs() {
  // return toAlloy.getAllSigs();
  // }

  public Map<String, Sig> getAllReachableUserDefinedSigs() {
    Map<String, Sig> all = toAlloy.getSigMap();
    for (Sig sig : toAlloy.getAllSigs()) {
      all.put(sig.label, sig);
    }

    return all;
  }

  // public Map<String, Sig> getSigMap() {
  // return toAlloy.getSigMap();
  // }

  public OBMXMI2Alloy() throws FileNotFoundException, UMLModelErrorException {
    toAlloy = new ToAlloy();
  }

  public boolean createAlloyModel(File xmiFileInput, String className)
      throws FileNotFoundException, UMLModelErrorException {
    if (!xmiFileInput.exists() || !xmiFileInput.canRead()) {
      System.err.println("File " + xmiFileInput.getAbsolutePath() + " does not exist or read.");
      return false;
    } else {
      this.loadOBMAndCreateAlloy(xmiFileInput, className);
      return true;
    }
  }

  public void createAlloyFile(File xmiFile, String className)
      throws FileNotFoundException, UMLModelErrorException {
    if (createAlloyModel(xmiFile, className)) {
      File outputFile = new File("generated-"
          + className.replaceAll("::", "_") /* alloyModule.getModuleName() */ + ".als");
      String outputFileName = toAlloy.createAlloyFile(outputFile);
      System.out.println(outputFileName + " is created");
    }
  }

  public void createAlloyFile(File xmiFile, String className, File outputFile)
      throws FileNotFoundException, UMLModelErrorException {
    if (createAlloyModel(xmiFile, className)) {
      String outputFileName = toAlloy.createAlloyFile(outputFile);
      System.out.println(outputFileName + " is created");
    }
  }

  private Map<org.eclipse.uml2.uml.NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> addClasses(
      org.eclipse.uml2.uml.NamedElement umlElement, SysMLUtil sysMLUtil,
      Map<org.eclipse.uml2.uml.NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass) {

    if (umlElement instanceof org.eclipse.uml2.uml.Class) {
      org.eclipse.uml2.uml.Class umlClass = (org.eclipse.uml2.uml.Class) umlElement;
      Set<org.eclipse.uml2.uml.Property> atts = sysMLUtil.getAllCorrectedAttributes(umlClass);
      // find property having the same type
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByTheirType = new HashMap<>();
      for (Property p : atts) {
        org.eclipse.uml2.uml.Type eType = p.getType();
        List<Property> ps = null;
        if ((ps = propertiesByTheirType.get(eType)) == null) {
          ps = new ArrayList<>();
          System.out.println("property: " + p.getName() + " " + eType);
          propertiesByTheirType.put(eType, ps);
        }
        propertiesByTheirType.get(eType).add(p);

        if (eType instanceof org.eclipse.uml2.uml.Class
            || eType instanceof org.eclipse.uml2.uml.PrimitiveType) {
          toAlloy.addAlloySig(eType.getName(), "parent not used for now");
          addClasses((org.eclipse.uml2.uml.NamedElement) eType, sysMLUtil, propertiesByClass);
        }
        propertiesByClass.put(umlClass, propertiesByTheirType);
      }
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


    sysmladapter = new SysMLAdapter(xmiFile, null);
    org.eclipse.uml2.uml.NamedElement mainClass = EMFUtil.getNamedElement(r, _className);

    Graph2AlloyExpr ge = new Graph2AlloyExpr();
    // key = class, value = (key = type, value =property>)
    Map<org.eclipse.uml2.uml.NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass =
        new HashMap<>();

    if (mainClass == null)
      return;
    else if (mainClass instanceof org.eclipse.uml2.uml.Class) {
      Sig mainSig = toAlloy.addAlloySig(mainClass.getName(), "parent not used for now", true);
      propertiesByClass = addClasses(mainClass, sysMLUtil, propertiesByClass);
    }

    List<String> noStepSigs = new ArrayList<>();
    Set<String> inputs = new HashSet<>(); // collect field type Sig having a transfer connector
                                          // with transferTarget "Customer"
    Set<String> outputs = new HashSet<>(); // collect field type Sig having a transfer connector
                                           // with transferSource "Supplier"
    for (org.eclipse.uml2.uml.NamedElement ne : propertiesByClass.keySet()) {
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      for (org.eclipse.uml2.uml.Type propertyType : propertiesByType.keySet()) {
        List<Property> ps = propertiesByType.get(propertyType);
        List<String> fsa = new ArrayList<>();
        int i = 0;
        for (Property p : ps) {
          if (p.getName() != null) { // TODO: add to validation?
            fsa.add(p.getName());
            ge.addNode(p.getName());
            i++;
          }
        }
        Collections.sort(fsa);

        Sig.Field[] fields = toAlloy.addDisjAlloyFields(fsa, propertyType.getName(), ne.getName());
        if (fields != null) {
          i = 0;
          for (Property p : ps) {
            if (p.getLower() == 1 && p.getUpper() == 1)
              toAlloy.addCardinalityEqualConstraintToField(fields[i], ne.getName(), 1);
            else if (p.getUpper() == -1 && p.getLower() >= 1) {
              toAlloy.addCardinalityGreaterThanEqualConstraintToField(fields[i], ne.getName(),
                  p.getLower());
            }
            i++;
          }
        }

      }
      Sig thisSig = toAlloy.getSigMap().get(ne.getName());
      // }//end of class


      if (ne instanceof org.eclipse.uml2.uml.Class) {
        org.eclipse.uml2.uml.Class c = (org.eclipse.uml2.uml.Class) ne;
        Set<Constraint> constraints = sysMLUtil.getAllRules(c);
        Set<org.eclipse.uml2.uml.Connector> connectors = sysMLUtil.getAllConnectors(c);

        for (org.eclipse.uml2.uml.Connector cn : connectors) {

          edu.umd.omgutil.uml.Element omgE = sysmladapter.mapObject(cn);
          if (omgE instanceof edu.umd.omgutil.uml.Connector) {
            edu.umd.omgutil.uml.Connector omgConnector = (edu.umd.omgutil.uml.Connector) omgE;

            edu.umd.omgutil.uml.Type owner = omgConnector.getFeaturingType();
            String source = null;
            String target = null;
            ConnectorEnd sourceCN = null;
            ConnectorEnd targetCN = null;
            String sourceTypeName = null; // used in Transfer
            String targetTypeName = null;// used in Transfer
            for (ConnectorEnd ce : ((Connector) cn).getEnds()) {



              if (ce.getDefiningEnd() == null) {
                System.out.println(ce.getRole().getLabel()); // suppliedProduct, receivedProduct
                Element roleOwner = ce.getRole().getOwner();
                if (roleOwner instanceof org.eclipse.uml2.uml.Class) {
                  System.out.println(((org.eclipse.uml2.uml.Class) roleOwner).getName());// Supplier,
                                                                                         // Customer
                }
                continue;
              }
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
              } else if (definingEndName.equals("transferSource")) {
                source = endsFeatureNames.get(0);
                System.out.println("s: " + source + ce.getRole().getType().getName());
                sourceTypeName = ce.getRole().getType().getName();
                noStepSigs.add(sourceTypeName);

                List<Property> ps = sysMLUtil.getPropertyPath(ce);
                System.out.println(ps);

              } else if (definingEndName.equals("transferTarget")) {
                target = endsFeatureNames.get(0);
                System.out.println("t: " + target + " " + ce.getRole().getType().getName());
                targetTypeName = ce.getRole().getType().getName();
                noStepSigs.add(targetTypeName);
              }



              if (sourceCN != null && targetCN != null) {
                Edge edge = ge.addEdge(source + target, source, target);
                if (isOneof(constraints, sourceCN, targetCN)) {
                  edge.setAttribute("oneof", true);
                }
              } else if (source != null && target != null) {
                handleTransferConnector(cn, thisSig, source, target, sourceTypeName,
                    targetTypeName);

                inputs.add(targetTypeName);
                outputs.add(sourceTypeName);
              }
            } // end of connectorEnd

          } // end of Connector

        } // org.eclipse.uml2.uml.Connector
        handleHappensBefore(ge, thisSig);

      } // end of class
    }
    toAlloy.addSteps(noStepSigs);
    handleNoInputsOutputs(inputs, outputs);

    // ge.display();

  }

  /**
   * this produces like toAlloy.noInputs("Supplier"); toAlloy.noOutputs("Customer");
   * 
   * @param inputs
   * @param outputs
   */
  private void handleNoInputsOutputs(Set<String> inputs, Set<String> outputs) {
    Set<String> insAndOuts =
        Stream.of(inputs, outputs).flatMap(x -> x.stream()).collect(Collectors.toSet());
    for (String s : insAndOuts) {
      if (!inputs.contains(s))
        toAlloy.noInputs(s);
      if (!outputs.contains(s))
        toAlloy.noOutputs(s);
    }
  }


  private void handleTransferConnector(org.eclipse.uml2.uml.Connector cn, Sig sig, String source,
      String target, String sourceTypeName, String targetTypeName) {

    String[] stTagNames = {"sourceOutputProperty", "targetInputProperty"};// , "itemType"}; this
                                                                          // property value is class
                                                                          // not property
    Map<String, List<Property>> stTagValues =
        getStreotypePropertyValues(cn, "Model::OBM::ItemFlow", stTagNames);
    if (stTagValues != null) {
      List<Property> sos = stTagValues.get(stTagNames[0]);
      List<Property> tis = stTagValues.get(stTagNames[1]); // name is "receivedProduct"

      for (Property p : sos) {
        String owner = ((org.eclipse.uml2.uml.Class) p.getOwner()).getName();
        toAlloy.addOutputs(owner, /* "Supplier" */ p.getName() /* "suppliedProduct" */);
        break; // assumption is having only one
      }
      for (Property p : tis) {
        String owner = ((org.eclipse.uml2.uml.Class) p.getOwner()).getName();
        toAlloy.addInputs(owner, /* "Customer" */p.getName() /* "receivedProduct" */);
        break; // assumption is having only one
      }
    }

    Association type = cn.getType();
    if (type.getName().compareTo("Transfer") == 0) {
      Sig.Field transferField = toAlloy.addAlloyTransferField(
          type.getName().toLowerCase() + firstCharUpper(source) + firstCharUpper(target), sig);
      toAlloy.createFnForTransferAndAddToOverallFact(sig, sig.domain(transferField), sourceTypeName,
          targetTypeName);
    }



  }

  /**
   * 
   * @param element
   * @param streotypeName
   * @param tagNames
   * @return null is not Stereotype with streotypeName applied to the element
   */
  private Map<String, List<Property>> getStreotypePropertyValues(Element element,
      String streotypeName, String[] tagNames) {

    Map<String, List<Property>> propertysByTagNames = new HashMap<>();
    Stereotype st = null;
    if ((st = element.getAppliedStereotype(streotypeName)) != null) {

      for (String propertyName : tagNames) {
        List<Property> results = new ArrayList<>();
        List<Object> properties = (List<Object>) (element.getValue(st, propertyName));
        for (Object property : properties) {
          if (property instanceof Property) {
            results.add((Property) property);
          } else {
            System.out.println(propertyName + " is not Property but " + property);
          }
        }
        propertysByTagNames.put(propertyName, results);
      }
      return propertysByTagNames;
    }
    return null;
  }

  /**
   * 
   * @param element (ie., Connector)
   * @param streotypeName (ie,. Model::OBM::ItemFlow)
   * @param propertyName (ie., sourceOutputProperty)
   * @return null if no Stereotype is applied to element or List<Property>
   */
  private List<Property> getStreotypePropertyValues(Element element, String streotypeName,
      String propertyName) {
    List<Property> results = new ArrayList<>();
    Stereotype st = null;
    if ((st = element.getAppliedStereotype(streotypeName)) != null) {
      List<Object> properties = (List<Object>) (element.getValue(st, propertyName));
      for (Object property : properties)
        results.add((Property) property);
      return results;
    }
    return null;
  }


  private String firstCharUpper(String o) {
    return o.substring(0, 1).toUpperCase() + o.substring(1).toLowerCase();
  }



  private void handleHappensBefore(Graph2AlloyExpr ge, Sig thisSig) {
    System.out.println("HB Function: ");
    Map<IObject, IObject> happensBeforeFnInfo = ge.getHappensBeforeFunction(); // before, after
    Graph2AlloyExpr.print(happensBeforeFnInfo);
    for (IObject before : happensBeforeFnInfo.keySet()) {
      before.sort();
      List<Expr> lbefore = toExprs(before, thisSig);
      IObject after = happensBeforeFnInfo.get(before);
      after.sort();
      List<Expr> lafter = toExprs(after, thisSig);

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
      before.sort();
      List<Expr> lbefore = toExprs(before, thisSig);
      IObject after = happensBeforeInvFnInfo.get(before);
      after.sort();
      List<Expr> lafter = toExprs(after, thisSig);

      for (int i = 0; i < lbefore.size(); i++) {
        Expr beforeExpr = lbefore.get(i);
        for (int j = 0; j < lafter.size(); j++) {
          Expr afterExpr = lafter.get(j);

          // toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(mainSig,
          // beforeExpr,
          // afterExpr);
          toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(thisSig, beforeExpr,
              afterExpr);
        }
      }
    } // end iterator
  }



  private boolean isOneOf(Constraint c) {
    ValueSpecification vs = c.getSpecification();
    if (vs instanceof OpaqueExpressionImpl) {
      edu.umd.omgutil.uml.OpaqueExpression omgE = (OpaqueExpression) sysmladapter.mapObject(vs);
      if (omgE.getBodies().contains("OneOf"))
        return true;
    }
    return false;
  }

  // if any of connector end is startCN or endCN then specify connector is one of
  private boolean isOneof(Set<Constraint> constraints, ConnectorEnd startCN, ConnectorEnd endCN) {
    for (Constraint ct : constraints) {
      if (isOneOf(ct)) {
        EList<Element> constraintedElements = ct.getConstrainedElements();
        if (constraintedElements.size() == 2) {
          if (constraintedElements.get(0) == startCN || constraintedElements.get(0) == endCN
              || constraintedElements.get(1) == startCN || constraintedElements.get(1) == endCN)
            return true;
        }
      }
    }
    return false;


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


