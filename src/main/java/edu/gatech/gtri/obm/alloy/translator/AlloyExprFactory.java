package edu.gatech.gtri.obm.alloy.translator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;

/**
 * A utility class to create Alloy expression
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 *
 */
public class AlloyExprFactory {

  /**
   * Create BijectionFiltered, isBeforeTraget and/or isAfterSource expression and combined with all x: OwnerSig (ie., {all x: OwnerSig | bijectionFiltered[..., ..., ...]})
   * 
   * @param ownerSig the sig the expression expr belong to.
   * @param from bijectionFilter's 2nd argument
   * @param to bijectionFilter's 3rd argument (sig or field)
   * @param func bijectionFiltered, isBeforeTarget, or isAfterSource
   * @return one or two expression ([bijection], [bijection, isBeforetarget] or [bijection, isAfterSource])
   */

  protected static Set<Expr> exprs_bijectionFilteredFactsForSig(Sig ownerSig, Expr from, Expr to,
      Func func) {

    Set<Expr> facts = new HashSet<>();

    ExprVar varX = makeVarX(ownerSig);
    Expr toExpr = null;
    Expr fromExpr = AlloyUtils.addExprVarToExpr(varX, from);

    if (to == null) {// not to field but to itself (sig x)
      // fact {all x: B | bijectionFiltered[targets, x.transferB2B, x]}
      toExpr = varX;
      if (func == Alloy.sources) { // {fact {all x: B | isBeforeTarget[x.transferBB1]}
        facts.add(Alloy.isBeforeTarget.call(fromExpr));
      } else if (func == Alloy.targets) {// fact {all x: B | isAfterSource[x.transferB2B]}
        facts.add(Alloy.isAfterSource.call(fromExpr));
      }
    } else {
      // fact {all x: MultipleObjectFlow | bijectionFiltered[outputs, x.p1, x.p1.i]}
      toExpr = AlloyUtils.addExprVarToExpr(varX, to);
    }

    Expr bijFilteredFn = Alloy.bijectionFiltered.call(func.call(), fromExpr, toExpr);
    facts.add(bijFilteredFn);

    return AlloyUtils.toSigAllFacts(ownerSig, facts);
  }

  /**
   * Creates a functionFiltered fact.
   * 
   * <pre>
   *  for example)
   * fact: fact f1 { all s: Loop | functionFiltered[happensBefore, s.p1, s.p2] } 
   * where ownerSig = Loop, from = p1, to = p2
   * </pre>
   * 
   * @param ownerSig the sig the expression expr belong to.
   * @param from the 2nd argument of functionFiltered function
   * @param to the 3rd argument of functionFiltered function
   * @return created an inverseFunctionFiltered expr
   * 
   */
  protected static Expr expr_functionFilteredHappensBeforeForSig(Sig ownerSig, Expr from, Expr to) {
    ExprVar varX = makeVarX(ownerSig);
    Expr funcFilteredExpr = Alloy.functionFiltered.call(Alloy.happensBefore.call(),
        AlloyUtils.addExprVarToExpr(varX, from), AlloyUtils.addExprVarToExpr(varX, to));
    return funcFilteredExpr.forAll(makeDecl(varX, ownerSig));
  }

  /**
   * Create an inverseFunctionFiltered fact with happensBefore. The "from" and "to" expr may includes more then one fields with +.
   * 
   * <pre>
   *  for example
   * fact f3 {all s: Loop | functionFiltered[happensBefore, s.p2, s.p2 + s.p3]} where 
   * ownerSig=Loop; from={p2} to={p2, p3}
   * </pre>
   * 
   * @param ownerSig the sig the expression expr belong to.
   * @param from the 2nd argument of functionFiltered function
   * @param to the 3rd argument of functionFiltered function
   * @return created an inverseFunctionFiltered expr
   */
  protected static Expr expr_inverseFunctionFilteredHappensBefore(Sig ownerSig, Expr from,
      Expr to) {
    ExprVar varX = makeVarX(ownerSig);
    Expr inverseFunctionFilteredExpr =
        Alloy.inverseFunctionFiltered.call(Alloy.happensBefore.call(),
            AlloyUtils.addExprVarToExpr(varX, from), AlloyUtils.addExprVarToExpr(varX, to));
    return inverseFunctionFilteredExpr.forAll(makeDecl(varX, ownerSig));
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
   * @param ownerSig the owner sig
   * @param transferExpr the transfer
   */
  protected static Expr exprs_subSettingItemRule(Sig ownerSig, Expr transferExpr) {
    // Set<Expr> facts = new HashSet<>();
    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);
    return Alloy.subsettingItemRuleForSources.call(varX.join(transferExpr)).forAll(decl)
        .and(Alloy.subsettingItemRuleForTargets.call(varX.join(transferExpr)).forAll(decl));
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
   * @param ownerSig the owner sig
   * @param transferExpr the transfer
   */
  protected static Set<Expr> exprs_isAfterSourceIsBeforeTarget(Sig ownerSig, Expr transferExpr) {
    Set<Expr> factsAdded = new HashSet<>();
    ExprVar varX = makeVarX(ownerSig);
    factsAdded.add(Alloy.isAfterSource.call(varX.join(transferExpr)));
    factsAdded.add(Alloy.isBeforeTarget.call(varX.join(transferExpr)));
    return factsAdded;
  }

  /**
   * Create a expression and its reverse expression and return
   * 
   * <pre>
   *  for example
   * {all x: ownerSig| transferOrderPay.items in x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount}
   * {all x: ownerSig| x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount in x.transferOrderPay.items}
   * where ownerSig={ownerSig} transfer ={transferOrderPay} toField ={order} func={o/source} targetInputsSourceOutputsFields={orderedFoodItem, orderAmount}
   * </pre>
   *
   * @param ownerSig the owner sig
   * @param transferExpr the transfer expression
   * @param toField
   * @param func
   * @param targetInputsSourceOutputsFields
   * @return two expressions to return
   */
  protected static Set<Expr> exprs_transferInItems(Sig ownerSig, Expr transferExpr, Expr toField,
      Func func, Set<Field> targetInputsSourceOutputsFields) {
    ExprVar varX = makeVarX(ownerSig);
    Expr funcCall = func.call();

    List<Field> sortedTargetInputsSourceOutputsFields =
        AlloyUtils.sortFields(targetInputsSourceOutputsFields);
    Expr all = null, all_r = null;
    for (Field field : sortedTargetInputsSourceOutputsFields) {
      all = all == null ? varX.join(transferExpr).join(funcCall).join(field)
          : all.plus(varX.join(transferExpr).join(funcCall).join(field));
    }
    // all = x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount

    // x.transferOrderPay.items in
    // x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount
    all_r = varX.join(transferExpr).join(Alloy.oitems.call()).in(all);

    // x.transferOrderPay.sources.orderedFoodItem + x.transferOrderPay.sources.orderAmount in
    // x.transferOrderPay.items
    all = all.in(varX.join(transferExpr).join(Alloy.oitems.call()));

    return new HashSet<>(Arrays.asList(all, all_r));
  }



  /**
   * Adds the cardinality equal constraint to field.
   *
   * @param ownerSig the owner sig
   * @param field the field
   * @param num the num
   */
  protected static Expr expr_cardinalityEqual(Sig ownerSig, Sig.Field field, int num) {
    ExprVar varX = makeVarX(ownerSig);
    return varX.join(field).cardinality().equal(ExprConstant.makeNUMBER(num))
        .forAll(makeDecl(varX, ownerSig));
  }

  /**
   * Adds the cardinality greater than equal constraint to field.
   *
   * @param ownerSig the owner sig
   * @param field the field
   * @param num the num
   */
  protected static Expr expr_cardinalityGreaterThanEqual(Sig ownerSig, Sig.Field field, int num) {
    ExprVar varX = makeVarX(ownerSig);
    return varX.join(field).cardinality().gte(ExprConstant.makeNUMBER(num))
        .forAll(makeDecl(varX, ownerSig));
  }

  /**
   * Adds the equal.
   *
   * @param ownerSig the owner sig
   * @param field1 the field 1
   * @param field2 the field 2
   */
  // fact {all x: B1 | x.vin = x.vout}
  protected static Expr expr_equal(Sig ownerSig, Sig.Field field1, Sig.Field field2) {
    ExprVar varX = makeVarX(ownerSig);
    return varX.join(field1).equal(varX.join(field2)).forAll(makeDecl(varX, ownerSig));
  }

  // fact {all x: B1 | x.vin = x.inputs}
  // because to support inheritance
  // change to {all x: B1 | x.vin in x.inputs} and {all x: B1 | x.inputs in x.vin}
  // other example) fact {all x: OFCustomPrepare | x.prepareDestination + x.preparedFoodItem in x.inputs}
  // and fact {all x: OFCustomPrepare | x.inputs in x.prepareDestination + x.preparedFoodItem}
  protected static Set<Expr> exprs_in(PrimSig ownerSig, List<Sig.Field> sortedFields, Func func) {
    ExprVar varX = makeVarX(ownerSig);
    Decl decl = makeDecl(varX, ownerSig);
    Expr fieldsExpr = null;
    for (Field field : sortedFields) {
      // sig.domain(field) or sig.parent.domain(sig.parent.field)
      Expr sigDomainField = AlloyUtils.getSigDomainField(field.label, ownerSig);
      if (sigDomainField != null)
        fieldsExpr = fieldsExpr == null ? varX.join(sigDomainField)
            : fieldsExpr.plus(varX.join(sigDomainField));
    }

    // .... in x.inputs
    Expr equalExpr = fieldsExpr.in(varX.join(func.call()));
    // addToOverallFact(equalExpr.forAll(decl));

    // x.inputs in ....
    Expr equalExprclosure = (varX.join(func.call())).in(fieldsExpr);
    // addToOverallFact(equalExprclosure.forAll(decl));

    return new HashSet<Expr>(Arrays.asList(equalExpr.forAll(decl), equalExprclosure.forAll(decl)));
  }

  // fact {all x: OFSingleFoodService | x.prepare.inputs in x.prepare.preparedFoodItem +
  // x.prepare.prepareDestination}
  protected static Expr expr_inOutClosure(PrimSig ownerSig, Field fieldOwner,
      List<Sig.Field> fieldOfFields, Func inOrOut) {
    ExprVar varX = makeVarX(ownerSig);
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
    return equalExprclosure.forAll(makeDecl(varX, ownerSig));

  }

  /**
   * Adds the one constraint to field (i.e., fact {all x: UnsatisfiableTransitivity | #(x.p1) = 1})
   *
   * @param var the var
   * @param ownerSig the owner sig
   * @param field the field
   */
  protected static Expr expr_fieldEqualsOne(ExprVar var, Sig ownerSig, Sig.Field field) {
    return var.join(field).cardinality().equal(ExprConstant.makeNUMBER(1))
        .forAll(makeDecl(var, ownerSig));
  }

  // fact {all x: Integer | no steps.x}
  protected static Expr expr_noStepsX(Sig sig) {
    return expr_noFuncX(sig, Alloy.osteps);
  }

  // fact {all x: Integer | no x.steps}
  protected static Expr expr_noXSteps(Sig sig) {
    return expr_noXFunc(sig, Alloy.osteps);
  }

  // {all x| no outputs.x}
  protected static Expr expr_noOutputsX(Sig sig) {
    return expr_noFuncX(sig, Alloy.ooutputs);
  }

  // {all x| no inputs.x}
  protected static Expr expr_noInputsX(Sig sig) {
    return expr_noFuncX(sig, Alloy.oinputs);
  }


  // {all x| no func.x}
  private static Expr expr_noFuncX(Sig sig, Func func) {
    ExprVar var = makeVarX(sig);
    return (func.call().join(var).no()).forAll(makeDecl(var, sig));
  }


  /**
   * Create an expression {all x: sig |no x.func}
   * 
   * @param sig sig of the expression
   * @param func function for the expression
   * @return an expression
   */
  private static Expr expr_noXFunc(Sig sig, Func func) {
    ExprVar var = makeVarX(sig);
    return (var.join(func.call()).no()).forAll(makeDecl(var, sig));
  }


  /**
   * Create no inputs expression {all x| no x.inputs} and return
   * 
   * @param sig
   * @return
   */
  protected static Expr expr_noXInputs(Sig sig) {
    return expr_noXFunc(sig, Alloy.oinputs);
  }

  /**
   * Create no outputs expression {all x| no x.outputs} and return
   * 
   * @param sig
   * @return
   */
  protected static Expr expr_noXOutputs(Sig sig) {
    return expr_noXFunc(sig, Alloy.ooutputs);
  }


  /**
   * Create no input expressions ({no inputs.x} and {no x.inputs}) and return
   * 
   * @param sig
   * @return {no inputs.x} and {no x.inputs}
   */
  protected static Expr exprs_noInputsXAndXInputs(Sig sig) {
    return exprs_noFuncXAndnoXFunc(sig, Alloy.oinputs);
  }

  /**
   * Create no output expressions ({no x.outputs} and {no outputs.x}) and return
   * 
   * @param sig
   * @return two expressions
   */
  protected static Expr exprs_noOutputsXAndXOutputs(Sig sig) {
    return exprs_noFuncXAndnoXFunc(sig, Alloy.ooutputs);
  }


  /**
   * Create one no expression and its reserve expressions (ie., {no inputs.x} and {no x.inputs}) and return
   * 
   * @param sig
   * @param func
   * @return two expressions connected with and
   */
  protected static Expr exprs_noFuncXAndnoXFunc(Sig sig,
      Func func) {
    ExprVar var = makeVarX(sig);
    Decl decl = makeDecl(var, sig);
    return ((func.call().join(var).no()).forAll(decl).and(
        (var.join(func.call()).no()).forAll(decl)));
  }


  /**
   * Create an expression {no item.x} and return
   * 
   * @param sig
   * @return
   */
  protected static Expr expr_noItemsX(Sig sig) {
    return expr_noFuncX(sig, Alloy.oitems);
  }

  /**
   * Create an expression for field to be no inputs or outputs {no x.p4.inputs} or {no x.p4.outputs}
   * 
   * @param sig
   * @param field
   * @param inputsOrOutputs
   * @return
   */
  protected static Expr expr_noInputsOrOutputsField(Sig sig, Field field, Func inputsOrOutputs) {
    ExprVar varX = makeVarX(sig);// x: MultipleObjectFlow
    Expr exprField = varX.join(sig.domain(field)); // x.p4
    return (exprField.join(inputsOrOutputs.call())/* x.p4.outputs */.no())
        .forAll(makeDecl(varX, sig));
  }



  /**
   * create two expressions connected with and
   * 
   * <pre>
   * fact {all x: MultipleObjectFlow | all p: x.p1 | p.i in p.outputs}
   * fact {all x: MultipleObjectFlow | all p: x.p1 | p.outpus in p.i}
   * </pre>
   * 
   * @param sig - an owner signature having the expression (i.e., MultipleObjectFlow)
   * @param sigField - a field of sig(i.e., p1)
   * @param fieldOfsigFieldType - a field (i.e., i) of sig's type (BehaviorWithParameter)
   * @param func (i.e., inputs, outputs)
   * @return
   */

  protected static Expr exprs_inField(Sig sig, Sig.Field sigField,
      Sig.Field fieldOfsigFieldType, Func func) {

    // all x: MultipleObjectFlow
    ExprVar varX = makeVarX(sig);
    Decl declX = makeDecl(varX, sig);

    // all p: x.p1
    ExprVar varP = ExprVar.make(null, "p", sigField.type());// from.type()); // p
    Expr exprField = varX.join(sig.domain(sigField)); // x.p1
    Decl declY = new Decl(null, null, null, List.of(varP), exprField);

    // {p.i in p.outputs}
    Expr equalExpr = varP.join(fieldOfsigFieldType).in(varP.join(func.call()));
    // {p.outputs in p.i}
    Expr equqlExpr2 = varP.join(func.call()).in(varP.join(fieldOfsigFieldType));

    return (equalExpr.forAll(declY).forAll(declX)).and(equqlExpr2.forAll(declY).forAll(declX));
  }



  /**
   * create an expression like ... fact {all x: SimpleSequence | no y: Transfer | y in x.steps}
   * 
   * @param sig sig for having the expression
   * @return expression
   */
  protected static Expr expr_noTransferXSteps(Sig sig) {
    ExprVar varX = makeVarX(sig);
    Decl declX = makeDecl(varX, sig);

    ExprVar varY = ExprVar.make(null, "y", Alloy.transferSig.type());
    Decl declY = new Decl(null, null, null, List.of(varY), Alloy.transferSig.oneOf());

    return ((varY).in(varX.join(Alloy.osteps.call())). /* y in x.steps */forNo(declY)
        ./* no y: Transfer */ forAll(declX))/* all x: SimpleSequence */;
  }


  /**
   * Create step facts { {... in x.steps} for all sigs and {x.steps in ...} or {no steps.x} for leaf-sig
   * 
   * @param sig the sig which have the facts
   * @param stepFields the fields of sigs to be included in step facts
   * @param addInXSteps boolean true for leaf and non-leaf sig to create {.... in x.steps}
   * @param addXStepsIn boolean true if leaf-sig to create {x.steps in ....} or {no steps.x}
   * @return
   */
  protected static Set<Expr> exprs_stepsFields(PrimSig sig, Set<String> stepFields,
      boolean addInXSteps, boolean addXStepsIn) {
    ExprVar varX = makeVarX(sig);
    Decl decl = makeDecl(varX, sig);
    Expr ostepsExpr1 = Alloy.osteps.call();

    List<String> sortedFieldLabel = AlloyUtils.sort(stepFields);;
    Set<Expr> rFacts = new HashSet<>();

    if (addXStepsIn) {// to only leaf sig - all fields including inherited/redefined
      Expr expr = null;
      Expr expr2 = null;
      int countTransfer = 0;
      for (String fieldName : sortedFieldLabel) {
        // sig.domain(field) or sig.parent.domain(sig.parent.field)
        Expr sigDomainField = AlloyUtils.getSigDomainField(fieldName, sig); // including inherited
                                                                            // fields
        if (sigDomainField != null) {
          expr = expr == null ? varX.join(sigDomainField) : expr.plus(varX.join(sigDomainField));
          if (fieldName.startsWith("transfer")) {// only transfer fields
            countTransfer++;
            expr2 =
                expr2 == null ? varX.join(sigDomainField)
                    : expr2.intersect(varX.join(sigDomainField)); // & = intersect
          }
        }
      }
      if (expr != null)
        rFacts.add(varX.join(ostepsExpr1).in(expr).forAll(decl)); // x.steps in .....
      if (countTransfer > 1 && expr2 != null) // not to have one no transfer like no {transferXXX}
        rFacts.add(expr2.no().forAll(decl)); // like {no x.transferOrderPay & x.transferOrderPrepare & x.transferOrderServe & x.transferPayEat & x.transferPrepareServe & x.transferServeEat} in OFControlLoopFoodService(leaf)
    }

    if (addInXSteps) {// for all sigs - own fields - not include redefined
      Expr expr = null;
      for (String fieldName : sortedFieldLabel) {
        Expr sigDomainField = AlloyUtils.getSigOwnField(fieldName, sig);
        // only own fields
        if (sigDomainField != null) {
          expr = expr == null ? varX.join(sigDomainField) : expr.plus(varX.join(sigDomainField));
        }
      }
      if (expr != null)
        rFacts.add((expr).in(varX.join(ostepsExpr1)).forAll(decl)); // .... in x.steps
      else if (addXStepsIn) // if leaf sig, then addXStepsIn = true -> if sig has no own fields then
                            // add {no steps.x}
        rFacts.add(AlloyExprFactory.expr_noStepsX(sig));
    }
    return rFacts;
  }

  // fact {all x: OFFoodService | x.eat in OFEat }
  // sig = OFFoodService, field eat, typeSig = OFEat
  protected static Expr expr_redefinedSubsetting(Sig sig, Field field, Sig typeSig) {
    ExprVar varX = makeVarX(sig);
    return varX.join(field).in(typeSig).forAll(makeDecl(varX, sig));
  }

  private static ExprVar makeVarX(Sig sig) {
    return ExprVar.make(null, "x", sig.type());
  }

  private static Decl makeDecl(ExprVar x, Sig sig) {
    return new Decl(null, null, null, List.of(x), sig.oneOf());
  }

  protected static Decl makeDecl(Sig ownerSig) {
    return new Decl(null, null, null, List.of(makeVarX(ownerSig)), ownerSig.oneOf());
  }



}
