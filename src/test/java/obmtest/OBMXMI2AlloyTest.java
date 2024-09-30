package obmtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.gatech.gtri.obm.alloy.translator.AlloyUtils;
import edu.gatech.gtri.obm.alloy.translator.OBMXMI2Alloy;
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

/**
 * JUnit Test for translation.
 *
 * <p>The Xmi model is loaded in memory first. Then each test creates a translator generate file.
 * After that the generated file and the expected file are loaded in memory as two alloy objects.
 * Then module(model) name, signatures/fields, and facts are compared.
 *
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 * @author Andrew H Shinjo, Graduate Student - Georgia Tech
 */
public class OBMXMI2AlloyTest {

  static OBMXMI2Alloy translater;

  // ***Note*** : output_and_testfiles_directory and obmmodel_directory must be the same for
  // comparing to pass.
  // Because comparing expected and generated files is performed by loading each files into memory
  // and then compared.

  // where the translated files will written and where the expected files are locating
  static final String output_and_testfiles_directory = "src/test/resources";
  // where obm xmi file and alloy library (Translator.als and utils/*.als) are locating
  static final String ombmodel_directory = "src/test/resources";

  static final File xmiFile = new File(ombmodel_directory, "OBMModel.xmi");

  // Testing list. Each string contains the qualifedName for class(String) and boolean (true if
  // expected to pass the test, otherwise false) separated by commas.
  @ParameterizedTest
  @CsvSource({
    "4.1.1 Time Orderings - AllControl.als, Model::4.1 Basic Examples::4.1.1 Time"
        + " Orderings::AllControl, true",
    "4.1.1 Time Orderings - Decision.als, Model::4.1 Basic Examples::4.1.1 Time"
        + " Orderings::Decision, true",
    "4.1.1 Time Orderings - Fork.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Fork, true",
    "4.1.1 Time Orderings - Join.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Join, true",
    "4.1.1 Time Orderings - Merge.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Merge,"
        + " true",
    "4.1.1 Time Orderings - SimpleSequence.als, Model::4.1 Basic Examples::4.1.1 Time"
        + " Orderings::SimpleSequence, true",
    "4.1.2 Loops - Loop.als, Model::4.1 Basic Examples::4.1.2 Loop::Loop ,true",
    "4.1.3 Behaviors with Steps - Composed.als, Model::4.1 Basic Examples::4.1.3 Behaviors with"
        + " Steps::Composed, true",

    // alloy file in name, qualifiedName of behavior model(class), boolean true if expected to pass,
    // false if expected to fail
    "4.1.4 Transfers and Parameters - ParameterBehavior_mw.als, Model::4.1 Basic Examples::4.1.4"
        + " Transfers and Parameters::ParameterBehavior, true",
    "4.1.4 Transfers and Parameters - TransferProduct.als, Model::4.1 Basic Examples::4.1.4"
        + " Transfers and Parameters::TransferProduct, true",
    "4.1.5 Steps with Multiple Executions - MultipleControlFlow.als, Model::4.1 Basic"
        + " Examples::4.1.5 Steps with Multiple Executions::MultipleControlFlow, true",
    "4.1.5 Steps with Multiple Executions - MultipleObjectFlow_mw.als, Model::4.1 Basic"
        + " Examples::4.1.5 Steps with Multiple Executions::MultipleObjectFlow, true",
    // expected to fail
    "4.1.5 Steps with Multiple Executions - MultipleControlFlow - Fail.als, Model::4.1 Basic"
        + " Examples::4.1.5 Steps with Multiple Executions::MultipleControlFlow, false",
    "4.1.6 Unsatisfiable - UnsatisfiableAsymmetry.als, Model::4.1 Basic Examples::4.1.6"
        + " Unsatisfiable::UnsatisfiableAsymmetry, true",
    "4.1.6 Unsatisfiable - UnsatisfiableComposition1.als, Model::4.1 Basic Examples::4.1.6"
        + " Unsatisfiable::UnsatisfiableComposition1, true",
    "4.1.6 Unsatisfiable - UnsatisfiableComposition2.als, Model::4.1 Basic Examples::4.1.6"
        + " Unsatisfiable::UnsatisfiableComposition2, true",
    "4.1.6 Unsatisfiable - UnsatisfiableMultiplicity.als, Model::4.1 Basic Examples::4.1.6"
        + " Unsatisfiable::UnsatisfiableMultiplicity, true",
    "4.1.6 Unsatisfiable - UnsatisfiableTransitivity.als, Model::4.1 Basic Examples::4.1.6"
        + " Unsatisfiable::UnsatisfiableTransitivity, true",
    "4.2.1 Food Service Control Flow - BuffetService.als, Model::4.2 Advanced Examples::4.2.1 Food"
        + " Service Control Flow::BuffetService, true",
    "4.2.1 Food Service Control Flow - FastFoodService.als, Model::4.2 Advanced Examples::4.2.1"
        + " Food Service Control Flow::FastFoodService, true",
    "4.2.1 Food Service Control Flow - ChurchSupper.als, Model::4.2 Advanced Examples::4.2.1 Food"
        + " Service Control Flow::ChurchSupper, true",
    "4.2.1 Food Service Control Flow - RestaurantService.als, Model::4.2 Advanced Examples::4.2.1"
        + " Food Service Control Flow::RestaurantService, true",
    "4.2.1 Food Service Control Flow - SingleFoodService.als, Model::4.2 Advanced Examples::4.2.1"
        + " Food Service Control Flow::SingleFoodService, true",
    "4.2.1 Food Service Control Flow - UnsatisfiableFoodService.als, Model::4.2 Advanced"
        + " Examples::4.2.1 Food Service Control Flow::UnsatisfiableFoodService, true",
    "4.2.2 Food Service Object Flow - OFControlLoopFoodService_mw.als, Model::4.2 Advanced"
        + " Examples::4.2.2 Food Service Object Flow::OFControlLoopFoodService, true",
    "4.2.2 Food Service Object Flow - OFSingleFoodService.als, Model::4.2 Advanced Examples::4.2.2"
        + " Food Service Object Flow::OFSingleFoodService, true",
    "4.2.2 Food Service Object Flow - OFParallelFoodService.als, Model::4.2 Advanced"
        + " Examples::4.2.2 Food Service Object Flow::OFParallelFoodService, true",
  })
  /**
   * Compare manually created/expected alloy file and translator generated file.
   *
   * <p>Initialize the translator by loading the xmiFile check if output_and_testfiles_directory
   * exists. Then, the translator is reused for all the tests. First, create an alloy file from a
   * class named sysMLClassQualifiedName from Obm xmi file using Alloy API. Next the created alloy
   * file is imported using Alloy API again to find AllReachableFacts and
   * AllReachableUserDefinedSigs. Also, the manually created alloy file (manualFileName) is imported
   * using Alloy API to find its AllReachableFacts and AllReachableUserDefinedSigs. Finally, the
   * Sigs(signatures) and Expressions(Reachable facts) of manually created/expected and generated by
   * translator are compared.
   *
   * @param _expectedAlloyFileName(String) - absolute path for expected file name
   * @param _classQualifiedName(String) - class's qualifedName (i.e., Model::4.1 Basic
   *     Examples::4.1.1 Time Orderings::Fork)
   * @param expctedResult(boolean) - true if expected to pass the test, false if expected to fail
   *     the test
   */
  public void compare(
      String _expectedAlloyFileName, String _classQualifiedName, boolean _expectedResult) {

    System.out.println("Manually created alloy file = " + _expectedAlloyFileName);
    System.out.println("Comparing QualifiedName for a class = " + _classQualifiedName);
    File apiFile =
        new File(output_and_testfiles_directory, _expectedAlloyFileName + "_Generated" + ".als");

    // loading xmi file to create translator instance. This happens once with the first test.
    if (!initializeOBMXMI2Alloy()) {
      System.err.println("Failed to load xmiFile, so no testing performed.");
      return;
    }
    if (!translater.createAlloyFile(_classQualifiedName, apiFile)) {
      System.out.println(translater.getErrorMessages());
      fail(
          "failed to create generated file: "
              + apiFile.getName()
              + " "
              + translater.getErrorMessages());
    }
    // creating comparator
    ExpressionComparator ec = new ExpressionComparator();

    ////////////////////// Set up (Importing Modules) /////////////////////////////////////////
    // API
    System.out.println("==== Loading api generated file...");
    CompModule apiModule = AlloyUtils.importAlloyModule(apiFile);
    // TEST
    System.out.println("==== Loading test/manual file...");
    File testFile = new File(output_and_testfiles_directory, _expectedAlloyFileName);
    System.out.println("testFile: " + testFile.exists() + "? " + testFile.getAbsolutePath());
    CompModule testModule = AlloyUtils.importAlloyModule(testFile);

    ///////////////////////// Compare Model name //////////////////////////////////////
    assertEquals(apiModule.getModelName(), testModule.getModelName());

    //////////////////////// Comparing Reachable Facts ////////////////////////////////
    // API
    System.out.println("Comparing Reachable facts.....");
    Expr api_reachableFacts = apiModule.getAllReachableFacts(); // test.getOverallFacts();
    System.out.println(api_reachableFacts);
    // TEST
    Expr test_reachableFacts = testModule.getAllReachableFacts();
    System.out.println(test_reachableFacts);

    // Compare
    if (_expectedResult)
      assertTrue(ec.compareTwoExpressionsFacts(api_reachableFacts, test_reachableFacts));
    else assertFalse(ec.compareTwoExpressionsFacts(api_reachableFacts, test_reachableFacts));

    ///////////////////////// Comparing Signature ////////////////////
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

    // Compare - Signature size
    assertTrue(api_SigByName.size() == test_SigByName.size());

    // Compare - Each Signature (Signatures and Facts)
    for (String sigName : api_SigByName.keySet()) {
      Sig alloyFileSig = test_SigByName.get(sigName);
      Sig apiSig = api_SigByName.get(sigName);
      assertTrue(ec.compareTwoExpressionsSignatures(alloyFileSig, apiSig));
    }
  }

  /**
   * initialize the translator by loading xmi file and specify which directory translator result
   * files will be written.
   */
  private static boolean initializeOBMXMI2Alloy() {

    // load only once
    if (translater == null) {
      System.out.println("error log is available in error.log");
      // write any errors to be in error file
      try {
        PrintStream o = new PrintStream(new File(output_and_testfiles_directory, "error.log"));
        System.setErr(o);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        System.out.println(
            "output_and_testfiles_directory you specified \""
                + output_and_testfiles_directory
                + "\" is not valid");
        return false;
      }

      try {
        translater = new OBMXMI2Alloy(ombmodel_directory);
        translater.loadXmiFile(xmiFile);

      } catch (FileNotFoundException | UMLModelErrorException e) {
        e.printStackTrace();
        System.out.println("xmiFile you specified \"" + xmiFile + "\" is not valid");
        return false;
      }
    }
    // already loaded
    return true;
  }
}
