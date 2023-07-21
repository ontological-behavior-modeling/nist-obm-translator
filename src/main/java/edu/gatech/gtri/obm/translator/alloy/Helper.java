package edu.gatech.gtri.obm.translator.alloy;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.ast.Browsable;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.Sig.PrimSig;

public class Helper {

  public static void printAllFunc(Module m) {
    System.out.println("===========allFunc====================");
    for (Func f : m.getAllFunc()) {
      System.out.println(f);
    }

  }

  /**
   * Prints all the facts in the Module, m.
   *
   * @param m = the module you want to print facts from
   */
  public static void printAllFact(Module m) {
    System.out.println("===========Module.AllFact====================");
    for (Pair<String, Expr> f : m.getAllFacts()) {
      System.out.println(f.a);
      System.out.println(f.b);
    }

  }

  public static void getAllCommands(Module m) {
    System.out.println("===========Module.AllCommands====================");
    for (Command f : m.getAllCommands()) {
      System.out.println(f);
      printCommand(f, m.getAllReachableUserDefinedSigs());
    }
  }

  private static void printCommand(Command c, Iterable<Sig> sigs) {
    System.out.println("bitwidth: " + c.bitwidth);
    System.out.println("check: " + c.check);
    System.out.println("expects: " + c.expects);
    System.out.println("formula: " + c.formula);
    System.out.println("label: " + c.label);
    System.out.println("maxseq: " + c.maxseq);
    System.out.println("maxstring: " + c.maxstring);
    System.out.println("overall: " + c.overall);
    System.out.println("parent: " + c.parent);
    System.out.println("pos: " + c.pos);
    System.out.println("scope: " + c.scope);
    System.out.println("additionalExactScopes: " + c.additionalExactScopes);
    System.out.println("getSubnodes(): " + c.getSubnodes());

    System.out.println("getAllStringConstants for module.getAllReachableUserDefinedSigs...."
        + ((ConstList<Sig>) sigs).size());
    Set<String> allstringConstraints = c.getAllStringConstants(sigs);
    int i = 1;
    for (Iterator<String> iter = allstringConstraints.iterator(); iter.hasNext();) {
      System.out.println("[" + i++ + "]: " + iter.next());
    }

  }

  public static void printExpr(Browsable expr, String tab) {
    for (int j = 0; j < expr.getSubnodes().size(); j++) {
      System.out.println(tab + "\t\t" + expr.getSubnodes().get(j) + " ----- " + "class: "
          + expr.getSubnodes().get(j).getClass());
      if (expr.getSubnodes().get(j).getSubnodes().size() != 0)
        printExpr(expr.getSubnodes().get(j), tab + "\t");
    }
  }

  public static void printOnlyThisFn(Expr expr) {
    System.out.println("=========== All Fn - common functions ========");
    System.out.println(expr.toString().substring(0, expr.toString().indexOf("(all t")));
    System.out.println("============================================");
    // (all t | AND[o/isAfterSource[t], o/isBeforeTarget[t]] <=> t in o/TransferBefore),
    // (all t | # t . o/items > 0), o/r/acyclic[o/items, o/Transfer], o/r/acyclic[o/sources,
    // o/Transfer], o/r/acyclic[o/targets, o/Transfer],
    // (all t | (all itm | o/during[t, itm])), (all t,t' | AND[t . o/items = t' . o/items, t .
    // o/sources = t' . o/sources, t . o/targets = t' . o/targets] => t = t'), (all t | (all src |
    // (all tgt | OR[t in src . o/stepsAndSubsteps, t in tgt . o/stepsAndSubsteps => t !in
    // o/TransferBefore]))),
    // (all o | (all input | (all output | input = output => o/during[o, input]))),
    // (all x,y | y in x . o/steps => ! x in y . o/steps),
    // (all x,y,z | AND[y in x . o/steps, z in y . ^ o/steps] => z !in x . o/steps),
    // (all x | o/during[x, x]),
    // (all x,y,z | AND[o/before[x, y], o/before[y, z]] => o/before[x, z]),
    // (all x,y,z | AND[o/during[x, y], o/during[y, z]] => o/during[x, z]),
    // (all x,y | AND[o/before[x, y], o/before[y, x]] <=> AND[o/before[x, x], o/before[y, y],
    // o/during[x, y], o/during[y, x]]),
    // (all x,y,z | AND[o/before[x, y], o/during[z, y]] => o/before[x, z]), (
    // all x,y,z | AND[o/before[y, x], o/during[z, y]] => o/before[z, x]),
    // (all x,y | y in x . o/steps => o/during[y, x])


  }

  public static void printOnlyThisSigs(List<Sig> allSigs) {
    System.out.println("================= All Sigs ==========================");
    System.out.println(allSigs);
    for (Sig s : allSigs) {

      if (s.label.startsWith("this")) {
        System.out.println(s.label);
        for (Iterator<Decl> ps = ((PrimSig) s).getFieldDecls().iterator(); ps.hasNext();)
          System.out.println("\t" + ps.next().expr);
      }
    }
  }

  public static void printSigs(List<Sig> allSigs) {
    System.out.println("================= All Sigs ==========================");
    System.out.println(allSigs);

    // for (Iterator<Sig> iter = allSigs.iterator(); iter.hasNext();) {
    // Sig s = iter.next();
    // System.out.println("====" + s.attributes + " " + s);
    // System.out.println("childern:");
    // System.out.println(((PrimSig) s).children());
    // System.out.println("descendents:");
    // for (Iterator<PrimSig> ps = ((PrimSig) s).descendents().iterator(); ps.hasNext();)
    // System.out.println("\t" + ps.next());
    // System.out.println("getFieldDecls.expr:");
    // for (Iterator<Decl> ps = ((PrimSig) s).getFieldDecls().iterator(); ps.hasNext();)
    // System.out.println("\t" + ps.next().expr);
    // System.out.println("getFields():");
    // for (Iterator<Field> ps = ((PrimSig) s).getFields().iterator(); ps.hasNext();)
    // System.out.println("\t" + ps.next());
    // System.out.println("getFacts(): size = " + s.getFacts().size());
    //
    // for (int i = 0; i < ((PrimSig) s).getFacts().size(); i++) {
    // Browsable b = s.getFacts().get(i);
    // System.out.println("\t[" + i + "] " + b + " ----- " + "class: " + b.getClass());
    // System.out.println("\tsubnode");
    // for (int j = 0; j < b.getSubnodes().size(); j++) {
    // System.out.println("\t\t" + b.getSubnodes().get(j) + " ----- " + "class: "
    // + b.getSubnodes().get(j).getClass());
    // }
    // }
    //

    // }

    // [this/Occurrence, o/TransferBefore, o/TransferBeforeSig, o/Transfer, o/TransferSig,
    // o/BinaryLink, o/BinaryLinkSig, o/OccurrenceSig, this/SimpleSequence, this/P1, this/P2]
  }

  public static void addAllReachableSig(Module m, List<Sig> allSigs) {
    for (Iterator<Sig> iter = m.getAllReachableSigs().iterator(); iter.hasNext();) {
      allSigs.add(iter.next());
    }
  }

  public static Field getReachableSigField(Module m, Sig sig, String fieldLabel) {

    for (Field f : sig.getFields()) {
      System.out.println(f.label);
      if (f.label.equals(fieldLabel)) {
        return f;
      }
    }
    return null;
  }

  /**
   * Gets a Signature lookingFor, in the Module m if it exists.
   *
   * @param m = the module
   * @param lookingFor = the name of the Signature
   * @return the Signature or null if it doesn't exist
   */
  public static Sig getReachableSig(Module m, String lookingFor) {
    for (Sig s : m.getAllReachableSigs()) {
      if (s.label.equals(lookingFor))
        return s;
    }
    return null;
  }

  public static void printReachableSigs(Module m) {
    System.out.println("================= Reachable Sigs===============");
    for (Sig s : m.getAllReachableSigs()) {
      System.out.println("====" + s.attributes + " " + s);

    }
  }

  public static void addAllReachableUserDefinedSigs(Module m, List<Sig> sigs) {
    for (Sig s : m.getAllReachableUserDefinedSigs()) {
      sigs.add(s);
    }
  }

  public static void printAllReachableUserDefinedSigs(Module m) {
    System.out.println("===========All getAllReachableUserDefinedSigs \"" + m.getModelName()
        + "\"===================");

    for (Sig s : m.getAllReachableUserDefinedSigs()) {
      System.out.println("====" + s.attributes + " " + s);
      System.out.println("childern:");
      System.out.println(((PrimSig) s).children());
      System.out.println("descendents:");
      for (Iterator<PrimSig> ps = ((PrimSig) s).descendents().iterator(); ps.hasNext();)
        System.out.println("\t" + ps.next());
      System.out.println("getFieldDecls.expr:");
      for (Iterator<Decl> ps = ((PrimSig) s).getFieldDecls().iterator(); ps.hasNext();)
        System.out.println("\t" + ps.next().expr);
      System.out.println("getFields():");
      for (Iterator<Field> ps = ((PrimSig) s).getFields().iterator(); ps.hasNext();)
        System.out.println("\t" + ps.next());
      System.out.println("getFacts() = Expr " + ((PrimSig) s).getFacts().size());
      int counter = 1, counter2 = 1;
      for (Expr expr : ((PrimSig) s).getFacts()) {

        System.out.println("\t[Expr: " + counter++ + "] " + expr);
        for (Iterator<Func> iterf = expr.findAllFunctions().iterator(); iterf.hasNext();) {
          System.out.println("\t\t[Func: " + counter2++ + "] " + iterf.next());
        }
      }


    }
  }

  public static void printReachableModules(Module module) {
    System.out.println("=========== All reachable modules ==================");
    int index = 0;
    for (Module m : module.getAllReachableModules()) {
      System.out.println(index++ + " " + m + " " + m.getModelName());
      // 0 module{}
      // 1 module{integer}
      // 2 module{o}
      // 3 module{o/r}
    }
  }

  public static Module getAllReachableModuleByName(Module module, String lookingForModuleName) {

    for (Module m : module.getAllReachableModules()) {
      if (m.getModelName().equals(lookingForModuleName))
        return m;
    }
    return null;
  }

  public static Func getFunction(Module module, String lookingForFunctionLabel) {
    for (Func f : module.getAllFunc()) {
      if (f.label.equals(lookingForFunctionLabel))
        return f;
    }
    return null;
  }

  public static void printAllFuctions(Module module) {
    int index = 0;
    for (Func f : module.getAllFunc()) {
      System.out.println("fn: " + index++ + " label: " + f.label + " toString: " + f);

    }
  }

  public static void printAllReachableFacts(Module module) {
    System.out.println("===AllReachableFacts  = Expr ====");
    Expr expr = module.getAllReachableFacts();
    System.out.println(expr);
    int counter2 = 1;
    for (Iterator<Func> iterf = expr.findAllFunctions().iterator(); iterf.hasNext();) {
      System.out.println("\t\t[Func: " + counter2++ + "] " + iterf.next());
    }
  }

  public static void printExprAndItsFunctions(Expr expr) {
    System.out.println("===Expr ====");
    System.out.println(expr.getClass());
    System.out.println("expr.toString()");
    System.out.println(expr);
    System.out.println("====== expr.findAllFunctions() = Iterable<Func>");
    int counter2 = 1;
    for (Iterator<Func> iterf = expr.findAllFunctions().iterator(); iterf.hasNext();) {
      System.out.println("\t\t[Func: " + counter2++ + "] " + iterf.next());
    }
    System.out.println("=============expr.getSubnodes() = List<Browsable> ================");
    printSubNode(expr, "", 0);

  }

  private static void printSubNode(Browsable browsable, String tab, int start) {
    for (int i = start; i < browsable.getSubnodes().size(); i++) {
      Browsable b = browsable.getSubnodes().get(i);
      System.out.println(tab + "[" + i + "] " + b + " ----- " + "class: " + b.getClass());
      // System.out.println(tab + "[" + i + "]" + b.getHTML());
      // printSubNode(browsable.getSubnodes().get(i), tab + "\t", 0);
    }
  }


}