package edu.gatech.gtri.obm.alloy.translator;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprBinary;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.uml.OpaqueExpression;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.internal.impl.OpaqueExpressionImpl;

/**
 * A utility class related Alloy objects
 *
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 */
public class AlloyUtils {

  /**
   * A list of invalid parent names to be created by translation. The list parents are already
   * created when the library is loaded.
   */
  static final List<String> invalidUserDefinedParentNames;

  static {
    invalidUserDefinedParentNames = new ArrayList<>();
    invalidUserDefinedParentNames.add("BehaviorOccurrence");
    invalidUserDefinedParentNames.add("Occurrence");
    invalidUserDefinedParentNames.add("Anything");
  }

  /**
   * Create a field and return. Note: The _fieldLabel is set of relations in _ownerSig -> _sigType.
   *
   * <p>sig _ownerSig { _fieldLable: set _sigType }
   *
   * @param _fieldLabel (String) - the field name to be created in the given _ownerSig
   * @param _ownerSig (Sig) - the owner of a field to be created
   * @param _sigType (Sig) - the field type of a field to be created
   * @return (Field) - the created field
   */
  protected static Sig.Field addField(String _fieldLabel, Sig _ownerSig, Sig _sigType) {
    return _ownerSig.addField(_fieldLabel, _sigType.setOf());
  }

  /**
   * Create a transfer field and return.
   *
   * @param _fieldLabel (String) - the field name to be created in the given _ownerSig
   * @param _ownerSig (Sig) - the owner of a field to be created
   * @return (Field) - the created field
   */
  protected static Field addTransferField(String _fieldLabel, Sig _ownerSig) {
    return addField(_fieldLabel, _ownerSig, Alloy.transferSig);
  }

  /**
   * Create the tricky fields (disj) and return
   *
   * <p>sig ownerSig { disj field0, field1: set sigType}
   *
   * <p>disj is the predicate "field0 and field1 share no elements in common".
   *
   * @param fieldNames (String[]) - the field names to be created
   * @param _ownerSig (Sig) - the signature that the created fields belong to
   * @param _sigType (Sig) - the type of fields
   * @return (Field[]) - the created fields
   */
  protected static Sig.Field[] addTrickyFields(String[] _fieldNames, Sig _ownerSig, Sig _sigType) {
    // 3rd parameter is isDisjoint but does not affect to write out as disj
    return _ownerSig.addTrickyField(null, null, null, null, null, _fieldNames, _sigType.setOf());
  }

  /**
   * Create a CompModule object having signatures and facts from an alloy file.
   *
   * <p><img src="doc-files/AlloyUtils_importAlloyModulef.svg"/>
   *
   * @param _alloyFile (File) - an alloy file (*.als)
   * @return (CompModule) - the created CompModule
   */
  public static CompModule importAlloyModule(File _alloyFile) {
    return AlloyUtils.importAlloyModule(_alloyFile.getAbsolutePath());
  }

  /**
   * Create a CompModule object having signatures and facts from an alloy absolute filename
   *
   * <p><img src="doc-files/AlloyUtils_importAlloyModules.svg"/>
   *
   * @param _absoluteAlloyFileName (String) - an absolute filename for an alloy file (*.als)
   * @return (CompModule) - the created CompModule
   */
  public static CompModule importAlloyModule(String _absoluteAlloyFileName) {
    return CompUtil.parseEverything_fromFile(new A4Reporter(), null, _absoluteAlloyFileName);
  }

  /**
   * Check if the _lookingFor signature is the _sig signature or _sig's ancestor.
   *
   * <p><img src="doc-files/AlloyUtils_selfOrAncestor.svg"/>
   *
   * @param _sig (Sig) - the signature to be compared with the _lookingFor signature
   * @param _lookingFor (Sig) - the _lookingFor signature
   * @return (boolean) true if the _lookingFor signature is _sig or _sig's ancestor, otherwise
   *     return false.
   */
  public static boolean selfOrAncestor(PrimSig _sig, Sig _lookingFor) {
    if (_sig == _lookingFor) return true;
    else if (_sig.parent != null) {
      return selfOrAncestor(_sig.parent, _lookingFor);
    }
    return false;
  }

  /**
   * Check if the given _parentSigName signature is valid to be created. Make sure not have the same
   * name as the library defined signatures.
   *
   * <p><img src="doc-files/AlloyUtils_isValidUserDefineParent.svg"/>
   *
   * @param _parentSigName (String) - the signature name to be validated.
   * @return (boolean) true if the given _parentSigName is valid signature to be created, otherwise
   *     return false.
   */
  public static boolean isValidUserDefineParent(String _parentSigName) {
    if (_parentSigName == null || invalidUserDefinedParentNames.contains(_parentSigName))
      return false;
    else return true;
  }

  /**
   * Check if the given <code>Sig</code> has at least one field or not.
   *
   * <p><img src="doc-files/AlloyUtils_hasOwnOrInheritedFields.svg"/>
   *
   * @param _sig (PrimSig) - a signature checked
   * @return (boolean) - true if the given signature has a field or its ancestor has a field.
   */
  public static boolean hasOwnOrInheritedFields(PrimSig _sig) {
    if (_sig.getFields().size() > 0) return true;
    while (_sig.parent != null) {
      if (_sig.parent.getFields().size() > 0) return true;
      else _sig = _sig.parent;
    }
    return false;
  }

  /**
   * Return a field as <code>Expr</code> of the given name/label in the given signature. If not
   * found return null.
   *
   * <p><img src="doc-files/AlloyUtils_getFieldFromSig.svg"/>
   *
   * @param _fieldNameLookingFor (String) - the field name/label looking for.
   * @param _sig (PrimSig) - the signature checked for having the field
   * @return (Expr) - a field if found, otherwise return null
   */
  public static Expr getFieldFromSig(String _fieldNameLookingFor, PrimSig _sig) {
    for (Sig.Field field : _sig.getFields()) { // getFields does not include redefined fields
      if (field.label.equals(_fieldNameLookingFor)) return field;
    }
    return null;
  }

  /**
   * Return a field as <code>Expr</code> of the given name/label in the given signature's ancestor.
   * If not found return null.
   *
   * <p><img src="doc-files/AlloyUtils_getFieldFromParentSig.svg"/>
   *
   * @param _fieldNameLookingFor (String) - the field name/label looking for.
   * @param _sig (PrimSig) - the signature checked for having the field
   * @return (Expr) - a field if found, otherwise return null
   */
  public static Expr getFieldFromParentSig(String _fieldNameLookingFor, PrimSig _sig) {
    while (_sig.parent != null) { // SingleFoodService -> FoodService -> this/Occurrence -> univ ->
      // null
      Expr field = getFieldFromSig(_fieldNameLookingFor, _sig.parent);
      if (field != null) return field;
      else {
        _sig = _sig.parent; // reset
      }
    }
    return null;
  }

  /**
   * Return a field as <code>Expr</code> of the given name/label in the given signature or its
   * ancestor. If not found return null.
   *
   * <p><img src="doc-files/AlloyUtils_getFieldAsExprFromSigOrItsParents.svg"/>
   *
   * @param _fieldNameLookingFor (String) - the field name/label looking for.
   * @param _sig (PrimSig) - the signature checked for having the field
   * @return (Expr) - a field if found, otherwise return null
   */
  public static Expr getFieldAsExprFromSigOrItsParents(String _fieldNameLookingFor, PrimSig _sig) {
    Expr field = getFieldFromSig(_fieldNameLookingFor, _sig);
    return field != null ? field : getFieldFromParentSig(_fieldNameLookingFor, _sig);
  }

  /**
   * Return a field as <code>Field</code> of the given name/label in the given signature or its
   * ancestor. If not found return null.
   *
   * <p><img src="doc-files/AlloyUtils_getFieldFromSigOrItsParents.svg"/>
   *
   * @param _fieldNameLookingFor (String) - the field name/label looking for.
   * @param _sig (PrimSig) - the signature checked for having the field
   * @return (Field) - a field if found, otherwise return null
   */
  public static Field getFieldFromSigOrItsParents(String _fieldNameLookingFor, PrimSig _sig) {
    return (Field) getFieldAsExprFromSigOrItsParents(_fieldNameLookingFor, _sig);
  }

  /**
   * Return a <code>Sig</code> in the given <code>Module</code> by name/label if exists, otherwise
   * return null.
   *
   * <p><img src="doc-files/AlloyUtils_getReachableSig.svg"/>
   *
   * @param _sigNameLookingFor (String) - the signature name/label looking for.
   * @param _module (Module) - the module the given signature might be in.
   * @return the found <code>Sig</code> or null if it doesn't exist.
   */
  public static Sig getReachableSig(String _sigNameLookingFor, Module _module) {
    for (Sig s : _module.getAllReachableSigs()) {
      if (s.label.equals(_sigNameLookingFor)) return s;
    }
    return null;
  }

  /**
   * Return a <code>Module</code> reachable from the given <code>Module</code> if exists, otherwise
   * return null.
   *
   * <p><img src="doc-files/AlloyUtils_getAllReachableModuleByName.svg"/>
   *
   * @param _moduleNameLookingFor (String) - the module name looking for.
   * @param _module (Module) - the base module to search all reachable modules from.
   * @return (Module) - the found <code>Module</code> or null if does't exist.
   */
  public static Module getAllReachableModuleByName(String _moduleNameLookingFor, Module _module) {
    for (Module m : _module.getAllReachableModules()) {
      if (m.getModelName().equals(_moduleNameLookingFor)) return m;
    }
    return null;
  }

  /**
   * Return a <code>Func</code> available from the given <code>Module</code> if exists, otherwise
   * return null.
   *
   * <p><img src="doc-files/AlloyUtils_getFunction.svg"/>
   *
   * @param _functionLabelLookingFor (String) - the function label looking for.
   * @param _module (Module) - the module to search the function.
   * @return (Func) = the found <code>Func</code> or null if does't exist.
   */
  public static Func getFunction(String _functionLabelLookingFor, Module _module) {
    for (Func f : _module.getAllFunc()) {
      if (f.label.equals(_functionLabelLookingFor)) return f;
    }
    return null;
  }

  /**
   * Sort the given fields alphabetically based on their labels and return
   *
   * <p><img src="doc-files/AlloyUtils_sortFields.svg"/>
   *
   * @param fields (Set<Field>)- the fields to be sorted.
   * @return (List<Field>) - sorted fields.
   */
  public static List<Field> sortFields(Set<Field> fields) {
    List<Field> sortedFields = new ArrayList<>(fields);
    Collections.sort(
        sortedFields,
        new Comparator<Field>() {
          public int compare(Field o1, Field o2) {
            return (o1.label).compareTo(o2.label);
          }
        });
    return sortedFields;
  }

  /**
   * Sort the given strings alphabetically and return.
   *
   * <p><img src="doc-files/AlloyUtils_sort.svg"/>
   *
   * @param strings (Set<String>) - the set of strings to be sorted
   * @return (List<String>) - the sorted strings.
   */
  public static List<String> sort(Set<String> strings) {
    List<String> sortedStrings = new ArrayList<>();
    for (String s : strings) sortedStrings.add(s);
    Collections.sort(sortedStrings);
    return sortedStrings;
  }

  /**
   * Return the set of field names/labels for the given fields.
   *
   * <p><img src="doc-files/AlloyUtils_fieldsLabels.svg"/>
   *
   * @param fields (Set<Field>) - the fields to be converted to names/labels.
   * @return (Set<String>) - the names/labels of the given fields
   */
  public static Set<String> fieldsLabels(Set<Field> fields) {
    return fields.stream().map(e -> e.label).collect(Collectors.toSet());
  }

  /**
   * Modify each given expression by adding the given signature declaration and return.
   *
   * <pre>
   * For example,
   * an expression <code>"bijectionFiltered[outputs, x.a, x.a.vout]"</code> is modified to
   * <code>"{all x: ParameterBehavior | bijectionFiltered[outputs, x.a, x.a.vout]}<code>.
   * </pre>
   *
   * <p><img src="doc-files/AlloyUtils_toSigAllFacts.svg"/>
   *
   * @param _ownerSig (Sig) - the signature for expressions
   * @param _exprs (Set<Expr>) - the expressions to be modified
   * @return (Set<Expr>) - the modified expressions
   */
  public static Set<Expr> toSigAllFacts(Sig _ownerSig, Set<Expr> _exprs) {
    Decl decl = AlloyExprFactory.makeDecl(_ownerSig);
    Set<Expr> rAll = new HashSet<>();
    for (Expr expr : _exprs) {
      rAll.add(expr.forAll(decl));
    }
    return rAll;
  }

  /**
   * Modify the given <code>Expr</code> using the given <code>ExprVar</code>. Only support the given
   * <code>Expr</code> to be <code>Expr</code> or <code>ExprBinary</code>.
   *
   * <p>For example, if the given <code>Expr</code> is <code>ExprBinary</code> of <b> p1 + p2</b>,
   * then join with the given <code> ExprVar</code> <b>s</b> and plus the two binary left and right
   * expression as <b>s.p1 + s.p2</b>.
   *
   * <p>If the given <code>Expr</code> is <code>Expr</code>, then just join the <code>ExprVar</code>
   * with <code>Expr</code> (i.e., p1 to s.p1)
   *
   * @param _original (Expr) - the original expression to be modified.
   * @param _s (ExprVar) - a expression variable to be used to modify.
   * @return (Expr) - the modified expression
   */
  protected static Expr addExprVarToExpr(Expr _original, ExprVar _s) {
    if (_original instanceof ExprBinary) {
      Expr left = addExprVarToExpr(((ExprBinary) _original).left, _s);
      Expr right = addExprVarToExpr(((ExprBinary) _original).right, _s);
      if (((ExprBinary) _original).op == ExprBinary.Op.PLUS) return left.plus(right);
      else return _s.join(_original); // x . BuffetService <: (FoodService <: eat) where original =
      // "BuffetService <: (FoodService <: eat)" with ((ExprBinary)
      // original).op = "<:"
    } else {
      return _s.join(_original); // x.BuffetService
    }
  }

  /**
   * Return boolean true if the given map NOT contain both the given key and the value, otherwise
   * return false.
   *
   * @param _map (Map<Field, Set<Field>) - the map to be checked.
   * @param _key (Field) - the field to be checked to be as the map's key.
   * @param _value (Field) - the field to be checked to be in the map's value.
   * @return (boolean) - true if both the given key and the given value is not in the map, otherwise
   *     return false.
   */
  protected static boolean notContainBothKeyAndValue(
      Map<Field, Set<Field>> _map, Field _key, Field _value) {
    return _map.containsKey(_key) ? (_map.get(_key).contains(_value) ? false : true) : true;
  }

  /**
   * The "Oneof" <code>Constraint</code> applies to two or more <code>ConnectorEnd(Element)<code>s.
   * This method finds the <code>ConnectorEnd/Element</code> list for each given <code>Constraint</code> and then return as a <code>Set</code>.
   *
   * @param _constraints (Set<Constraint>) - a set of constraints may have the "OneOf" constraint.
   * @param __sysmladapter (SysMLAdapter) - the omgutil's helper class for finding the necessary information for this method.
   * @return (Set<EList<Element>>) - a set of the list of <code>ConnectorEnds/Elements</code>.
   */
  protected static Set<EList<Element>> getOneOfRules(
      Set<Constraint> _constraints, SysMLAdapter _sysmladapter) {
    Set<EList<Element>> oneOfSet = new HashSet<>();
    for (Constraint c : _constraints) {
      ValueSpecification vs = c.getSpecification();
      if (vs instanceof OpaqueExpressionImpl) {
        edu.umd.omgutil.uml.OpaqueExpression omgE = (OpaqueExpression) _sysmladapter.mapObject(vs);
        if (omgE.getBodies().contains("OneOf")) {
          EList<Element> es = c.getConstrainedElements(); // Element = ConnectorEnd
          oneOfSet.add(es);
        }
      }
    }
    return oneOfSet;
  }

  /**
   * String utility - removes before "/" string if the given string contained "/" and return. This
   * is used when string is like "this/Occurrence" to remove "this/" portion and return
   * "Occurrence".
   *
   * <p><img src="doc-files/AlloyUtils_removeSlash.svg"/>
   *
   * @param _string (String) - a string to be checked
   * @return (String) - the returning string
   */
  public static String removeSlash(String _string) {
    if (_string.contains("/")) {
      int index = _string.lastIndexOf('/');
      return _string.substring(index + 1, _string.length());
    }
    return _string;
  }
}
