package obmtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.gatech.gtri.obm.alloy.translator.AlloyUtils;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprBinary;
import edu.mit.csail.sdg.ast.ExprCall;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprHasName;
import edu.mit.csail.sdg.ast.ExprITE;
import edu.mit.csail.sdg.ast.ExprLet;
import edu.mit.csail.sdg.ast.ExprList;
import edu.mit.csail.sdg.ast.ExprQt;
import edu.mit.csail.sdg.ast.ExprUnary;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Type;

/**
 * Comparator for two alloy objects.
 * 
 * @author Shinjo Andrew H, GIRI Intern, Georgia Tech
 * @author Wilson Miyako, ASDL, Georgia Tech
 *
 */

public class ExpressionComparator {

  /** used to prevent comparison to run forever */
  private final Set<List<Expr>> visitedExpressions;

  public ExpressionComparator() {
    visitedExpressions = new HashSet<>();
  }

  /**
   * Compare two Signatures (name, a parent, fields)
   * 
   * @param s1 - a Signature to be compared
   * @param s2 - a Signature to be compared
   * @return true if the same, otherwise return false
   */
  public boolean compareTwoExpressionsSigs(Sig s1, Sig s2) {
    visitedExpressions.clear();
    return compareExpr(s1, s2);
  }

  /**
   * Compare two facts
   * 
   * @param e1 - an expression containing one or more fact expressions
   * @param e2 - an expression containing one or more fact expressions
   * @return
   */
  public boolean compareTwoExpressionsFacts(Expr e1, Expr e2) {
    visitedExpressions.clear();

    // compare facts size
    int size1 = ((Collection<?>) ((ExprList) e1).args).size();
    int size2 = ((Collection<?>) ((ExprList) e2).args).size();

    if (size1 != size2) {
      System.err.println("The size of facts is different. " + size1 + " vs. " + size2);
      return false;
    }
    // e1.args.size() and ee1.size() are different. ee1 can contain like [(all x | no o/inputs . x),
    // (all x | no o/inputs . x)] the two expr of the same
    // Expressions as string are the same but what x represents(Signature) are different. For example,
    // fact {all x: AtomicBehavior | no inputs.x} vs.
    // fact {all x: MultipleControlFlow | no inputs.x}
    List<List<Expr>> ee1 = sortExprList((ExprList) e1);
    List<List<Expr>> ee2 = sortExprList((ExprList) e2);

    if (ee1.size() != ee2.size()) {
      System.err.println(
          "The size of sorted grouped facts are different. " + ee1.size() + " vs. " + ee2.size());
      return false;
    }

    for (int i = 0; i < ee1.size(); i++) {
      if (ee1.get(i).size() != ee2.get(i).size()) {
        System.err.println("The size of sorted grouped fact is different. " + ee1.get(i) + "("
            + ee1.get(i).size() + ") vs. " + ee2.get(i) + "(" + ee2.get(i).size() + ")");
        return false;
      }

      // make sure each expression in ee1.get(i) and ee2.get(i) are the same - expression is string and which signature the expression is for.
      Set<Integer> expr2usedJIndex = new HashSet<>();
      for (Expr expr1 : ee1.get(i)) {
        boolean expr1found = false;
        for (int j = 0; j < ee2.get(i).size(); j++) {
          if (!expr2usedJIndex.contains(j)) {
            Expr expr2 = ee2.get(i).get(j);
            if (!compareExpr(expr1, expr2))
              continue;
            else {
              expr1found = true;
              expr2usedJIndex.add(j);
              break;
            }
          }
        }
        if (!expr1found)
          return false;
      }
    }
    return true;
  }


  public boolean compareCommand(Command c1, Command c2) {

    if (c1 == null && c2 == null)
      return true;

    if (c1 == null || c2 == null) {
      System.err.println("compareCommand: c1 == null || c2 == null");
      return false;
    }

    // ConstList<Sig> additionalExactScopes
    if (c1.additionalExactScopes.size() != c2.additionalExactScopes.size()) {
      System.err.println("compareCommand: " + "c1.additionalExactScopes.size() != "
          + "c2.additionalExactScopes.size() (" + c1.additionalExactScopes.size() + " vs. "
          + c2.additionalExactScopes.size() + ")");
      return false;
    }

    for (int i = 0; i < c1.additionalExactScopes.size(); i++) {
      Sig s1 = c1.additionalExactScopes.get(i);
      Sig s2 = c2.additionalExactScopes.get(i);
      if (!compareSig(s1, s2)) {
        System.err.println("compareCommand: !compareSig(s1, s2) (" + s1 + " vs." + s2 + ")");
        return false;
      }
    }

    // int bitwidth
    if (c1.bitwidth != c2.bitwidth) {
      System.err.println("compareCommand: c1.bitwidth != c2.bitwidth (" + c1.bitwidth + " vs. "
          + c2.bitwidth + ")");
      return false;
    }

    // boolean check
    if (c1.check != c2.check) {
      System.err
          .println("compareCommand: c1.check != c2.check (" + c1.check + " vs. " + c2.check + ")");
      return false;
    }

    // int expects
    if (c1.expects != c2.expects) {
      System.err.println(
          "compareCommand: c1.expects != c2.expects (" + c1.expects + " vs. " + c2.expects + ")");
      return false;
    }

    // Expr formula
    if (!compareExpr(c1.formula, c2.formula)) {
      System.err.println("compareCommand: " + "!compareExpr(c1.formula, c2.formula) (" + c1.formula
          + " vs. " + c2.formula + ")");
      return false;
    }

    // String label
    if (!c1.label.equals(c2.label)) {
      System.err.println(
          "compareCommand: !c1.label.equals(c2.label) (" + c1.label + " vs. " + c2.label + ")");
      return false;
    }

    // int maxseq
    if (c1.maxseq != c2.maxseq) {
      System.err.println(
          "compareCommand: c1.maxseq != c2.maxseq (" + c1.maxseq + " vs. " + c2.maxseq + ")");
      return false;
    }
    // int maxstring
    if (c1.maxstring != c2.maxstring) {
      System.err.println("compareCommand: c1.maxstring != c2.maxstring ( " + c1.maxstring + " vs. "
          + c2.maxstring + ")");
      return false;
    }
    // int overall
    if (c1.overall != c2.overall) {
      System.err.println(
          "compareCommand: c1.overall != c2.overall (" + c1.overall + " vs. " + c2.overall + ")");
      return false;
    }
    // Command parent
    if (!compareCommand(c1.parent, c2.parent)) {
      System.err.println("compareCommand: " + "!compareCommand(c1.parent, c2.parent) (" + c1.parent
          + " vs. " + c2.parent + ")");
      return false;
    }
    // Pos pos (ignore)

    // ConstList<CommandScope> scope
    if (c1.scope.size() != c2.scope.size()) {
      System.err.println("compareCommand: " + "c1.scope.size() != c2.scope.size() ("
          + c1.scope.size() + " vs. " + c2.scope.size() + ")");
      return false;
    }

    for (int i = 0; i < c1.scope.size(); i++) {
      CommandScope cs1 = c1.scope.get(i);
      CommandScope cs2 = c2.scope.get(i);

      if (!compareCommandScope(cs1, cs2)) {
        System.err.println("compareCommand: !compareCommandScope(cs1, cs2)");
      }
    }

    return true;
  }

  public boolean compareCommandScope(CommandScope cs1, CommandScope cs2) {

    if (cs1 == null && cs2 == null) {
      System.err.println("compareCommandScope: cs1 == null && cs2 == null");
      return true;
    }

    if (cs1 == null || cs2 == null) {
      System.err.println("compareCommandScope: cs1 == null || cs2 == null");
      return false;
    }

    // int endingScope
    if (cs1.endingScope != cs2.endingScope) {
      System.err.println("compareCommandScope: " + "cs1.endingScope != cs2.endingScope ("
          + cs1.endingScope + " vs. " + cs2.endingScope + ")");
      return false;
    }
    // int increment
    if (cs1.endingScope != cs2.endingScope) {
      System.err.println("compareCommandScope: " + "cs1.increment != cs2.increment ("
          + cs1.increment + " vs. " + cs2.increment + ")");
      return false;
    }

    // boolean isExact
    if (cs1.isExact != cs2.isExact) {
      System.err.println("compareCommandScope: " + "cs1.isExact != cs2.isExact (" + cs1.isExact
          + " vs. " + cs2.isExact + ")");
      return false;
    }
    // Pos pos (ignored)

    // Sig sig
    if (!compareSig(cs1.sig, cs2.sig)) {
      System.err.println("compareCommandScope: " + "!compareSig(cs1.sig, cs2.sig)");
      return false;
    }

    // int startingScope
    if (cs1.startingScope != cs2.startingScope) {
      System.err.println("compareCommandScope: " + "cs1.startingScope != cs2.startingScope ("
          + cs1.startingScope + " vs. " + cs2.startingScope + ")");
      return false;
    }
    return true;
  }

  private boolean compareDecl(Decl d1, Decl d2) {

    if (d1 == null && d2 == null) {
      System.err.println("CompareDecls: d1 == null && d2 == null (" + d1 + " vs. " + d2 + ")");
      return true;
    }

    if (d1 == null && d2 != null) {
      System.err.println("CompareDecls: d1 == null && d2 != null(" + d1 + " vs. " + d2 + ")");
      return false;
    }
    if (d1 != null && d2 == null) {
      System.err.println("CompareDecls: d1 != null && d2 == null (" + d1 + " vs. " + d2 + ")");
      return false;
    }

    if (d1.disjoint == null && d2.disjoint != null) {
      System.err.println(
          "compareDecls: d1.disjoint == null && d2.disjoint != null (" + d1 + " vs. " + d2 + ")");
      return false;
    }
    if (d1.disjoint != null && d2.disjoint == null) {
      System.err.println(
          "compareDecls: d1.disjoint != null && d2.disjoint == null (" + d1 + " vs. " + d2 + ")");
      return false;
    }

    if (d1.disjoint2 == null && d2.disjoint2 != null) {
      System.err.println(
          "compareDecls: d1.disjoint2 == null && d2.disjoint2 != null (" + d1 + " vs. " + d2 + ")");
      return false;
    }
    if (d1.disjoint2 != null && d2.disjoint2 == null) {
      System.err.println(
          "compareDecls: d1.disjoint2 != null && d2.disjoint2 == null (" + d1 + " vs. " + d2 + ")");
      return false;
    }
    if (!compareExpr(d1.expr, d2.expr)) {
      System.err.println(
          "compareDecl: !compareExpr(d1.expr, d2.expr) (" + d1.expr + " vs. " + d2.expr + ")");
      return false;
    }

    if (d1.isPrivate == null && d2.isPrivate != null) {
      System.err.println("compareDecls: d1.isPrivate == null && d2.isPrivate != null ("
          + d1.isPrivate + " vs. " + d2.isPrivate + ")");
      return false;
    }
    if (d1.isPrivate != null && d2.isPrivate == null) {
      System.err.println("compareDecls: d1.isPrivate != null && d2.isPrivate == null" + d1.isPrivate
          + " vs. " + d2.isPrivate + ")");
      return false;
    }

    if (d1.names.size() != d2.names.size()) {
      System.err.println("compareDecls: d1.names.size() != d2.names.size() (" + d1.names.size()
          + " vs. " + d2.names.size() + ")");
      return false;
    }

    // check all d1.names are in d2.names
    boolean found;
    for (ExprHasName name1 : d1.names) {
      found = false;
      for (ExprHasName name2 : d2.names) {
        visitedExpressions.clear();
        if (compareAsString(name1, name2)) {
          if (compareExpr(name1, name2)) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        System.err.println(
            "compareDecls: decl1.name is not in decl2.names (" + name1 + " vs." + d2.names + ")");
        return false;
      }
    }
    // check all d2.names are in d1.names
    for (ExprHasName name2 : d2.names) {
      found = false;
      for (ExprHasName name1 : d1.names) {
        visitedExpressions.clear();
        if (compareAsString(name1, name2)) {
          if (compareExpr(name1, name2)) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        System.err.println(
            "compareDecls: decl2.name is not in decl1.names (" + name2 + " vs." + d1.names + ")");
        return false;
      }
    }

    return true;
  }

  private boolean compareExpr(Expr expr1, Expr expr2) {
    return compareExpr(expr1, expr2, false);
  }

  private boolean compareExpr(Expr expr1, Expr expr2, boolean start) {
    if (expr1 == null && expr2 == null) {
      return true;
    }

    if (expr1 == null && expr2 != null) {
      System.err
          .println("compareExpr: expr1 == null && expr2 != null (" + expr1 + " vs. " + expr2 + ")");
      return false;
    }
    if (expr1 != null && expr2 == null) {
      System.err
          .println("compareExpr: expr1 != null && expr2 == null" + expr1 + " vs. " + expr2 + ")");
      return false;
    }

    expr1 = expr1.deNOP();
    expr2 = expr2.deNOP();

    if (!expr1.getClass().equals(expr2.getClass())) {
      System.err.println("expr1.getClass() != expr2.getClass() (" + expr1.getClass() + " vs. "
          + expr2.getClass() + ")");
      return false;
    }

    if (visitedExpressions.contains(List.of(expr1, expr2))) {
      System.out.println(expr1 + " and " + expr2 + " is already visited");
      return true;
    }
    visitedExpressions.add(List.of(expr1, expr2));

    if (expr1.getClass().equals(Expr.class)) {
      System.err.println("CompareExpr Expr.class not implemented");
    } else if (expr1.getClass().equals(ExprBinary.class)) {
      if (!compareExprBinary((ExprBinary) expr1, (ExprBinary) expr2)) {

        System.err
            .println("compareExpr: !compareExprBinary((ExprBinary) expr1, (ExprBinary) expr2) ("
                + expr1 + " vs. " + expr2 + ")");
        return false;
      }
    } else if (expr1.getClass().equals(ExprCall.class)) {
      if (!compareExprCall((ExprCall) expr1, (ExprCall) expr2)) {
        System.err
            .println("compareExpr: " + "!compareExprCall((ExprCall) expr1, (ExprCall) expr2) ("
                + expr1 + " vs. " + expr2 + ")");
        return false;
      }
    } else if (expr1.getClass().equals(ExprConstant.class)) {
      if (!compareExprConstant((ExprConstant) expr1, (ExprConstant) expr2)) {
        System.err.println(
            "compareExpr: !compareExprConstant((ExprConstant) expr1, (ExprConstant) expr2) ("
                + expr1 + " vs. " + expr2 + ")");
        return false;
      }
    } else if (expr1.getClass().equals(ExprITE.class)) {
      if (!compareExprITE((ExprITE) expr1, (ExprITE) expr2)) {
        System.err.println("compareExpr: !compareExprITE((ExprITE) expr1, (ExprITE) expr2) ("
            + expr1 + " vs. " + expr2 + ")");
        return false;
      }
    } else if (expr1.getClass().equals(ExprLet.class)) {
      System.err.println("ExprLet: not implemented");
    } else if (expr1.getClass().equals(ExprList.class)) {
      if (!compareExprList((ExprList) expr1, (ExprList) expr2)) {
        System.err
            .println("compareExpr: " + "!compareExprList((ExprList) expr1, (ExprList) expr2) ("
                + expr1 + " vs. " + expr2 + ")");
        return false;
      }
    } else if (expr1.getClass().equals(ExprQt.class)) {
      if (!compareExprQt((ExprQt) expr1, (ExprQt) expr2)) {
        System.err.println("compareExpr: " + "!compareExprQt((ExprQt) expr1, (ExprQt) expr2) ("
            + expr1 + " vs. " + expr2 + ")");
        return false;
      }
    } else if (expr1.getClass().equals(ExprUnary.class)) {
      if (!compareExprUnary((ExprUnary) expr1, (ExprUnary) expr2)) {
        System.err
            .println("compareExpr: !compareExprUnary(" + "(ExprUnary) expr1, (ExprUnary) expr2) "
                + expr1 + " vs. " + expr2 + "(" + expr1 + " vs. " + expr2 + ")");
        return false;
      }
    } else if (expr1.getClass().equals(ExprVar.class)) {
      if (!compareExprVar((ExprVar) expr1, (ExprVar) expr2)) {
        System.err.println("compareExpr: !compareExprVar(" + "(ExprVar) expr1, (ExprVar) expr2) ("
            + expr1 + " vs. " + expr2 + ")");
        return false;
      }
    } else if (expr1.getClass().equals(Sig.Field.class)) {
      if (!compareSigField((Sig.Field) expr1, (Sig.Field) expr2)) {
        System.err.println("compareExpr: !compareSigField("
            + "(Sig.Field) expr1, (Sig.Field) expr2) (" + expr1 + " vs." + expr2 + ")");
        System.err.println();
        return false;
      }
    } else if (expr1.getClass().equals(Sig.class) || expr1.getClass().equals(Sig.PrimSig.class)) {
      if (!compareSig((Sig) expr1, (Sig) expr2)) {
        System.err.println("compareExpr: !compareSig(" + "(Sig) expr1, (Sig) expr2) (" + expr1
            + " vs." + expr2 + ")");
        System.err.println();
        return false;
      }
    } else {
      System.err.println("Unexpected class: " + expr1.getClass());
      return false;
    }

    return true;
  }

  private boolean compareExprBinary(ExprBinary expr1, ExprBinary expr2) {
    if (expr1 == null && expr2 == null) {
      System.err.println("compareExprBinary: expr1 == null && expr2 == null");
      return true;
    }
    if (expr1 == null && expr2 != null) {
      System.err.println(
          "compareExprBinary: expr1 == null && expr2 != null (" + expr1 + " vs. " + expr2 + ")");
      return false;
    }
    if (expr1 != null && expr2 == null) {
      System.err.println(
          "compareExprBinary: expr1 != null && expr2 == null(" + expr1 + " vs. " + expr2 + ")");
      return false;
    }
    if (!compareExpr(expr1.left, expr2.left)) {
      System.err.println("ExprBinary: !compareExpr(expr1.left, expr2.left) (" + expr1.left + " vs. "
          + expr2.left + ")");
      return false;
    }

    if (expr1.op != expr2.op) {
      System.err
          .println("ExprBinary: expr1.op != expr2.op (" + expr1.op + " vs. " + expr2.op + ")");
      return false;
    }
    if (!compareExpr(expr1.right, expr2.right)) {
      System.err.println("ExprBinary: !compareExpr(expr1.right, expr2.right) (" + expr1.right
          + " vs. " + expr2.right + ")");
      return false;
    }
    return true;
  }

  private boolean compareExprCall(ExprCall expr1, ExprCall expr2) {
    if (expr1 == null && expr2 == null) {
      System.err.println(
          "compareExprCall: expr1 == null && expr2 == null) (" + expr1 + " vs. " + expr2 + ")");
      return true;
    }

    if (expr1 == null && expr2 != null) {
      System.err.println(
          "compareExprCall: expr1 == null && expr2 != null) (" + expr1 + " vs. " + expr2 + ")");
      return false;
    }
    if (expr1 != null && expr2 == null) {
      System.err.println(
          "compareExprCall: expr1 != null && expr2 == null) (" + expr1 + " vs. " + expr2 + ")");
      return false;
    }

    if (expr1.args.size() != expr2.args.size()) {
      System.err.println("compareExprCall: expr1.args.size() != expr2.args.size() ( "
          + expr1.args.size() + " vs. " + expr2.args.size() + ")");
      return false;
    }

    // check all expr1.args are in expr2.args
    boolean found;
    for (Expr next1 : expr1.args) {
      found = false;
      for (Expr next2 : expr2.args) {
        visitedExpressions.clear();
        if (compareAsString(next1, next2))
          if (compareExpr(next1, next2)) {
            found = true;
            break;
          }
      }
      if (!found) {
        System.err
            .println("compareExprCall: expr.args (" + next1 + ") not found in " + expr2.args + ".");
        return false;
      }
    }
    // check all expr2.args are in expr1.args
    for (Expr next2 : expr2.args) {
      found = false;
      for (Expr next1 : expr1.args) {
        visitedExpressions.clear();
        if (compareAsString(next1, next2))
          if (compareExpr(next1, next2)) {
            found = true;
            break;
          }
      }
      if (!found) {
        System.err
            .println("compareExprCall: expr.args (" + next2 + ") not found in " + expr1.args + ".");
        return false;
      }
    }


    if (expr1.weight != expr2.weight) {
      System.err.println(
          "compareExprCall: different weight. (" + expr1.weight + " vs. " + expr2.weight + ")");
      return false;
    }

    if (!compareFunctions(expr1.fun, expr2.fun)) {
      System.err.println("compareExprCall: !compareFunctions(expr1.fun, expr2.fun) (" + expr1.fun
          + " vs. " + expr2.fun + ")");
      return false;
    }
    return true;
  }

  private boolean compareExprConstant(ExprConstant expr1, ExprConstant expr2) {
    if (expr1 == null && expr2 == null) {
      System.err.println(
          "compareExprConstant expr1 == null && expr2 == null (" + expr1 + " vs. " + expr2 + ")");
      return true;
    }

    if (expr1 == null && expr2 != null) {
      System.err.println(
          "compareExprConstant expr1 == null && expr2 != null (" + expr1 + " vs. " + expr2 + ")");
      return false;
    }
    if (expr1 != null && expr2 == null) {
      System.err.println(
          "compareExprConstant expr1 != null && expr2 == null (" + expr1 + " vs. " + expr2 + ")");
      return false;
    }

    if (expr1.op != expr2.op) {
      System.err
          .println("ExprConstant: expr1.op != expr2.op (" + expr1.op + " vs. " + expr2.op + ")");
      return false;
    }

    if (expr1.num != expr2.num) {
      System.err.println(
          "ExprConstant: expr1.num != expr2.num (" + expr1.num + " vs. " + expr2.num + ")");
      return false;
    }

    if (!expr1.string.equals(expr2.string)) {
      System.err.println("ExprConstant: expr1.string != expr2.string (" + expr1.string + " vs. "
          + expr2.string + ")");
      return false;
    }
    return true;
  }

  private boolean compareExprITE(ExprITE expr1, ExprITE expr2) {
    if (expr1 == null && expr2 == null) {
      System.err.println(
          "compareExprITE: expr1 == null && expr2 == null (" + expr1 + " vs. " + expr2 + ")");
      return true;
    }
    if (expr1 == null || expr2 == null) {
      System.err.println(
          "compareExprITE: expr1 != null || expr2 != null (" + expr1 + " vs. " + expr2 + ")");
      return false;
    }
    if (!compareExpr(expr1.cond, expr2.cond)) {
      System.err.println("compareExprITE: !compareExpr(expr1.cond, expr2.cond) (" + expr1.cond
          + " vs. " + expr2.cond + ")");
      return false;
    }
    if (!compareExpr(expr1.left, expr2.left)) {
      System.err.println("compareExprITE: !compareExpr(expr1.left, expr2.left) (" + expr1.left
          + " vs. " + expr2.left + ")");
      return false;
    }
    if (!compareExpr(expr1.right, expr2.right)) {
      System.err.println("compareExprITE: !compareExpr(expr1.right, expr2.right) (" + expr1.right
          + " vs. " + expr2.right + ")");
      return false;
    }
    return true;
  }


  private boolean compareExprList(ExprList expr1, ExprList expr2) {
    if (expr1 == null && expr2 == null) {
      return true;
    }
    if (expr1 != null && expr2 == null) {
      System.err.println(
          "compareExprList: " + "expr1 != null && expr2 == null (" + expr1 + " vs. " + expr2 + ")");
      return false;
    }
    if (expr1 == null && expr2 != null) {
      System.err.println(
          "compareExprList: " + "expr1 == null && expr2 != null (" + expr1 + " vs. " + expr2 + ")");
      return false;
    }

    if (expr1.op != expr2.op) {
      System.err
          .println("compareExprList: expr1.op != expr2.op (" + expr1.op + " vs. " + expr2.op + ")");
      return false;
    }

    if (expr1.args.size() != expr2.args.size()) {
      System.err.println("compareExprList: " + "expr1.args.size() != expr2.args.size() ("
          + expr1.args.size() + " vs. " + expr2.args.size() + ")");
      return false;
    }
    // for each expr1.arg
    for (int i = 0; i < expr1.args.size(); i++) {
      boolean found = false;
      for (int j = 0; j < expr2.args.size(); j++) {
        // first compare as string. Both could be the same as string like (all x | no x .o/inputs)
        // but one may be contained in sig A and the other may be contained in sig ParameterBehavior
        // visitedExpressions.clear();
        if (compareAsString(expr1.args.get(i), expr2.args.get(j))) {
          if (!compareExpr(expr1.args.get(i), expr2.args.get(j))) {
            System.err.println("compareExprList: expr1.args != expr2.args (" + expr1.args.get(i)
                + " vs. " + expr2.args.get(j) + ")");
            found = false;
          } else {
            found = true;
            break;
          }
        }
      } // end of j loop
      if (!found) {
        System.err.println(expr1.args.get(i) + " not found in " + expr2.args);
        return false;
      }
    }
    return true;
  }

  private boolean compareAsString(Expr expr1, Expr expr2) {
    String s1 = expr1.toString().replaceAll("this/", "").replaceAll("o/r/", "r/");
    String s2 = expr2.toString().replaceAll("this/", "").replaceAll("o/r/", "r/");

    if (s1.equals(s2)) {
      return true;
    } else {
      // o/r/acyclic[o/items, o/Transfer]
      // r/acyclic[o/items, o/Transfer]
      return false;
    }
  }

  private boolean compareAsString(Decl d1, Decl d2) {
    String s1 = d1.names.toString().replaceAll("this/", "").replaceAll("o/r/", "r/");
    String s2 = d2.names.toString().replaceAll("this/", "").replaceAll("o/r/", "r/");
    if (s1.compareTo(s2) == 0) {
      return true;
    } else {
      // o/r/acyclic[o/items, o/Transfer]
      // r/acyclic[o/items, o/Transfer]
      return false;
    }
  }

  private boolean compareExprQt(ExprQt expr1, ExprQt expr2) {
    if (expr1 == null && expr2 == null) {
      return true;
    }

    if (expr1 == null && expr2 != null) {
      System.err.println("compareExprQt: expr1 == null && expr2 != null");
      return false;
    }
    if (expr1 != null && expr2 == null) {
      System.err.println("compareExprQt: expr1 != null && expr2 == null");
      return false;
    }

    if (expr1.op != expr2.op) {
      System.err.println("compareExprQt: expr1.op(" + expr1.op + ") != expr2.op(" + expr2.op + ")");
      return false;
    }

    if (expr1.decls.size() != expr2.decls.size()) {
      System.err.println("compareExprQt: expr1.decls.size() != expr2.decls.size()");
      return false;
    }
    boolean found;
    // checking all expr1.decls in expr2.decls
    for (Decl d1 : expr1.decls) {
      found = false;
      for (Decl d2 : expr2.decls) {
        visitedExpressions.clear();
        if (compareAsString(d1, d2))
          if (compareDecl(d1, d2)) {
            found = true;
            break;
          }
        if (!found) {
          System.err.println("compareExprQt: " + d1 + "not in " + expr2.decls);
          return false;
        }
      }
    }



    if (!compareExpr(expr1.sub, expr2.sub)) {
      System.err.println(
          "compareExprQt: !compareExpr(expr1.sub, expr2.sub) " + expr1.sub + " vs. " + expr2.sub);
      return false;
    }
    return true;
  }

  private boolean compareExprUnary(ExprUnary expr1, ExprUnary expr2) {

    if (expr1 == null && expr2 == null) {
      return true;
    }

    if (expr1 != null && expr2 == null) {
      System.err.println("compareExprUnary: expr1 != null && expr2 == null");
      return false;
    }
    if (expr1 == null && expr2 != null) {
      System.err.println("compareExprUnary: expr1 == null && expr2 != null");
      return false;
    }

    if (expr1.op != expr2.op) {
      System.err.println("compareExprUnary: expr1.op != expr2.op " + expr1.op + " vs. " + expr2.op);
      return false;
    }
    if (!compareExpr(expr1.sub, expr2.sub)) {
      System.err.println("compareExprUnary: !compareExpr(expr1.sub, expr2.sub) " + expr1.sub
          + " vs. " + expr2.sub);
      return false;
    }
    return true;
  }

  private boolean compareExprVar(ExprVar expr1, ExprVar expr2) {
    if (expr1 == null && expr2 == null) {
      return true;
    }
    if (expr1 == null && expr2 != null) {
      System.err.println("compareExprVar: expr1 == null && expr2 != null");
      return false;
    }
    if (expr1 != null && expr2 == null) {
      System.err.println("compareExprVar: expr1 != null && expr2 == null");
      return false;
    }
    // expr.label is like "this" or "x" for comparing fact like "(all x | # x .
    // (this/MultipleControlFlow <: p1) = 2)"
    if (!expr1.label.equals(expr2.label)) {
      System.err.println("compareExprVar: expr1.label != expr2.label (" + expr1.label + " vs. "
          + expr2.label + ")");
      return false;
    }
    if (!compareType(expr1.type(), expr2.type())) {
      System.err.println(
          "compareExprVar: expr1.type() != expr2.type() " + expr1.type() + " vs. " + expr2.type());
      return false;
    }
    return true;
  }

  private boolean compareFunctions(Func func1, Func func2) {
    if (func1 == null && func2 == null) {
      System.err.println(
          "compareFunctions: func1 == null && func2 == null (" + func1 + " vs. " + func2 + ")");
      return true;
    }
    if (func1 == null || func2 == null) {
      System.err.println(
          "compareFunctions: func1 == null || func2 == null (" + func1 + " vs. " + func2 + ")");
      return false;
    }

    if (func1.decls.size() != func2.decls.size()) {
      System.err.println("compareFunction: " + "func1.decls.size() != func2.decls.size() ("
          + func1.decls.size() + " vs. " + func2.decls.size() + ")");
      return false;
    }
    // check all func1.decls in func2.decls
    for (int i = 0; i < func1.decls.size(); i++) {
      for (int j = 0; j < func2.decls.size(); j++) {
        visitedExpressions.clear();
        if (compareAsString(func1.decls.get(i), func2.decls.get(j))) {
          if (!compareDecl(func1.decls.get(i), func2.decls.get(j))) {
            System.err.println("compareFunction: !compareDecl (func.decls). " + func1.decls.get(i)
                + " and " + func2.decls.get(j) + "has a same string but different as Decl");
            return false;
          }
        }
      }
    }


    if (func1.isPred != func2.isPred) {
      System.err.println(
          "compareFunction: func1.isPred != func2.isPred " + func1.isPred + " vs. " + func2.isPred);
      return false;
    }
    if (func1.isPrivate == null && func2.isPrivate != null) {

      System.err
          .println("compareFunctions: " + "func1.isPrivate == null && func2.isPrivate != null");
      return false;
    }
    if (func1.isPrivate != null && func2.isPrivate == null) {
      System.err
          .println("compareFunctions: " + "func1.isPrivate != null && func2.isPrivate == null");
      return false;
    }
    if (!AlloyUtils.removeSlash(func1.label).equals(AlloyUtils.removeSlash(func2.label))) {
      System.err
          .println("compareFunctions: label is different. (" + AlloyUtils.removeSlash(func1.label)
              + " vs. " + AlloyUtils.removeSlash(func2.label) + ")");
      System.err.println();
      return false;
    }
    if (!compareExpr(func1.returnDecl, func2.returnDecl)) {
      System.err.println("compareFunctions: " + "!compareExpr(func1.returnDecl, func2.returnDecl) "
          + func1.returnDecl + " vs. " + func2.returnDecl);
      return false;
    }
    if (!compareExpr(func1.getBody(), func2.getBody())) {
      System.err.println("!compareExpr(func1.getBody(), func2.getBody()) " + func1.getBody()
          + " vs. " + func2.getBody());
      return false;
    }
    return true;
  }

  private boolean comparePrimSig(Sig.PrimSig primSig1, Sig.PrimSig primSig2) {
    if (primSig1.label.equals("univ") && primSig2.label.equals("univ")) {
      return true;
    }

    if (primSig1.children().size() != primSig2.children().size()) {
      System.err.println("comparePrimSig: primSig1(" + primSig1 + ").children().size()("
          + primSig1.children().size() + ") != primSig2(" + primSig2 + ").children().size() ("
          + +primSig2.children().size() + ")");
      return false;
    }
    return true;
  }

  private boolean compareSig(Sig sig1, Sig sig2) {

    if (sig1 == null && sig2 == null) {
      System.err.println("compareSig: sig1 == null && sig2 == null");
      return true;
    }
    if (sig1 == null && sig2 != null) {
      System.err
          .println("compareSig: sig1 == null && sig2 != null (" + sig1 + " vs. " + sig2 + ")");
      return false;
    }
    if (sig1 != null && sig2 == null) {
      System.err.println("compareSig: sig1 != null && sig2 == null(" + sig1 + " vs. " + sig2 + ")");
      return false;
    }

    if (sig1 instanceof Sig.PrimSig && sig2 instanceof Sig.PrimSig
        && !comparePrimSig((Sig.PrimSig) sig1, (Sig.PrimSig) sig2)) {
      return false;
    }

    if (sig1.attributes.size() != sig2.attributes.size()) {
      System.err.println("compareSig: sig1.attributes.size() != sig2.attributes.size() ("
          + sig1.attributes.size() + " vs. " + sig2.attributes.size() + ")");
      return false;
    }

    // sig.attributes are [0] where, [1]...[6] null, [7]..[9] subsig
    // order of sig1.attributes and sig2.attributes are the same
    for (int i = 0; i < sig1.attributes.size(); i++) {
      if (sig1.attributes.get(i) == null && sig2.attributes.get(i) == null)
        continue;
      else if (!sig1.attributes.get(i).toString().equals(sig2.attributes.get(i).toString())) {
        System.err
            .println("compareSig: !compareAttr(sig1.attributes.get(i), sig2.attributes.get(i))"
                + sig1.attributes.get(i) + " vs. " + sig2.attributes.get(i));
        return false;
      }
    }

    if (sig1.builtin != sig2.builtin) {
      System.err.println(
          "compareSig: sig1.builtin != sig2.builtin" + sig1.builtin + " vs. " + sig2.builtin);
      return false;
    }
    if (!compareDecl(sig1.decl, sig2.decl)) {
      System.err.println("compareSig:!compareDecl(sig1.decl, sig2.decl)");
      return false;
    }

    if (sig1.isAbstract == null && sig2.isAbstract != null) {
      System.err.println("compareSig: sig1.isAbstract == null && sig2.isAbstract != null");
      return false;
    }
    if (sig1.isAbstract != null && sig2.isAbstract == null) {
      System.err.println("compareSig: sig1.isAbstract != null && sig2.isAbstract == null");
      return false;
    }
    if (sig1.isEnum == null && sig2.isEnum != null) {
      System.err.println("compareSig: sig1.isEnum == null && sig2.isEnum != null");
      return false;
    }
    if (sig1.isEnum != null && sig2.isEnum == null) {
      System.err.println("compareSig: sig1.isEnum != null && sig2.isEnum == null");
      return false;
    }
    if (sig1.isLone == null && sig2.isLone != null) {
      System.err.println("compareSig: sig1.isLone == null && sig2.isLone != null");
      return false;
    }
    if (sig1.isLone != null && sig2.isLone == null) {
      System.err.println("compareSig: sig1.isLone != null && sig2.isLone == null");
      return false;
    }
    if (sig1.isMeta == null && sig2.isMeta != null) {
      System.err.println("compareSig: sig1.isMeta == null && sig2.isMeta != null");
      return false;
    }
    if (sig1.isMeta != null && sig2.isMeta == null) {
      System.err.println("compareSig: sig1.isMeta != null && sig2.isMeta == null");
      return false;
    }
    if (sig1.isOne == null && sig2.isOne != null) {
      System.err.println("compareSig: sig1.isOne == null && sig2.isOne != null");
      return false;
    }
    if (sig1.isOne != null && sig2.isOne == null) {
      System.err.println("compareSig: sig1.isOne != null && sig2.isOne == null");
      return false;
    }
    if (sig1.isPrivate == null && sig2.isPrivate != null) {
      System.err.println("compareSig: sig1.isPrivate == null && sig2.isPrivate != null");
      return false;
    }
    if (sig1.isPrivate != null && sig2.isPrivate == null) {
      System.err.println("compareSig: sig1.isPrivate != null && sig2.isPrivate == null");
      return false;
    }
    if (sig1.isSome == null && sig2.isSome != null) {
      System.err.println("compareSig: sig1.isSome == null && sig2.isSome != null");
      return false;
    }
    if (sig1.isSome != null && sig2.isSome == null) {
      System.err.println("compareSig: sig1.isSome != null && sig2.isSome == null");
      return false;
    }
    if (sig1.isSubset == null && sig2.isSubset != null) {
      System.err.println("compareSig: sig1.isSubset == null && sig2.isSubset != null");
      return false;
    }
    if (sig1.isSubset != null && sig2.isSubset == null) {
      System.err.println("compareSig: sig1.isSubset != null && sig2.isSubset == null");
      return false;
    }
    if (sig1.isSubsig == null && sig2.isSubsig != null) {
      System.err.println("compareSig: sig1.isSubsig == null && sig2.isSubsig != null");
      return false;
    }
    if (sig1.isSubsig != null && sig2.isSubsig == null) {
      System.err.println("compareSig: sig1.isSubsig != null && sig2.isSubsig == null");
      return false;
    }
    if (!AlloyUtils.removeSlash(sig1.label).equals(AlloyUtils.removeSlash(sig2.label))) {
      System.err.println("compareSig: !sig1.label.equals(sig2.label) "
          + AlloyUtils.removeSlash(sig1.label) + " vs. " + AlloyUtils.removeSlash(sig2.label));
      return false;
    }
    if (sig1.getDepth() != sig2.getDepth()) {
      System.err.println("compareSig: sig1.getDepth() != sig2.getDepth() " + sig1.getDepth()
          + " vs. " + sig2.getDepth());
      return false;
    }

    if (sig1.getFacts().size() != sig2.getFacts().size()) {
      System.err.println("compareSig: sig1.getFacts().size() != sig2.getFacts().size() ("
          + sig1.getFacts().size() + " vs. " + sig2.getFacts().size() + ")");
      return false;
    }

    // check all sig1.facts are in sig2.facts - not necessary check separately
    boolean found;
    if (sig1.getFieldDecls().size() != sig2.getFieldDecls().size()) {
      System.err.println("compareSig: sig1.getFieldDecls().size() != sig2.getFieldDecls().size() ("
          + sig1.getFieldDecls().size() + " vs. " + sig2.getFieldDecls().size() + ")");
      return false;
    }

    for (Decl decl1 : sig1.getFieldDecls()) {
      found = false;
      for (Decl decl2 : sig2.getFieldDecls()) {
        visitedExpressions.clear();
        if (compareDecl(decl1, decl2)) { // compare fields each.
          found = true;
          break;
        }
      }
      if (!found) {
        System.err
            .println("compareSig: fieldDecls " + decl1 + " not found in " + sig2.getFieldDecls());
        return false;
      }
    }


    if (sig1.getFields().size() != sig2.getFields().size()) {
      System.err.println("compareSig: sig1.getFields().size() != sig2.getFields().size() ("
          + sig1.getFields().size() + " vs. " + sig2.getFields().size() + ")");
      return false;
    }
    // check all sig1.fields are in sig2.fields
    for (Sig.Field f1 : sig1.getFields()) {
      found = false;
      for (Sig.Field f2 : sig2.getFields()) {
        visitedExpressions.clear();
        if (compareAsString(f1, f2)) {
          if (compareSigField(f1, f2)) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        System.err.println(
            "compareSig: !compareSig.Field failed. " + f1 + " not found in " + sig2.getFields());
        return false;
      }
    }

    // getHTML() not implemented
    // getSubnodes() not implemented

    if (sig1.isTopLevel() != sig2.isTopLevel()) {
      System.err.println("compareSig: sig1.isTopLevel() != sig2.isTopLevel() (" + sig1.isTopLevel()
          + " vs. " + sig2.isTopLevel() + ")");
      return false;
    }
    if (!AlloyUtils.removeSlash(sig1.toString()).equals(AlloyUtils.removeSlash(sig2.toString()))) {
      System.err.println("compareSig: !sig1.toString().equals(sig2.toString()) ("
          + AlloyUtils.removeSlash(sig1.toString()) + " vs. "
          + AlloyUtils.removeSlash(sig2.toString()) + ")");
      return false;
    }
    return true; // CompareSig
  }

  private boolean compareSigField(Sig.Field sig1Field, Sig.Field sig2Field) {
    if (sig1Field == null && sig2Field == null) {
      System.err.println(
          "sig1Field == null && sig2Field == null (" + sig1Field + " vs. " + sig2Field + ")");
      return true;
    }

    if (sig1Field == null && sig2Field != null) {
      System.err.println(
          "sig1Field == null && sig2Field != null (" + sig1Field + " vs. " + sig2Field + ")");
    } else if (sig1Field != null && sig2Field == null) {
      System.err.println(
          "sig1Field != null && sig2Field == null (" + sig1Field + " vs. " + sig2Field + ")");
    }

    if (!AlloyUtils.removeSlash(sig1Field.label).equals(AlloyUtils.removeSlash(sig2Field.label))) {
      System.err
          .println("Sig.Field: sig1.label != sig2.label (" + AlloyUtils.removeSlash(sig1Field.label)
              + " vs. " + AlloyUtils.removeSlash(sig2Field.label) + ")");
      return false;
    }

    if (sig1Field.defined != sig2Field.defined) {
      System.err.println("Sig.Field: sig1.defined != sig2.defined (" + sig1Field.defined + " vs. "
          + sig2Field.defined + ")");
      return false;
    }

    if ((sig1Field.isMeta == null && sig2Field.isMeta != null)
        || sig1Field.isMeta != null && sig2Field.isMeta == null) {
      System.err.println(
          "Sig.Field: isMeta different (" + sig1Field.isMeta + " vs. " + sig2Field.isMeta + ")");
      return false;
    }

    if ((sig1Field.isPrivate == null && sig2Field.isPrivate != null)
        || sig1Field.isPrivate != null && sig2Field.isPrivate == null) {
      System.err.println("Sig.Field: isPrivate different (" + sig1Field.isPrivate + " vs. "
          + sig2Field.isPrivate + ")");
      return false;
    }
    return true;
  }

  private boolean compareType(Type t1, Type t2) {
    if (t1 == null && t2 == null) {
      return true;
    }
    if (t1 == null || t2 == null) {
      return false;
    }
    if (t1.is_bool != t2.is_bool) {
      System.err.println("compareType: t1.is_bool != t2.is_bool");
      return false;
    }
    if (t1.is_int() != t2.is_int()) {
      System.err.println("compareType: t1.is_int() != t2.is_int()");
      return false;
    }
    if (t1.is_small_int() != t2.is_small_int()) {
      System.err.println("compareType: t1.is_small_int() != t2.is_small_int()");
      return false;
    }
    if (t1.arity() != t2.arity()) {
      System.err.println("compareType: t1.arity() != t2.arity()");
      return false;
    }
    if (t1.size() != t2.size()) {
      System.err.println("compareType: t1.size() != t2.size()");
      return false;
    }
    if (t1.hasNoTuple() != t2.hasNoTuple()) {
      System.err.println("compareType: t1.hasNoTuple() != t2.hasNoTuple()");
      return false;
    }
    if (t1.hasTuple() != t2.hasTuple()) {
      System.err.println("compareType: t1.hasTuple() != t2.hasTuple()");
      return false;
    }

    return true;
  }

  // util methods
  /**
   * sort ExprList of Expr in alphabetical order using expr.toString() method return is List of List<Expr> because some fact's have the same toString() even its belong to different Signatures (ie.,
   * inputs.x)
   * 
   * @param e1 ExprList
   * @return List<List<Expr>> List of List <Expr.toString()>
   */
  private static List<List<Expr>> sortExprList(ExprList e1) {
    List<String> keys = new ArrayList<>();
    // key = expr.toString(), value is List<Expr> because the same expr.toString() may exists for
    // different sigs (ie., no inputs.x)
    Map<String, List<Expr>> sortedMap = new HashMap<>();
    for (int i = 0; i < e1.args.size(); i++) {
      Expr expr = e1.args.get(i);
      String key = expr.toString();
      if (!sortedMap.containsKey(key)) {
        keys.add(key);
        sortedMap.put(key, new ArrayList<Expr>(Arrays.asList(expr)));
      } else {
        List<Expr> l = sortedMap.get(key);
        l.add(expr);
        sortedMap.put(key, l);
      }
    }
    // sort keys
    Collections.sort(keys);

    List<List<Expr>> sortedList = new ArrayList<List<Expr>>();
    for (String key : keys)
      sortedList.add(sortedMap.get(key));

    return sortedList;
  }

}

