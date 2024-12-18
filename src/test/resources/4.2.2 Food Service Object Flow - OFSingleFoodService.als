//***********************************************************************************************************
// Module: 		Single Food Service Object Flow
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence/Transfer module to show
//				object flow in various kinds of Food Service from Wyner (see NISTIR 8283).
// Notes:			The constraints in some of the signature facts for signatures are to prevent uninteresting 
//				and non-meaningful examples. Although some of these relations might be possible or 
//				even desirable in some modeling context, they are not desirable here. For example, 
//				FoodItem, Location, and Real are alll intended as objects to flow between process steps.
//				As such, they don't need to be temporally ordered, or be steps, or have inputs/outputs.
//***********************************************************************************************************
module OFSingleFoodServiceModule
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
//***********************************************************************************************************
/** 				OFOrder */
//***********************************************************************************************************
sig OFOrder extends Order {orderedFoodItem: set FoodItem}
fact {all x: OFOrder | #(x.orderedFoodItem) = 1}
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
fact {all x: OFFoodService | x.order in OFOrder}
fact {all x: OFFoodService | x.prepare in OFPrepare}
fact {all x: OFFoodService | x.pay in OFPay}
fact {all x: OFFoodService | x.eat in OFEat}
fact {all x: OFFoodService | x.serve in OFServe}
/** Constraints on transfers */
	/** Constraints on the Transfer from order to serve */
fact {all x: OFFoodService | x.transferOrderServe in x.steps}
fact {all x: OFFoodService | bijectionFiltered[sources, x.transferOrderServe, x.order]}
fact {all x: OFFoodService | bijectionFiltered[targets, x.transferOrderServe, x.serve]}
fact {all x: OFFoodService | subsettingItemRuleForSources[x.transferOrderServe]}
fact {all x: OFFoodService | subsettingItemRuleForTargets[x.transferOrderServe]}
fact {all x: OFFoodService | isAfterSource[x.transferOrderServe]}
fact {all x: OFFoodService | isBeforeTarget[x.transferOrderServe]}
//***********************************************************************************************************
/** 				Object Flow Single Food Service */
//***********************************************************************************************************
sig OFSingleFoodService extends OFFoodService{disj transferOrderPay, transferOrderPrepare, transferPayEat, transferPrepareServe, transferServeEat: set Transfer}
/** Constraints on OFSingleFoodService */
fact {all x: OFSingleFoodService | no x.inputs}
fact {all x: OFSingleFoodService | no inputs.x}
fact {all x: OFSingleFoodService | no outputs.x}
fact {all x: OFSingleFoodService | no x.outputs}
fact {all x: OFSingleFoodService | no items.x}

// Set Transfers as Steps and constrain Steps
fact {all x: OFSingleFoodService | x.transferOrderPay + x.transferOrderPrepare + x.transferPayEat + x.transferPrepareServe + x.transferServeEat in x.steps}
fact {all x: OFSingleFoodService | x.steps in x.eat + x.order + x.pay + x.prepare + x.serve + x.transferOrderPay + x.transferOrderPrepare + x.transferOrderServe + x.transferPayEat + x.transferPrepareServe + x.transferServeEat}

/** Constraints on process steps */
	/** Constraints on order: OFCustomOrder */
fact {all x: OFSingleFoodService | x.order in OFCustomOrder}
fact {all x: OFSingleFoodService | #(x.order) = 1}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.order, x.order.orderAmount]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.order, x.order.orderDestination]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.order, x.order.orderedFoodItem]}
fact {all x: OFSingleFoodService | x.order.outputs in x.order.orderAmount + x.order.orderDestination + x.order.orderedFoodItem}

	/** Constraints on pay: OFPay */
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.pay, x.pay.paidAmount]}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.pay, x.pay.paidFoodItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.pay, x.pay.paidFoodItem]}
fact {all x: OFSingleFoodService | x.pay.inputs in x.pay.paidAmount + x.pay.paidFoodItem}
fact {all x: OFSingleFoodService | x.pay.outputs in x.pay.paidFoodItem}

	/** Constraints on prepare: OFCustomPrepare */
fact {all x: OFSingleFoodService | x.prepare in OFCustomPrepare}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.prepare, x.prepare.prepareDestination]}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.prepare, x.prepare.preparedFoodItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.prepare, x.prepare.prepareDestination]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.prepare, x.prepare.preparedFoodItem]}
fact {all x: OFSingleFoodService | x.prepare.inputs in x.prepare.prepareDestination + x.prepare.preparedFoodItem}
fact {all x: OFSingleFoodService | x.prepare.outputs in x.prepare.prepareDestination + x.prepare.preparedFoodItem}

	/** Constraints on serve: OFCustomServe */
fact {all x: OFSingleFoodService | x.serve in OFCustomServe}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.serve, x.serve.servedFoodItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.serve, x.serve.serviceDestination]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.serve, x.serve.servedFoodItem]}
fact {all x: OFSingleFoodService | x.serve.inputs in x.serve.servedFoodItem + x.serve.serviceDestination}
fact {all x: OFSingleFoodService | x.serve.outputs in x.serve.servedFoodItem}

	/** Constraints on eat: OFEat */	
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.eat, x.eat.eatenItem]}
fact {all x: OFSingleFoodService | x.eat.inputs in x.eat.eatenItem}

/** Constraints on transfers */
	/** Ensure disjointness of Transfers, since some are inherited */
fact {all x: OFSingleFoodService | no x.transferOrderPay & x.transferOrderPrepare & x.transferOrderServe & x.transferPayEat & x.transferPrepareServe & x.transferServeEat}
	/** Constraints on the Transfer from order to pay*/
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferOrderPay, x.order]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferOrderPay, x.pay]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferOrderPay]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferOrderPay]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferOrderPay]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferOrderPay]}
fact {all x: OFSingleFoodService | x.transferOrderPay.items in x.transferOrderPay.sources.orderAmount + x.transferOrderPay.sources.orderedFoodItem}
fact {all x: OFSingleFoodService | x.transferOrderPay.sources.orderAmount + x.transferOrderPay.sources.orderedFoodItem in x.transferOrderPay.items}
fact {all x: OFSingleFoodService | x.transferOrderPay.items in x.transferOrderPay.targets.paidAmount + x.transferOrderPay.targets.paidFoodItem}
fact {all x: OFSingleFoodService | x.transferOrderPay.targets.paidAmount + x.transferOrderPay.targets.paidFoodItem in x.transferOrderPay.items}

	/** Constraints on the Transfer from pay to eat */
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferPayEat, x.pay]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferPayEat, x.eat]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferPayEat]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferPayEat]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferPayEat]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferPayEat]}
fact {all x: OFSingleFoodService | x.transferPayEat.items in x.transferPayEat.sources.paidFoodItem}
fact {all x: OFSingleFoodService | x.transferPayEat.items in x.transferPayEat.targets.eatenItem}
fact {all x: OFSingleFoodService | x.transferPayEat.sources.paidFoodItem in x.transferPayEat.items}
fact {all x: OFSingleFoodService | x.transferPayEat.targets.eatenItem in x.transferPayEat.items}

	/** Constraints on the Transfer from order to prepare*/
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferOrderPrepare, x.order]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferOrderPrepare, x.prepare]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferOrderPrepare]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferOrderPrepare]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferOrderPrepare]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferOrderPrepare]}
fact {all x: OFSingleFoodService | x.transferOrderPrepare.items in x.transferOrderPrepare.sources.orderDestination + x.transferOrderPrepare.sources.orderedFoodItem}
fact {all x: OFSingleFoodService | x.transferOrderPrepare.items in x.transferOrderPrepare.targets.prepareDestination + x.transferOrderPrepare.targets.preparedFoodItem}
fact {all x: OFSingleFoodService | x.transferOrderPrepare.sources.orderDestination + x.transferOrderPrepare.sources.orderedFoodItem in x.transferOrderPrepare.items}
fact {all x: OFSingleFoodService | x.transferOrderPrepare.targets.prepareDestination + x.transferOrderPrepare.targets.preparedFoodItem in x.transferOrderPrepare.items}

	/** Constraints on the Transfer from order to serve */
fact {all x: OFSingleFoodService | x.transferOrderServe.items in x.transferOrderServe.sources.orderedFoodItem}
fact {all x: OFSingleFoodService | x.transferOrderServe.items in x.transferOrderServe.targets.servedFoodItem}
fact {all x: OFSingleFoodService | x.transferOrderServe.sources.orderedFoodItem in x.transferOrderServe.items}
fact {all x: OFSingleFoodService | x.transferOrderServe.targets.servedFoodItem in x.transferOrderServe.items}

	/** Constraints on the Transfer from prepare to serve */
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferPrepareServe, x.prepare]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferPrepareServe, x.serve]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferPrepareServe]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferPrepareServe]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferPrepareServe]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferPrepareServe]}
fact {all x: OFSingleFoodService | x.transferPrepareServe.items in x.transferPrepareServe.sources.prepareDestination + x.transferPrepareServe.sources.preparedFoodItem}
fact {all x: OFSingleFoodService | x.transferPrepareServe.sources.prepareDestination + x.transferPrepareServe.sources.preparedFoodItem in x.transferPrepareServe.items}
fact {all x: OFSingleFoodService | x.transferPrepareServe.items in x.transferPrepareServe.targets.servedFoodItem + x.transferPrepareServe.targets.serviceDestination}
fact {all x: OFSingleFoodService | x.transferPrepareServe.targets.servedFoodItem + x.transferPrepareServe.targets.serviceDestination in x.transferPrepareServe.items}

	/** Constraints on the Transfer from serve to eat */
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferServeEat, x.serve]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferServeEat, x.eat]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferServeEat]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferServeEat]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferServeEat]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferServeEat]}
fact {all x: OFSingleFoodService | x.transferServeEat.items in x.transferServeEat.sources.servedFoodItem}
fact {all x: OFSingleFoodService | x.transferServeEat.items in x.transferServeEat.targets.eatenItem}
fact {all x: OFSingleFoodService | x.transferServeEat.sources.servedFoodItem in x.transferServeEat.items}
fact {all x: OFSingleFoodService | x.transferServeEat.targets.eatenItem in x.transferServeEat.items}
//***********************************************************************************************************
/** 			General Functions and Predicates */
//***********************************************************************************************************
pred instancesDuringExample{Order in OFSingleFoodService.order && Prepare in OFSingleFoodService.prepare && Serve in OFSingleFoodService.serve && Eat in OFSingleFoodService.eat && Pay in OFSingleFoodService.pay}
//***********************************************************************************************************
/** 				Checks and Runs */
//***********************************************************************************************************
run showOFSingleFoodService{instancesDuringExample} for 30 but exactly 1 OFSingleFoodService, 6 int
