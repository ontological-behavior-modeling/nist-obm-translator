package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.gatech.gtri.obm.translator.alloy.tofile.Translator;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;

public class ToAlloy {

  Alloy alloy;
  private Map<String, Sig> sigByName;
  private Map<String, Field> fieldByName;
  private Map<String, Sig> fieldTypeByFieldName; // used for pred p1DuringExample { AtomicBehavior
                                                 // in BehaviorFork.p1 }
  private Sig mainSig;

  public ToAlloy() {
    alloy = new Alloy();
    sigByName = new HashMap<>();
    fieldByName = new HashMap<>(); // assume field names are unique
                                   // among all sigs
    fieldTypeByFieldName = new HashMap<>(); // assume field names are unique for one file
  }

  public Sig addAlloySig(String name, String not_used_parentName, boolean isMainSig) {
    System.out.println("========");
    System.out.println("sig " + name + " extends " + not_used_parentName);
    System.out.println("========");

    Sig s = null;
    if (!sigByName.containsKey(name)) {
      s = alloy.createSigAsChildOfOccSigAndAddToAllSigs(name);
      sigByName.put(name, s);
      if (isMainSig)
        mainSig = s;
      return s;
    } else
      return null;
  }

  public Sig addAlloySig(String name, String not_used_parentName) {
    return addAlloySig(name, not_used_parentName, false);
  }

  // sig <OwnerSigName>
  // <fieldName>: set <fieldTypeName>
  // sig SimpleSequence
  // p1: set P1
  public Field addAlloyField(String fieldName, String fieldTypeName, String ownerSigName) {
    Sig.Field p1 =
        FuncUtils.addField(fieldName, sigByName.get(ownerSigName), sigByName.get(fieldTypeName));
    fieldByName.put(fieldName, p1);
    fieldTypeByFieldName.put(fieldName, sigByName.get(fieldTypeName));
    return p1;
  }

  public void createBijectionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(ownerSig, from, to);
  }

  public void createFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, from, to);
  }

  public void addOneConstraintToField(Sig.Field field, String ownerSigName) {
    alloy.addOneConstraintToField(sigByName.get(ownerSigName), field);
  }

  public void addRemainingFactAndPredicate() {
    alloy.addSteps(mainSig, fieldByName);
    alloy.addConstraint(mainSig, fieldByName, fieldTypeByFieldName);
  }


  public void createAlloyFile() {

    String moduleName =
        mainSig.label.startsWith("this") ? mainSig.label.substring(5) : mainSig.label;

    // Run commands
    Command command = alloy.createCommand(moduleName);

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact(), command);

    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());
    String outputFileName =
        new File("generated-" + alloyModule.getModuleName() + ".als").getAbsolutePath();
    translator.generateAlsFileContents(alloyModule, outputFileName);
  }

  public Map<String, Field> getFieldByName() {
    return fieldByName;
  }


}
