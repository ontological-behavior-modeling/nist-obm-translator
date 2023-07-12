package edu.gatech.gtri.obm.translator.alloy.tofile;

import java.util.Set;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;

import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.CommandScope;
import edu.mit.csail.sdg.ast.Decl;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.ExprBinary;
import edu.mit.csail.sdg.ast.ExprCall;
import edu.mit.csail.sdg.ast.ExprConstant;
import edu.mit.csail.sdg.ast.ExprITE;
import edu.mit.csail.sdg.ast.ExprLet;
import edu.mit.csail.sdg.ast.ExprList;
import edu.mit.csail.sdg.ast.ExprQt;
import edu.mit.csail.sdg.ast.ExprUnary;
import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Type;

public class ExpressionComparator {

	private final Set<Expr> visitedExpressions;
	
	public ExpressionComparator() {
		visitedExpressions = new HashSet<>();
		
		try {
			System.setErr(new PrintStream(new FileOutputStream("error.log")));
		} catch(FileNotFoundException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public boolean compareTwoExpressions(Expr e1, Expr e2) {
		visitedExpressions.clear();
		boolean same = compareExpr(e1, e2);
		visitedExpressions.clear();
		return same;
		
	}
	
	public boolean compareCommand(Command c1, Command c2) {
		
		if(c1 == null && c2 == null) {
			return true;
		}
		if(c1 == null || c2 == null) {
			System.err.println("c1=" + c1);
			System.err.println("c2=" + c2);
			System.err.println("compareCommand: c1 != null || c2 != null");
			System.err.println();
			return false;
		}
		
		// ConstList<Sig> additionalExactScopes
		if(c1.additionalExactScopes.size() != c2.additionalExactScopes.size()) {
			System.err.println("compareCommand: "
				+ "c1.additionalExactScopes.size() != "
				+ "c2.additionalExactScopes.size()");
			System.err.println("c1.additionalExactScopes.size()=" 
				+ c1.additionalExactScopes.size());
			System.err.println("c2.additionalExactScopes.size()=" 
				+ c2.additionalExactScopes.size());
			return false;
		}
		
		for(int i = 0; i < c1.additionalExactScopes.size(); i++) {
			Sig s1 = c1.additionalExactScopes.get(i);
			Sig s2 = c2.additionalExactScopes.get(i);
			if(!compareSig(s1, s2)) {
				System.err.println("compareCommand: !compareSig(s1, s2) "
					+ "for i=" + i);
				System.err.println("sig1=" + s1);
				System.err.println("sig2=" + s2);
				return false;
			}
		}
		
		// int bitwidth
		if(c1.bitwidth != c2.bitwidth) {
			System.err.println("compareCommand: c1.bitwidth != c2.bitwidth");
			System.err.println("c1.bitwidth=" + c1.bitwidth);
			System.err.println("c2.bitwidth=" + c2.bitwidth);
			return false;
		}
		
		// boolean check
		if(c1.check != c2.check) {
			System.err.println("compareCommand: c1.check != c2.check");
			System.err.println("c1.check=" + c1.check);
			System.err.println("c2.check=" + c2.check);
			return false;
		}
		
		// int expects
		if(c1.expects != c2.expects) {
			System.err.println("compareCommand: c1.check != c2.check");
			System.err.println("c1.expects=" + c1.expects);
			System.err.println("c2.expects=" + c2.expects);
			return false;
		}
		
		// Expr formula
		if(!compareExpr(c1.formula, c2.formula)) {
			System.err.println("compareCommand: "
				+ "!compareExpr(c1.formula, c2.formula)");
			System.err.println("c1.formula=" + c1.formula);
			System.err.println("c2.formula=" + c2.formula);
			return false;
		}
		
		// String label
		if(!c1.label.equals(c2.label)) {
			System.err.println("compareCommand: !c1.label.equals(c2.label)");
			System.err.println("c1.label=" + c1.label);
			System.err.println("c2.label=" + c2.label);
			return false;
		}
		
		// int maxseq
		if(c1.maxseq != c2.maxseq) {
			System.err.println("compareCommand: c1.maxseq != c2.maxseq");
			System.err.println("c1.maxseq=" + c1.maxseq);
			System.err.println("c2.maxseq=" + c2.maxseq);
			return false;
		}
		// int maxstring
		if(c1.maxstring != c2.maxstring) {
			System.err.println("compareCommand: c1.maxstring != c2.maxstring");
			System.err.println("c1.maxstring=" + c1.maxstring);
			System.err.println("c2.maxstring=" + c2.maxstring);
			return false;
		}
		// int overall
		if(c1.overall != c2.overall) {
			System.err.println("compareCommand: c1.overall != c2.overall");
			System.err.println("c1.overall=" + c1.overall);
			System.err.println("c2.overall=" + c2.overall);
			return false;
		}
		// Command parent
		if(!compareCommand(c1.parent, c2.parent)) {
			System.err.println("compareCommand: "
				+ "!compareCommand(c1.parent, c2.parent)");
			System.err.println("c1.parent=" + c1.parent);
			System.err.println("c2.parent=" + c2.parent);
			return false;
		}
		// Pos pos (ignore)
		
		// ConstList<CommandScope> scope
		if(c1.scope.size() != c2.scope.size()) {
			System.err.println("compareCommand: "
				+ "c1.scope.size() != c2.scope.size()");
			System.err.println("c1.scope.size()=" + c1.scope.size());
			System.err.println("c2.scope.size()=" + c2.scope.size());
			return false;
		}
		
		for(int i = 0; i < c1.scope.size(); i++) {
			CommandScope cs1 = c1.scope.get(i);
			CommandScope cs2 = c2.scope.get(i);
			
			if(!compareCommandScope(cs1, cs2)) {
				System.err.println("compareCommand: "
					+ "!compareCommandScope(cs1, cs2) for i=" + i);
			}
		}
		
		return true;
	}
	
	public boolean compareCommandScope(CommandScope cs1, CommandScope cs2) {
		
		if(cs1 == null && cs2 == null) {
			return true;
		}
		
		if(cs1 == null || cs2 == null) {
			System.err.println(
				"compareCommand: cs1 == null || cs2 == null");
			System.err.println("cs1=" + cs1);
			System.err.println("cs2=" + cs2);
			return false;
		}
		
		// int endingScope
		if(cs1.endingScope != cs2.endingScope) {
			System.err.println("compareCommandScope: "
				+ "cs1.endingScope != cs2.endingScope");
			System.err.println("cs1.endingScope=" + cs1.endingScope);
			System.err.println("cs2.endingScope=" + cs2.endingScope);
			return false;
		}
		// int increment
		if(cs1.endingScope != cs2.endingScope) {
			System.err.println("compareCommandScope: "
				+ "cs1.increment != cs2.increment");
			System.err.println("cs1.increment=" + cs1.increment);
			System.err.println("cs2.increment=" + cs2.increment);
			return false;
		}
		
		// boolean isExact
		if(cs1.isExact != cs2.isExact) {
			System.err.println("compareCommandScope: "
				+ "cs1.isExact != cs2.isExact");
			System.err.println("cs1.isExact=" + cs1.isExact);
			System.err.println("cs2.isExact=" + cs2.isExact);
			return false;
		}
		// Pos pos (ignored)
		
		// Sig sig
		if(!compareSig(cs1.sig, cs2.sig)) {
			System.err.println("compareCommandScope: "
				+ "!compareSig(cs1.sig, cs2.sig)");
			System.err.println("cs1.sig=" + cs1.sig);
			System.err.println("cs2.sig=" + cs2.sig);
			return false;
		}
		
		// int startingScope
		if(cs1.startingScope != cs2.startingScope) {
			System.err.println("compareCommandScope: "
				+ "cs1.startingScope != cs2.startingScope");
			System.err.println("cs1.startingScope=" + cs1.startingScope);
			System.err.println("cs2.startingScope=" + cs2.startingScope);
			return false;
		}
		
		return true;
	}
	
	private boolean compareDecl(Decl d1, Decl d2) {
		
		if(d1 == null && d2 == null) {
			return true;
		}
		
		if(d1 == null && d2 != null) {
			System.err.println("Decl d1 = " + d1);
			System.err.println("Decl d2 = " + d2);
			System.err.println("d1 == null && d2 != null");
			System.err.println();
			return false;
		}
		if(d1 != null && d2 == null) {
			System.err.println("Decl d1 = " + d1);
			System.err.println("Decl d2 = " + d2);
			System.err.println("d1 != null && d2 == null");
			System.err.println();
			return false;
		}
		
		if(d1.disjoint == null && d2.disjoint != null) {
			System.err.println("Decl d1 = " + d1);
			System.err.println("Decl d2 = " + d2);
			System.err.println("compareDecls: d1.disjoint == null && d2.disjoint != null");
			System.err.println();
			return false;
		}
		if(d1.disjoint != null && d2.disjoint == null) {
			System.err.println("Decl d1 = " + d1);
			System.err.println("Decl d2 = " + d2);
			System.err.println("compareDecls: d1.disjoint != null && d2.disjoint == null");
			System.err.println();
			return false;
		}
		
		if(d1.disjoint2 == null && d2.disjoint2 != null) {
			System.err.println("Decl d1 = " + d1);
			System.err.println("Decl d2 = " + d2);
			System.err.println("compareDecls: d1.disjoint2 == null && d2.disjoint2 != null");
			System.err.println();
			return false;
		}
		if(d1.disjoint2 != null && d2.disjoint2 == null) {
			System.err.println("Decl d1 = " + d1);
			System.err.println("Decl d2 = " + d2);
			System.err.println("compareDecls: d1.disjoint2 != null && d2.disjoint2 == null");
			System.err.println();
			return false;
		}
		
		if(!compareExpr(d1.expr, d2.expr)) {
			System.err.println("Decl d1 = " + d1);
			System.err.println("Decl d2 = " + d2);
			System.err.println("compareDecl: !compareExpr(d1.expr, d2.expr)");
			System.err.println("d1.expr = " + d1.expr);
			System.err.println("d2.expr = " + d2.expr);
			System.err.println();
			return false;
		}
		
		if(d1.isPrivate == null && d2.isPrivate != null) {
			System.err.println("Decl d1 = " + d1);
			System.err.println("Decl d2 = " + d2);
			System.err.println("compareDecls: d1.isPrivate == null && d2.isPrivate != null");
			System.err.println();
			return false;
		}
		if(d1.isPrivate != null && d2.isPrivate == null) {
			System.err.println("Decl d1 = " + d1);
			System.err.println("Decl d2 = " + d2);
			System.err.println("compareDecls: d1.isPrivate != null && d2.isPrivate == null");
			System.err.println();
			return false;
		}
				
		if(d1.names.size() != d2.names.size()) {
			System.err.println("Decl d1 = " + d1);
			System.err.println("Decl d2 = " + d2);
			System.err.println("compareDecls: d1.names.size() != d2.names.size()");
			System.err.println("d1.names.size() = " + d1.names.size());
			System.err.println("d2.names.size() = " + d2.names.size());
			System.err.println();
			return false;
		}
		
		for(int i = 0; i < d1.names.size(); i++) {
			if(!compareExpr(d1.names.get(i), d2.names.get(i))) {
				System.err.println("Decl d1 = " + d1);
				System.err.println("Decl d2 = " + d2);
				System.err.println("compareDecls: name " + i + " are different");
				System.err.println();
				return false;
			}
		}
		
		return true;
	}
	
	private boolean compareExpr(Expr expr1, Expr expr2) {
		
		if(expr1 == null && expr2 == null) {
			return true;
		}
		
		if(expr1 == null && expr2 != null) {
			System.err.println(expr1);
			System.err.println(expr2);
			System.err.println("compareExpr: expr1 == null && expr2 != null");
			System.err.println();
			return false;
		}
		if(expr1 != null && expr2 == null) {
			System.err.println(expr1);
			System.err.println(expr2);
			System.err.println("compareExpr: expr1 != null && expr2 == null");
			System.err.println();
			return false;
		}
		
		expr1 = expr1.deNOP();
		expr2 = expr2.deNOP();
		
		if(visitedExpressions.contains(expr1) && visitedExpressions.contains(expr2)) {
			return true;
		}
		
		visitedExpressions.add(expr1);
		visitedExpressions.add(expr2);
		
		if(!expr1.getClass().equals(expr2.getClass())) {
			System.err.println(expr1);
			System.err.println(expr2);
			System.err.println("compareExpr: !expr1.getClass().equals(expr2.getClass())");
			System.err.println("expr1.getClass(): " + expr1.getClass());
			System.err.println("expr2.getClass(): " + expr2.getClass());
			System.err.println();
			return false;
		}
		
		if(expr1.getClass().equals(Expr.class)) {
			System.err.println("Expr: not implemented");
		}
		else if(expr1.getClass().equals(ExprBinary.class)) {
			if(!compareExprBinary((ExprBinary) expr1, (ExprBinary) expr2)) {
				System.err.println(expr1);
				System.err.println(expr2);
				System.err.println("compareExpr: !compareExprBinary((ExprBinary) expr1, (ExprBinary) expr2)");
				System.err.println();
				return false;
			}
		}
		else if(expr1.getClass().equals(ExprCall.class)) {
			if(!compareExprCall((ExprCall) expr1, (ExprCall) expr2)) {
				System.err.println(expr1);
				System.err.println(expr2);
				System.err.println("compareExpr: expr1.getClass().equals(ExprCall.class)");
				System.err.println();
				return false;
			}
		}
		else if(expr1.getClass().equals(ExprConstant.class)) {
			if(!compareExprConstant((ExprConstant) expr1, (ExprConstant) expr2)) {
				System.err.println(expr1);
				System.err.println(expr2);
				System.err.println("compareExpr: !compareExprConstant((ExprConstant) expr1, (ExprConstant) expr2)");
				System.err.println();
				return false;
			}
		}
		else if(expr1.getClass().equals(ExprITE.class)) {
			System.err.println("ExprITE: not implemented");
		}
		else if(expr1.getClass().equals(ExprLet.class)) {
			System.err.println("ExprLet: not implemented");
		}
		else if(expr1.getClass().equals(ExprList.class)) {
			if(!compareExprList((ExprList) expr1, (ExprList) expr2)) {
				System.err.println(expr1);
				System.err.println(expr2);
				System.err.println("compareExpr: !compareExprList((ExprList) expr1, (ExprList) expr2)");
				System.err.println();
				return false;
			}
		}
		else if(expr1.getClass().equals(ExprQt.class)) {
			if(!compareExprQt((ExprQt) expr1, (ExprQt) expr2)) {
				System.err.println(expr1);
				System.err.println(expr2);
				System.err.println("compareExpr: !compareExprQt((ExprQt) expr1, (ExprQt) expr2)");
				System.err.println();
				return false;
			}
		}
		else if(expr1.getClass().equals(ExprUnary.class)) {
			if(!compareExprUnary((ExprUnary) expr1, (ExprUnary) expr2)) {
				System.err.println(expr1);
				System.err.println(expr2);
				System.err.println("compareExpr: !compareExprUnary((ExprUnary) expr1, (ExprUnary) expr2)");
				System.err.println();
				return false;
			}
		}
		else if(expr1.getClass().equals(ExprVar.class)) {
			if(!compareExprVar((ExprVar) expr1, (ExprVar) expr2)) {
				System.err.println(expr1);
				System.err.println(expr2);
				System.err.println("compareExpr: !compareExprVar((ExprVar) expr1, (ExprVar) expr2)");
				System.err.println();
				return false;
			}
		}
		else if(expr1.getClass().equals(Sig.Field.class)) {
			if(!compareSigField((Sig.Field) expr1, (Sig.Field) expr2)) {
				System.err.println(expr1);
				System.err.println(expr2);
				System.err.println("compareExpr: !compareSigField((Sig.Field) expr1, (Sig.Field) expr2)");
				System.err.println();
				return false;
			}
		}
		else if(expr1.getClass().equals(Sig.class) || expr1.getClass().equals(Sig.PrimSig.class)) {
			if(!compareSig((Sig) expr1, (Sig) expr2)) {
				System.err.println(expr1);
				System.err.println(expr2);
				System.err.println("compareExpr: !compareSig((Sig) expr1, (Sig) expr2)");
				System.err.println();
				return false;
			}
		}
		else {
			System.err.println("Unexpected class: " + expr1.getClass());
			return false;
		}
		
		return true;
	}
	
	private boolean compareExprBinary(ExprBinary expr1, ExprBinary expr2) {
		
		
		
		if(expr1 == null && expr2 == null) {
			return true;
		}
		
		if(expr1 == null && expr2 != null) {
			System.err.println("expr1: " + expr1);
			System.err.println("expr2: " + expr2);
			System.err.println("compareExprBinary: expr1 == null && expr2 != null");
			return false;
		}
		if(expr1 != null && expr2 == null) {
			System.err.println("expr1: " + expr1);
			System.err.println("expr2: " + expr2);
			System.err.println("compareExprBinary: expr1 != null && expr2 == null");
			return false;
		}
		if(!compareExpr(expr1.left, expr2.left)) {
			System.err.println("expr1: " + expr1);
			System.err.println("expr2: " + expr2);
			System.err.println("ExprBinary: !compareExpr(expr1.left, expr2.left)");
			System.err.println("expr1.left: " + expr1.left);
			System.err.println("expr2.left: " + expr2.left);
			return false;
		}
		
		if(expr1.op != expr2.op) {
			System.err.println("expr1: " + expr1);
			System.err.println("expr2: " + expr2);
			System.err.println("ExprBinary: expr1.op != expr2.op");
			System.err.println("expr1.op: " + expr1.op);
			System.err.println("expr2.op: " + expr2.op);
			return false;
		}
		
		if(!compareExpr(expr1.right, expr2.right)) {
			System.err.println("expr1: " + expr1);
			System.err.println("expr2: " + expr2);
			System.err.println("ExprBinary: !compareExpr(expr1.right, expr2.right)");
			System.err.println("expr1.right: " + expr1.right);
			System.err.println("expr2.right: " + expr2.right);
			return false;
		}
		
		return true;
	}
	
	private boolean compareExprCall(ExprCall expr1, ExprCall expr2) {
		
		if(expr1 == null && expr2 == null) {
			return true;
		}
		
		if(expr1 == null && expr2 != null) {
			System.err.println("ExprCall1: " + expr1);
			System.err.println("ExprCall2: " + expr2);
			return false;
		}
		if(expr1 != null && expr2 == null) {
			System.err.println("ExprCall1: " + expr1);
			System.err.println("ExprCall2: " + expr2);
			return false;
		}
		
		if(expr1.args.size() != expr2.args.size()) {
			System.err.println("ExprCall1: " + expr1);
			System.err.println("ExprCall2: " + expr2);
			System.err.println("ExprCall: expr1.args.size() != expr2.args.size()");
			return false;
		}
		
		Iterator<Expr> iter1 = expr1.args.iterator();
		Iterator<Expr> iter2 = expr2.args.iterator();
		
		while(iter1.hasNext() && iter2.hasNext()) {
			
			Expr next1 = iter1.next();
			Expr next2 = iter2.next();
						
			if(!compareExpr(next1, next2)) {
				System.err.println("ExprCall1: " + expr1);
				System.err.println("ExprCall2: " + expr2);
				System.err.println("ExprCall: different args");
				return false;
			}
		}
		
		if(expr1.weight != expr2.weight) {
			System.err.println("ExprCall1: " + expr1);
			System.err.println("ExprCall2: " + expr2);
			System.err.println("ExprCall: different weight");
			return false;
		}
		
		if(!compareFunctions(expr1.fun, expr2.fun)) {
			System.err.println("ExprCall1: " + expr1);
			System.err.println("ExprCall2: " + expr2);
			System.err.println("ExprCall: !compareFunctions(expr1.fun, expr2.fun)");
			System.err.println("expr1.fun: " + expr1.fun);
			System.err.println("expr2.fun: " + expr2.fun);
			return false;
		}
		
		return true;
	}
	
	private boolean compareExprConstant(ExprConstant expr1, ExprConstant expr2) {
		
		if(expr1 == null && expr2 == null) {
			return true;
		}
		
		if(expr1 == null && expr2 != null) {
			System.err.println("ExprConstant1: " + expr1);
			System.err.println("ExprConstant2: " + expr2);
			return false;
		}
		if(expr1 != null && expr2 == null) {
			System.err.println("ExprConstant1: " + expr1);
			System.err.println("ExprConstant2: " + expr2);
			return false;
		}
		
		if(expr1.op != expr2.op) {
			System.err.println("ExprConstant1: " + expr1);
			System.err.println("ExprConstant2: " + expr2);
			System.err.println("ExprConstant: expr1.op != expr2.op");
			return false;
		}
		
		if(expr1.num != expr2.num) {
			System.err.println("ExprConstant1: " + expr1);
			System.err.println("ExprConstant2: " + expr2);
			System.err.println("ExprConstant: expr1.num != expr2.num");
			return false;
		}
		
		if(!expr1.string.equals(expr2.string)) {
			System.err.println("ExprConstant1: " + expr1);
			System.err.println("ExprConstant2: " + expr2);
			System.err.println("ExprConstant: expr1.string != expr2.string");
			return false;
		}
		
		return true;
	}
	
	private boolean compareExprList(ExprList expr1, ExprList expr2) {
		
		if(expr1 == null && expr2 == null) {
			return true;
		}
		if(expr1 != null && expr2 == null) {
			System.err.println("ExprList1: " + expr1);
			System.err.println("ExprList2: " + expr2);
			System.err.println("expr1 != null && expr2 == null");
			return false;
		}
		if(expr1 == null && expr2 != null) {
			System.err.println("ExprList1: " + expr1);
			System.err.println("ExprList2: " + expr2);
			System.err.println("expr1 == null && expr2 != null");
			return false;
		}
		
		if(expr1.op != expr2.op) {
			System.err.println("ExprList1: " + expr1);
			System.err.println("ExprList2: " + expr2);
			System.err.println("ExprList: expr1.op != expr2.op");
			System.err.println("expr1.op = " + expr1.op);
			System.err.println("expr2.op = " + expr2.op);
			return false;
		}
		
		if(expr1.args.size() != expr2.args.size()) {
			System.err.println("ExprList1: " + expr1);
			System.err.println("ExprList2: " + expr2);
			System.err.println("ExprList: expr1.args.size() != expr2.args.size()");
			System.err.println("expr1.args.size() = " + expr1.args.size());
			System.err.println("expr2.args.size() = " + expr2.args.size());
			return false;
		}
		
		for(int i = 0; i < expr1.args.size(); i++) {
			if(!compareExpr(expr1.args.get(i), expr2.args.get(i))) {
				System.err.println("ExprList: expr1.args != expr2.args for i = " + i);
				System.err.println("expr1.args: " + expr1.args);
				System.err.println("expr2.args: " + expr2.args);
				System.err.println();
				return false;
			}
		}
		
		return true;
	}
	
	private boolean compareExprQt(ExprQt expr1, ExprQt expr2) {
		
		if(expr1 == null && expr2 == null) {
			return true;
		}
		
		if(expr1 == null && expr2 != null) {
			System.err.println("ExprQt1: " + expr1);
			System.err.println("ExprQt2: " + expr2);
			System.err.println("compareExprQt: expr1 == null && expr2 != null");
			System.err.println();
			return false;
		}
		if(expr1 != null && expr2 == null) {
			System.err.println("ExprQt1: " + expr1);
			System.err.println("ExprQt2: " + expr2);
			System.err.println("compareExprQt: expr1 != null && expr2 == null");
			System.err.println();
			return false;
		}
		
		if(expr1.op != expr2.op) {
			System.err.println("ExprQt1: " + expr1);
			System.err.println("ExprQt2: " + expr2);
			System.err.println("compareExprQt: expr1.op != expr2.op");
			System.err.println("expr1.op = " + expr1.op);
			System.err.println("expr2.op = " + expr2.op);
			System.err.println();
			return false;
		}
		
		if(expr1.decls.size() != expr2.decls.size()) {
			System.err.println("ExprQt1: " + expr1);
			System.err.println("ExprQt2: " + expr2);
			System.err.println("compareExprQt: expr1.decls.size() != expr2.decls.size()");
			System.err.println("expr1.decls.size() = " + expr1.decls.size());
			System.err.println("expr2.decls.size() = " + expr2.decls.size());
			System.err.println();
			return false;
		}
		
		Iterator<Decl> iterator1 = expr1.decls.iterator();
		Iterator<Decl> iterator2 = expr2.decls.iterator();
		
		while(iterator1.hasNext() && iterator2.hasNext()) {
			Decl d1 = iterator1.next();
			Decl d2 = iterator2.next();
			if(!compareDecl(d1, d2)) {
				System.err.println("ExprQt1: " + expr1);
				System.err.println("ExprQt2: " + expr2);
				System.err.println("compareExprQt: !compareDecl(d1, d2)");
				System.err.println();
				return false;
			}
		}
		

		if(!compareExpr(expr1.sub, expr2.sub)) {
			System.err.println("ExprQt1: " + expr1);
			System.err.println("ExprQt2: " + expr2);
			System.err.println("compareExprQt: !compareExpr(expr1.sub, expr2.sub)");
			System.err.println("expr1.sub: " + expr1.sub);
			System.err.println("expr2.sub: " + expr2.sub);
			System.err.println();
			return false;
		}
				
		return true;
	}
	
	private boolean compareExprUnary(ExprUnary expr1, ExprUnary expr2) {
	
		
		if(expr1 == null && expr2 == null) {
			return true;
		}
		
		if(expr1 != null && expr2 == null) {
			System.err.println("ExprUnary1: " + expr1);
			System.err.println("ExprUnary2: " + expr2);
			System.err.println("compareExprUnary: expr1 != null && expr2 == null");
			System.err.println();
			return false;
		}
		if(expr1 == null && expr2 != null) {
			System.err.println("ExprUnary1: " + expr1);
			System.err.println("ExprUnary2: " + expr2);
			System.err.println("compareExprUnary: expr1 == null && expr2 != null");
			System.err.println();
			return false;
		}
		
		if(expr1.op != expr2.op) {
			System.err.println("ExprUnary1: " + expr1);
			System.err.println("ExprUnary2: " + expr2);
			System.err.println("compareExprUnary: expr1.op != expr2.op");
			System.err.println("expr1.op = " + expr1.op);
			System.err.println("expr1.op = " + expr2.op);
			System.err.println();
			return false;
		}
		if(!compareExpr(expr1.sub, expr2.sub)) {
			System.err.println("ExprUnary1: " + expr1);
			System.err.println("ExprUnary2: " + expr2);
			System.err.println("compareExprUnary: !compareExpr(expr1.sub, expr2.sub)");
			System.err.println("expr1.sub = " + expr1.sub);
			System.err.println("expr2.sub = " + expr2.sub);
			System.err.println();
			return false;
		}
		
		return true;
	}
	
	private boolean compareExprVar(ExprVar expr1, ExprVar expr2) {
		
		if(expr1 == null && expr2 == null) {
			return true;
		}
		if(expr1 == null && expr2 != null) {
			System.err.println("ExprVar1: " + expr1);
			System.err.println("ExprVar2: " + expr2);
			System.err.println("compareExprVar: expr1 == null && expr2 != null");
			System.err.println();
			return false;
		}
		if(expr1 != null && expr2 == null) {
			System.err.println("ExprVar1: " + expr1);
			System.err.println("ExprVar2: " + expr2);
			System.err.println("compareExprVar: expr1 != null && expr2 == null");
			System.err.println();
			return false;
		}
//		if(!expr1.label.equals(expr2.label)) {
//			System.err.println("ExprVar1: " + expr1);
//			System.err.println("ExprVar2: " + expr2);
//			System.err.println("compareExprVar: expr1.label != expr2.label");
//			System.err.println("expr1.label: " + expr1.label);
//			System.err.println("expr2.label: " + expr2.label);
//			System.err.println();
//			return false;
//		}
		if(!compareType(expr1.type(), expr2.type())) {
			System.err.println("ExprVar1: " + expr1);
			System.err.println("ExprVar2: " + expr2);
			System.err.println("compareExprVar: expr1.type() != expr2.type()");
			System.err.println("expr1.type() = " + expr1.type());
			System.err.println("expr2.type() = " + expr2.type());
			System.err.println();
			return false;
		}
		
		return true;
	}
	
	private boolean compareFunctions(Func func1, Func func2) {
 		
 		if(func1 == null && func2 == null) {
 			return true;
 		}
		
		if(func1 == null && func2 != null) {
			System.err.println("Func func1: " + func1);
			System.err.println("Func func2: " + func2);
			System.err.println("compareFunction: func1 == null && func2 != null");
			return false;
		}
		
		if(func1 != null && func2 == null) {
			System.err.println("Func func1: " + func1);
			System.err.println("Func func2: " + func2);
			System.err.println("compareFunction: func1 != null && func2 == null");
			return false;
		}
				
		if(func1.decls.size() != func2.decls.size()) {
			System.err.println("Func func1: " + func1);
			System.err.println("Func func2: " + func2);
			System.err.println("compareFunction: func1.decls.size() != func2.decls.size()");
			return false;
		}
		
		for(int i = 0; i < func1.decls.size(); i++) {
			if(!compareDecl(func1.decls.get(i), func2.decls.get(i))) {
				System.err.println("Func func1: " + func1);
				System.err.println("Func func2: " + func2);
				System.err.println("compareFunction: !compareDecl(func1.decls.get(i), func2.decls.get(i)) for i=" + i);
				return false;
			}
		}
		
		if(func1.isPred != func2.isPred) {
			System.err.println("Func func1: " + func1);
			System.err.println("Func func2: " + func2);
			System.err.println("compareFunction: func1.isPred != func2.isPred");
			return false;
		}		
		if(func1.isPrivate == null && func2.isPrivate != null) {
			System.err.println("Func func1: " + func1);
			System.err.println("Func func2: " + func2);
			System.err.println("compareFunctions: func1.isPrivate == null && func2.isPrivate != null");
			return false;
		}
		if(func1.isPrivate != null && func2.isPrivate == null) {
			System.err.println("Func func1: " + func1);
			System.err.println("Func func2: " + func2);
			System.err.println("compareFunctions: func1.isPrivate != null && func2.isPrivate == null");
			return false;
		}		
		if(!MyAlloyLibrary.removeSlash(func1.label).equals(MyAlloyLibrary.removeSlash(func2.label))) {
			System.err.println("Func func1: " + func1);
			System.err.println("Func func2: " + func2);
			System.err.println("compareFunctions: !MyAlloyLibrary.removeSlash(func1.label).equals(MyAlloyLibrary.removeSlash(func2.label))");
			System.err.println("func1.label: " + func1.label);
			System.err.println("func2.label: " + func2.label);
			return false;
		}		
		if(!compareExpr(func1.returnDecl, func1.returnDecl)) {
			System.err.println("Func func1: " + func1);
			System.err.println("Func func2: " + func2);
			System.err.println("compareFunctions: !compareExpr(func1.returnDecl, func1.returnDecl)");
			return false;
		}
		
		return true;
	}
	
	private boolean comparePrimSig(Sig.PrimSig primSig1, Sig.PrimSig primSig2) {
		
		if(primSig1.label.equals("univ") && primSig2.label.equals("univ")) {
			return true;
		}
		
		if(primSig1.children().size() != primSig2.children().size()) {
			System.err.println(primSig1);
			System.err.println(primSig2);
			System.err.println("comparePrimSig: primSig1.children().size() != primSig2.children().size()");
			System.err.println("primSig1.children().size() = " + primSig1.children().size());
			System.err.println("primSig2.children().size() = " + primSig2.children().size());
			System.err.println();
			return false;
		}
		return true;
	}
	
	private boolean compareSig(Sig sig1, Sig sig2) {
		
		if(sig1 == null && sig2 == null) {
			return true;
		}
		if(sig1 == null && sig2 != null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1 == null && sig2 != null");
			return false;
		}
		if(sig1 != null && sig2 == null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1 != null && sig2 == null");
			return false;
		}
		
		if(sig1 instanceof Sig.PrimSig && sig2 instanceof Sig.PrimSig
		&& !comparePrimSig((Sig.PrimSig) sig1, (Sig.PrimSig) sig2)) {
			return false;
		}
			
		// ConstList<Attr> comparison not implemented.

		if(sig1.builtin != sig2.builtin) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.builtin != sig2.builtin");
			System.err.println("sig1.builtin = " + sig1.builtin);
			System.err.println("sig2.builtin = " + sig2.builtin);
			return false;
		}		
		if(!compareDecl(sig1.decl, sig2.decl)) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig:!compareDecl(sig1.decl, sig2.decl)");
			return false;
		}
		
		if(sig1.isAbstract == null && sig2.isAbstract != null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isAbstract == null && sig2.isAbstract != null");
			return false;
		}
		if(sig1.isAbstract != null && sig2.isAbstract == null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isAbstract != null && sig2.isAbstract == null");
			return false;
		}
		if(sig1.isEnum == null && sig2.isEnum != null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isEnum == null && sig2.isEnum != null");
			return false;
		}
		if(sig1.isEnum != null && sig2.isEnum == null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isEnum != null && sig2.isEnum == null");
			return false;
		}
		if(sig1.isLone == null && sig2.isLone != null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isLone == null && sig2.isLone != null");
			return false;
		}
		if(sig1.isLone != null && sig2.isLone == null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isLone != null && sig2.isLone == null");
			return false;
		}
		if(sig1.isMeta == null && sig2.isMeta != null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isMeta == null && sig2.isMeta != null");
			return false;
		}
		if(sig1.isMeta != null && sig2.isMeta == null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isMeta != null && sig2.isMeta == null");
			return false;
		}
		if(sig1.isOne == null && sig2.isOne != null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isOne == null && sig2.isOne != null");
			return false;
		}
		if(sig1.isOne != null && sig2.isOne == null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isOne != null && sig2.isOne == null");
			return false;
		}
		if(sig1.isPrivate == null && sig2.isPrivate != null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isPrivate == null && sig2.isPrivate != null");
			return false;
		}
		if(sig1.isPrivate != null && sig2.isPrivate == null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isPrivate != null && sig2.isPrivate == null");
			return false;
		}
		if(sig1.isSome == null && sig2.isSome != null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isSome == null && sig2.isSome != null");
			return false;
		}
		if(sig1.isSome != null && sig2.isSome == null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isSome != null && sig2.isSome == null");
			return false;
		}
		if(sig1.isSubset == null && sig2.isSubset != null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isSubset == null && sig2.isSubset != null");
			return false;
		}
		if(sig1.isSubset != null && sig2.isSubset == null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isSubset != null && sig2.isSubset == null");
			return false;
		}
		if(sig1.isSubsig == null && sig2.isSubsig != null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isSubsig == null && sig2.isSubsig != null");
			return false;
		}
		if(sig1.isSubsig != null && sig2.isSubsig == null) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isSubsig != null && sig2.isSubsig == null");
			return false;
		}
		if(!MyAlloyLibrary.removeSlash(sig1.label).equals(MyAlloyLibrary.removeSlash(sig2.label))) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: !sig1.label.equals(sig2.label)");
			System.err.println("sig1.label: " + MyAlloyLibrary.removeSlash(sig1.label));
			System.err.println("sig2.label: " + MyAlloyLibrary.removeSlash(sig2.label));
			System.err.println();
			return false;
		}	
		if(sig1.getDepth() != sig2.getDepth()) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.getDepth() != sig2.getDepth()");
			System.err.println("sig1.getDepth() = " + sig1.getDepth());
			System.err.println("sig2.getDepth() = " + sig2.getDepth());
			return false;
		}
				
		if(sig1.getFacts().size() != sig2.getFacts().size()) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.getFacts().size() != sig2.getFacts().size()");
			System.err.println("sig1.getFacts().size(): " + sig1.getFacts().size());
			System.err.println("sig2.getFacts().size(): " + sig2.getFacts().size());
			return false;
		}
		
		Iterator<Expr> sig1Facts = sig1.getFacts().iterator();
		Iterator<Expr> sig2Facts = sig2.getFacts().iterator();
		
		while(sig1Facts.hasNext() && sig2Facts.hasNext()) {
			
			Expr next1 = sig1Facts.next();
			Expr next2 = sig2Facts.next();
			
			if(!compareExpr(next1, next2)) {
				System.err.println("sig1: " + sig1);
				System.err.println("sig2: " + sig2);
				System.err.println("compareSig: !compareExpr(sig1Facts.next(), sig2Facts.next())");
				return false;
			}
		}
		
		if(sig1.getFieldDecls().size() != sig2.getFieldDecls().size()) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.getFieldDecls().size() != sig2.getFieldDecls().size()");
			return false;
		}
		
		Iterator<Decl> sig1FieldDecls = sig1.getFieldDecls().iterator();
		Iterator<Decl> sig2FieldDecls = sig2.getFieldDecls().iterator();
		
		while(sig1FieldDecls.hasNext() && sig2FieldDecls.hasNext()) {
			if(!compareDecl(sig1FieldDecls.next(), sig2FieldDecls.next())) {
				System.err.println("sig1: " + sig1);
				System.err.println("sig2: " + sig2);
				System.err.println("compareSig: !compareDecl(sig1FieldDecls.next(), sig2FieldDecls.next())");
				return false;
			}
		}
				
		if(sig1.getFields().size() != sig2.getFields().size()) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.getFields().size() != sig2.getFields().size()");
			return false;
		}
		
		Iterator<Sig.Field> sig1Fields = sig1.getFields().iterator();
		Iterator<Sig.Field> sig2Fields = sig2.getFields().iterator(); 
		
		while(sig1Fields.hasNext() && sig2Fields.hasNext()) {
			if(!compareSigField(sig1Fields.next(), sig2Fields.next())) {
				System.err.println("sig1: " + sig1);
				System.err.println("sig2: " + sig2);
				System.err.println("compareSig: !compareSigField(sig1Fields.next(), sig2Fields.next())");
				return false;
			}
		}
		
		
		// getHTML() not implemented
		// getSubnodes() not implemented
		
		if(sig1.isTopLevel() != sig2.isTopLevel()) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: sig1.isTopLevel() != sig2.isTopLevel()");
			return false;
		}
		if(!MyAlloyLibrary.removeSlash(sig1.toString()).equals(MyAlloyLibrary.removeSlash(sig2.toString()))) {
			System.err.println("sig1: " + sig1);
			System.err.println("sig2: " + sig2);
			System.err.println("compareSig: !sig1.toString().equals(sig2.toString())");
			return false;
		}
		
		return true;
	}

	private boolean compareSigField(Sig.Field sig1Field, Sig.Field sig2Field) {
		
		if(sig1Field == null && sig2Field == null) {
			return true;
		}
		
		if(sig1Field == null && sig2Field != null) {
			System.err.println("Sig.Field1: " + sig1Field);
			System.err.println("Sig.Field2: " + sig2Field);
			System.err.println("sig1Field == null && sig2Field != null");
		}
		else if(sig1Field != null && sig2Field == null) {
			System.err.println("Sig.Field1: " + sig1Field);
			System.err.println("Sig.Field2: " + sig2Field);
			System.err.println("sig1Field != null && sig2Field == null");
		}
		
		if(sig1Field.defined != sig2Field.defined) {
			System.err.println("Sig.Field1: " + sig1Field);
			System.err.println("Sig.Field2: " + sig2Field);
			System.err.println("Sig.Field: sig1.defined != sig2.defined");
			return false;
		}
		
		if((sig1Field.isMeta == null && sig2Field.isMeta != null) || sig1Field.isMeta != null && sig2Field.isMeta == null) {
			System.err.println("Sig.Field1: " + sig1Field);
			System.err.println("Sig.Field2: " + sig2Field);
			System.err.println("Sig.Field: isMeta different");
			return false;
		}
		
		if((sig1Field.isPrivate == null && sig2Field.isPrivate != null) || sig1Field.isPrivate != null && sig2Field.isPrivate == null) {
			System.err.println("Sig.Field1: " + sig1Field);
			System.err.println("Sig.Field2: " + sig2Field);
			System.err.println("Sig.Field: isPrivate different");
			return false;
		}
		
		return true;
	}
	
	private boolean compareType(Type t1, Type t2) {
		
		if(t1 == null && t2 == null) {
			return true;
		}
		
		if(t1 == null || t2 == null) {
			return false;
		}
		
//		if(!t1.toString().equals(t2.toString())) {
//			System.err.println("compareType: t1.toString() != t2.toString()");
//			System.err.println("t1.toString(): " + t1.toString());
//			System.err.println("t2.toString(): " + t2.toString());
//			return false;
//		}
		
		if(t1.is_bool != t2.is_bool) {
			System.err.println("compareType: t1.is_bool != t2.is_bool");
			return false;
		}
		if(t1.is_int() != t2.is_int()) {
			System.err.println("compareType: t1.is_int() != t2.is_int()");
			return false;
		}
		if(t1.is_small_int() != t2.is_small_int()) {
			System.err.println("compareType: t1.is_small_int() != t2.is_small_int()");
			return false;
		}
		if(t1.arity() != t2.arity()) {
			System.err.println("compareType: t1.arity() != t2.arity()");
			return false;
		}
		if(t1.size() != t2.size()) {
			System.err.println("compareType: t1.size() != t2.size()");
			return false;
		}
		if(t1.hasNoTuple() != t2.hasNoTuple()) {
			System.err.println("compareType: t1.hasNoTuple() != t2.hasNoTuple()");
			return false;
		}
		if(t1.hasTuple() != t2.hasTuple()) {
			System.err.println("compareType: t1.hasTuple() != t2.hasTuple()");
			return false;
		}
		
		return true;
	}
}


