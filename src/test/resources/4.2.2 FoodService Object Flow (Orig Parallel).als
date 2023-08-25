//***********************************************************************************************************
// Module: 		Food Service Object Flow Parallel
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence/Transfer module to show parallel
//				object flow in Food Service from Wyner (see NISTIR 8283).
// Notes:			The constraints in some of the signature facts for signatures are to prevent uninteresting 
//				and non-meaningful examples. Although some of these relations might be possible or 
//				even desirable in some modeling context, they are not desirable here. For example, 
//				FoodItem, Location, and Real are alll intended as objects to flow between process steps.
//				As such, they don't need to be temporally ordered, or be steps, or have inputs/outputs.
//***********************************************************************************************************
module FoodServiceObjectFlowParallel
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Order, Prepare, Serve, Eat, Pay extends Occurrence {}

sig FoodItem, Location, Real extends Occurrence {}{no this.happensBefore && no happensBefore.this
								&& no this.steps && no this.inputs && no this.outputs}
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
/** 				OFPrepare */
//***********************************************************************************************************
sig OFPrepare extends Prepare { prepareFoodItem: one FoodItem}
{
	prepareFoodItem in this.inputs
	prepareFoodItem in this.outputs
}
//***********************************************************************************************************
/** 				OFCustomPrepare */
//***********************************************************************************************************
sig OFCustomPrepare extends OFPrepare { prepareDestination: one Location}
{
	prepareDestination in this.inputs
	prepareDestination in this.outputs
}
//***********************************************************************************************************
/** 				OFServe */
//***********************************************************************************************************
sig OFServe extends Serve { serveFoodItem: one FoodItem}
{
	serveFoodItem in this.inputs
	serveFoodItem in this.outputs
}
//***********************************************************************************************************
/** 				OFCustomServe */
//***********************************************************************************************************
sig OFCustomServe extends OFServe { serveDestination: one Location}
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
/** 				Object Flow Food Service with Parallelism */
//***********************************************************************************************************
sig OFFoodServiceParallel extends Occurrence{
	order: set OFCustomOrder,
	pay: set OFPay,
	prepare: set OFCustomPrepare,
	serve: set OFCustomServe,
	eat: set OFEat,
	disj transferOrderPrepare, transferPrepareServe, transferServeEat, transferOrderPay, transferPayEat: set TransferBefore
}{
/** Constraints on OFParallelFoodService */
	// The containing activity dose not have any inputs or outputs, and is not the input or output of anything
	no this.inputs and no inputs.this
	no this.outputs and no outputs.this

	// Set new Steps and constrain Steps
	order + pay + prepare + serve + eat + transferOrderPrepare + transferPrepareServe 
	+ transferServeEat + transferOrderPay + transferPayEat in this.steps
	this.steps in order + pay + prepare + serve + eat + transferOrderPrepare + 
	transferPrepareServe + transferServeEat + transferOrderPay + transferPayEat

/** Constraints on process steps */
	/** Constraints on order: OFCustomOrder */
	#order = 2
	// Close the model: bound the outputs of order
	order.outputs in order.orderedFoodItem + order.orderAmount + order.orderDestination
	// Ensure each instance of order can only process one instance of each flowing object type and vice versa
	bijectionFiltered[outputs, order, FoodItem]
	bijectionFiltered[outputs, order, Real]
	bijectionFiltered[outputs, order, Location]

	/** Constraints on pay: OFPay */
	// Close the model: bound the inputs and outputs of pay
	pay.inputs in pay.paidAmount + pay.paidFoodItem
	pay.outputs in pay.paidFoodItem
	// Ensure each instance of pay can only process one instance of each flowing object type and vice versa
	bijectionFiltered[inputs, pay, Real]
	bijectionFiltered[inputs, pay, FoodItem]
	bijectionFiltered[outputs, pay, FoodItem]
//	functionFiltered[outputs, pay, FoodItem]	// Changed this. Not sure why it was a function. May have to change back.

	/** Constraints on prepare: OFCustomPrepare */
	// Close the model: bound the inputs and outputs of prepare
	prepare.inputs in prepare.prepareFoodItem + prepare.prepareDestination
	prepare.outputs in prepare.prepareFoodItem + prepare.prepareDestination
	// Ensure each instance of prepare can only process one instance of each flowing object type and vice versa
	bijectionFiltered[inputs, prepare, FoodItem]
	bijectionFiltered[inputs, prepare, Location]
	bijectionFiltered[outputs, prepare, FoodItem]
	bijectionFiltered[outputs, prepare, FoodItem]

	/** Constraints on serve: OFCustomServe */
	// Close the model: bound the inputs and outputs of prepareAndServe
	serve.inputs in serve.serveFoodItem + serve.serveDestination
	serve.outputs in serve.serveFoodItem
	// Ensure each instance of prepareAndServe can only process one instance of each flowing object type and vice versa
	bijectionFiltered[inputs, serve, FoodItem]
	bijectionFiltered[inputs, serve, Location]
	bijectionFiltered[outputs, serve, FoodItem]

	/** Constraints on eat: OFEat */
	// Close the model: bound the inputs of eat
	eat.inputs in eat.eatenItem
	// Ensure each instance of eat can only process one instance of each flowing object type and vice versa
	bijectionFiltered[inputs, eat, FoodItem]

/** Constraints on transfers */
	/** Constraints on the Transfer from order to pay*/
	bijectionFiltered[sources, transferOrderPay, order]
	bijectionFiltered[targets, transferOrderPay, pay]
	subsettingItemRuleForSources[transferOrderPay]
	subsettingItemRuleForTargets[transferOrderPay]
	// Close the model: bound the Transfer's Item relation to specific properties (relations) in the Source and Target
	//	with a two-way subsetting
	transferOrderPay.items in transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount
	transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount in transferOrderPay.items
	transferOrderPay.items in transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount
	transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount in transferOrderPay.items

	/** Constraints on the Transfer from pay to eat */
	bijectionFiltered[sources, transferPayEat, pay]
	bijectionFiltered[targets, transferPayEat, eat]
	subsettingItemRuleForSources[transferPayEat]
	subsettingItemRuleForTargets[transferPayEat]
	// Close the model: bound the Transfer's Item relation to specific properties (relations) in the Source and Target
	//	with a two-way subsetting
	transferPayEat.items in transferPayEat.sources.paidFoodItem
	transferPayEat.items in transferPayEat.targets.eatenItem
	transferPayEat.sources.paidFoodItem in transferPayEat.items
	transferPayEat.targets.eatenItem in transferPayEat.items

	/** Constraints on the Transfer from order to prepare */
	bijectionFiltered[sources, transferOrderPrepare, order]
	bijectionFiltered[targets, transferOrderPrepare, prepare]
	subsettingItemRuleForSources[transferOrderPrepare]
	subsettingItemRuleForTargets[transferOrderPrepare]
	transferOrderPrepare.items in transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination
	transferOrderPrepare.items in transferOrderPrepare.targets.prepareFoodItem + transferOrderPrepare.targets.prepareDestination
	transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination in transferOrderPrepare.items
	transferOrderPrepare.targets.prepareFoodItem + transferOrderPrepare.targets.prepareDestination in transferOrderPrepare.items

/** Constraints on the Transfer from prepare to serve */
	bijectionFiltered[sources, transferPrepareServe, prepare]
	bijectionFiltered[targets, transferPrepareServe, serve]
	subsettingItemRuleForSources[transferPrepareServe]
	subsettingItemRuleForTargets[transferPrepareServe]
	transferPrepareServe.items in transferPrepareServe.sources.prepareFoodItem + transferPrepareServe.sources.prepareDestination
	transferPrepareServe.items in transferPrepareServe.targets.serveFoodItem + transferPrepareServe.targets.serveDestination
	transferPrepareServe.sources.prepareFoodItem + transferPrepareServe.sources.prepareDestination in transferPrepareServe.items
	transferPrepareServe.targets.serveFoodItem + transferPrepareServe.targets.serveDestination in transferPrepareServe.items

	/** Constraints on the Transfer from prepareAndServe to eat TBD*/
	bijectionFiltered[sources, transferServeEat, serve]
	bijectionFiltered[targets, transferServeEat, eat]
	subsettingItemRuleForSources[transferServeEat]
	subsettingItemRuleForTargets[transferServeEat]
	// Close the model: bound the Transfer's Item relation to specific properties (relations) in the Source and Target
	//	with a two-way subsetting
	transferServeEat.items in transferServeEat.sources.serveFoodItem
	transferServeEat.items in transferServeEat.targets.eatenItem
	transferServeEat.sources.serveFoodItem in transferServeEat.items
	transferServeEat.targets.eatenItem in transferServeEat.items
}
//***********************************************************************************************************
/** 			General Functions and Predicates */
//***********************************************************************************************************
//pred suppressTransfers {no Transfer}
//pred suppressIO {no inputs and no outputs}
pred instancesDuringExample{Order in OFFoodServiceParallel.order && Prepare in OFFoodServiceParallel.prepare &&
		Serve in OFFoodServiceParallel.serve && Eat in OFFoodServiceParallel.eat && Pay in OFFoodServiceParallel.pay}
//***********************************************************************************************************
/** 				Checks and Runs */
//***********************************************************************************************************
run showOFFoodServiceParallel{nonZeroDurationOnly && instancesDuringExample} for 27 but exactly 1 OFFoodServiceParallel, 5 int


