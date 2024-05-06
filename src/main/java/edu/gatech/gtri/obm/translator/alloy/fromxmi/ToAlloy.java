package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;

public class ToAlloy {

  // Assumption - sig has a uniuqe name in a file

  private Alloy alloy;
  /** store map key = sig name, value = sig */
  private Map<String, PrimSig> sigByName;
  /** Main Sig name used as module name when write it out as an alloy file. */
  private String moduleName;

  /** @param working_dir where required alloy library (Transfer) is locating */
  public ToAlloy(String working_dir) {
    alloy = new Alloy(working_dir);
    sigByName = new HashMap<>();
  }

  /**
   * Gets the sig.
   *
   * @param name the name
   * @return the sig
   */
  public PrimSig getSig(String name) {
    return sigByName.get(name);
  }

  /**
   * Creates the alloy sig. Return PrimSig by name if exist in the instance variable SigbyName.
   * Otherwise create PrimSig by the name and put in the instance variable Map<String, PrimSig>
   * sigByName. If isMainSig is true, the instance variable String moduleName is defined.
   *
   * @param name of sig
   * @param parentSig PrimSig or null (if null, Occurrence will be the parentSig)
   * @param boolean isMainSig mainSig or not
   * @return the prim sig
   */
  public PrimSig createSigOrReturnSig(String name, PrimSig parentSig, boolean isMainSig) {

    if (!sigByName.containsKey(name)) { // when the name is not exist in the sigByName, create
      // PrimSig with appropriate parentSig
      PrimSig s = null;
      if (parentSig
          == null) // name is in AlloyUtils.invalidParentNames, then this sig's parent will
        // be alloy's this/Occurrence
        s = alloy.createSigAsChildOfOccSigAndAddToAllSigs(name);
      else s = alloy.createSigAsChildOfParentSigAddToAllSigs(name, parentSig);
      sigByName.put(name, s);
      if (isMainSig)
        // removing "this/" from s.label and assigns as moduleName
        moduleName = (s.label.startsWith("this") ? s.label.substring(5) : s.label) + "Module";
      return s;
    } else // when existing in the sigByName
    return sigByName.get(name);
  }

  /**
   * Create Sig if not created already
   *
   * @param name the name
   * @param parentName the parent name
   * @return the prim sig
   */
  public PrimSig createAlloySig(String name, String parentName) {

    if (sigByName.containsKey(name)) return sigByName.get(name);
    else {
      PrimSig s = null;
      if (!AlloyUtils.validParent(parentName))
        s = alloy.createSigAsChildOfOccSigAndAddToAllSigs(name);
      else {
        PrimSig parentSig = (PrimSig) sigByName.get(parentName);
        s = alloy.createSigAsChildOfParentSigAddToAllSigs(name, parentSig);
      }
      sigByName.put(name, s);
      return s;
    }
  }

  /**
   * Adds the disj alloy fields.
   *
   * @param fieldNamesListWithSameType the field names list with same type
   * @param typeSigName the type sig name
   * @param ownerSig the owner sig
   * @return the sig. field[]
   */
  public Sig.Field[] addDisjAlloyFields(
      List<String> fieldNamesListWithSameType, String typeSigName, PrimSig ownerSig) {

    String[] fieldNames = new String[fieldNamesListWithSameType.size()];
    fieldNamesListWithSameType.toArray(fieldNames);

    Sig.Field[] ps = null;
    Sig sigType = sigByName.get(typeSigName);
    if (sigType != null) {
      ps = FuncUtils.addTrickyField(fieldNames, ownerSig, sigType);
      if (ps.length != fieldNames.length) {
        return null; // this should not happens
      }
    } else return null; // this should not happens
    return ps;
  }

  // TODO: combine with inputs and outputs
  /**
   * @param ownerSig
   * @param from
   * @param fromField
   * @param to
   * @param toField
   * @param addEqual true if field shared the same type (BehaviorParameter)
   */

  // public Set<Expr> createBijectionFilteredInputsAndAddToOverallFact(Sig ownerSig,
  // Sig.Field fromField, Expr to, Sig.Field toField, boolean addEqual) {
  // return createBijectionFilteredInputsOrOutputsAndAddToOverallFact(ownerSig, fromField, to,
  // toField, addEqual, Alloy.oinputs);
  // }
  //
  //
  // public Set<Expr> createBijectionFilteredOutputsAndAddToOverallFact(Sig ownerSig,
  // Sig.Field fromField, Expr to, Sig.Field toField, boolean addEqual) {
  // return createBijectionFilteredInputsOrOutputsAndAddToOverallFact(ownerSig, fromField, to,
  // toField, addEqual, Alloy.ooutputs);
  // }

  /**
   * @param ownerSig
   * @param from
   * @param fromField
   * @param to
   * @param toField
   * @param addEqual true if field shared the same type (BehaviorParameter)
   * @param inputsOrOutputs Func Alloy.oinputs or Alloy.ooutputs
   */
  public Set<Expr> createBijectionFilteredInputsOrOutputs(
      Sig ownerSig,
      Sig.Field fromField,
      Expr to,
      Sig.Field toField,
      boolean addEqual,
      Func inputsOrOutputs) {

    Set<Expr> facts = new HashSet<>();
    facts.addAll(
        alloy.createBijectionFilteredToOverallFact(ownerSig, fromField, to, inputsOrOutputs));
    if (addEqual) // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}
    alloy.createEqualFieldToOverallFact(
          ownerSig, fromField, fromField, to, toField, inputsOrOutputs);
    return facts;
  }

  /**
   * Creates the bijection filtered happens before and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  // public void createBijectionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
  // Expr to) {
  // alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.happensBefore);
  // }

  public void createBijectionFiltered(Sig ownerSig, Expr from, Expr to, Func func) {
    alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, func);
  }

  /**
   * Creates the bijection filtered happens during and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  // public void createBijectionFilteredHappensDuringAndAddToOverallFact(Sig ownerSig, Expr from,
  // Expr to) {
  // alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.happensDuring);
  // }

  /**
   * Creates the function filtered happens before and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  public void createFunctionFilteredHappensBeforeAndAddToOverallFact(
      Sig ownerSig, Expr from, Expr to) {
    alloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, from, to);
  }

  /**
   * Creates the inverse function filtered happens before and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  public void createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(
      Sig ownerSig, Expr from, Expr to) {
    alloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, from, to);
  }

  /**
   * Adds the cardinality equal constraint to field.
   *
   * @param field the field
   * @param ownerSig the owner sig
   * @param num the num
   */
  public void addCardinalityEqualConstraintToField(Sig.Field field, PrimSig ownerSig, int num) {
    alloy.addCardinalityEqualConstraintToField(ownerSig, field, num);
  }

  /**
   * Adds the cardinality equal constraint to field.
   *
   * @param fieldName the field name
   * @param ownerSig the owner sig
   * @param num the num
   */
  public void addCardinalityEqualConstraintToField(String fieldName, PrimSig ownerSig, int num) {
    Sig.Field field = AlloyUtils.getFieldFromSigOrItsParents(fieldName, ownerSig); // FoodService <:
    // order,
    if (field != null) alloy.addCardinalityEqualConstraintToField(ownerSig, field, num);
    else
      System.err.println(
          "A field \"" + fieldName + "\" not found in Sig \"" + ownerSig.label + "\".");
  }

  /**
   * Adds the cardinality greater than equal constraint to field.
   *
   * @param field the field
   * @param ownerSig the owner sig
   * @param num the num
   */
  public void addCardinalityGreaterThanEqualConstraintToField(
      Sig.Field field, PrimSig ownerSig, int num) {
    alloy.addCardinalityGreaterThanEqualConstraintToField(ownerSig, field, num);
  }

  /**
   * Adds the cardinality greater than equal constraint to field.
   *
   * @param fieldName the field name
   * @param ownerSig the owner sig
   * @param num the num
   */
  public void addCardinalityGreaterThanEqualConstraintToField(
      String fieldName, PrimSig ownerSig, int num) {
    Sig.Field field = AlloyUtils.getFieldFromSigOrItsParents(fieldName, ownerSig);
    if (field != null) alloy.addCardinalityGreaterThanEqualConstraintToField(ownerSig, field, num);
    else
      System.err.println(
          "A field \"" + fieldName + "\" not found in Sig \"" + ownerSig.label + "\".");
  }

  public Set<Expr> createFnForTransfer(
      PrimSig ownerSig,
      Expr transfer,
      String source,
      String target,
      List<Set<Field>> targetInputsSourceOutputsFields,
      boolean toBeInherited) {

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
      if (targetInputsSourceOutputsFields.get(0).size() > 0)
        facts.addAll(
            alloy.createTransferInItems(
                ownerSig,
                transfer,
                sourceTypeField,
                Alloy.sources,
                targetInputsSourceOutputsFields.get(0),
                toBeInherited));
      if (targetInputsSourceOutputsFields.get(1).size() > 0)
        facts.addAll(
            alloy.createTransferInItems(
                ownerSig,
                transfer,
                targetTypeField,
                Alloy.targets,
                targetInputsSourceOutputsFields.get(1),
                toBeInherited));
    }
    // facts.addAll(alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, sourceTypeField,
    // Alloy.sources));
    // facts.addAll(alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, targetTypeField,
    // Alloy.targets));
    // facts.addAll(alloy.createSubSettingItemRuleOverallFact(ownerSig, transfer));

    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, sourceTypeField, Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, targetTypeField, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, transfer);

    return facts;
  }

  /**
   * @param ownerSig
   * @param transfer ie., transferbeforeAB
   * @param sourceTypeName
   * @param targetTypeName
   */
  public Set<Expr> createFnForTransferBefore(
      PrimSig ownerSig,
      Expr transfer,
      String sourceName,
      String targetName,
      List<Set<Field>> targetInputsSourceOutputsFields,
      boolean toBeInherited) {

    Set<Expr> facts = new HashSet<>();
    facts.addAll(
        createFnForTransfer(
            ownerSig,
            transfer,
            sourceName,
            targetName,
            targetInputsSourceOutputsFields,
            toBeInherited));
    facts.addAll(alloy.createIsAfterSourceIsBeforeTargetOverallFact(ownerSig, transfer));
    // fact {all x: ParameterBehavior | isAfterSource[x.transferbeforeAB]}//missing
    // fact {all x: ParameterBehavior | isBeforeTarget[x.transferbeforeAB]}//missing
    return facts;
  }

  // public void createNoInputsOrOutputField(String sigName, String fieldName, Func inputsOrOutputs)
  // {
  // createNoInputsOrOutputsField(sigName, fieldName, inputsOrOutputs);
  // }

  public void createNoInputsOrOutputsField(
      PrimSig ownerSig, String fieldName, Func inputsOrOutputs) {
    Sig.Field field = AlloyUtils.getFieldFromSigOrItsParents(fieldName, ownerSig);
    alloy.createNoInputsOrOutputsField(ownerSig, field, inputsOrOutputs);
  }

  // public void addInputsAndNoInputsX(Sig.PrimSig sig, Set<Field> fields, boolean addNoInputsX,
  // boolean addEqual) {
  // List<Field> sortedFields = AlloyUtils.sortFields(fields);
  //
  // if (addEqual)
  // alloy.addEqual2(sig, sortedFields, Alloy.oinputs);
  // if (addNoInputsX)
  // alloy.noInputsOrOutputsX(sig, Alloy.oinputs);
  //
  // }

  // public void addOutputsAndNoOutputsX(PrimSig sig, Set<Field> fields, boolean addNoOutputsX,
  // boolean addEqual) {
  //
  // List<Field> sortedFields = AlloyUtils.sortFields(fields);
  // if (addEqual)
  // alloy.addEqual2(sig, sortedFields, Alloy.ooutputs);
  // if (addNoOutputsX)
  // alloy.noInputsOrOutputsX(sig, Alloy.ooutputs);
  // }

  public void addInOrOutputsAndNoInOrOutputsX(
      PrimSig sig, Set<Field> fields, boolean addNoOutputsX, boolean addEqual, Func func) {

    List<Field> sortedFields = AlloyUtils.sortFields(fields);
    if (addEqual) alloy.addEqual2(sig, sortedFields, func);
    if (addNoOutputsX) alloy.noInputsOrOutputsX(sig, func);
  }

  public void createInOutClosure(
      PrimSig ownerSig, Field field, Set<Field> fieldOfFields, Func inputsOrOutputs) {
    List<Field> sortedfieldOfFields = AlloyUtils.sortFields(fieldOfFields);
    alloy.createInOutClosure(ownerSig, field, sortedfieldOfFields, inputsOrOutputs);
  }

  public void handleNoTransfer(Set<Sig> sigWithTransferFields, Set<PrimSig> leafSigs) {
    Object[] sigs =
        sigByName.values().stream().filter(sig -> !sigWithTransferFields.contains(sig)).toArray();
    for (Object sig : sigs) {
      if (leafSigs.contains(sig) && ((PrimSig) sig).getFields().size() > 0)
        alloy.noTransferStep((PrimSig) sig);
    }
  }

  /** @param transferingTypeSig (ie., Integer) */
  public void handleStepClosure(Set<String> transferingTypeSig, Set<PrimSig> leafSigs) {
    for (String sigName : transferingTypeSig) {
      Sig sig = sigByName.get(sigName);
      if (leafSigs.contains(sig)) alloy.noStepsX(sig); // fact {all x: Integer | no steps.x}
    }
  }

  /**
   * add both types (no steps and steps) of steps if the sig is leafSigs
   *
   * <p>no steps, for example, * fact {all x: AtomicBehavior | no x.steps}
   *
   * <p>steps, for examples, fact {all x: SimpleSequence | x.p1 + x.p2 in x.steps} fact {all x:
   * SimpleSequence | x.steps in x.p1 + x.p2}
   *
   * @param stepPropertiesBySig
   * @param hasParameterFileld boolean if fields with ParameterField exists or not
   * @param leafSig
   */
  public Set<Sig> addSteps(Map<String, Set<String>> stepPropertiesBySig, Set<PrimSig> leafSigs) {

    Set<Sig> noStepsSigs = new HashSet<>();
    for (String sigName : stepPropertiesBySig.keySet()) {

      PrimSig sig = sigByName.get(sigName);
      // if leaf Sig do
      if (leafSigs.contains(sig)) {
        if (stepPropertiesBySig.get(sigName).size() > 0) { // x.steps in .... only leaf
          alloy.addSteps(sig, stepPropertiesBySig.get(sigName), true, true);
        } else {
          alloy.noXSteps(sig);
          noStepsSigs.add(sig);
        }
      } else if (stepPropertiesBySig.get(sigName).size() > 0) // not leaf - (.... in x.steps) only
      alloy.addSteps(sig, stepPropertiesBySig.get(sigName), true, false);
    }
    return noStepsSigs;
  }

  // fact {all x: OFFoodService | x.eat in OFEat }
  public void addRedefinedSubsettingAsFacts(PrimSig sig, Set<Property> properties) {
    for (Property p : properties) {
      alloy.addRedefinedSubsettingAsFact(
          sig,
          AlloyUtils.getFieldFromParentSig(p.getName(), sig),
          sigByName.get(p.getType().getName()));
    }
  }

  /**
   * this produces like toAlloy.noInputs("Supplier"); toAlloy.noOutputs("Customer");
   *
   * @param sigInProperties {BehaviorParameterIn=[vin], BehaviorParameterInOut=[vin]}
   * @param outputs {BehaviorParameterOut=[vout], BehaviorParameterInOut=[vout]}
   */
  public void handleNoInputsOutputs(
      HashMap<String, Set<String>> sigInputProperties,
      HashMap<String, Set<String>> sigOutputProperties,
      Set<String> sigNames,
      Set<String> sigNameOfSharedFieldType,
      Set<PrimSig> leafSigs,
      Map<String, NamedElement> namedElementsBySigName) {

    Set<String> inputFlowFieldTypes = getFlowTypeSig(sigInputProperties); // Sigs [Integer]
    Set<String> outputFlowFieldTypes = getFlowTypeSig(sigOutputProperties); // Sigs [Integer]
    Set<String> inputOrOutputFlowFieldTypes = new HashSet<String>();
    inputOrOutputFlowFieldTypes.addAll(inputFlowFieldTypes);
    inputOrOutputFlowFieldTypes.addAll(outputFlowFieldTypes);

    for (String sigName : sigNames) {
      Sig.PrimSig sig = sigByName.get(sigName);

      // only for leafSigs
      if (!leafSigs.contains(sig)) continue;
      if (!inputOrOutputFlowFieldTypes.contains(sigName)) alloy.noItemsX(sig);

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

        addInOrOutputsAndNoInOrOutputsX(sig, inputsFields, addNoInputsX, addEqual, Alloy.oinputs);
      } else {
        if (inputFlowFieldTypes.contains(sigName)) // Integer is flowing
        alloy.noXInputsOrOutputs(sig, Alloy.oinputs); // fact {all x: Integer | no (x.inputs)}
        else {
          // if removed "no x.inputs" from child remove also from container that should not happens
          // or from AutomicBehavior or SimpleSequence....
          alloy.noInputsXAndXInputsOrOutputsXAndXOutputs(sig, Alloy.oinputs);
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
        addInOrOutputsAndNoInOrOutputsX(
            sig, outputsFields, addNoOutputsX, addEqual, Alloy.ooutputs);
      } else {
        if (outputFlowFieldTypes.contains(sigName)) { // Integer = type of what is flowing
          alloy.noXInputsOrOutputs(sig, Alloy.ooutputs); // fact {all x: Integer | no (x.outputs)}
        } else {
          alloy.noInputsXAndXInputsOrOutputsXAndXOutputs(sig, Alloy.ooutputs); // both "no
          // outputs.x" & "no
          // x.outputs"
        }
      }
    }
  }

  /**
   * @param inputsOrOutputs
   * @return Set of PrimSig names
   */
  public Set<String> getFlowTypeSig(HashMap<String, Set<String>> inputsOrOutputs) {

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
  public void addEqual(PrimSig ownerSig, String fieldName1, String fieldName2) {
    Field f1 = AlloyUtils.getFieldFromSigOrItsParents(fieldName1, ownerSig);
    Field f2 = AlloyUtils.getFieldFromSigOrItsParents(fieldName2, ownerSig);
    if (f1 != null && f2 != null) {
      alloy.addEqual(ownerSig, f1, f2);
    }
  }

  /**
   * Convert alloy objects into the given outputFile
   *
   * @param outputFile
   * @param parameterFields - Set of Fields having <<Parameter>> stereotype. The fields with the
   *     stereotype can's be disj.
   * @return
   */
  public boolean createAlloyFile(File outputFile, Set<Field> parameterFields) {

    // Run commands
    // Command command = alloy.createCommand(moduleName, 10);
    // Command[] commands = {command};

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact() /* , commands */);

    Translator translator =
        new Translator(
            alloy.getIgnoredExprs(), // alloy.getIgnoredFuncs(),
            alloy.getIgnoredSigs(),
            parameterFields);

    if (outputFile != null) {
      String outputFileName = outputFile.getAbsolutePath();
      translator.generateAlsFileContents(alloyModule, outputFileName);
      return true;
    }
    return false;
  }

  /**
   * Adds the alloy transfer field.
   *
   * @param fieldName the field name
   * @param ownerSig the owner sig
   * @return the field
   */
  public static Field addAlloyTransferField(String fieldName, Sig ownerSig) {
    // Sig transferSig = alloy.getTransferSig();
    return FuncUtils.addField(fieldName, ownerSig, Alloy.transferSig);
  }

  public void addFacts(String sigName, Set<Expr> exprs) {
    alloy.addFacts(sigByName.get(sigName), exprs);
  }
}
