package edu.gatech.gtri.obm.alloy.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;


public class MDUtils {

  /**
   * Return the rules(connectableElements) of connector end of the connector. The first in the array is for the given connector end. If the connector did not have two ends, the method return null.
   * 
   * @param cn A connector having rules/connectableElements
   * @param ce A connector end having the rule/connectableElement as the first element of the return list.
   * @return List of ConnectableElement. Its size should be two, otherwiser return null
   */
  public static List<ConnectableElement> getEndRolesForCEFirst(Connector cn, ConnectorEnd ce) {
    List<ConnectableElement> ces = new ArrayList<>();
    ces.add(ce.getRole());
    for (ConnectorEnd end : cn.getEnds()) {
      if (end != ce) {
        ces.add(end.getRole());
        return ces;
      }
    }
    return null;
  }

  public static Map<String, String> toNameAndType(Set<Property> ps) {
    Map<String, String> map = new HashMap<>();
    for (Property p : ps) {
      map.put(p.getName(), p.getType().getName());
    }
    return map;
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
   * Get class in hierarchy order in list but not include "BehaviorOccurence" or "Occurrence". Smaller the index, more ancestor. For example, for 4.2.1 FoodService Control Flow - BuffetService.als, [0]
   * = Food Service, [1] = SingleFoodService, and [2] = BuffetService where mainClass passed if for BuffertService
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
