// This file is created with code.

module ControlNodes
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig P1 extends Occurrence {}
sig P2 extends Occurrence {}
sig P3 extends Occurrence {}
sig P4 extends Occurrence {}
sig P5 extends Occurrence {}
sig P6 extends Occurrence {}
sig P7 extends Occurrence {}
sig SimpleSequence extends Occurrence { p1: set P1, p2: set P2 }
{
	functionFiltered[happensBefore, p1, p2]
	inverseFunctionFiltered[happensBefore, p1, p2]
	#(p1) = 1
	p1 + p2 in this.steps
	this.steps in p1 + p2
}
sig Fork extends Occurrence { p1: set P1, p2: set P2, p3: set P3 }
{
	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensBefore, p1, p3]
	#(p1) = 1
	p1 + p2 + p3 in this.steps
	this.steps in p1 + p2 + p3
}
sig Join extends Occurrence { p1: set P1, p2: set P2, p3: set P3 }
{
	bijectionFiltered[happensBefore, p1, p3]
	bijectionFiltered[happensBefore, p2, p3]
	#(p1) = 1
	#(p2) = 1
	p1 + p2 + p3 in this.steps
	this.steps in p1 + p2 + p3
}
sig Decision extends Occurrence { p1: set P1, p2: set P2, p3: set P3 }
{
	bijectionFiltered[happensBefore, p1, p2 + p3]
	#(p1) = 1
	p1 + p2 + p3 in this.steps
	this.steps in p1 + p2 + p3
}
sig Merge extends Occurrence { p1: set P1, p2: set P2, p3: set P3 }
{
	bijectionFiltered[happensBefore, p1 + p2, p3]
	#(p1) = 1
	#(p2) = 1
	p1 + p2 + p3 in this.steps
	this.steps in p1 + p2 + p3
}
sig AllControl extends Occurrence { p1: set P1, p2: set P2, p3: set P3, p4: set P4, p5: set P5, p6: set P6, p7: set P7 }
{
	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensBefore, p1, p3]
	bijectionFiltered[happensBefore, p2, p4]
	bijectionFiltered[happensBefore, p3, p4]
	bijectionFiltered[happensBefore, p4, p5 + p6]
	bijectionFiltered[happensBefore, p5 + p6, p7]
	#(p1) = 1
	p1 + p2 + p3 + p4 + p5 + p6 + p7 in this.steps
	this.steps in p1 + p2 + p3 + p4 + p5 + p6 + p7
}

// Facts:

// Functions and predicates:
pred suppressTransfers{no Transfer}
pred suppressIO{no inputs and no outputs}
pred instancesDuringExample{p1DuringExample and p2DuringExample and p3DuringExample and p4DuringExample and p5DuringExample and p6DuringExample and p7DuringExample}
pred onlySimpleSequence{#SimpleSequence = 1 and no Fork and no Join and no Decision and no Merge and no AllControl}
pred p1DuringExample{P1 in SimpleSequence.p1 + Fork.p1 + Join.p1 + Decision.p1 + Merge.p1 + AllControl.p1}
pred p2DuringExample{P2 in SimpleSequence.p2 + Fork.p2 + Join.p2 + Decision.p2 + Merge.p2 + AllControl.p2}
pred p3DuringExample{P3 in Fork.p3 + Join.p3 + Decision.p3 + Merge.p3 + AllControl.p3}
pred p4DuringExample{P4 in AllControl.p4}
pred p5DuringExample{P5 in AllControl.p5}
pred p6DuringExample{P6 in AllControl.p6}
pred p7DuringExample{P7 in AllControl.p7}
pred onlyFork{no SimpleSequence and #Fork = 1 and no Join and no Decision and no Merge and no AllControl}
pred onlyJoin{no SimpleSequence and no Fork and #Join = 1 and no Decision and no Merge and no AllControl}
pred onlyDecision{no SimpleSequence and no Fork and no Join and #Decision = 1 and no Merge and no AllControl}
pred onlyMerge{no SimpleSequence and no Fork and no Join and no Decision and #Merge = 1 and no AllControl}
pred onlyAllControl{no SimpleSequence and no Fork and no Join and no Decision and no Merge and #AllControl = 1}

// Commands:
run SimpleSequence{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlySimpleSequence} for 6
run fork{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyFork} for 10
run join{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyJoin} for 6
run decision{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyDecision} for 6
run merge{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyMerge} for 6
run AllControl{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyAllControl} for 10

