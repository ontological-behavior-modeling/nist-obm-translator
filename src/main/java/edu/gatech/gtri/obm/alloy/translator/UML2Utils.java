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
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;

/**
 * Utility class for UML2 to extract informations
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 *
 */
public class UML2Utils {

  /**
   * Return the rules(connectableElements) of connector end of the connector. The first in the array is for the given connector end. If the connector did not have two ends, the method return null.
   * 
   * @param cn A connector having rules/connectableElements
   * @param ce A connector end having the rule/connectableElement as the first element of the return list.
   * @return List of ConnectableElement. Its size should be two, otherwiser return null
   */
  protected static List<ConnectableElement> getEndRolesForCEFirst(Connector cn, ConnectorEnd ce) {
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

  /**
   * Convert A set of Property to a map having the property name as a key and the property's type name as a value.
   * 
   * @param ps - a set of property
   * @return A map
   */
  protected static Map<String, String> toNameAndType(Set<Property> ps) {
    Map<String, String> map = new HashMap<>();
    for (Property p : ps) {
      map.put(p.getName(), p.getType().getName());
    }
    return map;
  }

  /**
   * Find a set of NamedElement that is leaf in hierarchy from the given NamedElements
   * 
   * @param allNEs - all NamedElements
   * @return a set of NamedElement that is leaf in hierarchy
   */
  protected static Set<NamedElement> findLeafClass(Set<NamedElement> allNEs) {
    Set<NamedElement> leafClasses = new HashSet<>();
    Set<Classifier> parentClasses = new HashSet<>();
    for (NamedElement ne : allNEs) {
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
   * Get class in hierarchy (list) but not include "BehaviorOccurence" or "Occurrence". The top of hierarchy has an index of 0. The given class is the leaf/bottom of hierarchy class and has the largest
   * index. For example, for 4.2.1 FoodService Control Flow - BuffetService.als, [0] = Food Service, [1] = SingleFoodService, and [2] = BuffetService when the given class is BuffetService.
   * 
   * @param aClass - Class that is the bottom/leaf of the hierarchy
   * @return A list of class in hierarchy
   */
  protected static List<Class> createListIncludeSelfAndParents(Class aClass) {
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

  /**
   * Find a stereotype of element of the given streotypeName and return map of its tagName(string) and values(Properties)
   *
   * @param element - element whose stereotype properties to be found
   * @param streotypeName - stereotype name in string
   * @param tagNames -stereotype property names
   * @return Map (key = tag/property name string, value = properties) or null if the element does not have stereotype applied.
   */
  protected static Map<String, List<Property>> getStreotypePropertyValues(Element element,
      String streotypeName, String[] tagNames, List<String> messages) {

    Map<String, List<Property>> propertysByTagNames = new HashMap<>();
    Stereotype st = null;
    if ((st = element.getAppliedStereotype(streotypeName)) != null) {
      for (String propertyName : tagNames) {
        List<Property> results = new ArrayList<>();
        Object pObject = (element.getValue(st, propertyName));
        if (pObject instanceof List) {
          @SuppressWarnings("unchecked")
          List<Object> properties = (List<Object>) pObject;
          for (Object property : properties) {
            if (property instanceof Property) {
              results.add((Property) property);
            } else {
              messages.add(
                  propertyName + " is not an instance of Property but "
                      + property.getClass().getSimpleName() + ". so ignored.");
            }
          }
          propertysByTagNames.put(propertyName, results);
        }
      }
      return propertysByTagNames;
    }
    return null;
  }

}
