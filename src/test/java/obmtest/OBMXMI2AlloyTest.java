package obmtest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.OBMXMI2Alloy;
import edu.gatech.gtri.obm.translator.alloy.tofile.ExpressionComparator;
import edu.gatech.gtri.obm.translator.alloy.tofile.MyAlloyLibrary;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.umd.omgutil.UMLModelErrorException;

class OBMXMI2AlloyTest {

  @ParameterizedTest

  @CsvSource({
      "4.1.5 Multiple Execution Steps - Multiple Object Flow_mw.als, Model::Basic::ObjectFlowBehavior",
      "4.2.2 FoodService Object Flow - IFSingleFoodService - OFFoodService_mw.als, Model::Realistic::IFFoodService",
      "4.1.1 Control Nodes1 - SimpleSequence_modified.als,Model::Basic::SimpleSequence",
      "4.1.1 Control Nodes2 - Fork_modified.als, Model::Basic::BehaviorFork",
      "4.1.1 Control Nodes3 - Join_modified.als, Model::Basic::BehaviorJoin",
      "4.1.1 Control Nodes4 - Decision_modified.als, Model::Basic::BehaviorDecision",
      "4.1.1 Control Nodes5 - Merge_modified.als, Model::Basic::BehaviorMerge",
      "4.1.1 Control Nodes6 - CombinedControlNodes_modified.als, Model::Basic::ComplexBehavior",
      // failing fact is different
      "4.1.2 LoopsExamples_modified.als, Model::Basic::Loop",
      "4.1.3 CallingBehaviors_modified.als, Model::Basic::ComposedBehavior",
      "4.1.4 Transfers and Parameters1 - TransferProduct_modified.als, Model::Basic::ParticipantTransfer",
      "4.1.5 Multiple Execution Steps - Multiple Control Flow_modified.als, Model::Basic::ControlFlowBehavior",
      // 4.1.6
      // fails because p2 multiplicity
      "4.1.6 UnsatisfiableAsymmetry_mw.als, Model::Basic::UnsatisfiableAsymmetry",
      "4.1.6 UnsatisfiableTransitivity_mw.als, Model::Basic::UnsatisfiableTransitivity",
      "4.1.6 UnsatisfiableMultiplicity_mw.als, Model::Basic::UnsatisfiableMultiplicity",
      "4.1.6 UnsatisfiableComposition1_mw.als, Model::Basic::UnsatisfiableComposition1",
      "4.1.6 UnsatisfiableComposition2_mw.als, Model::Basic::UnsatisfiableComposition2",
      "4.2.1 FoodService Control Flow - FoodService_mw.als, Model::Realistic::FoodService",
      "4.2.1 FoodService Control Flow - SingleFoodService_mw.als, Model::Realistic::SingleFoodService",
      "4.2.1 FoodService Control Flow - BuffetService_mw.als, Model::Realistic::BuffetService",
      "4.2.1 FoodService Control Flow - ChurchSupperService_mw.als, Model::Realistic::ChurchSupper",
      "4.2.1 FoodService Control Flow - FastFoodService_mw.als, Model::Realistic::FastFoodService",
      "4.2.1 FoodService Control Flow - UsatisfiableFoodService_mw.als, Model::Realistic::UnsatisfiableService",
  //
  // TransferBefore
  // // wip waiting Jeremy's update obm file
  // // "4.1.4 Transfers and Parameters2 -
  // // ParameterBehavior.als,Model::Basic::ParameterBehavior",

  // // //
  })


  /**
   * 
   * @param manualFileName
   * @param className
   * @param fileToBecreatedFromXmiFile null if not to create outputfile
   * @throws FileNotFoundException
   * @throws UMLModelErrorException
   */
  void sameAbstractSyntaxTreeTestAndSignatures(String manualFileName, String className)
      throws FileNotFoundException, UMLModelErrorException {
    // PrintStream o = new PrintStream(new File("error.txt"));
    // // PrintStream console = System.out;
    // System.setErr(o);

    // System.setProperty(("java.io.tmpdir"),
    // "C:/Users/mw107/Documents/Projects/NIST OBM/info/obm-alloy-code_2023-05-26/obm");// find
    // transfer.als
    System.out.println("fileName = " + manualFileName);
    System.out.println("className = " + className);


    // ========== Create Alloy model from OBM XMI file & write as a file ==========

    OBMXMI2Alloy test = new OBMXMI2Alloy();
    File xmiFile = new File("src/test/resources/OBMModel_R.xmi");

    // File xmiFile = new File(
    // "C:/Users/mw107/Documents/Projects/NIST
    // OBM/GIT/NIST-OBM-Translator.git/develop_mw/target/classes/OBMModel_MW.xmi");
    System.out.println("XMIFile: " + xmiFile.exists() + "? " + xmiFile.getAbsolutePath());

    File apiFile = new File("src/test/resources/" + manualFileName + "_Generated-"
        + className.replaceAll("::", "_") /* alloyModule.getModuleName() */ + ".als");
    test.createAlloyFile(xmiFile, className, apiFile);


    ExpressionComparator ec = new ExpressionComparator();

    ////////////////////// Set up (Importing Modules) /////////////////////////////////////////
    // API
    CompModule apiModule = MyAlloyLibrary.importAlloyModule(apiFile);
    // TEST
    File testFile = new File("src/test/resources/" + manualFileName);
    System.out.println("testFile: " + testFile.exists() + "? " + testFile.getAbsolutePath());
    CompModule testModule = MyAlloyLibrary.importAlloyModule(testFile);


    //////////////////////// Comparing Reachable Facts ////////////////////////////////
    // API
    Expr api_reachableFacts = apiModule.getAllReachableFacts();// test.getOverallFacts();
    System.out.println(api_reachableFacts);
    // Test
    Expr test_reachableFacts = testModule.getAllReachableFacts();
    System.out.println(test_reachableFacts);
    // Compare
    assertTrue(ec.compareTwoExpressions(api_reachableFacts, test_reachableFacts));

    ///////////////////////// Comparing Sigs ////////////////////
    // ========== Set up Sigs ==========
    // API
    List<Sig> api_reachableDefinedSigs = apiModule.getAllReachableUserDefinedSigs();
    Map<String, Sig> api_SigByName = new HashMap<>();// test.getAllReachableUserDefinedSigs();
    for (Sig sig : api_reachableDefinedSigs) {
      api_SigByName.put(sig.label, sig);
    }
    // TEST
    List<Sig> test_reachableDefinedSigs = testModule.getAllReachableUserDefinedSigs();
    Map<String, Sig> test_SigByName = new HashMap<>();
    for (Sig sig : test_reachableDefinedSigs) {
      test_SigByName.put(sig.label, sig);
    }

    // Compare - Size
    assertTrue(api_SigByName.size() == test_SigByName.size());

    // Compare - each sig
    for (String sigName : api_SigByName.keySet()) {
      Sig alloyFileSig = test_SigByName.get(sigName);
      Sig apiSig = api_SigByName.get(sigName);
      assertTrue(ec.compareTwoExpressions(alloyFileSig, apiSig));
    }
  }


}
