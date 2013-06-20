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
	//protected final List<String> components = new ArrayList();
	protected final Map<String,List<String>> components = new HashMap();
	//protected final List<File> layoutFiles = new ArrayList();
	//protected File manifestFile;


	//public App(String classPath, String androidJar)
	//{
	//	this(null, classPath, androidJar);
	//}

	public App(/*File manifestFile,*/ String classPath, String androidJar, String outDir)
	{
		//computeClassNames(classPath, androidJar);
		
		//if(manifestFile == null)
		//	this.manifestFile = getManifestFile(classPath);

		//extractFiles(classPath);		
		File layoutDir = new File(outDir, "res/layout");
		File[] layoutFiles = layoutDir.listFiles(new FilenameFilter(){
				public boolean accept(File dir, String name){
					return name.endsWith(".xml");
				}
			});
		Set<String> xmlCallbacks = new HashSet();
		if(layoutFiles != null){
			for(File lf : layoutFiles){
				ParseLayout.process(lf, xmlCallbacks);
			}
		}

		Set<String> activities = new HashSet();
		Set<String> otherComps = new HashSet();
		File manifestFile = new File(outDir, "AndroidManifest.xml");				
		new ParseManifest().process(manifestFile, activities, otherComps);

		process(classPath, xmlCallbacks, activities, otherComps);
	}

	private void process(String classPath, Set<String> xmlCallbacks, Set<String> activities, Set<String> otherComps)
	{
		try{
			for(String cpElem : classPath.split(":")) {
				File f = new File(cpElem);
				if(!(f.exists())){
					System.out.println("WARNING: "+cpElem +" does not exists!");
					continue;
				}
				if(cpElem.endsWith(".jar")){
					/*
					JarFile archive = new JarFile(f);
					for (Enumeration entries = archive.entries(); entries.hasMoreElements();) {
						JarEntry entry = (JarEntry) entries.nextElement();
						String entryName = entry.getName();
						int extensionIndex = entryName.lastIndexOf('.');
						if (extensionIndex >= 0) {
							String entryExtension = entryName.substring(extensionIndex);
							if (".class".equals(entryExtension)) {
								entryName = entryName.substring(0, extensionIndex);
								entryName = entryName.replace('/', '.');
								classes.add(entryName.replace('$', '.'));
							}
						}
					}
					*/
				} else if(cpElem.endsWith(".apk")){
					DexFile dexFile = new DexFile(f);
					for (ClassDefItem defItem : dexFile.ClassDefsSection.getItems()) {
						String className = defItem.getClassType().getTypeDescriptor();
						if(className.charAt(0) == 'L'){
							int len = className.length();
							assert className.charAt(len-1) == ';';
							className = className.substring(1, len-1);
						}
						className = className.replace('/','.');
						String tmp = className.replace('$', '.');
						if(activities.contains(tmp))
							components.put(className, findCallbackMethods(defItem, xmlCallbacks));
						else if(otherComps.contains(tmp))
							components.put(className, Collections.EMPTY_LIST);
					}
				} else 
					assert false : cpElem;
			}
		} catch(IOException e){
			throw new Error(e);
		}
	}


	private List<String> findCallbackMethods(ClassDefItem classDef, Set<String> callbacks)
	{
		List<String> ret = null;
		ClassDataItem classData = classDef.getClassData();
		for(ClassDataItem.EncodedMethod method : classData.getVirtualMethods()) {
			String name = method.method.getMethodName().getStringValue();
			if(!callbacks.contains(name))
				continue;
			if(!Modifier.isPublic(method.accessFlags))
				continue;
			char c = method.method.getPrototype().getReturnType().getTypeDescriptor().charAt(0);
			if(c != 'V') //must be void type
				continue;
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

	/*
	//extract it from the stamp.app.jar
	private File getManifestFile(String classPath)
	{		
		try{
			JarEntry entry = null;
			JarFile jarFile = null;
			
			for(String cp : classPath.split(":")){
				File f = new File(cp);
				if(!(f.exists()))
					continue;
				
				JarFile jf = new JarFile(f);
				entry = jf.getJarEntry("AndroidManifest.xml");
				if(entry != null){
					jarFile = jf;
					break;
				}
			}
		
			if(jarFile == null)
				throw new Error("Did not find AndroidManifest.xml in classpath");
		
			InputStream in = jarFile.getInputStream(entry);			
			File androidManifestFile = File.createTempFile("stamp_android_manifest", null, null);
			androidManifestFile.deleteOnExit();
			OutputStream out = new FileOutputStream(androidManifestFile);
		
			new AXMLPrinter().convert(in, out);
		
			return androidManifestFile;
		} catch(IOException e){
			throw new Error(e);
		}
	}
	*/

	/**
	   extract AndroidManifest.xml and layout xml files from the apk
	 */
	/*
	private void extractFiles(String classPath)
	{
		try{
			for(String cp : classPath.split(":")){
				File f = new File(cp);
				if(!(f.exists()))
					continue;

				JarFile jf = new JarFile(f);
				Enumeration<?> entries = jf.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    String entryName = entry.getName();
					if(entryName.startsWith("res/layout/") && entryName.endsWith(".xml")){
						layoutFiles.add(extractFile(entryName, jf, entry));
					} else if(entryName.equals("AndroidManifest.xml")){
						manifestFile = extractFile(entryName, jf, entry);
					}
                }
			}
		} catch(IOException e){
			throw new Error(e);
		}
	}

	private File extractFile(String entryName, JarFile jf, JarEntry entry) throws IOException
	{
		File tmpFile = File.createTempFile(entryName.replace('/','_'), null, null);
		tmpFile.deleteOnExit();
		new AXMLPrinter().convert(jf.getInputStream(entry), new FileOutputStream(tmpFile));
		return tmpFile;
	}

	public Iterable<String> components()
	{
		return components;
	}

	public Set<String> xmlCallbacks()
	{
		return xmlCallbacks;
	}

	
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder("[");
		for(String s : components)
			builder.append(s + ", ");
		builder.append("]");
		return builder.toString();
	}
	*/
}
