package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.gatech.gtri.obm.translator.alloy.tofile.Translator;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;

public class ToAlloy {

  Alloy alloy;
  private Map<String, Sig> sigByName;
  // private Map<String, Field> fieldByName;
  // private Map<String, Sig> fieldTypeByFieldName; // used for pred p1DuringExample {
  // AtomicBehavior
  // in BehaviorFork.p1 }
  private LinkedHashMap<Field, Sig> filedTypeByField;

  private Sig mainSig;

  public ToAlloy() {
    alloy = new Alloy();
    sigByName = new HashMap<>();
    // fieldByName = new HashMap<>(); // assume field names are unique
    // among all sigs
    // fieldTypeByFieldName = new HashMap<>(); // assume field names are unique for one file
    filedTypeByField = new LinkedHashMap<>();
  }


  public Sig addAlloySig(String name, String not_used_parentName, boolean isMainSig) {
    Sig s = null;
    if (!sigByName.containsKey(name)) {
      s = alloy.createSigAsChildOfOccSigAndAddToAllSigs(name);
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

  public Map<String, Sig> getSigMap() {

    return sigByName;
  }

  public Sig addAlloySig(String name, String not_used_parentName) {
    return addAlloySig(name, not_used_parentName, false);
  }

  public Sig getTransferSig() {
    return alloy.getTransferSig();
  }

  // sig <OwnerSigName>
  // <fieldName>: set <fieldTypeName>
  // sig SimpleSequence
  // p1: set P1
  public Field addAlloyField(String fieldName, String fieldTypeName, String ownerSigName) {
    Sig.Field p1 =
        FuncUtils.addField(fieldName, sigByName.get(ownerSigName), sigByName.get(fieldTypeName));
    // fieldByName.put(fieldName, p1);
    // fieldTypeByFieldName.put(fieldName, sigByName.get(fieldTypeName));
    filedTypeByField.put(p1, sigByName.get(fieldTypeName));
    return p1;
  }

  public Field addAlloyField(String fieldName, String fieldTypeName, Sig ownerSig) {
    Sig.Field p1 = FuncUtils.addField(fieldName, ownerSig, sigByName.get(fieldTypeName));
    // fieldByName.put(fieldName, p1);
    // fieldTypeByFieldName.put(fieldName, sigByName.get(fieldTypeName));
    filedTypeByField.put(p1, sigByName.get(fieldTypeName));
    return p1;
  }



  public Field addAlloyTransferField(String fieldName, Sig ownerSig) {
    Sig transferSig = alloy.getTransferSig();
    Sig.Field p1 = FuncUtils.addField(fieldName, ownerSig, transferSig);
    filedTypeByField.put(p1, transferSig);
    return p1;
  }

  public Sig.Field[] addDisjAlloyFields(List<String> fieldNamesList, String typeSigName,
      String ownerSigName) {
    String[] fieldNames = toArray(fieldNamesList);
    Sig.Field[] ps = null;
    Sig sigType = sigByName.get(typeSigName);
    if (sigType != null) {
      ps = FuncUtils.addTrickyField(fieldNames, sigByName.get(ownerSigName), sigType);
      for (int i = 0; i < fieldNames.length; i++) {
        // fieldByName.put(fieldNames[i], ps[i]);
        // fieldTypeByFieldName.put(fieldNames[i], typeSig);
        filedTypeByField.put(ps[i], sigByName.get(typeSigName));
      }
    } else
      System.out.println("sigType is null");
    return ps;

  }



  public void createBijectionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.happensBefore);
  }



  public void createFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, from, to);
  }

  public void createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, from, to);
  }

  public void addCardinalityEqualConstraintToField(Sig.Field field, String ownerSigName, int num) {
    alloy.addCardinalityEqualConstraintToField(sigByName.get(ownerSigName), field, num);
  }

  public void addCardinalityGreaterThanEqualConstraintToField(Sig.Field field, String ownerSigName,
      int num) {
    alloy.addCardinalityGreaterThanEqualConstraintToField(sigByName.get(ownerSigName), field, num);
  }

  public void createFnForTransferAndAddToOverallFact(Sig ownerSig, Expr transfer,
      String sourceTypeName, String targetTypeName) {

    Expr sourceFieldExpr = null;
    Expr targetFieldExpr = null;
    for (Entry<Field, Sig> entry : filedTypeByField.entrySet()) {
      if (entry.getValue().label.compareTo(sourceTypeName) == 0 && entry.getKey().sig == ownerSig) {
        sourceFieldExpr = ownerSig.domain(entry.getKey());
      } else if (entry.getValue().label.compareTo(targetTypeName) == 0
          && entry.getKey().sig == ownerSig) {
        targetFieldExpr = ownerSig.domain(entry.getKey());
      }
    }

    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, sourceFieldExpr, Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, targetFieldExpr, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, transfer);
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
    Field f = getField2(fieldName);
    if (f != null)
      alloy.addInputs(s, sig, f);
  }

  public void addOutputs(String sigName, String fieldName) {
    Sig sig = sigByName.get(sigName);
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Field f = getField2(fieldName);
    if (f != null)
      alloy.addOutputs(s, sig, f);
  }

  public void addSteps(List<String> noStepSigs) {
    for (Sig sig : sigByName.values()) {
      if (!noStepSigs.contains(sig.label)) {
        ExprVar s = ExprVar.make(null, "x", sig.type());
        alloy.addSteps(s, sig, filedTypeByField);
      }
    }
  }


  public String createAlloyFile(File outputFile) {

    String moduleName =
        mainSig.label.startsWith("this") ? mainSig.label.substring(5) : mainSig.label;

    // Run commands
    Command command = alloy.createCommand(moduleName, 10);
    Command[] commands = {command};

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact(), commands);

    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());
    String outputFileName = outputFile.getAbsolutePath();
    translator.generateAlsFileContents(alloyModule, outputFileName);

    // Utils.runX(mainSig, alloy.getAllSigs(), alloy.getOverAllFact(), command);


    return outputFileName;
  }

  public Field getField(String fieldName) {
    Optional<Field> of =
        filedTypeByField.keySet().stream().filter(f -> f.label == fieldName).findFirst();
    return of.isPresent() ? of.get() : null;
  }

  public Field getField2(String fieldName) {
    Optional<Field> of =
        filedTypeByField.keySet().stream().filter(f -> f.label.equals(fieldName)).findFirst();
    return of.isPresent() ? of.get() : null;
  }

  public Field getField3(String fieldName) {
    Optional<Field> of = filedTypeByField.keySet().stream()
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
