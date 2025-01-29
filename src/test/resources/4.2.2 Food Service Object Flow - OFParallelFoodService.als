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
module OFParallelFoodServiceModule
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
/** 				Object Flow Food Service Parallel */
//***********************************************************************************************************
//sig OFParallelFoodService extends OFFoodService{
//	disj transferOrderPay, transferOrderPrepare, transferPayEat, transferPrepareServe, transferServeEat: set TransferBefore
//}{
///** Constraints on OFControlLoopFoodService */
//	// The containing activity dose not have any inputs or outputs, and is not the input or output of anything
//	no this.inputs and no inputs.this
//	no this.outputs and no outputs.this
//
//	// Constrain relations to their subtypes
//	order in OFCustomOrder
//	prepare in OFCustomPrepare
//	serve in OFCustomServe
//
//	// Set new Steps and constrain Steps
//	transferOrderPrepare + transferOrderPay + transferPayEat in this.steps
//	this.steps in order + pay + prepare + serve + eat + transferOrderPrepare + 
//	transferPrepareServe + transferOrderServe + transferServeEat + transferOrderPay + transferPayEat
//
///** Constraints on process steps */
//	/** Constraints on order: OFCustomOrder */
//	#order = 2
//	// Close the model: bound the outputs of order
//	order.outputs in order.orderedFoodItem + order.orderAmount + order.orderDestination
//	// Ensure each instance of order can only process one instance of each flowing object type and vice versa
//	bijectionFiltered[outputs, order, FoodItem]
//	bijectionFiltered[outputs, order, Real]
//	bijectionFiltered[outputs, order, Location]
//
//	/** Constraints on pay: OFPay */
//	// Close the model: bound the inputs and outputs of pay
//	pay.inputs in pay.paidAmount + pay.paidFoodItem
//	pay.outputs in pay.paidFoodItem
//	// Ensure each instance of pay can only process one instance of each flowing object type and vice versa
//	bijectionFiltered[inputs, pay, Real]
//	bijectionFiltered[inputs, pay, FoodItem]
//	bijectionFiltered[outputs, pay, FoodItem]
//
//	/** Constraints on prepare: OFCustomPrepare */
//	// Close the model: bound the inputs and outputs of prepare
//	prepare.inputs in prepare.preparedFoodItem + prepare.prepareDestination
//	prepare.outputs in prepare.preparedFoodItem + prepare.prepareDestination
//	// Ensure each instance of prepare can only process one instance of each flowing object type and vice versa
//	bijectionFiltered[inputs, prepare, FoodItem]
//	bijectionFiltered[inputs, prepare, Location]
//	bijectionFiltered[outputs, prepare, FoodItem]
//	bijectionFiltered[outputs, prepare, FoodItem]
//
//	/** Constraints on serve: OFCustomServe */
//	// Close the model: bound the inputs and outputs of prepareAndServe
//	serve.inputs in serve.servedFoodItem + serve.serviceDestination
//	serve.outputs in serve.servedFoodItem
//	// Ensure each instance of prepareAndServe can only process one instance of each flowing object type and vice versa
//	bijectionFiltered[inputs, serve, FoodItem]
//	bijectionFiltered[inputs, serve, Location]
//	bijectionFiltered[outputs, serve, FoodItem]
//
//	/** Constraints on eat: OFEat */
//	// Close the model: bound the inputs of eat
//	eat.inputs in eat.eatenItem
//	// Ensure each instance of eat can only process one instance of each flowing object type and vice versa
//	bijectionFiltered[inputs, eat, FoodItem]
//
///** Constraints on transfers */
//	/** Ensure disjointness of Transfers, since some are inherited */
//	no transferPrepareServe & transferOrderServe & transferServeEat & transferOrderPrepare & transferOrderPay & transferPayEat
//
//	/** Constraints on the Transfer from order to pay*/
//	bijectionFiltered[sources, transferOrderPay, order]
//	bijectionFiltered[targets, transferOrderPay, pay]
//	subsettingItemRuleForSources[transferOrderPay]
//	subsettingItemRuleForTargets[transferOrderPay]
//	// Close the model: bound the Transfer's Item relation to specific properties (relations) in the Source and Target
//	//	with a two-way subsetting
//	transferOrderPay.items in transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount
//	transferOrderPay.sources.orderedFoodItem + transferOrderPay.sources.orderAmount in transferOrderPay.items
//	transferOrderPay.items in transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount
//	transferOrderPay.targets.paidFoodItem + transferOrderPay.targets.paidAmount in transferOrderPay.items
//
//	/** Constraints on the Transfer from pay to eat */
//	bijectionFiltered[sources, transferPayEat, pay]
//	bijectionFiltered[targets, transferPayEat, eat]
//	subsettingItemRuleForSources[transferPayEat]
//	subsettingItemRuleForTargets[transferPayEat]
//	// Close the model: bound the Transfer's Item relation to specific properties (relations) in the Source and Target
//	//	with a two-way subsetting
//	transferPayEat.items in transferPayEat.sources.paidFoodItem
//	transferPayEat.items in transferPayEat.targets.eatenItem
//	transferPayEat.sources.paidFoodItem in transferPayEat.items
//	transferPayEat.targets.eatenItem in transferPayEat.items
//
//	/** Constraints on the Transfer from order to prepare */
//	bijectionFiltered[sources, transferOrderPrepare, order]
//	bijectionFiltered[targets, transferOrderPrepare, prepare]
//	subsettingItemRuleForSources[transferOrderPrepare]
//	subsettingItemRuleForTargets[transferOrderPrepare]
//	// Close the model: bound the Transfer's Item relation to specific properties (relations) in the Source and Target
//	//	with a two-way subsetting
//	transferOrderPrepare.items in transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination
//	transferOrderPrepare.items in transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination
//	transferOrderPrepare.sources.orderedFoodItem + transferOrderPrepare.sources.orderDestination in transferOrderPrepare.items
//	transferOrderPrepare.targets.preparedFoodItem + transferOrderPrepare.targets.prepareDestination in transferOrderPrepare.items
//
//	/** Constraints on the Transfer from order to serve */
//	// It *might* be necessary to add model closure constraints here. Not really sure what to do with this transfer yet.
//	transferOrderServe.items in transferOrderServe.sources.orderedFoodItem
//	transferOrderServe.items in transferOrderServe.targets.servedFoodItem
//	transferOrderServe.sources.orderedFoodItem in transferOrderServe.items 
//	transferOrderServe.targets.servedFoodItem in transferOrderServe.items
//
//	/** Constraints on the Transfer from prepare to serve */
//	// Close the model: bound the Transfer's Item relation to specific properties (relations) in the Source and Target
//	//	with a two-way subsetting
//	transferPrepareServe.items in transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination
//	transferPrepareServe.items in transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination
//	transferPrepareServe.sources.preparedFoodItem + transferPrepareServe.sources.prepareDestination in transferPrepareServe.items
//	transferPrepareServe.targets.servedFoodItem + transferPrepareServe.targets.serviceDestination in transferPrepareServe.items
//
//	/** Constraints on the Transfer from serve to eat */
//	// Close the model: bound the Transfer's Item relation to specific properties (relations) in the Source and Target
//	//	with a two-way subsetting
//	transferServeEat.items in transferServeEat.sources.servedFoodItem
//	transferServeEat.items in transferServeEat.targets.eatenItem
//	transferServeEat.sources.servedFoodItem in transferServeEat.items
//	transferServeEat.targets.eatenItem in transferServeEat.items
//}

sig OFParallelFoodService extends OFFoodService{disj transferOrderPay, transferOrderPrepare, transferPayEat, transferPrepareServe, transferServeEat: set Transfer}
/** Constraints on OFSingleFoodService */
fact {all x: OFParallelFoodService | no x.inputs}
fact {all x: OFParallelFoodService | no inputs.x}
fact {all x: OFParallelFoodService | no outputs.x}
fact {all x: OFParallelFoodService | no x.outputs}
fact {all x: OFParallelFoodService | no items.x}
fact {all x: OFParallelFoodService | x.transferOrderPay + x.transferOrderPrepare + x.transferPayEat + x.transferPrepareServe + x.transferServeEat in x.steps}
fact {all x: OFParallelFoodService | x.steps in x.eat + x.order + x.pay + x.prepare + x.serve + x.transferOrderPay + x.transferOrderPrepare + x.transferOrderServe + x.transferPayEat + x.transferPrepareServe + x.transferServeEat}

/** Constraints on process steps */
	/** Constraints on order: OFCustomOrder */
fact {all x: OFParallelFoodService | x.order in OFCustomOrder}
fact {all x: OFParallelFoodService | #(x.order) = 2}
fact {all x: OFParallelFoodService | bijectionFiltered[outputs, x.order, x.order.orderAmount]}
fact {all x: OFParallelFoodService | bijectionFiltered[outputs, x.order, x.order.orderDestination]}
fact {all x: OFParallelFoodService | bijectionFiltered[outputs, x.order, x.order.orderedFoodItem]}
fact {all x: OFParallelFoodService | x.order.outputs in x.order.orderAmount + x.order.orderDestination + x.order.orderedFoodItem}

	/** Constraints on pay: OFPay */
fact {all x: OFParallelFoodService | bijectionFiltered[inputs, x.pay, x.pay.paidAmount]}
fact {all x: OFParallelFoodService | bijectionFiltered[inputs, x.pay, x.pay.paidFoodItem]}
fact {all x: OFParallelFoodService | bijectionFiltered[outputs, x.pay, x.pay.paidFoodItem]}
fact {all x: OFParallelFoodService | x.pay.inputs in x.pay.paidAmount + x.pay.paidFoodItem}
fact {all x: OFParallelFoodService | x.pay.outputs in x.pay.paidFoodItem}

	/** Constraints on prepare: OFCustomPrepare */
fact {all x: OFParallelFoodService | x.prepare in OFCustomPrepare}
fact {all x: OFParallelFoodService | bijectionFiltered[inputs, x.prepare, x.prepare.prepareDestination]}
fact {all x: OFParallelFoodService | bijectionFiltered[inputs, x.prepare, x.prepare.preparedFoodItem]}
fact {all x: OFParallelFoodService | bijectionFiltered[outputs, x.prepare, x.prepare.prepareDestination]}
fact {all x: OFParallelFoodService | bijectionFiltered[outputs, x.prepare, x.prepare.preparedFoodItem]}
fact {all x: OFParallelFoodService | x.prepare.inputs in x.prepare.prepareDestination + x.prepare.preparedFoodItem}
fact {all x: OFParallelFoodService | x.prepare.outputs in x.prepare.prepareDestination + x.prepare.preparedFoodItem}

	/** Constraints on serve: OFCustomServe */
fact {all x: OFParallelFoodService | x.serve in OFCustomServe}
fact {all x: OFParallelFoodService | bijectionFiltered[inputs, x.serve, x.serve.servedFoodItem]}
fact {all x: OFParallelFoodService | bijectionFiltered[inputs, x.serve, x.serve.serviceDestination]}
fact {all x: OFParallelFoodService | bijectionFiltered[outputs, x.serve, x.serve.servedFoodItem]}
fact {all x: OFParallelFoodService | x.serve.inputs in x.serve.servedFoodItem + x.serve.serviceDestination}
fact {all x: OFParallelFoodService | x.serve.outputs in x.serve.servedFoodItem}

	/** Constraints on eat: OFEat */	
fact {all x: OFParallelFoodService | bijectionFiltered[inputs, x.eat, x.eat.eatenItem]}
fact {all x: OFParallelFoodService | x.eat.inputs in x.eat.eatenItem}

/** Constraints on transfers */
	/** Ensure disjointness of Transfers, since some are inherited */
fact {all x: OFParallelFoodService | no x.transferOrderPay & x.transferOrderPrepare & x.transferOrderServe & x.transferPayEat & x.transferPrepareServe & x.transferServeEat}
	/** Constraints on the Transfer from order to pay*/
fact {all x: OFParallelFoodService | bijectionFiltered[sources, x.transferOrderPay, x.order]}
fact {all x: OFParallelFoodService | bijectionFiltered[targets, x.transferOrderPay, x.pay]}
fact {all x: OFParallelFoodService | subsettingItemRuleForSources[x.transferOrderPay]}
fact {all x: OFParallelFoodService | subsettingItemRuleForTargets[x.transferOrderPay]}
fact {all x: OFParallelFoodService | isAfterSource[x.transferOrderPay]}
fact {all x: OFParallelFoodService | isBeforeTarget[x.transferOrderPay]}
fact {all x: OFParallelFoodService | x.transferOrderPay.items in x.transferOrderPay.sources.orderAmount + x.transferOrderPay.sources.orderedFoodItem}
fact {all x: OFParallelFoodService | x.transferOrderPay.sources.orderAmount + x.transferOrderPay.sources.orderedFoodItem in x.transferOrderPay.items}
fact {all x: OFParallelFoodService | x.transferOrderPay.items in x.transferOrderPay.targets.paidAmount + x.transferOrderPay.targets.paidFoodItem}
fact {all x: OFParallelFoodService | x.transferOrderPay.targets.paidAmount + x.transferOrderPay.targets.paidFoodItem in x.transferOrderPay.items}

	/** Constraints on the Transfer from pay to eat */
fact {all x: OFParallelFoodService | bijectionFiltered[sources, x.transferPayEat, x.pay]}
fact {all x: OFParallelFoodService | bijectionFiltered[targets, x.transferPayEat, x.eat]}
fact {all x: OFParallelFoodService | subsettingItemRuleForSources[x.transferPayEat]}
fact {all x: OFParallelFoodService | subsettingItemRuleForTargets[x.transferPayEat]}
fact {all x: OFParallelFoodService | isAfterSource[x.transferPayEat]}
fact {all x: OFParallelFoodService | isBeforeTarget[x.transferPayEat]}
fact {all x: OFParallelFoodService | x.transferPayEat.items in x.transferPayEat.sources.paidFoodItem}
fact {all x: OFParallelFoodService | x.transferPayEat.items in x.transferPayEat.targets.eatenItem}
fact {all x: OFParallelFoodService | x.transferPayEat.sources.paidFoodItem in x.transferPayEat.items}
fact {all x: OFParallelFoodService | x.transferPayEat.targets.eatenItem in x.transferPayEat.items}

	/** Constraints on the Transfer from order to prepare*/
fact {all x: OFParallelFoodService | bijectionFiltered[sources, x.transferOrderPrepare, x.order]}
fact {all x: OFParallelFoodService | bijectionFiltered[targets, x.transferOrderPrepare, x.prepare]}
fact {all x: OFParallelFoodService | subsettingItemRuleForSources[x.transferOrderPrepare]}
fact {all x: OFParallelFoodService | subsettingItemRuleForTargets[x.transferOrderPrepare]}
fact {all x: OFParallelFoodService | isAfterSource[x.transferOrderPrepare]}
fact {all x: OFParallelFoodService | isBeforeTarget[x.transferOrderPrepare]}
fact {all x: OFParallelFoodService | x.transferOrderPrepare.items in x.transferOrderPrepare.sources.orderDestination + x.transferOrderPrepare.sources.orderedFoodItem}
fact {all x: OFParallelFoodService | x.transferOrderPrepare.items in x.transferOrderPrepare.targets.prepareDestination + x.transferOrderPrepare.targets.preparedFoodItem}
fact {all x: OFParallelFoodService | x.transferOrderPrepare.sources.orderDestination + x.transferOrderPrepare.sources.orderedFoodItem in x.transferOrderPrepare.items}
fact {all x: OFParallelFoodService | x.transferOrderPrepare.targets.prepareDestination + x.transferOrderPrepare.targets.preparedFoodItem in x.transferOrderPrepare.items}

	/** Constraints on the Transfer from order to serve */
fact {all x: OFParallelFoodService | x.transferOrderServe.items in x.transferOrderServe.sources.orderedFoodItem}
fact {all x: OFParallelFoodService | x.transferOrderServe.items in x.transferOrderServe.targets.servedFoodItem}
fact {all x: OFParallelFoodService | x.transferOrderServe.sources.orderedFoodItem in x.transferOrderServe.items}
fact {all x: OFParallelFoodService | x.transferOrderServe.targets.servedFoodItem in x.transferOrderServe.items}

	/** Constraints on the Transfer from prepare to serve */
fact {all x: OFParallelFoodService | bijectionFiltered[sources, x.transferPrepareServe, x.prepare]}
fact {all x: OFParallelFoodService | bijectionFiltered[targets, x.transferPrepareServe, x.serve]}
fact {all x: OFParallelFoodService | subsettingItemRuleForSources[x.transferPrepareServe]}
fact {all x: OFParallelFoodService | subsettingItemRuleForTargets[x.transferPrepareServe]}
fact {all x: OFParallelFoodService | isAfterSource[x.transferPrepareServe]}
fact {all x: OFParallelFoodService | isBeforeTarget[x.transferPrepareServe]}
fact {all x: OFParallelFoodService | x.transferPrepareServe.items in x.transferPrepareServe.sources.prepareDestination + x.transferPrepareServe.sources.preparedFoodItem}
fact {all x: OFParallelFoodService | x.transferPrepareServe.sources.prepareDestination + x.transferPrepareServe.sources.preparedFoodItem in x.transferPrepareServe.items}
fact {all x: OFParallelFoodService | x.transferPrepareServe.items in x.transferPrepareServe.targets.servedFoodItem + x.transferPrepareServe.targets.serviceDestination}
fact {all x: OFParallelFoodService | x.transferPrepareServe.targets.servedFoodItem + x.transferPrepareServe.targets.serviceDestination in x.transferPrepareServe.items}

	/** Constraints on the Transfer from serve to eat */
fact {all x: OFParallelFoodService | bijectionFiltered[sources, x.transferServeEat, x.serve]}
fact {all x: OFParallelFoodService | bijectionFiltered[targets, x.transferServeEat, x.eat]}
fact {all x: OFParallelFoodService | subsettingItemRuleForSources[x.transferServeEat]}
fact {all x: OFParallelFoodService | subsettingItemRuleForTargets[x.transferServeEat]}
fact {all x: OFParallelFoodService | isAfterSource[x.transferServeEat]}
fact {all x: OFParallelFoodService | isBeforeTarget[x.transferServeEat]}
fact {all x: OFParallelFoodService | x.transferServeEat.items in x.transferServeEat.sources.servedFoodItem}
fact {all x: OFParallelFoodService | x.transferServeEat.items in x.transferServeEat.targets.eatenItem}
fact {all x: OFParallelFoodService | x.transferServeEat.sources.servedFoodItem in x.transferServeEat.items}
fact {all x: OFParallelFoodService | x.transferServeEat.targets.eatenItem in x.transferServeEat.items}
//***********************************************************************************************************
/** 			General Functions and Predicates */
//***********************************************************************************************************
pred instancesDuringExample{Order in OFParallelFoodService.order && Prepare in OFParallelFoodService.prepare && Serve in OFParallelFoodService.serve && Eat in OFParallelFoodService.eat && Pay in OFParallelFoodService.pay && Transfer in OFParallelFoodService.steps && FoodService in OFParallelFoodService}
//***********************************************************************************************************
/** 				Checks and Runs */
//***********************************************************************************************************
run showOFParallelFoodService{instancesDuringExample} for 30 but exactly 1 OFParallelFoodService, 6 int
