//*****************************************************************
// Module: 		Merge
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model merge nodes. 
//*****************************************************************
module BehaviorMerge
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					Merge */
//*****************************************************************
sig BehaviorMerge extends Occurrence {
	disj p1,p2,p3: set AtomicBehavior
}

fact {all x: BehaviorMerge | bijectionFiltered[happensBefore, x.p1 + x.p2, x.p3]}
fact {all x: BehaviorMerge | #(x.p1) = 1}
fact {all x: BehaviorMerge | #(x.p2) = 1}
fact {all x: BehaviorMerge | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: BehaviorMerge | x.steps in x.p1 + x.p2 + x.p3}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
//pred suppressTransfers {no Transfer}
//pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms from appearing randomly, to limit uninteresting examples from instantiating
//pred instancesDuringExample{AtomicBehavior in Merge.p1 + Merge.p2 + Merge.p3}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
//run merge{suppressTransfers and suppressIO and instancesDuringExample and some Merge} for 6

//mw modified to pass the test
//commented out General Functions and Predicates & Checks and Runs
//cardinary constraint to add () and spaces (ie., "#x.p1=1" to "#(x.p1) = 1"
//Sig MergeNodes is changed to BehaviorMerge

