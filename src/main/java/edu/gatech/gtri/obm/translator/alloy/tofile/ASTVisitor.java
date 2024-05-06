package edu.gatech.gtri.obm.translator.alloy.tofile;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprBinary;
import edu.mit.csail.sdg.ast.ExprCall;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprHasName;
import edu.mit.csail.sdg.ast.ExprITE;
import edu.mit.csail.sdg.ast.ExprLet;
import edu.mit.csail.sdg.ast.ExprList;
import edu.mit.csail.sdg.ast.ExprQt;
import edu.mit.csail.sdg.ast.ExprUnary;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.VisitQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/** The Class ASTVisitor. */
public class ASTVisitor extends VisitQuery<Expr> {

  /** The map. */
  private final Map<Expr, List<Expr>> map;

  /** The number of duplicate visits. */
  private int numberOfDuplicateVisits;

  /** The number of unique visits. */
  private int numberOfUniqueVisits;

  /** The number of visits. */
  private int numberOfVisits;

  /** The indent. */
  private int indent = 0;

  /** Instantiates a new AST visitor. */
  public ASTVisitor() {
    map = new HashMap<>();
    numberOfDuplicateVisits = 0;
    numberOfUniqueVisits = 0;
    numberOfVisits = 0;
  }

  /**
   * Gets the map.
   *
   * @return the map
   */
  public Map<Expr, List<Expr>> getMap() {
    return map;
  }

  /** Prints the visit info. */
  public void printVisitInfo() {
    System.out.println("Number of nodes visited:\t\t\t" + numberOfVisits);
    System.out.println("Number of unique nodes visited:\t\t" + numberOfUniqueVisits);
    System.out.println("Number of duplicate nodes visited:\t" + numberOfDuplicateVisits);
    System.out.println("Number of nodes mapped:\t\t\t\t" + map.keySet().size());
  }

  /**
   * Visit.
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(ExprBinary x) throws Err {
    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }
    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }

    indent++;

    map.get(x).add(x.left.accept(this));
    map.get(x).add(x.right.accept(this));

    indent--;

    return x;
  }

  /**
   * Visit.
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(ExprList x) throws Err {

    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }

    System.out.println("ExprList: " + x);

    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }

    indent++;

    for (Expr y : x.args) {
      map.get(x).add(y.accept(this));
    }
    System.out.println("ExprConstant: " + x);

    indent--;

    return x;
  }

  /**
   * Visit.
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(ExprCall x) throws Err {
    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }
    System.out.println("ExprCall: " + x);

    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }
    System.out.println("ExprITE: " + x);

    indent++;

    for (Expr y : x.args) {
      map.get(x).add(y.accept(this));
    }

    indent--;

    return x;
  }

  /**
   * Visits an ExprConstant node (this default implementation simply returns null).
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(ExprConstant x) throws Err {
    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }
    System.out.println("ExprConstant: " + x);

    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }

    return x;
  }

  /**
   * Visits an
   *
   * <p>ExprITE node (C - X else Y) by calling accept() on C, X, then Y.
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(ExprITE x) throws Err {
    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }
    System.out.println("ExprITE: " + x);

    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }

    indent++;

    map.get(x).add(x.cond.accept(this));
    map.get(x).add(x.left.accept(this));
    map.get(x).add(x.right.accept(this));

    indent--;

    return x;
  }

  /**
   * Visits an ExprLet node (let a=x | y) by calling accept() on "a", "x", then "y".
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(ExprLet x) throws Err {
    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }
    System.out.println("ExprLet: " + x);

    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }

    indent++;

    map.get(x).add(x.var.accept(this));
    map.get(x).add(x.expr.accept(this));
    map.get(x).add(x.sub.accept(this));

    indent--;

    return x;
  }

  /**
   * Visits an ExprQt node (all a,b,c:X1, d,e,f:X2... | F) by calling accept() on
   * a,b,c,X1,d,e,f,X2... then on F.
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(ExprQt x) throws Err {
    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }
    System.out.println("ExprQt: " + x);

    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }

    indent++;

    for (Decl d : x.decls) {
      for (ExprHasName v : d.names) {
        map.get(x).add(v.accept(this));
      }
      map.get(x).add(d.expr.accept(this));
    }

    map.get(x).add(x.sub.accept(this));

    indent--;

    return x;
  }

  /**
   * Visits an ExprUnary node (OP X) by calling accept() on X.
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(ExprUnary x) throws Err {

    if (x.op == ExprUnary.Op.NOOP) {
      return visitThis(x.deNOP());
    }

    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }
    System.out.println("ExprUnary: " + x);

    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }

    indent++;

    map.get(x).add(x.sub.accept(this));

    indent--;

    return x;
  }

  /**
   * Visits a ExprVar node (this default implementation simply returns null).
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(ExprVar x) throws Err {
    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }
    System.out.println("ExprVar: " + x);

    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }

    return x;
  }

  /**
   * Visits a Sig node (this default implementation simply returns null).
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(Sig x) throws Err {
    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }
    System.out.println("Sig: " + x);

    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }

    indent++;

    for (Field f : x.getFields()) {
      map.get(x).add(f.accept(this));
    }

    indent--;

    return x;
  }

  /**
   * Visit.
   *
   * @param x the x
   * @return the expr
   * @throws Err the err
   */
  @Override
  public Expr visit(Field x) throws Err {
    for (int i = 0; i < indent; i++) {
      System.out.print("\t");
    }
    System.out.println("Field: " + x);

    if (!map.containsKey(x)) {
      map.put(x, new ArrayList<>());
    }

    return x;
  }
}
