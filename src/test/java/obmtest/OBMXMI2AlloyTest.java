package obmtest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

  // OBMModel_modified.xmi
  // ComposedBehavior NextedBehavior.p5 multiplicity -> 1 to undefined.


  @ParameterizedTest
  @CsvSource({"4.1.1 Control Nodes1 - SimpleSequence_modified.als, Model::Basic::SimpleBehavior",
      "4.1.1 Control Nodes2 - Fork_modified.als, Model::Basic::BehaviorFork",
      "4.1.1 Control Nodes3 - Join_modified.als, Model::Basic::BehaviorJoin",
      "4.1.1 Control Nodes4 - Decision_modified.als, Model::Basic::BehaviorDecision",
      "4.1.1 Control Nodes5 - Merge_modified.als, Model::Basic::BehaviorMerge",
      "4.1.1 Control Nodes6 - CombinedControlNodes_modified.als, Model::Basic::ComplexBehavior",
      "4.1.2 LoopsExamples_modified.als, Model::Basic::Loop", //
      "4.1.3 CallingBehaviors_modified.als, Model::Basic::ComposedBehavior",
//      "4.1.4 Transfers and Parameters1 - TransferProduct.als, Model::Basic::ParticipantTransfer"//
  // 4.1.4
  // "generated-UnsatisfiableMultiplicity.als, Model::Basic::UnsatisfiableMultiplicity", //
  // 4.1.6
  // "generated-UnsatisfiableAsymmetry.als, Model::Basic::UnsatisfiableAsymmetry",
  // "generated-UnsatisfiableTransitivity.als, Model::Basic::UnsatisfiableTransitivity",
  // "generated-UnsatisfiableComposition1.als, Model::Basic::UnsatisfiableComposition1",
  // "generated-UnsatisfiableComposition2.als, Model::Basic::UnsatisfiableComposition2",


  // "fileName, className"
  // "generated-ComplexBehavior_MW.als, Model::Basic::ComplexBehavior_MW",
  // "generated-ControlFlowBehavior.als, Model::Basic::ControlFlowBehavior",

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
    File xmiFile = new File("src/test/resources/OBMModel_R.xmi");

    // File xmiFile = new File(
    // "C:/Users/mw107/Documents/Projects/NIST
    // OBM/GIT/NIST-OBM-Translator.git/develop_mw/target/classes/OBMModel_MW.xmi");
    System.out.println("XMIFile: " + xmiFile.exists() + "? " + xmiFile.getAbsolutePath());
    test.createAlloyFile(xmiFile, className);
    
    String genFileName = "generated-" + className.replaceAll("::", "_") + ".als";
    
    Path source = Paths.get(genFileName);
    Path newdir = Paths.get("src/test/resources/");
    try {
  		Files.move(source, newdir.resolve(source.getFileName()),StandardCopyOption.REPLACE_EXISTING);
  	} catch (IOException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}
    
    File testFile = new File("src/test/resources/" + fileName);
    
    // File testFile = new File(
    // "C:\\Users\\mw107\\Documents\\Projects\\NIST OBM\\info\\obm-alloy-code_2023-05-26\\obm\\"
    // + fileName);
    System.out.println("testFile: " + testFile.exists() + "? " + testFile.getAbsolutePath());
    
    String generatedFileName = "generated-" + className.replaceAll("::", "_") + ".als";
    File generatedFile = new File("src/test/resources/" + generatedFileName);
    System.out.println("generatedFile: " + generatedFile.exists() + "? " + generatedFile.getAbsolutePath());


    // ========== Create Alloy model from Alloy file ==========
    CompModule importedModule = MyAlloyLibrary.importAlloyModule(testFile);
    CompModule generatedModule = MyAlloyLibrary.importAlloyModule(generatedFile);


    // ========== Compare abstract syntax trees ==========

    ExpressionComparator ec = new ExpressionComparator();

    Expr sysmlAbstractSyntaxTree = generatedModule.getAllReachableFacts();
    Expr alloyFileAbstractSyntaxTree = importedModule.getAllReachableFacts();

    System.out.println(sysmlAbstractSyntaxTree);
    System.out.println(alloyFileAbstractSyntaxTree);

    assertTrue(ec.compareTwoExpressions(sysmlAbstractSyntaxTree, alloyFileAbstractSyntaxTree));

    // ========== Set up signatures ==========

    List<Sig> alloyFileSignatures = importedModule.getAllReachableUserDefinedSigs();
    List<Sig> generatedFileSignatures = generatedModule.getAllReachableUserDefinedSigs();
    Map<String, Sig> alloyFileSigMap = new HashMap<>();
    Map<String, Sig> genFileSigMap = new HashMap<>();
    
    for (Sig sig : alloyFileSignatures) {
      alloyFileSigMap.put(sig.label, sig);
    }
    for (Sig sig : generatedFileSignatures) {
      genFileSigMap.put(sig.label, sig);
    }

    // ========== Compare the number of signatures ==========
    assertTrue(genFileSigMap.size() == alloyFileSigMap.size());

    System.out.println(alloyFileSigMap);
    System.out.println(genFileSigMap);

    // ========== Compare each signature ==========

    for (String sigName : genFileSigMap.keySet()) {
      // System.out.println(alloyFileSigMap.get(sigName));
      // System.out.println(sysmlSigMap.get(sigName));
      Sig alloyFileSig = alloyFileSigMap.get(sigName);
      Sig sysmlSig = genFileSigMap.get(sigName);
      if (alloyFileSig == null)
        alloyFileSig = alloyFileSigMap.get("this/" + sigName);// this/BehaviorFork

      // System.out.println(alloyFileSig);
      assertTrue(ec.compareTwoExpressions(alloyFileSig, sysmlSig));
    }
  }


}
