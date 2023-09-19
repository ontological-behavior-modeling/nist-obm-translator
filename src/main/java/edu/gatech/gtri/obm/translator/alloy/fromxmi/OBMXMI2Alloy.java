package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.internal.impl.OpaqueExpressionImpl;
import edu.gatech.gtri.obm.translator.alloy.Helper;
import edu.gatech.gtri.obm.translator.alloy.MDUtils;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.EMFUtil;
import edu.umd.omgutil.UMLModelErrorException;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;
import edu.umd.omgutil.uml.OpaqueExpression;



public class OBMXMI2Alloy {

  ToAlloy toAlloy;
  SysMLAdapter sysmladapter;

  private enum CONNECTOR_TYPE {
    HAPPENS_BEFORE, HAPPENS_DURING, TRANSFER;
  }

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
    sysmladapter = new SysMLAdapter(xmiFile, null);

    try {
      while (!r.isLoaded()) {
        System.out.println("not loaded yet wait 1 sec...");
        Thread.sleep(1000);
      }

    } catch (Exception e) {
    }

    org.eclipse.uml2.uml.NamedElement mainClass = EMFUtil.getNamedElement(r, _className);

    // Graph2AlloyExpr graph2alloyExpr = new Graph2AlloyExpr();
    // key = class, value = (key = type, value =property>)
    Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass =
        new HashMap<>();
    // List<Map.Entry<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>>> list =
    // new LinkedList<>(propertiesByClass.entrySet());

    // // sort the linked list using Collections.sort()
    // Collections.sort(list,
    // new Comparator<Map.Entry<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>>>() {
    // @Override
    // public int compare(
    // Map.Entry<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> m1,
    // Map.Entry<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> m2) {
    // toAlloy.getSig(m1.getKey().getName());
    //
    // return m1.getValue().compareTo(m2.getValue());
    // }
    // });
    // list.forEach(System.out::println);

    if (mainClass == null)
      return;
    else if (mainClass instanceof org.eclipse.uml2.uml.Class) {
      // EList<Classifier> parents = ((org.eclipse.uml2.uml.Class) mainClass).getGenerals();
      // // Only one parent is allowed in Alloy
      // String parentName = parents.get(0).getName();
      // if (parents.size() > 0) {
      // System.err.println("Only one parent is allowed. One parent \"" + parentName
      // + "\" is included as sig \"" + mainClass.getName() + "\"'s parent");
      // }
      //
      // if (parentName.equals("BehaviorOccurrence")) {
      // // create main sig
      // toAlloy.createAlloySig(mainClass.getName(), null, true);
      // } else {

      List<Class> classInHierarchy = MDUtils.getClassInHierarchy((Class) mainClass);
      PrimSig parentSig = null;
      for (Class aClass : classInHierarchy) {
        boolean isMainSig = (aClass == mainClass) ? true : false;
        if (parentSig == null) { // create 1st parentSig
          parentSig = toAlloy.createAlloySig(aClass.getName(), null, isMainSig);
          propertiesByClass = addClasses(aClass, sysMLUtil, propertiesByClass);
        } else {
          PrimSig sig = toAlloy.createAlloySig(aClass.getName(), parentSig, isMainSig);
          propertiesByClass.putAll(addClasses(aClass, sysMLUtil, propertiesByClass));
          parentSig = sig;
        }

      }
    }



    // create process class in hiearchy order because to child class may not need creating
    // attributes if they are redefined
    // List<PrimSig> sigsInhiearchyOrder = toAlloy.getHierarchySigs(); // this contains sigs not in
    // for (PrimSig sig : sigsInhiearchyOrder) {
    // Optional<NamedElement> one = propertiesByClass.keySet().stream()
    // .filter(n -> n.getName().equals(sig.label)).findFirst();
    // if (one.isPresent()) {
    // NamedElement ne = one.get();
    // Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
    // processSig(ne, sig, propertiesByType, noStepSigs, inputs, outputs, sysMLUtil);
    // }
    // }


    Set<NamedElement> allClasses = propertiesByClass.keySet();
    List<Class> classInHierarchy = MDUtils.getClassInHierarchy((Class) mainClass);
    for (Class ne : classInHierarchy) {
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      processSig(ne, propertiesByType, sysMLUtil);
      allClasses.remove(ne);
    }
    for (NamedElement ne : allClasses) {
      System.out.println(ne.getName());
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      processSig((Class) ne, propertiesByType, sysMLUtil);
    }

    List<String> noStepSigs = new ArrayList<>();
    Set<String> inputs = new HashSet<>(); // collect field type Sig having a transfer connector
                                          // with transferTarget "Customer"
    Set<String> outputs = new HashSet<>(); // collect field type Sig having a transfer connector


    allClasses = propertiesByClass.keySet();// Suppler, Customer
    for (Class ne : classInHierarchy) {// ParticipantTransfer
      processConnector((Class) ne, noStepSigs, inputs, outputs, sysMLUtil);
      allClasses.remove(ne);
    }
    for (NamedElement ne : allClasses) {
      processConnector((Class) ne, noStepSigs, inputs, outputs, sysMLUtil);
    }

    toAlloy.addSteps(noStepSigs);

    handleNoInputsOutputs(inputs, outputs);

  }

  private void processSig(Class ne, /* PrimSig sigOfNamedElement, */
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType, SysMLUtil sysMLUtil) {

    PrimSig sigOfNamedElement = toAlloy.getSig(ne.getName());
    System.out.println("Processing: " + ne.getName() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

    if (propertiesByType != null) {
      for (org.eclipse.uml2.uml.Type propertyType : propertiesByType.keySet()) {
        // find property by type (ie., propetyType = Order, List<Property> = [order]);
        List<Property> ps = propertiesByType.get(propertyType);

        // sort property in alphabetical order, also remove redefined properties from the sorted
        // list.
        List<String> nonRedefinedPropertyInAlphabeticalOrder = new ArrayList<>();
        int i = 0;
        for (Property p : ps) {
          if (p.getName() != null) { // Since MD allow having no name.
            if (p.getRedefinedProperties().size() == 0) {
              nonRedefinedPropertyInAlphabeticalOrder.add(p.getName());
              // graph2alloyExpr.addNode(p.getName());
              i++;
            }
          } else {
            System.err.println(p.getQualifiedName()
                + "has no name, so ignored.  Please defined the name to be included");
          }
        }
        Collections.sort(nonRedefinedPropertyInAlphabeticalOrder);

        if (nonRedefinedPropertyInAlphabeticalOrder.size() > 0) {
          Sig.Field[] fields = toAlloy.addDisjAlloyFields(nonRedefinedPropertyInAlphabeticalOrder,
              propertyType.getName(), sigOfNamedElement);
          // server, Serve, SinglFooeService
          if (fields != null) {
            i = 0;
            for (Property p : ps) {
              if (p.getLower() == p.getUpper())
                toAlloy.addCardinalityEqualConstraintToField(fields[i], sigOfNamedElement,
                    p.getLower());
              else if (p.getUpper() == -1 && p.getLower() >= 1) {
                toAlloy.addCardinalityGreaterThanEqualConstraintToField(fields[i],
                    sigOfNamedElement, p.getLower());
              }
              i++;
            }
          }
        } else {
          for (Property p : ps) {
            if (p.getLower() == p.getUpper())
              toAlloy.addCardinalityEqualConstraintToField(p.getName(), sigOfNamedElement,
                  p.getLower());
            else if (p.getUpper() == -1 && p.getLower() >= 1) {
              toAlloy.addCardinalityGreaterThanEqualConstraintToField(p.getName(),
                  sigOfNamedElement, p.getLower());
            }
          }
        }

        ///
      }
    } // end processing property

  }

  private void processConnector(Class ne, /* PrimSig sigOfNamedElement, */
      List<String> noStepSigs, Set<String> inputs, Set<String> outputs, SysMLUtil sysMLUtil) {

    System.out.println("Process Connector: " + ne.getName());
    PrimSig sigOfNamedElement = toAlloy.getSig(ne.getName());

    Set<Constraint> constraints = sysMLUtil.getAllRules(ne);
    Set<EList<Element>> oneOfSets = getOneOfRules(constraints);
    Set<org.eclipse.uml2.uml.Connector> connectors = sysMLUtil.getAllConnectors(ne);
    System.out.println("connectors.size = " + connectors.size());

    // process connectors with oneof first
    Set<Connector> processedConnectors = new HashSet<>();
    for (EList<Element> oneOfSet : oneOfSets) {
      handleOneOfConnectors(sigOfNamedElement, connectors, oneOfSet, processedConnectors);
    }

    // process remaining of connectors
    for (org.eclipse.uml2.uml.Connector cn : connectors) {
      if (processedConnectors.contains(cn))
        continue; // oneof connectors so not need to process
      if (ne.getInheritedMembers().contains(cn))
        continue;// ignore inherited


      CONNECTOR_TYPE connector_type = null;
      edu.umd.omgutil.uml.Element omgE = sysmladapter.mapObject(cn);
      if (omgE instanceof edu.umd.omgutil.uml.Connector) {
        edu.umd.omgutil.uml.Connector omgConnector = (edu.umd.omgutil.uml.Connector) omgE;

        edu.umd.omgutil.uml.Type owner = omgConnector.getFeaturingType();
        String source = null;
        String target = null;
        // ConnectorEnd sourceCN = null;
        // ConnectorEnd targetCN = null;

        String sourceTypeName = null; // used in Transfer
        String targetTypeName = null;// used in Transfer
        for (ConnectorEnd ce : ((Connector) cn).getEnds()) {

          if (ce.getRole() != null) {
            System.out.println(ce.getRole());
          }

          if (ce.getDefiningEnd() == null) {
            System.out.println(ce.getRole().getLabel()); // suppliedProduct, receivedProduct
            Element roleOwner = ce.getRole().getOwner();
            if (roleOwner instanceof org.eclipse.uml2.uml.Class) {
              System.out.println(((org.eclipse.uml2.uml.Class) roleOwner).getName());// Supplier,
              // Customer
            }
            continue;
          }
          System.out.println(ce.getDefiningEnd().getName());
          String definingEndName = ce.getDefiningEnd().getName();
          edu.umd.omgutil.uml.ConnectorEnd end =
              (edu.umd.omgutil.uml.ConnectorEnd) sysmladapter.mapObject(ce);
          List<String> endsFeatureNames = end.getCorrectedFeaturePath(owner).stream()
              .map(f -> f.getName()).collect(Collectors.toList());

          if (definingEndName.equals("happensBefore-1")) {
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
            System.out.println("s: " + source + ce.getRole().getType().getName());
            sourceTypeName = ce.getRole().getType().getName();
            noStepSigs.add(sourceTypeName);

            List<Property> ps = sysMLUtil.getPropertyPath(ce);
            System.out.println(ps);

          } else if (definingEndName.equals("transferTarget")) {
            connector_type = CONNECTOR_TYPE.TRANSFER;
            target = endsFeatureNames.get(0);
            System.out.println("t: " + target + " " + ce.getRole().getType().getName());
            targetTypeName = ce.getRole().getType().getName();
            noStepSigs.add(targetTypeName);
          }

          if (source == null || target == null)
            continue;
          if (connector_type == CONNECTOR_TYPE.HAPPENS_BEFORE) {
            handleHappensBefore(sigOfNamedElement, source, target);
          } else if (connector_type == CONNECTOR_TYPE.HAPPENS_DURING)
            handleHappensDuring(sigOfNamedElement, source, target);

          // using graph to find out what is one of
          // if (sourceCN != null && targetCN != null) {
          // Edge edge = ge.addEdge(source + target, source, target);
          // if (isOneof(constraints, sourceCN, targetCN)) {
          // edge.setAttribute("oneof", true);
          // }
          else if (connector_type == CONNECTOR_TYPE.TRANSFER) {
            // else if (source != null && target != null) {
            handleTransferConnector(cn, sigOfNamedElement, source, target, sourceTypeName,
                targetTypeName);

            inputs.add(targetTypeName);
            outputs.add(sourceTypeName);
          }

        } // end of connectorEnd

      } // end of Connector

    } // org.eclipse.uml2.uml.Connector
    // handleHappensBefore(ge, thisSig); //getting happens before using graph


  }



  public Expr getOverallFacts() {
    return toAlloy.getOverallFacts();
  }

  public Map<String, Sig> getAllReachableUserDefinedSigs() {
    Map<String, PrimSig> pall = toAlloy.getSigMap();

    // convert pall value PrimSig to Sig
    Map<String, Sig> all = new HashMap<>();
    for (String key : pall.keySet())
      all.put(key, pall.get(key));

    for (Sig sig : toAlloy.getAllSigs()) {
      all.put(sig.label, sig);
    }
    return all;
  }

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

  private Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> addClasses(
      NamedElement umlElement, SysMLUtil sysMLUtil,
      Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass) {

    if (umlElement instanceof org.eclipse.uml2.uml.Class) {
      org.eclipse.uml2.uml.Class umlClass = (org.eclipse.uml2.uml.Class) umlElement;

      Set<org.eclipse.uml2.uml.Property> atts = sysMLUtil.getOwnedAttributes(umlClass);
      // find property having the same type
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByTheirType = new HashMap<>();
      for (Property p : atts) {

        System.out.println(p.getLabel());

        org.eclipse.uml2.uml.Type eType = p.getType();
        List<Property> ps = null;
        if ((ps = propertiesByTheirType.get(eType)) == null) {
          ps = new ArrayList<>();
          propertiesByTheirType.put(eType, ps);
        }
        propertiesByTheirType.get(eType).add(p);

        if (eType instanceof org.eclipse.uml2.uml.Class
            || eType instanceof org.eclipse.uml2.uml.PrimitiveType) {

          EList<Classifier> parents = null;
          if (eType instanceof org.eclipse.uml2.uml.Class) {
            parents = ((org.eclipse.uml2.uml.Class) eType).getGenerals();
          }
          // PaticipantTransfer.Product has no parent
          toAlloy.createAlloySig(eType.getName(),
              parents == null || parents.size() == 0 ? null : parents.get(0).getName());
          addClasses((org.eclipse.uml2.uml.NamedElement) eType, sysMLUtil, propertiesByClass);
        }
        propertiesByClass.put(umlClass, propertiesByTheirType);
      }
    }
    return propertiesByClass;
  }


  /**
   * Find property names for connector ends. One connector has two connector end. return string with
   * index = 0
   * 
   * @param cn
   * @param ce
   * @return
   */

  private String[] getEndPropertyNames(Connector cn, ConnectorEnd ce) {
    String[] names = new String[2];
    edu.umd.omgutil.uml.Connector omgE = (edu.umd.omgutil.uml.Connector) sysmladapter.mapObject(cn);
    edu.umd.omgutil.uml.Type owner = omgE.getFeaturingType();
    names[0] = getConnecterEndPropertyName(ce, owner);

    for (ConnectorEnd ce1 : cn.getEnds())
      if (ce1 != ce) {
        names[1] = getConnecterEndPropertyName(ce1, owner);
        break;
      }
    return names;
  }

  private String getConnecterEndPropertyName(ConnectorEnd ce, edu.umd.omgutil.uml.Type owner) {
    edu.umd.omgutil.uml.ConnectorEnd end =
        (edu.umd.omgutil.uml.ConnectorEnd) sysmladapter.mapObject(ce);
    List<String> endsFeatureNames = end.getCorrectedFeaturePath(owner).stream()
        .map(f -> f.getName()).collect(Collectors.toList());
    String name = endsFeatureNames.get(0);
    return name;

  }

  private void handleOneOfConnectors(Sig ownerSig, Set<Connector> connectors,
      List<Element> oneOfSet, Set<Connector> processedConnectors) {

    List<String> sourceNames = new ArrayList<>(); // for each connector
    List<String> targetNames = new ArrayList<>();
    boolean isSourceSideOneOf = false;
    for (org.eclipse.uml2.uml.Connector cn : connectors) {
      for (ConnectorEnd ce : cn.getEnds()) {

        Optional<Element> found = oneOfSet.stream().filter(e -> e == ce).findFirst();
        if (!found.isEmpty()) {
          processedConnectors.add(cn);
          String[] names = getEndPropertyNames(cn, ce);

          String definingEndName = ce.getDefiningEnd().getName();
          if (definingEndName.equals("happensBefore-1")) {
            isSourceSideOneOf = true; // source-sides have oneof
            sourceNames.add(names[0]);
            targetNames.add(names[1]);
          } else if (definingEndName.equals("happensBefore")) {
            isSourceSideOneOf = false; // target-side have oneof
            targetNames.add(names[0]);
            sourceNames.add(names[1]);
          }
        }
      }

    }

    // sort so not x.p2 + x.p1 but be x.p1 + x.p2
    Collections.sort(sourceNames);
    Collections.sort(targetNames);

    Expr beforeExpr = null;
    Expr afterExpr = null;
    if (isSourceSideOneOf) { // sourceSide need to be combined
      for (String sourceName : sourceNames) {
        beforeExpr = beforeExpr == null ? /* ownerSig.domain( */toAlloy.getField(sourceName)// )
            : beforeExpr.plus(/* ownerSig.domain( */toAlloy.getField(sourceName))/* ) */;
      }
      afterExpr = /* ownerSig.domain( */toAlloy.getField(targetNames.get(0))/* ) */;
    } else {
      afterExpr = null;
      for (String targetName : targetNames) {
        afterExpr = afterExpr == null ? /* ownerSig.domain( */toAlloy.getField(targetName)// )
            : afterExpr.plus(/* ownerSig.domain( */toAlloy.getField(targetName))/* ) */;
      }
      beforeExpr = /* ownerSig.domain( */toAlloy.getField(sourceNames.get(0))/* ) */;
    }
    // toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr,
    // afterExpr);
    // toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr,
    // afterExpr);

    toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr,
        afterExpr);
  }


  // sig BuffetService
  private void handleHappensBefore(PrimSig sig, String source, String target) {

    System.out.println("looking for source" + source + " in " + sig.label);
    System.out.println("looking for target" + target + " in " + sig.label);

    Field sourceField = Helper.getFieldFromSig(source, sig); // FoodService <: prepare
    Field targetField = Helper.getFieldFromSig(target, sig);

    if (sourceField != null && targetField != null) {
      // Expr sexpr = sig.domain(sourceField);
      // Expr texpr = sig.domain(targetField);
      // toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(sig, sexpr, texpr);
      toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(sig, sourceField,
          targetField);
    } else
      System.err.println("source or target for HappensBefore not in " + sig.label);
  }

  private void handleHappensDuring(PrimSig sig, String source, String target) {

    System.out.println("looking for source" + source + " in " + sig.label);
    System.out.println("looking for target" + target + " in " + sig.label);

    Field sourceField = Helper.getFieldFromSig(source, sig);
    Field targetField = Helper.getFieldFromSig(target, sig);

    if (sourceField != null && targetField != null) {
      // Expr sexpr = sig.domain(sourceField);
      // Expr texpr = sig.domain(targetField);
      // toAlloy.createBijectionFilteredHappensDuringAndAddToOverallFact(sig, sexpr, texpr);
      toAlloy.createBijectionFilteredHappensDuringAndAddToOverallFact(sig, sourceField,
          targetField);
    } else
      System.err.println("source or target for handleHappensDuring not in " + sig.label);

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

      System.out.println("sourceOutputProperty: " + sos.size());
      System.out.println("targetInputProperty: " + tis.size());
      for (Property p : sos) {
        String owner = ((org.eclipse.uml2.uml.Class) p.getOwner()).getName();
        toAlloy.addOutputs(owner, /* "Supplier" */ p.getName() /* "suppliedProduct" */);
        System.out.println("adding outputs to " + p.getName());
        break; // assumption is having only one
      }
      for (Property p : tis) {
        String owner = ((org.eclipse.uml2.uml.Class) p.getOwner()).getName();
        toAlloy.addInputs(owner, /* "Customer" */p.getName() /* "receivedProduct" */);
        System.out.println("adding input to " + p.getName());
        break; // assumption is having only one
      }
    }

    Association type = cn.getType();
    if (type.getName().compareTo("Transfer") == 0) {
      Sig.Field transferField = toAlloy.addAlloyTransferField(
          type.getName().toLowerCase() + firstCharUpper(source) + firstCharUpper(target), sig);
      toAlloy.createFnForTransferAndAddToOverallFact(sig, /* sig.domain( */transferField/* ) */,
          sourceTypeName, targetTypeName);

    } else if (type.getName().compareTo("TransferBefore") == 0) {

      // fact {all x: ParameterBehavior | bijectionFiltered[sources, x.transferbeforeAB, x.a]}
      // fact {all x: ParameterBehavior | bijectionFiltered[targets, x.transferbeforeAB, x.b]}
      // fact {all x: ParameterBehavior | subsettingItemRuleForSources[x.transferbeforeAB]}
      // fact {all x: ParameterBehavior | subsettingItemRuleForTargets[x.transferbeforeAB]}

      // fact {all x: ParameterBehavior | bijectionFiltered[sources, x.transferbeforeBC, x.b]}
      // fact {all x: ParameterBehavior | bijectionFiltered[targets, x.transferbeforeBC, x.c]}
      // fact {all x: ParameterBehavior | subsettingItemRuleForSources[x.transferbeforeBC]}
      // fact {all x: ParameterBehavior | subsettingItemRuleForTargets[x.transferbeforeBC]}

      // wip - this is when source has no incoming or target has no outgoing
      Sig.Field transferField = toAlloy.addAlloyTransferBeforeField(
          type.getName().toLowerCase() + firstCharUpper(source) + firstCharUpper(target), sig);
      toAlloy.createFnForTransferBeforeAndAddToOverallFact(sig,
          /* sig.domain( */transferField/* ) */, sourceTypeName, targetTypeName);
      // Sig.Field start = toAlloy.addAlloyTransferField(
      // type.getName().toLowerCase() + firstCharUpper(sig.label) + firstCharUpper(source), sig);
      // Sig.Field middle = toAlloy.addAlloyTransferField(
      // type.getName().toLowerCase() + firstCharUpper(source) + firstCharUpper(target), sig);
      // Sig.Field end = toAlloy.addAlloyTransferField(
      // type.getName().toLowerCase() + firstCharUpper(sig.label) + firstCharUpper(target), sig);
      // toAlloy.createFnForTransferBeforeAndAddToOverallFact(sig, sig.domain(start),
      // sig.domain(middle), sig.domain(end), sourceTypeName, targetTypeName);
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
   * Get two rules (ConnectorEnds) of each oneof constraint and return as set.
   * 
   * @param cs a set of Constraints
   * @return set of oneof constraint's two rules (ConnectorEnd)
   */
  private Set<EList<Element>> getOneOfRules(Set<Constraint> cs) {
    Set<EList<Element>> oneOfSet = new HashSet<>();
    for (Constraint c : cs) {
      ValueSpecification vs = c.getSpecification();
      if (vs instanceof OpaqueExpressionImpl) {
        edu.umd.omgutil.uml.OpaqueExpression omgE = (OpaqueExpression) sysmladapter.mapObject(vs);
        if (omgE.getBodies().contains("OneOf")) {
          EList<Element> es = c.getConstrainedElements(); // list of connectorEnds
          oneOfSet.add(es);
        }
      }
    }
    return oneOfSet;
  }



  private static String firstCharUpper(String o) {
    return o.substring(0, 1).toUpperCase() + o.substring(1).toLowerCase();
  }


  /**
   * 
   * @param element (ie., Connector)
   * @param streotypeName (ie,. Model::OBM::ItemFlow)
   * @param propertyName (ie., sourceOutputProperty)
   * @return null if no Stereotype is applied to element or List<Property>
   */
  // private List<Property> getStreotypePropertyValues(Element element, String streotypeName,
  // String propertyName) {
  // List<Property> results = new ArrayList<>();
  // Stereotype st = null;
  // if ((st = element.getAppliedStereotype(streotypeName)) != null) {
  // List<Object> properties = (List<Object>) (element.getValue(st, propertyName));
  // for (Object property : properties)
  // results.add((Property) property);
  // return results;
  // }
  // return null;
  // }

  // commented out getting happens before using graph

  // private boolean isOneOf(Constraint c) {
  // ValueSpecification vs = c.getSpecification();
  // if (vs instanceof OpaqueExpressionImpl) {
  // edu.umd.omgutil.uml.OpaqueExpression omgE = (OpaqueExpression) sysmladapter.mapObject(vs);
  // if (omgE.getBodies().contains("OneOf"))
  // return true;
  // }
  // return false;
  // }

  // if any of connector end is startCN or endCN then specify connector is one of
  // private boolean isOneof(Set<Constraint> constraints, ConnectorEnd startCN, ConnectorEnd endCN)
  // {
  // for (Constraint ct : constraints) {
  // if (isOneOf(ct)) {
  // EList<Element> constraintedElements = ct.getConstrainedElements();
  // if (constraintedElements.size() == 2) {
  // if (constraintedElements.get(0) == startCN || constraintedElements.get(0) == endCN
  // || constraintedElements.get(1) == startCN || constraintedElements.get(1) == endCN)
  // return true;
  // }
  // }
  // }
  // return false;
  // }

  /*
   * private void handleHappensBefore(Graph2AlloyExpr ge, Sig thisSig) {
   * System.out.println("HB Function: "); Map<IObject, IObject> happensBeforeFnInfo =
   * ge.getHappensBeforeFunction(); // before, after // Graph2AlloyExpr.print(happensBeforeFnInfo);
   * for (IObject before : happensBeforeFnInfo.keySet()) { before.sort(); List<Expr> lbefore =
   * toExprs(before, thisSig); IObject after = happensBeforeFnInfo.get(before); after.sort();
   * List<Expr> lafter = toExprs(after, thisSig);
   * 
   * for (int i = 0; i < lbefore.size(); i++) { Expr beforeExpr = lbefore.get(i); for (int j = 0; j
   * < lafter.size(); j++) { Expr afterExpr = lafter.get(j);
   * toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(thisSig, beforeExpr, afterExpr);
   * } } } // end iterator
   * 
   * System.out.println("HB Inverse Function: "); Map<IObject, IObject> happensBeforeInvFnInfo =
   * ge.getHappensBeforeInvFunction(); // before, Graph2AlloyExpr.print(happensBeforeInvFnInfo); //
   * after
   * 
   * for (IObject before : happensBeforeInvFnInfo.keySet()) { before.sort(); List<Expr> lbefore =
   * toExprs(before, thisSig); IObject after = happensBeforeInvFnInfo.get(before); after.sort();
   * List<Expr> lafter = toExprs(after, thisSig);
   * 
   * for (int i = 0; i < lbefore.size(); i++) { Expr beforeExpr = lbefore.get(i); for (int j = 0; j
   * < lafter.size(); j++) { Expr afterExpr = lafter.get(j);
   * 
   * // toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(mainSig, // beforeExpr, //
   * afterExpr); toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(thisSig,
   * beforeExpr, afterExpr); } } } // end iterator }
   * 
   * // assume if 1st list field in _aSig, all fields are also in _aSig public List<Expr>
   * toExprs(IObject _o, Sig _aSig) { List<Expr> exprs = new ArrayList<>(); if (_o instanceof
   * OListOR) { ONode onode = (ONode) ((OListOR) _o).get(0); Sig.Field f =
   * toAlloy.getField(onode.getName()); if (f.sig == _aSig) { Expr expr = _aSig.domain(f); for (int
   * i = 1; i < ((OListOR) _o).size(); i++) { onode = (ONode) ((OListOR) _o).get(i); expr =
   * expr.plus(_aSig.domain(toAlloy.getField(onode.getName()))); } exprs.add(expr); }
   * 
   * } else if (_o instanceof OListAND) { ONode onode = (ONode) ((OListAND) _o).get(0); Sig.Field f
   * = toAlloy.getField(onode.getName()); if (f.sig == _aSig) { exprs.add(_aSig.domain(f)); for (int
   * i = 1; i < ((OListAND) _o).size(); i++) { onode = (ONode) ((OListAND) _o).get(i); f =
   * toAlloy.getField(onode.getName()); exprs.add(_aSig.domain(f)); } } } else { ONode onode =
   * (ONode) _o; Sig.Field f = toAlloy.getField(onode.getName()); if (f.sig == _aSig)
   * exprs.add(_aSig.domain(f)); } return exprs; }
   */

}


