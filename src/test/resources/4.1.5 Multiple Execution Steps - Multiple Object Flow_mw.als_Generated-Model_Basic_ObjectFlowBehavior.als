// This file is created with code.

module ObjectFlowBehavior
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig ObjectFlowBehavior extends Occurrence { disj p1, p2, p3, p4: set BehaviorWithParameter, disj transferbeforeP1P2, transferbeforeP1P3, transferbeforeP2P4, transferbeforeP3P4: set TransferBefore }
sig BehaviorWithParameter extends Occurrence { i: set Integer }
sig Integer extends Occurrence {}

// Facts:
fact {all x: ObjectFlowBehavior | #(x.p1) = 2}
fact {all x: BehaviorWithParameter | #(x.i) = 1}
fact {all x: ObjectFlowBehavior | bijectionFiltered[sources, x.transferbeforeP1P2, x.p1]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[targets, x.transferbeforeP1P2, x.p2]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForSources[x.transferbeforeP1P2]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForTargets[x.transferbeforeP1P2]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[sources, x.transferbeforeP1P3, x.p1]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[targets, x.transferbeforeP1P3, x.p3]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForSources[x.transferbeforeP1P3]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForTargets[x.transferbeforeP1P3]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[sources, x.transferbeforeP2P4, x.p2]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[targets, x.transferbeforeP2P4, x.p4]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForSources[x.transferbeforeP2P4]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForTargets[x.transferbeforeP2P4]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[sources, x.transferbeforeP3P4, x.p3]}
fact {all x: ObjectFlowBehavior | bijectionFiltered[targets, x.transferbeforeP3P4, x.p4]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForSources[x.transferbeforeP3P4]}
fact {all x: ObjectFlowBehavior | subsettingItemRuleForTargets[x.transferbeforeP3P4]}
fact {all x: ObjectFlowBehavior | x.p1 + x.p2 + x.p3 + x.p4 + x.transferbeforeP1P2 + x.transferbeforeP1P3 + x.transferbeforeP2P4 + x.transferbeforeP3P4 in x.steps}
fact {all x: ObjectFlowBehavior | x.steps in x.p1 + x.p2 + x.p3 + x.p4 + x.transferbeforeP1P2 + x.transferbeforeP1P3 + x.transferbeforeP2P4 + x.transferbeforeP3P4}

