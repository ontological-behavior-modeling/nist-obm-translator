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
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.mit.csail.sdg.parser.CompUtil;

/**
 * A class to hold all signatures, fields, and facts to be translated to an alloy file
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 *
 */
public class Alloy {

  /**
   * The templateModule name string defined for the translator. Set using mainSig name. i.e.,) if the mainSig name is "SimpleSequence", the module name will be "SimpleSequenceModule".
   */
  private String moduleName;
  /**
   * List of Signatures consist of created for the translation
   */
  protected List<Sig> allSigs;
  /**
   * Expr of facts consist of facts created for the translation
   */
  protected Expr allFacts;
  /**
   * String used to load library into the templateModule that necessary for the translator.
   */
  protected static final String templateString =
      "open Transfer[Occurrence] as o \n" + "abstract sig Occurrence{}";

  /**
   * Module created from templateString and Signatures and Facts by the translator.
   */
  private static Module templateModule;
  /**
   * Module from the Transfer.als
   */
  private static Module transferModule;

  /** Functions constructed from the transfer module */
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

  /** Signatures constructed from the transfer module */
  protected static Sig transferSig;
  protected static Sig transferBeforeSig;
  protected static PrimSig occSig; // default parent/super type of Signature

  /**
   * Create a new Alloy assuming the required alloy library files (*.als) are locating at the given working directory.
   * 
   * @param working_dir - The absolute filename
   */
  protected Alloy(String working_dir) {

    System.setProperty(("java.io.tmpdir"), working_dir);
    templateModule = CompUtil.parseEverything_fromString(new A4Reporter(), templateString);

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

  public void initialize() {
    // initialize a list of signatures.
    allSigs = new ArrayList<Sig>();
    // initialize allFacts as null
    allFacts = null;

  }

  // Module
  protected String getModuleName() {
    return this.moduleName;
  }

  protected void setModuleName(String m) {
    this.moduleName = m;
  }

  // Signatures
  protected List<Sig> getAllSigs() {
    return this.allSigs;
  }

  protected void addToAllSigs(PrimSig sig) {
    allSigs.add(sig);
  }

  // Facts
  protected Expr getFacts() {
    return this.allFacts;
  }

  /**
   * Add a expression to allFacts instance variable
   * 
   * @param expr - a expression to be added
   */
  protected void addToFacts(Expr expr) {
    if (allFacts == null)
      allFacts = expr;
    else
      allFacts = allFacts.and(expr);
  }

  /**
   * Add a set of expression to allFacts instance variable
   * 
   * @param exprs
   */
  protected void addToFacts(Set<Expr> exprs) {
    for (Expr expr : exprs) {
      if (allFacts == null)
        allFacts = expr;
      else
        allFacts = allFacts.and(expr);
    }
  }


  /**
   * Write an alloy file from all Signatures and Facts.
   * 
   * @param outputFileName - an absolute file name for the alloy output file.
   * @param parameterFields - used to determine fields to be disj constraint (parameter files are not disj)
   * @throws FileNotFoundException - happens when the outputFileName is failed to be created (not exist, not writable etc...)
   */
  protected void toFile(String outputFileName, Set<Sig.Field> parameterFields)
      throws FileNotFoundException {

    ExprVisitor exprVisitor = new ExprVisitor(parameterFields);

    StringBuilder sb = new StringBuilder();

    sb.append("// This file is created with NIST OBM to Alloy Translator.\n\n").append("module ")
        .append(moduleName).append('\n').append("open Transfer[Occurrence] as o\n")
        .append("open utilities/types/relation as r\n").append("abstract sig Occurrence {}\n\n");

    Map<String, String> sigsByLabel = new HashMap<>();
    for (Sig sig : this.allSigs) {
      exprVisitor.isRootSig = true;
      sigsByLabel.put(sig.label, exprVisitor.visit(sig));
    }

    String s = exprVisitor.visitThis(this.allFacts);
    String formats = format(s, sigsByLabel);
    sb.append(formats);

    PrintWriter pw = new PrintWriter(outputFileName);
    pw.println(sb.toString());
    pw.close();
  }


  /**
   * Format this alloy object (Signatures/Fields and Facts) to string by grouping Signature/Fields and Facts for the Signature together.
   * 
   * @param factListInString
   * @param signatureBlockBySignature - Map (key = Signature name string, value = Signature and fields as written in the alloy file).
   * @return string of Signatures, Fields, and Facts grouped by Signature.
   */
  private static String format(String factListInString,
      Map<String, String> signatureBlockBySignature) {

    Map<String, List<String>> facts = new HashMap<>();
    String[] factlines = factListInString.split("\n");
    for (int i = 0; i < factlines.length; i++) {
      String factline = factlines[i];
      // separate factline into [signature, fact]
      // (ie., for " fact {all x: MultipleControlFlow | no y: Transfer | y in x.steps}" to ["fact {all x: MultipleControlFlow", "no y: Transfer", "y in x.steps"]
      String[] domainAndFacts = factline.split("\\|");

      if (facts.containsKey(domainAndFacts[0])) { // to identify Signature "fact {all x: AtomicBehavior"
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
    List<String> sigNames = new ArrayList<>(signatureBlockBySignature.keySet());
    Collections.sort(sigNames);
    for (String sigName : sigNames) {
      newS += signatureBlockBySignature.get(sigName); // sig AllControl extends Occurrence { disj p1, p2, p3, p4, p5, p6, p7: set AtomicBehavior}
      newS += getFacts(sigName, facts); // fact {...}\n fact {...}\n
    }
    return newS;
  }

  /**
   * Return fact body without the Signature portion from string array separated by "|". For example, String[] domainAndFacts for index = 1 or more of "fact {all x: MultipleControlFlow | no y: Transfer |
   * y in x.steps}" is ["fact {all x: MultipleControlFlow", "no y: Transfer", "y in x.steps"], this methods return "no y: Transfer | y in x.steps".
   * 
   * @param domainAndFacts - string array for a fact expression seperated by "|".
   * @return the fact body in a string
   */
  private static String getFactBody(String[] domainAndFacts) {
    String s = domainAndFacts[1];
    for (int i = 2; i < domainAndFacts.length; i++) {
      s += "|" + domainAndFacts[i];
    }
    return s;
  }

  /**
   * Return string facts (separated by "\n") for the given Signature name string
   * 
   * @param sigName - Signature name string
   * @param facts - Map (key = Signature name string, value = facts for the signature)
   * @return facts in string for the given Signature
   */
  private static String getFacts(String sigName, Map<String, List<String>> facts) {
    String newS = "";
    // ie., fact key = "fact {all x: AtomicBehavior" => key = "AutomaticBehavior"
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


