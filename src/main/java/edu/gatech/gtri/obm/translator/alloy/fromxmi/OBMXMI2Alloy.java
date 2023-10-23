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
  Set<Field> parameterFields;

  private static String STEREOTYPE_STEP = "Model::OBM::Step";
  private static String STEREOTYPE_PATICIPANT = "SysML::ParticipantProperty";
  public static String STEREOTYPE_PAREMETER = "Model::OBM::Parameter";// property
  private static String STEREOTYPE_ITEMFLOW = "Model::OBM::ItemFlow";
  private static String STEREOTYPE_BINDDINGCONNECTOR = "SysML::BindingConnector";

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

  public Set<Field> getParameterFields() {
    return this.parameterFields;
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

    parameterFields = new HashSet<>();
    ResourceSet rs = EMFUtil.createResourceSet();
    Resource r = EMFUtil.loadResourceWithDependencies(rs,
        URI.createFileURI(xmiFile.getAbsolutePath()), null);

    SysMLUtil sysMLUtil = new SysMLUtil(rs);
    sysmladapter = new SysMLAdapter(xmiFile, null);

    try {
      while (!r.isLoaded()) {
        System.out.println("not loaded yet wait 1 milli sec...");
        Thread.sleep(1000);
      }

    } catch (Exception e) {
    }

    org.eclipse.uml2.uml.NamedElement mainClass = EMFUtil.getNamedElement(r, _className);

    // Graph2AlloyExpr graph2alloyExpr = new Graph2AlloyExpr();
    // key = class, value = (key = type, value =property>)
    Map<Class, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass = new HashMap<>();
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
    else if (mainClass instanceof Class) {
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

    Map<String, Set<String>> stepPropertiesBySig = new HashMap<>();
    // properties in sigs
    Set<Class> allClassesConnectedToMainSigByFields = propertiesByClass.keySet(); // SimpleSequence,
                                                                                  // AtomicBehavior
    List<Class> classInHierarchy = MDUtils.getClassInHierarchy((Class) mainClass); // SimpleSequence

    // allclassedNames are used to create no inputs and no outputs later
    Set<String> allClassNames = Stream
        .of(allClassesConnectedToMainSigByFields.stream().map(c -> c.getName())
            .collect(Collectors.toSet()),
            classInHierarchy.stream().map(c -> c.getName()).collect(Collectors.toSet()))
        .flatMap(x -> x.stream()).collect(Collectors.toSet());

    for (Class ne : classInHierarchy) {
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      Set<String> stepProperties = processSig(ne, propertiesByType, sysMLUtil);
      stepPropertiesBySig.put(ne.getName(), stepProperties);
      // allClasses may contain a class in hierarchy
      allClassesConnectedToMainSigByFields.remove(ne);
    }
    for (NamedElement ne : allClassesConnectedToMainSigByFields) {
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType = propertiesByClass.get(ne);
      Set<String> stepProperties = processSig((Class) ne, propertiesByType, sysMLUtil);
      stepPropertiesBySig.put(ne.getName(), stepProperties);
    }

    // connectors
    Set<String> inputs = new HashSet<>(); // collect field type Sig having a transfer connector
                                          // with transferTarget "Customer"
    Set<String> outputs = new HashSet<>(); // collect field type Sig having a transfer connector

    allClassesConnectedToMainSigByFields = propertiesByClass.keySet();// Suppler, Customer
    for (Class ne : classInHierarchy) {// ParticipantTransfer/TransferProduct? Customer and Supplier
                                       // are children of Product
      processConnector((Class) ne, stepPropertiesBySig.get(ne.getName()), inputs, outputs,
          sysMLUtil);
      allClassesConnectedToMainSigByFields.remove(ne);
    }
    for (NamedElement ne : allClassesConnectedToMainSigByFields) {
      processConnector((Class) ne, stepPropertiesBySig.get(ne.getName()), inputs, outputs,
          sysMLUtil);
    }

    toAlloy.addSteps(stepPropertiesBySig);
    handleNoInputsOutputs(inputs, outputs, allClassNames);

  }

  private Set<String> processSig(Class ne, /* PrimSig sigOfNamedElement, */
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByType, SysMLUtil sysMLUtil) {

    PrimSig sigOfNamedElement = toAlloy.getSig(ne.getName());
    System.out.println("Processing: " + ne.getName() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

    Set<String> stepProperties = new HashSet<>();
    if (propertiesByType != null) {
      for (org.eclipse.uml2.uml.Type propertyType : propertiesByType.keySet()) {
        // find property by type (ie., propetyType = Order, List<Property> = [order]);
        List<Property> propertiesSortedByType = propertiesByType.get(propertyType);

        // sort property in alphabetical order, also remove redefined properties from the sorted
        // list.
        List<String> nonRedefinedPropertyInAlphabeticalOrderPerType = new ArrayList<>();
        Set<String> parameterProperty = new HashSet<>();
        for (Property p : propertiesSortedByType) {
          if (p.getName() != null) { // Since MD allow having no name.
            if (p.getRedefinedProperties().size() == 0) {
              nonRedefinedPropertyInAlphabeticalOrderPerType.add(p.getName());
              if (p.getAppliedStereotype(STEREOTYPE_STEP) != null
                  || p.getAppliedStereotype(STEREOTYPE_PATICIPANT) != null) {
                stepProperties.add(p.getName());
              } else if (p.getAppliedStereotype(STEREOTYPE_PAREMETER) != null) {
                parameterProperty.add(p.getName());
              }

            }
          } else {
            System.err.println(p.getQualifiedName()
                + "has no name, so ignored.  Please defined the name to be included");
          }
        }
        Collections.sort(nonRedefinedPropertyInAlphabeticalOrderPerType);

        if (nonRedefinedPropertyInAlphabeticalOrderPerType.size() > 0) {
          Sig.Field[] fields =
              toAlloy.addDisjAlloyFields(nonRedefinedPropertyInAlphabeticalOrderPerType,
                  propertyType.getName(), sigOfNamedElement);
          // server, Serve, SinglFooeService
          if (fields != null) {
            for (int j = 0; j < propertiesSortedByType.size(); j++) {
              addCardinality(propertiesSortedByType.get(j), sigOfNamedElement, fields[j].label);
              if (parameterProperty.contains(fields[j].label)) {
                this.parameterFields.add(fields[j]);
              }
            }
          }
        } else {
          for (Property p : propertiesSortedByType) {
            addCardinality(p, sigOfNamedElement, p.getName());
          }
        }
      }
    } // end processing property
    return stepProperties;
  }

  private void addCardinality(Property p, PrimSig sigOfNamedElement, String fieldName) {
    if (p.getLower() == p.getUpper())
      toAlloy.addCardinalityEqualConstraintToField(fieldName, sigOfNamedElement, p.getLower());
    else if (p.getUpper() == -1 && p.getLower() >= 1) {
      toAlloy.addCardinalityGreaterThanEqualConstraintToField(fieldName, sigOfNamedElement,
          p.getLower());
    }
  }

  private void processConnector(Class ne, /* PrimSig sigOfNamedElement, */
      Set<String> stepFieldNames, Set<String> inputs, Set<String> outputs, SysMLUtil sysMLUtil) {

    System.err.println("Process Connector: " + ne.getName());
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
              if (source != null && target != null) {
                toAlloy.addEqual(sigOfNamedElement, source, target);
              }
            }

          } else {
            System.out.println(ce.getDefiningEnd().getName());// transferSource
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
              System.out.println("s: " + source + ce.getRole().getType().getName());
              sourceTypeName = ce.getRole().getType().getName();

              List<Property> ps = sysMLUtil.getPropertyPath(ce);
              System.out.println(ps);

            } else if (definingEndName.equals("transferTarget")) {
              connector_type = CONNECTOR_TYPE.TRANSFER;
              target = endsFeatureNames.get(0);
              System.out.println("t: " + target + " " + ce.getRole().getType().getName());
              targetTypeName = ce.getRole().getType().getName();
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
              Association type = cn.getType();
              if (type.getName().equals("Transfer")) {
                handleTransferAndTransferBeforeInputsAndOutputs(cn, sigOfNamedElement, source,
                    target, sourceTypeName, targetTypeName);
                handleTransferFieldAndFn(sigOfNamedElement, source, target, sourceTypeName,
                    targetTypeName, stepFieldNames);
                inputs.add(targetTypeName);
                outputs.add(sourceTypeName);
              } else if (type.getName().equals("TransferBefore")) {
                handleTransferAndTransferBeforeInputsAndOutputs(cn, sigOfNamedElement, source,
                    target, sourceTypeName, targetTypeName);
                handleTransferBeforeFieldAndFn(sigOfNamedElement, source, target, sourceTypeName,
                    targetTypeName, stepFieldNames);
                inputs.add(targetTypeName);
                outputs.add(sourceTypeName);

              }
            }
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

  public OBMXMI2Alloy(String working_dir) throws FileNotFoundException, UMLModelErrorException {
    toAlloy = new ToAlloy(working_dir);
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

  // public void createAlloyFile(File xmiFile, String className)
  // throws FileNotFoundException, UMLModelErrorException {
  // if (createAlloyModel(xmiFile, className)) {
  // File outputFile = new File("generated-"
  // + className.replaceAll("::", "_") /* alloyModule.getModuleName() */ + ".als");
  // String outputFileName = toAlloy.createAlloyFile(outputFile);
  // System.out.println(outputFileName + " is created");
  // }
  // }

  public void createAlloyFile(File xmiFile, String className, File outputFile)
      throws FileNotFoundException, UMLModelErrorException {
    if (createAlloyModel(xmiFile, className)) {
      String outputFileName = toAlloy.createAlloyFile(outputFile, this.parameterFields);
      System.out.println(outputFileName + " is created");
    }
  }

  private Map<Class, Map<org.eclipse.uml2.uml.Type, List<Property>>> addClasses(
      NamedElement umlElement, SysMLUtil sysMLUtil,
      Map<Class, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass) {

    if (umlElement instanceof org.eclipse.uml2.uml.Class) {
      org.eclipse.uml2.uml.Class umlClass = (org.eclipse.uml2.uml.Class) umlElement;

      Set<org.eclipse.uml2.uml.Property> atts = sysMLUtil.getOwnedAttributes(umlClass);
      if (atts.size() == 0) {
        propertiesByClass.put(umlClass, null);
        return propertiesByClass;
      }

      // find property having the same type
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByTheirType = new HashMap<>();
      for (Property p : atts) {
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
          addClasses(eType, sysMLUtil, propertiesByClass);
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

  private void handleOneOfConnectors(PrimSig ownerSig, Set<Connector> connectors,
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
          if (definingEndName.equals("happensAfter")) {
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


    // handling case of self-loop
    List<String> sourceInTarget = getAsContainInBs(sourceNames, targetNames);
    List<String> targetInSource = getAsContainInBs(targetNames, sourceNames);
    if (sourceInTarget.size() > 0 && isSourceSideOneOf) { // sourceSide
      Expr beforeExpr_filtered = null; // p1
      Expr beforeExpr_all = null; // p1 plus p2

      Expr afterExpr =
          /* ownerSig.domain( */Helper.getFieldFromSig(targetNames.get(0), ownerSig)/* ) */;
      for (String sourceName : sourceNames) {
        if (!sourceInTarget.contains(sourceName)) {
          beforeExpr_filtered = beforeExpr_filtered == null
              ? /* ownerSig.domain( */Helper.getFieldFromSig(sourceName, ownerSig)// )
              : beforeExpr_filtered
                  .plus(/* ownerSig.domain( */Helper.getFieldFromSig(sourceName, ownerSig))/* ) */;
        }
      }
      for (String sourceName : sourceNames) {
        beforeExpr_all = beforeExpr_all == null
            ? /* ownerSig.domain( */Helper.getFieldFromSig(sourceName, ownerSig)// )
            : beforeExpr_all
                .plus(/* ownerSig.domain( */Helper.getFieldFromSig(sourceName, ownerSig))/* ) */;
      }

      toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr_filtered,
          afterExpr); // not include source in source
      toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig,
          beforeExpr_all, afterExpr);
    } else if (targetInSource.size() > 0 && !isSourceSideOneOf) {
      Expr afterExpr_filtered = null; // p3
      Expr afterExpr_all = null; // p2 + p3

      Expr beforeExpr =
          /* ownerSig.domain( */Helper.getFieldFromSig(sourceNames.get(0), ownerSig)/* ) */;
      for (String targetName : targetNames) {
        if (!targetInSource.contains(targetName)) {
          afterExpr_filtered = afterExpr_filtered == null
              ? /* ownerSig.domain( */Helper.getFieldFromSig(targetName, ownerSig)// )
              : afterExpr_filtered
                  .plus(/* ownerSig.domain( */Helper.getFieldFromSig(targetName, ownerSig))/* ) */;
        }
      }
      for (String targetName : targetNames) {
        afterExpr_all = afterExpr_all == null
            ? /* ownerSig.domain( */Helper.getFieldFromSig(targetName, ownerSig)// )
            : afterExpr_all
                .plus(/* ownerSig.domain( */Helper.getFieldFromSig(targetName, ownerSig))/* ) */;
      }

      toAlloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr,
          afterExpr_all); // not include target in source
      toAlloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr,
          afterExpr_filtered);

    } // non self-loop
    else {
      Expr beforeExpr = null;
      Expr afterExpr = null;
      if (isSourceSideOneOf) { // sourceSide need to be combined
        afterExpr =
            /* ownerSig.domain( */Helper.getFieldFromSig(targetNames.get(0), ownerSig)/* ) */;
        for (String sourceName : sourceNames) {
          beforeExpr = beforeExpr == null
              ? /* ownerSig.domain( */Helper.getFieldFromSig(sourceName, ownerSig)// )
              : beforeExpr
                  .plus(/* ownerSig.domain( */Helper.getFieldFromSig(sourceName, ownerSig))/* ) */;
        }

      } else {
        for (String targetName : targetNames) {
          afterExpr =
              afterExpr == null ? /* ownerSig.domain( */Helper.getFieldFromSig(targetName, ownerSig)// )
                  : afterExpr.plus(
                      /* ownerSig.domain( */Helper.getFieldFromSig(targetName, ownerSig))/* ) */;
        }
        beforeExpr =
            /* ownerSig.domain( */Helper.getFieldFromSig(sourceNames.get(0), ownerSig)/* ) */;
      }

      toAlloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(ownerSig, beforeExpr,
          afterExpr);
    }
  }

  /**
   * Find any a contained in b as List of String
   * 
   * @param a
   * @param b
   * @return
   */
  private List<String> getAsContainInBs(List<String> a, List<String> b) {
    List<String> contained = new ArrayList<String>();
    for (String s : a) {
      if (b.contains(s))
        contained.add(s);
    }
    return contained;
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

    Field sourceField = Helper.getFieldFromSig(source, sig);
    Field targetField = Helper.getFieldFromSig(target, sig);

    if (sourceField != null && targetField != null) {
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
  private void handleNoInputsOutputs(Set<String> inputs, Set<String> outputs,
      Set<String> allClasseNames) {
    // Set<String> insAndOuts =
    // Stream.of(inputs, outputs).flatMap(x -> x.stream()).collect(Collectors.toSet());
    // Set<String> allSigs = allClasses.stream().map(c -> c.getName()).collect(Collectors.toSet());
    for (String s : allClasseNames) {
      if (!inputs.contains(s))
        toAlloy.noInputs(s);
      if (!outputs.contains(s))
        toAlloy.noOutputs(s);
    }
  }


  private void handleTransferAndTransferBeforeInputsAndOutputs(org.eclipse.uml2.uml.Connector cn,
      PrimSig sig, String source, String target, String sourceTypeName, String targetTypeName) {

    String[] stTagNames = {"sourceOutputProperty", "targetInputProperty"};// , "itemType"}; this
                                                                          // property value is class
                                                                          // not property
    Map<String, List<Property>> stTagValues =
        getStreotypePropertyValues(cn, STEREOTYPE_ITEMFLOW, stTagNames);
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
  }

  private void handleTransferFieldAndFn(PrimSig sig, String source, String target,
      String sourceTypeName, String targetTypeName, Set<String> stepFieldNames) {
    String fieldName = "transfer" + firstCharUpper(source) + firstCharUpper(target);
    stepFieldNames.add(fieldName);
    Sig.Field transferField = toAlloy.addAlloyTransferField(fieldName, sig);
    toAlloy.createFnForTransferAndAddToOverallFact(sig, /* sig.domain( */transferField/* ) */,
        sourceTypeName, targetTypeName);
  }

  private void handleTransferBeforeFieldAndFn(PrimSig sig, String source, String target,
      String sourceTypeName, String targetTypeName, Set<String> stepFieldNames) {
    // String fieldName = "transferbefore" + firstCharUpper(source) + firstCharUpper(target);
    String fieldName = "transfer" + firstCharUpper(source) + firstCharUpper(target);
    stepFieldNames.add(fieldName);
    // Sig.Field transferField = toAlloy.addAlloyTransferBeforeField(fieldName, sig);
    Sig.Field transferField = toAlloy.addAlloyTransferField(fieldName, sig);
    toAlloy.createFnForTransferBeforeAndAddToOverallFact(sig, /* sig.domain( */transferField/* ) */,
        sourceTypeName, targetTypeName);
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


