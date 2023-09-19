// This is a file seperated by MW from Unsatisfiable
// make use AutomaticBehavior
// r/asymmetric[happensBefore] is commented out.  I don't know when to create this.

//*****************************************************************
/** 					UnsatisfiableAsymmetry */
//*****************************************************************
module UnsatisfiableComposition2
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}


sig AtomicBehavior extends Occurrence {}
//*****************************************************************
/** 					UnsatisfiableComposition2 */
//*****************************************************************
sig UnsatisfiableComposition2 extends Occurrence {
disj p1,p2,p3: set AtomicBehavior
}/*{
	#p1 = 1

	r/transitive[happensBefore]
	r/asymmetric[happensBefore]			// Comment line to produce instance

	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensDuring, p3, p1]
	bijectionFiltered[happensBefore, p2, p3]

	p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}
*/
fact {all x: UnsatisfiableComposition2 | #(x.p1) = 1}


fact {all x: UnsatisfiableComposition2 | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: UnsatisfiableComposition2 | bijectionFiltered[happensDuring, x.p3, x.p1]}
fact {all x: UnsatisfiableComposition2 | bijectionFiltered[happensBefore, x.p2, x.p3]}


fact {all x: UnsatisfiableComposition2 | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: UnsatisfiableComposition2 | x.steps in x.p1 + x.p2 + x.p3}