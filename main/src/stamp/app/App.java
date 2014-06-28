package stamp.app;

import java.util.*;
import java.util.jar.*;
import java.io.*;

/*
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.DexFile; 
*/

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.DexFileFactory;

import soot.Modifier;
import soot.jimple.Stmt;
import soot.jimple.Constant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.AssignStmt;
import soot.Local;
import soot.Value;
import soot.SootClass;
import soot.SootMethod;
import soot.Body;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

import shord.program.Program;

/**
 * @author Saswat Anand
 **/
public class App
{
	private List<Component> comps = new ArrayList();
	//private Map<Integer,Layout> layouts = new HashMap();
	private List<Layout> layouts = new ArrayList();
	private PublicXml publicXml;
	private Set<String> permissions = new HashSet();

	private String pkgName;
	private String version;
	private String iconPath;

	public static App readApp(String apkPath, String apktoolOutDir)
	{
		App app = new App();
	
		File manifestFile = new File(apktoolOutDir, "AndroidManifest.xml");				
		ParseManifest pmf = new ParseManifest(manifestFile, app);

		File resDir = new File(apktoolOutDir, "res");
		app.publicXml = new PublicXml(new File(resDir, "values/public.xml"));
		//List<Layout> layouts = new ParseLayout().process(resDir);
		app.layouts = new ParseLayout().process(resDir);

		app.process(apkPath, apktoolOutDir/*, layouts*/);
		
		return app;
	}

	public void process(String apkPath, String apktoolOutDir/*, List<Layout> layouts*/)
	{
		if(iconPath != null){
			if(iconPath.startsWith("@drawable/")){
				String icon = iconPath.substring("@drawable/".length()).concat(".png");
				File f = new File(apktoolOutDir.concat("/res/drawable"), icon);
				if(f.exists())
					iconPath = f.getPath();
				else {
					f = new File(apktoolOutDir.concat("/res/drawable-hdpi"), icon);
					if(f.exists())
						iconPath = f.getPath();
					else
						iconPath = null;
				}
			} else
				iconPath = null;
		}

		Set<String> widgetNames = new HashSet();
		for(Layout layout : layouts){
			widgetNames.addAll(layout.customWidgets);
		}

		List<Component> comps = this.comps;
		this.comps = new ArrayList();

		Set<String> compNames = new HashSet();
		for(Component c : comps){
			//System.out.println("@@ "+c.name);
			compNames.add(c.name);
		}

		filterDead(apkPath, compNames, widgetNames);

		System.out.println("^^ "+compNames.size());
		
		for(Component c : comps){
			if(compNames.contains(c.name))
				this.comps.add(c);
		}

		for(Layout layout : layouts){
			//this.layouts.put(layout.id, layout);
			
			Set<String> widgets = layout.customWidgets;
			for(Iterator<String> it = widgets.iterator(); it.hasNext();){
				String widget = it.next();
				if(!widgetNames.contains(widget))
					it.remove();
			}
		}
		
	}

	public List<Component> components()
	{
		return comps;
	}

	public Set<String> permissions()
	{
		return permissions;
	}

	public Layout layoutWithId(int id)
	{
		PublicXml.Entry e = publicXml.entryFor(id);
		String name = e.name;
		assert "layout".equals(e.type);
		for(Layout layout : layouts)
			if(name.equals(layout.fileName))
				return layout;
		return null;
	}

	public void setPackageName(String pkgName)
	{
		this.pkgName = pkgName;
	}

	public String getPackageName()
	{
		return this.pkgName;
	}
	
	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getVersion()
	{
		return this.version;
	}

	public void setIconPath(String icon)
	{
		this.iconPath = icon;
	}
	
	public String getIconPath()
	{
		return iconPath;
	}

	private void filterDead(String apkPath, Set<String> compNames, Set<String> widgetNames)
	{
		assert apkPath.endsWith(".apk");

		//Map<String,Integer> componentNameToLayoutId = new HashMap();

		Set<String> widgetNamesAvailable = new HashSet();
		Set<String> compNamesAvailable = new HashSet();

		try{
			File f = new File(apkPath);
			DexFile dexFile = DexFileFactory.loadDexFile(f, 1);
			//for (ClassDefItem defItem : dexFile.ClassDefsSection.getItems()) {
			//String className = defItem.getClassType().getTypeDescriptor();
			for(ClassDef defItem : dexFile.getClasses()){
				String className = defItem.getType();
				if(className.charAt(0) == 'L'){
					int len = className.length();
					assert className.charAt(len-1) == ';';
					className = className.substring(1, len-1);
				}
				className = className.replace('/','.');
				String tmp = className;
				//String tmp = className.replace('$', '.');
				if(compNames.contains(tmp)) {
					compNamesAvailable.add(tmp);
					//System.out.println("%% "+tmp);
					//componentNameToCallbacks.put(className, findCallbackMethods(defItem));
					//componentNameToLayoutId.put(className, findLayoutId(defItem));
				}
				//else if(otherComps.contains(tmp))
				//	componentNameToCallbacks.put(className, Collections.EMPTY_LIST);
				else if(widgetNames.contains(tmp))
					widgetNamesAvailable.add(tmp);
					//customWidgetToConstructors.put(className, findConstructors(defItem));
			}
		} catch(IOException e){
			throw new Error(e);
		}

		compNames.clear();
		compNames.addAll(compNamesAvailable);

		widgetNames.clear();
		widgetNames.addAll(widgetNamesAvailable);
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder("App : {\n");

		builder.append("package: "+pkgName+"\n");
		builder.append("version: "+version+"\n");

		builder.append("comps : {\n");
		for(Component c : comps){
			builder.append("\t"+c.toString()+"\n");
		}
		builder.append("}\n");

		builder.append("perms: {\n");
		for(String perm : permissions){
			builder.append("\t"+perm+"\n");
		}
		builder.append("}\n");

		builder.append("}");
		return builder.toString();
	}

	public void findLayouts()
	{
		for(Component comp : comps)
			findLayoutsFor(comp);
	}

	private void findLayoutsFor(Component comp)
	{
		if(comp.type != Component.Type.activity)
			return;

		SootClass activity = Program.g().scene().getSootClass(comp.name);
		
		for(SootMethod m : activity.getMethods()){
			if(!m.isConcrete())
				continue;
			Body body = m.retrieveActiveBody();
			SimpleLocalDefs sld = null;
			for(Unit u : body.getUnits()){
				Stmt s = (Stmt) u;
				if(!s.containsInvokeExpr())
					continue;
				InvokeExpr ie = s.getInvokeExpr();
				if(!ie.getMethod().getSignature().equals("<android.app.Activity: void setContentView(int)>"))
					continue;

				if(m.isStatic()){
					System.out.println("WARN: setContentView called in a static method "+m.getSignature());
					continue;
				} 

				Value rcvr = ((InstanceInvokeExpr) ie).getBase();
				Local thisLocal = body.getThisLocal();
				if(!rcvr.equals(thisLocal)){
					if(sld == null)
						sld = new SimpleLocalDefs(new ExceptionalUnitGraph(body));

					boolean warn = true;
					if(rcvr instanceof Local){
						warn = false;
						for(Unit def : sld.getDefsOfAt((Local) rcvr, u)){
							if(!(def instanceof AssignStmt) || !thisLocal.equals(((AssignStmt) def).getRightOp())){
								warn = true;
								break;
							}
						}
					}

					if(warn){
						System.out.println("WARN: rcvr of setContentView is not equal to ThisLocal of method "+m.getSignature());
						continue;
					}
				}

				Value arg = ie.getArg(0);
				if(arg instanceof Constant){
					int layoutId = ((IntConstant) arg).value;
					Layout layout = layoutWithId(layoutId);
					if(layout != null){
						comp.addLayout(layout);
						System.out.println("Layout: "+comp.name+" "+layout.fileName);
					}
					else
						System.out.println("WARN: Did not found layout for id = "+layoutId);
				} else {
					if(sld == null)
						sld = new SimpleLocalDefs(new ExceptionalUnitGraph(body));

					//System.out.println("WARN: Argument of setContentView is not constant");					
					for(Unit def : sld.getDefsOfAt((Local) arg, u)){
						if(!(def instanceof AssignStmt))
							continue;
						Value rhs = ((AssignStmt) def).getRightOp();
						if(!(rhs instanceof IntConstant))
							continue;
						int layoutId = ((IntConstant) rhs).value;
						Layout layout = layoutWithId(layoutId);
						if(layout != null){
							comp.addLayout(layout);
							System.out.println("Layout: "+comp.name+" "+layout.fileName);
						}
						else
							System.out.println("WARN: Did not found layout for id = "+layoutId);
					}
				}
			}
		}
	}
}
