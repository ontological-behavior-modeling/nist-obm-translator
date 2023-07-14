package manualtest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.mit.csail.sdg.ast.Sig;


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
		Sig ofstart = alloy.createSigAsChildOfOccSigAndAddToAllSigs("OFStart");
		Sig ofend = alloy.createSigAsChildOfOccSigAndAddToAllSigs("OFEnd");
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

		// ========== Define list of relations unique to the file ==========
		
		// Order: none
		// Prepare: none
		// Serve: none
		// Eat: none
		// Pay: none
		// FoodItem: none
		// Location: none
		// Real: none
		// OFStart: none
		// OFEnd: none
		
		// OFOrder:
		Sig.Field ofOrder_orderedFoodItem;
	}

}
