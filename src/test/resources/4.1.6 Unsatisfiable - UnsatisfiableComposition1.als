//*****************************************************************
// Module: 		Unsatisfiable - Composition 1
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence module to show
//				unsatisfiable conditions for models. 
//*****************************************************************
module UnsatisfiableComposition1Module
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
/** 					UnsatisfiableComposition1 */
//*****************************************************************
/**	UnsatisfiableComposition1 */
sig UnsatisfiableComposition1 extends Occurrence {disj p1, p2, p3: set AtomicBehavior}

// To make satisfiable, go to the library module and comment out the asymmetry constraint
// on HappensBefore.
fact {all x: UnsatisfiableComposition1 | #(x.p1) = 1}
fact {all x: UnsatisfiableComposition1 | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: UnsatisfiableComposition1 | bijectionFiltered[happensDuring, x.p3, x.p2]}
fact {all x: UnsatisfiableComposition1 | bijectionFiltered[happensBefore, x.p3, x.p1]}
fact {all x: UnsatisfiableComposition1 | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: UnsatisfiableComposition1 | x.steps in x.p1 + x.p2 + x.p3}
fact {all x: UnsatisfiableComposition1 | no y: Transfer | y in x.steps}
fact {all x: UnsatisfiableComposition1 | no x.inputs}
fact {all x: UnsatisfiableComposition1 | no inputs.x}
fact {all x: UnsatisfiableComposition1 | no x.outputs}
fact {all x: UnsatisfiableComposition1 | no outputs.x}
fact {all x: UnsatisfiableComposition1 | no items.x}
//******************************************************************************************************
/** 			General Functions and Predicates */
//******************************************************************************************************
pred instancesDuringExample {all x: AtomicBehavior | x in UnsatisfiableComposition1.steps}

//******************************************************************************************************
/** 				Checks and Runs */
//******************************************************************************************************
run showUnsatisfiableComposition1 {instancesDuringExample and some UnsatisfiableComposition1} for 12
