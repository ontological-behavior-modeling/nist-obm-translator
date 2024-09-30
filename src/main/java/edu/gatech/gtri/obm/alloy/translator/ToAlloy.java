package edu.gatech.gtri.obm.alloy.translator;

import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A helper class to translate OBMXMI to Alloy.
 *
 * <p>Assumption - All PrimSig created have unique names
 *
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 */
public class ToAlloy {

  /** Alloy object */
  private Alloy alloy;
  /**
   * Map key = sig name, value = the PrimSig having the key name. used to retrieve the PrimSig based
   * on its name.
   */
  private Map<String, PrimSig> sigByName;

  /**
   * A constructor
   *
   * @param _alloy(Alloy) - An Alloy object to be created by translator
   */
  protected ToAlloy(Alloy _alloy) {
    alloy = _alloy;
    alloy.initialize();
    sigByName = new HashMap<>();
  }

  /**
   * Gets a signature by name
   *
   * @param name(String) - the name of signature looking for
   * @return (PrimSig) - the found signature or null if the name is not in the sigByName instance
   *     variable
   */
  protected PrimSig getSig(String name) {
    return sigByName.get(name);
  }

  /**
   * Create PrimSig of the given name with the given parent Sig. Only called when the sig is not
   * exist yet and parentSig is not null.
   *
   * @param name(String) - name of PrimSig to be created
   * @param parentSig(PrimSig) - the parentSig, can not be null
   * @param isMainSig(boolean) - true if the given signature is mainSig(used to create
   *     this.moduleName), otherwise false
   * @return (PrimSig) - the created PrimSig or null if already existing
   */
  protected PrimSig createSig(String _name, PrimSig _parentSig, boolean _isMainSig) {

    PrimSig sig = createSig(_name, _parentSig);
    if (sig == null) return null;
    if (_isMainSig)
      // removing "this/" from s.label and assigns as moduleName
      alloy.setModuleName(
          (sig.label.startsWith("this") ? sig.label.substring(5) : sig.label) + "Module");
    return sig;
  }

  /**
   * Return already existing PrimSig of the given name. If the given name PrimSig does not exist,
   * create the PrimSig with the given parentName sig. When the parentName is null, Occurrence,
   * BehaviorOccurrence or Anything, the parent of created sig will be Occurrence. If the parentName
   * is other than that the parent should be already created with the name.
   *
   * @param _name(String) - the name of signature looking for.
   * @param _parentName(String) - the name of parent signature for the signature looking for. The
   *     parent should be already existing.
   * @return (PrimSig) - the found signature.
   */
  protected PrimSig createSigOrReturnSig(String _name, String _parentName) {

    PrimSig exstingSig = getSig(_name);
    if (exstingSig != null) return exstingSig;

    PrimSig parentSig = null;
    if (!AlloyUtils.isValidUserDefineParent(
        _parentName)) // null, Occurrence, BehaviorOccurrence or Anything
      // then parent is Occurrence
      parentSig = Alloy.occSig;
    else parentSig = (PrimSig) sigByName.get(_parentName);
    return createSig(_name, parentSig);
  }

  /**
   * Create PrimSig with the given name and the given parent Sig and add to the Alloy's sigs and
   * this.sigByNames
   *
   * @param _name(String) - The name of the PrimSig to be created
   * @param _parentSig(PrimSig) - The parent of the PrimSig to be created
   * @return (PrimSig) - the created PrimSig or null if the PrimSig already existed and not created.
   */
  private PrimSig createSig(String _name, PrimSig _parentSig) {
    if (getSig(_name) != null) // already existing in sigByName
    return null;
    PrimSig sig = new PrimSig(_name, _parentSig);
    alloy.addToAllSigs(sig);
    sigByName.put(_name, sig);
    return sig;
  }

  /**
   * Create the disjoint alloy fields and return.
   *
   * @param _fieldNamesListWithSameType(List<String>) - the names of fields that have the same type
   * @param _fieldSigTypeName(String) - the field type name
   * @param _ownerSig(PrimSig) - the fields owner signature
   * @return (Fields[]) - the disjoint alloy fields
   */
  protected Sig.Field[] addDisjAlloyFields(
      List<String> _fieldNamesListWithSameType, String _fieldSigTypeName, PrimSig _ownerSig) {

    String[] fieldNames = new String[_fieldNamesListWithSameType.size()];
    _fieldNamesListWithSameType.toArray(fieldNames);

    Sig.Field[] disjointFields = null;
    Sig fieldSigType = sigByName.get(_fieldSigTypeName);
    if (fieldSigType != null) {
      disjointFields = AlloyUtils.addTrickyFields(fieldNames, _ownerSig, fieldSigType);
      if (disjointFields.length != fieldNames.length) {
        return null; // this should not happens unless model is corrupted
      }
    } else return null; // this should not happens unless model is corrupted
    return disjointFields;
  }

  /**
   * Create two expressions to describe what is flowing and add as the alloy's fact list.
   *
   * <pre>
   * For example,
   * {all x: MultipleObjectFlow | all p: x.p1 | p.i in p.outputs}
   * {all x: MultipleObjectFlow | all p: x.p1 | p.outputs in p.in}
   * </pre>
   *
   * @param _ownerSig(Sig) - signature for the fact (i.e., MultipleObjectFlow(class))
   * @param _sigField(Field) - a field(property) of ownerSig(i.e., p1)
   * @param _fieldOfsigFieldType(Field) - a field(property) of sigField type(class) (ie., i of p1's
   *     type signature(class)) = sourceOutputProperty or targetInputProperty
   * @param _inputsOrOutputs(Func) - a function inputs or outputs
   */
  protected void createInFieldExpression(
      Sig _ownerSig, Sig.Field _sigField, Sig.Field _fieldOfsigFieldType, Func _inputsOrOutputs) {
    alloy.addToFacts(
        AlloyExprFactory.exprs_inField(
            _ownerSig, _sigField, _fieldOfsigFieldType, _inputsOrOutputs));
  }

  /**
   * Create the bijection filtered happens before and add as the alloy's fact list. The from and to
   * expression can be just a filed (i.e., p1) or multiple fields connected by plus (p1 + p2 + p3).
   *
   * <pre>
   * For example,
   * fact {all x: AllControl | bijectionFiltered[happensBefore, from, to]}
   * fact {all x: AllControl | bijectionFiltered[happensBefore, x.p1, x.p2 + x.p3]}
   * </pre>
   *
   * @param _ownerSig(Sig) - the owner signature of the created fact
   * @param _from(Expr) - the happensBefore function's from expression
   * @param _to(Expr) - the happensBefore function's to expression
   */
  protected void addBijectionFilteredHappensBeforeFact(Sig _ownerSig, Expr _from, Expr _to) {
    alloy.addToFacts(
        AlloyExprFactory.exprs_bijectionFilteredFactsForSig(
            _ownerSig, _from, _to, Alloy.happensBefore));
  }

  /**
   * Create the bijection filtered happens during and add as the alloy's fact list. The from and to
   * expression can be just a filed (i.e., p1, b.vin) or multiple fields connected by plus (p1 + p2
   * + p3).
   *
   * <pre>
   * For example,
   * fact {all x: UnsatisfiableComposition1 | bijectionFiltered[happensDuring, from, to]}
   * fact {all x: UnsatisfiableComposition1 | bijectionFiltered[happensDuring, x.p3, x.p2]}
   * </pre>
   *
   * @param _ownerSig(Sig) - the owner signature of the created fact
   * @param _from(Expr) - the happensDuring function's from expression
   * @param _to(Expr) - the happensDuring function's to expression
   */
  protected void addBijectionFilteredHappensDuringFact(Sig _ownerSig, Expr _from, Expr _to) {
    alloy.addToFacts(
        AlloyExprFactory.exprs_bijectionFilteredFactsForSig(
            _ownerSig, _from, _to, Alloy.happensDuring));
  }

  /**
   * Create the bijection filtered inputs during and add the alloy's fact list. The from and to
   * expression can be just a filed (i.e., p1, b.vin) or multiple fields connected by plus (p1 + p2
   * + p3).
   *
   * <pre>
   * For example,
   * fact {all x: ParameterBehavior | bijectionFiltered[inputs, from, to]}
   * fact {all x: ParameterBehavior | bijectionFiltered[inputs, x.b, x.b.vin]}
   * </pre>
   *
   * @param _ownerSig(Sig) - the owner signature of the created fact
   * @param _from(Expr) - the bujectionFiltered input function's from expression
   * @param _to(Expr) - the bujectionFiltered input function's to expression
   */
  protected void addBijectionFilteredInputsFact(Sig _ownerSig, Expr _from, Expr _to) {
    alloy.addToFacts(
        AlloyExprFactory.exprs_bijectionFilteredFactsForSig(_ownerSig, _from, _to, Alloy.oinputs));
  }

  /**
   * Create the bijection filtered inputs during and add to the alloy's fact list. The from and to
   * expression can be just a filed (i.e., p1, b.vin) or multiple fields connected by plus (p1 + p2
   * + p3).
   *
   * <pre>
   * For example,
   * fact {all x: B | bijectionFiltered[outputs, from, to]}
   * fact {all x: B | bijectionFiltered[outputs, x.b1, x.b1.vout]}
   * </pre>
   *
   * @param _ownerSig(Sig) - the owner signature of the created fact
   * @param _from(Expr) - the bujectionFiltered output function's from expression
   * @param _to(Expr) - the bujectionFiltered output function's to expression
   */
  protected void addBijectionFilteredOutputsFact(Sig _ownerSig, Expr _from, Expr _to) {
    alloy.addToFacts(
        AlloyExprFactory.exprs_bijectionFilteredFactsForSig(_ownerSig, _from, _to, Alloy.ooutputs));
  }

  /**
   * Creates the function filtered happens before and add to the alloy's fact list.
   *
   * @param _ownerSig(Sig) - the owner signature of the created fact
   * @param _from(Expr) - the functionFiltered function's from expression
   * @param _to(Expr) - the functionFiltered function's to expression
   */
  protected void addFunctionFilteredHappensBeforeFact(Sig _ownerSig, Expr _from, Expr _to) {
    alloy.addToFacts(
        AlloyExprFactory.expr_functionFilteredHappensBeforeForSig(_ownerSig, _from, _to));
  }

  /**
   * Creates the inverse function filtered happens before and add to the alloy's fact list.
   *
   * @param _ownerSig(Sig) - the owner signature of the created fact
   * @param _from(Expr) - the inverseFunctionFiltered function's from expression
   * @param _to(Expr) - the inverseFunctionFiltered function's to expression
   */
  protected void addInverseFunctionFilteredHappensBeforeFact(Sig _ownerSig, Expr _from, Expr _to) {
    alloy.addToFacts(
        AlloyExprFactory.expr_inverseFunctionFilteredHappensBefore(_ownerSig, _from, _to));
  }

  /**
   * Create a equal or greater than equal to the property's lower fact constraint as a fact to
   * sign's field and add to the alloy's fact list. if lower and upper are the same, add the equal
   * fact constraint. if upper is -1, lower is greater than equal to 1,
   *
   * @param _ownerSig(PrimSig) - the owner signature of field
   * @param _field(Field) - the sig's field having the constraint fact
   * @param _lower(int) - The lower (integer) bound of the multiplicity interval.
   * @param _upper(int) - The upper (integer) bound of the multiplicity interval.
   */
  protected void addCardinalityFact(PrimSig _ownerSig, Field _field, int _lower, int _upper) {
    if (_lower == _upper)
      alloy.addToFacts(AlloyExprFactory.expr_cardinalityEqual(_ownerSig, _field, _lower));
    else if (_upper == -1 && _lower >= 1) {
      alloy.addToFacts(
          AlloyExprFactory.expr_cardinalityGreaterThanEqual(_ownerSig, _field, _lower));
    }
  }

  /**
   * Create a equal or greater than equal to the property's lower fact constraint to sign's field
   * and add to the alloy's fact. if lower and upper are the same, add the equal fact constraint. if
   * upper is -1, lower is greater than equal to 1,
   *
   * @param _ownerSig(PrimSig) - the owner signature of field
   * @param _fieldName(String) - the field name the signature having the constraint fact
   * @param _lower(int) - The lower (integer) bound of the multiplicity interval.
   * @param _upper(int) - The upper (integer) bound of the multiplicity interval.
   */
  protected boolean addCardinalityFact(
      PrimSig _ownerSig, String _fieldName, int _lower, int _upper) {

    Sig.Field field = AlloyUtils.getFieldFromSigOrItsParents(_fieldName, _ownerSig); // FoodService
    if (field == null) return false;
    else addCardinalityFact(_ownerSig, field, _lower, _upper);
    return true;
  }

  /**
   * Handling facts for the transfer connector.
   *
   * <pre>
   * for example for 4.1.4 Transfers and Parameters1 - TransferProduct)
   * fact {all x: ParticipantTransfer | bijectionFiltered[sources, x.transferSupplierCustomer, x.supplier]}
   * fact {all x: ParticipantTransfer | bijectionFiltered[targets, x.transferSupplierCustomer, x.customer]}
   * fact {all x: ParticipantTransfer | subsettingItemRuleForSources[x.transferSupplierCustomer]}
   * fact {all x: ParticipantTransfer | subsettingItemRuleForTargets[x.transferSupplierCustomer]}
   *
   * And for the leaf signature only add below facts
   * source:
   * fact {all x: OFSingleFoodService | x.transferOrderPay.items in x.transferOrderPay.sources.orderAmount + x.transferOrderPay.sources.orderedFoodItem}
   * fact {all x: OFSingleFoodService | x.transferOrderPay.sources.orderAmount + x.transferOrderPay.sources.orderedFoodItem in x.transferOrderPay.items}
   * target:
   * fact {all x: OFSingleFoodService | x.transferOrderPay.items in x.transferOrderPay.targets.paidAmount + x.transferOrderPay.targets.paidFoodItem}
   * fact {all x: OFSingleFoodService | x.transferOrderPay.targets.paidAmount + x.transferOrderPay.targets.paidFoodItem in x.transferOrderPay.items}
   * </pre>
   *
   * @param _ownerSig(PrimSig) - the owner signature of the facts added.
   * @param _transfer(Field/Expression) - the transfer field
   * @param _sourceFiledName(String) - the source field name (i.e., supplier)
   * @param _targetFieldName(String) - the target field name (i.e., customer)
   * @param _sourceOutputsAndTargetInputsFields(List<Set<Field>) - list[0] = source output field
   *     set, list[1] = target input field set
   * @param _toBeInherited(boolean) - true if the fact is inherited, then the facts
   * @return (Set<Expr>) - return the leaf signature only facts that will be added to
   *     ConnectorHandler_Transfer's sigToFactsMap instance variable.
   */
  protected Set<Expr> addTransferFacts(
      PrimSig _ownerSig,
      Field _transfer,
      String _sourceFiledName,
      String _targetFieldName,
      List<Set<Field>> _sourceOutputsAndTargetInputsFields,
      boolean _toBeInherited) {

    Field sourceTypeField = AlloyUtils.getFieldFromSigOrItsParents(_sourceFiledName, _ownerSig);
    Field targetTypeField = AlloyUtils.getFieldFromSigOrItsParents(_targetFieldName, _ownerSig);

    Set<Expr> facts = new HashSet<>();
    // only for leaf node
    // fact {all x: OFSingleFoodService | x.transferOrderPay.sources.orderedFoodItem +
    // x.transferOrderPay.sources.orderAmount in x.transferOrderPay.items}
    if (_sourceOutputsAndTargetInputsFields != null) {
      if (_sourceOutputsAndTargetInputsFields.get(0).size() > 0) {
        Set<Expr> factsWithoutSig =
            AlloyExprFactory.exprs_transferInItems(
                _ownerSig, _transfer, Alloy.sources, _sourceOutputsAndTargetInputsFields.get(0));
        facts.addAll(factsWithoutSig);
        if (!_toBeInherited) alloy.addToFacts(AlloyUtils.toSigAllFacts(_ownerSig, factsWithoutSig));
      }
      if (_sourceOutputsAndTargetInputsFields.get(1).size() > 0) {
        Set<Expr> factsWithoutSig =
            AlloyExprFactory.exprs_transferInItems(
                _ownerSig, _transfer, Alloy.targets, _sourceOutputsAndTargetInputsFields.get(1));
        facts.addAll(factsWithoutSig);
        if (!_toBeInherited) alloy.addToFacts(AlloyUtils.toSigAllFacts(_ownerSig, factsWithoutSig));
      }
    }
    alloy.addToFacts(
        AlloyExprFactory.exprs_bijectionFilteredFactsForSig(
            _ownerSig, _transfer, sourceTypeField, Alloy.sources));
    alloy.addToFacts(
        AlloyExprFactory.exprs_bijectionFilteredFactsForSig(
            _ownerSig, _transfer, targetTypeField, Alloy.targets));
    alloy.addToFacts(AlloyExprFactory.exprs_subSettingItemRule(_ownerSig, _transfer));

    return facts;
  }

  /**
   * Handling facts for the TransferBefore connector
   *
   * @param _ownerSig(PrimSig) - the owner signature of the facts added.
   * @param _transfer(Field) - the transfer field (ie., transferbeforeAB)
   * @param _targetFieldName(String) - the target field name (i.e., customer)
   * @param _sourceOutputsAndTargetInputsFields(List<Set<Field>) - list[0] = source output field
   *     set, list[1] = target input field set
   * @param _toBeInherited(boolean) - true if the fact is inherited, then the facts
   * @return (Set<Expr>) - return facts that will be added to ConnectorHandler_Transfer's
   *     sigToFactsMap instance variable.
   */
  protected Set<Expr> addTransferBeforeFacts(
      PrimSig _ownerSig,
      Field _transfer,
      String _sourceFieldName,
      String _targetFieldName,
      List<Set<Field>> _sourceOutputsAndTargetInputsFields,
      boolean _toBeInherited) {

    // factsWithoutAll = isAfterSource[x.transferOrderServe] for
    // fact {all x: OFFoodService | isAfterSource[x.transferOrderServe]}
    Set<Expr> factsWithoutAll =
        addTransferFacts(
            _ownerSig,
            _transfer,
            _sourceFieldName,
            _targetFieldName,
            _sourceOutputsAndTargetInputsFields,
            _toBeInherited);

    Set<Expr> facts = AlloyExprFactory.exprs_isAfterSourceIsBeforeTarget(_ownerSig, _transfer);

    if (!_toBeInherited) {
      // facts above have
      // fact {all x: OFFoodService | isAfterSource[x.transferOrderServe]}
      // fact {all x: OFFoodService | isBeforeTarget[x.transferOrderServe]}
      // but NOT in inherited OFSingleFoodServie like
      // fact {all x: OFSingleFoodServie | isAfterSource[x.transferOrderServe]}
      // fact {all x: OFSingleFoodServie | isBeforeTarget[x.transferOrderServe]}
      factsWithoutAll.addAll(facts);
    }
    // toSigAllFacts make expr like "isAfterSource[x.transferOrderServe]" to "fact {all x:
    // OFFoodService | isAfterSource[x.transferOrderServe]}"
    alloy.addToFacts(AlloyUtils.toSigAllFacts(_ownerSig, facts));
    return factsWithoutAll;
  }

  /**
   * Create no inputs or outputs field facts and add to the alloy's fact list.
   *
   * <pre>
   * for example
   * {all x: TheGivenSig | no x.p4.inputs}
   * {all x: TheGivenSig | no x.p4.outputs}
   * </pre>
   *
   * @param _ownerSig(PrimSig) - the owner signature of the facts added.
   * @param _fieldName(String) - the field name of the signature having the facts
   * @param _inputsOrOutputs(Func) - the inputs or outputs function
   */
  protected void addNoInputsOrOutputsFieldFact(
      PrimSig _ownerSig, String _fieldName, Func _inputsOrOutputs) {
    Sig.Field field = AlloyUtils.getFieldFromSigOrItsParents(_fieldName, _ownerSig);
    alloy.addToFacts(
        AlloyExprFactory.expr_noInputsOrOutputsField(_ownerSig, field, _inputsOrOutputs));
  }

  /**
   * Add fact like "{all x: B1 | x.vin in x.inputs} and {all x: B1 | x.inputs in x.vin} and {all x|
   * no inputs.x}" based on the given addNoOutputsX and addEqual to the alloy's fact list.
   *
   * @param _ownerSig(PrimSig) - the owner signature of the facts added.
   * @param _fields(Set<Field>) - fields of the signatures to describe the fact
   * @param addEquall(boolean) - if true, includes facts like "..in x.outputs" and "x.outputs
   *     in...", if false, not add the fact
   */
  protected void addInInputsAndNoInputXFacts(
      PrimSig _ownerSig, Set<Field> _fields, boolean _addEqual) {

    List<Field> sortedFields = AlloyUtils.sortFields(_fields);
    if (_addEqual)
      // {all x: B1 | x.vin in x.inputs} and {all x: B1 | x.inputs in x.vin}
      alloy.addToFacts(AlloyExprFactory.exprs_in(_ownerSig, sortedFields, Alloy.oinputs));
    // {all x| no inputs.x}
    alloy.addToFacts(AlloyExprFactory.expr_noInputsX(_ownerSig));
  }

  /**
   * Add facts like "{all x: B1 | x.vin in x.outputs} and {all x: B1 | x.outputs in x.vin}" and
   * "{all x| no outputs.x}" for the given signature based on the given addNoOutputsX and addEqual
   * to the alloy's fact list.
   *
   * @param _ownerSig(PrimSig) - the owner signature of the facts added.
   * @param _fields(Set<Field>) - fields of the signatures to describe the fact
   * @param _addEqual(boolean) - if true, includes facts like "..in x.outputs" and "x.outputs
   *     in...", if false, not to includes
   */
  protected void addInOutputsAndNoOutputsXFacts(
      PrimSig _ownerSig, Set<Field> _fields, boolean _addEqual) {

    List<Field> sortedFields = AlloyUtils.sortFields(_fields);
    if (_addEqual)
      // {all x: B1 | x.vout in x.outputs} and {all x: B1 | x.outputs in x.vout}
      alloy.addToFacts(AlloyExprFactory.exprs_in(_ownerSig, sortedFields, Alloy.ooutputs));
    // {all x| no outputs.x}
    alloy.addToFacts(AlloyExprFactory.expr_noOutputsX(_ownerSig));
  }

  /**
   * Add a fact like "fact {all x: OFSingleFoodService | x.prepare.inputs in
   * x.prepare.preparedFoodItem + x.prepare.prepareDestination}" to the alloy's fact list.
   *
   * @param _ownerSig(PrimSig) - the owner signature of the facts added.
   * @param _field(Field) - A field of the given owner signature used to create expression after
   *     "x." to define inputs or outputs.
   * @param _fieldOfFields(Set<Field>) - fields of the field used to create expression after "in".
   * @param _inputsOrOutputs(Func) - A function either Alloy.oinputs or Alloy.ooutputs.
   */
  protected void addInOutClosureFact(
      PrimSig _ownerSig, Field _field, Set<Field> _fieldOfFields, Func _inputsOrOutputs) {
    List<Field> sortedfieldOfFields = AlloyUtils.sortFields(_fieldOfFields);
    alloy.addToFacts(
        AlloyExprFactory.expr_inOutClosure(
            _ownerSig, _field, sortedfieldOfFields, _inputsOrOutputs));
  }

  /**
   * Add a fact like "fact {all x: BuffetService | no y: Transfer | y in x.steps}" when a signature
   * is not in noTransferInXStepsFactSigs, is leaf sig, and has own or inherited fields to the
   * alloy's fact list.
   *
   * @param _noTransferInXStepsFactSigs(Set<Sig>) - A set of signature should not have this fact.
   * @param _leafSigs(Set<PrimSig>) - A set of leaf signatures.
   */
  protected void addNoTransferInXStepsFact(
      Set<Sig> _noTransferInXStepsFactSigs, Set<PrimSig> _leafSigs) {
    Object[] sigs =
        sigByName.values().stream()
            .filter(sig -> !_noTransferInXStepsFactSigs.contains(sig))
            .toArray();
    for (Object sig : sigs) {
      if (_leafSigs.contains(sig) && AlloyUtils.hasOwnOrInheritedFields((PrimSig) sig))
        alloy.addToFacts(AlloyExprFactory.expr_noTransferXSteps((PrimSig) sig));
    }
  }

  /**
   * Add a step closure fact (i.e., fact {all x: Integer | no steps.x}) if the given
   * transferingTypeSig (i.e., Integer) is a leaf Signature to the alloy's fact list.
   *
   * @param _transferingTypeSig(Set<String>) - signature names to be checked
   * @param _leafSigs(Set<PrimSig>) - a set of leaf Signatures
   */
  protected void addStepClosureFact(Set<String> _transferingTypeSig, Set<PrimSig> _leafSigs) {
    for (String sigName : _transferingTypeSig) {
      Sig sig = sigByName.get(sigName);
      if (_leafSigs.contains(sig))
        // fact {all x: Integer | no steps.x}
        alloy.addToFacts(AlloyExprFactory.expr_noStepsX(sig));
    }
  }

  /**
   * Add {no steps},{x.steps in ...}, {... in x.steps} facts to the alloy's fact list.
   *
   * <pre>
   * {no steps} is like fact {all x: AtomicBehavior | no x.steps}
   * {x.steps in ...} is like fact {all x: SimpleSequence | x.steps in x.p1 + x.p2}
   * {... in x.steps} is like fact {all x: SimpleSequence | x.p1 + x.p2 in x.steps}
   * </pre>
   *
   * @param _stepPropertiesBySig(Map<String, Set<String>) - a map (key = signature name, value = a
   *     set of property names) of step properties by signature
   * @param leafSig(Set<PrimSig>) - a set of signature that is leaf
   */
  protected Set<Sig> addStepsFacts(
      Map<String, Set<String>> _stepPropertiesBySig, Set<PrimSig> _leafSigs) {

    Set<Sig> noStepsSigs = new HashSet<>();
    for (String sigName : _stepPropertiesBySig.keySet()) {

      PrimSig sig = sigByName.get(sigName);
      // if leaf Sig do
      if (_leafSigs.contains(sig)) {
        if (_stepPropertiesBySig.get(sigName).size()
            > 0) { // {x.steps in ....} and {... in x.steps} for leaf signature
          alloy.addToFacts(
              AlloyExprFactory.exprs_stepsFields(
                  sig, _stepPropertiesBySig.get(sigName), true, true));
        } else {
          alloy.addToFacts(
              AlloyExprFactory.expr_noXSteps(
                  sig)); // {no steps} facts if leafSig but no stepProperties
          noStepsSigs.add(sig);
        }
      } else if (_stepPropertiesBySig.get(sigName).size()
          > 0) // not leaf signature {.... in x.steps} only
      alloy.addToFacts(
            AlloyExprFactory.exprs_stepsFields(
                sig, _stepPropertiesBySig.get(sigName), true, false));
    }
    return noStepsSigs;
  }

  /**
   * Add a fact like "fact {all x: OFFoodService | x.eat in OFEat }" to the alloy's fact list.
   *
   * @param _ownerSig(PrimSig) - A signature for the face to be defined
   * @param _propertyNameAndType(Map<String, String>) - A map where key is property name string and
   *     value is the property type (Class) name
   */
  protected void addRedefinedSubsettingAsFacts(
      PrimSig _ownerSig, Map<String, String> _propertyNameAndType) {

    for (String pName : _propertyNameAndType.keySet()) {
      alloy.addToFacts(
          AlloyExprFactory.expr_redefinedSubsetting(
              _ownerSig,
              (Field) AlloyUtils.getFieldFromParentSig(pName, _ownerSig),
              sigByName.get(_propertyNameAndType.get(pName))));
    }
  }

  /**
   * Add a equal fact of fields belong to a signature like "fact {all x: B1 | x.vin = x.vout}" to
   * the alloy's fact list.
   *
   * @param _ownerSig(PrimSig) - A signature for the fact to be defined
   * @param _fieldName1(String) - A field name of the Signature to be defined equal.
   * @param _fieldName2(string) - Another field name of the Signature to be defined equal.
   */
  protected void addEqualFact(PrimSig _ownerSig, String _fieldName1, String _fieldName2) {
    Field f1 = AlloyUtils.getFieldFromSigOrItsParents(_fieldName1, _ownerSig);
    Field f2 = AlloyUtils.getFieldFromSigOrItsParents(_fieldName2, _ownerSig);
    if (f1 != null && f2 != null) {
      alloy.addToFacts(AlloyExprFactory.expr_equal(_ownerSig, f1, f2));
    }
  }

  /**
   * Add facts like
   *
   * <pre>
   * {all x: B1 | x.vin in x.inputs}
   * {all x: B1 | x.inputs in x.vin}
   * {all x| no inputs.x}
   * {all x: Integer | no x.inputs}
   * {all x: Integer | no inputs.x}
   * for inputs and facts like
   * {all x: B1 | x.vin in x.outputs}
   * {all x: B1 | x.outputs in x.vin}
   * {all x| no outputs.x}
   * {all x: Integer | no x.outputs}
   * {all x: Integer | no outputs.x}
   * for outputs
   * </pre>
   *
   * to the alloy's fact list.
   *
   * @param _connectorsTargetInputPropertyNamesByClassName(Map<String, Set<String>>) - a dictionary
   *     where a key is a class name and value is the set of target input property names for all
   *     transfer connectors.
   * @param _connectorsSourceOutputPrpertyNamesByClassName(Map<String, Set<String>>) - a dictionary
   *     where a key is a class name and value is the set of source output property names for all
   *     transfer connectors.
   * @param _allSigNames(Set<String>) - all signature names
   * @param _sigNameWithTransferConnectorWithSameInputOutputFieldType(Set<String>) - This variable
   *     is the set of all the signature names where the signature is the type of both
   *     targetOutputProperty and sourceInputProperty of a transfer connector.
   * @param _leafSigs(Set<PrimSig>) - a set of leaf signatures.
   */
  protected void handleNoInputsOutputs(
      Map<String, Set<String>> _connectorsTargetInputPropertyNamesByClassName,
      Map<String, Set<String>> _connectorsSourceOutputPrpertyNamesByClassName,
      Set<String> _allSigNames,
      Set<String> _sigNameWithTransferConnectorWithSameInputOutputFieldType,
      Set<PrimSig> _leafSigs) {

    // find what signatures are flowing in
    Set<String> inputFlowFieldTypes =
        _connectorsTargetInputPropertyNamesByClassName.size() == 0
            ? null
            : getFlowTypeSig(_connectorsTargetInputPropertyNamesByClassName); // Sigs [Integer]
    // find what signatures are flowing out
    Set<String> outputFlowFieldTypes =
        _connectorsSourceOutputPrpertyNamesByClassName.size() == 0
            ? null
            : getFlowTypeSig(_connectorsSourceOutputPrpertyNamesByClassName); // Sigs [Integer]

    for (String sigName : _allSigNames) {
      Sig.PrimSig sig = sigByName.get(sigName);
      // if connector end type are the same, addEqual is true
      boolean addEqual =
          _sigNameWithTransferConnectorWithSameInputOutputFieldType.contains(sigName)
              ? false
              : true;

      // only for leafSigs
      if (!_leafSigs.contains(sig)) continue;
      if ((inputFlowFieldTypes == null || !(inputFlowFieldTypes.contains(sigName)))
          && (outputFlowFieldTypes == null || !(outputFlowFieldTypes.contains(sigName))))
        alloy.addToFacts(AlloyExprFactory.expr_noItemsX(sig));
      // inputs
      if (_connectorsTargetInputPropertyNamesByClassName.keySet().contains(sigName)) {
        Set<String> propertyNames = _connectorsTargetInputPropertyNamesByClassName.get(sigName);

        Set<Field> inputsFields = new HashSet<>();
        for (String propertyName : propertyNames)
          // sig = IFCustomServe, propertyName = IFServe.servedFoodItem
          inputsFields.add(AlloyUtils.getFieldFromSigOrItsParents(propertyName, sig));

        // "{all x: B1 | x.vin in x.inputs} and {all x: B1 | x.inputs in x.vin} and {all x| no
        // inputs.x}
        addInInputsAndNoInputXFacts(sig, inputsFields, addEqual);
      } else {
        if (inputFlowFieldTypes != null
            && inputFlowFieldTypes.contains(sigName)) // Integer = type of what is flowing
          // fact {all x: Integer | no (x.inputs)}
          alloy.addToFacts(AlloyExprFactory.expr_noXInputs(sig));
        else {
          // both "no inputs.x & no x.inputs"
          alloy.addToFacts(AlloyExprFactory.exprs_noInputsXAndXInputs(sig));
        }
      }
      // outputs
      if (_connectorsSourceOutputPrpertyNamesByClassName.keySet().contains(sigName)) {
        Set<String> propertyNames =
            _connectorsSourceOutputPrpertyNamesByClassName.get(sigName); // [vout]

        Set<Field> outputsFields = new HashSet<>();
        for (String propertyName : propertyNames)
          // sig = IFCustomServe, propertyName = IFServe.servedFoodItem
          outputsFields.add(AlloyUtils.getFieldFromSigOrItsParents(propertyName, sig));

        // {all x: B1 | x.vin in x.outputs} and {all x: B1 | x.outputs in x.vin}" and "{all x| no
        // outputs.x}"
        addInOutputsAndNoOutputsXFacts(sig, outputsFields, addEqual);
      } else {
        if (outputFlowFieldTypes != null
            && outputFlowFieldTypes.contains(sigName)) { // Integer = type of what is flowing
          // fact {all x: Integer | no (x.outputs)}
          alloy.addToFacts(AlloyExprFactory.expr_noXOutputs(sig));
        } else {
          // both "no outputs.x & no x.outputs"
          alloy.addToFacts(AlloyExprFactory.exprs_noOutputsXAndXOutputs(sig));
        }
      }
    }
  }

  /**
   * Find what is flowing in the inputs or outputs of connectors from field's type().fold() method and returns.
   *
   * <pre>
   * For example, For 4.1.4 Transfers and Parameters - ParameterBehavior.als, the inputs map is {B2=[vin], B=[vin], C=[vin], B1= [vin] B2=[vout], A=[vout]} and this method returns [Real]. For 4.1.4
   * Transfers and Parameters - TransferProduct.als, the inputs map is {Customer = [reveivedProduct]} and this method returns [Product]. Its output maps is {Supplier =[suppliedProduct]} and this method
   * returns [Product]
   *
   * For 4.2.2 Food Service Object Flow - OFParallelFoodService.als, the inputs map is {OFServe=[servedFoodItem], OFCustomPrepare=[preparedFoodItem, prepareDestination], OFEat=[eatenItem],
   * OFPay=[paidFoodItem, paidAmount], OFCustomServe=[servedFoodItem, serviceDestination]} and this method returns [FoodItem, Real, Location]. Its outputs map is {OFCustomOrder=[orderedFoodItem,
   * orderAmount, orderDestination], OFOrder=[orderedFoodItem], OFCustomPrepare=[preparedFoodItem, prepareDestination], OFPay=[paidFoodItem], OFCustomServe=[servedFoodItem]} and this method returns
   * [FoodItem, Real, Location]
   *
   * <pre>
   *
   * @param inputsOrOutputs(Map<String, set<String>) - a map where key is a signature name string and value is a set of its input or output field names.
   * @return (Set<String>) - A set of signature names of flowing item types.
   */
  protected Set<String> getFlowTypeSig(Map<String, Set<String>> _inputsOrOutputs) {

    Set<String> flowTypeSig = new HashSet<>();
    for (String sigName : _inputsOrOutputs.keySet()) {
      PrimSig sig = getSig(sigName);
      Set<String> fieldNames = _inputsOrOutputs.get(sigName);
      for (String fieldName : fieldNames) {
        Sig.Field f1 = AlloyUtils.getFieldFromSigOrItsParents(fieldName, sig);
        edu.mit.csail.sdg.ast.Type type = f1.type();
        List<List<PrimSig>> folds = type.fold(); // fold contains sig and field's type
        for (List<PrimSig> lp : folds) {
          for (PrimSig s : lp) {
            // when sigName = OFCustomerPrepare, fieldName = prepareFoodItem
            // for s = OFPrepare, the below method return true because OFPrepare is ancestor of
            // OFCustomerPrepare.
            // so its name/label will not be not included in flowTypeSig.
            if (!AlloyUtils.selfOrAncestor(sig, s)) flowTypeSig.add(s.label);
          }
        }
      }
    }
    return flowTypeSig;
  }

  /**
   * Adding facts for the given ownerSig to the alloy's fact list.
   *
   * @param _ownerSig(PrimSig) - A signature for the facts
   * @param _exprs(Set<Expr>) - expressions for facts
   */
  protected void addFacts(String _ownerSigName, Set<Expr> _exprs) {
    alloy.addToFacts(AlloyUtils.toSigAllFacts(sigByName.get(_ownerSigName), _exprs));
  }

  /**
   * Write the Alloy (signatures and facts) as a file.
   *
   * @param _outputFile(File) - A file to be written to be the alloy file.
   * @param _parameterFields(Set<Field>) - a set of Fields having <<Parameter>> stereotype. The
   *     fields with the stereotype can's be disj.
   * @return true if successfully translated otherwise return false
   * @throws FileNotFoundException - happens when the outputFileName is failed to be created (not
   *     exist, not writable etc...)
   */
  protected boolean createAlloyFile(File _outputFile, Set<Field> _parameterFields)
      throws FileNotFoundException {

    if (_outputFile != null && _outputFile.getParentFile().canWrite()) {
      alloy.toFile(_outputFile.getAbsolutePath(), _parameterFields);
      return true;
    }
    return false;
  }
}
