package stamp.app;

import javax.xml.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;
import java.util.*;

/*
* reads layout xml files to find out several info about the app
* @author Saswat Anand
*/
public class ParseLayout
{
	private Map<String,String> idToStringValue = new HashMap();
	private PublicXml publicXml = new PublicXml();

	List<Layout> process(File resDir)
	{
		publicXml.read(new File(resDir, "values/public.xml"));

		mapIdToStringValue(new File(resDir, "values/strings.xml"));

		File layoutDir = new File(resDir, "layout");
		File[] layoutFiles = layoutDir.listFiles(new FilenameFilter(){
				public boolean accept(File dir, String name){
					return name.endsWith(".xml");
				}
			});
		
		List<Layout> layouts = new ArrayList();
		if(layoutFiles != null){
			for(File lf : layoutFiles){
				//System.out.println("processing layout "+lf);
				layouts.add(processLayout(lf));
			}
		}
		return layouts;
	}

	private Layout processLayout(File layoutFile)
	{
		String layoutFileName = layoutFile.getName();
		layoutFileName = layoutFileName.substring(0, layoutFileName.length()-4); //drop ".xml"
		//System.out.println("++ "+layoutFileName);
		Integer id = publicXml.layoutIdFor(layoutFileName);
		
		Layout layout = new Layout(id, layoutFile.getName());

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

			findCallbacks(document, xpath, layout.callbacks);
			findWidgets(document, xpath, layout.widgets);

			return layout;
		}catch(Exception e){
			throw new Error(e);
		}
	}

	private void findWidgets(Document document, XPath xpath, List<Widget> widgets) throws javax.xml.xpath.XPathExpressionException
	{		
		NodeList nodes = (NodeList)
			xpath.evaluate("//*", document, XPathConstants.NODESET);
		//System.out.println("nodes.size() = "+nodes.getLength());
		for(int i = 0; i < nodes.getLength(); i++) {
			//System.out.println("HELLO");
			Element elem = (Element) nodes.item(i);
			//System.out.println("Widget: "+node.getNodeName());
			String id = elem.getAttribute("android:id");
			if(id.isEmpty())
				continue;//ignore for now. not sure whether we need to consider
			if(id.startsWith("@id/"))
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
	
	private void findCallbacks(Document document, XPath xpath, Set<String> callbacks) throws javax.xml.xpath.XPathExpressionException
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
						name = idToStringValue.get(name.substring("@string/".length()));
					}
					callbacks.add(name);
					System.out.println("Callback: "+name);
				}
				//System.out.println(n.getNodeName() + " " + );
			}
		}
	}
		
	private void mapIdToStringValue(File stringXmlFile)
	{
		if(!stringXmlFile.exists())
			return;
		try{
			File tmpFile = File.createTempFile("stamp_android_string_xml", null, null);
			tmpFile.deleteOnExit();
			UTF8ToAnsiUtils.main(new String[]{stringXmlFile.getAbsolutePath(), tmpFile.getAbsolutePath()});
			stringXmlFile = tmpFile;

			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Reader reader =
				new InputStreamReader(new FileInputStream(stringXmlFile), "Cp1252");
			Document document = builder.parse(new InputSource(reader));

			XPath xpath = XPathFactory.newInstance().newXPath();

			NodeList nodes = (NodeList)
				xpath.evaluate("/resources/string", document, XPathConstants.NODESET);
			//System.out.println("nodes.size() = "+nodes.getLength());
			for(int i = 0; i < nodes.getLength(); i++) {
				Element elem = (Element) nodes.item(i);
				//System.out.println("++++ "+node.getNodeName());
				String id = elem.getAttribute("name");
				String value = elem.getTextContent();
				idToStringValue.put(id, value);
				//System.out.println("## "+id+" "+value);
			}
		}catch(Exception e){
			throw new Error(e);
		}
	}
}
