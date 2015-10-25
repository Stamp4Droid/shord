package stamp.analyses.inferaliasmodel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
import soot.jimple.IdentityStmt;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import stamp.analyses.inferaliasmodel.MatchAllocToInitAnalysis2.MultivalueMap;
import chord.project.Chord;

@Chord(name = "instrument")
public class InstrumentationDataWriter extends JavaAnalysis {
	public static interface Monitor {
		public abstract String getRecord(int eventId);
	}
	
	public static class CallRetMonitor implements Monitor {
		private final String methodSignature;
		private final int bytecodeOffset;
		public CallRetMonitor(String methodSignature, int bytecodeOffset) {
			this.methodSignature = methodSignature;
			this.bytecodeOffset = bytecodeOffset;
		}
		@Override
		public String getRecord(int eventId) {
			return "METHCALLARG " + this.methodSignature + " " + this.bytecodeOffset + " " + -1 + " " + eventId;
		}
	}
	
	public static class NewInstanceMonitor implements Monitor {
		private final String methodSignature;
		private final int bytecodeOffset;
		public NewInstanceMonitor(String methodSignature, int bytecodeOffset) {
			this.methodSignature = methodSignature;
			this.bytecodeOffset = bytecodeOffset;
		}
		@Override
		public String getRecord(int eventId) {
			return "METHCALLARG " + this.methodSignature + " " + this.bytecodeOffset + " " + 0 + " " + eventId;
		}
	}
	
	public static class DefinitionMonitor implements Monitor {
		private final String methodSignature;
		private final int bytecodeOffset;
		public DefinitionMonitor(String methodSignature, int bytecodeOffset) {
			this.methodSignature = methodSignature;
			this.bytecodeOffset = bytecodeOffset;
		}
		@Override
		public String getRecord(int eventId) {
			return "DEFINITION " + this.methodSignature + " " + this.bytecodeOffset + " " + eventId;
		}
	}
	
	public static class MethodParamMonitor implements Monitor {
		private final String methodSignature;
		private final int argIndex;
		public MethodParamMonitor(String methodSignature, int argIndex) {
			this.methodSignature = methodSignature;
			this.argIndex = argIndex;
		}
		@Override
		public String getRecord(int eventId) {
			return "METHPARAM " + this.methodSignature + " " + this.argIndex + " " + eventId;
		}
	}
	
	public static class MonitorWriter {
		private final PrintWriter writer;
		private int eventId = 0;
		public MonitorWriter(PrintWriter writer) {
			this.writer = writer;
		}
		public void write(Monitor monitor) {
			this.writer.println(monitor.getRecord(this.eventId++));
		}
		public void writeAll(Iterable<Monitor> monitors) {
			for(Monitor monitor : monitors) {
				this.write(monitor);
			}
		}
		public void close() {
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
		MonitorWriter writer = new MonitorWriter(new PrintWriter(new File(outDir, "instrinfo.txt")));
		for(SootClass klass : Program.g().getClasses()) {
			for(SootMethod method : klass.getMethods()) {
				if(method.isConcrete()) {
					processMethod(method, writer);
				}
			}
		}
		writer.close();
	}
	
	private static void processMethod(SootMethod method, MonitorWriter writer) {
		String methodSignature = bytecodeSignature(method);
		MultivalueMap<Stmt,Stmt> matchAllocToInit = MatchAllocToInitAnalysis2.getMatchAllocToInit(method.retrieveActiveBody());
		List<Monitor> monitors = new ArrayList<Monitor>();
		
		// Handle params
		if(method.isStatic()) {
			monitors.add(new MethodParamMonitor(methodSignature, 0));
		}
		int offset = method.isStatic() ? 0 : 1;
		for(int i=0; i<method.getParameterCount(); i++) {
			if(method.getParameterType(i) instanceof RefLikeType) {
				monitors.add(new MethodParamMonitor(methodSignature, i + offset));
			}
		}
		
		// Handle units
		for(Unit unit : method.retrieveActiveBody().getUnits()) {
			Stmt stmt = (Stmt)unit;
			
			// new instances
			if(stmt instanceof DefinitionStmt) {
				Value leftOp = ((DefinitionStmt)stmt).getLeftOp();
				Value rightOp = ((DefinitionStmt)stmt).getRightOp();
				if(leftOp instanceof Local && rightOp instanceof NewExpr) {
					for(Stmt initInvkStmt : matchAllocToInit.get(stmt)) {
						int bytecodeOffset = bytecodeOffset(initInvkStmt);
						if(bytecodeOffset >= 0) {
							monitors.add(new NewInstanceMonitor(methodSignature, bytecodeOffset));
						} else {
							System.out.println("bytecode offset unavailable: " + method.getSignature());
						}
					}
				} else if(leftOp instanceof Local && rightOp instanceof AnyNewExpr) {
					// TODO: handle array allocation sites
				}
			}
			
			// call rets
			if(stmt.containsInvokeExpr() && (stmt instanceof DefinitionStmt)) {
				if(!(((DefinitionStmt)stmt).getLeftOp().getType() instanceof RefLikeType)) {
					continue;
				}
				int bytecodeOffset = bytecodeOffset(stmt);
				if(bytecodeOffset >= 0) {
					monitors.add(new CallRetMonitor(methodSignature, bytecodeOffset));
				} else {
					System.out.println("bytecode offset unavailable: "+method.getSignature());
				}
			}
			
			// definition other than invocation
			if(stmt instanceof DefinitionStmt) {
				DefinitionStmt defStmt = (DefinitionStmt)stmt;
				if(defStmt instanceof IdentityStmt) {
					continue;
				}
				if(defStmt.containsInvokeExpr()) {
					continue;
				}
				if(!(defStmt.getLeftOp() instanceof Local)) {
					continue;
				}
				if(defStmt.getRightOp() instanceof AnyNewExpr) {
					continue;
				}
				if(!(defStmt.getLeftOp().getType() instanceof RefLikeType)) {
					continue;
				}
				
				int bytecodeOffset = bytecodeOffset(stmt);
				if(bytecodeOffset >= 0) {
					monitors.add(new DefinitionMonitor(methodSignature, bytecodeOffset));
				} else {
					System.out.println("bytecode offset unavailable: "+method.getSignature());
				}
			}
		}
		
		writer.writeAll(monitors);
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
