package stamp.missingmodels.util.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * A standard way of printing out XML files.
 * 
 * @author Osbert Bastani
 */
public abstract class XMLObject implements Serializable {
	private static final long serialVersionUID = -6914620814798273359L;
	
	private final String name;
	private Map<String,String> attributes = new HashMap<String,String>();
	private boolean hasBody;

	public XMLObject(String name, boolean hasBody) {
		this.name = name;
		this.hasBody = hasBody;
	}

	public void putAttribute(String key, String value) {
		this.attributes.put(key, value);
	}
	
	public abstract String getInnerXML(int tabs);
	
	@Override
	public String toString() {
		return this.toString(0);
	}

	public String toString(int tabs) {
		StringBuilder sb = new StringBuilder();

		// initial tabs
		for(int i=0; i<tabs; i++) {
			sb.append("\t");
		}
		
		// build the header
		sb.append("<" + this.name + " ");
		for(Map.Entry<String,String> entry : this.attributes.entrySet()) {
			sb.append(entry.getKey() + "=\"" + entry.getValue() + "\" ");
		}

		// close and build the body and footer
		if(this.hasBody) {
			// build the body
			sb.append(">\n" + this.getInnerXML(tabs+1) + "\n");
			
			// ending tabs
			for(int i=0; i<tabs; i++) {
				sb.append("\t");
			}
			
			// build the ending
			sb.append("</" + this.name + ">\n");
		} else {
			sb.append("/>\n");
		}
		return sb.toString();
	}
	
	public static class XMLContainerObject extends XMLObject {
		private List<XMLObject> children = new ArrayList<XMLObject>();
		
		public XMLContainerObject(String name) {
			super(name, true);
		}

		public void addChild(XMLObject child) {
			this.children.add(child);
		}

		@Override
		public String getInnerXML(int tabs) {
			StringBuilder sb = new StringBuilder();
			for(XMLObject child : this.children) {
				sb.append(child.toString(tabs));
			}
			return sb.toString();
		}
	}
	
	public static class XMLTextObject extends XMLObject {
		private String innerXML = null;
		
		public XMLTextObject(String name) {
			super(name, true);
		}

		public void setInnerXML(String innerXML) {
			this.innerXML = innerXML;
		}

		@Override
		public String getInnerXML(int tabs) {
			StringBuilder sb = new StringBuilder();
			
			// tabs
			for(int i=0; i<tabs; i++) {
				sb.append("\t");
			}
			
			// text
			sb.append(this.innerXML);
			
			return sb.toString();
		}
	}
	
	public static class XMLEmptyObject extends XMLObject {
		public XMLEmptyObject(String name) {
			super(name, false);
		}

		@Override
		public String getInnerXML(int tabs) {
			return "";
		}
	}
}
