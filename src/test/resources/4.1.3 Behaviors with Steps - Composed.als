//*****************************************************************
// Module: 		ComposedModule
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model calling behaviors. 
//*****************************************************************
module ComposedModule
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
/** 					NestedBehavior */
//*****************************************************************
sig Nested extends Occurrence {disj p4, p5: set AtomicBehavior}

fact {all x: Nested | no x.inputs}
fact {all x: Nested | no x.outputs}
fact {all x: Nested | no inputs.x}
fact {all x: Nested | no outputs.x}
fact {all x: Nested | no items.x}
fact {all x: Nested | no y: Transfer | y in x.steps}
fact {all x: Nested | bijectionFiltered[happensBefore, x.p4, x.p5]}
fact {all x: Nested | #(x.p4) = 1}
fact {all x: Nested | x.p4 + x.p5 in x.steps}
fact {all x: Nested | x.steps in x.p4 + x.p5}
//*****************************************************************
/** 					ComposedBehavior */
//*****************************************************************
sig Composed extends Occurrence{disj p1, p3: set AtomicBehavior, p2: set Nested}

fact {all x: Composed | no x.inputs}
fact {all x: Composed | no x.outputs}
fact {all x: Composed | no inputs.x}
fact {all x: Composed | no outputs.x}
fact {all x: Composed | no items.x}
fact {all x: Composed | no y: Transfer | y in x.steps}
fact {all x: Composed | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: Composed | bijectionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: Composed | #(x.p1) = 1}
fact {all x: Composed | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: Composed | x.steps in x.p1 + x.p2 + x.p3}
//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred instancesDuringExample {AtomicBehavior in (Composed.p1 + Composed.p3 + Nested.p4 + Nested.p5) and Nested in Composed.p2}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run composedBehavior{instancesDuringExample and some Composed} for 12
