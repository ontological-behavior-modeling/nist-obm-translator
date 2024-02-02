package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;

public class ToAlloy {


  private Alloy alloy;
  /**
   * store map key = sig name, value = sig
   */
  private Map<String, PrimSig> sigByName;
  /**
   * Main Sig name used as module name when write it out as an alloy file.
   */
  private String moduleName;

  /**
   * 
   * @param working_dir where required alloy library (Transfer) is locating
   */
  public ToAlloy(String working_dir) {
    alloy = new Alloy(working_dir);
    sigByName = new HashMap<>();
  }

  public PrimSig getSig(String name) {
    return sigByName.get(name);
  }

  /**
   * return PrimSig by name if exist in the instance variable SigbyName. Otherwise create PrimSig by
   * the name and put in the instance variable Map<String, PrimSig> sigByName. If isMainSig is true,
   * the instance variable String moduleName is defined.
   * 
   * @param name of sig
   * @param parentSig PrimSig or null (if null, Occurrence will be the parentSig)
   * @param boolean isMainSig mainSig or not
   * @return
   */
  public PrimSig createSigOrReturnSig(String name, PrimSig parentSig, boolean isMainSig) {

    if (!sigByName.containsKey(name)) {// when the name is not exist in the sigByName, create
                                       // PrimSig with appropriate parentSig
      PrimSig s = null;
      if (parentSig == null)// name is in AlloyUtils.invalidParentNames, then this sig's parent will
                            // be alloy's this/Occurrence
        s = alloy.createSigAsChildOfOccSigAndAddToAllSigs(name);
      else
        s = alloy.createSigAsChildOfParentSigAddToAllSigs(name, parentSig);
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
   * @param name
   * @param parentName
   * @return
   */
  public PrimSig createAlloySig(String name, String parentName) {

    if (sigByName.containsKey(name))
      return sigByName.get(name);
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


  // create field with Transfer type
  public Field addAlloyTransferField(String fieldName, Sig ownerSig) {
    // Sig transferSig = alloy.getTransferSig();
    return FuncUtils.addField(fieldName, ownerSig, Alloy.transferSig);
  }

  // create field with TransferBefore type
  // public Field addAlloyTransferBeforeField(String fieldName, Sig ownerSig) {
  // // Sig transferBeforeSig = alloy.getTransferBeforeSig();
  // return FuncUtils.addField(fieldName, ownerSig, Alloy.transferBeforeSig);
  // }

  public Sig.Field[] addDisjAlloyFields(List<String> fieldNamesListWithSameType, String typeSigName,
      PrimSig ownerSig) {

    String[] fieldNames = new String[fieldNamesListWithSameType.size()];
    fieldNamesListWithSameType.toArray(fieldNames);

    Sig.Field[] ps = null;
    Sig sigType = sigByName.get(typeSigName);
    if (sigType != null) {
      ps = FuncUtils.addTrickyField(fieldNames, ownerSig, sigType);
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
   */
  public void createBijectionFilteredInputsAndAddToOverallFact(Sig ownerSig, Expr from,
      Sig.Field fromField, Expr to, Sig.Field toField, boolean addEqual) {
    alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.oinputs);

    if (addEqual) // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}
      alloy.createEqualFieldInputsToOverallFact(ownerSig, from, fromField, to, toField);
  }

  /**
   * 
   * @param ownerSig
   * @param from
   * @param fromField
   * @param to
   * @param toField
   * @param addEqual true if field shared the same type (BehaviorParameter)
   */
  public void createBijectionFilteredOutputsAndAddToOverallFact(Sig ownerSig, Expr from,
      Sig.Field fromField, Expr to, Sig.Field toField, boolean addEqual) {
    alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.ooutputs);
    if (addEqual)
      alloy.createEqualFieldOutputsToOverallFact(ownerSig, from, fromField, to, toField);
  }

  public void createBijectionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.happensBefore);
  }

  public void createBijectionFilteredHappensDuringAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.happensDuring);
  }

  public void createFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, from, to);
  }

  public void createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    alloy.createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(ownerSig, from, to);
  }

  public void addCardinalityEqualConstraintToField(Sig.Field field, PrimSig ownerSig, int num) {
    alloy.addCardinalityEqualConstraintToField(ownerSig, field, num);
  }

  public void addCardinalityEqualConstraintToField(String fieldName, PrimSig ownerSig, int num) {
    Sig.Field field = AlloyUtils.getFieldFromSig(fieldName, ownerSig); // FoodService <: order,
                                                                       // ownerSig
    // is SignleFoolService
    if (field != null)
      alloy.addCardinalityEqualConstraintToField(ownerSig, field, num);
    else
      System.err
          .println("A field \"" + fieldName + "\" not found in Sig \"" + ownerSig.label + "\".");
  }


  public void addCardinalityGreaterThanEqualConstraintToField(Sig.Field field, PrimSig ownerSig,
      int num) {
    alloy.addCardinalityGreaterThanEqualConstraintToField(ownerSig, field, num);
  }

  public void addCardinalityGreaterThanEqualConstraintToField(String fieldName, PrimSig ownerSig,
      int num) {
    Sig.Field field = AlloyUtils.getFieldFromSig(fieldName, ownerSig);
    if (field != null)
      alloy.addCardinalityGreaterThanEqualConstraintToField(ownerSig, field, num);
    else
      System.err
          .println("A field \"" + fieldName + "\" not found in Sig \"" + ownerSig.label + "\".");
  }

  public void createFnForTransferAndAddToOverallFact(PrimSig ownerSig, Expr transfer, String source,
      String target) {

    // for 4.1.4 Transfers and Parameters1 - TransferProduct_modified
    // sig ParticipantTransfer
    // sourceTypeName Supplier -> Field supplier
    // targetTypeName Customer -> Field customer


    Field sourceTypeField = AlloyUtils.getFieldFromSig(source, ownerSig);
    Field targetTypeField = AlloyUtils.getFieldFromSig(target, ownerSig);

    // fact {all x: ParticipantTransfer | bijectionFiltered[sources, x.transferSupplierCustomer,
    // x.supplier]}
    // fact {all x: ParticipantTransfer | bijectionFiltered[targets, x.transferSupplierCustomer,
    // x.customer]}
    // fact {all x: ParticipantTransfer | subsettingItemRuleForSources[x.transferSupplierCustomer]}
    // fact {all x: ParticipantTransfer | subsettingItemRuleForTargets[x.transferSupplierCustomer]}

    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, sourceTypeField, Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, targetTypeField, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, transfer);
  }

  /**
   * 
   * @param ownerSig
   * @param transfer ie., transferbeforeAB
   * @param sourceTypeName
   * @param targetTypeName
   */


  public void createFnForTransferBeforeAndAddToOverallFact(PrimSig ownerSig, Expr transfer,
      String sourceName, String targetName) {

    createFnForTransferAndAddToOverallFact(ownerSig, transfer, sourceName, targetName);
    alloy.createIsAfterSourceIsBeforeTargetOverallFact(ownerSig, transfer);
    // fact {all x: ParameterBehavior | isAfterSource[x.transferbeforeAB]}//missing
    // fact {all x: ParameterBehavior | isBeforeTarget[x.transferbeforeAB]}//missing
  }

  public void createNoInputsField(String sigName, String fieldName) {
    PrimSig sig = sigByName.get(sigName);
    Sig.Field field = AlloyUtils.getFieldFromSig(fieldName, sig);
    alloy.createNoInputsField(sig, field);
  }

  public void createNoOutputsField(String sigName, String fieldName) {
    PrimSig sig = sigByName.get(sigName);
    Sig.Field field = AlloyUtils.getFieldFromSig(fieldName, sig);
    alloy.createNoOutputsField(sig, field);
  }

  //
  // public void noInputsOutputs(Sig sig) {
  // alloy.noInputs(sig);
  // alloy.noOutputs(sig);
  // }



  // public void noOutputsBothAndItem(String sigName) {
  // Sig sig = sigByName.get(sigName);
  // alloy.noOutputsBothAndItem(sig);
  // }

  // public void addInputs(String sigName, String fieldName) {
  // PrimSig sig = sigByName.get(sigName);
  // ExprVar s = ExprVar.make(null, "x", sig.type());
  // Field f = AlloyUtils.getFieldFromSig(fieldName, sig);
  // if (f != null)
  // alloy.addInputs(s, sig, f);
  // else
  // System.err.println("No field \"" + fieldName + "\" in sig \"" + sigName + "\"");
  // }

  public void addInputsAndNoInputsX(Sig.PrimSig sig, String fieldName, boolean addNoInputsX,
      boolean addEqual) {
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Field f = AlloyUtils.getFieldFromSig(fieldName, sig);
    if (f != null)
      alloy.addInputsAndNoInputsX(s, sig, f, addNoInputsX, addEqual);
    else
      System.err.println("No field \"" + fieldName + "\" in sig \"" + sig.label + "\"");
  }

  // public void addInputs(String sigName, String fieldName) {
  // PrimSig sig = sigByName.get(sigName);
  // ExprVar s = ExprVar.make(null, "x", sig.type());
  // Field f = AlloyUtils.getFieldFromSig(fieldName, sig);
  // if (f != null)
  // alloy.addInputs(s, sig, f);
  // else
  // System.err.println("No field \"" + fieldName + "\" in sig \"" + sigName + "\"");
  // }

  // public void addOutputs(String sigName, String fieldName) {
  // PrimSig sig = sigByName.get(sigName);
  // ExprVar s = ExprVar.make(null, "x", sig.type());
  // Field f = AlloyUtils.getFieldFromSig(fieldName, sig);
  // if (f != null)
  // alloy.addOutputs(s, sig, f);
  // else
  // System.err.println("No field \"" + fieldName + "\" in sig \"" + sigName + "\"");
  // }

  public void addOutputsAndNoOutputsX(PrimSig sig, String fieldName, boolean addNoOutputsX,
      boolean addEqual) {
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Field f = AlloyUtils.getFieldFromSig(fieldName, sig);
    if (f != null)
      alloy.addOutputsAndNoOutputsX(s, sig, f, addNoOutputsX, addEqual);
    else
      System.err.println("No field \"" + fieldName + "\" in sig \"" + sig.label + "\"");
  }


  public void handleNoTransfer(Set<Sig> sigWithTransferFields) {

    Object[] sigs =
        sigByName.values().stream().filter(sig -> !sigWithTransferFields.contains(sig)).toArray();
    for (Object sig : sigs) {
      if (((PrimSig) sig).getFields().size() > 0)
        alloy.noTransferStep((PrimSig) sig);
    }
  }

  /**
   * 
   * @param transferingTypeSig (ie., Integer)
   */
  public void handleStepClosure(Set<String> transferingTypeSig) {
    for (String sigName : transferingTypeSig) {
      Sig sig = sigByName.get(sigName);
      alloy.noStepsX(sig); // fact {all x: Integer | no steps.x}
    }
  }

  /**
   * handle both steps
   * 
   * no steps, for example, fact {all x: AtomicBehavior | no x.steps}
   * 
   * steps, for examples, fact {all x: SimpleSequence | x.p1 + x.p2 in x.steps} fact {all x:
   * SimpleSequence | x.steps in x.p1 + x.p2}
   * 
   * @param stepPropertiesBySig
   * @param hasParameterFileld boolean if fields with ParameterField exists or not
   */
  public Set<Sig> handleSteps(Map<String, Set<String>> stepPropertiesBySig,
      Set<Field> parameterFields) {

    Set<Sig> noStepsSigs = new HashSet<>();
    for (String sigName : stepPropertiesBySig.keySet()) {
      Sig sig = sigByName.get(sigName);
      System.out.println(sigName);

      if (stepPropertiesBySig.get(sigName).size() > 0) {
        alloy.addSteps(sig, stepPropertiesBySig.get(sigName));
        // alloy.noTransferStep(sig); // i.e., fact {all x: SimpleSequence | no y: Transfer | y in
        // x.steps}
        // } else if (parameterFields.size() == 0) { // parameterFields is for entire model
        // alloy.noSteps(sig); // add like fact {all x: AtomicBehavior | no x.steps}
      } else {
        // boolean hasParamterFields = AlloyUtils.hasParameterField(sig, parameterFields);
        // if (!hasParamterFields) {
        alloy.noXSteps(sig);
        noStepsSigs.add(sig);
        // } else {
        // // alloy.noStepsX(sig);// "no steps.x"
        // noStepsSigs.add(sig);
        // }
      }
    }
    return noStepsSigs;

  }

  public Set<Sig> handleStepsOriginal(Map<String, Set<String>> stepPropertiesBySig,
      Set<Field> parameterFields) {

    Set<Sig> noStepsSigs = new HashSet<>();
    for (String sigName : stepPropertiesBySig.keySet()) {
      Sig sig = sigByName.get(sigName);
      System.out.println(sigName);
      if (stepPropertiesBySig.get(sigName).size() > 0) {
        alloy.addSteps(sig, stepPropertiesBySig.get(sigName));
        // alloy.noTransferStep(sig); // i.e., fact {all x: SimpleSequence | no y: Transfer | y in
        // x.steps}
        // } else if (parameterFields.size() == 0) { // parameterFields is for entire model
        // alloy.noSteps(sig); // add like fact {all x: AtomicBehavior | no x.steps}
      } else {
        boolean hasParamterFields = false;
        for (Field f : parameterFields) {
          System.out.println(f.label);
          if (f.sig == sig) {
            // alloy.noTransferStep(sig); // {all x: BehaviorWithParameterInOut | no y: Transfer | y
            // in
            // x.steps}

            hasParamterFields = true;
            break;
          }
        }
        if (!hasParamterFields) {
          alloy.noXSteps(sig);
          noStepsSigs.add(sig);
        }
      }
    }
    return noStepsSigs;

  }

  /**
   * 
   * @param sigInputProperties key = ownerSigName, value = field of the ownerSig
   * @param sigOutputProperties
   * @param sigNames
   */
  public void addBijectionInputsOutputsToContainer(HashMap<String, Set<String>> sigInputProperties,
      HashMap<String, Set<String>> sigOutputProperties, Set<String> sigNames) {
    for (String sigName : sigNames) {
      Sig ownerSig = sigByName.get(sigName);
      if (AlloyUtils.hasTransferField(ownerSig)) {

        Set<String> inputChildSigTypeNames = sigInputProperties.keySet();
        for (String inputChildSigTypeName : inputChildSigTypeNames) {
          List<Sig.Field> inputOwnerSigFields =
              AlloyUtils.findFieldWithType(ownerSig, inputChildSigTypeName);
          for (Sig.Field inputOwnerSigField : inputOwnerSigFields) {
            System.out.println("bijection Inputs");
            System.out.println(inputOwnerSigField.label);
            for (String sigInputPropertyForChildSig : sigInputProperties.get(inputChildSigTypeName))
              System.out.println(sigInputPropertyForChildSig);
          }
        }

        Set<String> outputChildSigTypeNames = sigOutputProperties.keySet();
        for (String outputChildSigTypeName : outputChildSigTypeNames) {
          List<Sig.Field> outputOwnerSigFields =
              AlloyUtils.findFieldWithType(ownerSig, outputChildSigTypeName);
          for (Sig.Field outputOwnerSigField : outputOwnerSigFields) {
            System.out.println("bijection Outputs");
            System.out.println(outputOwnerSigField.label);
            for (String sigOutputPropertyForChildSig : sigOutputProperties
                .get(outputChildSigTypeName))
              System.out.println(sigOutputPropertyForChildSig);
          }
        }

      }
    }
  }

  /**
   * this produces like toAlloy.noInputs("Supplier"); toAlloy.noOutputs("Customer");
   * 
   * @param sigInProperties {BehaviorParameterIn=[vin], BehaviorParameterInOut=[vin]}
   * @param outputs {BehaviorParameterOut=[vout], BehaviorParameterInOut=[vout]}
   */
  public void handleNoInputsOutputs(HashMap<String, Set<String>> sigInputProperties,
      HashMap<String, Set<String>> sigOutputProperties, Set<String> sigNames,
      Set<String> sigNameOfSharedFieldType) {


    Set<String> inputFlowFieldTypes = getFlowTypeSig(sigInputProperties); // Sigs [Integer]
    Set<String> outputFlowFieldTypes = getFlowTypeSig(sigOutputProperties); // Sigs [Integer]
    Set<String> inputOrOutputFlowFieldTypes = new HashSet<String>();
    inputOrOutputFlowFieldTypes.addAll(inputFlowFieldTypes);
    inputOrOutputFlowFieldTypes.addAll(outputFlowFieldTypes);

    for (String sigName : sigNames) {
      Sig.PrimSig sig = sigByName.get(sigName);

      if (!inputOrOutputFlowFieldTypes.contains(sigName))
        alloy.noItemsX(sig);

      // boolean addItem = false; // to avoid adding "no items.x" with both inputs and outputs
      // filter
      if (sigInputProperties.keySet().contains(sigName)) {
        Set<String> propertyNames = sigInputProperties.get(sigName);
        boolean addNoInputsX = true;
        for (String propertyName : propertyNames) {
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

          boolean addEqual = sigNameOfSharedFieldType.contains(sigName) ? false : true;
          addInputsAndNoInputsX(sig, propertyName, addNoInputsX, addEqual);
          // addInputs(sigName, propertyName);
          addNoInputsX = false; // so when propertyNames.length != 1 (vin, vout), not to add "no
          // inputs.x" more then one time
        }
      } else {
        if (inputFlowFieldTypes.contains(sigName))// Integer is flowing
          alloy.noXInputs(sig);// fact {all x: Integer | no (x.inputs)}
        else {
          // addItem = true;
          // noInputsBothAndItem(sigName); // "no x.inputs", "no inputs.x", "no items.x." -
          // BehaviourParameterOut

          // if removed "no x.inputs" from child remove also from container that should not happens
          // !!!!!!!!!!!!!! or from AutomicBehavior or SimpleSequence....
          alloy.noInputsXAndXInputs(sig);
          // alloy.noInputsX(sig); //"no inputs.x" only

        }
      }
      if (sigOutputProperties.keySet().contains(sigName)) {
        Set<String> propertyNames = sigOutputProperties.get(sigName); // [vout]
        boolean addNoOutputsX = true;
        for (String propertyName : propertyNames) {
          // ie.,
          // BehaviorWithParameter | "x.i in x.outputs" & "x.outputs in x.i"
          // BehaviourParemeterout | "x.vouts in x.outputs" & "x.outputs in x.vout"
          // InOut
          // Supplier
          // B2, A, B, B1
          // addOutputs(sigName, propertyName);
          boolean addEqual = sigNameOfSharedFieldType.contains(sigName) ? false : true;
          addOutputsAndNoOutputsX(sig, propertyName, addNoOutputsX, addEqual);
          addNoOutputsX = false;
        }
      } else {
        if (outputFlowFieldTypes.contains(sigName)) // Integer = type of what is flowing
          alloy.noXOutputs(sig);// fact {all x: Integer | no (x.outputs)}
        else {
          // alloy.noOutputsX(sig); //"no outputs.x" only
          alloy.noOutputsXAndXOutputs(sig); // both "no outputs.x" & "no x.outputs"
          // if (addItem)
          // noOutputsXAndXOutputs(sigName); // "no outputs.x" & "no x.outputs
          // else
          // noOutputsBothAndItem(sigName); // "no outputs.x", "no x.outputs" and "no items.x"
        }

      }
    }
  }

  /**
   * 
   * @param inputsOrOutputs
   * @return Set of PrimSig names
   */
  public Set<String> getFlowTypeSig(HashMap<String, Set<String>> inputsOrOutputs) {

    Set<String> flowTypeSig = new HashSet<>();
    for (String sigName : inputsOrOutputs.keySet()) {
      PrimSig sig = getSig(sigName);
      Set<String> fieldNames = inputsOrOutputs.get(sigName);
      for (String fieldName : fieldNames) {
        Sig.Field f1 = AlloyUtils.getFieldFromSig(fieldName, sig);
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



  /**
   * this produces like toAlloy.noInputs("Supplier"); toAlloy.noOutputs("Customer");
   * 
   * @param inputs
   * @param outputs
   */
  // public void handleNoInputsOutputs(Set<String> inputs, Set<String> outputs,
  // Set<String> allClasseNames) {
  // // Set<String> insAndOuts =
  // // Stream.of(inputs, outputs).flatMap(x -> x.stream()).collect(Collectors.toSet());
  // // Set<String> allSigs = allClasses.stream().map(c -> c.getName()).collect(Collectors.toSet());
  // for (String s : allClasseNames) {
  // if (!inputs.contains(s))
  // noInputs(s);
  // if (!outputs.contains(s))
  // noOutputs(s);
  // }
  // }

  // fact {all x: B1 | x.vin=x.vout}
  public void addEqual(PrimSig ownerSig, String fieldName1, String fieldName2) {
    Field f1 = AlloyUtils.getFieldFromSig(fieldName1, ownerSig);
    Field f2 = AlloyUtils.getFieldFromSig(fieldName2, ownerSig);
    if (f1 != null && f2 != null) {
      alloy.addEqual(ownerSig, f1, f2);
    }
  }

  public String createAlloyFile(File outputFile, Set<Field> parameterFields) {


    // Run commands
    Command command = alloy.createCommand(moduleName, 10);
    Command[] commands = {command};

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact(), commands);

    Translator translator = new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(),
        alloy.getIgnoredSigs(), parameterFields);

    if (outputFile != null) {
      String outputFileName = outputFile.getAbsolutePath();
      translator.generateAlsFileContents(alloyModule, outputFileName);
      return outputFileName;
    }
    // Utils.runX(mainSig, alloy.getAllSigs(), alloy.getOverAllFact(), command);


    return "No outputfile ";
  }


}
