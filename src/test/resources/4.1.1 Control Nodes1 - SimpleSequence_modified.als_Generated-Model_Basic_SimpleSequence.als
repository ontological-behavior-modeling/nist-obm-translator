// This file is created with code.

module SimpleSequence
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig SimpleSequence extends Occurrence { disj p1, p2: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact {all x: SimpleSequence | #(x.p1) = 1}
fact {all x: SimpleSequence | #(x.p2) = 1}
fact {all x: SimpleSequence | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: SimpleSequence | x.p1 + x.p2 in x.steps}
fact {all x: SimpleSequence | x.steps in x.p1 + x.p2}

