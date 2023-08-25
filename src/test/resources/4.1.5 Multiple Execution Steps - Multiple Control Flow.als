//*****************************************************************
// Module: 		MultipleExecutionSteps - MultipleControlFlow
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model control nodes for
//				control flow. 
//*****************************************************************
module MultipleExecutionStepsControlFlow
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					MultipleControlFlow */
//*****************************************************************
sig MultipleControlFlow extends Occurrence {
	disj p1,p2,p3,p4: set AtomicBehavior
}

fact {all x: MultipleControlFlow | #x.p1=2}
fact {all x: MultipleControlFlow | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: MultipleControlFlow | bijectionFiltered[happensBefore, x.p1, x.p3] }
fact {all x: MultipleControlFlow | bijectionFiltered[happensBefore, x.p2, x.p4]}
fact {all x: MultipleControlFlow | bijectionFiltered[happensBefore, x.p3, x.p4]}
fact {all x: MultipleControlFlow | x.p1 + x.p2 + x.p3 + x.p4 in x.steps}
fact {all x: MultipleControlFlow | x.steps in x.p1 + x.p2 + x.p3 + x.p4}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
pred instancesDuringExample {AtomicBehavior in (MultipleControlFlow.p1 + MultipleControlFlow.p2 + MultipleControlFlow.p3 + MultipleControlFlow.p4)}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run multipleControlFlow{suppressTransfers and suppressIO and instancesDuringExample and some MultipleControlFlow} for 9
