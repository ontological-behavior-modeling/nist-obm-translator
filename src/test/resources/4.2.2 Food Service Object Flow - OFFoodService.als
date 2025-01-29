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
module OFFoodServiceModule
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
fact {all x: FoodItem | no x.happensBefore}
fact {all x: FoodItem | no happensBefore.x}

sig Location extends Occurrence {}
fact {all x: Location | no x.inputs}
fact {all x: Location | no x.outputs}
fact {all x: Location | no x.steps}
fact {all x: Location | no steps.x}
fact {all x: Location | no x.happensBefore}
fact {all x: Location | no happensBefore.x}

sig Real extends Occurrence {}
fact {all x: Real | no x.inputs}
fact {all x: Real | no x.outputs}
fact {all x: Real | no x.steps}
fact {all x: Real | no steps.x}
fact {all x: Real | no x.happensBefore}
fact {all x: Real | no happensBefore.x}

// Iniitial nodes and activity final nodes will always have these properties
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
sig OFOrder extends Order {orderedFoodItem: one FoodItem}		// does it actually need to be exactly one food item?
fact {all x: OFOrder | x.orderedFoodItem in x.outputs}
//***********************************************************************************************************
/** 				OFCustomOrder */
//***********************************************************************************************************
sig OFCustomOrder extends OFOrder {
	orderAmount: one Real, 
	orderDestination: one Location
}
fact {all x: OFCustomOrder | no x.inputs}
fact {all x: OFCustomOrder | x.orderAmount in x.outputs}
fact {all x: OFCustomOrder | x.orderDestination in x.outputs}
//***********************************************************************************************************
/** 				OFPrepare */
//***********************************************************************************************************
sig OFPrepare extends Prepare {preparedFoodItem: one FoodItem}
fact {all x: OFPrepare | x.preparedFoodItem in x.inputs}
fact {all x: OFPrepare | x.preparedFoodItem in x.outputs}
//***********************************************************************************************************
/** 				OFCustomPrepare */
//***********************************************************************************************************
sig OFCustomPrepare extends OFPrepare {prepareDestination: one Location}
fact {all x: OFCustomPrepare | x.prepareDestination in x.inputs}
fact {all x: OFCustomPrepare | x.prepareDestination in x.outputs}
//***********************************************************************************************************
/** 				OFServe */
//***********************************************************************************************************
sig OFServe extends Serve {servedFoodItem: one FoodItem}
fact {all x: OFServe | x.servedFoodItem in x.inputs}
fact {all x: OFServe | x.servedFoodItem in x.outputs}
//***********************************************************************************************************
/** 				OFCustomServe */
//***********************************************************************************************************
sig OFCustomServe extends OFServe {serviceDestination: one Location}
fact {all x: OFCustomServe | x.serviceDestination in x.inputs}
//***********************************************************************************************************
/** 				OFEat */
//***********************************************************************************************************
sig OFEat extends Eat {eatenItem: one FoodItem}
fact {all x: OFEat | x.eatenItem in x.inputs}
fact {all x: OFEat | no x.outputs}
//***********************************************************************************************************
/** 				OFPay */
//***********************************************************************************************************
sig OFPay extends Pay {
	paidAmount: one Real, 
	paidFoodItem: one FoodItem}
fact {all x: OFPay | x.paidAmount in x.inputs}
fact {all x: OFPay | x.paidFoodItem in x.inputs}
fact {all x: OFPay | x.paidFoodItem in x.outputs}
//***********************************************************************************************************
/** 				Food Service */
//***********************************************************************************************************
sig FoodService extends Occurrence {
	order: set Order,
	prepare: set Prepare,
	pay: set Pay,
	eat: set Eat,
	serve: set Serve
}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.order, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.prepare, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.serve, x.eat]}
fact {all x: FoodService | x.order + x.prepare + x.pay + x.eat + x.serve in x.steps}
//***********************************************************************************************************
/** 				OFFoodService */
//***********************************************************************************************************
sig OFFoodService extends FoodService {
	disj transferPrepareServe, transferOrderServe, transferServeEat: set TransferBefore
}
/** Constraints on OFFoodService */
// Prevent extraneous Input and Output relations. Probably need to do this at the leaf nodes
//fact {all x: OFFoodService | no x.inputs}
//fact {all x: OFFoodService | no inputs.x}
//fact {all x: OFFoodService | no x.outputs}
//fact {all x: OFFoodService | no outputs.x}
// Constrain relations to their subtypes
fact {all x: OFFoodService | x.order in OFOrder}
fact {all x: OFFoodService | x.prepare in OFPrepare}
fact {all x: OFFoodService | x.pay in OFPay}
fact {all x: OFFoodService | x.eat in OFEat}
fact {all x: OFFoodService | x.serve in OFServe}
// Set Transfers as Steps
fact {all x: OFFoodService | x.transferPrepareServe + x.transferOrderServe + x.transferServeEat in x.steps}
/** Constraints on process steps */
	/** Constraints on order: OFOrder */
	/** Constraints on prepare: OFPrepare */
	/** Constraints on serve: OFServe */
	/** Constraints on eat: OFEat */	
/** Constraints on transfers */
	/** Constraints on the Transfer from prepare to serve */
fact {all x: OFFoodService | bijectionFiltered[sources, x.transferPrepareServe, x.prepare]}
fact {all x: OFFoodService | bijectionFiltered[targets, x.transferPrepareServe, x.serve]}
fact {all x: OFFoodService | subsettingItemRuleForSources[x.transferPrepareServe]}
fact {all x: OFFoodService | subsettingItemRuleForTargets[x.transferPrepareServe]}
	/** Constraints on the Transfer from serve to eat */
fact {all x: OFFoodService | bijectionFiltered[sources, x.transferServeEat, x.serve]}
fact {all x: OFFoodService | bijectionFiltered[targets, x.transferServeEat, x.eat]}
fact {all x: OFFoodService | subsettingItemRuleForSources[x.transferServeEat]}
fact {all x: OFFoodService | subsettingItemRuleForTargets[x.transferServeEat]}
	/** Constraints on the Transfer from order to serve */
fact {all x: OFFoodService | bijectionFiltered[sources, x.transferOrderServe, x.order]}
fact {all x: OFFoodService | bijectionFiltered[targets, x.transferOrderServe, x.serve]}
fact {all x: OFFoodService | subsettingItemRuleForSources[x.transferOrderServe]}
fact {all x: OFFoodService | subsettingItemRuleForTargets[x.transferOrderServe]}
//***********************************************************************************************************
/** 				Object Flow Single Food Service */
//***********************************************************************************************************
sig OFSingleFoodService extends OFFoodService{
	disj transferOrderPrepare, transferOrderPay, transferPayEat: set TransferBefore
}
/** Constraints on OFSingleFoodService */
	// Constrain relations to their subtypes
fact {all x: OFSingleFoodService | x.order in OFCustomOrder}
fact {all x: OFSingleFoodService | x.prepare in OFCustomPrepare}
fact {all x: OFSingleFoodService | x.serve in OFCustomServe}

// Set Transfers as Steps and constrain Steps
fact {all x: OFSingleFoodService | x.transferOrderPrepare + x.transferOrderPay + x.transferPayEat in x.steps}
fact {all x: OFSingleFoodService | x.steps in x.order + x.prepare + x.pay + x.serve + x.eat + x.transferPrepareServe + x.transferOrderServe + x.transferServeEat + x.transferOrderPrepare + x.transferOrderPay + x.transferPayEat}

/** Constraints on process steps */
	/** Constraints on order: OFCustomOrder */
fact {all x: OFSingleFoodService | #x.order = 1}
// Specific
fact {all x: OFSingleFoodService | x.order.outputs in x.order.orderedFoodItem + x.order.orderAmount + x.order.orderDestination}

	/** Constraints on pay: OFPay */
// Specific
fact {all x: OFSingleFoodService | x.pay.inputs in x.pay.paidAmount + x.pay.paidFoodItem}
fact {all x: OFSingleFoodService | x.pay.outputs in x.pay.paidFoodItem}

	/** Constraints on prepare: OFCustomPrepare */
// Specific
fact {all x: OFSingleFoodService | x.prepare.inputs in x.prepare.preparedFoodItem + x.prepare.prepareDestination}
fact {all x: OFSingleFoodService | x.prepare.outputs in x.prepare.preparedFoodItem + x.prepare.prepareDestination}

	/** Constraints on serve: OFCustomServe */
// Specific/
fact {all x: OFSingleFoodService | x.serve.inputs in x.serve.servedFoodItem + x.serve.serviceDestination}
fact {all x: OFSingleFoodService | x.serve.outputs in x.serve.servedFoodItem + x.serve.serviceDestination}

	/** Constraints on eat: OFEat */	
// Specific
fact {all x: OFSingleFoodService | x.eat.inputs in x.eat.eatenItem}

/** Constraints on transfers */
	/** Constraints on the Transfer from order to pay*/
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferOrderPay, x.order]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferOrderPay, x.pay]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferOrderPay]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferOrderPay]}
fact {all x: OFSingleFoodService | x.transferOrderPay.items in x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount}
fact {all x: OFSingleFoodService | x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount in x.transferOrderPay.items}
fact {all x: OFSingleFoodService | x.transferOrderPay.items in x.transferOrderPay.targets.paidFoodItem + x.transferOrderPay.targets.paidAmount}
fact {all x: OFSingleFoodService | x.transferOrderPay.targets.paidFoodItem + x.transferOrderPay.targets.paidAmount in x.transferOrderPay.items}

	/** Constraints on the Transfer from pay to eat */
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferPayEat, x.pay]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferPayEat, x.eat]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferPayEat]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferPayEat]}
fact {all x: OFSingleFoodService | x.transferPayEat.items in x.transferPayEat.sources.paidFoodItem}
fact {all x: OFSingleFoodService | x.transferPayEat.items in x.transferPayEat.targets.eatenItem}
fact {all x: OFSingleFoodService | x.transferPayEat.sources.paidFoodItem in x.transferPayEat.items}
fact {all x: OFSingleFoodService | x.transferPayEat.targets.eatenItem in x.transferPayEat.items}

	/** Constraints on the Transfer from order to serve */
fact {all x: OFSingleFoodService | x.transferOrderServe.items in x.transferOrderServe.sources.orderedFoodItem}
fact {all x: OFSingleFoodService | x.transferOrderServe.items in x.transferOrderServe.targets.servedFoodItem}
fact {all x: OFSingleFoodService | x.transferOrderServe.sources.orderedFoodItem in x.transferOrderServe.items}
fact {all x: OFSingleFoodService | x.transferOrderServe.targets.servedFoodItem in x.transferOrderServe.items}

	/** Constraints on the Transfer from order to prepare*/
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferOrderPrepare, x.order]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferOrderPrepare, x.prepare]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferOrderPrepare]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferOrderPrepare]}
fact {all x: OFSingleFoodService | x.transferOrderPrepare.items in x.transferOrderPrepare.sources.orderedFoodItem + x.transferOrderPrepare.sources.orderDestination}
fact {all x: OFSingleFoodService | x.transferOrderPrepare.items in x.transferOrderPrepare.targets.preparedFoodItem + x.transferOrderPrepare.targets.prepareDestination}
fact {all x: OFSingleFoodService | x.transferOrderPrepare.sources.orderedFoodItem + x.transferOrderPrepare.sources.orderDestination in x.transferOrderPrepare.items}
fact {all x: OFSingleFoodService | x.transferOrderPrepare.targets.preparedFoodItem + x.transferOrderPrepare.targets.prepareDestination in x.transferOrderPrepare.items}

	/** Constraints on the Transfer from prepare to serve */
fact {all x: OFSingleFoodService | x.transferPrepareServe.items in x.transferPrepareServe.sources.preparedFoodItem + x.transferPrepareServe.sources.prepareDestination}
fact {all x: OFSingleFoodService | x.transferPrepareServe.sources.preparedFoodItem + x.transferPrepareServe.sources.prepareDestination in x.transferPrepareServe.items}
fact {all x: OFSingleFoodService | x.transferPrepareServe.items in x.transferPrepareServe.targets.servedFoodItem + x.transferPrepareServe.targets.serviceDestination}
fact {all x: OFSingleFoodService | x.transferPrepareServe.targets.servedFoodItem + x.transferPrepareServe.targets.serviceDestination in x.transferPrepareServe.items}

	/** Constraints on the Transfer from serve to eat */
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
run showOFSingleFoodService{instancesDuringExample} for 15 but exactly 1 OFSingleFoodService
