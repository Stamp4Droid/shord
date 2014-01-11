package stamp.app;

import java.util.*;
import java.util.jar.*;
import java.io.*;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.DexFile; 

import soot.Modifier;

/**
 * @author Saswat Anand
 **/
public class App
{
	private List<Component> comps = new ArrayList();
	private Map<Integer,Layout> layouts = new HashMap();
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
		List<Layout> layouts = new ParseLayout().process(resDir);

		app.process(apkPath, apktoolOutDir, layouts);
		
		return app;
	}

	public void process(String apkPath, String apktoolOutDir, List<Layout> layouts)
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
			this.layouts.put(layout.id, layout);
			
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
		return layouts.get(id);
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
			DexFile dexFile = new DexFile(f);
			for (ClassDefItem defItem : dexFile.ClassDefsSection.getItems()) {
				String className = defItem.getClassType().getTypeDescriptor();
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
					System.out.println("%% "+tmp);
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
}
