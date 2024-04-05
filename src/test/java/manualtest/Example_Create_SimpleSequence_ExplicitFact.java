package manualtest;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.Translator;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprHasName;
import edu.mit.csail.sdg.ast.ExprList;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Example_Create_SimpleSequence_ExplicitFact {

  public static void main(String[] args) {

    String moduleName = "SimpleSequence_ExplicitFact";
    String outFileName = "generated-" + moduleName + ".als";
    Alloy sst = new Alloy("src/test/resources");

    Set<Sig> ignoredSigs = new HashSet<>();
    Set<Expr> ignoredExprs = new HashSet<>();
    Set<Func> ignoredFuncs = new HashSet<>();

    // ========== Specify list of signatures, facts, and functions to ignore ==========

    for (Sig sig : sst.getAllSigs()) {
      ignoredSigs.add(sig);
    }

    ExprList exprList = (ExprList) sst.getOverAllFact();
    for (Expr expr : exprList.args) {
      ignoredExprs.add(expr);
    }

    for (Module module : sst.getTransferModule().getAllReachableModules()) {
      for (Func func : module.getAllFunc()) {
        ignoredFuncs.add(func);
      }
    }

    // ========== Define list of signatures unique to the file ==========

    Sig p1Sig = sst.createSigAsChildOfOccSigAndAddToAllSigs("P1");
    Sig p2Sig = sst.createSigAsChildOfOccSigAndAddToAllSigs("P2");
    Sig mainSig = sst.createSigAsChildOfOccSigAndAddToAllSigs("SimpleSequence");

    // ========== Define list of relations unique to the file ==========

    Sig.Field p1 = FuncUtils.addField("p1", mainSig, p1Sig);
    Sig.Field p2 = FuncUtils.addField("p2", mainSig, p2Sig);

    // ========== Define explicit facts + functions/predicates ==========

    Func osteps = AlloyUtils.getFunction(sst.transferModule, "o/steps");
    Func funcFiltered = AlloyUtils.getFunction(sst.transferModule, "o/functionFiltered");
    Func happensBefore = AlloyUtils.getFunction(sst.transferModule, "o/happensBefore");
    Func inverseFuncFiltered =
        AlloyUtils.getFunction(sst.transferModule, "o/inverseFunctionFiltered");
    Expr ostepsExpr1 = osteps.call();
    Expr ostepsExpr2 = osteps.call();

    ExprVar s1 = ExprVar.make(null, "s", mainSig.type());
    List<ExprHasName> names1 = new ArrayList<>(List.of(s1));

    ExprVar s2 = ExprVar.make(null, "s", mainSig.type());
    List<ExprHasName> names2 = new ArrayList<>(List.of(s2));

    ExprVar s3 = ExprVar.make(null, "s", mainSig.type());
    List<ExprHasName> names3 = new ArrayList<>(List.of(s3));

    ExprVar s4 = ExprVar.make(null, "s", mainSig.type());
    List<ExprHasName> names4 = new ArrayList<>(List.of(s4));

    ExprVar s5 = ExprVar.make(null, "s", mainSig.type());
    List<ExprHasName> names5 = new ArrayList<>(List.of(s5));

    Decl decl1 = new Decl(null, null, null, names1, mainSig.oneOf());
    Decl decl2 = new Decl(null, null, null, names2, mainSig.oneOf());
    Decl decl3 = new Decl(null, null, null, names3, mainSig.oneOf());
    Decl decl4 = new Decl(null, null, null, names4, mainSig.oneOf());
    Decl decl5 = new Decl(null, null, null, names5, mainSig.oneOf());

    Expr funcFilteredExpr =
        funcFiltered.call(
            happensBefore.call(), s4.join(mainSig.domain(p1)), s4.join(mainSig.domain(p2)));
    Expr inverseFuncFilteredExpr =
        inverseFuncFiltered.call(
            happensBefore.call(), s5.join(mainSig.domain(p1)), s5.join(mainSig.domain(p2)));

    sst.addToOverallFact(
        s1.join(mainSig.domain(p1))
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .forAll(decl1)
            .and(
                s2.join(mainSig.domain(p1))
                    .plus(s2.join(mainSig.domain(p2)))
                    .in(s2.join(ostepsExpr1))
                    .forAll(decl2))
            .and(
                s3.join(ostepsExpr2)
                    .in(s3.join(mainSig.domain(p1)).plus(s3.join(mainSig.domain(p2))))
                    .forAll(decl3))
            .and(funcFilteredExpr.forAll(decl4))
            .and(inverseFuncFilteredExpr.forAll(decl5)));

    Func nonZeroDurationOnlyFunction =
        AlloyUtils.getFunction(sst.transferModule, "o/nonZeroDurationOnly");
    Expr nonZeroDurationOnlyFunctionExpression = nonZeroDurationOnlyFunction.call();

    Sig transfer = AlloyUtils.getReachableSig(sst.transferModule, "o/Transfer");
    Expr suppressTransfersExpessionBody = transfer.no();
    Func suppressTransfersFunction =
        new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
    Expr suppressTransfersExpression = suppressTransfersFunction.call();

    Func inputs = AlloyUtils.getFunction(sst.transferModule, "o/inputs");
    Func outputs = AlloyUtils.getFunction(sst.transferModule, "o/outputs");
    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
    Func suppressIOFunction = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
    Expr suppressIOExpression = suppressIOFunction.call();

    Pos pos = null;
    String label = "p1DuringExample";
    List<Decl> decls = new ArrayList<>();
    Expr returnDecl = null;
    Expr body = p1Sig.in(mainSig.join(p1));
    Func p1DuringExamplePredicate = new Func(pos, label, decls, returnDecl, body);
    Expr p1DuringExampleExpression = p1DuringExamplePredicate.call();

    Func p2DuringExamplePredicate =
        new Func(null, "p2DuringExample", new ArrayList<>(), null, p2Sig.in(mainSig.join(p2)));
    Expr p2DuringExampleExpression = p2DuringExamplePredicate.call();

    Func instancesDuringExamplePredicate =
        new Func(
            null,
            "instancesDuringExample",
            new ArrayList<>(),
            null,
            p1DuringExampleExpression.and(p2DuringExampleExpression));
    Expr instancesDuringExampleExpression = instancesDuringExamplePredicate.call();

    Func onlySimpleSequencePredicate =
        new Func(
            null,
            "onlySimpleSequence",
            new ArrayList<>(),
            null,
            mainSig.cardinality().equal(ExprConstant.makeNUMBER(1)));
    Expr onlySimpleSequenceExpression = onlySimpleSequencePredicate.call();
    Expr _nameExpr =
        nonZeroDurationOnlyFunctionExpression
            .and(suppressTransfersExpression)
            .and(suppressIOExpression)
            .and(instancesDuringExampleExpression)
            .and(onlySimpleSequenceExpression);

    Pos _pos = null;
    String _label = "SimpleSequence";
    boolean _check = false;
    int _overall = 6;
    int _bitwidth = -1;
    int _maxseq = -1;
    int _expects = -1;
    Iterable<CommandScope> _scope = Arrays.asList(new CommandScope[] {});
    Iterable<Sig> _additionalExactSig = Arrays.asList(new Sig[] {});
    Expr _formula = _nameExpr.and(sst.getOverAllFact());
    Command _parent = null;

    // ========== Define command ==========

    Command command =
        new Command(
            _pos,
            _nameExpr,
            _label,
            _check,
            _overall,
            _bitwidth,
            _maxseq,
            _expects,
            _scope,
            _additionalExactSig,
            _formula,
            _parent);
    Command[] commands = {command};

    AlloyModule alloyModule =
        new AlloyModule(moduleName, sst.getAllSigs(), sst.getOverAllFact(), commands);

    Translator translator = new Translator(ignoredExprs, ignoredFuncs, ignoredSigs);

    translator.generateAlsFileContents(alloyModule, outFileName);
  }
}
