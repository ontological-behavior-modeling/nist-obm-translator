//*****************************************************************
// Module: 		ForkNode
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model a fork. 
//*****************************************************************
module ForkNode
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					Fork */
//*****************************************************************
sig Fork extends Occurrence {
	disj p1,p2,p3: set AtomicBehavior
}

fact {all x: Fork | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: Fork | bijectionFiltered[happensBefore, x.p1, x.p3]}
fact {all x: Fork | #x.p1=1}
fact {all x: Fork | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: Fork | x.steps in x.p1 + x.p2 + x.p3}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms from appearing randomly, to limit uninteresting examples from instantiating
pred instancesDuringExample {AtomicBehavior in Fork.p1 + Fork.p2 + Fork.p3}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run fork{suppressTransfers and suppressIO and instancesDuringExample and some Fork} for 10
