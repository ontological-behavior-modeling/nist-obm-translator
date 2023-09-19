// This file is created with code.

module UnsatisfiableTransitivity
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig UnsatisfiableTransitivity extends Occurrence { disj p1, p2, p3: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact {all x: UnsatisfiableTransitivity | #(x.p1) = 1}
fact {all x: UnsatisfiableTransitivity | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: UnsatisfiableTransitivity | bijectionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: UnsatisfiableTransitivity | bijectionFiltered[happensBefore, x.p3, x.p1]}
fact {all x: UnsatisfiableTransitivity | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: UnsatisfiableTransitivity | x.steps in x.p1 + x.p2 + x.p3}

