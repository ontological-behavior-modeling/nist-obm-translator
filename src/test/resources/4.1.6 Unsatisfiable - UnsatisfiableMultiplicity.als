//*****************************************************************
// Module: 		Unsatisfiable - Multiplicity
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence module to show
//				unsatisfiable conditions for models. 
//*****************************************************************
module UnsatisfiableMultiplicityModule
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
/** 					UnsatisfiableMultiplicity */
//*****************************************************************
/**	UnsatisfiableMultiplicity */
sig UnsatisfiableMultiplicity extends Occurrence {disj p1, p2, p3, p4: set AtomicBehavior}

fact {all x: UnsatisfiableMultiplicity | #(x.p1) = 1}		// Change multiplicity to 2 to produce instance
fact {all x: UnsatisfiableMultiplicity | bijectionFiltered[happensBefore, x.p1, x.p2 + x.p3]}
fact {all x: UnsatisfiableMultiplicity | bijectionFiltered[happensBefore, x.p2, x.p4]}
fact {all x: UnsatisfiableMultiplicity | bijectionFiltered[happensBefore, x.p3, x.p4]}
fact {all x: UnsatisfiableMultiplicity | x.p1 + x.p2 + x.p3 + x.p4 in x.steps}
fact {all x: UnsatisfiableMultiplicity | x.steps in x.p1 + x.p2 + x.p3 + x.p4}
fact {all x: UnsatisfiableMultiplicity | no y: Transfer | y in x.steps}
fact {all x: UnsatisfiableMultiplicity | no x.inputs}
fact {all x: UnsatisfiableMultiplicity | no inputs.x}
fact {all x: UnsatisfiableMultiplicity | no x.outputs}
fact {all x: UnsatisfiableMultiplicity | no outputs.x}
fact {all x: UnsatisfiableMultiplicity | no items.x}
//******************************************************************************************************
/** 			General Functions and Predicates */
//******************************************************************************************************
pred instancesDuringExample {all x: AtomicBehavior | x in UnsatisfiableMultiplicity.steps}

//******************************************************************************************************
/** 				Checks and Runs */
//******************************************************************************************************
run showUnsatisfiableMultiplicity {instancesDuringExample and some UnsatisfiableMultiplicity} for 12
