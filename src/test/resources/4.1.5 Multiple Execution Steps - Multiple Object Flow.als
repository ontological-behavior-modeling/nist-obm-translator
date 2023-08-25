//*****************************************************************
// Module: 		MultipleExecutionSteps - MultipleObjectFlow
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model control nodes for
//				object flow. 
//*****************************************************************
module MultipleExecutionSteps
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig BehaviorWithParameter, Real extends Occurrence{}

//*****************************************************************
/** 					MultipleObjectFlow */
//*****************************************************************
sig MultipleObjectFlow extends Occurrence {
	disj p1,p2,p3,p4: set BehaviorWithParameter,
	disj transferP1P2, transferP1P3, transferP2P4, transferP3P4: set TransferBefore
}{
	no this.inputs
	no this.outputs

	/** Constraints on p1: P1 */
	#p1 = 2		// Multiplicity of p1
	p1.outputs in Real
	bijectionFiltered[outputs, p1, Real]
	
	// Two Transfers specified to act as a fork
	/** Constraints on the Transfer from p1 to p2 */
	bijectionFiltered[sources, transferP1P2, p1]
	bijectionFiltered[targets, transferP1P2, p2]
	subsettingItemRuleForSources[transferP1P2]
	subsettingItemRuleForTargets[transferP1P2]
	/** Constraints on the Transfer from p1 to p3 */
	bijectionFiltered[sources, transferP1P3, p1]
	bijectionFiltered[targets, transferP1P3, p3]
	subsettingItemRuleForSources[transferP1P3]
	subsettingItemRuleForTargets[transferP1P3]

	/** Constraints on p2: P2 */
	eachOccInputsEqualOutputs[p2]
	p2.inputs in Real
	p2.outputs in Real
	p2.inputs = p2.outputs
	bijectionFiltered[inputs, p2, Real]
	bijectionFiltered[outputs, p2, Real]

	/** Constraints on p3: P3 */
	eachOccInputsEqualOutputs[p3]
	p3.inputs in Real
	p3.outputs in Real
	p3.inputs = p3.outputs
	bijectionFiltered[inputs, p3, Real]
	bijectionFiltered[outputs, p3, Real]

	// Two Transfers specified to act as a join
	/** Constraints on the Transfer from p2 to p4 */
	bijectionFiltered[sources, transferP2P4, p2]
	bijectionFiltered[targets, transferP2P4, p4]
	subsettingItemRuleForSources[transferP2P4]
	subsettingItemRuleForTargets[transferP2P4]
	/** Constraints on the Transfer from p3 to p4 */
	bijectionFiltered[sources, transferP3P4, p3]
	bijectionFiltered[targets, transferP3P4, p4]
	subsettingItemRuleForSources[transferP3P4]
	subsettingItemRuleForTargets[transferP3P4]

	/** Constraints on p4: P4 */
	p4.inputs in Real
	bijectionFiltered[inputs, p4, Real]

	p1 + p2 + p3 + p4 + transferP1P2 + transferP1P3 + transferP2P4 + transferP3P4 in this.steps and 
		this.steps in p1 + p2 + p3 + p4 + transferP1P2 + transferP1P3 + transferP2P4 + transferP3P4
}

//fact f{all x: MultipleObjectFlow | no x.inputs}
//fact f{all x: MultipleObjectFlow | no x.outputs}
///** Constraints on p1: P1 */
//fact f{all x: MultipleObjectFlow | #x.p1 = 2}
//fact f{all x: MultipleObjectFlow | x.p1.outputs in Real}
//fact f{all x: MultipleObjectFlow | inverseFunctionFiltered[outputs, x.p1, Real]}
///**Two Transfers specified to act as a fork
//	/** Constraints on the Transfer from p1 to p2 */
//fact f{all x: MultipleObjectFlow | bijectionFiltered[sources, x.transferP1P2, x.p1]}
//fact f{all x: MultipleObjectFlow | bijectionFiltered[targets, x.transferP1P2, x.p2]}
//fact f{all x: MultipleObjectFlow | x.transferP1P2.items in Real}
//fact f{all x: MultipleObjectFlow | subsettingItemRuleForSources[x.transferP1P2]}
//fact f{all x: MultipleObjectFlow | subsettingItemRuleForTargets[x.transferP1P2]}
//	/** Constraints on the Transfer from p1 to p3 */
//fact f{all x: MultipleObjectFlow | bijectionFiltered[sources, x.transferP1P3, x.p1]}
//fact f{all x: MultipleObjectFlow | bijectionFiltered[targets, x.transferP1P3, x.p3]}
//fact f{all x: MultipleObjectFlow | x.transferP1P3.items in Real}
//fact f{all x: MultipleObjectFlow | subsettingItemRuleForSources[x.transferP1P3]}
//fact f{all x: MultipleObjectFlow | subsettingItemRuleForTargets[x.transferP1P3]}
///** Constraints on p2: P2 */
//fact f{all x: MultipleObjectFlow | eachOccInputsEqualOutputs[x.p2]}
//fact f{all x: MultipleObjectFlow | x.p2.inputs in Real}
//fact f{all x: MultipleObjectFlow | x.p2.outputs in Real}
//fact f{all x: MultipleObjectFlow | x.p2.inputs = x.p2.outputs}
///** Constraints on p3: P3 */
//fact f{all x: MultipleObjectFlow | eachOccInputsEqualOutputs[x.p3]}
//fact f{all x: MultipleObjectFlow | x.p3.inputs in Real}
//fact f{all x: MultipleObjectFlow | x.p3.outputs in Real}
//fact f{all x: MultipleObjectFlow | x.p3.inputs = p3.outputs}
///**Two Transfers specified to act as a join */
//	/** Constraints on the Transfer from p2 to p4 */
//fact f{all x: MultipleObjectFlow | bijectionFiltered[sources, x.transferP2P4, x.p2]}
//fact f{all x: MultipleObjectFlow | bijectionFiltered[targets, x.transferP2P4, x.p4]}
//fact f{all x: MultipleObjectFlow | x.transferP2P4.items in Real}
//fact f{all x: MultipleObjectFlow | subsettingItemRuleForSources[x.transferP2P4]}
//fact f{all x: MultipleObjectFlow | subsettingItemRuleForTargets[x.transferP2P4]}
//	/** Constraints on the Transfer from p3 to p4 */
//fact f{all x: MultipleObjectFlow | bijectionFiltered[sources, x.transferP3P4, x.p3]}
//fact f{all x: MultipleObjectFlow | bijectionFiltered[targets, x.transferP3P4, x.p4]}
//fact f{all x: MultipleObjectFlow | x.transferP3P4.items in Real}
//fact f{all x: MultipleObjectFlow | subsettingItemRuleForSources[x.transferP3P4]}
//fact f{all x: MultipleObjectFlow | subsettingItemRuleForTargets[x.transferP3P4]}
///** Constraints on p4: P4 */
//fact f{all x: MultipleObjectFlow | x.p4.inputs in Real}
//fact f{all x: MultipleObjectFlow | inverseFunctionFiltered[inputs, x.p4, Real]}
///** Model closure */
//fact f{all x: MultipleObjectFlow | x.p1 + x.p2 + x.p3 + x.p4 + x.transferP1P2 + x.transferP1P3 + x.transferP2P4 + x.transferP3P4 in this.steps}
//fact f{all x: MultipleObjectFlow | x.steps in x.p1 + x.p2 + x.p3 + x.p4 + x.transferP1P2 + x.transferP1P3 + x.transferP2P4 + x.transferP3P4}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred instancesDuringExample {BehaviorWithParameter in MultipleObjectFlow.p1 + MultipleObjectFlow.p2 + MultipleObjectFlow.p3 + MultipleObjectFlow.p4}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run multipleObjectFlow{instancesDuringExample and some MultipleObjectFlow} for 19
check {no x: MultipleObjectFlow | x in Transfer.outputs and noTransferInputsOrOutputs[Transfer]} for 20

