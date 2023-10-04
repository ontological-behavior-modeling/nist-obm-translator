//*****************************************************************
// Module: 		Parameter Behaviors
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence module to show
//				unsatisfiable conditions for models. 
//*****************************************************************
module Unsatisfiable
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig P1, P2, P3, P4 extends Occurrence{}
//*****************************************************************
/** 					UnsatisfiableMultiplicity */
//*****************************************************************
/**	UnsatisfiableMultiplicity */
sig UnsatisfiableMultiplicity extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3,
	p4: set P4
}{
	#p1 = 2	// Change multiplicity to 2 to produce instance

	// Decision from p1 to p2 and p3
	bijectionFiltered[happensBefore, p1, p2+p3]

	// Join from p2 and p3 to p4
	bijectionFiltered[happensBefore, p2, p4]
	bijectionFiltered[happensBefore, p3, p4]

	p1 + p2 + p3 + p4 in this.steps and this.steps in p1 + p2 + p3 + p4
}

fact {all x: UnsatisfiableMultiplicity | 

//*****************************************************************
/** 					UnsatisfiableAsymmetry */
//*****************************************************************
sig UnsatisfiableAsymmetry extends Occurrence {
	p1: set P1,
	p2: set P2
}{
	#p1 = 1
	#p2 = 1

//	r/asymmetric[happensBefore]	// Comment this line to produce instance

	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensBefore, p2, p1]

	p1 + p2 in this.steps and this.steps in p1 + p2
}
//*****************************************************************
/** 					UnsatisfiableTransitivity */
//*****************************************************************
sig UnsatisfiableTransitivity extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3
}{
	#p1 = 1

	// ^ is the transitive closure operator
//	r/transitive[happensBefore]			// Comment this line to produce an instance
	r/asymmetric[happensBefore]

	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensBefore, p2, p3]
	bijectionFiltered[happensBefore, p3, p1]

	p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}
//*****************************************************************
/** 					UnsatisfiableComposition1 */
//*****************************************************************
sig UnsatisfiableComposition1 extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3
}{
	#p1 = 1

	r/transitive[happensBefore]
	r/asymmetric[happensBefore]			// Comment line to produce instance

	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensDuring, p3, p2]
	bijectionFiltered[happensBefore, p3, p1]

	p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}
//*****************************************************************
/** 					UnsatisfiableComposition2 */
//*****************************************************************
sig UnsatisfiableComposition2 extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3
}{
	#p1 = 1

	r/transitive[happensBefore]
	r/asymmetric[happensBefore]			// Comment line to produce instance

	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensDuring, p3, p1]
	bijectionFiltered[happensBefore, p2, p3]

	p1 + p2 + p3 in this.steps and this.steps in p1 + p2 + p3
}

//******************************************************************************************************
/** 			General Functions and Predicates */
//******************************************************************************************************
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
pred p1DuringExample {P1 in (UnsatisfiableMultiplicity.p1 + UnsatisfiableAsymmetry.p1 + UnsatisfiableTransitivity.p1 + 
						UnsatisfiableComposition1.p1 + UnsatisfiableComposition2.p1)}
pred p2DuringExample {P2 in (UnsatisfiableMultiplicity.p2 + UnsatisfiableAsymmetry.p2 + UnsatisfiableTransitivity.p2 + 
						UnsatisfiableComposition1.p2 + UnsatisfiableComposition2.p2)}
pred p3DuringExample {P3 in (UnsatisfiableMultiplicity.p3 + UnsatisfiableTransitivity.p3 + 
						UnsatisfiableComposition1.p3 + UnsatisfiableComposition2.p3)}
pred p4DuringExample {P4 in (UnsatisfiableMultiplicity.p4)}
pred instancesDuringExample {p1DuringExample and p2DuringExample and p3DuringExample and p4DuringExample}
pred onlyUnsatisfiableMultiplicity{#UnsatisfiableMultiplicity = 1 and no UnsatisfiableAsymmetry and no UnsatisfiableTransitivity 
								and no UnsatisfiableComposition1 and no UnsatisfiableComposition2}
pred onlyUnsatisfiableAsymmetry {no UnsatisfiableMultiplicity and #UnsatisfiableAsymmetry = 1 and no UnsatisfiableTransitivity 
								 and no UnsatisfiableComposition1 and no UnsatisfiableComposition2}
pred onlyUnsatisfiableTransitivity {no UnsatisfiableMultiplicity and no UnsatisfiableAsymmetry and #UnsatisfiableTransitivity = 1 
								 and no UnsatisfiableComposition1 and no UnsatisfiableComposition2}
pred onlyUnsatisfiableComposition1 {no UnsatisfiableMultiplicity and no UnsatisfiableAsymmetry and no UnsatisfiableTransitivity 
								 and #UnsatisfiableComposition1 = 1 and no UnsatisfiableComposition2}
pred onlyUnsatisfiableComposition2 {no UnsatisfiableMultiplicity and no UnsatisfiableAsymmetry and no UnsatisfiableTransitivity 
								 and no UnsatisfiableComposition1 and #UnsatisfiableComposition2 = 1}
//******************************************************************************************************
/** 				Checks and Runs */
//******************************************************************************************************
run showUnsatisfiableMultiplicity {instancesDuringExample and nonZeroDurationOnly and suppressTransfers and suppressIO and onlyUnsatisfiableMultiplicity} for 15
run showUnsatisfiableAsymmetry {instancesDuringExample and suppressTransfers and suppressIO and onlyUnsatisfiableAsymmetry} for 15
run showUnsatisfiableTransitivity {instancesDuringExample and suppressTransfers and suppressIO and onlyUnsatisfiableTransitivity} for 15
run showUnsatisfiableComposition1 {instancesDuringExample and suppressTransfers and suppressIO and onlyUnsatisfiableComposition1} for 15
run showUnsatisfiableComposition2 {instancesDuringExample and suppressTransfers and suppressIO and onlyUnsatisfiableComposition2} for 15
