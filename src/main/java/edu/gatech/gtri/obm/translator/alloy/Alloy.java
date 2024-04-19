package edu.gatech.gtri.obm.translator.alloy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprBinary;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprHasName;
import edu.mit.csail.sdg.ast.ExprList;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.mit.csail.sdg.parser.CompUtil;

public class Alloy {

  protected static PrimSig occSig;
  protected static Module templateModule;
  public static Module transferModule;

  protected static Set<Sig> ignoredSigs;
  protected static Set<Expr> ignoredExprs;
  protected static Set<Func> ignoredFuncs;

  public static Func happensBefore;
  public static Func happensDuring;

  public static Func sources;
  public static Func targets;
  public static Func subsettingItemRuleForSources;
  public static Func subsettingItemRuleForTargets;
  public static Func isAfterSource;
  public static Func isBeforeTarget;

  protected static Func bijectionFiltered;
  protected static Func functionFiltered;
  protected static Func inverseFunctionFiltered;

  protected static Func osteps;
  public static Func oinputs;
  public static Func ooutputs;
  public static Func oitems;

  protected ExprList uniqueFact;
  protected Expr templateFact;
  protected Expr overallFact;
  protected List<Sig> allSigs;
  protected Expr _nameExpr;

  public static Sig transferSig;
  public static Sig transferBeforeSig;

  protected static final String templateString =
      "open Transfer[Occurrence] as o \n" + "abstract sig Occurrence{}";


  /**
   * 
   * @param working_dir where required alloy library defined in templateString is locating.
   */
  public Alloy(String working_dir) {

    System.setProperty(("java.io.tmpdir"), working_dir);
    templateModule = CompUtil.parseEverything_fromString(new A4Reporter(), templateString);

    // can not define directory because Module.getAllReachableUserDefinedSigs returns ConstList<Sig>
    // and does not allow to add to the list
    allSigs = new ArrayList<Sig>();
    ignoredSigs = new HashSet<>();
    for (Sig sig : templateModule.getAllReachableUserDefinedSigs()) {
      allSigs.add(sig);
      ignoredSigs.add(sig);
    }
    // add all facts from the occurrence module.
    ignoredExprs = new HashSet<>();

    ExprList exprList = (ExprList) templateModule.getAllReachableFacts();
    for (Expr expr : exprList.args) {
      ignoredExprs.add(expr);
    }


    // abstract
    occSig = (PrimSig) AlloyUtils.getReachableSig(templateModule, "this/Occurrence");
    transferModule = AlloyUtils.getAllReachableModuleByName(templateModule, "TransferModule");
    ignoredFuncs = new HashSet<>();
    for (Module module : transferModule.getAllReachableModules()) {
      for (Func func : module.getAllFunc()) {
        ignoredFuncs.add(func);
      }
    }

    happensBefore = AlloyUtils.getFunction(transferModule, "o/happensBefore");
    happensDuring = AlloyUtils.getFunction(transferModule, "o/happensDuring");
    bijectionFiltered = AlloyUtils.getFunction(transferModule, "o/bijectionFiltered");
    functionFiltered = AlloyUtils.getFunction(transferModule, "o/functionFiltered");
    inverseFunctionFiltered = AlloyUtils.getFunction(transferModule, "o/inverseFunctionFiltered");

    sources = AlloyUtils.getFunction(transferModule, "o/sources");
    targets = AlloyUtils.getFunction(transferModule, "o/targets");
    subsettingItemRuleForSources =
        AlloyUtils.getFunction(transferModule, "o/subsettingItemRuleForSources");
    subsettingItemRuleForTargets =
        AlloyUtils.getFunction(transferModule, "o/subsettingItemRuleForTargets");

    isAfterSource = AlloyUtils.getFunction(transferModule, "o/isAfterSource");
    isBeforeTarget = AlloyUtils.getFunction(transferModule, "o/isBeforeTarget");


    osteps = AlloyUtils.getFunction(transferModule, "o/steps");
    oinputs = AlloyUtils.getFunction(transferModule, "o/inputs");
    ooutputs = AlloyUtils.getFunction(transferModule, "o/outputs");
    oitems = AlloyUtils.getFunction(transferModule, "o/items");

    transferSig = AlloyUtils.getReachableSig(transferModule, "o/Transfer");
    transferBeforeSig = AlloyUtils.getReachableSig(transferModule, "o/TransferBefore");
  }


  public Module getTransferModule() {
    return Alloy.transferModule;
  }

  public Module getTemplateModule() {
    return Alloy.templateModule;
  }


  public PrimSig getOccSig() {
    return Alloy.occSig;
  }

  public Expr getOverAllFact() {
    if (overallFact == null) {
      return templateModule.getAllReachableFacts();
    }

    return this.overallFact.and(templateModule.getAllReachableFacts());
  }

  public List<Sig> getAllSigs() {
    return this.allSigs;
  }

  public PrimSig createSigAndAddToAllSigs(String label, PrimSig parent) {
    PrimSig s = new PrimSig(label, parent);
    allSigs.add(s);
    return s;
  }

  public PrimSig createSigAsChildOfOccSigAndAddToAllSigs(String label) {
    return createSigAndAddToAllSigs(label, Alloy.occSig);
  }

  public PrimSig createSigAsChildOfParentSigAddToAllSigs(String label, PrimSig parentSig) {
    return createSigAndAddToAllSigs(label, parentSig);
  }

  public void addToOverallFact(Expr expr) {
    if (overallFact == null) {
      overallFact = expr;
    } else {
      overallFact = overallFact.and(expr);
    }
  }


  public Set<Sig> getIgnoredSigs() {
    return ignoredSigs;
  }


  public Set<Expr> getIgnoredExprs() {
    return ignoredExprs;
  }


  public Set<Func> getIgnoredFuncs() {
    return ignoredFuncs;
  }

  /**
   * support when Expr original is ExprBinary(ie., p1 + p2) to add ExprVar s in both so returns s.p1
   * and s.p2. if original is like "BuffetService <: (FoodService <: eat)" -> ((ExprBinary)
   * original).op = "<:", in this case just return s.join(original) =
   * 
   * @param s
   * @param original
   * @return
   */
  private Expr addExprVarToExpr(ExprVar s, Expr original) {
    if (original instanceof ExprBinary) {
      Expr left = addExprVarToExpr(s, ((ExprBinary) original).left);
      Expr right = addExprVarToExpr(s, ((ExprBinary) original).right);
      if (((ExprBinary) original).op == ExprBinary.Op.PLUS)
        return left.plus(right);
      else
        return s.join(original); // x . BuffetService <: (FoodService <: eat) where original =
                                 // "BuffetService <: (FoodService <: eat)" with ((ExprBinary)
                                 // original).op = "<:"
    } else {
      return s.join(original); // x.BuffetService
    }
  }

  public void createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);
    Expr inverseFunctionFilteredExpr = inverseFunctionFiltered.call(happensBefore.call(),
        addExprVarToExpr(varX, from), addExprVarToExpr(varX, to));
    this.addToOverallFact(inverseFunctionFilteredExpr.forAll(decl));
  }

  /**
   * Creates a inverseFunctionFiltered fact with happensBefore. Use when "from" or "to" has a +
   * sign. fact f3 {all s: Loop | functionFiltered[happensBefore, s.p2, s.p2 + s.p3]} ownerSig=Loop;
   * from={p2}; to={p2, p3}
   * 
   * In this example, "to" has a + sign.
   */
  public void createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig,
      Expr[] from, Expr[] to) {

    assert from.length > 0 : "error: from.length must be greater than 0";
    assert to.length > 0 : "error: to.length must be greater than 0";

    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);

    Expr _from = varX.join(from[0]), _to = varX.join(to[0]);

    for (int i = 1; i < from.length; i++) {
      _from = _from.plus(varX.join(from[i]));
    }
    for (int i = 1; i < to.length; i++) {
      _to = _to.plus(varX.join(to[i]));
    }

    Expr inverseFunctionFilteredExpression =
        inverseFunctionFiltered.call(happensBefore.call(), _from, _to);


    addToOverallFact(inverseFunctionFilteredExpression.forAll(decl));
  }

  /**
   * Creates a functionFiltered fact. Example Alloy fact: fact f1 { all s: Loop |
   * functionFiltered[happensBefore, s.p1, s.p2] } ownerSig = Loop, from = p1, to = p2
   * 
   * This function doesn't handle the case where "from" or "to" has a plus sign in it. Example Alloy
   * fact this function can't create: fact f3 {all s: Loop | functionFiltered[happensBefore, s.p2,
   * s.p2 + s.p3]}
   * 
   * I wrote another function below to handle this case with + sign.
   * createFunctionFilteredHappensBeforeAndAllToOverAllFact( Sig ownerSig, Expr[] from, Expr[] to)
   */
  public void createFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);
    Expr funcFilteredExpr = functionFiltered.call(happensBefore.call(),
        addExprVarToExpr(varX, from), addExprVarToExpr(varX, to));
    this.addToOverallFact(funcFilteredExpr.forAll(decl));
  }


  /**
   * Creates a functionFiltered fact with happensBefore. Use when "from" or "to" has a + sign. fact
   * f3 {all s: Loop | functionFiltered[happensBefore, s.p2, s.p2 + s.p3]} ownerSig=Loop; from={p2};
   * to={p2, p3}
   * 
   * In this example, "to" has a + sign.
   */
  public void createFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr[] from,
      Expr[] to) {

    assert from.length > 0 : "error: from.length must be greater than 0";
    assert to.length > 0 : "error: to.length must be greater than 0";

    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Expr _from = s.join(from[0]), _to = s.join(to[0]);

    for (int i = 1; i < from.length; i++) {
      _from = _from.plus(s.join(from[i]));
    }
    for (int i = 1; i < to.length; i++) {
      _to = _to.plus(s.join(to[i]));
    }

    Expr funcFilteredExpr = functionFiltered.call(happensBefore.call(), _from, _to);
    List<ExprHasName> names = List.of(s);
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    addToOverallFact(funcFilteredExpr.forAll(decl));
  }

  /**
   * Creates the sub setting item rule overall fact.
   *
   * @param ownerSig the owner sig
   * @param transfer the transfer
   */
  public Set<Expr> createSubSettingItemRuleOverallFact(Sig ownerSig, Expr transfer) {
    Set<Expr> factsAdded = new HashSet<>();
    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);
    this.addToOverallFact(subsettingItemRuleForSources.call(varX.join(transfer)).forAll(decl));
    this.addToOverallFact(subsettingItemRuleForTargets.call(varX.join(transfer)).forAll(decl));

    factsAdded.add(subsettingItemRuleForSources.call(varX.join(transfer)));
    factsAdded.add(subsettingItemRuleForTargets.call(varX.join(transfer)));
    return factsAdded;
  }

  /**
   * Creates the is after source is before target overall fact.
   *
   * @param ownerSig the owner sig
   * @param transfer the transfer
   */
  public Set<Expr> createIsAfterSourceIsBeforeTargetOverallFact(Sig ownerSig, Expr transfer) {

    Set<Expr> factsAdded = new HashSet<>();
    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);
    this.addToOverallFact(isAfterSource.call(varX.join(transfer)).forAll(decl));
    this.addToOverallFact(isBeforeTarget.call(varX.join(transfer)).forAll(decl));
    factsAdded.add(isAfterSource.call(varX.join(transfer)));
    factsAdded.add(isBeforeTarget.call(varX.join(transfer)));
    return factsAdded;

  }


  public void addFacts(Sig ownerSig, Set<Expr> facts) {
    Decl decl = makeDecl(makeVarX(ownerSig), ownerSig);
    for (Expr fact : facts)
      this.addToOverallFact(fact.forAll(decl));
  }

  // fact {all x: OFSingleFoodService | x.transferOrderPay.sources.orderedFoodItem +
  // x.transferOrderPay.sources.orderAmount in x.transferOrderPay.items}
  // fact {all x: OFSingleFoodService | x.transferOrderPay.items in
  // x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount}
  public Set<Expr> createTransferInItems(Sig ownerSig, Expr transfer, Expr toField, Func func,
      Set<Field> targetInputsSourceOutputsFields, boolean toBeInherited) {
    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);
    Expr funcCall = func.call();

    List<Field> sortedTargetInputsSourceOutputsFields =
        AlloyUtils.sortFields(targetInputsSourceOutputsFields);
    Expr all = null, all_r = null;
    for (Field field : sortedTargetInputsSourceOutputsFields) {
      all = all == null ? varX.join(transfer).join(funcCall).join(field)
          : all.plus(varX.join(transfer).join(funcCall).join(field));
    }
    // all = x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount

    // x.transferOrderPay.items in
    // x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount
    all_r = varX.join(transfer).join(Alloy.oitems.call()).in(all);

    // x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount in
    // x.transferOrderPay.items
    all = all.in(varX.join(transfer).join(Alloy.oitems.call()));

    if (!toBeInherited) {
      this.addToOverallFact(all.forAll(decl));
      this.addToOverallFact(all_r.forAll(decl));
    }

    Set<Expr> factsAdded = new HashSet<>();
    factsAdded.add(all);
    factsAdded.add(all_r);
    return factsAdded;
  }

  /**
   * Creates the function filtered and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   * @param func the func
   */
  // fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferOrderPay, x.order]}
  public Set<Expr> createBijectionFilteredToOverallFact(Sig ownerSig, Expr from, Expr to,
      Func func) {

    Set<Expr> factsAdded = new HashSet<>();

    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);

    Expr fromExpr = null;
    Expr toExpr = null;

    boolean justFunction = false;
    if (to == null) {// just x - means no field but to itself
                     // i.e., fact {all x: B | bijectionFiltered[sources, x.transferB1B2, x.b1]} in
                     // 4.1.4 Transfer Parameter2 -Parameter Behavior.als
      justFunction = true;
      toExpr = varX;
    } else {
      // fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p1, x.p1.i]}
      toExpr = addExprVarToExpr(varX, to);
    }
    if (from == null) {// just x - means no field but to itself
      fromExpr = varX;
    } else
      fromExpr = addExprVarToExpr(varX, from);

    Expr fnc_inversefnc_or_bijection = bijectionFiltered.call(func.call(), fromExpr, toExpr);
    if (justFunction) { // to == null so toExpr = varX
      if (func == Alloy.sources) { // {fact {all x: B | isBeforeTarget[x.transferBB1]}
        this.addToOverallFact(isBeforeTarget.call(fromExpr).forAll(decl));
        factsAdded.add(isBeforeTarget.call(fromExpr));
      } else if (func == Alloy.targets) {// fact {all x: B | isAfterSource[x.transferB2B]}
        this.addToOverallFact(isAfterSource.call(fromExpr).forAll(decl));
        factsAdded.add(isAfterSource.call(fromExpr));
      }
    }
    this.addToOverallFact(fnc_inversefnc_or_bijection.forAll(decl));
    factsAdded.add(fnc_inversefnc_or_bijection);

    return factsAdded;
  }



  /**
   * Creates the bijection filtered happens before and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  public void createBijectionFilteredHappensBeforeAndAddToOverallFact(ExprVar varX, Sig ownerSig,
      Expr from, Expr to) {

    Expr bijectionFilteredExpr =
        bijectionFiltered.call(happensBefore.call(), varX.join(from), varX.join(to));
    Decl decl = makeDecl(varX, ownerSig);
    this.addToOverallFact(bijectionFilteredExpr.forAll(decl));
  }

  /**
   * Returns nonZeroDurationOnly and suppressTransfers and suppressIO
   * 
   * @return nonZeroDurationOnly and suppressTransfers and suppressIO
   */
  public Expr getCommonCmdExprs() {
    Func nonZeroDurationOnlyFunction =
        AlloyUtils.getFunction(transferModule, "o/nonZeroDurationOnly");

    Expr nonZeroDurationOnlyFunctionExpression = nonZeroDurationOnlyFunction.call();

    Sig transfer = AlloyUtils.getReachableSig(transferModule, "o/Transfer");
    Expr suppressTransfersExpessionBody = transfer.no();
    Func suppressTransfersFunction =
        new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);

    Expr suppressTransfersExpression = suppressTransfersFunction.call();

    Func inputs = AlloyUtils.getFunction(transferModule, "o/inputs");
    Func outputs = AlloyUtils.getFunction(transferModule, "o/outputs");
    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());

    Func suppressIOFunction = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
    Expr suppressIOExpression = suppressIOFunction.call();

    return nonZeroDurationOnlyFunctionExpression.and(suppressTransfersExpression)
        .and(suppressIOExpression);
  }

  /**
   * Adds the cardinality equal constraint to field.
   *
   * @param ownerSig the owner sig
   * @param field the field
   * @param num the num
   */
  public void addCardinalityEqualConstraintToField(Sig ownerSig, Sig.Field field, int num) {
    ExprVar varX = makeVarX(ownerSig);
    this.addToOverallFact(varX.join(field).cardinality().equal(ExprConstant.makeNUMBER(num))
        .forAll(makeDecl(varX, ownerSig)));
  }

  /**
   * Adds the cardinality greater than equal constraint to field.
   *
   * @param ownerSig the owner sig
   * @param field the field
   * @param num the num
   */
  public void addCardinalityGreaterThanEqualConstraintToField(Sig ownerSig, Sig.Field field,
      int num) {
    ExprVar varX = makeVarX(ownerSig);
    this.addToOverallFact(varX.join(field).cardinality().gte(ExprConstant.makeNUMBER(num))
        .forAll(makeDecl(varX, ownerSig)));
  }

  /**
   * Adds the equal.
   *
   * @param ownerSig the owner sig
   * @param field1 the field 1
   * @param field2 the field 2
   */
  // fact {all x: B1 | x.vin = x.vout}
  public void addEqual(Sig ownerSig, Sig.Field field1, Sig.Field field2) {
    ExprVar varX = makeVarX(ownerSig);
    this.addToOverallFact(
        varX.join(field1).equal(varX.join(field2)).forAll(makeDecl(varX, ownerSig)));
  }

  // fact {all x: B1 | x.vin = x.inputs}
  // change to {all x: B1 | x.vin in x.inputs}
  public void addEqual2(PrimSig ownerSig, List<Sig.Field> sortedFields, Func func) {
    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);
    Expr fieldsExpr = null;
    for (Field field : sortedFields) {
      // sig.domain(field) or sig.parent.domain(sig.parent.field)
      Expr sigDomainField = AlloyUtils.getSigDomainFileld(field.label, ownerSig);
      if (sigDomainField != null)
        fieldsExpr = fieldsExpr == null ? varX.join(sigDomainField)
            : fieldsExpr.plus(varX.join(sigDomainField));
    }

    // .... in x.inputs
    Expr equalExpr = fieldsExpr.in(varX.join(func.call()));
    addToOverallFact(equalExpr.forAll(decl));

    // x.inputs in ....
    Expr equalExprclosure = (varX.join(func.call())).in(fieldsExpr);
    addToOverallFact(equalExprclosure.forAll(decl));
  }

  // fact {all x: OFSingleFoodService | x.prepare.inputs in x.prepare.preparedFoodItem +
  // x.prepare.prepareDestination}
  public void createInOutClosure(PrimSig ownerSig, Field fieldOwner, List<Sig.Field> fieldOfFields,
      Func inOrOut) {
    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);
    Expr fieldsExpr = null;
    for (Field field : fieldOfFields) {
      // sig.domain(field) or sig.parent.domain(sig.parent.field)
      // Expr sigDomainField = AlloyUtils.getSigDomainFileld(field.label, ownerSig);
      // if (sigDomainField != null)
      fieldsExpr = fieldsExpr == null ? varX.join(fieldOwner).join(field)
          : fieldsExpr.plus(varX.join(fieldOwner).join(field));
    }
    // x.inputs in ....
    Expr equalExprclosure = (varX.join(fieldOwner).join(inOrOut.call())).in(fieldsExpr);
    addToOverallFact(equalExprclosure.forAll(decl));

  }

  /**
   * Adds the one constraint to field.
   *
   * @param var the var
   * @param ownerSig the owner sig
   * @param field the field
   */
  public void addOneConstraintToField(ExprVar var, Sig ownerSig, Sig.Field field) {
    Decl decl = makeDecl(var, ownerSig);
    this.addToOverallFact(
        var.join(field).cardinality().equal(ExprConstant.makeNUMBER(1)).forAll(decl));
  }

  // fact {all x: Integer | no steps.x}
  public void noStepsX(Sig sig) {
    ExprVar var = makeVarX(sig);
    Decl decl = makeDecl(var, sig);
    addToOverallFact(osteps.call().join(var).no().forAll(decl));
  }

  // fact {all x: Integer | no x.steps}
  public void noXSteps(Sig sig) {
    ExprVar var = makeVarX(sig);
    Decl decl = makeDecl(var, sig);
    addToOverallFact((var.join(osteps.call()).no()).forAll(decl));
  }



  // all x | no inputs.x or // {all x| no outputs.x}
  public void noInputsOrOutputsX(Sig sig, Func inputsOrOutpus) {
    ExprVar var = makeVarX(sig);
    Decl decl = makeDecl(var, sig);
    addToOverallFact((inputsOrOutpus.call().join(var).no()).forAll(decl));
  }


  // (all x | no x.inputs) // (all x | no x.outputs)
  public void noXInputsOrOutputs(Sig sig, Func inputsOrOutputs) {
    ExprVar var = makeVarX(sig);
    Decl decl = makeDecl(var, sig);
    addToOverallFact((var.join(inputsOrOutputs.call()).no()).forAll(decl));
  }

  // no inputs.x
  // no x.inputs
  // no x.outputs
  // no outputs.x
  public void noInputsXAndXInputsOrOutputsXAndXOutputs(Sig sig, Func inputsOrOutputs) {
    ExprVar var = makeVarX(sig);
    Decl decl = makeDecl(var, sig);
    addToOverallFact((inputsOrOutputs.call().join(var).no()).forAll(decl));
    addToOverallFact((var.join(inputsOrOutputs.call()).no()).forAll(decl));
  }



  public void noItemsX(Sig sig) {
    ExprVar var = makeVarX(sig);
    Decl decl = makeDecl(var, sig);
    addToOverallFact((oitems.call().join(var).no()).forAll(decl)); // no item.x
  }



  public void createNoInputsOrOutputsField(Sig sig, Field field, Func inputsOrOutputs) {
    ExprVar varX = makeVarX(sig);
    Decl declX = makeDecl(varX, sig);// x: MultipleObjectFlow
    Expr exprField = varX.join(sig.domain(field)); // x.p4
    addToOverallFact((exprField.join(inputsOrOutputs.call())/* x.p4.outputs */.no()).forAll(declX));
  }


  // fact {all x: MultipleObjectFlow | all p: x.p2 | p.i = p.inputs}
  // or
  // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}
  public void createEqualFieldToOverallFact(Sig sig, Expr from, Sig.Field fromField, Expr to,
      Sig.Field toField, Func func) {

    // all x: MultipleObjectFlow
    ExprVar varX = makeVarX(sig);
    Decl declX = makeDecl(varX, sig);

    // all p: x.p1
    ExprVar varP = ExprVar.make(null, "p", from.type()); // p
    Expr exprField = varX.join(sig.domain(fromField)); // x.p1
    Decl declY = new Decl(null, null, null, List.of(varP), exprField);

    // p.i = p.outputs (func = outputs) to p.i in p.outputs
    Expr equalExpr = varP.join(toField).in(varP.join(func.call()));
    // and p.outputs in p.i
    Expr equqlExpr2 = varP.join(func.call()).in(varP.join(toField));

    // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}
    // to
    // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i in p.outputs}
    // fact {all x: MultipleObjectFlow | all p: x.p1 | p.outpus in p.i}
    addToOverallFact(equalExpr.forAll(declY).forAll(declX));
    addToOverallFact(equqlExpr2.forAll(declY).forAll(declX));
  }



  /**
   * Add expression like ... fact {all x: SimpleSequence | no y: Transfer | y in x.steps}
   * 
   * @param sig
   */
  public void noTransferStep(Sig sig) {
    ExprVar varX = makeVarX(sig);
    Decl declX = makeDecl(varX, sig);

    ExprVar varY = ExprVar.make(null, "y", transferSig.type());
    Decl declY = new Decl(null, null, null, List.of(varY), transferSig.oneOf());

    Expr ostepsExpr1 = osteps.call();
    addToOverallFact((varY).in(varX.join(ostepsExpr1)). /* y in x.steps */forNo(declY)
        ./* no y: Transfer */ forAll(declX))/* all x: SimpleSequence */;

  }

  /**
   * Adds the steps.
   *
   * @param sig the sig
   * @param stepFields the step fields
   */
  public void addSteps(PrimSig sig, Set<String> stepFields, boolean addInXSteps,
      boolean addXStepsIn) {
    ExprVar varX = makeVarX(sig);
    Decl decl = makeDecl(varX, sig);
    Expr ostepsExpr1 = osteps.call();

    List<String> sortedFieldLabel = new ArrayList<>();
    for (String stepField : stepFields)
      sortedFieldLabel.add(stepField);
    Collections.sort(sortedFieldLabel);


    if (addXStepsIn) {// to only leaf sig - all fields including inherited/redefined
      Expr expr = null;
      for (String fieldName : sortedFieldLabel) {
        // sig.domain(field) or sig.parent.domain(sig.parent.field)
        Expr sigDomainField = AlloyUtils.getSigDomainFileld(fieldName, sig); // including inherited
                                                                             // fields
        if (sigDomainField != null)
          expr = expr == null ? varX.join(sigDomainField) : expr.plus(varX.join(sigDomainField));
      }
      if (expr != null)
        addToOverallFact(varX.join(ostepsExpr1).in(expr).forAll(decl)); // x.steps in .....
    }

    if (addInXSteps) {// for all sigs - own fields - not include redefined
      Expr expr = null;
      for (String fieldName : sortedFieldLabel) {
        Expr sigDomainField = AlloyUtils.getSigOwnField(fieldName, sig);
        if (sigDomainField != null) {
          expr = expr == null ? varX.join(sigDomainField) : expr.plus(varX.join(sigDomainField));
        }
      }
      if (expr != null)
        addToOverallFact((expr).in(varX.join(ostepsExpr1)).forAll(decl)); // .... in x.steps
    }
  }

  // fact {all x: OFFoodService | x.eat in OFEat }
  // sig = OFFoodService, field eat, typeSig = OFEat
  public void addRedefinedSubsettingAsFact(Sig sig, Field field, Sig typeSig) {
    ExprVar varX = makeVarX(sig);
    Decl decl = makeDecl(varX, sig);
    addToOverallFact(varX.join(field).in(typeSig).forAll(decl));
  }

  private static ExprVar makeVarX(Sig sig) {
    return ExprVar.make(null, "x", sig.type());
  }

  private static Decl makeDecl(ExprVar x, Sig sig) {
    return new Decl(null, null, null, List.of(x), sig.oneOf());
  }
}


