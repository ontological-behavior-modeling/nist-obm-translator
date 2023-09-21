//*****************************************************************
// Module: 		BehaviorJoin
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model join nodes. 
//*****************************************************************
module JoinNode
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					BehaviorJoin */
//*****************************************************************
sig BehaviorJoin extends Occurrence {
	disj p1,p2,p3: set AtomicBehavior
}

fact {all x: BehaviorJoin | functionFiltered[happensBefore, x.p1, x.p3]}
fact {all x: BehaviorJoin | functionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: BehaviorJoin | inverseFunctionFiltered[happensBefore, x.p1, x.p3]}
fact {all x: BehaviorJoin | inverseFunctionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: BehaviorJoin | #(x.p1) = 1}
fact {all x: BehaviorJoin | #(x.p2) = 1}
fact {all x: BehaviorJoin | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: BehaviorJoin | x.steps in x.p1 + x.p2 + x.p3}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
//pred suppressTransfers {no Transfer}
//pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms for P1-P7 from appearing randomly, to limit uninteresting examples from instantiating
//pred instancesDuringExample{AtomicBehavior in Join.p1 + Join.p2 + Join.p3}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
//run join{suppressTransfers and suppressIO and instancesDuringExample and some Join} for 6
//mw modified to pass the test
//commented out General Functions and Predicates & Checks and Runs
//cardinary constraint to add () and spaces (ie., "#x.p1=1" to "#(x.p1) = 1"
//binjection is modified to functionFiltered & inverseFunctionFiltered
//Sig Join is changed to BehaviorJoin