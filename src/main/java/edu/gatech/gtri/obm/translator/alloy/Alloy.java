package edu.gatech.gtri.obm.translator.alloy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

  protected static final String templateString =
      "open Transfer[Occurrence] as o \n" + "abstract sig Occurrence{}";
  protected static PrimSig occSig;
  protected static Module templateModule;
  public static Module transferModule;

  protected static Set<Sig> ignoredSigs;
  protected static Set<Expr> ignoredExprs;
  protected static Set<Func> ignoredFuncs;

  protected static Func happensBefore;
  protected static Func bijectionFiltered;
  protected static Func funcFiltered;
  protected static Func inverseFunctionFiltered;

  protected Expr overallFact;
  protected List<Sig> allSigs;
  protected Expr _nameExpr;



  public Alloy() {

    File tFile = new File(Alloy.class.getResource("/Transfer.als").getFile());
    System.setProperty(("java.io.tmpdir"), tFile.getParent());// find


    A4Reporter rep = new A4Reporter();
    templateModule = CompUtil.parseEverything_fromString(rep, templateString);

    // can not define directory because Module.getAllReachableUserDefinedSigs returns ConstList<Sig>
    // and does not allow to add to the list
    allSigs = new ArrayList<Sig>();
    ignoredSigs = new HashSet<>();
    for (Sig sig : templateModule.getAllReachableUserDefinedSigs()) {
      allSigs.add(sig);
      ignoredSigs.add(sig);
    }
    // add all facts from the occurrence module.
    overallFact = templateModule.getAllReachableFacts();
    ignoredExprs = new HashSet<>();

    ExprList exprList = (ExprList) overallFact;
    for (Expr expr : exprList.args) {
      ignoredExprs.add(expr);
    }


    // abstract
    occSig = (PrimSig) Helper.getReachableSig(templateModule, "this/Occurrence");
    transferModule = Helper.getAllReachableModuleByName(templateModule, "TransferModule");
    ignoredFuncs = new HashSet<>();
    for (Module module : transferModule.getAllReachableModules()) {
      for (Func func : module.getAllFunc()) {
        ignoredFuncs.add(func);
      }
    }

    happensBefore = Helper.getFunction(transferModule, "o/happensBefore");
    bijectionFiltered = Helper.getFunction(transferModule, "o/bijectionFiltered");
    funcFiltered = Helper.getFunction(transferModule, "o/functionFiltered");
    inverseFunctionFiltered = Helper.getFunction(transferModule, "o/inverseFunctionFiltered");


    // constraints
    Func nonZeroDurationOnlyFunction = Helper.getFunction(transferModule, "o/nonZeroDurationOnly");
    Expr nonZeroDurationOnlyFunctionExpression = nonZeroDurationOnlyFunction.call();

    Sig transfer = Helper.getReachableSig(transferModule, "o/Transfer");
    Expr suppressTransfersExpessionBody = transfer.no();
    Func suppressTransfersFunction =
        new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
    Expr suppressTransfersExpression = suppressTransfersFunction.call();

    Func inputs = Helper.getFunction(transferModule, "o/inputs");
    Func outputs = Helper.getFunction(transferModule, "o/outputs");
    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
    Func suppressIOFunction = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
    Expr suppressIOExpression = suppressIOFunction.call();


    _nameExpr = nonZeroDurationOnlyFunctionExpression.and(suppressTransfersExpression)
        .and(suppressIOExpression);
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
    return this.overallFact;
  }

  public List<Sig> getAllSigs() {
    return this.allSigs;
  }

  protected Sig createSigAndAddToAllSigs(String label, PrimSig parent) {
    // Sig s = new PrimSig("this/" + label, parent);
    Sig s = new PrimSig(label, parent);
    allSigs.add(s);
    return s;
  }

  public Sig createSigAsChildOfOccSigAndAddToAllSigs(String label) {
    return createSigAndAddToAllSigs(label, Alloy.occSig);
  }

  public void addToOverallFact(Expr expr) {
    overallFact = expr.and(overallFact);
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
   * and s.p2.
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
        return null;// not supported yet
    } else
      return s.join(original);
  }

  public void createInverseFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    ExprVar s = ExprVar.make(null, "s", ownerSig.type());
    Expr inverseFunctionFilteredExpr = inverseFunctionFiltered.call(happensBefore.call(),
        addExprVarToExpr(s, from), addExprVarToExpr(s, to));

    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(inverseFunctionFilteredExpr.forAll(decl));
  }


  public void createFunctionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    ExprVar s = ExprVar.make(null, "s", ownerSig.type());

    Expr funcFilteredExpr =
        funcFiltered.call(happensBefore.call(), addExprVarToExpr(s, from), addExprVarToExpr(s, to));
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(funcFilteredExpr.forAll(decl));
  }

  public void createBijectionFilteredHappensBeforeAndAddToOverallFact(Sig ownerSig, Expr from,
      Expr to) {
    ExprVar s = ExprVar.make(null, "s", ownerSig.type());
    Expr bijectionFilteredExpr = bijectionFiltered.call(happensBefore.call(),
        addExprVarToExpr(s, from), addExprVarToExpr(s, to));

    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(bijectionFilteredExpr.forAll(decl));
  }


  public void addToNameExpr(Expr expr) {
    _nameExpr = _nameExpr.and(expr);
  }

  public void addOneConstraintToField(Sig ownerSig, Sig.Field field) {
    ExprVar s = ExprVar.make(null, "s", ownerSig.type());
    List<ExprHasName> names = new ArrayList<>(List.of(s));
    Decl decl = new Decl(null, null, null, names, ownerSig.oneOf());
    this.addToOverallFact(s.join(ownerSig.domain(field)).cardinality()
        .equal(ExprConstant.makeNUMBER(1)).forAll(decl));
  }

  public void addSteps(Sig ownerSig, /* Map<String, Field> fieldByName, */
      Map<Field, Sig> filedTypeByField) {

    // steps
    Func osteps = Helper.getFunction(transferModule, "o/steps");
    Expr ostepsExpr1 = osteps.call();
    Expr ostepsExpr2 = osteps.call();

    ExprVar s1 = ExprVar.make(null, "s", ownerSig.type());
    List<ExprHasName> names1 = new ArrayList<>(List.of(s1));
    Decl decl1 = new Decl(null, null, null, names1, ownerSig.oneOf());
    Expr expr1 = createStepExpr(s1, ownerSig, filedTypeByField);
    if (expr1 != null)
      addToOverallFact((expr1).in(s1.join(ostepsExpr1)).forAll(decl1));

    ExprVar s2 = ExprVar.make(null, "s", ownerSig.type());
    List<ExprHasName> names2 = new ArrayList<>(List.of(s2));
    Decl decl2 = new Decl(null, null, null, names2, ownerSig.oneOf());
    Expr expr2 = createStepExpr(s2, ownerSig, filedTypeByField);
    if (expr2 != null)
      this.addToOverallFact(s2.join(ostepsExpr2).in(expr2).forAll(decl2));
  }

  private Expr createStepExpr(ExprVar s, Sig ownerSig,
      /* Map<String, Field> fieldByName*, */ Map<Field, Sig> filedTypeByField) {
    Expr expr = null;
    // for (Iterator<String> iter = fieldByName.keySet().iterator(); iter.hasNext();) {
    // String fieldName = iter.next();
    // Sig.Field field = fieldByName.get(fieldName);
    for (Field field : filedTypeByField.keySet()) {
      if (field.sig == ownerSig)
        expr = expr == null ? s.join(ownerSig.domain(field))
            : expr.plus(s.join(ownerSig.domain(field)));
    }

    return expr;
  }

  private Map<Sig, List<Field>> toFieldsByFieldType(Map<Field, Sig> fieldByFieldType) {
    Map<Sig, List<Field>> fieldsByFieldType = new HashMap<>();
    for (Sig.Field field : fieldByFieldType.keySet()) {
      Sig type = fieldByFieldType.get(field);
      List<Field> fields = null;
      if (fieldsByFieldType.containsKey(type)) {
        fields = fieldsByFieldType.get(type);
      } else {
        fields = new ArrayList<>();
        fieldsByFieldType.put(type, fields);
      }
      fields.add(field);
    }
    return fieldsByFieldType;
  }


  public void addConstraint(Sig ownerSig,
      /*
       * Map<String, Field> fieldByName, Map<String, Sig> fieldTypeByFieldName,
       */ Map<Field, Sig> fieldByFieldType) {

    Map<Sig, List<Field>> fieldsByFieldType = toFieldsByFieldType(fieldByFieldType);
    Expr duringExampleExpressions = null;
    for (Sig type : fieldsByFieldType.keySet()) {
      String labelPrefix = "";
      Expr body = null;
      for (Field field : fieldsByFieldType.get(type)) {
        if (field.sig == ownerSig) {
          body = body == null ? ownerSig.join(field) : body.plus(ownerSig.join(field));
          labelPrefix += field.label;
        }
      }
      if (body != null) {
        Expr pDuringExampleBody = type.in(body);
        String label = labelPrefix + "DuringExample"; // p1DuringExample
        Pos pos = null;
        List<Decl> decls = new ArrayList<>();
        Expr returnDecl = null;
        Func duringExamplePredicate = new Func(pos, label, decls, returnDecl, pDuringExampleBody);
        Expr duringExampleExpression = duringExamplePredicate.call();
        duringExampleExpressions = duringExampleExpressions == null ? duringExampleExpression
            : duringExampleExpressions.and(duringExampleExpression);
      }
    }
    if (duringExampleExpressions != null) {
      Func instancesDuringExamplePredicate = new Func(null, "instancesDuringExample",
          new ArrayList<>(), null, duringExampleExpressions);
      Expr instancesDuringExampleExpression = instancesDuringExamplePredicate.call();
      addToNameExpr(instancesDuringExampleExpression);
    }

  }

  public void addOnlyConstraint(Sig sig) {
    Func onlySimpleSequencePredicate = new Func(null, "only" + sig.label, new ArrayList<>(), null,
        sig.cardinality().equal(ExprConstant.makeNUMBER(1)));
    Expr onlySimpleSequenceExpression = onlySimpleSequencePredicate.call();
    addToNameExpr(onlySimpleSequenceExpression);
  }

  public void addConstraintzz(Sig ownerSig, Map<String, Field> fieldByName,
      Map<String, Sig> fieldTypeByFieldName) {

    // During
    Pos pos = null;
    Expr duringExampleExpressions = null;
    String label = "pDuringExample"; // p1DuringExample
    Expr body = null;
    String commonFieldName = "";
    for (String fieldName : fieldByName.keySet()) {
      commonFieldName = fieldName;
      Sig.Field field = fieldByName.get(fieldName);
      body = body == null ? ownerSig.join(field) : body.plus(ownerSig.join(field));
    }
    // assuming all fieldTypeByFieldName.get(fieldName) are the same
    Expr pDuringExampleBody = fieldTypeByFieldName.get(commonFieldName).in(body);
    List<Decl> decls = new ArrayList<>();
    Expr returnDecl = null;
    Func duringExamplePredicate = new Func(pos, label, decls, returnDecl, pDuringExampleBody);
    Expr duringExampleExpression = duringExamplePredicate.call();
    duringExampleExpressions = duringExampleExpressions == null ? duringExampleExpression
        : duringExampleExpressions.and(duringExampleExpression);



    Func instancesDuringExamplePredicate =
        new Func(null, "instancesDuringExample", new ArrayList<>(), null, duringExampleExpressions);
    Expr instancesDuringExampleExpression = instancesDuringExamplePredicate.call();

    Func onlySimpleSequencePredicate = new Func(null, "only" + ownerSig.label, new ArrayList<>(),
        null, ownerSig.cardinality().equal(ExprConstant.makeNUMBER(1)));
    Expr onlySimpleSequenceExpression = onlySimpleSequencePredicate.call();

    addToNameExpr(instancesDuringExampleExpression);
    addToNameExpr(onlySimpleSequenceExpression);
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
    Expr _formula = _nameExpr.and(getOverAllFact());
    Command _parent = null;

    // ========== Define command ==========

    Command command = new Command(_pos, _nameExpr, _label, _check, _overall, _bitwidth, _maxseq,
        _expects, _scope, _additionalExactSig, _formula, _parent);
    return command;
  }

}


