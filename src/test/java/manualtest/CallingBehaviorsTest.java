package manualtest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.Translator;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprHasName;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import obmtest.ExpressionComparator;
import org.junit.jupiter.api.Test;

class CallingBehaviorsTest {

  @Test
  void test() {
    String manualfile = "src/test/resources/4.1.3 CallingBehaviors.als";
    Alloy alloy = new Alloy("src/test/resources");

    // ========== Define list of signatures unique to the file ==========

    Sig abSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("AtomicBehavior");

    Sig nestedBehaviorSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("NestedBehavior");
    Sig composedBehaviorSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("ComposedBehavior");

    // ========== Define list of relations unique to the file ==========

    Sig.Field nestedBehavior_p4 = FuncUtils.addField("p4", nestedBehaviorSig, abSig);
    Sig.Field nestedBehavior_p5 = FuncUtils.addField("p5", nestedBehaviorSig, abSig);

    Sig.Field composedBehavior_p1 = FuncUtils.addField("p1", composedBehaviorSig, abSig);
    Sig.Field composedBehavior_p2 =
        FuncUtils.addField("p2", composedBehaviorSig, nestedBehaviorSig);
    Sig.Field composedBehavior_p3 = FuncUtils.addField("p3", composedBehaviorSig, abSig);

    // ========== Define implicit facts ==========

    Func bijectionFilteredFunction =
        AlloyUtils.getFunction(Alloy.transferModule, "o/bijectionFiltered");
    Func happensBeforeFunction = AlloyUtils.getFunction(Alloy.transferModule, "o/happensBefore");
    Func stepsFunction = AlloyUtils.getFunction(Alloy.transferModule, "o/steps");

    // NestedBehavior
    ExprVar s1 = ExprVar.make(null, "x", nestedBehaviorSig.type());
    ExprVar s2 = ExprVar.make(null, "x", nestedBehaviorSig.type());
    ExprVar s3 = ExprVar.make(null, "x", nestedBehaviorSig.type());
    ExprVar s4 = ExprVar.make(null, "x", nestedBehaviorSig.type());

    List<ExprHasName> names1 = new ArrayList<>(List.of(s1));
    List<ExprHasName> names2 = new ArrayList<>(List.of(s2));
    List<ExprHasName> names3 = new ArrayList<>(List.of(s3));
    List<ExprHasName> names4 = new ArrayList<>(List.of(s4));

    Decl decl1 = new Decl(null, null, null, names1, nestedBehaviorSig.oneOf());
    Decl decl2 = new Decl(null, null, null, names2, nestedBehaviorSig.oneOf());
    Decl decl3 = new Decl(null, null, null, names3, nestedBehaviorSig.oneOf());
    Decl decl4 = new Decl(null, null, null, names4, nestedBehaviorSig.oneOf());

    alloy.addToOverallFact(
        (bijectionFilteredFunction
                .call(
                    happensBeforeFunction.call(),
                    nestedBehaviorSig.join(nestedBehavior_p4),
                    nestedBehaviorSig.join(nestedBehavior_p5))
                .forAll(decl1))
            .and(
                nestedBehaviorSig
                    .join(nestedBehavior_p4)
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(decl2))
            .and(
                nestedBehaviorSig
                    .join(nestedBehaviorSig.domain(nestedBehavior_p4))
                    .plus(nestedBehaviorSig.join(nestedBehaviorSig.domain(nestedBehavior_p5)))
                    .in(nestedBehaviorSig.join(stepsFunction.call()))
                    .forAll(decl3))
            .and(
                nestedBehaviorSig
                    .join(stepsFunction.call())
                    .in(
                        nestedBehaviorSig
                            .join(nestedBehaviorSig.domain(nestedBehavior_p4))
                            .plus(
                                nestedBehaviorSig.join(
                                    nestedBehaviorSig.domain(nestedBehavior_p5))))
                    .forAll(decl4)));

    // ComposedBehavior
    ExprVar c_s1 = ExprVar.make(null, "x", composedBehaviorSig.type());
    ExprVar c_s2 = ExprVar.make(null, "x", composedBehaviorSig.type());
    ExprVar c_s3 = ExprVar.make(null, "x", composedBehaviorSig.type());
    ExprVar c_s4 = ExprVar.make(null, "x", composedBehaviorSig.type());
    ExprVar c_s5 = ExprVar.make(null, "x", composedBehaviorSig.type());

    List<ExprHasName> c_names1 = new ArrayList<>(List.of(c_s1));
    List<ExprHasName> c_names2 = new ArrayList<>(List.of(c_s2));
    List<ExprHasName> c_names3 = new ArrayList<>(List.of(c_s3));
    List<ExprHasName> c_names4 = new ArrayList<>(List.of(c_s4));
    List<ExprHasName> c_names5 = new ArrayList<>(List.of(c_s5));

    Decl c_decl1 = new Decl(null, null, null, c_names1, composedBehaviorSig.oneOf());
    Decl c_decl2 = new Decl(null, null, null, c_names2, composedBehaviorSig.oneOf());
    Decl c_decl3 = new Decl(null, null, null, c_names3, composedBehaviorSig.oneOf());
    Decl c_decl4 = new Decl(null, null, null, c_names4, composedBehaviorSig.oneOf());
    Decl c_decl5 = new Decl(null, null, null, c_names5, composedBehaviorSig.oneOf());

    alloy.addToOverallFact(
        (bijectionFilteredFunction
                .call(
                    happensBeforeFunction.call(),
                    composedBehaviorSig.join(composedBehaviorSig.domain(composedBehavior_p1)),
                    composedBehaviorSig.join(composedBehaviorSig.domain(composedBehavior_p2)))
                .forAll(c_decl1))
            .and(
                bijectionFilteredFunction
                    .call(
                        happensBeforeFunction.call(),
                        composedBehaviorSig.join(composedBehaviorSig.domain(composedBehavior_p2)),
                        composedBehaviorSig.join(composedBehaviorSig.domain(composedBehavior_p3)))
                    .forAll(c_decl2))
            .and(
                composedBehaviorSig
                    .join(composedBehaviorSig.domain(composedBehavior_p1))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(c_decl3))
            .and(
                composedBehaviorSig
                    .join(composedBehavior_p1)
                    .plus(composedBehaviorSig.join(composedBehaviorSig.domain(composedBehavior_p2)))
                    .plus(composedBehaviorSig.join(composedBehaviorSig.domain(composedBehavior_p3)))
                    .in(composedBehaviorSig.join(stepsFunction.call()))
                    .forAll(c_decl4))
            .and(
                composedBehaviorSig
                    .join(stepsFunction.call())
                    .in(
                        composedBehaviorSig
                            .join(composedBehaviorSig.domain(composedBehavior_p1))
                            .plus(
                                composedBehaviorSig.join(
                                    composedBehaviorSig.domain(composedBehavior_p2)))
                            .plus(
                                composedBehaviorSig.join(
                                    composedBehaviorSig.domain(composedBehavior_p3))))
                    .forAll(c_decl5)));

    // ========== Define functions and predicates ==========

    // suppressTransfers
    Sig transfer = AlloyUtils.getReachableSig(Alloy.transferModule, "o/Transfer");
    Expr suppressTransfersExpessionBody = transfer.no();
    Func suppressTransfersFunction =
        new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
    Expr suppressTransfersExpression = suppressTransfersFunction.call();

    // suppressIO
    Func inputs = AlloyUtils.getFunction(Alloy.transferModule, "o/inputs");
    Func outputs = AlloyUtils.getFunction(Alloy.transferModule, "o/outputs");
    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
    Func suppressIOFunction = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
    Expr suppressIOExpression = suppressIOFunction.call();

    // p1DuringExample
    Expr p1DuringExampleBody = abSig.in(composedBehaviorSig.join(composedBehavior_p1));
    Func p1DuringExamplePredicate =
        new Func(null, "p1DuringExample", new ArrayList<>(), null, p1DuringExampleBody);
    Expr p1DuringExampleExpression = p1DuringExamplePredicate.call();

    // p2DuringExample
    Expr p2DuringExampleBody = nestedBehaviorSig.in(composedBehaviorSig.join(composedBehavior_p2));
    Func p2DuringExamplePredicate =
        new Func(null, "p2DuringExample", new ArrayList<>(), null, p2DuringExampleBody);
    Expr p2DuringExampleExpression = p2DuringExamplePredicate.call();

    // p3DuringExample
    Expr p3DuringExampleBody = abSig.in(composedBehaviorSig.join(composedBehavior_p3));
    Func p3DuringExamplePredicate =
        new Func(null, "p3DuringExample", new ArrayList<>(), null, p3DuringExampleBody);
    Expr p3DuringExampleExpression = p3DuringExamplePredicate.call();

    // p4DuringExample
    Expr p4DuringExampleBody = abSig.in(nestedBehaviorSig.join(nestedBehavior_p4));
    Func p4DuringExamplePredicate =
        new Func(null, "p4DuringExample", new ArrayList<>(), null, p4DuringExampleBody);
    Expr p4DuringExampleExpression = p4DuringExamplePredicate.call();

    // p5DuringExample
    Expr p5DuringExampleBody = abSig.in(nestedBehaviorSig.join(nestedBehavior_p5));
    Func p5DuringExamplePredicate =
        new Func(null, "p5DuringExample", new ArrayList<>(), null, p5DuringExampleBody);
    Expr p5DuringExampleExpression = p5DuringExamplePredicate.call();

    // instancesDuringExample
    Expr instancesDuringExampleBody =
        p1DuringExampleExpression
            .and(p2DuringExampleExpression)
            .and(p3DuringExampleExpression)
            .and(p4DuringExampleExpression)
            .and(p5DuringExampleExpression);
    Func instancesDuringExampleFunction =
        new Func(
            null, "instancesDuringExample", new ArrayList<>(), null, instancesDuringExampleBody);
    Expr instancesDuringExampleExpression = instancesDuringExampleFunction.call();

    // onlyComposedBehavior
    Expr onlyComposedBehaviorBody =
        composedBehaviorSig.cardinality().equal(ExprConstant.makeNUMBER(1));
    Func onlyComposedBehaviorFunction =
        new Func(null, "onlyComposedBehavior", new ArrayList<>(), null, onlyComposedBehaviorBody);
    Expr onlyComposedBehaviorExpression = onlyComposedBehaviorFunction.call();

    // nonZeroDurationOnly
    // Func nonZeroDurationOnlyFunction =
    // Helper.getFunction(Alloy.transferModule, "o/nonZeroDurationOnly");
    // Expr nonZeroDurationOnlyExpression = nonZeroDurationOnlyFunction.call();

    // ========== Define command ==========
    Expr composedBehaviorCmdExpr =
        (suppressTransfersExpression)
            .and(suppressIOExpression)
            .and(instancesDuringExampleExpression)
            .and(onlyComposedBehaviorExpression);
    Command composedBehaviorCmd =
        new Command(
            null,
            composedBehaviorCmdExpr,
            "composedBehavior",
            false,
            6,
            -1,
            -1,
            -1,
            Arrays.asList(new CommandScope[] {}),
            Arrays.asList(new Sig[] {}),
            composedBehaviorCmdExpr.and(alloy.getOverAllFact()),
            null);

    // ========== Write file ==========
    AlloyModule alloyModule =
        new AlloyModule(
            "CallingBehaviors",
            alloy.getAllSigs(),
            alloy.getOverAllFact(),
            new Command[] {composedBehaviorCmd});
    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());
    String outFileName = "src/test/resources/generated-" + alloyModule.getModuleName() + ".als";
    translator.generateAlsFileContents(alloyModule, outFileName);

    // ========== Create Alloy file version ==========

    CompModule importedModule = AlloyUtils.importAlloyModule(manualfile);

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
