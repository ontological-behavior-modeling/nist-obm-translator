//*****************************************************************
// Module: 		MultipleExecutionSteps - MultipleControlFlow
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model control nodes for
//				control flow. 
//*****************************************************************
module MultipleControlFlowModule
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

fact {all x: AtomicBehavior | no x.inputs}
fact {all x: AtomicBehavior | no x.outputs}
fact {all x: AtomicBehavior | no inputs.x}
fact {all x: AtomicBehavior | no outputs.x}
fact {all x: AtomicBehavior | no items.x}
fact {all x: AtomicBehavior | no x.steps}

//*****************************************************************
/** 					MultipleControlFlow */
//*****************************************************************
sig MultipleControlFlow extends Occurrence {disj p1, p2, p3, p4: set AtomicBehavior}

fact {all x: MultipleControlFlow | no x.inputs}
fact {all x: MultipleControlFlow | no x.outputs}
fact {all x: MultipleControlFlow | no inputs.x}
fact {all x: MultipleControlFlow | no outputs.x}
fact {all x: MultipleControlFlow | no items.x}
fact {all x: MultipleControlFlow | no y: Transfer | y in x.steps}
fact {all x: MultipleControlFlow | #(x.p1) = 2}
fact {all x: MultipleControlFlow | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: MultipleControlFlow | bijectionFiltered[happensBefore, x.p1, x.p3]}
fact {all x: MultipleControlFlow | bijectionFiltered[happensBefore, x.p2, x.p4]}
fact {all x: MultipleControlFlow | bijectionFiltered[happensBefore, x.p3, x.p4]}
fact {all x: MultipleControlFlow | x.p1 + x.p2 + x.p3 + x.p4 in x.steps}
fact {all x: MultipleControlFlow | x.steps in x.p1 + x.p2 + x.p3 + x.p4}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred instancesDuringExample {AtomicBehavior in MultipleControlFlow.steps}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run multipleControlFlow{instancesDuringExample and some MultipleControlFlow} for 9
