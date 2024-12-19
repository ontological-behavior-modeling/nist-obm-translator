//*****************************************************************
// Module: 		Unsatisfiable - Asymmetry
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence module to show
//				unsatisfiable conditions for models. 
//*****************************************************************
module UnsatisfiableAsymmetryModule
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
/** 					UnsatisfiableAsymmetry */
//*****************************************************************
sig UnsatisfiableAsymmetry extends Occurrence {disj p1, p2: set AtomicBehavior}

fact {all x: UnsatisfiableAsymmetry | #(x.p1) = 1}
fact {all x: UnsatisfiableAsymmetry | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: UnsatisfiableAsymmetry | bijectionFiltered[happensBefore, x.p2, x.p1]}
fact {all x: UnsatisfiableAsymmetry | x.p1 + x.p2 in x.steps}
fact {all x: UnsatisfiableAsymmetry | x.steps in x.p1 + x.p2}
fact {all x: UnsatisfiableAsymmetry | no y: Transfer | y in x.steps}
fact {all x: UnsatisfiableAsymmetry | no x.inputs}
fact {all x: UnsatisfiableAsymmetry | no inputs.x}
fact {all x: UnsatisfiableAsymmetry | no x.outputs}
fact {all x: UnsatisfiableAsymmetry | no outputs.x}
fact {all x: UnsatisfiableAsymmetry | no items.x}
//******************************************************************************************************
/** 			General Functions and Predicates */
//******************************************************************************************************
pred instancesDuringExample {all x: AtomicBehavior | x in UnsatisfiableAsymmetry.steps}

//******************************************************************************************************
/** 				Checks and Runs */
//******************************************************************************************************
run showUnsatisfiableAsymmetry {instancesDuringExample and some UnsatisfiableAsymmetry} for 12
