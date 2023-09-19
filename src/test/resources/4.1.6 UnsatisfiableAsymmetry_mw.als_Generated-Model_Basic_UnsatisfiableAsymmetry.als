// This file is created with code.

module UnsatisfiableAsymmetry
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig UnsatisfiableAsymmetry extends Occurrence { disj p1, p2: set AtomicBehavior }
sig AtomicBehavior extends Occurrence {}

// Facts:
fact {all x: UnsatisfiableAsymmetry | #(x.p1) = 1}
fact {all x: UnsatisfiableAsymmetry | bijectionFiltered[happensBefore, x.p1, x.p2]}
fact {all x: UnsatisfiableAsymmetry | bijectionFiltered[happensBefore, x.p2, x.p1]}
fact {all x: UnsatisfiableAsymmetry | x.p1 + x.p2 in x.steps}
fact {all x: UnsatisfiableAsymmetry | x.steps in x.p1 + x.p2}

