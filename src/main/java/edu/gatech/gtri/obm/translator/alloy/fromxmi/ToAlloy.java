package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.Helper;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.gatech.gtri.obm.translator.alloy.tofile.Translator;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;

public class ToAlloy {

  Alloy alloy;
  private Map<String, PrimSig> sigByName;
  // private Map<String, Field> fieldByName;
  // private Map<String, Sig> fieldTypeByFieldName; // used for pred p1DuringExample {
  // AtomicBehavior
  // in BehaviorFork.p1 }
  private LinkedHashMap<Field, Sig> fieldTypeByField;

  private Sig mainSig;

  public ToAlloy() {
    alloy = new Alloy();
    sigByName = new HashMap<>();
    // fieldByName = new HashMap<>(); // assume field names are unique
    // among all sigs
    // fieldTypeByFieldName = new HashMap<>(); // assume field names are unique for one file
    fieldTypeByField = new LinkedHashMap<>();
  }


  /**
   * 
   * @param name
   * @param parentSig PrimSig or null (if null, Occurrence will be the parentSig)
   * @param isMainSig
   * @return
   */
  public PrimSig createAlloySig(String name, PrimSig parentSig, boolean isMainSig) {
    PrimSig s = null;
    if (!sigByName.containsKey(name)) {
      if (parentSig == null)
        s = alloy.createSigAsChildOfOccSigAndAddToAllSigs(name); // Occurrence as the parent
      else
        s = alloy.createSigAsChildOfParentSigAddToAllSigs(name, parentSig);
      sigByName.put(name, s);
      if (isMainSig)
        mainSig = s;
      return s;
    } else
      return sigByName.get(name);
  }

  /**
   *
   * @param name
   * @param parentName - parent must not be from library (i.e., Occurrence)
   * @param isMainSig
   * @return
   */
  public PrimSig createAlloySig2(String name, String parentName, boolean isMainSig) {
    PrimSig s = null;
    if (!sigByName.containsKey(name)) {
      PrimSig parentSig = (PrimSig) sigByName.get(parentName);
      System.out.println(parentSig);
      s = alloy.createSigAsChildOfParentSigAddToAllSigs(name, parentSig);

      sigByName.put(name, s);
      if (isMainSig)
        mainSig = s;
      return s;
    } else
      return sigByName.get(name);
  }


  public Expr getOverallFacts() {
    return alloy.getOverAllFact();
  }

  public List<Sig> getAllSigs() {
    return alloy.getAllSigs();
  }

  public List<PrimSig> getHierarchySigs() {
    List<PrimSig> list = new ArrayList<PrimSig>();
    List<PrimSig> renamingSigs = new ArrayList<>();
    for (PrimSig aSig : sigByName.values()) {
      if (aSig.parent.label.equals("this/Occurrence")) {
        list.add(aSig);
      } else
        renamingSigs.add(aSig);
    }

    while (renamingSigs.size() > 0) {
      PrimSig aSig = renamingSigs.get(0);
      boolean added = addToHierarchySigList(list, aSig);
      if (added)
        renamingSigs.remove(0);
    }

    return list;
  }

  private boolean addToHierarchySigList(List<PrimSig> list, PrimSig aSig) {
    Optional<PrimSig> parentSig = list.stream().filter(s -> s == aSig.parent).findFirst();
    if (parentSig.isPresent()) {
      int indexOfParent = list.indexOf(parentSig.get());
      list.add(indexOfParent + 1, aSig);
      return true;
    }
    return false;
  }


  public PrimSig getSig(String name) {
    return sigByName.get(name);
  }


  public Map<String, PrimSig> getSigMap() {
    return sigByName;
  }


  public PrimSig createAlloySig(String name, String parentName) {
    if (!Helper.validParent(parentName))
      return createAlloySig(name, (PrimSig) null, false);
    else
      return createAlloySig2(name, parentName, false);
  }

  /**
   * Create Sig in Alloy with "Occurrence" as its parent
   * 
   * @param name
   * @return
   */
  public PrimSig createAlloySig(String name) {
    return createAlloySig(name, (PrimSig) null, false);
  }

  public Sig getTransferSig() {
    return alloy.getTransferSig();
  }

  // sig <OwnerSigName>
  // <fieldName>: set <fieldTypeName>
  // sig SimpleSequence
  // p1: set P1
  // public Field addAlloyField(String fieldName, String fieldTypeName, String ownerSigName) {
  // Sig.Field p1 =
  // FuncUtils.addField(fieldName, sigByName.get(ownerSigName), sigByName.get(fieldTypeName));
  // // fieldByName.put(fieldName, p1);
  // // fieldTypeByFieldName.put(fieldName, sigByName.get(fieldTypeName));
  // fieldTypeByField.put(p1, sigByName.get(fieldTypeName));
  // return p1;
  // }
  //
  // public Field addAlloyField(String fieldName, String fieldTypeName, Sig ownerSig) {
  // Sig.Field p1 = FuncUtils.addField(fieldName, ownerSig, sigByName.get(fieldTypeName));
  // // fieldByName.put(fieldName, p1);
  // // fieldTypeByFieldName.put(fieldName, sigByName.get(fieldTypeName));
  // fieldTypeByField.put(p1, sigByName.get(fieldTypeName));
  // return p1;
  // }


  // create field with Transfer type
  public Field addAlloyTransferField(String fieldName, Sig ownerSig) {
    Sig transferSig = alloy.getTransferSig();
    Sig.Field p1 = FuncUtils.addField(fieldName, ownerSig, transferSig);
    fieldTypeByField.put(p1, transferSig);
    return p1;
  }

  // create field with TransferBefore type
  public Field addAlloyTransferBeforeField(String fieldName, Sig ownerSig) {
    Sig transferBeforeSig = alloy.getTransferBeforeSig();
    Sig.Field p1 = FuncUtils.addField(fieldName, ownerSig, transferBeforeSig);
    fieldTypeByField.put(p1, transferBeforeSig);
    return p1;
  }

  public Sig.Field[] addDisjAlloyFields(List<String> fieldNamesList, String typeSigName,
      PrimSig ownerSig) {

    String[] fieldNames = toArray(fieldNamesList);
    Sig.Field[] ps = null;
    Sig sigType = sigByName.get(typeSigName);
    if (sigType != null) {
      // TODO this may fails of not all fields are not in ownerSig. Could be defined in ownerSig's
      // parent Sig.
      ps = FuncUtils.addTrickyField(fieldNames, ownerSig, sigType);
      if (ps.length != fieldNames.length) {
        System.err.println("!!!!!!!!!!! Thread issue?");
      }
      // Do i need code this way?
      // while (ps.length != fieldNames.length) {
      // System.out.println(
      // "??????ps.length = " + ps.length + " fieldNames.length = " + fieldNames.length);
      // // going to different thread so need to wait
      // try {
      // Thread.sleep(5000);
      // } catch (InterruptedException e) {
      // // TODO Auto-generated catch block
      // e.printStackTrace();
      // }
      // ps = FuncUtils.addTrickyField(fieldNames, ownerSig, sigType);
      //
      // }
      for (int i = 0; i < fieldNames.length; i++) {
        // fieldByName.put(fieldNames[i], ps[i]);
        // fieldTypeByFieldName.put(fieldNames[i], typeSig);
        fieldTypeByField.put(ps[i], sigByName.get(typeSigName));
      }
    } else
      System.err.println("sigType is null");
    return ps;

  }

  // public Sig.Field[] addDisjAlloyFields(List<String> fieldNamesList, String typeSigName,
  // String ownerSigName) {
  //
  // String[] fieldNames = toArray(fieldNamesList);
  // Sig.Field[] ps = null;
  // Sig sigType = sigByName.get(typeSigName);
  // if (sigType != null) {
  // ps = FuncUtils.addTrickyField(fieldNames, sigByName.get(ownerSigName), sigType);
  // while (ps.length != fieldNames.length) {
  // System.out.println(
  // "??????ps.length = " + ps.length + " fieldNames.length = " + fieldNames.length);
  // try {
  // Thread.sleep(5000);
  // } catch (InterruptedException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // ps = FuncUtils.addTrickyField(fieldNames, sigByName.get(ownerSigName), sigType);
  //
  // }
  // for (int i = 0; i < fieldNames.length; i++) {
  // // fieldByName.put(fieldNames[i], ps[i]);
  // // fieldTypeByFieldName.put(fieldNames[i], typeSig);
  // fieldTypeByField.put(ps[i], sigByName.get(typeSigName));
  // }
  // } else
  // System.out.println("sigType is null");
  // return ps;
  //
  // }



  public void createBijectionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.happensBefore);
  }

  public void createBijectionFilteredHappensDuringAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.happensDuring);
  }



  public void createFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, from, to);
  }

  public void createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, from, to);
  }

  public void addCardinalityEqualConstraintToField(Sig.Field field, PrimSig ownerSig, int num) {
    alloy.addCardinalityEqualConstraintToField(ownerSig, field, num);
  }

  public void addCardinalityEqualConstraintToField(String fieldName, PrimSig ownerSig, int num) {
    Sig.Field field = Helper.getFieldFromSig(fieldName, ownerSig); // FoodService <: order, ownerSig
                                                                   // is SignleFoolService
    if (field != null)
      alloy.addCardinalityEqualConstraintToField(ownerSig, field, num);
    else
      System.err
          .println("A field \"" + fieldName + "\" not found in Sig \"" + ownerSig.label + "\".");
  }


  public void addCardinalityGreaterThanEqualConstraintToField(Sig.Field field, PrimSig ownerSig,
      int num) {
    alloy.addCardinalityGreaterThanEqualConstraintToField(ownerSig, field, num);
  }

  public void addCardinalityGreaterThanEqualConstraintToField(String fieldName, PrimSig ownerSig,
      int num) {
    Sig.Field field = Helper.getFieldFromSig(fieldName, ownerSig);
    if (field != null)
      alloy.addCardinalityGreaterThanEqualConstraintToField(ownerSig, field, num);
    else
      System.err
          .println("A field \"" + fieldName + "\" not found in Sig \"" + ownerSig.label + "\".");
  }


  public void createFnForTransferAndAddToOverallFact(Sig ownerSig, Expr transfer,
      String sourceTypeName, String targetTypeName) {

    Expr sourceFieldExpr = null;
    Expr targetFieldExpr = null;
    for (Entry<Field, Sig> entry : fieldTypeByField.entrySet()) {
      if (entry.getValue().label.compareTo(sourceTypeName) == 0 && entry.getKey().sig == ownerSig) {
        sourceFieldExpr = ownerSig.domain(entry.getKey());
      } else if (entry.getValue().label.compareTo(targetTypeName) == 0
          && entry.getKey().sig == ownerSig) {
        targetFieldExpr = ownerSig.domain(entry.getKey());
      }
    }

    // fact {all x: ParticipantTransfer | bijectionFiltered[sources, x.transferSupplierCustomer,
    // x.supplier]}
    // fact {all x: ParticipantTransfer | bijectionFiltered[targets, x.transferSupplierCustomer,
    // x.customer]}
    // fact {all x: ParticipantTransfer | subsettingItemRuleForSources[x.transferSupplierCustomer]}
    // fact {all x: ParticipantTransfer | subsettingItemRuleForTargets[x.transferSupplierCustomer]}

    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, sourceFieldExpr, Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, targetFieldExpr, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, transfer);
  }

  /**
   * 
   * @param ownerSig
   * @param transfer ie., transferbeforeAB
   * @param sourceTypeName
   * @param targetTypeName
   */
  public void createFnForTransferBeforeAndAddToOverallFact(Sig ownerSig, Expr transfer,
      String sourceTypeName, String targetTypeName) {

    Expr sourceFieldExpr = null;
    Expr targetFieldExpr = null;
    for (Entry<Field, Sig> entry : fieldTypeByField.entrySet()) {
      if (entry.getValue().label.compareTo(sourceTypeName) == 0 && entry.getKey().sig == ownerSig) {
        sourceFieldExpr = ownerSig.domain(entry.getKey());
      } else if (entry.getValue().label.compareTo(targetTypeName) == 0
          && entry.getKey().sig == ownerSig) {
        targetFieldExpr = ownerSig.domain(entry.getKey());
      }
    }

    // fact {all x: ParticipantTransfer | bijectionFiltered[sources, x.transferSupplierCustomer,
    // x.supplier]}
    // fact {all x: ParticipantTransfer | bijectionFiltered[targets, x.transferSupplierCustomer,
    // x.customer]}
    // fact {all x: ParticipantTransfer | subsettingItemRuleForSources[x.transferSupplierCustomer]}
    // fact {all x: ParticipantTransfer | subsettingItemRuleForTargets[x.transferSupplierCustomer]}

    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, sourceFieldExpr, Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, targetFieldExpr, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, transfer);
    alloy.createIsAfterSourceIsBeforeTargetOverallFact(ownerSig, transfer);

    // fact {all x: ParameterBehavior | isAfterSource[x.transferbeforeAB]}//missing
    // fact {all x: ParameterBehavior | isBeforeTarget[x.transferbeforeAB]}//missing
  }



  public void createFnForTransferBeforeAndAddToOverallFact(Sig ownerSig, Expr start, Expr middle,
      Expr end, String sourceTypeName, String targetTypeName) {

    Expr sourceFieldExpr = null;
    Expr targetFieldExpr = null;
    for (Entry<Field, Sig> entry : fieldTypeByField.entrySet()) {
      if (entry.getValue().label.compareTo(sourceTypeName) == 0 && entry.getKey().sig == ownerSig) {
        sourceFieldExpr = ownerSig.domain(entry.getKey());
      } else if (entry.getValue().label.compareTo(targetTypeName) == 0
          && entry.getKey().sig == ownerSig) {
        targetFieldExpr = ownerSig.domain(entry.getKey());
      }
    }

    /** start --- Constraints on the Transfer from input of B to intput of B1 */
    // fact {all x: B | functionFiltered[sources, x.transferBB1, x]}//missing
    // fact {all x: B | bijectionFiltered[targets, x.transferBB1, x.b1]}//missing
    // fact {all x: B | subsettingItemRuleForSources[x.transferBB1]}//missing
    // fact {all x: B | subsettingItemRuleForTargets[x.transferBB1]}//missing
    // fact {all x: B | isBeforeTarget[x.transferBB1]}//missing

    alloy.createFunctionFilteredAndAddToOverallFact(ownerSig, start, sourceFieldExpr,
        Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, start, sourceFieldExpr, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, start);

    /** middle Constraints on the Transfer from output of B1 to input of B2 */
    // fact {all x: B | bijectionFiltered[sources, x.transferbeforeB1B2, x.b1]}//missing
    // fact {all x: B | bijectionFiltered[targets, x.transferbeforeB1B2, x.b2]}//missing
    // fact {all x: B | subsettingItemRuleForSources[x.transferBB1]}//missing
    // fact {all x: B | subsettingItemRuleForTargets[x.transferBB1]}//missing
    // fact {all x: B | isAfterSource[x.transferbeforeB1B2]}//missing
    // fact {all x: B | isBeforeTarget[x.transferbeforeB1B2]}//missing
    alloy.createBijectionFilteredToOverallFact(ownerSig, middle, sourceFieldExpr, Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, middle, targetFieldExpr, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, middle);

    /** Constraints on the Transfer from output of B2 to output of B */
    // fact {all x: B | bijectionFiltered[sources, x.transferB2B, x.b2]}//missing
    // fact {all x: B | functionFiltered[targets, x.transferB2B, x]}//missing
    // fact {all x: B | subsettingItemRuleForSources[x.transferB2B]}//missing
    // fact {all x: B | subsettingItemRuleForTargets[x.transferB2B]}//missing
    // fact {all x: B | isAfterSource[x.transferB2B]}//missing
    alloy.createFunctionFilteredAndAddToOverallFact(ownerSig, end, targetFieldExpr, Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, end, targetFieldExpr, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, end);
  }

  public void noInputs(String sigName) {
    Sig sig = sigByName.get(sigName);
    alloy.noInputs(sig);
  }

  public void noOutputs(String sigName) {
    Sig sig = sigByName.get(sigName);
    alloy.noOutputs(sig);
  }

  public void addInputs(String sigName, String fieldName) {
    Sig sig = sigByName.get(sigName);
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Field f = getField(fieldName);
    if (f != null)
      alloy.addInputs(s, sig, f);
    else
      System.err.println("No field \"" + fieldName + "\" in sig \"" + sigName + "\"");
  }

  public void addOutputs(String sigName, String fieldName) {
    Sig sig = sigByName.get(sigName);
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Field f = getField(fieldName);
    if (f != null)
      alloy.addOutputs(s, sig, f);
    else
      System.err.println("No field \"" + fieldName + "\" in sig \"" + sigName + "\"");
  }

  public void addSteps(List<String> noStepSigs) {
    for (Sig sig : sigByName.values()) {
      if (!noStepSigs.contains(sig.label)) {
        ExprVar s = ExprVar.make(null, "x", sig.type());
        alloy.addSteps(s, sig, fieldTypeByField);
      }
    }
  }


  public String createAlloyFile(File outputFile) {

    // set modulename without this
    String moduleName =
        mainSig.label.startsWith("this") ? mainSig.label.substring(5) : mainSig.label;

    // Run commands
    Command command = alloy.createCommand(moduleName, 10);
    Command[] commands = {command};

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact(), commands);

    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());

    if (outputFile != null) {
      String outputFileName = outputFile.getAbsolutePath();
      translator.generateAlsFileContents(alloyModule, outputFileName);
      return outputFileName;
    }
    // Utils.runX(mainSig, alloy.getAllSigs(), alloy.getOverAllFact(), command);


    return "No outputfile ";
  }

  public Field getField(String fieldName) {
    Optional<Field> of =
        fieldTypeByField.keySet().stream().filter(f -> f.label.equals(fieldName)).findFirst();
    return of.isPresent() ? of.get() : null;
  }

  public Entry<Field, Sig> getFieldFieldType(String fieldName) {
    for (Iterator<Entry<Field, Sig>> iter = fieldTypeByField.entrySet().iterator(); iter
        .hasNext();) {
      Entry<Field, Sig> entry = iter.next();
      if (entry.getKey().label.equals(fieldName))
        return entry;
    }
    return null;

  }

  public Field getField3(String fieldName) {
    Optional<Field> of = fieldTypeByField.keySet().stream()
        .filter(f -> f.label.compareTo(fieldName) == 0).findFirst();
    return of.isPresent() ? of.get() : null;
  }

  public String[] toArray(List<String> o) {
    String[] r = new String[o.size()];
    int i = 0;
    for (String s : o) {
      r[i++] = s;
    }
    return r;
  }

}
