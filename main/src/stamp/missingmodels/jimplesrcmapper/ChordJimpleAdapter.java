package stamp.missingmodels.jimplesrcmapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import stamp.missingmodels.jimplesrcmapper.CodeStructureInfo.CodeStructure;
import stamp.missingmodels.jimplesrcmapper.CodeStructureInfo.SimpleCodeStructure;
import stamp.srcmap.sourceinfo.javasource.JavaSourceInfo;

public class ChordJimpleAdapter extends JimpleVisitorWithStructure {
	private final PrintWriter writer;
	private JavaSourceInfo sourceInfo;
	
	public ChordJimpleAdapter(JavaSourceInfo sourceInfo, File outputFile) throws IOException {
		this.sourceInfo = sourceInfo;
		this.writer = new PrintWriter(new FileOutputStream(outputFile));
	}
	
	/** Visits a type declaration */
	// See ChordAdapter.visit(TypeDeclaration)
	@Override
	public void visit(SootClass cl, CodeStructure classInfo) {
		String chordSig = this.sourceInfo.chordTypeFor(cl.getType());
		int lineNum = classInfo.declarationLineNum;
		// update class info
	}
		
	/** Visits the method declaration */
	@Override
	public void visit(SootMethod m, CodeStructure methodInfo) {
		int startPos = methodInfo.declarationStart;
		int endPos = methodInfo.declarationEnd;
		String chordSig = this.sourceInfo.chordSigFor(m);
		// update method info
	}
	
	/** Starts a method local variable declaration visit */
	public void visit(Local local, SimpleCodeStructure localStructure) {}

	/** Starts a unit graph statement visit */
	public void visit(Unit stmt, SimpleCodeStructure unitStructure) {
		if(stmt instanceof Stmt) {
			Stmt s = (Stmt)stmt;
			if(s.containsInvokeExpr()){
			} else if(s.containsFieldRef()){
			} else if(s.containsArrayRef()) {
			} else if(s instanceof AssignStmt) {
			} else if(s instanceof ReturnStmt){
			} else if(s instanceof IdentityStmt){
			} else if(s instanceof TableSwitchStmt){
			} else if(s instanceof LookupSwitchStmt){
			}
		}
	}
}
