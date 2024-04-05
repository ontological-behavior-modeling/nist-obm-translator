package manualtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.Translator;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.parser.CompModule;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import obmtest.ExpressionComparator;
import org.junit.jupiter.api.Test;

class FoodServiceControlFlowExplicitFactTest {

  @Test
  void test() {
    String moduleName = "FoodServiceControlFlow_ExplicitFast";
    String outFileName = "src/test/resources/generated-" + moduleName + ".als";
    String filename = "src/test/resources/4.2.1_FoodServiceControlFlow_ExplicitFacts.als";
    Alloy alloy = new Alloy("src/test/resources");

    // ========== Define list of signatures unique to the file ==========

    Sig orderSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Order");
    Sig prepareSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Prepare");
    Sig serveSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Serve");
    Sig eatSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Eat");
    Sig paySig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Pay");
    Sig foodServiceSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("FoodService");
    Sig singleFoodServiceSig =
        alloy.createSigAndAddToAllSigs("SingleFoodService", (Sig.PrimSig) foodServiceSig);
    Sig buffetServiceSig =
        alloy.createSigAndAddToAllSigs("BuffetService", (Sig.PrimSig) singleFoodServiceSig);
    Sig churchSupperServiceSig =
        alloy.createSigAndAddToAllSigs("ChurchSupperService", (Sig.PrimSig) singleFoodServiceSig);
    Sig fastFoodServiceSig =
        alloy.createSigAndAddToAllSigs("FastFoodService", (Sig.PrimSig) singleFoodServiceSig);
    Sig restaurantServiceSig =
        alloy.createSigAndAddToAllSigs("RestaurantService", (Sig.PrimSig) singleFoodServiceSig);
    Sig unsatisfiableFoodServiceSig =
        alloy.createSigAndAddToAllSigs(
            "UnsatisfiableFoodService", (Sig.PrimSig) singleFoodServiceSig);

    // ========== Define list of relations unique to the file ==========

    // FoodService fields:
    Sig.Field foodService_orderField = FuncUtils.addField("order", foodServiceSig, orderSig);
    Sig.Field foodService_prepareField = FuncUtils.addField("prepare", foodServiceSig, prepareSig);
    Sig.Field foodService_payField = FuncUtils.addField("pay", foodServiceSig, paySig);
    Sig.Field foodService_eatField = FuncUtils.addField("eat", foodServiceSig, eatSig);
    Sig.Field foodService_serveField = FuncUtils.addField("serve", foodServiceSig, serveSig);

    LinkedHashMap<Field, Sig> fieldTypeByField = new LinkedHashMap<>();
    fieldTypeByField.put(foodService_orderField, orderSig);
    fieldTypeByField.put(foodService_prepareField, prepareSig);
    fieldTypeByField.put(foodService_payField, paySig);
    fieldTypeByField.put(foodService_eatField, eatSig);
    fieldTypeByField.put(foodService_serveField, serveSig);

    // SingleFoodService fields: none
    // BuffetService fields: none
    // ChurchSupperService fields: none
    // FastFoodService fields: none
    // RestaurantService fields: none
    // UnsatisfiableFoodService fields: none

    // ========== Define explicit facts ==========

    // FoodService:

    ExprVar fs = ExprVar.make(null, "fs", foodServiceSig.type());

    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        fs, foodServiceSig, foodService_orderField, foodService_serveField);

    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        fs, foodServiceSig, foodService_prepareField, foodService_serveField);

    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        fs, foodServiceSig, foodService_serveField, foodService_eatField);

    // Map<String, Sig.Field> fieldByName = new LinkedHashMap<>();
    // fieldByName.put(foodService_orderField.label, foodService_orderField);
    // fieldByName.put(foodService_prepareField.label, foodService_prepareField);
    // fieldByName.put(foodService_payField.label, foodService_payField);
    // fieldByName.put(foodService_eatField.label, foodService_eatField);
    // fieldByName.put(foodService_serveField.label, foodService_serveField);

    Set<String> foodServiceSteps = new HashSet<String>();
    foodServiceSteps.add("order");
    foodServiceSteps.add("prepare");
    foodServiceSteps.add("pay");
    foodServiceSteps.add("eat");
    foodServiceSteps.add("serve");
    alloy.addSteps(foodServiceSig, foodServiceSteps);

    // SingleFoodService:

    ExprVar sfs = ExprVar.make(null, "sfs", singleFoodServiceSig.type());

    alloy.addOneConstraintToField(sfs, singleFoodServiceSig, foodService_orderField);
    alloy.addOneConstraintToField(sfs, singleFoodServiceSig, foodService_prepareField);
    alloy.addOneConstraintToField(sfs, singleFoodServiceSig, foodService_payField);
    alloy.addOneConstraintToField(sfs, singleFoodServiceSig, foodService_eatField);
    alloy.addOneConstraintToField(sfs, singleFoodServiceSig, foodService_serveField);

    // BuffetService:
    ExprVar bs = ExprVar.make(null, "bs", buffetServiceSig.type());
    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        bs, buffetServiceSig, foodService_prepareField, foodService_orderField);
    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        bs, buffetServiceSig, foodService_eatField, foodService_payField);

    // ChurchSupperService:
    Func bijectionFiltered = AlloyUtils.getFunction(Alloy.transferModule, "o/bijectionFiltered");
    Func happensBefore = AlloyUtils.getFunction(Alloy.transferModule, "o/happensBefore");
    ExprVar css = ExprVar.make(null, "css", churchSupperServiceSig.type());
    Decl cssDecl = new Decl(null, null, null, List.of(css), churchSupperServiceSig.oneOf());
    alloy.addToOverallFact(
        bijectionFiltered
            .call(
                happensBefore.call(),
                css.join(foodService_payField),
                css.join(foodService_prepareField))
            .forAll(cssDecl));
    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        css, churchSupperServiceSig, foodService_payField, foodService_orderField);

    // FastFoodService:
    ExprVar ffs = ExprVar.make(null, "ffs", fastFoodServiceSig.type());
    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        ffs, fastFoodServiceSig, foodService_orderField, foodService_payField);
    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        ffs, fastFoodServiceSig, foodService_payField, foodService_eatField);

    // RestaurantService:
    ExprVar rs = ExprVar.make(null, "rs", restaurantServiceSig.type());
    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        rs, restaurantServiceSig, foodService_eatField, foodService_payField);

    // UnsatisfiableFoodService
    ExprVar ufs = ExprVar.make(null, "ufs", unsatisfiableFoodServiceSig.type());
    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        ufs, unsatisfiableFoodServiceSig, foodService_eatField, foodService_payField);
    alloy.createBijectionFilteredHappensBeforeAndAddToOverallFact(
        ufs, unsatisfiableFoodServiceSig, foodService_payField, foodService_prepareField);

    // Functions and Predicates ==========

    // suppressTransfers: added by default by Alloy constructor
    // suppressIO: added by default by Alloy constructor

    ExprVar g = ExprVar.make(null, "g", foodServiceSig.type());

    // noChildFoodService
    Expr noChildFoodServiceExpr =
        buffetServiceSig
            .no()
            .and(churchSupperServiceSig.no())
            .and(fastFoodServiceSig.no())
            .and(restaurantServiceSig.no())
            .and(unsatisfiableFoodServiceSig.no());

    Func noChildFoodServiceFunc =
        new Func(null, "noChildFoodService", null, null, noChildFoodServiceExpr);

    // instancesDuringExample
    Expr instancesDuringExampleExpr =
        orderSig
            .in(foodServiceSig.join(foodService_orderField))
            .and(prepareSig.in(foodServiceSig.join(foodService_prepareField)))
            .and(serveSig.in(foodServiceSig.join(foodService_serveField)))
            .and(eatSig.in(foodServiceSig.join(foodService_eatField)))
            .and(paySig.in(foodServiceSig.join(foodService_payField)));
    Func instancesDuringExampleFunc =
        new Func(null, "instancesDuringExample", null, null, instancesDuringExampleExpr);

    // onlyFoodService
    Expr onlyFoodServiceExpr =
        foodServiceSig
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .and(singleFoodServiceSig.no())
            .and(noChildFoodServiceFunc.call());
    Func onlyFoodServiceFunc = new Func(null, "onlyFoodService", null, null, onlyFoodServiceExpr);

    // onlySingleFoodService
    Expr onlySingleFoodServiceExpr =
        foodServiceSig.in(singleFoodServiceSig).and(noChildFoodServiceFunc.call());
    Func onlySingleFoodServiceFunc =
        new Func(null, "onlySingleFoodService", null, null, onlySingleFoodServiceExpr);

    // onlyBuffetService
    Decl decl_obs = new Decl(null, null, null, List.of(g), foodServiceSig.oneOf());

    Expr onlyBuffetServiceExpr =
        buffetServiceSig
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .and(g.in(buffetServiceSig).forAll(decl_obs));

    Func onlyBuffetServiceFunc =
        new Func(null, "onlyBuffetService", null, null, onlyBuffetServiceExpr);

    // onlyChurchSupperService

    Decl decl_css = new Decl(null, null, null, List.of(g), churchSupperServiceSig.oneOf());

    Expr onlyChurchSupperServiceExpr =
        churchSupperServiceSig
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .and(g.in(churchSupperServiceSig).forAll(decl_css));

    Func onlyChurchSupperServiceFunc =
        new Func(null, "onlyChurchSupperService", null, null, onlyChurchSupperServiceExpr);

    // onlyFastFoodService

    Decl decl_offs = new Decl(null, null, null, List.of(g), fastFoodServiceSig.oneOf());

    Expr onlyFastFoodServiceExpr =
        foodServiceSig
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .and(g.in(fastFoodServiceSig).forAll(decl_offs));

    Func onlyFastFoodServiceFunc =
        new Func(null, "onlyFastFoodService", null, null, onlyFastFoodServiceExpr);

    // onlyRestaurantService

    Decl decl = new Decl(null, null, null, List.of(g), restaurantServiceSig.oneOf());

    Expr onlyRestaurantServiceExpr =
        restaurantServiceSig
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .and(g.in(restaurantServiceSig).forAll(decl));
    Func onlyRestaurantServiceFunc =
        new Func(null, "onlyRestaurantService", null, null, onlyRestaurantServiceExpr);

    // onlyUnsatisfiableFoodService
    Decl unsatisfiableFoodServiceDecl =
        new Decl(null, null, null, List.of(g), unsatisfiableFoodServiceSig.oneOf());

    Expr onlyUnsatisfiableFoodServiceExpr =
        unsatisfiableFoodServiceSig
            .cardinality()
            .equal(ExprConstant.makeNUMBER(1))
            .and(g.in(unsatisfiableFoodServiceSig).forAll(unsatisfiableFoodServiceDecl));

    Func onlyUnsatisfiableFoodServiceFunc =
        new Func(
            null, "onlyUnsatisfiableFoodService", null, null, onlyUnsatisfiableFoodServiceExpr);

    // ========== Commands ==========

    // nonZeroDurationOnly
    Func nonZeroDurationOnlyFunc =
        AlloyUtils.getFunction(Alloy.transferModule, "o/nonZeroDurationOnly");

    // suppressTransfers
    Sig transfer = AlloyUtils.getReachableSig(Alloy.transferModule, "o/Transfer");
    Expr suppressTransfersExpessionBody = transfer.no();
    Func suppressTransfersFunc =
        new Func(null, "suppressTransfers", null, null, suppressTransfersExpessionBody);

    // suppressIO
    Func inputs = AlloyUtils.getFunction(Alloy.transferModule, "o/inputs");
    Func outputs = AlloyUtils.getFunction(Alloy.transferModule, "o/outputs");
    Expr suppressIOExpressionBody = inputs.call().no().and(outputs.call().no());
    Func suppressIOFunc = new Func(null, "suppressIO", null, null, suppressIOExpressionBody);

    // showFoodService
    Expr showFoodServiceCmdExpr =
        nonZeroDurationOnlyFunc
            .call()
            .and(instancesDuringExampleFunc.call())
            .and(onlyFoodServiceFunc.call())
            .and(suppressTransfersFunc.call())
            .and(suppressIOFunc.call());

    Command showFoodServiceCmd =
        new Command(
            null,
            showFoodServiceCmdExpr,
            "showFoodService",
            false,
            10,
            -1,
            -1,
            -1,
            List.of(),
            List.of(),
            alloy.getOverAllFact().and(showFoodServiceCmdExpr),
            null);

    // showSingleFoodService
    Expr showSingleFoodServiceExpr =
        nonZeroDurationOnlyFunc
            .call()
            .and(instancesDuringExampleFunc.call())
            .and(onlySingleFoodServiceFunc.call())
            .and(suppressTransfersFunc.call())
            .and(suppressIOFunc.call());

    CommandScope ssf_cs = new CommandScope(singleFoodServiceSig, true, 1);

    Command showSingleFoodServiceCmd =
        new Command(
            null,
            showSingleFoodServiceExpr,
            "showSingleFoodService",
            false,
            10,
            -1,
            -1,
            -1,
            List.of(ssf_cs),
            List.of(),
            alloy.getOverAllFact().and(showSingleFoodServiceExpr),
            null);

    // showBuffetService
    Expr showBuffetServiceExpr =
        nonZeroDurationOnlyFunc
            .call()
            .and(instancesDuringExampleFunc.call())
            .and(onlyBuffetServiceFunc.call())
            .and(suppressTransfersFunc.call())
            .and(suppressIOFunc.call());

    Command showBuffetServiceCmd =
        new Command(
            null,
            showBuffetServiceExpr,
            "showBuffetService",
            false,
            10,
            -1,
            -1,
            -1,
            List.of(),
            List.of(),
            alloy.getOverAllFact().and(showBuffetServiceExpr),
            null);

    // showChurchSupperService
    Expr showChurchSupperServiceExpr =
        nonZeroDurationOnlyFunc
            .call()
            .and(instancesDuringExampleFunc.call())
            .and(onlyChurchSupperServiceFunc.call())
            .and(suppressTransfersFunc.call())
            .and(suppressIOFunc.call());

    Command showChurchSupperServiceCmd =
        new Command(
            null,
            showChurchSupperServiceExpr,
            "showChurchSupperService",
            false,
            10,
            -1,
            -1,
            -1,
            List.of(),
            List.of(),
            alloy.getOverAllFact().and(showChurchSupperServiceExpr),
            null);

    // showFastFoodService
    Expr showFastFoodServiceExpr =
        nonZeroDurationOnlyFunc
            .call()
            .and(instancesDuringExampleFunc.call())
            .and(onlyFastFoodServiceFunc.call())
            .and(suppressTransfersFunc.call())
            .and(suppressIOFunc.call());

    Command showFastFoodServiceCmd =
        new Command(
            null,
            showFastFoodServiceExpr,
            "showFastFoodService",
            false,
            10,
            -1,
            -1,
            -1,
            List.of(),
            List.of(),
            alloy.getOverAllFact().and(showFastFoodServiceExpr),
            null);

    // showRestaurantService
    Expr showRestaurantServiceExpr =
        nonZeroDurationOnlyFunc
            .call()
            .and(instancesDuringExampleFunc.call())
            .and(onlyRestaurantServiceFunc.call())
            .and(suppressTransfersFunc.call())
            .and(suppressIOFunc.call());

    Command showRestaurantServiceCmd =
        new Command(
            null,
            showRestaurantServiceExpr,
            "showRestaurantService",
            false,
            10,
            -1,
            -1,
            -1,
            List.of(),
            List.of(),
            alloy.getOverAllFact().and(showRestaurantServiceExpr),
            null);

    // showUnsatisfiableFoodService
    Expr showUnsatisfiableFoodServiceExpr =
        instancesDuringExampleFunc
            .call()
            .and(onlyUnsatisfiableFoodServiceFunc.call())
            .and(suppressTransfersFunc.call())
            .and(suppressIOFunc.call());

    Command showUnsatisfiableFoodServiceCmd =
        new Command(
            null,
            showUnsatisfiableFoodServiceExpr,
            "showUnsatisfiableFoodService",
            false,
            15,
            -1,
            -1,
            -1,
            List.of(),
            List.of(),
            alloy.getOverAllFact().and(showUnsatisfiableFoodServiceExpr),
            null);

    Command[] commands = {
      showFoodServiceCmd,
      showSingleFoodServiceCmd,
      showBuffetServiceCmd,
      showChurchSupperServiceCmd,
      showFastFoodServiceCmd,
      showRestaurantServiceCmd,
      showUnsatisfiableFoodServiceCmd
    };

    // ========== Create Alloy file version ==========

    CompModule importedModule = AlloyUtils.importAlloyModule(filename);

    // ========== Test if facts are equal ==========

    ExpressionComparator ec = new ExpressionComparator();

    Expr fileFacts = importedModule.getAllReachableFacts();
    Expr apiFacts = alloy.getOverAllFact();
    assertTrue(ec.compareTwoExpressions(fileFacts, apiFacts));

    // ========== Test if signatures are equal ==========

    List<Sig> fileSigs = importedModule.getAllReachableUserDefinedSigs();
    List<Sig> apiSigs = alloy.getAllSigs();
    Map<String, Sig> fileMap = new HashMap<>();
    Map<String, Sig> apiMap = new HashMap<>();

    for (Sig sig : fileSigs) {
      fileMap.put(AlloyUtils.removeSlash(sig.toString()), sig);
    }
    for (Sig sig : apiSigs) {
      apiMap.put(AlloyUtils.removeSlash(sig.toString()), sig);
    }

    assertTrue(fileSigs.size() == apiSigs.size());

    for (String sigName : fileMap.keySet()) {
      assertTrue(apiMap.containsKey(sigName));
      assertTrue(ec.compareTwoExpressions(fileMap.get(sigName), apiMap.get(sigName)));
    }

    // ========== Test if command(s) are equal ==========

    List<Command> importedCommands = importedModule.getAllCommands();

    assertEquals(commands.length, importedCommands.size());

    for (int i = 0; i < commands.length; i++) {
      assertTrue(ec.compareCommand(commands[i], importedCommands.get(i)));
    }

    // ========== Write file ==========

    AlloyModule alloyModule =
        new AlloyModule(moduleName, alloy.getAllSigs(), alloy.getOverAllFact(), commands);
    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());

    translator.generateAlsFileContents(alloyModule, outFileName);
  }
}
