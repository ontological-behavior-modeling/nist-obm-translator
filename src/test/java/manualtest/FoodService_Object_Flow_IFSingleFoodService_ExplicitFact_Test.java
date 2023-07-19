package manualtest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.Helper;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.alloy4.Pos;


class FoodService_Object_Flow_IFSingleFoodService_ExplicitFact_Test {

	@Test
	void test() {
		
		Alloy alloy = new Alloy();
		
		// ========== Define list of signatures unique to the file ==========
		
		Sig order = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Order");
		Sig prepare = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Prepare");
		Sig serve = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Serve");
		Sig eat = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Eat");
		Sig pay = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Serve");
		Sig foodItem = alloy.createSigAsChildOfOccSigAndAddToAllSigs("FoodItem");
		Sig location = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Location");
		Sig real = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Real");
		Sig ofStart = alloy.createSigAsChildOfOccSigAndAddToAllSigs("ofStart");
		Sig ofEnd = alloy.createSigAsChildOfOccSigAndAddToAllSigs("ofEnd");
		Sig ofOrder = alloy.createSigAndAddToAllSigs("ofOrder", (Sig.PrimSig) order);
		Sig ofCustomOrder = alloy.createSigAndAddToAllSigs("OFCustomOrder", (Sig.PrimSig) ofOrder);
		Sig ofPrepare = alloy.createSigAndAddToAllSigs("OFPrepare", (Sig.PrimSig) prepare);
		Sig ofCustomPrepare = alloy.createSigAndAddToAllSigs("OFCustomPrepare", (Sig.PrimSig) ofPrepare);
		Sig ofServe = alloy.createSigAndAddToAllSigs("OFServe", (Sig.PrimSig) serve);
		Sig ofCustomServe = alloy.createSigAndAddToAllSigs("OFCustomServe", (Sig.PrimSig) ofServe);
		Sig ofEat = alloy.createSigAndAddToAllSigs("OFEat", (Sig.PrimSig) eat);
		Sig ofPay = alloy.createSigAndAddToAllSigs("OFPay", (Sig.PrimSig) pay);
		Sig foodService = alloy.createSigAsChildOfOccSigAndAddToAllSigs("FoodService");
		Sig ofFoodService = alloy.createSigAndAddToAllSigs("OFFoodService", (Sig.PrimSig) foodService);
		Sig ofSingleFoodService = alloy.createSigAndAddToAllSigs("OFSingleFoodService", (Sig.PrimSig) ofFoodService);
		Sig ofLoopFoodService = alloy.createSigAndAddToAllSigs("OFLoopFoodService", (Sig.PrimSig) ofFoodService);
		Sig ofParallelFoodService = alloy.createSigAndAddToAllSigs("OFParallelFoodService", (Sig.PrimSig) ofFoodService);
		
		Sig transferBefore = Helper.getReachableSig(alloy.getTemplateModule(), "TransferBefore");

		// ========== Define list of relations unique to the file ==========
		
		// Order: none
		// Prepare: none
		// Serve: none
		// Eat: none
		// Pay: none
		// FoodItem: none
		// Location: none
		// Real: none
		// ofStart: none
		// ofEnd: none
		
		// ofOrder
		Sig.Field orderedFoodItem = FuncUtils.addOneField("orderedFoodItem", ofOrder, foodItem);
		
		// OFCustomOrder
		Sig.Field orderAmount = FuncUtils.addOneField("orderAmount", ofCustomOrder, real);
		Sig.Field orderDestination = FuncUtils.addOneField("orderDestination", ofCustomOrder, location);
		
		// OFPrepare
		Sig.Field preparedFoodItem = FuncUtils.addOneField("preparedFoodItem", ofPrepare, foodItem);
		
		// OFCustomPrepare
		Sig.Field prepareDestination = FuncUtils.addOneField("prepareDestination", ofCustomPrepare, location);
		
		// OFServe
		Sig.Field servedFoodItem = FuncUtils.addOneField("servedFoodItem", ofServe, foodItem);
		
		// OFCustomServe
		Sig.Field serviceDestination = FuncUtils.addOneField("serviceDestination", ofCustomServe, location);
		
		// OFEat
		Sig.Field eatenItem = FuncUtils.addOneField("eatenItem", ofEat, foodItem);
		
		// OFPay
		Sig.Field paidAmount = FuncUtils.addOneField("paidAmount", ofPay, real);
		Sig.Field paidFoodItem = FuncUtils.addOneField("paidFoodItem", ofPay, foodItem);
		
		// FoodService
		Sig.Field foodService_order = FuncUtils.addField("order", foodService, order);
		Sig.Field foodService_prepare = FuncUtils.addField("prepare", foodService, prepare);
		Sig.Field foodService_pay = FuncUtils.addField("pay", foodService, pay);
		Sig.Field foodService_eat = FuncUtils.addField("eat", foodService, eat);
		Sig.Field foodService_serve = FuncUtils.addField("serve", foodService, serve);
		
		// OFFoodService
		Sig.Field[] ofFoodService_fields = ofFoodService.addTrickyField(
			null, null, Pos.UNKNOWN, null, null, 
			new String[] {"transferPrepareServe", "transferOrderServe", "transferServeEat"}, 
			transferBefore);
		Sig.Field transferPrepareServe = ofFoodService_fields[0];
		Sig.Field transferOrderServe = ofFoodService_fields[1];
		Sig.Field transferServeEat = ofFoodService_fields[2];
		
		// OFSingleFoodService
		Sig.Field[] ofSingleFoodService_fields = ofSingleFoodService.addTrickyField(
			null, null, Pos.UNKNOWN, null, null, 
			new String[] {"transferOrderPrepare", "transferOrderPay", "transferPayEat"}, 
			transferBefore);
		Sig.Field transferOrderPrepare = ofSingleFoodService_fields[0];
		Sig.Field transferOrderPay = ofSingleFoodService_fields[1];
		Sig.Field transferPayEat = ofSingleFoodService_fields[2];
		
		// OFLoopFoodService
		Sig.Field ofLoopFoodService_start = FuncUtils.addOneField("start", ofLoopFoodService, ofStart);
		Sig.Field ofLoopFoodService_end = FuncUtils.addOneField("end", ofLoopFoodService, ofEnd);
		Sig.Field[] ofLoopFoodService_fields = ofLoopFoodService.addTrickyField(
			null, null, Pos.UNKNOWN, null, null, 
			new String[] {"transferOrderPrepare", "transferOrderPay", "transferPayEat"}, 
			transferBefore);
		Sig.Field ofLoopFoodService_transferOrderPrepare = ofLoopFoodService_fields[0];
		Sig.Field ofLoopFoodService_transferOrderPay = ofLoopFoodService_fields[1];
		Sig.Field ofLoopFoodService_transferPayEat = ofLoopFoodService_fields[2];
		
		// OFParallelFoodService: none
		
		/***** Explicit Facts *****/
		
		// Setup:
		Func happensBefore = Helper.getFunction(Alloy.transferModule, "o/happensBefore");
		Func steps = Helper.getFunction(Alloy.transferModule, "o/steps");
		Func inputs = Helper.getFunction(Alloy.transferModule, "o/inputs");
	    Func outputs = Helper.getFunction(Alloy.transferModule, "o/outputs");
	    Func items = Helper.getFunction(Alloy.transferModule, "o/items");
	    Func sources = Helper.getFunction(Alloy.transferModule, "o/sources");
	    Func targets = Helper.getFunction(Alloy.transferModule, "o/targets");
	    Func bijectionFiltered = Helper.getFunction(Alloy.transferModule, "o/bijectionFiltered");
	    Func subsettingItemRuleForSources = Helper.getFunction(Alloy.transferModule, "o/subsettingItemRuleForSources");
	    Func subsettingItemRuleForTargets = Helper.getFunction(Alloy.transferModule, "o/subsettingItemRuleForTargets");
		
		// Order: none
		// Prepare: none
		// Serve: none
		// Eat: none
		// Pay: none
		
		/***** 
		 FoodItem:
		 Implicit fact: "no this.happensBefore && no happensBefore.this && no this.steps && no this.inputs && no this.outputs"
		 Explicit fact: "all food_item: FoodItem | no food_item.happensBefore && no happensBefore.food_item && no food_item.steps && no food_item.inputs && no food_item.outputs"
		 *****/
		
		ExprVar foodItem_var = ExprVar.make(null, "food_item", foodItem.type());
		Decl foodItem_decl = new Decl(null, null, null, List.of(foodItem_var), foodItem.oneOf());
		
		alloy.addToOverallFact(
			foodItem_var.join(happensBefore.call()).no()
			.and(happensBefore.call().join(foodItem_var).no())
			.and(foodItem_var.join(steps.call()).no())
			.and(foodItem_var.join(inputs.call()).no())
			.and(foodItem_var.join(outputs.call()).no())
			.forAll(foodItem_decl));
		
		/***** 
		 Location:
		 Implicit fact: "no this.happensBefore && no happensBefore.this && no this.steps && no this.inputs && no this.outputs"
		 Explicit fact: "all location: Location | no location.happensBefore && no happensBefore.location && no location.steps && no location.inputs && no location.outputs"
		 *****/
		
		ExprVar location_var = ExprVar.make(null, "location", location.type());
		Decl location_decl = new Decl(null, null, null, List.of(location_var), location.oneOf());
		
		alloy.addToOverallFact(
			location_var.join(happensBefore.call()).no()
			.and(happensBefore.call().join(location_var).no())
			.and(location_var.join(steps.call()).no())
			.and(location_var.join(inputs.call()).no())
			.and(location_var.join(outputs.call()).no())
			.forAll(location_decl));
		
		/***** 
		 Location:
		 Implicit fact: "no this.happensBefore && no happensBefore.this && no this.steps && no this.inputs && no this.outputs"
		 Explicit fact: "all real: Real | no real.happensBefore && no happensBefore.real && no real.steps && no real.inputs && no real.outputs"
		 *****/
		
		ExprVar real_var = ExprVar.make(null, "real", real.type());
		Decl real_decl = new Decl(null, null, null, List.of(real_var), real.oneOf());
		
		alloy.addToOverallFact(
			real_var.join(happensBefore.call()).no()
			.and(happensBefore.call().join(real_var).no())
			.and(real_var.join(steps.call()).no())
			.and(real_var.join(inputs.call()).no())
			.and(real_var.join(outputs.call()).no())
			.forAll(real_decl));
		
		/*****
		OFStart:
		Implicit fact: "no inputs.this && no outputs.this && no items.this"
		Explicit fact: "all of_start: OFStart | no inputs.of_start && no outputs.of_start && no items.of_start"
		*****/
		
		ExprVar ofStart_var = ExprVar.make(null, "of_start", ofStart.type());
		Decl ofStart_decl = new Decl(null, null, null, List.of(ofStart_var), ofStart.oneOf());
		
		alloy.addToOverallFact(
			inputs.call().join(ofStart_var).no()
			.and(outputs.call().join(ofStart_var).no())
			.and(items.call().join(ofStart_var).no())
			.forAll(ofStart_decl));
		
		/*****
		OFEnd:
		Implicit fact: "no inputs.this && no outputs.this && no items.this"
		Explicit fact: "all of_end: OFEnd | no inputs.of_end && no outputs.of_end && no items.of_end"
		*****/
		
		ExprVar ofEnd_var = ExprVar.make(null, "of_end", ofEnd.type());
		Decl ofEnd_decl = new Decl(null, null, null, List.of(ofEnd_var), ofEnd.oneOf());
		
		alloy.addToOverallFact(
			inputs.call().join(ofEnd_var).no()
			.and(outputs.call().join(ofEnd_var).no())
			.and(items.call().join(ofEnd_var).no())
			.forAll(ofEnd_decl));
		
		// ofOrder
		// Implicit fact:
		// "no this.inputs"
		// "orderedFoodItem in this.outputs"
		// Explicit fact:
		// "all of_order: ofOrder | no of_order.inputs"
		// "all of_order: ofOrder | of_order.orderedFoodItem in of_order.outputs"
		
		ExprVar ofOrder_var = ExprVar.make(null, "of_order", ofOrder.type());
		Decl ofOrder_decl = new Decl(null, null, null, List.of(ofOrder_var), ofOrder.oneOf());
		
		alloy.addToOverallFact(ofOrder_var.join(inputs.call()).no().forAll(ofOrder_decl));
		alloy.addToOverallFact(ofOrder_var.join(orderedFoodItem).in(ofOrder_var.join(outputs.call())).forAll(ofOrder_decl));
		
		// OFCustomOrder
		// Implicit fact:
		// "orderAmount in this.outputs"
		// "orderDestination in this.outputs"
		// Explicit fact:
		// "all of_custom_order: OFCustomOrder | of_custom_order.orderAmount in of_custom_order.outputs"
		// "all of_custom_order: OFCustomOrder | of_custom_order.orderDestination in of_custom_order.outputs"
		
		ExprVar ofCustomOrder_var = ExprVar.make(null, "of_custom_order", ofCustomOrder.type());
		Decl ofCustomOrder_decl = new Decl(null, null, null, List.of(ofCustomOrder_var), ofCustomOrder_var.oneOf());
		
		alloy.addToOverallFact(ofCustomOrder_var.join(orderAmount).in(ofCustomOrder_var.join(outputs.call())).forAll(ofCustomOrder_decl));
		alloy.addToOverallFact(ofCustomOrder.join(orderDestination).in(ofCustomOrder.join(outputs.call())));
		
		// OFPrepare
		// Implicit fact:
		// "preparedFoodItem in this.inputs"
		// "preparedFoodItem in this.outputs"
		// Explicit fact:
		// "all of_prepare: OFPrepare | of_prepare.preparedFoodItem in of_prepare.inputs"
		// "all of_prepare: OFPrepare | of_prepare.preparedFoodItem in of_prepare.outputs"
		
		ExprVar ofPrepare_var = ExprVar.make(null, "of_prepare", ofPrepare.type());
		Decl ofPrepareOrder_decl = new Decl(null, null, null, List.of(ofPrepare_var), ofPrepare_var.oneOf());
		
		alloy.addToOverallFact(ofPrepare_var.join(preparedFoodItem).in(ofPrepare_var.join(inputs.call())).forAll(ofPrepareOrder_decl));
		alloy.addToOverallFact(ofPrepare_var.join(preparedFoodItem).in(ofPrepare_var.join(outputs.call())).forAll(ofPrepareOrder_decl));
		
		// OFCustomPrepare
		// Implicit fact:
		// "prepareDestination in this.inputs"
		// "prepareDestination in this.outputs"
		// Explicit fact:
		// "all of_custom_prepare: OFCustomPrepare | of_custom_prepare.prepareDestination in of_custom_prepare.inputs"
		// "all of_custom_prepare: OFCustomPrepare | of_custom_prepare.prepareDestination in of_custom_prepare.outputs"
		
		ExprVar ofCustomPrepare_var = ExprVar.make(null, "of_custom_prepare", ofCustomPrepare.type());
		Decl ofCustomPrepare_decl = new Decl(null, null, null, List.of(ofCustomPrepare_var), ofCustomPrepare_var.oneOf());
		
		alloy.addToOverallFact(
			ofCustomPrepare_var.join(prepareDestination)
			.in(ofCustomPrepare_var.join(inputs.call()))
			.forAll(ofCustomPrepare_decl));
		
		alloy.addToOverallFact(
			ofCustomPrepare_var.join(prepareDestination)
			.in(ofCustomPrepare_var.join(outputs.call()))
			.forAll(ofCustomPrepare_decl));
		
		// OFServe
		// Implicit fact:
		// "servedFoodItem in this.inputs"
		// "servedFoodItem in this.outputs"
		// Explicit fact:
		// "all of_serve: OFServe | of_serve.servedFoodItem in of_serve.inputs"
		// "all of_serve: OFServe | of_serve.servedFoodItem in of_serve.outputs"
		
		ExprVar ofServe_var = ExprVar.make(null, "of_serve", ofServe.type());
		Decl ofServe_decl = new Decl(null, null, null, List.of(ofServe_var), ofServe_var.oneOf());
		
		alloy.addToOverallFact(ofServe_var.join(servedFoodItem).in(ofServe_var).join(inputs.call()).forAll(ofServe_decl));
		alloy.addToOverallFact(ofServe_var.join(servedFoodItem).in(ofServe_var).join(outputs.call()).forAll(ofServe_decl));
		
		// OFCustomServe
		// Implicit fact:
		// "serviceDestination in this.inputs"
		// Explicit fact:
		// "all of_custom_serve: OFCustomServe | of_custom_serve.serviceDestination in of_custom_serve.inputs"
		
		ExprVar ofCustomServe_var = ExprVar.make(null, "of_custom_serve", ofCustomServe.type());
		Decl ofCustomServe_decl = new Decl(null, null, null, List.of(ofCustomServe_var), ofCustomServe_var.oneOf());
		
		alloy.addToOverallFact(
			ofCustomServe_var.join(serviceDestination)
			.in(ofCustomServe_var.join(inputs.call()))
			.forAll(ofCustomServe_decl));
		
		// OFEat
		// Implicit fact:
		// "eatenItem in this.inputs"
		// "no this.outputs"
		// Explicit fact:
		// "all of_eat: OFEat | of_eat.eatenItem in of_eat.inputs"
		// "all of_eat: OFEat | no of_eat.outputs"
		
		ExprVar ofEat_var = ExprVar.make(null, "of_eat", ofEat.type());
		Decl ofEat_decl = new Decl(null, null, null, List.of(ofEat_var), ofEat_var.oneOf());
		
		alloy.addToOverallFact(ofEat_var.join(eatenItem).in(ofEat_var.join(inputs.call())).forAll(ofEat_decl));
		alloy.addToOverallFact(ofEat_var.join(outputs.call()).no().forAll(ofEat_decl));
		
		// OFPay
		// Implicit fact:
		// paidAmount in this.inputs
		// paidFoodItem in this.inputs
		// paidFoodItem in this.outputs
		// Explicit fact:
		// all of_pay: OFPay | of_pay.paidAmount in of_pay.inputs
		// all of_pay: OFPay | of_pay.paidFoodItem in of_pay.inputs
		// all of_pay: OFPay | of_pay.paidFoodItem in of_pay.outputs
		
		ExprVar ofPay_var = ExprVar.make(null, "of_pay", ofPay.type());
		Decl ofPay_decl = new Decl(null, null, null, List.of(ofPay_var), ofPay_var.oneOf());
		
		alloy.addToOverallFact(ofPay_var.join(paidAmount).in(ofPay_var.join(inputs.call())).forAll(ofPay_decl));
		alloy.addToOverallFact(ofPay_var.join(paidFoodItem).in(ofPay_var.join(inputs.call())).forAll(ofPay_decl));
		alloy.addToOverallFact(ofPay_var.join(paidFoodItem).in(ofPay_var.join(outputs.call())).forAll(ofPay_decl));
		
		// FoodService
		// Implicit facts:
		// bijectionFiltered[happensBefore, order, serve]
		// bijectionFiltered[happensBefore, prepare, serve]
		// bijectionFiltered[happensBefore, serve, eat]
		// order + prepare + pay + eat + serve in this.steps
		// Explicit facts:
		// all food_service: FoodService | bijectionFiltered[happensBefore, food_service.order, food_service.serve]
		// all food_service: FoodService | bijectionFiltered[happensBefore, food_service.prepare, food_service.serve]
		// all food_service: FoodService | bijectionFiltered[happensBefore, food_service.serve, food_service.eat]
		// all food_service: FoodService | food_service.order + food_service.prepare + food_service.pay + food_service.eat + food_service.serve in food_service.steps
		
		ExprVar foodService_var = ExprVar.make(null, "food_service", foodService.type());
		Decl foodService_decl = new Decl(null, null, null, List.of(foodService_var), foodService.oneOf());
		
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(foodService, foodService_order, foodService_serve);
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(foodService, foodService_prepare, foodService_serve);
		alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(foodService, foodService_serve, foodService_eat);
		
		alloy.addToOverallFact(foodService_var.join(foodService_order)
			.plus(foodService_var.join(foodService_prepare))
			.plus(foodService_var.join(foodService_pay))
			.plus(foodService_var.join(foodService_eat))
			.plus(foodService_var.join(foodService_serve))
			.in(foodService_var.join(steps.call())).forAll(foodService_decl));
		
		// OFFoodService
		// Implicit Fact:
		// 26) no this.inputs and no inputs.this
		// 27) no this.outputs and no outputs.this
		// 28) order in OFOrder
		// 29) prepare in OFPrepare
		// 30) pay in OFPay
		// 31) eat in OFEat
		// 32) serve in OFServe
		// 33) transferPrepareServe + transferOrderServe + transferServeEat in this.steps	
		// 34) bijectionFiltered[sources, transferPrepareServe, prepare]
		// 35) bijectionFiltered[targets, transferPrepareServe, serve]
		// 36) subsettingItemRuleForSources[transferPrepareServe]
		// 37) subsettingItemRuleForTargets[transferPrepareServe]
		// 38) bijectionFiltered[sources, transferServeEat, serve]
		// 39) bijectionFiltered[targets, transferServeEat, eat]
		// 40) subsettingItemRuleForSources[transferServeEat]
		// 41) subsettingItemRuleForTargets[transferServeEat]
		// 42) bijectionFiltered[sources, transferOrderServe, order]
		// 43) bijectionFiltered[targets, transferOrderServe, serve]
		// 44) subsettingItemRuleForSources[transferOrderServe]
		// 45) subsettingItemRuleForTargets[transferOrderServe]
		// Explicit Fact:
		// 26) no of_food_service.inputs and no inputs.of_food_service
		// 27) no of_food_service.outputs and no outputs.of_food_service
		// 28) of_food_service.order in OFOrder
		// 29) of_food_service.prepare in OFPrepare
		// 30) of_food_service.pay in OFPay
		// 31) of_food_service.eat in OFEat
		// 32) of_food_service.serve in OFServe
		// 33) of_food_service.transferPrepareServe + of_food_service.transferOrderServe + of_food_service.transferServeEat in of_food_service.steps
		// 34) bijectionFiltered[sources, of_food_service.transferPrepareServe, of_food_service.prepare]
		// 35) bijectionFiltered[targets, of_food_service.transferPrepareServe, of_food_service.serve]
		// 36) subsettingItemRuleForSources[of_food_service.transferPrepareServe]
		// 37) subsettingItemRuleForTargets[of_food_service.transferPrepareServe]
		// 38) bijectionFiltered[sources, of_food_service.transferServeEat, of_food_service.serve]
		// 39) bijectionFiltered[targets, of_food_service.transferServeEat, of_food_service.eat]
		// 40) subsettingItemRuleForSources[of_food_service.transferServeEat]
		// 41) subsettingItemRuleForTargets[of_food_service.transferServeEat]
		// 42) bijectionFiltered[sources, of_food_service.transferOrderServe, of_food_service.order]
		// 43) bijectionFiltered[targets, of_food_service.transferOrderServe, of_food_service.serve]
		// 44) subsettingItemRuleForSources[of_food_service.transferOrderServe]
		// 45) subsettingItemRuleForTargets[of_food_service.transferOrderServe]
		
		ExprVar ofFoodService_var = ExprVar.make(null, "of_food_service", ofFoodService.type());
		Decl ofFoodService_decl = new Decl(null, null, null, List.of(ofFoodService_var), ofFoodService.oneOf());
		
		/*26*/ alloy.addToOverallFact(ofFoodService_var.join(inputs.call()).and(inputs.call().join(ofFoodService_var)).forAll(ofFoodService_decl).forAll(ofFoodService_decl));
		/*27*/ alloy.addToOverallFact(ofFoodService_var.join(outputs.call()).no().and(outputs.call().and(ofFoodService_var)).no().forAll(ofFoodService_decl));
		/*28*/ alloy.addToOverallFact(ofFoodService_var.join(order).in(ofOrder).forAll(ofFoodService_decl));
		/*29*/ alloy.addToOverallFact(ofFoodService_var.join(prepare).in(ofPrepare).forAll(ofFoodService_decl));
		/*30*/ alloy.addToOverallFact(ofFoodService_var.join(pay).in(ofPay).forAll(ofFoodService_decl));
		/*31*/ alloy.addToOverallFact(ofFoodService_var.join(eat).in(ofEat).forAll(ofFoodService_decl));
		/*32*/ alloy.addToOverallFact(ofFoodService_var.join(serve).in(ofServe).forAll(ofFoodService_decl));
		/*33*/ alloy.addToOverallFact(ofFoodService_var.join(transferPrepareServe).plus(ofFoodService_var.join(transferOrderServe))
			.plus(ofFoodService_var.join(transferServeEat)).in(ofFoodService_var.join(steps.call())).forAll(ofFoodService_decl));
		/*34*/ alloy.addToOverallFact(bijectionFiltered.call(sources.call(), ofFoodService_var.join(transferPrepareServe), ofFoodService_var.join(prepare)).forAll(ofFoodService_decl));
		/*35*/ alloy.addToOverallFact(bijectionFiltered.call(targets.call(), ofFoodService_var.join(transferPrepareServe), ofFoodService_var.join(serve)).forAll(ofFoodService_decl));
		/*36*/ alloy.addToOverallFact(subsettingItemRuleForSources.call(ofFoodService_var.join(transferPrepareServe)).forAll(ofFoodService_decl));
		/*37*/ alloy.addToOverallFact(subsettingItemRuleForTargets.call(ofFoodService_var.join(transferPrepareServe)).forAll(ofFoodService_decl));
		/*38*/ alloy.addToOverallFact(bijectionFiltered.call(sources.call(), ofFoodService_var.join(transferServeEat), ofFoodService_var.join(serve)).forAll(ofFoodService_decl));
		/*39*/ alloy.addToOverallFact(bijectionFiltered.call(targets.call(), ofFoodService_var.join(transferServeEat), ofFoodService_var.join(eat)).forAll(ofFoodService_decl));
		/*40*/ alloy.addToOverallFact(subsettingItemRuleForSources.call(ofFoodService_var.join(transferServeEat)).forAll(ofFoodService_decl));
		/*41*/ alloy.addToOverallFact(subsettingItemRuleForTargets.call(ofFoodService_var.join(transferServeEat)).forAll(ofFoodService_decl));
		/*42*/ alloy.addToOverallFact(bijectionFiltered.call(sources.call(), ofFoodService_var.join(transferOrderServe), ofFoodService_var.join(order)).forAll(ofFoodService_decl));
		/*43*/ alloy.addToOverallFact(bijectionFiltered.call(targets.call(), ofFoodService_var.join(transferOrderServe), ofFoodService_var.join(serve)).forAll(ofFoodService_decl));
		/*44*/ alloy.addToOverallFact(subsettingItemRuleForSources.call(ofFoodService_var.join(transferOrderServe)).forAll(ofFoodService_decl));
		/*45*/ alloy.addToOverallFact(subsettingItemRuleForTargets.call(ofFoodService_var.join(transferOrderServe)).forAll(ofFoodService_decl));
		
		// OFSingleFoodService
		// Implicit Fact
		// 46) order in OFCustomOrder
		// 47) prepare in OFCustomPrepare
		// 48) serve in OFCustomServe
		// 49) transferOrderPrepare + transferOrderPay + transferPayEat in this.steps
		// 50) this.steps in order + prepare + pay + serve + eat + transferPrepareServe + transferOrderServe + transferServeEat + transferOrderPrepare + transferOrderPay + transferPayEat
		// 51) #order = 1
		// 52) order.outputs in order.orderedFoodItem + order.orderAmount + order.orderDestination
		// 53) pay.inputs in pay.paidAmount + pay.paidFoodItem
		// 54) pay.outputs in pay.paidFoodItem
		// 55) prepare.inputs in prepare.preparedFoodItem + prepare.prepareDestination
		// 56) prepare.outputs in prepare.preparedFoodItem + prepare.prepareDestination
		// 57) serve.inputs in serve.servedFoodItem + serve.serviceDestination
		// 58) serve.outputs in serve.servedFoodItem + serve.serviceDestination
		// 59) eat.inputs in eat.eatenItem
		// 60) bijectionFiltered[sources, transferOrderPay, order]
		// 61) bijectionFiltered[targets, transferOrderPay, pay]
		// 62) subsettingItemRuleForSources[transferOrderPay]
		// 63) subsettingItemRuleForTargets[transferOrderPay]
		// 64) transferOrderPay.items in transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount
		// 65) transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount in transferOrderPay.items
		// 66) transferOrderPay.items in transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount
		// 67) transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount in transferOrderPay.items
		// 68) bijectionFiltered[sources, transferPayEat, pay]
		// 69) bijectionFiltered[targets, transferPayEat, eat]
		// 70) subsettingItemRuleForSources[transferPayEat]
		// 71) subsettingItemRuleForTargets[transferPayEat]
		// 72) transferPayEat.items in transferPayEat.sources.paidFoodItem
		// 73) transferPayEat.items in transferPayEat.targets.eatenItem
		// 74) transferPayEat.sources.paidFoodItem in transferPayEat.items
		// 75) transferPayEat.targets.eatenItem in transferPayEat.items
		// 76) transferOrderServe.items in transferOrderServe.sources.orderedFoodItem
		// Explicit Fact
		// 46) of_single_food_service.order in OFCustomOrder
		// 47) of_single_food_service.prepare in OFCustomPrepare
		// 48) of_single_food_service.serve in OFCustomServe
		// 49) of_single_food_service.transferOrderPrepare + of_single_food_service.transferOrderPay + of_single_food_service.transferPayEat in of_single_food_service.steps
		// 50) of_single_food_service.steps in of_single_food_service.order + of_single_food_service.prepare + of_single_food_service.pay + of_single_food_service.serve + of_single_food_service.eat + of_single_food_service.transferPrepareServe + of_single_food_service.transferOrderServe + of_single_food_service.transferServeEat + of_single_food_service.transferOrderPrepare + of_single_food_service.transferOrderPay + of_single_food_service.transferPayEat
		// 51) #of_single_food_service.order = 1
		// 52) (of_single_food_service.order).outputs in (of_single_food_service.order).orderedFoodItem + (of_single_food_service.order).orderAmount + (of_single_food_service.order).orderDestination
		// 53) (of_single_food_service.pay).inputs + (of_single_food_service.pay).paidAmount in (of_single_food_service.pay).paidFoodItem
		// 54) (of_single_food_service.pay).outputs in (of_single_food_service.pay).paidFoodItem
		// 55) (of_single_food_service.prepare).inputs in (of_single_food_service.prepare).preparedFoodItem + (of_single_food_service.prepare).prepareDestination
		// 56) (of_single_food_service.prepare).outputs in (of_single_food_service.prepare).preparedFoodItem + (of_single_food_service.prepare).prepareDestination
		// 57) (of_single_food_service.serve).inputs in (of_single_food_service.serve).servedFoodItem + (of_single_food_service.serve).serviceDestination
		// 58) (of_single_food_service.serve).outputs in (of_single_food_service.serve).servedFoodItem + (of_single_food_service.serve).serviceDestination
		// 59) (of_single_food_service.eat).inputs in (of_single_food_service.eat).eatenItem
		// 60) bijectionFiltered[sources, of_single_food_service.transferOrderPay, of_single_food_service.order]
		// 61) bijectionFiltered[targets, of_single_food_service.transferOrderPay, of_single_food_service.pay]
		// 62) subsettingItemRuleForSources[of_single_food_service.transferOrderPay]
		// 63) subsettingItemRuleForTargets[of_single_food_service.transferOrderPay]
		// 64) (of_single_food_service.transferOrderPay).items in (of_single_food_service.transferOrderPay).sources.orderedFoodItem + (of_single_food_service.transferOrderPay).sources.orderAmount
		// 65) (of_single_food_service.transferOrderPay).sources.orderedFoodItem + (of_single_food_service.transferOrderPay).sources.orderAmount in (of_single_food_service.transferOrderPay).items
		// 66) (of_single_food_service.transferOrderPay).items in (of_single_food_service.transferOrderPay).targets.paidFoodItem + (of_single_food_service.transferOrderPay).targets.paidAmount
		// 67) (of_single_food_service.transferOrderPay).targets.paidFoodItem + (of_single_food_service.transferOrderPay).targets.paidAmount in (of_single_food_service.transferOrderPay).items
		// 68) bijectionFiltered[sources, of_single_food_service.transferPayEat, of_single_food_service.pay]
		// 69) bijectionFiltered[targets, of_single_food_service.transferPayEat, of_single_food_service.eat]
		// 70) subsettingItemRuleForSources[of_single_food_service.transferPayEat]
		// 71) subsettingItemRuleForTargets[of_single_food_service.transferPayEat]
		// 72) (of_single_food_service.transferPayEat).items in (of_single_food_service.transferPayEat).sources.paidFoodItem
		// 73) (of_single_food_service.transferPayEat).items in (of_single_food_service.transferPayEat).targets.eatenItem
		// 74) (of_single_food_service.transferPayEat).sources.paidFoodItem in (of_single_food_service.transferPayEat).items
		// 75) (of_single_food_service.transferPayEat).targets.eatenItem in (of_single_food_service.transferPayEat).items
		// 76) (of_single_food_service.transferOrderServe).items in (of_single_food_service.transferOrderServe).sources.orderedFoodItem
		
		ExprVar ofSingleFoodService_var = ExprVar.make(null, "of_single_food_service", ofSingleFoodService.type());
		Decl ofSingleFoodService_decl = new Decl(null, null, null, List.of(ofSingleFoodService_var), ofSingleFoodService.oneOf());
		
		/*46*/ 	alloy.addToOverallFact(ofSingleFoodService_var.join(order).in(ofCustomOrder).forAll(ofSingleFoodService_decl));
		/*47*/ 	alloy.addToOverallFact(ofSingleFoodService_var.join(prepare).in(ofCustomPrepare).forAll(ofSingleFoodService_decl));
		/*48*/ 	alloy.addToOverallFact(ofSingleFoodService_var.join(serve).in(ofCustomServe).forAll(ofSingleFoodService_decl));
		/*49*/ 	alloy.addToOverallFact(ofSingleFoodService_var.join(transferOrderPrepare).plus(ofSingleFoodService_var.join(transferOrderPay)).plus(ofSingleFoodService_var.join(transferPayEat)).in(ofSingleFoodService_var.join(steps.call())).forAll(ofSingleFoodService_decl));
		/*50*/	alloy.addToOverallFact(ofSingleFoodService_var.join(steps.call()).in(ofSingleFoodService_var.join(order).plus(ofSingleFoodService_var.join(prepare)).plus(ofSingleFoodService_var.join(pay)).plus(ofSingleFoodService_var.join(serve)).plus(ofSingleFoodService_var.join(eat)).plus(ofSingleFoodService_var.join(transferPrepareServe)).plus(ofSingleFoodService_var.join(transferOrderServe)).plus(ofSingleFoodService_var.join(transferServeEat)).plus(ofSingleFoodService_var.join(transferOrderPrepare)).plus(ofSingleFoodService_var.join(transferOrderPay)).plus(ofSingleFoodService_var.join(transferPayEat))).forAll(ofSingleFoodService_decl));
		/*51*/	alloy.addToOverallFact(ofSingleFoodService_var.join(order).cardinality().equal(ExprConstant.makeNUMBER(1)).forAll(ofSingleFoodService_decl));
		/*52*/	alloy.addToOverallFact(ofSingleFoodService_var.join(order).join(outputs.call()).in(ofSingleFoodService_var.join(order).join(orderedFoodItem).plus(ofSingleFoodService_var.join(order).join(orderAmount)).plus(ofSingleFoodService_var.join(order).join(orderDestination))).forAll(ofSingleFoodService_decl));
		/*53*/ 	alloy.addToOverallFact(ofSingleFoodService_var.join(pay).join(inputs.call()).plus(ofSingleFoodService_var.join(pay).join(paidAmount)).in(ofSingleFoodService_var.join(pay).join(paidFoodItem)).forAll(ofSingleFoodService_decl));
		/*54*/	alloy.addToOverallFact(ofSingleFoodService_var.join(pay).join(outputs.call()).in(ofSingleFoodService_var.join(pay).join(paidFoodItem)).forAll(ofSingleFoodService_decl));
		/*55*/	alloy.addToOverallFact(ofSingleFoodService_var.join(prepare).join(inputs.call()).in(ofSingleFoodService_var.join(prepare).join(preparedFoodItem).plus(ofSingleFoodService_var.join(prepare).join(prepareDestination))).forAll(ofSingleFoodService_decl));
		/*56*/	alloy.addToOverallFact(ofSingleFoodService_var.join(prepare).join(outputs.call()).in(ofSingleFoodService_var.join(prepare).join(preparedFoodItem).plus(ofSingleFoodService_var.join(prepare).join(prepareDestination))).forAll(ofSingleFoodService_decl));
		/*57*/	alloy.addToOverallFact(ofSingleFoodService_var.join(serve).join(inputs.call()).in(ofSingleFoodService_var.join(serve).join(servedFoodItem)).plus(ofSingleFoodService_var.join(serve).join(serviceDestination)).forAll(ofSingleFoodService_decl));
		/*58*/	alloy.addToOverallFact(ofSingleFoodService_var.join(serve).join(outputs.call()).in(ofSingleFoodService_var.join(serve).join(servedFoodItem)).plus(ofSingleFoodService_var.join(serve).join(serviceDestination)).forAll(ofSingleFoodService_decl));
		/*59*/	alloy.addToOverallFact(ofSingleFoodService_var.join(eat).join(inputs.call()).in(ofSingleFoodService_var.join(eat).join(eatenItem)).forAll(ofSingleFoodService_decl));
		/*60*/ 	alloy.addToOverallFact(bijectionFiltered.call(sources.call(), ofSingleFoodService_var.join(transferOrderPay), ofSingleFoodService.join(order)).forAll(ofSingleFoodService_decl));
		/*61*/	alloy.addToOverallFact(bijectionFiltered.call(targets.call(), ofSingleFoodService_var.join(transferOrderPay), ofSingleFoodService.join(pay)).forAll(ofSingleFoodService_decl));
		/*62*/	alloy.addToOverallFact(subsettingItemRuleForSources.call(ofSingleFoodService_var.join(transferOrderPay)).forAll(ofSingleFoodService_decl));
		/*63*/	alloy.addToOverallFact(subsettingItemRuleForTargets.call(ofSingleFoodService_var.join(transferOrderPay)).forAll(ofSingleFoodService_decl));
		/*64*/	alloy.addToOverallFact(ofSingleFoodService_var.join(transferOrderPay).join(items.call()).in(ofSingleFoodService_var.join(transferOrderPay).join(sources.call()).join(orderedFoodItem).plus(ofSingleFoodService_var).join(transferOrderPay).join(sources.call()).join(orderAmount)).forAll(ofSingleFoodService_decl));
		/*65*/	alloy.addToOverallFact(ofSingleFoodService_var.join(transferOrderPay).join(sources.call()).join(orderedFoodItem).plus(ofSingleFoodService_var.join(transferOrderPay).join(sources.call()).join(orderAmount)).in(ofSingleFoodService.join(transferOrderPay).join(items.call())).forAll(ofSingleFoodService_decl));
		/*66*/	alloy.addToOverallFact(ofSingleFoodService_var.join(transferOrderPay).join(items.call()).in(ofSingleFoodService_var.join(transferOrderPay).join(targets.call()).join(paidFoodItem).plus(ofSingleFoodService_var.join(transferOrderPay).join(targets.call()).join(paidAmount))).forAll(ofSingleFoodService_decl));
		/*67*/	alloy.addToOverallFact(ofSingleFoodService.join(transferOrderPay).join(targets.call()).join(paidFoodItem).plus(ofSingleFoodService_var.join(transferOrderPay).join(targets.call()).join(paidAmount)).in(ofSingleFoodService_var.join(transferOrderPay).join(items.call())).forAll(ofSingleFoodService_decl));
		/*68*/	alloy.addToOverallFact(bijectionFiltered.call(sources.call(), ofSingleFoodService.join(transferPayEat), ofSingleFoodService.join(pay)).forAll(ofSingleFoodService_decl));
		/*69*/	alloy.addToOverallFact(bijectionFiltered.call(targets.call(), ofSingleFoodService.join(transferPayEat), ofSingleFoodService.join(eat)).forAll(ofSingleFoodService_decl));
		/*70*/	alloy.addToOverallFact(subsettingItemRuleForSources.call(ofSingleFoodService.join(transferPayEat)).forAll(ofSingleFoodService_decl));
		/*71*/	alloy.addToOverallFact(subsettingItemRuleForTargets.call(ofSingleFoodService.join(transferPayEat)).forAll(ofSingleFoodService_decl));
		/*72*/	alloy.addToOverallFact(ofSingleFoodService.join(transferPayEat).join(items.call()).in(ofSingleFoodService_var.join(transferPayEat).join(sources.call().join(paidFoodItem))).forAll(ofSingleFoodService_decl));
		/*73*/	alloy.addToOverallFact(ofSingleFoodService.join(transferPayEat).join(items.call()).in(ofSingleFoodService_var.join(transferPayEat).join(targets.call().join(eatenItem))).forAll(ofSingleFoodService_decl));
		/*74*/	alloy.addToOverallFact(ofSingleFoodService_var.join(transferPayEat).join(sources.call()).join(paidFoodItem).in(ofSingleFoodService.join(transferPayEat).join(items.call())).forAll(ofSingleFoodService_decl));
		/*75*/	alloy.addToOverallFact(ofSingleFoodService.join(transferPayEat).join(targets.call()).join(eatenItem).in(ofSingleFoodService_var.join(transferPayEat).join(items.call())).forAll(ofSingleFoodService_decl));
		/*76*/	alloy.addToOverallFact(ofSingleFoodService.join(transferOrderServe).join(items.call()).in(ofSingleFoodService.join(transferOrderServe).join(sources.call()).join(orderedFoodItem)));
		
	
	}	

}
