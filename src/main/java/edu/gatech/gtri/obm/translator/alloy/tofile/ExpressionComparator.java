package edu.gatech.gtri.obm.translator.alloy.tofile;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
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
	}
	
	public boolean compareTwoExpressions(Expr e1, Expr e2) {
		visitedExpressions.clear();
		boolean same = compareExpr(e1, e2);
		visitedExpressions.clear();
		return same;
		
	}
	
	private boolean compareDecl(Decl d1, Decl d2) {
		
		System.out.println("Decl d1: " + d1);
		System.out.println("Decl d2: " + d2);
		
		if(d1 == null && d2 == null) {
			return true;
		}
		
		if(d1 == null || d2 == null) {
			return false;
		}
		
		if(d1.disjoint == null && d2.disjoint != null) {
			System.err.println("compareDecls: d1.disjoint null, d2.disjoint not null");
			return false;
		}
		if(d1.disjoint != null && d2.disjoint == null) {
			System.err.println("compareDecls: d1.disjoint not null, d2.disjoint null");
			return false;
		}
		
		if(d1.disjoint2 == null && d2.disjoint2 != null) {
			System.err.println("compareDecls: d1.disjoint2 null, d2.disjoint2 not null");
			return false;
		}
		if(d1.disjoint2 != null && d2.disjoint2 == null) {
			System.err.println("compareDecls: d1.disjoint2 not null, d2.disjoint2 null");
			return false;
		}
		
		if(!compareExpr(d1.expr, d2.expr)) {
			System.err.println("compareDecl: !compareExpr(d1.expr, d2.expr)");
			return false;
		}
		
		if(d1.isPrivate == null && d2.isPrivate != null) {
			System.err.println("compareDecls: d1.isPrivate is null, d2.isPrivate not null");
			return false;
		}
		if(d1.isPrivate != null && d2.isPrivate == null) {
			System.err.println("compareDecls: d1.isPrivate not null, d2isPrivate is null");
			return false;
		}
				
		if(d1.names.size() != d2.names.size()) {
			System.err.println("compareDecls: names has different size.");
			return false;
		}
		
		for(int i = 0; i < d1.names.size(); i++) {
			if(!compareExpr(d1.names.get(i), d2.names.get(i))) {
				System.err.println("compareDecls: name " + i + " are different");
				return false;
			}
		}
		
		return true;
	}
	
	private boolean compareExpr(Expr fileExpr, Expr apiExpr) {
		
		if(fileExpr == null && apiExpr == null) {
			return true;
		}
		
		if(fileExpr == null || apiExpr == null) {
			return false;
		}
		
		fileExpr = fileExpr.deNOP();
		apiExpr = apiExpr.deNOP();
		
		if(visitedExpressions.contains(fileExpr) && visitedExpressions.contains(apiExpr)) {
			return true;
		}
		
		visitedExpressions.add(fileExpr);
		visitedExpressions.add(apiExpr);
		
		if(!fileExpr.getClass().equals(apiExpr.getClass())) {
			System.err.println("compareExpr: !fileExpr.getClass().equals(apiExpr.getClass())");
			System.err.println("fileExpr.getClass(): " + fileExpr.getClass());
			System.err.println("apiExpr.getClass(): " + apiExpr.getClass());
			return false;
		}
		
		if(fileExpr.getClass().equals(Expr.class)) {
			System.err.println("Expr: not implemented");
		}
		else if(fileExpr.getClass().equals(ExprBinary.class)) {
			if(!compareExprBinary((ExprBinary) fileExpr, (ExprBinary) apiExpr)) {
				return false;
			}
		}
		else if(fileExpr.getClass().equals(ExprCall.class)) {
			if(!compareExprCall((ExprCall) fileExpr, (ExprCall) apiExpr)) {
				return false;
			}
		}
		else if(fileExpr.getClass().equals(ExprConstant.class)) {
			if(!compareExprConstant((ExprConstant) fileExpr, (ExprConstant) apiExpr)) {
				return false;
			}
		}
		else if(fileExpr.getClass().equals(ExprITE.class)) {
			System.err.println("ExprITE: not implemented");
		}
		else if(fileExpr.getClass().equals(ExprLet.class)) {
			System.err.println("ExprLet: not implemented");
		}
		else if(fileExpr.getClass().equals(ExprList.class)) {
			if(!compareExprList((ExprList) fileExpr, (ExprList) apiExpr)) {
				return false;
			}
		}
		else if(fileExpr.getClass().equals(ExprQt.class)) {
			if(!compareExprQt((ExprQt) fileExpr, (ExprQt) apiExpr)) {
				return false;
			}
		}
		else if(fileExpr.getClass().equals(ExprUnary.class)) {
			if(!compareExprUnary((ExprUnary) fileExpr, (ExprUnary) apiExpr)) {
				return false;
			}
		}
		else if(fileExpr.getClass().equals(ExprVar.class)) {
			if(!compareExprVar((ExprVar) fileExpr, (ExprVar) apiExpr)) {
				return false;
			}
		}
		else if(fileExpr.getClass().equals(Sig.Field.class)) {
			if(!compareSigField((Sig.Field) fileExpr, (Sig.Field) apiExpr)) {
				return false;
			}
		}
		else if(fileExpr.getClass().equals(Sig.class) || fileExpr.getClass().equals(Sig.PrimSig.class)) {
			if(!compareSig((Sig) fileExpr, (Sig) apiExpr)) {
				return false;
			}
		}
		else {
			System.err.println("Unexpected class: " + fileExpr.getClass());
			return false;
		}
		
		return true;
	}
	
	private boolean compareExprBinary(ExprBinary fileExpr, ExprBinary apiExpr) {
		
		System.out.println("fileExpr: " + fileExpr);
		System.out.println("apiExpr: " + apiExpr);
		
		if(fileExpr == null && apiExpr == null) {
			return true;
		}
		
		if(fileExpr == null || apiExpr == null) {
			return false;
		}
				
		
		if(!compareExpr(fileExpr.left, apiExpr.left)) {
			System.err.println("ExprBinary: !compareExpr(fileExpr.left, apiExpr.left)");
			System.err.println("fileExpr.left: " + fileExpr.left);
			System.err.println("apiExpr.left: " + apiExpr.left);
			return false;
		}
		
		if(fileExpr.op != apiExpr.op) {
			System.err.println("ExprBinary: fileExpr.op != apiExpr.op");
			System.err.println("fileExpr.op: " + fileExpr.op);
			System.err.println("apiExpr.op: " + apiExpr.op);
			return false;
		}
		
		if(!compareExpr(fileExpr.right, apiExpr.right)) {
			System.err.println("ExprBinary: !compareExpr(fileExpr.right, apiExpr.right)");
			System.err.println("fileExpr.right: " + fileExpr.right);
			System.err.println("apiExpr.right: " + apiExpr.right);
			return false;
		}
		
		return true;
	}
	
	private boolean compareExprCall(ExprCall fileExpr, ExprCall apiExpr) {
		
		System.out.println("ExprCall1: " + fileExpr);
		System.out.println("ExprCall2: " + apiExpr);
		
		if(fileExpr == null && apiExpr == null) {
			return true;
		}
		
		if(fileExpr == null || apiExpr == null) {
			return false;
		}
		
		if(fileExpr.args.size() != apiExpr.args.size()) {
			System.err.println("ExprCall: fileExpr.args.size() != apiExpr.args.size()");
			return false;
		}
		
		Iterator<Expr> iter1 = fileExpr.args.iterator();
		Iterator<Expr> iter2 = apiExpr.args.iterator();
		
		while(iter1.hasNext() && iter2.hasNext()) {
			
			Expr next1 = iter1.next();
			Expr next2 = iter2.next();
						
			if(!compareExpr(next1, next2)) {
				System.err.println("ExprCall: different args");
				return false;
			}
		}
		
		if(fileExpr.weight != apiExpr.weight) {
			System.err.println("ExprCall: different weight");
			return false;
		}
		
		if(!compareFunctions(fileExpr.fun, apiExpr.fun)) {
			System.err.println("ExprCall: different functions");
			return false;
		}
		
		
		return true;
	}
	
	private boolean compareExprConstant(ExprConstant fileExpr, ExprConstant apiExpr) {
		
		System.out.println("ExprConstant1: " + fileExpr);
		System.out.println("ExprConstant2: " + apiExpr);
		
		if(fileExpr == null && apiExpr == null) {
			return true;
		}
		
		if(fileExpr == null || apiExpr == null) {
			return false;
		}
		
		if(fileExpr.op != apiExpr.op) {
			System.err.println("ExprConstant: fileExpr.op != apiExpr.op");
			return false;
		}
		
		if(fileExpr.num != apiExpr.num) {
			System.err.println("ExprConstant: fileExpr.num != apiExpr.num");
			return false;
		}
		
		if(!fileExpr.string.equals(apiExpr.string)) {
			System.err.println("ExprConstant: fileExpr.string != apiExpr.string");
			return false;
		}
		
		return true;
	}
	
	private boolean compareExprList(ExprList fileExpr, ExprList apiExpr) {
		
		System.out.println("ExprList1: " + fileExpr);
		System.out.println("ExprList2: " + apiExpr);
		
		if(fileExpr == null && apiExpr == null) {
			return true;
		}
		if(fileExpr == null || apiExpr == null) {
			return false;
		}
		
		if(fileExpr.op != apiExpr.op) {
			System.err.println("ExprList: fileExpr.op != apiExpr.op");
			return false;
		}
		
		if(fileExpr.args.size() != apiExpr.args.size()) {
			System.err.println("ExprList: fileExpr.args.size() != apiExpr.args.size()");
			return false;
		}
		
		for(int i = 0; i < fileExpr.args.size(); i++) {
			if(!compareExpr(fileExpr.args.get(i), apiExpr.args.get(i))) {
				System.err.println("ExprList: fileExpr.args != apiExpr.args");
				System.err.println("fileExpr.args: " + fileExpr.args);
				System.err.println("apiExpr.args: " + apiExpr.args);
				System.out.println();
				return false;
			}
		}
		
		return true;
	}
	
	private boolean compareExprQt(ExprQt fileExpr, ExprQt apiExpr) {
		
		System.out.println("ExprQt1: " + fileExpr);
		System.out.println("ExprQt2: " + apiExpr);
		
		if(fileExpr == null && apiExpr == null) {
			return true;
		}
		
		if(fileExpr == null || apiExpr == null) {
			return false;
		}
		
		if(fileExpr.op != apiExpr.op) {
			System.err.println("ExprtQt: fileExpr.op != apiExpr.op");
			return false;
		}
		
		if(fileExpr.decls.size() != apiExpr.decls.size()) {
			System.err.println("fileExpr.decls.size() != apiExpr.decls.size()");
			return false;
		}
		
		Iterator<Decl> iterator1 = fileExpr.decls.iterator();
		Iterator<Decl> iterator2 = apiExpr.decls.iterator();
		
		while(iterator1.hasNext() && iterator2.hasNext()) {
			if(!compareDecl(iterator1.next(), iterator2.next())) {
				System.err.println("ExprQt: different decls.");
				return false;
			}
		}
		

		if(!compareExpr(fileExpr.sub, apiExpr.sub)) {
			System.err.println("ExprQt: different sub");
			System.err.println("fileExpr.sub: " + fileExpr.sub);
			System.err.println("apiExpr.sub: " + apiExpr.sub);
			System.out.println();
			return false;
		}
				
		return true;
	}
	
	private boolean compareExprUnary(ExprUnary fileExpr, ExprUnary apiExpr) {
		
		System.out.println("ExprUnary1: " + fileExpr);
		System.out.println("ExprUnary2: " + apiExpr);
		
		if(fileExpr == null && apiExpr == null) {
			return true;
		}
		
		if(fileExpr == null || apiExpr == null) {
			return false;
		}
		
		if(fileExpr.op != apiExpr.op) {
			System.err.println("compareExprUnary: fileExpr.op != apiExpr.op");
			return false;
		}
		if(!compareExpr(fileExpr.sub, apiExpr.sub)) {
			System.err.println("compareExprUnary: !compareExpr(fileExpr.sub, apiExpr.sub)");
			System.err.println("fileExpr.sub: " + fileExpr.sub);
			System.err.println("apiExpr.sub: " + apiExpr.sub);
			return false;
		}
		
		return true;
	}
	
	private boolean compareExprVar(ExprVar fileExpr, ExprVar apiExpr) {
		
		System.out.println("ExprVar1: " + fileExpr);
		System.out.println("ExprVar2: " + apiExpr);
		
		if(fileExpr == null && apiExpr == null) {
			return true;
		}
		if(fileExpr == null || apiExpr == null) {
			return false;
		}
		
		if(!fileExpr.label.equals(apiExpr.label)) {
			System.err.println("ExprVar: fileExpr.label != apiExpr.label");
			return false;
		}
		
		if(!compareType(fileExpr.type(), apiExpr.type())) {
			System.err.println("ExprVar: fileExpr.type() != apiExpr.type()");
			return false;
		}
		
		return true;
	}
	
	private boolean compareFunctions(Func fileFunc, Func apiFunc) {
 		
 		System.out.println("Func fileFunc: " + fileFunc);
		System.out.println("Func apiFunc: " + apiFunc);
		
		if(fileFunc == null && apiFunc != null) {
			System.out.println("compareFunction: fileFunc null, apiFunc not null");
			return false;
		}
		
		if(fileFunc != null && apiFunc == null) {
			System.out.println("compareFunction: fileFunc not null, apiFunc null");
			return false;
		}
				
		if(fileFunc.decls.size() != apiFunc.decls.size()) {
			System.err.println("compareFunction: Decls size different.");
			return false;
		}
		
		for(int i = 0; i < fileFunc.decls.size(); i++) {
			if(!compareDecl(fileFunc.decls.get(i), apiFunc.decls.get(i))) {
				System.err.println("compareFunction: Different decls.");
				return false;
			}
		}
		
		if(fileFunc.isPred != apiFunc.isPred) {
			System.err.println("compareFunction: isPred different.");
			return false;
		}		
		if(fileFunc.isPrivate == null && apiFunc.isPrivate != null) {
			System.err.println("compareFunctions: isPrivate: fileFunc null, apiFunc not null");
			return false;
		}
		if(fileFunc.isPrivate != null && apiFunc.isPrivate == null) {
			System.err.println("compareFunctions isPrivate: fileFunc not null, apiFunc null");
			return false;
		}		
		if(!fileFunc.label.equals(apiFunc.label)) {
			System.out.println("compareFunctions: Different labels.");
			return false;
		}		
		if(!compareExpr(fileFunc.returnDecl, fileFunc.returnDecl)) {
			System.out.println("compareFunctions: Different returnDecl.");
			return false;
		}
		
		return true;
	}
	
	private boolean compareSig(Sig fileSig, Sig apiSig) {
		
		if(fileSig == null && apiSig == null) {
			return true;
		}
		if(fileSig == null || apiSig == null) {
			System.err.println("compareSig: fileSig == null || apiSig == null");
			return false;
		}
		
		System.out.println("fileSig: " + fileSig);
		System.out.println("apiSig: " + apiSig);
		
		// ConstList<Attr> comparison not implemented.

		if(fileSig.builtin != apiSig.builtin) {
			System.err.println("compareSig: fileSig.builtin != apiSig.builtin");
			return false;
		}		
		if(!compareDecl(fileSig.decl, apiSig.decl)) {
			System.err.println("compareSig: !compareDecl(fileSig.decl, apiSig.decl)");
			return false;
		}
		
		if(fileSig.isAbstract == null && apiSig.isAbstract != null) {
			System.err.println("compareSig: fileSig.isAbstract == null && apiSig.isAbstract != null");
			return false;
		}
		if(fileSig.isAbstract != null && apiSig.isAbstract == null) {
			System.err.println("compareSig: fileSig.isAbstract != null && apiSig.isAbstract == null");
			return false;
		}
		if(fileSig.isEnum == null && apiSig.isEnum != null) {
			System.err.println("compareSig: fileSig.isEnum == null && apiSig.isEnum != null");
			return false;
		}
		if(fileSig.isEnum != null && apiSig.isEnum == null) {
			System.err.println("compareSig: fileSig.isEnum != null && apiSig.isEnum == null");
			return false;
		}
		if(fileSig.isLone == null && apiSig.isLone != null) {
			System.err.println("compareSig: fileSig.isLone == null && apiSig.isLone != null");
			return false;
		}
		if(fileSig.isLone != null && apiSig.isLone == null) {
			System.err.println("compareSig: fileSig.isLone != null && apiSig.isLone == null");
			return false;
		}
		if(fileSig.isMeta == null && apiSig.isMeta != null) {
			System.err.println("compareSig: fileSig.isMeta == null && apiSig.isMeta != null");
			return false;
		}
		if(fileSig.isMeta != null && apiSig.isMeta == null) {
			System.err.println("compareSig: fileSig.isMeta != null && apiSig.isMeta == null");
			return false;
		}
		if(fileSig.isOne == null && apiSig.isOne != null) {
			System.err.println("compareSig: fileSig.isOne == null && apiSig.isOne != null");
			return false;
		}
		if(fileSig.isOne != null && apiSig.isOne == null) {
			System.err.println("compareSig: fileSig.isOne != null && apiSig.isOne == null");
			return false;
		}
		if(fileSig.isPrivate == null && apiSig.isPrivate != null) {
			System.err.println("compareSig: fileSig.isPrivate == null && apiSig.isPrivate != null");
			return false;
		}
		if(fileSig.isPrivate != null && apiSig.isPrivate == null) {
			System.err.println("compareSig: fileSig.isPrivate != null && apiSig.isPrivate == null");
			return false;
		}
		if(fileSig.isSome == null && apiSig.isSome != null) {
			System.err.println("compareSig: fileSig.isSome == null && apiSig.isSome != null");
			return false;
		}
		if(fileSig.isSome != null && apiSig.isSome == null) {
			System.err.println("compareSig: fileSig.isSome != null && apiSig.isSome == null");
			return false;
		}
		if(fileSig.isSubset == null && apiSig.isSubset != null) {
			System.err.println("compareSig: fileSig.isSubset == null && apiSig.isSubset != null");
			return false;
		}
		if(fileSig.isSubset != null && apiSig.isSubset == null) {
			System.err.println("compareSig: fileSig.isSubset != null && apiSig.isSubset == null");
			return false;
		}
		if(fileSig.isSubsig == null && apiSig.isSubsig != null) {
			System.err.println("compareSig: fileSig.isSubsig == null && apiSig.isSubsig != null");
			return false;
		}
		if(fileSig.isSubsig != null && apiSig.isSubsig == null) {
			System.err.println("compareSig: fileSig.isSubsig != null && apiSig.isSubsig == null");
			return false;
		}
		if(!MyAlloyLibrary.removeSlash(fileSig.label).equals(MyAlloyLibrary.removeSlash(apiSig.label))) {
			System.err.println("compareSig: !fileSig.label.equals(apiSig.label)");
			System.err.println("fileSig.label: " + MyAlloyLibrary.removeSlash(fileSig.label));
			System.err.println("apiSig.label: " + MyAlloyLibrary.removeSlash(apiSig.label));
			return false;
		}	
		if(fileSig.getDepth() != apiSig.getDepth()) {
			System.err.println("compareSig: fileSig.getDepth() != apiSig.getDepth()");
			return false;
		}
				
		if(fileSig.getFacts().size() != apiSig.getFacts().size()) {
			System.err.println("compareSig: fileSig.getFacts().size() != apiSig.getFacts().size()");
			System.err.println("fileSig.getFacts().size(): " + fileSig.getFacts().size());
			System.err.println("apiSig.getFacts().size(): " + apiSig.getFacts().size());
			return false;
		}
		
		Iterator<Expr> fileSigFacts = fileSig.getFacts().iterator();
		Iterator<Expr> apiSigFacts = apiSig.getFacts().iterator();
		
		while(fileSigFacts.hasNext() && apiSigFacts.hasNext()) {
			
			Expr next1 = fileSigFacts.next();
			Expr next2 = apiSigFacts.next();
			
			if(!compareExpr(next1, next2)) {
				System.err.println("compareSig: !compareExpr(fileSigFacts.next(), apiSigFacts.next())");
				return false;
			}
		}
		
		if(fileSig.getFieldDecls().size() != apiSig.getFieldDecls().size()) {
			System.err.println("compareSig: fileSig.getFieldDecls().size() != apiSig.getFieldDecls().size()");
			return false;
		}
		
		Iterator<Decl> fileSigFieldDecls = fileSig.getFieldDecls().iterator();
		Iterator<Decl> apiSigFieldDecls = apiSig.getFieldDecls().iterator();
		
		while(fileSigFieldDecls.hasNext() && apiSigFieldDecls.hasNext()) {
			if(!compareDecl(fileSigFieldDecls.next(), apiSigFieldDecls.next())) {
				System.err.println("compareSig: !compareDecl(fileSigFieldDecls.next(), apiSigFieldDecls.next())");
				return false;
			}
		}
				
		if(fileSig.getFields().size() != apiSig.getFields().size()) {
			System.err.println("compareSig: fileSig.getFields().size() != apiSig.getFields().size()");
			return false;
		}
		
		Iterator<Sig.Field> fileSigFields = fileSig.getFields().iterator();
		Iterator<Sig.Field> apiSigFields = apiSig.getFields().iterator(); 
		
		while(fileSigFields.hasNext() && apiSigFields.hasNext()) {
			if(!compareSigField(fileSigFields.next(), apiSigFields.next())) {
				System.err.println("compareSig: !compareSigField(fileSigFields.next(), apiSigFields.next())");
				return false;
			}
		}
		
		
		// getHTML() not implemented
		// getSubnodes() not implemented
		
		if(fileSig.isTopLevel() != apiSig.isTopLevel()) {
			System.err.println("compareSig: fileSig.isTopLevel() != apiSig.isTopLevel()");
			return false;
		}
		if(!MyAlloyLibrary.removeSlash(fileSig.toString()).equals(MyAlloyLibrary.removeSlash(apiSig.toString()))) {
			System.err.println("compareSig: !fileSig.toString().equals(apiSig.toString())");
			return false;
		}
		
		return true;
	}

	private boolean compareSigField(Sig.Field fileSigField, Sig.Field apiSigField) {
	
		System.out.println("Sig.Field1: " + fileSigField);
		System.out.println("Sig.Field2: " + apiSigField);
		
		if(fileSigField == null && apiSigField == null) {
			return true;
		}
		
		if(fileSigField == null || apiSigField == null) {
			return false;
		}
		
		if(fileSigField.defined != apiSigField.defined) {
			System.err.println("Sig.Field: fileSig.defined != apiSig.defined");
			return false;
		}
		
		if((fileSigField.isMeta == null && apiSigField.isMeta != null) || fileSigField.isMeta != null && apiSigField.isMeta == null) {
			System.err.println("Sig.Field: isMeta different");
			return false;
		}
		
		if((fileSigField.isPrivate == null && apiSigField.isPrivate != null) || fileSigField.isPrivate != null && apiSigField.isPrivate == null) {
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


