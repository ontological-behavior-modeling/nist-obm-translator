package edu.gatech.gtri.obm.translator.alloy;

import static edu.mit.csail.sdg.alloy4.A4Preferences.ImplicitThis;
import static edu.mit.csail.sdg.alloy4.A4Preferences.VerbosityPref;

import edu.mit.csail.sdg.alloy4.A4Preferences.Verbosity;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Computer;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4.ErrorType;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Version;
import edu.mit.csail.sdg.alloy4.XMLNode;
import edu.mit.csail.sdg.alloy4viz.AlloyInstance;
import edu.mit.csail.sdg.alloy4viz.AlloyModel;
import edu.mit.csail.sdg.alloy4viz.AlloyType;
import edu.mit.csail.sdg.alloy4viz.VizGUI;
import edu.mit.csail.sdg.alloy4viz.VizState;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.sim.SimInstance;
import edu.mit.csail.sdg.sim.SimTuple;
import edu.mit.csail.sdg.sim.SimTupleset;
import edu.mit.csail.sdg.translator.A4Options;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.A4SolutionReader;
import edu.mit.csail.sdg.translator.A4Tuple;
import edu.mit.csail.sdg.translator.A4TupleSet;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kodkod.engine.fol2sat.HigherOrderDeclException;
import org.alloytools.alloy.core.AlloyCore;

// TODO: Auto-generated Javadoc
/** The Class VisualizerHelper. */
public class VisualizerHelper {

  /** The recent refresh. */
  static Boolean updated = false, recentRefresh = false;

  // mw SimpleReporter line 608 is where A4Solution.toString() to add or not by next
  /** The set of Strings already enumerated for this current solution. */
  private static final Set<String> ansToString = new LinkedHashSet<String>();

  /** The Constant aInstances. */
  private static final Set<AlloyInstance> aInstances = new LinkedHashSet<AlloyInstance>();

  /** The Constant aModels. */
  private static final Set<AlloyModel> aModels = new LinkedHashSet<AlloyModel>();

  /** The solution counter. */
  private static int solutionCounter = 1;

  /** The viz. */
  private static VizGUI viz;

  /**
   * Update updated.
   *
   * @param flag the flag
   */
  public static void updateUpdated(boolean flag) {
    updated = flag;
  }

  /**
   * Checks for updated.
   *
   * @return true, if successful
   */
  public static boolean hasUpdated() {
    return updated;
  }

  /** The previous ans. */
  private static A4Solution previousAns;

  /**
   * Generate visualizer with next capability.
   *
   * @param sigs the sigs
   * @param command the command
   * @throws Err the err
   */
  public static void generateVisualizerWithNextCapability(Iterable<Sig> sigs, Command command)
      throws Err {
    VizGUI viz = null;
    // Parse+typecheck the model
    System.out.println("=========== Parsing+Typechecking " + "" + " =============");

    // Choose some default options for how you want to execute the commands
    A4Options options = new A4Options();

    options.solver = A4Options.SatSolver.SAT4J;

    A4Reporter rep = new A4Reporter();
    // Execute the command
    System.out.println("============ Command " + command + ": ============");
    A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, sigs, command, options);

    while (ans.satisfiable()) {
      ans.writeXML("out/alloy_example_output.xml");
      if (viz == null) {
        viz =
            new VizGUI(
                false,
                "out/alloy_example_output.xml",
                null,
                new Computer() {
                  @Override
                  public String compute(Object o) throws Err {
                    updateUpdated(true);
                    // ans.next().writeXML("alloy_example_output.xml");
                    // note this is just a dummy return, I'm not sure how to use the string to
                    // compute
                    // ans.next()
                    return "continue";
                  }
                },
                null);
      } else {
        if (recentRefresh == false) {
          // do nothing
        } else {
          recentRefresh = false;
          viz.loadXML("out/alloy_example_output.xml", true);
        }
      }
      if (hasUpdated()) {
        recentRefresh = true;
        ans = ans.next();
        updateUpdated(false);
        continue;
      }
    }
  }

  /** Update vis. */
  public static void updateVis() {
    A4Solution ans = previousAns.next();
    ++solutionCounter;
    if (ans.satisfiable()) {

      System.out.println("============= Solution #" + solutionCounter);
      System.out.println(ans.getAllAtoms().toString());

      ans.writeXML("out/alloy_example_output-" + solutionCounter + ".xml");
      viz.loadXML("out/alloy_example_output-" + solutionCounter + ".xml", true);
      AlloyInstance myInstance = viz.getVizState().getOriginalInstance();
      System.out.println("AnsToString: " + ansToString.add(ans.toString()));
      System.out.println("AlloyInstance:" + aInstances.add(myInstance));
      System.out.println("AllyModel: " + aModels.add(myInstance.model));
      write(myInstance, solutionCounter);

      previousAns = ans; // previousAns.next();
    } else {
      System.out.println("Next answer is not satisfiable");
      ans.writeXML("out/alloy_example_output.xml"); // this make pop up
    }
  }

  //  public static void generateVisualizerY(Iterable<Sig> sigs, Command cmd) {
  //    viz = null; // reassign viz b/c it's static it needs to be reset to null here just to be
  // sure.
  //    A4Options options = new A4Options();
  //    options.tempDirectory = "out";
  //    options.solverDirectory = "out";
  //    options.recordKodkod = true;// RecordKodkod.get();
  //    options.noOverflow = true;// NoOverflow.get();
  //    options.unrolls = -1;// Version.experimental ? Unrolls.get() : (-1);
  //    options.skolemDepth = 4;// SkolemDepth.get();
  //    options.coreMinimization = 2;// CoreMinimization.get();
  //    options.inferPartialInstance = false;// InferPartialInstance.get();
  //    options.coreGranularity = 0;// CoreGranularity.get();
  //    options.solver = A4Options.SatSolver.SAT4J;
  //    A4Solution ans = TranslateAlloyToKodkod.execute_command(null, sigs, cmd, options);
  //    int counter = 0;
  //    int scCounter = 0;
  //    FileWriter fw = null;
  //    FileWriter fprojected = null;
  //    Set<String> ansToString = new LinkedHashSet<String>();
  //    Set<String> ansModifiedToString = new LinkedHashSet<String>();
  //    Set<AlloyInstance> ainstances = new HashSet<AlloyInstance>();
  //    Set<AlloyModel> amodels = new HashSet<AlloyModel>();
  //
  //    Set<String> projectedOverInstances = new HashSet<String>();
  //    // AlloyInstance.toString(), value = index
  //    HashMap<String, Set<Integer>> projectedCounter = new HashMap<String, Set<Integer>>();
  //    HashMap<String, Set<Integer>> a4solutionCounter = new HashMap<String, Set<Integer>>();
  //
  //    System.out.println(System.currentTimeMillis());
  //    long uniqueTime = System.currentTimeMillis();
  //
  //    try {
  //
  //      fw = new FileWriter("lib/a4solution_out/coutSS" + uniqueTime + ".txt", true);
  //      fprojected =
  //          new FileWriter("lib/a4solution_out/projectedInstances" + uniqueTime + ".txt", true);
  //    } catch (IOException e1) {
  //      // TODO Auto-generated catch block
  //      e1.printStackTrace();
  //    }
  //    while (ans.satisfiable()) {
  //      boolean unique = ansToString.add(ans.toString());
  //      System.out.println("[Solution" + ++counter + "]: unique? " + unique);
  //      // System.out.println("maxseq: " + ans.getMaxSeq() + " bitwidth: " + ans.getBitwidth());
  //      // System.out.println("original cmd: " + ans.getOriginalCommand());
  //      // System.out.println("reachable sig: " + ans.getAllReachableSigs());
  //      // System.out.println("============atoms");
  //      for (Iterator<ExprVar> iter = ans.getAllAtoms().iterator(); iter.hasNext();) {
  //        ExprVar ev = iter.next();
  //        if (ev.toString().startsWith("SimpleSequence"))
  //          scCounter++;
  //      }
  //
  //      try {
  //
  //
  //
  //        String ms = ans.toString().replaceAll("\\{o/BinaryLinkSig\\$0\\}", "{}")
  //            .replaceAll("\\{o/TransferSig\\$0\\}", "{}")
  //            .replaceAll("\\{o/TransferBeforeSig\\$0\\}", "{}");
  //
  //        ms = ms.replaceAll("o/BinaryLinkSig", "X").replaceAll("o/TransferSig", "X")
  //            .replaceAll("o/TransferBeforeSig", "X");
  //
  //        boolean uniqueModified = ansModifiedToString.add(ms);
  //
  //
  //        FileWriter ff = new FileWriter("lib/a4solution_out/a4s" + counter + ".txt", false);
  //        FileWriter ff2 = new FileWriter(
  //            "lib/a4solution_out/a4s_modified" + counter + "- " + uniqueModified + ".txt",
  // false);
  //
  //        ff.write(ans.toString());
  //        ff2.write(ms);
  //        ff.close();
  //        fw.write(counter + " " + unique + " " + uniqueModified + " " + ans.getAllAtoms() +
  // "\n");
  //        ff2.close();
  //      } catch (IOException e) {
  //        // TODO Auto-generated catch block
  //        e.printStackTrace();
  //      }
  //
  //      // System.out.println("============getAllSkolems");
  //      // for (Iterator<ExprVar> iter = ans.getAllSkolems().iterator(); iter.hasNext();) {
  //      // System.out.println(iter.next());
  //      // }
  //
  //
  //      // System.out.println(ans.toString());
  //      ans.writeXML("out/alloy_example_output_" + counter + ".xml");
  //
  //      // read
  //      File f = new File("out/alloy_example_output_" + counter + ".xml");
  //      AlloyInstance myInstance = StaticInstanceReader.parseInstance(f);
  //      ainstances.add(myInstance);
  //      amodels.add(myInstance.model);
  //
  //      VizState myState = null;
  //      if (myState == null)
  //        myState = new VizState(myInstance);
  //
  //      System.out.println("==========AlloyTypes===========");
  //      System.out.println(myState.getOriginalModel().getTypes().size() + " "
  //          + myState.getOriginalModel().getTypes());
  //      for (final AlloyType type : myState.getOriginalModel().getTypes())
  //        if (myState.canProject(type)) {
  //
  //          System.out.println("can project: " + type.getName());
  //          myState.project(type);
  //          List<AlloyAtom> latoms = myState.getOriginalInstance().type2atoms(type); //
  // o/BinaryLinkSig$0
  //          System.out.println("ListOfAtoms: " + latoms);
  //          Map<AlloyType, AlloyAtom> map = new LinkedHashMap<AlloyType, AlloyAtom>();
  //          if (type.getName().equals("o/OccurrenceSig")) { // latoms = [o/BinaryLinkSig]
  //            // // TypePanel tp = new TypePanel(type, atoms, null);
  //            List<AlloyAtom> atoms = new ArrayList<AlloyAtom>(latoms);
  //            Collections.sort(atoms);
  //            List<AlloyAtom> sortedatoms = ConstList.make(atoms);
  //            map.put(type, latoms.get(0)); // not sure VuzGraogPanel
  // atomCombo.getSelectedIndex();
  //            AlloyProjection currentProjection = new AlloyProjection(map);
  //
  //            JPanel graph = myState.getGraph(currentProjection);
  //            // JPanel ans = StaticGraphMaker.produceGraph(myInstance, this, currentProjection);
  //
  //            try {
  //              fprojected.write("=========================a4solution: \n");
  //              fprojected.write(ans.toString());
  //
  //              fprojected.write("-------------------------originalInstance: ");
  //              fprojected.write("mw-original-instance: " + myInstance.hashCode());
  //              fprojected.write("mw-original-model: " + myInstance.model.hashCode() + "\n");
  //              fprojected.write(myInstance.toString());
  //
  //
  //              fprojected.write("-----------------------projectedInstance: \n");
  //              AlloyInstance projectedinstance =
  //                  StaticProjector.project(myInstance, currentProjection);
  //              fprojected.write("mw-instance: " + projectedinstance.hashCode());
  //              AlloyModel projectedmodel = projectedinstance.model;
  //              fprojected.write("mw-model: " + projectedmodel.hashCode() + "\n");
  //              fprojected.write(projectedinstance.toString());
  //
  //
  //
  //              boolean up = projectedOverInstances.add(projectedinstance.toString());
  //              System.out.println("-----------------------projectedAlloyInstance: unique?" + up);
  //
  //              Set<Integer> sint = projectedCounter.get(projectedinstance.toString());
  //              if (sint == null) {
  //                sint = new HashSet<Integer>();
  //                sint.add(Integer.valueOf(counter));
  //                projectedCounter.put(projectedinstance.toString(), sint);
  //              } else
  //                sint.add(Integer.valueOf(counter));
  //
  //
  //              Set<Integer> sinta = a4solutionCounter.get(ans.toString());
  //              if (sinta == null) {
  //                sinta = new HashSet<Integer>();
  //                sinta.add(Integer.valueOf(counter));
  //                a4solutionCounter.put(ans.toString().toString(), sinta);
  //              } else
  //                sinta.add(Integer.valueOf(counter));
  //
  //
  //
  //            } catch (Exception e) {
  //              e.printStackTrace();
  //
  //            }
  //          }
  //        }
  //
  //      ans = ans.next();
  //    }
  //    if (counter == 0)
  //      System.out.println("no solution found.");
  //    try
  //
  //    {
  //      fw.write("# of unique ansToString = " + ansToString.size());
  //      fw.close();
  //      fprojected.close();
  //    } catch (IOException e) {
  //      // TODO Auto-generated catch block
  //      e.printStackTrace();
  //    }
  //    System.out.println("# of a4 solution: " + counter);
  //    System.out.println("# of unique a4 solution: " + ansToString.size());
  //    System.out.println("# of unique modified a4 soltuion: " + ansModifiedToString.size());
  //    System.out.println("# of unique AlloyInstances: " + ainstances.size());
  //    System.out.println("# of unique AlloyModels: " + amodels.size());
  //    System.out.println("# of unique projectedOver: " + projectedOverInstances.size());
  //    FileWriter fprojectedSummary = null;
  //    FileWriter fa4SolutionSummary = null;
  //    try {
  //      fprojectedSummary =
  //          new FileWriter("lib/a4solution_out/projectedsummary_" + uniqueTime + ".txt", false);
  //      int c = 1;
  //      for (Iterator<String> iter = projectedCounter.keySet().iterator(); iter.hasNext();) {
  //        String s = iter.next();
  //        fprojectedSummary
  //            .write(c++ + ": " + projectedCounter.get(s) + "================================\n");
  //        fprojectedSummary.write(s + "\n");
  //      }
  //      c = 1;
  //      fa4SolutionSummary =
  //          new FileWriter("lib/a4solution_out/a4solutionsummary_" + uniqueTime + ".txt", false);
  //      for (Iterator<String> iter = a4solutionCounter.keySet().iterator(); iter.hasNext();) {
  //        String s = iter.next();
  //        fa4SolutionSummary
  //            .write(c++ + ": " + a4solutionCounter.get(s) +
  // "================================\n");
  //        fa4SolutionSummary.write(s + "\n");
  //
  //
  //      }
  //    } catch (IOException e) {
  //      e.printStackTrace();
  //    } finally {
  //      try {
  //        fprojectedSummary.close();
  //        fa4SolutionSummary.close();
  //      } catch (IOException e) {
  //        // TODO Auto-generated catch block
  //        e.printStackTrace();
  //      }
  //    }
  //
  //
  //
  //  }

  /**
   * Generate visualizer X.
   *
   * @param sigs the sigs
   * @param cmd the cmd
   */
  public static void generateVisualizerX(Iterable<Sig> sigs, Command cmd) {
    viz = null; // reassign viz b/c it's static it needs to be reset to null here just to be sure.
    A4Options options = new A4Options();
    options.tempDirectory = "out";
    options.solverDirectory = "out";
    options.recordKodkod = true; // RecordKodkod.get();
    options.noOverflow = true; // NoOverflow.get();
    options.unrolls = -1; // Version.experimental ? Unrolls.get() : (-1);
    options.skolemDepth = 4; // SkolemDepth.get();
    options.coreMinimization = 2; // CoreMinimization.get();
    options.inferPartialInstance = false; // InferPartialInstance.get();
    options.coreGranularity = 0; // CoreGranularity.get();
    options.solver = A4Options.SatSolver.SAT4J;
    A4Solution ans = TranslateAlloyToKodkod.execute_command(null, sigs, cmd, options);
    int counter = 0;
    int scCounter = 0;
    FileWriter fw = null;
    Set<String> ansToString = new LinkedHashSet<String>();
    Set<String> ansModifiedToString = new LinkedHashSet<String>();
    // try {
    // System.out.println(System.currentTimeMillis());
    // fw = new FileWriter("lib/a4solution_out/coutSS" + System.currentTimeMillis() + ".txt", true);
    // } catch (IOException e1) {
    // // TODO Auto-generated catch block
    // e1.printStackTrace();
    // }
    while (ans.satisfiable()) {
      boolean unique = ansToString.add(ans.toString());
      System.out.println("[Solution" + ++counter + "]: unique? " + unique);
      // System.out.println("maxseq: " + ans.getMaxSeq() + " bitwidth: " + ans.getBitwidth());
      // System.out.println("original cmd: " + ans.getOriginalCommand());
      // System.out.println("reachable sig: " + ans.getAllReachableSigs());
      // System.out.println("============atoms");
      // for (Iterator<ExprVar> iter = ans.getAllAtoms().iterator(); iter.hasNext();) {
      // ExprVar ev = iter.next();
      // if (ev.toString().startsWith("SimpleSequence"))
      // scCounter++;
      // }

      // try {

      String ms =
          ans.toString()
              .replaceAll("\\{o/BinaryLinkSig\\$0\\}", "{}")
              .replaceAll("\\{o/TransferSig\\$0\\}", "{}")
              .replaceAll("\\{o/TransferBeforeSig\\$0\\}", "{}");

      ms =
          ms.replaceAll("o/BinaryLinkSig", "X")
              .replaceAll("o/TransferSig", "X")
              .replaceAll("o/TransferBeforeSig", "X");

      boolean uniqueModified = ansModifiedToString.add(ms);

      // FileWriter ff = new FileWriter("examples/a4solution_out/a4s" + counter + ".txt", false);
      // FileWriter ff2 = new FileWriter(
      // "examples/a4solution_out/a4s_modified" + counter + "- " + uniqueModified + ".txt",
      // false);
      //
      // ff.write(ans.toString());
      // ff2.write(ms);
      // ff.close();
      // fw.write(counter + " " + unique + " " + uniqueModified + " " + ans.getAllAtoms() + "\n");
      // ff2.close();
      // } catch (IOException e) {
      // // TODO Auto-generated catch block
      // e.printStackTrace();
      // }

      // System.out.println("============getAllSkolems");
      // for (Iterator<ExprVar> iter = ans.getAllSkolems().iterator(); iter.hasNext();) {
      // System.out.println(iter.next());
      // }

      // System.out.println(ans.toString());
      // ans.writeXML("out/alloy_example_output_" + counter + ".xml");
      //
      // File f = new File("out/alloy_example_output_" + counter + ".xml");
      // AlloyInstance myInstance = StaticInstanceReader.parseInstance(f);
      // System.out.println(myInstance);

      ans = ans.next();
    }
    if (counter == 0) System.out.println("no solution found.");
    // try {
    // fw.write("# of unique ansToString = " + ansToString.size());
    // fw.close();
    // } catch (IOException e) {
    // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    System.out.println("# of a4 solution: " + counter);
    System.out.println("# of unique a4 solution: " + ansToString.size());
    System.out.println("# of unique modified a4 soltuion: " + ansModifiedToString.size());
  }

  /**
   * Generate visualizer.
   *
   * @param sigs the sigs
   * @param cmd the cmd
   */
  public static void generateVisualizer(Iterable<Sig> sigs, Command cmd) {
    viz = null; // reassign viz b/c it's static it needs to be reset to null here just to be sure.

    A4Options options = new A4Options();
    // options.skolemDepth = 4;
    // options.noOverflow = true;
    // options.recordKodkod = true;

    // from simpleGUI
    options.tempDirectory = "out";
    options.solverDirectory = "out";
    options.recordKodkod = true; // RecordKodkod.get();
    options.noOverflow = true; // NoOverflow.get();
    // options.unrolls = -1;// Version.experimental ? Unrolls.get() : (-1);
    options.skolemDepth = 4; // SkolemDepth.get();
    // options.coreMinimization = 2;// CoreMinimization.get();
    // options.inferPartialInstance = false;// InferPartialInstance.get();
    // options.coreGranularity = 0;// CoreGranularity.get();
    options.symmetry = 20;
    // options.originalFilename = Util.canon(text.get().getFilename());
    A4Reporter rep =
        new A4Reporter() {

          // For example, here we choose to display each "warning" by printing
          // it to System.out
          @Override
          public void warning(ErrorWarning msg) {
            System.out.print("Relevance Warning:\n" + (msg.toString().trim()) + "\n\n");
            System.out.flush();
          }
        };

    options.solver = A4Options.SatSolver.SAT4J;
    A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, sigs, cmd, options);

    System.out.println("anser============================");
    System.out.println(ans);
    System.out.println(ans.debugExtractKInput());
    // System.out.println(ans.debugExtractKInstance());

    previousAns = ans; // update the static previous answer
    if (ans.satisfiable()) {
      System.out.println("============= Solution #" + solutionCounter);
      System.out.println(ans.getAllAtoms().toString());
      System.out.println(ans.toString());
      // ans.writeXML("C:/NoMagic
      // NoInstall/Cameo_Systems_Modeler_190_sp3_no_install_base_config/plugins/GTRI/AlloyUtil/APIRecreatedAlloyFiles/alloy_example_output.xml");
      ans.writeXML("out/alloy_example_output-" + solutionCounter + ".xml");

      // System.out.println("===========all atoms ===========");
      // for (Iterator<ExprVar> iter = ans.getAllAtoms().iterator(); iter.hasNext();) {
      // System.out.println(iter.next());
      // }
      // System.out.println("===========reachable sig===========");
      // for (Iterator<Sig> iter = ans.getAllReachableSigs().iterator(); iter.hasNext();) {
      // System.out.println(iter.next());
      // }

      // A4SolutionWriter.writeMetamodel(sigs, "", new PrintWriter(new File("out/xx.txt")));
      if (viz == null) {
        // viz = new VizGUI(false, "", windowmenu2, enumerator, evaluator);
        // viz = new VizGUI(false, "out/alloy_example_output.xml", null, enumerator, evaluator);
        // viz = new VizGUI(false, "out/alloy_example_output.xml", null);// ,, enumerator,
        // evaluator);
        viz =
            new VizGUI(
                true,
                "out/alloy_example_output-" + solutionCounter + ".xml",
                null,
                new Computer() {
                  @Override
                  public Object compute(Object input) throws Exception {

                    // VizState myState = new VizState(myInstance);

                    // VizState myState = new VizState(myInstance);
                    // ConstSet<AlloyType> ptypes = myState.getProjectedTypes();
                    // for (Iterator<AlloyType> iter = ptypes.iterator(); iter.hasNext();) {
                    // AlloyType atype = iter.next();
                    // System.out.println(atype.getName());
                    // // viz.getVizState().project(atype);
                    // }

                    updateVis();
                    return input;
                  }
                },
                null);
      } else {
        viz.loadXML("out/alloy_example_output-" + solutionCounter + ".xml", true);
      }
      AlloyInstance myInstance = viz.getVizState().getOriginalInstance();
      System.out.println("AnsToString: " + ansToString.add(ans.toString()));
      System.out.println("AlloyInstance:" + aInstances.add(myInstance));
      System.out.println("AllyModel: " + aModels.add(myInstance.model));
      System.out.println(myInstance.toString());

      write(myInstance, solutionCounter);

    } else System.out.println("No satisfied solution.");
  }

  /**
   * Write.
   *
   * @param myInstance the my instance
   * @param counter the counter
   */
  private static void write(AlloyInstance myInstance, int counter) {
    try {
      FileWriter f = new FileWriter("out/s" + counter + ".txt");
      f.write("Instance: \n");
      f.write(myInstance.hashCode() + "\n");
      f.write(myInstance.toString() + "\n");
      f.write("Model: \n");
      f.write(myInstance.hashCode() + "\n");
      f.write(myInstance.model.toString());
      f.close();

      VizState myState = viz.getVizState();
      // System.out.println("MyState: ");
      // System.out.println(myState);
      List<AlloyType> canProjectAlloyTypes = new ArrayList<>();
      List<AlloyType> projected = new ArrayList<>();
      // final Set<AlloyType> projected = myState.getProjectedTypes();
      for (final AlloyType t : myState.getOriginalModel().getTypes()) {
        // .out.println(t.getName());
        if (myState.canProject(t)) canProjectAlloyTypes.add(t);
        // System.out.println("can project? " + myState.canProject(t));
        if (projected.contains(t)) projected.add(t);
        // System.out.println("projected? : " + projected.contains(t));
      }
      // System.out.println("canProject? " + canProjectAlloyTypes);
      // System.out.println("possible to Project: " + myState.getOriginalModel().getTypes());
      // System.out.println("projected? " + projected);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  /** This object performs solution enumeration. */
  // private final Computer enumerator = new Computer() {
  //
  // @Override
  // public String compute(Object input) {
  // final String arg = (String) input;
  // // OurUtil.show(frame);
  // if (WorkerEngine.isBusy())
  // throw new RuntimeException(
  // "Alloy4 is currently executing a SAT solver command. Please wait until that command has
  // finished.");
  // SimpleCallback1 cb = new SimpleCallback1(SimpleGUI.this, viz, log,
  // VerbosityPref.get().ordinal(), latestAlloyVersionName, latestAlloyVersion);
  // SimpleTask2 task = new SimpleTask2();
  // task.filename = arg;
  // try {
  // if (AlloyCore.isDebug())
  // WorkerEngine.runLocally(task, cb);
  // else
  // WorkerEngine.run(task, SubMemory.get(), SubStack.get(), alloyHome() + fs + "binary", "",
  // cb);
  // // task.run(cb);
  // } catch (Throwable ex) {
  // WorkerEngine.stop();
  // log.logBold("Fatal Error: Solver failed due to unknown reason.\n"
  // + "One possible cause is that, in the Options menu, your specified\n"
  // + "memory size is larger than the amount allowed by your OS.\n"
  // + "Also, please make sure \"java\" is in your program path.\n");
  // log.logDivider();
  // log.flush();
  // doStop(2);
  // return arg;
  // }
  // subrunningTask = 2;
  // runmenu.setEnabled(false);
  // runbutton.setVisible(false);
  // showbutton.setEnabled(false);
  // stopbutton.setVisible(true);
  // return arg;
  // }
  // };

  /** This object performs expression evaluation. */
  private static Computer evaluator =
      new Computer() {

        private String filename = null;

        @Override
        public final Object compute(final Object input) throws Exception {
          if (input instanceof File) {
            filename = ((File) input).getAbsolutePath();
            return "";
          }
          if (!(input instanceof String)) return "";
          final String str = (String) input;
          if (str.trim().length() == 0) return ""; // Empty
          // line
          Module root = null;
          A4Solution ans = null;
          try {
            Map<String, String> fc = new LinkedHashMap<String, String>();
            XMLNode x = new XMLNode(new File(filename));
            if (!x.is("alloy")) throw new Exception();
            String mainname = null;
            for (XMLNode sub : x)
              if (sub.is("instance")) {
                mainname = sub.getAttribute("filename");
                break;
              }
            if (mainname == null) throw new Exception();
            for (XMLNode sub : x)
              if (sub.is("source")) {
                String name = sub.getAttribute("filename");
                String content = sub.getAttribute("content");
                fc.put(name, content);
              }
            root =
                CompUtil.parseEverything_fromFile(
                    A4Reporter.NOP,
                    fc,
                    mainname,
                    (Version.experimental && ImplicitThis.get()) ? 2 : 1);
            ans = A4SolutionReader.read(root.getAllReachableSigs(), x);
            for (ExprVar a : ans.getAllAtoms()) {
              root.addGlobal(a.label, a);
            }
            for (ExprVar a : ans.getAllSkolems()) {
              root.addGlobal(a.label, a);
            }
          } catch (Throwable ex) {
            throw new ErrorFatal("Failed to read or parse the XML file.");
          }
          try {
            Expr e = CompUtil.parseOneExpression_fromString(root, str);
            if (AlloyCore.isDebug() && VerbosityPref.get() == Verbosity.FULLDEBUG) {
              SimInstance simInst = convert(root, ans);
              if (simInst.wasOverflow()) return simInst.visitThis(e).toString() + " (OF)";
            }
            return ans.eval(e);
          } catch (HigherOrderDeclException ex) {
            throw new ErrorType("Higher-order quantification is not allowed in the evaluator.");
          }
        }
      };

  // mw from Simple GUI.java
  /**
   * Converts an A4TupleSet into a SimTupleset object.
   *
   * @param object the object
   * @return the sim tupleset
   * @throws Err the err
   */
  private static SimTupleset convert(Object object) throws Err {
    if (!(object instanceof A4TupleSet))
      throw new ErrorFatal("Unexpected type error: expecting an A4TupleSet.");
    A4TupleSet s = (A4TupleSet) object;
    if (s.size() == 0) return SimTupleset.EMPTY;
    List<SimTuple> list = new ArrayList<SimTuple>(s.size());
    int arity = s.arity();
    for (A4Tuple t : s) {
      String[] array = new String[arity];
      for (int i = 0; i < t.arity(); i++) array[i] = t.atom(i);
      list.add(SimTuple.make(array));
    }
    return SimTupleset.make(list);
  }

  // mw from Simple GUI.java
  /**
   * Converts an A4Solution into a SimInstance object.
   *
   * @param root the root
   * @param ans the ans
   * @return the sim instance
   * @throws Err the err
   */
  private static SimInstance convert(Module root, A4Solution ans) throws Err {
    SimInstance ct = new SimInstance(root, ans.getBitwidth(), ans.getMaxSeq());
    for (Sig s : ans.getAllReachableSigs()) {
      if (!s.builtin) ct.init(s, convert(ans.eval(s)));
      for (Field f : s.getFields()) if (!f.defined) ct.init(f, convert(ans.eval(f)));
    }
    for (ExprVar a : ans.getAllAtoms()) ct.init(a, convert(ans.eval(a)));
    for (ExprVar a : ans.getAllSkolems()) ct.init(a, convert(ans.eval(a)));
    return ct;
  }

  /**
   * Generate visualizer.
   *
   * @param world the world
   * @throws Err the err
   */
  /*
   * TODO: THIS IS WORKING COMPLETELY WITH A PREPARSED MODEL.
   */
  public static void generateVisualizer(Module world) throws Err {

    // The visualizer (We will initialize it to nonnull when we visualize an Alloy solution)
    viz = null;
    // Choose some default options for how you want to execute the commands

    A4Options options = new A4Options();

    options.solver = A4Options.SatSolver.SAT4J;

    for (Command command : world.getAllCommands()) {
      // Execute the command
      System.out.println("============ Command " + command + ": ============");
      A4Solution ans =
          TranslateAlloyToKodkod.execute_command(
              null, world.getAllReachableSigs(), command, options);
      previousAns = ans;
      // Print the outcome
      // If satisfiable...
      if (ans.satisfiable()) {
        // You can query "ans" to find out the values of each set or type.
        // This can be useful for debugging.
        //
        // You can also write the outcome to an XML file
        ans.writeXML("out/alloy_example_output.xml");
        //
        // You can then visualize the XML file by calling this:
        if (viz == null) {
          viz =
              new VizGUI(
                  false,
                  "out/alloy_example_output.xml",
                  null,
                  new Computer() {
                    @Override
                    public Object compute(Object input) throws Exception {
                      updateVis();
                      return input;
                    }
                  },
                  null);
        } else {
          viz.loadXML("out/alloy_example_output.xml", true);
        }
      }
    }
  }
}

/*
 * public static void generateVisualizerWithNextCapability(Module world) throws Err { VizGUI viz =
 * null; // Parse+typecheck the model
 * //System.out.println("=========== Parsing+Typechecking "+""+" =============");
 *
 * // Choose some default options for how you want to execute the commands A4Options options = new
 * A4Options();
 *
 * options.solver = A4Options.SatSolver.SAT4J;
 *
 * for (Command command: world.getAllCommands()) {
 *
 * // Execute the command //System.out.println("============ Command "+command+": ============");
 * A4Solution ans = TranslateAlloyToKodkod.execute_command(null, world.getAllReachableSigs(),
 * command, options); while(ans!=null){ if(ans.satisfiable()) { //
 * ans.writeXML("AlloyUtil/APIRecreatedAlloyFiles/alloy_example_output.xml");
 * ans.writeXML("plugins/GTRI/AlloyUtil/APIRecreatedAlloyFiles/alloy_example_output.xml"); if (viz
 * == null) { // viz = new VizGUI(false,
 * "AlloyUtil/APIRecreatedAlloyFiles/alloy_example_output.xml", null, new Computer() { viz = new
 * VizGUI(false, "plugins/GTRI/AlloyUtil/APIRecreatedAlloyFiles/alloy_example_output.xml", null, new
 * Computer() {
 *
 * @Override public String compute(Object o) throws Err { updateUpdated(true);
 * //ans.next().writeXML("alloy_example_output.xml"); //note this is just a dummy return, I'm not
 * sure how to use the string to compute ans.next() return "continue"; } }, null); } else { if
 * (recentRefresh == false) { //do nothing } else { recentRefresh = false;
 * //viz.loadXML("AlloyUtil/APIRecreatedAlloyFiles/alloy_example_output.xml", true);
 * viz.loadXML("plugins/GTRI/AlloyUtil/APIRecreatedAlloyFiles/alloy_example_output.xml", true); } }
 * if (hasUpdated()) { recentRefresh = true; ans = ans.next(); updateUpdated(false); continue; } } }
 *
 * } }
 */
