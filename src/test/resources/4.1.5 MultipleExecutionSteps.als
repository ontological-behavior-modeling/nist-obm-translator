//*****************************************************************
// Module: 		MultipleExecutionSteps
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model control nodes. 
//*****************************************************************
module MultipleExecutionSteps
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig P1, P2, P3, P4, Real extends Occurrence{}

//*****************************************************************
/** 					MultipleControlFlow */
//*****************************************************************
sig MultipleControlFlow extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3,
	p4: set P4
}{
	#p1 = 2	// Multiplicity of P1

	// Fork from p1 to p2 and p3
	bijectionFiltered[happensBefore, p1, p2]
	bijectionFiltered[happensBefore, p1, p3]

	// Join from p2 and p3 to p4
	bijectionFiltered[happensBefore, p2, p4]
	bijectionFiltered[happensBefore, p3, p4]

	p1 + p2 + p3 + p4 in this.steps and this.steps in p1 + p2 + p3 + p4
}

//*****************************************************************
/** 					MultipleObjectFlow */
//*****************************************************************
sig MultipleObjectFlow extends Occurrence {
	p1: set P1,
	p2: set P2,
	p3: set P3,
	p4: set P4,
	disj transferP1P2, transferP1P3, transferP2P4, transferP3P4: set TransferBefore
}{
	no this.inputs
	no this.outputs

	/** Constraints on p1: P1 */
	#p1 = 2		// Multiplicity of p1
	p1.outputs in Real
	inverseFunctionFiltered[outputs, p1, Real]
	
	// Two Transfers specified to act as a fork
	/** Constraints on the Transfer from p1 to p2 */
	bijectionFiltered[sources, transferP1P2, p1]
	bijectionFiltered[targets, transferP1P2, p2]
	transferP1P2.items in Real
	subsettingItemRuleForSources[transferP1P2]
	subsettingItemRuleForTargets[transferP1P2]
	/** Constraints on the Transfer from p1 to p3 */
	bijectionFiltered[sources, transferP1P3, p1]
	bijectionFiltered[targets, transferP1P3, p3]
	transferP1P3.items in Real
	subsettingItemRuleForSources[transferP1P3]
	subsettingItemRuleForTargets[transferP1P3]

	/** Constraints on p2: P2 */
	eachOccInputsEqualOutputs[p2]
	p2.inputs in Real
	p2.outputs in Real
	p2.inputs = p2.outputs

	/** Constraints on p3: P3 */
	eachOccInputsEqualOutputs[p3]
	p3.inputs in Real
	p3.outputs in Real
	p3.inputs = p3.outputs

	// Two Transfers specified to act as a join
	/** Constraints on the Transfer from p2 to p4 */
	bijectionFiltered[sources, transferP2P4, p2]
	bijectionFiltered[targets, transferP2P4, p4]
	transferP2P4.items in Real
	subsettingItemRuleForSources[transferP2P4]
	subsettingItemRuleForTargets[transferP2P4]
	/** Constraints on the Transfer from p3 to p4 */
	bijectionFiltered[sources, transferP3P4, p3]
	bijectionFiltered[targets, transferP3P4, p4]
	transferP3P4.items in Real
	subsettingItemRuleForSources[transferP3P4]
	subsettingItemRuleForTargets[transferP3P4]

	/** Constraints on p4: P4 */
	p4.inputs in Real
	inverseFunctionFiltered[inputs, p4, Real]

	p1 + p2 + p3 + p4 + transferP1P2 + transferP1P3 + transferP2P4 + transferP3P4 in this.steps and 
		this.steps in p1 + p2 + p3 + p4 + transferP1P2 + transferP1P3 + transferP2P4 + transferP3P4
}

//*****************************************************************
/** 				General Facts */
//*****************************************************************


//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
pred p1DuringExample {P1 in (MultipleControlFlow.p1 + MultipleObjectFlow.p1)}
pred p2DuringExample {P2 in (MultipleControlFlow.p2 + MultipleObjectFlow.p2)}
pred p3DuringExample {P3 in (MultipleControlFlow.p3 + MultipleObjectFlow.p3)}
pred p4DuringExample {P4 in (MultipleControlFlow.p4 + MultipleObjectFlow.p4)}
pred instancesDuringExample {p1DuringExample and p2DuringExample and p3DuringExample and p4DuringExample}
pred onlyMultipleControlFlow {#MultipleControlFlow = 1 and no MultipleObjectFlow}
pred onlyMultipleObjectFlow {no MultipleControlFlow and #MultipleObjectFlow = 1}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run multipleControlFlow{suppressTransfers and suppressIO and instancesDuringExample and onlyMultipleControlFlow} for 9
run multipleObjectFlow{instancesDuringExample and onlyMultipleObjectFlow} for 19

