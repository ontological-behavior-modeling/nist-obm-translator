package manualtest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.Translator;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import obmtest.ExpressionComparator;
import org.junit.jupiter.api.Test;

class LoopsExample_ExplicitFact_Test {

  @Test
  void test() {

    String moduleName = "LoopsExample_ExplicitFact";
    String outFileName = "src/test/resources/generated-" + moduleName + ".als";
    String filename = "src/test/resources/4.1.2 LoopsExample-Explicit-Facts.als";
    Alloy alloy = new Alloy("src/test/resources");

    // ========== Define list of signatures unique to the file ==========

    Sig abSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("AtomicBehavior");
    Sig loopSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Loop");

    // ========== Define list of relations unique to the file ==========

    Sig.Field loop_p1 = FuncUtils.addField("p1", loopSig, abSig);
    Sig.Field loop_p2 = FuncUtils.addField("p2", loopSig, abSig);
    Sig.Field loop_p3 = FuncUtils.addField("p3", loopSig, abSig);

    // ========== Define implicit facts ==========

    // ========== Define explicit facts ==========

    // functionFiltered[happensBefore, s.p1, s.p2]
    alloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(loopSig, loop_p1, loop_p2);

    // inverseFunctionFiltered[happensBefore, s.p1 + s.p2, s.p2]
    alloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(
        loopSig, new Expr[] {loop_p1, loop_p2}, new Expr[] {loop_p2});

    // functionFiltered[happensBefore, s.p2, s.p2 + s.p3]
    alloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(
        loopSig, new Expr[] {loop_p2}, new Expr[] {loop_p2, loop_p3});

    // inverseFunctionFiltered[happensBefore, s.p2, s.p3]
    alloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(loopSig, loop_p2, loop_p3);

    // #s.p1 = 1

    ExprVar s = ExprVar.make(null, "x", loopSig.type());
    Decl decl = new Decl(null, null, null, List.of(s), loopSig.oneOf());

    alloy.addToOverallFact(
        s.join(loop_p1).cardinality().equal(ExprConstant.makeNUMBER(1)).forAll(decl));

    // #s.p2 >= 2
    ExprVar s2 = ExprVar.make(null, "x", loopSig.type());
    Decl decl2 = new Decl(null, null, null, List.of(s2), loopSig.oneOf());

    alloy.addToOverallFact(
        s2.join(loop_p2).cardinality().gte(ExprConstant.makeNUMBER(2)).forAll(decl2));

    // #s.p3 >= 1
    ExprVar s3 = ExprVar.make(null, "x", loopSig.type());
    Decl decl3 = new Decl(null, null, null, List.of(s3), loopSig.oneOf());

    alloy.addToOverallFact(
        s3.join(loop_p3).cardinality().gte(ExprConstant.makeNUMBER(1)).forAll(decl3));

    // s.p1 + s.p2 + s.p3 in s.steps
    ExprVar s4 = ExprVar.make(null, "x", loopSig.type());
    Decl decl4 = new Decl(null, null, null, List.of(s4), loopSig.oneOf());

    Func stepFunctions = AlloyUtils.getFunction(Alloy.transferModule, "o/steps");

    alloy.addToOverallFact(
        s4.join(loop_p1)
            .plus(s4.join(loop_p2))
            .plus(s4.join(loop_p3))
            .in(s4.join(stepFunctions.call()))
            .forAll(decl4));

    // s.steps in s.p1 + s.p2 + s.p3
    ExprVar s5 = ExprVar.make(null, "s", loopSig.type());
    Decl decl5 = new Decl(null, null, null, List.of(s5), loopSig.oneOf());

    alloy.addToOverallFact(
        s5.join(stepFunctions.call())
            .in(s5.join(loop_p1).plus(s5.join(loop_p2)).plus(s5.join(loop_p3)))
            .forAll(decl5));

    // ========== Define command(s) ==========

    // Setup predicates used by command(s).

    // p1DuringExample
    // Expr p1DuringExampleExpr = abSig.in(loopSig.join(loop_p1));
    // Func p1DuringExampleFunc =
    // new Func(null, "p1DuringExample", List.of(), null, p1DuringExampleExpr);
    //
    // // p2DuringExample
    // Expr p2DuringExampleExpr = abSig.in(loopSig.join(loop_p2));
    // Func p2DuringExampleFunc =
    // new Func(null, "p2DuringExample", List.of(), null, p2DuringExampleExpr);
    //
    // // p3DuringExample
    // Expr p3DuringExampleExpr = abSig.in(loopSig.join(loop_p3));
    // Func p3DuringExampleFunc =
    // new Func(null, "p3DuringExample", List.of(), null, p3DuringExampleExpr);
    //
    // // p4DuringExample
    // Expr p4DuringExampleExpr = abSig.no();
    // Func p4DuringExampleFunc =
    // new Func(null, "p4DuringExample", List.of(), null, p4DuringExampleExpr);
    //
    // // instancesDuringExample
    // Expr instancesDuringExampleExpr =
    // p1DuringExampleExpr
    // .and(p2DuringExampleExpr)
    // .and(p3DuringExampleExpr)
    // .and(p4DuringExampleExpr);
    // Func instancesDuringExampleFunc =
    // new Func(null, "instancesDuringExample", List.of(), null, instancesDuringExampleExpr);
    //
    // // onlyLoop
    // Expr onlyLoopExpr = loopSig.cardinality().equal(ExprConstant.makeNUMBER(1));
    // Func onlyLoopFunc = new Func(null, "onlyLoop", List.of(), null, onlyLoopExpr);
    //
    // // run loop{nonZeroDurationOnly and suppressTransfers and suppressIO
    // // and instancesDuringExample and onlyLoop} for 12
    //
    // Expr loopExpr =
    // alloy.getCommonCmdExprs().and(instancesDuringExampleFunc.call()).and(onlyLoopFunc.call());
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
    // List.of(),
    // List.of(),
    // loopExpr.and(alloy.getOverAllFact()),
    // null);

    Command[] commands = new Command[] {};

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
