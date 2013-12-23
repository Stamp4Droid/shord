package stamp.harnessgen;

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
	private Map<String,Integer> layoutToId = new HashMap();

	List<Layout> process(File resDir)
	{
		mapLayoutToId(new File(resDir, "values/public.xml"));

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
		System.out.println("++ "+layoutFileName);
		Integer id = layoutToId.get(layoutFileName);
		
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
			findWidgets(document, xpath, layout.customWidgets);

			return layout;
		}catch(Exception e){
			throw new Error(e);
		}
	}

	private void findWidgets(Document document, XPath xpath, Set<String> widgets) throws javax.xml.xpath.XPathExpressionException
	{		
		NodeList nodes = (NodeList)
			xpath.evaluate("//*", document, XPathConstants.NODESET);
		//System.out.println("nodes.size() = "+nodes.getLength());
		for(int i = 0; i < nodes.getLength(); i++) {
			//System.out.println("HELLO");
			Node node = nodes.item(i);
			//System.out.println("Widget: "+node.getNodeName());
			widgets.add(node.getNodeName());
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
					callbacks.add(n.getNodeValue());
					System.out.println("Callback: "+n.getNodeValue());
				}
				//System.out.println(n.getNodeName() + " " + );
			}
		}
	}
	
	private void mapLayoutToId(File publicXmlFile)
	{
		try{
			File tmpFile = File.createTempFile("stamp_android_public_xml", null, null);
			tmpFile.deleteOnExit();
			UTF8ToAnsiUtils.main(new String[]{publicXmlFile.getAbsolutePath(), tmpFile.getAbsolutePath()});
			publicXmlFile = tmpFile;

			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Reader reader =
				new InputStreamReader(new FileInputStream(publicXmlFile), "Cp1252");
			Document document = builder.parse(new InputSource(reader));

			XPath xpath = XPathFactory.newInstance().newXPath();

			NodeList nodes = (NodeList)
				xpath.evaluate("/resources/public[@type=\"layout\"]", document, XPathConstants.NODESET);
			//System.out.println("nodes.size() = "+nodes.getLength());
			for(int i = 0; i < nodes.getLength(); i++) {
				//System.out.println("HELLO");
				Element elem = (Element) nodes.item(i);
				//System.out.println("++++ "+node.getNodeName());
				String layout = elem.getAttribute("name");
				Integer id = Integer.decode(elem.getAttribute("id"));
				layoutToId.put(layout, id);
				//System.out.println("## "+layout+" "+id);
			}
		}catch(Exception e){
			throw new Error(e);
		}		
	}
}
