// This is a file seperated by MW from Unsatisfiable
// make use AutomaticBehavior
// This test fails because OMBModel_R.xmi's p2 multiplicity is 0..*

//*****************************************************************
/** 					UnsatisfiableAsymmetry */
//*****************************************************************
module UnsatisfiableAsymmetry
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}


sig AtomicBehavior extends Occurrence {}
sig UnsatisfiableAsymmetry extends Occurrence {
	disj p1, p2: set AtomicBehavior
}{
	//#p1 = 1
	//#p2 = 1

//	r/asymmetric[happensBefore]	// Comment this line to produce instance

	//bijectionFiltered[happensBefore, p1, p2]
	//bijectionFiltered[happensBefore, p2, p1]

	//p1 + p2 in this.steps and this.steps in p1 + p2
}

fact {all x: UnsatisfiableAsymmetry | #(x.p1) = 1}
fact {all x: UnsatisfiableAsymmetry | #(x.p2) = 1}
fact {all x: UnsatisfiableAsymmetry | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: UnsatisfiableAsymmetry | bijectionFiltered[happensBefore, x.p2, x.p1]}
fact {all x: UnsatisfiableAsymmetry | x.p1 + x.p2 in x.steps}
fact {all x: UnsatisfiableAsymmetry | x.steps in x.p1 + x.p2}