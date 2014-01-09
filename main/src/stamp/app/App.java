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

	public App(String apkPath, String apktoolOutDir)
	{
		File resDir = new File(apktoolOutDir, "res");
		List<Layout> layouts = new ParseLayout().process(resDir);

		Set<String> widgetNames = new HashSet();
		for(Layout layout : layouts){
			widgetNames.addAll(layout.customWidgets);
		}

		File manifestFile = new File(apktoolOutDir, "AndroidManifest.xml");				
		List<Component> comps = new ParseManifest().process(manifestFile);

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
	
	public Layout layoutWithId(int id)
	{
		return layouts.get(id);
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
		for(Component c : comps){
			builder.append(c.toString()+"\n");
		}
		builder.append("}");
		return builder.toString();
	}
}