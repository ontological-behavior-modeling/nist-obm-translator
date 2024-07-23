package edu.gatech.gtri.obm.alloy.translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;

/**
 * A helper class to translate OBMXMI to Alloy.
 * 
 * Assumption - All PrimSig created have unique names
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 *
 */
public class ToAlloy {

  /**
   * Alloy object
   */
  private Alloy alloy;
  /**
   * Map key = sig name, value = the PrimSig having the key name. used to retrieve the PrimSig based on its name.
   */
  private Map<String, PrimSig> sigByName;

  /**
   * A constructor
   * 
   * @param _alloy - An Alloy object to be created by translator
   */
  protected ToAlloy(Alloy _alloy) {
    alloy = _alloy;
    alloy.initialize();
    sigByName = new HashMap<>();
  }

  /**
   * Gets the sig.
   *
   * @param name the name
   * @return the sig
   */
  protected PrimSig getSig(String name) {
    return sigByName.get(name);
  }

  /**
   * Create PrimSig of the given name with the given parent Sig. Only called when the sig is not exist yet and parentSig is not null
   * 
   * @param name - name of PrimSig to be created
   * @param parentSig - the parentSig, can not be null
   * @param isMainSig - boolean mainSig or not, if true, used to create this.moduleName
   * @return created PrimSig or null if already existing
   */
  protected PrimSig createSig(String name, PrimSig parentSig, boolean isMainSig) {

    PrimSig sig = createSig(name, parentSig);
    if (sig == null)
      return null;
    if (isMainSig)
      // removing "this/" from s.label and assigns as moduleName
      alloy.setModuleName(
          (sig.label.startsWith("this") ? sig.label.substring(5) : sig.label) + "Module");
    return sig;
  }

  /**
   * Return already existing PrimSig of the given name. If the given name PrimSig does not exist, create the PrimSig with the given parentName sig. When the parentName is null, Occurrence,
   * BehaviorOccurrence or Anything, the parent of created sig will be Occurrence. If the parentName is other than that the parent should be already created with the name.
   * 
   * @param name the name
   * @param parentName - the name of parent sig. Assume already exists
   * @return the prim sig
   */
  protected PrimSig createSigOrReturnSig(String name, String parentName) {

    PrimSig exstingSig = getSig(name);
    if (exstingSig != null)
      return exstingSig;

    PrimSig parentSig = null;
    if (!AlloyUtils.validParent(parentName)) // null, Occurrence, BehaviorOccurrence or Anything
                                             // then parent is Occurrence
      parentSig = Alloy.occSig;
    else
      parentSig = (PrimSig) sigByName.get(parentName);
    return createSig(name, parentSig);
  }

  /**
   * Create PrimSig with the given name and the given parent Sig and add to the Alloy's sigs and this.sigByNames
   * 
   * @param name - The name of the PrimSig to be created
   * @param parentSig - The parent of the PrimSig to be created
   * @return created PrimSig or null if already existing
   */
  private PrimSig createSig(String name, PrimSig parentSig) {
    if (getSig(name) != null) // already existing in sigByName
      return null;
    PrimSig sig = new PrimSig(name, parentSig);
    alloy.addToAllSigs(sig);
    sigByName.put(name, sig);
    return sig;
  }


  /**
   * Adds the disjoint alloy fields.
   *
   * @param fieldNamesListWithSameType the field names list with same type
   * @param fieldSigTypeName the type sig name
   * @param ownerSig the owner sig
   * @return the sig. field[]
   */
  protected Sig.Field[] addDisjAlloyFields(List<String> fieldNamesListWithSameType,
      String fieldSigTypeName, PrimSig ownerSig) {

    String[] fieldNames = new String[fieldNamesListWithSameType.size()];
    fieldNamesListWithSameType.toArray(fieldNames);

    Sig.Field[] ps = null;
    Sig fieldSigType = sigByName.get(fieldSigTypeName);
    if (fieldSigType != null) {
      ps = AlloyUtils.addTrickyFields(fieldNames, ownerSig, fieldSigType);
      if (ps.length != fieldNames.length) {
        return null; // this should not happens
      }
    } else
      return null; // this should not happens
    return ps;

  }



  /**
   * Create two expressions to describe what is flowing and add to the alloy's facts.
   * 
   * <pre>
   * For example,
   * {all x: MultipleObjectFlow | all p: x.p1 | p.i in p.outputs} 
   * {all x: MultipleObjectFlow | all p: x.p1 | p.outputs in p.in}
   * </pre>
   * 
   * @param ownerSig - signature for the fact (i.e., MultipleObjectFlow(class))
   * @param sigField - a field(property) of ownerSig(i.e., p1)
   * @param fieldOfsigFieldType - a field(property) of sigField type(class) (ie., i of p1's type signature(class)) = sourceOutputProperty or targetInputProperty
   * @param inputsOrOutputs - a function inputs or outputs
   */
  protected void createInField(Sig ownerSig, Sig.Field sigField,
      Sig.Field fieldOfsigFieldType, Func inputsOrOutputs) {
    alloy.addToFacts(AlloyExprFactory.exprs_inField(ownerSig, sigField, fieldOfsigFieldType,
        inputsOrOutputs));
  }

  /**
   * Create the bijection filtered happens before and add to the alloy's facts. The from and to expression can be just a filed (i.e., p1) or multiple fields connected by plus (p1 + p2 + p3).
   * 
   * <pre>
   * For example, 
   * fact {all x: AllControl | bijectionFiltered[happensBefore, from, to]}
   * fact {all x: AllControl | bijectionFiltered[happensBefore, x.p1, x.p2 + x.p3]}
   * </pre>
   * 
   * @param ownerSig - the owner signature
   * @param from - the from expression
   * @param to - the to expression
   */
  protected void createBijectionFilteredHappensBefore(Sig ownerSig, Expr from, Expr to) {
    alloy.addToFacts(
        AlloyExprFactory.exprs_bijectionFilteredFactsForSig(ownerSig, from, to,
            Alloy.happensBefore));
  }

  /**
   * Create the bijection filtered happens during and add to the alloy's facts. The from and to expression can be just a filed (i.e., p1, b.vin) or multiple fields connected by plus (p1 + p2 + p3).
   * 
   * <pre>
   * For example, 
   * fact {all x: UnsatisfiableComposition1 | bijectionFiltered[happensDuring, from, to]}
   * fact {all x: UnsatisfiableComposition1 | bijectionFiltered[happensDuring, x.p3, x.p2]}
   * </pre>
   * 
   * @param ownerSig - the owner signature
   * @param from - the from expression
   * @param to - the to expression
   */
  protected void createBijectionFilteredHappensDuring(Sig ownerSig, Expr from, Expr to) {
    alloy.addToFacts(
        AlloyExprFactory.exprs_bijectionFilteredFactsForSig(ownerSig, from, to,
            Alloy.happensDuring));
  }

  /**
   * Create the bijection filtered inputs during and add the alloy's facts. The from and to expression can be just a filed (i.e., p1, b.vin) or multiple fields connected by plus (p1 + p2 + p3).
   * 
   * <pre>
   * For example, 
   * fact {all x: ParameterBehavior | bijectionFiltered[inputs, from, to]}
   * fact {all x: ParameterBehavior | bijectionFiltered[inputs, x.b, x.b.vin]}
   * </pre>
   * 
   * @param ownerSig - the owner signature
   * @param from - the from expression
   * @param to - the to expression
   */
  protected void createBijectionFilteredInputs(Sig ownerSig, Expr from, Expr to) {
    alloy.addToFacts(
        AlloyExprFactory.exprs_bijectionFilteredFactsForSig(ownerSig, from, to, Alloy.oinputs));
  }

  /**
   * Create the bijection filtered inputs during and add to the alloy's facts. The from and to expression can be just a filed (i.e., p1, b.vin) or multiple fields connected by plus (p1 + p2 + p3).
   * 
   * <pre>
   * For example, 
   * fact {all x: B | bijectionFiltered[outputs, from, to]}
   * fact {all x: B | bijectionFiltered[outputs, x.b1, x.b1.vout]}
   * </pre>
   * 
   * @param ownerSig - the owner signature
   * @param from - the from expression
   * @param to - the to expression
   */
  protected void createBijectionFilteredOutputs(Sig ownerSig, Expr from, Expr to) {
    alloy.addToFacts(
        AlloyExprFactory.exprs_bijectionFilteredFactsForSig(ownerSig, from, to, Alloy.ooutputs));
  }


  /**
   * Creates the function filtered happens before and add to the alloy's facts.
   *
   * @param ownerSig - the owner signature
   * @param from - the from expression
   * @param to - the to expression
   */
  protected void createFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.addToFacts(
        AlloyExprFactory.expr_functionFilteredHappensBeforeForSig(ownerSig, from, to));
  }

  /**
   * Creates the inverse function filtered happens before and add to the alloy's facts.
   *
   * @param ownerSig - the owner signature
   * @param from - the from expression
   * @param to - the to expression
   */
  protected void createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig,
      Expr from, Expr to) {
    alloy.addToFacts(
        AlloyExprFactory.expr_inverseFunctionFilteredHappensBefore(ownerSig, from, to));
  }

  /**
   * Create a equal or greater than equal to the property's lower fact constraint to sign's field. if lower and upper are the same, add the equal fact constraint. if upper is -1, lower is greater than
   * equal to 1,
   * 
   * @param sig the owner of field
   * @param fieldName name of the sig's field having the constraint fact
   * @param lower The lower (integer) bound of the multiplicity interval.
   * @param upper The upper (integer) bound of the multiplicity interval.
   * @return true if the fact added successfully, false if the fact can not add successfully because the field did not exist in the sig.
   */
  protected void addCardinalityFact(PrimSig sig, Field field, int lower, int upper) {
    if (lower == upper)
      alloy.addToFacts(AlloyExprFactory.expr_cardinalityEqual(sig, field, lower));
    // addCardinalityEqualConstraintToField(field, sig, lower);
    else if (upper == -1 && lower >= 1) {
      // addCardinalityGreaterThanEqualConstraintToFieldFact(field, sig, lower);
      alloy.addToFacts(AlloyExprFactory.expr_cardinalityGreaterThanEqual(sig, field, lower));
    }
  }

  /**
   * Create a equal or greater than equal to the property's lower fact constraint to sign's field. if lower and upper are the same, add the equal fact constraint. if upper is -1, lower is greater than
   * equal to 1,
   * 
   * @param sig the owner of field
   * @param fieldName name of the sig's field having the constraint fact
   * @param lower The lower (integer) bound of the multiplicity interval.
   * @param upper The upper (integer) bound of the multiplicity interval.
   * @return true if the fact added successfully, false if the fact can not add successfully because the field did not exist in the sig.
   */
  protected boolean addCardinalityFact(PrimSig sig, String fieldName, int lower, int upper) {

    Sig.Field field = AlloyUtils.getFieldFromSigOrItsParents(fieldName, sig); // FoodService
    if (field == null)
      return false;
    else
      addCardinalityFact(sig, field, lower, upper);
    return true;
  }

  /**
   * Adding facts for Transfer connector.
   * 
   * @param ownerSig
   * @param transfer
   * @param source
   * @param target
   * @param targetInputsSourceOutputsFields
   * @param toBeInherited
   * @return
   */
  protected Set<Expr> addTransferFacts(PrimSig ownerSig, Expr transfer, String source,
      String target, List<Set<Field>> targetInputsSourceOutputsFields, boolean toBeInherited) {

    // for 4.1.4 Transfers and Parameters1 - TransferProduct_modified
    // sig ParticipantTransfer
    // sourceTypeName Supplier -> Field supplier
    // targetTypeName Customer -> Field customer

    Field sourceTypeField = AlloyUtils.getFieldFromSigOrItsParents(source, ownerSig);
    Field targetTypeField = AlloyUtils.getFieldFromSigOrItsParents(target, ownerSig);

    // fact {all x: ParticipantTransfer | bijectionFiltered[sources, x.transferSupplierCustomer,
    // x.supplier]}
    // fact {all x: ParticipantTransfer | bijectionFiltered[targets, x.transferSupplierCustomer,
    // x.customer]}
    // fact {all x: ParticipantTransfer | subsettingItemRuleForSources[x.transferSupplierCustomer]}
    // fact {all x: ParticipantTransfer | subsettingItemRuleForTargets[x.transferSupplierCustomer]}


    // fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferOrderPay, x.order]}
    Set<Expr> facts = new HashSet<>();

    // only for leaf node
    // fact {all x: OFSingleFoodService | x.transferOrderPay.sources.orderedFoodItem +
    // x.transferOrderPay.sources.orderAmount in x.transferOrderPay.items}
    if (targetInputsSourceOutputsFields != null) {
      if (targetInputsSourceOutputsFields.get(0).size() > 0) {
        Set<Expr> factsWithoutSig = AlloyExprFactory.exprs_transferInItems(ownerSig, transfer,
            sourceTypeField, Alloy.sources, targetInputsSourceOutputsFields.get(0));
        facts.addAll(factsWithoutSig);
        if (!toBeInherited)
          alloy.addToFacts(AlloyUtils.toSigAllFacts(ownerSig, factsWithoutSig));
      }
      if (targetInputsSourceOutputsFields.get(1).size() > 0) {
        Set<Expr> factsWithoutSig = AlloyExprFactory.exprs_transferInItems(ownerSig, transfer,
            targetTypeField, Alloy.targets, targetInputsSourceOutputsFields.get(1));
        facts.addAll(factsWithoutSig);
        if (!toBeInherited)
          alloy.addToFacts(AlloyUtils.toSigAllFacts(ownerSig, factsWithoutSig));
      }
    }
    alloy.addToFacts(AlloyExprFactory.exprs_bijectionFilteredFactsForSig(ownerSig, transfer,
        sourceTypeField, Alloy.sources));
    alloy.addToFacts(AlloyExprFactory.exprs_bijectionFilteredFactsForSig(ownerSig, transfer,
        targetTypeField, Alloy.targets));
    alloy.addToFacts(AlloyExprFactory.exprs_subSettingItemRule(ownerSig, transfer));

    return facts;
  }



  /**
   * Adding facts for TransferBefore connector
   * 
   * @param ownerSig
   * @param transfer ie., transferbeforeAB
   * @param sourceTypeName
   * @param targetTypeName
   */
  protected Set<Expr> addTransferBeforeFacts(PrimSig ownerSig, Expr transfer, String sourceName,
      String targetName, List<Set<Field>> targetInputsSourceOutputsFields, boolean toBeInherited) {

    // factsWithoutAll = isAfterSource[x.transferOrderServe] of
    // fact {all x: OFFoodService | isAfterSource[x.transferOrderServe]}
    Set<Expr> factsWithoutAll = addTransferFacts(ownerSig, transfer, sourceName, targetName,
        targetInputsSourceOutputsFields, toBeInherited);

    Set<Expr> facts = AlloyExprFactory.exprs_isAfterSourceIsBeforeTarget(ownerSig, transfer);

    if (!toBeInherited) {

      // facts above have
      // fact {all x: OFFoodService | isAfterSource[x.transferOrderServe]}
      // fact {all x: OFFoodService | isBeforeTarget[x.transferOrderServe]}
      // but NOT in inherited OFSingleFoodServie like
      // fact {all x: OFSingleFoodServie | isAfterSource[x.transferOrderServe]}
      // fact {all x: OFSingleFoodServie | isBeforeTarget[x.transferOrderServe]}

      factsWithoutAll.addAll(facts);
    }
    // toSigAllFacts make expr like "isAfterSource[x.transferOrderServe]" to "fact {all x: OFFoodService | isAfterSource[x.transferOrderServe]}"
    alloy.addToFacts(AlloyUtils.toSigAllFacts(ownerSig, facts));
    return factsWithoutAll;
  }

  protected void addNoInputsOrOutputsFieldFact(PrimSig ownerSig, String fieldName,
      Func inputsOrOutputs) {
    Sig.Field field = AlloyUtils.getFieldFromSigOrItsParents(fieldName, ownerSig);
    alloy.addToFacts(
        AlloyExprFactory.expr_noInputsOrOutputsField(ownerSig, field, inputsOrOutputs));
  }

  /**
   * Add fact like "{all x: B1 | x.vin in x.inputs} and {all x: B1 | x.inputs in x.vin} and {all x| no inputs.x}" based on the given addNoOutputsX and addEqual.
   * 
   * @param sig - A signature might have facts
   * @param fields - fields or the signatures to describe the fact
   * @param addNoInputsX - boolean if true, include a fact like {all x| no outputs.x}
   * @param addEquall - boolean if true, includes facts like "..in x.outputs" and "x.outputs in...", if false, not to includes
   */
  protected void addInInputsAndNoInputXFacts(PrimSig sig, Set<Field> fields,
      boolean addNoInputsX, boolean addEqual) {

    List<Field> sortedFields = AlloyUtils.sortFields(fields);
    if (addEqual)
      // {all x: B1 | x.vin in x.inputs} and {all x: B1 | x.inputs in x.vin}
      alloy.addToFacts(AlloyExprFactory.exprs_in(sig, sortedFields, Alloy.oinputs));
    if (addNoInputsX)
      // {all x| no inputs.x}
      alloy.addToFacts(AlloyExprFactory.expr_noInputsX(sig));
  }

  /**
   * Add facts like "{all x: B1 | x.vin in x.outputs} and {all x: B1 | x.outputs in x.vin}" and "{all x| no outputs.x}" for the given signature based on the given addNoOutputsX and addEqual
   * 
   * @param sig - A signature might have facts
   * @param fields -fields or the signatures to describe the fact
   * @param addNoOutputsX - boolean if true, include a fact like {all x| no outputs.x}
   * @param addEqual - boolean if true, includes facts like "..in x.outputs" and "x.outputs in...", if false, not to includes
   */
  protected void addInOutputsAndNoOutputsXFacts(PrimSig sig, Set<Field> fields,
      boolean addNoOutputsX, boolean addEqual) {

    List<Field> sortedFields = AlloyUtils.sortFields(fields);
    if (addEqual)
      // {all x: B1 | x.vout in x.outputs} and {all x: B1 | x.outputs in x.vout}
      alloy.addToFacts(AlloyExprFactory.exprs_in(sig, sortedFields, Alloy.ooutputs));
    if (addNoOutputsX)
      // {all x| no outputs.x}
      alloy.addToFacts(AlloyExprFactory.expr_noOutputsX(sig));
  }


  /**
   * Add a fact like "fact {all x: OFSingleFoodService | x.prepare.inputs in x.prepare.preparedFoodItem + x.prepare.prepareDestination}"
   * 
   * @param ownerSig - A signature of the fact
   * @param field - A field of the given owner signature used to create expression after "x." to define inputs or outputs
   * @param fieldOfFields - fields of the field used to create expression after "in"
   * @param inputsOrOutputs - A function either inputs or outputs
   */
  protected void addInOutClosureFact(PrimSig ownerSig, Field field, Set<Field> fieldOfFields,
      Func inputsOrOutputs) {
    List<Field> sortedfieldOfFields = AlloyUtils.sortFields(fieldOfFields);
    alloy.addToFacts(
        AlloyExprFactory.expr_inOutClosure(ownerSig, field, sortedfieldOfFields, inputsOrOutputs));
  }

  /**
   * Add a fact like "fact {all x: BuffetService | no y: Transfer | y in x.steps}" when a signature is not in noTransferInXStepsFactSigs, is leaf sig, and has own or inherited fields.
   * 
   * @param noTransferInXStepsFactSigs - A set of signature should not have this fact.
   * @param leafSigs - A set of leaf signatures.
   */
  protected void addNoTransferInXStepsFact(Set<Sig> noTransferInXStepsFactSigs,
      Set<PrimSig> leafSigs) {
    Object[] sigs =
        sigByName.values().stream().filter(sig -> !noTransferInXStepsFactSigs.contains(sig))
            .toArray();
    for (Object sig : sigs) {
      if (leafSigs.contains(sig) && AlloyUtils.hasOwnOrInheritedFields((PrimSig) sig))
        alloy.addToFacts(AlloyExprFactory.expr_noTransferXSteps((PrimSig) sig));
    }
  }

  /**
   * Add a step closure fact (i.e., fact {all x: Integer | no steps.x}) if the given transferingTypeSig (i.e., Integer) is a leaf Signature.
   * 
   * @param transferingTypeSig - signature names to be checked
   * @param leafSigs - a set of leaf Signatures
   */
  protected void addStepClosureFact(Set<String> transferingTypeSig, Set<PrimSig> leafSigs) {
    for (String sigName : transferingTypeSig) {
      Sig sig = sigByName.get(sigName);
      if (leafSigs.contains(sig))
        // fact {all x: Integer | no steps.x}
        alloy.addToFacts(AlloyExprFactory.expr_noStepsX(sig));
    }
  }

  /**
   * add {no steps},{x.steps in ...}, {... in x.steps} facts
   * 
   * <pre>
   * {no steps} is like fact {all x: AtomicBehavior | no x.steps} 
   * {x.steps in ...} is like fact {all x: SimpleSequence | x.steps in x.p1 + x.p2} 
   * {... in x.steps} is like fact {all x: SimpleSequence | x.p1 + x.p2 in x.steps}
   * </pre>
   * 
   * @param stepPropertiesBySig - a map (key = signature name, value = a set of property names) of step properties by signature
   * @param leafSig - a set of signature that is leaf
   */
  protected Set<Sig> addStepsFacts(Map<String, Set<String>> stepPropertiesBySig,
      Set<PrimSig> leafSigs) {

    Set<Sig> noStepsSigs = new HashSet<>();
    for (String sigName : stepPropertiesBySig.keySet()) {

      PrimSig sig = sigByName.get(sigName);
      // if leaf Sig do
      if (leafSigs.contains(sig)) {
        if (stepPropertiesBySig.get(sigName).size() > 0) { // {x.steps in ....} and {... in x.steps} for leaf signature
          alloy.addToFacts(
              AlloyExprFactory.exprs_stepsFields(sig, stepPropertiesBySig.get(sigName), true,
                  true));
        } else {
          alloy.addToFacts(AlloyExprFactory.expr_noXSteps(sig)); // {no steps} facts if leafSig but no stepProperties
          noStepsSigs.add(sig);
        }
      } else if (stepPropertiesBySig.get(sigName).size() > 0) // not leaf signature {.... in x.steps} only
        alloy.addToFacts(
            AlloyExprFactory.exprs_stepsFields(sig, stepPropertiesBySig.get(sigName), true, false));
    }
    return noStepsSigs;
  }



  /**
   * Add a fact like "fact {all x: OFFoodService | x.eat in OFEat }"
   * 
   * @param ownerSig - A signature for the face to be defined
   * @param propertyNameAndType - A map where key is property name string and value is the property type (Class) name
   */
  protected void addRedefinedSubsettingAsFacts(PrimSig ownerSig,
      Map<String, String> propertyNameAndType) {

    for (String pName : propertyNameAndType.keySet()) {
      alloy.addToFacts(
          AlloyExprFactory.expr_redefinedSubsetting(ownerSig,
              AlloyUtils.getFieldFromParentSig(pName, ownerSig),
              sigByName.get(propertyNameAndType.get(pName))));
    }
  }

  /**
   * Add a equal fact of fields belong to a signature like "fact {all x: B1 | x.vin = x.vout}"
   * 
   * @param ownerSig - A signature for the fact to be defined
   * @param fieldName1 - A field name of the Signature to be defined equal.
   * @param fieldName2 - Another field name of the Signature to be defined equal.
   */
  protected void addEqualFact(PrimSig ownerSig, String fieldName1, String fieldName2) {
    Field f1 = AlloyUtils.getFieldFromSigOrItsParents(fieldName1, ownerSig);
    Field f2 = AlloyUtils.getFieldFromSigOrItsParents(fieldName2, ownerSig);
    if (f1 != null && f2 != null) {
      alloy.addToFacts(AlloyExprFactory.expr_equal(ownerSig, f1, f2));
    }
  }

  /**
   * this produces like toAlloy.noInputs("Supplier"); toAlloy.noOutputs("Customer");
   * 
   * @param sigNameWithTransferConnectorWithSameInputOutputFieldType {BehaviorParameterIn=[vin], BehaviorParameterInOut=[vin]}
   * @param outputs {BehaviorParameterOut=[vout], BehaviorParameterInOut=[vout]}
   */
  /**
   * 
   * @param sigInputProperties
   * @param sigOutputProperties
   * @param sigNames
   * @param sigNameWithTransferConnectorWithSameInputOutputFieldType
   * @param leafSigs
   */
  protected void handleNoInputsOutputs(HashMap<String, Set<String>> sigInputProperties,
      HashMap<String, Set<String>> sigOutputProperties, Set<String> sigNames,
      Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType, Set<PrimSig> leafSigs) {

    // find what signatures are flowing in
    Set<String> inputFlowFieldTypes =
        sigInputProperties.size() == 0 ? null : getFlowTypeSig(sigInputProperties); // Sigs [Integer]
    // find what signatures are flowing out
    Set<String> outputFlowFieldTypes =
        sigOutputProperties.size() == 0 ? null : getFlowTypeSig(sigOutputProperties); // Sigs [Integer]


    for (String sigName : sigNames) {
      Sig.PrimSig sig = sigByName.get(sigName);
      // if connector end type are the same, addEqual is true
      boolean addEqual =
          sigNameWithTransferConnectorWithSameInputOutputFieldType.contains(sigName) ? false : true;

      // only for leafSigs
      if (!leafSigs.contains(sig))
        continue;
      if ((inputFlowFieldTypes == null || !(inputFlowFieldTypes.contains(sigName))) &&
          (outputFlowFieldTypes == null || !(outputFlowFieldTypes.contains(sigName))))
        alloy.addToFacts(AlloyExprFactory.expr_noItemsX(sig));
      // if (!inputOrOutputFlowFieldTypes.contains(sigName))
      // alloy.addToFacts(AlloyFactory.expr_noItemsX(sig));

      // boolean addItem = false; // to avoid adding "no items.x" with both inputs and outputs
      // filter
      // inputs
      if (sigInputProperties.keySet().contains(sigName)) {
        Set<String> propertyNames = sigInputProperties.get(sigName);
        boolean addNoInputsX = true;

        // if (propertyNames.size() > 0)
        // addNoInputsX = false; // so when propertyNames.length != 1 (vin, vout), not to add "no
        // inputs.x" more then one time
        // for (String propertyName : propertyNames) {
        // ie.,
        // fact {all x: BehaviorWithParameter | x.i in x.inputs}
        // fact {all x: BehaviorWithParameter | x.inputs in x.i}
        // BehavioiurParameterIn, InOut
        // BehaviorWithParameter
        // Customer
        // fact {all x: Customer | x.receivedProduct in x.inputs}
        // fact {all x: Customer | x.inputs in x.receivedProduct}
        // &&&
        // fact {all x: Customer | no inputs.x}
        // B2, B, C, B1

        Set<Field> inputsFields = new HashSet<>();
        for (String propertyName : propertyNames) {
          // sig = IFCustomServe, propertyName = IFServe.servedFoodItem
          inputsFields.add(AlloyUtils.getFieldFromSigOrItsParents(propertyName, sig));
        }
        addInInputsAndNoInputXFacts(sig, inputsFields, addNoInputsX, addEqual);
      } else {
        if (inputFlowFieldTypes != null && inputFlowFieldTypes.contains(sigName))// Integer is flowing
          // fact {all x: Integer | no (x.inputs)}
          alloy.addToFacts(AlloyExprFactory.expr_noXInputs(sig));
        else {
          // if removed "no x.inputs" from child remove also from container that should not happens
          // or from AutomicBehavior or SimpleSequence....
          alloy.addToFacts(
              AlloyExprFactory.exprs_noInputsXAndXInputs(sig));
        }
      }
      // outputs
      if (sigOutputProperties.keySet().contains(sigName)) {
        Set<String> propertyNames = sigOutputProperties.get(sigName); // [vout]

        Set<Field> outputsFields = new HashSet<>();
        for (String propertyName : propertyNames) {
          // sig = IFCustomServe, propertyName = IFServe.servedFoodItem
          outputsFields.add(AlloyUtils.getFieldFromSigOrItsParents(propertyName, sig));
        }
        boolean addNoOutputsX = true;
        addInOutputsAndNoOutputsXFacts(sig, outputsFields, addNoOutputsX, addEqual);
      } else {
        if (outputFlowFieldTypes != null && outputFlowFieldTypes.contains(sigName)) {// Integer = type of what is flowing
          // fact {all x: Integer | no (x.outputs)}
          alloy.addToFacts(AlloyExprFactory.expr_noXOutputs(sig));
        } else {
          // both "no outputs.x & no x.outputs"
          alloy.addToFacts(
              AlloyExprFactory.exprs_noOutputsXAndXOutputs(sig));
        }
      }
    }

  }

  /**
   * Find what is flowing in the inputs or outputs of connectors from field's type().fold() method.
   * 
   * For 4.1.4 Transfers and Parameters - ParameterBehavior.als, the inputs map is {B2=[vin], B=[vin], C=[vin], B1= [vin] B2=[vout], A=[vout]} and this method returns [Real].
   * 
   * For 4.1.4 Transfers and Parameters - TransferProduct.als, the inputs map is {Customer = [reveivedProduct]} and this method returns [Product]. Its output maps is {Supplier =[suppliedProduct]} and
   * this method returns [Product]
   * 
   * For 4.2.2 Food Service Object Flow - OFParallelFoodService.als, the inputs map is {OFServe=[servedFoodItem], OFCustomPrepare=[preparedFoodItem, prepareDestination], OFEat=[eatenItem],
   * OFPay=[paidFoodItem, paidAmount], OFCustomServe=[servedFoodItem, serviceDestination]} and this method returns [FoodItem, Real, Location]. Its outputs map is {OFCustomOrder=[orderedFoodItem,
   * orderAmount, orderDestination], OFOrder=[orderedFoodItem], OFCustomPrepare=[preparedFoodItem, prepareDestination], OFPay=[paidFoodItem], OFCustomServe=[servedFoodItem]} and this method returns
   * [FoodItem, Real, Location]
   * 
   * @param inputsOrOutputs - a map where key is a signature name string and value is a set of its input or output field name strings.
   * @return A set of signature name strings of flowing item type name strings.
   */
  protected Set<String> getFlowTypeSig(Map<String, Set<String>> inputsOrOutputs) {

    Set<String> flowTypeSig = new HashSet<>();
    for (String sigName : inputsOrOutputs.keySet()) {
      PrimSig sig = getSig(sigName);
      Set<String> fieldNames = inputsOrOutputs.get(sigName);
      for (String fieldName : fieldNames) {
        Sig.Field f1 = AlloyUtils.getFieldFromSigOrItsParents(fieldName, sig);
        edu.mit.csail.sdg.ast.Type type = f1.type();
        List<List<PrimSig>> folds = type.fold(); // fold contains sig and field's type
        for (List<PrimSig> lp : folds) {
          for (PrimSig s : lp) {
            // when sigName = OFCustomerPrepare, fieldName = prepareFoodItem
            // for s = OFPrepare, the below method return true because OFPrepare is ancestor of OFCustomerPrepare.
            // so its name/label will not be not included in flowTypeSig.
            if (!AlloyUtils.selfOrAncestor(sig, s))
              flowTypeSig.add(s.label);
          }
        }
      }
    }
    return flowTypeSig;
  }

  protected void addFacts(String sigName, Set<Expr> exprs) {
    alloy.addToFacts(AlloyUtils.toSigAllFacts(sigByName.get(sigName), exprs));
  }

  /**
   * Write the Alloy object as a file.
   * 
   * @param outputFile - A file to be written to be the alloy file.
   * @param parameterFields - Set of Fields having <<Parameter>> stereotype. The fields with the stereotype can's be disj.
   * @return true if successfully translated otherwise return false
   * @throws FileNotFoundException - happens when the outputFileName is failed to be created (not exist, not writable etc...)
   */
  protected boolean createAlloyFile(File outputFile, Set<Field> parameterFields)
      throws FileNotFoundException {

    if (outputFile != null && outputFile.getParentFile().canWrite()) {
      alloy.toFile(outputFile.getAbsolutePath(), parameterFields);
      return true;
    }
    return false;
  }



}
