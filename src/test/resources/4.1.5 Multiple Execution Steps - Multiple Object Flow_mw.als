//*****************************************************************
// Module: 		MultipleExecutionSteps - MultipleObjectFlow
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model control nodes for
//				object flow. 
//*****************************************************************
//MW
//1) Module MultipleExecutionSteps is changed to ObjectFlowBehavior
//2) Sig MultipleObjectFlow is changed to ObjectFlowBehavior
//3) Use Integer instead of Real
//4) Sig BehaviorWithParameter having i: set Integer
//5) Change sig ObjectFlowBehavior trasferP1P2 to transferbeforeP1P2 because its type is TransferBefore
//similar for tarnsferP1P3, ransferP2P4, transferP3P4.  They are not in OBMModel but created based on connectors
//5) functionFiltered and inverseFunctionFiltered are combined to bijectionFiltered

module ObjectFlowBehavior
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig BehaviorWithParameter extends Occurrence
{
	i: set Integer 
}
sig Integer extends Occurrence{}

//*****************************************************************
/** 					MultipleObjectFlow */
//*****************************************************************
sig ObjectFlowBehavior extends Occurrence {
	disj p1,p2,p3,p4: set BehaviorWithParameter,
	disj transferbeforeP1P2, transferbeforeP1P3, transferbeforeP2P4, transferbeforeP3P4: set TransferBefore
}


//not implemented yet
//no this.inputs
fact {all x: ObjectFlowBehavior | no x.inputs}
//no this.outputs
fact {all x: ObjectFlowBehavior | no x.outputs}
//inverseFunctionFiltered[outputs, p1, Real]
fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[outputs, x.p1, i]}
//inverseFunctionFiltered[inputs, p4, Real]
fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[inputs, x.p4, i]}
//p1.outputs in Real
fact {all x: ObjectFlowBehavior, y: x.p1 | y.outputs in i}
//p2.inputs in Real
//p2.outputs in Real
fact {all x: ObjectFlowBehavior | all y: x.p2 | y.inputs in i}
fact {all x: ObjectFlowBehavior, y: x.p2 | y.outputs in i}
//p3.inputs in Real
//p3.outputs in Real
fact {all x: ObjectFlowBehavior, y: x.p3 | y.inputs in i}
fact {all x: ObjectFlowBehavior, y: x.p3 | y.outputs in i}
//p4.inputs in Real
fact {all x: ObjectFlowBehavior, y: x.p4 | y.inputs in i}
//transferP1P2.items in Real
fact {all x: ObjectFlowBehavior, y: x.transferP1P2 | y.items in i}
//transferP1P3.items in Real
fact {all x: ObjectFlowBehavior, y: x.transferP1P3 | y.items in i}
//transferP2P4.items in Real
fact {all x: ObjectFlowBehavior, y: x.transferP2P4 | y.items in i}
//transferP3P4.items in Real
fact {all x: ObjectFlowBehavior, y: x.transferP3P4 | y.items in i}

//eachOccInputsEqualOutputs[p2]
fact {all x: ObjectFlowBehavior| eachOccInputsEqualOutputs[x.p2]}
//eachOccInputsEqualOutputs[p3]
fact {all x: ObjectFlowBehavior| eachOccInputsEqualOutputs[x.p3]}

//Are this the same as eachOccInputsEqualOutputs[p2] eachOccInputsEqualOutputs[p3]
//don't know how to make explicit facts
//????????????????????????????
//p2.inputs = p2.outputs
//p3.inputs = p3.outputs


/////////////////////// done ////////////////////////////
//Constraints on p1: P1 
fact {all x: ObjectFlowBehavior | #(x.p1) = 2}
//Two Transfers specified to act as a fork
/** Constraints on the Transfer from p1 to p2 */
//fact {all x: ObjectFlowBehavior | functionFiltered[sources, x.transferbeforeP1P2, x.p1]}
//fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[sources, x.transferbeforeP1P2, x.p1]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[sources, x.transferbeforeP1P2, x.p1]}

//fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[targets, x.transferbeforeP1P2, x.p2]}
//fact {all x: ObjectFlowBehavior | functionFiltered[targets, x.transferbeforeP1P2, x.p2]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[targets, x.transferbeforeP1P2, x.p2]}

//????????
fact {all x: ObjectFlowBehavior | x.transferbeforeP1P2.items in Integer} 

fact {all x: ObjectFlowBehavior | subsettingItemRuleForSources[x.transferbeforeP1P2]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForTargets[x.transferbeforeP1P2]}

/** Constraints on the Transfer from p1 to p3 */
//fact {all x: ObjectFlowBehavior | functionFiltered[sources, x.transferbeforeP1P3, x.p1]}
//fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[sources, x.transferbeforeP1P3, x.p1]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[sources, x.transferbeforeP1P3, x.p1]}

//fact {all x: ObjectFlowBehavior | functionFiltered[targets, x.transferbeforeP1P3, x.p3]}
//fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[targets, x.transferbeforeP1P3, x.p3]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[targets, x.transferbeforeP1P3, x.p3]}

//????????
fact {all x: ObjectFlowBehavior | x.transferbeforeP1P3.items in Integer}

fact {all x: ObjectFlowBehavior | subsettingItemRuleForSources[x.transferbeforeP1P3]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForTargets[x.transferbeforeP1P3]}

/** Constraints on p2: P2 */
fact {all x: ObjectFlowBehavior | eachOccInputsEqualOutputs[x.p2]}
fact {all x: ObjectFlowBehavior | x.p2.inputs in Integer}
fact {all x: ObjectFlowBehavior | x.p2.outputs in Integer}
fact {all x: ObjectFlowBehavior | x.p2.inputs = x.p2.outputs}
///** Constraints on p3: P3 */
fact {all x: ObjectFlowBehavior | eachOccInputsEqualOutputs[x.p3]}
fact {all x: ObjectFlowBehavior | x.p3.inputs in Integer}
fact {all x: ObjectFlowBehavior | x.p3.outputs in Integer}
fact {all x: ObjectFlowBehavior | x.p3.inputs = x.p3.outputs}


/**Two Transfers specified to act as a join */
/** Constraints on the Transfer from p2 to p4 */
//fact {all x: ObjectFlowBehavior | functionFiltered[sources, x.transferbeforeP2P4, x.p2]}
//fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[sources, x.transferbeforeP2P4, x.p2]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[sources, x.transferbeforeP2P4, x.p2]}

//fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[targets, x.transferbeforeP2P4, x.p4]}
//fact {all x: ObjectFlowBehavior | functionFiltered[targets, x.transferbeforeP2P4, x.p4]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[targets, x.transferbeforeP2P4, x.p4]}
//?????
fact {all x: ObjectFlowBehavior | x.transferbeforeP2P4.items in Integer}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForSources[x.transferbeforeP2P4]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForTargets[x.transferbeforeP2P4]}

/** Constraints on the Transfer from p3 to p4 */
//fact {all x: ObjectFlowBehavior | functionFiltered[sources, x.transferbeforeP3P4, x.p3]}
//fact {all x: ObjectFlowBehavior | bijectionFiltered[sources, x.transferbeforeP3P4, x.p3]}
fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[sources, x.transferbeforeP3P4, x.p3]}

//fact {all x: ObjectFlowBehavior | functionFiltered[targets, x.transferbeforeP3P4, x.p4]}
//fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[targets, x.transferbeforeP3P4, x.p4]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[targets, x.transferbeforeP3P4, x.p4]}

//??????
fact {all x: ObjectFlowBehavior | x.transferbeforeP3P4.items in Integer}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForSources[x.transferbeforeP3P4]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForTargets[x.transferbeforeP3P4]}

/** Constraints on p4: P4 */
//?????
fact {all x: ObjectFlowBehavior | x.p4.inputs in Integer}
//????????????????
fact {all x: ObjectFlowBehavior | inverseFunctionFiltered[inputs, x.p4, Integer]}
/** Model closure */
fact {all x: ObjectFlowBehavior | x.p1 + x.p2 + x.p3 + x.p4 + x.transferbeforeP1P2 + x.transferbeforeP1P3 + x.transferbeforeP2P4 + x.transferbeforeP3P4 in x.steps}
fact {all x: ObjectFlowBehavior | x.steps in x.p1 + x.p2 + x.p3 + x.p4 + x.transferbeforeP1P2 + x.transferbeforeP1P3 + x.transferbeforeP2P4 + x.transferbeforeP3P4}







