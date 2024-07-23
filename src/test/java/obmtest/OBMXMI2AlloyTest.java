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


/**
 * JUnit Test for translation.
 * 
 * @author Miyako Wilson, AE(ASDL) - Georgia Tech
 * @author Andrew H Shinjo, Graduate Student - Georgia Tech
 * 
 */
class OBMXMI2AlloyTest {

  static OBMXMI2Alloy translater;
  static final String ombmodel_dir = "src/test/resources";
  static final String output_and_testfiles_dir = "src/test/resources";
  static final File xmiFile = new File(ombmodel_dir, "OBMModel.xmi");


  @ParameterizedTest
  @CsvSource({

      // alloy file in name, qualifiedName of behavior model(class), boolean true if expected to pass, false if expected to fail
      "4.1.4 Transfers and Parameters - ParameterBehavior_mw.als, Model::4.1 Basic Examples::4.1.4 Transfers and Parameters::ParameterBehavior, true",
      "4.1.4 Transfers and Parameters - TransferProduct.als, Model::4.1 Basic Examples::4.1.4 Transfers and Parameters::TransferProduct, true",
      "4.1.5 Steps with Multiple Executions - MultipleControlFlow.als, Model::4.1 Basic Examples::4.1.5 Steps with Multiple Executions::MultipleControlFlow, true",
      "4.1.5 Steps with Multiple Executions - MultipleObjectFlow_mw.als, Model::4.1 Basic Examples::4.1.5 Steps with Multiple Executions::MultipleObjectFlow, true",


      "4.2.2 Food Service Object Flow - OFControlLoopFoodService_mw.als, Model::4.2 Advanced Examples::4.2.2 Food Service Object Flow::OFControlLoopFoodService, true",
      "4.2.2 Food Service Object Flow - OFSingleFoodService.als, Model::4.2 Advanced Examples::4.2.2 Food Service Object Flow::OFSingleFoodService, true",
      "4.2.2 Food Service Object Flow - OFParallelFoodService.als, Model::4.2 Advanced Examples::4.2.2 Food Service Object Flow::OFParallelFoodService, true",

      // expected to fail
      "4.1.5 Steps with Multiple Executions - MultipleControlFlow - Fail.als, Model::4.1 Basic Examples::4.1.5 Steps with Multiple Executions::MultipleControlFlow, false",

      "4.1.1 Time Orderings - AllControl.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::AllControl, true",
      "4.1.1 Time Orderings - Decision.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Decision, true",
      "4.1.1 Time Orderings - Fork.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Fork, true",
      "4.1.1 Time Orderings - Join.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Join, true",
      "4.1.1 Time Orderings - Merge.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::Merge, true",
      "4.1.1 Time Orderings - SimpleSequence.als, Model::4.1 Basic Examples::4.1.1 Time Orderings::SimpleSequence, true",
      "4.1.2 Loops - Loop.als, Model::4.1 Basic Examples::4.1.2 Loop::Loop ,true",
      "4.1.3 Behaviors with Steps - Composed.als, Model::4.1 Basic Examples::4.1.3 Behaviors with Steps::Composed, true",

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
  void compare(String manualFileName, String sysMLClassQualifiedName,
      boolean expectedResult)
      throws FileNotFoundException, UMLModelErrorException {

    System.out.println("Manually created alloy file = " + manualFileName);
    System.out.println("Comparing QualifiedName for a class = " + sysMLClassQualifiedName);
    File apiFile = new File(output_and_testfiles_dir,
        manualFileName + "_Generated" + ".als");


    initializeOBMXMI2Alloy();
    if (!translater.createAlloyFile(sysMLClassQualifiedName, apiFile)) {
      System.out.println(translater.getErrorMessages());
      fail("failed to create generated file: " + apiFile.getName() + " "
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
      assertTrue(ec.compareTwoExpressionsSigs(alloyFileSig, apiSig));
    }
  }

  static void initializeOBMXMI2Alloy() {// throws FileNotFoundException, UMLModelErrorException {

    if (translater == null) {
      System.out.println("error log is available in error.log");
      // write any errors to be in error file
      try {
        PrintStream o = new PrintStream(new File(output_and_testfiles_dir, "error.log"));
        System.setErr(o);
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      try {
        translater =
            new OBMXMI2Alloy(output_and_testfiles_dir);
        translater.loadXmiFile(xmiFile);

      } catch (FileNotFoundException | UMLModelErrorException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
