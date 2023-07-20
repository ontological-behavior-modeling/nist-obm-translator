package edu.gatech.gtri.obm.translator.alloy.tofile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprBinary;
import edu.mit.csail.sdg.ast.ExprCall;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprHasName;
import edu.mit.csail.sdg.ast.ExprList;
import edu.mit.csail.sdg.ast.ExprQt;
import edu.mit.csail.sdg.ast.ExprUnary;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.ast.VisitQuery;

public class ExprVisitor extends VisitQuery<String> {

  private final Set<Expr> ignoredExprs;
  private boolean isRoot = true;
  private boolean fieldAfterSig = false;
  private boolean isImplicitFact = false;
  private int factNumber = 1;

  public ExprVisitor(Set<Expr> ignoredExprs) {
    this.ignoredExprs = ignoredExprs;
  }

  @Override
  public String visit(ExprBinary x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    StringBuilder sb = new StringBuilder();

    if (x.op == ExprBinary.Op.JOIN) {
      String left = x.left.accept(this);
      String right = x.right.accept(this);

      if (left.equals("this") && x.right instanceof Sig.Field) {
        return right;
      }


      return sb.append(left).append(x.op.toString()).append(right).toString();
    }

    String op = x.op.toString();

    if (x.op == ExprBinary.Op.NOT_IN) {
      op = "not in";
    }

    return sb.append(x.left.accept(this)).append(' ').append(op).append(' ')
        .append(x.right.accept(this)).toString();
  }

  @Override
  public String visit(ExprCall x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    String funcName = MyAlloyLibrary.removeSlash(x.fun.label);
    String[] args = new String[x.args.size()];

    for (int i = 0; i < x.args.size(); i++) {
      args[i] = x.args.get(i).accept(this);
    }

    StringBuilder sb = new StringBuilder();
    sb.append(funcName);

    if (!x.args.isEmpty()) {
      sb.append('[').append(String.join(", ", args)).append(']');
    }

    return sb.toString();
  }

  @Override
  public String visit(ExprConstant x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    return x.toString();
  }

  @Override
  public String visit(ExprList x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    if (isRoot) {
      isRoot = false;
      StringBuilder sb = new StringBuilder();
      for (Expr y : x.args) {

        if (ignoredExprs.contains(y)) {
          continue;
        }

        String out = y.accept(this);
        if (out != null) {

          if (!isImplicitFact) {
            sb.append("fact f").append(factNumber).append(" { ");
          }

          sb.append(out);

          if (!isImplicitFact) {
            sb.append(" }");
          }

          sb.append('\n');
          factNumber++;
        }

      }
      return sb.toString();
    }

    String[] args = new String[x.args.size()];
    for (int i = 0; i < x.args.size(); i++) {
      args[i] = x.args.get(i).accept(this);
    }

    StringBuilder op = new StringBuilder();
    op.append(' ').append(x.op.toString().toLowerCase()).append(' ');

    return String.join(op, args);
  }

  @Override
  public String visit(ExprQt x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    String op = x.op.toString();
    String names = "";
    String sigType = "";
    String sub = x.sub.accept(this);

    for (Decl decl : x.decls) {
      names = getNamesFromDecl(decl);
      String text = decl.expr.accept(this);
      sigType = text.contains(" ") ? text.substring(text.indexOf(" ") + 1) : text;
    }

    StringBuilder sb = new StringBuilder();
    return sb.append(op).append(' ').append(names).append(": ")
        .append(MyAlloyLibrary.removeSlash(sigType)).append(" | ").append(sub).toString();
  }

  @Override
  public String visit(ExprUnary x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    if (x.op == ExprUnary.Op.NOOP) {
      return visitThis(x.deNOP());
    }

    StringBuilder sb = new StringBuilder();
    String out = x.sub.accept(this);

    if (x.sub instanceof ExprBinary) {
      out = "(" + out + ")";
    }

    if (x.op == ExprUnary.Op.CARDINALITY) {
      out = "#" + out;
    } else if (x.op == ExprUnary.Op.SETOF) {
      out = "set " + out;
    } else if (x.op == ExprUnary.Op.NOT) {
      out = "not " + out;
    } else if (x.op == ExprUnary.Op.NO) {
      out = "no " + out;
    } else if (x.op == ExprUnary.Op.CLOSURE) {
      out = sb.append('^').append(out).toString();
    } else if (x.op == ExprUnary.Op.ONE || x.op == ExprUnary.Op.ONEOF) {
      out = "one " + out;
    } else if (x.op == ExprUnary.Op.TRANSPOSE) {
      out = "~" + out;
    }

    return out;
  }

  @Override
  public String visit(ExprVar x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    return x.label;
  }

  @Override
  public String visit(Sig x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    // Sig declaration.
    if (isRoot) {

      StringBuilder sb = new StringBuilder();
      isRoot = false;

      if (x.isOne != null) {
        sb.append("one ");
      } else if (x.isLone != null) {
        sb.append("lone ");
      } else if (x.isSome != null) {
        sb.append("some ");
      }

      if (x.isAbstract != null) {
        sb.append("abstract ");
      }

      sb.append("sig ").append(MyAlloyLibrary.removeSlash(x.label));

      if (x instanceof Sig.PrimSig) {
        Sig.PrimSig ps = (Sig.PrimSig) x;
        if (ps.parent != null) {
          sb.append(" extends ");
          sb.append(MyAlloyLibrary.removeSlash(ps.parent.label));
        }
      }

      sb.append(" {");

      List<String> fieldStrings = new ArrayList<>();

      fieldAfterSig = true;

      for (Field f : x.getFields()) {
        fieldStrings.add(f.accept(this));
      }
      if (!fieldStrings.isEmpty()) {
        sb.append(String.join(",", fieldStrings)).append(' ');
      }

      sb.append("}\n");

      fieldAfterSig = false;
      isRoot = true;

      if (!x.getFacts().isEmpty()) {

        sb.append("{\n");

        int size = x.getFacts().size();
        String[] facts = new String[size];

        isImplicitFact = true;

        for (int i = 0; i < size; i++) {
          Expr fact = x.getFacts().get(i);
          facts[i] = fact.accept(this);
        }

        isImplicitFact = false;

        sb.append(String.join(" ", facts));

        sb.append("}\n");
      }


      return sb.toString();
    }

    return MyAlloyLibrary.removeSlash(x.label);
  }

  @Override
  public String visit(Field x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }
    if (fieldAfterSig) {
      String prefix = x.decl().disjoint != null ? "disj " : "";
      StringBuilder sb = new StringBuilder();
      String output = sb.append(' ').append(prefix + MyAlloyLibrary.removeSlash(x.label))
          .append(": ").append(x.decl().expr.accept(this)).toString();
      return output;
    }

    return x.label;
  }

  private String getNamesFromDecl(Decl decl) {
    List<String> names = new ArrayList<>();
    for (ExprHasName name : decl.names) {
      names.add(name.accept(this));
    }

    return String.join(",", names.toArray(new String[names.size()]));
  }
}
