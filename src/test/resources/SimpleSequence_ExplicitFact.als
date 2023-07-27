//*****************************************************************
// Module: 		ControlNodes
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model control nodes. 
//*****************************************************************
module ControlNodes
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig P1, P2 extends Occurrence{}

//*****************************************************************
/** 					SimpleSequence */
//*****************************************************************
sig SimpleSequence extends Occurrence{
	p1: set P1,
	p2: set P2
}


fact f1 {all s: SimpleSequence | #s.p1 = 1 }
fact f2 {all s: SimpleSequence | s.p1 + s.p2 in s.steps}
fact f3 {all s: SimpleSequence | s.steps in s.p1 + s.p2}
fact f4 {all s: SimpleSequence | functionFiltered[happensBefore, s.p1, s.p2]}
fact f5 {all s: SimpleSequence | inverseFunctionFiltered[happensBefore, s.p1, s.p2]}

//*****************************************************************
/** 				General Facts */
//*****************************************************************

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
//pred nonZeroDurationOnly{all occ: Occurrence | not o/isZeroDuration[occ]}
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms for P1-P7 from appearing randomly, to limit uninteresting examples from instantiating
pred p1DuringExample {P1 in (SimpleSequence.p1)}
pred p2DuringExample {P2 in (SimpleSequence.p2)}
pred instancesDuringExample {p1DuringExample and p2DuringExample}
// These are used to limit the type and number of signatures that can instantiate.
pred onlySimpleSequence {#SimpleSequence = 1}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run SimpleSequence{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlySimpleSequence} for 6
