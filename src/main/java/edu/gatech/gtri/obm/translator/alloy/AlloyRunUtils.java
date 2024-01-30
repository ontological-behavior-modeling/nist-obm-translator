package edu.gatech.gtri.obm.translator.alloy;

import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import java.util.Arrays;
import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class AlloyRunUtils. */
public class AlloyRunUtils {

  /**
   * Run.
   *
   * @param _mainSig the main sig
   * @param _overallScope the overall scope
   * @param _allSigs the all sigs
   * @param _overallFact the overall fact
   */
  public static void run(Sig _mainSig, int _overallScope, List<Sig> _allSigs, Expr _overallFact) {
    // Sig sig, boolean isExact, int scope
    Iterable<CommandScope> scope = Arrays.asList(new CommandScope(_mainSig, false, 10));

    Command myCommand =
        // overall, Bitwidth, maxseq, expects
        // having runConstraint argument does not change result
        new Command(
            null,
            null,
            "" /*
                * label - the label for this command (it is only for
                * pretty-printing and does not have to be unique)
                */,
            false /* check - true if this is a "check"; false if this is a "run" */,
            _overallScope /*
                           * overall - the overall scope (0 or higher) (-1 if no overall scope
                           * wasspecified)
                           */,
            -1 /* bitwidth - the integer bitwidth (0 or higher) (-1 if it was notspecified) */,
            -1 /* maxseq - the maximum sequence length (0 or higher) (-1 if it was notspecified) */,
            -1 /* expects - the expected value (0 or 1) (-1 if no expectation wasspecified) */,
            scope /* the formula that must be satisfied by this command */,
            null,
            _overallFact,
            null);
    VisualizerHelper viz = new VisualizerHelper();
    viz.generateVisualizer(_allSigs, myCommand);
  }

  /**
   * Run X.
   *
   * @param _mainSig the main sig
   * @param _allSigs the all sigs
   * @param _overallFact the overall fact
   * @param command the command
   */
  public static void runX(Sig _mainSig, List<Sig> _allSigs, Expr _overallFact, Command command) {
    VisualizerHelper viz = new VisualizerHelper();
    viz.generateVisualizerX(_allSigs, command);
  }

  /**
   * Run X.
   *
   * @param _mainSig the main sig
   * @param _overallScope the overall scope
   * @param _allSigs the all sigs
   * @param _overallFact the overall fact
   */
  public static void runX(Sig _mainSig, int _overallScope, List<Sig> _allSigs, Expr _overallFact) {
    // Sig sig, boolean isExact, int scope
    Iterable<CommandScope> scope = Arrays.asList(new CommandScope(_mainSig, false, _overallScope));
    Command myCommand =
        // overall, Bitwidth, maxseq, expects
        // having runConstraint argument does not change result
        new Command(
            null,
            null,
            "" /*
                * label - the label for this command (it is only for
                * pretty-printing and does not have to be unique)
                */,
            false /* check - true if this is a "check"; false if this is a "run" */,
            _overallScope /*
                           * overall - the overall scope (0 or higher) (-1 if no overall scope was
                           * specified)
                           */,
            -1 /* bitwidth - the integer bitwidth (0 or higher) (-1 if it was notspecified) */,
            -1 /* maxseq - the maximum sequence length (0 or higher) (-1 if it was notspecified) */,
            -1 /* expects - the expected value (0 or 1) (-1 if no expectation wasspecified) */,
            scope /* the formula that must be satisfied by this command */,
            null,
            _overallFact,
            null);
    runX(_mainSig, _allSigs, _overallFact, myCommand);
  }
}
