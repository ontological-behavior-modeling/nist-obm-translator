// This file is created with code.

module BehaviorDecision
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig BehaviorDecision extends Occurrence { disj p1, p2, p3: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact {all x: BehaviorDecision | #(x.p1) = 1}
fact {all x: BehaviorDecision | bijectionFiltered[happensBefore, x.p1, x.p2 + x.p3]}
fact {all x: BehaviorDecision | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: BehaviorDecision | x.steps in x.p1 + x.p2 + x.p3}

