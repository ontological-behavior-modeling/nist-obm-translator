//*****************************************************************
// Module: 		ControlNodes
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model control nodes. 
//*****************************************************************
module ControlNodes
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence{}

//*****************************************************************
/** 					SimpleSequence */
//*****************************************************************
sig SimpleSequence extends Occurrence{
	disj p1, p2: set AtomicBehavior
}{
	functionFiltered[happensBefore, p1, p2]
	inverseFunctionFiltered[happensBefore, p1, p2]
	#p1 = 1
	p1 + p2 in this.steps and this.steps in p1 + p2
}
