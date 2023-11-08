//******************************************************************************************************
// Module: 		TransferModule
// Author:		Jeremy Doerr
// Purpose: 	Provides a library for ontological behavior modeling (OBM)
//******************************************************************************************************
module TransferModule[Occurrence]
open utilities/types/relation as r

//******************************************************************************************************
/** TransferBefore */
//******************************************************************************************************
sig TransferBefore extends Transfer{}{
	// The Source always occurs before the Transfer and the Transfer always occurs before the Target
//	all src: this.sources | intransitiveBefore[src, this]
//	all tgt: this.targets | intransitiveBefore[this, tgt]
}
sig TransferBeforeSig extends TransferSig{}

fact afterSourceAndBeforeTargetIsTransferBefore {all t: Transfer | isAfterSource[t] and isBeforeTarget[t] <=> t in TransferBefore}
//******************************************************************************************************
/** Transfer */
//******************************************************************************************************
sig Transfer extends BinaryLink{}
sig TransferSig extends BinaryLinkSig{Item: Transfer -> some Occurrence}	// Might transfer more than one

// Maybe add a fact to force an Item tuple for each Source. Right now, not constraining a minimum number of Item tuples.
// Do we need exactly one? One Item for each Source's Input or Output?
fact oneOrMoreItemForEachTransfer {all t: Transfer | #t.items > 0}
fact transferNotOwnItem { r/acyclic[items, Transfer]}
fact transferNotOwnSource { r/acyclic[sources, Transfer]}
fact transferNotOwnTarget { r/acyclic[targets, Transfer]}
fact transferOccursDuringItsItem{all t: Transfer | all itm: t.items | during[t, itm]}
// No 2 Transfers can share the same Item, Source, and Target (as tuple)
fact uniqueTransfers {all t, t': Transfer | t.items = t'.items and t.sources = t'.sources and t.targets = t'.targets => t = t'}	// This may be only applicable to 
fact transferToFromContainerNotTransferBefore{
	all t: Transfer | all src: t.sources | all tgt: t.targets | t in src.stepsAndSubsteps or t in tgt.stepsAndSubsteps => t not in TransferBefore}

/** Potential "optional facts". */
// Maybe include a single fact statement that calls all these that can be commented out. Or a single pred that can be called from other modules.
pred transferOptionalFacts {noTransferingContainer and noTransferInputsOrOutputs[Transfer]}
pred noTransferingContainer {all t: Transfer | t.~steps not in t.items}
pred noTransferInputsOrOutputs [transfers: set Transfer]{no transfers.inputs and no transfers.outputs}

// Needs to be a tighter constraint for physical applications. Test the below. 
pred uniqueItemsFromSameSource [transfers: set Transfer] {all disj t, t': transfers | t.sources = t'.sources => t.items != t'.items}
pred uniqueItemsToSameTarget [transfers: set Transfer] {all disj t, t': transfers | t.targets = t'.targets => t.items != t'.items}

// Set the Source of the Transfer
pred setTransfersSource[transfers: set Transfer, srcs: set Occurrence]{}
// Set the Target of the Transfer
pred setTransfersTarget[transfers: set Transfer, tgts: set Occurrence] {}
// Set the Item of the Transfer
pred transferItem[transfers: set Transfer, occurrences: set Occurrence] {}

// True in some specific Manufacture examples, but not always true.
pred transfersBetweenStepsAreTransferBefore [transfers: set Transfer] {
	all t: transfers | all src: t.sources | all tgt: t.targets | (t not in src.steps) and (t not in tgt.steps) <=> t in TransferBefore}

// These subsetting and binding rules are for Transfers that happen (or may happen) as a step of their Source or Target. 
// 	It is a way to get the Input from or Output to the "boundary" or "containing" Occurrence.
// 	They map Inputs to Inputs and Outputs to Outputs for things on the boundary.
// Subsetting (looser)
pred subsettingItemRuleForSources [transfers: set Transfer] {
	all t: transfers | all src: t.sources | t in src.steps => t.items in src.inputs else t.items in src.outputs}	// Missing constraint to ensure there are actual items
pred subsettingItemRuleForTargets [transfers: set Transfer] {
	all t: transfers | all tgt: t.targets | t in tgt.steps => t.items in tgt.outputs else t.items in tgt.inputs}
//Binding (tighter)
pred bindingItemRuleForSources [transfers: set Transfer] {
	all t: transfers | all src: t.sources | t in src.steps => t.items = src.inputs else t.items = src.outputs}
pred bindingItemRuleForTargets [transfers: set Transfer] {
	all t: transfers | all tgt: t.targets | t in tgt.steps => t.items = tgt.outputs else t.items = tgt.inputs}

// Specify where each transfer in the passed set get their items from
pred transferEachItemFromOutputOfSource [transfers: set Transfer] {
	all t: transfers | t.items in (t.sources).outputs}
pred transferEachItemFromInputOfSource [transfers: set Transfer] {
	all t: transfers | t.items in (t.sources).inputs}
pred transferEachItemToInputOfTarget [transfers: set Transfer] {
	all t: transfers | t.items in (t.targets).inputs}
pred transferEachItemToOutputOfTarget [transfers: set Transfer] {
	all t: transfers | t.items in (t.targets).outputs}

// Specify that every transfer in the passed set transfers all the items from the specified location.
// Every specified Item passes through every passed Transfer
pred transferEveryItemFromOutputOfSource [transfers: set Transfer] {
	all t: transfers | all src: t.sources | all occ: Occurrence | occ in src.outputs <=> occ in t.items}
pred transferEveryItemFromInputOfSource [transfers: set Transfer] {
	all t: transfers | all src: t.sources | all occ: Occurrence | occ in src.inputs <=> occ in t.items}
pred transferEveryItemToInputOfTarget [transfers: set Transfer] {
	all t: transfers | all tgt: t.targets | all occ: Occurrence | occ in tgt.inputs <=> occ in t.items}
pred transferEveryItemToOutputOfTarget [transfers: set Transfer] {
	all t: transfers | all tgt: t.targets | all occ: Occurrence | occ in tgt.outputs <=> occ in t.items}

pred isAfterSource [transfers: set Transfer]{all t: transfers | intransitiveBefore[t.sources,t]}
pred isBeforeTarget [transfers: set Transfer]{all t: transfers | intransitiveBefore[t,t.targets]}
pred noOccurrenceItemOfStep {all occ: Occurrence | all t: Transfer | t in occ.stepsAndSubsteps => occ not in t.items}

//******************************************************************************************************
/** BinaryLink */
//******************************************************************************************************
abstract sig BinaryLink extends Occurrence{}
one sig BinaryLinkSig extends OccurrenceSig{Source, Target: BinaryLink -> one Occurrence}	/** Write check to ensure both act as functions */

//******************************************************************************************************
/** OccurrenceSig */
//******************************************************************************************************
one sig OccurrenceSig {
	HappensDuring, HappensBefore, Overlaps: Occurrence -> Occurrence,	
	Step: Occurrence lone -> Occurrence,					// An Occurrence can only be a step of one other Occurrence at most
	Input, Output: Occurrence -> Occurrence

}{	// No step can be a step of itself, transitively or intransitively.
	r/acyclic[Step, Occurrence]
	
	//	An Occurrence can't be its own input or output. OK to leave for now
	r/irreflexive[Input]
	r/irreflexive[Output]
	r/asymmetric[Input]
	r/asymmetric[Output]
	// Update from Conrad on 2021-07-13
//	r/acyclic[Input + Output, Occurrence]
//	r/atransitive[Input]
//	r/atransitive[Output]
	// Need "atransitive" predicate/function in the Relation module
}

//fact overlapsComplementsHBAndHD {all x,y: Occurrence | not before[x,y] => overlap[x,y] /*or before[y,x] or during[x,y] or during[y,x]*/}
fact overlapsComplementsHBAndHD {all  x,y: Occurrence | not (before[x,y] or before[y,x] or during[x,y] or during[y,x]) <=> overlap[x,y]}
//fact overlapsComplementsHBAndHD {all disj x,y: Occurrence | not (before[x,y] or during[x,y]) <=> overlap[x,y]}

/** Loosen this for streaming */
fact occurrenceHappensDuringInputEqualsOutput {all o: Occurrence | all input: o.inputs | all output: o.outputs | input = output => during[o, input]}
fact stepIsAsymmetric{all x,y: Occurrence | y in x.steps => not (x in y.steps)}
fact stepIsAtransitive {all x,y,z : Occurrence | y in x.steps and z in y.^steps => z not in x.steps}
fact stepHasOneSource{all x: Occurrence | #(x.~steps) <= 1} 
fact occurrencesHappenDuringThemselves{all x: Occurrence | during[x,x]}
fact beforeIsTransitive{all x,y,z: Occurrence | before[x,y] and before[y,z] => before[x,z]}
fact beforeIsAcyclic{all x: Occurrence | not before[x,x]}
fact duringIsTransitive{all x,y,z: Occurrence | during[x,y] and during[y,z] => during[x,z]}
// before is symmetric only when x and y are duration and happen during each other
fact beforeSymmetry{all x,y: Occurrence | before[x,y] and before[y,x] <=> before[x,x] and before[y,y] and during[x,y] and during[y,x]}
// If x happens before y and z happens during y, then x must also happen before z
fact interrelationshipConstraint1{all x,y,z: Occurrence | before[x,y] and during[z,y] => before[x,z]} // loosened this and bidirectional overlaps works
// If y happens before x and z happens during y, then z must also happen before x
fact interrelationshipConstraint2{all x,y,z: Occurrence | before[y,x] and during[z,y] => before[z,x]} // loosened this and bidirectional overlaps works
fact crossoverStructuralTemporalConstraints {all x,y: Occurrence | y in x.steps => during[y,x]}
/** TODO: Add predicates about Occurrences creating or destroying inputs and outputs */
pred noOccurrenceInputOutputOfStep {all x, y: Occurrence | y in x.stepsAndSubsteps => x not in y.inputs and x not in y.outputs}

//******************************************************************************************************
/** General Functions and Predicates */
//******************************************************************************************************
/**		Items */
fun items: Transfer -> Occurrence {TransferSig.Item}
/**		Sources/Targets */
fun sources: Occurrence -> Occurrence {BinaryLinkSig.Source}
fun targets: Occurrence -> Occurrence {BinaryLinkSig.Target}
/**		Inputs/Outputs */
fun inputs: Occurrence -> Occurrence {OccurrenceSig.Input}
fun outputs: Occurrence -> Occurrence {OccurrenceSig.Output}
pred irreflexiveOutput {r/irreflexive[OccurrenceSig.Output]}
pred irreflexiveInput {r/irreflexive[OccurrenceSig.Input]}
pred eachOccInputsEqualOutputs [occSet: set Occurrence] {all x: occSet | x.inputs = x.outputs}
/** 		Steps */
fun steps: Occurrence -> Occurrence {OccurrenceSig.Step}			// Return the set of all steps
fun stepsAndSubsteps: Occurrence -> Occurrence {^steps}			// Return the set of all steps and substeps (transitive closure of Steps)
fun superSteps: Occurrence -> Occurrence {~stepsAndSubsteps}		// Return the set of all super steps (transpose of transitive closure of Steps)
/** 		Temporal */
fun happensBefore: Occurrence -> Occurrence {OccurrenceSig.HappensBefore}
fun happensDuring: Occurrence -> Occurrence {OccurrenceSig.HappensDuring}
fun overlaps: Occurrence -> Occurrence {OccurrenceSig.Overlaps}
// Return the sets of the occurrences that happen before and during the passed occurrence
fun returnSetHappensBefore[x: Occurrence]: set Occurrence {x.~(^(OccurrenceSig.HappensBefore))}
fun returnSetHappensDuring[x: Occurrence]: set Occurrence {x.~(^(OccurrenceSig.HappensDuring))}
pred before [x,y: Occurrence]{x in returnSetHappensBefore[y]}
pred during [x,y: Occurrence]{x in returnSetHappensDuring[y]}
pred overlap [x,y: Occurrence]{x in y.~(^(OccurrenceSig.Overlaps))}
pred intransitiveBefore [x,y: Occurrence] {x->y in happensBefore}
pred intransitiveAfter [x,y: Occurrence] {y->x in happensBefore}
pred intransitiveDuring [x,y: Occurrence] {x->y in happensDuring}

pred isEqualDuration[x,y: Occurrence] {during[x,y] and during[y,x]} // this also implies that they start at the same time

/** 		Binary Relations */
pred functionFiltered[relation: univ -> univ, src, tgt: set Occurrence] {r/function[(src <: relation) & (relation :> tgt), src]}
pred inverseFunctionFiltered[relation: univ -> univ, src, tgt: set Occurrence] {r/bijective[(src <: relation) & (relation :> tgt), tgt]}
pred totalSurjectiveFiltered[relation: univ -> univ, src, tgt: set Occurrence] {r/total[(src <: relation) & (relation :> tgt), src] and
														r/surjective[(src <: relation) & (relation :> tgt), tgt]}
pred bijectionFiltered[relation: univ -> univ, src, tgt: set Occurrence] {r/bijection[(src <: relation) & (relation :> tgt), src, tgt]}

/**			Convenience Predicates and Functions for Examples */
pred stepsOnlyExistInContainer[occSet: set Occurrence]{all o: occSet | some steps.o}
// TODO: some way to suppress nonsensical relations. E.g., an instance of an atomic signature should not have a Step relation to another instance

//******************************************************************************************************
/** Dynamic Operation Predicates and Assertions */
//******************************************************************************************************
//pred addSteps[s, s': OccurrenceSig, src, tgt: Occurrence]{
//	s'.Step = s.Step + src -> tgt
//}
//pred showAddSteps[disj s, s': OccurrenceSig, disj src, tgt: Occurrence]{
//	addSteps[s, s', src, tgt]
//	#(s.Step) = 0
//	#(s'.Step) = 1
//}
//
//run showAddSteps for 8 but 2 OccurrenceSig

//******************************************************************************************************
/** Checks and Runs */
//******************************************************************************************************
/**		Counterexample predicates */
pred cyclicHasStep {some ^steps & iden}
pred acyclicHappensDuring {no ^happensDuring & iden}
pred reflexiveHasStep {all occ: Occurrence | r/reflexive[steps,occ]}
pred irreflexiveHappensDuring {r/irreflexive[happensDuring]}
pred asymmetricHappensDuring {r/asymmetric[happensDuring]}
pred beforeBecauseBeforeADuring {all x,y,z: Occurrence | before[x,y] and during[z,y] => before[x,z]}
pred symmetricBefore {all x,y: Occurrence | before[x,y] and before[y,x]}
//pred symmetricNonZeroDurationBefore {nonZeroDurationOnly and symmetricBefore and #Occurrence > 0}
pred symmetricDuring { all x,y: Occurrence | during[x,y] and during[y,x]}
//******************************************************************************************************
/** Checks and Runs */
//******************************************************************************************************
run show {#HappensBefore > 0 and #OccurrenceSig.Input > 0 and 
		#OccurrenceSig.Output > 0} for 5
// Checks for hasStep
check stepCannotBeSubstepOfSelf {all x: Occurrence | not (x in x.stepsAndSubsteps)} for 20
//check substepCannotOccurBeforeSuperstepUnlessZeroDuration {all disj x,y: Occurrence | x in y.superSteps => not before[x,y] or isZeroDuration[y]} for 8
check allSubstepsOccurDuringSupersteps {all x,y: Occurrence | y in x.stepsAndSubsteps => during[y,x]} for 8
check hasStepIsAcyclic {all x: Occurrence | not (x in x.stepsAndSubsteps)} for 20
check eachStepHasOneParent {all x: Occurrence | #(x.~steps) =< 1} for 25
run someStepHasMultipleParents {some x: Occurrence | #(x.~steps) > 1} for 25 expect 0

// Checks for before() and during()
check beforeAndDuringOnlyForFirstIsZeroDuration {all x,y: Occurrence | before[x,y] and during[x,y] => before[x,x]} for 20
check beforeAcyclicExceptZeroDuration {all disj x,y: Occurrence | before[x,x] and (y->y in ^(x->x) or x->y in ^(x->x) or y->x in ^(x->x)) => before[y,y]} for 20
check beforeBecauseImBeforeADuring {all x,y,z: Occurrence | before[x,y] and during[z,y] => before[x,z]} for 20
check beforeBecauseImDuringABefore {all x,y,z: Occurrence | during[x,y] and before[y,z] => before[x,z]} for 20
check {all x,y: Occurrence | during[x,y] or during[y,x] => not (before[x,y] or before[y,x])} for 20
check {all x,y: Occurrence | before[x,y] or before[y,x] => not (during[x,y] or during[y,x])} for 20

// Added per NIST
check allOccurrencesHappenDuringThemselves {all x: Occurrence | during[x,x]} for 20

// Checks for BinaryLink
check eachLinkHasOneSource { all link: BinaryLink | #link.sources = 1} for 25
check eachLinkHasOneTarget { all link: BinaryLink | #link.targets = 1} for 25

// Checks for Transfer
run someTransferIsItsOwnItem {some t: Transfer | t in t.^items} for 25 expect 0
run someTransferIsItsOwnSource {some t: Transfer | t in t.^sources} for 25 expect 0
run someTransferIsItsOwnTarget {some t: Transfer | t in t.^targets} for 25 expect 0
run someTransferDoesNotOccurDuringItsItem {some t: Transfer | some itm: t.items | not during[t,itm]} for 20 expect 0
run someNonUniqueTransferExists {some disj t, t': Transfer | t.items = t'.items and t.sources = t'.sources and t.targets = t'.targets} for 25 expect 0
run someTransferNoItem { some t: Transfer | #t.items = 0} for 25 expect 0
