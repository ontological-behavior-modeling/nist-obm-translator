// This file is created with code.

module LoopsExample_ExplicitFact
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig P1 extends Occurrence {}
sig P2 extends Occurrence {}
sig P3 extends Occurrence {}
sig P4 extends Occurrence {}
sig Loop extends Occurrence { p1: set P1, p2: set P2, p3: set P3 }

// Facts:
fact f1 {all s: Loop | functionFiltered[happensBefore, s.p1, s.p2]}
fact f2 {all s: Loop | inverseFunctionFiltered[happensBefore, s.p1 + s.p2, s.p2]}
fact f3 {all s: Loop | functionFiltered[happensBefore, s.p2, s.p2 + s.p3]}
fact f4 {all s: Loop | inverseFunctionFiltered[happensBefore, s.p2, s.p3]}
fact f5 {all s: Loop | #(s.p1) = 1}
fact f6 {all s: Loop | #(s.p2) >= 2}
fact f7 {all s: Loop | #(s.p3) >= 1}
fact f8 {all s: Loop | s.p1 + s.p2 + s.p3 in s.steps}
fact f9 {all s: Loop | s.steps in s.p1 + s.p2 + s.p3}

// Functions and predicates:
pred suppressTransfers{no Transfer}
pred suppressIO{no inputs and no outputs}
pred instancesDuringExample{P1 in Loop.p1 and P2 in Loop.p2 and P3 in Loop.p3 and no P4}
pred onlyLoop{#Loop = 1}

// Commands:
run loop{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyLoop} for 12

