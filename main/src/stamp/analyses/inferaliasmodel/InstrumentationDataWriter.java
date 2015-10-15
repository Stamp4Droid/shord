package stamp.analyses.inferaliasmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import shord.program.Program;
import shord.project.analyses.JavaAnalysis;
import soot.Local;
import soot.RefLikeType;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AnyNewExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import stamp.analyses.inferaliasmodel.MatchAllocToInitAnalysis2.MultivalueMap;
import chord.project.Chord;

@Chord(name = "instrument")
public class InstrumentationDataWriter extends JavaAnalysis {
	private static class EventWriter {
		private final PrintWriter writer;
		private int eventId = 0;
		private EventWriter(PrintWriter writer) {
			this.writer = writer;
		}
		private void writeMethodCallRet(String methodSignature, int bytecodeOffset) {
			this.writer.println("METHCALLARG " + methodSignature + " " + bytecodeOffset + " " + -1 + " " + this.eventId++);
		}
		private void writeNewInstance(String methodSignature, int bytecodeOffset) {
			this.writer.println("METHCALLARG " + methodSignature + " " + bytecodeOffset + " " + 0 + " " + this.eventId++);
		}
		private void writeMethodParam(String methodSignature, int argIndex) {
			this.writer.println("METHPARAM " + methodSignature + " " + argIndex + " " + this.eventId++);
		}
		private void close() {
			this.writer.close();
		}
	}
	
	public static void write() {
		try {
			writeHelper();
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to write instrumentation!");
		}
	}
	
	private static void writeHelper() throws IOException {
		File outDir = new File(System.getProperty("stamp.out.dir"), "inferaliasmodel");
		outDir.mkdirs();
		EventWriter eventWriter = new EventWriter(new PrintWriter(new BufferedWriter(new FileWriter(new File(outDir, "instrinfo.txt")))));
		for(SootClass klass : Program.g().getClasses()) {
			for(SootMethod method : klass.getMethods()) {
				if(method.isConcrete()) {
					processMethod(method, eventWriter);
				}
			}
		}
		eventWriter.close();
	}
	
	private static void processMethod(SootMethod method, EventWriter writer) {
		String methodSignature = bytecodeSignature(method);
		MultivalueMap<Stmt,Stmt> matchAllocToInit = MatchAllocToInitAnalysis2.getMatchAllocToInit(method.retrieveActiveBody());
		
		// Handle params
		if(method.isStatic()) {
			writer.writeMethodParam(methodSignature, 0);
		}
		int offset = method.isStatic() ? 0 : 1;
		for(int i=0; i<method.getParameterCount(); i++) {
			if(method.getParameterType(i) instanceof RefLikeType) {
				writer.writeMethodParam(methodSignature, i + offset);
			}
		}
		
		// Handle units
		for(Unit unit : method.retrieveActiveBody().getUnits()) {
			Stmt stmt = (Stmt)unit;
			
			// Handle allocation statement
			if(stmt instanceof DefinitionStmt) {
				Value leftOp = ((DefinitionStmt)stmt).getLeftOp();
				Value rightOp = ((DefinitionStmt)stmt).getRightOp();
				if(leftOp instanceof Local && rightOp instanceof NewExpr) {
					for(Stmt initInvkStmt : matchAllocToInit.get(stmt)) {
						int bytecodeOffset = bytecodeOffset(initInvkStmt);
						if(bytecodeOffset >= 0) {
							writer.writeNewInstance(methodSignature, bytecodeOffset);
						} else {
							System.out.println("bytecode offset unavailable: " + method.getSignature());
						}
					}
				} else if(leftOp instanceof Local && rightOp instanceof AnyNewExpr) {
					// TODO: handle array allocation sites
				}
			}
			
			// Handle invocation statement
			if(stmt.containsInvokeExpr() && (stmt instanceof DefinitionStmt)) {
				int bytecodeOffset = bytecodeOffset(stmt);
				if(bytecodeOffset >= 0) {
					writer.writeMethodCallRet(methodSignature, bytecodeOffset);
				} else {
					System.out.println("bytecode offset unavailable: "+method.getSignature());
				}
			}
		}
	}
	
	
	@Override
	public void run() {
		write();
	}
	
	private static int bytecodeOffset(Stmt stmt) {
		for(Tag tag : stmt.getTags()) {
			if(tag instanceof BytecodeOffsetTag) {
				return ((BytecodeOffsetTag)tag).getBytecodeOffset();
			}
		}
		return -1;
	}

	private static String bytecodeSignature(SootMethod method) {
		String sig = method.getBytecodeSignature();
		return sig.substring(sig.indexOf(' ')+1, sig.length()-1) + "@L" + method.getDeclaringClass().getName().replace('.', '/') + ";";
	}
}
