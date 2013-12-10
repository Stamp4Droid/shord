package com.apposcopy.obfuscate;

import java.util.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;

public class FixManifest
{
	private Document document;
	private String pkgName; 
	private XPath xpath;
	private Mapper mapper;
	private Set<String> actionStrings = new HashSet();

	FixManifest(String mappingFile)
	{
		this.mapper = new Mapper(mappingFile);
	}

	Set<String> getActionStrings()
	{
		return actionStrings;
	}

	void fixManifest(String manifestFile) 
	{
		try{
			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			this.document = builder.parse(manifestFile);
			
			this.xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new PersonalNamespaceContext());
		
			//find package name
			Node node = (Node)
				xpath.evaluate("/manifest", document, XPathConstants.NODE);
			this.pkgName = node.getAttributes().getNamedItem("package").getNodeValue();

			for(String compType : new String[]{"activity", "service", "receiver"}){
				readActionStrings(compType);
			}

			for(String compType : new String[]{"activity", "service", "receiver"}){
				fixCompNames(compType);
			}
			
			writeToFile(manifestFile);
		}catch(Exception e){
			throw new Error(e);
		}
	}

	private void readActionStrings(String componentType) throws Exception
	{
		NodeList nodes = (NodeList)
			xpath.evaluate("/manifest/application/"+componentType+"/intent-filter/action", document, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			NamedNodeMap nnm = node.getAttributes();
			String name = null;
			for(int j = 0; j < nnm.getLength(); j++){
				Node n = nnm.item(j);
				if(n.getNodeName().equals("android:name")){
					name = n.getNodeValue();
					actionStrings.add(name);
					break;
				}
			}
		}
	}

	private String fixName(String comp)
	{
		if(comp.startsWith("."))
			comp = pkgName + comp;
		else if(comp.indexOf('.') < 0)
			comp = pkgName + "." + comp;
		comp = mapper.oldToNewClassName.get(comp);
		return comp;
	}

	private void fixCompNames(String componentType) throws Exception
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
					n.setNodeValue(fixName(name));
					break;
				}
				//System.out.println(n.getNodeName() + " " + );
			}			
			assert name != null : node.getNodeName();
		}
	}

	void writeToFile(String fileName)
	{
		Source source = new DOMSource(document);
		Result result = new StreamResult(new File(fileName));
		Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			throw new Error(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new Error(e);
		} catch (TransformerException e) {
			throw new Error(e);
		}
	}

	public static void main(String[] args)
	{
		String mappingFile = args[0];
		String manifestFile = args[1];
		
		FixManifest fm = new FixManifest(mappingFile);
		fm.fixManifest(manifestFile);
	}

	/*
	private static void fixManifest(Mapper mapper, String manifestFile) throws Exception
	{
		File tmpFile = File.createTempFile("stamp_android_manifest", null, null);
		tmpFile.deleteOnExit();
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(tmpFile)));

		BufferedReader reader = new BufferedReader(new FileReader(manifestFile));
		String line;
		while((line = reader.readLine()) != null){
			line = process(line);
			writer.println(line);
		}
		reader.close();
		writer.close();
		
		copy(tmpFile, new File(manifestFile));
	}


	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

	public static void copy(File srcFile, File dstFile) throws IOException
	{
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new BufferedInputStream(new FileInputStream(srcFile), DEFAULT_BUFFER_SIZE);
			output = new BufferedOutputStream(new FileOutputStream(dstFile), DEFAULT_BUFFER_SIZE);
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			for (int length = 0; ((length = input.read(buffer)) > 0);) {
				output.write(buffer, 0, length);
			}
		} finally {
			if (output != null) try { output.close(); } catch (IOException e) { throw e; }
			if (input != null) try { input.close(); } catch (IOException e) { throw e; }
		}
	}
	*/

}

class PersonalNamespaceContext implements NamespaceContext 
{
	public String getNamespaceURI(String prefix) {
		if (prefix == null) throw new NullPointerException("Null prefix");
		else if ("android".equals(prefix)) return "http://schemas.android.com/apk/res/android";
		else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
			return XMLConstants.NULL_NS_URI;
	}
	
	// This method isn't necessary for XPath processing.
	public String getPrefix(String uri) {
		throw new UnsupportedOperationException();
	}
	
	// This method isn't necessary for XPath processing either.
	public Iterator getPrefixes(String uri) {
		throw new UnsupportedOperationException();
	}
	
}
