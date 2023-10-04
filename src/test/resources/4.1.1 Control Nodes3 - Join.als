//*****************************************************************
// Module: 		Join
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model join nodes. 
//*****************************************************************
module JoinNode
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					Join */
//*****************************************************************
sig Join extends Occurrence {
	disj p1,p2,p3: set AtomicBehavior
}

fact {all x: Join | bijectionFiltered[happensBefore, x.p1, x.p3]}
fact {all x: Join | bijectionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: Join | #x.p1=1}
fact {all x: Join | #x.p2=1}
fact {all x: Join | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: Join | x.steps in x.p1 + x.p2 + x.p3}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms for P1-P7 from appearing randomly, to limit uninteresting examples from instantiating
pred instancesDuringExample{AtomicBehavior in Join.p1 + Join.p2 + Join.p3}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run join{suppressTransfers and suppressIO and instancesDuringExample and some Join} for 6
