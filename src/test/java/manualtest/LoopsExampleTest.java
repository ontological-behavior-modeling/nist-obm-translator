package manualtest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.Translator;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import obmtest.ExpressionComparator;
import org.junit.jupiter.api.Test;

class LoopsExampleTest {

  @Test
  void test() {
    String moduleName = "LoopsExample";
    String outFileName = "src/test/resources/generated-" + moduleName + ".als";
    String filename = "src/test/resources/4.1.2 LoopsExamples.als";
    Alloy alloy = new Alloy("src/test/resources");
    // ========== Define list of signatures unique to the file ==========

    Sig abSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("AtomicBehavior");
    Sig loopSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Loop");

    // ========== Define list of relations unique to the file ==========

    Sig.Field loop_p1 = FuncUtils.addField("p1", loopSig, abSig);
    Sig.Field loop_p2 = FuncUtils.addField("p2", loopSig, abSig);
    Sig.Field loop_p3 = FuncUtils.addField("p3", loopSig, abSig);

    // ========== Define implicit facts ==========

    // Loop

    ExprVar loopThis = ExprVar.make(null, "this", loopSig.type());

    Func functionFilteredFunction =
        AlloyUtils.getFunction(alloy.transferModule, "o/functionFiltered");
    Func happensBefore = AlloyUtils.getFunction(Alloy.transferModule, "o/happensBefore");
    Func inverseFunctionFilteredFunction =
        AlloyUtils.getFunction(alloy.transferModule, "o/inverseFunctionFiltered");
    Func stepsFunction = AlloyUtils.getFunction(Alloy.transferModule, "o/steps");

    loopSig.addFact(
        functionFilteredFunction
            .call(happensBefore.call(), loopThis.join(loop_p1), loopThis.join(loop_p2))
            .and(
                inverseFunctionFilteredFunction.call(
                    happensBefore.call(),
                    loopThis.join(loop_p1).plus(loopThis.join(loop_p2)),
                    loopThis.join(loop_p2)))
            .and(
                functionFilteredFunction.call(
                    happensBefore.call(),
                    loopThis.join(loop_p2),
                    loopThis.join(loop_p2).plus(loopThis.join(loop_p3))))
            .and(
                inverseFunctionFilteredFunction.call(
                    happensBefore.call(), loopThis.join(loop_p2), loopThis.join(loop_p3)))
            .and(loopThis.join(loop_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
            .and(loopThis.join(loop_p2).cardinality().gte(ExprConstant.makeNUMBER(2)))
            .and(loopThis.join(loop_p3).cardinality().gte(ExprConstant.makeNUMBER(1)))
            .and(
                loopThis
                    .join(loop_p1)
                    .plus(loopThis.join(loop_p2))
                    .plus(loopThis.join(loop_p3))
                    .in(loopThis.join(stepsFunction.call()))
                    .and(
                        loopThis
                            .join(stepsFunction.call())
                            .in(
                                loopThis
                                    .join(loop_p1)
                                    .plus(loopThis.join(loop_p2))
                                    .plus(loopThis.join(loop_p3))))));

    // ========== Define functions and predicates ==========

    // suppressTransfers
    Sig transfer = AlloyUtils.getReachableSig(alloy.transferModule, "o/Transfer");
    Expr suppressTransfersExpessionBody = transfer.no();
    Func suppressTransfersFunction =
        new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
    Expr suppressTransfersExpression = suppressTransfersFunction.call();

    // suppressIO
    Func inputs = AlloyUtils.getFunction(alloy.transferModule, "o/inputs");
    Func outputs = AlloyUtils.getFunction(alloy.transferModule, "o/outputs");
    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
    Func suppressIOFunction = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
    Expr suppressIOExpression = suppressIOFunction.call();

    // p1DuringExample
    Expr p1DuringExampleBody = abSig.in(loopSig.join(loop_p1));
    Func p1DuringExamplePredicate =
        new Func(null, "p1DuringExample", new ArrayList<>(), null, p1DuringExampleBody);
    Expr p1DuringExampleExpression = p1DuringExamplePredicate.call();

    // p2DuringExample
    Expr p2DuringExampleBody = abSig.in(loopSig.join(loop_p2));
    Func p2DuringExamplePredicate =
        new Func(null, "p2DuringExample", new ArrayList<>(), null, p2DuringExampleBody);
    Expr p2DuringExampleExpression = p2DuringExamplePredicate.call();

    // p3DuringExample
    Expr p3DuringExampleBody = abSig.in(loopSig.join(loop_p3));
    Func p3DuringExamplePredicate =
        new Func(null, "p3DuringExample", new ArrayList<>(), null, p3DuringExampleBody);
    Expr p3DuringExampleExpression = p3DuringExamplePredicate.call();

    // p4DuringExample
    Expr p4DuringExampleBody = abSig.no();
    Func p4DuringExamplePredicate =
        new Func(null, "p4DuringExample", new ArrayList<>(), null, p4DuringExampleBody);
    Expr p4DuringExampleExpression = p4DuringExamplePredicate.call();

    // instancesDuringExample
    Expr instancesDuringExampleBody =
        p1DuringExampleExpression
            .and(p2DuringExampleExpression)
            .and(p3DuringExampleExpression)
            .and(p4DuringExampleExpression);
    Func instancesDuringExamplePredicate =
        new Func(
            null, "instancesDuringExample", new ArrayList<>(), null, instancesDuringExampleBody);
    Expr instancesDuringExampleExpression = instancesDuringExamplePredicate.call();

    // instancesDuringExample
    Expr onlyLoopBody = loopSig.cardinality().equal(ExprConstant.makeNUMBER(1));
    Func onlyLoopPredicate = new Func(null, "onlyLoop", new ArrayList<>(), null, onlyLoopBody);
    Expr onlyLoopExpression = onlyLoopPredicate.call();

    // nonZeroDurationOnly
    // Func nonZeroDurationOnlyFunction = Helper.getFunction(alloy.transferModule,
    // "o/nonZeroDurationOnly");
    // Expr nonZeroDurationOnlyExpression = nonZeroDurationOnlyFunction.call();

    // ========== Define command(s) ==========

    // Expr loopExpr =
    // alloy.getCommonCmdExprs().and(instancesDuringExampleExpression).and(onlyLoopExpression);
    // Command loopCmd =
    // new Command(
    // null,
    // loopExpr,
    // "loop",
    // false,
    // 12,
    // -1,
    // -1,
    // -1,
    // new ArrayList<>(),
    // new ArrayList<>(),
    // loopExpr.and(alloy.getOverAllFact()),
    // null);

    Command[] commands = {};

    // ========== Write file ==========

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact(), commands);

    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());

    translator.generateAlsFileContents(alloyModule, outFileName);

    // ========== Import real AST from file ==========

    CompModule importedModule = AlloyUtils.importAlloyModule(filename);

    CompModule apiModule = AlloyUtils.importAlloyModule(outFileName);

    // ========== Test if they are equal ==========

    ExpressionComparator ec = new ExpressionComparator();

    Expr fileFacts = importedModule.getAllReachableFacts();
    Expr apiFacts = apiModule.getAllReachableFacts();
    List<Sig> fileSigs = importedModule.getAllReachableUserDefinedSigs();
    List<Sig> apiSigs = apiModule.getAllReachableUserDefinedSigs();

    Map<String, Sig> fileMap = new HashMap<>();
    Map<String, Sig> apiMap = new HashMap<>();

    for (Sig sig : fileSigs) {
      fileMap.put(AlloyUtils.removeSlash(sig.toString()), sig);
    }
    for (Sig sig : apiSigs) {
      apiMap.put(AlloyUtils.removeSlash(sig.toString()), sig);
    }

    assertTrue(ec.compareTwoExpressions(fileFacts, apiFacts));
    assertTrue(fileSigs.size() == apiSigs.size());

    for (String sigName : fileMap.keySet()) {
      assertTrue(apiMap.containsKey(sigName));
      assertTrue(ec.compareTwoExpressions(fileMap.get(sigName), apiMap.get(sigName)));
    }
  }
}
