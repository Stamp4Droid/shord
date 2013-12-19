package stamp.harnessgen;

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
			System.out.println("@@ "+c.name);
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

		/*
		for(Map.Entry<String,Integer> entry : componentNameToLayoutId.entrySet()){
			String compName = entry.getKey();
			String layoutId = entry.getValue();
			
			for(String widget : layoutIdToWidgets.get(layoutId)){
				List<String> constructors = customWidgetToConstructors.get(widget);
				if(constructors != null){
					List customWidgets = componentNameToCustomWidgets.get(compName);
					if(customWidgets == null){
						customWidgets = new ArrayList();
						componentNameToCustomWidgets.put(compName, customWidgets);
					}
					customWidgets.add(widget);
				}
			}
		}
		*/
	}


	/*
	private List<String> findCallbackMethods(ClassDefItem classDef)
	{
		List<String> ret = null;
		ClassDataItem classData = classDef.getClassData();
		for(ClassDataItem.EncodedMethod method : classData.getVirtualMethods()) {
			String name = method.method.getMethodName().getStringValue();
			if(!xmlCallbacks.contains(name))
				continue;
			if(!Modifier.isPublic(method.accessFlags))
				continue;
			char c = method.method.getPrototype().getReturnType().getTypeDescriptor().charAt(0);
			if(c != 'V') //must be void type
				continue;
			if(method.method.getPrototype().getParameters() == null){
				continue;
			}
			List<TypeIdItem> paramTypes = method.method.getPrototype().getParameters().getTypes();
			if(paramTypes.size() != 1)
				continue;
			String p0Type = paramTypes.get(0).getTypeDescriptor();
			if(!p0Type.equals("Landroid/view/View;"))
				continue;
			
			if(ret == null)
				ret = new LinkedList();
			ret.add(name);
		}		
		return ret == null ? Collections.EMPTY_LIST : ret;
	} 


	private List<String> findConstructors(ClassDefItem classDef)
	{
		List<String> ret = new ArrayList();
		//System.out.println("custom GUI "+classDef); 
		ClassDataItem classData = classDef.getClassData();
		for(ClassDataItem.EncodedMethod method : classData.getDirectMethods()) {
			String name = method.method.getMethodName().getStringValue();
			if(!name.equals("<init>"))
				continue;
			//if(!Modifier.isPublic(method.accessFlags))
			//continue;
			//System.out.println("sig: "+name+"*"+method.method.getShortMethodString());
			ret.add(method.method.getShortMethodString());
		}
		return ret;
	}


	private Integer findLayoutId(ClassDefItem classDef)
	{
		List<String> ret = null;
		ClassDataItem classData = classDef.getClassData();
		for(ClassDataItem.EncodedMethod method : classData.getVirtualMethods()) {
			String name = method.method.getMethodName().getStringValue();
			if(!name.equals("onCreate"))
				continue;

			char c = method.method.getPrototype().getReturnType().getTypeDescriptor().charAt(0);
			if(c != 'V') //must be void type
				continue;
			if(method.method.getPrototype().getParameters() == null){
				continue;
			}
			List<TypeIdItem> paramTypes = method.method.getPrototype().getParameters().getTypes();
			if(paramTypes.size() != 1)
				continue;
			String p0Type = paramTypes.get(0).getTypeDescriptor();
			if(!p0Type.equals("Landroid/os/Bundle;"))
				continue;
			
			//found the onCreate method. now scan through each stmt
			
			CodeItem codeItem = method.codeItem;
			for(Instruction ins : codeItem.getInstructions){
				Opcode op = ins.opcode;
				if(op != INVOKE_VIRTUAL)
					continue;
			}
		}
	}	
	*/
}
