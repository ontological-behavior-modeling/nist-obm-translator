import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.Helper;
import edu.gatech.gtri.obm.translator.alloy.tofile.ExpressionComparator;
import edu.gatech.gtri.obm.translator.alloy.tofile.MyAlloyLibrary;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;

class ControlNodesTest {

  @Test
  void test() throws UnsupportedEncodingException {
    Alloy activity = new Alloy();

    // ========== Define list of signatures unique to the file ==========

    Sig p1Sig = activity.createSigAsChildOfOccSigAndAddToAllSigs("P1");
    Sig p2Sig = activity.createSigAsChildOfOccSigAndAddToAllSigs("P2");
    Sig p3Sig = activity.createSigAsChildOfOccSigAndAddToAllSigs("P3");
    Sig p4Sig = activity.createSigAsChildOfOccSigAndAddToAllSigs("P4");
    Sig p5Sig = activity.createSigAsChildOfOccSigAndAddToAllSigs("P5");
    Sig p6Sig = activity.createSigAsChildOfOccSigAndAddToAllSigs("P6");
    Sig p7Sig = activity.createSigAsChildOfOccSigAndAddToAllSigs("P7");
    Sig simpleSequenceSig = activity.createSigAsChildOfOccSigAndAddToAllSigs("SimpleSequence");
    Sig forkSig = activity.createSigAsChildOfOccSigAndAddToAllSigs("Fork");
    Sig joinSig = activity.createSigAsChildOfOccSigAndAddToAllSigs("Join");
    Sig decisionSig = activity.createSigAsChildOfOccSigAndAddToAllSigs("Decision");
    Sig mergeSig = activity.createSigAsChildOfOccSigAndAddToAllSigs("Merge");
    Sig allControlSig = activity.createSigAsChildOfOccSigAndAddToAllSigs("AllControl");

    // ========== Define list of relations unique to the file ==========

    Sig.Field simpleSequence_p1 = FuncUtils.addField("p1", simpleSequenceSig, p1Sig);
    Sig.Field simpleSequence_p2 = FuncUtils.addField("p2", simpleSequenceSig, p2Sig);

    Sig.Field fork_p1 = FuncUtils.addField("p1", forkSig, p1Sig);
    Sig.Field fork_p2 = FuncUtils.addField("p2", forkSig, p2Sig);
    Sig.Field fork_p3 = FuncUtils.addField("p3", forkSig, p3Sig);

    Sig.Field join_p1 = FuncUtils.addField("p1", joinSig, p1Sig);
    Sig.Field join_p2 = FuncUtils.addField("p2", joinSig, p2Sig);
    Sig.Field join_p3 = FuncUtils.addField("p3", joinSig, p3Sig);

    Sig.Field decision_p1 = FuncUtils.addField("p1", decisionSig, p1Sig);
    Sig.Field decision_p2 = FuncUtils.addField("p2", decisionSig, p2Sig);
    Sig.Field decision_p3 = FuncUtils.addField("p3", decisionSig, p3Sig);

    Sig.Field merge_p1 = FuncUtils.addField("p1", mergeSig, p1Sig);
    Sig.Field merge_p2 = FuncUtils.addField("p2", mergeSig, p2Sig);
    Sig.Field merge_p3 = FuncUtils.addField("p3", mergeSig, p3Sig);

    Sig.Field allControl_p1 = FuncUtils.addField("p1", allControlSig, p1Sig);
    Sig.Field allControl_p2 = FuncUtils.addField("p2", allControlSig, p2Sig);
    Sig.Field allControl_p3 = FuncUtils.addField("p3", allControlSig, p3Sig);
    Sig.Field allControl_p4 = FuncUtils.addField("p4", allControlSig, p4Sig);
    Sig.Field allControl_p5 = FuncUtils.addField("p5", allControlSig, p5Sig);
    Sig.Field allControl_p6 = FuncUtils.addField("p6", allControlSig, p6Sig);
    Sig.Field allControl_p7 = FuncUtils.addField("p7", allControlSig, p7Sig);

    // ========== Define implicit facts ==========

    // SimpleSequence

    Func stepsFunction = Helper.getFunction(activity.transferModule, "o/steps");
    Func functionFilteredFunction =
        Helper.getFunction(activity.transferModule, "o/functionFiltered");
    Func inverseFunctionFilteredFunction =
        Helper.getFunction(activity.transferModule, "o/inverseFunctionFiltered");
    Func happensBefore = Helper.getFunction(activity.transferModule, "o/happensBefore");

    ExprVar simpleSequenceThis = ExprVar.make(null, "this", simpleSequenceSig.type());

    Expr functionFilteredExpression = functionFilteredFunction.call(happensBefore.call(),
        simpleSequenceThis.join(simpleSequence_p1), simpleSequenceThis.join(simpleSequence_p2));

    Expr inverseFunctionFilteredExpression =
        inverseFunctionFilteredFunction.call(happensBefore.call(),
            simpleSequenceThis.join(simpleSequence_p1), simpleSequenceThis.join(simpleSequence_p2));

    simpleSequenceSig.addFact(functionFilteredExpression.and(inverseFunctionFilteredExpression)
        .and(simpleSequenceThis.join(simpleSequence_p1).cardinality()
            .equal(ExprConstant.makeNUMBER(1)))
        .and(simpleSequenceThis.join(simpleSequence_p1)
            .plus(simpleSequenceThis.join(simpleSequence_p2))
            .in(simpleSequenceThis.join(stepsFunction.call())))
        .and(simpleSequenceThis.join(stepsFunction.call()).in(simpleSequenceThis
            .join(simpleSequence_p1).plus(simpleSequenceThis.join(simpleSequence_p2)))));

    // Fork

    Func bijectionFilteredFunction =
        Helper.getFunction(activity.transferModule, "o/bijectionFiltered");

    ExprVar forkThis = ExprVar.make(null, "this", forkSig.type());

    Expr bijectionFilteredExpression1 = bijectionFilteredFunction.call(happensBefore.call(),
        forkThis.join(fork_p1), forkThis.join(fork_p2));
    Expr bijectionFilteredExpression2 = bijectionFilteredFunction.call(happensBefore.call(),
        forkThis.join(fork_p1), forkThis.join(fork_p3));

    forkSig.addFact(bijectionFilteredExpression1.and(bijectionFilteredExpression2)
        .and(forkThis.join(fork_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
        .and(forkThis.join(fork_p1).plus(forkThis.join(fork_p2)).plus(forkThis.join(fork_p3))
            .in(forkThis.join(stepsFunction.call())))
        .and(forkThis.join(stepsFunction.call())
            .in(forkThis.join(fork_p1).plus(forkThis.join(fork_p2)).plus(forkThis.join(fork_p3)))));

    // Join

    ExprVar joinThis = ExprVar.make(null, "this", joinSig.type());

    Expr bijectionFilteredExpression3 = bijectionFilteredFunction.call(happensBefore.call(),
        joinThis.join(join_p1), joinThis.join(join_p3));
    Expr bijectionFilteredExpression4 = bijectionFilteredFunction.call(happensBefore.call(),
        joinThis.join(join_p2), joinThis.join(join_p3));

    joinSig.addFact(bijectionFilteredExpression3.and(bijectionFilteredExpression4)
        .and(joinThis.join(join_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
        .and(joinThis.join(join_p2).cardinality().equal(ExprConstant.makeNUMBER(1)))
        .and(joinThis.join(join_p1).plus(joinThis.join(join_p2)).plus(joinThis.join(join_p3))
            .in(joinThis.join(stepsFunction.call())))
        .and(joinThis.join(stepsFunction.call())
            .in(joinThis.join(join_p1).plus(joinThis.join(join_p2)).plus(joinThis.join(join_p3)))));

    // Decision

    ExprVar decisionThis = ExprVar.make(null, "this", decisionSig.type());

    Expr bijectionFilteredExpression5 =
        bijectionFilteredFunction.call(happensBefore.call(), decisionThis.join(decision_p1),
            decisionThis.join(decision_p2).plus(decisionThis.join(decision_p3)));

    decisionSig.addFact(bijectionFilteredExpression5
        .and(decisionThis.join(decision_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
        .and(decisionThis.join(decision_p1).plus(decisionThis.join(decision_p2))
            .plus(decisionThis.join(decision_p3)).in(decisionThis.join(stepsFunction.call())))
        .and(decisionThis.join(stepsFunction.call()).in(decisionThis.join(decision_p1)
            .plus(decisionThis.join(decision_p2)).plus(decisionThis.join(decision_p3)))));

    // Merge

    ExprVar mergeThis = ExprVar.make(null, "this", mergeSig.type());

    mergeSig.addFact(bijectionFilteredFunction
        .call(happensBefore.call(), mergeThis.join(merge_p1).plus(mergeThis.join(merge_p2)),
            mergeThis.join(merge_p3))
        .and(mergeThis.join(merge_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
        .and(mergeThis.join(merge_p2).cardinality().equal(ExprConstant.makeNUMBER(1)))
        .and(mergeThis.join(merge_p1).plus(mergeThis.join(merge_p2)).plus(mergeThis.join(merge_p3))
            .in(mergeThis.join(stepsFunction.call())))
        .and(mergeThis.join(stepsFunction.call()).in(mergeThis.join(merge_p1)
            .plus(mergeThis.join(merge_p2)).plus(mergeThis.join(merge_p3)))));

    // AllControl

    ExprVar allControlThis = ExprVar.make(null, "this", allControlSig.type());

    allControlSig.addFact(bijectionFilteredFunction
        .call(happensBefore.call(), allControlThis.join(allControl_p1),
            allControlThis.join(allControl_p2))
        .and(bijectionFilteredFunction.call(happensBefore.call(),
            allControlThis.join(allControl_p1), allControlThis.join(allControl_p3)))
        .and(bijectionFilteredFunction.call(happensBefore.call(),
            allControlThis.join(allControl_p2), allControlThis.join(allControl_p4)))
        .and(bijectionFilteredFunction.call(happensBefore.call(),
            allControlThis.join(allControl_p3), allControlThis.join(allControl_p4)))
        .and(
            bijectionFilteredFunction.call(happensBefore.call(), allControlThis.join(allControl_p4),
                allControlThis.join(allControl_p5).plus(allControlThis.join(allControl_p6))))
        .and(bijectionFilteredFunction.call(happensBefore.call(),
            allControlThis.join(allControl_p5).plus(allControlThis.join(allControl_p6)),
            allControlThis.join(allControl_p7)))
        .and(allControlThis.join(allControl_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
        .and(allControlThis.join(allControl_p1).plus(allControlThis.join(allControl_p2))
            .plus(allControlThis.join(allControl_p3)).plus(allControlThis.join(allControl_p4))
            .plus(allControlThis.join(allControl_p5)).plus(allControlThis.join(allControl_p6))
            .plus(allControlThis.join(allControl_p7)).in(allControlThis.join(stepsFunction.call())))
        .and(allControlThis.join(stepsFunction.call())
            .in(allControlThis.join(allControl_p1).plus(allControlThis.join(allControl_p2))
                .plus(allControlThis.join(allControl_p3)).plus(allControlThis.join(allControl_p4))
                .plus(allControlThis.join(allControl_p5)).plus(allControlThis.join(allControl_p6))
                .plus(allControlThis.join(allControl_p7)))));

    // ========== Define functions and predicates ==========

    // suppressTransfers
    Sig transfer = Helper.getReachableSig(activity.transferModule, "o/Transfer");
    Expr suppressTransfersExpessionBody = transfer.no();
    Func suppressTransfersFunction =
        new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
    Expr suppressTransfersExpression = suppressTransfersFunction.call();

    // suppressIO
    Func inputs = Helper.getFunction(activity.transferModule, "o/inputs");
    Func outputs = Helper.getFunction(activity.transferModule, "o/outputs");
    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
    Func suppressIOFunction = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
    Expr suppressIOExpression = suppressIOFunction.call();

    // p1DuringExample
    Expr p1DuringExampleBody = p1Sig.in(simpleSequenceSig.join(simpleSequence_p1)
        .plus(forkSig.join(fork_p1)).plus(joinSig.join(join_p1)).plus(decisionSig.join(decision_p1))
        .plus(mergeSig.join(merge_p1)).plus(allControlSig.join(allControl_p1)));
    Func p1DuringExamplePredicate =
        new Func(null, "p1DuringExample", new ArrayList<>(), null, p1DuringExampleBody);
    Expr p1DuringExampleExpression = p1DuringExamplePredicate.call();

    // p2DuringExample
    Expr p2DuringExampleBody = p2Sig.in(simpleSequenceSig.join(simpleSequence_p2)
        .plus(forkSig.join(fork_p2).plus(joinSig.join(join_p2)).plus(decisionSig.join(decision_p2)
            .plus(mergeSig.join(merge_p2).plus(allControlSig.join(allControl_p2))))));
    Func p2DuringExamplePredicate =
        new Func(null, "p2DuringExample", new ArrayList<>(), null, p2DuringExampleBody);
    Expr p2DuringExampleExpression = p2DuringExamplePredicate.call();

    // p3DuringExample
    Expr p3DuringExampleBody =
        p3Sig.in(forkSig.join(fork_p3).plus(joinSig.join(join_p3).plus(decisionSig.join(decision_p3)
            .plus(mergeSig.join(merge_p3).plus(allControlSig.join(allControl_p3))))));
    Func p3DuringExamplePredicate =
        new Func(null, "p3DuringExample", new ArrayList<>(), null, p3DuringExampleBody);
    Expr p3DuringExampleExpression = p3DuringExamplePredicate.call();

    // p4DuringExample
    Expr p4DuringExampleBody = p4Sig.in(allControlSig.join(allControl_p4));
    Func p4DuringExamplePredicate =
        new Func(null, "p4DuringExample", new ArrayList<>(), null, p4DuringExampleBody);
    Expr p4DuringExampleExpression = p4DuringExamplePredicate.call();

    // p5DuringExample
    Expr p5DuringExampleBody = p5Sig.in(allControlSig.join(allControl_p5));
    Func p5DuringExamplePredicate =
        new Func(null, "p5DuringExample", new ArrayList<>(), null, p5DuringExampleBody);
    Expr p5DuringExampleExpression = p5DuringExamplePredicate.call();

    // p6DuringExample
    Expr p6DuringExampleBody = p6Sig.in(allControlSig.join(allControl_p6));
    Func p6DuringExamplePredicate =
        new Func(null, "p6DuringExample", new ArrayList<>(), null, p6DuringExampleBody);
    Expr p6DuringExampleExpression = p6DuringExamplePredicate.call();

    // p6DuringExample
    Expr p7DuringExampleBody = p7Sig.in(allControlSig.join(allControl_p7));
    Func p7DuringExamplePredicate =
        new Func(null, "p7DuringExample", new ArrayList<>(), null, p7DuringExampleBody);
    Expr p7DuringExampleExpression = p7DuringExamplePredicate.call();

    // instancesDuringExample
    Expr instancesDuringExampleBody =
        p1DuringExampleExpression.and(p2DuringExampleExpression).and(p3DuringExampleExpression)
            .and(p4DuringExampleExpression).and(p5DuringExampleExpression)
            .and(p6DuringExampleExpression).and(p7DuringExampleExpression);
    Func instancesDuringExamplePredicate = new Func(null, "instancesDuringExample",
        new ArrayList<>(), null, instancesDuringExampleBody);
    Expr instancesDuringExampleExpression = instancesDuringExamplePredicate.call();

    // onlySimpleSequence
    Expr onlySimpleSequenceBody =
        simpleSequenceSig.cardinality().equal(ExprConstant.makeNUMBER(1)).and(forkSig.no())
            .and(joinSig.no()).and(decisionSig.no()).and(mergeSig.no()).and(allControlSig.no());
    Func onlySimpleSequencePredicate =
        new Func(null, "onlySimpleSequence", new ArrayList<>(), null, onlySimpleSequenceBody);
    Expr onlySimpleSequenceExpression = onlySimpleSequencePredicate.call();

    // onlyFork
    Expr onlyForkBody =
        simpleSequenceSig.no().and(forkSig.cardinality().equal(ExprConstant.makeNUMBER(1)))
            .and(joinSig.no()).and(decisionSig.no()).and(mergeSig.no()).and(allControlSig.no());
    Func onlyForkPredicate = new Func(null, "onlyFork", new ArrayList<>(), null, onlyForkBody);
    Expr onlyForkExpression = onlyForkPredicate.call();

    // onlyJoin
    Expr onlyJoinBody = simpleSequenceSig.no().and(forkSig.no())
        .and(joinSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(decisionSig.no())
        .and(mergeSig.no()).and(allControlSig.no());
    Func onlyJoinPredicate = new Func(null, "onlyJoin", new ArrayList<>(), null, onlyJoinBody);
    Expr onlyJoinExpression = onlyJoinPredicate.call();

    // onlyDecision
    Expr onlyDecisionBody = simpleSequenceSig.no().and(forkSig.no()).and(joinSig.no())
        .and(decisionSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(mergeSig.no())
        .and(allControlSig.no());
    Func onlyDecisionPredicate =
        new Func(null, "onlyDecision", new ArrayList<>(), null, onlyDecisionBody);
    Expr onlyDecisionExpression = onlyDecisionPredicate.call();

    // onlyMerge
    Expr onlyMergeBody =
        simpleSequenceSig.no().and(forkSig.no()).and(joinSig.no()).and(decisionSig.no())
            .and(mergeSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(allControlSig.no());
    Func onlyMergePredicate = new Func(null, "onlyMerge", new ArrayList<>(), null, onlyMergeBody);
    Expr onlyMergeExpression = onlyMergePredicate.call();

    // onlyAllControl
    Expr onlyAllControlBody =
        simpleSequenceSig.no().and(forkSig.no()).and(joinSig.no()).and(decisionSig.no())
            .and(mergeSig.no()).and(allControlSig.cardinality().equal(ExprConstant.makeNUMBER(1)));
    Func onlyAllControlPredicate =
        new Func(null, "onlyAllControl", new ArrayList<>(), null, onlyAllControlBody);
    Expr onlyAllControlExpression = onlyAllControlPredicate.call();

    // nonZeroDurationOnly
    Func nonZeroDurationOnlyFunction =
        Helper.getFunction(activity.transferModule, "o/nonZeroDurationOnly");
    Expr nonZeroDurationOnlyExpression = nonZeroDurationOnlyFunction.call();

    // ========== Import real AST from file ==========

    String path = URLDecoder.decode(
        CallingBehaviorsTest.class.getResource("/4.1.1 ControlNodesExamples.als").getPath(),
        "UTF-8");
    File testFile = new File(path);
    if (testFile.exists())
      assertFalse(false, testFile.getAbsolutePath() + " is missing");
    CompModule importedModule = MyAlloyLibrary.importAlloyModule(testFile);

    // ========== Test if they are equal ==========

    ExpressionComparator ec = new ExpressionComparator();

    Expr fileFacts = importedModule.getAllReachableFacts();
    Expr apiFacts = activity.getOverAllFact();
    List<Sig> fileSigs = importedModule.getAllReachableUserDefinedSigs();
    List<Sig> apiSigs = activity.getAllSigs();

    Map<String, Sig> fileMap = new HashMap<>();
    Map<String, Sig> apiMap = new HashMap<>();

    for (Sig sig : fileSigs) {
      fileMap.put(MyAlloyLibrary.removeSlash(sig.toString()), sig);
    }
    for (Sig sig : apiSigs) {
      apiMap.put(MyAlloyLibrary.removeSlash(sig.toString()), sig);
    }

    assertTrue(ec.compareTwoExpressions(fileFacts, apiFacts));
    assertTrue(fileSigs.size() == apiSigs.size());

    for (String sigName : fileMap.keySet()) {
      assertTrue(apiMap.containsKey(sigName));
      assertTrue(ec.compareTwoExpressions(fileMap.get(sigName), apiMap.get(sigName)));
    }
  }

}
