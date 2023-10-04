//***********************************************************************************************************
// Module: 		Manufacture Object Flow
// Written by:		Jeremy Doerr
// Purpose: 		Alternate example from that shown in NISTIR 8283 section 4.2.2. Proposed due to
//				tractability issues in Alloy Analyzer. A generic manufacturing process with multiple
//				object flows. Includes steps Receive, ProcessWidget, ProcessInvoice, and Ship. Objects
//				include Widget and WorkOrder. Flow includes a fork and join. Individual examples will
//				include looping and parallel processing.
//***********************************************************************************************************
module ManufactureObjectFlow
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Widget, WorkOrder extends Occurrence{}{no this.happensBefore && no happensBefore.this && 
								no this.steps && no this.inputs && no this.outputs}

sig Start, End extends Occurrence{}

sig Receive extends Occurrence{
	receivedWidget: one Widget,
	receivedWorkOrder: one WorkOrder
}{
	no this.inputs
	receivedWidget + receivedWorkOrder in this.outputs and this.outputs in receivedWidget + receivedWorkOrder
}

sig ProcessWidget extends Occurrence{
	widgetToProcess: one Widget,
	processedWidget: one Widget,
	widgetWorkOrder: one WorkOrder
}{
	processedWidget = widgetToProcess
	widgetToProcess + widgetWorkOrder in this.inputs and this.inputs in widgetToProcess + widgetWorkOrder
	processedWidget in this.outputs and this.outputs in processedWidget
}

sig ProcessInvoice extends Occurrence{
	workOrderToProcess: one WorkOrder,
	processedWorkOrder: one WorkOrder
}{
	processedWorkOrder = workOrderToProcess
	workOrderToProcess in this.inputs and this.inputs in workOrderToProcess
	processedWorkOrder in this.outputs and this.outputs in processedWorkOrder
}

sig Ship extends Occurrence{
	widgetToShip: one Widget,
	shipWorkOrder: one WorkOrder
}{
	widgetToShip + shipWorkOrder in this.inputs and this.inputs in widgetToShip + shipWorkOrder
	no this.outputs
}
//***********************************************************************************************************
/** 				Single Manufacture with Multiple Object Types */
//***********************************************************************************************************
sig ManufactureSingle extends Occurrence{
	receive: set Receive,
	processInvoice: set ProcessInvoice,
	processWidget: set ProcessWidget,
	ship: set Ship,
	disj tReceiveProcInvoice, tReceiveProcWidget, tProcWidgetShip, tProcInvoiceShip: set TransferBefore
}{
	/** Constraints on ManufactureSingle */
	no this.inputs and no inputs.this
	no this.outputs and no outputs.this
	receive + processInvoice + processWidget + ship + tReceiveProcInvoice + tReceiveProcWidget + tProcWidgetShip + tProcInvoiceShip in this.steps and this.steps in receive + processInvoice + processWidget + ship + tReceiveProcInvoice + tReceiveProcWidget + tProcWidgetShip + tProcInvoiceShip

	/** Constraints on receive: Receive */
	#receive = 1

	/** Constraints on transfer from receive to processInvoice */
	bijectionFiltered[sources, tReceiveProcInvoice, receive]
	bijectionFiltered[targets, tReceiveProcInvoice, processInvoice]
	subsettingItemRuleForSources[tReceiveProcInvoice]
	subsettingItemRuleForTargets[tReceiveProcInvoice]

	/** Constraints on processInvoice */

	/** Constraints on transfer from receive to processWidget */
	bijectionFiltered[sources, tReceiveProcWidget, receive]
	bijectionFiltered[targets, tReceiveProcWidget, processWidget]
	bindingItemRuleForSources[tReceiveProcWidget]
	bindingItemRuleForTargets[tReceiveProcWidget]

	/** Constraints on processWidget: ProcessWidget */

	/** Constraints on transfer from tProcInvoiceShip to ship */
	bijectionFiltered[sources, tProcInvoiceShip, processInvoice]
	bijectionFiltered[targets, tProcInvoiceShip, ship]
	subsettingItemRuleForSources[tProcInvoiceShip]
	subsettingItemRuleForTargets[tProcInvoiceShip]

	/** Constraints on transfer from processWidget to ship */
	bijectionFiltered[sources, tProcWidgetShip, processWidget]
	bijectionFiltered[targets, tProcWidgetShip, ship]
	subsettingItemRuleForSources[tProcWidgetShip]
	subsettingItemRuleForTargets[tProcWidgetShip]

	/** Constraints on ship: Ship */

}
//***********************************************************************************************************
/** 				Loop Manufacture with Multiple Object Types*/
//***********************************************************************************************************
sig ManufactureLoop extends Occurrence{
	init: one Start,
	receive: set Receive,
	processInvoice: set ProcessInvoice,
	processWidget: set ProcessWidget,
	ship: set Ship,
	final: one End,
	disj tReceiveProcInvoice, tReceiveProcWidget, tProcWidgetShip, tProcInvoiceShip: set TransferBefore
}{
	/** Constraints on ManufactureParallel */
	no this.inputs and no inputs.this
	no this.outputs and no outputs.this
	init + receive + processInvoice + processWidget + ship + tReceiveProcInvoice + tReceiveProcWidget + tProcWidgetShip + tProcInvoiceShip + final in this.steps and this.steps in init + receive + processInvoice + processWidget + ship + tReceiveProcInvoice + tReceiveProcWidget + tProcWidgetShip + tProcInvoiceShip + final

	/** Constraints on init: Start */
//	/* New */#init = 1
	/* New */functionFiltered[happensBefore, init, receive]

	/** Constraints on receive: Receive */
	/* Modified */#receive = 2
	/* New */bijectionFiltered[outputs, receive, Widget]
	/* New */bijectionFiltered[outputs, receive, WorkOrder]
	/* New */inverseFunctionFiltered[happensBefore, init + ship, receive]

	/** Constraints on transfer from receive to processInvoice */
	bijectionFiltered[sources, tReceiveProcInvoice, receive]
	bijectionFiltered[targets, tReceiveProcInvoice, processInvoice]
	subsettingItemRuleForSources[tReceiveProcInvoice]
	subsettingItemRuleForTargets[tReceiveProcInvoice]

	/** Constraints on processInvoice */
	functionFiltered[happensBefore, processInvoice, ship]
	/* New */bijectionFiltered[inputs, processInvoice, WorkOrder]

	/** Constraints on transfer from receive to processWidget */
	bijectionFiltered[sources, tReceiveProcWidget, receive]
	bijectionFiltered[targets, tReceiveProcWidget, processWidget]
	bindingItemRuleForSources[tReceiveProcWidget]
	bindingItemRuleForTargets[tReceiveProcWidget]

	/** Constraints on processWidget: ProcessWidget */
	/* New */bijectionFiltered[inputs, processWidget, Widget]
	/* New */bijectionFiltered[inputs, processWidget, WorkOrder]

	/** Constraints on transfer from tProcInvoiceShip to ship */
	bijectionFiltered[sources, tProcInvoiceShip, processInvoice]
	bijectionFiltered[targets, tProcInvoiceShip, ship]
	subsettingItemRuleForSources[tProcInvoiceShip]
	subsettingItemRuleForTargets[tProcInvoiceShip]

	/** Constraints on transfer from processWidget to ship */
	bijectionFiltered[sources, tProcWidgetShip, processWidget]
	bijectionFiltered[targets, tProcWidgetShip, ship]
	subsettingItemRuleForSources[tProcWidgetShip]
	subsettingItemRuleForTargets[tProcWidgetShip]

	/** Constraints on ship: Ship */
	/* New */bijectionFiltered[inputs, ship, Widget]
	/* New */functionFiltered[happensBefore, ship, receive + final]

	/** Constraints on final: End */
//	/* New */#final = 1
	/* New */inverseFunctionFiltered[happensBefore, ship, final]
}
//***********************************************************************************************************
/** 				Parallel Manufacture with Multiple Object Type*/
//					Mostly the same as ManufactureSingle, with the few changed/added lines marked
//***********************************************************************************************************
sig ManufactureParallel extends Occurrence{
	receive: set Receive,
	processInvoice: set ProcessInvoice,
	processWidget: set ProcessWidget,
	ship: set Ship,
	disj tReceiveProcInvoice, tReceiveProcWidget, tProcWidgetShip, tProcInvoiceShip: set TransferBefore
}{
	/** Constraints on ManufactureParallel */
	no this.inputs and no inputs.this
	no this.outputs and no outputs.this
	receive + processInvoice + processWidget + ship + tReceiveProcInvoice + tReceiveProcWidget + tProcWidgetShip + tProcInvoiceShip in this.steps and this.steps in receive + processInvoice + processWidget + ship + tReceiveProcInvoice + tReceiveProcWidget + tProcWidgetShip + tProcInvoiceShip

	/** Constraints on receive: Receive */
	/* Modified */#receive = 2
	/* New */bijectionFiltered[outputs, receive, Widget]
	/* New */bijectionFiltered[outputs, receive, WorkOrder]

	/** Constraints on transfer from receive to processInvoice */
	bijectionFiltered[sources, tReceiveProcInvoice, receive]
	bijectionFiltered[targets, tReceiveProcInvoice, processInvoice]
	subsettingItemRuleForSources[tReceiveProcInvoice]
	subsettingItemRuleForTargets[tReceiveProcInvoice]

	/** Constraints on processInvoice */
	functionFiltered[happensBefore, processInvoice, ship]
	/* New */bijectionFiltered[inputs, processInvoice, WorkOrder]

	/** Constraints on transfer from receive to processWidget */
	bijectionFiltered[sources, tReceiveProcWidget, receive]
	bijectionFiltered[targets, tReceiveProcWidget, processWidget]
	bindingItemRuleForSources[tReceiveProcWidget]
	bindingItemRuleForTargets[tReceiveProcWidget]
//	subsettingItemRuleForSources[tReceiveProcWidget]
//	subsettingItemRuleForTargets[tReceiveProcWidget]

	/** Constraints on processWidget: ProcessWidget */
	/* New */bijectionFiltered[inputs, processWidget, Widget]
	/* New */bijectionFiltered[inputs, processWidget, WorkOrder]

	/** Constraints on transfer from tProcInvoiceShip to ship */
	bijectionFiltered[sources, tProcInvoiceShip, processInvoice]
	bijectionFiltered[targets, tProcInvoiceShip, ship]
	subsettingItemRuleForSources[tProcInvoiceShip]
	subsettingItemRuleForTargets[tProcInvoiceShip]

	/** Constraints on transfer from processWidget to ship */
	bijectionFiltered[sources, tProcWidgetShip, processWidget]
	bijectionFiltered[targets, tProcWidgetShip, ship]
	subsettingItemRuleForSources[tProcWidgetShip]
	subsettingItemRuleForTargets[tProcWidgetShip]

	/** Constraints on ship: Ship */
	/* New */bijectionFiltered[inputs, ship, Widget]
	/* New */bijectionFiltered[inputs, ship, WorkOrder]
}
//***********************************************************************************************************
/** Functions and Predicates */
//***********************************************************************************************************
pred receiveDuringExample {Receive in (ManufactureSingle.steps + ManufactureLoop.steps + ManufactureParallel.steps)}
pred processInvoiceDuringExample {ProcessInvoice in (ManufactureSingle.steps + ManufactureLoop.steps + ManufactureParallel.steps)}
pred processWidgetDuringExample {ProcessWidget in (ManufactureSingle.steps + ManufactureLoop.steps + ManufactureParallel.steps)}
pred shipDuringExample {Ship in (ManufactureSingle.steps + ManufactureLoop.steps + ManufactureParallel.steps)}
pred startDuringExample {Start in (ManufactureLoop.steps)}
pred endDuringExample {End in (ManufactureLoop.steps)}
pred transferDuringExample {Transfer in (ManufactureSingle.steps + ManufactureLoop.steps + ManufactureParallel.steps)}
pred stepInstancesDuringExample {receiveDuringExample && processInvoiceDuringExample && processWidgetDuringExample &&
						shipDuringExample && startDuringExample && endDuringExample && transferDuringExample}
//***********************************************************************************************************
/** 				Checks and Runs */
//***********************************************************************************************************
run showManufactureSingle{nonZeroDurationOnly && stepInstancesDuringExample} for 11 but exactly 1 ManufactureSingle, 0 ManufactureLoop, 0 ManufactureParallel
run showManufactureLoop{nonZeroDurationOnly && stepInstancesDuringExample} for 23 but 0 ManufactureSingle, exactly 1 ManufactureLoop, 0 ManufactureParallel
run showManufactureParallel{nonZeroDurationOnly && stepInstancesDuringExample} for 21 but 0 ManufactureSingle, 0 ManufactureLoop, exactly 1 ManufactureParallel
