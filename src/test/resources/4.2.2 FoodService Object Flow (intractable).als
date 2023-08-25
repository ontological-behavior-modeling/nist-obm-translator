//***********************************************************************************************************
// Module: 		Food Service
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence/Transfer module to show
//				object flow in various kinds of Food Service from Wyner (see NISTIR 8283).
// Notes:			The constraints in some of the signature facts for signatures are to prevent uninteresting 
//				and non-meaningful examples. Although some of these relations might be possible or 
//				even desirable in some modeling context, they are not desirable here. For example, 
//				FoodItem, Location, and Real are alll intended as objects to flow between process steps.
//				As such, they don't need to be temporally ordered, or be steps, or have inputs/outputs.
//***********************************************************************************************************
module FoodService
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Order, Prepare, Serve, Eat, Pay extends Occurrence {}

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
/** 				OFPrepare */
//***********************************************************************************************************
sig OFPrepare extends Prepare {preparedFoodItem: one FoodItem}
{
	preparedFoodItem in this.inputs
	preparedFoodItem in this.outputs
}
//***********************************************************************************************************
/** 				OFCustomPrepare */
//***********************************************************************************************************
sig OFCustomPrepare extends OFPrepare {prepareDestination: one Location}
{
	prepareDestination in this.inputs
	prepareDestination in this.outputs
}
//***********************************************************************************************************
/** 				OFServe */
//***********************************************************************************************************
sig OFServe extends Serve {servedFoodItem: one FoodItem}
{
	servedFoodItem in this.inputs
	servedFoodItem in this.outputs
}
//***********************************************************************************************************
/** 				OFCustomServe */
//***********************************************************************************************************
sig OFCustomServe extends OFServe {serviceDestination: one Location}
{
	serviceDestination in this.inputs
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
/** 				Food Service */
//***********************************************************************************************************
sig FoodService extends Occurrence {
	order: set Order,
	prepare: set Prepare,
	pay: set Pay,
	eat: set Eat,
	serve: set Serve
}{
	bijectionFiltered[happensBefore, order, serve]
	bijectionFiltered[happensBefore, prepare, serve]
	bijectionFiltered[happensBefore, serve, eat]

	order + prepare + pay + eat + serve in this.steps
}
//***********************************************************************************************************
/** 				OFFoodService */
//***********************************************************************************************************
sig OFFoodService extends FoodService {
	disj transferPrepareServe, transferOrderServe, transferServeEat: set TransferBefore
}{
/** Constraints on OFFoodService */
	// Prevent extraneous Input and Output relations. (might be able to relax in the presence of other constraints)
	no this.inputs and no inputs.this
	no this.outputs and no outputs.this

	// Constrain relations to their subtypes
	order in OFOrder
	prepare in OFPrepare
	pay in OFPay
	eat in OFEat
	serve in OFServe

	// Set Transfers as Steps
	transferPrepareServe + transferOrderServe + transferServeEat in this.steps

/** Constraints on process steps */
	/** Constraints on order: OFOrder */

	/** Constraints on prepare: OFPrepare */

	/** Constraints on serve: OFServe */

	/** Constraints on eat: OFEat */	

/** Constraints on transfers */
	/** Constraints on the Transfer from prepare to serve */
	bijectionFiltered[sources, transferPrepareServe, prepare]
	bijectionFiltered[targets, transferPrepareServe, serve]
	subsettingItemRuleForSources[transferPrepareServe]
	subsettingItemRuleForTargets[transferPrepareServe]

	/** Constraints on the Transfer from serve to eat */
	bijectionFiltered[sources, transferServeEat, serve]
	bijectionFiltered[targets, transferServeEat, eat]
	subsettingItemRuleForSources[transferServeEat]
	subsettingItemRuleForTargets[transferServeEat]

	/** Constraints on the Transfer from order to serve */
	bijectionFiltered[sources, transferOrderServe, order]
	bijectionFiltered[targets, transferOrderServe, serve]
	subsettingItemRuleForSources[transferOrderServe]
	subsettingItemRuleForTargets[transferOrderServe]
}
//***********************************************************************************************************
/** 				Object Flow Single Food Service */
//***********************************************************************************************************
sig OFSingleFoodService extends OFFoodService{
	disj transferOrderPrepare, transferOrderPay, transferPayEat: set TransferBefore
}{
/** Constraints on OFSingleFoodService */
	// Constrain relations to their subtypes
	order in OFCustomOrder
	prepare in OFCustomPrepare
	serve in OFCustomServe

	// Set Transfers as Steps and constrain Steps
	transferOrderPrepare + transferOrderPay + transferPayEat in this.steps
	this.steps in order + prepare + pay + serve + eat + transferPrepareServe + transferOrderServe
		 + transferServeEat + transferOrderPrepare + transferOrderPay + transferPayEat

/** Constraints on process steps */
	/** Constraints on order: OFCustomOrder */
	#order = 1
	order.outputs in order.orderedFoodItem + order.orderAmount + order.orderDestination

	/** Constraints on pay: OFPay */
	pay.inputs in pay.paidAmount + pay.paidFoodItem
	pay.outputs in pay.paidFoodItem	

	/** Constraints on prepare: OFCustomPrepare */
	prepare.inputs in prepare.preparedFoodItem + prepare.prepareDestination
	prepare.outputs in prepare.preparedFoodItem + prepare.prepareDestination

	/** Constraints on serve: OFCustomServe */
	serve.inputs in serve.servedFoodItem + serve.serviceDestination
	serve.outputs in serve.servedFoodItem + serve.serviceDestination

	/** Constraints on eat: OFEat */	
	eat.inputs in eat.eatenItem

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

	/** Constraints on the Transfer from order to serve */
	transferOrderServe.items in transferOrderServe.sources.orderedFoodItem
	transferOrderServe.items in transferOrderServe.targets.servedFoodItem
	transferOrderServe.sources.orderedFoodItem in transferOrderServe.items 
	transferOrderServe.targets.servedFoodItem in transferOrderServe.items 

	/** Constraints on the Transfer from order to prepare*/
	bijectionFiltered[sources, transferOrderPrepare, order]
	bijectionFiltered[targets, transferOrderPrepare, prepare]
	subsettingItemRuleForSources[transferOrderPrepare]
	subsettingItemRuleForTargets[transferOrderPrepare]
	transferOrderPrepare.items in transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination
	transferOrderPrepare.items in transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination
	transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination in transferOrderPrepare.items
	transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination in transferOrderPrepare.items

	/** Constraints on the Transfer from prepare to serve */
	transferPrepareServe.items in transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination
	transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination in transferPrepareServe.items
	transferPrepareServe.items in transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination
	transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination in transferPrepareServe.items

	/** Constraints on the Transfer from serve to eat */
	transferServeEat.items in transferServeEat.sources.servedFoodItem
	transferServeEat.items in transferServeEat.targets.eatenItem
	transferServeEat.sources.servedFoodItem in transferServeEat.items
	transferServeEat.targets.eatenItem in transferServeEat.items
}
//***********************************************************************************************************
/** 				Object Flow Food Service with Loop */
//***********************************************************************************************************
sig OFLoopFoodService extends OFFoodService{
	start: one OFStart,
	end: one OFEnd,
	disj transferOrderPrepare, transferOrderPay, transferPayEat: set TransferBefore
}{
/** Constraints on OFSingleFoodService */
	// Constrain relations to their subtypes
	order in OFCustomOrder
	prepare in OFCustomPrepare
	serve in OFCustomServe

	// Set new Steps and constrain Steps
	start + end + transferOrderPrepare + transferOrderPay + transferPayEat in this.steps
	this.steps in order + prepare + pay + serve + eat + start + end + transferPrepareServe 
	+ transferOrderServe	 + transferServeEat + transferOrderPrepare + transferOrderPay + 
	transferPayEat

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
	pay.inputs in pay.paidAmount + pay.paidFoodItem
	bijectionFiltered[inputs, pay, Real]
	bijectionFiltered[inputs, pay, FoodItem]
	pay.outputs in pay.paidFoodItem
	functionFiltered[outputs, pay, FoodItem]

	/** Constraints on prepare: OFCustomPrepare */
	prepare.inputs in prepare.preparedFoodItem + prepare.prepareDestination
	bijectionFiltered[inputs, prepare, FoodItem]
	bijectionFiltered[inputs, prepare, Location]
	prepare.outputs in prepare.preparedFoodItem + prepare.prepareDestination
	bijectionFiltered[outputs, prepare, FoodItem]
	bijectionFiltered[outputs, prepare, Location]

	/** Constraints on serve: OFCustomServe */
	serve.inputs in serve.servedFoodItem + serve.serviceDestination
	bijectionFiltered[inputs, serve, FoodItem]
	bijectionFiltered[inputs, serve, Location]
	serve.outputs in serve.servedFoodItem + serve.serviceDestination
	bijectionFiltered[outputs, serve, Location]
	bijectionFiltered[outputs, serve, FoodItem]

	/** Constraints on eat: OFEat */	
	eat.inputs in eat.eatenItem
	bijectionFiltered[inputs, eat, FoodItem] 
	functionFiltered[happensBefore, eat, end + order]

	/** Constraints on end: OFEnd */
	#end = 1
//	inverseFunctionFiltered[happensBefore, eat, end]

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

	/** Constraints on the Transfer from order to serve */
	transferOrderServe.items in transferOrderServe.sources.orderedFoodItem
	transferOrderServe.items in transferOrderServe.targets.servedFoodItem
	transferOrderServe.sources.orderedFoodItem in transferOrderServe.items 
	transferOrderServe.targets.servedFoodItem in transferOrderServe.items 

	/** Constraints on the Transfer from order to prepare*/
	bijectionFiltered[sources, transferOrderPrepare, order]
	bijectionFiltered[targets, transferOrderPrepare, prepare]
	subsettingItemRuleForSources[transferOrderPrepare]
	subsettingItemRuleForTargets[transferOrderPrepare]
	transferOrderPrepare.items in transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination
	transferOrderPrepare.items in transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination
	transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination in transferOrderPrepare.items
	transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination in transferOrderPrepare.items

	/** Constraints on the Transfer from prepare to serve */
	transferPrepareServe.items in transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination
	transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination in transferPrepareServe.items
	transferPrepareServe.items in transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination
	transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination in transferPrepareServe.items

	/** Constraints on the Transfer from serve to eat */
	transferServeEat.items in transferServeEat.sources.servedFoodItem
	transferServeEat.items in transferServeEat.targets.eatenItem
	transferServeEat.sources.servedFoodItem in transferServeEat.items
	transferServeEat.targets.eatenItem in transferServeEat.items
}
//***********************************************************************************************************
/** 				Object Flow Food Service with Parallelism */
//***********************************************************************************************************
sig OFParallelFoodService extends OFFoodService{
	disj transferOrderPrepare, transferOrderPay, transferPayEat: set TransferBefore
}{
/** Constraints on OFSingleFoodService */
	// Constrain relations to their subtypes
	order in OFCustomOrder
	prepare in OFCustomPrepare
	serve in OFCustomServe

	// Set Transfers as Steps and constrain Steps
	transferOrderPrepare + transferOrderPay + transferPayEat in this.steps
	this.steps in order + prepare + pay + serve + eat + transferPrepareServe + transferOrderServe
		 + transferServeEat + transferOrderPrepare + transferOrderPay + transferPayEat

/** Constraints on process steps */
	/** Constraints on order: OFCustomOrder */
	#order = 2
	order.outputs in order.orderedFoodItem + order.orderAmount + order.orderDestination
	bijectionFiltered[outputs, order, FoodItem]
	bijectionFiltered[outputs, order, Real]
	bijectionFiltered[outputs, order, Location]

	/** Constraints on pay: OFPay */
	pay.inputs in pay.paidAmount + pay.paidFoodItem
	bijectionFiltered[inputs, pay, Real]
	bijectionFiltered[inputs, pay, FoodItem]
	pay.outputs in pay.paidFoodItem
	functionFiltered[outputs, pay, FoodItem]

	/** Constraints on prepare: OFCustomPrepare */
	prepare.inputs in prepare.preparedFoodItem + prepare.prepareDestination
	bijectionFiltered[inputs, prepare, FoodItem]
	bijectionFiltered[inputs, prepare, Location]
	prepare.outputs in prepare.preparedFoodItem + prepare.prepareDestination
	bijectionFiltered[outputs, prepare, FoodItem]
	bijectionFiltered[outputs, prepare, Location]

	/** Constraints on serve: OFCustomServe */
	serve.inputs in serve.servedFoodItem + serve.serviceDestination
	bijectionFiltered[inputs, serve, FoodItem]
	bijectionFiltered[inputs, serve, Location]
	serve.outputs in serve.servedFoodItem + serve.serviceDestination
	bijectionFiltered[outputs, serve, Location]
	bijectionFiltered[outputs, serve, FoodItem]

	/** Constraints on eat: OFEat */	
	eat.inputs in eat.eatenItem
	bijectionFiltered[inputs, eat, FoodItem] 

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

	/** Constraints on the Transfer from order to serve */
	transferOrderServe.items in transferOrderServe.sources.orderedFoodItem
	transferOrderServe.items in transferOrderServe.targets.servedFoodItem
	transferOrderServe.sources.orderedFoodItem in transferOrderServe.items 
	transferOrderServe.targets.servedFoodItem in transferOrderServe.items 

	/** Constraints on the Transfer from order to prepare*/
	bijectionFiltered[sources, transferOrderPrepare, order]
	bijectionFiltered[targets, transferOrderPrepare, prepare]
	subsettingItemRuleForSources[transferOrderPrepare]
	subsettingItemRuleForTargets[transferOrderPrepare]
	transferOrderPrepare.items in transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination
	transferOrderPrepare.items in transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination
	transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination in transferOrderPrepare.items
	transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination in transferOrderPrepare.items

	/** Constraints on the Transfer from prepare to serve */
	transferPrepareServe.items in transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination
	transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination in transferPrepareServe.items
	transferPrepareServe.items in transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination
	transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination in transferPrepareServe.items

	/** Constraints on the Transfer from serve to eat */
	transferServeEat.items in transferServeEat.sources.servedFoodItem
	transferServeEat.items in transferServeEat.targets.eatenItem
	transferServeEat.sources.servedFoodItem in transferServeEat.items
	transferServeEat.targets.eatenItem in transferServeEat.items
}
//***********************************************************************************************************
/** 			General Functions and Predicates */
//***********************************************************************************************************
//pred suppressTransfers {no Transfer}
//pred suppressIO {no inputs and no outputs}
pred instancesDuringExample{Order in OFFoodService.order && Prepare in OFFoodService.prepare
		&& Serve in OFFoodService.serve && Eat in OFFoodService.eat && Pay in OFFoodService.pay}
pred onlyOFFoodService {FoodService in OFFoodService && noChildFoodService && #OFFoodService = 1 && noCustomFoodService}
pred onlyOFSingleFoodService {FoodService in OFSingleFoodService && noChildFoodService}
pred onlyOFLoopFoodService {FoodService in OFLoopFoodService}
pred onlyOFParallelFoodService {FoodService in OFParallelFoodService}
pred noCustomFoodService {no OFCustomOrder && no OFCustomPrepare && no OFCustomServe}
pred noChildFoodService {no OFLoopFoodService && no OFParallelFoodService}
//***********************************************************************************************************
/** 				Checks and Runs */
//***********************************************************************************************************
run showOFFoodService{nonZeroDurationOnly && instancesDuringExample && onlyOFFoodService && #OFFoodService.order = 1} for 12
run showOFSingleFoodService{nonZeroDurationOnly && instancesDuringExample && onlyOFSingleFoodService} for 15 but exactly 1 OFSingleFoodService
run showOFLoopFoodService{nonZeroDurationOnly && instancesDuringExample && onlyOFLoopFoodService} for 30 but exactly 1 OFLoopFoodService
run showOFParallelFoodService{nonZeroDurationOnly && instancesDuringExample && onlyOFParallelFoodService} for 27 but exactly 1 OFParallelFoodService
