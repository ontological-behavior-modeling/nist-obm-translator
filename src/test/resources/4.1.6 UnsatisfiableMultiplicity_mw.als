// This is a file seperated by MW from Unsatisfiable
// make use AutomaticBehavior



module UnsatisfiableMultiplicity
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig AtomicBehavior extends Occurrence {}
//*****************************************************************
/** 					UnsatisfiableMultiplicity */
//*****************************************************************
/**	UnsatisfiableMultiplicity */
sig UnsatisfiableMultiplicity extends Occurrence {
	disj p1,p2,p3,p4: set AtomicBehavior
}
	//#p1 = 2	// Change multiplicity to 2 to produce instance

	// Decision from p1 to p2 and p3
	//bijectionFiltered[happensBefore, p1, p2+p3]

	// Join from p2 and p3 to p4
	//bijectionFiltered[happensBefore, p2, p4]
	//bijectionFiltered[happensBefore, p3, p4]

	//p1 + p2 + p3 + p4 in this.steps and this.steps in p1 + p2 + p3 + p4


fact {all x: UnsatisfiableMultiplicity | #(x.p1) = 1}

fact {all x: UnsatisfiableMultiplicity | bijectionFiltered[happensBefore, x.p1, x.p2+x.p3]}
fact {all x: UnsatisfiableMultiplicity | bijectionFiltered[happensBefore, x.p2, x.p4]}
fact {all x: UnsatisfiableMultiplicity | bijectionFiltered[happensBefore, x.p3, x.p4]}

fact {all x: UnsatisfiableMultiplicity | x.p1 + x.p2 + x.p3 + x.p4 in x.steps}
fact {all x: UnsatisfiableMultiplicity | x.steps in x.p1 + x.p2 + x.p3 + x.p4}