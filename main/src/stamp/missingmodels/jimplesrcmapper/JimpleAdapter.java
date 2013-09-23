package stamp.missingmodels.jimplesrcmapper;

import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;

public class JimpleAdapter {
	/** Visits the declaration of a field in a class */
	public void visit(SootField f, int start, int len) {}
	
	/** Visits the method declaration */
	public void visit(SootMethod m, int start, int len) {}
	
	/** Visits the declaration of a local variable in a method */
	public void visit(Local local, int start, int len) {}
	
	/** Visits a statement in the unit graph of a method */
	public void visit(Unit stmt, int start, int len) {}
	
	/*
	public void finish() {}
	
	public boolean visit(TypeDeclaration node) {}
	public void endVisit(TypeDeclaration node) {}
	
	public boolean visit(EnumConstantDeclaration node) {}
	public void endVisit(EnumConstantDeclaration node) {}
	
	public boolean visit(EnumDeclaration node) {}
	public void endVisit(EnumDeclaration node) {}
	
	public boolean visit(AnonymousClassDeclaration node) {}
	public void endVisit(AnonymousClassDeclaration node) {}
	
	public boolean visit(MethodDeclaration node) {}
	public void endVisit(MethodDeclaration node) {} 

	public boolean visit(Initializer node) {}
	public void endVisit(Initializer node) {}
	
	public boolean visit(ClassInstanceCreation cic) {}
	public boolean visit(MethodInvocation mi) {}

	public boolean visit(SuperMethodInvocation mi) {}
	public void endVisit(SuperMethodInvocation mi) {}
	
	public boolean visit(FieldAccess fa) {}
	public void endVisit(FieldAccess fa) {}
	
	public boolean visit(ExpressionStatement stmt) {}
	public void endVisit(ExpressionStatement stmt) {}
	
	public boolean visit(VariableDeclarationStatement stmt) {}
	public void endVisit(VariableDeclarationStatement stmt) {}
	
	public boolean visit(Assignment as) {}
	public void endVisit(Assignment as) {}
	
	public boolean visit(ReturnStatement rs) {}
	public void endVisit(ReturnStatement rs) {}
	
	public boolean visit(SwitchStatement ss) {}
	public void endVisit(SwitchStatement ss) {}

	public boolean visit(SimpleType st) {}
	public void endVisit(SimpleType st) {}
	*/
}
