// This file is created with code.

module BehaviorDecision
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig BehaviorDecision extends Occurrence { p1: set AtomicBehavior, p2: set AtomicBehavior, p3: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact f1 { all s: BehaviorDecision | s.steps in s.p1 + s.p2 + s.p3 }
fact f2 { all s: BehaviorDecision | s.p1 + s.p2 + s.p3 in s.steps }
fact f3 { all s: BehaviorDecision | inverseFunctionFiltered[happensBefore, s.p1, s.p3] }
fact f4 { all s: BehaviorDecision | inverseFunctionFiltered[happensBefore, s.p1, s.p2] }
fact f5 { all s: BehaviorDecision | functionFiltered[happensBefore, s.p1, s.p3] }
fact f6 { all s: BehaviorDecision | functionFiltered[happensBefore, s.p1, s.p2] }
fact f7 { all s: BehaviorDecision | #(s.p1) = 1 }

// Functions and predicates:
pred suppressTransfers { no Transfer }
pred suppressIO { no inputs and no outputs }
pred instancesDuringExample { p1DuringExample and p2DuringExample and p3DuringExample }
pred onlyBehaviorDecision { #BehaviorDecision = 1 }
pred p1DuringExample { AtomicBehavior in BehaviorDecision.p1 }
pred p2DuringExample { AtomicBehavior in BehaviorDecision.p2 }
pred p3DuringExample { AtomicBehavior in BehaviorDecision.p3 }

// Run commands
run BehaviorDecision{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyBehaviorDecision} for 6
