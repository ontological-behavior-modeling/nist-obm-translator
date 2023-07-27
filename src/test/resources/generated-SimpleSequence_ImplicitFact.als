// This file is created with code.

module SimpleSequence_ImplicitFact
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig P1 extends Occurrence {}
sig P2 extends Occurrence {}
sig SimpleSequence extends Occurrence { p1: set P1, p2: set P2 }
{
	functionFiltered[happensBefore, p1, p2]
	inverseFunctionFiltered[happensBefore, p1, p2]
	#(p1) = 1
	p1 + p2 in this.steps
	this.steps in p1 + p2
}

// Facts:

// Functions and predicates:
pred suppressTransfers{no Transfer}
pred suppressIO{no inputs and no outputs}
pred instancesDuringExample{p1DuringExample and p2DuringExample}
pred onlySimpleSequence{#SimpleSequence = 1}
pred p1DuringExample{P1 in SimpleSequence.p1}
pred p2DuringExample{P2 in SimpleSequence.p2}

// Commands:
run SimpleSequence{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlySimpleSequence} for 6

