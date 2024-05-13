package edu.gatech.gtri.obm.alloy.translator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.mit.csail.sdg.parser.CompUtil;

public class Alloy {

  protected static PrimSig occSig;
  private static Module templateModule;
  private static Module transferModule;

  private static ConstList<Sig> ignoredSigs;
  private static Expr ignoredExprs;

  protected static Func happensBefore;
  protected static Func happensDuring;

  protected static Func sources;
  protected static Func targets;
  protected static Func subsettingItemRuleForSources;
  protected static Func subsettingItemRuleForTargets;
  protected static Func isAfterSource;
  protected static Func isBeforeTarget;

  protected static Func bijectionFiltered;
  protected static Func functionFiltered;
  protected static Func inverseFunctionFiltered;

  protected static Func osteps;
  protected static Func oinputs;
  protected static Func ooutputs;
  protected static Func oitems;

  protected static Sig transferSig;
  protected static Sig transferBeforeSig;



  protected Expr overallFact;
  protected List<Sig> allSigs;
  /**
   * The module name used to write out the alloy file. Assigned using mainSig name. i.e.,) if the mainSig name is "SimpleSequence", the module name will be "SimpleSequenceModule".
   */
  private String moduleName;



  protected static final String templateString =
      "open Transfer[Occurrence] as o \n" + "abstract sig Occurrence{}";

  /**
   * 
   * @param working_dir where required alloy library defined in templateString is locating.
   */
  protected Alloy(String working_dir) {

    System.setProperty(("java.io.tmpdir"), working_dir);
    templateModule = CompUtil.parseEverything_fromString(new A4Reporter(), templateString);

    ignoredSigs = templateModule.getAllReachableUserDefinedSigs();

    allSigs = new ArrayList<Sig>();
    allSigs.addAll(ignoredSigs);

    // For this templateModule, contains ExprList<ExprUnary>
    ignoredExprs = templateModule.getAllReachableFacts();
    overallFact = ignoredExprs;

    // abstract
    occSig = (PrimSig) AlloyUtils.getReachableSig(templateModule, "this/Occurrence");
    transferModule = AlloyUtils.getAllReachableModuleByName(templateModule, "TransferModule");

    happensBefore = AlloyUtils.getFunction(transferModule, "o/happensBefore");
    happensDuring = AlloyUtils.getFunction(transferModule, "o/happensDuring");
    bijectionFiltered = AlloyUtils.getFunction(transferModule, "o/bijectionFiltered");
    functionFiltered = AlloyUtils.getFunction(transferModule, "o/functionFiltered");
    inverseFunctionFiltered = AlloyUtils.getFunction(transferModule, "o/inverseFunctionFiltered");

    sources = AlloyUtils.getFunction(transferModule, "o/sources");
    targets = AlloyUtils.getFunction(transferModule, "o/targets");
    subsettingItemRuleForSources =
        AlloyUtils.getFunction(transferModule, "o/subsettingItemRuleForSources");
    subsettingItemRuleForTargets =
        AlloyUtils.getFunction(transferModule, "o/subsettingItemRuleForTargets");

    isAfterSource = AlloyUtils.getFunction(transferModule, "o/isAfterSource");
    isBeforeTarget = AlloyUtils.getFunction(transferModule, "o/isBeforeTarget");


    osteps = AlloyUtils.getFunction(transferModule, "o/steps");
    oinputs = AlloyUtils.getFunction(transferModule, "o/inputs");
    ooutputs = AlloyUtils.getFunction(transferModule, "o/outputs");
    oitems = AlloyUtils.getFunction(transferModule, "o/items");

    transferSig = AlloyUtils.getReachableSig(transferModule, "o/Transfer");
    transferBeforeSig = AlloyUtils.getReachableSig(transferModule, "o/TransferBefore");
  }

  protected void setModuleName(String m) {
    this.moduleName = m;
  }

  protected String getModuleName() {
    return this.moduleName;
  }


  protected List<Sig> getAllSigs() {
    return this.allSigs;
  }

  protected void addToAllSigs(PrimSig sig) {
    allSigs.add(sig);
  }

  protected Expr getOverAllFact() {
    return this.overallFact;
  }

  protected void addToOverallFact(Expr expr) {
    overallFact = overallFact.and(expr);
  }

  protected void addToOverallFacts(Set<Expr> exprs) {
    for (Expr expr : exprs)
      overallFact = overallFact.and(expr);
  }

  protected ConstList<Sig> getIgnoredSigs() {
    return ignoredSigs;
  }

  protected Expr getIgnoredExprs() {
    return ignoredExprs;
  }



  protected void toFile(String outputFileName, Set<Sig.Field> parameterFields)
      throws FileNotFoundException {

    ExprVisitor exprVisitor = new ExprVisitor(this.getIgnoredExprs(), parameterFields);

    StringBuilder sb = new StringBuilder();

    sb.append("// This file is created with NIST OBM to Alloy Translator.\n\n").append("module ")
        .append(moduleName).append('\n').append("open Transfer[Occurrence] as o\n")
        .append("open utilities/types/relation as r\n").append("abstract sig Occurrence {}\n\n");

    Map<String, String> sigsByLabel = new HashMap<>();
    for (Sig sig : this.allSigs) {
      if (!ignoredSigs.contains(sig)) {
        exprVisitor.isRootSig = true;
        sigsByLabel.put(sig.label, exprVisitor.visit(sig));
      }
    }

    String s = exprVisitor.visitThis(this.overallFact);
    String formats = format(s, sigsByLabel);
    sb.append(formats);

    PrintWriter pw = new PrintWriter(outputFileName);
    pw.println(sb.toString());
    pw.close();
  }


  /**
   * String collected with ExprVisitor to format to be write out
   * 
   * @param s
   * @param sigs
   * @return
   */
  private static String format(String s, Map<String, String> sigs) {

    Map<String, List<String>> facts = new HashMap<>();
    String[] lines = s.split("\n");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      String[] domainAndFacts = line.split("\\|");

      if (facts.containsKey(domainAndFacts[0])) {
        List<String> existingFacts = facts.get(domainAndFacts[0]);
        existingFacts.add(getFactBody(domainAndFacts));
        facts.put(domainAndFacts[0], existingFacts);
      } else {
        List<String> newFacts = new ArrayList<String>();
        newFacts.add(getFactBody(domainAndFacts));
        // key = fact {all x: OFServe, value = [,,,]
        facts.put(domainAndFacts[0], newFacts);
      }

    }
    String newS = "";
    List<String> sigNames = new ArrayList<>(sigs.keySet());
    Collections.sort(sigNames);
    for (String sigName : sigNames) {
      newS += sigs.get(sigName);
      newS += getFacts(sigName, facts);
    }
    return newS;
  }

  private static String getFactBody(String[] domainAndFacts) {
    String s = domainAndFacts[1];
    for (int i = 2; i < domainAndFacts.length; i++) {
      s += "|" + domainAndFacts[i];
    }
    return s;
  }

  private static String getFacts(String sigName, Map<String, List<String>> facts) {
    String newS = "";
    Optional<String> key = facts.keySet().stream()
        .filter(akey -> akey.split(":")[1].trim().equals(sigName)).findFirst();
    if (key.isPresent()) {
      for (String fact : facts.get(key.get())) {
        newS += key.get() + "|" + fact + "\n";
      }
    }
    return newS;
  }

}


