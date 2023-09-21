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
	disj p1,p2: set AtomicBehavior
}

fact {all x: SimpleSequence | functionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: SimpleSequence | inverseFunctionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: SimpleSequence | #x.p1=1}
fact {all x: SimpleSequence | #x.p2=1}
fact {all x: SimpleSequence | x.p1 + x.p2 in x.steps}
fact {all x: SimpleSequence | x.steps in x.p1 + x.p2}

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

//Modified JN on 9-8-23
//Combined all Control Nodes .als files into one file