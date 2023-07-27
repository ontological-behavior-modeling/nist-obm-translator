// This file is created with code.

module LoopsExample
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig P1 extends Occurrence {}
sig P2 extends Occurrence {}
sig P3 extends Occurrence {}
sig P4 extends Occurrence {}
sig Loop extends Occurrence { p1: set P1, p2: set P2, p3: set P3 }
{
	functionFiltered[happensBefore, p1, p2]
	inverseFunctionFiltered[happensBefore, p1 + p2, p2]
	functionFiltered[happensBefore, p2, p2 + p3]
	inverseFunctionFiltered[happensBefore, p2, p3]
	#(p1) = 1
	#(p2) >= 2
	#(p3) >= 1
	p1 + p2 + p3 in this.steps
	this.steps in p1 + p2 + p3
}

// Facts:

// Functions and predicates:
pred suppressTransfers{no Transfer}
pred suppressIO{no inputs and no outputs}
pred instancesDuringExample{p1DuringExample and p2DuringExample and p3DuringExample and p4DuringExample}
pred onlyLoop{#Loop = 1}
pred p1DuringExample{P1 in Loop.p1}
pred p2DuringExample{P2 in Loop.p2}
pred p3DuringExample{P3 in Loop.p3}
pred p4DuringExample{no P4}

// Commands:
run loop{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyLoop} for 12

