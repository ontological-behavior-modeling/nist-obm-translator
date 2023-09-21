//*****************************************************************
// Module: 		Loops
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model loops. 
//*****************************************************************
module Loops
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					SelfLoop */
//*****************************************************************
sig Loop extends Occurrence {
	disj p1,p2,p3: set AtomicBehavior
}{
	
	functionFiltered[happensBefore, p1, p2]
	inverseFunctionFiltered[happensBefore, p1 + p2, p2]
	functionFiltered[happensBefore, p2, p2 + p3]
	inverseFunctionFiltered[happensBefore, p2, p3]

	#p1 = 1		// Multiplicity of p1
	#p2 >= 2		// Multiplicity of p2
	#p3 >= 1		// Multiplicity of p3

	p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}

//*****************************************************************
/** 				General Facts */
//*****************************************************************


//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
//pred nonZeroDurationOnly{all occ: Occurrence | not o/isZeroDuration[occ]}
//pred suppressTransfers {no Transfer}
//pred suppressIO {no inputs and no outputs}
//pred p1DuringExample {P1 in (Loop.p1)}
//pred p2DuringExample {P2 in (Loop.p2)}
//pred p3DuringExample {P3 in (Loop.p3)}
//pred p4DuringExample {no P4}
//pred instancesDuringExample {p1DuringExample and p2DuringExample and p3DuringExample and p4DuringExample}
//pred onlyLoop {#Loop = 1}
//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
//run loop{suppressTransfers and suppressIO and instancesDuringExample and onlyLoop} for 12
