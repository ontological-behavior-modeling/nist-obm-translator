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
sig ComplexBehavior extends Occurrence{
	disj p1,p2,p3,p4,p5,p6,p7: set AtomicBehavior
}

fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p1, x.p3]}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p2, x.p4]}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p3, x.p4]}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p4, x.p5 + x.p6]}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p5 + x.p6, x.p7]}
fact {all x: ComplexBehavior | #(x.p1) = 1}
fact {all x: ComplexBehavior | x.p1 + x.p2 + x.p3 + x.p4 + x.p5 + x.p6 + x.p7 in x.steps}
fact {all x: ComplexBehavior | x.steps in x.p1 + x.p2 + x.p3 + x.p4 + x.p5 + x.p6 + x.p7}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
//pred suppressTransfers {no Transfer}
//pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms from appearing randomly, to limit uninteresting examples from instantiating
//pred instancesDuringExample {AtomicBehavior in AllControl.p1 + AllControl.p2 + AllControl.p3 + AllControl.p4 + AllControl.p5 + AllControl.p6 + AllControl.p7}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
//run AllControl{suppressTransfers and suppressIO and instancesDuringExample and some AllControl} for 10

//mw modified to pass the test
//commented out General Functions and Predicates & Checks and Runs
//cardinary constraint to add () and spaces (ie., "#x.p1=1" to "#(x.p1) = 1"
//Sig AllControl is changed to ComplexBehavior
//binjection is modified to functionFiltered & inverseFunctionFiltered

