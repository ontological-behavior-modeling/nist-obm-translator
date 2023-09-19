// This file is created with code.

module Loop
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig Loop extends Occurrence { disj p1, p2, p3: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact {all x: Loop | #(x.p1) = 1}
fact {all x: Loop | #(x.p2) >= 2}
fact {all x: Loop | #(x.p3) = 1}
fact {all x: Loop | bijectionFiltered[happensBefore, x.p2, x.p2 + x.p3]}
fact {all x: Loop | bijectionFiltered[happensBefore, x.p1 + x.p2, x.p2]}
fact {all x: Loop | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: Loop | x.steps in x.p1 + x.p2 + x.p3}

