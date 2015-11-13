package stamp.analyses.inferaliasmodel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import shord.analyses.SiteAllocNode;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.jimple.InvokeStmt;
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
	
	private static List<Stmt> allocs = null;
	public static List<Stmt> getFilteredAllocs() {
		if(allocs == null) {
			allocs = new ArrayList<Stmt>();
			ProgramRel relEscapeCountH = (ProgramRel)ClassicProject.g().getTrgt("EscapeCountH");
			relEscapeCountH.load();
			for(Object obj : relEscapeCountH.getAry1ValTuples()) {
				if(!(obj instanceof SiteAllocNode)) {
					System.out.println("INVALID ALLOC: " + obj);
					continue;
				}
				SiteAllocNode alloc = (SiteAllocNode)obj;
				Stmt stmt = (Stmt)alloc.getUnit();
				allocs.add(stmt);
			}
			relEscapeCountH.close();
		}
		return allocs;
	}
	
	private static List<Stmt> invokes = null;
	public static List<Stmt> getFilteredInvokes() {
		if(invokes == null) {
			invokes = new ArrayList<Stmt>();
			ProgramRel relStubInvokeMonitorRet = (ProgramRel)ClassicProject.g().getTrgt("StubInvokeMonitorRet");
			relStubInvokeMonitorRet.load();
			for(Object obj : relStubInvokeMonitorRet.getAry1ValTuples()) {
				if(!(obj instanceof InvokeStmt)) {
					System.out.println("INVALID INVOKE: " + obj);
					continue;
				}
				InvokeStmt stmt = (InvokeStmt)obj;
				invokes.add(stmt);
			}
			relStubInvokeMonitorRet.close();
		}
		return invokes;
	}
	
	public static void run() {
		List<Monitor> monitors = new ArrayList<Monitor>();
		for(Stmt stmt : getFilteredAllocs()) {
			monitors.addAll(getMonitorsForAliasModels(stmt));
		}
		for(Stmt stmt : getFilteredInvokes()) {
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
