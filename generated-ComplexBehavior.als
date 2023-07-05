// This file is created with code.

module ComplexBehavior
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig ComplexBehavior extends Occurrence { p1: set AtomicBehavior, p2: set AtomicBehavior, p3: set AtomicBehavior, p4: set AtomicBehavior, p5: set AtomicBehavior, p6: set AtomicBehavior, p7: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact f1 { all s: ComplexBehavior | s.steps in s.p1 + s.p2 + s.p3 + s.p4 + s.p5 + s.p6 + s.p7 }
fact f2 { all s: ComplexBehavior | s.p1 + s.p2 + s.p3 + s.p4 + s.p5 + s.p6 + s.p7 in s.steps }
fact f3 { all s: ComplexBehavior | inverseFunctionFiltered[happensBefore, s.p5, s.p7] }
fact f4 { all s: ComplexBehavior | inverseFunctionFiltered[happensBefore, s.p6, s.p7] }
fact f5 { all s: ComplexBehavior | inverseFunctionFiltered[happensBefore, s.p4, s.p5] }
fact f6 { all s: ComplexBehavior | inverseFunctionFiltered[happensBefore, s.p2, s.p4] }
fact f7 { all s: ComplexBehavior | inverseFunctionFiltered[happensBefore, s.p3, s.p4] }
fact f8 { all s: ComplexBehavior | inverseFunctionFiltered[happensBefore, s.p4, s.p6] }
fact f9 { all s: ComplexBehavior | inverseFunctionFiltered[happensBefore, s.p1, s.p3] }
fact f10 { all s: ComplexBehavior | inverseFunctionFiltered[happensBefore, s.p1, s.p2] }
fact f11 { all s: ComplexBehavior | functionFiltered[happensBefore, s.p5, s.p7] }
fact f12 { all s: ComplexBehavior | functionFiltered[happensBefore, s.p6, s.p7] }
fact f13 { all s: ComplexBehavior | functionFiltered[happensBefore, s.p4, s.p6] }
fact f14 { all s: ComplexBehavior | functionFiltered[happensBefore, s.p4, s.p5] }
fact f15 { all s: ComplexBehavior | functionFiltered[happensBefore, s.p1, s.p3] }
fact f16 { all s: ComplexBehavior | functionFiltered[happensBefore, s.p1, s.p2] }
fact f17 { all s: ComplexBehavior | functionFiltered[happensBefore, s.p3, s.p4] }
fact f18 { all s: ComplexBehavior | functionFiltered[happensBefore, s.p2, s.p4] }
fact f19 { all s: ComplexBehavior | #(s.p1) = 1 }

// Functions and predicates:
pred suppressTransfers { no Transfer }
pred suppressIO { no inputs and no outputs }
pred instancesDuringExample { p1DuringExample and p2DuringExample and p3DuringExample and p4DuringExample and p5DuringExample and p6DuringExample and p7DuringExample }
pred onlyComplexBehavior { #ComplexBehavior = 1 }
pred p1DuringExample { AtomicBehavior in ComplexBehavior.p1 }
pred p2DuringExample { AtomicBehavior in ComplexBehavior.p2 }
pred p3DuringExample { AtomicBehavior in ComplexBehavior.p3 }
pred p4DuringExample { AtomicBehavior in ComplexBehavior.p4 }
pred p5DuringExample { AtomicBehavior in ComplexBehavior.p5 }
pred p6DuringExample { AtomicBehavior in ComplexBehavior.p6 }
pred p7DuringExample { AtomicBehavior in ComplexBehavior.p7 }

// Run commands
run ComplexBehavior{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyComplexBehavior} for 6
