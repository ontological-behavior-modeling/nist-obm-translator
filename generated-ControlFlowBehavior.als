// This file is created with code.

module ControlFlowBehavior
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig ControlFlowBehavior extends Occurrence { p1: set AtomicBehavior, p2: set AtomicBehavior, p3: set AtomicBehavior, p4: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact f1 { all s: ControlFlowBehavior | s.steps in s.p1 + s.p2 + s.p3 + s.p4 }
fact f2 { all s: ControlFlowBehavior | s.p1 + s.p2 + s.p3 + s.p4 in s.steps }
fact f3 { all s: ControlFlowBehavior | inverseFunctionFiltered[happensBefore, s.p3, s.p4] }
fact f4 { all s: ControlFlowBehavior | inverseFunctionFiltered[happensBefore, s.p2, s.p4] }
fact f5 { all s: ControlFlowBehavior | inverseFunctionFiltered[happensBefore, s.p1, s.p3] }
fact f6 { all s: ControlFlowBehavior | inverseFunctionFiltered[happensBefore, s.p1, s.p2] }
fact f7 { all s: ControlFlowBehavior | functionFiltered[happensBefore, s.p3, s.p4] }
fact f8 { all s: ControlFlowBehavior | functionFiltered[happensBefore, s.p1, s.p3] }
fact f9 { all s: ControlFlowBehavior | functionFiltered[happensBefore, s.p1, s.p2] }
fact f10 { all s: ControlFlowBehavior | functionFiltered[happensBefore, s.p2, s.p4] }

// Functions and predicates:
pred suppressTransfers { no Transfer }
pred suppressIO { no inputs and no outputs }
pred instancesDuringExample { p1DuringExample and p2DuringExample and p3DuringExample and p4DuringExample }
pred onlyControlFlowBehavior { #ControlFlowBehavior = 1 }
pred p1DuringExample { AtomicBehavior in ControlFlowBehavior.p1 }
pred p2DuringExample { AtomicBehavior in ControlFlowBehavior.p2 }
pred p3DuringExample { AtomicBehavior in ControlFlowBehavior.p3 }
pred p4DuringExample { AtomicBehavior in ControlFlowBehavior.p4 }

// Run commands
run ControlFlowBehavior{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyControlFlowBehavior} for 6
