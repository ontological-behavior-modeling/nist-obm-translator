//*****************************************************************
// Module: 		SimpleSequence
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model a simple sequence
//				of actions.
//*****************************************************************
module SimpleSequence
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					SimpleSequence */
//*****************************************************************
sig SimpleSequence extends Occurrence{
	disj p1,p2: set AtomicBehavior
}

fact {all x: SimpleSequence | functionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: SimpleSequence | inverseFunctionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: SimpleSequence | #x.p1=1}
fact {all x: SimpleSequence | x.p1 + x.p2 in x.steps}
fact {all x: SimpleSequence | x.steps in x.p1 + x.p2}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms from appearing randomly, to limit uninteresting examples from instantiating
pred instancesDuringExample {AtomicBehavior in SimpleSequence.p1 + SimpleSequence.p2}
pred crossover {some disj x,y: SimpleSequence | during[x.p2, y] and during[y.p2, x] and not during[x.p1, y] and not during[y.p1, x]}
pred someOverlaps {some x: Occurrence | #(x.overlaps) > 1}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run disjointnessCheck1{some x: AtomicBehavior | x in SimpleSequence.p1 and x in SimpleSequence.p2} for 6
run disjointessCheck2{some x: SimpleSequence | x.p1 = x.p2}
run SimpleSequence{suppressTransfers and suppressIO and instancesDuringExample and some SimpleSequence} for 6
run SimpleSequenceWithCrossover{suppressTransfers and suppressIO and instancesDuringExample and some SimpleSequence and crossover} for 8

