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

public class ToAlloy {

  // Assumption - All PrimSig created have unique names

  private Alloy alloy;
  /**
   * Map key = sig name, value = the PrimSig having the key name. used to retrieve the PrimSig based on its name.
   */
  private Map<String, PrimSig> sigByName;


  /**
   * 
   * @param working_dir where required alloy library (Transfer) is locating
   */
  protected ToAlloy(String working_dir) {
    alloy = new Alloy(working_dir);
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
    if (getSig(name) != null)
      return null;
    PrimSig sig = new PrimSig(name, parentSig);
    alloy.addToAllSigs(sig);
    sigByName.put(name, sig);
    return sig;
  }


  /**
   * Adds the disj alloy fields.
   *
   * @param fieldNamesListWithSameType the field names list with same type
   * @param typeSigName the type sig name
   * @param ownerSig the owner sig
   * @return the sig. field[]
   */
  protected Sig.Field[] addDisjAlloyFields(List<String> fieldNamesListWithSameType,
      String typeSigName, PrimSig ownerSig) {

    String[] fieldNames = new String[fieldNamesListWithSameType.size()];
    fieldNamesListWithSameType.toArray(fieldNames);

    Sig.Field[] ps = null;
    Sig sigType = sigByName.get(typeSigName);
    if (sigType != null) {
      ps = AlloyUtils.addTrickyFields(fieldNames, ownerSig, sigType);
      if (ps.length != fieldNames.length) {
        return null; // this should not happens
      }
    } else
      return null; // this should not happens
    return ps;

  }


  /**
   * 
   * @param ownerSig
   * @param from
   * @param fromField
   * @param to
   * @param toField
   * @param addEqual true if field shared the same type (BehaviorParameter)
   * @param inputsOrOutputs Func Alloy.oinputs or Alloy.ooutputs
   * @return Set of Expr without sig prefix (ie.,{all x: B | bijectionFiltered[targets, x.transferB2B, x]} without "all x:B")
   */
  // protected void createBijectionFilteredInputsOrOutputs(Sig ownerSig, Sig.Field fromField,
  // Expr to, Sig.Field toField, boolean addEqual, Func inputsOrOutputs) {
  //
  // if (addEqual) // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}
  // alloy.addToOverallFact(AlloyFactory.exprs_inField(ownerSig, fromField, fromField, to, toField,
  // inputsOrOutputs));;
  //
  // // Set<Expr> factsWithoutSig =
  // // AlloyFactory.exprs_bijectionFiltered(ownerSig, fromField, to, inputsOrOutputs);
  // // alloy.addToOverallFacts(AlloyUtils.toSigAllFacts(ownerSig, factsWithoutSig));
  //
  //
  // // alloy.addToOverallFacts(
  // // AlloyFactory.exprs_bijectionFilteredFactsForSig(ownerSig, fromField, to, inputsOrOutputs));
  // createBijectionFiltered(ownerSig, fromField, to, inputsOrOutputs);
  // // return factsWithoutSig;
  // }

  // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}
  protected void createInField(Sig ownerSig, Sig.Field fromField,
      Expr to, Sig.Field toField, Func inputsOrOutputs) {
    alloy.addToOverallFact(AlloyFactory.exprs_inField(ownerSig, fromField, fromField, to, toField,
        inputsOrOutputs));;
  }

  /**
   * Creates the bijection filtered happens before and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   * @param func (ie., Alloy.happensBefore, Alloy.happensDuring)
   */
  // private void createBijectionFiltered(Sig ownerSig, Expr from, Expr to, Func func) {
  // alloy.addToOverallFacts(
  // AlloyFactory.exprs_bijectionFilteredFactsForSig(ownerSig, from, to, func));
  // }

  protected void createBijectionFilteredHappensBefore(Sig ownerSig, Expr from, Expr to) {
    alloy.addToOverallFacts(
        AlloyFactory.exprs_bijectionFilteredFactsForSig(ownerSig, from, to, Alloy.happensBefore));
  }

  protected void createBijectionFilteredHappensDuring(Sig ownerSig, Expr from, Expr to) {
    alloy.addToOverallFacts(
        AlloyFactory.exprs_bijectionFilteredFactsForSig(ownerSig, from, to, Alloy.happensDuring));
  }

  protected void createBijectionFilteredInputs(Sig ownerSig, Expr from, Expr to) {
    alloy.addToOverallFacts(
        AlloyFactory.exprs_bijectionFilteredFactsForSig(ownerSig, from, to, Alloy.oinputs));
  }

  protected void createBijectionFilteredOutputs(Sig ownerSig, Expr from, Expr to) {
    alloy.addToOverallFacts(
        AlloyFactory.exprs_bijectionFilteredFactsForSig(ownerSig, from, to, Alloy.ooutputs));
  }


  /**
   * Creates the function filtered happens before and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  protected void createFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.addToOverallFact(
        AlloyFactory.expr_functionFilteredHappensBeforeForSig(ownerSig, from, to));
  }

  /**
   * Creates the inverse function filtered happens before and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  protected void createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig,
      Expr from, Expr to) {
    alloy.addToOverallFact(
        AlloyFactory.expr_inverseFunctionFilteredHappensBefore(ownerSig, from, to));
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
      alloy.addToOverallFact(AlloyFactory.expr_cardinalityEqual(sig, field, lower));
    // addCardinalityEqualConstraintToField(field, sig, lower);
    else if (upper == -1 && lower >= 1) {
      // addCardinalityGreaterThanEqualConstraintToFieldFact(field, sig, lower);
      alloy.addToOverallFact(AlloyFactory.expr_cardinalityGreaterThanEqual(sig, field, lower));
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
        Set<Expr> factsWithoutSig = AlloyFactory.exprs_transferInItems(ownerSig, transfer,
            sourceTypeField, Alloy.sources, targetInputsSourceOutputsFields.get(0));
        facts.addAll(factsWithoutSig);
        if (!toBeInherited)
          alloy.addToOverallFacts(AlloyUtils.toSigAllFacts(ownerSig, factsWithoutSig));
      }
      if (targetInputsSourceOutputsFields.get(1).size() > 0) {
        Set<Expr> factsWithoutSig = AlloyFactory.exprs_transferInItems(ownerSig, transfer,
            targetTypeField, Alloy.targets, targetInputsSourceOutputsFields.get(1));
        facts.addAll(factsWithoutSig);
        if (!toBeInherited)
          alloy.addToOverallFacts(AlloyUtils.toSigAllFacts(ownerSig, factsWithoutSig));
      }
    }
    alloy.addToOverallFacts(AlloyFactory.exprs_bijectionFilteredFactsForSig(ownerSig, transfer,
        sourceTypeField, Alloy.sources));
    alloy.addToOverallFacts(AlloyFactory.exprs_bijectionFilteredFactsForSig(ownerSig, transfer,
        targetTypeField, Alloy.targets));
    alloy.addToOverallFact(AlloyFactory.exprs_subSettingItemRule(ownerSig, transfer));

    return facts;
  }



  /**
   * 
   * @param ownerSig
   * @param transfer ie., transferbeforeAB
   * @param sourceTypeName
   * @param targetTypeName
   */
  protected Set<Expr> addTransferBeforeFacts(PrimSig ownerSig, Expr transfer, String sourceName,
      String targetName, List<Set<Field>> targetInputsSourceOutputsFields, boolean toBeInherited) {

    Set<Expr> factsWithoutAll = new HashSet<>();
    factsWithoutAll.addAll(addTransferFacts(ownerSig, transfer, sourceName, targetName,
        targetInputsSourceOutputsFields, toBeInherited));

    Set<Expr> facts = AlloyFactory.exprs_isAfterSourceIsBeforeTarget(ownerSig, transfer);
    factsWithoutAll.addAll(facts);

    // fact {all x: ParameterBehavior | isAfterSource[x.transferbeforeAB]}
    // fact {all x: ParameterBehavior | isBeforeTarget[x.transferbeforeAB]}
    alloy.addToOverallFacts(AlloyUtils.toSigAllFacts(ownerSig, facts));
    return factsWithoutAll;
  }

  protected void addNoInputsOrOutputsFieldFact(PrimSig ownerSig, String fieldName,
      Func inputsOrOutputs) {
    Sig.Field field = AlloyUtils.getFieldFromSigOrItsParents(fieldName, ownerSig);
    alloy.addToOverallFact(
        AlloyFactory.expr_noInputsOrOutputsField(ownerSig, field, inputsOrOutputs));
  }


  protected void addInInputsAndNoInputXFacts(PrimSig sig, Set<Field> fields,
      boolean addNoInputsX, boolean addEqual) {

    List<Field> sortedFields = AlloyUtils.sortFields(fields);
    if (addEqual)
      alloy.addToOverallFacts(AlloyFactory.exprs_in(sig, sortedFields, Alloy.oinputs));
    if (addNoInputsX)
      alloy.addToOverallFact(AlloyFactory.expr_noInputsX(sig));
  }

  protected void addInOutputsAndNoOutputsXFacts(PrimSig sig, Set<Field> fields,
      boolean addNoOutputsX, boolean addEqual) {

    List<Field> sortedFields = AlloyUtils.sortFields(fields);
    if (addEqual)
      alloy.addToOverallFacts(AlloyFactory.exprs_in(sig, sortedFields, Alloy.ooutputs));
    if (addNoOutputsX)
      alloy.addToOverallFact(AlloyFactory.expr_noOutputsX(sig));
  }



  protected void addInOutClosureFact(PrimSig ownerSig, Field field, Set<Field> fieldOfFields,
      Func inputsOrOutputs) {
    List<Field> sortedfieldOfFields = AlloyUtils.sortFields(fieldOfFields);
    alloy.addToOverallFact(
        AlloyFactory.expr_inOutClosure(ownerSig, field, sortedfieldOfFields, inputsOrOutputs));
  }

  protected void addNoTransferFact(Set<Sig> sigWithTransferFields, Set<PrimSig> leafSigs) {
    Object[] sigs =
        sigByName.values().stream().filter(sig -> !sigWithTransferFields.contains(sig)).toArray();
    for (Object sig : sigs) {
      // adding fact {all x: BuffetService | no y: Transfer | y in x.steps}
      if (leafSigs.contains(sig) && AlloyUtils.hasOwnOrInheritedFields((PrimSig) sig))
        alloy.addToOverallFact(AlloyFactory.expr_noTransferXSteps((PrimSig) sig));
    }
  }

  /**
   * 
   * @param transferingTypeSig (ie., Integer)
   */
  protected void addStepClosureFact(Set<String> transferingTypeSig, Set<PrimSig> leafSigs) {
    for (String sigName : transferingTypeSig) {
      Sig sig = sigByName.get(sigName);
      System.out.println(sigName);
      if (leafSigs.contains(sig))
        // fact {all x: Integer | no steps.x}
        alloy.addToOverallFact(AlloyFactory.expr_noStepsX(sig));
    }
  }

  /**
   * add both types (no steps and steps) of steps if the sig is leafSigs
   * 
   * no steps, for example, * fact {all x: AtomicBehavior | no x.steps}
   * 
   * steps, for examples, fact {all x: SimpleSequence | x.p1 + x.p2 in x.steps} fact {all x: SimpleSequence | x.steps in x.p1 + x.p2}
   * 
   * @param stepPropertiesBySig
   * @param hasParameterFileld boolean if fields with ParameterField exists or not
   * @param leafSig
   */
  protected Set<Sig> addStepsFacts(Map<String, Set<String>> stepPropertiesBySig,
      Set<PrimSig> leafSigs) {

    Set<Sig> noStepsSigs = new HashSet<>();
    for (String sigName : stepPropertiesBySig.keySet()) {

      PrimSig sig = sigByName.get(sigName);
      // if leaf Sig do
      if (leafSigs.contains(sig)) {
        if (stepPropertiesBySig.get(sigName).size() > 0) { // x.steps in .... only leaf or {no
                                                           // steps.x}
          alloy.addToOverallFacts(
              AlloyFactory.exprs_stepsFields(sig, stepPropertiesBySig.get(sigName), true, true));
        } else {
          alloy.addToOverallFact(AlloyFactory.expr_noXSteps(sig));
          noStepsSigs.add(sig);
        }
      } else if (stepPropertiesBySig.get(sigName).size() > 0) // not leaf - (.... in x.steps) only
        alloy.addToOverallFacts(
            AlloyFactory.exprs_stepsFields(sig, stepPropertiesBySig.get(sigName), true, false));
    }
    return noStepsSigs;
  }


  // protected void addRedefinedSubsettingAsFacts(PrimSig sig, Set<Property> properties) {
  // for (Property p : properties) {
  // alloy.addToOverallFact(AlloyFactory.redefinedSubsettingAsFactForSig(sig,
  // AlloyUtils.getFieldFromParentSig(p.getName(), sig),
  // sigByName.get(p.getType().getName())));
  // }
  // }

  // fact {all x: OFFoodService | x.eat in OFEat }
  protected void addRedefinedSubsettingAsFacts(PrimSig sig,
      Map<String, String> propertyNameAndType) {

    for (String pName : propertyNameAndType.keySet()) {
      alloy.addToOverallFact(
          AlloyFactory.expr_redefinedSubsetting(sig, AlloyUtils.getFieldFromParentSig(pName, sig),
              sigByName.get(propertyNameAndType.get(pName))));
    }
  }

  /**
   * this produces like toAlloy.noInputs("Supplier"); toAlloy.noOutputs("Customer");
   * 
   * @param sigInProperties {BehaviorParameterIn=[vin], BehaviorParameterInOut=[vin]}
   * @param outputs {BehaviorParameterOut=[vout], BehaviorParameterInOut=[vout]}
   */
  protected void handleNoInputsOutputs(HashMap<String, Set<String>> sigInputProperties,
      HashMap<String, Set<String>> sigOutputProperties, Set<String> sigNames,
      Set<String> sigNameOfSharedFieldType, Set<PrimSig> leafSigs) {

    Set<String> inputFlowFieldTypes = getFlowTypeSig(sigInputProperties); // Sigs [Integer]
    Set<String> outputFlowFieldTypes = getFlowTypeSig(sigOutputProperties); // Sigs [Integer]
    Set<String> inputOrOutputFlowFieldTypes = new HashSet<String>();
    inputOrOutputFlowFieldTypes.addAll(inputFlowFieldTypes);
    inputOrOutputFlowFieldTypes.addAll(outputFlowFieldTypes);

    for (String sigName : sigNames) {
      Sig.PrimSig sig = sigByName.get(sigName);

      // only for leafSigs
      if (!leafSigs.contains(sig))
        continue;
      if (!inputOrOutputFlowFieldTypes.contains(sigName))
        alloy.addToOverallFact(AlloyFactory.expr_noItemsX(sig));

      // boolean addItem = false; // to avoid adding "no items.x" with both inputs and outputs
      // filter
      // inputs
      if (sigInputProperties.keySet().contains(sigName)) {
        Set<String> propertyNames = sigInputProperties.get(sigName);
        boolean addNoInputsX = true;
        // used to be = but now in x.inputs because to support in inheritance
        boolean addEqual = sigNameOfSharedFieldType.contains(sigName) ? false : true;
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
        if (inputFlowFieldTypes.contains(sigName))// Integer is flowing
          alloy.addToOverallFact(AlloyFactory.expr_noXInputs(sig));// fact
                                                                   // {all
        // x: Integer
        // | no
        // (x.inputs)}
        else {
          // if removed "no x.inputs" from child remove also from container that should not happens
          // or from AutomicBehavior or SimpleSequence....
          alloy.addToOverallFact(
              AlloyFactory.exprs_noInputsXAndXInputs(sig));
        }
      }
      // outputs
      if (sigOutputProperties.keySet().contains(sigName)) {
        Set<String> propertyNames = sigOutputProperties.get(sigName); // [vout]

        boolean addEqual = sigNameOfSharedFieldType.contains(sigName) ? false : true;
        Set<Field> outputsFields = new HashSet<>();
        for (String propertyName : propertyNames) {
          // sig = IFCustomServe, propertyName = IFServe.servedFoodItem
          outputsFields.add(AlloyUtils.getFieldFromSigOrItsParents(propertyName, sig));
        }
        boolean addNoOutputsX = true;
        addInOutputsAndNoOutputsXFacts(sig, outputsFields, addNoOutputsX, addEqual);
      } else {
        if (outputFlowFieldTypes.contains(sigName)) {// Integer = type of what is flowing
          alloy.addToOverallFact(AlloyFactory.expr_noXOutputs(sig));
          // fact {all x: Integer | no (x.outputs)}
        } else {
          alloy.addToOverallFact(
              AlloyFactory.exprs_noOutputsXAndXOutputs(sig));
          // both "no
          // outputs.x" & "no
          // x.outputs"
        }
      }
    }

  }

  /**
   * 
   * @param inputsOrOutputs
   * @return Set of PrimSig names
   */
  protected Set<String> getFlowTypeSig(HashMap<String, Set<String>> inputsOrOutputs) {

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
            if (s != sig) {
              flowTypeSig.add(s.label);
            }
          }
        }
      }
    }
    return flowTypeSig;
  }



  // fact {all x: B1 | x.vin = x.vout}
  protected void addEqualFact(PrimSig ownerSig, String fieldName1, String fieldName2) {
    Field f1 = AlloyUtils.getFieldFromSigOrItsParents(fieldName1, ownerSig);
    Field f2 = AlloyUtils.getFieldFromSigOrItsParents(fieldName2, ownerSig);
    if (f1 != null && f2 != null) {
      alloy.addToOverallFact(AlloyFactory.expr_equal(ownerSig, f1, f2));
    }
  }


  protected void addFacts(String sigName, Set<Expr> exprs) {
    alloy.addToOverallFacts(AlloyUtils.toSigAllFacts(sigByName.get(sigName), exprs));
  }

  /**
   * Write alloy objects as a file.
   * 
   * @param outputFile
   * @param parameterFields - Set of Fields having <<Parameter>> stereotype. The fields with the stereotype can's be disj.
   * @return
   * @throws FileNotFoundException
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
