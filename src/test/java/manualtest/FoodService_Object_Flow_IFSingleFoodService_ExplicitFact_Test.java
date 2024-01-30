package manualtest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.Translator;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
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

class FoodService_Object_Flow_IFSingleFoodService_ExplicitFact_Test {

  @Test
  void test() {
    String moduleName = "FoodServiceObjectControlFlowIFSingleFoodService_ExplicitFact";
    String outFileName = "src/test/resources/generated-" + moduleName + ".als";
    String filename =
        "src/test/resources/4.2.2_FoodServiceObjectFlowIFSingleFoodService_ExplicitFacts.als";
    Alloy alloy = new Alloy("src/test/resources");

    // ========== Define list of signatures unique to the file ==========

    Sig order = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Order");
    Sig prepare = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Prepare");
    Sig serve = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Serve");
    Sig eat = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Eat");
    Sig pay = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Pay");
    Sig foodItem = alloy.createSigAsChildOfOccSigAndAddToAllSigs("FoodItem");
    Sig location = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Location");
    Sig real = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Real");
    Sig ofStart = alloy.createSigAsChildOfOccSigAndAddToAllSigs("OFStart");
    Sig ofEnd = alloy.createSigAsChildOfOccSigAndAddToAllSigs("OFEnd");
    Sig ofOrder = alloy.createSigAndAddToAllSigs("OFOrder", (Sig.PrimSig) order);
    Sig ofCustomOrder = alloy.createSigAndAddToAllSigs("OFCustomOrder", (Sig.PrimSig) ofOrder);
    Sig ofPrepare = alloy.createSigAndAddToAllSigs("OFPrepare", (Sig.PrimSig) prepare);
    Sig ofCustomPrepare =
        alloy.createSigAndAddToAllSigs("OFCustomPrepare", (Sig.PrimSig) ofPrepare);
    Sig ofServe = alloy.createSigAndAddToAllSigs("OFServe", (Sig.PrimSig) serve);
    Sig ofCustomServe = alloy.createSigAndAddToAllSigs("OFCustomServe", (Sig.PrimSig) ofServe);
    Sig ofEat = alloy.createSigAndAddToAllSigs("OFEat", (Sig.PrimSig) eat);
    Sig ofPay = alloy.createSigAndAddToAllSigs("OFPay", (Sig.PrimSig) pay);
    Sig foodService = alloy.createSigAsChildOfOccSigAndAddToAllSigs("FoodService");
    Sig ofFoodService = alloy.createSigAndAddToAllSigs("OFFoodService", (Sig.PrimSig) foodService);
    Sig ofSingleFoodService =
        alloy.createSigAndAddToAllSigs("OFSingleFoodService", (Sig.PrimSig) ofFoodService);
    Sig ofLoopFoodService =
        alloy.createSigAndAddToAllSigs("OFLoopFoodService", (Sig.PrimSig) ofFoodService);
    Sig ofParallelFoodService =
        alloy.createSigAndAddToAllSigs("OFParallelFoodService", (Sig.PrimSig) ofFoodService);

    Sig transferBefore = AlloyUtils.getReachableSig(alloy.getTemplateModule(), "o/TransferBefore");

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
    Sig.Field orderedFoodItem_ofOrder = FuncUtils.addOneField("orderedFoodItem", ofOrder, foodItem);

    // OFCustomOrder
    Sig.Field orderAmount_ofCustomOrder = FuncUtils.addOneField("orderAmount", ofCustomOrder, real);
    Sig.Field orderDestination_ofCustomOrder =
        FuncUtils.addOneField("orderDestination", ofCustomOrder, location);

    // OFPrepare
    Sig.Field preparedFoodItem_ofPrepare =
        FuncUtils.addOneField("preparedFoodItem", ofPrepare, foodItem);

    // OFCustomPrepare
    Sig.Field prepareDestination_ofCustomPrepare =
        FuncUtils.addOneField("prepareDestination", ofCustomPrepare, location);

    // OFServe
    Sig.Field servedFoodItem_ofServe = FuncUtils.addOneField("servedFoodItem", ofServe, foodItem);

    // OFCustomServe
    Sig.Field serviceDestination_ofCustomServe =
        FuncUtils.addOneField("serviceDestination", ofCustomServe, location);

    // OFEat
    Sig.Field eatenItem_ofEat = FuncUtils.addOneField("eatenItem", ofEat, foodItem);

    // OFPay
    Sig.Field paidAmount_ofPay = FuncUtils.addOneField("paidAmount", ofPay, real);
    Sig.Field paidFoodItem_ofPay = FuncUtils.addOneField("paidFoodItem", ofPay, foodItem);

    // FoodService

    Sig.Field order_foodService = FuncUtils.addField("order", foodService, order);
    Sig.Field prepare_foodService = FuncUtils.addField("prepare", foodService, prepare);
    Sig.Field pay_foodService = FuncUtils.addField("pay", foodService, pay);
    Sig.Field eat_foodService = FuncUtils.addField("eat", foodService, eat);
    Sig.Field serve_foodService = FuncUtils.addField("serve", foodService, serve);

    // OFFoodService
    Sig.Field[] ofFoodService_fields =
        ofFoodService.addTrickyField(
            null,
            null,
            Pos.UNKNOWN,
            null,
            null,
            new String[] {"transferPrepareServe", "transferOrderServe", "transferServeEat"},
            transferBefore.setOf());
    Sig.Field transferPrepareServe_ofFoodService = ofFoodService_fields[0];
    Sig.Field transferOrderServe_ofFoodService = ofFoodService_fields[1];
    Sig.Field transferServeEat_ofFoodService = ofFoodService_fields[2];

    // OFSingleFoodService
    Sig.Field[] ofSingleFoodService_fields =
        ofSingleFoodService.addTrickyField(
            null,
            null,
            Pos.UNKNOWN,
            null,
            null,
            new String[] {"transferOrderPrepare", "transferOrderPay", "transferPayEat"},
            transferBefore.setOf());
    Sig.Field transferOrderPrepare_ofSingleFoodService = ofSingleFoodService_fields[0];
    Sig.Field transferOrderPay_ofSingleFoodService = ofSingleFoodService_fields[1];
    Sig.Field transferPayEat_ofSingleFoodService = ofSingleFoodService_fields[2];

    // OFLoopFoodService
    Sig.Field start_ofLoopFoodService = FuncUtils.addOneField("start", ofLoopFoodService, ofStart);
    Sig.Field end_ofLoopFoodService = FuncUtils.addOneField("end", ofLoopFoodService, ofEnd);
    Sig.Field[] ofLoopFoodService_fields =
        ofLoopFoodService.addTrickyField(
            null,
            null,
            Pos.UNKNOWN,
            null,
            null,
            new String[] {"transferOrderPrepare", "transferOrderPay", "transferPayEat"},
            transferBefore.setOf());
    Sig.Field transferOrderPrepare_ofLoopFoodService = ofLoopFoodService_fields[0];
    Sig.Field transferOrderPay_ofLoopFoodService = ofLoopFoodService_fields[1];
    Sig.Field transferPayEat_ofLoopFoodService = ofLoopFoodService_fields[2];

    // OFParallelFoodService: none

    /***** Explicit Facts *****/

    // Setup:
    Func happensBefore = AlloyUtils.getFunction(Alloy.transferModule, "o/happensBefore");
    Func steps = AlloyUtils.getFunction(Alloy.transferModule, "o/steps");
    Func inputs = AlloyUtils.getFunction(Alloy.transferModule, "o/inputs");
    Func outputs = AlloyUtils.getFunction(Alloy.transferModule, "o/outputs");
    Func items = AlloyUtils.getFunction(Alloy.transferModule, "o/items");
    Func sources = AlloyUtils.getFunction(Alloy.transferModule, "o/sources");
    Func targets = AlloyUtils.getFunction(Alloy.transferModule, "o/targets");
    Func functionFiltered = AlloyUtils.getFunction(Alloy.transferModule, "o/functionFiltered");
    Func bijectionFiltered = AlloyUtils.getFunction(Alloy.transferModule, "o/bijectionFiltered");
    Func inverseFunctionFiltered =
        AlloyUtils.getFunction(Alloy.transferModule, "o/inverseFunctionFiltered");
    Func subsettingItemRuleForSources =
        AlloyUtils.getFunction(Alloy.transferModule, "o/subsettingItemRuleForSources");
    Func subsettingItemRuleForTargets =
        AlloyUtils.getFunction(Alloy.transferModule, "o/subsettingItemRuleForTargets");

    // Order: none
    // Prepare: none
    // Serve: none
    // Eat: none
    // Pay: none

    /*****
     * FoodItem: Implicit fact: "no this.happensBefore && no happensBefore.this && no this.steps &&
     * no this.inputs && no this.outputs" Explicit fact: "all food_item: FoodItem | no
     * food_item.happensBefore && no happensBefore.food_item && no food_item.steps && no
     * food_item.inputs && no food_item.outputs"
     *****/

    ExprVar foodItem_var = ExprVar.make(null, "food_item", foodItem.type());
    Decl foodItem_decl = new Decl(null, null, null, List.of(foodItem_var), foodItem.oneOf());

    alloy.addToOverallFact(
        foodItem_var
            .join(happensBefore.call())
            .no()
            .and(happensBefore.call().join(foodItem_var).no())
            .and(foodItem_var.join(steps.call()).no())
            .and(foodItem_var.join(inputs.call()).no())
            .and(foodItem_var.join(outputs.call()).no())
            .forAll(foodItem_decl));

    /*****
     * Location: Implicit fact: "no this.happensBefore && no happensBefore.this && no this.steps &&
     * no this.inputs && no this.outputs" Explicit fact: "all location: Location | no
     * location.happensBefore && no happensBefore.location && no location.steps && no
     * location.inputs && no location.outputs"
     *****/

    ExprVar location_var = ExprVar.make(null, "location", location.type());
    Decl location_decl = new Decl(null, null, null, List.of(location_var), location.oneOf());

    alloy.addToOverallFact(
        location_var
            .join(happensBefore.call())
            .no()
            .and(happensBefore.call().join(location_var).no())
            .and(location_var.join(steps.call()).no())
            .and(location_var.join(inputs.call()).no())
            .and(location_var.join(outputs.call()).no())
            .forAll(location_decl));

    /*****
     * Location: Implicit fact: "no this.happensBefore && no happensBefore.this && no this.steps &&
     * no this.inputs && no this.outputs" Explicit fact: "all real: Real | no real.happensBefore &&
     * no happensBefore.real && no real.steps && no real.inputs && no real.outputs"
     *****/

    ExprVar real_var = ExprVar.make(null, "real", real.type());
    Decl real_decl = new Decl(null, null, null, List.of(real_var), real.oneOf());

    alloy.addToOverallFact(
        real_var
            .join(happensBefore.call())
            .no()
            .and(happensBefore.call().join(real_var).no())
            .and(real_var.join(steps.call()).no())
            .and(real_var.join(inputs.call()).no())
            .and(real_var.join(outputs.call()).no())
            .forAll(real_decl));

    /*****
     * OFStart: Implicit fact: "no inputs.this && no outputs.this && no items.this" Explicit fact:
     * "all of_start: OFStart | no inputs.of_start && no outputs.of_start && no items.of_start"
     *****/

    ExprVar ofStart_var = ExprVar.make(null, "of_start", ofStart.type());
    Decl ofStart_decl = new Decl(null, null, null, List.of(ofStart_var), ofStart.oneOf());

    alloy.addToOverallFact(
        inputs
            .call()
            .join(ofStart_var)
            .no()
            .and(outputs.call().join(ofStart_var).no())
            .and(items.call().join(ofStart_var).no())
            .forAll(ofStart_decl));

    /*****
     * OFEnd: Implicit fact: "no inputs.this && no outputs.this && no items.this" Explicit fact:
     * "all of_end: OFEnd | no inputs.of_end && no outputs.of_end && no items.of_end"
     *****/

    ExprVar ofEnd_var = ExprVar.make(null, "of_end", ofEnd.type());
    Decl ofEnd_decl = new Decl(null, null, null, List.of(ofEnd_var), ofEnd.oneOf());

    alloy.addToOverallFact(
        inputs
            .call()
            .join(ofEnd_var)
            .no()
            .and(outputs.call().join(ofEnd_var).no())
            .and(items.call().join(ofEnd_var).no())
            .forAll(ofEnd_decl));

    // ofOrder
    // Implicit fact:
    // "no this.inputs"
    // "orderedFoodItem_ofOrder in this.outputs"
    // Explicit fact:
    // "all of_order: ofOrder | no of_order.inputs"
    // "all of_order: ofOrder | of_order.orderedFoodItem_ofOrder in of_order.outputs"

    ExprVar ofOrder_var = ExprVar.make(null, "of_order", ofOrder.type());
    Decl ofOrder_decl = new Decl(null, null, null, List.of(ofOrder_var), ofOrder.oneOf());

    alloy.addToOverallFact(ofOrder_var.join(inputs.call()).no().forAll(ofOrder_decl));
    alloy.addToOverallFact(
        ofOrder_var
            .join(orderedFoodItem_ofOrder)
            .in(ofOrder_var.join(outputs.call()))
            .forAll(ofOrder_decl));

    // OFCustomOrder
    // Implicit fact:
    // "orderAmount_ofCustomOrder in this.outputs"
    // "orderDestination_ofCustomOrder in this.outputs"
    // Explicit fact:
    // "all of_custom_order: OFCustomOrder | of_custom_order.orderAmount_ofCustomOrder in
    // of_custom_order.outputs"
    // "all of_custom_order: OFCustomOrder | of_custom_order.orderDestination_ofCustomOrder in
    // of_custom_order.outputs"

    ExprVar ofCustomOrder_var = ExprVar.make(null, "of_custom_order", ofCustomOrder.type());
    Decl ofCustomOrder_decl =
        new Decl(null, null, null, List.of(ofCustomOrder_var), ofCustomOrder.oneOf());

    /* 8 */ alloy.addToOverallFact(
        ofCustomOrder_var
            .join(orderAmount_ofCustomOrder)
            .in(ofCustomOrder_var.join(outputs.call()))
            .forAll(ofCustomOrder_decl));
    /* 9 */ alloy.addToOverallFact(
        ofCustomOrder_var
            .join(orderDestination_ofCustomOrder)
            .in(ofCustomOrder_var.join(outputs.call()))
            .forAll(ofCustomOrder_decl));

    // OFPrepare
    // Implicit fact:
    // "preparedFoodItem_ofPrepare in this.inputs"
    // "preparedFoodItem_ofPrepare in this.outputs"
    // Explicit fact:
    // "all of_prepare: OFPrepare | of_prepare.preparedFoodItem_ofPrepare in of_prepare.inputs"
    // "all of_prepare: OFPrepare | of_prepare.preparedFoodItem_ofPrepare in of_prepare.outputs"

    ExprVar ofPrepare_var = ExprVar.make(null, "of_prepare", ofPrepare.type());
    Decl ofPrepareOrder_decl =
        new Decl(null, null, null, List.of(ofPrepare_var), ofPrepare.oneOf());

    /* 10 */ alloy.addToOverallFact(
        ofPrepare_var
            .join(preparedFoodItem_ofPrepare)
            .in(ofPrepare_var.join(inputs.call()))
            .forAll(ofPrepareOrder_decl));
    /* 11 */ alloy.addToOverallFact(
        ofPrepare_var
            .join(preparedFoodItem_ofPrepare)
            .in(ofPrepare_var.join(outputs.call()))
            .forAll(ofPrepareOrder_decl));

    // OFCustomPrepare
    // Implicit fact:
    // "prepareDestination_ofCustomPrepare in this.inputs"
    // "prepareDestination_ofCustomPrepare in this.outputs"
    // Explicit fact:
    // "all of_custom_prepare: OFCustomPrepare |
    // of_custom_prepare.prepareDestination_ofCustomPrepare in of_custom_prepare.inputs"
    // "all of_custom_prepare: OFCustomPrepare |
    // of_custom_prepare.prepareDestination_ofCustomPrepare in of_custom_prepare.outputs"

    ExprVar ofCustomPrepare_var = ExprVar.make(null, "of_custom_prepare", ofCustomPrepare.type());
    Decl ofCustomPrepare_decl =
        new Decl(null, null, null, List.of(ofCustomPrepare_var), ofCustomPrepare.oneOf());

    /* 12 */ alloy.addToOverallFact(
        ofCustomPrepare_var
            .join(prepareDestination_ofCustomPrepare)
            .in(ofCustomPrepare_var.join(inputs.call()))
            .forAll(ofCustomPrepare_decl));
    /* 13 */ alloy.addToOverallFact(
        ofCustomPrepare_var
            .join(prepareDestination_ofCustomPrepare)
            .in(ofCustomPrepare_var.join(outputs.call()))
            .forAll(ofCustomPrepare_decl));

    // OFServe
    // Implicit fact:
    // "servedFoodItem_ofServe in this.inputs"
    // "servedFoodItem_ofServe in this.outputs"
    // Explicit fact:
    // "all of_serve: OFServe | of_serve.servedFoodItem_ofServe in of_serve.inputs"
    // "all of_serve: OFServe | of_serve.servedFoodItem_ofServe in of_serve.outputs"

    ExprVar ofServe_var = ExprVar.make(null, "of_serve", ofServe.type());
    Decl ofServe_decl = new Decl(null, null, null, List.of(ofServe_var), ofServe.oneOf());

    /* 14 */ alloy.addToOverallFact(
        ofServe_var
            .join(servedFoodItem_ofServe)
            .in(ofServe_var.join(inputs.call()))
            .forAll(ofServe_decl));
    /* 15 */ alloy.addToOverallFact(
        ofServe_var
            .join(servedFoodItem_ofServe)
            .in(ofServe_var.join(outputs.call()))
            .forAll(ofServe_decl));

    // OFCustomServe
    // Implicit fact:
    // "serviceDestination_ofCustomServe in this.inputs"
    // Explicit fact:
    // "all of_custom_serve: OFCustomServe | of_custom_serve.serviceDestination_ofCustomServe in
    // of_custom_serve.inputs"

    ExprVar ofCustomServe_var = ExprVar.make(null, "of_custom_serve", ofCustomServe.type());
    Decl ofCustomServe_decl =
        new Decl(null, null, null, List.of(ofCustomServe_var), ofCustomServe.oneOf());

    /* 16 */ alloy.addToOverallFact(
        ofCustomServe_var
            .join(serviceDestination_ofCustomServe)
            .in(ofCustomServe_var.join(inputs.call()))
            .forAll(ofCustomServe_decl));

    // OFEat
    // Implicit fact:
    // "eatenItem_ofEat in this.inputs"
    // "no this.outputs"
    // Explicit fact:
    // "all of_eat: OFEat | of_eat.eatenItem_ofEat in of_eat.inputs"
    // "all of_eat: OFEat | no of_eat.outputs"

    ExprVar ofEat_var = ExprVar.make(null, "of_eat", ofEat.type());
    Decl ofEat_decl = new Decl(null, null, null, List.of(ofEat_var), ofEat.oneOf());

    /* 17 */ alloy.addToOverallFact(
        ofEat_var.join(eatenItem_ofEat).in(ofEat_var.join(inputs.call())).forAll(ofEat_decl));
    /* 18 */ alloy.addToOverallFact(ofEat_var.join(outputs.call()).no().forAll(ofEat_decl));

    // OFPay
    // Implicit fact:
    // paidAmount_ofPay in this.inputs
    // paidFoodItem_ofPay in this.inputs
    // paidFoodItem_ofPay in this.outputs
    // Explicit fact:
    // all of_pay: OFPay | of_pay.paidAmount_ofPay in of_pay.inputs
    // all of_pay: OFPay | of_pay.paidFoodItem_ofPay in of_pay.inputs
    // all of_pay: OFPay | of_pay.paidFoodItem_ofPay in of_pay.outputs

    ExprVar ofPay_var = ExprVar.make(null, "of_pay", ofPay.type());
    Decl ofPay_decl = new Decl(null, null, null, List.of(ofPay_var), ofPay.oneOf());

    /* 19 */ alloy.addToOverallFact(
        ofPay_var.join(paidAmount_ofPay).in(ofPay_var.join(inputs.call())).forAll(ofPay_decl));
    /* 20 */ alloy.addToOverallFact(
        ofPay_var.join(paidFoodItem_ofPay).in(ofPay_var.join(inputs.call())).forAll(ofPay_decl));
    /* 21 */ alloy.addToOverallFact(
        ofPay_var.join(paidFoodItem_ofPay).in(ofPay_var.join(outputs.call())).forAll(ofPay_decl));

    // FoodService
    // Implicit facts:
    // bijectionFiltered[happensBefore, order, serve]
    // bijectionFiltered[happensBefore, prepare, serve]
    // bijectionFiltered[happensBefore, serve, eat]
    // order + prepare + pay + eat + serve in this.steps
    // Explicit facts:
    // all food_service: FoodService | bijectionFiltered[happensBefore, food_service.order,
    // food_service.serve]
    // all food_service: FoodService | bijectionFiltered[happensBefore, food_service.prepare,
    // food_service.serve]
    // all food_service: FoodService | bijectionFiltered[happensBefore, food_service.serve,
    // food_service.eat]
    // all food_service: FoodService | food_service.order + food_service.prepare + food_service.pay
    // + food_service.eat + food_service.serve in food_service.steps

    ExprVar foodService_var = ExprVar.make(null, "food_service", foodService.type());
    Decl foodService_decl =
        new Decl(null, null, null, List.of(foodService_var), foodService.oneOf());

    /* 22 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                happensBefore.call(),
                foodService_var.join(order_foodService),
                foodService_var.join(serve_foodService))
            .forAll(foodService_decl));
    /* 23 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                happensBefore.call(),
                foodService_var.join(prepare_foodService),
                foodService_var.join(serve_foodService))
            .forAll(foodService_decl));
    /* 24 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                happensBefore.call(),
                foodService_var.join(serve_foodService),
                foodService_var.join(eat_foodService))
            .forAll(foodService_decl));
    /* 25 */ alloy.addToOverallFact(
        foodService_var
            .join(order_foodService)
            .plus(foodService_var.join(prepare_foodService))
            .plus(foodService_var.join(pay_foodService))
            .plus(foodService_var.join(eat_foodService))
            .plus(foodService_var.join(serve_foodService))
            .in(foodService_var.join(steps.call()))
            .forAll(foodService_decl));

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
    // 33) of_food_service.transferPrepareServe + of_food_service.transferOrderServe +
    // of_food_service.transferServeEat in of_food_service.steps
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
    Decl ofFoodService_decl =
        new Decl(null, null, null, List.of(ofFoodService_var), ofFoodService.oneOf());

    /* 26 */ alloy.addToOverallFact(
        ofFoodService_var
            .join(inputs.call())
            .no()
            .and(inputs.call().join(ofFoodService_var).no())
            .forAll(ofFoodService_decl));
    /* 27 */ alloy.addToOverallFact(
        ofFoodService_var
            .join(outputs.call())
            .no()
            .and(outputs.call().join(ofFoodService_var).no())
            .forAll(ofFoodService_decl));
    /* 28 */ alloy.addToOverallFact(
        ofFoodService_var.join(order_foodService).in(ofOrder).forAll(ofFoodService_decl));
    /* 29 */ alloy.addToOverallFact(
        ofFoodService_var.join(prepare_foodService).in(ofPrepare).forAll(ofFoodService_decl));
    /* 30 */ alloy.addToOverallFact(
        ofFoodService_var.join(pay_foodService).in(ofPay).forAll(ofFoodService_decl));
    /* 31 */ alloy.addToOverallFact(
        ofFoodService_var.join(eat_foodService).in(ofEat).forAll(ofFoodService_decl));
    /* 32 */ alloy.addToOverallFact(
        ofFoodService_var.join(serve_foodService).in(ofServe).forAll(ofFoodService_decl));
    /* 33 */ alloy.addToOverallFact(
        ofFoodService_var
            .join(transferPrepareServe_ofFoodService)
            .plus(ofFoodService_var.join(transferOrderServe_ofFoodService))
            .plus(ofFoodService_var.join(transferServeEat_ofFoodService))
            .in(ofFoodService_var.join(steps.call()))
            .forAll(ofFoodService_decl));
    /* 34 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                sources.call(),
                ofFoodService_var.join(transferPrepareServe_ofFoodService),
                ofFoodService_var.join(prepare_foodService))
            .forAll(ofFoodService_decl));
    /* 35 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                targets.call(),
                ofFoodService_var.join(transferPrepareServe_ofFoodService),
                ofFoodService_var.join(serve_foodService))
            .forAll(ofFoodService_decl));
    /* 36 */ alloy.addToOverallFact(
        subsettingItemRuleForSources
            .call(ofFoodService_var.join(transferPrepareServe_ofFoodService))
            .forAll(ofFoodService_decl));
    /* 37 */ alloy.addToOverallFact(
        subsettingItemRuleForTargets
            .call(ofFoodService_var.join(transferPrepareServe_ofFoodService))
            .forAll(ofFoodService_decl));
    /* 38 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                sources.call(),
                ofFoodService_var.join(transferServeEat_ofFoodService),
                ofFoodService_var.join(serve_foodService))
            .forAll(ofFoodService_decl));
    /* 39 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                targets.call(),
                ofFoodService_var.join(transferServeEat_ofFoodService),
                ofFoodService_var.join(eat_foodService))
            .forAll(ofFoodService_decl));
    /* 40 */ alloy.addToOverallFact(
        subsettingItemRuleForSources
            .call(ofFoodService_var.join(transferServeEat_ofFoodService))
            .forAll(ofFoodService_decl));
    /* 41 */ alloy.addToOverallFact(
        subsettingItemRuleForTargets
            .call(ofFoodService_var.join(transferServeEat_ofFoodService))
            .forAll(ofFoodService_decl));
    /* 42 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                sources.call(),
                ofFoodService_var.join(transferOrderServe_ofFoodService),
                ofFoodService_var.join(order_foodService))
            .forAll(ofFoodService_decl));
    /* 43 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                targets.call(),
                ofFoodService_var.join(transferOrderServe_ofFoodService),
                ofFoodService_var.join(serve_foodService))
            .forAll(ofFoodService_decl));
    /* 44 */ alloy.addToOverallFact(
        subsettingItemRuleForSources
            .call(ofFoodService_var.join(transferOrderServe_ofFoodService))
            .forAll(ofFoodService_decl));
    /* 45 */ alloy.addToOverallFact(
        subsettingItemRuleForTargets
            .call(ofFoodService_var.join(transferOrderServe_ofFoodService))
            .forAll(ofFoodService_decl));

    // OFSingleFoodService
    // Implicit Fact
    // 46) order in OFCustomOrder
    // 47) prepare in OFCustomPrepare
    // 48) serve in OFCustomServe
    // 49) transferOrderPrepare + transferOrderPay + transferPayEat in this.steps
    // 50) this.steps in order + prepare + pay + serve + eat + transferPrepareServe +
    // transferOrderServe + transferServeEat + transferOrderPrepare + transferOrderPay +
    // transferPayEat
    // 51) #order = 1
    // 52) order.outputs in order.orderedFoodItem_ofOrder + order.orderAmount_ofCustomOrder +
    // order.orderDestination_ofCustomOrder
    // 53) pay.inputs in pay.paidAmount_ofPay + pay.paidFoodItem_ofPay
    // 54) pay.outputs in pay.paidFoodItem_ofPay
    // 55) prepare.inputs in prepare.preparedFoodItem_ofPrepare +
    // prepare.prepareDestination_ofCustomPrepare
    // 56) prepare.outputs in prepare.preparedFoodItem_ofPrepare +
    // prepare.prepareDestination_ofCustomPrepare
    // 57) serve.inputs in serve.servedFoodItem_ofServe + serve.serviceDestination_ofCustomServe
    // 58) serve.outputs in serve.servedFoodItem_ofServe + serve.serviceDestination_ofCustomServe
    // 59) eat.inputs in eat.eatenItem_ofEat
    // 60) bijectionFiltered[sources, transferOrderPay, order]
    // 61) bijectionFiltered[targets, transferOrderPay, pay]
    // 62) subsettingItemRuleForSources[transferOrderPay]
    // 63) subsettingItemRuleForTargets[transferOrderPay]
    // 64) transferOrderPay.items in transferOrderPay.sources.orderedFoodItem_ofOrder +
    // transferOrderPay.sources.orderAmount_ofCustomOrder
    // 65) transferOrderPay.sources.orderedFoodItem_ofOrder +
    // transferOrderPay.sources.orderAmount_ofCustomOrder in transferOrderPay.items
    // 66) transferOrderPay.items in transferOrderPay.targets.paidFoodItem_ofPay +
    // transferOrderPay.targets.paidAmount_ofPay
    // 67) transferOrderPay.targets.paidFoodItem_ofPay + transferOrderPay.targets.paidAmount_ofPay
    // in transferOrderPay.items
    // 68) bijectionFiltered[sources, transferPayEat, pay]
    // 69) bijectionFiltered[targets, transferPayEat, eat]
    // 70) subsettingItemRuleForSources[transferPayEat]
    // 71) subsettingItemRuleForTargets[transferPayEat]
    // 72) transferPayEat.items in transferPayEat.sources.paidFoodItem_ofPay
    // 73) transferPayEat.items in transferPayEat.targets.eatenItem_ofEat
    // 74) transferPayEat.sources.paidFoodItem_ofPay in transferPayEat.items
    // 75) transferPayEat.targets.eatenItem_ofEat in transferPayEat.items
    // 76) transferOrderServe.items in transferOrderServe.sources.orderedFoodItem_ofOrder
    // 77) transferOrderServe.items in transferOrderServe.targets.servedFoodItem_ofServe
    // 78) transferOrderServe.sources.orderedFoodItem_ofOrder in transferOrderServe.items
    // 79) transferOrderServe.targets.servedFoodItem_ofServe in transferOrderServe.items
    // 80) bijectionFiltered[sources, transferOrderPrepare, order]
    // 81) bijectionFiltered[targets, transferOrderPrepare, prepare]
    // 82) subsettingItemRuleForSources[transferOrderPrepare]
    // 83) subsettingItemRuleForTargets[transferOrderPrepare]
    // 84) transferOrderPrepare.items in transferOrderPrepare.sources.orderedFoodItem_ofOrder +
    // transferOrderPrepare.sources.orderDestination_ofCustomOrder
    // 85) transferOrderPrepare.items in transferOrderPrepare.targets.preparedFoodItem_ofPrepare +
    // transferOrderPrepare.targets.prepareDestination_ofCustomPrepare
    // 86) transferOrderPrepare.sources.orderedFoodItem_ofOrder +
    // transferOrderPrepare.sources.orderDestination_ofCustomOrder in transferOrderPrepare.items
    // 87) transferOrderPrepare.targets.preparedFoodItem_ofPrepare +
    // transferOrderPrepare.targets.prepareDestination_ofCustomPrepare in transferOrderPrepare.items
    // 88) transferPrepareServe.items in transferPrepareServe.sources.preparedFoodItem_ofPrepare +
    // transferPrepareServe.sources.prepareDestination_ofCustomPrepare
    // 89) transferPrepareServe.sources.preparedFoodItem_ofPrepare +
    // transferPrepareServe.sources.prepareDestination_ofCustomPrepare in transferPrepareServe.items
    // 90) transferPrepareServe.items in transferPrepareServe.targets.servedFoodItem_ofServe +
    // transferPrepareServe.targets.serviceDestination_ofCustomServe
    // 91) transferPrepareServe.targets.servedFoodItem_ofServe +
    // transferPrepareServe.targets.serviceDestination_ofCustomServe in transferPrepareServe.items
    // 92) transferServeEat.items in transferServeEat.sources.servedFoodItem_ofServe
    // 93) transferServeEat.items in transferServeEat.targets.eatenItem_ofEat
    // 94) transferServeEat.sources.servedFoodItem_ofServe in transferServeEat.items
    // 95) transferServeEat.targets.eatenItem_ofEat in transferServeEat.items
    // Explicit Fact
    // 46) of_single_food_service.order in OFCustomOrder
    // 47) of_single_food_service.prepare in OFCustomPrepare
    // 48) of_single_food_service.serve in OFCustomServe
    // 49) of_single_food_service.transferOrderPrepare + of_single_food_service.transferOrderPay +
    // of_single_food_service.transferPayEat in of_single_food_service.steps
    // 50) of_single_food_service.steps in of_single_food_service.order +
    // of_single_food_service.prepare + of_single_food_service.pay + of_single_food_service.serve +
    // of_single_food_service.eat + of_single_food_service.transferPrepareServe +
    // of_single_food_service.transferOrderServe + of_single_food_service.transferServeEat +
    // of_single_food_service.transferOrderPrepare + of_single_food_service.transferOrderPay +
    // of_single_food_service.transferPayEat
    // 51) #of_single_food_service.order = 1
    // 52) (of_single_food_service.order).outputs in
    // (of_single_food_service.order).orderedFoodItem_ofOrder +
    // (of_single_food_service.order).orderAmount_ofCustomOrder +
    // (of_single_food_service.order).orderDestination_ofCustomOrder
    // 53) (of_single_food_service.pay).inputs + (of_single_food_service.pay).paidAmount_ofPay in
    // (of_single_food_service.pay).paidFoodItem_ofPay
    // 54) (of_single_food_service.pay).outputs in (of_single_food_service.pay).paidFoodItem_ofPay
    // 55) (of_single_food_service.prepare).inputs in
    // (of_single_food_service.prepare).preparedFoodItem_ofPrepare +
    // (of_single_food_service.prepare).prepareDestination_ofCustomPrepare
    // 56) (of_single_food_service.prepare).outputs in
    // (of_single_food_service.prepare).preparedFoodItem_ofPrepare +
    // (of_single_food_service.prepare).prepareDestination_ofCustomPrepare
    // 57) (of_single_food_service.serve).inputs in
    // (of_single_food_service.serve).servedFoodItem_ofServe +
    // (of_single_food_service.serve).serviceDestination_ofCustomServe
    // 58) (of_single_food_service.serve).outputs in
    // (of_single_food_service.serve).servedFoodItem_ofServe +
    // (of_single_food_service.serve).serviceDestination_ofCustomServe
    // 59) (of_single_food_service.eat).inputs in (of_single_food_service.eat).eatenItem_ofEat
    // 60) bijectionFiltered[sources, of_single_food_service.transferOrderPay,
    // of_single_food_service.order]
    // 61) bijectionFiltered[targets, of_single_food_service.transferOrderPay,
    // of_single_food_service.pay]
    // 62) subsettingItemRuleForSources[of_single_food_service.transferOrderPay]
    // 63) subsettingItemRuleForTargets[of_single_food_service.transferOrderPay]
    // 64) (of_single_food_service.transferOrderPay).items in
    // (of_single_food_service.transferOrderPay).sources.orderedFoodItem_ofOrder +
    // (of_single_food_service.transferOrderPay).sources.orderAmount_ofCustomOrder
    // 65) (of_single_food_service.transferOrderPay).sources.orderedFoodItem_ofOrder +
    // (of_single_food_service.transferOrderPay).sources.orderAmount_ofCustomOrder in
    // (of_single_food_service.transferOrderPay).items
    // 66) (of_single_food_service.transferOrderPay).items in
    // (of_single_food_service.transferOrderPay).targets.paidFoodItem_ofPay +
    // (of_single_food_service.transferOrderPay).targets.paidAmount_ofPay
    // 67) (of_single_food_service.transferOrderPay).targets.paidFoodItem_ofPay +
    // (of_single_food_service.transferOrderPay).targets.paidAmount_ofPay in
    // (of_single_food_service.transferOrderPay).items
    // 68) bijectionFiltered[sources, of_single_food_service.transferPayEat,
    // of_single_food_service.pay]
    // 69) bijectionFiltered[targets, of_single_food_service.transferPayEat,
    // of_single_food_service.eat]
    // 70) subsettingItemRuleForSources[of_single_food_service.transferPayEat]
    // 71) subsettingItemRuleForTargets[of_single_food_service.transferPayEat]
    // 72) (of_single_food_service.transferPayEat).items in
    // (of_single_food_service.transferPayEat).sources.paidFoodItem_ofPay
    // 73) (of_single_food_service.transferPayEat).items in
    // (of_single_food_service.transferPayEat).targets.eatenItem_ofEat
    // 74) (of_single_food_service.transferPayEat).sources.paidFoodItem_ofPay in
    // (of_single_food_service.transferPayEat).items
    // 75) (of_single_food_service.transferPayEat).targets.eatenItem_ofEat in
    // (of_single_food_service.transferPayEat).items
    // 76) (of_single_food_service.transferOrderServe).items in
    // (of_single_food_service.transferOrderServe).sources.orderedFoodItem_ofOrder
    // 77) (of_single_food_service.transferOrderServe).items in
    // (of_single_food_service.transferOrderServe).targets.servedFoodItem_ofServe
    // 78) (of_single_food_service.transferOrderServe).sources.orderedFoodItem_ofOrder in
    // (of_single_food_service.transferOrderServe).items
    // 79) (of_single_food_service.transferOrderServe).targets.servedFoodItem_ofServe in
    // (of_single_food_service.transferOrderServe).items
    // 80) bijectionFiltered[sources, of_single_food_service.transferOrderPrepare,
    // of_single_food_service.order]
    // 81) bijectionFiltered[targets, of_single_food_service.transferOrderPrepare,
    // of_single_food_service.prepare]
    // 82) subsettingItemRuleForSources[of_single_food_service.transferOrderPrepare]
    // 83) subsettingItemRuleForTargets[of_single_food_service.transferOrderPrepare]
    // 84) (of_single_food_service.transferOrderPrepare).items in
    // (of_single_food_service.transferOrderPrepare).sources.orderedFoodItem_ofOrder +
    // (of_single_food_service.transferOrderPrepare).sources.orderDestination_ofCustomOrder
    // 85) (of_single_food_service.transferOrderPrepare).items in
    // (of_single_food_service.transferOrderPrepare).targets.preparedFoodItem_ofPrepare +
    // (of_single_food_service.transferOrderPrepare).targets.prepareDestination_ofCustomPrepare
    // 86) (of_single_food_service.transferOrderPrepare).sources.orderedFoodItem_ofOrder +
    // (of_single_food_service.transferOrderPrepare).sources.orderDestination_ofCustomOrder in
    // (of_single_food_service.transferOrderPrepare).items
    // 87) (of_single_food_service.transferOrderPrepare).targets.preparedFoodItem_ofPrepare +
    // (of_single_food_service.transferOrderPrepare).targets.prepareDestination_ofCustomPrepare in
    // (of_single_food_service.transferOrderPrepare).items
    // 88) (of_single_food_service.transferPrepareServe).items in
    // (of_single_food_service.transferPrepareServe).sources.preparedFoodItem_ofPrepare +
    // (of_single_food_service.transferPrepareServe).sources.prepareDestination_ofCustomPrepare
    // 89) (of_single_food_service.transferPrepareServe).sources.preparedFoodItem_ofPrepare +
    // (of_single_food_service.transferPrepareServe).sources.prepareDestination_ofCustomPrepare in
    // (of_single_food_service.transferPrepareServe).items
    // 90) (of_single_food_service.transferPrepareServe).items in
    // (of_single_food_service.transferPrepareServe).targets.servedFoodItem_ofServe +
    // (of_single_food_service.transferPrepareServe).targets.serviceDestination_ofCustomServe
    // 91) (of_single_food_service.transferPrepareServe).targets.servedFoodItem_ofServe +
    // (of_single_food_service.transferPrepareServe).targets.serviceDestination_ofCustomServe in
    // (of_single_food_service.transferPrepareServe).items
    // 92) (of_single_food_service.transferServeEat).items in
    // (of_single_food_service.transferServeEat).sources.servedFoodItem_ofServe
    // 93) (of_single_food_service.transferServeEat).items in
    // (of_single_food_service.transferServeEat).targets.eatenItem_ofEat
    // 94) (of_single_food_service.transferServeEat).sources.servedFoodItem_ofServe in
    // (of_single_food_service.transferServeEat).items
    // 95) (of_single_food_service.transferServeEat).targets.eatenItem_ofEat in
    // (of_single_food_service.transferServeEat).items

    ExprVar ofSingleFoodService_var =
        ExprVar.make(null, "of_single_food_service", ofSingleFoodService.type());
    Decl ofSingleFoodService_decl =
        new Decl(null, null, null, List.of(ofSingleFoodService_var), ofSingleFoodService.oneOf());

    /* 46 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(order_foodService)
            .in(ofCustomOrder)
            .forAll(ofSingleFoodService_decl));
    /* 47 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(prepare_foodService)
            .in(ofCustomPrepare)
            .forAll(ofSingleFoodService_decl));
    /* 48 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(serve_foodService)
            .in(ofCustomServe)
            .forAll(ofSingleFoodService_decl));
    /* 49 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderPrepare_ofSingleFoodService)
            .plus(ofSingleFoodService_var.join(transferOrderPay_ofSingleFoodService))
            .plus(ofSingleFoodService_var.join(transferPayEat_ofSingleFoodService))
            .in(ofSingleFoodService_var.join(steps.call()))
            .forAll(ofSingleFoodService_decl));
    /* 50 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(steps.call())
            .in(
                ofSingleFoodService_var
                    .join(order_foodService)
                    .plus(ofSingleFoodService_var.join(prepare_foodService))
                    .plus(ofSingleFoodService_var.join(pay_foodService))
                    .plus(ofSingleFoodService_var.join(serve_foodService))
                    .plus(ofSingleFoodService_var.join(eat_foodService))
                    .plus(ofSingleFoodService_var.join(transferPrepareServe_ofFoodService))
                    .plus(ofSingleFoodService_var.join(transferOrderServe_ofFoodService))
                    .plus(ofSingleFoodService_var.join(transferServeEat_ofFoodService))
                    .plus(ofSingleFoodService_var.join(transferOrderPrepare_ofSingleFoodService))
                    .plus(ofSingleFoodService_var.join(transferOrderPay_ofSingleFoodService))
                    .plus(ofSingleFoodService_var.join(transferPayEat_ofSingleFoodService)))
            .forAll(ofSingleFoodService_decl));
    /* 51 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(order_foodService)
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .forAll(ofSingleFoodService_decl));
    /* 52 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(order_foodService)
            .join(outputs.call())
            .in(
                ofSingleFoodService_var
                    .join(order_foodService)
                    .join(orderedFoodItem_ofOrder)
                    .plus(
                        ofSingleFoodService_var
                            .join(order_foodService)
                            .join(orderAmount_ofCustomOrder))
                    .plus(
                        ofSingleFoodService_var
                            .join(order_foodService)
                            .join(orderDestination_ofCustomOrder)))
            .forAll(ofSingleFoodService_decl));
    /* 53 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(pay_foodService)
            .join(inputs.call())
            .plus(ofSingleFoodService_var.join(pay_foodService).join(paidAmount_ofPay))
            .in(ofSingleFoodService_var.join(pay_foodService).join(paidFoodItem_ofPay))
            .forAll(ofSingleFoodService_decl));
    /* 54 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(pay_foodService)
            .join(outputs.call())
            .in(ofSingleFoodService_var.join(pay_foodService).join(paidFoodItem_ofPay))
            .forAll(ofSingleFoodService_decl));
    /* 55 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(prepare_foodService)
            .join(inputs.call())
            .in(
                ofSingleFoodService_var
                    .join(prepare_foodService)
                    .join(preparedFoodItem_ofPrepare)
                    .plus(
                        ofSingleFoodService_var
                            .join(prepare_foodService)
                            .join(prepareDestination_ofCustomPrepare)))
            .forAll(ofSingleFoodService_decl));
    /* 56 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(prepare_foodService)
            .join(outputs.call())
            .in(
                ofSingleFoodService_var
                    .join(prepare_foodService)
                    .join(preparedFoodItem_ofPrepare)
                    .plus(
                        ofSingleFoodService_var
                            .join(prepare_foodService)
                            .join(prepareDestination_ofCustomPrepare)))
            .forAll(ofSingleFoodService_decl));
    /* 57 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(serve_foodService)
            .join(inputs.call())
            .in(
                ofSingleFoodService_var
                    .join(serve_foodService)
                    .join(servedFoodItem_ofServe)
                    .plus(
                        ofSingleFoodService_var
                            .join(serve_foodService)
                            .join(serviceDestination_ofCustomServe)))
            .forAll(ofSingleFoodService_decl));
    /* 58 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(serve_foodService)
            .join(outputs.call())
            .in(
                ofSingleFoodService_var
                    .join(serve_foodService)
                    .join(servedFoodItem_ofServe)
                    .plus(
                        ofSingleFoodService_var
                            .join(serve_foodService)
                            .join(serviceDestination_ofCustomServe)))
            .forAll(ofSingleFoodService_decl));
    /* 59 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(eat_foodService)
            .join(inputs.call())
            .in(ofSingleFoodService_var.join(eat_foodService).join(eatenItem_ofEat))
            .forAll(ofSingleFoodService_decl));
    /* 60 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                sources.call(),
                ofSingleFoodService_var.join(transferOrderPay_ofSingleFoodService),
                ofSingleFoodService_var.join(order_foodService))
            .forAll(ofSingleFoodService_decl));
    /* 61 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                targets.call(),
                ofSingleFoodService_var.join(transferOrderPay_ofSingleFoodService),
                ofSingleFoodService_var.join(pay_foodService))
            .forAll(ofSingleFoodService_decl));
    /* 62 */ alloy.addToOverallFact(
        subsettingItemRuleForSources
            .call(ofSingleFoodService_var.join(transferOrderPay_ofSingleFoodService))
            .forAll(ofSingleFoodService_decl));
    /* 63 */ alloy.addToOverallFact(
        subsettingItemRuleForTargets
            .call(ofSingleFoodService_var.join(transferOrderPay_ofSingleFoodService))
            .forAll(ofSingleFoodService_decl));
    /* 64 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderPay_ofSingleFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferOrderPay_ofSingleFoodService)
                    .join(sources.call())
                    .join(orderedFoodItem_ofOrder)
                    .plus(
                        ofSingleFoodService_var
                            .join(transferOrderPay_ofSingleFoodService)
                            .join(sources.call())
                            .join(orderAmount_ofCustomOrder)))
            .forAll(ofSingleFoodService_decl));
    /* 65 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderPay_ofSingleFoodService)
            .join(sources.call())
            .join(orderedFoodItem_ofOrder)
            .plus(
                ofSingleFoodService_var
                    .join(transferOrderPay_ofSingleFoodService)
                    .join(sources.call())
                    .join(orderAmount_ofCustomOrder))
            .in(
                ofSingleFoodService_var
                    .join(transferOrderPay_ofSingleFoodService)
                    .join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 66 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderPay_ofSingleFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferOrderPay_ofSingleFoodService)
                    .join(targets.call())
                    .join(paidFoodItem_ofPay)
                    .plus(
                        ofSingleFoodService_var
                            .join(transferOrderPay_ofSingleFoodService)
                            .join(targets.call())
                            .join(paidAmount_ofPay)))
            .forAll(ofSingleFoodService_decl));
    /* 67 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderPay_ofSingleFoodService)
            .join(targets.call())
            .join(paidFoodItem_ofPay)
            .plus(
                ofSingleFoodService_var
                    .join(transferOrderPay_ofSingleFoodService)
                    .join(targets.call())
                    .join(paidAmount_ofPay))
            .in(
                ofSingleFoodService_var
                    .join(transferOrderPay_ofSingleFoodService)
                    .join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 68 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                sources.call(),
                ofSingleFoodService_var.join(transferPayEat_ofSingleFoodService),
                ofSingleFoodService_var.join(pay_foodService))
            .forAll(ofSingleFoodService_decl));
    /* 69 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                targets.call(),
                ofSingleFoodService_var.join(transferPayEat_ofSingleFoodService),
                ofSingleFoodService_var.join(eat_foodService))
            .forAll(ofSingleFoodService_decl));
    /* 70 */ alloy.addToOverallFact(
        subsettingItemRuleForSources
            .call(ofSingleFoodService_var.join(transferPayEat_ofSingleFoodService))
            .forAll(ofSingleFoodService_decl));
    /* 71 */ alloy.addToOverallFact(
        subsettingItemRuleForTargets
            .call(ofSingleFoodService_var.join(transferPayEat_ofSingleFoodService))
            .forAll(ofSingleFoodService_decl));
    /* 72 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferPayEat_ofSingleFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferPayEat_ofSingleFoodService)
                    .join(sources.call())
                    .join(paidFoodItem_ofPay))
            .forAll(ofSingleFoodService_decl));
    /* 73 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferPayEat_ofSingleFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferPayEat_ofSingleFoodService)
                    .join(targets.call())
                    .join(eatenItem_ofEat))
            .forAll(ofSingleFoodService_decl));
    /* 74 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferPayEat_ofSingleFoodService)
            .join(sources.call())
            .join(paidFoodItem_ofPay)
            .in(ofSingleFoodService_var.join(transferPayEat_ofSingleFoodService).join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 75 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferPayEat_ofSingleFoodService)
            .join(targets.call())
            .join(eatenItem_ofEat)
            .in(ofSingleFoodService_var.join(transferPayEat_ofSingleFoodService).join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 76 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderServe_ofFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferOrderServe_ofFoodService)
                    .join(sources.call())
                    .join(orderedFoodItem_ofOrder))
            .forAll(ofSingleFoodService_decl));
    /* 77 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderServe_ofFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferOrderServe_ofFoodService)
                    .join(targets.call())
                    .join(servedFoodItem_ofServe))
            .forAll(ofSingleFoodService_decl));
    /* 78 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderServe_ofFoodService)
            .join(sources.call())
            .join(orderedFoodItem_ofOrder)
            .in(ofSingleFoodService_var.join(transferOrderServe_ofFoodService).join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 79 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderServe_ofFoodService)
            .join(targets.call())
            .join(servedFoodItem_ofServe)
            .in(ofSingleFoodService_var.join(transferOrderServe_ofFoodService).join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 80 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                sources.call(),
                ofSingleFoodService_var.join(transferOrderPrepare_ofSingleFoodService),
                ofSingleFoodService_var.join(order_foodService))
            .forAll(ofSingleFoodService_decl));
    /* 81 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                targets.call(),
                ofSingleFoodService_var.join(transferOrderPrepare_ofSingleFoodService),
                ofSingleFoodService_var.join(prepare_foodService))
            .forAll(ofSingleFoodService_decl));
    /* 82 */ alloy.addToOverallFact(
        subsettingItemRuleForSources
            .call(ofSingleFoodService_var.join(transferOrderPrepare_ofSingleFoodService))
            .forAll(ofSingleFoodService_decl));
    /* 83 */ alloy.addToOverallFact(
        subsettingItemRuleForTargets
            .call(ofSingleFoodService_var.join(transferOrderPrepare_ofSingleFoodService))
            .forAll(ofSingleFoodService_decl));
    /* 84 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderPrepare_ofSingleFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferOrderPrepare_ofSingleFoodService)
                    .join(sources.call())
                    .join(orderedFoodItem_ofOrder)
                    .plus(
                        ofSingleFoodService_var
                            .join(transferOrderPrepare_ofSingleFoodService)
                            .join(sources.call())
                            .join(orderDestination_ofCustomOrder)))
            .forAll(ofSingleFoodService_decl));
    /* 85 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderPrepare_ofSingleFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferOrderPrepare_ofSingleFoodService)
                    .join(targets.call())
                    .join(preparedFoodItem_ofPrepare)
                    .plus(
                        ofSingleFoodService_var
                            .join(transferOrderPrepare_ofSingleFoodService)
                            .join(targets.call())
                            .join(prepareDestination_ofCustomPrepare)))
            .forAll(ofSingleFoodService_decl));
    /* 86 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderPrepare_ofSingleFoodService)
            .join(sources.call())
            .join(orderedFoodItem_ofOrder)
            .plus(
                ofSingleFoodService_var
                    .join(transferOrderPrepare_ofSingleFoodService)
                    .join(sources.call())
                    .join(orderDestination_ofCustomOrder))
            .in(
                ofSingleFoodService_var
                    .join(transferOrderPrepare_ofSingleFoodService)
                    .join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 87 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferOrderPrepare_ofSingleFoodService)
            .join(targets.call())
            .join(preparedFoodItem_ofPrepare)
            .plus(
                ofSingleFoodService_var
                    .join(transferOrderPrepare_ofSingleFoodService)
                    .join(targets.call())
                    .join(prepareDestination_ofCustomPrepare))
            .in(
                ofSingleFoodService_var
                    .join(transferOrderPrepare_ofSingleFoodService)
                    .join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 88 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferPrepareServe_ofFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferPrepareServe_ofFoodService)
                    .join(sources.call())
                    .join(preparedFoodItem_ofPrepare)
                    .plus(
                        ofSingleFoodService_var
                            .join(transferPrepareServe_ofFoodService)
                            .join(sources.call())
                            .join(prepareDestination_ofCustomPrepare)))
            .forAll(ofSingleFoodService_decl));
    /* 89 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferPrepareServe_ofFoodService)
            .join(sources.call())
            .join(preparedFoodItem_ofPrepare)
            .plus(
                ofSingleFoodService_var
                    .join(transferPrepareServe_ofFoodService)
                    .join(sources.call())
                    .join(prepareDestination_ofCustomPrepare))
            .in(ofSingleFoodService_var.join(transferPrepareServe_ofFoodService).join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 90 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferPrepareServe_ofFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferPrepareServe_ofFoodService)
                    .join(targets.call())
                    .join(servedFoodItem_ofServe)
                    .plus(
                        ofSingleFoodService_var
                            .join(transferPrepareServe_ofFoodService)
                            .join(targets.call())
                            .join(serviceDestination_ofCustomServe)))
            .forAll(ofSingleFoodService_decl));
    /* 91 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferPrepareServe_ofFoodService)
            .join(targets.call())
            .join(servedFoodItem_ofServe)
            .plus(
                ofSingleFoodService_var
                    .join(transferPrepareServe_ofFoodService)
                    .join(targets.call())
                    .join(serviceDestination_ofCustomServe))
            .in(ofSingleFoodService_var.join(transferPrepareServe_ofFoodService).join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 92 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferServeEat_ofFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferServeEat_ofFoodService)
                    .join(sources.call())
                    .join(servedFoodItem_ofServe))
            .forAll(ofSingleFoodService_decl));
    /* 93 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferServeEat_ofFoodService)
            .join(items.call())
            .in(
                ofSingleFoodService_var
                    .join(transferServeEat_ofFoodService)
                    .join(targets.call())
                    .join(eatenItem_ofEat))
            .forAll(ofSingleFoodService_decl));
    /* 94 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferServeEat_ofFoodService)
            .join(sources.call())
            .join(servedFoodItem_ofServe)
            .in(ofSingleFoodService_var.join(transferServeEat_ofFoodService).join(items.call()))
            .forAll(ofSingleFoodService_decl));
    /* 95 */ alloy.addToOverallFact(
        ofSingleFoodService_var
            .join(transferServeEat_ofFoodService)
            .join(targets.call())
            .join(eatenItem_ofEat)
            .in(ofSingleFoodService_var.join(transferServeEat_ofFoodService).join(items.call()))
            .forAll(ofSingleFoodService_decl));

    // OFLoopFoodService
    // Implicit Fact
    // 96) order in OFCustomOrder
    // 97) prepare in OFCustomPrepare
    // 98) serve in OFCustomServe
    // 99) start + end + transferOrderPrepare + transferOrderPay + transferPayEat in this.steps
    // 100) this.steps in order + prepare + pay + serve + eat + start + end + transferPrepareServe +
    // transferOrderServe + transferServeEat + transferOrderPrepare + transferOrderPay +
    // transferPayEat
    // 101) #start = 1
    // 102) functionFiltered[happensBefore, start, order]
    // 103) #order = 2
    // 104) order.outputs in order.orderedFoodItem + order.orderAmount + order.orderDestination
    // 105) bijectionFiltered[outputs, order, FoodItem]
    // 106) bijectionFiltered[outputs, order, Real]
    // 107) bijectionFiltered[outputs, order, Location]
    // 108) inverseFunctionFiltered[happensBefore, start + eat, order]
    // 109) pay.inputs in pay.paidAmount + pay.paidFoodItem
    // 110) bijectionFiltered[inputs, pay, Real]
    // 111) bijectionFiltered[inputs, pay, FoodItem]
    // 112) pay.outputs in pay.paidFoodItem
    // 113) functionFiltered[outputs, pay, FoodItem]
    // 114) prepare.inputs in prepare.preparedFoodItem + prepare.prepareDestination
    // 115) bijectionFiltered[inputs, prepare, FoodItem]
    // 116) bijectionFiltered[inputs, prepare, Location]
    // 117) prepare.outputs in prepare.preparedFoodItem + prepare.prepareDestination
    // 118) bijectionFiltered[outputs, prepare, FoodItem]
    // 119) bijectionFiltered[outputs, prepare, Location]
    // 120) serve.inputs in serve.servedFoodItem + serve.serviceDestination
    // 121) bijectionFiltered[inputs, serve, FoodItem]
    // 122) bijectionFiltered[inputs, serve, Location]
    // 123) serve.outputs in serve.servedFoodItem + serve.serviceDestination
    // 124) bijectionFiltered[outputs, serve, Location]
    // 125) bijectionFiltered[outputs, serve, FoodItem]
    // 126) eat.inputs in eat.eatenItem
    // 127) bijectionFiltered[inputs, eat, FoodItem]
    // 128) functionFiltered[happensBefore, eat, end + order]
    // 129) #end = 1
    // 130) bijectionFiltered[sources, transferOrderPay, order]
    // 131) bijectionFiltered[targets, transferOrderPay, pay]
    // 132) subsettingItemRuleForSources[transferOrderPay]
    // 133) subsettingItemRuleForTargets[transferOrderPay]
    // 134) transferOrderPay.items in transferOrderPay.sources.orderedFoodItem +
    // transferOrderPay.sources.orderAmount
    // 135) transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount in
    // transferOrderPay.items
    // 136) transferOrderPay.items in transferOrderPay.targets.paidFoodItem +
    // transferOrderPay.targets.paidAmount
    // 137) transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount in
    // transferOrderPay.items
    // 138) bijectionFiltered[sources, transferPayEat, pay]
    // 139) bijectionFiltered[targets, transferPayEat, eat]
    // 140) subsettingItemRuleForSources[transferPayEat]
    // 141) subsettingItemRuleForTargets[transferPayEat]
    // 142) transferPayEat.items in transferPayEat.sources.paidFoodItem
    // 143) transferPayEat.items in transferPayEat.targets.eatenItem
    // 144) transferPayEat.sources.paidFoodItem in transferPayEat.items
    // 145) transferPayEat.targets.eatenItem in transferPayEat.items
    // 146) transferOrderServe.items in transferOrderServe.sources.orderedFoodItem
    // 147) transferOrderServe.items in transferOrderServe.targets.servedFoodItem
    // 148) transferOrderServe.sources.orderedFoodItem in transferOrderServe.items
    // 149) transferOrderServe.targets.servedFoodItem in transferOrderServe.items
    // 150) bijectionFiltered[sources, transferOrderPrepare, order]
    // 151) bijectionFiltered[targets, transferOrderPrepare, prepare]
    // 152) subsettingItemRuleForSources[transferOrderPrepare]
    // 153) subsettingItemRuleForTargets[transferOrderPrepare]
    // 154) transferOrderPrepare.items in transferOrderPrepare.sources.orderedFoodItem +
    // transferOrderPrepare.sources.orderDestination
    // 155) transferOrderPrepare.items in transferOrderPrepare.targets.preparedFoodItem +
    // transferOrderPrepare.targets.prepareDestination
    // 156) transferOrderPrepare.sources.orderedFoodItem +
    // transferOrderPrepare.sources.orderDestination in transferOrderPrepare.items
    // 157) transferOrderPrepare.targets.preparedFoodItem +
    // transferOrderPrepare.targets.prepareDestination in transferOrderPrepare.items
    // 158) transferPrepareServe.items in transferPrepareServe.sources.preparedFoodItem +
    // transferPrepareServe.sources.prepareDestination
    // 159) transferPrepareServe.sources.preparedFoodItem +
    // transferPrepareServe.sources.prepareDestination in transferPrepareServe.items
    // 160) transferPrepareServe.items in transferPrepareServe.targets.servedFoodItem +
    // transferPrepareServe.targets.serviceDestination
    // 161) transferPrepareServe.targets.servedFoodItem +
    // transferPrepareServe.targets.serviceDestination in transferPrepareServe.items
    // 162) transferServeEat.items in transferServeEat.sources.servedFoodItem
    // 163) transferServeEat.items in transferServeEat.targets.eatenItem
    // 164) transferServeEat.sources.servedFoodItem in transferServeEat.items
    // 165) transferServeEat.targets.eatenItem in transferServeEat.items

    // Explicit Fact
    // 96) (of_loop_food_service.order) in OFCustomOrder
    // 97) (of_loop_food_service.prepare) in OFCustomPrepare
    // 98) (of_loop_food_service.serve) in OFCustomServe
    // 99) (of_loop_food_service.start) + (of_loop_food_service.end) +
    // (of_loop_food_service.transferOrderPrepare) + (of_loop_food_service.transferOrderPay) +
    // (of_loop_food_service.transferPayEat) in (of_loop_food_service.steps)
    // 100) (of_loop_food_service.steps) in (of_loop_food_service.order) +
    // (of_loop_food_service.prepare) + (of_loop_food_service.pay) + (of_loop_food_service.serve) +
    // (of_loop_food_service.eat) + (of_loop_food_service.start) + (of_loop_food_service.end) +
    // (of_loop_food_service.transferPrepareServe) + (of_loop_food_service.transferOrderServe) +
    // (of_loop_food_service.transferServeEat) + (of_loop_food_service.transferOrderPrepare) +
    // (of_loop_food_service.transferOrderPay) + (of_loop_food_service.transferPayEat)
    // 101) #(of_loop_food_service.start) = 1
    // 102) functionFiltered[happensBefore, (of_loop_food_service.start),
    // (of_loop_food_service.order)]
    // 103) #(of_loop_food_service.order) = 2
    // 104) (of_loop_food_service.order).outputs in (of_loop_food_service.order).orderedFoodItem +
    // (of_loop_food_service.order).orderAmount + (of_loop_food_service.order).orderDestination
    // 105) bijectionFiltered[outputs, (of_loop_food_service.order), FoodItem]
    // 106) bijectionFiltered[outputs, (of_loop_food_service.order), Real]
    // 107) bijectionFiltered[outputs, (of_loop_food_service.order), Location]
    // 108) inverseFunctionFiltered[happensBefore, (of_loop_food_service.start) +
    // (of_loop_food_service.eat), (of_loop_food_service.order)]
    // 109) (of_loop_food_service.pay).inputs in (of_loop_food_service.pay).paidAmount +
    // (of_loop_food_service.pay).paidFoodItem
    // 110) bijectionFiltered[inputs, (of_loop_food_service.pay), Real]
    // 111) bijectionFiltered[inputs, (of_loop_food_service.pay), FoodItem]
    // 112) (of_loop_food_service.pay).outputs in (of_loop_food_service.pay).paidFoodItem
    // 113) functionFiltered[outputs, (of_loop_food_service.pay), FoodItem]
    // 114) (of_loop_food_service.prepare).inputs in (of_loop_food_service.prepare).preparedFoodItem
    // + (of_loop_food_service.prepare).prepareDestination
    // 115) bijectionFiltered[inputs, (of_loop_food_service.prepare), FoodItem]
    // 116) bijectionFiltered[inputs, (of_loop_food_service.prepare), Location]
    // 117) (of_loop_food_service.prepare).outputs in
    // (of_loop_food_service.prepare).preparedFoodItem +
    // (of_loop_food_service.prepare).prepareDestination
    // 118) bijectionFiltered[outputs, (of_loop_food_service.prepare), FoodItem]
    // 119) bijectionFiltered[outputs, (of_loop_food_service.prepare), Location]
    // 120) (of_loop_food_service.serve).inputs in (of_loop_food_service.serve).servedFoodItem +
    // (of_loop_food_service.serve).serviceDestination
    // 121) bijectionFiltered[inputs, (of_loop_food_service.serve), FoodItem]
    // 122) bijectionFiltered[inputs, (of_loop_food_service.serve), Location]
    // 123) (of_loop_food_service.serve).outputs in (of_loop_food_service.serve).servedFoodItem +
    // (of_loop_food_service.serve).serviceDestination
    // 124) bijectionFiltered[outputs, (of_loop_food_service.serve), Location]
    // 125) bijectionFiltered[outputs, (of_loop_food_service.serve), FoodItem]
    // 126) (of_loop_food_service.eat).inputs in (of_loop_food_service.eat).eatenItem
    // 127) bijectionFiltered[inputs, (of_loop_food_service.eat), FoodItem]
    // 128) functionFiltered[happensBefore, (of_loop_food_service.eat), (of_loop_food_service.end) +
    // (of_loop_food_service.order)]
    // 129) #(of_loop_food_service.end) = 1
    // 130) bijectionFiltered[sources, (of_loop_food_service.transferOrderPay),
    // (of_loop_food_service.order)]
    // 131) bijectionFiltered[targets, (of_loop_food_service.transferOrderPay),
    // (of_loop_food_service.pay)]
    // 132) subsettingItemRuleForSources[(of_loop_food_service.transferOrderPay)]
    // 133) subsettingItemRuleForTargets[(of_loop_food_service.transferOrderPay)]
    // 134) (of_loop_food_service.transferOrderPay).items in
    // (of_loop_food_service.transferOrderPay).sources.orderedFoodItem +
    // (of_loop_food_service.transferOrderPay).sources.orderAmount
    // 135) (of_loop_food_service.transferOrderPay).sources.orderedFoodItem +
    // (of_loop_food_service.transferOrderPay).sources.orderAmount in
    // (of_loop_food_service.transferOrderPay).items
    // 136) (of_loop_food_service.transferOrderPay).items in
    // (of_loop_food_service.transferOrderPay).targets.paidFoodItem +
    // (of_loop_food_service.transferOrderPay).targets.paidAmount
    // 137) (of_loop_food_service.transferOrderPay).targets.paidFoodItem +
    // (of_loop_food_service.transferOrderPay).targets.paidAmount in
    // (of_loop_food_service.transferOrderPay).items
    // 138) bijectionFiltered[sources, (of_loop_food_service.transferPayEat),
    // (of_loop_food_service.pay)]
    // 139) bijectionFiltered[targets, (of_loop_food_service.transferPayEat),
    // (of_loop_food_service.eat)]
    // 140) subsettingItemRuleForSources[(of_loop_food_service.transferPayEat)]
    // 141) subsettingItemRuleForTargets[(of_loop_food_service.transferPayEat)]
    // 142) (of_loop_food_service.transferPayEat).items in
    // (of_loop_food_service.transferPayEat).sources.paidFoodItem
    // 143) (of_loop_food_service.transferPayEat).items in
    // (of_loop_food_service.transferPayEat).targets.eatenItem
    // 144) (of_loop_food_service.transferPayEat).sources.paidFoodItem in
    // (of_loop_food_service.transferPayEat).items
    // 145) (of_loop_food_service.transferPayEat).targets.eatenItem in
    // (of_loop_food_service.transferPayEat).items
    // 146) (of_loop_food_service.transferOrderServe).items in
    // (of_loop_food_service.transferOrderServe).sources.orderedFoodItem
    // 147) (of_loop_food_service.transferOrderServe).items in
    // (of_loop_food_service.transferOrderServe).targets.servedFoodItem
    // 148) (of_loop_food_service.transferOrderServe).sources.orderedFoodItem in
    // (of_loop_food_service.transferOrderServe).items
    // 149) (of_loop_food_service.transferOrderServe).targets.servedFoodItem in
    // (of_loop_food_service.transferOrderServe).items
    // 150) bijectionFiltered[sources, (of_loop_food_service.transferOrderPrepare),
    // (of_loop_food_service.order)]
    // 151) bijectionFiltered[targets, (of_loop_food_service.transferOrderPrepare),
    // (of_loop_food_service.prepare)]
    // 152) subsettingItemRuleForSources[(of_loop_food_service.transferOrderPrepare)]
    // 153) subsettingItemRuleForTargets[(of_loop_food_service.transferOrderPrepare)]
    // 154) (of_loop_food_service.transferOrderPrepare).items in
    // (of_loop_food_service.transferOrderPrepare).sources.orderedFoodItem +
    // (of_loop_food_service.transferOrderPrepare).sources.orderDestination
    // 155) (of_loop_food_service.transferOrderPrepare).items in
    // (of_loop_food_service.transferOrderPrepare).targets.preparedFoodItem +
    // (of_loop_food_service.transferOrderPrepare).targets.prepareDestination
    // 156) (of_loop_food_service.transferOrderPrepare).sources.orderedFoodItem +
    // (of_loop_food_service.transferOrderPrepare).sources.orderDestination in
    // (of_loop_food_service.transferOrderPrepare).items
    // 157) (of_loop_food_service.transferOrderPrepare).targets.preparedFoodItem +
    // (of_loop_food_service.transferOrderPrepare).targets.prepareDestination in
    // (of_loop_food_service.transferOrderPrepare).items
    // 158) (of_loop_food_service.transferPrepareServe).items in
    // (of_loop_food_service.transferPrepareServe).sources.preparedFoodItem +
    // (of_loop_food_service.transferPrepareServe).sources.prepareDestination
    // 159) (of_loop_food_service.transferPrepareServe).sources.preparedFoodItem +
    // (of_loop_food_service.transferPrepareServe).sources.prepareDestination in
    // (of_loop_food_service.transferPrepareServe).items
    // 160) (of_loop_food_service.transferPrepareServe).items in
    // (of_loop_food_service.transferPrepareServe).targets.servedFoodItem +
    // (of_loop_food_service.transferPrepareServe).targets.serviceDestination
    // 161) (of_loop_food_service.transferPrepareServe).targets.servedFoodItem +
    // (of_loop_food_service.transferPrepareServe).targets.serviceDestination in
    // (of_loop_food_service.transferPrepareServe).items
    // 162) (of_loop_food_service.transferServeEat).items in
    // (of_loop_food_service.transferServeEat).sources.servedFoodItem
    // 163) (of_loop_food_service.transferServeEat).items in
    // (of_loop_food_service.transferServeEat).targets.eatenItem
    // 164) (of_loop_food_service.transferServeEat).sources.servedFoodItem in
    // (of_loop_food_service.transferServeEat).items
    // 165) (of_loop_food_service.transferServeEat).targets.eatenItem in
    // (of_loop_food_service.transferServeEat).items

    ExprVar ofLoopFoodServiceVar =
        ExprVar.make(null, "of_loop_food_service", ofLoopFoodService.type());
    Decl ofLoopFoodServiceDecl =
        new Decl(null, null, null, List.of(ofLoopFoodServiceVar), ofLoopFoodService.oneOf());

    /* 96 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(order_foodService)
            .in(ofCustomOrder)
            .forAll(ofLoopFoodServiceDecl));
    /* 97 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(prepare_foodService)
            .in(ofCustomPrepare)
            .forAll(ofLoopFoodServiceDecl));
    /* 98 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(serve_foodService)
            .in(ofCustomServe)
            .forAll(ofLoopFoodServiceDecl));
    /* 99 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(start_ofLoopFoodService)
            .plus(ofLoopFoodServiceVar.join(end_ofLoopFoodService))
            .plus(ofLoopFoodServiceVar.join(transferOrderPrepare_ofLoopFoodService))
            .plus(ofLoopFoodServiceVar.join(transferOrderPay_ofLoopFoodService))
            .plus(ofLoopFoodServiceVar.join(transferPayEat_ofLoopFoodService))
            .in(ofLoopFoodServiceVar.join(steps.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 100 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(steps.call())
            .in(
                ofLoopFoodServiceVar
                    .join(order_foodService)
                    .plus(ofLoopFoodServiceVar.join(prepare_foodService))
                    .plus(ofLoopFoodServiceVar.join(pay_foodService))
                    .plus(ofLoopFoodServiceVar.join(serve_foodService))
                    .plus(ofLoopFoodServiceVar.join(eat_foodService))
                    .plus(ofLoopFoodServiceVar.join(start_ofLoopFoodService))
                    .plus(ofLoopFoodServiceVar.join(end_ofLoopFoodService))
                    .plus(ofLoopFoodServiceVar.join(transferPrepareServe_ofFoodService))
                    .plus(ofLoopFoodServiceVar.join(transferOrderServe_ofFoodService))
                    .plus(ofLoopFoodServiceVar.join(transferServeEat_ofFoodService))
                    .plus(ofLoopFoodServiceVar.join(transferOrderPrepare_ofLoopFoodService))
                    .plus(ofLoopFoodServiceVar.join(transferOrderPay_ofLoopFoodService))
                    .plus(ofLoopFoodServiceVar.join(transferPayEat_ofLoopFoodService)))
            .forAll(ofLoopFoodServiceDecl));
    /* 101 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(start_ofLoopFoodService)
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .forAll(ofLoopFoodServiceDecl));
    /* 102 */ alloy.addToOverallFact(
        functionFiltered
            .call(
                happensBefore.call(),
                ofLoopFoodServiceVar.join(start_ofLoopFoodService),
                ofLoopFoodServiceVar.join(order_foodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 103 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(order_foodService)
            .cardinality()
            .equal(ExprConstant.makeNUMBER(2))
            .forAll(ofLoopFoodServiceDecl));
    /* 104 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(order_foodService)
            .join(outputs.call())
            .in(
                ofLoopFoodServiceVar
                    .join(order_foodService)
                    .join(orderedFoodItem_ofOrder)
                    .plus(
                        ofLoopFoodServiceVar
                            .join(order_foodService)
                            .join(orderAmount_ofCustomOrder))
                    .plus(
                        ofLoopFoodServiceVar
                            .join(order_foodService)
                            .join(orderDestination_ofCustomOrder)))
            .forAll(ofLoopFoodServiceDecl));
    /* 105 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(outputs.call(), ofLoopFoodServiceVar.join(order_foodService), foodItem)
            .forAll(ofLoopFoodServiceDecl));
    /* 106 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(outputs.call(), ofLoopFoodServiceVar.join(order_foodService), real)
            .forAll(ofLoopFoodServiceDecl));
    /* 107 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(outputs.call(), ofLoopFoodServiceVar.join(order_foodService), location)
            .forAll(ofLoopFoodServiceDecl));
    /* 108 */ alloy.addToOverallFact(
        inverseFunctionFiltered
            .call(
                happensBefore.call(),
                ofLoopFoodServiceVar
                    .join(start_ofLoopFoodService)
                    .plus(ofLoopFoodServiceVar.join(eat_foodService)),
                ofLoopFoodServiceVar.join(order_foodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 109 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(pay_foodService)
            .join(inputs.call())
            .in(
                ofLoopFoodServiceVar
                    .join(pay_foodService)
                    .join(paidAmount_ofPay)
                    .plus(ofLoopFoodServiceVar.join(pay_foodService).join(paidFoodItem_ofPay)))
            .forAll(ofLoopFoodServiceDecl));
    /* 110 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(inputs.call(), ofLoopFoodServiceVar.join(pay_foodService), real)
            .forAll(ofLoopFoodServiceDecl));
    /* 111 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(inputs.call(), ofLoopFoodServiceVar.join(pay_foodService), foodItem)
            .forAll(ofLoopFoodServiceDecl));
    /* 112 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(pay_foodService)
            .join(outputs.call())
            .in(ofLoopFoodServiceVar.join(pay_foodService).join(paidFoodItem_ofPay))
            .forAll(ofLoopFoodServiceDecl));
    /* 113 */ alloy.addToOverallFact(
        functionFiltered
            .call(outputs.call(), ofLoopFoodServiceVar.join(pay_foodService), foodItem)
            .forAll(ofLoopFoodServiceDecl));
    /* 114 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(prepare_foodService)
            .join(inputs.call())
            .in(
                ofLoopFoodServiceVar
                    .join(prepare_foodService)
                    .join(preparedFoodItem_ofPrepare)
                    .plus(
                        ofLoopFoodServiceVar
                            .join(prepare_foodService)
                            .join(prepareDestination_ofCustomPrepare)))
            .forAll(ofLoopFoodServiceDecl));
    /* 115 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(inputs.call(), ofLoopFoodServiceVar.join(prepare_foodService), foodItem)
            .forAll(ofLoopFoodServiceDecl));
    /* 116 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(inputs.call(), ofLoopFoodServiceVar.join(prepare_foodService), location)
            .forAll(ofLoopFoodServiceDecl));
    /* 117 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(prepare_foodService)
            .join(outputs.call())
            .in(
                ofLoopFoodServiceVar
                    .join(prepare_foodService)
                    .join(preparedFoodItem_ofPrepare)
                    .plus(
                        ofLoopFoodServiceVar
                            .join(prepare_foodService)
                            .join(prepareDestination_ofCustomPrepare)))
            .forAll(ofLoopFoodServiceDecl));
    /* 118 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(outputs.call(), ofLoopFoodServiceVar.join(prepare_foodService), foodItem)
            .forAll(ofLoopFoodServiceDecl));
    /* 119 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(outputs.call(), ofLoopFoodServiceVar.join(prepare_foodService), location)
            .forAll(ofLoopFoodServiceDecl));
    /* 120 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(serve_foodService)
            .join(inputs.call())
            .in(
                ofLoopFoodServiceVar
                    .join(serve_foodService)
                    .join(servedFoodItem_ofServe)
                    .plus(
                        ofLoopFoodServiceVar
                            .join(serve_foodService)
                            .join(serviceDestination_ofCustomServe)))
            .forAll(ofLoopFoodServiceDecl));
    /* 121 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(inputs.call(), ofLoopFoodServiceVar.join(serve_foodService), foodItem)
            .forAll(ofLoopFoodServiceDecl));
    /* 122 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(inputs.call(), ofLoopFoodServiceVar.join(serve_foodService), location)
            .forAll(ofLoopFoodServiceDecl));
    /* 123 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(serve_foodService)
            .join(outputs.call())
            .in(
                ofLoopFoodServiceVar
                    .join(serve_foodService)
                    .join(servedFoodItem_ofServe)
                    .plus(
                        ofLoopFoodServiceVar
                            .join(serve_foodService)
                            .join(serviceDestination_ofCustomServe)))
            .forAll(ofLoopFoodServiceDecl));
    /* 124 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(outputs.call(), ofLoopFoodServiceVar.join(serve_foodService), location)
            .forAll(ofLoopFoodServiceDecl));
    /* 125 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(outputs.call(), ofLoopFoodServiceVar.join(serve_foodService), foodItem)
            .forAll(ofLoopFoodServiceDecl));
    /* 126 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(eat_foodService)
            .join(inputs.call())
            .in(ofLoopFoodServiceVar.join(eat_foodService).join(eatenItem_ofEat))
            .forAll(ofLoopFoodServiceDecl));
    /* 127 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(inputs.call(), ofLoopFoodServiceVar.join(eat_foodService), foodItem)
            .forAll(ofLoopFoodServiceDecl));
    /* 128 */ alloy.addToOverallFact(
        functionFiltered
            .call(
                happensBefore.call(),
                ofLoopFoodServiceVar.join(eat_foodService),
                ofLoopFoodServiceVar
                    .join(end_ofLoopFoodService)
                    .plus(ofLoopFoodServiceVar.join(order_foodService)))
            .forAll(ofLoopFoodServiceDecl));
    /* 129 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(end_ofLoopFoodService)
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .forAll(ofLoopFoodServiceDecl));
    /* 130 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                sources.call(),
                ofLoopFoodServiceVar.join(transferOrderPay_ofLoopFoodService),
                ofLoopFoodServiceVar.join(order_foodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 131 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                targets.call(),
                ofLoopFoodServiceVar.join(transferOrderPay_ofLoopFoodService),
                ofLoopFoodServiceVar.join(pay_foodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 132 */ alloy.addToOverallFact(
        subsettingItemRuleForSources
            .call(ofLoopFoodServiceVar.join(transferOrderPay_ofLoopFoodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 133 */ alloy.addToOverallFact(
        subsettingItemRuleForTargets
            .call(ofLoopFoodServiceVar.join(transferOrderPay_ofLoopFoodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 134 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderPay_ofLoopFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferOrderPay_ofLoopFoodService)
                    .join(sources.call())
                    .join(orderedFoodItem_ofOrder)
                    .plus(
                        ofLoopFoodServiceVar
                            .join(transferOrderPay_ofLoopFoodService)
                            .join(sources.call())
                            .join(orderAmount_ofCustomOrder)))
            .forAll(ofLoopFoodServiceDecl));
    /* 135 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderPay_ofLoopFoodService)
            .join(sources.call())
            .join(orderedFoodItem_ofOrder)
            .plus(
                ofLoopFoodServiceVar
                    .join(transferOrderPay_ofLoopFoodService)
                    .join(sources.call())
                    .join(orderAmount_ofCustomOrder))
            .in(ofLoopFoodServiceVar.join(transferOrderPay_ofLoopFoodService).join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 136 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderPay_ofLoopFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferOrderPay_ofLoopFoodService)
                    .join(targets.call())
                    .join(paidFoodItem_ofPay)
                    .plus(
                        ofLoopFoodServiceVar
                            .join(transferOrderPay_ofLoopFoodService)
                            .join(targets.call())
                            .join(paidAmount_ofPay)))
            .forAll(ofLoopFoodServiceDecl));
    /* 137 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderPay_ofLoopFoodService)
            .join(targets.call())
            .join(paidFoodItem_ofPay)
            .plus(
                ofLoopFoodServiceVar
                    .join(transferOrderPay_ofLoopFoodService)
                    .join(targets.call())
                    .join(paidAmount_ofPay))
            .in(ofLoopFoodServiceVar.join(transferOrderPay_ofLoopFoodService).join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 138 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                sources.call(),
                ofLoopFoodServiceVar.join(transferPayEat_ofLoopFoodService),
                ofLoopFoodServiceVar.join(pay_foodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 139 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                targets.call(),
                ofLoopFoodServiceVar.join(transferPayEat_ofLoopFoodService),
                ofLoopFoodServiceVar.join(eat_foodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 140 */ alloy.addToOverallFact(
        subsettingItemRuleForSources
            .call(ofLoopFoodServiceVar.join(transferPayEat_ofLoopFoodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 141 */ alloy.addToOverallFact(
        subsettingItemRuleForTargets
            .call(ofLoopFoodServiceVar.join(transferPayEat_ofLoopFoodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 142 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferPayEat_ofLoopFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferPayEat_ofLoopFoodService)
                    .join(sources.call())
                    .join(paidFoodItem_ofPay))
            .forAll(ofLoopFoodServiceDecl));
    /* 143 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferPayEat_ofLoopFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferPayEat_ofLoopFoodService)
                    .join(targets.call())
                    .join(eatenItem_ofEat))
            .forAll(ofLoopFoodServiceDecl));
    /* 144 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferPayEat_ofLoopFoodService)
            .join(sources.call())
            .join(paidFoodItem_ofPay)
            .in(ofLoopFoodServiceVar.join(transferPayEat_ofLoopFoodService).join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 145 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferPayEat_ofLoopFoodService)
            .join(targets.call())
            .join(eatenItem_ofEat)
            .in(ofLoopFoodServiceVar.join(transferPayEat_ofLoopFoodService).join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 146 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderServe_ofFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferOrderServe_ofFoodService)
                    .join(sources.call())
                    .join(orderedFoodItem_ofOrder))
            .forAll(ofLoopFoodServiceDecl));
    /* 147 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderServe_ofFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferOrderServe_ofFoodService)
                    .join(targets.call())
                    .join(servedFoodItem_ofServe))
            .forAll(ofLoopFoodServiceDecl));
    /* 148 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderServe_ofFoodService)
            .join(sources.call())
            .join(orderedFoodItem_ofOrder)
            .in(ofLoopFoodServiceVar.join(transferOrderServe_ofFoodService).join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 149 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderServe_ofFoodService)
            .join(targets.call())
            .join(servedFoodItem_ofServe)
            .in(ofLoopFoodServiceVar.join(transferOrderServe_ofFoodService).join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 150 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                sources.call(),
                ofLoopFoodServiceVar.join(transferOrderPrepare_ofLoopFoodService),
                ofLoopFoodServiceVar.join(order_foodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 151 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                targets.call(),
                ofLoopFoodServiceVar.join(transferOrderPrepare_ofLoopFoodService),
                ofLoopFoodServiceVar.join(prepare_foodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 152 */ alloy.addToOverallFact(
        subsettingItemRuleForSources
            .call(ofLoopFoodServiceVar.join(transferOrderPrepare_ofLoopFoodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 153 */ alloy.addToOverallFact(
        subsettingItemRuleForTargets
            .call(ofLoopFoodServiceVar.join(transferOrderPrepare_ofLoopFoodService))
            .forAll(ofLoopFoodServiceDecl));
    /* 154 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderPrepare_ofLoopFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferOrderPrepare_ofLoopFoodService)
                    .join(sources.call())
                    .join(orderedFoodItem_ofOrder)
                    .plus(
                        ofLoopFoodServiceVar
                            .join(transferOrderPrepare_ofLoopFoodService)
                            .join(sources.call())
                            .join(orderDestination_ofCustomOrder)))
            .forAll(ofLoopFoodServiceDecl));
    /* 155 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderPrepare_ofLoopFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferOrderPrepare_ofLoopFoodService)
                    .join(targets.call())
                    .join(preparedFoodItem_ofPrepare)
                    .plus(
                        ofLoopFoodServiceVar
                            .join(transferOrderPrepare_ofLoopFoodService)
                            .join(targets.call())
                            .join(prepareDestination_ofCustomPrepare)))
            .forAll(ofLoopFoodServiceDecl));
    /* 156 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderPrepare_ofLoopFoodService)
            .join(sources.call())
            .join(orderedFoodItem_ofOrder)
            .plus(
                ofLoopFoodServiceVar
                    .join(transferOrderPrepare_ofLoopFoodService)
                    .join(sources.call())
                    .join(orderDestination_ofCustomOrder))
            .in(
                ofLoopFoodServiceVar
                    .join(transferOrderPrepare_ofLoopFoodService)
                    .join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 157 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferOrderPrepare_ofLoopFoodService)
            .join(targets.call())
            .join(preparedFoodItem_ofPrepare)
            .plus(
                ofLoopFoodServiceVar
                    .join(transferOrderPrepare_ofLoopFoodService)
                    .join(targets.call())
                    .join(prepareDestination_ofCustomPrepare))
            .in(
                ofLoopFoodServiceVar
                    .join(transferOrderPrepare_ofLoopFoodService)
                    .join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 158 */ alloy.addToOverallFact(
        (ofLoopFoodServiceVar
                .join(transferPrepareServe_ofFoodService)
                .join(items.call())
                .in(
                    ofLoopFoodServiceVar
                        .join(transferPrepareServe_ofFoodService)
                        .join(sources.call())
                        .join(preparedFoodItem_ofPrepare)
                        .plus(
                            ofLoopFoodServiceVar
                                .join(transferPrepareServe_ofFoodService)
                                .join(sources.call())
                                .join(prepareDestination_ofCustomPrepare))))
            .forAll(ofLoopFoodServiceDecl));
    /* 159 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferPrepareServe_ofFoodService)
            .join(sources.call())
            .join(preparedFoodItem_ofPrepare)
            .plus(
                ofLoopFoodServiceVar
                    .join(transferPrepareServe_ofFoodService)
                    .join(sources.call())
                    .join(prepareDestination_ofCustomPrepare))
            .in(ofLoopFoodServiceVar.join(transferPrepareServe_ofFoodService).join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 160 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferPrepareServe_ofFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferPrepareServe_ofFoodService)
                    .join(targets.call())
                    .join(servedFoodItem_ofServe)
                    .plus(
                        ofLoopFoodServiceVar
                            .join(transferPrepareServe_ofFoodService)
                            .join(targets.call())
                            .join(serviceDestination_ofCustomServe)))
            .forAll(ofLoopFoodServiceDecl));
    /* 161 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferPrepareServe_ofFoodService)
            .join(targets.call())
            .join(servedFoodItem_ofServe)
            .plus(
                ofLoopFoodServiceVar
                    .join(transferPrepareServe_ofFoodService)
                    .join(targets.call())
                    .join(serviceDestination_ofCustomServe))
            .in(ofLoopFoodServiceVar.join(transferPrepareServe_ofFoodService).join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 162 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferServeEat_ofFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferServeEat_ofFoodService)
                    .join(sources.call())
                    .join(servedFoodItem_ofServe))
            .forAll(ofLoopFoodServiceDecl));
    /* 163 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferServeEat_ofFoodService)
            .join(items.call())
            .in(
                ofLoopFoodServiceVar
                    .join(transferServeEat_ofFoodService)
                    .join(targets.call())
                    .join(eatenItem_ofEat))
            .forAll(ofLoopFoodServiceDecl));
    /* 164 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferServeEat_ofFoodService)
            .join(sources.call())
            .join(servedFoodItem_ofServe)
            .in(ofLoopFoodServiceVar.join(transferServeEat_ofFoodService).join(items.call()))
            .forAll(ofLoopFoodServiceDecl));
    /* 165 */ alloy.addToOverallFact(
        ofLoopFoodServiceVar
            .join(transferServeEat_ofFoodService)
            .join(targets.call())
            .join(eatenItem_ofEat)
            .in(ofLoopFoodServiceVar.join(transferServeEat_ofFoodService).join(items.call()))
            .forAll(ofLoopFoodServiceDecl));

    // OFParallelFoodService
    // Implicit Fact
    // 166) bijectionFiltered[happensBefore, pay, prepare]
    // 167) bijectionFiltered[happensBefore, pay, order]
    // Explicit Fact
    // 166) bijectionFiltered[happensBefore, of_parallel_food_service.pay,
    // of_parallel_food_service.prepare]
    // 167) bijectionFiltered[happensBefore, of_parallel_food_service.pay,
    // of_parallel_food_service.order]

    ExprVar ofParallelFoodServiceVar =
        ExprVar.make(null, "of_parallel_food_service", ofParallelFoodService.type());
    Decl ofParallelFoodServiceDecl =
        new Decl(
            null, null, null, List.of(ofParallelFoodServiceVar), ofParallelFoodService.oneOf());

    /* 166 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                happensBefore.call(),
                ofParallelFoodServiceVar.join(pay_foodService),
                ofParallelFoodServiceVar.join(prepare_foodService))
            .forAll(ofParallelFoodServiceDecl));
    /* 167 */ alloy.addToOverallFact(
        bijectionFiltered
            .call(
                happensBefore.call(),
                ofParallelFoodServiceVar.join(pay_foodService),
                ofParallelFoodServiceVar.join(order_foodService))
            .forAll(ofParallelFoodServiceDecl));

    // General Functions and Predicates

    // instancesDuringExample
    Expr instancesDuringExampleExpr =
        order
            .in(ofFoodService.join(order_foodService))
            .and(prepare.in(ofFoodService.join(prepare_foodService)))
            .and(serve.in(ofFoodService.join(serve_foodService)))
            .and(eat.in(ofFoodService.join(eat_foodService)))
            .and(pay.in(ofFoodService.join(pay_foodService)));
    Func instancesDuringExampleFunc =
        new Func(null, "instancesDuringExample", null, null, instancesDuringExampleExpr);

    // noCustomFoodService
    Expr noCustomFoodServiceExpr =
        ofCustomOrder.no().and(ofCustomPrepare.no()).and(ofCustomServe.no());
    Func noCustomFoodServiceFunc =
        new Func(null, "noCustomFoodService", null, null, noCustomFoodServiceExpr);

    // noChildFoodService
    Expr noChildFoodServiceExpr =
        ofSingleFoodService.no().and(ofLoopFoodService.no()).and(ofParallelFoodService.no());
    Func noChildFoodServiceFunc =
        new Func(null, "noChildFoodService", null, null, noChildFoodServiceExpr);

    // onlyOFFoodService
    Expr onlyOfFoodServiceExpr =
        foodService
            .in(ofFoodService)
            .and(noChildFoodServiceFunc.call())
            .and(ofFoodService.cardinality().equal(ExprConstant.makeNUMBER(1)))
            .and(noCustomFoodServiceFunc.call());
    Func onlyOfFoodServiceFunc =
        new Func(null, "onlyOFFoodService", null, null, onlyOfFoodServiceExpr);

    // onlyOFSingleFoodService
    Expr onlyOfSingleFoodServiceExpr = foodService.in(ofSingleFoodService);
    Func onlyOfSingleFoodServiceFunc =
        new Func(null, "onlyOFSingleFoodService", null, null, onlyOfSingleFoodServiceExpr);

    // onlyOFLoopFoodService
    Expr onlyOfLoopFoodServiceExpr = foodService.in(ofLoopFoodService);
    Func onlyOfLoopFoodServiceFunc =
        new Func(null, "onlyOFLoopFoodService", null, null, onlyOfLoopFoodServiceExpr);

    // onlyOFParallelFoodService
    Expr onlyOfParallelFoodServiceExpr = foodService.in(ofParallelFoodService);
    Func onlyOfParallelFoodServiceFunc =
        new Func(null, "onlyOFParallelFoodService", null, null, onlyOfParallelFoodServiceExpr);

    // Checks and runs

    // setup
    //
    Func nonZeroDurationOnlyFunc =
        AlloyUtils.getFunction(Alloy.transferModule, "o/nonZeroDurationOnly");

    // showOFFoodService
    Expr showOFFoodServiceExpr =
        nonZeroDurationOnlyFunc
            .call()
            .and(instancesDuringExampleFunc.call())
            .and(onlyOfFoodServiceFunc.call())
            .and(
                ofFoodService
                    .join(order_foodService)
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1)));
    Command showOFFoodServiceCommand =
        new Command(
            null,
            showOFFoodServiceExpr,
            "showOFFoodService",
            false,
            12,
            -1,
            -1,
            -1,
            List.of(),
            List.of(),
            alloy.getOverAllFact().and(showOFFoodServiceExpr),
            null);

    // showOFSingleFoodService
    Expr showOFSingleFoodServiceExpr =
        nonZeroDurationOnlyFunc
            .call()
            .and(instancesDuringExampleFunc.call())
            .and(onlyOfSingleFoodServiceFunc.call());
    CommandScope cs1 = new CommandScope(ofSingleFoodService, true, 1);
    ;
    Command showOFSingleFoodServiceCommand =
        new Command(
            null,
            showOFSingleFoodServiceExpr,
            "showOFSingleFoodService",
            false,
            15,
            -1,
            -1,
            -1,
            List.of(cs1),
            List.of(),
            alloy.getOverAllFact().and(showOFSingleFoodServiceExpr),
            null);

    // showOFLoopFoodService
    Expr showOFLoopFoodServiceExpr =
        nonZeroDurationOnlyFunc
            .call()
            .and(instancesDuringExampleFunc.call())
            .and(onlyOfLoopFoodServiceFunc.call());
    CommandScope cs2 = new CommandScope(ofLoopFoodService, true, 1);
    Command showOFLoopFoodServiceCommand =
        new Command(
            null,
            showOFLoopFoodServiceExpr,
            "showOFLoopFoodService",
            false,
            30,
            -1,
            -1,
            -1,
            List.of(cs2),
            List.of(),
            alloy.getOverAllFact().and(showOFLoopFoodServiceExpr),
            null);

    // showOFParallelFoodService
    Expr showOFParallelFoodServiceExpr =
        nonZeroDurationOnlyFunc
            .call()
            .and(instancesDuringExampleFunc.call())
            .and(onlyOfParallelFoodServiceFunc.call());
    Command showOFParallelFoodServiceCommand =
        new Command(
            null,
            showOFParallelFoodServiceExpr,
            "showOFParallelFoodService",
            false,
            10,
            -1,
            -1,
            -1,
            List.of(),
            List.of(),
            alloy.getOverAllFact().and(showOFParallelFoodServiceExpr),
            null);
    //
    // Command[] commands = {showOFFoodServiceCommand, showOFSingleFoodServiceCommand,
    // showOFLoopFoodServiceCommand, showOFParallelFoodServiceCommand};

    // ===== Create Alloy file version =====
    CompModule importedModule = AlloyUtils.importAlloyModule(filename);

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

    for (Sig sig : fileSigs) {
      fileMap.put(AlloyUtils.removeSlash(sig.toString()), sig);
    }
    for (Sig sig : apiSigs) {
      apiMap.put(AlloyUtils.removeSlash(sig.toString()), sig);
    }

    assertTrue(fileSigs.size() == apiSigs.size());

    for (String sigName : fileMap.keySet()) {
      assertTrue(apiMap.containsKey(sigName));
      assertTrue(ec.compareTwoExpressions(fileMap.get(sigName), apiMap.get(sigName)));
    }

    // ========== Test if command(s) are equal ==========

    // List<Command> importedCommands = importedModule.getAllCommands();
    //
    // assertEquals(commands.length, importedCommands.size());
    //
    // for (int i = 0; i < commands.length; i++) {
    // assertTrue(ec.compareCommand(commands[i], importedCommands.get(i)));
    // }

    // commands without content
    Command command = alloy.createCommand(moduleName, 10);
    Command[] commands = {command};

    // ========== Write file ==========

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact(), commands);

    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());

    translator.generateAlsFileContents(alloyModule, outFileName);
  }
}
