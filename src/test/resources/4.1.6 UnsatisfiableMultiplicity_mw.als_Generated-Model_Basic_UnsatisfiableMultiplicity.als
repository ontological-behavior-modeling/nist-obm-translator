// This file is created with code.

module UnsatisfiableMultiplicity
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig UnsatisfiableMultiplicity extends Occurrence { disj p1, p2, p3, p4: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact {all x: UnsatisfiableMultiplicity | #(x.p1) = 1}
fact {all x: UnsatisfiableMultiplicity | bijectionFiltered[happensBefore, x.p1, x.p2 + x.p3]}
fact {all x: UnsatisfiableMultiplicity | bijectionFiltered[happensBefore, x.p3, x.p4]}
fact {all x: UnsatisfiableMultiplicity | bijectionFiltered[happensBefore, x.p2, x.p4]}
fact {all x: UnsatisfiableMultiplicity | x.p1 + x.p2 + x.p3 + x.p4 in x.steps}
fact {all x: UnsatisfiableMultiplicity | x.steps in x.p1 + x.p2 + x.p3 + x.p4}

