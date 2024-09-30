package obmtest;

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
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.mit.csail.sdg.ast.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Comparator for two alloy objects.
 *
 * @author Shinjo Andrew H, GIRI Intern, Georgia Tech
 * @author Wilson Miyako, ASDL, Georgia Tech
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
   * @param _signature1(Sig) - a Signatures to be compared
   * @param _signature2(Sig) - a Signatures to be compared
   * @return (boolean) - true if they are considered as the same, otherwise return false
   */
  public boolean compareTwoExpressionsSignatures(Sig _signature1, Sig _signature2) {
    visitedExpressions.clear();
    return compareExpr(_signature1, _signature2);
  }

  /**
   * Compare two Exprs(facts) assuming the givens are ExprList
   *
   * @param _expression1(Expr) - an Expr containing one or more fact expressions
   * @param _expression2(Expr) - an Expr containing one or more fact expressions
   * @return (boolean) - true if they are considered as the same, otherwise return false
   */
  public boolean compareTwoExpressionsFacts(Expr _expression1, Expr _expression2) {
    visitedExpressions.clear();

    // compare facts size
    int size1 = ((Collection<?>) ((ExprList) _expression1).args).size();
    int size2 = ((Collection<?>) ((ExprList) _expression2).args).size();

    if (size1 != size2) {
      System.err.println("The size of facts is different. " + size1 + " vs. " + size2);
      return false;
    }
    // e1.args.size() and ee1.size() are different. ee1 can contain like [(all x | no o/inputs . x),
    // (all x | no o/inputs . x)] the two expr of the same
    // Expressions as string are the same but what x represents(Signature) are different. For
    // example,
    // fact {all x: AtomicBehavior | no inputs.x} vs.
    // fact {all x: MultipleControlFlow | no inputs.x}
    List<List<Expr>> ee1 = sortExprList((ExprList) _expression1);
    List<List<Expr>> ee2 = sortExprList((ExprList) _expression2);

    if (ee1.size() != ee2.size()) {
      System.err.println(
          "The size of sorted grouped facts are different. " + ee1.size() + " vs. " + ee2.size());
      return false;
    }

    for (int i = 0; i < ee1.size(); i++) {
      if (ee1.get(i).size() != ee2.get(i).size()) {
        System.err.println(
            "The size of sorted grouped fact is different. "
                + ee1.get(i)
                + "("
                + ee1.get(i).size()
                + ") vs. "
                + ee2.get(i)
                + "("
                + ee2.get(i).size()
                + ")");
        return false;
      }

      // make sure each expression in ee1.get(i) and ee2.get(i) are the same - expression is string
      // and which signature the expression is for.
      Set<Integer> expr2usedJIndex = new HashSet<>();
      for (Expr expr1 : ee1.get(i)) {
        boolean expr1found = false;
        for (int j = 0; j < ee2.get(i).size(); j++) {
          if (!expr2usedJIndex.contains(j)) {
            Expr expr2 = ee2.get(i).get(j);
            if (!compareExpr(expr1, expr2)) continue;
            else {
              expr1found = true;
              expr2usedJIndex.add(j);
              break;
            }
          }
        }
        if (!expr1found) return false;
      }
    }
    return true;
  }

  /**
   * Compare two Commands
   *
   * @param _command1(Command) - a Command to be compared
   * @param _command2(Command) - a Command to be compared
   * @return (boolean) true if considered as the same, otherwise false
   */
  private boolean compareCommand(Command _command1, Command _command2) {

    if (_command1 == null && _command2 == null) return true;

    if (_command1 == null || _command2 == null) {
      System.err.println("compareCommand: c1 == null || c2 == null");
      return false;
    }

    // ConstList<Sig> additionalExactScopes
    if (_command1.additionalExactScopes.size() != _command2.additionalExactScopes.size()) {
      System.err.println(
          "compareCommand: "
              + "c1.additionalExactScopes.size() != "
              + "c2.additionalExactScopes.size() ("
              + _command1.additionalExactScopes.size()
              + " vs. "
              + _command2.additionalExactScopes.size()
              + ")");
      return false;
    }

    for (int i = 0; i < _command1.additionalExactScopes.size(); i++) {
      Sig s1 = _command1.additionalExactScopes.get(i);
      Sig s2 = _command2.additionalExactScopes.get(i);
      if (!compareSig(s1, s2)) {
        System.err.println("compareCommand: !compareSig(s1, s2) (" + s1 + " vs." + s2 + ")");
        return false;
      }
    }

    // int bitwidth
    if (_command1.bitwidth != _command2.bitwidth) {
      System.err.println(
          "compareCommand: c1.bitwidth != c2.bitwidth ("
              + _command1.bitwidth
              + " vs. "
              + _command2.bitwidth
              + ")");
      return false;
    }

    // boolean check
    if (_command1.check != _command2.check) {
      System.err.println(
          "compareCommand: c1.check != c2.check ("
              + _command1.check
              + " vs. "
              + _command2.check
              + ")");
      return false;
    }

    // int expects
    if (_command1.expects != _command2.expects) {
      System.err.println(
          "compareCommand: c1.expects != c2.expects ("
              + _command1.expects
              + " vs. "
              + _command2.expects
              + ")");
      return false;
    }

    // Expr formula
    if (!compareExpr(_command1.formula, _command2.formula)) {
      System.err.println(
          "compareCommand: "
              + "!compareExpr(c1.formula, c2.formula) ("
              + _command1.formula
              + " vs. "
              + _command2.formula
              + ")");
      return false;
    }

    // String label
    if (!_command1.label.equals(_command2.label)) {
      System.err.println(
          "compareCommand: !c1.label.equals(c2.label) ("
              + _command1.label
              + " vs. "
              + _command2.label
              + ")");
      return false;
    }

    // int maxseq
    if (_command1.maxseq != _command2.maxseq) {
      System.err.println(
          "compareCommand: c1.maxseq != c2.maxseq ("
              + _command1.maxseq
              + " vs. "
              + _command2.maxseq
              + ")");
      return false;
    }
    // int maxstring
    if (_command1.maxstring != _command2.maxstring) {
      System.err.println(
          "compareCommand: c1.maxstring != c2.maxstring ( "
              + _command1.maxstring
              + " vs. "
              + _command2.maxstring
              + ")");
      return false;
    }
    // int overall
    if (_command1.overall != _command2.overall) {
      System.err.println(
          "compareCommand: c1.overall != c2.overall ("
              + _command1.overall
              + " vs. "
              + _command2.overall
              + ")");
      return false;
    }
    // Command parent
    if (!compareCommand(_command1.parent, _command2.parent)) {
      System.err.println(
          "compareCommand: "
              + "!compareCommand(c1.parent, c2.parent) ("
              + _command1.parent
              + " vs. "
              + _command2.parent
              + ")");
      return false;
    }
    // Pos pos (ignore)

    // ConstList<CommandScope> scope
    if (_command1.scope.size() != _command2.scope.size()) {
      System.err.println(
          "compareCommand: "
              + "c1.scope.size() != c2.scope.size() ("
              + _command1.scope.size()
              + " vs. "
              + _command2.scope.size()
              + ")");
      return false;
    }

    for (int i = 0; i < _command1.scope.size(); i++) {
      CommandScope cs1 = _command1.scope.get(i);
      CommandScope cs2 = _command2.scope.get(i);

      if (!compareCommandScope(cs1, cs2)) {
        System.err.println("compareCommand: !compareCommandScope(cs1, cs2)");
      }
    }
    return true;
  }

  /**
   * Compare two CommandScopes
   *
   * @param _commandscope1(CommandScope) - a CommandScope to be compared
   * @param _commandscope2(CommandScope) - a CommandScope to be compared
   * @return (boolean) true if considered as the same, otherwise false
   */
  private boolean compareCommandScope(CommandScope _commandscope1, CommandScope _commandscope2) {

    if (_commandscope1 == null && _commandscope2 == null) {
      System.err.println("compareCommandScope: cs1 == null && cs2 == null");
      return true;
    }

    if (_commandscope1 == null || _commandscope2 == null) {
      System.err.println("compareCommandScope: cs1 == null || cs2 == null");
      return false;
    }

    // int endingScope
    if (_commandscope1.endingScope != _commandscope2.endingScope) {
      System.err.println(
          "compareCommandScope: "
              + "cs1.endingScope != cs2.endingScope ("
              + _commandscope1.endingScope
              + " vs. "
              + _commandscope2.endingScope
              + ")");
      return false;
    }
    // int increment
    if (_commandscope1.endingScope != _commandscope2.endingScope) {
      System.err.println(
          "compareCommandScope: "
              + "cs1.increment != cs2.increment ("
              + _commandscope1.increment
              + " vs. "
              + _commandscope2.increment
              + ")");
      return false;
    }

    // boolean isExact
    if (_commandscope1.isExact != _commandscope2.isExact) {
      System.err.println(
          "compareCommandScope: "
              + "cs1.isExact != cs2.isExact ("
              + _commandscope1.isExact
              + " vs. "
              + _commandscope2.isExact
              + ")");
      return false;
    }
    // Pos pos (ignored)

    // Sig sig
    if (!compareSig(_commandscope1.sig, _commandscope2.sig)) {
      System.err.println("compareCommandScope: " + "!compareSig(cs1.sig, cs2.sig)");
      return false;
    }

    // int startingScope
    if (_commandscope1.startingScope != _commandscope2.startingScope) {
      System.err.println(
          "compareCommandScope: "
              + "cs1.startingScope != cs2.startingScope ("
              + _commandscope1.startingScope
              + " vs. "
              + _commandscope2.startingScope
              + ")");
      return false;
    }
    return true;
  }

  /**
   * Compare two Decls
   *
   * @param _decl1(Decl) - a Decl to be compared
   * @param _decl2(Decl) - a Decl to be compared
   * @return (boolean) true if considered as the same, otherwise false
   */
  private boolean compareDecl(Decl _decl1, Decl _decl2) {

    if (_decl1 == null && _decl2 == null) {
      System.err.println(
          "CompareDecls: d1 == null && d2 == null (" + _decl1 + " vs. " + _decl2 + ")");
      return true;
    }

    if (_decl1 == null && _decl2 != null) {
      System.err.println(
          "CompareDecls: d1 == null && d2 != null(" + _decl1 + " vs. " + _decl2 + ")");
      return false;
    }
    if (_decl1 != null && _decl2 == null) {
      System.err.println(
          "CompareDecls: d1 != null && d2 == null (" + _decl1 + " vs. " + _decl2 + ")");
      return false;
    }

    if (_decl1.disjoint == null && _decl2.disjoint != null) {
      System.err.println(
          "compareDecls: d1.disjoint == null && d2.disjoint != null ("
              + _decl1
              + " vs. "
              + _decl2
              + ")");
      return false;
    }
    if (_decl1.disjoint != null && _decl2.disjoint == null) {
      System.err.println(
          "compareDecls: d1.disjoint != null && d2.disjoint == null ("
              + _decl1
              + " vs. "
              + _decl2
              + ")");
      return false;
    }

    if (_decl1.disjoint2 == null && _decl2.disjoint2 != null) {
      System.err.println(
          "compareDecls: d1.disjoint2 == null && d2.disjoint2 != null ("
              + _decl1
              + " vs. "
              + _decl2
              + ")");
      return false;
    }
    if (_decl1.disjoint2 != null && _decl2.disjoint2 == null) {
      System.err.println(
          "compareDecls: d1.disjoint2 != null && d2.disjoint2 == null ("
              + _decl1
              + " vs. "
              + _decl2
              + ")");
      return false;
    }
    if (!compareExpr(_decl1.expr, _decl2.expr)) {
      System.err.println(
          "compareDecl: !compareExpr(d1.expr, d2.expr) ("
              + _decl1.expr
              + " vs. "
              + _decl2.expr
              + ")");
      return false;
    }

    if (_decl1.isPrivate == null && _decl2.isPrivate != null) {
      System.err.println(
          "compareDecls: d1.isPrivate == null && d2.isPrivate != null ("
              + _decl1.isPrivate
              + " vs. "
              + _decl2.isPrivate
              + ")");
      return false;
    }
    if (_decl1.isPrivate != null && _decl2.isPrivate == null) {
      System.err.println(
          "compareDecls: d1.isPrivate != null && d2.isPrivate == null"
              + _decl1.isPrivate
              + " vs. "
              + _decl2.isPrivate
              + ")");
      return false;
    }

    if (_decl1.names.size() != _decl2.names.size()) {
      System.err.println(
          "compareDecls: d1.names.size() != d2.names.size() ("
              + _decl1.names.size()
              + " vs. "
              + _decl2.names.size()
              + ")");
      return false;
    }

    // check all d1.names are in d2.names
    boolean found;
    for (ExprHasName name1 : _decl1.names) {
      found = false;
      for (ExprHasName name2 : _decl2.names) {
        visitedExpressions.clear();
        if (compareExprAsString(name1, name2)) {
          if (compareExpr(name1, name2)) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        System.err.println(
            "compareDecls: decl1.name is not in decl2.names ("
                + name1
                + " vs."
                + _decl2.names
                + ")");
        return false;
      }
    }
    // check all d2.names are in d1.names
    for (ExprHasName name2 : _decl2.names) {
      found = false;
      for (ExprHasName name1 : _decl1.names) {
        visitedExpressions.clear();
        if (compareExprAsString(name1, name2)) {
          if (compareExpr(name1, name2)) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        System.err.println(
            "compareDecls: decl2.name is not in decl1.names ("
                + name2
                + " vs."
                + _decl1.names
                + ")");
        return false;
      }
    }

    return true;
  }

  /**
   * Compare two Exprs
   *
   * @param _expr1(Expr) - a Expr to be compared
   * @param _expr2(Expr) - a Expr to be compared
   * @return (boolean) true if considered as the same, otherwise false
   */
  private boolean compareExpr(Expr _expr1, Expr _expr2) {

    if (_expr1 == null && _expr2 == null) {
      return true;
    }

    if (_expr1 == null && _expr2 != null) {
      System.err.println(
          "compareExpr: expr1 == null && expr2 != null (" + _expr1 + " vs. " + _expr2 + ")");
      return false;
    }
    if (_expr1 != null && _expr2 == null) {
      System.err.println(
          "compareExpr: expr1 != null && expr2 == null" + _expr1 + " vs. " + _expr2 + ")");
      return false;
    }

    _expr1 = _expr1.deNOP();
    _expr2 = _expr2.deNOP();

    if (!_expr1.getClass().equals(_expr2.getClass())) {
      System.err.println(
          "expr1.getClass() != expr2.getClass() ("
              + _expr1.getClass()
              + " vs. "
              + _expr2.getClass()
              + ")");
      return false;
    }

    if (visitedExpressions.contains(List.of(_expr1, _expr2))) {
      System.out.println(_expr1 + " and " + _expr2 + " is already visited");
      return true;
    }
    visitedExpressions.add(List.of(_expr1, _expr2));

    if (_expr1.getClass().equals(Expr.class)) {
      System.err.println("CompareExpr Expr.class not implemented");
    } else if (_expr1.getClass().equals(ExprBinary.class)) {
      if (!compareExprBinary((ExprBinary) _expr1, (ExprBinary) _expr2)) {

        System.err.println(
            "compareExpr: !compareExprBinary((ExprBinary) expr1, (ExprBinary) expr2) ("
                + _expr1
                + " vs. "
                + _expr2
                + ")");
        return false;
      }
    } else if (_expr1.getClass().equals(ExprCall.class)) {
      if (!compareExprCall((ExprCall) _expr1, (ExprCall) _expr2)) {
        System.err.println(
            "compareExpr: "
                + "!compareExprCall((ExprCall) expr1, (ExprCall) expr2) ("
                + _expr1
                + " vs. "
                + _expr2
                + ")");
        return false;
      }
    } else if (_expr1.getClass().equals(ExprConstant.class)) {
      if (!compareExprConstant((ExprConstant) _expr1, (ExprConstant) _expr2)) {
        System.err.println(
            "compareExpr: !compareExprConstant((ExprConstant) expr1, (ExprConstant) expr2) ("
                + _expr1
                + " vs. "
                + _expr2
                + ")");
        return false;
      }
    } else if (_expr1.getClass().equals(ExprITE.class)) {
      if (!compareExprITE((ExprITE) _expr1, (ExprITE) _expr2)) {
        System.err.println(
            "compareExpr: !compareExprITE((ExprITE) expr1, (ExprITE) expr2) ("
                + _expr1
                + " vs. "
                + _expr2
                + ")");
        return false;
      }
    } else if (_expr1.getClass().equals(ExprLet.class)) {
      System.err.println("ExprLet: not implemented");
    } else if (_expr1.getClass().equals(ExprList.class)) {
      if (!compareExprList((ExprList) _expr1, (ExprList) _expr2)) {
        System.err.println(
            "compareExpr: "
                + "!compareExprList((ExprList) expr1, (ExprList) expr2) ("
                + _expr1
                + " vs. "
                + _expr2
                + ")");
        return false;
      }
    } else if (_expr1.getClass().equals(ExprQt.class)) {
      if (!compareExprQt((ExprQt) _expr1, (ExprQt) _expr2)) {
        System.err.println(
            "compareExpr: "
                + "!compareExprQt((ExprQt) expr1, (ExprQt) expr2) ("
                + _expr1
                + " vs. "
                + _expr2
                + ")");
        return false;
      }
    } else if (_expr1.getClass().equals(ExprUnary.class)) {
      if (!compareExprUnary((ExprUnary) _expr1, (ExprUnary) _expr2)) {
        System.err.println(
            "compareExpr: !compareExprUnary("
                + "(ExprUnary) expr1, (ExprUnary) expr2) "
                + _expr1
                + " vs. "
                + _expr2
                + "("
                + _expr1
                + " vs. "
                + _expr2
                + ")");
        return false;
      }
    } else if (_expr1.getClass().equals(ExprVar.class)) {
      if (!compareExprVar((ExprVar) _expr1, (ExprVar) _expr2)) {
        System.err.println(
            "compareExpr: !compareExprVar("
                + "(ExprVar) expr1, (ExprVar) expr2) ("
                + _expr1
                + " vs. "
                + _expr2
                + ")");
        return false;
      }
    } else if (_expr1.getClass().equals(Sig.Field.class)) {
      if (!compareSigField((Sig.Field) _expr1, (Sig.Field) _expr2)) {
        System.err.println(
            "compareExpr: !compareSigField("
                + "(Sig.Field) expr1, (Sig.Field) expr2) ("
                + _expr1
                + " vs."
                + _expr2
                + ")");
        System.err.println();
        return false;
      }
    } else if (_expr1.getClass().equals(Sig.class) || _expr1.getClass().equals(Sig.PrimSig.class)) {
      if (!compareSig((Sig) _expr1, (Sig) _expr2)) {
        System.err.println(
            "compareExpr: !compareSig("
                + "(Sig) expr1, (Sig) expr2) ("
                + _expr1
                + " vs."
                + _expr2
                + ")");
        System.err.println();
        return false;
      }
    } else {
      System.err.println("Unexpected class: " + _expr1.getClass());
      return false;
    }

    return true;
  }

  /**
   * Compare two ExprBinarys
   *
   * @param _exprbinary1(ExprBinary) - a ExprBinary to be compared
   * @param _exprbinary2(ExprBinary) - a ExprBinary to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareExprBinary(ExprBinary _exprbinary1, ExprBinary _exprbinary2) {
    if (_exprbinary1 == null && _exprbinary2 == null) {
      System.err.println("compareExprBinary: expr1 == null && expr2 == null");
      return true;
    }
    if (_exprbinary1 == null && _exprbinary2 != null) {
      System.err.println(
          "compareExprBinary: expr1 == null && expr2 != null ("
              + _exprbinary1
              + " vs. "
              + _exprbinary2
              + ")");
      return false;
    }
    if (_exprbinary1 != null && _exprbinary2 == null) {
      System.err.println(
          "compareExprBinary: expr1 != null && expr2 == null("
              + _exprbinary1
              + " vs. "
              + _exprbinary2
              + ")");
      return false;
    }
    if (!compareExpr(_exprbinary1.left, _exprbinary2.left)) {
      System.err.println(
          "ExprBinary: !compareExpr(expr1.left, expr2.left) ("
              + _exprbinary1.left
              + " vs. "
              + _exprbinary2.left
              + ")");
      return false;
    }

    if (_exprbinary1.op != _exprbinary2.op) {
      System.err.println(
          "ExprBinary: expr1.op != expr2.op (" + _exprbinary1.op + " vs. " + _exprbinary2.op + ")");
      return false;
    }
    if (!compareExpr(_exprbinary1.right, _exprbinary2.right)) {
      System.err.println(
          "ExprBinary: !compareExpr(expr1.right, expr2.right) ("
              + _exprbinary1.right
              + " vs. "
              + _exprbinary2.right
              + ")");
      return false;
    }
    return true;
  }

  /**
   * Compare two ExprCalls
   *
   * @param _exprcall1(ExprCall) - a ExprCall to be compared
   * @param _exprcall2(ExprCall) - a ExprCall to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareExprCall(ExprCall _exprcall1, ExprCall _exprcall2) {
    if (_exprcall1 == null && _exprcall2 == null) {
      System.err.println(
          "compareExprCall: expr1 == null && expr2 == null) ("
              + _exprcall1
              + " vs. "
              + _exprcall2
              + ")");
      return true;
    }

    if (_exprcall1 == null && _exprcall2 != null) {
      System.err.println(
          "compareExprCall: expr1 == null && expr2 != null) ("
              + _exprcall1
              + " vs. "
              + _exprcall2
              + ")");
      return false;
    }
    if (_exprcall1 != null && _exprcall2 == null) {
      System.err.println(
          "compareExprCall: expr1 != null && expr2 == null) ("
              + _exprcall1
              + " vs. "
              + _exprcall2
              + ")");
      return false;
    }

    if (_exprcall1.args.size() != _exprcall2.args.size()) {
      System.err.println(
          "compareExprCall: expr1.args.size() != expr2.args.size() ( "
              + _exprcall1.args.size()
              + " vs. "
              + _exprcall2.args.size()
              + ")");
      return false;
    }

    // check all expr1.args are in expr2.args
    boolean found;
    for (Expr next1 : _exprcall1.args) {
      found = false;
      for (Expr next2 : _exprcall2.args) {
        visitedExpressions.clear();
        if (compareExprAsString(next1, next2))
          if (compareExpr(next1, next2)) {
            found = true;
            break;
          }
      }
      if (!found) {
        System.err.println(
            "compareExprCall: expr.args (" + next1 + ") not found in " + _exprcall2.args + ".");
        return false;
      }
    }
    // check all expr2.args are in expr1.args
    for (Expr next2 : _exprcall2.args) {
      found = false;
      for (Expr next1 : _exprcall1.args) {
        visitedExpressions.clear();
        if (compareExprAsString(next1, next2))
          if (compareExpr(next1, next2)) {
            found = true;
            break;
          }
      }
      if (!found) {
        System.err.println(
            "compareExprCall: expr.args (" + next2 + ") not found in " + _exprcall1.args + ".");
        return false;
      }
    }

    if (_exprcall1.weight != _exprcall2.weight) {
      System.err.println(
          "compareExprCall: different weight. ("
              + _exprcall1.weight
              + " vs. "
              + _exprcall2.weight
              + ")");
      return false;
    }

    if (!compareFunctions(_exprcall1.fun, _exprcall2.fun)) {
      System.err.println(
          "compareExprCall: !compareFunctions(expr1.fun, expr2.fun) ("
              + _exprcall1.fun
              + " vs. "
              + _exprcall2.fun
              + ")");
      return false;
    }
    return true;
  }

  /**
   * Compare two ExprConstants
   *
   * @param _exprconstant1(ExprConstant) - a ExprConstant to be compared
   * @param _exprconstant2(ExprConstant) - a ExprConstant to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareExprConstant(ExprConstant _exprconstant1, ExprConstant _exprconstant2) {
    if (_exprconstant1 == null && _exprconstant2 == null) {
      System.err.println(
          "compareExprConstant expr1 == null && expr2 == null ("
              + _exprconstant1
              + " vs. "
              + _exprconstant2
              + ")");
      return true;
    }

    if (_exprconstant1 == null && _exprconstant2 != null) {
      System.err.println(
          "compareExprConstant expr1 == null && expr2 != null ("
              + _exprconstant1
              + " vs. "
              + _exprconstant2
              + ")");
      return false;
    }
    if (_exprconstant1 != null && _exprconstant2 == null) {
      System.err.println(
          "compareExprConstant expr1 != null && expr2 == null ("
              + _exprconstant1
              + " vs. "
              + _exprconstant2
              + ")");
      return false;
    }

    if (_exprconstant1.op != _exprconstant2.op) {
      System.err.println(
          "ExprConstant: expr1.op != expr2.op ("
              + _exprconstant1.op
              + " vs. "
              + _exprconstant2.op
              + ")");
      return false;
    }

    if (_exprconstant1.num != _exprconstant2.num) {
      System.err.println(
          "ExprConstant: expr1.num != expr2.num ("
              + _exprconstant1.num
              + " vs. "
              + _exprconstant2.num
              + ")");
      return false;
    }

    if (!_exprconstant1.string.equals(_exprconstant2.string)) {
      System.err.println(
          "ExprConstant: expr1.string != expr2.string ("
              + _exprconstant1.string
              + " vs. "
              + _exprconstant2.string
              + ")");
      return false;
    }
    return true;
  }

  /**
   * Compare two ExprITEs
   *
   * @param _exprITE1(ExprITE) - a ExprITE to be compared
   * @param _exprITE2(ExprITE) - a ExprITE to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareExprITE(ExprITE _exprITE1, ExprITE _exprITE2) {
    if (_exprITE1 == null && _exprITE2 == null) {
      System.err.println(
          "compareExprITE: expr1 == null && expr2 == null ("
              + _exprITE1
              + " vs. "
              + _exprITE2
              + ")");
      return true;
    }
    if (_exprITE1 == null || _exprITE2 == null) {
      System.err.println(
          "compareExprITE: expr1 != null || expr2 != null ("
              + _exprITE1
              + " vs. "
              + _exprITE2
              + ")");
      return false;
    }
    if (!compareExpr(_exprITE1.cond, _exprITE2.cond)) {
      System.err.println(
          "compareExprITE: !compareExpr(expr1.cond, expr2.cond) ("
              + _exprITE1.cond
              + " vs. "
              + _exprITE2.cond
              + ")");
      return false;
    }
    if (!compareExpr(_exprITE1.left, _exprITE2.left)) {
      System.err.println(
          "compareExprITE: !compareExpr(expr1.left, expr2.left) ("
              + _exprITE1.left
              + " vs. "
              + _exprITE2.left
              + ")");
      return false;
    }
    if (!compareExpr(_exprITE1.right, _exprITE2.right)) {
      System.err.println(
          "compareExprITE: !compareExpr(expr1.right, expr2.right) ("
              + _exprITE1.right
              + " vs. "
              + _exprITE2.right
              + ")");
      return false;
    }
    return true;
  }

  /**
   * Compare two ExprLists
   *
   * @param _exprITE1(ExprList) - a ExprList to be compared
   * @param _exprITE2(ExprList) - a ExprList to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareExprList(ExprList _exprList1, ExprList _exprList2) {
    if (_exprList1 == null && _exprList2 == null) {
      return true;
    }
    if (_exprList1 != null && _exprList2 == null) {
      System.err.println(
          "compareExprList: "
              + "expr1 != null && expr2 == null ("
              + _exprList1
              + " vs. "
              + _exprList2
              + ")");
      return false;
    }
    if (_exprList1 == null && _exprList2 != null) {
      System.err.println(
          "compareExprList: "
              + "expr1 == null && expr2 != null ("
              + _exprList1
              + " vs. "
              + _exprList2
              + ")");
      return false;
    }

    if (_exprList1.op != _exprList2.op) {
      System.err.println(
          "compareExprList: expr1.op != expr2.op ("
              + _exprList1.op
              + " vs. "
              + _exprList2.op
              + ")");
      return false;
    }

    if (_exprList1.args.size() != _exprList2.args.size()) {
      System.err.println(
          "compareExprList: "
              + "expr1.args.size() != expr2.args.size() ("
              + _exprList1.args.size()
              + " vs. "
              + _exprList2.args.size()
              + ")");
      return false;
    }
    // for each expr1.arg
    for (int i = 0; i < _exprList1.args.size(); i++) {
      boolean found = false;
      for (int j = 0; j < _exprList2.args.size(); j++) {
        // first compare as string. Both could be the same as string like (all x | no x .o/inputs)
        // but one may be contained in sig A and the other may be contained in sig ParameterBehavior
        // visitedExpressions.clear();
        if (compareExprAsString(_exprList1.args.get(i), _exprList2.args.get(j))) {
          if (!compareExpr(_exprList1.args.get(i), _exprList2.args.get(j))) {
            System.err.println(
                "compareExprList: expr1.args != expr2.args ("
                    + _exprList1.args.get(i)
                    + " vs. "
                    + _exprList2.args.get(j)
                    + ")");
            found = false;
          } else {
            found = true;
            break;
          }
        }
      } // end of j loop
      if (!found) {
        System.err.println(_exprList1.args.get(i) + " not found in " + _exprList2.args);
        return false;
      }
    }
    return true;
  }

  /**
   * Compare two ExprQts
   *
   * @param _exprQt1(ExprQt) - a ExprQt to be compared
   * @param _exprQt2(ExprQt) - a ExprQt to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareExprQt(ExprQt _exprQt1, ExprQt _exprQt2) {
    if (_exprQt1 == null && _exprQt2 == null) {
      return true;
    }

    if (_exprQt1 == null && _exprQt2 != null) {
      System.err.println("compareExprQt: expr1 == null && expr2 != null");
      return false;
    }
    if (_exprQt1 != null && _exprQt2 == null) {
      System.err.println("compareExprQt: expr1 != null && expr2 == null");
      return false;
    }

    if (_exprQt1.op != _exprQt2.op) {
      System.err.println(
          "compareExprQt: expr1.op(" + _exprQt1.op + ") != expr2.op(" + _exprQt2.op + ")");
      return false;
    }

    if (_exprQt1.decls.size() != _exprQt2.decls.size()) {
      System.err.println("compareExprQt: expr1.decls.size() != expr2.decls.size()");
      return false;
    }
    boolean found;
    // checking all expr1.decls in expr2.decls
    for (Decl d1 : _exprQt1.decls) {
      found = false;
      for (Decl d2 : _exprQt2.decls) {
        visitedExpressions.clear();
        if (compareDeclAsString(d1, d2))
          if (compareDecl(d1, d2)) {
            found = true;
            break;
          }
        if (!found) {
          System.err.println("compareExprQt: " + d1 + "not in " + _exprQt2.decls);
          return false;
        }
      }
    }

    if (!compareExpr(_exprQt1.sub, _exprQt2.sub)) {
      System.err.println(
          "compareExprQt: !compareExpr(expr1.sub, expr2.sub) "
              + _exprQt1.sub
              + " vs. "
              + _exprQt2.sub);
      return false;
    }
    return true;
  }

  /**
   * Compare two ExprUnarys
   *
   * @param _exprunary1(ExprUnary) - a ExprUnary to be compared
   * @param _exprunary2(ExprUnary) - a ExprUnary to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareExprUnary(ExprUnary _exprunary1, ExprUnary _exprunary2) {

    if (_exprunary1 == null && _exprunary2 == null) {
      return true;
    }

    if (_exprunary1 != null && _exprunary2 == null) {
      System.err.println("compareExprUnary: expr1 != null && expr2 == null");
      return false;
    }
    if (_exprunary1 == null && _exprunary2 != null) {
      System.err.println("compareExprUnary: expr1 == null && expr2 != null");
      return false;
    }

    if (_exprunary1.op != _exprunary2.op) {
      System.err.println(
          "compareExprUnary: expr1.op != expr2.op " + _exprunary1.op + " vs. " + _exprunary2.op);
      return false;
    }
    if (!compareExpr(_exprunary1.sub, _exprunary2.sub)) {
      System.err.println(
          "compareExprUnary: !compareExpr(expr1.sub, expr2.sub) "
              + _exprunary1.sub
              + " vs. "
              + _exprunary2.sub);
      return false;
    }
    return true;
  }

  /**
   * Compare two ExprVars
   *
   * @param _exprVar1(ExprVar) - a ExprVar to be compared
   * @param _exprVar2(ExprVar) - a ExprVar to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareExprVar(ExprVar _exprVar1, ExprVar _exprVar2) {
    if (_exprVar1 == null && _exprVar2 == null) {
      return true;
    }
    if (_exprVar1 == null && _exprVar2 != null) {
      System.err.println("compareExprVar: expr1 == null && expr2 != null");
      return false;
    }
    if (_exprVar1 != null && _exprVar2 == null) {
      System.err.println("compareExprVar: expr1 != null && expr2 == null");
      return false;
    }
    // expr.label is like "this" or "x" for comparing fact like "(all x | # x .
    // (this/MultipleControlFlow <: p1) = 2)"
    if (!_exprVar1.label.equals(_exprVar2.label)) {
      System.err.println(
          "compareExprVar: expr1.label != expr2.label ("
              + _exprVar1.label
              + " vs. "
              + _exprVar2.label
              + ")");
      return false;
    }
    if (!compareType(_exprVar1.type(), _exprVar2.type())) {
      System.err.println(
          "compareExprVar: expr1.type() != expr2.type() "
              + _exprVar1.type()
              + " vs. "
              + _exprVar2.type());
      return false;
    }
    return true;
  }

  /**
   * Compare two Funcs
   *
   * @param _func1(Func) - a Func to be compared
   * @param _func2(Func) - a Func to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareFunctions(Func _func1, Func _func2) {
    if (_func1 == null && _func2 == null) {
      System.err.println(
          "compareFunctions: func1 == null && func2 == null (" + _func1 + " vs. " + _func2 + ")");
      return true;
    }
    if (_func1 == null || _func2 == null) {
      System.err.println(
          "compareFunctions: func1 == null || func2 == null (" + _func1 + " vs. " + _func2 + ")");
      return false;
    }

    if (_func1.decls.size() != _func2.decls.size()) {
      System.err.println(
          "compareFunction: "
              + "func1.decls.size() != func2.decls.size() ("
              + _func1.decls.size()
              + " vs. "
              + _func2.decls.size()
              + ")");
      return false;
    }
    // check all func1.decls in func2.decls
    for (int i = 0; i < _func1.decls.size(); i++) {
      for (int j = 0; j < _func2.decls.size(); j++) {
        visitedExpressions.clear();
        if (compareDeclAsString(_func1.decls.get(i), _func2.decls.get(j))) {
          if (!compareDecl(_func1.decls.get(i), _func2.decls.get(j))) {
            System.err.println(
                "compareFunction: !compareDecl (func.decls). "
                    + _func1.decls.get(i)
                    + " and "
                    + _func2.decls.get(j)
                    + "has a same string but different as Decl");
            return false;
          }
        }
      }
    }

    if (_func1.isPred != _func2.isPred) {
      System.err.println(
          "compareFunction: func1.isPred != func2.isPred "
              + _func1.isPred
              + " vs. "
              + _func2.isPred);
      return false;
    }
    if (_func1.isPrivate == null && _func2.isPrivate != null) {

      System.err.println(
          "compareFunctions: " + "func1.isPrivate == null && func2.isPrivate != null");
      return false;
    }
    if (_func1.isPrivate != null && _func2.isPrivate == null) {
      System.err.println(
          "compareFunctions: " + "func1.isPrivate != null && func2.isPrivate == null");
      return false;
    }
    if (!AlloyUtils.removeSlash(_func1.label).equals(AlloyUtils.removeSlash(_func2.label))) {
      System.err.println(
          "compareFunctions: label is different. ("
              + AlloyUtils.removeSlash(_func1.label)
              + " vs. "
              + AlloyUtils.removeSlash(_func2.label)
              + ")");
      System.err.println();
      return false;
    }
    if (!compareExpr(_func1.returnDecl, _func2.returnDecl)) {
      System.err.println(
          "compareFunctions: "
              + "!compareExpr(func1.returnDecl, func2.returnDecl) "
              + _func1.returnDecl
              + " vs. "
              + _func2.returnDecl);
      return false;
    }
    if (!compareExpr(_func1.getBody(), _func2.getBody())) {
      System.err.println(
          "!compareExpr(func1.getBody(), func2.getBody()) "
              + _func1.getBody()
              + " vs. "
              + _func2.getBody());
      return false;
    }
    return true;
  }

  /**
   * Compare two PrimSigs
   *
   * @param _primSig1(Sig) - a PrimSig to be compared
   * @param _primSig2(Sig) - a PrimSig to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean comparePrimSig(PrimSig _primSig1, PrimSig _primSig2) {
    if (_primSig1.label.equals("univ") && _primSig2.label.equals("univ")) {
      return true;
    }

    if (_primSig1.children().size() != _primSig2.children().size()) {
      System.err.println(
          "comparePrimSig: primSig1("
              + _primSig1
              + ").children().size()("
              + _primSig1.children().size()
              + ") != primSig2("
              + _primSig2
              + ").children().size() ("
              + +_primSig2.children().size()
              + ")");
      return false;
    }
    return true;
  }

  /**
   * Compare two Sigs
   *
   * @param _sig1(Sig) - a Sig to be compared
   * @param _sig2(Sig) - a Sig to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareSig(Sig _sig1, Sig _sig2) {

    if (_sig1 == null && _sig2 == null) {
      System.err.println("compareSig: sig1 == null && sig2 == null");
      return true;
    }
    if (_sig1 == null && _sig2 != null) {
      System.err.println(
          "compareSig: sig1 == null && sig2 != null (" + _sig1 + " vs. " + _sig2 + ")");
      return false;
    }
    if (_sig1 != null && _sig2 == null) {
      System.err.println(
          "compareSig: sig1 != null && sig2 == null(" + _sig1 + " vs. " + _sig2 + ")");
      return false;
    }

    if (_sig1 instanceof Sig.PrimSig
        && _sig2 instanceof Sig.PrimSig
        && !comparePrimSig((Sig.PrimSig) _sig1, (Sig.PrimSig) _sig2)) {
      return false;
    }

    if (_sig1.attributes.size() != _sig2.attributes.size()) {
      System.err.println(
          "compareSig: sig1.attributes.size() != sig2.attributes.size() ("
              + _sig1.attributes.size()
              + " vs. "
              + _sig2.attributes.size()
              + ")");
      return false;
    }

    // sig.attributes are [0] where, [1]...[6] null, [7]..[9] subsig
    // order of sig1.attributes and sig2.attributes are the same
    for (int i = 0; i < _sig1.attributes.size(); i++) {
      if (_sig1.attributes.get(i) == null && _sig2.attributes.get(i) == null) continue;
      else if (!_sig1.attributes.get(i).toString().equals(_sig2.attributes.get(i).toString())) {
        System.err.println(
            "compareSig: !compareAttr(sig1.attributes.get(i), sig2.attributes.get(i))"
                + _sig1.attributes.get(i)
                + " vs. "
                + _sig2.attributes.get(i));
        return false;
      }
    }

    if (_sig1.builtin != _sig2.builtin) {
      System.err.println(
          "compareSig: sig1.builtin != sig2.builtin" + _sig1.builtin + " vs. " + _sig2.builtin);
      return false;
    }
    if (!compareDecl(_sig1.decl, _sig2.decl)) {
      System.err.println("compareSig:!compareDecl(sig1.decl, sig2.decl)");
      return false;
    }

    if (_sig1.isAbstract == null && _sig2.isAbstract != null) {
      System.err.println("compareSig: sig1.isAbstract == null && sig2.isAbstract != null");
      return false;
    }
    if (_sig1.isAbstract != null && _sig2.isAbstract == null) {
      System.err.println("compareSig: sig1.isAbstract != null && sig2.isAbstract == null");
      return false;
    }
    if (_sig1.isEnum == null && _sig2.isEnum != null) {
      System.err.println("compareSig: sig1.isEnum == null && sig2.isEnum != null");
      return false;
    }
    if (_sig1.isEnum != null && _sig2.isEnum == null) {
      System.err.println("compareSig: sig1.isEnum != null && sig2.isEnum == null");
      return false;
    }
    if (_sig1.isLone == null && _sig2.isLone != null) {
      System.err.println("compareSig: sig1.isLone == null && sig2.isLone != null");
      return false;
    }
    if (_sig1.isLone != null && _sig2.isLone == null) {
      System.err.println("compareSig: sig1.isLone != null && sig2.isLone == null");
      return false;
    }
    if (_sig1.isMeta == null && _sig2.isMeta != null) {
      System.err.println("compareSig: sig1.isMeta == null && sig2.isMeta != null");
      return false;
    }
    if (_sig1.isMeta != null && _sig2.isMeta == null) {
      System.err.println("compareSig: sig1.isMeta != null && sig2.isMeta == null");
      return false;
    }
    if (_sig1.isOne == null && _sig2.isOne != null) {
      System.err.println("compareSig: sig1.isOne == null && sig2.isOne != null");
      return false;
    }
    if (_sig1.isOne != null && _sig2.isOne == null) {
      System.err.println("compareSig: sig1.isOne != null && sig2.isOne == null");
      return false;
    }
    if (_sig1.isPrivate == null && _sig2.isPrivate != null) {
      System.err.println("compareSig: sig1.isPrivate == null && sig2.isPrivate != null");
      return false;
    }
    if (_sig1.isPrivate != null && _sig2.isPrivate == null) {
      System.err.println("compareSig: sig1.isPrivate != null && sig2.isPrivate == null");
      return false;
    }
    if (_sig1.isSome == null && _sig2.isSome != null) {
      System.err.println("compareSig: sig1.isSome == null && sig2.isSome != null");
      return false;
    }
    if (_sig1.isSome != null && _sig2.isSome == null) {
      System.err.println("compareSig: sig1.isSome != null && sig2.isSome == null");
      return false;
    }
    if (_sig1.isSubset == null && _sig2.isSubset != null) {
      System.err.println("compareSig: sig1.isSubset == null && sig2.isSubset != null");
      return false;
    }
    if (_sig1.isSubset != null && _sig2.isSubset == null) {
      System.err.println("compareSig: sig1.isSubset != null && sig2.isSubset == null");
      return false;
    }
    if (_sig1.isSubsig == null && _sig2.isSubsig != null) {
      System.err.println("compareSig: sig1.isSubsig == null && sig2.isSubsig != null");
      return false;
    }
    if (_sig1.isSubsig != null && _sig2.isSubsig == null) {
      System.err.println("compareSig: sig1.isSubsig != null && sig2.isSubsig == null");
      return false;
    }
    if (!AlloyUtils.removeSlash(_sig1.label).equals(AlloyUtils.removeSlash(_sig2.label))) {
      System.err.println(
          "compareSig: !sig1.label.equals(sig2.label) "
              + AlloyUtils.removeSlash(_sig1.label)
              + " vs. "
              + AlloyUtils.removeSlash(_sig2.label));
      return false;
    }
    if (_sig1.getDepth() != _sig2.getDepth()) {
      System.err.println(
          "compareSig: sig1.getDepth() != sig2.getDepth() "
              + _sig1.getDepth()
              + " vs. "
              + _sig2.getDepth());
      return false;
    }

    if (_sig1.getFacts().size() != _sig2.getFacts().size()) {
      System.err.println(
          "compareSig: sig1.getFacts().size() != sig2.getFacts().size() ("
              + _sig1.getFacts().size()
              + " vs. "
              + _sig2.getFacts().size()
              + ")");
      return false;
    }

    // check all sig1.facts are in sig2.facts - not necessary check separately
    boolean found;
    if (_sig1.getFieldDecls().size() != _sig2.getFieldDecls().size()) {
      System.err.println(
          "compareSig: sig1.getFieldDecls().size() != sig2.getFieldDecls().size() ("
              + _sig1.getFieldDecls().size()
              + " vs. "
              + _sig2.getFieldDecls().size()
              + ")");
      return false;
    }

    for (Decl decl1 : _sig1.getFieldDecls()) {
      found = false;
      for (Decl decl2 : _sig2.getFieldDecls()) {
        visitedExpressions.clear();
        if (compareDecl(decl1, decl2)) { // compare fields each.
          found = true;
          break;
        }
      }
      if (!found) {
        System.err.println(
            "compareSig: fieldDecls " + decl1 + " not found in " + _sig2.getFieldDecls());
        return false;
      }
    }

    if (_sig1.getFields().size() != _sig2.getFields().size()) {
      System.err.println(
          "compareSig: sig1.getFields().size() != sig2.getFields().size() ("
              + _sig1.getFields().size()
              + " vs. "
              + _sig2.getFields().size()
              + ")");
      return false;
    }
    // check all sig1.fields are in sig2.fields
    for (Sig.Field f1 : _sig1.getFields()) {
      found = false;
      for (Sig.Field f2 : _sig2.getFields()) {
        visitedExpressions.clear();
        if (compareExprAsString(f1, f2)) {
          if (compareSigField(f1, f2)) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        System.err.println(
            "compareSig: !compareSig.Field failed. " + f1 + " not found in " + _sig2.getFields());
        return false;
      }
    }

    // getHTML() not implemented
    // getSubnodes() not implemented

    if (_sig1.isTopLevel() != _sig2.isTopLevel()) {
      System.err.println(
          "compareSig: sig1.isTopLevel() != sig2.isTopLevel() ("
              + _sig1.isTopLevel()
              + " vs. "
              + _sig2.isTopLevel()
              + ")");
      return false;
    }
    if (!AlloyUtils.removeSlash(_sig1.toString())
        .equals(AlloyUtils.removeSlash(_sig2.toString()))) {
      System.err.println(
          "compareSig: !sig1.toString().equals(sig2.toString()) ("
              + AlloyUtils.removeSlash(_sig1.toString())
              + " vs. "
              + AlloyUtils.removeSlash(_sig2.toString())
              + ")");
      return false;
    }
    return true; // CompareSig
  }

  /**
   * Compare two Fields
   *
   * @param _field1(Field) - a Field to be compared
   * @param _field2(Field) - a Field to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareSigField(Field _field1, Field _field2) {
    if (_field1 == null && _field2 == null) {
      System.err.println(
          "sig1Field == null && sig2Field == null (" + _field1 + " vs. " + _field2 + ")");
      return true;
    }

    if (_field1 == null && _field2 != null) {
      System.err.println(
          "sig1Field == null && sig2Field != null (" + _field1 + " vs. " + _field2 + ")");
    } else if (_field1 != null && _field2 == null) {
      System.err.println(
          "sig1Field != null && sig2Field == null (" + _field1 + " vs. " + _field2 + ")");
    }

    if (!AlloyUtils.removeSlash(_field1.label).equals(AlloyUtils.removeSlash(_field2.label))) {
      System.err.println(
          "Sig.Field: sig1.label != sig2.label ("
              + AlloyUtils.removeSlash(_field1.label)
              + " vs. "
              + AlloyUtils.removeSlash(_field2.label)
              + ")");
      return false;
    }

    if (_field1.defined != _field2.defined) {
      System.err.println(
          "Sig.Field: sig1.defined != sig2.defined ("
              + _field1.defined
              + " vs. "
              + _field2.defined
              + ")");
      return false;
    }

    if ((_field1.isMeta == null && _field2.isMeta != null)
        || _field1.isMeta != null && _field2.isMeta == null) {
      System.err.println(
          "Sig.Field: isMeta different (" + _field1.isMeta + " vs. " + _field2.isMeta + ")");
      return false;
    }

    if ((_field1.isPrivate == null && _field2.isPrivate != null)
        || _field1.isPrivate != null && _field2.isPrivate == null) {
      System.err.println(
          "Sig.Field: isPrivate different ("
              + _field1.isPrivate
              + " vs. "
              + _field2.isPrivate
              + ")");
      return false;
    }
    return true;
  }

  /**
   * Compare two Types
   *
   * @param _type1(Type) - a Type to be compared
   * @param _type2(Type) - a Type to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareType(Type _type1, Type _type2) {
    if (_type1 == null && _type2 == null) {
      return true;
    }
    if (_type1 == null || _type2 == null) {
      return false;
    }
    if (_type1.is_bool != _type2.is_bool) {
      System.err.println("compareType: t1.is_bool != t2.is_bool");
      return false;
    }
    if (_type1.is_int() != _type2.is_int()) {
      System.err.println("compareType: t1.is_int() != t2.is_int()");
      return false;
    }
    if (_type1.is_small_int() != _type2.is_small_int()) {
      System.err.println("compareType: t1.is_small_int() != t2.is_small_int()");
      return false;
    }
    if (_type1.arity() != _type2.arity()) {
      System.err.println("compareType: t1.arity() != t2.arity()");
      return false;
    }
    if (_type1.size() != _type2.size()) {
      System.err.println("compareType: t1.size() != t2.size()");
      return false;
    }
    if (_type1.hasNoTuple() != _type2.hasNoTuple()) {
      System.err.println("compareType: t1.hasNoTuple() != t2.hasNoTuple()");
      return false;
    }
    if (_type1.hasTuple() != _type2.hasTuple()) {
      System.err.println("compareType: t1.hasTuple() != t2.hasTuple()");
      return false;
    }

    return true;
  }

  /**
   * Compare two Exprs by converting to string
   *
   * @param _expr1(Expr) - a ExprList to be compared
   * @param _expr2(Expr) - a ExprList to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareExprAsString(Expr _expr1, Expr _expr2) {
    String s1 = _expr1.toString().replaceAll("this/", "").replaceAll("o/r/", "r/");
    String s2 = _expr2.toString().replaceAll("this/", "").replaceAll("o/r/", "r/");

    if (s1.equals(s2)) return true;
    return false;
  }

  /**
   * Compare two Decls by converting to string
   *
   * @param _decl1(Decl) - a Decl to be compared
   * @param _expr2(Decl) - a Decl to be compared
   * @return (boolean) true if determined as the same, otherwise false
   */
  private boolean compareDeclAsString(Decl _decl1, Decl _decl2) {
    String s1 = _decl1.names.toString().replaceAll("this/", "").replaceAll("o/r/", "r/");
    String s2 = _decl2.names.toString().replaceAll("this/", "").replaceAll("o/r/", "r/");
    if (s1.compareTo(s2) == 0) return true;
    return false;
  }

  // util methods
  /**
   * sort ExprList of Expr in alphabetical order using expr.toString() method return is List of
   * List<Expr> because some fact's have the same toString() even its belong to different Signatures
   * (ie., inputs.x)
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
    for (String key : keys) sortedList.add(sortedMap.get(key));

    return sortedList;
  }
}
