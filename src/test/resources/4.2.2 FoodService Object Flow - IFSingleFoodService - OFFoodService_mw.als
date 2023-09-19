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

//mw
//change OFFoodService to IFFoodService
//change OF to IF (ie. OFStart to IFStart)
//

module IFFoodService
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Order, Prepare, Serve, Eat, Pay extends Occurrence {}

sig FoodItem, Location, Real extends Occurrence {}{no this.happensBefore && no happensBefore.this
								&& no this.steps && no this.inputs && no this.outputs}

sig IFStart, IFEnd extends Occurrence {}{no inputs.this && no outputs.this && no items.this}
//***********************************************************************************************************
/** 				IFOrder */
//***********************************************************************************************************
sig IFOrder extends Order {orderedFoodItem: one FoodItem}
{
	no this.inputs
	//orderedFoodItem in this.outputs
}
fact {all x: IFOrder | no x.inputs}
fact {all x: IFOrder | x.orderedFoodItem in x.outputs}
fact {all x: IFOrder | x.outputs in x.orderedFoodItem}
//***********************************************************************************************************
/** 				IFCustomOrder */
//***********************************************************************************************************
sig IFCustomOrder extends IFOrder {
	orderAmount: one Real, 
	orderDestination: one Location
}/*{
	orderAmount in this.outputs
	orderDestination in this.outputs
}*/

fact {all x: IFCustomOrder | x.orderAmount in x.outputs}
fact {all x: IFCustomOrder | x.outputs in x.orderAmount}
fact {all x: IFCustomOrder | x.orderDestination in x.outputs}
fact {all x: IFCustomOrder | x.outputs in x.orderDestination}
//***********************************************************************************************************
/** 				IFPrepare */
//***********************************************************************************************************
sig IFPrepare extends Prepare //{preparedFoodItem: one FoodItem}
{
	//preparedFoodItem in this.inputs
	//preparedFoodItem in this.outputs
	preparedFoodItem: set FoodItem
}
fact {all x: IFPrepare | #(x.preparedFoodItem) = 1 }
fact {all x: IFPrepare | x.preparedFoodItem in x.inputs}
fact {all x: IFPrepare | x.inputs in x.preparedFoodItem}
fact {all x: IFPrepare | x.preparedFoodItem in x.outputs}
fact {all x: IFPrepare | x.outputs in x.preparedFoodItem}
//***********************************************************************************************************
/** 				IFCustomPrepare */
//***********************************************************************************************************
sig IFCustomPrepare extends IFPrepare// {prepareDestination: one Location}
{
	//prepareDestination in this.inputs
	//prepareDestination in this.outputs
	prepareDestination: set Location
}
fact {all x: IFCustomPrepare | #(x.prepareDestination) = 1 }
fact {all x: IFCustomPrepare | x.prepareDestination in x.inputs}
fact {all x: IFCustomPrepare | x.inputs in x.prepareDestination}
fact {all x: IFCustomPrepare | x.prepareDestination in x.outputs}
fact {all x: IFCustomPrepare | x.outputs in x.prepareDestination}
//***********************************************************************************************************
/** 				IFServe */
//***********************************************************************************************************
sig IFServe extends Serve //{servedFoodItem: one FoodItem}
{
	//servedFoodItem in this.inputs
	//servedFoodItem in this.outputs
	servedFoodItem: set FoodItem
}
fact {all x: IFServe | #(x.servedFoodItem) = 1 }
fact {all x: IFServe | x.servedFoodItem in x.inputs}
fact {all x: IFServe | x.inputs in x.servedFoodItem}
fact {all x: IFServe | x.servedFoodItem in x.outputs}
fact {all x: IFServe | x.outputs in x.servedFoodItem}
//***********************************************************************************************************
/** 				IFCustomServe */
//***********************************************************************************************************
sig IFCustomServe extends IFServe //{serviceDestination: one Location}
{
	//serviceDestination in this.inputs
	serviceDestination: set Location
}
fact {all x: IFCustomServe | #(x.serviceDestination) = 1 }
fact {all x: IFCustomServe | x.serviceDestination in x.inputs}
fact {all x: IFCustomServe | x.inputs in x.serviceDestination}
//***********************************************************************************************************
/** 				IFEat */
//***********************************************************************************************************
sig IFEat extends Eat// {eatenItem: one FoodItem}
{
	//eatenItem in this.inputs
	//no this.outputs
	eatenItem: set FoodItem
}
fact {all x: FoodItem | #(x.eatenItem) = 1 }
fact {all x: IFEat | x.eatenItem in x.inputs}
fact {all x: IFEat | x.inputs in x.eatenItem}
fact {all x: IFEat | no x.outputs}

//***********************************************************************************************************
/** 				IFPay */
//***********************************************************************************************************
sig IFPay extends Pay {
	paidAmount: one Real, 
	paidFoodItem: one FoodItem}
/*{
	paidAmount in this.inputs
	paidFoodItem in this.inputs
	paidFoodItem in this.outputs
}*/
fact {all x: IFEat | x.paidAmount in x.inputs}
fact {all x: IFEat | x.inputs in x.paidAmount}
fact {all x: IFEat | x.paidFoodItem in x.inputs}
fact {all x: IFEat | x.inputs in x.paidFoodItem}
fact {all x: IFEat | x.paidFoodItem in x.outputs}
fact {all x: IFEat | x.outputs in x.paidFoodItem}

//***********************************************************************************************************
/** 				Food Service */
//***********************************************************************************************************
sig FoodService extends Occurrence {
	order: set Order,
	prepare: set Prepare,
	pay: set Pay,
	eat: set Eat,
	serve: set Serve
}/*{
	bijectionFiltered[happensBefore, order, serve]
	bijectionFiltered[happensBefore, prepare, serve]
	bijectionFiltered[happensBefore, serve, eat]

	order + prepare + pay + eat + serve in this.steps
}*/
fact {all x: FoodService | bijectionFiltered[happensBefore, x.order, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.prepare, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.serve, x.eat]}

fact {all x: FoodService | x.eat + x.order + x.pay + x.prepare + x.serve in x.steps}
fact {all x: FoodService | x.steps in x.eat + x.order + x.pay + x.prepare + x.serve}
//***********************************************************************************************************
/** 				IFFoodService */
//***********************************************************************************************************
sig IFFoodService extends FoodService {
	disj transferPrepareServe, transferOrderServe, transferServeEat: set TransferBefore
}{
/** Constraints on IFFoodService */
	// Prevent extraneous Input and Output relations. (might be able to relax in the presence of other constraints)
	//no this.inputs and no inputs.this
	//no this.outputs and no outputs.this

	// Constrain relations to their subtypes
	order in IFOrder
	prepare in IFPrepare
	pay in IFPay
	eat in IFEat
	serve in IFServe

	// Set Transfers as Steps
	//transferPrepareServe + transferOrderServe + transferServeEat in this.steps

/** Constraints on process steps */
	/** Constraints on order: IFOrder */

	/** Constraints on prepare: IFPrepare */

	/** Constraints on serve: IFServe */

	/** Constraints on eat: IFEat */	

/** Constraints on transfers */
	/** Constraints on the Transfer from prepare to serve */
	//bijectionFiltered[sources, transferPrepareServe, prepare]
	//bijectionFiltered[targets, transferPrepareServe, serve]
	//subsettingItemRuleForSources[transferPrepareServe]
	//subsettingItemRuleForTargets[transferPrepareServe]

	/** Constraints on the Transfer from serve to eat */
	//bijectionFiltered[sources, transferServeEat, serve]
	//bijectionFiltered[targets, transferServeEat, eat]
	//subsettingItemRuleForSources[transferServeEat]
	//subsettingItemRuleForTargets[transferServeEat]

	/** Constraints on the Transfer from order to serve */
	//bijectionFiltered[sources, transferOrderServe, order]
	//bijectionFiltered[targets, transferOrderServe, serve]
	//subsettingItemRuleForSources[transferOrderServe]
	//subsettingItemRuleForTargets[transferOrderServe]
}


fact {all x: IFFoodService | bijectionFiltered[sources, x.transferPrepareServe, x.prepare]}
fact {all x: IFFoodService | bijectionFiltered[targets, x.transferPrepareServe, x.serve]}
fact {all x: IFFoodService | subsettingItemRuleForSources[x.transferPrepareServe]}
fact {all x: IFFoodService | subsettingItemRuleForTargets[x.transferPrepareServe]}

fact {all x: IFFoodService | bijectionFiltered[sources, x.transferServeEat, x.serve]}
fact {all x: IFFoodService | bijectionFiltered[targets, x.transferServeEat, x.eat]}
fact {all x: IFFoodService | subsettingItemRuleForSources[x.transferServeEat]}
fact {all x: IFFoodService | subsettingItemRuleForTargets[x.transferServeEat]}

fact {all x: IFFoodService | bijectionFiltered[sources, x.transferOrderServe, x.order]}
fact {all x: IFFoodService | bijectionFiltered[targets, x.transferOrderServe, x.serve]}
fact {all x: IFFoodService | subsettingItemRuleForSources[x.transferOrderServe]}
fact {all x: IFFoodService | subsettingItemRuleForTargets[x.transferOrderServe]}

fact {all x: IFFoodService | transferPrepareServe +transferOrderServe + transferServeEat in x.steps}
//??????????????
//fact {all x: IFFoodService | x.steps in transferPrepareServe +transferOrderServe + transferServeEat}

//???????????????
//no this.inputs and no inputs.this
//no this.outputs and no outputs.this
fact {all x: IFFoodService | no x.inputs}
fact {all x: IFFoodService | no x.outputs}
