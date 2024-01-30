package edu.gatech.gtri.obm.translator.alloy;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;

// TODO: Auto-generated Javadoc
/** The Class MDUtils. */
public class MDUtils {

  /**
   * Get class in hierarchy order in list but not include "BehaviorOccurence" or "Occurrence".
   * Smaller the index, more ancestor
   *
   * @param mainClass the main class
   * @return the class in hierarchy
   */
  public static List<Class> getClassInHierarchy(Class mainClass) {
    List<Class> list = new ArrayList<Class>();
    list.add(mainClass);
    while ((mainClass = getParent(mainClass)) != null) {
      list.add(0, mainClass);
    }
    return list;
  }

  /**
   * Gets the parent.
   *
   * @param aClass the a class
   * @return the parent
   */
  private static Class getParent(Class aClass) {
    EList<Classifier> parents = ((org.eclipse.uml2.uml.Class) aClass).getGenerals();
    if (parents.size() >= 1) {
      if (parents.size() != 1) {
        // Only one parent is allowed in Alloy
        String parentName = parents.get(0).getName();
        System.err.println(
            "Only one parent is allowed. One parent \""
                + parentName
                + "\" is included as sig \""
                + aClass.getName()
                + "\"'s parent");
      }
      // parentName == null , "BehaviorOccurrence", "Occurrence" or "Anything"
      if (!AlloyUtils.validParent(((Class) parents.get(0)).getName())) return null;
      return (Class) parents.get(0);
    } else return null;
  }
}
