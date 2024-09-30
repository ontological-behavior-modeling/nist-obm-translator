package edu.gatech.gtri.obm.alloy.translator;

import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A utility class to create Alloy expression
 *
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 */
public class AlloyExprFactory {

  /**
   * Create BijectionFiltered, isBeforeTraget and/or isAfterSource expression and combined with all
   * x: OwnerSig (ie., {all x: OwnerSig | bijectionFiltered[..., ..., ...]})
   *
   * @param _ownerSig (Sig) - the signature the expression belong to.
   * @param _from (Expr) - bijectionFilter function's 2nd argument
   * @param _to (Expr) - bijectionFilter function's 3rd argument (signature or field)
   * @param _func (Func) - bijectionFiltered, isBeforeTarget, or isAfterSource
   * @return (Set<Expr>) - one or two expression ([bijection], [bijection, isBeforetarget] or
   *     [bijection, isAfterSource])
   */
  protected static Set<Expr> exprs_bijectionFilteredFactsForSig(
      Sig _ownerSig, Expr _from, Expr _to, Func _func) {

    Set<Expr> facts = new HashSet<>();

    ExprVar varX = makeVarX(_ownerSig);
    Expr toExpr = null;
    Expr fromExpr = AlloyUtils.addExprVarToExpr(_from, varX);

    if (_to == null) { // not to field but to itself (sig x)
      // fact {all x: B | bijectionFiltered[targets, x.transferB2B, x]}
      toExpr = varX;
      if (_func == Alloy.sources) { // {fact {all x: B | isBeforeTarget[x.transferBB1]}
        facts.add(Alloy.isBeforeTarget.call(fromExpr));
      } else if (_func == Alloy.targets) { // fact {all x: B | isAfterSource[x.transferB2B]}
        facts.add(Alloy.isAfterSource.call(fromExpr));
      }
    } else {
      // fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p1, x.p1.i]}
      toExpr = AlloyUtils.addExprVarToExpr(_to, varX);
    }

    Expr bijFilteredFn = Alloy.bijectionFiltered.call(_func.call(), fromExpr, toExpr);
    facts.add(bijFilteredFn);

    return AlloyUtils.toSigAllFacts(_ownerSig, facts);
  }

  /**
   * Creates a functionFiltered fact.
   *
   * <pre>
   * for example)
   * fact: fact f1 { all s: Loop | functionFiltered[happensBefore, s.p1, s.p2] }
   * where ownerSig = Loop, from = p1, to = p2
   * </pre>
   *
   * @param _ownerSig (Sig) - the signature the expression expression belong to.
   * @param _from (Expr) - the 2nd argument of functionFiltered function
   * @param _to (Expr) - the 3rd argument of functionFiltered function
   * @return (Expr) - created the inverseFunctionFiltered expression
   */
  protected static Expr expr_functionFilteredHappensBeforeForSig(
      Sig _ownerSig, Expr _from, Expr _to) {
    ExprVar varX = makeVarX(_ownerSig);
    Expr funcFilteredExpr =
        Alloy.functionFiltered.call(
            Alloy.happensBefore.call(),
            AlloyUtils.addExprVarToExpr(_from, varX),
            AlloyUtils.addExprVarToExpr(_to, varX));
    return funcFilteredExpr.forAll(makeDecl(_ownerSig, varX));
  }

  /**
   * Create an inverseFunctionFiltered fact with happensBefore. The "from" and "to" expr may
   * includes more then one fields with +.
   *
   * <pre>
   *  for example
   * fact f3 {all s: Loop | functionFiltered[happensBefore, s.p2, s.p2 + s.p3]} where
   * ownerSig=Loop; from={p2} to={p2, p3}
   * </pre>
   *
   * @param _ownerSig (Sig) - the sig the expression expr belong to.
   * @param _from (Expr) - the 2nd argument of functionFiltered function
   * @param _to (Expr) - the 3rd argument of functionFiltered function
   * @return (Expr) - created the inverseFunctionFiltered expr
   */
  protected static Expr expr_inverseFunctionFilteredHappensBefore(
      Sig _ownerSig, Expr _from, Expr _to) {
    ExprVar varX = makeVarX(_ownerSig);
    Expr inverseFunctionFilteredExpr =
        Alloy.inverseFunctionFiltered.call(
            Alloy.happensBefore.call(),
            AlloyUtils.addExprVarToExpr(_from, varX),
            AlloyUtils.addExprVarToExpr(_to, varX));
    return inverseFunctionFilteredExpr.forAll(makeDecl(_ownerSig, varX));
  }

  /**
   * Creates the two sub setting item rule expressions connected with "and" and return.
   *
   * <pre>
   * for example)
   * (fact {all x: ParticipantTransfer | subsettingItemRuleForSources[x.transferSupplierCustomer]}).and
   * (fact {all x: ParticipantTransfer | subsettingItemRuleForTargets[x.transferSupplierCustomer]})
   * where ownerSig = {ParticipantTransfer}, transferExpr = {transferSupplierCustomer}
   * </pre>
   *
   * @param _ownerSig (Sig) - the owner signature
   * @param _transferExpr (Expr) - the transfer expression
   * @return (Expr) - returning expression
   */
  protected static Expr exprs_subSettingItemRule(Sig _ownerSig, Expr _transferExpr) {
    // Set<Expr> facts = new HashSet<>();
    ExprVar varX = makeVarX(_ownerSig);
    Decl decl = makeDecl(_ownerSig, varX);
    return Alloy.subsettingItemRuleForSources
        .call(varX.join(_transferExpr))
        .forAll(decl)
        .and(Alloy.subsettingItemRuleForTargets.call(varX.join(_transferExpr)).forAll(decl));
  }

  /**
   * Creates the isAfterSource and the isBeforeTarget expressions and return
   *
   * <pre>
   * For example)
   * {all x: ParameterBehavior | isAfterSource[x.transferbeforeAB]}
   * {all x: ParameterBehavior | isBeforeTarget[x.transferbeforeAB]}
   * where ownerSig = {ParameterBehavior}, transterExpr={transferbeforeAB}
   * </pre>
   *
   * @param _ownerSig (Sig) - the owner sig
   * @param _transferExpr (Expr) - the transfer expression
   * @return (Set<Expr>) - the isAfterSource and isBeforeTarget Expressions for the given transfer
   *     expression for the given signature
   */
  protected static Set<Expr> exprs_isAfterSourceIsBeforeTarget(Sig _ownerSig, Expr _transferExpr) {
    Set<Expr> factsAdded = new HashSet<>();
    ExprVar varX = makeVarX(_ownerSig);
    factsAdded.add(Alloy.isAfterSource.call(varX.join(_transferExpr)));
    factsAdded.add(Alloy.isBeforeTarget.call(varX.join(_transferExpr)));
    return factsAdded;
  }

  /**
   * Create a expression for transfer and its reverse expression and return the two expression as a
   * set
   *
   * <pre>
   *  for example
   * {all x: ownerSig| transferOrderPay.items in x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount}
   * {all x: ownerSig| x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount in x.transferOrderPay.items}
   * where ownerSig={ownerSig}, transfer ={transferOrderPay}, func={o/source}, and targetInputsSourceOutputsFields={orderedFoodItem, orderAmount}
   *
   * fact {all x: TransferProduct | x.transferSupplierCustomer.items in x.transferSupplierCustomer.targets.receivedProduct}
   * fact {all x: TransferProduct | x.transferSupplierCustomer.targets.receivedProduct in x.transferSupplierCustomer.items}
   * where ownerSig={ownerSig}, transfer={transferSupplierCustomer}, func={o/target}, and targetInputsSourceOutputsFields={receivedProduct}
   * </pre>
   *
   * @param _ownerSig (Sig) - the owner sig
   * @param _transferExpr (Expr) - the transfer expression
   * @param _func (Func) - a function (o/sources or o/targets)
   * @param _targetInputs_or_sourceOutputs fields (Set<Field>) - fields when _func is o/sources then
   *     sourceOutputProperties and when _func is o/target, then targetInputProperty
   * @return (Set<Expr>) - the two expressions to return
   */
  protected static Set<Expr> exprs_transferInItems(
      Sig _ownerSig,
      Expr _transferExpr,
      Func _func,
      Set<Field> _targetInputs_or_sourceOutputsFields) {
    ExprVar varX = makeVarX(_ownerSig);
    Expr funcCall = _func.call();

    List<Field> sortedTargetInputsSourceOutputsFields =
        AlloyUtils.sortFields(_targetInputs_or_sourceOutputsFields);
    Expr all = null, all_r = null;
    for (Field field : sortedTargetInputsSourceOutputsFields) {
      all =
          all == null
              ? varX.join(_transferExpr).join(funcCall).join(field)
              : all.plus(varX.join(_transferExpr).join(funcCall).join(field));
    }
    // all = x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount

    // x.transferOrderPay.items in
    // x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount
    all_r = varX.join(_transferExpr).join(Alloy.oitems.call()).in(all);

    // x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount in
    // x.transferOrderPay.items
    all = all.in(varX.join(_transferExpr).join(Alloy.oitems.call()));

    return new HashSet<>(Arrays.asList(all, all_r));
  }

  /**
   * Adds the cardinality equal constraint to field (i.e, fact {all x: ParameterBehavior | #(x.a) =
   * 1})
   *
   * @param _ownerSig (Sig) - the owner signature
   * @param _field (Field) - the field of the signature
   * @param _number (int) - equal to number
   * @return (Expr) - the created expression
   */
  protected static Expr expr_cardinalityEqual(Sig _ownerSig, Sig.Field _field, int _number) {
    ExprVar varX = makeVarX(_ownerSig);
    return varX.join(_field)
        .cardinality()
        .equal(ExprConstant.makeNUMBER(_number))
        .forAll(makeDecl(_ownerSig, varX));
  }

  /**
   * Adds the cardinality greater than equal constraint to field.
   *
   * @param _ownerSig (Sig) - the owner signature
   * @param _field (Field) - the field of the signature
   * @param _number (int) - greater than equal to number
   * @return (Expr) - the created expression
   */
  protected static Expr expr_cardinalityGreaterThanEqual(
      Sig _ownerSig, Sig.Field _field, int _number) {
    ExprVar varX = makeVarX(_ownerSig);
    return varX.join(_field)
        .cardinality()
        .gte(ExprConstant.makeNUMBER(_number))
        .forAll(makeDecl(_ownerSig, varX));
  }

  /**
   * Adds the equal constraint (i.e, fact {all x: B1 | x.vin = x.vout})
   *
   * @param _ownerSig (Sig) - the owner signature
   * @param _field1 (Field) - one of field in the owner signature to define equal to
   * @param _field2 (Field) - one of field in the owner signature to define equal to
   * @return (Expr) - the created expression
   */
  protected static Expr expr_equal(Sig _ownerSig, Sig.Field _field1, Sig.Field _field2) {
    ExprVar varX = makeVarX(_ownerSig);
    return varX.join(_field1).equal(varX.join(_field2)).forAll(makeDecl(_ownerSig, varX));
  }

  /**
   * Create two expressions like {all x: B1 | x.vin in x.inputs} and its closure like {all x: B1 |
   * x.inputs in x.vin}.
   *
   * <p>The other example {all x: OFCustomPrepare | x.prepareDestination + x.preparedFoodItem in
   * x.inputs} and its closure like {all x: OFCustomPrepare | x.inputs in x.prepareDestination +
   * x.preparedFoodItem}
   *
   * @param _ownerSig (PrimSig) - the owner signature
   * @param _sortedFields (List<Field>) - the fields of the owner signature alphabetically sorted by
   *     their labels
   * @param _inOrOutFunc (Func) - Alloy.oinputs or Alloy.ooutput function
   * @return (Set<Expr>) - the created two expressions
   */
  protected static Set<Expr> exprs_in(
      PrimSig _ownerSig, List<Sig.Field> _sortedFields, Func _inOrOutFunc) {
    ExprVar varX = makeVarX(_ownerSig);
    Decl decl = makeDecl(_ownerSig, varX);
    Expr fieldsExpr = null;
    for (Field field : _sortedFields) {
      // sig.domain(field) or sig.parent.domain(sig.parent.field)
      Expr sigDomainField = AlloyUtils.getFieldAsExprFromSigOrItsParents(field.label, _ownerSig);
      if (sigDomainField != null)
        fieldsExpr =
            fieldsExpr == null
                ? varX.join(sigDomainField)
                : fieldsExpr.plus(varX.join(sigDomainField));
    }

    // .... in x.inputs
    Expr equalExpr = fieldsExpr.in(varX.join(_inOrOutFunc.call()));
    // addToOverallFact(equalExpr.forAll(decl));

    // x.inputs in ....
    Expr equalExprclosure = (varX.join(_inOrOutFunc.call())).in(fieldsExpr);
    // addToOverallFact(equalExprclosure.forAll(decl));

    return new HashSet<Expr>(Arrays.asList(equalExpr.forAll(decl), equalExprclosure.forAll(decl)));
  }

  /**
   * Create a closure expression like {all x: OFSingleFoodService | x.prepare.inputs in
   * x.prepare.preparedFoodItem + x.prepare.prepareDestination} where _ownerSig =
   * OFSingleFoodService, _fieldOwner = prepare, _fieldsOfField = [preparedFoodItem.
   * prepareDestination], _inOrOutFunc = inputs.
   *
   * @param _ownerSig (PrimSig) - the owner signature
   * @param _fieldOwner (Field) - a field (a relation to the owner signature) for the owner
   *     signature
   * @param _fieldsOfField (List<Field>) - a field type signature fields (i.e., prepareFoodItem and
   *     prepareDestination are for prepare field type of Prepare signature).
   * @param _inOrOutFunc (Func) - Alloy.oinputs or Alloy.ooutputs function
   * @return (Expr) - the created expression
   */
  protected static Expr expr_inOutClosure(
      PrimSig _ownerSig, Field _fieldOwner, List<Sig.Field> _fieldsOfField, Func _inOrOutFunc) {
    ExprVar varX = makeVarX(_ownerSig);
    Expr fieldsExpr = null;
    for (Field field : _fieldsOfField)
      fieldsExpr =
          fieldsExpr == null
              ? varX.join(_fieldOwner).join(field)
              : fieldsExpr.plus(varX.join(_fieldOwner).join(field));

    // x.inputs in ....
    Expr equalExprclosure = (varX.join(_fieldOwner).join(_inOrOutFunc.call())).in(fieldsExpr);
    return equalExprclosure.forAll(makeDecl(_ownerSig, varX));
  }

  /**
   * Create a no expression based on function (i.e., {all x| no func.x}) and return
   *
   * @param _sig (Sig) - the owner signature
   * @param _func (Func) - a function for the expression
   * @return (Expr) - the created expression
   */
  private static Expr expr_noFuncX(Sig _sig, Func _func) {
    ExprVar var = makeVarX(_sig);
    return (_func.call().join(var).no()).forAll(makeDecl(_sig, var));
  }

  /**
   * Create a no outputs expression like {all x| no outputs.x} and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @return (Expr) - the created expression
   */
  protected static Expr expr_noOutputsX(Sig _sig) {
    return expr_noFuncX(_sig, Alloy.ooutputs);
  }

  /**
   * Create a no inputs expression like {all x| no inputs.x} and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @return (Expr) - the created expression
   */
  protected static Expr expr_noInputsX(Sig _sig) {
    return expr_noFuncX(_sig, Alloy.oinputs);
  }

  /**
   * Create a expression like {all x: Integer | no steps.x}
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @return (Expr) - the created expression
   */
  protected static Expr expr_noStepsX(Sig _sig) {
    return expr_noFuncX(_sig, Alloy.osteps);
  }

  /**
   * Create a expression like {all x: Integer | no x.steps}
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @return (Expr) - the created expression
   */
  protected static Expr expr_noXSteps(Sig _sig) {
    return expr_noXFunc(_sig, Alloy.osteps);
  }

  /**
   * Create an expression like{ all x: sig |no x.func}
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @param _func (Func) - function for the expression
   * @return (Expr) - the created expression
   */
  private static Expr expr_noXFunc(Sig _sig, Func _func) {
    ExprVar var = makeVarX(_sig);
    return (var.join(_func.call()).no()).forAll(makeDecl(_sig, var));
  }

  /**
   * Create no inputs expression {all x| no x.inputs} and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @return (Expr) - the created expression
   */
  protected static Expr expr_noXInputs(Sig _sig) {
    return expr_noXFunc(_sig, Alloy.oinputs);
  }

  /**
   * Create no outputs expression {all x| no x.outputs} and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @return (Expr) - the created expression
   */
  protected static Expr expr_noXOutputs(Sig _sig) {
    return expr_noXFunc(_sig, Alloy.ooutputs);
  }

  /**
   * Create no inputs expression and its closure expression connected with and "{all x| no inputs.x}
   * and {all x| no x.inputs}" and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @return (Expr) - the two created expression
   */
  protected static Expr exprs_noInputsXAndXInputs(Sig _sig) {
    return exprs_noFuncXAndnoXFunc(_sig, Alloy.oinputs);
  }

  /**
   * Create no outputs expression and its closure expression connected with and "{all x| no
   * x.outputs} and {all x| no outputs.x}" and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @return (Expr) - the two created expression connected with and
   */
  protected static Expr exprs_noOutputsXAndXOutputs(Sig _sig) {
    return exprs_noFuncXAndnoXFunc(_sig, Alloy.ooutputs);
  }

  /**
   * Create one no expression and its closure expression connected with and (ie., "{all x| no
   * inputs.x} and {all x| no x.inputs}") and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @param _func (Func) - function for the expression
   * @return (Expr) - the two created expression connected with and
   */
  protected static Expr exprs_noFuncXAndnoXFunc(Sig _sig, Func _func) {
    ExprVar var = makeVarX(_sig);
    Decl decl = makeDecl(_sig, var);
    return ((_func.call().join(var).no())
        .forAll(decl)
        .and((var.join(_func.call()).no()).forAll(decl)));
  }

  /**
   * Create the no item expression {all x : TheGivenSig | no item.x} and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @return (Expr) - the created expression
   */
  protected static Expr expr_noItemsX(Sig _sig) {
    return expr_noFuncX(_sig, Alloy.oitems);
  }

  /**
   * Create an expression for a field to be no inputs or outputs like {all x: TheGivenSig | no
   * x.p4.inputs} or {all x: TheGivenSig |no x.p4.outputs} and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @param _field (Field) - the field to be defined as inputs or outputs
   * @param _inputsOrOutputs (Func) - Alloy.oinputs or Alloy.ooutputs Function
   * @return (Expr) - the created expression
   */
  protected static Expr expr_noInputsOrOutputsField(Sig _sig, Field _field, Func _inputsOrOutputs) {
    ExprVar varX = makeVarX(_sig); // x: TheGivenSig
    Expr exprField = varX.join(_sig.domain(_field)); // x.p4
    return (exprField.join(_inputsOrOutputs.call()) /* x.p4.outputs */.no())
        .forAll(makeDecl(_sig, varX));
  }

  /**
   * create two expressions like below:
   *
   * <pre>
   * {all x: MultipleObjectFlow | all p: x.p1 | p.i in p.outputs}
   * and
   * {all x: MultipleObjectFlow | all p: x.p1 | p.outpus in p.i}
   * </pre>
   *
   * @param _sig (Sig) - the owner signature of the creating expression (i.e., MultipleObjectFlow)
   * @param _sigField (Field) - a field of sig(i.e., p1)
   * @param _fieldOfsigFieldType (Field) - a field (i.e., i) of sig field(i.e, p1)'s type
   *     (BehaviorWithParameter)
   * @param _func (Func) - Alloy.oinputs or Alloy.ooutputs Function
   * @return (Expr) - the created expressions (two expression connected with "and")
   */
  protected static Expr exprs_inField(
      Sig _sig, Sig.Field _sigField, Sig.Field _fieldOfsigFieldType, Func _func) {

    // all x: MultipleObjectFlow
    ExprVar varX = makeVarX(_sig);
    Decl declX = makeDecl(_sig, varX);

    // all p: x.p1
    ExprVar varP = ExprVar.make(null, "p", _sigField.type()); // from.type()); // p
    Expr exprField = varX.join(_sig.domain(_sigField)); // x.p1
    Decl declY = new Decl(null, null, null, List.of(varP), exprField);

    // {p.i in p.outputs}
    Expr equalExpr = varP.join(_fieldOfsigFieldType).in(varP.join(_func.call()));
    // {p.outputs in p.i}
    Expr equqlExpr2 = varP.join(_func.call()).in(varP.join(_fieldOfsigFieldType));

    return (equalExpr.forAll(declY).forAll(declX)).and(equqlExpr2.forAll(declY).forAll(declX));
  }

  /**
   * Create an expression like {all x: SimpleSequence | no y: Transfer | y in x.steps} and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression
   * @return (Expr) - the created expression
   */
  protected static Expr expr_noTransferXSteps(Sig _sig) {
    ExprVar varX = makeVarX(_sig);
    Decl declX = makeDecl(_sig, varX);

    ExprVar varY = ExprVar.make(null, "y", Alloy.transferSig.type());
    Decl declY = new Decl(null, null, null, List.of(varY), Alloy.transferSig.oneOf());

    return ((varY)
        .in(varX.join(Alloy.osteps.call()))
        .
        /* y in x.steps */ forNo(declY)
        .
        /* no y: Transfer */ forAll(declX)) /* all x: SimpleSequence */;
  }

  /**
   * Create step expressions { {... in x.steps} for all signatures and {x.steps in ...} or {no
   * steps.x} for leaf signatures and return
   *
   * <pre>
   * For example,
   * {all x: Real | no (steps.x)}
   * {all x: B | x.b1 + x.b2 + x.transferB1B2 + x.transferB2B + x.transferBB1 in x.steps}
   * {all x: B | x.steps in x.b1 + x.b2 + x.transferB1B2 + x.transferB2B + x.transferBB1}
   * </pre>
   *
   * @param _sig(Sig) - the owner signature of the creating expression
   * @param _stepFields (Set<String>) - field names of the signature to be included in step
   *     expression
   * @param _addInXSteps (boolean) - if true, create a expression for leaf or non-leaf signature
   *     like {.... in x.steps}
   * @param _addXStepsIn (boolean) - if true, create a expression for leaf-signature to create
   *     {x.steps in ....} or {no steps.x}
   * @return (Set<Expr>) - the created expressions
   */
  protected static Set<Expr> exprs_stepsFields(
      PrimSig _sig, Set<String> _stepFields, boolean _addInXSteps, boolean _addXStepsIn) {
    ExprVar varX = makeVarX(_sig);
    Decl decl = makeDecl(_sig, varX);
    Expr ostepsExpr1 = Alloy.osteps.call();

    List<String> sortedFieldLabel = AlloyUtils.sort(_stepFields);
    ;
    Set<Expr> rFacts = new HashSet<>();

    if (_addXStepsIn) { // to only leaf sig - all fields including inherited/redefined
      Expr expr = null;
      Expr expr2 = null;
      int countTransfer = 0;
      for (String fieldName : sortedFieldLabel) {
        // sig.domain(field) or sig.parent.domain(sig.parent.field)
        Expr sigDomainField =
            AlloyUtils.getFieldAsExprFromSigOrItsParents(fieldName, _sig); // including inherited
        // fields
        if (sigDomainField != null) {
          expr = expr == null ? varX.join(sigDomainField) : expr.plus(varX.join(sigDomainField));
          if (fieldName.startsWith("transfer")) { // only transfer fields
            countTransfer++;
            expr2 =
                expr2 == null
                    ? varX.join(sigDomainField)
                    : expr2.intersect(varX.join(sigDomainField)); // & = intersect
          }
        }
      }
      if (expr != null)
        rFacts.add(varX.join(ostepsExpr1).in(expr).forAll(decl)); // x.steps in .....
      if (countTransfer > 1 && expr2 != null) // not to have one no transfer like no {transferXXX}
      rFacts.add(expr2.no().forAll(decl)); // like {no x.transferOrderPay & x.transferOrderPrepare &
      // x.transferOrderServe & x.transferPayEat & x.transferPrepareServe &
      // x.transferServeEat} in OFControlLoopFoodService(leaf)
    }

    if (_addInXSteps) { // for all sigs - own fields - not include redefined
      Expr expr = null;
      for (String fieldName : sortedFieldLabel) {
        Expr sigDomainField = AlloyUtils.getFieldFromSig(fieldName, _sig);
        // only own fields
        if (sigDomainField != null) {
          expr = expr == null ? varX.join(sigDomainField) : expr.plus(varX.join(sigDomainField));
        }
      }
      if (expr != null)
        rFacts.add((expr).in(varX.join(ostepsExpr1)).forAll(decl)); // .... in x.steps
      else if (_addXStepsIn) // if leaf sig, then addXStepsIn = true -> if sig has no own fields
        // then
        // add {no steps.x}
        rFacts.add(AlloyExprFactory.expr_noStepsX(_sig));
    }
    return rFacts;
  }

  /**
   * Create a expression like {all x: OFControlLoopFoodService | x.prepare in OFCustomPrepare} and
   * return The expression is to redefine field type specified by parent signature
   * (FoodService.prepare: Prepare > OFFoodService.prepare: OFPrepare >
   * OFControlLoopFoodService.prepare: OFCustomPrepare) by a constraint
   *
   * @param _sig (Sig) - the owner signature of the creating expression (i.e.,
   *     OFControlLoopFoodService)
   * @param _field (Field) - a field for the signature (i.e., prepare)
   * @param _typeSig (Sig) - the type of field (i.e., OFCustomPrepare)
   * @return (Expr) - the created expression
   */
  protected static Expr expr_redefinedSubsetting(Sig _sig, Field _field, Sig _typeSig) {
    ExprVar varX = makeVarX(_sig);
    return varX.join(_field).in(_typeSig).forAll(makeDecl(_sig, varX));
  }

  /**
   * Create ExprVar x for the given signature and return
   *
   * @param _sig (Sig) - the owner signature of the creating expression (i.e., TheGivenSig)
   * @return (ExprVar) - the created expression variable
   */
  private static ExprVar makeVarX(Sig _sig) {
    return ExprVar.make(null, "x", _sig.type());
  }

  /**
   * Create Decl for signature with the given expression variable as "one of" (i.e, x: TheGivenSig)
   * and return. Combine with for all and used as a part of expression (i.e., "x: OFFoodService" of
   * "{all x: OFFoodService | x.eat in OFEat }")
   *
   * @param _sig (Sig) - the owner signature of the creating expression (i.e.,
   *     OFControlLoopFoodService)
   * @param _x (ExprVar) - an expression variable
   * @return (Decl) - the created Decl
   */
  private static Decl makeDecl(Sig _sig, ExprVar _x) {
    return new Decl(null, null, null, List.of(_x), _sig.oneOf());
  }

  /**
   * Create Decl for signature with Decl x as "one of" and return. Combine with for all and used as
   * a part of expression (i.e., "x: OFFoodService" of "{all x: OFFoodService | x.eat in OFEat }"}).
   *
   * @param _sig (Sig) - the owner signature of the creating expression (i.e.,
   *     OFControlLoopFoodService)
   * @return (Decl) the created Decl
   */
  protected static Decl makeDecl(Sig _sig) {
    return new Decl(null, null, null, List.of(makeVarX(_sig)), _sig.oneOf());
  }
}
