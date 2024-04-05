package manualtest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.gatech.gtri.obm.translator.alloy.Alloy;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.FuncUtils;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.Translator;
import edu.gatech.gtri.obm.translator.alloy.tofile.AlloyModule;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprHasName;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import obmtest.ExpressionComparator;
import org.junit.jupiter.api.Test;

class ControlNodesTest {

  @Test
  void test() {

    String modulename = "ControlNodes";
    String outFileName = "src/test/resources/generated-" + modulename + ".als";
    String manualfilename = "src/test/resources/4.1.1 ControlNodesExamples.als";
    Alloy alloy = new Alloy("src/test/resources");

    // ========== Define list of signatures unique to the file ==========

    Sig abSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("AtomicBehavior");
    Sig simpleSequenceSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("SimpleSequence");
    Sig forkSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Fork");
    Sig joinSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Join");
    Sig decisionSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Decision");
    Sig mergeSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("Merge");
    Sig allControlSig = alloy.createSigAsChildOfOccSigAndAddToAllSigs("AllControl");

    // ========== Define list of relations unique to the file ==========

    Sig.Field simpleSequence_p1 = FuncUtils.addField("p1", simpleSequenceSig, abSig);
    Sig.Field simpleSequence_p2 = FuncUtils.addField("p2", simpleSequenceSig, abSig);

    Sig.Field fork_p1 = FuncUtils.addField("p1", forkSig, abSig);
    Sig.Field fork_p2 = FuncUtils.addField("p2", forkSig, abSig);
    Sig.Field fork_p3 = FuncUtils.addField("p3", forkSig, abSig);

    Sig.Field join_p1 = FuncUtils.addField("p1", joinSig, abSig);
    Sig.Field join_p2 = FuncUtils.addField("p2", joinSig, abSig);
    Sig.Field join_p3 = FuncUtils.addField("p3", joinSig, abSig);

    Sig.Field decision_p1 = FuncUtils.addField("p1", decisionSig, abSig);
    Sig.Field decision_p2 = FuncUtils.addField("p2", decisionSig, abSig);
    Sig.Field decision_p3 = FuncUtils.addField("p3", decisionSig, abSig);

    Sig.Field merge_p1 = FuncUtils.addField("p1", mergeSig, abSig);
    Sig.Field merge_p2 = FuncUtils.addField("p2", mergeSig, abSig);
    Sig.Field merge_p3 = FuncUtils.addField("p3", mergeSig, abSig);

    Sig.Field allControl_p1 = FuncUtils.addField("p1", allControlSig, abSig);
    Sig.Field allControl_p2 = FuncUtils.addField("p2", allControlSig, abSig);
    Sig.Field allControl_p3 = FuncUtils.addField("p3", allControlSig, abSig);
    Sig.Field allControl_p4 = FuncUtils.addField("p4", allControlSig, abSig);
    Sig.Field allControl_p5 = FuncUtils.addField("p5", allControlSig, abSig);
    Sig.Field allControl_p6 = FuncUtils.addField("p6", allControlSig, abSig);
    Sig.Field allControl_p7 = FuncUtils.addField("p7", allControlSig, abSig);

    // ========== Define implicit facts ==========

    // SimpleSequence

    Func stepsFunction = AlloyUtils.getFunction(alloy.transferModule, "o/steps");
    Func functionFilteredFunction =
        AlloyUtils.getFunction(alloy.transferModule, "o/functionFiltered");
    Func inverseFunctionFilteredFunction =
        AlloyUtils.getFunction(alloy.transferModule, "o/inverseFunctionFiltered");
    Func happensBefore = AlloyUtils.getFunction(alloy.transferModule, "o/happensBefore");

    ExprVar s1 = ExprVar.make(null, "x", simpleSequenceSig.type());
    ExprVar s2 = ExprVar.make(null, "x", simpleSequenceSig.type());
    ExprVar s3 = ExprVar.make(null, "x", simpleSequenceSig.type());
    ExprVar s4 = ExprVar.make(null, "x", simpleSequenceSig.type());
    ExprVar s5 = ExprVar.make(null, "x", simpleSequenceSig.type());
    ExprVar s6 = ExprVar.make(null, "x", simpleSequenceSig.type());

    List<ExprHasName> names1 = new ArrayList<>(List.of(s1));
    List<ExprHasName> names2 = new ArrayList<>(List.of(s2));
    List<ExprHasName> names3 = new ArrayList<>(List.of(s3));
    List<ExprHasName> names4 = new ArrayList<>(List.of(s4));
    List<ExprHasName> names5 = new ArrayList<>(List.of(s5));
    List<ExprHasName> names6 = new ArrayList<>(List.of(s6));

    Decl decl1 = new Decl(null, null, null, names1, simpleSequenceSig.oneOf());
    Decl decl2 = new Decl(null, null, null, names2, simpleSequenceSig.oneOf());
    Decl decl3 = new Decl(null, null, null, names3, simpleSequenceSig.oneOf());
    Decl decl4 = new Decl(null, null, null, names4, simpleSequenceSig.oneOf());
    Decl decl5 = new Decl(null, null, null, names5, simpleSequenceSig.oneOf());
    Decl decl6 = new Decl(null, null, null, names6, simpleSequenceSig.oneOf());

    Expr functionFilteredExpr =
        functionFilteredFunction.call(
            happensBefore.call(),
            s1.join(simpleSequenceSig.domain(simpleSequence_p1)),
            s1.join(simpleSequenceSig.domain(simpleSequence_p2)));

    Expr inverseFunctionFilteredExpr =
        inverseFunctionFilteredFunction.call(
            happensBefore.call(),
            s2.join(simpleSequenceSig.domain(simpleSequence_p1)),
            s2.join(simpleSequenceSig.domain(simpleSequence_p2)));

    alloy.addToOverallFact(
        (functionFilteredExpr.forAll(decl1))
            .and(inverseFunctionFilteredExpr.forAll(decl2))
            .and(
                s3.join(simpleSequenceSig.domain(simpleSequence_p1))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(decl3))
            .and(
                s4.join(simpleSequenceSig.domain(simpleSequence_p2))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(decl4))
            .and(
                s5.join(simpleSequenceSig.domain(simpleSequence_p1))
                    .plus(s5.join(simpleSequence_p2))
                    .in(s5.join(stepsFunction.call()))
                    .forAll(decl5))
            .and(
                s6.join(stepsFunction.call())
                    .in(
                        s6.join(simpleSequenceSig.domain(simpleSequence_p1))
                            .plus(s6.join(simpleSequence_p2)))
                    .forAll(decl6)));

    // // Fork

    Func bijectionFilteredFunction =
        AlloyUtils.getFunction(alloy.transferModule, "o/bijectionFiltered");

    ExprVar forkThis = ExprVar.make(null, "this", forkSig.type());

    ExprVar f_s1 = ExprVar.make(null, "x", forkSig.type());
    ExprVar f_s2 = ExprVar.make(null, "x", forkSig.type());
    ExprVar f_s3 = ExprVar.make(null, "x", forkSig.type());
    ExprVar f_s4 = ExprVar.make(null, "x", forkSig.type());
    ExprVar f_s5 = ExprVar.make(null, "x", forkSig.type());

    List<ExprHasName> f_names1 = new ArrayList<>(List.of(f_s1));
    List<ExprHasName> f_names2 = new ArrayList<>(List.of(f_s2));
    List<ExprHasName> f_names3 = new ArrayList<>(List.of(f_s3));
    List<ExprHasName> f_names4 = new ArrayList<>(List.of(f_s4));
    List<ExprHasName> f_names5 = new ArrayList<>(List.of(f_s5));

    Decl f_decl1 = new Decl(null, null, null, f_names1, forkSig.oneOf());
    Decl f_decl2 = new Decl(null, null, null, f_names2, forkSig.oneOf());
    Decl f_decl3 = new Decl(null, null, null, f_names3, forkSig.oneOf());
    Decl f_decl4 = new Decl(null, null, null, f_names4, forkSig.oneOf());
    Decl f_decl5 = new Decl(null, null, null, f_names5, forkSig.oneOf());

    Expr bijectionFilteredExpr1 =
        bijectionFilteredFunction.call(
            happensBefore.call(),
            f_s1.join(forkSig.domain(fork_p1)),
            f_s1.join(forkSig.domain(fork_p2)));
    Expr bijectionFilteredExpr2 =
        bijectionFilteredFunction.call(
            happensBefore.call(),
            f_s2.join(forkSig.domain(fork_p1)),
            f_s2.join(forkSig.domain(fork_p3)));

    alloy.addToOverallFact(
        (bijectionFilteredExpr1.forAll(f_decl1))
            .and(bijectionFilteredExpr2.forAll(f_decl2))
            .and(
                f_s3.join(forkSig.domain(fork_p1))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(f_decl3))
            .and(
                f_s4.join(forkSig.domain(fork_p1))
                    .plus(f_s3.join(forkSig.domain(fork_p2)))
                    .plus(f_s3.join(forkSig.domain(fork_p3)))
                    .in(f_s3.join(stepsFunction.call()))
                    .forAll(f_decl4))
            .and(
                f_s5.join(stepsFunction.call())
                    .in(
                        f_s5.join(forkSig.domain(fork_p1))
                            .plus(f_s5.join(forkSig.domain(fork_p2)))
                            .plus(f_s5.join(forkSig.domain(fork_p3))))
                    .forAll(f_decl5)));

    // Join

    ExprVar joinThis = ExprVar.make(null, "this", joinSig.type());

    ExprVar j_s1 = ExprVar.make(null, "x", joinSig.type());
    ExprVar j_s2 = ExprVar.make(null, "x", joinSig.type());
    ExprVar j_s3 = ExprVar.make(null, "x", joinSig.type());
    ExprVar j_s4 = ExprVar.make(null, "x", joinSig.type());
    ExprVar j_s5 = ExprVar.make(null, "x", joinSig.type());
    ExprVar j_s6 = ExprVar.make(null, "x", joinSig.type());

    List<ExprHasName> j_names1 = new ArrayList<>(List.of(j_s1));
    List<ExprHasName> j_names2 = new ArrayList<>(List.of(j_s2));
    List<ExprHasName> j_names3 = new ArrayList<>(List.of(j_s3));
    List<ExprHasName> j_names4 = new ArrayList<>(List.of(j_s4));
    List<ExprHasName> j_names5 = new ArrayList<>(List.of(j_s5));
    List<ExprHasName> j_names6 = new ArrayList<>(List.of(j_s6));

    Decl j_decl1 = new Decl(null, null, null, j_names1, joinSig.oneOf());
    Decl j_decl2 = new Decl(null, null, null, j_names2, joinSig.oneOf());
    Decl j_decl3 = new Decl(null, null, null, j_names3, joinSig.oneOf());
    Decl j_decl4 = new Decl(null, null, null, j_names4, joinSig.oneOf());
    Decl j_decl5 = new Decl(null, null, null, j_names5, joinSig.oneOf());
    Decl j_decl6 = new Decl(null, null, null, j_names6, joinSig.oneOf());

    Expr bijectionFilteredExpr3 =
        bijectionFilteredFunction.call(
            happensBefore.call(),
            j_s1.join(joinSig.domain(join_p1)),
            j_s1.join(joinSig.domain(join_p3)));
    Expr bijectionFilteredExpr4 =
        bijectionFilteredFunction.call(
            happensBefore.call(),
            j_s2.join(joinSig.domain(join_p2)),
            j_s2.join(joinSig.domain(join_p3)));

    alloy.addToOverallFact(
        (bijectionFilteredExpr3.forAll(j_decl1))
            .and(bijectionFilteredExpr4.forAll(j_decl2))
            .and(
                j_s3.join(joinSig.domain(join_p1))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(j_decl3))
            .and(
                j_s4.join(joinSig.domain(join_p2))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(j_decl4))
            .and(
                j_s5.join(joinSig.domain(join_p1))
                    .plus(j_s5.join(joinSig.domain(join_p2)))
                    .plus(j_s5.join(joinSig.domain(join_p3)))
                    .in(j_s5.join(stepsFunction.call()))
                    .forAll(j_decl5))
            .and(
                j_s6.join(stepsFunction.call())
                    .in(
                        j_s6.join(joinSig.domain(join_p1))
                            .plus(j_s6.join(joinSig.domain(join_p2)))
                            .plus(j_s6.join(joinSig.domain(join_p3))))
                    .forAll(j_decl6)));

    // Decision
    ExprVar d_s1 = ExprVar.make(null, "x", decisionSig.type());
    ExprVar d_s2 = ExprVar.make(null, "x", decisionSig.type());
    ExprVar d_s3 = ExprVar.make(null, "x", decisionSig.type());
    ExprVar d_s4 = ExprVar.make(null, "x", decisionSig.type());
    ExprVar d_s5 = ExprVar.make(null, "x", decisionSig.type());
    ExprVar d_s6 = ExprVar.make(null, "x", decisionSig.type());

    List<ExprHasName> d_names1 = new ArrayList<>(List.of(d_s1));
    List<ExprHasName> d_names2 = new ArrayList<>(List.of(d_s2));
    List<ExprHasName> d_names3 = new ArrayList<>(List.of(d_s3));
    List<ExprHasName> d_names4 = new ArrayList<>(List.of(d_s4));
    List<ExprHasName> d_names5 = new ArrayList<>(List.of(d_s5));
    List<ExprHasName> d_names6 = new ArrayList<>(List.of(d_s6));

    Decl d_decl1 = new Decl(null, null, null, d_names1, decisionSig.oneOf());
    Decl d_decl2 = new Decl(null, null, null, d_names2, decisionSig.oneOf());
    Decl d_decl3 = new Decl(null, null, null, d_names3, decisionSig.oneOf());
    Decl d_decl4 = new Decl(null, null, null, d_names4, decisionSig.oneOf());
    Decl d_decl5 = new Decl(null, null, null, d_names5, decisionSig.oneOf());
    Decl d_decl6 = new Decl(null, null, null, d_names6, decisionSig.oneOf());

    Expr functionFilteredExpr2 =
        functionFilteredFunction.call(
            happensBefore.call(),
            d_s1.join(decisionSig.domain(decision_p1)),
            (d_s1.join(decisionSig.domain(decision_p2)))
                .plus(d_s1.join(decisionSig.domain(decision_p3))));

    Expr inverseFunctionFilteredExpr2 =
        inverseFunctionFilteredFunction.call(
            happensBefore.call(),
            d_s2.join(decisionSig.domain(decision_p1)),
            (d_s2.join(decisionSig.domain(decision_p2)))
                .plus(d_s2.join(decisionSig.domain(decision_p3))));

    alloy.addToOverallFact(
        (functionFilteredExpr2.forAll(d_decl2))
            .and(inverseFunctionFilteredExpr2.forAll(d_decl2))
            .and(
                d_s3.join(decisionSig.domain(decision_p1))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(d_decl3))
            .and(
                d_s4.join(decisionSig.domain(decision_p1))
                    .plus(d_s4.join(decisionSig.domain(decision_p2)))
                    .plus(d_s4.join(decisionSig.domain(decision_p3)))
                    .in(d_s4.join(stepsFunction.call()))
                    .forAll(d_decl4))
            .and(
                d_s5.join(stepsFunction.call())
                    .in(
                        d_s5.join(decisionSig.domain(decision_p1))
                            .plus(d_s5.join(decisionSig.domain(decision_p2)))
                            .plus(d_s5.join(decisionSig.domain(decision_p3))))
                    .forAll(d_decl5)));

    // Merge

    ExprVar m_s1 = ExprVar.make(null, "x", mergeSig.type());
    ExprVar m_s2 = ExprVar.make(null, "x", mergeSig.type());
    ExprVar m_s3 = ExprVar.make(null, "x", mergeSig.type());
    ExprVar m_s4 = ExprVar.make(null, "x", mergeSig.type());
    ExprVar m_s5 = ExprVar.make(null, "x", mergeSig.type());
    ExprVar m_s6 = ExprVar.make(null, "x", mergeSig.type());

    List<ExprHasName> m_names1 = new ArrayList<>(List.of(m_s1));
    List<ExprHasName> m_names2 = new ArrayList<>(List.of(m_s2));
    List<ExprHasName> m_names3 = new ArrayList<>(List.of(m_s3));
    List<ExprHasName> m_names4 = new ArrayList<>(List.of(m_s4));
    List<ExprHasName> m_names5 = new ArrayList<>(List.of(m_s5));
    List<ExprHasName> m_names6 = new ArrayList<>(List.of(m_s6));

    Decl m_decl1 = new Decl(null, null, null, m_names1, mergeSig.oneOf());
    Decl m_decl2 = new Decl(null, null, null, m_names2, mergeSig.oneOf());
    Decl m_decl3 = new Decl(null, null, null, m_names3, mergeSig.oneOf());
    Decl m_decl4 = new Decl(null, null, null, m_names4, mergeSig.oneOf());
    Decl m_decl5 = new Decl(null, null, null, m_names5, mergeSig.oneOf());
    Decl m_decl6 = new Decl(null, null, null, m_names6, mergeSig.oneOf());

    Expr functionFilteredExpr3 =
        functionFilteredFunction.call(
            happensBefore.call(),
            m_s1.join(mergeSig.domain(merge_p1)).plus(m_s1.join(mergeSig.domain(merge_p2))),
            (m_s1.join(mergeSig.domain(merge_p3))));

    Expr inverseFunctionFilteredExpr3 =
        inverseFunctionFilteredFunction.call(
            happensBefore.call(),
            m_s2.join(mergeSig.domain(merge_p1)).plus(m_s2.join(mergeSig.domain(merge_p2))),
            (m_s2.join(mergeSig.domain(merge_p3))));

    alloy.addToOverallFact(
        (functionFilteredExpr3.forAll(m_decl1))
            .and(inverseFunctionFilteredExpr3.forAll(m_decl2))
            .and(
                m_s3.join(mergeSig.domain(merge_p1))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(m_decl3))
            .and(
                m_s4.join(mergeSig.domain(merge_p2))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(m_decl4))
            .and(
                m_s5.join(mergeSig.domain(merge_p1))
                    .plus(m_s5.join(mergeSig.domain(merge_p2)))
                    .plus(m_s5.join(mergeSig.domain(merge_p3)))
                    .in(m_s5.join(stepsFunction.call()))
                    .forAll(m_decl5))
            .and(
                m_s6.join(stepsFunction.call())
                    .in(
                        m_s6.join(mergeSig.domain(merge_p1))
                            .plus(m_s6.join(mergeSig.domain(merge_p2)))
                            .plus(m_s6.join(mergeSig.domain(merge_p3))))
                    .forAll(m_decl6)));

    // AllControl
    ExprVar ac_s1 = ExprVar.make(null, "x", allControlSig.type());
    ExprVar ac_s2 = ExprVar.make(null, "x", allControlSig.type());
    ExprVar ac_s3 = ExprVar.make(null, "x", allControlSig.type());
    ExprVar ac_s4 = ExprVar.make(null, "x", allControlSig.type());
    ExprVar ac_s5 = ExprVar.make(null, "x", allControlSig.type());
    ExprVar ac_s6 = ExprVar.make(null, "x", allControlSig.type());
    ExprVar ac_s7 = ExprVar.make(null, "x", allControlSig.type());
    ExprVar ac_s8 = ExprVar.make(null, "x", allControlSig.type());
    ExprVar ac_s9 = ExprVar.make(null, "x", allControlSig.type());

    List<ExprHasName> ac_names1 = new ArrayList<>(List.of(ac_s1));
    List<ExprHasName> ac_names2 = new ArrayList<>(List.of(ac_s2));
    List<ExprHasName> ac_names3 = new ArrayList<>(List.of(ac_s3));
    List<ExprHasName> ac_names4 = new ArrayList<>(List.of(ac_s4));
    List<ExprHasName> ac_names5 = new ArrayList<>(List.of(ac_s5));
    List<ExprHasName> ac_names6 = new ArrayList<>(List.of(ac_s6));
    List<ExprHasName> ac_names7 = new ArrayList<>(List.of(ac_s4));
    List<ExprHasName> ac_names8 = new ArrayList<>(List.of(ac_s5));
    List<ExprHasName> ac_names9 = new ArrayList<>(List.of(ac_s6));

    Decl ac_decl1 = new Decl(null, null, null, ac_names1, allControlSig.oneOf());
    Decl ac_decl2 = new Decl(null, null, null, ac_names2, allControlSig.oneOf());
    Decl ac_decl3 = new Decl(null, null, null, ac_names3, allControlSig.oneOf());
    Decl ac_decl4 = new Decl(null, null, null, ac_names4, allControlSig.oneOf());
    Decl ac_decl5 = new Decl(null, null, null, ac_names5, allControlSig.oneOf());
    Decl ac_decl6 = new Decl(null, null, null, ac_names6, allControlSig.oneOf());
    Decl ac_decl7 = new Decl(null, null, null, ac_names7, allControlSig.oneOf());
    Decl ac_decl8 = new Decl(null, null, null, ac_names8, allControlSig.oneOf());
    Decl ac_decl9 = new Decl(null, null, null, ac_names9, allControlSig.oneOf());

    alloy.addToOverallFact(
        (bijectionFilteredFunction
                .call(
                    happensBefore.call(),
                    ac_s1.join(allControlSig.domain(allControl_p1)),
                    ac_s1.join(allControlSig.domain(allControl_p2)))
                .forAll(ac_decl1))
            .and(
                bijectionFilteredFunction
                    .call(
                        happensBefore.call(),
                        ac_s2.join(allControlSig.domain(allControl_p1)),
                        ac_s2.join(allControlSig.domain(allControl_p3)))
                    .forAll(ac_decl2))
            .and(
                bijectionFilteredFunction
                    .call(
                        happensBefore.call(),
                        ac_s3.join(allControlSig.domain(allControl_p2)),
                        ac_s3.join(allControlSig.domain(allControl_p4)))
                    .forAll(ac_decl3))
            .and(
                bijectionFilteredFunction
                    .call(
                        happensBefore.call(),
                        ac_s4.join(allControlSig.domain(allControl_p3)),
                        ac_s4.join(allControlSig.domain(allControl_p4)))
                    .forAll(ac_decl4))
            .and(
                bijectionFilteredFunction
                    .call(
                        happensBefore.call(),
                        ac_s5.join(allControlSig.domain(allControl_p4)),
                        ac_s5
                            .join(allControlSig.domain(allControl_p5))
                            .plus(ac_s5.join(allControlSig.domain(allControl_p6))))
                    .forAll(ac_decl5))
            .and(
                bijectionFilteredFunction
                    .call(
                        happensBefore.call(),
                        ac_s6
                            .join(allControlSig.domain(allControl_p5))
                            .plus(ac_s6.join(allControlSig.domain(allControl_p6))),
                        ac_s6.join(allControlSig.domain(allControl_p7)))
                    .forAll(ac_decl6))
            .and(
                ac_s7
                    .join(allControlSig.domain(allControl_p1))
                    .cardinality()
                    .equal(ExprConstant.makeNUMBER(1))
                    .forAll(ac_decl7))
            .and(
                ac_s8
                    .join(allControlSig.domain(allControl_p1))
                    .plus(ac_s8.join(allControlSig.domain(allControl_p2)))
                    .plus(ac_s8.join(allControlSig.domain(allControl_p3)))
                    .plus(ac_s8.join(allControlSig.domain(allControl_p4)))
                    .plus(ac_s8.join(allControlSig.domain(allControl_p5)))
                    .plus(ac_s8.join(allControlSig.domain(allControl_p6)))
                    .plus(ac_s8.join(allControlSig.domain(allControl_p7)))
                    .in(ac_s8.join(stepsFunction.call()))
                    .forAll(ac_decl8))
            .and(
                ac_s9
                    .join(stepsFunction.call())
                    .in(
                        ac_s9
                            .join(allControlSig.domain(allControl_p1))
                            .plus(ac_s9.join(allControlSig.domain(allControl_p2)))
                            .plus(ac_s9.join(allControlSig.domain(allControl_p3)))
                            .plus(ac_s9.join(allControlSig.domain(allControl_p4)))
                            .plus(ac_s9.join(allControlSig.domain(allControl_p5)))
                            .plus(ac_s9.join(allControlSig.domain(allControl_p6)))
                            .plus(ac_s9.join(allControlSig.domain(allControl_p7))))
                    .forAll(ac_decl9)));

    // ========== Define functions and predicates ==========

    // // p1DuringExample
    // Expr p1DuringExampleBody =
    // abSig.in(/*simpleSequenceSig.join(simpleSequence_p1).plus(forkSig.join(fork_p1)).plus(joinSig.join(join_p1)).plus(decisionSig.join(decision_p1)).plus(mergeSig.join(merge_p1)).plus*/(allControlSig.join(allControl_p1)));
    // Func p1DuringExamplePredicate = new Func(null, "p1DuringExample", new ArrayList<>(),
    // null, p1DuringExampleBody);
    // Expr p1DuringExampleExpr = p1DuringExamplePredicate.call();
    //
    // // p2DuringExample
    // Expr p2DuringExampleBody =
    // abSig.in(/*simpleSequenceSig.join(simpleSequence_p2).plus(forkSig.join(fork_p2).plus(joinSig.join(join_p2)).plus(decisionSig.join(decision_p2).plus(mergeSig.join(merge_p2).plus*/(allControlSig.join(allControl_p2)));//)));
    // Func p2DuringExamplePredicate = new Func(null, "p2DuringExample", new ArrayList<>(),
    // null, p2DuringExampleBody);
    // Expr p2DuringExampleExpr = p2DuringExamplePredicate.call();
    //
    // // p3DuringExample
    // Expr p3DuringExampleBody =
    // abSig.in(/*forkSig.join(fork_p3).plus(joinSig.join(join_p3).plus(decisionSig.join(decision_p3).plus(mergeSig.join(merge_p3).plus*/(allControlSig.join(allControl_p3)));//)));
    // Func p3DuringExamplePredicate = new Func(null, "p3DuringExample", new ArrayList<>(),
    // null, p3DuringExampleBody);
    // Expr p3DuringExampleExpr = p3DuringExamplePredicate.call();
    //
    // // p4DuringExample
    // Expr p4DuringExampleBody = abSig.in(allControlSig.join(allControl_p4));
    // Func p4DuringExamplePredicate = new Func(null, "p4DuringExample", new ArrayList<>(),
    // null, p4DuringExampleBody);
    // Expr p4DuringExampleExpr = p4DuringExamplePredicate.call();
    //
    // // p5DuringExample
    // Expr p5DuringExampleBody = abSig.in(allControlSig.join(allControl_p5));
    // Func p5DuringExamplePredicate = new Func(null, "p5DuringExample", new ArrayList<>(),
    // null, p5DuringExampleBody);
    // Expr p5DuringExampleExpr = p5DuringExamplePredicate.call();
    //
    // // p6DuringExample
    // Expr p6DuringExampleBody = abSig.in(allControlSig.join(allControl_p6));
    // Func p6DuringExamplePredicate = new Func(null, "p6DuringExample", new ArrayList<>(),
    // null, p6DuringExampleBody);
    // Expr p6DuringExampleExpr = p6DuringExamplePredicate.call();
    //
    // // p6DuringExample
    // Expr p7DuringExampleBody = abSig.in(allControlSig.join(allControl_p7));
    // Func p7DuringExamplePredicate = new Func(null, "p7DuringExample", new ArrayList<>(),
    // null, p7DuringExampleBody);
    // Expr p7DuringExampleExpr = p7DuringExamplePredicate.call();
    //
    // // instancesDuringExample
    // Expr instancesDuringExampleBody =
    // p1DuringExampleExpr.and(p2DuringExampleExpr).and(p3DuringExampleExpr).and(p4DuringExampleExpr).and(p5DuringExampleExpr).and(p6DuringExampleExpr).and(p7DuringExampleExpr);
    // Func instancesDuringExamplePredicate = new Func(null, "instancesDuringExample", new
    // ArrayList<>(), null, instancesDuringExampleBody);
    // Expr instancesDuringExampleExpr = instancesDuringExamplePredicate.call();

    // // onlySimpleSequence
    // Expr onlySimpleSequenceBody =
    // simpleSequenceSig.cardinality().equal(ExprConstant.makeNUMBER(1)).and(forkSig.no()).and(joinSig.no()).and(decisionSig.no()).and(mergeSig.no()).and(allControlSig.no());
    // Func onlySimpleSequencePredicate = new Func(null, "onlySimpleSequence", new
    // ArrayList<>(), null, onlySimpleSequenceBody);
    // Expr onlySimpleSequenceExpr = onlySimpleSequencePredicate.call();
    //
    // // onlyFork
    // Expr onlyForkBody =
    // simpleSequenceSig.no().and(forkSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(joinSig.no()).and(decisionSig.no()).and(mergeSig.no()).and(allControlSig.no());
    // Func onlyForkPredicate = new Func(null, "onlyFork", new ArrayList<>(), null,
    // onlyForkBody);
    // Expr onlyForkExpr = onlyForkPredicate.call();
    //
    // // onlyJoin
    // Expr onlyJoinBody =
    // simpleSequenceSig.no().and(forkSig.no()).and(joinSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(decisionSig.no()).and(mergeSig.no()).and(allControlSig.no());
    // Func onlyJoinPredicate = new Func(null, "onlyJoin", new ArrayList<>(), null,
    // onlyJoinBody);
    // Expr onlyJoinExpr = onlyJoinPredicate.call();
    //
    // // onlyDecision
    // Expr onlyDecisionBody =
    // simpleSequenceSig.no().and(forkSig.no()).and(joinSig.no()).and(decisionSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(mergeSig.no()).and(allControlSig.no());
    // Func onlyDecisionPredicate = new Func(null, "onlyDecision", new ArrayList<>(), null,
    // onlyDecisionBody);
    // Expr onlyDecisionExpr = onlyDecisionPredicate.call();
    //
    // // onlyMerge
    // Expr onlyMergeBody =
    // simpleSequenceSig.no().and(forkSig.no()).and(joinSig.no()).and(decisionSig.no()).and(mergeSig.cardinality().equal(ExprConstant.makeNUMBER(1))).and(allControlSig.no());
    // Func onlyMergePredicate = new Func(null, "onlyMerge", new ArrayList<>(), null,
    // onlyMergeBody);
    // Expr onlyMergeExpr = onlyMergePredicate.call();

    // onlyAllControl
    // Expr onlyAllControlBody =
    // /*simpleSequenceSig.no().and(forkSig.no()).and(joinSig.no()).and(decisionSig.no()).and(mergeSig.no()).and*/(allControlSig.cardinality().equal(ExprConstant.makeNUMBER(1)));
    // Func onlyAllControlPredicate = new Func(null, "onlyAllControl", new ArrayList<>(), null,
    // onlyAllControlBody);
    // Expr onlyAllControlExpr = onlyAllControlPredicate.call();

    // ========== Define command(s) ==========

    // Expr simpleSequenceExpr = alloy.getCommonCmdExprs()
    // .and(instancesDuringExampleExpr).and(onlySimpleSequenceExpr);
    //
    // Expr forkExpr = alloy.getCommonCmdExprs()
    // .and(instancesDuringExampleExpr).and(onlyForkExpr);
    //
    // Expr joinExpr = alloy.getCommonCmdExprs()
    // .and(instancesDuringExampleExpr).and(onlyJoinExpr);
    //
    // Expr decisionExpr = alloy.getCommonCmdExprs()
    // .and(instancesDuringExampleExpr).and(onlyDecisionExpr);
    //
    // Expr mergeExpr = alloy.getCommonCmdExprs()
    // .and(instancesDuringExampleExpr).and(onlyMergeExpr);

    // Expr allControlExpr = alloy.getCommonCmdExprs()
    // .and(instancesDuringExampleExpr).and(onlyAllControlExpr);

    // Command simpleSequenceCmd = new Command(
    // null, simpleSequenceExpr, "SimpleSequence", false, 6,
    // -1, -1, -1, Arrays.asList(new CommandScope[] {}),
    // Arrays.asList(new Sig[] {}),
    // simpleSequenceExpr.and(alloy.getOverAllFact()), null);
    //
    // Command forkCmd = new Command(
    // null, forkExpr, "fork", false, 10,
    // -1, -1, -1, Arrays.asList(new CommandScope[] {}),
    // Arrays.asList(new Sig[] {}),
    // forkExpr.and(alloy.getOverAllFact()), null);
    //
    // Command joinCmd = new Command(
    // null, joinExpr, "join", false, 6,
    // -1, -1, -1, Arrays.asList(new CommandScope[] {}),
    // Arrays.asList(new Sig[] {}),
    // joinExpr.and(alloy.getOverAllFact()), null);
    //
    // Command decisionCmd = new Command(null, decisionExpr, "decision",
    // false, 6, -1, -1, -1, Arrays.asList(new CommandScope[] {}),
    // Arrays.asList(new Sig[] {}), decisionExpr.and(alloy.getOverAllFact()),
    // null);
    //
    // Command mergeCmd = new Command(null, mergeExpr, "merge", false, 6, -1,
    // -1, -1, new ArrayList<>(), new ArrayList<>(),
    // mergeExpr.and(alloy.getOverAllFact()), null);

    // Command allControlCmd = new Command(null, allControlExpr, "AllControl",
    // false, 10, -1, -1, -1, new ArrayList<>(), new ArrayList<>(),
    // allControlExpr.and(alloy.getOverAllFact()), null);

    Command[] commands = {
      /*
       * simpleSequenceCmd, forkCmd, joinCmd, decisionCmd, mergeCmd, allControlCmd
       */
    };

    // ========== Write file ==========

    AlloyModule alloyModule =
        new AlloyModule(modulename, alloy.getAllSigs(), alloy.getOverAllFact(), commands);

    Translator translator =
        new Translator(alloy.getIgnoredExprs(), alloy.getIgnoredFuncs(), alloy.getIgnoredSigs());

    translator.generateAlsFileContents(alloyModule, outFileName);

    // ========== Import real AST from file ==========

    CompModule importedModule = AlloyUtils.importAlloyModule(manualfilename);

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
