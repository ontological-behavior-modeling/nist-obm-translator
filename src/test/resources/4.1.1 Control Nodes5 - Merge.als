//*****************************************************************
// Module: 		Merge
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model merge nodes. 
//*****************************************************************
module MergeNodes
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					Merge */
//*****************************************************************
sig Merge extends Occurrence {
	disj p1,p2,p3: set AtomicBehavior
}

fact {all x: Merge | functionFiltered[happensBefore, x.p1 + x.p2, x.p3]}
fact {all x: Merge | inverseFunctionFiltered[happensBefore, x.p1 + x.p2, x.p3]}
fact {all x: Merge | #x.p1=1}
fact {all x: Merge | #x.p2=1}
fact {all x: Merge | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: Merge | x.steps in x.p1 + x.p2 + x.p3}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms from appearing randomly, to limit uninteresting examples from instantiating
pred instancesDuringExample{AtomicBehavior in Merge.p1 + Merge.p2 + Merge.p3}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run merge{suppressTransfers and suppressIO and instancesDuringExample and some Merge} for 6
