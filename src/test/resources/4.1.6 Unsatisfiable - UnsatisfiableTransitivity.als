//*****************************************************************
// Module: 		Unsatisfiable - Transitivity
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence module to show
//				unsatisfiable conditions for models. 
//*****************************************************************
module UnsatisfiableTransitivityModule
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
/** 					UnsatisfiableTransitivity */
//*****************************************************************
/**	UnsatisfiableTransitivity */
sig UnsatisfiableTransitivity extends Occurrence {disj p1, p2, p3: set AtomicBehavior}

// To make satisfiable, go to the library module, swap the standard transitivity and asymmetry
// constraints for the alternates, and comment out the transitivity one.
fact {all x: UnsatisfiableTransitivity | #(x.p1) = 1}
fact {all x: UnsatisfiableTransitivity | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: UnsatisfiableTransitivity | bijectionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: UnsatisfiableTransitivity | bijectionFiltered[happensBefore, x.p3, x.p1]}
fact {all x: UnsatisfiableTransitivity | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: UnsatisfiableTransitivity | x.steps in x.p1 + x.p2 + x.p3}
fact {all x: UnsatisfiableTransitivity | no y: Transfer | y in x.steps}
fact {all x: UnsatisfiableTransitivity | no x.inputs}
fact {all x: UnsatisfiableTransitivity | no inputs.x}
fact {all x: UnsatisfiableTransitivity | no x.outputs}
fact {all x: UnsatisfiableTransitivity | no outputs.x}
fact {all x: UnsatisfiableTransitivity | no items.x}
//******************************************************************************************************
/** 			General Functions and Predicates */
//******************************************************************************************************
pred instancesDuringExample {all x: AtomicBehavior | x in UnsatisfiableTransitivity.steps}

//******************************************************************************************************
/** 				Checks and Runs */
//******************************************************************************************************
run showUnsatisfiableTransitivity {instancesDuringExample and some UnsatisfiableTransitivity} for 12
