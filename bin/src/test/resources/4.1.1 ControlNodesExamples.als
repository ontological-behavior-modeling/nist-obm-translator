//*****************************************************************
// Module: 		ControlNodes
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model control nodes. 
//*****************************************************************
module ControlNodes
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig P1, P2, P3, P4, P5, P6, P7 extends Occurrence{}

//*****************************************************************
/** 					SimpleSequence */
//*****************************************************************
sig SimpleSequence extends Occurrence{
	p1: set P1,
	p2: set P2
}{
	functionFiltered[happensBefore, p1, p2]
	inverseFunctionFiltered[happensBefore, p1, p2]

	#p1 = 1

	p1 + p2 in this.steps and this.steps in p1 + p2
}

//*****************************************************************
/** 					Fork */
//*****************************************************************
sig Fork extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3
}{
	// How to tell a fork has happened?
	// A single set of source nodes and 2 or more sets of target nodes such that each source node instance 
	// 	maps to exactly 1 target node instance in each set of target nodes across the happensBefore relation
	// In other words, the subset of happensBefore tuples from the source node set to each target node set is a bijection
	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensBefore, p1, p3]

	#p1 = 1	// Multiplicity of P1

	p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}

//*****************************************************************
/** 					Join */
//*****************************************************************
sig Join extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3
}{
	// How to tell a join has happened?
	// 2 or more sets of source nodes and 1 set of target nodes such that each target node instance
	// 	maps from a single source node instance in each set of source nodes across the happensBefore relation
	// In other words, the subset of happensBefore tuples from the each set of source nodes to the target node set is a bijection
	bijectionFiltered[happensBefore, p1, p3]
	bijectionFiltered[happensBefore, p2, p3]

	#p1 = 1	// Multiplicity of P1
	#p2 = 1	// Multiplicity of P2

	p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}

//*****************************************************************
/** 					Decision */
//*****************************************************************
sig Decision extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3
}{
	// How to tell a decision has happened?
	// A single set of source nodes and 2 or more sets of target nodes such that each source node instance 
	// 	maps to exactly 1 target node instance in one of the sets of target nodes across the happensBefore relation
	// In other words, the subset of happensBefore tuples from the source node set to the union of the sets of target nodes is a bijection
	bijectionFiltered[happensBefore, p1, p2 + p3]

	#p1 = 1	// Multiplicity of P1

	p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}

//*****************************************************************
/** 					Merge */
//*****************************************************************
sig Merge extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3
}{
	// How to tell a merge has happened?
	// 2 or more sets of source nodes and 1 set of target nodes such that each target node instance
	// 	maps from a single source node instance in 1 of the sets of source nodes across the happensBefore relation
	// In other words, the subset of happensBefore tuples from the union of the sets of source nodes to the target node set is a bijection
	bijectionFiltered[happensBefore, p1 + p2, p3]

	#p1 = 1			// Multiplicity of P1
	#p2 = 1			// Multiplicity of P2

	p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}

//*****************************************************************
/** 					Complex Behavior */
//*****************************************************************
sig AllControl extends Occurrence{
	p1: set P1,
	p2: set P2,
	p3: set P3,
	p4: set P4,
	p5: set P5,
	p6: set P6,
	p7: set P7
}{
	// Fork
	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensBefore, p1, p3]
	// Join
	bijectionFiltered[happensBefore, p2, p4]
	bijectionFiltered[happensBefore, p3, p4]
	// Decision
	bijectionFiltered[happensBefore, p4, p5 + p6]
//	// Merge
	bijectionFiltered[happensBefore, p5 + p6, p7]

	#p1 = 1

	p1 + p2 + p3 + p4 + p5 + p6 + p7 in this.steps and this.steps in p1 + p2 + p3 + p4 + p5 + p6 + p7
}

//*****************************************************************
/** 				General Facts */
//*****************************************************************

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
//pred nonZeroDurationOnly{all occ: Occurrence | not o/isZeroDuration[occ]}
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
// These constraint extraneous atoms for P1-P7 from appearing randomly, to limit uninteresting examples from instantiating
pred p1DuringExample {P1 in (SimpleSequence.p1 + Fork.p1 + Join.p1 + Decision.p1 + Merge.p1 + AllControl.p1)}
pred p2DuringExample {P2 in (SimpleSequence.p2 + Fork.p2 + Join.p2 + Decision.p2 + Merge.p2 + AllControl.p2)}
pred p3DuringExample {P3 in (Fork.p3 + Join.p3 + Decision.p3 + Merge.p3 + AllControl.p3)}
pred p4DuringExample {P4 in (AllControl.p4)}
pred p5DuringExample {P5 in (AllControl.p5)}
pred p6DuringExample {P6 in (AllControl.p6)}
pred p7DuringExample {P7 in (AllControl.p7)}
pred instancesDuringExample {p1DuringExample and p2DuringExample and p3DuringExample and 
	p4DuringExample and p5DuringExample and p6DuringExample and p7DuringExample}
// These are used to limit the type and number of signatures that can instantiate.
pred onlySimpleSequence {#SimpleSequence = 1 and no Fork and no Join and no Decision and no Merge and no AllControl}
pred onlyFork {no SimpleSequence and #Fork = 1 and no Join and no Decision and no Merge and no AllControl}
pred onlyJoin {no SimpleSequence and no Fork and #Join = 1 and no Decision and no Merge and no AllControl}
pred onlyDecision {no SimpleSequence and no Fork and no Join and #Decision = 1 and no Merge and no AllControl}
pred onlyMerge {no SimpleSequence and no Fork and no Join and no Decision and #Merge = 1 and no AllControl}
pred onlyAllControl {no SimpleSequence and no Fork and no Join and no Decision and no Merge and #AllControl = 1}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run SimpleSequence{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlySimpleSequence} for 6
run fork{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyFork} for 10
run join{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyJoin} for 6
run decision{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyDecision} for 6
run merge{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyMerge} for 6
run AllControl{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyAllControl} for 10
