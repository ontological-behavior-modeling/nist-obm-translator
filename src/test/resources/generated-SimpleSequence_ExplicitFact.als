// This file is created with code.

module SimpleSequence_ExplicitFact
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig P1 extends Occurrence {}
sig P2 extends Occurrence {}
sig SimpleSequence extends Occurrence { p1: set P1, p2: set P2 }

// Facts:
fact f1 {all s: SimpleSequence | #(s.p1) = 1}
fact f2 {all s: SimpleSequence | s.p1 + s.p2 in s.steps}
fact f3 {all s: SimpleSequence | s.steps in s.p1 + s.p2}
fact f4 {all s: SimpleSequence | functionFiltered[happensBefore, s.p1, s.p2]}
fact f5 {all s: SimpleSequence | inverseFunctionFiltered[happensBefore, s.p1, s.p2]}

// Functions and predicates:
pred suppressTransfers{no Transfer}
pred suppressIO{no inputs and no outputs}
pred instancesDuringExample{p1DuringExample and p2DuringExample}
pred onlySimpleSequence{#SimpleSequence = 1}
pred p1DuringExample{P1 in SimpleSequence.p1}
pred p2DuringExample{P2 in SimpleSequence.p2}

// Commands:
run SimpleSequence{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlySimpleSequence} for 6

