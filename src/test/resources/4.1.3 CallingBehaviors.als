//*****************************************************************
// Module: 		Calling Behaviors
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model calling behaviors. 
//*****************************************************************
module CallingBehaviors
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig P1, P3, P4, P5 extends Occurrence{}

//*****************************************************************
/** 					NestedBehavior */
//*****************************************************************
sig NestedBehavior extends Occurrence {
	p4: set P4,
	p5: set P5
}{
	bijectionFiltered[happensBefore, p4, p5]

	#p4 = 1		// Multiplicity of P4

	p4 + p5 in this.steps and this.steps in p4 + p5
}

//*****************************************************************
/** 					ComposedBehavior */
//*****************************************************************
sig ComposedBehavior extends Occurrence{
	p1: set P1,
	p2: set NestedBehavior,
	p3: set P3
}{
	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensBefore, p2, p3]
	
	#p1 = 1		// Multiplicity of P1

	p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
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
pred p1DuringExample {P1 in ComposedBehavior.p1}
pred p2DuringExample {NestedBehavior in ComposedBehavior.p2}
pred p3DuringExample {P3 in ComposedBehavior.p3}
pred p4DuringExample {P4 in NestedBehavior.p4}
pred p5DuringExample {P5 in NestedBehavior.p5}
pred instancesDuringExample {p1DuringExample and p2DuringExample and p3DuringExample and 
	p4DuringExample and p5DuringExample}
pred onlyComposedBehavior {#ComposedBehavior = 1}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run composedBehavior{nonZeroDurationOnly and suppressTransfers and suppressIO and 
				instancesDuringExample and onlyComposedBehavior} for 6


