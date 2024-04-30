package edu.gatech.gtri.obm.translator.alloy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.NamedElement;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.ToAlloy;
import edu.mit.csail.sdg.ast.Sig.PrimSig;

public class MDUtils {

  public static Set<PrimSig> toSigs(Set<NamedElement> nes, ToAlloy toAlloy) {
    Set<PrimSig> sigs = new HashSet<>();
    for (NamedElement ne : nes) {
      sigs.add(toAlloy.getSig(ne.getName()));
    }
    return sigs;
  }

  public static Set<NamedElement> findLeafClass(Set<NamedElement> allNEs) {
    Set<NamedElement> leafClasses = new HashSet<>();
    Set<Classifier> parentClasses = new HashSet<>();
    for (NamedElement ne : allNEs) {
      System.out.println(ne.getName());
      leafClasses.add(ne);
      if (ne instanceof Class) {
        List<Classifier> parents = ((Class) ne).getGenerals();
        parentClasses.addAll(parents);
      }
    }
    leafClasses.removeAll(parentClasses);
    return leafClasses;
  }



  /**
   * Get class in hierarchy order in list but not include "BehaviorOccurence" or "Occurrence".
   * Smaller the index, more ancestor. For example, for 4.2.1 FoodService Control Flow -
   * BuffetService.als, [0] = Food Service, [1] = SingleFoodService, and [2] = BuffetService where
   * mainClass passed if for BuffertService
   * 
   * @param aClass
   * @return
   */
  public static List<Class> createListIncludeSelfAndParents(Class aClass) {
    List<Class> list = new ArrayList<Class>();
    list.add(aClass);
    while ((aClass = getParent(aClass)) != null) {
      list.add(0, aClass);
    }
    return list;
  }

  /**
   * return a parent of aClass. Assume only one parent (Alloy allows only one parent to be defined).
   * 
   * @param aClass
   * @return
   */
  private static Class getParent(Class aClass) {
    EList<Classifier> parents = ((org.eclipse.uml2.uml.Class) aClass).getGenerals();
    if (parents.size() >= 1) {
      if (parents.size() != 1) {
        // Only one parent is allowed in Alloy
        String parentName = parents.get(0).getName();
        System.err.println("Only one parent is allowed. One parent \"" + parentName
            + "\" is included as sig \"" + aClass.getName() + "\"'s parent");
      }
      // parentName == null , "BehaviorOccurrence", "Occurrence" or "Anything"
      if (!AlloyUtils.validParent(((Class) parents.get(0)).getName()))
        return null;
      return (Class) parents.get(0);
    } else
      return null;
  }
}
