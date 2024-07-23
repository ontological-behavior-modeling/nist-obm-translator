package edu.gatech.gtri.obm.alloy.translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.NamedElement;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;
import edu.umd.omgutil.EMFUtil;
import edu.umd.omgutil.UMLModelErrorException;
import edu.umd.omgutil.sysml.sysml1.SysMLAdapter;
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;


/**
 * <p>
 * A class to translate SysML Behavior Model in a xmi file into an alloy file.
 * 
 * Example:
 * 
 * <pre>
 * OBMXMI2Alloy translator = new OBMXMI2Alloy("C:\\alloylibs")
 * if (translator.loadXmiFile(new File("C:\\OBMModel.xmi"))){
 *  boolean success = translator.createAlloyFile("Model::4.1 Basic Examples::4.1.1 Time Orderings::SimpleSequence",new File("C:\\output.als")));
 *      if (!success)
 *          System.out.println(translfator.getErrorMessages());
 *  }
 *  else
 *      System.out.println(translator.getErrorMessages());
 *  System.out.println(translator.getMessages());
 * </pre>
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 *
 */
public final class OBMXMI2Alloy {
  /**
   * A class to collect all signatures, fields, and facts to be translated to an alloy file
   */
  private Alloy alloy;
  /**
   * A class connect this and Alloy class
   */
  private ToAlloy toAlloy;
  /**
   * omgutil SysMLUtil - Util method from omgutil
   */
  private SysMLUtil sysMLUtil;
  /**
   * omgutil SysMLAdapter - Adapter for SysML from omgutil
   */
  private SysMLAdapter sysMLAdapter;
  /**
   * omgUtil Resource - Resource from omgutil - used to get Class object from the xmilFile using EMFUtil
   */
  private Resource resource;
  /**
   * errorMessages collected during the translation.
   */
  List<String> errorMessages;
  /**
   * messages collected during the translation.
   */
  List<String> messages;


  /**
   * A constructor to set the given alloyLibPath as an instance variable.
   * 
   * @param alloyLibPath - An absolute path name string for the required library folder containing Transfer.als and utilities(folder) necessary for translation.
   */
  public OBMXMI2Alloy(String _alloyLibPath) {
    this.alloy = new Alloy(_alloyLibPath);
  }

  /**
   * loading xmiFile to preparing for translation
   * 
   * @param xmiFile - xmiFile containing classes you like to translate to an alloy file.
   * @return true if successful, otherwise return false
   * @throws FileNotFoundException - the given xmiFile does not exist
   * @throws UMLModelErrorException - the problem constructing utility objects from xmiFile
   */
  public boolean loadXmiFile(File xmiFile) throws FileNotFoundException, UMLModelErrorException {

    try {
      ResourceSet rs = EMFUtil.createResourceSet();
      this.resource = EMFUtil.loadResourceWithDependencies(rs,
          URI.createFileURI(xmiFile.getAbsolutePath()), null);
      // omgutil SysMLUtil - used to create the omgutil ResourceSet used during the translation
      this.sysMLUtil = new SysMLUtil(rs);
      // omgutil's SysMLAdapter to be used in ConnectorHandler
      this.sysMLAdapter = new SysMLAdapter(xmiFile, null);
    } catch (FileNotFoundException e) {
      this.errorMessages
          .add("Failed to initialize the translator. Make sure xmiFile exists in "
              + xmiFile.getAbsolutePath() + " and readable. " + e.getMessage());
      return false;
    } catch (UMLModelErrorException e) {
      this.errorMessages
          .add("Failed to initialize the translator. Make sure xmiFile exists in "
              + xmiFile.getAbsolutePath() + " and readable. " + e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Create an alloy output file of the qualifideName class/behavior model in the xml file. If this method return false, you may use getErrorMessages() to know why cause failure.
   * 
   * @param qualifiedName of a UML:Class for translation (ie., Model::FoodService::OFSingleFoodService)
   * @param outputFile - the output alloy file
   * @return boolean true if the given outputFile is created from the given xmlFile and the qualifiedName; false if fails.
   */
  public boolean createAlloyFile(String qualifiedName, File outputFile) {

    toAlloy = new ToAlloy(alloy);
    this.errorMessages = new ArrayList<>();
    this.messages = new ArrayList<>();

    Set<Field> parameterFields = null;
    if ((parameterFields = CreateAlloy(qualifiedName)) != null) {
      try {
        boolean success = toAlloy.createAlloyFile(outputFile, parameterFields);
        if (success)
          this.messages.add(outputFile.getAbsolutePath() + " is created");
        else
          this.errorMessages.add("Failed to create the alloy file as "
              + outputFile.getAbsolutePath() + ". May not have write permission.");
        return success;
      } catch (IOException e) {
        this.errorMessages.add("Failed to translate the alloy file: " + e.getMessage());
      }
    }
    return false; // failed to translate
  }


  /**
   * find the given class and create alloy objects in memory.
   * 
   * @param - classQualifiedName - the qualified name string of a class contained in the xml file (i.e., Model::4.1 Basic Examples::4.1.2 Loop::Loop)
   * @return Set<Field> parameterfields used by calling method to write out disj signature fields to an alloy file.
   */
  private Set<Field> CreateAlloy(String classQualifiedName) {
    // using omgUtil get NamedElement to translate
    NamedElement mainNamedElement = EMFUtil.getNamedElement(resource, classQualifiedName);
    // the NamedElement must be Class to able to translate
    if (mainNamedElement == null) {
      this.errorMessages.add(classQualifiedName + " not found.");
      return null;
    } else if (!(mainNamedElement instanceof Class)) {
      this.errorMessages.add(classQualifiedName + " is not Class. Not able to translate to Alloy.");
      return null;
    }
    // cast to Class
    Class mainClass = (Class) mainNamedElement;

    // ClasssesHandler - from main class creates Signatures and Fields for the Alloy object
    ClassesHandler classesHandler =
        new ClassesHandler(mainClass, this.toAlloy, sysMLUtil);
    if (!classesHandler.process()) {
      this.errorMessages.addAll(classesHandler.getErrorMessages());
      return null;
    }
    // get necessary information collected by ClassesHandler.process method
    Set<Field> parameterFields = classesHandler.getParameterFields(); // fields map from property with STEREOTYPE_PAREMETER
    Set<NamedElement> allClasses = classesHandler.getAllClasses(); // all NamedElements that map to signature connecting from main class.
    List<Class> classInHierarchy = classesHandler.getClassInHierarchy(); // hierarchy of main class. The main class has the largest index value.
    Set<PrimSig> leafSigs = classesHandler.getLeafSigs(); // leaf signatures
    Map<String, Set<String>> stepPropertiesBySig = classesHandler.getStepPropertiesBySig();
    String mainSigLabel = classesHandler.getMainSigLabel(); // possible to obtained from classQualifiedName

    // ConnectorsHandler - analyzing connectors for classes to create facts for the Alloy object
    ConnectorsHandler connectorsHandler = new ConnectorsHandler(sysMLAdapter, sysMLUtil, leafSigs,
        this.toAlloy, parameterFields, stepPropertiesBySig);
    connectorsHandler.process(classInHierarchy, allClasses);
    // add messages collected during the connectorshandler process to this.messages
    this.messages.addAll(connectorsHandler.getMessages());

    // get necessary information colleced by ConnectorsHandler.process method
    // a set of signature names having a transfer connector with same input and output type.
    Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType =
        connectorsHandler.getSigNameWithTransferConnectorWithSameInputOutputFieldType();
    // a map - connector target input property names by class name
    HashMap<String, Set<String>> connectorTargetInputPropertyNamesByClassName =
        connectorsHandler.getConnectorTargetInputPropertyNamesByClassName();
    // a map - connector source output property names by class name
    HashMap<String, Set<String>> connectorSourceOutputPrpertyNamesByClassName =
        connectorsHandler.getConnectorSourceOutputPrpertyNamesByClassName();
    // a set of signature names of transfer type
    Set<String> transferingTypeSig = connectorsHandler.getTransferingTypeSig();
    // a map - a transfer field names per signature name
    Map<String, Set<String>> sigToTransferFieldMap = connectorsHandler.getSigToTransferFieldMap();

    // a map - facts per signature name
    Map<String, Set<Expr>> sigToFactsMap = connectorsHandler.getSigToFactsMap();

    // a set of signatures having transfer fields, signature names are the same as sigToTransferFieldMap.keySet()
    Set<Sig> sigWithTransferFieldsAndNoStepSigs = connectorsHandler.getSigWithTransferFields();


    // loop through mainSig's parent to collect transferSigs and added to mainSig as its fields
    // Note: Only supporting transferFields inherited to mainSig.
    // If maingSig has a parent who inherited tranferFields, currently the translator is not
    // supported to handle the inherited transfer fields for non-mainSig.
    Set<String> mainSigInheritingTransferFields = new HashSet<>();
    Set<Expr> mainSigInheritingTransferRelatedFacts = new HashSet<>();
    // classInHiearchy = [0]=grand parent [1]=parent [2]=child(mainClass)
    for (int i = 0; i < classInHierarchy.size() - 1; i++) {
      Set<String> possibleMainSigInheritingTransferFields =
          sigToTransferFieldMap.get(classInHierarchy.get(i).getName());
      if (possibleMainSigInheritingTransferFields != null) {
        mainSigInheritingTransferFields.addAll(possibleMainSigInheritingTransferFields);
      }
      Set<Expr> possibleMainSigInheritingTransferRelatedFacts =
          sigToFactsMap.get(classInHierarchy.get(i).getName());
      if (possibleMainSigInheritingTransferRelatedFacts != null)
        mainSigInheritingTransferRelatedFacts.addAll(possibleMainSigInheritingTransferRelatedFacts);
    }
    // add mainSig's inherited transfer fields to step Properties for the mainSig
    stepPropertiesBySig.get(mainSigLabel).addAll(mainSigInheritingTransferFields);
    // add facts(inheriting transfer related) for main signature to Alloy using toAlloy
    toAlloy.addFacts(mainSigLabel, mainSigInheritingTransferRelatedFacts);

    // add {no steps}, {x.steps in ...}, {... x.steps} facts to the Alloy object
    Set<Sig> noStepsSigs = toAlloy.addStepsFacts(stepPropertiesBySig, leafSigs);
    // if {no x.steps} and signatures with transfer field

    // combing noStepsSigs and sigWithTransferFields and pass to toAlloy.addNoTransferInXStepsFact method to add facts like "fact {all x: BuffetService | no y: Transfer | y in x.steps}"
    sigWithTransferFieldsAndNoStepSigs.addAll(noStepsSigs);
    toAlloy.addNoTransferInXStepsFact(sigWithTransferFieldsAndNoStepSigs, leafSigs);

    Set<String> allClassNames = allClasses.stream().map(c -> c.getName())
        .collect(Collectors.toSet());

    // if the name of signatures is in sigNameOfShardFieldType, then equal input/output facts (ie., {all x: B1 | x.vin in x.inputs} and {all x: B1 | x.inputs in x.vin}}
    // are not be added
    toAlloy.handleNoInputsOutputs(connectorTargetInputPropertyNamesByClassName,
        connectorSourceOutputPrpertyNamesByClassName, allClassNames,
        sigNameWithTransferConnectorWithSameInputOutputFieldType, leafSigs);

    // adding no steps.x
    // fact {all x: Integer | no steps.x}, fact {all x: Real | no steps.x} or {all x: Product | no
    // steps.x}
    toAlloy.addStepClosureFact(transferingTypeSig, leafSigs);

    return parameterFields;
  }


  /**
   * Get messages collected while the translation a class.
   * 
   * @return message - list of message strings
   */
  public List<String> getMessages() {
    return this.messages;
  }

  /**
   * Get errorMessages collected while the translation a class.
   * 
   * @return errorMessage - list of error message strings
   */
  public List<String> getErrorMessages() {
    return this.errorMessages;
  }

}


