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
	static void process(File layoutFile, Set<String> callbacks)
	{
		try{
			File tmpFile = File.createTempFile("stamp_android_layout", null, null);
			tmpFile.deleteOnExit();
			UTF8ToAnsiUtils.main(new String[]{layoutFile.getAbsolutePath(), tmpFile.getAbsolutePath()});
			layoutFile = tmpFile;

			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(layoutFile);
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new PersonalNamespaceContext());
			
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
						System.out.println("CALLBACK: "+n.getNodeValue());
					}
					//System.out.println(n.getNodeName() + " " + );
				}
			}
		}catch(Exception e){
			throw new Error(e);
		}
	}
}