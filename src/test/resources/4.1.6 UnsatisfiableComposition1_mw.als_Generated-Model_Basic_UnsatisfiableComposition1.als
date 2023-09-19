// This file is created with code.

module UnsatisfiableComposition1
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig UnsatisfiableComposition1 extends Occurrence { disj p1, p2, p3: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact {all x: UnsatisfiableComposition1 | #(x.p1) = 1}
fact {all x: UnsatisfiableComposition1 | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: UnsatisfiableComposition1 | bijectionFiltered[happensDuring, x.p3, x.p2]}
fact {all x: UnsatisfiableComposition1 | bijectionFiltered[happensBefore, x.p3, x.p1]}
fact {all x: UnsatisfiableComposition1 | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: UnsatisfiableComposition1 | x.steps in x.p1 + x.p2 + x.p3}

