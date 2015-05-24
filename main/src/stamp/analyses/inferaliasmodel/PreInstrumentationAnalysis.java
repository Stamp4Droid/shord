package stamp.analyses.inferaliasmodel;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.RefLikeType;
import soot.jimple.Stmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.Tag;
import soot.tagkit.BytecodeOffsetTag;
import soot.util.ArrayNumberer;
import soot.util.Chain;

import shord.project.analyses.JavaAnalysis;
import shord.program.Program;

import java.io.*;
import java.util.*;

import chord.project.Chord;
/*
 * @author Saswat Anand
 */
@Chord(name = "preinst")
public class PreInstrumentationAnalysis extends JavaAnalysis
{
	private int methIndex;
	private SootMethod method;
	private Chain<Unit> units;
	private CallGraph callGraph;
	private PrintWriter writer;

	public void run()
	{
		try{
			String stampOutDir = System.getProperty("stamp.out.dir");
			
			File outDir = new File(stampOutDir, "inferaliasmodel");
			outDir.mkdirs();
			
			File instrInfoFile = new File(outDir, "instrinfo.txt");
			writer = new PrintWriter(new BufferedWriter(new FileWriter(instrInfoFile)));
			
			Program prog = Program.g();
			prog.runCHA();
			
			File methodsInfoFile = new File(outDir, "methods.txt");
			PrintWriter methodsInfoFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(methodsInfoFile)));
			callGraph = Scene.v().getCallGraph();
			ArrayNumberer<SootMethod> methNumberer = Scene.v().getMethodNumberer();
			for(SootClass klass : prog.getClasses()){
				if(prog.isFrameworkClass(klass))
					continue;
				for(SootMethod method : klass.getMethods()){
					if(!method.isConcrete())
						continue;
					this.method = method;
					this.methIndex = (int) methNumberer.get(method);
					methodsInfoFileWriter.println(methIndex +" "+method.getSignature());
					this.units = method.retrieveActiveBody().getUnits();
					System.out.println("preinst: "+method.getSignature());
					process(methIndex);
				}
			}
			
			methodsInfoFileWriter.close();
			writer.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}
	
	private void process(int methIndex)
	{
		Iterator<Unit> uit = units.snapshotIterator();
		int stmtIndex = 0;
		while(uit.hasNext()){
			Stmt stmt = (Stmt) uit.next();
			processNewStmt(stmt, stmtIndex);
			processInvkStmt(stmt, stmtIndex);
			stmtIndex++;
		}
	}

	private void processNewStmt(Stmt stmt, int stmtIndex)
	{
		return;
	}

	private void processInvkStmt(Stmt stmt, int stmtIndex)
	{
		if(!stmt.containsInvokeExpr())
			return;

		if(!(stmt instanceof DefinitionStmt))
			return;

		if(!(((DefinitionStmt) stmt).getLeftOp().getType() instanceof RefLikeType))
			return;

		Iterator<Edge> edgeIt = callGraph.edgesOutOf(stmt);
		boolean instrument = false;
		while(edgeIt.hasNext()){
			SootMethod target = (SootMethod) edgeIt.next().getTgt();
			SootClass tgtClass = target.getDeclaringClass();
			String tgtClassName = tgtClass.getName();
			if(tgtClassName.startsWith("java.lang."))
				continue;
			if(Program.g().isFrameworkClass(tgtClass)){
				instrument = true;
				break;
			}
		}
		if(!instrument)
			return;
	
		output(stmt, stmtIndex, -1);		
	}
		
	void output(Stmt stmt, int stmtIndex, int argIndex)
	{
		if(methIndex >= 65536 || stmtIndex >= 65536)
			throw new RuntimeException("unexpected "+ "methIndex = "+methIndex+" stmtIndex = "+stmtIndex);
		
		int id = (methIndex << 16) | stmtIndex;

		int bytecodeOffset = -1;
		for(Tag tag : stmt.getTags()){
			if(tag instanceof BytecodeOffsetTag){
				bytecodeOffset = ((BytecodeOffsetTag) tag).getBytecodeOffset();
				break;
			}
		}
		assert bytecodeOffset >= 0;

		String sig = method.getBytecodeSignature();
		sig = sig.substring(sig.indexOf(' ')+1, sig.length()-1)+"@L"+method.getDeclaringClass().getName().replace('.', '/')+";";
		writer.println(sig+" "+bytecodeOffset+" "+argIndex+" "+id);
	}
}