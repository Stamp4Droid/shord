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
import soot.jimple.AssignStmt;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import stamp.analyses.inferaliasmodel.MatchAllocToInitAnalysis2.MultivalueMap;
import stamp.analyses.inferaliasmodel.MonitorMapUtils.MonitorMap;
import chord.project.Chord;

@Chord(name = "instrument")
public class InstrumentationDataWriter extends JavaAnalysis {
	public static interface Monitor {
		public abstract String getRecord(int eventId);
	}
	
	public static class CallRetMonitor implements Monitor {
		private final String methodSignature;
		private final int bytecodeOffset;
		private CallRetMonitor(String methodSignature, int bytecodeOffset) {
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
		private NewInstanceMonitor(String methodSignature, int bytecodeOffset) {
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
		private DefinitionMonitor(String methodSignature, int bytecodeOffset) {
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
		private MethodParamMonitor(String methodSignature, int argIndex) {
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
		List<Monitor> monitors = new ArrayList<Monitor>();
		
		// Handle params
		if(!method.isStatic()) {
			monitors.add(getMonitorForMethodParam(method, 0));
		}
		for(int i=0; i<method.getParameterCount(); i++) {
			if(method.getParameterType(i) instanceof RefLikeType) {
				monitors.add(getMonitorForMethodParam(method, i));
			}
		}
		
		// Handle units
		for(Unit unit : method.retrieveActiveBody().getUnits()) {
			monitors.addAll(getMonitorForDefinition(method, (Stmt)unit));
		}
		
		writer.writeAll(monitors);
	}
	
	
	@Override
	public void run() {
		new MonitorMap();
		write();
	}
	
	public static Monitor getMonitorForMethodThis(SootMethod method) {
		return new MethodParamMonitor(bytecodeSignature(method), 0);
	}
	
	public static Monitor getMonitorForMethodParam(SootMethod method, int paramIndex) {
		return new MethodParamMonitor(bytecodeSignature(method), paramIndex + (method.isStatic() ? 0 : 1));
	}
	
	public static List<Monitor> getMonitorForDefinition(SootMethod method, Stmt stmt) {
		List<Monitor> monitors = new ArrayList<Monitor>();
		
		if(method == null) {
			System.out.println("NULL METHOD: " + method + " ## " + stmt);
			return monitors;
		}
		
		if(stmt == null) {
			System.out.println("NULL STMT: " + method + " ## " + stmt);
			return monitors;
		}
		
		if(!(stmt instanceof AssignStmt)) {
			return monitors;
		}
		
		// setup
		String methodSignature = bytecodeSignature(method);
		MultivalueMap<Stmt,Stmt> matchAllocToInit = MatchAllocToInitAnalysis2.getMatchAllocToInit(method.retrieveActiveBody());
		AssignStmt assignStmt = (AssignStmt)stmt;
		Value leftOp = assignStmt.getLeftOp();
		Value rightOp = assignStmt.getRightOp();
		
		// checks
		if(!(leftOp instanceof Local)) {
			return monitors;
		}
		if(!(leftOp.getType() instanceof RefLikeType)) {
			return monitors;
		}
		
		// new instances
		if(rightOp instanceof NewExpr) {
			for(Stmt initInvkStmt : matchAllocToInit.get(stmt)) {
				int bytecodeOffset = bytecodeOffset(initInvkStmt);
				if(bytecodeOffset >= 0) {
					System.out.println("INST NEW: " + initInvkStmt);
					monitors.add(new NewInstanceMonitor(methodSignature, bytecodeOffset));
				} else {
					System.out.println("bytecode offset unavailable: " + method.getSignature());
				}
			}
		}
		
		if(rightOp instanceof AnyNewExpr) {
			System.out.println("UNHANDLED: array allocation sites");
		}
		
		// call rets
		if(stmt.containsInvokeExpr()) {
			int bytecodeOffset = bytecodeOffset(stmt);
			if(bytecodeOffset >= 0) {
				System.out.println("INST RET: " + stmt);
				monitors.add(new CallRetMonitor(methodSignature, bytecodeOffset));
			} else {
				System.out.println("bytecode offset unavailable: " + method.getSignature());
			}
		}
		
		// definition other than invocation
		if(!(rightOp instanceof AnyNewExpr) && !(rightOp instanceof NewExpr) && !stmt.containsInvokeExpr()) {
			int bytecodeOffset = bytecodeOffset(stmt);
			if(bytecodeOffset >= 0) {
				System.out.println("INST DEF: " + stmt);
				monitors.add(new DefinitionMonitor(methodSignature, bytecodeOffset));
			} else {
				System.out.println("bytecode offset unavailable: " + method.getSignature());
			}
		}
		
		return monitors;
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
