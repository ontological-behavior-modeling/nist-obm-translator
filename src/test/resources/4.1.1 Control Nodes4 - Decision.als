//*****************************************************************
// Module: 		Decision
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model decision nodes. 
//*****************************************************************
module DecisionNodes
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					Decision */
//*****************************************************************
sig Decision extends Occurrence {
	disj p1,p2,p3: set AtomicBehavior
}

fact {all x: Decision | functionFiltered[happensBefore, x.p1, x.p2 + x.p3]}
fact {all x: Decision | inverseFunctionFiltered[happensBefore, x.p1, x.p2 + x.p3]}
fact {all x: Decision | #x.p1=1}
fact {all x: Decision | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: Decision | x.steps in x.p1 + x.p2 + x.p3}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms from appearing randomly, to limit uninteresting examples from instantiating
pred instancesDuringExample{AtomicBehavior in Decision.p1 + Decision.p2 + Decision.p3}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run decision{suppressTransfers and suppressIO and instancesDuringExample and some Decision} for 6
