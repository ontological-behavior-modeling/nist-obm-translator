//***********************************************************************************************************
// Module: 		Food Service Loop Object Flow
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence/Transfer module to show looping
//				object flow in vFood Service from Wyner (see NISTIR 8283).
// Notes:			The constraints in some of the signature facts for signatures are to prevent uninteresting 
//				and non-meaningful examples. Although some of these relations might be possible or 
//				even desirable in some modeling context, they are not desirable here. For example, 
//				FoodItem, Location, and Real are alll intended as objects to flow between process steps.
//				As such, they don't need to be temporally ordered, or be steps, or have inputs/outputs.
//***********************************************************************************************************
module FoodServiceLoopObjectFlo
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Order, PrepareAndServe, Eat, Pay extends Occurrence {}

sig FoodItem, Location, Real extends Occurrence {}{no this.happensBefore && no happensBefore.this
								&& no this.steps && no this.inputs && no this.outputs}

sig OFStart, OFEnd extends Occurrence {}{no inputs.this && no outputs.this && no items.this}
//***********************************************************************************************************
/** 				OFOrder */
//***********************************************************************************************************
sig OFOrder extends Order {orderedFoodItem: one FoodItem}
{
	no this.inputs
	orderedFoodItem in this.outputs
}
//***********************************************************************************************************
/** 				OFCustomOrder */
//***********************************************************************************************************
sig OFCustomOrder extends OFOrder {
	orderAmount: one Real, 
	orderDestination: one Location
}{
	orderAmount in this.outputs
	orderDestination in this.outputs
}
//***********************************************************************************************************
/** 				OFPrepareAndServe */
//***********************************************************************************************************
sig OFPrepareAndServe extends PrepareAndServe { prepareAndServeFoodItem: one FoodItem}
{
	prepareAndServeFoodItem in this.inputs
	prepareAndServeFoodItem in this.outputs
}
//***********************************************************************************************************
/** 				OFCustomPrepareAndServe */
//***********************************************************************************************************
sig OFCustomPrepareAndServe extends OFPrepareAndServe { serveDestination: one Location}
{
	serveDestination in this.inputs
}
//***********************************************************************************************************
/** 				OFEat */
//***********************************************************************************************************
sig OFEat extends Eat {eatenItem: one FoodItem}
{
	eatenItem in this.inputs
	no this.outputs
}
//***********************************************************************************************************
/** 				OFPay */
//***********************************************************************************************************
sig OFPay extends Pay {
	paidAmount: one Real, 
	paidFoodItem: one FoodItem}
{
	paidAmount in this.inputs
	paidFoodItem in this.inputs
	paidFoodItem in this.outputs
}


//***********************************************************************************************************
/** 				Object Flow Food Service with Loop */
//***********************************************************************************************************
sig OFLoopFoodService extends Occurrence{
	start: one OFStart,
	end: one OFEnd,
	order: set OFCustomOrder,
	pay: set OFPay,
	prepareAndServe: set OFCustomPrepareAndServe,
	eat: set OFEat,
	disj transferOrderPrepareAndServe, transferPrepareAndServeEat, transferOrderPay, transferPayEat: set TransferBefore
}{
/** Constraints on OFSingleFoodService */
	no this.inputs and no inputs.this
	no this.outputs and no outputs.this

	// Set new Steps and constrain Steps
	start + end + order + pay + prepareAndServe + eat + transferOrderPrepareAndServe 
	+ transferPrepareAndServeEat + transferOrderPay + transferPayEat in this.steps
	this.steps in start + end + order + pay + prepareAndServe + eat + transferOrderPrepareAndServe
	+ transferPrepareAndServeEat + transferOrderPay + transferPayEat

/** Constraints on process steps */
	/** Constraints on start: OFStart */
	#start = 1
	functionFiltered[happensBefore, start, order]

	/** Constraints on order: OFCustomOrder */
	#order = 2
	order.outputs in order.orderedFoodItem + order.orderAmount + order.orderDestination
	bijectionFiltered[outputs, order, FoodItem]
	bijectionFiltered[outputs, order, Real]
	bijectionFiltered[outputs, order, Location]
	inverseFunctionFiltered[happensBefore, start + eat, order]

	/** Constraints on pay: OFPay */
	// Close the model: bound the inputs and outputs of pay
	pay.inputs in pay.paidAmount + pay.paidFoodItem
	pay.outputs in pay.paidFoodItem
	// Ensure each instance of pay can only process one instance of each flowing object type and vice versa
	bijectionFiltered[inputs, pay, Real]
	bijectionFiltered[inputs, pay, FoodItem]
	bijectionFiltered[outputs, pay, FoodItem]
//	functionFiltered[outputs, pay, FoodItem]	// Changed this. Not sure why it was a function. May have to change back.

	/** Constraints on prepare: OFCustomPrepareAndServe */
	// Close the model: bound the inputs and outputs of prepareAndServe
	prepareAndServe.inputs in prepareAndServe.prepareAndServeFoodItem + prepareAndServe.serveDestination
	prepareAndServe.outputs in prepareAndServe.prepareAndServeFoodItem
	// Ensure each instance of prepareAndServe can only process one instance of each flowing object type and vice versa
	bijectionFiltered[inputs, prepareAndServe, FoodItem]
	bijectionFiltered[inputs, prepareAndServe, Location]
	bijectionFiltered[outputs, prepareAndServe, FoodItem]

	/** Constraints on eat: OFEat */
	// Close the model: bound the inputs of eat
	eat.inputs in eat.eatenItem
	// Ensure each instance of eat can only process one instance of each flowing object type and vice versa
	bijectionFiltered[inputs, eat, FoodItem]
	// Temporal flow
	functionFiltered[happensBefore, eat, end + order]

	/** Constraints on end: OFEnd */
	#end = 1
	inverseFunctionFiltered[happensBefore, eat, end]

/** Constraints on transfers */
	/** Constraints on the Transfer from order to pay*/
	bijectionFiltered[sources, transferOrderPay, order]
	bijectionFiltered[targets, transferOrderPay, pay]
	subsettingItemRuleForSources[transferOrderPay]
	subsettingItemRuleForTargets[transferOrderPay]
	transferOrderPay.items in transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount
	transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount in transferOrderPay.items
	transferOrderPay.items in transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount
	transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount in transferOrderPay.items

	/** Constraints on the Transfer from pay to eat */
	bijectionFiltered[sources, transferPayEat, pay]
	bijectionFiltered[targets, transferPayEat, eat]
	subsettingItemRuleForSources[transferPayEat]
	subsettingItemRuleForTargets[transferPayEat]
	transferPayEat.items in transferPayEat.sources.paidFoodItem
	transferPayEat.items in transferPayEat.targets.eatenItem
	transferPayEat.sources.paidFoodItem in transferPayEat.items
	transferPayEat.targets.eatenItem in transferPayEat.items

	/** Constraints on the Transfer from order to prepareAndServe*/
	bijectionFiltered[sources, transferOrderPrepareAndServe, order]
	bijectionFiltered[targets, transferOrderPrepareAndServe, prepareAndServe]
	subsettingItemRuleForSources[transferOrderPrepareAndServe]
	subsettingItemRuleForTargets[transferOrderPrepareAndServe]
	transferOrderPrepareAndServe.items in transferOrderPrepareAndServe.sources.orderedFoodItem + transferOrderPrepareAndServe.sources.orderDestination
	transferOrderPrepareAndServe.items in transferOrderPrepareAndServe.targets.prepareAndServeFoodItem + transferOrderPrepareAndServe.targets.serveDestination
	transferOrderPrepareAndServe.sources.orderedFoodItem + transferOrderPrepareAndServe.sources.orderDestination in transferOrderPrepareAndServe.items
	transferOrderPrepareAndServe.targets.prepareAndServeFoodItem + transferOrderPrepareAndServe.targets.serveDestination in transferOrderPrepareAndServe.items

	/** Constraints on the Transfer from prepareAndServe to eat */
	bijectionFiltered[sources, transferPrepareAndServeEat, prepareAndServe]
	bijectionFiltered[targets, transferPrepareAndServeEat, eat]
	subsettingItemRuleForSources[transferPrepareAndServeEat]
	subsettingItemRuleForTargets[transferPrepareAndServeEat]
	transferPrepareAndServeEat.items in transferPrepareAndServeEat.sources.prepareAndServeFoodItem
	transferPrepareAndServeEat.items in transferPrepareAndServeEat.targets.eatenItem
	transferPrepareAndServeEat.sources.prepareAndServeFoodItem in transferPrepareAndServeEat.items
	transferPrepareAndServeEat.targets.eatenItem in transferPrepareAndServeEat.items
}
//***********************************************************************************************************
/** 			General Functions and Predicates */
//***********************************************************************************************************
//pred suppressTransfers {no Transfer}
//pred suppressIO {no inputs and no outputs}
pred instancesDuringExample{Order in OFLoopFoodService.order && PrepareAndServe in OFLoopFoodService.prepareAndServe &&
		Eat in OFLoopFoodService.eat && Pay in OFLoopFoodService.pay}
//***********************************************************************************************************
/** 				Checks and Runs */
//***********************************************************************************************************
run showOFLoopFoodService{nonZeroDurationOnly && instancesDuringExample} for 25 but exactly 1 OFLoopFoodService, 5 int


