// This file is created with code.

module ComplexBehavior
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig ComplexBehavior extends Occurrence { disj p1, p2, p3, p4, p5, p6, p7: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact {all x: ComplexBehavior | #(x.p1) = 1}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p4, x.p5 + x.p6]}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p5 + x.p6, x.p7]}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p1, x.p3]}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p3, x.p4]}
fact {all x: ComplexBehavior | bijectionFiltered[happensBefore, x.p2, x.p4]}
fact {all x: ComplexBehavior | x.p1 + x.p2 + x.p3 + x.p4 + x.p5 + x.p6 + x.p7 in x.steps}
fact {all x: ComplexBehavior | x.steps in x.p1 + x.p2 + x.p3 + x.p4 + x.p5 + x.p6 + x.p7}

