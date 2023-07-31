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
  @CsvSource({// "generated-ComplexBehavior_MW.als, Model::Basic::ComplexBehavior_MW",
      "generated-BehaviorFork.als, Model::Basic::BehaviorFork",
  // "generated-BehaviorJoin.als, Model::Basic::BehaviorJoin",
  // "generated-ComplexBehavior.als, Model::Basic::ComplexBehavior",
  // "generated-ControlFlowBehavior.als, Model::Basic::ControlFlowBehavior",
  // "generated-BehaviorDecision.als, Model::Basic::BehaviorDecision"
  // "fileName, className"
  })

  // @CsvSource({"OriginalBehaviorFork.als, Model::Basic::BehaviorFork",
  // "BehaviorJoinFileDoesNotExist.als, Model::Basic::BehaviorJoin",
  // "ComplexBehaviorFileDoesNotExist.als, Model::Basic::ComplexBehavior",
  // "ControlFlowBehaviorFileDoesNotExist.als, Model::Basic::ControlFlowBehavior",
  // "BehaviorDecisionFileDoesNotExist.als, Model::Basic::BehaviorDecision"
  //// "fileName, className"
  // })
  void sameAbstractSyntaxTreeTestAndSignatures(String fileName, String className)
      throws FileNotFoundException, UMLModelErrorException {
    // PrintStream o = new PrintStream(new File("error.txt"));
    // // PrintStream console = System.out;
    // System.setErr(o);

    // System.setProperty(("java.io.tmpdir"),
    // "C:/Users/mw107/Documents/Projects/NIST OBM/info/obm-alloy-code_2023-05-26/obm");// find
    // transfer.als
    System.out.println("fileName = " + fileName);
    System.out.println("className = " + className);

    // ========== Create Alloy model from SysML ==========

    OBMXMI2Alloy test = new OBMXMI2Alloy();
    // File xmiFile = new File(OBMXMI2Alloy.class.getResource("/OBMModel_MW.xmi").getFile());
    File xmiFile = new File(
        "C:/Users/mw107/Documents/Projects/NIST OBM/GIT/NIST-OBM-Translator.git/develop_mw/target/classes/OBMModel_MW.xmi");
    System.out.println(xmiFile.exists());
    test.createAlloyFile(xmiFile, className);

    // File testFile = new File(OBMXMI2AlloyTest.class.getResource("/" + fileName).getFile());
    File testFile = new File(
        "C:\\Users\\mw107\\Documents\\Projects\\NIST OBM\\info\\obm-alloy-code_2023-05-26\\obm\\"
            + fileName);

    // ========== Create Alloy model from Alloy file ==========
    CompModule importedModule = MyAlloyLibrary.importAlloyModule(testFile);


    // ========== Compare abstract syntax trees ==========

    ExpressionComparator ec = new ExpressionComparator();

    Expr sysmlAbstractSyntaxTree = test.getOverallFacts();
    Expr alloyFileAbstractSyntaxTree = importedModule.getAllReachableFacts();

    System.out.println(sysmlAbstractSyntaxTree);
    System.out.println(alloyFileAbstractSyntaxTree);

    assertTrue(ec.compareTwoExpressions(sysmlAbstractSyntaxTree, alloyFileAbstractSyntaxTree));

    // ========== Set up signatures ==========

    List<Sig> alloyFileSignatures = importedModule.getAllReachableUserDefinedSigs();

    Map<String, Sig> sysmlSigMap = test.getAllReachableUserDefinedSigs();
    Map<String, Sig> alloyFileSigMap = new HashMap<>();

    for (Sig sig : alloyFileSignatures) {
      alloyFileSigMap.put(sig.label, sig);
    }

    // ========== Compare the number of signatures ==========
    assertTrue(sysmlSigMap.size() == alloyFileSigMap.size());

    System.out.println(alloyFileSigMap);
    System.out.println(sysmlSigMap);

    // ========== Compare each signature ==========

    for (String sigName : sysmlSigMap.keySet()) {
      System.out.println(alloyFileSigMap.get(sigName));
      System.out.println(sysmlSigMap.get(sigName));
      Sig alloyFileSig = alloyFileSigMap.get(sigName);
      Sig sysmlSig = sysmlSigMap.get(sigName);
      if (alloyFileSig == null)
        alloyFileSig = alloyFileSigMap.get("this/" + sigName);// this/BehaviorFork

      System.out.println(alloyFileSig);
      assertTrue(ec.compareTwoExpressions(alloyFileSig, sysmlSig));
    }
  }


}
