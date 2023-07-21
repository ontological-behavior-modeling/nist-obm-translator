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
module FoodService
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Order, Prepare, Serve, Eat, Pay extends Occurrence {}

sig FoodItem, Location, Real extends Occurrence {}{
	// no this.happensBefore && no happensBefore.this && no this.steps && no this.inputs && no this.outputs
}

sig OFStart, OFEnd extends Occurrence {}{
	// no inputs.this && no outputs.this && no items.this
}
//***********************************************************************************************************
/** 				OFOrder */
//***********************************************************************************************************
sig OFOrder extends Order {orderedFoodItem: one FoodItem}
{
	// no this.inputs
	// orderedFoodItem in this.outputs
}
//***********************************************************************************************************
/** 				OFCustomOrder */
//***********************************************************************************************************
sig OFCustomOrder extends OFOrder {
	orderAmount: one Real, 
	orderDestination: one Location
}{
	// orderAmount in this.outputs
	// orderDestination in this.outputs
}
//***********************************************************************************************************
/** 				OFPrepare */
//***********************************************************************************************************
sig OFPrepare extends Prepare {
	preparedFoodItem: one FoodItem
}{
	// preparedFoodItem in this.inputs
	// preparedFoodItem in this.outputs
}
//***********************************************************************************************************
/** 				OFCustomPrepare */
//***********************************************************************************************************
sig OFCustomPrepare extends OFPrepare {
	prepareDestination: one Location
}{
	// prepareDestination in this.inputs
	// prepareDestination in this.outputs
}
//***********************************************************************************************************
/** 				OFServe */
//***********************************************************************************************************
sig OFServe extends Serve {
	servedFoodItem: one FoodItem
}{
	// servedFoodItem in this.inputs
	// servedFoodItem in this.outputs
}
//***********************************************************************************************************
/** 				OFCustomServe */
//***********************************************************************************************************
sig OFCustomServe extends OFServe {
	serviceDestination: one Location
}{
	// serviceDestination in this.inputs
}
//***********************************************************************************************************
/** 				OFEat */
//***********************************************************************************************************
sig OFEat extends Eat {
	eatenItem: one FoodItem
}{
	// eatenItem in this.inputs
	// no this.outputs
}
//***********************************************************************************************************
/** 				OFPay */
//***********************************************************************************************************
sig OFPay extends Pay {
	paidAmount: one Real, 
	paidFoodItem: one FoodItem
}{
	// paidAmount in this.inputs
	// paidFoodItem in this.inputs
	// paidFoodItem in this.outputs
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
	// bijectionFiltered[happensBefore, order, serve]
	// bijectionFiltered[happensBefore, prepare, serve]
	// bijectionFiltered[happensBefore, serve, eat]
	// order + prepare + pay + eat + serve in this.steps
}
//***********************************************************************************************************
/** 				OFFoodService */
//***********************************************************************************************************
sig OFFoodService extends FoodService {
	disj transferPrepareServe, transferOrderServe, transferServeEat: set TransferBefore
}{
/** Constraints on OFFoodService */
	// Prevent extraneous Input and Output relations. (might be able to relax in the presence of other constraints)
	// no this.inputs and no inputs.this
	// no this.outputs and no outputs.this

	// Constrain relations to their subtypes
	// order in OFOrder
	// prepare in OFPrepare
	// pay in OFPay
	// eat in OFEat
	// serve in OFServe

	// Set Transfers as Steps
	// transferPrepareServe + transferOrderServe + transferServeEat in this.steps

/** Constraints on process steps */
	/** Constraints on order: OFOrder */

	/** Constraints on prepare: OFPrepare */

	/** Constraints on serve: OFServe */

	/** Constraints on eat: OFEat */	

/** Constraints on transfers */
	/** Constraints on the Transfer from prepare to serve */
	// bijectionFiltered[sources, transferPrepareServe, prepare]
	// bijectionFiltered[targets, transferPrepareServe, serve]
	// subsettingItemRuleForSources[transferPrepareServe]
	// subsettingItemRuleForTargets[transferPrepareServe]

	/** Constraints on the Transfer from serve to eat */
	// bijectionFiltered[sources, transferServeEat, serve]
	// bijectionFiltered[targets, transferServeEat, eat]
	// subsettingItemRuleForSources[transferServeEat]
	// subsettingItemRuleForTargets[transferServeEat]

	/** Constraints on the Transfer from order to serve */
	// bijectionFiltered[sources, transferOrderServe, order]
	// bijectionFiltered[targets, transferOrderServe, serve]
	// subsettingItemRuleForSources[transferOrderServe]
	// subsettingItemRuleForTargets[transferOrderServe]
}
//***********************************************************************************************************
/** 				Object Flow Single Food Service */
//***********************************************************************************************************
sig OFSingleFoodService extends OFFoodService{
	disj transferOrderPrepare, transferOrderPay, transferPayEat: set TransferBefore
}{
/** Constraints on OFSingleFoodService */
	// Constrain relations to their subtypes
// 	order in OFCustomOrder
// 	prepare in OFCustomPrepare
// 	serve in OFCustomServe

// 	// Set Transfers as Steps and constrain Steps
// 	transferOrderPrepare + transferOrderPay + transferPayEat in this.steps
// 	this.steps in order + prepare + pay + serve + eat + transferPrepareServe + transferOrderServe
// 		 + transferServeEat + transferOrderPrepare + transferOrderPay + transferPayEat

// /** Constraints on process steps */
// 	/** Constraints on order: OFCustomOrder */
// 	#order = 1
// //	// Generic
// //	order.outputs in FoodItem + Real + Location
// 	// Specific
// 	order.outputs in order.orderedFoodItem + order.orderAmount + order.orderDestination

// 	/** Constraints on pay: OFPay */
// //	// Generic
// //	pay.inputs in FoodItem + Real
// //	pay.outputs in FoodItem
// 	// Specific
// 	pay.inputs in pay.paidAmount + pay.paidFoodItem
// 	pay.outputs in pay.paidFoodItem	

// 	/** Constraints on prepare: OFCustomPrepare */
// //	// Generic
// //	prepare.inputs in FoodItem + Location
// //	prepare.outputs in FoodItem + Location
// 	// Specific
// 	prepare.inputs in prepare.preparedFoodItem + prepare.prepareDestination
// 	prepare.outputs in prepare.preparedFoodItem + prepare.prepareDestination

// 	/** Constraints on serve: OFCustomServe */
// //	// Generic
// //	serve.inputs in FoodItem + Location
// //	serve.outputs in FoodItem
// 	// Specific/
// 	serve.inputs in serve.servedFoodItem + serve.serviceDestination
// 	serve.outputs in serve.servedFoodItem + serve.serviceDestination

// 	/** Constraints on eat: OFEat */	
// //	// Generic
// //	eat.inputs in FoodItem
// 	// Specific
// 	eat.inputs in eat.eatenItem

// /** Constraints on transfers */
// 	/** Constraints on the Transfer from order to pay*/
// 	bijectionFiltered[sources, transferOrderPay, order]
// 	bijectionFiltered[targets, transferOrderPay, pay]
// 	subsettingItemRuleForSources[transferOrderPay]
// 	subsettingItemRuleForTargets[transferOrderPay]
// 	transferOrderPay.items in transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount
// 	transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount in transferOrderPay.items
// 	transferOrderPay.items in transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount
// 	transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount in transferOrderPay.items

// 	/** Constraints on the Transfer from pay to eat */
// 	bijectionFiltered[sources, transferPayEat, pay]
// 	bijectionFiltered[targets, transferPayEat, eat]
// 	subsettingItemRuleForSources[transferPayEat]
// 	subsettingItemRuleForTargets[transferPayEat]
// 	transferPayEat.items in transferPayEat.sources.paidFoodItem
// 	transferPayEat.items in transferPayEat.targets.eatenItem
// 	transferPayEat.sources.paidFoodItem in transferPayEat.items
// 	transferPayEat.targets.eatenItem in transferPayEat.items

// 	/** Constraints on the Transfer from order to serve */
// 	transferOrderServe.items in transferOrderServe.sources.orderedFoodItem
// 	transferOrderServe.items in transferOrderServe.targets.servedFoodItem
// 	transferOrderServe.sources.orderedFoodItem in transferOrderServe.items 
// 	transferOrderServe.targets.servedFoodItem in transferOrderServe.items 

// 	/** Constraints on the Transfer from order to prepare*/
// 	bijectionFiltered[sources, transferOrderPrepare, order]
// 	bijectionFiltered[targets, transferOrderPrepare, prepare]
// 	subsettingItemRuleForSources[transferOrderPrepare]
// 	subsettingItemRuleForTargets[transferOrderPrepare]
// 	transferOrderPrepare.items in transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination
// 	transferOrderPrepare.items in transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination
// 	transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination in transferOrderPrepare.items
// 	transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination in transferOrderPrepare.items

// 	/** Constraints on the Transfer from prepare to serve */
// 	transferPrepareServe.items in transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination
// 	transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination in transferPrepareServe.items
// 	transferPrepareServe.items in transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination
// 	transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination in transferPrepareServe.items

// 	/** Constraints on the Transfer from serve to eat */
// 	transferServeEat.items in transferServeEat.sources.servedFoodItem
// 	transferServeEat.items in transferServeEat.targets.eatenItem
// 	transferServeEat.sources.servedFoodItem in transferServeEat.items
// 	transferServeEat.targets.eatenItem in transferServeEat.items
}
//***********************************************************************************************************
/** 				Object Flow Food Service with Loop */
//***********************************************************************************************************
sig OFLoopFoodService extends OFFoodService{
	start: one OFStart,
	end: one OFEnd,
	disj transferOrderPrepare, transferOrderPay, transferPayEat: set TransferBefore
}{
// /** Constraints on OFSingleFoodService */
// 	// Constrain relations to their subtypes
// 	order in OFCustomOrder
// 	prepare in OFCustomPrepare
// 	serve in OFCustomServe

// 	// Set new Steps and constrain Steps
// 	start + end + transferOrderPrepare + transferOrderPay + transferPayEat in this.steps
// 	this.steps in order + prepare + pay + serve + eat + start + end + transferPrepareServe 
// 	+ transferOrderServe	 + transferServeEat + transferOrderPrepare + transferOrderPay + 
// 	transferPayEat

// /** Constraints on process steps */
// 	/** Constraints on start: OFStart */
// 	#start = 1
// 	functionFiltered[happensBefore, start, order]

// 	/** Constraints on order: OFCustomOrder */
// 	#order = 2
// 	order.outputs in order.orderedFoodItem + order.orderAmount + order.orderDestination
// 	bijectionFiltered[outputs, order, FoodItem]
// 	bijectionFiltered[outputs, order, Real]
// 	bijectionFiltered[outputs, order, Location]
// 	inverseFunctionFiltered[happensBefore, start + eat, order]

// 	/** Constraints on pay: OFPay */
// 	pay.inputs in pay.paidAmount + pay.paidFoodItem
// 	bijectionFiltered[inputs, pay, Real]
// 	bijectionFiltered[inputs, pay, FoodItem]
// 	pay.outputs in pay.paidFoodItem
// 	functionFiltered[outputs, pay, FoodItem]

// 	/** Constraints on prepare: OFCustomPrepare */
// 	prepare.inputs in prepare.preparedFoodItem + prepare.prepareDestination
// 	bijectionFiltered[inputs, prepare, FoodItem]
// 	bijectionFiltered[inputs, prepare, Location]
// 	prepare.outputs in prepare.preparedFoodItem + prepare.prepareDestination
// 	bijectionFiltered[outputs, prepare, FoodItem]
// 	bijectionFiltered[outputs, prepare, Location]

// 	/** Constraints on serve: OFCustomServe */
// 	serve.inputs in serve.servedFoodItem + serve.serviceDestination
// 	bijectionFiltered[inputs, serve, FoodItem]
// 	bijectionFiltered[inputs, serve, Location]
// 	serve.outputs in serve.servedFoodItem + serve.serviceDestination
// 	bijectionFiltered[outputs, serve, Location]
// 	bijectionFiltered[outputs, serve, FoodItem]

// 	/** Constraints on eat: OFEat */	
// 	eat.inputs in eat.eatenItem
// 	bijectionFiltered[inputs, eat, FoodItem] 
// 	functionFiltered[happensBefore, eat, end + order]

// 	/** Constraints on end: OFEnd */
// 	#end = 1
// //	inverseFunctionFiltered[happensBefore, eat, end]

// /** Constraints on transfers */
// 	/** Constraints on the Transfer from order to pay*/
// 	bijectionFiltered[sources, transferOrderPay, order]
// 	bijectionFiltered[targets, transferOrderPay, pay]
// 	subsettingItemRuleForSources[transferOrderPay]
// 	subsettingItemRuleForTargets[transferOrderPay]
// 	transferOrderPay.items in transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount
// 	transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount in transferOrderPay.items
// 	transferOrderPay.items in transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount
// 	transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount in transferOrderPay.items

// 	/** Constraints on the Transfer from pay to eat */
// 	bijectionFiltered[sources, transferPayEat, pay]
// 	bijectionFiltered[targets, transferPayEat, eat]
// 	subsettingItemRuleForSources[transferPayEat]
// 	subsettingItemRuleForTargets[transferPayEat]
// 	transferPayEat.items in transferPayEat.sources.paidFoodItem
// 	transferPayEat.items in transferPayEat.targets.eatenItem
// 	transferPayEat.sources.paidFoodItem in transferPayEat.items
// 	transferPayEat.targets.eatenItem in transferPayEat.items

// 	/** Constraints on the Transfer from order to serve */
// 	transferOrderServe.items in transferOrderServe.sources.orderedFoodItem
// 	transferOrderServe.items in transferOrderServe.targets.servedFoodItem
// 	transferOrderServe.sources.orderedFoodItem in transferOrderServe.items 
// 	transferOrderServe.targets.servedFoodItem in transferOrderServe.items 

// 	/** Constraints on the Transfer from order to prepare*/
// 	bijectionFiltered[sources, transferOrderPrepare, order]
// 	bijectionFiltered[targets, transferOrderPrepare, prepare]
// 	subsettingItemRuleForSources[transferOrderPrepare]
// 	subsettingItemRuleForTargets[transferOrderPrepare]
// 	transferOrderPrepare.items in transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination
// 	transferOrderPrepare.items in transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination
// 	transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination in transferOrderPrepare.items
// 	transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination in transferOrderPrepare.items

// 	/** Constraints on the Transfer from prepare to serve */
// 	transferPrepareServe.items in transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination
// 	transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination in transferPrepareServe.items
// 	transferPrepareServe.items in transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination
// 	transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination in transferPrepareServe.items

// 	/** Constraints on the Transfer from serve to eat */
// 	transferServeEat.items in transferServeEat.sources.servedFoodItem
// 	transferServeEat.items in transferServeEat.targets.eatenItem
// 	transferServeEat.sources.servedFoodItem in transferServeEat.items
// 	transferServeEat.targets.eatenItem in transferServeEat.items
}
//***********************************************************************************************************
/** 				Object Flow Food Service with Parallelism */
//***********************************************************************************************************
sig OFParallelFoodService extends OFFoodService{}{
	// bijectionFiltered[happensBefore, pay, prepare]
	// bijectionFiltered[happensBefore, pay, order]
}

// Explicit facts

// FoodItem -- OK
fact f1 {all food_item: FoodItem | no food_item.happensBefore && no happensBefore.food_item && no food_item.steps && no food_item.inputs && no food_item.outputs}

// Location -- OK
fact f2 {all location: Location | no location.happensBefore && no happensBefore.location && no location.steps && no location.inputs && no location.outputs}

// Real -- OK
fact f3 {all real: Real | no real.happensBefore && no happensBefore.real && no real.steps && no real.inputs && no real.outputs}

// OFStart -- OK
fact f4 {all of_start: OFStart | no inputs.of_start && no outputs.of_start && no items.of_start}

// OFEnd -- OK
fact f5 {all of_end: OFEnd | no inputs.of_end && no outputs.of_end && no items.of_end}

// OFOrder -- OK
fact f6 {all of_order: OFOrder | no of_order.inputs}
fact f7 {all of_order: OFOrder | of_order.orderedFoodItem in of_order.outputs}

// OFCustomOrder -- OK
fact f8 {all of_custom_order: OFCustomOrder | of_custom_order.orderAmount in of_custom_order.outputs}
fact f9 {all of_custom_order: OFCustomOrder | of_custom_order.orderDestination in of_custom_order.outputs}

// OFPrepare -- OK
fact f10 {all of_prepare: OFPrepare | of_prepare.preparedFoodItem in of_prepare.inputs}
fact f11 {all of_prepare: OFPrepare | of_prepare.preparedFoodItem in of_prepare.outputs}

// OFCustomPrepare -- OK
fact f12 {all of_custom_prepare: OFCustomPrepare | of_custom_prepare.prepareDestination in of_custom_prepare.inputs}
fact f13 {all of_custom_prepare: OFCustomPrepare | of_custom_prepare.prepareDestination in of_custom_prepare.outputs}

// OFServe -- OK
fact f14 {all of_serve: OFServe | of_serve.servedFoodItem in of_serve.inputs}
fact f15 {all of_serve: OFServe | of_serve.servedFoodItem in of_serve.outputs}

// OFCustomServe -- OK
fact f16 {all of_custom_serve: OFCustomServe | of_custom_serve.serviceDestination in of_custom_serve.inputs}

// OFEat -- OK
fact f17 {all of_eat: OFEat | of_eat.eatenItem in of_eat.inputs}
fact f18 {all of_eat: OFEat | no of_eat.outputs}

// OFPay -- OK
fact f19 {all of_pay: OFPay | of_pay.paidAmount in of_pay.inputs}
fact f20 {all of_pay: OFPay | of_pay.paidFoodItem in of_pay.inputs}
fact f21 {all of_pay: OFPay | of_pay.paidFoodItem in of_pay.outputs}

// FoodService -- OK
fact f22 {all food_service: FoodService | bijectionFiltered[happensBefore, food_service.order, food_service.serve]}
fact f23 {all food_service: FoodService | bijectionFiltered[happensBefore, food_service.prepare, food_service.serve]}
fact f24 {all food_service: FoodService | bijectionFiltered[happensBefore, food_service.serve, food_service.eat]}
fact f25 {all food_service: FoodService | food_service.order + food_service.prepare + food_service.pay + food_service.eat + food_service.serve in food_service.steps}

// OFFoodService -- OK
fact f26 {all of_food_service: OFFoodService | no of_food_service.inputs and no inputs.of_food_service}
fact f27 {all of_food_service: OFFoodService | no of_food_service.outputs and no outputs.of_food_service}
fact f28 {all of_food_service: OFFoodService | of_food_service.order in OFOrder}
fact f29 {all of_food_service: OFFoodService | of_food_service.prepare in OFPrepare}
fact f30 {all of_food_service: OFFoodService | of_food_service.pay in OFPay}
fact f31 {all of_food_service: OFFoodService | of_food_service.eat in OFEat}
fact f32 {all of_food_service: OFFoodService | of_food_service.serve in OFServe}
fact f33 {all of_food_service: OFFoodService | of_food_service.transferPrepareServe + of_food_service.transferOrderServe + of_food_service.transferServeEat in of_food_service.steps}
fact f34 {all of_food_service: OFFoodService | bijectionFiltered[sources, of_food_service.transferPrepareServe, of_food_service.prepare]}
fact f35 {all of_food_service: OFFoodService | bijectionFiltered[targets, of_food_service.transferPrepareServe, of_food_service.serve]}
fact f36 {all of_food_service: OFFoodService | subsettingItemRuleForSources[of_food_service.transferPrepareServe]}
fact f37 {all of_food_service: OFFoodService | subsettingItemRuleForTargets[of_food_service.transferPrepareServe]}
fact f38 {all of_food_service: OFFoodService | bijectionFiltered[sources, of_food_service.transferServeEat, of_food_service.serve]}
fact f39 {all of_food_service: OFFoodService | bijectionFiltered[targets, of_food_service.transferServeEat, of_food_service.eat]}
fact f40 {all of_food_service: OFFoodService | subsettingItemRuleForSources[of_food_service.transferServeEat]}
fact f41 {all of_food_service: OFFoodService | subsettingItemRuleForTargets[of_food_service.transferServeEat]}
fact f42 {all of_food_service: OFFoodService | bijectionFiltered[sources, of_food_service.transferOrderServe, of_food_service.order]}
fact f43 {all of_food_service: OFFoodService | bijectionFiltered[targets, of_food_service.transferOrderServe, of_food_service.serve]}
fact f44 {all of_food_service: OFFoodService | subsettingItemRuleForSources[of_food_service.transferOrderServe]}
fact f45 {all of_food_service: OFFoodService | subsettingItemRuleForTargets[of_food_service.transferOrderServe]}

// OFSingleFoodService -- OK
fact f46 {all of_single_food_service: OFSingleFoodService | of_single_food_service.order in OFCustomOrder}
fact f47 {all of_single_food_service: OFSingleFoodService | of_single_food_service.prepare in OFCustomPrepare}
fact f48 {all of_single_food_service: OFSingleFoodService | of_single_food_service.serve in OFCustomServe}
fact f49 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderPrepare + of_single_food_service.transferOrderPay + of_single_food_service.transferPayEat in of_single_food_service.steps}
fact f50 {all of_single_food_service: OFSingleFoodService | of_single_food_service.steps in of_single_food_service.order + of_single_food_service.prepare + of_single_food_service.pay + of_single_food_service.serve + of_single_food_service.eat + of_single_food_service.transferPrepareServe + of_single_food_service.transferOrderServe + of_single_food_service.transferServeEat + of_single_food_service.transferOrderPrepare + of_single_food_service.transferOrderPay + of_single_food_service.transferPayEat}
fact f51 {all of_single_food_service: OFSingleFoodService | #of_single_food_service.order = 1}
fact f52 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.order).outputs in (of_single_food_service.order).orderedFoodItem + (of_single_food_service.order).orderAmount + (of_single_food_service.order).orderDestination}
fact f53 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.pay).inputs + (of_single_food_service.pay).paidAmount in (of_single_food_service.pay).paidFoodItem}
fact f54 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.pay).outputs in (of_single_food_service.pay).paidFoodItem}
fact f55 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.prepare).inputs in (of_single_food_service.prepare).preparedFoodItem + (of_single_food_service.prepare).prepareDestination}
fact f56 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.prepare).outputs in (of_single_food_service.prepare).preparedFoodItem + (of_single_food_service.prepare).prepareDestination}
fact f57 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.serve).inputs in (of_single_food_service.serve).servedFoodItem + (of_single_food_service.serve).serviceDestination}
fact f58 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.serve).outputs in (of_single_food_service.serve).servedFoodItem + (of_single_food_service.serve).serviceDestination}
fact f59 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.eat).inputs in (of_single_food_service.eat).eatenItem}
fact f60 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[sources, of_single_food_service.transferOrderPay, of_single_food_service.order]}
fact f61 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[targets, of_single_food_service.transferOrderPay, of_single_food_service.pay]}
fact f62 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForSources[of_single_food_service.transferOrderPay]}
fact f63 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForTargets[of_single_food_service.transferOrderPay]}
fact f64 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderPay).items in (of_single_food_service.transferOrderPay).sources.orderedFoodItem + (of_single_food_service.transferOrderPay).sources.orderAmount}
fact f65 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderPay).sources.orderedFoodItem + (of_single_food_service.transferOrderPay).sources.orderAmount in (of_single_food_service.transferOrderPay).items}
fact f66 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderPay).items in (of_single_food_service.transferOrderPay).targets.paidFoodItem + (of_single_food_service.transferOrderPay).targets.paidAmount}
fact f67 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderPay).targets.paidFoodItem + (of_single_food_service.transferOrderPay).targets.paidAmount in (of_single_food_service.transferOrderPay).items}
fact f68 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[sources, of_single_food_service.transferPayEat, of_single_food_service.pay]}
fact f69 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[targets, of_single_food_service.transferPayEat, of_single_food_service.eat]}
fact f70 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForSources[of_single_food_service.transferPayEat]}
fact f71 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForTargets[of_single_food_service.transferPayEat]}
fact f72 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferPayEat).items in (of_single_food_service.transferPayEat).sources.paidFoodItem}
fact f73 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferPayEat).items in (of_single_food_service.transferPayEat).targets.eatenItem}
fact f74 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferPayEat).sources.paidFoodItem in (of_single_food_service.transferPayEat).items}
fact f75 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferPayEat).targets.eatenItem in (of_single_food_service.transferPayEat).items}
fact f76 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderServe).items in (of_single_food_service.transferOrderServe).sources.orderedFoodItem}
fact f77 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderServe).items in (of_single_food_service.transferOrderServe).targets.servedFoodItem}
fact f78 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderServe).sources.orderedFoodItem in (of_single_food_service.transferOrderServe).items }
fact f79 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderServe).targets.servedFoodItem in (of_single_food_service.transferOrderServe).items }
fact f80 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[sources, of_single_food_service.transferOrderPrepare, of_single_food_service.order]}
fact f81 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[targets, of_single_food_service.transferOrderPrepare, of_single_food_service.prepare]}
fact f82 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForSources[of_single_food_service.transferOrderPrepare]}
fact f83 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForTargets[of_single_food_service.transferOrderPrepare]}
fact f84 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderPrepare).items in (of_single_food_service.transferOrderPrepare).sources.orderedFoodItem + (of_single_food_service.transferOrderPrepare).sources.orderDestination}
fact f85 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderPrepare).items in (of_single_food_service.transferOrderPrepare).targets.preparedFoodItem + (of_single_food_service.transferOrderPrepare).targets.prepareDestination}
fact f86 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderPrepare).sources.orderedFoodItem + (of_single_food_service.transferOrderPrepare).sources.orderDestination in (of_single_food_service.transferOrderPrepare).items}
fact f87 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferOrderPrepare).targets.preparedFoodItem + (of_single_food_service.transferOrderPrepare).targets.prepareDestination in (of_single_food_service.transferOrderPrepare).items}
fact f88 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferPrepareServe).items in (of_single_food_service.transferPrepareServe).sources.preparedFoodItem + (of_single_food_service.transferPrepareServe).sources.prepareDestination}
fact f89 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferPrepareServe).sources.preparedFoodItem + (of_single_food_service.transferPrepareServe).sources.prepareDestination in (of_single_food_service.transferPrepareServe).items}
fact f90 {all of_single_food_service: OFSingleFoodService |  (of_single_food_service.transferPrepareServe).items in (of_single_food_service.transferPrepareServe).targets.servedFoodItem + (of_single_food_service.transferPrepareServe).targets.serviceDestination}
fact f91 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferPrepareServe).targets.servedFoodItem + (of_single_food_service.transferPrepareServe).targets.serviceDestination in (of_single_food_service.transferPrepareServe).items}
fact f92 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferServeEat).items in (of_single_food_service.transferServeEat).sources.servedFoodItem}
fact f93 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferServeEat).items in (of_single_food_service.transferServeEat).targets.eatenItem}
fact f94 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferServeEat).sources.servedFoodItem in (of_single_food_service.transferServeEat).items}
fact f95 {all of_single_food_service: OFSingleFoodService | (of_single_food_service.transferServeEat).targets.eatenItem in (of_single_food_service.transferServeEat).items}

// OFLoopFoodService OK

fact f96 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.order) in OFCustomOrder}
fact f97 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.prepare) in OFCustomPrepare}
fact f98 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.serve) in OFCustomServe}
fact f99 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.start) + (of_loop_food_service.end) + (of_loop_food_service.transferOrderPrepare) + (of_loop_food_service.transferOrderPay) + (of_loop_food_service.transferPayEat) in (of_loop_food_service.steps)}
fact f100 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.steps) in (of_loop_food_service.order) + (of_loop_food_service.prepare) + (of_loop_food_service.pay) + (of_loop_food_service.serve) + (of_loop_food_service.eat) + (of_loop_food_service.start) + (of_loop_food_service.end) + (of_loop_food_service.transferPrepareServe) + (of_loop_food_service.transferOrderServe) + (of_loop_food_service.transferServeEat) + (of_loop_food_service.transferOrderPrepare) + (of_loop_food_service.transferOrderPay) + (of_loop_food_service.transferPayEat)}
fact f101 {all of_loop_food_service: OFLoopFoodService | #(of_loop_food_service.start) = 1}
fact f102 {all of_loop_food_service: OFLoopFoodService | functionFiltered[happensBefore, (of_loop_food_service.start), (of_loop_food_service.order)]}
fact f103 {all of_loop_food_service: OFLoopFoodService | #(of_loop_food_service.order) = 2}
fact f104 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.order).outputs in (of_loop_food_service.order).orderedFoodItem + (of_loop_food_service.order).orderAmount + (of_loop_food_service.order).orderDestination}
fact f105 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, (of_loop_food_service.order), FoodItem]}
fact f106 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, (of_loop_food_service.order), Real]}
fact f107 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, (of_loop_food_service.order), Location]}
fact f108 {all of_loop_food_service: OFLoopFoodService | inverseFunctionFiltered[happensBefore, (of_loop_food_service.start) + (of_loop_food_service.eat), (of_loop_food_service.order)]}
fact f109 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.pay).inputs in (of_loop_food_service.pay).paidAmount + (of_loop_food_service.pay).paidFoodItem}
fact f110 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, (of_loop_food_service.pay), Real]}
fact f111 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, (of_loop_food_service.pay), FoodItem]}
fact f112 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.pay).outputs in (of_loop_food_service.pay).paidFoodItem}
fact f113 {all of_loop_food_service: OFLoopFoodService | functionFiltered[outputs, (of_loop_food_service.pay), FoodItem]}
fact f114 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.prepare).inputs in (of_loop_food_service.prepare).preparedFoodItem + (of_loop_food_service.prepare).prepareDestination}
fact f115 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, (of_loop_food_service.prepare), FoodItem]}
fact f116 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, (of_loop_food_service.prepare), Location]}
fact f117 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.prepare).outputs in (of_loop_food_service.prepare).preparedFoodItem + (of_loop_food_service.prepare).prepareDestination}
fact f118 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, (of_loop_food_service.prepare), FoodItem]}
fact f119 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, (of_loop_food_service.prepare), Location]}
fact f120 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.serve).inputs in (of_loop_food_service.serve).servedFoodItem + (of_loop_food_service.serve).serviceDestination}
fact f121 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, (of_loop_food_service.serve), FoodItem]}
fact f122 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, (of_loop_food_service.serve), Location]}
fact f123 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.serve).outputs in (of_loop_food_service.serve).servedFoodItem + (of_loop_food_service.serve).serviceDestination}
fact f124 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, (of_loop_food_service.serve), Location]}
fact f125 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, (of_loop_food_service.serve), FoodItem]}
fact f126 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.eat).inputs in (of_loop_food_service.eat).eatenItem}
fact f127 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, (of_loop_food_service.eat), FoodItem]}
fact f128 {all of_loop_food_service: OFLoopFoodService | functionFiltered[happensBefore, (of_loop_food_service.eat), (of_loop_food_service.end) + (of_loop_food_service.order)]}
fact f129 {all of_loop_food_service: OFLoopFoodService | #(of_loop_food_service.end) = 1}
fact f130 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[sources, (of_loop_food_service.transferOrderPay), (of_loop_food_service.order)]}
fact f131 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[targets, (of_loop_food_service.transferOrderPay), (of_loop_food_service.pay)]}
fact f132 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForSources[(of_loop_food_service.transferOrderPay)]}
fact f133 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForTargets[(of_loop_food_service.transferOrderPay)]}
fact f134 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderPay).items in (of_loop_food_service.transferOrderPay).sources.orderedFoodItem + (of_loop_food_service.transferOrderPay).sources.orderAmount}
fact f135 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderPay).sources.orderedFoodItem + (of_loop_food_service.transferOrderPay).sources.orderAmount in (of_loop_food_service.transferOrderPay).items}
fact f136 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderPay).items in (of_loop_food_service.transferOrderPay).targets.paidFoodItem + (of_loop_food_service.transferOrderPay).targets.paidAmount}
fact f137 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderPay).targets.paidFoodItem + (of_loop_food_service.transferOrderPay).targets.paidAmount in (of_loop_food_service.transferOrderPay).items}
fact f138 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[sources, (of_loop_food_service.transferPayEat), (of_loop_food_service.pay)]}
fact f139 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[targets, (of_loop_food_service.transferPayEat), (of_loop_food_service.eat)]}
fact f140 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForSources[(of_loop_food_service.transferPayEat)]}
fact f141 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForTargets[(of_loop_food_service.transferPayEat)]}
fact f142 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferPayEat).items in (of_loop_food_service.transferPayEat).sources.paidFoodItem}
fact f143 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferPayEat).items in (of_loop_food_service.transferPayEat).targets.eatenItem}
fact f144 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferPayEat).sources.paidFoodItem in (of_loop_food_service.transferPayEat).items}
fact f145 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferPayEat).targets.eatenItem in (of_loop_food_service.transferPayEat).items}
fact f146 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderServe).items in (of_loop_food_service.transferOrderServe).sources.orderedFoodItem}
fact f147 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderServe).items in (of_loop_food_service.transferOrderServe).targets.servedFoodItem}
fact f148 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderServe).sources.orderedFoodItem in (of_loop_food_service.transferOrderServe).items }
fact f149 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderServe).targets.servedFoodItem in (of_loop_food_service.transferOrderServe).items}
fact f150 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[sources, (of_loop_food_service.transferOrderPrepare), (of_loop_food_service.order)]}
fact f151 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[targets, (of_loop_food_service.transferOrderPrepare), (of_loop_food_service.prepare)]}
fact f152 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForSources[(of_loop_food_service.transferOrderPrepare)]}
fact f153 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForTargets[(of_loop_food_service.transferOrderPrepare)]}
fact f154 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderPrepare).items in (of_loop_food_service.transferOrderPrepare).sources.orderedFoodItem + (of_loop_food_service.transferOrderPrepare).sources.orderDestination}
fact f155 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderPrepare).items in (of_loop_food_service.transferOrderPrepare).targets.preparedFoodItem + (of_loop_food_service.transferOrderPrepare).targets.prepareDestination}
fact f156 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderPrepare).sources.orderedFoodItem + (of_loop_food_service.transferOrderPrepare).sources.orderDestination in (of_loop_food_service.transferOrderPrepare).items}
fact f157 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferOrderPrepare).targets.preparedFoodItem + (of_loop_food_service.transferOrderPrepare).targets.prepareDestination in (of_loop_food_service.transferOrderPrepare).items}
fact f158 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferPrepareServe).items in (of_loop_food_service.transferPrepareServe).sources.preparedFoodItem + (of_loop_food_service.transferPrepareServe).sources.prepareDestination}
fact f159 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferPrepareServe).sources.preparedFoodItem + (of_loop_food_service.transferPrepareServe).sources.prepareDestination in (of_loop_food_service.transferPrepareServe).items}
fact f160 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferPrepareServe).items in (of_loop_food_service.transferPrepareServe).targets.servedFoodItem + (of_loop_food_service.transferPrepareServe).targets.serviceDestination}
fact f161 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferPrepareServe).targets.servedFoodItem + (of_loop_food_service.transferPrepareServe).targets.serviceDestination in (of_loop_food_service.transferPrepareServe).items}
fact f162 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferServeEat).items in (of_loop_food_service.transferServeEat).sources.servedFoodItem}
fact f163 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferServeEat).items in (of_loop_food_service.transferServeEat).targets.eatenItem}
fact f164 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferServeEat).sources.servedFoodItem in (of_loop_food_service.transferServeEat).items}
fact f165 {all of_loop_food_service: OFLoopFoodService | (of_loop_food_service.transferServeEat).targets.eatenItem in (of_loop_food_service.transferServeEat).items}

// OFParallelFoodService OK OK
fact f166 {all of_parallel_food_service: OFParallelFoodService | bijectionFiltered[happensBefore, of_parallel_food_service.pay, of_parallel_food_service.prepare]}
fact f167 {all of_parallel_food_service: OFParallelFoodService | bijectionFiltered[happensBefore, of_parallel_food_service.pay, of_parallel_food_service.order]}

//***********************************************************************************************************
/** 			General Functions and Predicates */
//***********************************************************************************************************
//pred suppressTransfers {no Transfer}
//pred suppressIO {no inputs and no outputs}
pred instancesDuringExample{Order in OFFoodService.order && Prepare in OFFoodService.prepare
		&& Serve in OFFoodService.serve && Eat in OFFoodService.eat && Pay in OFFoodService.pay}
pred onlyOFFoodService {FoodService in OFFoodService && noChildFoodService && #OFFoodService = 1 && noCustomFoodService}
pred onlyOFSingleFoodService {FoodService in OFSingleFoodService}
pred onlyOFLoopFoodService {FoodService in OFLoopFoodService}
pred onlyOFParallelFoodService {FoodService in OFParallelFoodService}
pred noCustomFoodService {no OFCustomOrder && no OFCustomPrepare && no OFCustomServe}
pred noChildFoodService {no OFSingleFoodService && no OFLoopFoodService && no OFParallelFoodService}
//***********************************************************************************************************
/** 				Checks and Runs */
//***********************************************************************************************************
run showOFFoodService{nonZeroDurationOnly && instancesDuringExample && onlyOFFoodService && #OFFoodService.order = 1} for 12
run showOFSingleFoodService{nonZeroDurationOnly && instancesDuringExample && onlyOFSingleFoodService} for 15 but exactly 1 OFSingleFoodService
run showOFLoopFoodService{nonZeroDurationOnly && instancesDuringExample && onlyOFLoopFoodService} for 30 but exactly 1 OFLoopFoodService
run showOFParallelFoodService{nonZeroDurationOnly && instancesDuringExample && onlyOFParallelFoodService} for 10
