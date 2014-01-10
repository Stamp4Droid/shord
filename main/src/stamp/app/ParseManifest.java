package stamp.app;

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
	private App app;
	private XPath xpath;
	private Document document;

	public ParseManifest(File manifestFile, App app)
	{
		this.app = app;

		try{
			File tmpFile = File.createTempFile("stamp_android_manifest", null, null);
			tmpFile.deleteOnExit();
			UTF8ToAnsiUtils.main(new String[]{manifestFile.getAbsolutePath(), tmpFile.getAbsolutePath()});
			manifestFile = tmpFile;

			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.parse(manifestFile);
			
			xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new PersonalNamespaceContext());
			
			//find package name
			Node node = (Node)
				xpath.evaluate("/manifest", document, XPathConstants.NODE);
			app.setPackageName(node.getAttributes().getNamedItem("package").getNodeValue());
			app.setVersion(node.getAttributes().getNamedItem("android:versionName").getNodeValue());

			readComponentInfo();
			readPermissionInfo();			
		}catch(Exception e){
			throw new Error(e);
		}		
	}

	private void readPermissionInfo() throws Exception
	{
		Set<String> permissions = app.permissions();
		NodeList nodes = (NodeList)
			xpath.evaluate("/manifest/uses-permission", document, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			Attr attr = ((Element) node).getAttributeNode("android:name");
			permissions.add(attr.getValue());
		}
	}

	private void readComponentInfo() throws Exception
	{		
		//find activities
		findComponents(xpath, document, Component.Type.activity);
		findComponents(xpath, document, Component.Type.service);
		findComponents(xpath, document, Component.Type.receiver);
		
		Node node = (Node)
			xpath.evaluate("/manifest/application", document, XPathConstants.NODE);
		
		//backup agent
		Node backupAgent = node.getAttributes().getNamedItem("android:backupAgent");
		if(backupAgent != null)
			addComp(new Component(fixName(backupAgent.getNodeValue())));
			
		//application class
		Node application = node.getAttributes().getNamedItem("android:name");
		if(application != null)
			addComp(new Component(fixName(application.getNodeValue())));
	}

	private String fixName(String comp)
	{
		String pkgName = app.getPackageName();
		if(comp.startsWith("."))
			comp = pkgName + comp;
		else if(comp.indexOf('.') < 0)
			comp = pkgName + "." + comp;
		return comp;
	}


	private void findComponents(XPath xpath, Document document, Component.Type componentType) throws Exception
	{
		NodeList nodes = (NodeList)
			xpath.evaluate("/manifest/application/"+componentType, document, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			//find the name of component
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
			Component comp = addComp(new Component(fixName(name), componentType));			
			setIntentFilters(comp, node);
		}
	}

	private void setIntentFilters(Component comp, Node compNode)
	{
		Node ifNode = compNode.getFirstChild();
		while(ifNode != null){
			if(ifNode.getNodeName().equals("intent-filter")){
				IntentFilter intentFilter = new IntentFilter();
				comp.addIntentFilter(intentFilter);

				Attr attr = ((Element) ifNode).getAttributeNode("android:priority");
				if(attr != null){
					intentFilter.setPriority(attr.getValue());
				}
				
				Node actNode = ifNode.getFirstChild();
				while(actNode != null){
					if(actNode.getNodeName().equals("action")){
						Attr actNameNode = ((Element) actNode).getAttributeNode("android:name");
						if(actNameNode != null){
							intentFilter.addAction(actNameNode.getValue());
						}
					}
					actNode = actNode.getNextSibling();
				}
				
			}
			ifNode = ifNode.getNextSibling();
		}
	}
	

	private Component addComp(Component c)
	{
		List<Component> comps = app.components();
		for(Component comp : comps){
			if(comp.name.equals(c.name)){
				assert c.type.equals(comp.type);
				return comp;
			}
		}
		comps.add(c);
		return c;
	}
}
