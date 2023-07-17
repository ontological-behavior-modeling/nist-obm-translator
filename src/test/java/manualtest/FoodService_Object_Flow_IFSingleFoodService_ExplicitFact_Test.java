package manualtest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.Helper;
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
		Sig oforder = alloy.createSigAndAddToAllSigs("OFOrder", (Sig.PrimSig) order);
		Sig ofCustomOrder = alloy.createSigAndAddToAllSigs("OFCustomOrder", (Sig.PrimSig) oforder);
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
		
		// OFOrder
		Sig.Field ofOrder_orderedFoodItem = FuncUtils.addOneField("orderedFoodItem", oforder, foodItem);
		
		// OFCustomOrder
		Sig.Field ofCustomOrder_orderAmount = FuncUtils.addOneField("orderAmount", ofCustomOrder, real);
		Sig.Field ofCustomOrder_orderDestination = FuncUtils.addOneField("orderDestination", ofCustomOrder, location);
		
		// OFPrepare
		Sig.Field ofPrepare_preparedFoodItem = FuncUtils.addOneField("preparedFoodItem", ofPrepare, foodItem);
		
		// OFCustomPrepare
		Sig.Field ofCustomPrepare_prepareDestination = FuncUtils.addOneField("prepareDestination", ofCustomPrepare, location);
		
		// OFServe
		Sig.Field ofServe_servedFoodItem = FuncUtils.addOneField("servedFoodItem", ofServe, foodItem);
		
		// OFCustomServe
		Sig.Field ofCustomServe_serviceDestination = FuncUtils.addOneField("serviceDestination", ofCustomServe, location);
		
		// OFEat
		Sig.Field ofEat_eatenItem = FuncUtils.addOneField("eatenItem", ofEat, foodItem);
		
		// OFPay
		Sig.Field ofPay_paidAmount = FuncUtils.addOneField("paidAmount", ofPay, real);
		Sig.Field ofPay_paidFoodItem = FuncUtils.addOneField("paidFoodItem", ofPay, foodItem);
		
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
		
		// OFSingleFoodService
		Sig.Field[] ofSingleFoodService_fields = ofSingleFoodService.addTrickyField(
			null, null, Pos.UNKNOWN, null, null, 
			new String[] {"transferOrderPrepare", "transferOrderPay", "transferPayEat"}, 
			transferBefore);
		
		// OFLoopFoodService
		Sig.Field ofLoopFoodService_start = FuncUtils.addOneField("start", ofLoopFoodService, ofStart);
		Sig.Field ofLoopFoodService_end = FuncUtils.addOneField("end", ofLoopFoodService, ofEnd);
		Sig.Field[] ofLoopFoodService_fields = ofLoopFoodService.addTrickyField(
			null, null, Pos.UNKNOWN, null, null, 
			new String[] {"transferOrderPrepare", "transferOrderPay", "transferPayEat"}, 
			transferBefore);
		
		// OFParallelFoodService: none
		
		
		
	}	

}
