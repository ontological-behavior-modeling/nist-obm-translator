//***********************************************************************************************************
// Module: 		Food Service Object Flow with Control Loop
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence/Transfer module to show sequential
//				object flow in Food Service from Wyner (see NISTIR 8283).
// Notes:			The constraints in some of the signature facts for signatures are to prevent uninteresting 
//				and non-meaningful examples. Although some of these relations might be possible or 
//				even desirable in some modeling context, they are not desirable here. For example, 
//				FoodItem, Location, and Real are alll intended as objects to flow between process steps.
//				As such, they don't need to be temporally ordered, or be steps, or have inputs/outputs.
//***********************************************************************************************************
module OFControlLoopFoodServiceModule
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Order extends Occurrence {}
sig Prepare extends Occurrence {}
sig Serve extends Occurrence {}
sig Eat extends Occurrence {}
sig Pay extends Occurrence {}

// Items are universal, can define constraints
sig FoodItem extends Occurrence {}
fact {all x: FoodItem | no x.inputs}
fact {all x: FoodItem | no x.outputs}
fact {all x: FoodItem | no x.steps}
fact {all x: FoodItem | no steps.x}

sig Location extends Occurrence {}
fact {all x: Location | no x.inputs}
fact {all x: Location | no x.outputs}
fact {all x: Location | no x.steps}
fact {all x: Location | no steps.x}

sig Real extends Occurrence {}
fact {all x: Real | no x.inputs}
fact {all x: Real | no x.outputs}
fact {all x: Real | no x.steps}
fact {all x: Real | no steps.x}

sig OFStart extends Occurrence {}
fact {all x: OFStart | no x.inputs}
fact {all x: OFStart | no inputs.x}
fact {all x: OFStart | no x.outputs}
fact {all x: OFStart | no outputs.x}
fact {all x: OFStart | no items.x}
fact {all x: OFStart | no x.steps}

sig OFEnd extends Occurrence {}
fact {all x: OFEnd | no x.inputs}
fact {all x: OFEnd | no inputs.x}
fact {all x: OFEnd | no x.outputs}
fact {all x: OFEnd | no outputs.x}
fact {all x: OFEnd | no items.x}
fact {all x: OFEnd | no x.steps}
//***********************************************************************************************************
/** 				OFOrder */
//***********************************************************************************************************
sig OFOrder extends Order {orderedFoodItem: set FoodItem}
fact {all x: OFOrder | #x.orderedFoodItem = 1}
//***********************************************************************************************************
/** 				OFCustomOrder */
//***********************************************************************************************************
sig OFCustomOrder extends OFOrder {orderAmount: set Real, orderDestination: set Location}
fact {all x: OFCustomOrder | #(x.orderAmount) = 1}
fact {all x: OFCustomOrder | #(x.orderDestination) = 1}
fact {all x: OFCustomOrder | no x.inputs}
fact {all x: OFCustomOrder | no inputs.x}
fact {all x: OFCustomOrder | no outputs.x}
fact {all x: OFCustomOrder | no items.x}
fact {all x: OFCustomOrder | no x.steps}
fact {all x: OFCustomOrder | x.orderAmount + x.orderDestination + x.orderedFoodItem in x.outputs}
fact {all x: OFCustomOrder | x.outputs in x.orderAmount + x.orderDestination + x.orderedFoodItem}
//***********************************************************************************************************
/** 				OFPrepare */
//***********************************************************************************************************
sig OFPrepare extends Prepare {preparedFoodItem: set FoodItem}
fact {all x: OFPrepare | #(x.preparedFoodItem) = 1}
//***********************************************************************************************************
/** 				OFCustomPrepare */
//***********************************************************************************************************
sig OFCustomPrepare extends OFPrepare {prepareDestination: set Location}
fact {all x: OFCustomPrepare | #(x.prepareDestination) = 1}
fact {all x: OFCustomPrepare | x.prepareDestination + x.preparedFoodItem in x.inputs}
fact {all x: OFCustomPrepare | x.inputs in x.prepareDestination + x.preparedFoodItem}
fact {all x: OFCustomPrepare | x.prepareDestination + x.preparedFoodItem in x.outputs}
fact {all x: OFCustomPrepare | x.outputs in x.prepareDestination + x.preparedFoodItem}
fact {all x: OFCustomPrepare | no inputs.x}
fact {all x: OFCustomPrepare | no outputs.x}
fact {all x: OFCustomPrepare | no items.x}
fact {all x: OFCustomPrepare | no x.steps}
//***********************************************************************************************************
/** 				OFServe */
//***********************************************************************************************************
sig OFServe extends Serve {servedFoodItem: set FoodItem}
fact {all x: OFServe | #(x.servedFoodItem) = 1}
//***********************************************************************************************************
/** 				OFCustomServe */
//***********************************************************************************************************
sig OFCustomServe extends OFServe {serviceDestination: set Location}
fact {all x: OFCustomServe | #(x.serviceDestination) = 1}
fact {all x: OFCustomServe | x.servedFoodItem + x.serviceDestination in x.inputs}
fact {all x: OFCustomServe | x.inputs in x.servedFoodItem + x.serviceDestination}
fact {all x: OFCustomServe | x.servedFoodItem in x.outputs}
fact {all x: OFCustomServe | x.outputs in x.servedFoodItem}
fact {all x: OFCustomServe | no inputs.x}
fact {all x: OFCustomServe | no outputs.x}
fact {all x: OFCustomServe | no items.x}
fact {all x: OFCustomServe | no x.steps}
//***********************************************************************************************************
/** 				OFEat */
//***********************************************************************************************************
sig OFEat extends Eat {eatenItem: set FoodItem}
fact {all x: OFEat | #(x.eatenItem) = 1}
fact {all x: OFEat | x.eatenItem in x.inputs}
fact {all x: OFEat | x.inputs in x.eatenItem}
fact {all x: OFEat | no x.outputs}
fact {all x: OFEat | no inputs.x}
fact {all x: OFEat | no outputs.x}
fact {all x: OFEat | no items.x}
fact {all x: OFEat | no x.steps}
//***********************************************************************************************************
/** 				OFPay */
//***********************************************************************************************************
sig OFPay extends Pay {paidAmount: set Real, paidFoodItem: set FoodItem}
fact {all x: OFPay | #(x.paidAmount) = 1}
fact {all x: OFPay | #(x.paidFoodItem) = 1}
fact {all x: OFPay | x.paidAmount + x.paidFoodItem in x.inputs}
fact {all x: OFPay | x.inputs in x.paidAmount + x.paidFoodItem}
fact {all x: OFPay | x.paidFoodItem in x.outputs}
fact {all x: OFPay | x.outputs in x.paidFoodItem}
fact {all x: OFPay | no inputs.x}
fact {all x: OFPay | no outputs.x}
fact {all x: OFPay | no items.x}
fact {all x: OFPay | no x.steps}
//***********************************************************************************************************
/** 				Food Service */
//***********************************************************************************************************
sig FoodService extends Occurrence {eat: set Eat, order: set Order, pay: set Pay, prepare: set Prepare, serve: set Serve}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.order, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.prepare, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.serve, x.eat]}
fact {all x: FoodService | x.eat + x.order + x.pay + x.prepare + x.serve in x.steps}
//***********************************************************************************************************
/** 				OFFoodService */
//***********************************************************************************************************
sig OFFoodService extends FoodService {transferOrderServe: set Transfer}
/** Constraints on OFFoodService */
// Constrain relations to their subtypes
fact {all x: OFFoodService | x.order in OFOrder}
fact {all x: OFFoodService | x.prepare in OFPrepare}
fact {all x: OFFoodService | x.pay in OFPay}
fact {all x: OFFoodService | x.eat in OFEat}
fact {all x: OFFoodService | x.serve in OFServe}
fact {all x: OFFoodService | x.transferOrderServe in x.steps}
/** Constraints on transfers */
	/** Constraints on the Transfer from order to serve */
fact {all x: OFFoodService | bijectionFiltered[sources, x.transferOrderServe, x.order]}
fact {all x: OFFoodService | bijectionFiltered[targets, x.transferOrderServe, x.serve]}
fact {all x: OFFoodService | subsettingItemRuleForSources[x.transferOrderServe]}
fact {all x: OFFoodService | subsettingItemRuleForTargets[x.transferOrderServe]}
fact {all x: OFFoodService | isAfterSource[x.transferOrderServe]}
fact {all x: OFFoodService | isBeforeTarget[x.transferOrderServe]}
//***********************************************************************************************************
/** 				Object Flow Food Service with Control Loop */
//***********************************************************************************************************
sig OFControlLoopFoodService extends OFFoodService{start: set OFStart, end: set OFEnd, disj transferOrderPay, transferPayEat, transferOrderPrepare, transferPrepareServe, transferServeEat: set Transfer}
/** Constraints on OFControlLoopFoodService */
fact {all x: OFControlLoopFoodService | no x.inputs}
fact {all x: OFControlLoopFoodService | no inputs.x}
fact {all x: OFControlLoopFoodService | no outputs.x}
fact {all x: OFControlLoopFoodService | no x.outputs}
fact {all x: OFControlLoopFoodService | no items.x}

// Set new Steps and constrain Steps
fact {all x: OFControlLoopFoodService | x.end + x.start + x.transferOrderPay + x.transferOrderPrepare + x.transferPayEat + x.transferPrepareServe + x.transferServeEat in x.steps}
fact {all x: OFControlLoopFoodService | x.steps in x.eat + x.end + x.order + x.pay + x.prepare + x.serve + x.start + x.transferOrderPay + x.transferOrderPrepare + x.transferOrderServe + x.transferPayEat + x.transferPrepareServe + x.transferServeEat}

/** Constraints on process steps */
	/** HappensBefore constraints */
fact {all x: OFControlLoopFoodService | functionFiltered[happensBefore, x.start, x.order]}
fact {all x: OFControlLoopFoodService | inverseFunctionFiltered[happensBefore, x.eat + x.start, x.order]}
fact {all x: OFControlLoopFoodService | functionFiltered[happensBefore, x.eat, x.end + x.order]}
fact {all x: OFControlLoopFoodService | inverseFunctionFiltered[happensBefore, x.eat, x.end]}

	/** Constraints on start */
fact {all x: OFControlLoopFoodService | #(x.start) = 1}

	/** Constraints on order */
fact {all x: OFControlLoopFoodService | x.order in OFCustomOrder}
fact {all x: OFControlLoopFoodService | #(x.order) = 2}
fact {all x: OFControlLoopFoodService | bijectionFiltered[outputs, x.order, x.order.orderAmount]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[outputs, x.order, x.order.orderDestination]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[outputs, x.order, x.order.orderedFoodItem]}
fact {all x: OFControlLoopFoodService | x.order.outputs in x.order.orderAmount + x.order.orderDestination + x.order.orderedFoodItem}

	/** Constraints on pay */
fact {all x: OFControlLoopFoodService | x.pay.inputs in x.pay.paidAmount + x.pay.paidFoodItem}
fact {all x: OFControlLoopFoodService | x.pay.outputs in x.pay.paidFoodItem}
fact {all x: OFControlLoopFoodService | bijectionFiltered[inputs, x.pay, x.pay.paidAmount]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[inputs, x.pay, x.pay.paidFoodItem]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[outputs, x.pay, x.pay.paidFoodItem]}
//	functionFiltered[outputs, pay, FoodItem]	// Changed this. Not sure why it was a function. May have to change back.

	/** Constraints on prepare */
fact {all x: OFControlLoopFoodService | x.prepare in OFCustomPrepare}
fact {all x: OFControlLoopFoodService | bijectionFiltered[inputs, x.prepare, x.prepare.prepareDestination]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[inputs, x.prepare, x.prepare.preparedFoodItem]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[outputs, x.prepare, x.prepare.prepareDestination]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[outputs, x.prepare, x.prepare.preparedFoodItem]}
fact {all x: OFControlLoopFoodService | x.prepare.inputs in x.prepare.prepareDestination + x.prepare.preparedFoodItem}
fact {all x: OFControlLoopFoodService | x.prepare.outputs in x.prepare.prepareDestination + x.prepare.preparedFoodItem}

	/** Constraints on serve */
fact {all x: OFControlLoopFoodService | x.serve in OFCustomServe}
fact {all x: OFControlLoopFoodService | bijectionFiltered[inputs, x.serve, x.serve.servedFoodItem]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[inputs, x.serve, x.serve.serviceDestination]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[outputs, x.serve, x.serve.servedFoodItem]}
fact {all x: OFControlLoopFoodService | x.serve.inputs in x.serve.servedFoodItem + x.serve.serviceDestination}
fact {all x: OFControlLoopFoodService | x.serve.outputs in x.serve.servedFoodItem}

	/** Constraints on eat */
fact {all x: OFControlLoopFoodService | bijectionFiltered[inputs, x.eat, x.eat.eatenItem]}
fact {all x: OFControlLoopFoodService | x.eat.inputs in x.eat.eatenItem}

	/** Constraints on end */
fact {all x: OFControlLoopFoodService | #(x.end) = 1}

/** Constraints on transfers */
	/** Ensure disjointness of Transfers, since some are inherited */
fact {all x: OFControlLoopFoodService | no x.transferOrderPay & x.transferOrderPrepare & x.transferOrderServe & x.transferPayEat & x.transferPrepareServe & x.transferServeEat}

	/** Constraints on the Transfer from order to pay*/
fact {all x: OFControlLoopFoodService | bijectionFiltered[sources, x.transferOrderPay, x.order]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[targets, x.transferOrderPay, x.pay]}
fact {all x: OFControlLoopFoodService | subsettingItemRuleForSources[x.transferOrderPay]}
fact {all x: OFControlLoopFoodService | subsettingItemRuleForTargets[x.transferOrderPay]}
fact {all x: OFControlLoopFoodService | isAfterSource[x.transferOrderPay]}
fact {all x: OFControlLoopFoodService | isBeforeTarget[x.transferOrderPay]}
fact {all x: OFControlLoopFoodService | x.transferOrderPay.items in x.transferOrderPay.sources.orderAmount + x.transferOrderPay.sources.orderedFoodItem}
fact {all x: OFControlLoopFoodService | x.transferOrderPay.sources.orderAmount + x.transferOrderPay.sources.orderedFoodItem in x.transferOrderPay.items}
fact {all x: OFControlLoopFoodService | x.transferOrderPay.items in x.transferOrderPay.targets.paidAmount + x.transferOrderPay.targets.paidFoodItem}
fact {all x: OFControlLoopFoodService | x.transferOrderPay.targets.paidAmount + x.transferOrderPay.targets.paidFoodItem in x.transferOrderPay.items}

	/** Constraints on the Transfer from order to prepare */
fact {all x: OFControlLoopFoodService | bijectionFiltered[sources, x.transferOrderPrepare, x.order]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[targets, x.transferOrderPrepare, x.prepare]}
fact {all x: OFControlLoopFoodService | subsettingItemRuleForSources[x.transferOrderPrepare]}
fact {all x: OFControlLoopFoodService | subsettingItemRuleForTargets[x.transferOrderPrepare]}
fact {all x: OFControlLoopFoodService | isAfterSource[x.transferOrderPrepare]}
fact {all x: OFControlLoopFoodService | isBeforeTarget[x.transferOrderPrepare]}
fact {all x: OFControlLoopFoodService | x.transferOrderPrepare.items in x.transferOrderPrepare.sources.orderDestination + x.transferOrderPrepare.sources.orderedFoodItem}
fact {all x: OFControlLoopFoodService | x.transferOrderPrepare.items in x.transferOrderPrepare.targets.prepareDestination + x.transferOrderPrepare.targets.preparedFoodItem}
fact {all x: OFControlLoopFoodService | x.transferOrderPrepare.sources.orderDestination + x.transferOrderPrepare.sources.orderedFoodItem in x.transferOrderPrepare.items}
fact {all x: OFControlLoopFoodService | x.transferOrderPrepare.targets.prepareDestination + x.transferOrderPrepare.targets.preparedFoodItem in x.transferOrderPrepare.items}

	/** Constraints on the Transfer from order to serve */
fact {all x: OFControlLoopFoodService | x.transferOrderServe.items in x.transferOrderServe.sources.orderedFoodItem}
fact {all x: OFControlLoopFoodService | x.transferOrderServe.items in x.transferOrderServe.targets.servedFoodItem}
fact {all x: OFControlLoopFoodService | x.transferOrderServe.sources.orderedFoodItem in x.transferOrderServe.items}
fact {all x: OFControlLoopFoodService | x.transferOrderServe.targets.servedFoodItem in x.transferOrderServe.items}

	/** Constraints on the Transfer from pay to eat */
fact {all x: OFControlLoopFoodService | bijectionFiltered[sources, x.transferPayEat, x.pay]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[targets, x.transferPayEat, x.eat]}
fact {all x: OFControlLoopFoodService | subsettingItemRuleForSources[x.transferPayEat]}
fact {all x: OFControlLoopFoodService | subsettingItemRuleForTargets[x.transferPayEat]}
fact {all x: OFControlLoopFoodService | isAfterSource[x.transferPayEat]}
fact {all x: OFControlLoopFoodService | isBeforeTarget[x.transferPayEat]}
fact {all x: OFControlLoopFoodService | x.transferPayEat.items in x.transferPayEat.sources.paidFoodItem}
fact {all x: OFControlLoopFoodService | x.transferPayEat.items in x.transferPayEat.targets.eatenItem}
fact {all x: OFControlLoopFoodService | x.transferPayEat.sources.paidFoodItem in x.transferPayEat.items}
fact {all x: OFControlLoopFoodService | x.transferPayEat.targets.eatenItem in x.transferPayEat.items}

	/** Constraints on the Transfer from prepare to serve */
fact {all x: OFControlLoopFoodService | bijectionFiltered[sources, x.transferPrepareServe, x.prepare]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[targets, x.transferPrepareServe, x.serve]}
fact {all x: OFControlLoopFoodService | subsettingItemRuleForSources[x.transferPrepareServe]}
fact {all x: OFControlLoopFoodService | subsettingItemRuleForTargets[x.transferPrepareServe]}
fact {all x: OFControlLoopFoodService | isAfterSource[x.transferPrepareServe]}
fact {all x: OFControlLoopFoodService | isBeforeTarget[x.transferPrepareServe]}
fact {all x: OFControlLoopFoodService | x.transferPrepareServe.items in x.transferPrepareServe.sources.prepareDestination + x.transferPrepareServe.sources.preparedFoodItem}
fact {all x: OFControlLoopFoodService | x.transferPrepareServe.sources.prepareDestination + x.transferPrepareServe.sources.preparedFoodItem in x.transferPrepareServe.items}
fact {all x: OFControlLoopFoodService | x.transferPrepareServe.items in x.transferPrepareServe.targets.servedFoodItem + x.transferPrepareServe.targets.serviceDestination}
fact {all x: OFControlLoopFoodService | x.transferPrepareServe.targets.servedFoodItem + x.transferPrepareServe.targets.serviceDestination in x.transferPrepareServe.items}

	/** Constraints on the Transfer from serve to eat */
fact {all x: OFControlLoopFoodService | bijectionFiltered[sources, x.transferServeEat, x.serve]}
fact {all x: OFControlLoopFoodService | bijectionFiltered[targets, x.transferServeEat, x.eat]}
fact {all x: OFControlLoopFoodService | subsettingItemRuleForSources[x.transferServeEat]}
fact {all x: OFControlLoopFoodService | subsettingItemRuleForTargets[x.transferServeEat]}
fact {all x: OFControlLoopFoodService | isAfterSource[x.transferServeEat]}
fact {all x: OFControlLoopFoodService | isBeforeTarget[x.transferServeEat]}
fact {all x: OFControlLoopFoodService | x.transferServeEat.items in x.transferServeEat.sources.servedFoodItem}
fact {all x: OFControlLoopFoodService | x.transferServeEat.items in x.transferServeEat.targets.eatenItem}
fact {all x: OFControlLoopFoodService | x.transferServeEat.sources.servedFoodItem in x.transferServeEat.items}
fact {all x: OFControlLoopFoodService | x.transferServeEat.targets.eatenItem in x.transferServeEat.items}
//***********************************************************************************************************
/** 			General Functions and Predicates */
//***********************************************************************************************************
pred instancesDuringExample{Order in OFControlLoopFoodService.order && Prepare in OFControlLoopFoodService.prepare && Serve in OFControlLoopFoodService.serve && Eat in OFControlLoopFoodService.eat && Pay in OFControlLoopFoodService.pay && Transfer in OFControlLoopFoodService.steps && FoodService in OFControlLoopFoodService}
//***********************************************************************************************************
/** 				Checks and Runs */
//***********************************************************************************************************
run showOFControlLoopFoodService{instancesDuringExample} for 32 but exactly 1 OFControlLoopFoodService, 6 int
