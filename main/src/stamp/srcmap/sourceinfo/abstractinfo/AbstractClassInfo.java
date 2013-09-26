package stamp.srcmap.sourceinfo.abstractinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import stamp.srcmap.sourceinfo.ClassInfo;
import stamp.srcmap.sourceinfo.MethodInfo;
import stamp.srcmap.sourceinfo.javainfo.JavaMethodInfo;

/**
 * @author Saswat Anand 
 */
public class AbstractClassInfo implements ClassInfo {

	@Override
	public int lineNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public MethodInfo methodInfo(String chordSig) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int lineNum(String chordMethSig) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> aliasSigs(String chordMethSig) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<String>> allAliasSigs() {
		// TODO Auto-generated method stub
		return null;
	}

}