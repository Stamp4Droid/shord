package stamp.app;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;


import java.util.*;

public class Layout
{
	public final List<Widget> widgets = new ArrayList();
	public final Set<String> callbacks = new HashSet();
	public final int id;
	public final String fileName;
	public final List<String> includedLayouts = new ArrayList();

	Layout(int id, File layoutFile, PublicXml publicXml, StringXml stringXml)
	{
		this.id = id;
		this.fileName = layoutFile.getName();

		try{
			File tmpFile = File.createTempFile("stamp_android_layout", null, null);
			tmpFile.deleteOnExit();
			UTF8ToAnsiUtils.main(new String[]{layoutFile.getAbsolutePath(), tmpFile.getAbsolutePath()});
			layoutFile = tmpFile;

			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Reader reader =
				new InputStreamReader(new FileInputStream(layoutFile), "Cp1252");
			Document document = builder.parse(new InputSource(reader));

			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new PersonalNamespaceContext());

			findCallbacks(document, xpath, stringXml);
			findWidgets(document, xpath, publicXml);
		}catch(Exception e){
			throw new Error(e);
		}

	}

	private void findWidgets(Document document, XPath xpath, PublicXml publicXml) throws javax.xml.xpath.XPathExpressionException
	{		
		NodeList nodes = (NodeList)
			xpath.evaluate("//*", document, XPathConstants.NODESET);
		//System.out.println("nodes.size() = "+nodes.getLength());
		for(int i = 0; i < nodes.getLength(); i++) {
			//System.out.println("HELLO");
			Element elem = (Element) nodes.item(i);
			//System.out.println("Widget: "+node.getNodeName());
			String elemTag = elem.getNodeName();
			if(elemTag.equals("include")){
				String layoutId = elem.getAttribute("layout");
				assert layoutId.startsWith("@layout/") : layoutId;
				includedLayouts.add(layoutId.substring(8));
				continue;
			}
			else if(elemTag.equals("fragment"))
				continue;

			String id = elem.getAttribute("android:id");
			if(id.isEmpty())
				;  //no id, harness will just instantiate, but not store in any field
			else if(id.startsWith("@id/"))
				id = id.substring(1);
			else if(id.startsWith("@+id/"))
				id = id.substring(2);
			else if(id.startsWith("@*android:id/"))
				id = id.substring(2);
			else
				assert false : id;
			int numId = -1;
			if(id.startsWith("id/"))
				numId = publicXml.idIdFor(id.substring(3));
			widgets.add(new Widget(elem.getTagName(), id, numId));
		}
	}
	
	private void findCallbacks(Document document, XPath xpath, StringXml stringXml) throws javax.xml.xpath.XPathExpressionException
	{
		NodeList nodes = (NodeList)
			xpath.evaluate("//*[@onClick]", document, XPathConstants.NODESET);
		
		for(int i = 0; i < nodes.getLength(); i++) {
			//System.out.println("HELLO");
			Node node = nodes.item(i);
			NamedNodeMap nnm = node.getAttributes();
			String name = null;
			for(int j = 0; j < nnm.getLength(); j++){
				Node n = nnm.item(j);
				if(n.getNodeName().equals("android:onClick")){
					name = n.getNodeValue();
					if(name.startsWith("@string/")){
						String stringId = name.substring("@string/".length());
						name = stringXml.stringValueFor(stringId);
					}
					callbacks.add(name);
					System.out.println("Callback: "+name);
				}
				//System.out.println(n.getNodeName() + " " + );
			}
		}
	}


	public String toString()
	{
		StringBuilder builder = new StringBuilder("{");
		builder.append("\"file\": \""+fileName+"\", ");
		builder.append("\"id\": \""+id+"\", ");
		builder.append("\"widgets\": [");
		int len = widgets.size();
		for(int i = 0; i < len; i++){
			builder.append(widgets.get(i).toString());
			if(i < (len-1))
				builder.append(", ");
		}
		builder.append("], ");
		builder.append("\"callbacks\": [");
		len = callbacks.size();
		int i = 0;
		for(String cb : callbacks){
			builder.append("\""+cb+"\"");
			if(i < (len-1))
				builder.append(", ");
			i++;
		}
		builder.append("]");
		builder.append("}");
		return builder.toString();
	}
	
}