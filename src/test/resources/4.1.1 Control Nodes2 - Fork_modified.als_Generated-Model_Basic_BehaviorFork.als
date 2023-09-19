// This file is created with code.

module BehaviorFork
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig BehaviorFork extends Occurrence { disj p1, p2, p3: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact {all x: BehaviorFork | #(x.p1) = 1}
fact {all x: BehaviorFork | bijectionFiltered[happensBefore, x.p1, x.p3]}
fact {all x: BehaviorFork | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: BehaviorFork | x.p1 + x.p2 + x.p3 in x.steps}
fact {all x: BehaviorFork | x.steps in x.p1 + x.p2 + x.p3}

