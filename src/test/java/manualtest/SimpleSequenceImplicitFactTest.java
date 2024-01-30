package manualtest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.Translator;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import obmtest.ExpressionComparator;
import org.junit.jupiter.api.Test;

class SimpleSequenceImplicitFactTest {

  @Test
  void test() {

    String moduleName = "SimpleSequence_ImplicitFact";
    String outFileName = "src/test/resources/generated-" + moduleName + ".als";
    String filename =
        "src/test/resources/4.1.1 Control Nodes1 - SimpleSequence_Implicit_modified.als";
    Alloy alloy = new Alloy("src/test/resources");

    // ========== Define list of signatures unique to the file ==========

    Sig abSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("AtomicBehavior");
    Sig mainSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("SimpleSequence");

    // ========== Define list of relations unique to the file ==========

    Sig.Field p1 = FuncUtils.addField("p1", mainSig, abSig);
    Sig.Field p2 = FuncUtils.addField("p2", mainSig, abSig);

    // ========== Define implicit facts ==========

    Func stepsFunction = AlloyUtils.getFunction(Alloy.transferModule, "o/steps");
    Func functionFilteredFunction =
        AlloyUtils.getFunction(Alloy.transferModule, "o/functionFiltered");
    Func inverseFunctionFilteredFunction =
        AlloyUtils.getFunction(Alloy.transferModule, "o/inverseFunctionFiltered");
    Func happensBefore = AlloyUtils.getFunction(Alloy.transferModule, "o/happensBefore");

    ExprVar thisVar = ExprVar.make(null, "this", mainSig.type());

    Expr functionFilteredExpression =
        functionFilteredFunction.call(happensBefore.call(), thisVar.join(p1), thisVar.join(p2));
    Expr inverseFunctionFilteredExpression =
        inverseFunctionFilteredFunction.call(
            happensBefore.call(), thisVar.join(p1), thisVar.join(p2));

    mainSig.addFact(
        functionFilteredExpression
            .and(inverseFunctionFilteredExpression)
            .and(thisVar.join(p1).cardinality().equal(ExprConstant.makeNUMBER(1)))
            .and(thisVar.join(p1).plus(thisVar.join(p2)).in(thisVar.join(stepsFunction.call())))
            .and(thisVar.join(stepsFunction.call()).in(thisVar.join(p1).plus(thisVar.join(p2)))));

    // ========== Define functions and predicates ==========

    // Func nonZeroDurationOnlyFunction = Helper.getFunction(Alloy.transferModule,
    // "o/nonZeroDurationOnly");
    // Expr nonZeroDurationOnlyFunctionExpression = nonZeroDurationOnlyFunction.call();

    Sig transfer = AlloyUtils.getReachableSig(Alloy.transferModule, "o/Transfer");
    Expr suppressTransfersExpessionBody = transfer.no();
    Func suppressTransfersFunction =
        new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);
    Expr suppressTransfersExpression = suppressTransfersFunction.call();

    Func inputs = AlloyUtils.getFunction(Alloy.transferModule, "o/inputs");
    Func outputs = AlloyUtils.getFunction(Alloy.transferModule, "o/outputs");
    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
    Func suppressIOFunction = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);
    Expr suppressIOExpression = suppressIOFunction.call();

    Func p1DuringExamplePredicate =
        new Func(null, "p1DuringExample", new ArrayList<>(), null, abSig.in(mainSig.join(p1)));
    Expr p1DuringExampleExpression = p1DuringExamplePredicate.call();

    Func p2DuringExamplePredicate =
        new Func(null, "p2DuringExample", new ArrayList<>(), null, abSig.in(mainSig.join(p2)));
    Expr p2DuringExampleExpression = p2DuringExamplePredicate.call();

    Func instancesDuringExamplePredicate =
        new Func(
            null,
            "instancesDuringExample",
            new ArrayList<>(),
            null,
            p1DuringExampleExpression.and(p2DuringExampleExpression));
    Expr instancesDuringExampleExpression = instancesDuringExamplePredicate.call();

    Func onlySimpleSequencePredicate =
        new Func(
            null,
            "onlySimpleSequence",
            new ArrayList<>(),
            null,
            mainSig.cardinality().equal(ExprConstant.makeNUMBER(1)));
    Expr onlySimpleSequenceExpression = onlySimpleSequencePredicate.call();

    Expr predicates =
        (suppressTransfersExpression)
            .and(suppressIOExpression)
            .and(instancesDuringExampleExpression)
            .and(onlySimpleSequenceExpression);

    // ========== Done creating AST ==========

    // ========== Define command ==========

    Expr simpleSequencImplicitFactExpr =
        (suppressTransfersExpression)
            .and(suppressIOExpression)
            .and(instancesDuringExampleExpression)
            .and(onlySimpleSequenceExpression);

    Command simpleSequenceImplicitFactCmd =
        new Command(
            null,
            simpleSequencImplicitFactExpr,
            "SimpleSequence",
            false,
            6,
            -1,
            -1,
            -1,
            Arrays.asList(new CommandScope[] {}),
            Arrays.asList(new Sig[] {}),
            simpleSequencImplicitFactExpr.and(alloy.getOverAllFact()),
            null);

    // ========== Write file ==========

    Command[] commands = new Command[] {simpleSequenceImplicitFactCmd};

    AlloyModule alloyModule =
        new AlloyModule(
            "SimpleSequence_ImplicitFact", alloy.getAllSigs(), alloy.getOverAllFact(), commands);

    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());

    translator.generateAlsFileContents(alloyModule, outFileName);

    // ========== Import real AST from file ==========

    CompModule importedModule = AlloyUtils.importAlloyModule(filename);

    CompModule apiModule = AlloyUtils.importAlloyModule(outFileName);

    // ========== Test if they are equal ==========

    ExpressionComparator ec = new ExpressionComparator();

    Expr fileFacts = importedModule.getAllReachableFacts();
    Expr apiFacts = apiModule.getAllReachableFacts();
    List<Sig> fileSigs = importedModule.getAllReachableUserDefinedSigs();
    List<Sig> apiSigs = apiModule.getAllReachableUserDefinedSigs();

    Map<String, Sig> fileMap = new HashMap<>();
    Map<String, Sig> apiMap = new HashMap<>();

    for (Sig sig : fileSigs) {
      fileMap.put(AlloyUtils.removeSlash(sig.toString()), sig);
    }
    for (Sig sig : apiSigs) {
      apiMap.put(AlloyUtils.removeSlash(sig.toString()), sig);
    }

    assertTrue(ec.compareTwoExpressions(fileFacts, apiFacts));
    assertTrue(fileSigs.size() == apiSigs.size());

    for (String sigName : fileMap.keySet()) {
      assertTrue(apiMap.containsKey(sigName));
      assertTrue(ec.compareTwoExpressions(fileMap.get(sigName), apiMap.get(sigName)));
    }
  }
}
