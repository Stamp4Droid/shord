package stamp.missingmodels.jimplesrcmapper;

import static org.apache.commons.lang3.StringEscapeUtils.escapeXml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import stamp.missingmodels.jimplesrcmapper.CodeStructureInfo.CodeStructure;
import stamp.missingmodels.jimplesrcmapper.CodeStructureInfo.SimpleCodeStructure;
import stamp.srcmap.Expr;
import stamp.srcmap.InvkMarker;
import stamp.srcmap.SimpleMarker;
import stamp.srcmap.sourceinfo.SourceInfo;

public class ChordJimpleAdapter extends JimpleVisitorWithStructure {
	private final PrintWriter writer;
	private String topLevelClassSig;
	
	public ChordJimpleAdapter(File outputFile) throws IOException {
		this.writer = new PrintWriter(new FileOutputStream(outputFile));
	}

	/** Visited before writing the class */
	@Override
	public void start(SimpleCodeStructure fileInfo) {
		this.writer.println("<root>");
	}
	
	/** Visited after writing the class */
	@Override
	public void end() {
		this.writer.println("</root>");
		this.writer.close();		
	}
	
	/** Visits a type declaration */
	// See ChordAdapter.visit(TypeDeclaration)
	@Override
	public void visit(SootClass cl, CodeStructure classInfo) {
		String chordSig = ""; // TODO URGENT
		int lineNum = classInfo.declarationLineNum;
		this.writer.println("<class chordsig=\"" + escapeXml(chordSig) + "\" line=\"" + lineNum + "\">");
		if(!cl.hasOuterClass()) {
			this.topLevelClassSig = chordSig;
		}
	}
	
	/** Ends the visit to a type declaration */
	// See ChordAdapter.endVisit(TypeDeclaration)
	@Override
	public void endVisit(SootClass cl) {
		if(!cl.hasOuterClass()) {
			this.topLevelClassSig = null;
		}
		this.writer.println("</class>");
	}
	
	/** Visits the method declaration */
	@Override
	public void visit(SootMethod m, CodeStructure methodInfo) {
		int startPos = methodInfo.declarationStart;
		int endPos = methodInfo.declarationEnd;
		String chordSig = ""; // TODO URGENT
		String className = chordSig.substring(chordSig.indexOf('@')+1);
		String sig = ""; // TODO URGENT
		
		if(m.hasActiveBody() && (methodInfo.bodyStart != -1 || methodInfo.bodyEnd != -1)) {
			throw new RuntimeException("Method " + m.toString() + " has active body but no information!");
		}
        
        this.writer.println("<method chordsig=\"" + escapeXml(chordSig) + "\" sig=\"" + escapeXml(sig) + "\" startpos=\"" + startPos + "\" endpos=\"" + endPos + "\">");
		Set<String> aliasDescs = new HashSet<String>();
		String methDesc = chordSig.substring(chordSig.indexOf(':')+1, chordSig.indexOf('@'));
		
		List<SootMethod> overriddenMethods = new ArrayList<SootMethod>(); // TODO URGENT
		for(SootMethod m2 : overriddenMethods) {
			String aliasSig = ""; // TODO URGENT chordSigFor(m2)
			aliasSig = aliasSig.substring(aliasSig.indexOf(':')+1, aliasSig.indexOf('@'));
			if(!aliasSig.equals(methDesc)) {
				aliasDescs.add(aliasSig);
			}
		}
		for(String desc : aliasDescs) {
			this.writer.println("\t<alias>" + escapeXml(desc) + "</alias>");
		}

		// parameter names
		// TODO URGENT do this for abstract methods
		/*
		List<Local> params = new ArrayList<Local>();
		for(int i=0; i<m.getParameterCount(); i++) {
			params.add(m.getActiveBody().getParameterLocal(i));
		}
		for(int i=0; i<params.size(); i++){
			// TODO URGENT implement writeSimpleMarker
			//SingleVariableDeclaration p = (SingleVariableDeclaration)params.get(i);
			//writeSimpleMarker("param", null, -1, p.getName());
		}
		*/
	}
	
	/** Ends the visit to a method declaration */
	@Override
	public void endVisit(SootMethod m) {
		this.writer.println("</method>");		
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
