//*****************************************************************
// Module: 		Loops
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model loops. 
//*****************************************************************


// This file was modified to only have explicit facts.

module Loops
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig P1, P2, P3, P4 extends Occurrence{}

//*****************************************************************
/** 					SelfLoop */
//*****************************************************************
sig Loop extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3
}{
	// functionFiltered[happensBefore, p1, p2]
	// inverseFunctionFiltered[happensBefore, p1 + p2, p2]
	// functionFiltered[happensBefore, p2, p2 + p3]
	// inverseFunctionFiltered[happensBefore, p2, p3]

	// #p1 = 1		// Multiplicity of p1
	// #p2 >= 2		// Multiplicity of p2
	// #p3 >= 1		// Multiplicity of p3

	// p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}

//*****************************************************************
/** 				General Facts */
//*****************************************************************

fact f1 { all s: Loop | functionFiltered[happensBefore, s.p1, s.p2] }
fact f2 { all s: Loop | inverseFunctionFiltered[happensBefore, s.p1 + s.p2, s.p2] }
fact f3 { all s: Loop | functionFiltered[happensBefore, s.p2, s.p2 + s.p3] }
fact f4 { all s: Loop | inverseFunctionFiltered[happensBefore, s.p2, s.p3] }
fact f5 { all s: Loop | #s.p1 = 1 }
fact f6 { all s: Loop | #s.p2 >= 2 }
fact f7 { all s: Loop | #s.p3 >= 1 }
fact f8 { all s: Loop | s.p1 + s.p2 + s.p3 in s.steps and  s.steps in s.p1 + s.p2 + s.p3 }

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
//pred nonZeroDurationOnly{all occ: Occurrence | not o/isZeroDuration[occ]}
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
pred p1DuringExample {P1 in (Loop.p1)}
pred p2DuringExample {P2 in (Loop.p2)}
pred p3DuringExample {P3 in (Loop.p3)}
pred p4DuringExample {no P4}
pred instancesDuringExample {p1DuringExample and p2DuringExample and p3DuringExample and p4DuringExample}
pred onlyLoop {#Loop = 1}
//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run loop{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyLoop} for 12
