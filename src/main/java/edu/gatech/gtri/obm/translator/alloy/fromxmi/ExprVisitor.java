package edu.gatech.gtri.obm.translator.alloy.fromxmi;

import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ExprVisitor extends VisitQuery<String> {

  private final Set<Expr> ignoredExprs;
  public boolean isRootSig = false;
  private boolean isRootExprList = true;
  private boolean fieldAfterSig = false;
  private boolean isImplicitFact = false;
  private boolean isSigFact = false;
  // used to include disj fields or not
  // disj fields are fields with the same type but not with Parameter stereotype
  private final Set<Sig.Field> parameterFields;

  public ExprVisitor(Set<Expr> ignoredExprs, Set<Sig.Field> parameterFields) {
    this.ignoredExprs = ignoredExprs;
    this.parameterFields = parameterFields;
  }

  @Override
  public String visit(ExprBinary x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    isRootSig = false;

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

    return sb.append(x.left.accept(this))
        .append(' ')
        .append(op)
        .append(' ')
        .append(x.right.accept(this))
        .toString();
  }

  @Override
  public String visit(ExprCall x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    isRootSig = false;

    String funcName = AlloyUtils.removeSlash(x.fun.label);
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

    isRootSig = false;

    return x.toString();
  }

  @Override
  public String visit(ExprList x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    isRootSig = false;

    if (isRootExprList || isSigFact) {

      isRootExprList = false;
      StringBuilder sb = new StringBuilder();
      for (Expr y : x.args) {

        if (ignoredExprs.contains(y)) {
          continue;
        }

        String fact = y.accept(this);

        if (!isImplicitFact) {
          sb.append("fact ").append("{").append(fact).append('}').append('\n');
        } else if (isImplicitFact) {
          sb.append('\t').append(fact).append('\n');
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

    // Remove empty strings from array
    args = Arrays.stream(args).filter(Predicate.isEqual("").negate()).toArray(String[]::new);
    return String.join(op, args);
  }

  @Override
  public String visit(ExprQt x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    isRootSig = false;

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
    return sb.append(op)
        .append(' ')
        .append(names)
        .append(": ")
        .append(AlloyUtils.removeSlash(sigType))
        .append(" | ")
        .append(sub)
        .toString();
  }

  @Override
  public String visit(ExprUnary x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    isRootSig = false;

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
    isRootSig = false;
    return ignoredExprs.contains(x) ? "" : x.label;
  }

  @Override
  public String visit(Sig x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    if (isRootSig) {

      StringBuilder sb = new StringBuilder();
      isRootSig = false;

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

      sb.append("sig ").append(AlloyUtils.removeSlash(x.label));

      if (x instanceof Sig.PrimSig) {
        Sig.PrimSig ps = (Sig.PrimSig) x;
        if (ps.parent != null) {
          sb.append(" extends ");
          sb.append(AlloyUtils.removeSlash(ps.parent.label));
        }
      }

      // ========== Start: fields inside signature ==========

      sb.append(" {");

      int numberOfFields = x.getFields().size();

      if (numberOfFields > 0) {
        // sb.append("\n");
        fieldAfterSig = true;

        Map<String, List<Sig.Field>> fieldByType = new HashMap<>(); // x.decl().expr.accept(this)
        for (Sig.Field f : x.getFields()) {
          fieldByType = sortFields(f, fieldByType);
        }
        String fields = "";
        StringBuilder sbb = new StringBuilder();

        List<String> sortedType = new ArrayList<>(fieldByType.keySet());
        Collections.sort(sortedType);

        for (String type : sortedType) {
          List<Sig.Field> fs = fieldByType.get(type);
          if (fs.size() == 1) {
            fields =
                (fields.length() == 0 ? sbb.append(' ') : sbb.append(", ")) // sbb.append(",\n
                    // "))
                    .append(AlloyUtils.removeSlash(fs.get(0).label))
                    .append(": ")
                    .append(type)
                    .toString();
          } else { // have to be > 1

            boolean isdisj = true;
            String[] labels = new String[fs.size()];
            for (int i = 0; i < fs.size(); i++) {

              if (this.parameterFields.contains(fs.get(i))) isdisj = false;
              labels[i] = AlloyUtils.removeSlash(fs.get(i).label);
            }
            if (isdisj)
              fields =
                  (fields.length() == 0
                          ? sbb.append(' ')
                          : /* sbb.append(",\n ")) */ sbb.append(", "))
                      .append("disj ")
                      .append(String.join(", ", labels))
                      .append(": ")
                      .append(type)
                      .toString();
            else
              fields =
                  (fields.length() == 0 ? sbb.append(' ') : sbb.append(", ")) /*
                                                                                    * sbb.
                                                                                    * append(",\n "
                                                                                    * ))
                                                                                    */
                      .append(String.join(", ", labels))
                      .append(": ")
                      .append(type)
                      .toString();
          }
        }

        // // Produce strings for each field
        // for (int i = 0; i < numberOfFields; i++) {
        // Sig.Field field = x.getFields().get(i);
        // fields[i] = field.accept(this);
        //
        // }
        // sb.append(String.join(",", fields)).append(' ');
        sb.append(fields); /* .append('\n'); */
        sb.append("}\n");
      } else sb.append("}\n");

      fieldAfterSig = false;

      // ========== End: signature fields ==========

      // ========== Start: implicit facts ==========

      int numberOfImplicitFacts = x.getFacts().size();

      if (numberOfImplicitFacts > 0) {

        String[] facts = new String[numberOfImplicitFacts];

        isImplicitFact = true;
        isSigFact = true;

        for (int i = 0; i < numberOfImplicitFacts; i++) {
          Expr fact = x.getFacts().get(i);
          facts[i] = fact.accept(this);
        }

        isImplicitFact = false;
        isSigFact = false;

        sb.append("{\n").append(String.join(" ", facts)).append('}').append('\n');
      }

      // ========== End: implicit facts ==========

      return sb.toString();
    }

    return AlloyUtils.removeSlash(x.label);
  }

  public Map<String, List<Field>> sortFields(Field x, Map<String, List<Field>> map) throws Err {

    if (ignoredExprs.contains(x)) {
      return map;
    }

    isRootSig = false;

    if (fieldAfterSig) {
      String type = x.decl().expr.accept(this);
      List<Field> fs = null;
      if (map.containsKey(type)) fs = map.get(type);
      else {
        fs = new ArrayList<>();
        map.put(type, fs);
      }
      fs.add(x);
    }

    return map;
  }

  @Override
  public String visit(Field x) throws Err {

    if (ignoredExprs.contains(x)) {
      return "";
    }

    isRootSig = false;

    if (fieldAfterSig) {

      StringBuilder sb = new StringBuilder();
      String output =
          sb.append(' ')
              .append(AlloyUtils.removeSlash(x.label))
              .append(": ")
              .append(x.decl().expr.accept(this))
              .toString();
      return output;
    }

    return x.label;
  }

  private String getNamesFromDecl(Decl decl) {

    isRootSig = false;

    List<String> names = new ArrayList<>();
    for (ExprHasName name : decl.names) {
      names.add(name.accept(this));
    }

    return String.join(",", names.toArray(new String[names.size()]));
  }
}
