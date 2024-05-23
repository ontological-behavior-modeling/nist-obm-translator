package obmtest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import edu.gatech.gtri.obm.alloy.translator.AlloyUtils;
import edu.gatech.gtri.obm.alloy.translator.OBMXMI2Alloy;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.umd.omgutil.UMLModelErrorException;


/*
 * Testing set up
 *
 * als files
 *
 * from:Box\NIST OBM Translator\Alloy Models\obm-alloy-code_2023-09-25. zip\obm\*
 *
 * to:obm-alloy-code_2023-09-25\obm
 *
 * xmi files
 *
 * from:Box\NIST OBM Translator\NIST UML-SysML OBM Models\obmsmttrans_2023-09-25. zip\obmsmttrans\samples\OBMModel.xmi Box\NIST OBM Translator\NIST UML-SysML OBM Models\obmsmttrans_2023-09-25.
 * zip\obmsmttrans\samples\OBM.xmi
 *
 * to:obm-alloy-code_2023-09-25\obm
 */
class OBMXMI2AlloyTest {

  @ParameterizedTest


  // The order of fields in sig does not matter even trasfer (ie., transferP1P3)
  // The order of fact fields matter (ie., fact {all x: SimpleSequence | x.p1 + x.p2 in x.steps}
  // pass and fact {all x: SimpleSequence | x.p2 + x.p1 in x.steps} fails
  @CsvSource({

      "4.1.1 Time Orderings - AllControl.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::AllControl, true",
      "4.1.1 Time Orderings - Decision.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Decision, true",
      "4.1.1 Time Orderings - Fork.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Fork, true",
      "4.1.1 Time Orderings - Join.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Join, true",
      "4.1.1 Time Orderings - Merge.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Merge, true",
      "4.1.1 Time Orderings - SimpleSequence.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::SimpleSequence, true",
      "4.1.2 Loops - Loop.als, Model::4.1 Basic Examples::4.1.2 Loop::Loop ,true",
      "4.1.3 Behaviors with Steps - Composed.als, Model::4.1 Basic Examples::4.1.3 Behaviors with Steps::Composed, true",
      "4.1.4 Transfers and Parameters - ParameterBehavior_mw.als, Model::4.1 Basic Examples::4.1.4 Transfers and Parameters::ParameterBehavior, true",
      "4.1.4 Transfers and Parameters - TransferProduct.als, Model::4.1 Basic Examples::4.1.4 Transfers and Parameters::TransferProduct, true",
      // mw test fails "4.1.5 Multiple Execution Steps1 - Multiple Control Flow_Fail.als, Model::Basic::MultipleControlFlow, false",
      "4.1.5 Steps with Multiple Executions - MultipleControlFlow.als, Model::4.1 Basic Examples::4.1.5 Steps with Multiple Executions::MultipleControlFlow, true",
      "4.1.5 Steps with Multiple Executions - MultipleObjectFlow_mw.als, Model::4.1 Basic Examples::4.1.5 Steps with Multiple Executions::MultipleObjectFlow, true",

      "4.1.6 Unsatisfiable - UnsatisfiableAsymmetry.als, Model::4.1 Basic Examples::4.1.6 Unsatisfiable::UnsatisfiableAsymmetry, true",
      "4.1.6 Unsatisfiable - UnsatisfiableComposition1.als, Model::4.1 Basic Examples::4.1.6 Unsatisfiable::UnsatisfiableComposition1, true",
      "4.1.6 Unsatisfiable - UnsatisfiableComposition2.als, Model::4.1 Basic Examples::4.1.6 Unsatisfiable::UnsatisfiableComposition2, true",
      "4.1.6 Unsatisfiable - UnsatisfiableMultiplicity.als, Model::4.1 Basic Examples::4.1.6 Unsatisfiable::UnsatisfiableMultiplicity, true",
      "4.1.6 Unsatisfiable - UnsatisfiableTransitivity.als, Model::4.1 Basic Examples::4.1.6 Unsatisfiable::UnsatisfiableTransitivity, true",

      "4.2.1 Food Service Control Flow - BuffetService.als, Model::4.2 Advanced Examples::4.2.1 Food Service Control Flow::BuffetService, true",
      "4.2.1 Food Service Control Flow - FastFoodService.als, Model::4.2 Advanced Examples::4.2.1 Food Service Control Flow::FastFoodService, true",
      "4.2.1 Food Service Control Flow - ChurchSupper.als, Model::4.2 Advanced Examples::4.2.1 Food Service Control Flow::ChurchSupper, true",
      "4.2.1 Food Service Control Flow - RestaurantService.als, Model::4.2 Advanced Examples::4.2.1 Food Service Control Flow::RestaurantService, true",
      "4.2.1 Food Service Control Flow - SingleFoodService.als, Model::4.2 Advanced Examples::4.2.1 Food Service Control Flow::SingleFoodService, true",
      "4.2.1 Food Service Control Flow - UnsatisfiableFoodService.als, Model::4.2 Advanced Examples::4.2.1 Food Service Control Flow::UnsatisfiableFoodService, true",

      "4.2.2 Food Service Object Flow - OFFoodService.als, Model::4.2 Advanced Examples::4.2.2 Food Service Object Flow::OFFoodService, true",
      "4.2.2 Food Service Object Flow - OFControlLoopFoodService_mw.als, Model::4.2 Advanced Examples::4.2.2 Food Service Object Flow::OFControlLoopFoodService, true",
      "4.2.2 Food Service Object Flow - OFSingleFoodService.als, Model::4.2 Advanced Examples::4.2.2 Food Service Object Flow::OFSingleFoodService, true",
      "4.2.2 Food Service Object Flow - OFParallelFoodService.als, Model::4.2 Advanced Examples::4.2.2 Food Service Object Flow::OFParallelFoodService, true",

  /*
   * 
   * 
   * "4.1.5 Multiple Execution Steps1 - Multiple Control Flow_mw.als, Model::Basic::MultipleControlFlow, true",
   * 
   * 
   * // mw // "4.1.5 Multiple Execution Steps2 - Multiple Object Flow Alt_mw.als, Model::Basic::MultipleObjectFlowAlt,true", // mw
   * "4.1.5 Multiple Execution Steps2 - Multiple Object Flow_mw.als, Model::Basic::MultipleObjectFlow,true",
   * 
   * // mw ok email from Jeremy 4/15/24 "4.1.4 Transfers and Parameters2 - ParameterBehavior.als,Model::Basic::ParameterBehavior,true", // mw change = x.inputs to in x.inputs okd email from Jeremy
   * 4/15/24 "4.1.4 Transfers and Parameters1 - TransferProduct.als, Model::Basic::TransferProduct,true",
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * "4.1.3 CallingBehaviors.als, Model::Basic::Composed,true",
   * 
   * 
   * 
   * // // 4.1.6 // fact {all x: AtomicBehavior | no y: Transfer | y in x.steps} to fact {all x: AtomicBehavior // | no x.steps}
   * "4.1.6 Unsatisfiable - Asymmetry_mw.als, Model::Basic::UnsatisfiableAsymmetry, true", // not available from jeremy
   * "4.1.6 UnsatisfiableTransitivity_mw.als, Model::Basic::UnsatisfiableTransitivity, true", "4.1.6 UnsatisfiableMultiplicity_mw.als, Model::Basic::UnsatisfiableMultiplicity, true",
   * "4.1.6 UnsatisfiableComposition1_mw.als, Model::Basic::UnsatisfiableComposition1, true", "4.1.6 UnsatisfiableComposition2_mw.als, Model::Basic::UnsatisfiableComposition2, true",
   */

  })


  /**
   * create an alloy file from a class named sysMLClassQualifiedName from Obm xmi file using Alloy API. The created alloy file is imported using Alloy API again to find AllReachableFacts and
   * AllReachableUserDefinedSigs. Also, the manually created alloy file (manualFileName) is imported using Alloy API to find its AllReachableFacts and AllReachableUserDefinedSigs. Then, the Sigs and
   * Expressions(Reachable facts) of manually created and generated by translator are compared.
   * 
   * @param manualFileName
   * @param sysMLClassQualifiedName
   * @param expctedResult boolean - true if expected to pass the test, false if expected to fail the test
   * @throws FileNotFoundException
   * @throws UMLModelErrorException
   */
  void compare(String manualFileName, String sysMLClassQualifiedName, boolean expectedResult)
      throws FileNotFoundException, UMLModelErrorException {

    System.out.println("Manually created alloy file = " + manualFileName);
    System.out.println("Comparing QualifiedName for a class = " + sysMLClassQualifiedName);

    // ========== Create Alloy model from OBM XMI file & write as a file ==========

    String ombmodel_dir = "src/test/resources";
    String output_and_testfiles_dir = "src/test/resources";
    File xmiFile = new File(ombmodel_dir, "OBMModel.xmi");
    // File xmiFile = new File(ombmodel_dir, "OBMModel2.xml");

    // write any errors to be in error file
    PrintStream o = new PrintStream(new File(output_and_testfiles_dir, "error.txt"));
    System.setErr(o);

    File apiFile = new File(output_and_testfiles_dir,
        manualFileName + "_Generated" + ".als");



    OBMXMI2Alloy test = new OBMXMI2Alloy(output_and_testfiles_dir);
    if (!test.createAlloyFile(xmiFile, sysMLClassQualifiedName, apiFile)) {
      System.out.println(test.getErrorMessages());
      fail("failed to create generated file: " + apiFile.getName() + " " + test.getErrorMessages());
    }


    // creating comparator
    ExpressionComparator ec = new ExpressionComparator();

    ////////////////////// Set up (Importing Modules) /////////////////////////////////////////
    // API
    System.out.println("==== Loading api generated file...");
    CompModule apiModule = AlloyUtils.importAlloyModule(apiFile);
    // TEST
    System.out.println("==== Loading test/manual file...");
    File testFile = new File(output_and_testfiles_dir, manualFileName);
    System.out.println("testFile: " + testFile.exists() + "? " + testFile.getAbsolutePath());
    CompModule testModule = AlloyUtils.importAlloyModule(testFile);

    //////////////////////// Comparing Reachable Facts ////////////////////////////////
    // API
    System.out.println("Comparing Reachable facts.....");
    Expr api_reachableFacts = apiModule.getAllReachableFacts();// test.getOverallFacts();
    System.out.println(api_reachableFacts);
    // TEST
    Expr test_reachableFacts = testModule.getAllReachableFacts();
    System.out.println(test_reachableFacts);

    // Compare
    if (expectedResult)
      assertTrue(ec.compareTwoExpressionsFacts(api_reachableFacts, test_reachableFacts));
    else
      assertFalse(ec.compareTwoExpressionsFacts(api_reachableFacts, test_reachableFacts));


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
      assertTrue(ec.compareTwoExpressionsSigs(alloyFileSig, apiSig));
    }
  }
}
