package stamp.injectannot;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Body;
import soot.SootField;
import soot.Unit;
import soot.Local;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.jimple.Constant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.AssignStmt;
import soot.util.Chain;
import soot.util.NumberedSet;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import stamp.app.App;
import stamp.app.Layout;
import stamp.app.Widget;
import shord.project.analyses.JavaAnalysis;
import shord.program.Program;

import chord.project.Chord;
/*
 * @author Saswat Anand
*/
@Chord(name="gui-fix")
public class GuiFix extends JavaAnalysis
{
	private Map<Integer,List<String>> viewIdToWidgetFlds = new HashMap();
	private SimpleLocalDefs sld = null;
	private Body body;
	private SootClass gClass;

	public GuiFix()
	{
		String widgetsListFile = System.getProperty("stamp.widgets.file");
		BufferedReader reader;
		try{
			reader = new BufferedReader(new FileReader(widgetsListFile));
			String gClassName = reader.readLine();
			this.gClass = Scene.v().getSootClass(gClassName);

			String line;
			while((line = reader.readLine()) != null){
				String[] tokens = line.split(",");
				int id = Integer.parseInt(tokens[0]);
				String widgetSubsig = tokens[1];
				if(id >= 0){
					List<String> ws = viewIdToWidgetFlds.get(id);
					if(ws == null){
						ws = new ArrayList();
						viewIdToWidgetFlds.put(id, ws);
					}
					ws.add(widgetSubsig);
				}
			}
			reader.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}

	public void run()
	{
		NumberedSet fklasses = frameworkClasses();
		for(SootClass klass : Program.g().getClasses()){
			if(fklasses.contains(klass))
				continue;
			for(SootMethod method : klass.getMethods()){
				if(!method.isConcrete())
					continue;
				this.body = method.retrieveActiveBody();
				process();
			}
		}
	}
	
	private void process()
	{
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> uit = units.snapshotIterator();
		while(uit.hasNext()){
			Stmt stmt = (Stmt) uit.next();
			if(!stmt.containsInvokeExpr() || !(stmt instanceof AssignStmt))
				continue;
			InvokeExpr ie = stmt.getInvokeExpr();
			SootMethod callee = ie.getMethod();
			if(!callee.getSubSignature().equals("android.view.View findViewById(int)"))
				continue;

			int viewId = viewId(ie.getArg(0), stmt);
			List<String> widgetFlds = viewIdToWidgetFlds.get(viewId);
			if(widgetFlds == null)
				continue;
			Local leftOp = (Local) ((AssignStmt) stmt).getLeftOp();
			for(String subsig : widgetFlds){
				System.out.println("%% "+viewId+" "+subsig);
				SootField fld = gClass.getField(subsig);
				Stmt loadStmt = Jimple.v().newAssignStmt(leftOp, Jimple.v().newStaticFieldRef(fld.makeRef()));
				units.insertBefore(loadStmt, stmt);
			}
			units.remove(stmt);
		}
		sld = null;
	}

	private int viewId(Value arg, Stmt stmt)
	{
		int viewId = -1;
		if(arg instanceof Constant){
			viewId = ((IntConstant) arg).value;
		} else {
			if(sld == null)
				sld = new SimpleLocalDefs(new ExceptionalUnitGraph(body));
			
			for(Unit def : sld.getDefsOfAt((Local) arg, stmt)){
				if(!(def instanceof AssignStmt))
					continue;
				Value rhs = ((AssignStmt) def).getRightOp();
				if(rhs instanceof IntConstant){
					viewId = ((IntConstant) rhs).value;
					break;
				}
			}
		}
		return viewId;
	}

	NumberedSet frameworkClasses()
	{
		Scene scene = Scene.v();
		NumberedSet frameworkClasses = new NumberedSet(scene.getClassNumberer());
		String androidJar = System.getProperty("stamp.android.jar");
		JarFile archive;
		try{
			archive = new JarFile(androidJar);
		}catch(IOException e){
			throw new Error(e);
		}
		for (Enumeration entries = archive.entries(); entries.hasMoreElements();) {
			JarEntry entry = (JarEntry) entries.nextElement();
			String entryName = entry.getName();
			int extensionIndex = entryName.lastIndexOf('.');
			if (extensionIndex >= 0) {
				String entryExtension = entryName.substring(extensionIndex);
				if (".class".equals(entryExtension)) {
					entryName = entryName.substring(0, extensionIndex);
					entryName = entryName.replace('/', '.');
					if(scene.containsClass(entryName))
						frameworkClasses.add(scene.getSootClass(entryName));
				}
			}
		}
		return frameworkClasses;
	}
}