//*****************************************************************
// Module: 		Loops
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model loops. 
//*****************************************************************
module LoopModule
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
/** 					SelfLoop */
//*****************************************************************
sig Loop extends Occurrence {disj p1, p2, p3: set AtomicBehavior}

fact {all x: Loop | #(x.p1) = 1}
fact {all x: Loop | #(x.p2) >= 2}
fact {all x: Loop | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: Loop | x.steps in x.p1 + x.p2 + x.p3}
fact {all x: Loop | functionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: Loop | inverseFunctionFiltered[happensBefore, x.p1 + x.p2, x.p2]}
fact {all x: Loop | functionFiltered[happensBefore, x.p2, x.p2 + x.p3]}
fact {all x: Loop | inverseFunctionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: Loop | no x.inputs}
fact {all x: Loop | no x.outputs}
fact {all x: Loop | no inputs.x}
fact {all x: Loop | no outputs.x}
fact {all x: Loop | no items.x}
fact {all x: Loop | no y: Transfer | y in x.steps}
//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
//pred suppressTransfers {no Transfer}
//pred suppressIO {no inputs and no outputs}
pred instancesDuringExample {AtomicBehavior in Loop.p1 + Loop.p2 + Loop.p3}
//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run loop{instancesDuringExample and some Loop} for 12
