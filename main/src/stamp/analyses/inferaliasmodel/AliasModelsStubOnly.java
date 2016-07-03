package stamp.analyses.inferaliasmodel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import shord.analyses.SiteAllocNode;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.jimple.Stmt;
import stamp.analyses.inferaliasmodel.InstrumentationDataWriter.CallRetMonitor;
import stamp.analyses.inferaliasmodel.InstrumentationDataWriter.Monitor;
import stamp.analyses.inferaliasmodel.InstrumentationDataWriter.MonitorWriter;
import stamp.analyses.inferaliasmodel.InstrumentationDataWriter.NewInstanceMonitor;
import stamp.analyses.inferaliasmodel.MonitorMapUtils.VariableMap;

public class AliasModelsStubOnly {
	private static List<Monitor> getMonitorsForAliasModels(Stmt stmt) {
		System.out.println("PROCESSING: " + stmt);
		VariableMap varMap = MonitorMapUtils.getVarMap();
		List<Monitor> monitors = new ArrayList<Monitor>();
		for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(varMap.getMethod(stmt), stmt)) {
			if(monitor instanceof CallRetMonitor || monitor instanceof NewInstanceMonitor) {
				monitors.add(monitor);
			}
		}
		return monitors;
	}
	
	private static List<Stmt> frameworkAllocs = null;
	public static List<Stmt> getFrameworkAllocs() {
		if(frameworkAllocs == null) {
			frameworkAllocs = new ArrayList<Stmt>();
			ProgramRel relEscapeCountH = (ProgramRel)ClassicProject.g().getTrgt("EscapeH");
			relEscapeCountH.load();
			for(Object obj : relEscapeCountH.getAry1ValTuples()) {
				if(!(obj instanceof SiteAllocNode)) {
					System.out.println("INVALID FRAMEWORK ALLOC: " + obj);
					continue;
				}
				SiteAllocNode alloc = (SiteAllocNode)obj;
				Stmt stmt = (Stmt)alloc.getUnit();
				frameworkAllocs.add(stmt);
			}
			relEscapeCountH.close();
		}
		return frameworkAllocs;
	}
	
	private static List<Stmt> frameworkLimAllocs = null;
	public static List<Stmt> getFrameworkLimAllocs() {
		if(frameworkLimAllocs == null) {
			frameworkLimAllocs = new ArrayList<Stmt>();
			ProgramRel relEscapeCountH = (ProgramRel)ClassicProject.g().getTrgt("EscapeLimH");
			relEscapeCountH.load();
			for(Object obj : relEscapeCountH.getAry1ValTuples()) {
				if(!(obj instanceof SiteAllocNode)) {
					System.out.println("INVALID FRAMEWORK LIM ALLOC: " + obj);
					continue;
				}
				SiteAllocNode alloc = (SiteAllocNode)obj;
				Stmt stmt = (Stmt)alloc.getUnit();
				frameworkLimAllocs.add(stmt);
			}
			relEscapeCountH.close();
		}
		return frameworkLimAllocs;
	}
	
	private static List<Stmt> stubAllocs = null;
	public static List<Stmt> getStubAllocs() {
		if(stubAllocs == null) {
			stubAllocs = new ArrayList<Stmt>();
			ProgramRel relEscapeCountH = (ProgramRel)ClassicProject.g().getTrgt("EscapeCountH");
			relEscapeCountH.load();
			for(Object obj : relEscapeCountH.getAry1ValTuples()) {
				if(!(obj instanceof SiteAllocNode)) {
					System.out.println("INVALID STUB ALLOC: " + obj);
					continue;
				}
				SiteAllocNode alloc = (SiteAllocNode)obj;
				Stmt stmt = (Stmt)alloc.getUnit();
				stubAllocs.add(stmt);
			}
			relEscapeCountH.close();
		}
		return stubAllocs;
	}
	
	private static List<Stmt> stubLimAllocs = null;
	public static List<Stmt> getStubLimAllocs() {
		if(stubLimAllocs == null) {
			stubLimAllocs = new ArrayList<Stmt>();
			ProgramRel relEscapeCountH = (ProgramRel)ClassicProject.g().getTrgt("EscapeCountLimH");
			relEscapeCountH.load();
			for(Object obj : relEscapeCountH.getAry1ValTuples()) {
				if(!(obj instanceof SiteAllocNode)) {
					System.out.println("INVALID STUB LIM ALLOC: " + obj);
					continue;
				}
				SiteAllocNode alloc = (SiteAllocNode)obj;
				Stmt stmt = (Stmt)alloc.getUnit();
				stubLimAllocs.add(stmt);
			}
			relEscapeCountH.close();
		}
		return stubLimAllocs;
	}
	
	private static List<Stmt> frameworkInvokes = null;
	public static List<Stmt> getFrameworkInvokes() {
		if(frameworkInvokes == null) {
			frameworkInvokes = new ArrayList<Stmt>();
			ProgramRel relStubInvokeMonitorRet = (ProgramRel)ClassicProject.g().getTrgt("FrameworkInvokeMonitorRet");
			relStubInvokeMonitorRet.load();
			for(Object obj : relStubInvokeMonitorRet.getAry1ValTuples()) {
				if(!(obj instanceof Stmt)) {
					System.out.println("INVALID FRAMEWORK INVOKE: " + obj);
					continue;
				}
				Stmt stmt = (Stmt)obj;
				frameworkInvokes.add(stmt);
			}
			relStubInvokeMonitorRet.close();
		}
		return frameworkInvokes;
	}
	
	private static List<Stmt> stubInvokes = null;
	public static List<Stmt> getStubInvokes() {
		if(stubInvokes == null) {
			stubInvokes = new ArrayList<Stmt>();
			ProgramRel relStubInvokeMonitorRet = (ProgramRel)ClassicProject.g().getTrgt("StubInvokeMonitorRet");
			relStubInvokeMonitorRet.load();
			for(Object obj : relStubInvokeMonitorRet.getAry1ValTuples()) {
				if(!(obj instanceof Stmt)) {
					System.out.println("INVALID STUB INVOKE: " + obj);
					continue;
				}
				Stmt stmt = (Stmt)obj;
				stubInvokes.add(stmt);
			}
			relStubInvokeMonitorRet.close();
		}
		return stubInvokes;
	}
	
	public static void run() {
		List<Monitor> monitors = new ArrayList<Monitor>();
		for(Stmt stmt : getStubAllocs()) {
			monitors.addAll(getMonitorsForAliasModels(stmt));
		}
		for(Stmt stmt : getStubInvokes()) {
			monitors.addAll(getMonitorsForAliasModels(stmt));
		}
		
		try {
			File outDir = new File(System.getProperty("stamp.out.dir"), "inferaliasmodel");
			outDir.mkdirs();
			MonitorWriter writer = new MonitorWriter(new PrintWriter(new File(outDir, "instrinfo.txt")));
			writer.writeAll(monitors);
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to write instrumentation!");
		}
	}
}
