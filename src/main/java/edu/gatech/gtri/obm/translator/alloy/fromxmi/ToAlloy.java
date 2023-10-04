package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.Helper;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.gatech.gtri.obm.translator.alloy.tofile.Translator;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;

public class ToAlloy {

  private Alloy alloy;
  private Map<String, PrimSig> sigByName;
  private Sig mainSig;

  public ToAlloy(String working_dir) {
    alloy = new Alloy(working_dir);
    sigByName = new HashMap<>();
  }


  /**
   * 
   * @param name
   * @param parentSig PrimSig or null (if null, Occurrence will be the parentSig)
   * @param isMainSig
   * @return
   */
  public PrimSig createAlloySig(String name, PrimSig parentSig, boolean isMainSig) {
    PrimSig s = null;
    if (!sigByName.containsKey(name)) {
      if (parentSig == null)
        s = alloy.createSigAsChildOfOccSigAndAddToAllSigs(name); // Occurrence as the parent
      else
        s = alloy.createSigAsChildOfParentSigAddToAllSigs(name, parentSig);
      sigByName.put(name, s);
      if (isMainSig)
        mainSig = s;
      return s;
    } else
      return sigByName.get(name);
  }

  /**
   *
   * @param name
   * @param parentName - parent must not be from library (i.e., Occurrence)
   * @param isMainSig
   * @return
   */
  public PrimSig createAlloySig2(String name, String parentName, boolean isMainSig) {
    PrimSig s = null;
    if (!sigByName.containsKey(name)) {
      PrimSig parentSig = (PrimSig) sigByName.get(parentName);
      System.out.println(parentSig);
      s = alloy.createSigAsChildOfParentSigAddToAllSigs(name, parentSig);

      sigByName.put(name, s);
      if (isMainSig)
        mainSig = s;
      return s;
    } else
      return sigByName.get(name);
  }


  public Expr getOverallFacts() {
    return alloy.getOverAllFact();
  }

  public List<Sig> getAllSigs() {
    return alloy.getAllSigs();
  }

  public List<PrimSig> getHierarchySigs() {
    List<PrimSig> list = new ArrayList<PrimSig>();
    List<PrimSig> renamingSigs = new ArrayList<>();
    for (PrimSig aSig : sigByName.values()) {
      if (aSig.parent.label.equals("this/Occurrence")) {
        list.add(aSig);
      } else
        renamingSigs.add(aSig);
    }

    while (renamingSigs.size() > 0) {
      PrimSig aSig = renamingSigs.get(0);
      boolean added = addToHierarchySigList(list, aSig);
      if (added)
        renamingSigs.remove(0);
    }

    return list;
  }

  private boolean addToHierarchySigList(List<PrimSig> list, PrimSig aSig) {
    Optional<PrimSig> parentSig = list.stream().filter(s -> s == aSig.parent).findFirst();
    if (parentSig.isPresent()) {
      int indexOfParent = list.indexOf(parentSig.get());
      list.add(indexOfParent + 1, aSig);
      return true;
    }
    return false;
  }


  public PrimSig getSig(String name) {
    return sigByName.get(name);
  }


  public Map<String, PrimSig> getSigMap() {
    return sigByName;
  }


  public PrimSig createAlloySig(String name, String parentName) {
    if (!Helper.validParent(parentName))
      return createAlloySig(name, (PrimSig) null, false);
    else
      return createAlloySig2(name, parentName, false);
  }

  /**
   * Create Sig in Alloy with "Occurrence" as its parent
   * 
   * @param name
   * @return
   */
  public PrimSig createAlloySig(String name) {
    return createAlloySig(name, (PrimSig) null, false);
  }

  public Sig getTransferSig() {
    return alloy.getTransferSig();
  }

  // create field with Transfer type
  public Field addAlloyTransferField(String fieldName, Sig ownerSig) {
    Sig transferSig = alloy.getTransferSig();
    return FuncUtils.addField(fieldName, ownerSig, transferSig);
  }

  // create field with TransferBefore type
  public Field addAlloyTransferBeforeField(String fieldName, Sig ownerSig) {
    Sig transferBeforeSig = alloy.getTransferBeforeSig();
    return FuncUtils.addField(fieldName, ownerSig, transferBeforeSig);
  }

  public Sig.Field[] addDisjAlloyFields(List<String> fieldNamesListWithSameType, String typeSigName,
      PrimSig ownerSig) {

    String[] fieldNames = toArray(fieldNamesListWithSameType);
    Sig.Field[] ps = null;
    Sig sigType = sigByName.get(typeSigName);
    if (sigType != null) {
      ps = FuncUtils.addTrickyField(fieldNames, ownerSig, sigType);
      if (ps.length != fieldNames.length) {
        System.err.println("!!!!!!!!!!! Thread issue?");
      }
    } else
      System.err.println("sigType is null");
    return ps;

  }

  // public Sig.Field[] addDisjAlloyFields(List<String> fieldNamesList, String typeSigName,
  // String ownerSigName) {
  //
  // String[] fieldNames = toArray(fieldNamesList);
  // Sig.Field[] ps = null;
  // Sig sigType = sigByName.get(typeSigName);
  // if (sigType != null) {
  // ps = FuncUtils.addTrickyField(fieldNames, sigByName.get(ownerSigName), sigType);
  // while (ps.length != fieldNames.length) {
  // System.out.println(
  // "??????ps.length = " + ps.length + " fieldNames.length = " + fieldNames.length);
  // try {
  // Thread.sleep(5000);
  // } catch (InterruptedException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // ps = FuncUtils.addTrickyField(fieldNames, sigByName.get(ownerSigName), sigType);
  //
  // }
  // for (int i = 0; i < fieldNames.length; i++) {
  // // fieldByName.put(fieldNames[i], ps[i]);
  // // fieldTypeByFieldName.put(fieldNames[i], typeSig);
  // fieldTypeByField.put(ps[i], sigByName.get(typeSigName));
  // }
  // } else
  // System.out.println("sigType is null");
  // return ps;
  //
  // }



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
    Sig.Field field = Helper.getFieldFromSig(fieldName, ownerSig); // FoodService <: order, ownerSig
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
    Sig.Field field = Helper.getFieldFromSig(fieldName, ownerSig);
    if (field != null)
      alloy.addCardinalityGreaterThanEqualConstraintToField(ownerSig, field, num);
    else
      System.err
          .println("A field \"" + fieldName + "\" not found in Sig \"" + ownerSig.label + "\".");
  }


  public void createFnForTransferAndAddToOverallFact(PrimSig ownerSig, Expr transfer,
      String sourceTypeName, String targetTypeName) {

    // for 4.1.4 Transfers and Parameters1 - TransferProduct_modified
    // sig ParticipantTransfer
    // sourceTypeName Supplier -> Field supplier
    // targetTypeName Customer -> Field customer

    // assume only one field has the same type
    Field sourceTypeField = Helper.getFieldFromSigByFieldType(sourceTypeName, ownerSig);
    Field targetTypeField = Helper.getFieldFromSigByFieldType(targetTypeName, ownerSig);

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
      String sourceTypeName, String targetTypeName) {

    Field sourceTypeField = Helper.getFieldFromSigByFieldType(sourceTypeName, ownerSig);
    Field targetTypeField = Helper.getFieldFromSigByFieldType(targetTypeName, ownerSig);


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



  public void createFnForTransferBeforeAndAddToOverallFact(PrimSig ownerSig, Expr start,
      Expr middle, Expr end, String sourceTypeName, String targetTypeName) {

    Field sourceTypeField = Helper.getFieldFromSigByFieldType(sourceTypeName, ownerSig);
    Field targetTypeField = Helper.getFieldFromSigByFieldType(targetTypeName, ownerSig);


    /** start --- Constraints on the Transfer from input of B to intput of B1 */
    // fact {all x: B | functionFiltered[sources, x.transferBB1, x]}//missing
    // fact {all x: B | bijectionFiltered[targets, x.transferBB1, x.b1]}//missing
    // fact {all x: B | subsettingItemRuleForSources[x.transferBB1]}//missing
    // fact {all x: B | subsettingItemRuleForTargets[x.transferBB1]}//missing
    // fact {all x: B | isBeforeTarget[x.transferBB1]}//missing

    alloy.createFunctionFilteredAndAddToOverallFact(ownerSig, start, sourceTypeField,
        Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, start, targetTypeField, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, start);

    /** middle Constraints on the Transfer from output of B1 to input of B2 */
    // fact {all x: B | bijectionFiltered[sources, x.transferbeforeB1B2, x.b1]}//missing
    // fact {all x: B | bijectionFiltered[targets, x.transferbeforeB1B2, x.b2]}//missing
    // fact {all x: B | subsettingItemRuleForSources[x.transferBB1]}//missing
    // fact {all x: B | subsettingItemRuleForTargets[x.transferBB1]}//missing
    // fact {all x: B | isAfterSource[x.transferbeforeB1B2]}//missing
    // fact {all x: B | isBeforeTarget[x.transferbeforeB1B2]}//missing
    alloy.createBijectionFilteredToOverallFact(ownerSig, middle, sourceTypeField, Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, middle, targetTypeField, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, middle);

    /** Constraints on the Transfer from output of B2 to output of B */
    // fact {all x: B | bijectionFiltered[sources, x.transferB2B, x.b2]}//missing
    // fact {all x: B | functionFiltered[targets, x.transferB2B, x]}//missing
    // fact {all x: B | subsettingItemRuleForSources[x.transferB2B]}//missing
    // fact {all x: B | subsettingItemRuleForTargets[x.transferB2B]}//missing
    // fact {all x: B | isAfterSource[x.transferB2B]}//missing
    alloy.createFunctionFilteredAndAddToOverallFact(ownerSig, end, sourceTypeField, Alloy.sources);
    alloy.createBijectionFilteredToOverallFact(ownerSig, end, targetTypeField, Alloy.targets);
    alloy.createSubSettingItemRuleOverallFact(ownerSig, end);
  }

  public void noInputs(String sigName) {
    Sig sig = sigByName.get(sigName);
    alloy.noInputs(sig);
  }

  public void noOutputs(String sigName) {
    Sig sig = sigByName.get(sigName);
    alloy.noOutputs(sig);
  }

  public void addInputs(String sigName, String fieldName) {
    PrimSig sig = sigByName.get(sigName);
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Field f = Helper.getFieldFromSig(fieldName, sig);
    if (f != null)
      alloy.addInputs(s, sig, f);
    else
      System.err.println("No field \"" + fieldName + "\" in sig \"" + sigName + "\"");
  }

  public void addOutputs(String sigName, String fieldName) {
    PrimSig sig = sigByName.get(sigName);
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Field f = Helper.getFieldFromSig(fieldName, sig);
    if (f != null)
      alloy.addOutputs(s, sig, f);
    else
      System.err.println("No field \"" + fieldName + "\" in sig \"" + sigName + "\"");
  }

  public void addSteps(List<String> noStepSigs) {
    for (Sig sig : sigByName.values()) {
      if (!noStepSigs.contains(sig.label)) {
        ExprVar s = ExprVar.make(null, "x", sig.type());
        alloy.addSteps(s, sig);
      }
    }
  }


  public String createAlloyFile(File outputFile) {

    // set modulename without this
    String moduleName =
        mainSig.label.startsWith("this") ? mainSig.label.substring(5) : mainSig.label;

    // Run commands
    Command command = alloy.createCommand(moduleName, 10);
    Command[] commands = {command};

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact(), commands);

    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());

    if (outputFile != null) {
      String outputFileName = outputFile.getAbsolutePath();
      translator.generateAlsFileContents(alloyModule, outputFileName);
      return outputFileName;
    }
    // Utils.runX(mainSig, alloy.getAllSigs(), alloy.getOverAllFact(), command);


    return "No outputfile ";
  }


  public String[] toArray(List<String> o) {
    String[] r = new String[o.size()];
    int i = 0;
    for (String s : o) {
      r[i++] = s;
    }
    return r;
  }

}
