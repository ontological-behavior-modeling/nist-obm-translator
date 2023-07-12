package manualtest;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.Helper;
import edu.gatech.gtri.obm.translator.alloy.tofile.MyAlloyLibrary;
import edu.gatech.gtri.obm.translator.alloy.tofile.Translator;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.gatech.gtri.obm.translator.alloy.tofile.ExpressionComparator;

class ControlNodesTest {

	@Test
	void test() {
		Alloy alloy = new Alloy();
		
		// ========== Define list of signatures unique to the file ==========
		
		Sig p1Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P1");
		Sig p2Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P2");
		Sig p3Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P3");
		Sig p4Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P4");
		Sig p5Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P5");
		Sig p6Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P6");
		Sig p7Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P7");
		Sig simpleSequenceSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("SimpleSequence");
		Sig forkSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Fork");
		Sig joinSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Join");
		Sig decisionSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Decision");
		Sig mergeSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Merge");
		Sig allControlSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("AllControl");
		
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
		
		Func stepsFunction = Helper.getFunction(alloy.transferModule, "o/steps");
		Func functionFilteredFunction = Helper.getFunction(alloy.transferModule, "o/functionFiltered");
		Func inverseFunctionFilteredFunction = Helper.getFunction(alloy.transferModule, "o/inverseFunctionFiltered");
		Func happensBefore = Helper.getFunction(alloy.transferModule, "o/happensBefore");
		
		ExprVar simpleSequenceThis = ExprVar.make(null, "this", simpleSequenceSig.type());
		
		Expr functionFilteredExpr = functionFilteredFunction.call(
		happensBefore.call(), simpleSequenceThis.join(simpleSequence_p1), simpleSequenceThis.join(simpleSequence_p2));  
		
	    Expr inverseFunctionFilteredExpr = inverseFunctionFilteredFunction.call(
		happensBefore.call(), simpleSequenceThis.join(simpleSequence_p1), simpleSequenceThis.join(simpleSequence_p2));
		
		simpleSequenceSig.addFact(
			functionFilteredExpr
    		.and(inverseFunctionFilteredExpr)
    		.and(simpleSequenceThis.join(simpleSequence_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
    		.and(simpleSequenceThis.join(simpleSequence_p1).plus(simpleSequenceThis.join(simpleSequence_p2)).in(simpleSequenceThis.join(stepsFunction.call())))
    		.and(simpleSequenceThis.join(stepsFunction.call()).in(simpleSequenceThis.join(simpleSequence_p1).plus(simpleSequenceThis.join(simpleSequence_p2)))));
		
		// Fork
		
		Func bijectionFilteredFunction = Helper.getFunction(alloy.transferModule, "o/bijectionFiltered");
		
		ExprVar forkThis = ExprVar.make(null, "this", forkSig.type());
		
		Expr bijectionFilteredExpr1 = bijectionFilteredFunction.call(happensBefore.call(), forkThis.join(fork_p1), forkThis.join(fork_p2));
		Expr bijectionFilteredExpr2 = bijectionFilteredFunction.call(happensBefore.call(), forkThis.join(fork_p1), forkThis.join(fork_p3));
		
		forkSig.addFact(bijectionFilteredExpr1.and(bijectionFilteredExpr2)
    		.and(forkThis.join(fork_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
    		.and(forkThis.join(fork_p1).plus(forkThis.join(fork_p2)).plus(forkThis.join(fork_p3)).in(forkThis.join(stepsFunction.call())))
    		.and(forkThis.join(stepsFunction.call()).in(forkThis.join(fork_p1).plus(forkThis.join(fork_p2)).plus(forkThis.join(fork_p3)))));
		
		// Join
		
		ExprVar joinThis = ExprVar.make(null, "this", joinSig.type());
		
		Expr bijectionFilteredExpr3 = bijectionFilteredFunction.call(happensBefore.call(), joinThis.join(join_p1), joinThis.join(join_p3));
		Expr bijectionFilteredExpr4 = bijectionFilteredFunction.call(happensBefore.call(), joinThis.join(join_p2), joinThis.join(join_p3));
		
		joinSig.addFact(bijectionFilteredExpr3.and(bijectionFilteredExpr4)
		.and(joinThis.join(join_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
		.and(joinThis.join(join_p2).cardinality().equal(ExprConstant.makeNUMBER(1)))
		.and(joinThis.join(join_p1).plus(joinThis.join(join_p2)).plus(joinThis.join(join_p3)).in(joinThis.join(stepsFunction.call())))
		.and(joinThis.join(stepsFunction.call()).in(joinThis.join(join_p1).plus(joinThis.join(join_p2)).plus(joinThis.join(join_p3)))));
		
		// Decision
		
		ExprVar decisionThis = ExprVar.make(null, "this", decisionSig.type());
		
		Expr bijectionFilteredExpr5 = bijectionFilteredFunction.call(happensBefore.call(), decisionThis.join(decision_p1), decisionThis.join(decision_p2).plus(decisionThis.join(decision_p3)));
				
		decisionSig.addFact(bijectionFilteredExpr5
		.and(decisionThis.join(decision_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
		.and(decisionThis.join(decision_p1).plus(decisionThis.join(decision_p2)).plus(decisionThis.join(decision_p3)).in(decisionThis.join(stepsFunction.call())))
		.and(decisionThis.join(stepsFunction.call()).in(decisionThis.join(decision_p1).plus(decisionThis.join(decision_p2)).plus(decisionThis.join(decision_p3)))));
		
		// Merge
		
		ExprVar mergeThis = ExprVar.make(null, "this", mergeSig.type());
		
		mergeSig.addFact(bijectionFilteredFunction.call(happensBefore.call(), mergeThis.join(merge_p1).plus(mergeThis.join(merge_p2)), mergeThis.join(merge_p3))
		.and(mergeThis.join(merge_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
		.and(mergeThis.join(merge_p2).cardinality().equal(ExprConstant.makeNUMBER(1)))
		.and(mergeThis.join(merge_p1).plus(mergeThis.join(merge_p2)).plus(mergeThis.join(merge_p3)).in(mergeThis.join(stepsFunction.call())))
		.and(mergeThis.join(stepsFunction.call()).in(mergeThis.join(merge_p1).plus(mergeThis.join(merge_p2)).plus(mergeThis.join(merge_p3)))));
		
		// AllControl
		
		ExprVar allControlThis = ExprVar.make(null, "this", allControlSig.type());
		
		allControlSig.addFact(
		bijectionFilteredFunction.call(happensBefore.call(), allControlThis.join(allControl_p1), allControlThis.join(allControl_p2))
		.and(bijectionFilteredFunction.call(happensBefore.call(), allControlThis.join(allControl_p1), allControlThis.join(allControl_p3)))
		.and(bijectionFilteredFunction.call(happensBefore.call(), allControlThis.join(allControl_p2), allControlThis.join(allControl_p4)))
		.and(bijectionFilteredFunction.call(happensBefore.call(), allControlThis.join(allControl_p3), allControlThis.join(allControl_p4)))
		.and(bijectionFilteredFunction.call(happensBefore.call(), allControlThis.join(allControl_p4), allControlThis.join(allControl_p5).plus(allControlThis.join(allControl_p6))))
		.and(bijectionFilteredFunction.call(happensBefore.call(), allControlThis.join(allControl_p5).plus(allControlThis.join(allControl_p6)), allControlThis.join(allControl_p7)))
		.and(allControlThis.join(allControl_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
		.and(allControlThis.join(allControl_p1).plus(allControlThis.join(allControl_p2)).plus(allControlThis.join(allControl_p3)).plus(allControlThis.join(allControl_p4)).plus(allControlThis.join(allControl_p5)).plus(allControlThis.join(allControl_p6)).plus(allControlThis.join(allControl_p7)).in(allControlThis.join(stepsFunction.call())))
		.and(allControlThis.join(stepsFunction.call()).in(allControlThis.join(allControl_p1).plus(allControlThis.join(allControl_p2)).plus(allControlThis.join(allControl_p3)).plus(allControlThis.join(allControl_p4)).plus(allControlThis.join(allControl_p5)).plus(allControlThis.join(allControl_p6)).plus(allControlThis.join(allControl_p7)))));
		
		// ========== Define functions and predicates ==========
	    
	    // p1DuringExample
	    Expr p1DuringExampleBody = p1Sig.in(simpleSequenceSig.join(simpleSequence_p1).plus(forkSig.join(fork_p1)).plus(joinSig.join(join_p1)).plus(decisionSig.join(decision_p1)).plus(mergeSig.join(merge_p1)).plus(allControlSig.join(allControl_p1)));
	    Func p1DuringExamplePredicate = new Func(null, "p1DuringExample", new ArrayList<>(), null, p1DuringExampleBody);
	    Expr p1DuringExampleExpr = p1DuringExamplePredicate.call();
	    
	    // p2DuringExample
	    Expr p2DuringExampleBody = p2Sig.in(simpleSequenceSig.join(simpleSequence_p2).plus(forkSig.join(fork_p2).plus(joinSig.join(join_p2)).plus(decisionSig.join(decision_p2).plus(mergeSig.join(merge_p2).plus(allControlSig.join(allControl_p2))))));
	    Func p2DuringExamplePredicate = new Func(null, "p2DuringExample", new ArrayList<>(), null, p2DuringExampleBody);
	    Expr p2DuringExampleExpr = p2DuringExamplePredicate.call();
	    
	    // p3DuringExample
	    Expr p3DuringExampleBody = p3Sig.in(forkSig.join(fork_p3).plus(joinSig.join(join_p3).plus(decisionSig.join(decision_p3).plus(mergeSig.join(merge_p3).plus(allControlSig.join(allControl_p3))))));
	    Func p3DuringExamplePredicate = new Func(null, "p3DuringExample", new ArrayList<>(), null, p3DuringExampleBody);
	    Expr p3DuringExampleExpr = p3DuringExamplePredicate.call();
	    
	    // p4DuringExample
	    Expr p4DuringExampleBody = p4Sig.in(allControlSig.join(allControl_p4));
	    Func p4DuringExamplePredicate = new Func(null, "p4DuringExample", new ArrayList<>(), null, p4DuringExampleBody);
	    Expr p4DuringExampleExpr = p4DuringExamplePredicate.call();
	    
	    // p5DuringExample
	    Expr p5DuringExampleBody = p5Sig.in(allControlSig.join(allControl_p5));
	    Func p5DuringExamplePredicate = new Func(null, "p5DuringExample", new ArrayList<>(), null, p5DuringExampleBody);
	    Expr p5DuringExampleExpr = p5DuringExamplePredicate.call();
	    
	    // p6DuringExample
	    Expr p6DuringExampleBody = p6Sig.in(allControlSig.join(allControl_p6));
	    Func p6DuringExamplePredicate = new Func(null, "p6DuringExample", new ArrayList<>(), null, p6DuringExampleBody);
	    Expr p6DuringExampleExpr = p6DuringExamplePredicate.call();
	    
	    // p6DuringExample
	    Expr p7DuringExampleBody = p7Sig.in(allControlSig.join(allControl_p7));
	    Func p7DuringExamplePredicate = new Func(null, "p7DuringExample", new ArrayList<>(), null, p7DuringExampleBody);
	    Expr p7DuringExampleExpr = p7DuringExamplePredicate.call();
	    
	    // instancesDuringExample
	    Expr instancesDuringExampleBody = p1DuringExampleExpr.and(p2DuringExampleExpr).and(p3DuringExampleExpr).and(p4DuringExampleExpr).and(p5DuringExampleExpr).and(p6DuringExampleExpr).and(p7DuringExampleExpr);
	    Func instancesDuringExamplePredicate = new Func(null, "instancesDuringExample", new ArrayList<>(), null, instancesDuringExampleBody);
	    Expr instancesDuringExampleExpr = instancesDuringExamplePredicate.call();
	    
	    // onlySimpleSequence
	    Expr onlySimpleSequenceBody = simpleSequenceSig.cardinality().equal(ExprConstant.makeNUMBER(1)).and(forkSig.no()).and(joinSig.no()).and(decisionSig.no()).and(mergeSig.no()).and(allControlSig.no());
	    Func onlySimpleSequencePredicate = new Func(null, "onlySimpleSequence", new ArrayList<>(), null, onlySimpleSequenceBody);
	    Expr onlySimpleSequenceExpr = onlySimpleSequencePredicate.call();
	    
	    // onlyFork
	    Expr onlyForkBody = simpleSequenceSig.no().and(forkSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(joinSig.no()).and(decisionSig.no()).and(mergeSig.no()).and(allControlSig.no());
	    Func onlyForkPredicate = new Func(null, "onlyFork", new ArrayList<>(), null, onlyForkBody);
	    Expr onlyForkExpr = onlyForkPredicate.call();
	    
	    // onlyJoin
	    Expr onlyJoinBody = simpleSequenceSig.no().and(forkSig.no()).and(joinSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(decisionSig.no()).and(mergeSig.no()).and(allControlSig.no());
	    Func onlyJoinPredicate = new Func(null, "onlyJoin", new ArrayList<>(), null, onlyJoinBody);
	    Expr onlyJoinExpr = onlyJoinPredicate.call();
	    
	    // onlyDecision
	    Expr onlyDecisionBody = simpleSequenceSig.no().and(forkSig.no()).and(joinSig.no()).and(decisionSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(mergeSig.no()).and(allControlSig.no());
	    Func onlyDecisionPredicate = new Func(null, "onlyDecision", new ArrayList<>(), null, onlyDecisionBody);
	    Expr onlyDecisionExpr = onlyDecisionPredicate.call();
	    
	    // onlyMerge
	    Expr onlyMergeBody = simpleSequenceSig.no().and(forkSig.no()).and(joinSig.no()).and(decisionSig.no()).and(mergeSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(allControlSig.no());
	    Func onlyMergePredicate = new Func(null, "onlyMerge", new ArrayList<>(), null, onlyMergeBody);
	    Expr onlyMergeExpr = onlyMergePredicate.call();
	    
	    // onlyAllControl
	    Expr onlyAllControlBody = simpleSequenceSig.no().and(forkSig.no()).and(joinSig.no()).and(decisionSig.no()).and(mergeSig.no()).and(allControlSig.cardinality().equal(ExprConstant.makeNUMBER(1)));
	    Func onlyAllControlPredicate = new Func(null, "onlyAllControl", new ArrayList<>(), null, onlyAllControlBody);
	    Expr onlyAllControlExpr = onlyAllControlPredicate.call();
	    
	    // ========== Import real AST from file ==========
	    
	    String filename = "src/test/resources/4.1.1 ControlNodesExamples.als";
	    CompModule importedModule = MyAlloyLibrary.importAlloyModule(filename);
	    
	    // ========== Test if they are equal ==========
	    
	    ExpressionComparator ec = new ExpressionComparator();
	    
	    Expr fileFacts = importedModule.getAllReachableFacts();
	    Expr apiFacts = alloy.getOverAllFact();
	    List<Sig> fileSigs = importedModule.getAllReachableUserDefinedSigs();
	    List<Sig> apiSigs = alloy.getAllSigs();
	    
	    Map<String, Sig> fileMap = new HashMap<>();
	    Map<String, Sig> apiMap = new HashMap<>();
	    
	    for(Sig sig : fileSigs) {
	    	fileMap.put(MyAlloyLibrary.removeSlash(sig.toString()), sig);
	    }
	    for(Sig sig : apiSigs) {
	    	apiMap.put(MyAlloyLibrary.removeSlash(sig.toString()), sig);
	    }
	    
	    assertTrue(ec.compareTwoExpressions(fileFacts, apiFacts));
	    assertTrue(fileSigs.size() == apiSigs.size());
	    
	    for(String sigName : fileMap.keySet()) {
	    	assertTrue(apiMap.containsKey(sigName));
	    	assertTrue(ec.compareTwoExpressions(fileMap.get(sigName), apiMap.get(sigName)));
	    }
	    
	    // ========== Define command(s) ==========
	    	    
	    Expr simpleSequenceExpr = alloy.getCommonCmdExprs()
		.and(instancesDuringExampleExpr).and(onlySimpleSequenceExpr);
	    
	    Expr forkExpr = alloy.getCommonCmdExprs()
		.and(instancesDuringExampleExpr).and(onlyForkExpr);
	    
	    Expr joinExpr = alloy.getCommonCmdExprs()
		.and(instancesDuringExampleExpr).and(onlyJoinExpr);
	    
	    Expr decisionExpr = alloy.getCommonCmdExprs()
		.and(instancesDuringExampleExpr).and(onlyDecisionExpr);
	    
	    Expr mergeExpr = alloy.getCommonCmdExprs()
		.and(instancesDuringExampleExpr).and(onlyMergeExpr);
	    
	    Expr allControlExpr = alloy.getCommonCmdExprs()
		.and(instancesDuringExampleExpr).and(onlyAllControlExpr);
	
	    Command simpleSequenceCmd = new Command(
		null, simpleSequenceExpr, "SimpleSequence", false, 6,
		-1, -1, -1, Arrays.asList(new CommandScope[] {}),
		Arrays.asList(new Sig[] {}), 
		simpleSequenceExpr.and(alloy.getOverAllFact()), null);
	    
	    Command forkCmd = new Command(
		null, forkExpr, "fork", false, 10,
		-1, -1, -1, Arrays.asList(new CommandScope[] {}),
		Arrays.asList(new Sig[] {}), 
		forkExpr.and(alloy.getOverAllFact()), null);
	    
	    Command joinCmd = new Command(
		null, joinExpr, "join", false, 6,
		-1, -1, -1, Arrays.asList(new CommandScope[] {}),
		Arrays.asList(new Sig[] {}), 
		joinExpr.and(alloy.getOverAllFact()), null);
	    
	    Command decisionCmd = new Command(null, decisionExpr, "decision", 
		false, 6, -1, -1, -1, Arrays.asList(new CommandScope[] {}),
		Arrays.asList(new Sig[] {}), decisionExpr.and(alloy.getOverAllFact()), 
		null);
	    
	    Command mergeCmd = new Command(null, mergeExpr, "merge", false, 6, -1,
    	-1, -1, new ArrayList<>(), new ArrayList<>(), 
    	mergeExpr.and(alloy.getOverAllFact()), null);
	    
	    Command allControlCmd = new Command(null, allControlExpr, "AllControl",
		false, 10, -1, -1, -1, new ArrayList<>(), new ArrayList<>(), 
		allControlExpr.and(alloy.getOverAllFact()), null);
	    
	    Command[] commands = {simpleSequenceCmd, forkCmd, joinCmd, decisionCmd,
		mergeCmd, allControlCmd};
	    
	    // ========== Write file ==========
	    
	    AlloyModule alloyModule = new AlloyModule("ControlNodes",
		alloy.getAllSigs(), alloy.getOverAllFact(), commands);
	    
	    Translator translator = new Translator(alloy.getIgnoredExprs(), 
		alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());
	    
	    String outFileName = "src/test/resources/generated-" 
	    + alloyModule.getModuleName() + ".als";
	    
	    translator.generateAlsFileContents(alloyModule, outFileName);
	}
}