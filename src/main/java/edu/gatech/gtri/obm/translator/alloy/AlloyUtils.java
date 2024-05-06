package edu.gatech.gtri.obm.translator.alloy;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.parser.CompUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.NamedElement;

public class AlloyUtils {

  // TODO? maybe just BehaviorOccurence is ok
  static final List<String> invalidParentNames;

  static {
    invalidParentNames = new ArrayList<>();
    invalidParentNames.add("BehaviorOccurrence");
    invalidParentNames.add("Occurrence");
    invalidParentNames.add("Anything");
  }

  /**
   * check if this sig has a field with <<Parameter>> stereotype.
   *
   * @param sig
   * @param parameterFields
   * @return
   */
  public static boolean hasParameterField(Sig sig, Set<Field> parameterFields) {
    for (Field f : parameterFields) {
      if (f.sig == sig) {
        return true;
      }
    }
    return false;
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

  /**
   * Valid parent.
   *
   * @param parentName the parent name
   * @return true, if successful
   */
  public static boolean validParent(String parentName) {
    // System.out.println(parentName);
    if (parentName == null || invalidParentNames.contains(parentName)) return false;
    else return true;
  }

  public static List<Sig.Field> findFieldWithType(Sig ownerSig, String typeName) {
    List<Sig.Field> ownerSigFields = new ArrayList<>();
    for (Sig.Field field : ownerSig.getFields()) {
      List<List<Sig.PrimSig>> folds = field.type().fold();
      for (List<Sig.PrimSig> typeList : folds) {
        for (Sig.PrimSig type : typeList) {
          if (type.label.equals(typeName)) {
            ownerSigFields.add(field);
          }
        }
      }
    }
    return ownerSigFields;
  }

  /**
   * find if the ownerSig has a field created by connector (ie., transferSupplierCustomer) with type
   * Transfer. If ownerSig has a field like "transferSupplierCustomer: set Transfer" then return
   * true, otherwise return false
   *
   * @param ownerSig sig which is checked to have a transfer field
   * @return true or false
   */
  public static boolean hasTransferField(Sig ownerSig) {
    for (Sig.Field field : ownerSig.getFields()) {
      if (field.label.startsWith("transfer")) { // transferSupplierCustomer
        java.util.List<java.util.List<Sig.PrimSig>> folds = field.type().fold();
        for (java.util.List<Sig.PrimSig> typeList : folds) { // [TransferProduct, o/BinaryLink] when
          // ownerSig == TransferProduct
          for (Sig.PrimSig type : typeList) {
            if (type.children().contains(Alloy.transferSig)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public static Sig.Field getFieldFromSig(String fieldNameLookingFor, PrimSig sig) {
    for (Sig.Field field : sig.getFields()) {
      if (field.label.equals(fieldNameLookingFor)) return field;
    }
    return null;
  }

  /**
   * Find Field from sig by fieldName. If not find in the sig, try to find in its parent
   * recursively.
   *
   * @param fieldNameLookingFor field's name looking for
   * @param sig PrimSig sig supposed to having the field
   * @return Field if found, otherwise return null
   */
  public static Sig.Field getFieldFromSigOrItsParents(String fieldNameLookingFor, PrimSig sig) {
    for (Sig.Field field : sig.getFields()) {
      if (field.label.equals(fieldNameLookingFor)) return field;
    }
    while (sig.parent != null) { // SingleFoodService -> FoodService -> this/Occurrence -> univ ->
      // null
      // System.out.println(sig.parent);
      Field field = getFieldFromSigOrItsParents(fieldNameLookingFor, sig.parent);
      if (field != null) return field;
      else {
        sig = sig.parent; // reset
      }
    }
    return null;
  }

  public static Sig.Field getFieldFromParentSig(String fieldNameLookingFor, PrimSig sig) {
    while (sig.parent != null) { // SingleFoodService -> FoodService -> this/Occurrence -> univ ->
      // null
      // System.out.println(sig.parent);
      Field field = getFieldFromSigOrItsParents(fieldNameLookingFor, sig.parent);
      if (field != null) return field;
      else {
        sig = sig.parent; // reset
      }
    }
    return null;
  }

  // sig.domain(sigField) or parentSig.domain(parentSigField)
  public static Expr getSigDomainFileld(String fieldNameLookingFor, PrimSig sig) {
    for (Sig.Field field : sig.getFields()) { // getFields does not include redefined fields
      if (field.label.equals(fieldNameLookingFor)) return sig.domain(field);
    }
    while (sig.parent != null) { // SingleFoodService -> FoodService -> this/Occurrence -> univ ->
      // null
      Field field = getFieldFromSigOrItsParents(fieldNameLookingFor, sig.parent);
      if (field != null)
        // return sig.domain(field);
        // return sig.parent.domain(field);
        return field;
      else {
        sig = sig.parent; // reset
      }
    }
    return null;
  }

  public static Expr getSigOwnField(String fieldNameLookingFor, PrimSig sig) {
    for (Sig.Field field : sig.getFields()) { // getFields does not include redefined fields
      if (field.label.equals(fieldNameLookingFor)) return sig.domain(field);
    }
    return null;
  }

  // Assume only one field with the same type
  // not searching through inherited fields
  public static Sig.Field getFieldFromSigByFieldType(String fieldTypeName, PrimSig sig) {
    // 4.1.4 Transfers and Parameters1 - TransferProduct_modified
    // Sig =PatifipantTransfer, String fieldType = Supplier
    for (Sig.Field field : sig.getFields()) {
      java.util.List<java.util.List<Sig.PrimSig>> folds = field.type().fold();
      for (java.util.List<Sig.PrimSig> fold : folds) {
        // fold = [PaticipantTransfer, Custome]
        if (fold.get(fold.size() - 1).label.equals(fieldTypeName)) // last one
        return field;
      }
    }
    return null;
  }

  public static void addAllReachableSig(Module m, List<Sig> allSigs) {
    for (Iterator<Sig> iter = m.getAllReachableSigs().iterator(); iter.hasNext(); ) {
      allSigs.add(iter.next());
    }
  }

  public static Field getReachableSigField(Module m, Sig sig, String fieldLabel) {

    for (Field f : sig.getFields()) {
      System.out.println(f.label);
      if (f.label.equals(fieldLabel)) {
        return f;
      }
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
      if (s.label.equals(lookingFor)) return s;
    }
    return null;
  }

  public static Module getAllReachableModuleByName(Module module, String lookingForModuleName) {

    for (Module m : module.getAllReachableModules()) {
      if (m.getModelName().equals(lookingForModuleName)) return m;
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
      if (f.label.equals(lookingForFunctionLabel)) return f;
    }
    return null;
  }

  public static Set<Field> getInheritedFields(
      PrimSig sig,
      HashMap<String, Set<String>> inputsOrOutputsPersig,
      Map<String, NamedElement> namedElementsBySigName) {

    List<Class> classInHierarchy =
        MDUtils.createListIncludeSelfAndParents((Class) namedElementsBySigName.get(sig.label));

    Set<Field> inputsOrOutputsFields = new HashSet<>();
    for (int i = 0; i < classInHierarchy.size() - 1; i++) {
      Set<String> fieldNames = inputsOrOutputsPersig.get(classInHierarchy.get(i).getName());
      if (fieldNames != null)
        for (String fieldName : fieldNames)
          inputsOrOutputsFields.add(AlloyUtils.getFieldFromSigOrItsParents(fieldName, sig));
    }

    return inputsOrOutputsFields;
  }

  /**
   * Sort Fields based on its label alphabetically
   *
   * @param fields - the sig fields to be sorted
   * @return sorted List<Field>
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
   * Convert Set<Fileld> to Set<String> of field.label
   *
   * @param fields - the sig fields to be formatted as Set<String> of its label
   * @return Set<String> of fields's label
   */
  public static Set<String> fieldsLabels(Set<Field> fields) {
    return fields.stream().map(e -> e.label).collect(Collectors.toSet());
  }
}
