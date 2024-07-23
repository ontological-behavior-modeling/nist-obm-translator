package edu.gatech.gtri.obm.alloy.translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;

/**
 * Start from a main class to its properties to their property type classes to create signature and fields. Instance variables, parameterFields, leafSigs, stepPropertiesBySig, classHiearchy,
 * mainClass, and propertiesByClass are collected during the process and passed back to OBMXMI2Alloy to complete the translation.
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 */
public class ClassesHandler {

  /** Stereotype qualified names */
  private static String STEREOTYPE_STEP = "Model::OBM::Step";
  private static String STEREOTYPE_PATICIPANT = "SysML::ParticipantProperty";
  private static String STEREOTYPE_PAREMETER = "Model::OBM::Parameter";

  /**
   * A class connect this and Alloy class
   */
  ToAlloy toAlloy;
  /**
   * a map where key = NamedElement(Class or org.eclipse.uml2.uml.PrimitiveType(Integer, Real etc.)) value = property. Used to create disjoint fields
   */
  Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> propertiesByClass;
  /**
   * A map where key is sig name string and value is a set of field name strings. This includes inherited fields/properties and used in closure facts.
   */
  Map<String, Set<String>> stepPropertiesBySig;

  /**
   * A set of leaf signatures
   */
  Set<PrimSig> leafSigs;
  /**
   * A set of Alloy fields created for Properties with <<Parameter>> stereotype
   */
  Set<Field> parameterFields;

  /**
   * omgutil SysMLUtil - used to create the omgutil ResourceSet used during the translation
   */
  SysMLUtil sysMLUtil;

  /**
   * A main Class translating to Alloy.
   */
  Class mainClass;
  /**
   * A list of classes in hierarchy where a class with highest index is the main class translating to Alloy. A lowest index class is oldest in the hierarchy.
   */
  List<Class> classInHierarchy;

  /**
   * errorMessages collected during the translation. Resetting by each createAlloyFile method call.
   */
  List<String> errorMessages;


  protected ClassesHandler(Class _mainClass, ToAlloy _toAlloy, SysMLUtil _sysMLUtil) {
    this.mainClass = _mainClass;
    this.toAlloy = _toAlloy;
    this.sysMLUtil = _sysMLUtil;
  }



  protected boolean process() {

    parameterFields = new HashSet<>(); // Set<Field>
    propertiesByClass = new HashMap<>();// Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>>

    // The main class will be the last in this list
    List<org.eclipse.uml2.uml.Class> classInHierarchyForMain =
        UML2Utils.createListIncludeSelfAndParents(mainClass);
    PrimSig parentSig = Alloy.occSig; // oldest's parent is always Occurrence
    for (Class aClass : classInHierarchyForMain) { // loop through oldest to youngest(main
                                                   // is the youngest)
      boolean isMainSig = (aClass == mainClass) ? true : false;
      // create Signature - returned parentSig will be the next aClass(Signature)'s parent
      parentSig = toAlloy.createSig(aClass.getName(), parentSig, isMainSig);
      if (parentSig == null) {
        this.errorMessages.add(
            "Signature named \"" + aClass.getName()
                + "\" already existed (possibly in the required library).  The name must be unique.");
        return false;
      }
      processClassToSig(aClass); // update this.propertiesByClass
    }

    // The order of list is [0]=grand parent [1]=parent [2]=child(mainClass)
    this.classInHierarchy = UML2Utils.createListIncludeSelfAndParents(mainClass);

    Map<PrimSig, Set<Property>> redefinedPropertiesBySig = new HashMap<>(); // updated in addFieldsToSig method
    stepPropertiesBySig = new HashMap<>();
    // go throw signatures in classInHierarchy first then the remaining in allClassesConnectedToMainSigByFields
    for (Class aClass : classInHierarchy) {
      PrimSig sigOfNamedElement = toAlloy.getSig(aClass.getName());
      redefinedPropertiesBySig.put(sigOfNamedElement,
          addFieldsToSig(propertiesByClass.get(aClass), sigOfNamedElement));
      stepPropertiesBySig.put(aClass.getName(), collectAllStepProperties(aClass));
    }

    // go through the remaining classes
    for (NamedElement ne : propertiesByClass.keySet()) {
      if (!classInHierarchy.contains(ne)) {
        PrimSig sigOfNamedElement = toAlloy.getSig(ne.getName());
        redefinedPropertiesBySig.put(sigOfNamedElement,
            addFieldsToSig(propertiesByClass.get(ne), sigOfNamedElement));
        stepPropertiesBySig.put(ne.getName(), collectAllStepProperties(ne));
      }
    }

    for (PrimSig sig : redefinedPropertiesBySig.keySet()) {
      // fact {all x: OFFoodService | x.eat in OFEat }
      // fact {all x: IFSingleFoodService | x.order in IFCustomerOrder}
      toAlloy.addRedefinedSubsettingAsFacts(sig,
          UML2Utils.toNameAndType(redefinedPropertiesBySig.get(sig)));
    }

    Set<NamedElement> leafClasses = UML2Utils.findLeafClass(propertiesByClass.keySet()); // class and PrimitiveType
    leafSigs =
        leafClasses.stream().map(ne -> toAlloy.getSig(ne.getName())).collect(Collectors.toSet());

    return true;

  }

  /**
   * Add fields in the given signature (non redefined attributes only), Add cardinality facts (ie., abc = 1) and return redefined properties of this signature. this.parameterFields is updated.
   * 
   * @param _propertiesByType - Map<Type, List<Property>> map of properties by type (key = property/field type(signature), value = properties/fields)
   * @param _ownerSig - Signature of a class
   * @return redefinedProperties Set<Property> of the _sigOfNamedElement
   */
  private Set<Property> addFieldsToSig(
      Map<org.eclipse.uml2.uml.Type, List<Property>> _propertiesByType,
      PrimSig _ownerSig) {

    Set<Property> redefinedProperties = new HashSet<>();
    if (_propertiesByType != null) {
      for (org.eclipse.uml2.uml.Type propertyType : _propertiesByType.keySet()) {
        // find property by type (ie., propetyType = Order, List<Property> = [order]);
        List<Property> propertiesSortedByType = _propertiesByType.get(propertyType);

        // sort property in alphabetical order, also remove redefined properties from the sorted
        // list.
        List<String> nonRedefinedPropertyInAlphabeticalOrderPerType = new ArrayList<>();
        Set<String> parameterProperties = new HashSet<>();
        // Set<String> valueTypeProperties = new HashSet<>();
        for (Property p : propertiesSortedByType) {
          if (p.getName() != null) { // Since MD allow having no name.
            if (p.getRedefinedProperties().size() == 0)
              nonRedefinedPropertyInAlphabeticalOrderPerType.add(p.getName());
            else
              redefinedProperties.add(p);

            if (p.getAppliedStereotype(STEREOTYPE_PAREMETER) != null)
              parameterProperties.add(p.getName());

          } else {
            this.errorMessages.add(p.getQualifiedName()
                + "has no name, so ignored.  Please defined the name to be included");
          }
        }

        Collections.sort(nonRedefinedPropertyInAlphabeticalOrderPerType);

        if (nonRedefinedPropertyInAlphabeticalOrderPerType.size() > 0) {
          Sig.Field[] fields =
              toAlloy.addDisjAlloyFields(nonRedefinedPropertyInAlphabeticalOrderPerType,
                  propertyType.getName(), _ownerSig);
          // server, Serve, SinglFooeService
          if (fields != null) { // this should not happens
            for (int j = 0; j < propertiesSortedByType.size(); j++) {
              toAlloy.addCardinalityFact(_ownerSig, fields[j],
                  propertiesSortedByType.get(j).getLower(),
                  propertiesSortedByType.get(j).getUpper());
              if (parameterProperties.contains(fields[j].label)) // {
                this.parameterFields.add(fields[j]);
            }
          }
        } else { // cardinality only when no redefined properties
          for (Property p : propertiesSortedByType) {
            boolean added =
                toAlloy.addCardinalityFact(_ownerSig, p.getName(), p.getLower(),
                    p.getUpper());
            if (!added)
              this.errorMessages.add("A field \"" + p.getName() + " not found in Sig "
                  + _ownerSig.label + ".  Failed to add cadinality constraint.");
          }
        }
      }
    } // end processing property
    return redefinedProperties;
  }


  /*
   * go through a class, its properties, a property, and its type (class) recursively to complete propertiesByClass (Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>>) and create
   * signatures of the property types(Class or PrimitiveType).
   * 
   * For example, this processClassToSig method is called from outside of this method : with umlElement = FoodService, then called recursively(internally) umlElement as Prepare -> Order -> Serve -> Eat
   * -> Pay. The propertiesByClass is Map<NamedElement, Map<org.eclipse.uml2.uml.Type, List<Property>>> where the key NamedElement to be mapped to Sig (class or PrimitiveType like Integer and Real) and
   * value is Map<org.eclipse.uml2.uml.Type, List<Property>. The map's key type is property/field's type and List<Property> is property/fields having the same type.
   * 
   * For example, sig SimpleSequence extends Occurrence { disj p1,p2: set AtomicBehavior } propertiesByClass's key = SimpleSequence and value = (key = AtomicBehavior, value =[p1,p2])
   * 
   * @param _namedElement NamedElement either org.eclipse.uml2.uml.Class or org.eclipse.uml2.uml.PrimitiveType to be analyzed to complete propertiesByClass
   */
  private void processClassToSig(NamedElement _namedElement) {
    if (_namedElement instanceof org.eclipse.uml2.uml.Class) {
      Set<Property> atts = sysMLUtil.getOwnedAttributes((org.eclipse.uml2.uml.Class) _namedElement);

      if (atts.size() == 0) {
        propertiesByClass.put(_namedElement, null);
        return;
      }

      // find property having the same type
      // for example) sig MultipleObjectFlowAlt extends Occurrence
      // { p1: set BehaviorWithParameterOut, ....
      // key = BehaviorWithParameterOut, value = [p1]
      Map<org.eclipse.uml2.uml.Type, List<Property>> propertiesByTheirType = new HashMap<>();
      for (Property p : atts) {
        org.eclipse.uml2.uml.Type eType = p.getType();
        List<Property> ps = propertiesByTheirType.get(eType) == null ? new ArrayList<>()
            : propertiesByTheirType.get(eType);
        ps.add(p);
        propertiesByTheirType.put(eType, ps);


        if (eType instanceof org.eclipse.uml2.uml.Class
            || eType instanceof org.eclipse.uml2.uml.PrimitiveType) {

          EList<Classifier> parents = null;
          if (eType instanceof org.eclipse.uml2.uml.Class) {
            parents = ((org.eclipse.uml2.uml.Class) eType).getGenerals();
          }
          // alloy allows only one parent
          // create Sig of property type with or without parent
          // parent should already exists
          toAlloy.createSigOrReturnSig(eType.getName(),
              parents == null || parents.size() == 0 ? null : parents.get(0).getName());
          // process both eType(Class or PrimitiveType) recursively
          processClassToSig(eType);
        }
        propertiesByClass.put(_namedElement, propertiesByTheirType);
      }
    }
    // like Integer and Real - assume no properties, thus put null as the value of propertiesByClass
    else if (_namedElement instanceof org.eclipse.uml2.uml.PrimitiveType) {
      propertiesByClass.put(_namedElement, null);
    }
  }

  /**
   * get property (including inherited ones) string names of the given namedElement if the namedElement is class and the property has STEROTYPE_STEP or STREOTYPE_PATICIPANT stereotype.
   * 
   * @param _ne - A NamedElement that can be Class or PrimitiveType to get properties.
   * @return Set<String> property names or null if the given ne is PrimitiveType(i.e., Real, Integer)
   */
  private Set<String> collectAllStepProperties(NamedElement _ne) {

    Set<String> stepProperties = new HashSet<>();
    if (_ne instanceof org.eclipse.uml2.uml.Class) { // ne can be PrimitiveType
      Set<org.eclipse.uml2.uml.Property> atts =
          sysMLUtil.getAllAttributes((org.eclipse.uml2.uml.Class) _ne);
      for (Property p : atts) {
        if (p.getAppliedStereotype(STEREOTYPE_STEP) != null
            || p.getAppliedStereotype(STEREOTYPE_PATICIPANT) != null) {
          stepProperties.add(p.getName());
        }
      }
    }
    return stepProperties;
  }


  protected Set<Field> getParameterFields() {
    return this.parameterFields;
  }

  protected Set<PrimSig> getLeafSigs() {
    return this.leafSigs;
  }

  protected Map<String, Set<String>> getStepPropertiesBySig() {
    return this.stepPropertiesBySig;
  }

  protected String getMainSigLabel() {
    return this.mainClass.getName();
  }

  protected List<Class> getClassInHierarchy() {
    return this.classInHierarchy;
  }

  protected Set<NamedElement> getAllClasses() {
    return this.propertiesByClass.keySet();
  }

  /**
   * Get errorMessages collected while the translation.
   * 
   * @return errorMessage - list of error message strings
   */
  protected List<String> getErrorMessages() {
    return this.errorMessages;
  }

}
