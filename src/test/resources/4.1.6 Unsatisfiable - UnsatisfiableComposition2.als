//*****************************************************************
// Module: 		Unsatisfiable - Composition 2
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence module to show
//				unsatisfiable conditions for models. 
//*****************************************************************
module UnsatisfiableComposition2Module
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
/** 					UnsatisfiableComposition2 */
//*****************************************************************
/**	UnsatisfiableComposition2 */
sig UnsatisfiableComposition2 extends Occurrence {disj p1, p2, p3: set AtomicBehavior}

// To make satisfiable, go to the library module and comment out the asymmetry constraint
// on HappensBefore.
fact {all x: UnsatisfiableComposition2 | #(x.p1) = 1}
fact {all x: UnsatisfiableComposition2 | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: UnsatisfiableComposition2 | bijectionFiltered[happensDuring, x.p3, x.p1]}
fact {all x: UnsatisfiableComposition2 | bijectionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: UnsatisfiableComposition2 | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: UnsatisfiableComposition2 | x.steps in x.p1 + x.p2 + x.p3}
fact {all x: UnsatisfiableComposition2 | no y: Transfer | y in x.steps}
fact {all x: UnsatisfiableComposition2 | no x.inputs}
fact {all x: UnsatisfiableComposition2 | no inputs.x}
fact {all x: UnsatisfiableComposition2 | no x.outputs}
fact {all x: UnsatisfiableComposition2 | no outputs.x}
fact {all x: UnsatisfiableComposition2 | no items.x}
//******************************************************************************************************
/** 			General Functions and Predicates */
//******************************************************************************************************
pred instancesDuringExample {all x: AtomicBehavior | x in UnsatisfiableComposition2.steps}

//******************************************************************************************************
/** 				Checks and Runs */
//******************************************************************************************************
run showUnsatisfiableComposition2 {instancesDuringExample and some UnsatisfiableComposition2} for 12
