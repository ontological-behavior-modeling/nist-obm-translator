// This file is created with code.

module BehaviorJoin
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig BehaviorJoin extends Occurrence { p1: set AtomicBehavior, p2: set AtomicBehavior, p3: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact f1 { all s: BehaviorJoin | s.steps in s.p1 + s.p2 + s.p3 }
fact f2 { all s: BehaviorJoin | s.p1 + s.p2 + s.p3 in s.steps }
fact f3 { all s: BehaviorJoin | inverseFunctionFiltered[happensBefore, s.p2, s.p3] }
fact f4 { all s: BehaviorJoin | inverseFunctionFiltered[happensBefore, s.p1, s.p3] }
fact f5 { all s: BehaviorJoin | functionFiltered[happensBefore, s.p1, s.p3] }
fact f6 { all s: BehaviorJoin | functionFiltered[happensBefore, s.p2, s.p3] }
fact f7 { all s: BehaviorJoin | #(s.p2) = 1 }
fact f8 { all s: BehaviorJoin | #(s.p1) = 1 }

// Functions and predicates:
pred suppressTransfers { no Transfer }
pred suppressIO { no inputs and no outputs }
pred instancesDuringExample { p1DuringExample and p2DuringExample and p3DuringExample }
pred onlyBehaviorJoin { #BehaviorJoin = 1 }
pred p1DuringExample { AtomicBehavior in BehaviorJoin.p1 }
pred p2DuringExample { AtomicBehavior in BehaviorJoin.p2 }
pred p3DuringExample { AtomicBehavior in BehaviorJoin.p3 }

// Run commands
run BehaviorJoin{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyBehaviorJoin} for 6
