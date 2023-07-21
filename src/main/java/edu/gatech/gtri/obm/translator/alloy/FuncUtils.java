package edu.gatech.gtri.obm.translator.alloy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;

public class FuncUtils {

  public static Expr getDuringExampleExpr(String label, Sig sig, Sig.Field field, Sig fieldType) {
    return new Func(null, label, null, null, fieldType.in(sig.join(field))).call();
  }

  public static Expr exprToFunToExpr(String label, Expr expr) {
    return new Func(null, label, null, null, expr).call();
  }

  /**
   * Adds a field to a Signature with the format: sig ${sig} { ${label}: set ${sigType} }
   *
   * @param label = the name of the field
   * @param sig = the Signature the field is being added to
   * @param sigType = the Signature being connected to sig, given a set multiplicity
   * @return Sig.Field = the newly created field
   */
  public static Sig.Field addField(String label, Sig sig, Sig sigType) {
    return sig.addField(label, sigType.setOf());
  }

  public static Sig.Field[] addTrickyField(java.lang.String[] labels, Sig sig, Sig sigType) {
    Pos isDisjoint = new Pos("", 0, 0);
    return sig.addTrickyField(null, null, isDisjoint, null, null, labels, sigType.setOf());
  }

  public static Expr createExprEqualToNumber(Expr expr, int num) {
    return expr.cardinality().equal(ExprConstant.makeNUMBER(num));
  }

  public static Expr createExprGreaterThanEqualToNumber(Expr expr, int num) {
    return expr.cardinality().gte(ExprConstant.makeNUMBER(num));
  }

  public static Expr createExprGreaterToNumber(Expr expr, int num) {
    return expr.cardinality().gt(ExprConstant.makeNUMBER(num));
  }

  public static Expr addBijectionFilteredExprHappensBefore(Module transferModule, Expr first,
      Expr second) {
    Func happensBefore = Helper.getFunction(transferModule, "o/happensBefore");
    Func bijectionFiltered = Helper.getFunction(transferModule, "o/bijectionFiltered");

    // bijectionFiltered[happensBefore, first, second]
    Expr bijectionFilteredExpr = bijectionFiltered.call(happensBefore.getBody(), first, second);
    return bijectionFilteredExpr;
  }

  public static Expr addFunctionFilteredExprHappensBefore(Module transferModule, Expr first,
      Expr second) {
    Func happensBefore = Helper.getFunction(transferModule, "o/happensBefore");
    Func fn = Helper.getFunction(transferModule, "o/functionFiltered");

    // bijectionFiltered[happensBefore, first, second]
    Expr expr = fn.call(happensBefore.getBody(), first, second);
    return expr;
  }

  public static Expr addInverseFunctionFilteredExprHappensBefore(Module transferModule, Expr first,
      Expr second) {
    Func happensBefore = Helper.getFunction(transferModule, "o/happensBefore");
    Func fn = Helper.getFunction(transferModule, "o/inverseFunctionFiltered");

    // bijectionFiltered[happensBefore, first, second]
    Expr expr = fn.call(happensBefore.getBody(), first, second);
    return expr;
  }

  // expr[0].and(expr[1]).and(expr[2])
  public static Expr addExprs(List<Expr> exprs) {
    Expr e = exprs.get(0);
    for (int i = 1; i < exprs.size(); i++) {
      e = e.and(exprs.get(i));
    }
    return e;
  }

  public static Expr addToExpr(Expr original, Expr toAdd) {
    original = toAdd.and(original);
    return original;
  }

  public static Expr addNonZeroDurationOnly(Module transferModule) {
    // pred nonZeroDurationOnly{all occ: Occurrence | not o/isZeroDuration[occ]}
    // pred isZeroDuration [x: Occurrence] {before[x,x]}
    // v1
    Func nonZeroDurationOnlyFunc = Helper.getFunction(transferModule, "o/nonZeroDurationOnly");
    Expr nonZeroDurationOnlyFuncExpr = nonZeroDurationOnlyFunc.call();
    return nonZeroDurationOnlyFuncExpr;
  }


  public static Expr addSuppressTransfersExpr(Module transferModule) {
    Sig transfer = Helper.getReachableSig(transferModule, "o/Transfer");
    Expr suppressTransfersExprBody = transfer.no();
    Func suppressTransfers =
        new Func(null, "suppressTransfers", null, null, suppressTransfersExprBody);
    Expr suppressTransfersExpr = suppressTransfers.call();
    return suppressTransfersExpr;
  }

  public static Expr addSuppressIOExpr(Module transferModule) {
    // pred suppressIO {no inputs and no outputs}
    Func inputs = Helper.getFunction(transferModule, "o/inputs");
    Func outputs = Helper.getFunction(transferModule, "o/outputs");
    Expr suppressIOExprBody = inputs.call().no().and(outputs.call().no());
    Func suppressIOFuc = new Func(null, "suppressIO", null, null, suppressIOExprBody);
    Expr suppressIOExpr = suppressIOFuc.call();
    return suppressIOExpr;
  }

  // pred onlySimpleSequence {#SimpleSequence = 1}
  // onlyOneSigExpr(false, "onlySimpleSequence", simpleSequence)
  public static Expr onlyOneSigExpr(String label, Sig sig) {
    Expr expr = new Func(null, label, null, null, FuncUtils.createExprEqualToNumber(sig, 1)).call();
    return expr;
  }


  public static Expr addStep(Module transferModule, List<Sig.Field> fields, Sig sig) {
    return FuncUtils.addToExpr(FuncUtils.addStepsInFields(transferModule, fields, sig),
        FuncUtils.addFieldsInStep(transferModule, fields, sig));
  }

  // p1 + p2 + p3 in this.steps
  public static Expr addFieldsInStep(Module transferModule, List<Sig.Field> fields, Sig sig) {
    Func osteps = Helper.getFunction(transferModule, "o/steps");
    Expr ostepsExpr = osteps.call();
    Expr e = sig.join(fields.get(0));
    for (int i = 1; i < fields.size(); i++) {
      e = e.plus(sig.join(fields.get(i)));
    }
    e = e.in(sig.join(ostepsExpr));
    return e;
  }

  // this.steps in p1 + p2 + p3
  public static Expr addStepsInFields(Module transferModule, List<Sig.Field> fields, Sig sig) {
    Func osteps = Helper.getFunction(transferModule, "o/steps");
    Expr ostepsExpr = osteps.call();
    Expr e = sig.join(fields.get(0));
    for (int i = 1; i < fields.size(); i++) {
      e = e.plus(sig.join(fields.get(i)));
    }
    e = sig.join(ostepsExpr).in(e);
    return e;
  }

  public static Expr createConstraintExpr(Module transferModule, Sig mainSig,
      List<Sig.Field> fields, List<Sig> attTypeSigs) {


    // pred nonZeroDurationOnly{all occ: Occurrence | not o/isZeroDuration[occ]}
    Expr nonZeroDurationOnlyFuncExpr = FuncUtils.addNonZeroDurationOnly(transferModule);
    // suppresstransfer
    Expr suppressTransfersExpr = FuncUtils.addSuppressTransfersExpr(transferModule);
    // suppressIO
    Expr suppressIOExpr = FuncUtils.addSuppressIOExpr(transferModule);

    List<Expr> lduring = new ArrayList<>();
    for (int i = 0; i < fields.size(); i++) {
      lduring.add(FuncUtils.getDuringExampleExpr(fields.get(i) + "DuringExample", mainSig,
          fields.get(i), attTypeSigs.get(i)));
    }
    // pred p1DuringExample {P1 in (Join.p1)}
    Expr instancesDuringExampleExpr =
        FuncUtils.exprToFunToExpr("instancesDuringExample", FuncUtils.addExprs(lduring));

    // pred onlyJoin {#Join = 1}
    Expr onlySigExpr = FuncUtils.onlyOneSigExpr("onlySig", mainSig);


    return FuncUtils.addExprs(Arrays.asList(nonZeroDurationOnlyFuncExpr, suppressTransfersExpr,
        suppressIOExpr, instancesDuringExampleExpr, onlySigExpr));
  }

  public static Expr forkHappensBefore(Module _transferModule, Sig _mainSig, Sig.Field _p1,
      Sig.Field _p2, Sig.Field _p3) {
    Expr e1 = FuncUtils.addBijectionFilteredExprHappensBefore(_transferModule, _mainSig.join(_p1),
        _mainSig.join(_p2));
    Expr e2 = FuncUtils.addBijectionFilteredExprHappensBefore(_transferModule, _mainSig.join(_p1),
        _mainSig.join(_p3));
    return FuncUtils.addExprs(Arrays.asList(e1, e2));
  }

  public static Expr decisionHappensBefore(Module _transferModule, Sig _mainSig, Sig.Field _p1,
      Sig.Field _p2, Sig.Field... _p3s) {
    Expr plus = _mainSig.join(_p2);
    for (Sig.Field p3 : _p3s) {
      plus = plus.plus(_mainSig.join(p3));
    }

    Expr e1 =
        FuncUtils.addBijectionFilteredExprHappensBefore(_transferModule, _mainSig.join(_p1), plus);
    return e1;
  }

  public static Expr loopHappensBefore(Module _transferModule, Sig _mainSig, Sig.Field _p1,
      Sig.Field _p2, Sig.Field _p3) {
    Expr e1 = FuncUtils.addFunctionFilteredExprHappensBefore(_transferModule, _mainSig.join(_p1),
        _mainSig.join(_p2));
    Expr e2 = FuncUtils.addInverseFunctionFilteredExprHappensBefore(_transferModule,
        _mainSig.join(_p1).plus(_mainSig.join(_p2)), _mainSig.join(_p2));

    Expr e3 = FuncUtils.addFunctionFilteredExprHappensBefore(_transferModule, _mainSig.join(_p2),
        _mainSig.join(_p2).plus(_mainSig.join(_p3)));
    Expr e4 = FuncUtils.addInverseFunctionFilteredExprHappensBefore(_transferModule,
        _mainSig.join(_p2), _mainSig.join(_p3));

    return FuncUtils.addExprs(Arrays.asList(e1, e2, e3, e4));
  }
}