//*****************************************************************
// Module: 		Complex Behavior
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model control nodes. 
//*****************************************************************
module ComplexBehavior
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					Complex Behavior */
//*****************************************************************
sig AllControl extends Occurrence{
	disj p1,p2,p3,p4,p5,p6,p7: set AtomicBehavior
}

fact {all x: AllControl | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: AllControl | bijectionFiltered[happensBefore, x.p1, x.p3]}
fact {all x: AllControl | bijectionFiltered[happensBefore, x.p2, x.p4]}
fact {all x: AllControl | bijectionFiltered[happensBefore, x.p3, x.p4]}
fact {all x: AllControl | bijectionFiltered[happensBefore, x.p4, x.p5 + x.p6]}
fact {all x: AllControl | bijectionFiltered[happensBefore, x.p5 + x.p6, x.p7]}
fact {all x: AllControl | #x.p1=1}
fact {all x: AllControl | x.p1 + x.p2 + x.p3 + x.p4 + x.p5 + x.p6 + x.p7 in x.steps}
fact {all x: AllControl | x.steps in x.p1 + x.p2 + x.p3 + x.p4 + x.p5 + x.p6 + x.p7}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms from appearing randomly, to limit uninteresting examples from instantiating
pred instancesDuringExample {AtomicBehavior in AllControl.p1 + AllControl.p2 + AllControl.p3 + AllControl.p4 + AllControl.p5 + AllControl.p6 + AllControl.p7}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run AllControl{suppressTransfers and suppressIO and instancesDuringExample and some AllControl} for 10
