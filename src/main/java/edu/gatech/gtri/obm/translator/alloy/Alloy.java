package edu.gatech.gtri.obm.translator.alloy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
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

    // // constraints
    // Func nonZeroDurationOnlyFunction = Helper.getFunction(transferModule,
    // "o/nonZeroDurationOnly");
    // Expr nonZeroDurationOnlyFunctionExpression = nonZeroDurationOnlyFunction.call();
    //
    // Sig transfer = Helper.getReachableSig(transferModule, "o/Transfer");
    // Expr suppressTransfersExpessionBody = transfer.no();
    // Func suppressTransfersFunction =
    // new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
    // Expr suppressTransfersExpression = suppressTransfersFunction.call();
    //
    // Func inputs = Helper.getFunction(transferModule, "o/inputs");
    // Func outputs = Helper.getFunction(transferModule, "o/outputs");
    // Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
    // Func suppressIOFunction = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
    // Expr suppressIOExpression = suppressIOFunction.call();
    //
    //
    // _nameExpr = nonZeroDurationOnlyFunctionExpression.and(suppressTransfersExpression)
    // .and(suppressIOExpression);
  }

  // public Sig getTransferSig() {
  // Sig transfer = AlloyUtils.getReachableSig(transferModule, "o/Transfer");
  // return transfer;
  // }
  //
  // public Sig getTransferBeforeSig() {
  // Sig transfer = AlloyUtils.getReachableSig(transferModule, "o/TransferBefore");
  // return transfer;
  // }

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
    // Sig s = new PrimSig("this/" + label, parent);
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
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Expr inverseFunctionFilteredExpr = inverseFunctionFiltered.call(happensBefore.call(),
        addExprVarToExpr(s, from), addExprVarToExpr(s, to));

    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
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

    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Expr _from = s.join(from[0]), _to = s.join(to[0]);

    for (int i = 1; i < from.length; i++) {
      _from = _from.plus(s.join(from[i]));
    }
    for (int i = 1; i < to.length; i++) {
      _to = _to.plus(s.join(to[i]));
    }

    Expr inverseFunctionFilteredExpression =
        inverseFunctionFiltered.call(happensBefore.call(), _from, _to);

    List<ExprHasName> names = List.of(s);
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
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
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());

    Expr funcFilteredExpr = functionFiltered.call(happensBefore.call(), addExprVarToExpr(s, from),
        addExprVarToExpr(s, to));
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
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

  // public void createBijectionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
  // Expr to) {
  // ExprVar s = ExprVar.make(null, "x", ownerSig.type());
  //
  // Expr bijectionFilteredExpr = bijectionFiltered.call(happensBefore.call(),
  // addExprVarToExpr(s, from), addExprVarToExpr(s, to));
  //
  //
  // List<ExprHasName> names = new ArrayList<>(List.of(s));
  // Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
  // this.addToOverallFact(bijectionFilteredExpr.forAll(decl));
  // }

  // transfer = x.tarnsferSupplierCustomer
  public void createSubSettingItemRuleOverallFact(Sig ownerSig, Expr transfer) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Decl decl = new Decl(null, null, null, List.of(s), ownerSig.oneOf());
    this.addToOverallFact(subsettingItemRuleForSources.call(s.join(transfer)).forAll(decl));
    this.addToOverallFact(subsettingItemRuleForTargets.call(s.join(transfer)).forAll(decl));
  }

  public void createIsAfterSourceIsBeforeTargetOverallFact(Sig ownerSig, Expr transfer) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Decl decl = new Decl(null, null, null, List.of(s), ownerSig.oneOf());
    this.addToOverallFact(isAfterSource.call(s.join(transfer)).forAll(decl));
    this.addToOverallFact(isBeforeTarget.call(s.join(transfer)).forAll(decl));
  }

  // private void createIsAfterSourceOverallFact(Sig ownerSig, Expr transfer) {
  // ExprVar s = ExprVar.make(null, "x", ownerSig.type());
  // Decl decl = new Decl(null, null, null, List.of(s), ownerSig.oneOf());
  // this.addToOverallFact(isAfterSource.call(s.join(transfer)).forAll(decl));
  // }
  //
  // private void createIsBeforeTargetOverallFact(Sig ownerSig, Expr transfer) {
  // ExprVar s = ExprVar.make(null, "x", ownerSig.type());
  // Decl decl = new Decl(null, null, null, List.of(s), ownerSig.oneOf());
  // this.addToOverallFact(isBeforeTarget.call(s.join(transfer)).forAll(decl));
  // }


  public void createInverseFunctionFilteredAndAddToOverallFact(Sig ownerSig, Expr from, Expr to,
      Func func) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());

    Expr funcFilteredExpr = inverseFunctionFiltered.call(func.call(), addExprVarToExpr(s, from),
        addExprVarToExpr(s, to));
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(funcFilteredExpr.forAll(decl));
  }

  /* if from or to is null, use ExprVar x */
  public void createFunctionFilteredAndAddToOverallFact(Sig ownerSig, Expr from, Expr to,
      Func func) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());

    to = to == null ? s : to;
    from = from == null ? s : from;

    Expr funcFilteredExpr =
        functionFiltered.call(func.call(), addExprVarToExpr(s, from), addExprVarToExpr(s, to));
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(funcFilteredExpr.forAll(decl));
  }

  /* if from or to is null, use ExprVar x */
  public void createBijectionFilteredToOverallFact(Sig ownerSig, Expr from, Expr to, Func func) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Decl decl = new Decl(null, null, null, List.of(s), ownerSig.oneOf());
    Expr fromExpr = null;
    Expr toExpr = null;

    boolean justFunction = false;
    boolean justInverseFunction = false;
    if (to == null) {// just x - means no field but to itself
                     // i.e., fact {all x: B | bijectionFiltered[sources, x.transferB1B2, x.b1]} in
                     // 4.1.4 Transfer Parameter2 -Parameter Behavior.als
      justFunction = true;
      toExpr = s;
    } else
      toExpr = addExprVarToExpr(s, to);
    if (from == null) {// just x - means no field but to itself
      justInverseFunction = true;
      fromExpr = s;
    } else
      fromExpr = addExprVarToExpr(s, from);

    Expr funcCall = func.call();
    Expr fnc_inversefnc_or_bijection = null;
    if (!justFunction && !justInverseFunction) {
      fnc_inversefnc_or_bijection = bijectionFiltered.call(funcCall, fromExpr, toExpr);
    } else if (justFunction) {
      // fnc_inversefnc_or_bijection = functionFiltered.call(funcCall, fromExpr, toExpr);
      fnc_inversefnc_or_bijection = bijectionFiltered.call(funcCall, fromExpr, toExpr);
      if (func == Alloy.sources) { // {fact {all x: B | isBeforeTarget[x.transferBB1]}
        this.addToOverallFact(isBeforeTarget.call(fromExpr).forAll(decl));
      } else if (func == Alloy.targets) {// fact {all x: B | isAfterSource[x.transferB2B]}
        this.addToOverallFact(isAfterSource.call(fromExpr).forAll(decl));
      }
    }
    // else if (justInverseFunction) {
    // fnc_inversefnc_or_bijection = inverseFunctionFiltered.call(funcCall, fromExpr, toExpr);
    // }
    this.addToOverallFact(fnc_inversefnc_or_bijection.forAll(decl));
  }



  /**
   * used by manual test Example: all s: FoodService | bijectionFiltered[happensBefore, s.order,
   * s.serve]
   * 
   * @param ownerSig = FoodService
   * @param var = s
   * @param from = order
   * @param to = serve
   */
  public void createBijectionFilteredHappensBeforeAndAddToOverallFact(ExprVar var, Sig ownerSig,
      Expr from, Expr to) {

    Expr bijectionFilteredExpr =
        bijectionFiltered.call(happensBefore.call(), var.join(from), var.join(to));

    Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
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

  public void addToNameExpr(Expr expr) {
    _nameExpr = _nameExpr.and(expr);
  }

  public void addCardinalityEqualConstraintToField(Sig ownerSig, Sig.Field field, int num) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(
        s.join(field).cardinality().equal(ExprConstant.makeNUMBER(num)).forAll(decl));
  }

  public void addCardinalityGreaterThanEqualConstraintToField(Sig ownerSig, Sig.Field field,
      int num) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(
        s.join(field).cardinality().gte(ExprConstant.makeNUMBER(num)).forAll(decl));
  }

  // fact {all x: B1 | x.vin=x.vout}
  public void addEqual(Sig ownerSig, Sig.Field field1, Sig.Field field2) {
    ExprVar x = ExprVar.make(null, "x", ownerSig.type());
    List<ExprHasName> names = new ArrayList<>(List.of(x));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(x.join(field1).equal(x.join(field2)).forAll(decl));
  }

  // fact {all x: B1 | x.vin = x.inputs}
  public void addEqual2(Sig ownerSig, Sig.Field field1, Func func) {
    ExprVar varX = ExprVar.make(null, "x", ownerSig.type());
    List<ExprHasName> names = new ArrayList<>(List.of(varX));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    Expr equalExpr = varX.join(field1).equal(varX.join(func.call()));
    addToOverallFact(equalExpr.forAll(decl));
  }

  public void addOneConstraintToField(ExprVar var, Sig ownerSig, Sig.Field field) {
    Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
    this.addToOverallFact(
        var.join(field).cardinality().equal(ExprConstant.makeNUMBER(1)).forAll(decl));
  }

  // fact {all x: Integer | no steps.x}
  public void noStepsX(Sig sig) {
    ExprVar varX = ExprVar.make(null, "x", sig.type());
    Decl declX = new Decl(null, null, null, List.of(varX), sig.oneOf());
    addToOverallFact(osteps.call().join(varX).no().forAll(declX));
  }

  // fact {all x: Integer | no x.steps}
  public void noXSteps(Sig sig) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
    addToOverallFact((var.join(osteps.call()).no()).forAll(decl));
  }

  // (all x | no x.inputs)
  public void noXInputs(Sig sig) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
    addToOverallFact((var.join(oinputs.call()).no()).forAll(decl));
  }

  // all x | no inputs.x
  public void noInputsX(Sig sig) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
    addToOverallFact((oinputs.call().join(var).no()).forAll(decl));
  }

  // no inputs.x
  // no x.inputs
  public void noInputsXAndXInputs(Sig sig) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
    addToOverallFact((oinputs.call().join(var).no()).forAll(decl));
    addToOverallFact((var.join(oinputs.call()).no()).forAll(decl));
  }



  // (all x | no x.outputs)
  public void noXOutputs(Sig sig) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
    addToOverallFact((var.join(ooutputs.call()).no()).forAll(decl));
  }

  // {all x| no outputs.x}
  public void noOutputsX(Sig sig) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
    addToOverallFact((ooutputs.call().join(var).no()).forAll(decl));
  }


  // // (all x | no outputs.x)
  // public void noOutputs(Sig sig) {
  // ExprVar var = ExprVar.make(null, "x", sig.type());
  // Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
  // addToOverallFact((ooutputs.call().join(var).no()).forAll(decl));
  // }

  public void noItemsX(Sig sig) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
    addToOverallFact((oitems.call().join(var).no()).forAll(decl)); // no item.x
  }

  // no x.inputs
  // no inputs.x
  // no items.x
  // public void noInputsBothAndItem(Sig sig) {
  // ExprVar var = ExprVar.make(null, "x", sig.type());
  // Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
  // addToOverallFact((var.join(oinputs.call()).no()).forAll(decl)); // no x.inputs
  // addToOverallFact((oinputs.call().join(var).no()).forAll(decl)); // no inputs.x
  // addToOverallFact((oitems.call().join(var).no()).forAll(decl)); // no item.x
  // }

  // no x.outputs
  // no outputs.x
  public void noOutputsXAndXOutputs(Sig sig) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
    addToOverallFact((var.join(ooutputs.call()).no()).forAll(decl));
    addToOverallFact((ooutputs.call().join(var).no()).forAll(decl));
  }

  // no x.outputs
  // no outputs.x
  // no items.x
  // public void noOutputsBothAndItem(Sig sig) {
  // ExprVar var = ExprVar.make(null, "x", sig.type());
  // Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
  // addToOverallFact((var.join(ooutputs.call()).no()).forAll(decl)); // no x.outputs
  // addToOverallFact((ooutputs.call().join(var).no()).forAll(decl)); // no outputs.x
  // addToOverallFact((oitems.call().join(var).no()).forAll(decl)); // no items.x
  // }



  // public void addInputs(ExprVar var, Sig ownerSig, Field field) {
  // Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
  // Expr expr = var.join(ownerSig.domain(field));
  // if (expr != null) {
  // // addToOverallFact((expr).in(var.join(oinputsExpr1)).forAll(decl));
  // // addToOverallFact(var.join(oinputsExpr2).in(expr).forAll(decl));
  // // fact {all x: BehaviorWithParamterInOut | x.input in x.inputs}
  // // fact {all x: BehaviorWithParamterInOut | x.inputs in x.input}
  // addBothToOverallFact(var, expr, oinputs.call(), decl);
  // }
  // }
  public void addInputsAndNoInputsX(ExprVar var, Sig ownerSig, Field field, boolean addNoInputsX,
      boolean addEqual) {
    // Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
    // Expr expr = var.join(ownerSig.domain(field));
    // if (expr != null) {
    // addToOverallFact((expr).in(var.join(oinputsExpr1)).forAll(decl));
    // addToOverallFact(var.join(oinputsExpr2).in(expr).forAll(decl));
    // fact {all x: BehaviorWithParamterInOut | x.input in x.inputs}
    // fact {all x: BehaviorWithParamterInOut | x.inputs in x.input}
    // addBothToOverallFact(var, expr, oinputs.call(), decl);



    // change to //fact {all x: B1 | bijectionFiltered[inputs, x, x.vin]}
    // & fact {all x: B1 | x.vin = x.inputs}
    // createBijectionFilteredAddToOverallFact2(ownerSig, field, Alloy.oinputs);


    // fact {all x: B | x.vout = x.inputs}
    if (addEqual) {
      addEqual2(ownerSig, field, oinputs);
    }

    // TODO combine later
    if (addNoInputsX)
      noInputsX(ownerSig);

    // }

  }

  private void createBijectionFilteredAddToOverallFact2(Sig ownerSig, Expr inOrOut, Func func) {
    ExprVar varX = ExprVar.make(null, "x", ownerSig.type());
    inOrOut = varX.join(inOrOut);
    Expr bijectionFilteredExpr = bijectionFiltered.call(func.call(), varX, inOrOut);
    Decl decl = new Decl(null, null, null, List.of(varX), ownerSig.oneOf());
    this.addToOverallFact(bijectionFilteredExpr.forAll(decl));
  }


  public void addOutputsAndNoOutputsX(ExprVar var, Sig ownerSig, Field field, boolean addNoOutputsX,
      boolean addEqual) {
    // Expr ooutExpr1 = ooutputs.call();
    // Expr oinputsExpr2 = ooutputs.call();

    // Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
    // Expr expr = var.join(ownerSig.domain(field));
    // if (expr != null) {
    // addToOverallFact((expr).in(var.join(oinputsExpr1)).forAll(decl));
    // addToOverallFact(var.join(oinputsExpr2).in(expr).forAll(decl));
    // fact {all x: BehaviorWithParamterInOut | x.output in x.outputs}
    // fact {all x: BehaviorWithParamterInOut | x.outputs in x.output}
    // addBothToOverallFact(var, expr, ooutputs.call(), decl);

    // change to //fact {all x: B1 | bijectionFiltered[outputs, x, x.out]}
    // & fact {all x: B1 | x.vout = x.outputs}

    ///
    // createBijectionFilteredAddToOverallFact2(ownerSig, field, Alloy.ooutputs);
    if (addEqual)
      addEqual2(ownerSig, field, ooutputs);



    // TODO combine later
    if (addNoOutputsX)
      noOutputsX(ownerSig);
    // }
  }
  // public void addOutputs(ExprVar var, Sig ownerSig, Field field) {
  // // Expr ooutExpr1 = ooutputs.call();
  // // Expr oinputsExpr2 = ooutputs.call();
  //
  // Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
  // Expr expr = var.join(ownerSig.domain(field));
  // if (expr != null) {
  // // addToOverallFact((expr).in(var.join(oinputsExpr1)).forAll(decl));
  // // addToOverallFact(var.join(oinputsExpr2).in(expr).forAll(decl));
  // // fact {all x: BehaviorWithParamterInOut | x.output in x.outputs}
  // // fact {all x: BehaviorWithParamterInOut | x.outputs in x.output}
  // addBothToOverallFact(var, expr, ooutputs.call(), decl);
  // }
  // }



  private void addBothToOverallFact(ExprVar var, Expr expr, Expr varJoinExpr, Decl decl) {
    addToOverallFact((expr).in(var.join(varJoinExpr)).forAll(decl));
    addToOverallFact(var.join(varJoinExpr).in(expr).forAll(decl));
  }


  // fact {all x: MultipleObjectFlow | all p: x.p2 | p.i = p.inputs}
  public void createEqualFieldInputsToOverallFact(Sig sig, Expr from, Sig.Field fromField, Expr to,
      Sig.Field toField) {
    createEqualFieldToOverallFact(sig, from, fromField, to, toField, oinputs);
  }

  // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}
  public void createEqualFieldOutputsToOverallFact(Sig sig, Expr from, Sig.Field fromField, Expr to,
      Sig.Field toField) {
    createEqualFieldToOverallFact(sig, from, fromField, to, toField, ooutputs);
  }

  // fact {all x: MultipleObjectFlow | no x.p1.inputs}
  public void createNoInputsField(Sig sig, Field field) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl declX = new Decl(null, null, null, List.of(var), sig.oneOf());// x: MultipleObjectFlow
    Expr exprField = var.join(sig.domain(field)); // x.p1
    addToOverallFact((exprField.join(oinputs.call())/* x.p1.inputs */.no()).forAll(declX));
  }

  // fact {all x: MultipleObjectFlow | no x.p4.outputs}
  public void createNoOutputsField(Sig sig, Field field) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl declX = new Decl(null, null, null, List.of(var), sig.oneOf());// x: MultipleObjectFlow
    Expr exprField = var.join(sig.domain(field)); // x.p4
    addToOverallFact((exprField.join(ooutputs.call())/* x.p4.outputs */.no()).forAll(declX));
  }

  // fact {all x: MultipleObjectFlow | all p: x.p2 | p.i = p.inputs}
  // or
  // fact {all x: MultipleObjectFlow | all p: x.p1 | p.i = p.outputs}
  private void createEqualFieldToOverallFact(Sig sig, Expr from, Sig.Field fromField, Expr to,
      Sig.Field toField, Func func) {

    // all x: MultipleObjectFlow
    ExprVar varX = ExprVar.make(null, "x", sig.type());
    Decl declX = new Decl(null, null, null, List.of(varX), sig.oneOf());

    // all p: x.p1
    ExprVar varP = ExprVar.make(null, "p", from.type()); // p
    Expr exprField = varX.join(sig.domain(fromField)); // x.p1
    Decl declY = new Decl(null, null, null, List.of(varP), exprField);

    // p.i = p.outputs (func = outputs)
    Expr equalExpr = varP.join(toField).equal(varP.join(func.call()));

    addToOverallFact(equalExpr.forAll(declY).forAll(declX));
  }



  /**
   * Add expression like ... fact {all x: SimpleSequence | no y: Transfer | y in x.steps}
   * 
   * @param sig
   */
  public void noTransferStep(Sig sig) {
    ExprVar varX = ExprVar.make(null, "x", sig.type());
    Decl declX = new Decl(null, null, null, List.of(varX), sig.oneOf());

    ExprVar varY = ExprVar.make(null, "y", transferSig.type());
    Decl declY = new Decl(null, null, null, List.of(varY), transferSig.oneOf());

    Expr ostepsExpr1 = osteps.call();
    addToOverallFact((varY).in(varX.join(ostepsExpr1)). /* y in x.steps */forNo(declY)
        ./* no y: Transfer */ forAll(declX))/* all x: SimpleSequence */;

  }

  public void addSteps(Sig sig, Set<String> stepFields) {
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Expr ostepsExpr1 = osteps.call();
    Decl decl = new Decl(null, null, null, List.of(s), sig.oneOf());

    List<String> sortedFieldLabel = new ArrayList<>();
    for (String stepField : stepFields)
      sortedFieldLabel.add(stepField);
    Collections.sort(sortedFieldLabel);

    Expr expr = null;
    for (String fieldName : sortedFieldLabel) {
      for (Field field : sig.getFields()) {
        if (field.label.equals(fieldName)) {
          expr = expr == null ? s.join(sig.domain(field)) : expr.plus(s.join(sig.domain(field)));
          break;
        }
      }
    }
    if (expr != null) {
      addBothToOverallFact(s, expr, ostepsExpr1, decl);
    }
  }



  public Command createCommand(String label, int __overall) {
    Pos _pos = null;
    String _label = label;
    boolean _check = false;
    int _overall = __overall;
    int _bitwidth = -1;
    int _maxseq = -1;
    int _expects = -1;
    Iterable<CommandScope> _scope = Arrays.asList(new CommandScope[] {});
    Iterable<Sig> _additionalExactSig = Arrays.asList(new Sig[] {});
    Expr _formula = getOverAllFact(); // _nameExpr.and(getOverAllFact());
    Command _parent = null;

    // ========== Define command ==========

    Command command = new Command(_pos, _nameExpr, _label, _check, _overall, _bitwidth, _maxseq,
        _expects, _scope, _additionalExactSig, _formula, _parent);
    return command;
  }

  /*
   * private Map<Sig, List<Field>> toFieldsByFieldType(Map<Field, Sig> fieldByFieldType) { Map<Sig,
   * List<Field>> fieldsByFieldType = new HashMap<>(); for (Sig.Field field :
   * fieldByFieldType.keySet()) { Sig type = fieldByFieldType.get(field); List<Field> fields = null;
   * if (fieldsByFieldType.containsKey(type)) { fields = fieldsByFieldType.get(type); } else {
   * fields = new ArrayList<>(); fieldsByFieldType.put(type, fields); } fields.add(field); } return
   * fieldsByFieldType;
   * 
   * }
   * 
   * 
   * public void addConstraint(Sig ownerSig, Map<Field, Sig> fieldByFieldType) {
   * 
   * Map<Sig, List<Field>> fieldsByFieldType = toFieldsByFieldType(fieldByFieldType); Expr
   * duringExampleExpressions = null; for (Sig type : fieldsByFieldType.keySet()) { String
   * labelPrefix = ""; Expr body = null; for (Field field : fieldsByFieldType.get(type)) { if
   * (field.sig == ownerSig) { body = body == null ? ownerSig.join(field) :
   * body.plus(ownerSig.join(field)); labelPrefix += field.label; } } if (body != null) { Expr
   * pDuringExampleBody = type.in(body); String label = labelPrefix + "DuringExample"; //
   * p1DuringExample Pos pos = null; List<Decl> decls = new ArrayList<>(); Expr returnDecl = null;
   * Func duringExamplePredicate = new Func(pos, label, decls, returnDecl, pDuringExampleBody); Expr
   * duringExampleExpression = duringExamplePredicate.call(); duringExampleExpressions =
   * duringExampleExpressions == null ? duringExampleExpression :
   * duringExampleExpressions.and(duringExampleExpression); } } if (duringExampleExpressions !=
   * null) { Func instancesDuringExamplePredicate = new Func(null, "instancesDuringExample", new
   * ArrayList<>(), null, duringExampleExpressions); Expr instancesDuringExampleExpression =
   * instancesDuringExamplePredicate.call(); addToNameExpr(instancesDuringExampleExpression); }
   * 
   * }
   * 
   * public void addOnlyConstraint(Sig sig) { Func onlySimpleSequencePredicate = new Func(null,
   * "only" + sig.label, new ArrayList<>(), null,
   * sig.cardinality().equal(ExprConstant.makeNUMBER(1))); Expr onlySimpleSequenceExpression =
   * onlySimpleSequencePredicate.call(); addToNameExpr(onlySimpleSequenceExpression); }
   * 
   * public void addConstraintzz(Sig ownerSig, Map<String, Field> fieldByName, Map<String, Sig>
   * fieldTypeByFieldName) {
   * 
   * // During Pos pos = null; Expr duringExampleExpressions = null; String label =
   * "pDuringExample"; // p1DuringExample Expr body = null; String commonFieldName = ""; for (String
   * fieldName : fieldByName.keySet()) { commonFieldName = fieldName; Sig.Field field =
   * fieldByName.get(fieldName); body = body == null ? ownerSig.join(field) :
   * body.plus(ownerSig.join(field)); } // assuming all fieldTypeByFieldName.get(fieldName) are the
   * same Expr pDuringExampleBody = fieldTypeByFieldName.get(commonFieldName).in(body); List<Decl>
   * decls = new ArrayList<>(); Expr returnDecl = null; Func duringExamplePredicate = new Func(pos,
   * label, decls, returnDecl, pDuringExampleBody); Expr duringExampleExpression =
   * duringExamplePredicate.call(); duringExampleExpressions = duringExampleExpressions == null ?
   * duringExampleExpression : duringExampleExpressions.and(duringExampleExpression);
   * 
   * 
   * 
   * Func instancesDuringExamplePredicate = new Func(null, "instancesDuringExample", new
   * ArrayList<>(), null, duringExampleExpressions); Expr instancesDuringExampleExpression =
   * instancesDuringExamplePredicate.call();
   * 
   * Func onlySimpleSequencePredicate = new Func(null, "only" + ownerSig.label, new ArrayList<>(),
   * null, ownerSig.cardinality().equal(ExprConstant.makeNUMBER(1))); Expr
   * onlySimpleSequenceExpression = onlySimpleSequencePredicate.call();
   * 
   * addToNameExpr(instancesDuringExampleExpression); addToNameExpr(onlySimpleSequenceExpression); }
   */


  /*
   * public Command createRunCommand(String label, int overall) { Pos pos = null; boolean check =
   * false; int bitwidth = -1; int maxseq = -1; int expects = -1; Iterable<CommandScope> scope =
   * Arrays.asList(new CommandScope[] {}); Iterable<Sig> additionalExactSig = Arrays.asList(new
   * Sig[] {}); Expr formula = _nameExpr.and(getOverAllFact()); Command parent = null; return new
   * Command(pos, _nameExpr, label, check, overall, bitwidth, maxseq, expects, scope,
   * additionalExactSig, formula, parent); }
   */


}


