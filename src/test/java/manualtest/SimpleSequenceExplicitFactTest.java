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
import edu.mit.csail.sdg.ast.ExprHasName;
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

class SimpleSequenceExplicitFactTest {

  @Test
  void test() {

    String moduleName = "SimpleSequence_ExplicitFact";
    String outFileName = "src/test/resources/generated-" + moduleName + ".als";
    String filename = "src/test/resources/4.1.1 Control Nodes1 - SimpleSequence_modified.als";
    Alloy alloy = new Alloy("src/test/resources");

    // ========== Define list of signatures unique to the file ==========

    Sig abSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("AtomicBehavior");
    Sig mainSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("SimpleBehavior");

    // ========== Define list of relations unique to the file ==========

    Sig.Field p1 = FuncUtils.addField("p1", mainSig, abSig);
    Sig.Field p2 = FuncUtils.addField("p2", mainSig, abSig);

    // ========== Define explicit facts + functions/predicates ==========

    Func osteps = AlloyUtils.getFunction(Alloy.transferModule, "o/steps");
    Func funcFiltered = AlloyUtils.getFunction(Alloy.transferModule, "o/functionFiltered");
    Func happensBefore = AlloyUtils.getFunction(Alloy.transferModule, "o/happensBefore");
    Func inverseFuncFiltered =
        AlloyUtils.getFunction(Alloy.transferModule, "o/inverseFunctionFiltered");
    Expr ostepsExpr1 = osteps.call();
    Expr ostepsExpr2 = osteps.call();

    ExprVar s1 = ExprVar.make(null, "x", mainSig.type());
    ExprVar s2 = ExprVar.make(null, "x", mainSig.type());
    ExprVar s3 = ExprVar.make(null, "x", mainSig.type());
    ExprVar s4 = ExprVar.make(null, "x", mainSig.type());
    ExprVar s5 = ExprVar.make(null, "x", mainSig.type());
    ExprVar s6 = ExprVar.make(null, "x", mainSig.type());

    List<ExprHasName> names1 = new ArrayList<>(List.of(s1));
    List<ExprHasName> names2 = new ArrayList<>(List.of(s2));
    List<ExprHasName> names3 = new ArrayList<>(List.of(s3));
    List<ExprHasName> names4 = new ArrayList<>(List.of(s4));
    List<ExprHasName> names5 = new ArrayList<>(List.of(s5));
    List<ExprHasName> names6 = new ArrayList<>(List.of(s6));

    Decl decl1 = new Decl(null, null, null, names1, mainSig.oneOf());
    Decl decl2 = new Decl(null, null, null, names2, mainSig.oneOf());
    Decl decl3 = new Decl(null, null, null, names3, mainSig.oneOf());
    Decl decl4 = new Decl(null, null, null, names4, mainSig.oneOf());
    Decl decl5 = new Decl(null, null, null, names5, mainSig.oneOf());
    Decl decl6 = new Decl(null, null, null, names6, mainSig.oneOf());

    Expr funcFilteredExpr =
        funcFiltered.call(
            happensBefore.call(), s4.join(mainSig.domain(p1)), s4.join(mainSig.domain(p2)));
    Expr inverseFuncFilteredExpr =
        inverseFuncFiltered.call(
            happensBefore.call(), s5.join(mainSig.domain(p1)), s5.join(mainSig.domain(p2)));

    alloy.addToOverallFact(
        s1.join(mainSig.domain(p1))
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .forAll(decl1)
            .and(
                s6.join(mainSig.domain(p2))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(decl6))
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

    // Func nonZeroDurationOnlyFunction =
    // Helper.getFunction(Alloy.transferModule, "o/nonZeroDurationOnly");
    // Expr nonZeroDurationOnlyFunctionExpression = nonZeroDurationOnlyFunction.call();
    //
    // Sig transfer = Helper.getReachableSig(Alloy.transferModule, "o/Transfer");
    // Expr suppressTransfersExpessionBody = transfer.no();
    // Func suppressTransfersFunction =
    // new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
    // Expr suppressTransfersExpression = suppressTransfersFunction.call();
    //
    // Func inputs = Helper.getFunction(Alloy.transferModule, "o/inputs");
    // Func outputs = Helper.getFunction(Alloy.transferModule, "o/outputs");
    // Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
    // Func suppressIOFunction = new Func(null, "suppressIO", null, null,
    // suppressIOExpressionBody);
    // Expr suppressIOExpression = suppressIOFunction.call();
    //
    // Pos pos = null;
    // String label = "p1DuringExample";
    // List<Decl> decls = new ArrayList<>();
    // Expr returnDecl = null;
    // Expr body = p1Sig.in(mainSig.join(p1));
    // Func p1DuringExamplePredicate = new Func(pos, label, decls, returnDecl, body);
    // Expr p1DuringExampleExpression = p1DuringExamplePredicate.call();
    //
    // Func p2DuringExamplePredicate =
    // new Func(null, "p2DuringExample", new ArrayList<>(), null,
    // p2Sig.in(mainSig.join(p2)));
    // Expr p2DuringExampleExpression = p2DuringExamplePredicate.call();
    //
    // Func instancesDuringExamplePredicate = new Func(null, "instancesDuringExample",
    // new ArrayList<>(), null, p1DuringExampleExpression.and(p2DuringExampleExpression));
    // Expr instancesDuringExampleExpression = instancesDuringExamplePredicate.call();
    //
    // Func onlySimpleSequencePredicate = new Func(null, "onlySimpleSequence", new ArrayList<>(),
    // null,
    // mainSig.cardinality().equal(ExprConstant.makeNUMBER(1)));
    // Expr onlySimpleSequenceExpression = onlySimpleSequencePredicate.call();
    // Expr _nameExpr = (suppressTransfersExpression)
    // .and(suppressIOExpression).and(instancesDuringExampleExpression)
    // .and(onlySimpleSequenceExpression);
    //
    // // ========== Define command ==========
    //
    // Expr simpleSequenceExplicitFactExpr =
    // (suppressTransfersExpression).and(suppressIOExpression)
    // .and(instancesDuringExampleExpression).and(onlySimpleSequenceExpression);
    // Command simpleSequenceExplicitFactCmd =
    // new Command(null, simpleSequenceExplicitFactExpr, "SimpleBehavior", false, 6, -1, -1,
    // -1,
    // Arrays.asList(new CommandScope[] {}), Arrays.asList(new Sig[] {}),
    // simpleSequenceExplicitFactExpr.and(alloy.getOverAllFact()), null);

    // ========== Write file ==========

    Command[] commands = new Command[] {};

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact(), commands);

    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());

    translator.generateAlsFileContents(alloyModule, outFileName);

    // ========== Create Alloy file version ==========

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
