import static org.junit.jupiter.api.Assertions.*;
import edu.gatech.gtri.obm.translator.alloy.tofile.ExpressionComparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.Helper;
import edu.gatech.gtri.obm.translator.alloy.tofile.MyAlloyLibrary;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Expr;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;




class CallingBehaviorsTest {

	@Test
	void test() {
		Alloy alloy = new Alloy();
		
		// ========== Define list of signatures unique to the file ==========
		
		Sig p1Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P1");
		Sig p3Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P3");
		Sig p4Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P4");
		Sig p5Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P5");
		Sig nestedBehaviorSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("NestedBehavior");
		Sig composedBehaviorSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("ComposedBehavior");
		
		// ========== Define list of relations unique to the file ==========
		
		Sig.Field nestedBehavior_p4 = FuncUtils.addField("p4", nestedBehaviorSig, p4Sig);
		Sig.Field nestedBehavior_p5 = FuncUtils.addField("p5", nestedBehaviorSig, p5Sig);
		
		Sig.Field composedBehavior_p1 = FuncUtils.addField("p1", composedBehaviorSig, p1Sig);
		Sig.Field composedBehavior_p2 = FuncUtils.addField("p2", composedBehaviorSig, nestedBehaviorSig);
		Sig.Field composedBehavior_p3 = FuncUtils.addField("p3", composedBehaviorSig, p3Sig);
		
		// ========== Define implicit facts ==========
		
		Func bijectionFilteredFunction = Helper.getFunction(Alloy.transferModule, "o/bijectionFiltered");
		Func happensBeforeFunction = Helper.getFunction(Alloy.transferModule, "o/happensBefore");
		Func stepsFunction = Helper.getFunction(Alloy.transferModule, "o/steps");
		
		// NestedBehavior
		
		ExprVar this_nestedBehavior = ExprVar.make(null, "this", nestedBehaviorSig.type());
		
		nestedBehaviorSig.addFact(
			bijectionFilteredFunction.call(happensBeforeFunction.call(), this_nestedBehavior.join(nestedBehavior_p4), this_nestedBehavior.join(nestedBehavior_p5))
			.and(this_nestedBehavior.join(nestedBehavior_p4).cardinality().equal(ExprConstant.makeNUMBER(1)))
			.and(this_nestedBehavior.join(nestedBehavior_p4).plus(this_nestedBehavior.join(nestedBehavior_p5)).in(this_nestedBehavior.join(stepsFunction.call())))
			.and(this_nestedBehavior.join(stepsFunction.call()).in(this_nestedBehavior.join(nestedBehavior_p4).plus(this_nestedBehavior.join(nestedBehavior_p5))))
		);
		
		// ComposedBehavior
		
		ExprVar this_composedBehavior = ExprVar.make(null, "this", composedBehaviorSig.type());
		
		composedBehaviorSig.addFact(
			bijectionFilteredFunction.call(happensBeforeFunction.call(), this_composedBehavior.join(composedBehavior_p1), this_composedBehavior.join(composedBehavior_p2))
			.and(bijectionFilteredFunction.call(happensBeforeFunction.call(), this_composedBehavior.join(composedBehavior_p2), this_composedBehavior.join(composedBehavior_p3)))
			.and(this_composedBehavior.join(composedBehavior_p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
			.and(this_composedBehavior.join(composedBehavior_p1).plus(this_composedBehavior.join(composedBehavior_p2)).plus(this_composedBehavior.join(composedBehavior_p3)).in(this_composedBehavior.join(stepsFunction.call())))
			.and(this_composedBehavior.join(stepsFunction.call()).in(this_composedBehavior.join(composedBehavior_p1).plus(this_composedBehavior.join(composedBehavior_p2)).plus(this_composedBehavior.join(composedBehavior_p3))))
		);
		
		// ========== Define functions and predicates ==========
		
		// suppressTransfers
		Sig transfer = Helper.getReachableSig(Alloy.transferModule, "o/Transfer");
	    Expr suppressTransfersExpessionBody = transfer.no();
	    Func suppressTransfersFunction = new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
	    Expr suppressTransfersExpression = suppressTransfersFunction.call();
	    
	    // suppressIO
	    Func inputs = Helper.getFunction(Alloy.transferModule, "o/inputs");
	    Func outputs = Helper.getFunction(Alloy.transferModule, "o/outputs");
	    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
	    Func suppressIOFunction = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
	    Expr suppressIOExpression = suppressIOFunction.call();
	    
	    // p1DuringExample
	    Expr p1DuringExampleBody = p1Sig.in(composedBehaviorSig.join(p1Sig));
	    Func p1DuringExamplePredicate = new Func(null, "p1DuringExample", new ArrayList<>(), null, p1DuringExampleBody);
	    Expr p1DuringExampleExpression = p1DuringExamplePredicate.call();
	    
	    // p2DuringExample
	    Expr p2DuringExampleBody = nestedBehaviorSig.in(composedBehaviorSig.join(composedBehavior_p2));
	    Func p2DuringExamplePredicate = new Func(null, "p2DuringExample", new ArrayList<>(), null, p2DuringExampleBody);
	    Expr p2DuringExampleExpression = p2DuringExamplePredicate.call();
	    
	    // p3DuringExample
	    Expr p3DuringExampleBody = p3Sig.in(composedBehaviorSig.join(composedBehavior_p3));
	    Func p3DuringExamplePredicate = new Func(null, "p3DuringExample", new ArrayList<>(), null, p3DuringExampleBody);
	    Expr p3DuringExampleExpression = p3DuringExamplePredicate.call();
	    
	    // p4DuringExample
	    Expr p4DuringExampleBody = p4Sig.in(nestedBehaviorSig.join(nestedBehavior_p4));
	    Func p4DuringExamplePredicate = new Func(null, "p4DuringExample", new ArrayList<>(), null, p4DuringExampleBody);
	    Expr p4DuringExampleExpression = p4DuringExamplePredicate.call();
	    
	    // p5DuringExample
	    Expr p5DuringExampleBody = p5Sig.in(nestedBehaviorSig.join(nestedBehavior_p5));
	    Func p5DuringExamplePredicate = new Func(null, "p5DuringExample", new ArrayList<>(), null, p5DuringExampleBody);
	    Expr p5DuringExampleExpression = p5DuringExamplePredicate.call();
	    
	    // instancesDuringExample
	    Expr instancesDuringExampleBody = p1DuringExampleExpression.and(p2DuringExampleExpression).and(p3DuringExampleExpression).and(p4DuringExampleExpression).and(p5DuringExampleExpression);
	    Func instancesDuringExampleFunction = new Func(null, "instancesDuringExample", new ArrayList<>(), null, instancesDuringExampleBody);
	    Expr instancesDuringExampleExpression = instancesDuringExampleFunction.call();
	    
	    // onlyComposedBehavior
	    Expr onlyComposedBehaviorBody = composedBehaviorSig.cardinality().equal(ExprConstant.makeNUMBER(1));
	    Func onlyComposedBehaviorFunction = new Func(null, "onlyComposedBehavior", new ArrayList<>(), null, onlyComposedBehaviorBody);
	    Expr onlyComposedBehaviorExpression = onlyComposedBehaviorFunction.call();
	    
	    // nonZeroDurationOnly
	    Func nonZeroDurationOnlyFunction = Helper.getFunction(Alloy.transferModule, "o/nonZeroDurationOnly");
	    Expr nonZeroDurationOnlyExpression = nonZeroDurationOnlyFunction.call();
	    
	    // ========== Create Alloy file version ==========
	    
	    String filename = "4.1.3 CallingBehaviors.als";
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
	}

}