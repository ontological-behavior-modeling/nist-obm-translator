package edu.gatech.gtri.obm.translator.alloy.fromxmi;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;

/** The Class Translator. */
public class Translator {

  /** The expr visitor. */
  private final ExprVisitor exprVisitor;
  /** The ignored sigs. */
  private final Set<Sig> ignoredSigs;

  /**
   * Instantiates a new translator.
   *
   * @param ignoredExprs the ignored exprs
   * @param ignoredSigs the ignored sigs
   * @param parameterFields the parameter fields
   */
  public Translator(Set<Expr> ignoredExprs, Set<Sig> ignoredSigs, Set<Sig.Field> parameterFields) {
    exprVisitor = new ExprVisitor(ignoredExprs, parameterFields);
    this.ignoredSigs = ignoredSigs;
  }

  /**
   * Generate als file contents.
   *
   * @param alloyModule the alloy module
   * @param outFilename the out filename
   */
  public void generateAlsFileContents(AlloyModule alloyModule, String outFilename) {
    StringBuilder sb = new StringBuilder();

    sb.append("// This file is created with NIST OBM to Alloy Translator.\n\n").append("module ")
        .append(alloyModule.getModuleName()).append('\n').append("open Transfer[Occurrence] as o\n")
        .append("open utilities/types/relation as r\n").append("abstract sig Occurrence {}\n\n");

    Map<String, String> sigs = new HashMap<>();
    for (Sig sig : alloyModule.getSignatures()) {
      if (!ignoredSigs.contains(sig)) {
        exprVisitor.isRootSig = true;
        // sb.append(exprVisitor.visit(sig));
        sigs.put(sig.label, exprVisitor.visit(sig));
      }
    }

    String s = exprVisitor.visitThis(alloyModule.getFacts());
    String formats = format(s, sigs);
    sb.append(formats);

    try {
      PrintWriter pw = new PrintWriter(outFilename);
      pw.println(sb.toString());
      pw.close();
    } catch (IOException e) {
      System.err.println(e);
    }
  }

  private String getFactBody(String[] domainAndFacts) {
    String s = domainAndFacts[1];
    for (int i = 2; i < domainAndFacts.length; i++) {
      s += "|" + domainAndFacts[i];
    }
    return s;
  }

  private String format(String s, Map<String, String> sigs) {

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

  private String getFacts(String sigName, Map<String, List<String>> facts) {
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
