package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

// TODO: Auto-generated Javadoc
/** The Class Translator. */
public class Translator {

  /** The expr visitor. */
  private final ExprVisitor exprVisitor;

  /** The ignored funcs. */
  private final Set<Func> ignoredFuncs;

  /** The ignored sigs. */
  private final Set<Sig> ignoredSigs;

  /**
   * Instantiates a new translator.
   *
   * @param ignoredExprs the ignored exprs
   * @param ignoredFuncs the ignored funcs
   * @param ignoredSigs the ignored sigs
   */
  public Translator(Set<Expr> ignoredExprs, Set<Func> ignoredFuncs, Set<Sig> ignoredSigs) {
    this(ignoredExprs, ignoredFuncs, ignoredSigs, (Set<Sig.Field>) new HashSet<Sig.Field>());
  }

  /**
   * Instantiates a new translator.
   *
   * @param ignoredExprs the ignored exprs
   * @param ignoredFuncs the ignored funcs
   * @param ignoredSigs the ignored sigs
   * @param parameterFields the parameter fields
   */
  public Translator(
      Set<Expr> ignoredExprs,
      Set<Func> ignoredFuncs,
      Set<Sig> ignoredSigs,
      Set<Sig.Field> parameterFields) {
    exprVisitor = new ExprVisitor(ignoredExprs, parameterFields);
    this.ignoredFuncs = ignoredFuncs;
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

    sb.append("// This file is created with code.\n\n")
        .append("module ")
        .append(alloyModule.getModuleName())
        .append('\n')
        .append("open Transfer[Occurrence] as o\n")
        .append("open utilities/types/relation as r\n")
        .append("abstract sig Occurrence {}\n\n")
        .append("// Signatures:\n");

    for (Sig sig : alloyModule.getSignatures()) {
      if (!ignoredSigs.contains(sig)) {
        exprVisitor.isRootSig = true;
        sb.append(exprVisitor.visit(sig));
      }
    }

    sb.append("\n// Facts:\n");
    sb.append(exprVisitor.visitThis(alloyModule.getFacts()));
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

          sb.append('{')
              .append(AlloyUtils.removeSlash(exprVisitor.visitThis(func.getBody())))
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

          sb.append(": ")
              .append(AlloyUtils.removeSlash(exprVisitor.visitThis(func.returnDecl)))
              .append(" {")
              .append(AlloyUtils.removeSlash(exprVisitor.visitThis(func.getBody())))
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
}
