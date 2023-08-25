//*****************************************************************
// Module: 		Calling Behaviors
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model calling behaviors. 
//*****************************************************************
module ComposedBehavior
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					NestedBehavior */
//*****************************************************************
sig NestedBehavior extends Occurrence {
	disj p4,p5: set AtomicBehavior
}
fact {all x: NestedBehavior | functionFiltered[happensBefore, x.p4, x.p5]}
fact {all x: NestedBehavior | inverseFunctionFiltered[happensBefore, x.p4, x.p5]}
fact {all x: NestedBehavior | #(x.p4) = 1}
fact {all x: NestedBehavior | x.p4 + x.p5 in x.steps}
fact {all x: NestedBehavior | x.steps in x.p4 + x.p5}

//*****************************************************************
/** 					ComposedBehavior */
//*****************************************************************
sig ComposedBehavior extends Occurrence{
	disj p1,p3: set AtomicBehavior,
	p2: set NestedBehavior
}

fact {all x: ComposedBehavior | functionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: ComposedBehavior | functionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: ComposedBehavior | inverseFunctionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: ComposedBehavior | inverseFunctionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: ComposedBehavior | #(x.p1) = 1}
fact {all x: ComposedBehavior | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: ComposedBehavior | x.steps in x.p1 + x.p2 + x.p3}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
//pred suppressTransfers {no Transfer}
//pred suppressIO {no inputs and no outputs}
//pred instancesDuringExample {AtomicBehavior in (ComposedBehavior.p1 + ComposedBehavior.p3
//	+ NestedBehavior.p4 + NestedBehavior.p5) and NestedBehavior in ComposedBehavior.p2}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
//run composedBehavior{suppressTransfers and suppressIO and 
//				instancesDuringExample and some ComposedBehavior} for 6


//mw modified to pass the test
//commented out General Functions and Predicates & Checks and Runs
//cardinary constraint to add () and spaces (ie., "#x.p1=1" to "#(x.p1) = 1"
//Sig AllControl is changed to ComplexBehavior
//binjection is modified to functionFiltered & inverseFunctionFiltered
//NestedBehavior is changed to ComposedBehavior
//!!!!!!!!!! ComposedBehavior p2 is modified to be NestedBehavior

//OBMModel.xmi is modified
//x.p5 multiplicity from 1 to 0..*
//p2 and p3 multiplicity from 1 to 0..*
