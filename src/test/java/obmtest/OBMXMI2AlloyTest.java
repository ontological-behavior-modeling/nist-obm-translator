package obmtest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.OBMXMI2Alloy;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.umd.omgutil.UMLModelErrorException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/*
 * Testing set up ===============
 *
 * als files
 *
 * from:Box\NIST OBM Translator\Alloy Models\obm-alloy-code_2023-09-25. zip\obm\*
 *
 * to:obm-alloy-code_2023-09-25\obm
 *
 * xmi files
 *
 * from:Box\NIST OBM Translator\NIST UML-SysML OBM Models\obmsmttrans_2023-09-25.
 * zip\obmsmttrans\samples\OBMModel.xmi Box\NIST OBM Translator\NIST UML-SysML OBM
 * Models\obmsmttrans_2023-09-25. zip\obmsmttrans\samples\OBM.xmi
 *
 * to:obm-alloy-code_2023-09-25\obm
 */

class OBMXMI2AlloyTest {

  @ParameterizedTest
  @CsvSource({
    // sig's fields order matter
    "4.1.4 Transfers and Parameters2 - ParameterBehavior_mw.als,Model::Basic::ParameterBehavior",
    // "4.1.5 Multiple Execution Steps - Multiple Object Flow_mw.als,
    // Model::Basic::ObjectFlowBehavior",
    //
    // "4.2.2 FoodService Object Flow - IFSingleFoodService - OFFoodService_mw.als,
    // Model::Realistic::IFFoodService",
    "4.1.1 Control Nodes1 - SimpleSequence_mw.als, Model::Basic::SimpleSequence",
    "4.1.1 Control Nodes2 - Fork_mw.als, Model::Basic::Fork",
    "4.1.1 Control Nodes3 - Join_mw.als, Model::Basic::Join",
    "4.1.1 Control Nodes4 - Decision_mw.als, Model::Basic::Decision",
    "4.1.1 Control Nodes5 - Merge_mw.als, Model::Basic::Merge",
    "4.1.1 Control Nodes6 - CombinedControlNodes_mw.als, Model::Basic::AllControl",
    "4.1.2 LoopsExamples_mw.als, Model::Basic::Loop",
    "4.1.3 CallingBehaviors_mw.als, Model::Basic::ComposedBehavior",
    // due to not creating steps in TransferProduct <<Step>> vs. <<paticipant>>
    "4.1.4 Transfers and Parameters1 - TransferProduct_mw.als, Model::Basic::TransferProduct",
    "4.1.5 Multiple Execution Steps - Multiple Control Flow_mw.als,"
        + " Model::Basic::ControlFlowBehavior",
    // // 4.1.6
    // // fails because p2 multiplicity
    "4.1.6 UnsatisfiableAsymmetry_mw.als, Model::Basic::UnsatisfiableAsymmetry",
    "4.1.6 UnsatisfiableTransitivity_mw.als, Model::Basic::UnsatisfiableTransitivity",
    "4.1.6 UnsatisfiableMultiplicity_mw.als, Model::Basic::UnsatisfiableMultiplicity",
    "4.1.6 UnsatisfiableComposition1_mw.als, Model::Basic::UnsatisfiableComposition1",
    "4.1.6 UnsatisfiableComposition2_mw.als, Model::Basic::UnsatisfiableComposition2",
    "4.2.1 FoodService Control Flow - FoodService_mw.als, Model::Realistic::FoodService",
    "4.2.1 FoodService Control Flow - SingleFoodService_mw.als,"
        + " Model::Realistic::SingleFoodService",
    "4.2.1 FoodService Control Flow - BuffetService_mw.als, Model::Realistic::BuffetService",
    "4.2.1 FoodService Control Flow - ChurchSupperService_mw.als, Model::Realistic::ChurchSupper",
    "4.2.1 FoodService Control Flow - FastFoodService_mw.als, Model::Realistic::FastFoodService",
    "4.2.1 FoodService Control Flow -"
        + " UsatisfiableFoodService_mw.als,Model::Realistic::UnsatisfiableService",
  })

  /**
   * @param manualFileName
   * @param className
   * @param fileToBecreatedFromXmiFile null if not to create outputfile
   * @throws FileNotFoundException
   * @throws UMLModelErrorException
   */
  void sameAbstractSyntaxTreeTestAndSignatures(String manualFileName, String className)
      throws FileNotFoundException, UMLModelErrorException {

    System.out.println("manual file to compare = " + manualFileName);
    System.out.println("className = " + className);

    // ========== Create Alloy model from OBM XMI file & write as a file ==========

    String ombmodel_dir = "src/test/resources";
    String output_and_testfiles_dir = "src/test/resources";
    File xmiFile = new File(ombmodel_dir, "OBMModel.xmi");

    // setting any errors to be in error file
    PrintStream o = new PrintStream(new File(output_and_testfiles_dir, "error.txt"));
    System.setErr(o);

    File apiFile =
        new File(
            output_and_testfiles_dir,
            manualFileName
                + "_Generated-"
                + className.replaceAll("::", "_") /* alloyModule.getModuleName() */
                + ".als");

    OBMXMI2Alloy test = new OBMXMI2Alloy(output_and_testfiles_dir);
    if (!test.createAlloyFile(xmiFile, className, apiFile)) return;

    // creating comparator
    ExpressionComparator ec = new ExpressionComparator();

    ////////////////////// Set up (Importing Modules) /////////////////////////////////////////
    // API
    CompModule apiModule = AlloyUtils.importAlloyModule(apiFile);
    // TEST
    File testFile = new File(output_and_testfiles_dir, manualFileName);
    System.out.println("testFile: " + testFile.exists() + "? " + testFile.getAbsolutePath());
    CompModule testModule = AlloyUtils.importAlloyModule(testFile);

    //////////////////////// Comparing Reachable Facts ////////////////////////////////
    // API
    Expr api_reachableFacts = apiModule.getAllReachableFacts(); // test.getOverallFacts();
    System.out.println(api_reachableFacts);
    // TEST
    Expr test_reachableFacts = testModule.getAllReachableFacts();
    System.out.println(test_reachableFacts);
    // Compare
    assertTrue(ec.compareTwoExpressions(api_reachableFacts, test_reachableFacts));

    ///////////////////////// Comparing Sigs ////////////////////
    // API
    List<Sig> api_reachableDefinedSigs = apiModule.getAllReachableUserDefinedSigs();
    Map<String, Sig> api_SigByName = new HashMap<>(); // test.getAllReachableUserDefinedSigs();
    for (Sig sig : api_reachableDefinedSigs) {
      api_SigByName.put(sig.label, sig);
    }
    // TEST
    List<Sig> test_reachableDefinedSigs = testModule.getAllReachableUserDefinedSigs();
    Map<String, Sig> test_SigByName = new HashMap<>();
    for (Sig sig : test_reachableDefinedSigs) {
      test_SigByName.put(sig.label, sig);
    }

    // Compare - Sig size
    assertTrue(api_SigByName.size() == test_SigByName.size());

    // Compare - Each sig
    for (String sigName : api_SigByName.keySet()) {
      Sig alloyFileSig = test_SigByName.get(sigName);
      Sig apiSig = api_SigByName.get(sigName);
      assertTrue(ec.compareTwoExpressions(alloyFileSig, apiSig));
    }
  }
}
