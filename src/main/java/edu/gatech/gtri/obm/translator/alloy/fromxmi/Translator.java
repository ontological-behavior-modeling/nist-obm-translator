package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;

public class Translator {

  private final ExprVisitor exprVisitor;
  private final Set<Func> ignoredFuncs;
  private final Set<Sig> ignoredSigs;

  public Translator(Set<Expr> ignoredExprs, Set<Func> ignoredFuncs, Set<Sig> ignoredSigs) {
    this(ignoredExprs, ignoredFuncs, ignoredSigs, (Set<Sig.Field>) new HashSet<Sig.Field>());
  }

  public Translator(Set<Expr> ignoredExprs, Set<Func> ignoredFuncs, Set<Sig> ignoredSigs,
      Set<Sig.Field> parameterFields) {
    exprVisitor = new ExprVisitor(ignoredExprs, parameterFields);
    this.ignoredFuncs = ignoredFuncs;
    this.ignoredSigs = ignoredSigs;
  }

  public void generateAlsFileContents(AlloyModule alloyModule, String outFilename) {
    StringBuilder sb = new StringBuilder();

    sb.append("// This file is created with code.\n\n").append("module ")
        .append(alloyModule.getModuleName()).append('\n').append("open Transfer[Occurrence] as o\n")
        .append("open utilities/types/relation as r\n").append("abstract sig Occurrence {}\n\n")
        .append("// Signatures:\n");

    Map<String, String> sigs = new HashMap<>();
    for (Sig sig : alloyModule.getSignatures()) {
      if (!ignoredSigs.contains(sig)) {
        exprVisitor.isRootSig = true;
        // sb.append(exprVisitor.visit(sig));
        sigs.put(sig.label, exprVisitor.visit(sig));
      }
    }

    // sb.append("\n// Facts:\n");


    String s = exprVisitor.visitThis(alloyModule.getFacts());
    String formats = format(s, sigs);
    sb.append(formats);

    // sb.append("\n// Functions and predicates:\n");

    Command[] commands = alloyModule.getCommands();
    Set<String> visitedFuncs = new HashSet<>();

    // Get the functions and predicates from the commands.
    for (Command command : commands) {

      for (Func func : command.formula.findAllFunctions()) {

        if (ignoredFuncs.contains(func) || visitedFuncs.contains(func.toString())) {
          continue;
        }

        visitedFuncs.add(func.toString());

        if (func.isPred) {
          sb.append("pred ").append(AlloyUtils.removeSlash(func.label));

          if (!func.decls.isEmpty()) {
            sb.append('[');

            for (int j = 0; j < func.decls.size(); j++) {
              Decl decl = func.decls.get(j);
              String[] declarations = new String[decl.names.size()];
              for (int i = 0; i < decl.names.size(); i++) {
                declarations[i] = decl.names.get(i).toString();
              }
              sb.append(String.join(",", declarations));
              sb.append(": ");
              sb.append(exprVisitor.visitThis(decl.expr));

              if (j != func.decls.size() - 1) {
                sb.append(", ");
              }
            }

            sb.append(']');
          }

          sb.append('{').append(AlloyUtils.removeSlash(exprVisitor.visitThis(func.getBody())))
              .append("}\n");

        } else if (!func.isPred) {
          sb.append("fun ").append(AlloyUtils.removeSlash(func.label));

          if (!func.decls.isEmpty()) {
            sb.append('[');

            for (int j = 0; j < func.decls.size(); j++) {
              Decl decl = func.decls.get(j);
              String[] declarations = new String[decl.names.size()];
              for (int i = 0; i < decl.names.size(); i++) {
                declarations[i] = decl.names.get(i).toString();
              }
              sb.append(String.join(",", declarations));
              sb.append(": ");
              sb.append(exprVisitor.visitThis(decl.expr));

              if (j != func.decls.size() - 1) {
                sb.append(", ");
              }
            }

            sb.append(']');
          }

          sb.append(": ").append(AlloyUtils.removeSlash(exprVisitor.visitThis(func.returnDecl)))
              .append(" {").append(AlloyUtils.removeSlash(exprVisitor.visitThis(func.getBody())))
              .append("}\n");
        }
      }
    }

    // sb.append("\n// Commands:\n");
    //
    // for(Command command : commands) {
    //
    // if(command.check) {
    // sb.append("check ");
    // }
    // else if(!command.check) {
    // sb.append("run ");
    // }
    //
    // sb.append(command.label).append('{')
    // .append(exprVisitor.visitThis(command.nameExpr)).append("} for ")
    // .append(command.overall);
    //
    // if(!command.scope.isEmpty()) {
    // sb.append(" but ");
    //
    // for(CommandScope cs : command.scope) {
    // sb.append("exactly ").append(cs.startingScope).append(' ')
    // .append(cs.sig);
    // }
    // }
    //
    // sb.append('\n');
    // }

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
      newS += "\n" + sigs.get(sigName);
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
