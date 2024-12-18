//*****************************************************************
// Module: 		MultipleExecutionSteps - MultipleObjectFlow
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model control nodes for
//				object flow. 
//*****************************************************************
module MultipleObjectFlowModule
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Integer extends Occurrence{}

fact {all x: Integer | no x.inputs}
fact {all x: Integer | no x.outputs}
fact {all x: Integer | no x.steps}
fact {all x: Integer | no steps.x}
//*****************************************************************
/** 					BehaviorWithParameter */
//*****************************************************************
sig BehaviorWithParameter extends Occurrence{i: set Integer}

fact {all x: BehaviorWithParameter | #(x.i) = 1}
// Model closure
fact {all x: BehaviorWithParameter | no x.steps}
fact {all x: BehaviorWithParameter | no inputs.x}
fact {all x: BehaviorWithParameter | no outputs.x}
fact {all x: BehaviorWithParameter | no items.x}
//*****************************************************************
/** 					MultipleObjectFlow */
//*****************************************************************
sig MultipleObjectFlow extends Occurrence {disj p1, p2, p3, p4: set BehaviorWithParameter, disj transferP1P2, transferP1P3, transferP2P4, transferP3P4: set Transfer}

fact {all x: MultipleObjectFlow | no x.inputs}
fact {all x: MultipleObjectFlow | no x.outputs}
fact {all x: MultipleObjectFlow | no inputs.x}
fact {all x: MultipleObjectFlow | no outputs.x}
fact {all x: MultipleObjectFlow | no items.x}

fact {all x: MultipleObjectFlow | x.p1.outputs in x.p1.i}
fact {all x: MultipleObjectFlow | x.p2.inputs in x.p2.i}
fact {all x: MultipleObjectFlow | x.p2.outputs in x.p2.i}
fact {all x: MultipleObjectFlow | x.p3.inputs in x.p3.i}
fact {all x: MultipleObjectFlow | x.p3.outputs in x.p3.i}
fact {all x: MultipleObjectFlow | x.p4.inputs in x.p4.i}
fact {all x: MultipleObjectFlow | no (x.transferP1P2 & x.transferP1P3 & x.transferP2P4 & x.transferP3P4)}

/** Constraints on p1: P1 */
fact {all x: MultipleObjectFlow | #(x.p1) = 2}
fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p1, x.p1.i]}
fact {all x: MultipleObjectFlow | all p: x.p1 | p.i in p.outputs}
fact {all x: MultipleObjectFlow | all p: x.p1 | p.outputs in p.i}
fact {all x: MultipleObjectFlow | no x.p1.inputs}

// Two Transfers specified to act as a fork
/** Constraints on the Transfer from p1 to p2 */
fact {all x: MultipleObjectFlow | bijectionFiltered[sources, x.transferP1P2, x.p1]}
fact {all x: MultipleObjectFlow | bijectionFiltered[targets, x.transferP1P2, x.p2]}
fact {all x: MultipleObjectFlow | subsettingItemRuleForSources[x.transferP1P2]}
fact {all x: MultipleObjectFlow | subsettingItemRuleForTargets[x.transferP1P2]}
fact {all x: MultipleObjectFlow | isAfterSource[x.transferP1P2]}
fact {all x: MultipleObjectFlow | isBeforeTarget[x.transferP1P2]}
fact {all x: MultipleObjectFlow | x.transferP1P2.items in x.transferP1P2.sources.i}
fact {all x: MultipleObjectFlow | x.transferP1P2.items in x.transferP1P2.targets.i}
fact {all x: MultipleObjectFlow | x.transferP1P2.sources.i in x.transferP1P2.items}
fact {all x: MultipleObjectFlow | x.transferP1P2.targets.i in x.transferP1P2.items}
/** Constraints on the Transfer from p1 to p3 */
fact {all x: MultipleObjectFlow | bijectionFiltered[sources, x.transferP1P3, x.p1]}
fact {all x: MultipleObjectFlow | bijectionFiltered[targets, x.transferP1P3, x.p3]}
fact {all x: MultipleObjectFlow | subsettingItemRuleForSources[x.transferP1P3]}
fact {all x: MultipleObjectFlow | subsettingItemRuleForTargets[x.transferP1P3]}
fact {all x: MultipleObjectFlow | isAfterSource[x.transferP1P3]}
fact {all x: MultipleObjectFlow | isBeforeTarget[x.transferP1P3]}
fact {all x: MultipleObjectFlow | x.transferP1P3.items in x.transferP1P3.sources.i}
fact {all x: MultipleObjectFlow | x.transferP1P3.items in x.transferP1P3.targets.i}
fact {all x: MultipleObjectFlow | x.transferP1P3.sources.i in x.transferP1P3.items}
fact {all x: MultipleObjectFlow | x.transferP1P3.targets.i in x.transferP1P3.items}

/** Constraints on p2: P2 */
fact {all x: MultipleObjectFlow | bijectionFiltered[inputs, x.p2, x.p2.i]}
fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p2, x.p2.i]}
fact {all x: MultipleObjectFlow | all p: x.p2 | p.i in p.inputs}
fact {all x: MultipleObjectFlow | all p: x.p2 | p.inputs in p.i}
fact {all x: MultipleObjectFlow | all p: x.p2 | p.i in p.outputs}
fact {all x: MultipleObjectFlow | all p: x.p2 | p.outputs in p.i}

/** Constraints on p3: P3 */
fact {all x: MultipleObjectFlow | bijectionFiltered[inputs, x.p3, x.p3.i]}
fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p3, x.p3.i]}
fact {all x: MultipleObjectFlow | all p: x.p3 | p.i in p.inputs}
fact {all x: MultipleObjectFlow | all p: x.p3 | p.inputs in p.i}
fact {all x: MultipleObjectFlow | all p: x.p3 | p.i in p.outputs}
fact {all x: MultipleObjectFlow | all p: x.p3 | p.outputs in p.i}

// Two Transfers specified to act as a join
/** Constraints on the Transfer from p2 to p4 */
fact {all x: MultipleObjectFlow | bijectionFiltered[sources, x.transferP2P4, x.p2]}
fact {all x: MultipleObjectFlow | bijectionFiltered[targets, x.transferP2P4, x.p4]}
fact {all x: MultipleObjectFlow | subsettingItemRuleForSources[x.transferP2P4]}
fact {all x: MultipleObjectFlow | subsettingItemRuleForTargets[x.transferP2P4]}
fact {all x: MultipleObjectFlow | isAfterSource[x.transferP2P4]}
fact {all x: MultipleObjectFlow | isBeforeTarget[x.transferP2P4]}
fact {all x: MultipleObjectFlow | x.transferP2P4.items in x.transferP2P4.sources.i}
fact {all x: MultipleObjectFlow | x.transferP2P4.items in x.transferP2P4.targets.i}
fact {all x: MultipleObjectFlow | x.transferP2P4.sources.i in x.transferP2P4.items}
fact {all x: MultipleObjectFlow | x.transferP2P4.targets.i in x.transferP2P4.items}
/** Constraints on the Transfer from p3 to p4 */
fact {all x: MultipleObjectFlow | bijectionFiltered[sources, x.transferP3P4, x.p3]}
fact {all x: MultipleObjectFlow | bijectionFiltered[targets, x.transferP3P4, x.p4]}
fact {all x: MultipleObjectFlow | subsettingItemRuleForSources[x.transferP3P4]}
fact {all x: MultipleObjectFlow | subsettingItemRuleForTargets[x.transferP3P4]}
fact {all x: MultipleObjectFlow | isAfterSource[x.transferP3P4]}
fact {all x: MultipleObjectFlow | isBeforeTarget[x.transferP3P4]}
fact {all x: MultipleObjectFlow | x.transferP3P4.items in x.transferP3P4.sources.i}
fact {all x: MultipleObjectFlow | x.transferP3P4.items in x.transferP3P4.targets.i}
fact {all x: MultipleObjectFlow | x.transferP3P4.sources.i in x.transferP3P4.items}
fact {all x: MultipleObjectFlow | x.transferP3P4.targets.i in x.transferP3P4.items}

/** Constraints on p4: P4 */
fact {all x: MultipleObjectFlow | bijectionFiltered[inputs, x.p4, x.p4.i]}
fact {all x: MultipleObjectFlow | all p: x.p4 | p.i in p.inputs}
fact {all x: MultipleObjectFlow | all p: x.p4 | p.inputs in p.i}
fact {all x: MultipleObjectFlow | no x.p4.outputs}

fact {all x: MultipleObjectFlow | x.p1 + x.p2 + x.p3 + x.p4 + x.transferP1P2 + x.transferP1P3 + x.transferP2P4 + x.transferP3P4 in x.steps}
fact {all x: MultipleObjectFlow | x.steps in x.p1 + x.p2 + x.p3 + x.p4 + x.transferP1P2 + x.transferP1P3 + x.transferP2P4 + x.transferP3P4}
//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred instancesDuringExample {BehaviorWithParameter in (MultipleObjectFlow.p1 + MultipleObjectFlow.p2 + MultipleObjectFlow.p3 + MultipleObjectFlow.p4) && Transfer in MultipleObjectFlow.steps}
pred strictOrdering {no MultipleObjectFlow.p1.~happensBefore && 
	(MultipleObjectFlow.steps - (MultipleObjectFlow.transferP1P2 + MultipleObjectFlow.transferP1P3)) not in MultipleObjectFlow.p1.happensBefore	&& 
	(MultipleObjectFlow.steps - MultipleObjectFlow.p1) not in MultipleObjectFlow.transferP1P2.~happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.p2) not in MultipleObjectFlow.transferP1P2.happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.p1) not in MultipleObjectFlow.transferP1P3.~happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.p3) not in MultipleObjectFlow.transferP1P3.happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.transferP1P2) not in MultipleObjectFlow.p2.~happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.transferP2P4) not in MultipleObjectFlow.p2.happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.transferP1P3) not in MultipleObjectFlow.p3.~happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.transferP3P4) not in MultipleObjectFlow.p3.happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.p2) not in MultipleObjectFlow.transferP2P4.~happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.p4) not in MultipleObjectFlow.transferP2P4.happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.p3) not in MultipleObjectFlow.transferP3P4.~happensBefore && 
	(MultipleObjectFlow.steps - MultipleObjectFlow.p4) not in MultipleObjectFlow.transferP3P4.happensBefore &&
	(MultipleObjectFlow.steps - (MultipleObjectFlow.transferP2P4 + MultipleObjectFlow.transferP3P4)) not in MultipleObjectFlow.p4.~happensBefore && 
//	p: MultipleObjectFlow.p1.happensBefore
	no MultipleObjectFlow.p4.happensBefore &&
	all t: TransferBefore | t not in (MultipleObjectFlow.steps - t.sources).happensBefore && t not in ((MultipleObjectFlow.steps - t.targets).~happensBefore)
}
pred noP1WithSharedOutputs {no m: MultipleObjectFlow | instancesDuringExample and (some disj p1', p1'': m.p1 | p1'.outputs = p1''.outputs)}
pred noP2WithSharedInputs {no m: MultipleObjectFlow | instancesDuringExample and (some disj p2', p2'': m.p2 | p2'.inputs = p2''.inputs)}
pred noP2WithSharedOutputs {no m: MultipleObjectFlow | instancesDuringExample and (some disj p2', p2'': m.p2 | p2'.outputs = p2''.outputs)}
pred noP3WithSharedInputs {no m: MultipleObjectFlow | instancesDuringExample and (some disj p3', p3'': m.p3 | p3'.inputs = p3''.inputs)}
pred noP3WithSharedOutputs {no m: MultipleObjectFlow | instancesDuringExample and (some disj p3', p3'': m.p3 | p3'.outputs = p3''.outputs)}
pred noP4WithSharedInputs {no m: MultipleObjectFlow | instancesDuringExample and (some disj p4', p4'': m.p4 | p4'.inputs = p4''.inputs)}
assert consistentIO {all b: BehaviorWithParameter | (#(b.outputs) > 0 => b.i = b.outputs) and (#(b.inputs) > 0 => b.i = b.inputs)}
assert assertNoP1WithSharedOutputs {noP1WithSharedOutputs}
assert assertNoP2WithSharedInputs {noP2WithSharedInputs}
assert assertNoP2WithSharedOutputs {noP2WithSharedOutputs}
assert assertNoP3WithSharedInputs {noP3WithSharedInputs}
assert assertNoP3WithSharedOutputs {noP3WithSharedOutputs}
assert assertNoP4WithSharedInputs {noP4WithSharedInputs}
assert noStepsWithSharedIO {noP1WithSharedOutputs and noP2WithSharedInputs and noP2WithSharedOutputs and noP3WithSharedInputs and noP3WithSharedOutputs and noP4WithSharedInputs}
//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run multipleObjectFlow{instancesDuringExample} for 19 but exactly 1 MultipleObjectFlow, 6 int

check consistentIO for 19 but exactly 1 MultipleObjectFlow
check noStepsWithSharedIO for 19 but exactly 1 MultipleObjectFlow
check assertNoP1WithSharedOutputs for 19 but exactly 1 MultipleObjectFlow
check assertNoP2WithSharedInputs for 19 but exactly 1 MultipleObjectFlow
check assertNoP2WithSharedOutputs for 19 but exactly 1 MultipleObjectFlow
check assertNoP3WithSharedInputs for 19 but exactly 1 MultipleObjectFlow
check assertNoP3WithSharedOutputs for 19 but exactly 1 MultipleObjectFlow
check assertNoP4WithSharedInputs for 19 but exactly 1 MultipleObjectFlow
