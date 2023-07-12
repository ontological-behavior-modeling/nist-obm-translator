package manualtest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.Helper;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.gatech.gtri.obm.translator.alloy.tofile.ExpressionComparator;
import edu.gatech.gtri.obm.translator.alloy.tofile.MyAlloyLibrary;
import edu.gatech.gtri.obm.translator.alloy.tofile.Translator;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprHasName;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
import edu.mit.csail.sdg.ast.Decl;

import org.junit.jupiter.api.Test;

class FoodServiceControlFlowExplicitFactTest {

	@Test
	void test() {
		Alloy alloy = new Alloy();
		
		// ========== Define list of signatures unique to the file ==========
		
		Sig orderSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Order");
		Sig prepareSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Prepare");
		Sig serveSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Serve");
		Sig eatSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Eat");
		Sig paySig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Pay");
		Sig foodServiceSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("FoodService");
		Sig singleFoodServiceSig = alloy.createSigAndAddToAllSigs("SingleFoodService", (Sig.PrimSig) foodServiceSig);
		Sig buffetServiceSig = alloy.createSigAndAddToAllSigs("BuffetService", (Sig.PrimSig) foodServiceSig);
		Sig churchSupperServiceSig = alloy.createSigAndAddToAllSigs("ChurchSupperService", (Sig.PrimSig) foodServiceSig);
		Sig fastFoodServiceSig = alloy.createSigAndAddToAllSigs("FastFoodService", (Sig.PrimSig) foodServiceSig);
		Sig restaurantServiceSig = alloy.createSigAndAddToAllSigs("RestaurantService", (Sig.PrimSig) foodServiceSig);
		Sig UnsatisfiableFoodServiceSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("UnsatisfiableFoodService");
		
		// ========== Define list of relations unique to the file ==========
		
		// FoodService fields:
		Sig.Field foodService_orderField = FuncUtils.addField("order", foodServiceSig, orderSig);
		Sig.Field foodService_prepareField = FuncUtils.addField("prepare", foodServiceSig, prepareSig);
		Sig.Field foodService_payField = FuncUtils.addField("pay", foodServiceSig, paySig);
		Sig.Field foodService_eatField = FuncUtils.addField("eat", foodServiceSig, eatSig);
		Sig.Field foodService_serveField = FuncUtils.addField("serve", foodServiceSig, serveSig);
		
		// SingleFoodService fields: none
		// BuffetService fields: none
		// ChurchSupperService fields: none
		// FastFoodService fields: none
		// RestaurantService fields: none
		// UnsatisfiableFoodService fields: none
		
		 // ========== Define explicit facts ==========
		
		// FoodService:
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(foodServiceSig, foodService_orderField, foodService_serveField);
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(foodServiceSig, foodService_prepareField, foodService_serveField);
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(foodServiceSig, foodService_serveField, foodService_eatField);
		
		Map<String, Sig.Field> fieldByName = new HashMap<>();
		fieldByName.put(foodService_orderField.label, foodService_orderField);
		fieldByName.put(foodService_prepareField.label, foodService_prepareField);
		fieldByName.put(foodService_payField.label, foodService_payField);
		fieldByName.put(foodService_eatField.label, foodService_eatField);
		fieldByName.put(foodService_serveField.label, foodService_serveField);
		
		alloy.addSteps(foodServiceSig, fieldByName);
		
		// SingleFoodService:
		alloy.addOneConstraintToField(singleFoodServiceSig, foodService_orderField);
		alloy.addOneConstraintToField(singleFoodServiceSig, foodService_prepareField);
		alloy.addOneConstraintToField(singleFoodServiceSig, foodService_payField);
		alloy.addOneConstraintToField(singleFoodServiceSig, foodService_eatField);
		alloy.addOneConstraintToField(singleFoodServiceSig, foodService_serveField);
		
		
		// BuffetService:
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(buffetServiceSig, foodService_prepareField, foodService_orderField);
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(buffetServiceSig, foodService_eatField, foodService_payField);
		
		// ChurchSupperService:
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(churchSupperServiceSig, foodService_payField, foodService_prepareField);
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(churchSupperServiceSig, foodService_payField, foodService_orderField);
		
		// FastFoodService:
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(fastFoodServiceSig, foodService_orderField, foodService_payField);
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(fastFoodServiceSig, foodService_payField, foodService_eatField);
		
		// RestaurantService:
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(restaurantServiceSig, foodService_eatField, foodService_payField);
		
		// UnsatisfiableFoodService
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(UnsatisfiableFoodServiceSig, foodService_eatField, foodService_payField);
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(UnsatisfiableFoodServiceSig, foodService_payField, foodService_prepareField);
		
		// Functions and Predicates ==========
		
		// suppressTransfers: added by default by Alloy constructor
		// suppressIO: added by default by Alloy constructor
		
		ExprVar g = ExprVar.make(null,  "g", foodServiceSig.type());
		
		// noChildFoodService
		Expr noChildFoodServiceExpr = buffetServiceSig.no().and(churchSupperServiceSig.no()).and(fastFoodServiceSig.no()).and(restaurantServiceSig.no()).and(UnsatisfiableFoodServiceSig.no());
		Func noChildFoodServiceFunc = new Func(null, "noChildFoodService", null, null, noChildFoodServiceExpr);
		
		// instancesDuringExample
		Expr instancesDuringExampleExpr = orderSig.in(foodServiceSig.join(foodService_orderField))
			.and(prepareSig.in(foodServiceSig.join(foodService_prepareField)))
			.and(serveSig.in(foodServiceSig.join(foodService_serveField)))
			.and(eatSig.in(foodServiceSig.join(foodService_eatField)))
			.and(paySig.in(foodServiceSig.join(foodService_payField)));
		Func instancesDuringExampleFunc = new Func(null, "instancesDuringExample", null, null, instancesDuringExampleExpr);
		
		
		// onlyFoodService
		Expr onlyFoodServiceExpr = foodServiceSig.cardinality().equal(ExprConstant.makeNUMBER(1)).and(singleFoodServiceSig.no().and(noChildFoodServiceExpr));
		Func onlyFoodServiceFunc = new Func(null, "onlyFoodService", null, null, onlyFoodServiceExpr);
		
		// onlySingleFoodService
		Expr onlySingleFoodServiceExpr = foodServiceSig.in(singleFoodServiceSig.and(noChildFoodServiceExpr));
		Func onlySingleFoodServiceFunc = new Func(null, "onlySingleFoodService", null, null, onlySingleFoodServiceExpr);
		
		// onlyBuffetService
		Expr onlyBuffetServiceExpr = buffetServiceSig.cardinality().equal(ExprConstant.makeNUMBER(1)).and(foodServiceSig.or(g.in(buffetServiceSig)));
		Func onlyBuffetServiceFunc = new Func(null, "onlyBuffetService", null, null, onlyBuffetServiceExpr);
		
		// onlyChurchSupperService
		Expr onlyChurchSupperServiceExpr = churchSupperServiceSig.cardinality().equal(ExprConstant.makeNUMBER(1)).and(foodServiceSig.or(g.in(churchSupperServiceSig)));
		Func onlyChurchSupperServiceFunc = new Func(null, "onlyChurchSupperService", null, null, onlyChurchSupperServiceExpr);
		
		// onlyFastFoodService
		Expr onlyFastFoodServiceExpr = foodServiceSig.cardinality().equal(ExprConstant.makeNUMBER(1)).and(foodServiceSig.or(g).in(UnsatisfiableFoodServiceSig));
		Func onlyFastFoodServiceFunc = new Func(null, "onlyFastFoodService", null, null, onlyFastFoodServiceExpr);
		
		// onlyRestaurantService
		
		Decl decl = 
		new Decl(null, null, null, List.of(g), restaurantServiceSig.oneOf());
		Expr onlyRestaurantServiceExpr = restaurantServiceSig.cardinality()
		.equal(ExprConstant.makeNUMBER(1)).and(g.in(restaurantServiceSig)
		.forAll(decl));
		Func onlyRestaurantServiceFunc = new Func(null, "onlyRestaurantService", null, null, onlyRestaurantServiceExpr);
	
		// onlyUnsatisfiableFoodService
		Decl unsatisfiableFoodServiceDecl = new Decl(null, null, null, List.of(g), UnsatisfiableFoodServiceSig.oneOf());
		Expr onlyUnsatisfiableFoodServiceExpr = UnsatisfiableFoodServiceSig.cardinality().equal(ExprConstant.makeNUMBER(1)).and(g.in(UnsatisfiableFoodServiceSig).forAll(unsatisfiableFoodServiceDecl));
		Func onlyUnsatisfiableFoodServiceFunc = new Func(null, "onlyUnsatisfiableFoodService", null, null, onlyUnsatisfiableFoodServiceExpr);
		
		// ========== Commands ==========
		
		// nonZeroDurationOnly
		Func nonZeroDurationOnlyFunc = Helper.getFunction(Alloy.transferModule, "o/nonZeroDurationOnly");
		
	    // suppressTransfers
		Sig transfer = Helper.getReachableSig(Alloy.transferModule, "o/Transfer");
	    Expr suppressTransfersExpessionBody = transfer.no();
	    Func suppressTransfersFunc = new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
	    
	    // suppressIO
	    Func inputs = Helper.getFunction(Alloy.transferModule, "o/inputs");
	    Func outputs = Helper.getFunction(Alloy.transferModule, "o/outputs");
	    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
	    Func suppressIOFunc = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
		
		// showFoodService
	    Expr showFoodServiceCmdExpr = nonZeroDurationOnlyFunc.call().and(instancesDuringExampleExpr).and(onlyFoodServiceExpr).and(suppressTransfersFunc.call()).and(suppressIOFunc.call());
	    Command showFoodServiceCmd = new Command(
	    	null, showFoodServiceCmdExpr, "showFoodService", false, 10, 
	    	-1, -1, -1, Arrays.asList(new CommandScope[] {}), 
	    	Arrays.asList(new Sig[] {}), 
	    	showFoodServiceCmdExpr.and(alloy.getOverAllFact()), null);
	    		
		
		// showSingleFoodService
	    Expr showSingleFoodServiceExpr = nonZeroDurationOnlyFunc.call().and(instancesDuringExampleExpr).and(onlySingleFoodServiceExpr).and(suppressTransfersFunc.call()).and(suppressIOFunc.call());
	    Command showSingleFoodServiceCmd = new Command(
    		null, showSingleFoodServiceExpr, "showSingleFoodService", false, 
    		10, -1, -1, -1, Arrays.asList(new CommandScope[] {}), 
    		Arrays.asList(new Sig[] {}), 
    		showSingleFoodServiceExpr.and(alloy.getOverAllFact()), null);
	    
		// showBuffetService
	    Expr showBuffetServiceExpr = nonZeroDurationOnlyFunc.call().and(instancesDuringExampleExpr).and(onlySingleFoodServiceExpr).and(suppressTransfersFunc.call()).and(suppressIOFunc.call());
	    Command showBuffetServiceCmd = new Command(
	    		null, showSingleFoodServiceExpr, "showBuffetService", false, 
	    		10, -1, -1, -1, Arrays.asList(new CommandScope[] {}), 
	    		Arrays.asList(new Sig[] {}), 
	    		showSingleFoodServiceExpr.and(alloy.getOverAllFact()), null);
	    
		// showChurchSupperService
	    Expr showChurchSupperServiceExpr = nonZeroDurationOnlyFunc.call().and(instancesDuringExampleExpr).and(onlyChurchSupperServiceExpr).and(suppressTransfersFunc.call()).and(suppressIOFunc.call());
	    Command showChurchSupperServiceCmd = new Command(
	    		null, showSingleFoodServiceExpr, "showChurchSupperService", false, 
	    		10, -1, -1, -1, Arrays.asList(new CommandScope[] {}), 
	    		Arrays.asList(new Sig[] {}), 
	    		showChurchSupperServiceExpr.and(alloy.getOverAllFact()), null);
	    
		// showFastFoodService
	    Expr showFastFoodServiceExpr = nonZeroDurationOnlyFunc.call().and(instancesDuringExampleExpr).and(onlyFastFoodServiceExpr).and(suppressTransfersFunc.call()).and(suppressIOFunc.call());
	    Command showFastFoodServiceCmd = new Command(
	    		null, showSingleFoodServiceExpr, "showFastFoodService", false, 
	    		10, -1, -1, -1, Arrays.asList(new CommandScope[] {}), 
	    		Arrays.asList(new Sig[] {}), 
	    		showFastFoodServiceExpr.and(alloy.getOverAllFact()), null);
	    
		// showRestaurantService
	    Expr showRestaurantServiceExpr = alloy.getCommonCmdExprs()
		.and(instancesDuringExampleFunc.call())
		.and(onlyRestaurantServiceFunc.call());
	    Command showRestaurantServiceCmd = new Command(null, 
		showRestaurantServiceExpr, "showRestaurantService", false, 10, -1, -1, -1, List.of(), List.of(), 
    		showRestaurantServiceExpr.and(alloy.getOverAllFact()), null);
	    
		// showUnsatisfiableFoodService
	    Expr showUnsatisfiableFoodServiceExpr = 
		instancesDuringExampleExpr.and(onlyUnsatisfiableFoodServiceExpr)
		.and(suppressTransfersFunc.call()).and(suppressIOFunc.call());
	    Command showUnsatisfiableFoodServiceCmd = new Command(
		null, showUnsatisfiableFoodServiceExpr, "showUnsatisfiableFoodService", false, 
		15, -1, -1, -1, Arrays.asList(new CommandScope[] {}), 
		Arrays.asList(new Sig[] {}), 
		showUnsatisfiableFoodServiceExpr.and(alloy.getOverAllFact()), null);
	    
	    Command[] commands = {showFoodServiceCmd, showSingleFoodServiceCmd, showBuffetServiceCmd, showChurchSupperServiceCmd, showFastFoodServiceCmd, showRestaurantServiceCmd, showUnsatisfiableFoodServiceCmd};
		
		
		// ========== Create Alloy file version ==========
		String filename = "src/test/resources/4.2.1_FoodServiceControlFlow_ExplicitFacts.als";
	    CompModule importedModule = MyAlloyLibrary.importAlloyModule(filename);
		
	    // ========== Test if facts are equal ==========
	    
	    ExpressionComparator ec = new ExpressionComparator();
	    
	    Expr fileFacts = importedModule.getAllReachableFacts();
	    Expr apiFacts = alloy.getOverAllFact();
	    assertTrue(ec.compareTwoExpressions(fileFacts, apiFacts));
	    
	    // ========== Test if signatures are equal ==========
	    
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
	    
	    assertTrue(fileSigs.size() == apiSigs.size());
	    
	    for(String sigName : fileMap.keySet()) {
	    	assertTrue(apiMap.containsKey(sigName));
	    	assertTrue(ec.compareTwoExpressions(fileMap.get(sigName), apiMap.get(sigName)));
	    }
	    
	    // ========== Write file ==========
	    
	    AlloyModule alloyModule = new AlloyModule("FoodServiceControlFlow_ExplicitFast", alloy.getAllSigs(), alloy.getOverAllFact(), commands);
	    Translator translator = new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());
	    String outFileName = "src/test/resources/generated-" + alloyModule.getModuleName() + ".als";
	    translator.generateAlsFileContents(alloyModule, outFileName);
	    
	}
}
