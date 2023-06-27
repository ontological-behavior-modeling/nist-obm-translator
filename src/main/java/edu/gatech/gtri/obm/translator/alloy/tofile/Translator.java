package edu.gatech.gtri.obm.translator.alloy.tofile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprList;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;

public class Translator {

  private final ExprVisitor exprVisitor;
  private final Set<Func> ignoredFuncs;
  private final Set<Sig> ignoredSigs;

  public Translator(Set<Expr> ignoredExprs, Set<Func> ignoredFuncs, Set<Sig> ignoredSigs) {
    exprVisitor = new ExprVisitor(ignoredExprs);
    this.ignoredFuncs = ignoredFuncs;
    this.ignoredSigs = ignoredSigs;
  }

  public void generateAlsFileContents(AlloyModule alloyModule, String outFilename) {
    StringBuilder sb = new StringBuilder();

    sb.append("// This file is created with code.").append("\n\n").append("module ")
        .append(alloyModule.getModuleName()).append('\n').append("open Transfer[Occurrence] as o\n")
        .append("open utilities/types/relation as r\n").append("abstract sig Occurrence {}")
        .append("\n\n").append("// Signatures:\n");

    for (Sig sig : alloyModule.getSignatures()) {
      if (!ignoredSigs.contains(sig)) {
        sb.append(exprVisitor.visit(sig));
      }
    }

    sb.append('\n');

    sb.append("// Facts:\n");
    sb.append(exprVisitor.visitThis(alloyModule.getFacts()));
    sb.append('\n');

    sb.append("// Functions and predicates:\n");

    Command command = alloyModule.getCommand();
    Iterator<Func> funcs = command.formula.findAllFunctions().iterator();

    while (funcs.hasNext()) {

      Func nextFunc = funcs.next();

      if (ignoredFuncs.contains(nextFunc)) {
        continue;
      }

      if (nextFunc.isPred) {
        sb.append("pred ").append(MyAlloyLibrary.removeSlash(nextFunc.label));

        if (!nextFunc.decls.isEmpty()) {
          sb.append('[');

          for (int j = 0; j < nextFunc.decls.size(); j++) {
            Decl decl = nextFunc.decls.get(j);
            String[] declarations = new String[decl.names.size()];
            for (int i = 0; i < decl.names.size(); i++) {
              declarations[i] = decl.names.get(i).toString();
            }
            sb.append(String.join(",", declarations));
            sb.append(": ");
            sb.append(exprVisitor.visitThis(decl.expr));

            if (j != nextFunc.decls.size() - 1) {
              sb.append(", ");
            }
          }

          sb.append(']');
        }

        sb.append(" { ")
            .append(MyAlloyLibrary.removeSlash(exprVisitor.visitThis(nextFunc.getBody())))
            .append(" }").append('\n');
      } else if (!nextFunc.isPred) {
        sb.append("fun ").append(MyAlloyLibrary.removeSlash(nextFunc.label));

        if (!nextFunc.decls.isEmpty()) {
          sb.append('[');

          for (int j = 0; j < nextFunc.decls.size(); j++) {
            Decl decl = nextFunc.decls.get(j);
            String[] declarations = new String[decl.names.size()];
            for (int i = 0; i < decl.names.size(); i++) {
              declarations[i] = decl.names.get(i).toString();
            }
            sb.append(String.join(",", declarations));
            sb.append(": ");
            sb.append(exprVisitor.visitThis(decl.expr));

            if (j != nextFunc.decls.size() - 1) {
              sb.append(", ");
            }
          }

          sb.append(']');
        }

        sb.append(": ")
            .append(MyAlloyLibrary.removeSlash(exprVisitor.visitThis(nextFunc.returnDecl)))
            .append(" {")
            .append(MyAlloyLibrary.removeSlash(exprVisitor.visitThis(nextFunc.getBody())))
            .append('}').append('\n');
      }
    }

    sb.append('\n');

    // Run command
    sb.append("// Run commands\n");

    ExprList nameExpr = (ExprList) command.nameExpr;
    String[] runPredicates = new String[nameExpr.args.size()];

    for (int i = 0; i < nameExpr.args.size(); i++) {
      runPredicates[i] = MyAlloyLibrary.removeSlash(nameExpr.args.get(i).toString());
    }


    sb.append("run ").append(command.label).append('{').append(String.join(" and ", runPredicates))
        .append("} for ").append(command.overall);

    try {
      PrintWriter pw = new PrintWriter(outFilename);
      pw.println(sb.toString());
      pw.close();
    } catch (IOException e) {
      System.err.println(e);
    }
  }

}
