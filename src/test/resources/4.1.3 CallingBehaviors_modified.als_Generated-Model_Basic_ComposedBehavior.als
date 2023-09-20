// This file is created with code.

module ComposedBehavior
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig ComposedBehavior extends Occurrence { disj p1, p3: set AtomicBehavior, p2: set NestedBehavior }
sig AtomicBehavior extends Occurrence {}
sig NestedBehavior extends Occurrence { disj p4, p5: set AtomicBehavior }

// Facts:
fact {all x: ComposedBehavior | #(x.p1) = 1}
fact {all x: ComposedBehavior | #(x.p3) = 1}
fact {all x: ComposedBehavior | #(x.p2) = 1}
fact {all x: NestedBehavior | #(x.p4) = 1}
fact {all x: NestedBehavior | #(x.p5) = 1}
fact {all x: ComposedBehavior | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: ComposedBehavior | bijectionFiltered[happensBefore, x.p2, x.p3]}
fact {all x: NestedBehavior | bijectionFiltered[happensBefore, x.p4, x.p5]}
fact {all x: ComposedBehavior | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: ComposedBehavior | x.steps in x.p1 + x.p2 + x.p3}
fact {all x: NestedBehavior | x.p4 + x.p5 in x.steps}
fact {all x: NestedBehavior | x.steps in x.p4 + x.p5}

