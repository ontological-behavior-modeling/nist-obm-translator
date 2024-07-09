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
import edu.umd.omgutil.sysml.sysml1.SysMLUtil;


/**
 * <p>
 * Translate SysML Behavior Model in a xmi file into an Alloy file.
 * 
 * 
 * Example:
 * 
 * <pre>
 * OBMXMI2Alloy translator = new OBMXMI2Alloy("C:\\temp\\OBMModel.xmi");
 * translator.createAlloyFile(new File("C:\\OBMModel.xmi"),
 *     "Model::4.1 Basic Examples::4.1.1 Time Orderings::SimpleSequence",
 *     new File("C:\\output.als"));
 * </pre>
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 *
 */
public class OBMXMI2Alloy_original {
  /**
   * errorMessages collected during the translation. Resetting by each createAlloyFile method call.
   */
  List<String> errorMessages;
  /**
   * messages collected during the translation. Resetting by each createAlloyFile method call.
   */
  List<String> messages;
  /**
   * A class connect this and Alloy class
   */
  ToAlloy toAlloy;

  /**
   * An absolute path name string for the required library folder containing Transfer.als and utilities(folder) necessary for creating own OBMUtil alloy object.
   */
  private String alloyLibPath;

  /**
   * A constructor to set the given alloyLibPath as an instance variable.
   * 
   * @param alloyLibPath the abstract pathname.
   */
  public OBMXMI2Alloy_original(String _alloyLibPath) {
    this.alloyLibPath = _alloyLibPath;
  }

  /**
   * Initialize the translator. Called by createAlloyFile method.
   */
  private void reset() {
    toAlloy = new ToAlloy(alloyLibPath);
    this.errorMessages = new ArrayList<>();
    this.messages = new ArrayList<>();
  }

  /**
   * Initialize the translator and create an alloy output file of the qualifideName class/behavior model in the xml file. If this method return false, you may use getErrorMessages() to know why failed.
   * 
   * @param xmiFile - the xmi file contain a class to be translated to an alloy file.
   * @param qualifiedName of a UML:Class for translation (ie., Model::FoodService::OFSingleFoodService)
   * @param outputFile - the output alloy file
   * @return true if the given outputFile is created from the given xmlFile and the qualifiedName; false if fails.
   */
  public boolean createAlloyFile(File xmiFile, String qualifiedName, File outputFile) {
    reset();
    if (!xmiFile.exists() || !xmiFile.canRead()) {
      this.errorMessages.add(
          "A file " + xmiFile.getAbsolutePath() + " does not exist or no permission to read.");
      return false;
    }
    Set<Field> parameterFields = null;
    if ((parameterFields = loadOBMAndCreateAlloy(xmiFile, qualifiedName)) != null) {
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

    return false;
  }

  /**
   * Get errorMessages collected while the translation.
   * 
   * @return errorMessage - list of error message strings
   */
  public List<String> getErrorMessages() {
    return this.errorMessages;
  }


  /**
   * Load the given xmi file and find the given class and create alloy objects in memory.
   * 
   * @xmiFile xmiFile - A xmi file contains a class to be converted to an alloy file
   * @param - classQualifiedName - the qualified name string of a class contained in the xml file (i.e., Model::4.1 Basic Examples::4.1.2 Loop::Loop)
   * @return boolean true if successfully created as alloy objects otherwise false. A user can use getErrorMessages() to retrieve the error messages.
   */
  private Set<Field> loadOBMAndCreateAlloy(File xmiFile, String classQualifiedName) {

    // parameterFields = new HashSet<>();
    // valueTypeFields = new HashSet<>();
    ResourceSet rs;
    try {
      rs = EMFUtil.createResourceSet();
    } catch (FileNotFoundException e1) {
      this.errorMessages.add("Failed to initialize OmgUtil.");
      return null;
    }
    Resource resource = EMFUtil.loadResourceWithDependencies(rs,
        URI.createFileURI(xmiFile.getAbsolutePath()), null);

    try {
      while (!resource.isLoaded()) {
        this.messages.add("XMI Resource not loaded yet wait 1 milli sec...");
        Thread.sleep(1000);
      }
    } catch (Exception e) {
    }
    /**
     * omgutil SysMLUtil - used to create the omgutil ResourceSet used during the translation
     */
    SysMLUtil sysMLUtil;
    try {
      sysMLUtil = new SysMLUtil(rs);
    } catch (UMLModelErrorException e) {
      this.errorMessages.add("Failed to initialize OmgUtil's SysMLUtil. " + e.getMessage());
      return null;
    }
    // classes
    ClassesHandler classHandler =
        new ClassesHandler(resource, classQualifiedName, xmiFile.getAbsolutePath(), this.toAlloy,
            sysMLUtil);
    if (!classHandler.process()) {
      this.errorMessages.addAll(classHandler.getErrorMessages());
      return null;
    }
    Set<Field> parameterFields = classHandler.getParameterFields();
    Set<PrimSig> leafSigs = classHandler.getLeafSigs();
    Map<String, Set<String>> stepPropertiesBySig = classHandler.getStepPropertiesBySig();
    String mainSigLabel = classHandler.getMainSigLabel();
    List<Class> classInHierarchy = classHandler.getClassInHierarchy();
    Set<NamedElement> allClasses = classHandler.getAllClasses();


    // connectors
    ConnectorsHandler connectorsHandler = null;
    try {
      connectorsHandler = new ConnectorsHandler(xmiFile, sysMLUtil, leafSigs,
          this.toAlloy, parameterFields, stepPropertiesBySig);
      connectorsHandler.process(classInHierarchy, allClasses);
    } catch (UMLModelErrorException e) { // throw by OMGUtil's sysmlAdapter initialization
      this.errorMessages.add("Failed to load " + xmiFile + " into OmgUtil. " + e.getMessage());
      return null;
    } catch (FileNotFoundException e) {// throw by OMGUtil's sysmlAdapter initialization, but this should not happens because its existence checked earlier
      this.errorMessages.add("Failed to load " + xmiFile + " into OmgUtil. " + e.getMessage());
      return null;
    }

    Set<Sig> sigWithTransferFields = connectorsHandler.getSigWithTransferFields();
    Set<String> sigNameWithTransferConnectorWithSameInputOutputFieldType =
        connectorsHandler.getSigNameWithTransferConnectorWithSameInputOutputFieldType();
    HashMap<String, Set<String>> connectorTargetInputPropertyNamesByClassName =
        connectorsHandler.getConnectorTargetInputPropertyNamesByClassName();
    HashMap<String, Set<String>> connectorSourceOutputPrpertyNamesByClassName =
        connectorsHandler.getConnectorSourceOutputPrpertyNamesByClassName();

    Set<String> transferingTypeSig = connectorsHandler.getTransferingTypeSig();
    Map<String, Set<String>> sigToTransferFieldMap = connectorsHandler.getSigToTransferFieldMap();
    Map<String, Set<Expr>> sigToFactsMap = connectorsHandler.getSigToFactsMap();
    this.messages.addAll(connectorsHandler.getMessages());

    // using sigToFactsMap and sigToTransferFieldMap to add transferFields to mainSig.
    // loop through mainSig's parent to collect transferSigs and added to mainSig as its fields
    // Note: Only supporting transferFields inherited to mainSig.
    // If maingSig has a parent who inherited tranferFields, currently the translator is not
    // supported to handle the inherited transfer fields for non mainSig.
    Set<String> mainSigInheritingTransferFields = new HashSet<>();
    // i.e.,
    // o/isBeforeTarget[x . (IFFoodService <: transferOrderServe)]
    // o/bijectionFiltered[o/outputs, x . (FoodService <: order), x . (FoodService <: order) .
    // (IFOrder <: orderedFoodItem)]
    Set<Expr> mainSigInheritingTransferRelatedFacts = new HashSet<>();
    // classInHiearchy = [0: FoodService, 1: IFoodService, 2: IFSingleFoodServive]
    for (int i = 0; i < classInHierarchy.size() - 1; i++) {
      Set<String> possibleMainSigInheritingTransferFields =
          sigToTransferFieldMap.get(classInHierarchy.get(i).getName());
      if (possibleMainSigInheritingTransferFields != null) {
        // transferOrderServe - OFSingleFoodService
        mainSigInheritingTransferFields.addAll(possibleMainSigInheritingTransferFields);
      }
      Set<Expr> possibleMainSigInheritingTransferRelatedFacts =
          sigToFactsMap.get(classInHierarchy.get(i).getName());
      if (possibleMainSigInheritingTransferRelatedFacts != null)
        mainSigInheritingTransferRelatedFacts.addAll(possibleMainSigInheritingTransferRelatedFacts);
    }
    // add mainSig's inherited transfer fields to step Properties for the mainSig
    stepPropertiesBySig.get(mainSigLabel).addAll(mainSigInheritingTransferFields);

    toAlloy.addFacts(mainSigLabel, mainSigInheritingTransferRelatedFacts);


    Set<Sig> noStepsSigs = toAlloy.addStepsFacts(stepPropertiesBySig, leafSigs);
    // if "no x.steps" and sig with fields with type Transfer should not have below:
    // fact {all x: BehaviorWithParameterOut | no y: Transfer | y in x.steps}
    sigWithTransferFields.addAll(noStepsSigs);
    toAlloy.addNoTransferInXStepsFact(sigWithTransferFields, leafSigs);

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
}


