package edu.gatech.gtri.obm.translator.alloy.fromxmi;

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
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/** The Class ToAlloy. */
public class ToAlloy {

  /** The alloy. */
  private Alloy alloy;

  /** store map key = sig name, value = sig. */
  private Map<String, PrimSig> sigByName;
  /** Main Sig name used as module name when write it out as an alloy file. */
  private String moduleName;

  /**
   * Instantiates a new to alloy.
   *
   * @param working_dir where required alloy library (Transfer) is locating
   */
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
   * Creates the alloy sig.
   *
   * @param name of sig
   * @param parentSig PrimSig or null (if null, Occurrence will be the parentSig)
   * @param isMainSig mainSig or not
   * @return the prim sig
   */
  public PrimSig createAlloySig(String name, PrimSig parentSig, boolean isMainSig) {

    if (!sigByName.containsKey(name)) {
      PrimSig s = null;
      if (parentSig == null)
        s = alloy.createSigAsChildOfOccSigAndAddToAllSigs(name); // "Occurrence" as the parent
      else s = alloy.createSigAsChildOfParentSigAddToAllSigs(name, parentSig);
      sigByName.put(name, s);
      if (isMainSig) moduleName = s.label.startsWith("this") ? s.label.substring(5) : s.label;

      return s;
    } else return sigByName.get(name);
  }

  /**
   * Creates the alloy sig.
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
   * Adds the alloy transfer field.
   *
   * @param fieldName the field name
   * @param ownerSig the owner sig
   * @return the field
   */
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

  /**
   * Creates the bijection filtered happens before and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  public void createBijectionFilteredHappensBeforeAndAddToOverallFact(
      Sig ownerSig, Expr from, Expr to) {
    alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.happensBefore);
  }

  /**
   * Creates the bijection filtered happens during and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  public void createBijectionFilteredHappensDuringAndAddToOverallFact(
      Sig ownerSig, Expr from, Expr to) {
    alloy.createBijectionFilteredToOverallFact(ownerSig, from, to, Alloy.happensDuring);
  }

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
    Sig.Field field = AlloyUtils.getFieldFromSig(fieldName, ownerSig); // FoodService <: order,
    // ownerSig
    // is SignleFoolService
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
    Sig.Field field = AlloyUtils.getFieldFromSig(fieldName, ownerSig);
    if (field != null) alloy.addCardinalityGreaterThanEqualConstraintToField(ownerSig, field, num);
    else
      System.err.println(
          "A field \"" + fieldName + "\" not found in Sig \"" + ownerSig.label + "\".");
  }

  /**
   * Creates the fn for transfer and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param transfer the transfer
   * @param sourceTypeName the source type name
   * @param targetTypeName the target type name
   */
  public void createFnForTransferAndAddToOverallFact(
      PrimSig ownerSig, Expr transfer, String sourceTypeName, String targetTypeName) {

    // for 4.1.4 Transfers and Parameters1 - TransferProduct_modified
    // sig ParticipantTransfer
    // sourceTypeName Supplier -> Field supplier
    // targetTypeName Customer -> Field customer

    // assume only one field has the same type
    Field sourceTypeField = AlloyUtils.getFieldFromSigByFieldType(sourceTypeName, ownerSig);
    Field targetTypeField = AlloyUtils.getFieldFromSigByFieldType(targetTypeName, ownerSig);

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
   * Creates the fn for transfer before and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param transfer ie., transferbeforeAB
   * @param sourceTypeName the source type name
   * @param targetTypeName the target type name
   */
  public void createFnForTransferBeforeAndAddToOverallFact(
      PrimSig ownerSig, Expr transfer, String sourceTypeName, String targetTypeName) {

    Field sourceTypeField = AlloyUtils.getFieldFromSigByFieldType(sourceTypeName, ownerSig);
    Field targetTypeField = AlloyUtils.getFieldFromSigByFieldType(targetTypeName, ownerSig);

    // fact {all x: ParticipantTransfer | bijectionFiltered[sources, x.transferSupplierCustomer,
    // x.supplier]}
    // fact {all x: ParticipantTransfer | bijectionFiltered[targets, x.transferSupplierCustomer,
    // x.customer]}
    // fact {all x: ParticipantTransfer | subsettingItemRuleForSources[x.transferSupplierCustomer]}
    // fact {all x: ParticipantTransfer | subsettingItemRuleForTargets[x.transferSupplierCustomer]}

    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, sourceTypeField, Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, transfer, targetTypeField, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, transfer);
    alloy.createIsAfterSourceIsBeforeTargetOverallFact(ownerSig, transfer);

    // fact {all x: ParameterBehavior | isAfterSource[x.transferbeforeAB]}//missing
    // fact {all x: ParameterBehavior | isBeforeTarget[x.transferbeforeAB]}//missing
  }

  /**
   * No inputs outputs.
   *
   * @param sig the sig
   */
  public void noInputsOutputs(Sig sig) {
    alloy.noInputs(sig);
    alloy.noOutputs(sig);
  }

  /**
   * No inputs.
   *
   * @param sigName the sig name
   */
  public void noInputs(String sigName) {
    Sig sig = sigByName.get(sigName);
    alloy.noInputs(sig);
  }

  /**
   * No outputs.
   *
   * @param sigName the sig name
   */
  public void noOutputs(String sigName) {
    Sig sig = sigByName.get(sigName);
    alloy.noOutputs(sig);
  }

  /**
   * Adds the inputs.
   *
   * @param sigName the sig name
   * @param fieldName the field name
   */
  public void addInputs(String sigName, String fieldName) {
    PrimSig sig = sigByName.get(sigName);
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Field f = AlloyUtils.getFieldFromSig(fieldName, sig);
    if (f != null) alloy.addInputs(s, sig, f);
    else System.err.println("No field \"" + fieldName + "\" in sig \"" + sigName + "\"");
  }

  /**
   * Adds the outputs.
   *
   * @param sigName the sig name
   * @param fieldName the field name
   */
  public void addOutputs(String sigName, String fieldName) {
    PrimSig sig = sigByName.get(sigName);
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Field f = AlloyUtils.getFieldFromSig(fieldName, sig);
    if (f != null) alloy.addOutputs(s, sig, f);
    else System.err.println("No field \"" + fieldName + "\" in sig \"" + sigName + "\"");
  }

  /**
   * Adds the steps.
   *
   * @param stepPropertiesBySig the step properties by sig
   */
  public void addSteps(Map<String, Set<String>> stepPropertiesBySig) {
    for (String sigName : stepPropertiesBySig.keySet()) {
      Sig sig = sigByName.get(sigName);
      alloy.addSteps(sig, stepPropertiesBySig.get(sigName));
    }
  }

  /**
   * Adds the equal.
   *
   * @param ownerSig the owner sig
   * @param fieldName1 the field name 1
   * @param fieldName2 the field name 2
   */
  // fact {all x: B1 | x.vin=x.vout}
  public void addEqual(PrimSig ownerSig, String fieldName1, String fieldName2) {
    Field f1 = AlloyUtils.getFieldFromSig(fieldName1, ownerSig);
    Field f2 = AlloyUtils.getFieldFromSig(fieldName2, ownerSig);
    if (f1 != null && f2 != null) {
      alloy.addEqual(ownerSig, f1, f2);
    }
  }

  /**
   * Creates the alloy file.
   *
   * @param outputFile the output file
   * @param parameterFields the parameter fields
   * @return the string
   */
  public String createAlloyFile(File outputFile, Set<Field> parameterFields) {

    // Run commands
    Command command = alloy.createCommand(moduleName, 10);
    Command[] commands = {command};

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact(), commands);

    Translator translator =
        new Translator(
            alloy.getIgnoredExprs(),
            alloy.getIgnoredFuncs(),
            alloy.getIgnoredSigs(),
            parameterFields);

    if (outputFile != null) {
      String outputFileName = outputFile.getAbsolutePath();
      translator.generateAlsFileContents(alloyModule, outputFileName);
      return outputFileName;
    }
    // Utils.runX(mainSig, alloy.getAllSigs(), alloy.getOverAllFact(), command);

    return "No outputfile ";
  }
}
