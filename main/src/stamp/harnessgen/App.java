package stamp.harnessgen;

import java.util.*;
import java.util.jar.*;
import java.io.*;

import org.jf.dexlib.ClassDefItem;                                                                                                                                                                                  
import org.jf.dexlib.DexFile; 

/**
 * @author Saswat Anand
 **/
public class App
{
	protected final List<String> components = new ArrayList();
	protected final List<String> classes = new ArrayList();

	public App(String classPath, String androidJar)
	{
		this(null, classPath, androidJar);
	}

	public App(File manifestFile, String classPath, String androidJar)
	{
		computeClassNames(classPath, androidJar);
		
		if(manifestFile == null)
			manifestFile = getManifestFile(classPath);
		
		for(String c : ParseManifest.process(manifestFile)){
			if(classes.contains(c))
				components.add(c);
		}
	}

	//extract it from the stamp.app.jar
	private static File getManifestFile(String classPath)
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

	public Iterable<String> components()
	{
		return components;
	}

	private void computeClassNames(String classPath, String androidJar)
	{
		try{
			for(String cpElem : classPath.split(":")) {
				File f = new File(cpElem);
				if(!(f.exists())){
					System.out.println("WARNING: "+cpElem +" does not exists!");
					continue;
				}
				if(cpElem.endsWith(".jar")){
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
						classes.add(className.replace('$', '.'));
					}
				} else 
					assert false : cpElem;
			}
		} catch(IOException e){
			throw new Error(e);
		}
	}
	
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder("[");
		for(String s : components)
			builder.append(s + ", ");
		builder.append("]");
		return builder.toString();
	}
}