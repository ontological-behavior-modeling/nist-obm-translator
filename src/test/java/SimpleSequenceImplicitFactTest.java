import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.Helper;
import edu.gatech.gtri.obm.translator.alloy.tofile.ExpressionComparator;
import edu.gatech.gtri.obm.translator.alloy.tofile.MyAlloyLibrary;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;

class SimpleSequenceImplicitFactTest {

  @Test
  void test() throws UnsupportedEncodingException {

    Alloy alloy = new Alloy();

    // ========== Define list of signatures unique to the file ==========

    Sig p1Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P1");
    Sig p2Sig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("P2");
    Sig mainSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("SimpleSequence");

    // ========== Define list of relations unique to the file ==========

    Sig.Field p1 = FuncUtils.addField("p1", mainSig, p1Sig);
    Sig.Field p2 = FuncUtils.addField("p2", mainSig, p2Sig);

    // ========== Define implicit facts ==========

    Func stepsFunction = Helper.getFunction(Alloy.transferModule, "o/steps");
    Func functionFilteredFunction = Helper.getFunction(Alloy.transferModule, "o/functionFiltered");
    Func inverseFunctionFilteredFunction =
        Helper.getFunction(Alloy.transferModule, "o/inverseFunctionFiltered");
    Func happensBefore = Helper.getFunction(Alloy.transferModule, "o/happensBefore");

    ExprVar thisVar = ExprVar.make(null, "this", mainSig.type());

    Expr functionFilteredExpression =
        functionFilteredFunction.call(happensBefore.call(), thisVar.join(p1), thisVar.join(p2));
    Expr inverseFunctionFilteredExpression = inverseFunctionFilteredFunction
        .call(happensBefore.call(), thisVar.join(p1), thisVar.join(p2));

    mainSig.addFact(functionFilteredExpression.and(inverseFunctionFilteredExpression)
        .and(thisVar.join(p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
        .and(thisVar.join(p1).plus(thisVar.join(p2)).in(thisVar.join(stepsFunction.call())))
        .and(thisVar.join(stepsFunction.call()).in(thisVar.join(p1).plus(thisVar.join(p2)))));



    // ========== Define functions and predicates ==========

    Func nonZeroDurationOnlyFunction =
        Helper.getFunction(Alloy.transferModule, "o/nonZeroDurationOnly");
    Expr nonZeroDurationOnlyFunctionExpression = nonZeroDurationOnlyFunction.call();

    Sig transfer = Helper.getReachableSig(Alloy.transferModule, "o/Transfer");
    Expr suppressTransfersExpessionBody = transfer.no();
    Func suppressTransfersFunction =
        new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
    Expr suppressTransfersExpression = suppressTransfersFunction.call();

    Func inputs = Helper.getFunction(Alloy.transferModule, "o/inputs");
    Func outputs = Helper.getFunction(Alloy.transferModule, "o/outputs");
    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
    Func suppressIOFunction = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
    Expr suppressIOExpression = suppressIOFunction.call();

    Func p1DuringExamplePredicate =
        new Func(null, "p1DuringExample", new ArrayList<>(), null, p1Sig.in(mainSig.join(p1)));
    Expr p1DuringExampleExpression = p1DuringExamplePredicate.call();

    Func p2DuringExamplePredicate =
        new Func(null, "p2DuringExample", new ArrayList<>(), null, p2Sig.in(mainSig.join(p2)));
    Expr p2DuringExampleExpression = p2DuringExamplePredicate.call();

    Func instancesDuringExamplePredicate = new Func(null, "instancesDuringExample",
        new ArrayList<>(), null, p1DuringExampleExpression.and(p2DuringExampleExpression));
    Expr instancesDuringExampleExpression = instancesDuringExamplePredicate.call();

    Func onlySimpleSequencePredicate = new Func(null, "onlySimpleSequence", new ArrayList<>(), null,
        mainSig.cardinality().equal(ExprConstant.makeNUMBER(1)));
    Expr onlySimpleSequenceExpression = onlySimpleSequencePredicate.call();

    Expr predicates = nonZeroDurationOnlyFunctionExpression.and(suppressTransfersExpression)
        .and(suppressIOExpression).and(instancesDuringExampleExpression)
        .and(onlySimpleSequenceExpression);


    // ========== Done creating AST ==========

    // ========== Import real AST from file ==========

    String path = URLDecoder.decode(
        CallingBehaviorsTest.class.getResource("/SimpleSequence_ImplicitFact.als").getPath(),
        "UTF-8");
    File testFile = new File(path);
    if (testFile.exists())
      assertFalse(false, testFile.getAbsolutePath() + " is missing");
    CompModule importedModule = MyAlloyLibrary.importAlloyModule(testFile);

    // ========== Test if they are equal ==========

    ExpressionComparator ec = new ExpressionComparator();

    Expr fileFacts = importedModule.getAllReachableFacts();
    Expr apiFacts = alloy.getOverAllFact();
    List<Sig> fileSigs = importedModule.getAllReachableUserDefinedSigs();
    List<Sig> apiSigs = alloy.getAllSigs();

    Map<String, Sig> fileMap = new HashMap<>();
    Map<String, Sig> apiMap = new HashMap<>();

    for (Sig sig : fileSigs) {
      fileMap.put(MyAlloyLibrary.removeSlash(sig.toString()), sig);
    }
    for (Sig sig : apiSigs) {
      apiMap.put(MyAlloyLibrary.removeSlash(sig.toString()), sig);
    }

    assertTrue(ec.compareTwoExpressions(fileFacts, apiFacts));
    assertTrue(fileSigs.size() == apiSigs.size());

    for (String sigName : fileMap.keySet()) {
      assertTrue(apiMap.containsKey(sigName));
      assertTrue(ec.compareTwoExpressions(fileMap.get(sigName), apiMap.get(sigName)));
    }
  }

}
