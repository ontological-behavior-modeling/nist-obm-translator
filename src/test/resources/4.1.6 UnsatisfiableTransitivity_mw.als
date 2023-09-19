// This is a file seperated by MW from Unsatisfiable
// make use AutomaticBehavior
// r/asymmetric[happensBefore] is commented out.  I don't know when to create this.

//*****************************************************************
/** 					UnsatisfiableAsymmetry */
//*****************************************************************
module UnsatisfiableTransitivity
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}


sig AtomicBehavior extends Occurrence {}
sig UnsatisfiableTransitivity extends Occurrence {
	disj p1,p2, p3: set AtomicBehavior
}{
	//#p1 = 1

	// ^ is the transitive closure operator
//	r/transitive[happensBefore]			// Comment this line to produce an instance
	
	
	//mw Don't know whent to create this looking at obm.xmi diagram
	//r/asymmetric[happensBefore]

	//bijectionFiltered[happensBefore, p1, p2]
	//bijectionFiltered[happensBefore, p2, p3]
	//bijectionFiltered[happensBefore, p3, p1]

	//p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}

fact {all x: UnsatisfiableTransitivity | #(x.p1) = 1}

fact {all x: UnsatisfiableTransitivity | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: UnsatisfiableTransitivity | bijectionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: UnsatisfiableTransitivity | bijectionFiltered[happensBefore, x.p3, x.p1]}

fact {all x: UnsatisfiableTransitivity | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: UnsatisfiableTransitivity | x.steps in x.p1 + x.p2 + x.p3}