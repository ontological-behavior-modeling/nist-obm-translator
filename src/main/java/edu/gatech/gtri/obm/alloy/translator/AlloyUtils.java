package edu.gatech.gtri.obm.alloy.translator;

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

/**
 * A utility class for Alloy Fields,
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 *
 */
public class AlloyUtils {


  final static List<String> invalidParentNames;
  static {
    invalidParentNames = new ArrayList<>();
    invalidParentNames.add("BehaviorOccurrence");
    invalidParentNames.add("Occurrence");// it is valid
    invalidParentNames.add("Anything");
  }

  /**
   * Create a field and return
   * 
   * sig ownerSig { label: set sigType }
   *
   * @param fieldLabel the field name
   * @param ownerSig the Sig the field belong to
   * @param sigType the type of field
   * @return a created field
   */
  protected static Sig.Field addField(String fieldLabel, Sig ownerSig, Sig sigType) {
    return ownerSig.addField(fieldLabel, sigType.setOf());
  }

  /**
   * Create an transfer field and return.
   *
   * @param fieldLabel the field name
   * @param ownerSig the Sig that the field belong to
   * @return a created field
   */
  protected static Field addTransferField(String fieldLabel, Sig ownerSig) {
    return addField(fieldLabel, ownerSig, Alloy.transferSig);
  }



  /**
   * Create the tricky fields (disj) and return
   * 
   * sig ownerSig { disj field0, field1: set sigType}
   *
   * @param fieldNames the filed names
   * @param ownerSig the Sig that the fields belong to
   * @param sigType the type of fields
   * @return created fields
   */
  protected static Sig.Field[] addTrickyFields(java.lang.String[] fieldNames, Sig ownerSig,
      Sig sigType) {
    // 3rd parameter is isDisjoint but does not affect to write out as disj
    return ownerSig.addTrickyField(null, null, null, null, null, fieldNames, sigType.setOf());
  }

  /**
   * Import alloy module.
   *
   * @param f the f
   * @return the comp module
   */
  public static CompModule importAlloyModule(File f) {
    return AlloyUtils.importAlloyModule(f.getAbsolutePath());
  }

  /**
   * Import alloy module.
   *
   * @param absoluteFileName the absolute file name
   * @return the comp module
   */
  public static CompModule importAlloyModule(String absoluteFileName) {
    return CompUtil.parseEverything_fromFile(new A4Reporter(), null, absoluteFileName);
  }

  /**
   * Removes the slash.
   *
   * @param sig the sig
   * @return the string
   */
  public static String removeSlash(String sig) {
    if (sig.contains("/")) {
      int index = sig.lastIndexOf('/');
      return sig.substring(index + 1, sig.length());
    }

    return sig;
  }

  public static boolean selfOrAncestor(PrimSig sig, Sig lookingFor) {
    if (sig == lookingFor)
      return true;
    else if (sig.parent != null) {
      return selfOrAncestor(sig.parent, lookingFor);
    }
    return false;
  }

  /**
   * Valid parent.
   *
   * @param parentName the parent name
   * @return true, if successful
   */
  public static boolean validParent(String parentName) {
    if (parentName == null || invalidParentNames.contains(parentName))
      return false;
    else
      return true;
  }

  public static boolean hasOwnOrInheritedFields(PrimSig sig) {
    if (sig.getFields().size() > 0)
      return true;
    while (sig.parent != null) {
      if (sig.parent.getFields().size() > 0)
        return true;
      else
        sig = sig.parent;
    }
    return false;
  }


  /**
   * Find Field from sig by fieldName. If not find in the sig, try to find in its parent recursively.
   * 
   * @param fieldNameLookingFor field's name looking for
   * @param sig PrimSig sig supposed to having the field
   * @return Field if found, otherwise return null
   */
  public static Sig.Field getFieldFromSigOrItsParents(String fieldNameLookingFor, PrimSig sig) {
    for (Sig.Field field : sig.getFields()) {
      if (field.label.equals(fieldNameLookingFor))
        return field;
    }
    while (sig.parent != null) { // SingleFoodService -> FoodService -> this/Occurrence -> univ ->
                                 // null
      Field field = getFieldFromSigOrItsParents(fieldNameLookingFor, sig.parent);
      if (field != null)
        return field;
      else {
        sig = sig.parent; // reset
      }
    }
    return null;
  }

  public static Sig.Field getFieldFromParentSig(String fieldNameLookingFor, PrimSig sig) {
    while (sig.parent != null) { // SingleFoodService -> FoodService -> this/Occurrence -> univ ->
                                 // null
      Field field = getFieldFromSigOrItsParents(fieldNameLookingFor, sig.parent);
      if (field != null)
        return field;
      else {
        sig = sig.parent; // reset
      }
    }
    return null;
  }

  // sig.domain(sigField) or parentSig.domain(parentSigField)
  public static Expr getSigDomainField(String fieldNameLookingFor, PrimSig sig) {
    for (Sig.Field field : sig.getFields()) { // getFields does not include redefined fields
      if (field.label.equals(fieldNameLookingFor))
        return sig.domain(field);
    }
    while (sig.parent != null) { // SingleFoodService -> FoodService -> this/Occurrence -> univ ->
                                 // null
      Field field = getFieldFromSigOrItsParents(fieldNameLookingFor, sig.parent);
      if (field != null)
        return field;
      else {
        sig = sig.parent; // reset
      }
    }
    return null;
  }

  public static Expr getSigOwnField(String fieldNameLookingFor, PrimSig sig) {
    for (Sig.Field field : sig.getFields()) { // getFields does not include redefined fields
      if (field.label.equals(fieldNameLookingFor))
        return sig.domain(field);
    }
    return null;
  }



  /**
   * Gets a Signature lookingFor, in the Module m if it exists.
   *
   * @param m = the module
   * @param lookingFor = the name of the Signature
   * @return the Signature or null if it doesn't exist
   */
  public static Sig getReachableSig(Module m, String lookingFor) {
    for (Sig s : m.getAllReachableSigs()) {
      if (s.label.equals(lookingFor))
        return s;
    }
    return null;
  }

  public static Module getAllReachableModuleByName(Module module, String lookingForModuleName) {

    for (Module m : module.getAllReachableModules()) {
      if (m.getModelName().equals(lookingForModuleName))
        return m;
    }
    return null;
  }

  /**
   * Get a function in module based on label
   * 
   * @param module
   * @param lookingForFunctionLabel
   * @return function
   */
  public static Func getFunction(Module module, String lookingForFunctionLabel) {
    for (Func f : module.getAllFunc()) {
      if (f.label.equals(lookingForFunctionLabel))
        return f;
    }
    return null;
  }


  /**
   * Sort Fields based on its label alphabetically
   * 
   * @param fields the set of fields to be sorted
   * @return sorted list of fields
   */
  public static List<Field> sortFields(Set<Field> fields) {
    List<Field> sortedFields = new ArrayList<>(fields);
    Collections.sort(sortedFields, new Comparator<Field>() {
      public int compare(Field o1, Field o2) {
        return (o1.label).compareTo(o2.label);
      }
    });
    return sortedFields;
  }

  /**
   * Sort the strings alphabetically
   * 
   * @param strings the set of strings to be sorted
   * @return sorted list of strings
   */
  public static List<String> sort(Set<String> strings) {
    List<String> sortedStrings = new ArrayList<>();
    for (String s : strings)
      sortedStrings.add(s);
    Collections.sort(sortedStrings);
    return sortedStrings;
  }



  /**
   * Convert Set<Fileld> to Set<String> of field.label
   * 
   * @param fields - the sig fields to be formatted as Set<String> of its label
   * @return Set<String> of fields's label
   */
  public static Set<String> fieldsLabels(Set<Field> fields) {
    return fields.stream().map(e -> e.label).collect(Collectors.toSet());
  }

  public static Set<Expr> toSigAllFacts(Sig ownerSig, Set<Expr> exprs) {
    Decl decl = AlloyExprFactory.makeDecl(ownerSig);
    Set<Expr> rAll = new HashSet<>();
    for (Expr expr : exprs) {
      rAll.add(expr.forAll(decl));
    }
    return rAll;
  }

  /**
   * support when Expr original is ExprBinary(ie., p1 + p2) to add ExprVar s in both so returns s.p1 and s.p2. if original is like "BuffetService <: (FoodService <: eat)" -> ((ExprBinary) original).op =
   * "<:", in this case just return s.join(original) =
   * 
   * @param s
   * @param original
   * @return
   */
  protected static Expr addExprVarToExpr(ExprVar s, Expr original) {
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

  /**
   * Return boolean if the map not contain both the given key and the given value
   * 
   * @param map key = Field, values = Set of Fields
   * @param key key(Field) to be checked
   * @param value value(Field) to be checked
   * @return true if both the given key and the given value is not in the map, otherwise return false
   */
  protected static boolean notContainBothKeyAndValue(Map<Field, Set<Field>> map, Field key,
      Field value) {
    return map.containsKey(key) ? (map.get(key).contains(value) ? false : true) : true;
  }


  /**
   * Get two rules (ConnectorEnds) of each one of constraint using omgutils.SysMLAdapter and return as set.
   * 
   * @param cs a set of Constraints
   * @return set of oneof constraint's two rules (ConnectorEnd)
   */
  protected static Set<EList<Element>> getOneOfRules(SysMLAdapter sysmladapter,
      Set<Constraint> cs) {
    Set<EList<Element>> oneOfSet = new HashSet<>();
    for (Constraint c : cs) {
      ValueSpecification vs = c.getSpecification();
      if (vs instanceof OpaqueExpressionImpl) {
        edu.umd.omgutil.uml.OpaqueExpression omgE = (OpaqueExpression) sysmladapter.mapObject(vs);
        if (omgE.getBodies().contains("OneOf")) {
          EList<Element> es = c.getConstrainedElements(); // list of connectorEnds
          oneOfSet.add(es);
        }
      }
    }
    return oneOfSet;
  }
}
