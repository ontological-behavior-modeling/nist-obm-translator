package edu.gatech.gtri.obm.alloy.translator;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.mit.csail.sdg.parser.CompUtil;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A class loads supporting libraries (Translator.als and utilities/*.als) and stores all
 * signatures, fields, and facts data to be translated to an alloy file.
 *
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 */
public class Alloy {

  /**
   * The templateModule name string defined for the translator. Set using mainSig name. i.e.,) if
   * the mainSig name is "SimpleSequence", the module name will be "SimpleSequenceModule".
   */
  private String moduleName;
  /** List of Signatures consist of created for the translation */
  protected List<Sig> allSigs;
  /** Expr of facts consist of facts created for the translation */
  protected Expr allFacts;
  /** String used to load library into the templateModule that necessary for the translator. */
  protected static final String templateString =
      "open Transfer[Occurrence] as o \n" + "abstract sig Occurrence{}";

  /** Module created from templateString and Signatures and Facts by the translator. */
  private static Module templateModule;
  /** Module from the Transfer.als */
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
   * Create this object and load required alloy libraries (Translator.als and utilities/*.als) are
   * locating at the given working directory.
   *
   * @param _workingDirectory (String) - The absolute file path
   */
  protected Alloy(String _workingDirectory) {

    System.setProperty(("java.io.tmpdir"), _workingDirectory);
    templateModule = CompUtil.parseEverything_fromString(new A4Reporter(), templateString);

    // abstract
    occSig = (PrimSig) AlloyUtils.getReachableSig("this/Occurrence", templateModule);
    transferModule = AlloyUtils.getAllReachableModuleByName("TransferModule", templateModule);

    happensBefore = AlloyUtils.getFunction("o/happensBefore", transferModule);
    happensDuring = AlloyUtils.getFunction("o/happensDuring", transferModule);
    bijectionFiltered = AlloyUtils.getFunction("o/bijectionFiltered", transferModule);
    functionFiltered = AlloyUtils.getFunction("o/functionFiltered", transferModule);
    inverseFunctionFiltered = AlloyUtils.getFunction("o/inverseFunctionFiltered", transferModule);
    sources = AlloyUtils.getFunction("o/sources", transferModule);
    targets = AlloyUtils.getFunction("o/targets", transferModule);
    subsettingItemRuleForSources =
        AlloyUtils.getFunction("o/subsettingItemRuleForSources", transferModule);
    subsettingItemRuleForTargets =
        AlloyUtils.getFunction("o/subsettingItemRuleForTargets", transferModule);
    isAfterSource = AlloyUtils.getFunction("o/isAfterSource", transferModule);
    isBeforeTarget = AlloyUtils.getFunction("o/isBeforeTarget", transferModule);
    osteps = AlloyUtils.getFunction("o/steps", transferModule);
    oinputs = AlloyUtils.getFunction("o/inputs", transferModule);
    ooutputs = AlloyUtils.getFunction("o/outputs", transferModule);
    oitems = AlloyUtils.getFunction("o/items", transferModule);
    transferSig = AlloyUtils.getReachableSig("o/Transfer", transferModule);
    transferBeforeSig = AlloyUtils.getReachableSig("o/TransferBefore", transferModule);
  }

  /**
   * initialize/reset instance variables when the library location not changed but a class
   * translating is changed
   */
  public void initialize() {
    // initialize a list of signatures.
    allSigs = new ArrayList<Sig>();
    // initialize allFacts as null
    allFacts = null;
  }

  /**
   * Get method for module
   *
   * @return (String) - module name in string
   */
  protected String getModuleName() {
    return this.moduleName;
  }

  /**
   * Set the module name that used to writing out this alloy object to a file.
   *
   * @param _moduleName (String)
   */
  protected void setModuleName(String _moduleName) {
    this.moduleName = _moduleName;
  }

  /**
   * Get method for all signatures
   *
   * @return (List<Sig>) - all signatures
   */
  protected List<Sig> getAllSigs() {
    return this.allSigs;
  }

  /**
   * Add a given sig to allSigs instance variable
   *
   * @param _sig (PrimSig) - a sig to be added
   */
  protected void addToAllSigs(PrimSig _sig) {
    allSigs.add(_sig);
  }

  /**
   * Get method for facts
   *
   * @return (Expr) - all facts
   */
  protected Expr getFacts() {
    return this.allFacts;
  }

  /**
   * Add a expression to allFacts instance variable
   *
   * @param _expr (Expr) - a expression to be added
   */
  protected void addToFacts(Expr _expr) {
    if (allFacts == null) allFacts = _expr;
    else allFacts = allFacts.and(_expr);
  }

  /**
   * Add a set of expression to allFacts instance variable
   *
   * @param _exprs (Set<Expr>) - a set of expression to be added
   */
  protected void addToFacts(Set<Expr> _exprs) {
    for (Expr expr : _exprs) {
      if (allFacts == null) allFacts = expr;
      else allFacts = allFacts.and(expr);
    }
  }

  /**
   * Write an alloy file from all Signatures and Facts as a file.
   *
   * @param _outputFileName (String) - an absolute file name for the alloy output file to be written
   *     as
   * @param _parameterFields (Set<Field>) - a set of Fields. used to determine fields to be disj
   *     constraint (parameter fields are not disj)
   * @throws FileNotFoundException - happens when the outputFileName is failed to be created (not
   *     exist, not writable etc...)
   */
  protected void toFile(String _outputFileName, Set<Sig.Field> _parameterFields)
      throws FileNotFoundException {

    ExprVisitor exprVisitor = new ExprVisitor(_parameterFields);

    StringBuilder sb = new StringBuilder();

    sb.append("// This file is created with NIST OBM to Alloy Translator.\n\n")
        .append("module ")
        .append(moduleName)
        .append('\n')
        .append("open Transfer[Occurrence] as o\n")
        .append("open utilities/types/relation as r\n")
        .append("abstract sig Occurrence {}\n\n");

    Map<String, String> sigsByLabel = new HashMap<>();
    for (Sig sig : this.allSigs) {
      exprVisitor.isRootSig = true;
      sigsByLabel.put(sig.label, exprVisitor.visit(sig));
    }

    String s = exprVisitor.visitThis(this.allFacts);
    String formats = format(s, sigsByLabel);
    sb.append(formats);

    PrintWriter pw = new PrintWriter(_outputFileName);
    pw.println(sb.toString());
    pw.close();
  }

  /**
   * Format this alloy object (Signatures/Fields and Facts) to string by grouping Signature/Fields
   * and Facts for the Signature together.
   *
   * @param _factListInString (String) - all facts in string
   * @param _signatureBlockBySignature (Map: key = Signature name string, value: Signature and
   *     fields as written in the alloy file) - signature and its fields
   * @return (String) - A Module, Signatures, Fields, and Facts grouped by Signature in string
   */
  private static String format(
      String _factListInString, Map<String, String> _signatureBlockBySignature) {

    Map<String, List<String>> facts = new HashMap<>();
    String[] factlines = _factListInString.split("\n");
    for (int i = 0; i < factlines.length; i++) {
      String factline = factlines[i];
      // separate factline into [signature, fact]
      // (ie., for " fact {all x: MultipleControlFlow | no y: Transfer | y in x.steps}" to ["fact
      // {all x: MultipleControlFlow", "no y: Transfer", "y in x.steps"]
      String[] domainAndFacts = factline.split("\\|");

      if (facts.containsKey(
          domainAndFacts[0])) { // to identify Signature "fact {all x: AtomicBehavior"
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
    List<String> sigNames = new ArrayList<>(_signatureBlockBySignature.keySet());
    Collections.sort(sigNames);
    for (String sigName : sigNames) {
      newS +=
          _signatureBlockBySignature.get(
              sigName); // sig AllControl extends Occurrence { disj p1, p2, p3, p4, p5, p6, p7: set
      // AtomicBehavior}
      newS += getFacts(sigName, facts); // fact {...}\n fact {...}\n
    }
    return newS;
  }

  /**
   * Return fact body without the Signature portion from string array separated by "|". For example,
   * when String[] _domainAndFacts for a fact "fact {all x: MultipleControlFlow | no y: Transfer | y
   * in x.steps}" is ["fact {all x: MultipleControlFlow", "no y: Transfer", "y in x.steps"], this
   * methods return _domainAndFacts's index of 1 or more separated by "|": "no y: Transfer | y in
   * x.steps".
   *
   * @param _domainAndFacts (String[]) - string array for a fact expression separate by "|".
   * @return (String) - the fact body in string
   */
  private static String getFactBody(String[] _domainAndFacts) {
    String s = _domainAndFacts[1];
    for (int i = 2; i < _domainAndFacts.length; i++) {
      s += "|" + _domainAndFacts[i];
    }
    return s;
  }

  /**
   * Return string facts (separated by "\n") for the given Signature name string (i.e., "fact {all
   * x: A | #(x.vout) = 1}\nfact {all x: A | no (items.x)}...")
   *
   * @param sigName (String) - Signature name string
   * @param _facts (Map: key = Signature name string, value = facts for the signature) - facts per
   *     signature name
   * @return (String) - facts in string for the given Signature
   */
  private static String getFacts(String _sigName, Map<String, List<String>> _facts) {
    String newS = "";
    // ie., fact key = "fact {all x: AtomicBehavior" => key = "AutomaticBehavior"
    Optional<String> key =
        _facts.keySet().stream()
            .filter(akey -> akey.split(":")[1].trim().equals(_sigName))
            .findFirst();
    if (key.isPresent()) {
      for (String fact : _facts.get(key.get())) {
        newS += key.get() + "|" + fact + "\n";
      }
    }
    return newS;
  }
}
