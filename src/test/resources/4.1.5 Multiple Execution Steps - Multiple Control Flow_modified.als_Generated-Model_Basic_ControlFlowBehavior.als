// This file is created with code.

module ControlFlowBehavior
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig ControlFlowBehavior extends Occurrence { disj p1, p2, p3, p4: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact {all x: ControlFlowBehavior | #(x.p1) = 2}
fact {all x: ControlFlowBehavior | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: ControlFlowBehavior | bijectionFiltered[happensBefore, x.p1, x.p3]}
fact {all x: ControlFlowBehavior | bijectionFiltered[happensBefore, x.p2, x.p4]}
fact {all x: ControlFlowBehavior | bijectionFiltered[happensBefore, x.p3, x.p4]}
fact {all x: ControlFlowBehavior | x.p1 + x.p2 + x.p3 + x.p4 in x.steps}
fact {all x: ControlFlowBehavior | x.steps in x.p1 + x.p2 + x.p3 + x.p4}

