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
 */
public class UML2Utils {

  /**
   * Return the two rules(ConnectableElements) of connector end of the connector. The first in the
   * array is for the given connector end. If the connector did not have two ends, the method return
   * null.
   *
   * @param _connector(Connector) A connector having rules/connectableElements
   * @param _connectorEnd(ConnectorEnd) A connector end having the rule/connectableElement as the
   *     first element of the return list.
   * @return (List<ConnectableElement>)- A list of ConnectableElements of the given connector. Its
   *     size should be two, otherwise return null
   */
  protected static List<ConnectableElement> getEndRolesForCEFirst(
      Connector _connector, ConnectorEnd _connectorEnd) {
    List<ConnectableElement> ces = new ArrayList<>();
    ces.add(_connectorEnd.getRole());
    for (ConnectorEnd end : _connector.getEnds()) {
      if (end != _connectorEnd) {
        ces.add(end.getRole());
        return ces;
      }
    }
    return null;
  }

  /**
   * Convert A set of Property to a map having the property name as a key and the property's type
   * name as a value.
   *
   * @param _properties(Set<Property>) - a set of property
   * @return (Map<String, String>) - a dictionary where key is a property name and value is a
   *     property type name.
   */
  protected static Map<String, String> toNameAndType(Set<Property> _properties) {
    Map<String, String> map = new HashMap<>();
    for (Property p : _properties) {
      map.put(p.getName(), p.getType().getName());
    }
    return map;
  }

  /**
   * Find NamedElements where they are leaf in hierarchy for the given NamedElements.
   *
   * @param _namedElements(Set<NamedElement>) - a set of NamedElement to be filtered.
   * @return (Set<NamedElement>) - NamedElements where they are leaf in hierarchy.
   */
  protected static Set<NamedElement> findLeafClass(Set<NamedElement> _namedElements) {
    Set<NamedElement> leafClasses = new HashSet<>();
    Set<Classifier> parentClasses = new HashSet<>();
    for (NamedElement ne : _namedElements) {
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
   * Get class in hierarchy (list) but not include "BehaviorOccurence" or "Occurrence". The top of
   * hierarchy has an index of 0. The given class is the leaf/bottom of hierarchy class and has the
   * largest index. For example, for 4.2.1 FoodService Control Flow - BuffetService.als, [0] = Food
   * Service, [1] = SingleFoodService, and [2] = BuffetService when the given class is
   * BuffetService.
   *
   * @param _aClass(Class) - A class is the bottom/leaf in the hierarchy
   * @return (List<Class>) - A list of class in hierarchy.
   */
  protected static List<Class> createListIncludeSelfAndParents(Class _aClass) {
    List<Class> list = new ArrayList<Class>();
    list.add(_aClass);
    while ((_aClass = getParent(_aClass)) != null) {
      list.add(0, _aClass);
    }
    return list;
  }

  /**
   * Return a parent of the given class. Assume only one parent (Alloy allows only one parent to be
   * defined).
   *
   * @param _aClass(Class) - a class to find its parent
   * @return (Class) - a parent class
   */
  private static Class getParent(Class _aClass) {
    EList<Classifier> parents = ((org.eclipse.uml2.uml.Class) _aClass).getGenerals();
    if (parents.size() >= 1) {
      if (parents.size() != 1) {
        // Only one parent is allowed in Alloy
        String parentName = parents.get(0).getName();
        System.err.println(
            "Only one parent is allowed. One parent \""
                + parentName
                + "\" is included as sig \""
                + _aClass.getName()
                + "\"'s parent");
      }
      // parentName == null , "BehaviorOccurrence", "Occurrence" or "Anything"
      if (!AlloyUtils.isValidUserDefineParent(((Class) parents.get(0)).getName())) return null;
      return (Class) parents.get(0);
    } else return null;
  }

  /**
   * Find a stereotype of the given element's based on the given streotypeName and return the
   * sterotype's properties as a map where key is the property name and values are its properties.
   *
   * @param _element(Element) - an element whose stereotype properties to be found
   * @param _streotypeName(String) - stereotype name in string
   * @param _streotypePropertyNames(String[]) -stereotype property(tag) names
   * @param _allMessages(List<String>) - all messages where message(s_ may be added while executing
   *     this method.
   * @return (Map<String, List<Property>) - a map (key = tag/property name, value = properties) or
   *     null if the element does not have stereotype applied.
   */
  protected static Map<String, List<Property>> getStreotypePropertyValues(
      Element _element,
      String _streotypeName,
      String[] _streotypePropertyNames,
      List<String> _allMessages) {

    Map<String, List<Property>> propertysByTagNames = new HashMap<>();
    Stereotype st = null;
    if ((st = _element.getAppliedStereotype(_streotypeName)) != null) {
      for (String propertyName : _streotypePropertyNames) {
        List<Property> results = new ArrayList<>();
        Object pObject = (_element.getValue(st, propertyName));
        if (pObject instanceof List) {
          @SuppressWarnings("unchecked")
          List<Object> properties = (List<Object>) pObject;
          for (Object property : properties) {
            if (property instanceof Property) {
              results.add((Property) property);
            } else {
              _allMessages.add(
                  propertyName
                      + " is not an instance of Property but "
                      + property.getClass().getSimpleName()
                      + ". so ignored.");
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
