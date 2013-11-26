package stamp.harnessgen;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;
import java.util.*;
import java.util.jar.JarFile;

/*
* reads AndroidManifest.xml to find out several info about the app
* @author Saswat Anand
*/
public class ParseManifest
{
	private String pkgName;

	List<Component> process(File manifestFile)
	{
		try{
			File tmpFile = File.createTempFile("stamp_android_manifest", null, null);
			tmpFile.deleteOnExit();
			UTF8ToAnsiUtils.main(new String[]{manifestFile.getAbsolutePath(), tmpFile.getAbsolutePath()});
			manifestFile = tmpFile;

			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(manifestFile);
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new PersonalNamespaceContext());
			
			//find package name
			Node node = (Node)
				xpath.evaluate("/manifest", document, XPathConstants.NODE);
			pkgName = node.getAttributes().getNamedItem("package").getNodeValue();
			
			List<Component> comps = new ArrayList();

			//find activities
			findComponents(xpath, document, comps, "activity");
			findComponents(xpath, document, comps, "service");
			findComponents(xpath, document, comps, "receiver");
			
			node = (Node)
				xpath.evaluate("/manifest/application", document, XPathConstants.NODE);

			//backup agent
			Node backupAgent = node.getAttributes().getNamedItem("android:backupAgent");
			if(backupAgent != null)
				comps.add(new Component(fixName(backupAgent.getNodeValue()), false));
			
			//application class
			Node application = node.getAttributes().getNamedItem("android:name");
			if(application != null)
				comps.add(new Component(fixName(application.getNodeValue()), false));

			return comps;
		}catch(Exception e){
			throw new Error(e);
		}
	}

	private String fixName(String comp)
	{
		if(comp.startsWith("."))
			comp = pkgName + comp;
		else if(comp.indexOf('.') < 0)
			comp = pkgName + "." + comp;
		return comp;
	}


	private void findComponents(XPath xpath, Document document, List<Component> comps, String componentType) throws Exception
	{
		NodeList nodes = (NodeList)
			xpath.evaluate("/manifest/application/"+componentType, document, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			NamedNodeMap nnm = node.getAttributes();
			String name = null;
			for(int j = 0; j < nnm.getLength(); j++){
				Node n = nnm.item(j);
				if(n.getNodeName().equals("android:name")){
					name = n.getNodeValue();
					break;
				}
				//System.out.println(n.getNodeName() + " " + );
			}			
			assert name != null : node.getNodeName();
			comps.add(new Component(fixName(name), componentType.equals("activity")));
		}
	}

	/*
	public static void main(String[] args) throws Exception
	{
		File androidManifestFile = new File(args[0]);
		String classPath = args[1];
		String androidJar = args[2];

		List<JarFile> jars = new ArrayList();
		for(String cp : classPath.split(":")){
			if(!(new File(cp).exists()))
				System.out.println("WARNING: "+cp +" does not exists!");
			else
				jars.add(new JarFile(cp));
		}

		App app = new App(androidManifestFile, classPath, androidJar);
		System.out.println(app);
		}*/
}