package stamp.app;

import javax.xml.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;
import java.util.*;

public class PublicXml
{
	public static class Entry
	{
		public final Integer id;
		public final String name;
		public final String type;
		
		private Entry(Integer id, String name, String type)
		{
			this.id = id;
			this.name = name;
			this.type = type;
		}
	}

	private final List<Entry> entries = new ArrayList();

	public PublicXml(File publicXmlFile)
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
				xpath.evaluate("/resources/public", document, XPathConstants.NODESET);
			//System.out.println("nodes.size() = "+nodes.getLength());
			for(int i = 0; i < nodes.getLength(); i++) {
				Element elem = (Element) nodes.item(i);
				String name = elem.getAttribute("name");
				Integer id = Integer.decode(elem.getAttribute("id"));
				String type = elem.getAttribute("type");
				Entry entry = new Entry(id, name, type);
				entries.add(entry);
				//layoutToId.put(layout, id);
				//System.out.println("## "+layout+" "+id);
			}
		}catch(Exception e){
			throw new Error(e);
		}		
	}
	
	public Entry entryFor(Integer id)
	{
		for(Entry e : entries)
			if(e.id.equals(id))
				return e;
		return null;
	}

	public Entry entryFor(String name)
	{
		for(Entry e : entries)
			if(e.name.equals(name))
				return e;
		return null;
	}
	
}