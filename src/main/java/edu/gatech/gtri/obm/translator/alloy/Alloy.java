package edu.gatech.gtri.obm.translator.alloy;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: Auto-generated Javadoc
/** The Class Alloy. */
public class Alloy {

  /** The occ sig. */
  protected static PrimSig occSig;

  /** The template module. */
  protected static Module templateModule;

  /** The transfer module. */
  public static Module transferModule;

  /** The ignored sigs. */
  protected static Set<Sig> ignoredSigs;

  /** The ignored exprs. */
  protected static Set<Expr> ignoredExprs;

  /** The ignored funcs. */
  protected static Set<Func> ignoredFuncs;

  /** The happens before. */
  public static Func happensBefore;

  /** The happens during. */
  public static Func happensDuring;

  /** The sources. */
  public static Func sources;

  /** The targets. */
  public static Func targets;

  /** The subsetting item rule for sources. */
  public static Func subsettingItemRuleForSources;

  /** The subsetting item rule for targets. */
  public static Func subsettingItemRuleForTargets;

  /** The is after source. */
  public static Func isAfterSource;

  /** The is before target. */
  public static Func isBeforeTarget;

  /** The bijection filtered. */
  protected static Func bijectionFiltered;

  /** The function filtered. */
  protected static Func functionFiltered;

  /** The inverse function filtered. */
  protected static Func inverseFunctionFiltered;

  /** The osteps. */
  protected static Func osteps;

  /** The oinputs. */
  protected static Func oinputs;

  /** The ooutputs. */
  protected static Func ooutputs;

  /** The unique fact. */
  protected ExprList uniqueFact;

  /** The template fact. */
  protected Expr templateFact;

  /** The overall fact. */
  protected Expr overallFact;

  /** The all sigs. */
  protected List<Sig> allSigs;

  /** The name expr. */
  protected Expr _nameExpr;

  /** The transfer sig. */
  public static Sig transferSig;

  /** The transfer before sig. */
  public static Sig transferBeforeSig;

  /** The Constant templateString. */
  protected static final String templateString =
      "open Transfer[Occurrence] as o \n" + "abstract sig Occurrence{}";

  /**
   * Instantiates a new alloy.
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

  /**
   * Gets the transfer module.
   *
   * @return the transfer module
   */
  public Module getTransferModule() {
    return Alloy.transferModule;
  }

  /**
   * Gets the template module.
   *
   * @return the template module
   */
  public Module getTemplateModule() {
    return Alloy.templateModule;
  }

  /**
   * Gets the occ sig.
   *
   * @return the occ sig
   */
  public PrimSig getOccSig() {
    return Alloy.occSig;
  }

  /**
   * Gets the over all fact.
   *
   * @return the over all fact
   */
  public Expr getOverAllFact() {
    if (overallFact == null) {
      return templateModule.getAllReachableFacts();
    }

    return this.overallFact.and(templateModule.getAllReachableFacts());
  }

  /**
   * Gets the all sigs.
   *
   * @return the all sigs
   */
  public List<Sig> getAllSigs() {
    return this.allSigs;
  }

  /**
   * Creates the sig and add to all sigs.
   *
   * @param label the label
   * @param parent the parent
   * @return the prim sig
   */
  public PrimSig createSigAndAddToAllSigs(String label, PrimSig parent) {
    // Sig s = new PrimSig("this/" + label, parent);
    PrimSig s = new PrimSig(label, parent);
    allSigs.add(s);
    return s;
  }

  /**
   * Creates the sig as child of occ sig and add to all sigs.
   *
   * @param label the label
   * @return the prim sig
   */
  public PrimSig createSigAsChildOfOccSigAndAddToAllSigs(String label) {
    return createSigAndAddToAllSigs(label, Alloy.occSig);
  }

  /**
   * Creates the sig as child of parent sig add to all sigs.
   *
   * @param label the label
   * @param parentSig the parent sig
   * @return the prim sig
   */
  public PrimSig createSigAsChildOfParentSigAddToAllSigs(String label, PrimSig parentSig) {
    return createSigAndAddToAllSigs(label, parentSig);
  }

  /**
   * Adds the to overall fact.
   *
   * @param expr the expr
   */
  public void addToOverallFact(Expr expr) {
    if (overallFact == null) {
      overallFact = expr;
    } else {
      overallFact = overallFact.and(expr);
    }
  }

  /**
   * Gets the ignored sigs.
   *
   * @return the ignored sigs
   */
  public Set<Sig> getIgnoredSigs() {
    return ignoredSigs;
  }

  /**
   * Gets the ignored exprs.
   *
   * @return the ignored exprs
   */
  public Set<Expr> getIgnoredExprs() {
    return ignoredExprs;
  }

  /**
   * Gets the ignored funcs.
   *
   * @return the ignored funcs
   */
  public Set<Func> getIgnoredFuncs() {
    return ignoredFuncs;
  }

  /**
   * support when Expr original is ExprBinary(ie., p1 + p2) to add ExprVar s in both so returns s.p1
   * and s.p2. if original is like "BuffetService <: (FoodService <: eat)" -> ((ExprBinary)
   * original).op = "<:", in this case just return s.join(original) =
   *
   * @param s the s
   * @param original the original
   * @return the expr
   */
  private Expr addExprVarToExpr(ExprVar s, Expr original) {
    if (original instanceof ExprBinary) {
      Expr left = addExprVarToExpr(s, ((ExprBinary) original).left);
      Expr right = addExprVarToExpr(s, ((ExprBinary) original).right);
      if (((ExprBinary) original).op == ExprBinary.Op.PLUS) return left.plus(right);
      else return s.join(original); // x . BuffetService <: (FoodService <: eat) where original =
      // "BuffetService <: (FoodService <: eat)" with ((ExprBinary)
      // original).op = "<:"
    } else {
      return s.join(original); // x.BuffetService
    }
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
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Expr inverseFunctionFilteredExpr =
        inverseFunctionFiltered.call(
            happensBefore.call(), addExprVarToExpr(s, from), addExprVarToExpr(s, to));

    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(inverseFunctionFilteredExpr.forAll(decl));
  }

  /**
   * Creates a inverseFunctionFiltered fact with happensBefore. Use when "from" or "to" has a +
   * sign. fact f3 {all s: Loop | functionFiltered[happensBefore, s.p2, s.p2 + s.p3]} ownerSig=Loop;
   * from={p2}; to={p2, p3}
   *
   * <p>In this example, "to" has a + sign.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  public void createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(
      Sig ownerSig, Expr[] from, Expr[] to) {

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
   * <p>This function doesn't handle the case where "from" or "to" has a plus sign in it. Example
   * Alloy fact this function can't create: fact f3 {all s: Loop | functionFiltered[happensBefore,
   * s.p2, s.p2 + s.p3]}
   *
   * <p>I wrote another function below to handle this case with + sign.
   * createFunctionFilteredHappensBeforeAndAllToOverAllFact( Sig ownerSig, Expr[] from, Expr[] to)
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  public void createFunctionFilteredHappensBeforeAndAddToOverallFact(
      Sig ownerSig, Expr from, Expr to) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());

    Expr funcFilteredExpr =
        functionFiltered.call(
            happensBefore.call(), addExprVarToExpr(s, from), addExprVarToExpr(s, to));
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(funcFilteredExpr.forAll(decl));
  }

  /**
   * Creates a functionFiltered fact with happensBefore. Use when "from" or "to" has a + sign. fact
   * f3 {all s: Loop | functionFiltered[happensBefore, s.p2, s.p2 + s.p3]} ownerSig=Loop; from={p2};
   * to={p2, p3}
   *
   * <p>In this example, "to" has a + sign.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  public void createFunctionFilteredHappensBeforeAndAddToOverallFact(
      Sig ownerSig, Expr[] from, Expr[] to) {

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
   * Creates the bijection filtered happens before and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   */
  public void createBijectionFilteredHappensBeforeAndAddToOverallFact(
      Sig ownerSig, Expr from, Expr to) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());

    Expr bijectionFilteredExpr =
        bijectionFiltered.call(
            happensBefore.call(), addExprVarToExpr(s, from), addExprVarToExpr(s, to));

    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(bijectionFilteredExpr.forAll(decl));
  }

  /**
   * Creates the sub setting item rule overall fact.
   *
   * @param ownerSig the owner sig
   * @param transfer the transfer
   */
  // transfer = x.tarnsferSupplierCustomer
  public void createSubSettingItemRuleOverallFact(Sig ownerSig, Expr transfer) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Decl decl = new Decl(null, null, null, List.of(s), ownerSig.oneOf());
    this.addToOverallFact(subsettingItemRuleForSources.call(s.join(transfer)).forAll(decl));
    this.addToOverallFact(subsettingItemRuleForTargets.call(s.join(transfer)).forAll(decl));
  }

  /**
   * Creates the is after source is before target overall fact.
   *
   * @param ownerSig the owner sig
   * @param transfer the transfer
   */
  public void createIsAfterSourceIsBeforeTargetOverallFact(Sig ownerSig, Expr transfer) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Decl decl = new Decl(null, null, null, List.of(s), ownerSig.oneOf());
    this.addToOverallFact(isAfterSource.call(s.join(transfer)).forAll(decl));
    this.addToOverallFact(isBeforeTarget.call(s.join(transfer)).forAll(decl));
  }

  /**
   * Creates the is after source overall fact.
   *
   * @param ownerSig the owner sig
   * @param transfer the transfer
   */
  public void createIsAfterSourceOverallFact(Sig ownerSig, Expr transfer) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Decl decl = new Decl(null, null, null, List.of(s), ownerSig.oneOf());
    this.addToOverallFact(isAfterSource.call(s.join(transfer)).forAll(decl));
  }

  /**
   * Creates the is before target overall fact.
   *
   * @param ownerSig the owner sig
   * @param transfer the transfer
   */
  public void createIsBeforeTargetOverallFact(Sig ownerSig, Expr transfer) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Decl decl = new Decl(null, null, null, List.of(s), ownerSig.oneOf());
    this.addToOverallFact(isBeforeTarget.call(s.join(transfer)).forAll(decl));
  }

  /**
   * Creates the inverse function filtered and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   * @param func the func
   */
  public void createInverseFunctionFilteredAndAddToOverallFact(
      Sig ownerSig, Expr from, Expr to, Func func) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());

    Expr funcFilteredExpr =
        inverseFunctionFiltered.call(
            func.call(), addExprVarToExpr(s, from), addExprVarToExpr(s, to));
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(funcFilteredExpr.forAll(decl));
  }

  /**
   * Creates the function filtered and add to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   * @param func the func
   */
  /* if from or to is null, use ExprVar x */
  public void createFunctionFilteredAndAddToOverallFact(
      Sig ownerSig, Expr from, Expr to, Func func) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());

    to = to == null ? s : to;
    from = from == null ? s : from;

    Expr funcFilteredExpr =
        functionFiltered.call(func.call(), addExprVarToExpr(s, from), addExprVarToExpr(s, to));
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(funcFilteredExpr.forAll(decl));
  }

  /**
   * Creates the bijection filtered to overall fact.
   *
   * @param ownerSig the owner sig
   * @param from the from
   * @param to the to
   * @param func the func
   */
  /* if from or to is null, use ExprVar x */
  public void createBijectionFilteredToOverallFact(Sig ownerSig, Expr from, Expr to, Func func) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    Expr fromExpr = null;
    Expr toExpr = null;

    boolean justFunction = false;
    boolean justInverseFunction = false;
    if (to == null) { // just x - means no field but to itself
      // i.e., fact {all x: B | bijectionFiltered[sources, x.transferB1B2, x.b1]} in
      // 4.1.4 Transfer Parameter2 -Parameter Behavior.als
      justFunction = true;
      toExpr = s;
    } else toExpr = addExprVarToExpr(s, to);
    if (from == null) { // just x - means no field but to itself
      justInverseFunction = true;
      fromExpr = s;
    } else fromExpr = addExprVarToExpr(s, from);

    Expr funcCall = func.call();

    Expr fnc_inversefnc_or_bijection = null;
    if (!justFunction && !justInverseFunction) {
      fnc_inversefnc_or_bijection = bijectionFiltered.call(funcCall, fromExpr, toExpr);
    } else if (justFunction) {
      fnc_inversefnc_or_bijection = functionFiltered.call(funcCall, fromExpr, toExpr);
    } else if (justInverseFunction) {
      fnc_inversefnc_or_bijection = inverseFunctionFiltered.call(funcCall, fromExpr, toExpr);
    }

    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(fnc_inversefnc_or_bijection.forAll(decl));
  }

  /**
   * Example: all s: FoodService | bijectionFiltered[happensBefore, s.order, s.serve]
   *
   * @param var = s
   * @param ownerSig = FoodService
   * @param from = order
   * @param to = serve
   */
  public void createBijectionFilteredHappensBeforeAndAddToOverallFact(
      ExprVar var, Sig ownerSig, Expr from, Expr to) {

    Expr bijectionFilteredExpr =
        bijectionFiltered.call(happensBefore.call(), var.join(from), var.join(to));

    Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
    this.addToOverallFact(bijectionFilteredExpr.forAll(decl));
  }

  /**
   * Returns nonZeroDurationOnly and suppressTransfers and suppressIO.
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

    return nonZeroDurationOnlyFunctionExpression
        .and(suppressTransfersExpression)
        .and(suppressIOExpression);
  }

  /**
   * Adds the to name expr.
   *
   * @param expr the expr
   */
  public void addToNameExpr(Expr expr) {
    _nameExpr = _nameExpr.and(expr);
  }

  /**
   * Adds the cardinality equal constraint to field.
   *
   * @param ownerSig the owner sig
   * @param field the field
   * @param num the num
   */
  public void addCardinalityEqualConstraintToField(Sig ownerSig, Sig.Field field, int num) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(
        s.join(field).cardinality().equal(ExprConstant.makeNUMBER(num)).forAll(decl));
  }

  /**
   * Adds the cardinality greater than equal constraint to field.
   *
   * @param ownerSig the owner sig
   * @param field the field
   * @param num the num
   */
  public void addCardinalityGreaterThanEqualConstraintToField(
      Sig ownerSig, Sig.Field field, int num) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(
        s.join(field).cardinality().gte(ExprConstant.makeNUMBER(num)).forAll(decl));
  }

  /**
   * Adds the equal.
   *
   * @param ownerSig the owner sig
   * @param field1 the field 1
   * @param field2 the field 2
   */
  // fact {all x: B1 | x.vin=x.vout}
  public void addEqual(Sig ownerSig, Sig.Field field1, Sig.Field field2) {
    ExprVar s = ExprVar.make(null, "x", ownerSig.type());
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(s.join(field1).equal(s.join(field2)).forAll(decl));
  }

  /**
   * Adds the one constraint to field.
   *
   * @param var the var
   * @param ownerSig the owner sig
   * @param field the field
   */
  public void addOneConstraintToField(ExprVar var, Sig ownerSig, Sig.Field field) {
    Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
    this.addToOverallFact(
        var.join(field).cardinality().equal(ExprConstant.makeNUMBER(1)).forAll(decl));
  }

  /**
   * No inputs.
   *
   * @param sig the sig
   */
  public void noInputs(Sig sig) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
    addToOverallFact((var.join(oinputs.call()).no()).forAll(decl));
  }

  /**
   * No outputs.
   *
   * @param sig the sig
   */
  public void noOutputs(Sig sig) {
    ExprVar var = ExprVar.make(null, "x", sig.type());
    Decl decl = new Decl(null, null, null, List.of(var), sig.oneOf());
    addToOverallFact((var.join(ooutputs.call()).no()).forAll(decl));
  }

  /**
   * Adds the inputs.
   *
   * @param var the var
   * @param ownerSig the owner sig
   * @param field the field
   */
  public void addInputs(ExprVar var, Sig ownerSig, Field field) {
    Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
    Expr expr = var.join(ownerSig.domain(field));
    if (expr != null) {
      // addToOverallFact((expr).in(var.join(oinputsExpr1)).forAll(decl));
      // addToOverallFact(var.join(oinputsExpr2).in(expr).forAll(decl));
      addBothToOverallFact(var, expr, oinputs.call(), decl);
    }
  }

  /**
   * Adds the outputs.
   *
   * @param var the var
   * @param ownerSig the owner sig
   * @param field the field
   */
  public void addOutputs(ExprVar var, Sig ownerSig, Field field) {
    // Expr ooutExpr1 = ooutputs.call();
    // Expr oinputsExpr2 = ooutputs.call();

    Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
    Expr expr = var.join(ownerSig.domain(field));
    if (expr != null) {
      // addToOverallFact((expr).in(var.join(oinputsExpr1)).forAll(decl));
      // addToOverallFact(var.join(oinputsExpr2).in(expr).forAll(decl));
      addBothToOverallFact(var, expr, ooutputs.call(), decl);
    }
  }

  /**
   * Adds the both to overall fact.
   *
   * @param var the var
   * @param expr the expr
   * @param varJoinExpr the var join expr
   * @param decl the decl
   */
  private void addBothToOverallFact(ExprVar var, Expr expr, Expr varJoinExpr, Decl decl) {
    addToOverallFact((expr).in(var.join(varJoinExpr)).forAll(decl));
    addToOverallFact(var.join(varJoinExpr).in(expr).forAll(decl));
  }

  // public void addSteps(ExprVar var, Sig ownerSig) {
  //
  // // ?? do you need different call?
  // // steps
  // Expr ostepsExpr1 = osteps.call();
  // // Expr ostepsExpr2 = osteps.call();
  //
  // Decl decl = new Decl(null, null, null, List.of(var), ownerSig.oneOf());
  // Expr expr = createStepExpr(var, ownerSig);
  // if (expr != null) {
  // // addToOverallFact((expr).in(var.join(ostepsExpr1)).forAll(decl));
  // // addToOverallFact(var.join(ostepsExpr2).in(expr).forAll(decl));
  // addBothToOverallFact(var, expr, ostepsExpr1, decl);
  // }
  // }
  //
  // private Expr createStepExpr(ExprVar s, Sig ownerSig) {
  //
  // List<String> sortedFieldLabel = new ArrayList<>();
  // for (Field field : ownerSig.getFields()) {
  // sortedFieldLabel.add(field.label);
  // }
  // Collections.sort(sortedFieldLabel);
  //
  // Expr expr = null;
  //
  // for (String fieldName : sortedFieldLabel) {
  // for (Field field : ownerSig.getFields()) {
  // if (field.label.equals(fieldName)) {
  // expr = expr == null ? s.join(ownerSig.domain(field))
  // : expr.plus(s.join(ownerSig.domain(field)));
  // break;
  // }
  // }
  // }
  //
  //
  // return expr;
  // }

  /**
   * Adds the steps.
   *
   * @param sig the sig
   * @param stepFields the step fields
   */
  public void addSteps(Sig sig, Set<String> stepFields) {
    ExprVar s = ExprVar.make(null, "x", sig.type());
    Expr ostepsExpr1 = osteps.call();
    Decl decl = new Decl(null, null, null, List.of(s), sig.oneOf());

    List<String> sortedFieldLabel = new ArrayList<>();
    for (String stepField : stepFields) sortedFieldLabel.add(stepField);
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

  /**
   * Creates the command.
   *
   * @param label the label
   * @param __overall the overall
   * @return the command
   */
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

    Command command =
        new Command(
            _pos,
            _nameExpr,
            _label,
            _check,
            _overall,
            _bitwidth,
            _maxseq,
            _expects,
            _scope,
            _additionalExactSig,
            _formula,
            _parent);
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
