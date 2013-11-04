package shord.analyses;

import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.AbstractJasminClass;

import java.io.File;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.jimple.internal.ImmediateBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JimpleLocalBox;
import soot.jimple.internal.VariableBox;
import soot.tagkit.LinkTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import java.util.*;
import stamp.analyses.ImplicitIntentDef;
import stamp.analyses.ReachingDefsAnalysis;
import stamp.analyses.iccg.*;
import stamp.harnessgen.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import shord.program.Program;

import soot.util.NumberedSet;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.analyses.ProgramDom;
import shord.project.ClassicProject;

import chord.project.Chord;

import java.util.jar.*;
import java.io.*;
import chord.util.Utils;
import chord.util.tuple.object.Pair;
import chord.util.tuple.object.Trio;
import chord.util.tuple.object.Quad;
import chord.project.OutDirUtils;


import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
  * Search for implicit intent.
  **/

/*
@Chord(name="post-java-iccg",
       //consumes={ "CallerComp", "CICM" },
       consumes={ "CallerComp", "CICM", "depComp" },
       namesOfTypes = { "M", "T", "C", "I"},
       types = { DomM.class, DomT.class, DomC.class, DomI.class},
       namesOfSigns = { "CalledComp", "CICM", "ICCG", "ICCGImp" },
       signs = { "M0,C0,T0:M0_C0_T0", "C0,I0,C1,M0:C0_I0_C1_M0", "T0,H0:T0_H0", "T0,T1:T0_T1" }
       )
*/
@Chord(name="post-java-iccg",
       consumes={ "CallerComp", "CICM", "depComp", "CT", "flow"}
       )

public class PostIccgBuilder extends JavaAnalysis
{

	private ProgramRel relCallerComp;
	private ProgramRel relCICM;

	private int maxArgs = -1;
	private FastHierarchy fh;
	public static NumberedSet stubMethods;

	public static final boolean ignoreStubs = false;

	private SootClass klass;

	//One ICCG per each app.
	private static ICCG iccg = new ICCG();

	Map<String,Set<String>> component2Meths = new HashMap<String, Set<String>>();

	private Map<String, XmlNode> components = new HashMap<String, XmlNode>();

    	public static List actionList = new ArrayList();

	private String pkgName = "";

	Map<String, Set> pMap = new HashMap<String, Set>();

	public PostIccgBuilder()
	{
		String stampOutDir = System.getProperty("stamp.out.dir");
		//parse manifest.xml
		String manifestDir = stampOutDir + "/apktool-out";
		File manifestFile = new File(manifestDir, "AndroidManifest.xml");
		//new ParseManifest().extractComponents(manifestFile, components);
		ParseManifest manifest = new ParseManifest();
		manifest.extractComponents(manifestFile, components);
		pkgName = manifest.getPkgName();

        /*

		//plot every node to graph + unknown.
		Iterator iter = components.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String)entry.getKey();
			XmlNode val = (XmlNode)entry.getValue();
			String nodeName = val.getName();
			System.out.println("nodeinfo:" + val.getName() +" "+ val.getType()+" " + val.getPermission()+ " " 
				+ val.getMain() + val.getIntentFilter());

			if(nodeName.indexOf(".") == 0) nodeName = pkgName +  nodeName;
			if(nodeName.indexOf(".") == -1) nodeName = pkgName + "." +  nodeName;

			ICCGNode iNode = new ICCGNode(nodeName);
			iNode.setMain(val.getMain());
			iNode.setType(val.getType());
			iNode.setIntentFilter(val.getIntentFilter());

			if (val.getMain()) {
				iNode.setShape("diamond");
			} else if("activity".equals(val.getType())) {
				iNode.setShape("ellipse");
			} else if("service".equals(val.getType())) {
				iNode.setShape("box");
			} else if("receiver".equals(val.getType())) {
				iNode.setShape("box");
			}
			iccg.addNode(iNode);
		}

		//create unknown node.
		ICCGNode unknownNode = new ICCGNode("unknown");
		unknownNode.setShape("box");
		iccg.addNode(unknownNode);

		ICCGNode notFoundNode = new ICCGNode("targetNotFound");
		notFoundNode.setShape("box");
		iccg.addNode(notFoundNode);
        */

	}

   	void genPermission(ICCG ig)
	{
		relCallerComp = (ProgramRel) ClassicProject.g().getTrgt("CallerComp");
		relCallerComp.load();

		Iterable<Trio<SootMethod,Ctxt,RefType>> res1 = relCallerComp.getAry3ValTuples();
		for(Trio<SootMethod,Ctxt,RefType> pair : res1) {
			SootMethod mm = pair.val0;
			RefType cc = pair.val2;
			Set<String> hs = component2Meths.get(cc.getClassName());
			if(hs == null) {
				hs = new HashSet<String>();
				component2Meths.put(cc.getClassName(), hs);
			}
			hs.add(mm.getSignature());
			//add permission.
			ICCGNode iNode = ig.getNode(cc.getClassName());
			if(iNode!=null) {
				if(pMap.get(mm.getSignature()) != null) {
					//do union.
					iNode.setPermission(pMap.get(mm.getSignature()));
				}
			}

		}

		relCallerComp.close();
	}

    //locate extraneous components.
   	void findExtra(ICCG ig)
	{
		ProgramRel relConjunctSet = (ProgramRel) ClassicProject.g().getTrgt("ConjunctSet");
		relConjunctSet.load();

        Set<String> mainSet = new HashSet<String>();
        Set<String> reachSet = new HashSet<String>();
        Iterator iter = ICCGBuilder.components.entrySet().iterator();
        String pkgName = ICCGBuilder.pkgName;
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String)entry.getKey();
            XmlNode val = (XmlNode)entry.getValue();
            String nodeName = val.getName();
            if(nodeName.indexOf(".") == 0) nodeName = pkgName +  nodeName;
            if(nodeName.indexOf(".") == -1) nodeName = pkgName + "." +  nodeName;
            if(val.getMain()) mainSet.add(nodeName);
        }

        //First, pick up the main components from AndroidManifest.xml.
		Iterable<Trio<RefType,RefType,RefType>> res = relConjunctSet.getAry3ValTuples();
		for(Trio<RefType, RefType, RefType> trio: res) {
            //Second,
			RefType src = trio.val0;
			RefType tgt = trio.val1;
            String srcStr = src.getClassName();
            String tgtStr = tgt.getClassName();
            if(mainSet.contains(srcStr)){
                reachSet.add(tgtStr);
            }
           /* else if(mainSet.contains(tgtStr)) {
                reachSet.add(srcStr);
            }*/
		}
        mainSet.addAll(reachSet);

    	ProgramRel relICCG = (ProgramRel) ClassicProject.g().getTrgt("ICCG");
		relICCG.load();

		Iterable<Pair<RefType,RefType>> reachRes = relICCG.getAry2ValTuples();
		for(Pair<RefType, RefType> pair: reachRes) {
			RefType src = pair.val0;
			RefType tgt = pair.val1;
            String srcStr = src.getClassName();
            String tgtStr = tgt.getClassName();
            if(!mainSet.contains(srcStr) && mainSet.contains(tgtStr)){
                mainSet.remove(tgtStr);
            }
		}
    


        System.out.println("MainSet....." + mainSet);
        for(String mainNode : mainSet){
            ICCGNode mNode = ig.getNode(mainNode);
            mNode.setMain(true);
        }

		relICCG.close();
		relConjunctSet.close();
	}




    //dump out ICCG.
    public void dump() {
	    ICCG iccg = new ICCG();
        int cnt = 1;

        Iterator iter = components.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String)entry.getKey();
			XmlNode val = (XmlNode)entry.getValue();
			String nodeName = val.getName();

			if(nodeName.indexOf(".") == 0) nodeName = pkgName +  nodeName;
			if(nodeName.indexOf(".") == -1) nodeName = pkgName + "." +  nodeName;

			ICCGNode iNode = new ICCGNode(nodeName, cnt++);
			iNode.setMain(val.getMain());
			iNode.setType(val.getType());
			iNode.setIntentFilter(val.getIntentFilter());

			if (val.getMain()) {
				iNode.setShape("diamond");
			} else if("activity".equals(val.getType())) {
				iNode.setShape("ellipse");
			} else if("service".equals(val.getType())) {
				iNode.setShape("box");
			} else if("receiver".equals(val.getType())) {
				iNode.setShape("box");
			}
			iccg.addNode(iNode);
		}

        ProgramRel relICCG = (ProgramRel) ClassicProject.g().getTrgt("ICCG");
		relICCG.load();
        //ProgramRel relICCGImp = (ProgramRel) ClassicProject.g().getTrgt("ICCGImp");
		//relICCGImp.load();

		/*Iterable<Pair<Object,Object>> res1 = relICCG.getAry2ValTuples();
		for(Pair<Object,Object> pair : res1) {
            if(!(pair.val1 instanceof StringConstNode)) continue;
			RefType t  = (RefType)pair.val0;
			StringConstNode s = (StringConstNode)pair.val1;
	        ICCGEdge iccgEdge = new ICCGEdge();
            ICCGNode srcNode = iccg.getNode(t.getClassName());
            ICCGNode tgtNode = iccg.getNode(s.getValue());

            iccgEdge.setTgt(tgtNode);
            iccgEdge.setSrc(srcNode);
            iccg.addEdge(iccgEdge);

		}*/

		Iterable<Pair<Object,Object>> res2 = relICCG.getAry2ValTuples();
		for(Pair<Object,Object> pair : res2) {
			RefType t  = (RefType)pair.val0;
			RefType s = (RefType)pair.val1;
	        ICCGEdge iccgEdge = new ICCGEdge();
            ICCGNode srcNode = iccg.getNode(t.getClassName());
            ICCGNode tgtNode = iccg.getNode(s.getClassName());

            iccgEdge.setTgt(tgtNode);
            iccgEdge.setSrc(srcNode);
            iccg.addEdge(iccgEdge);
			iccgEdge.setAsynchronous(true);
		}


		relICCG.close();
		//relICCGImp.close();
        findExtra(iccg);
        genPermission(iccg);
        plotFlow2Comp(iccg);
		//System.out.println(iccg.getSignature());
        PrintWriter out = OutDirUtils.newPrintWriter(dotFilePath);
        out.println(iccg.getSignature());
        out.close();
    }

    private Map<String, Integer> depMap = new HashMap<String,Integer>();

    public void compDepend() 
    {
        ProgramRel relDepComp = (ProgramRel) ClassicProject.g().getTrgt("depComp");
		relDepComp.load();

        //k1:varComp
        Iterable<Quad<Object,Object,Object,Object>> varComp = relDepComp.getAry4ValTuples();
        for(Quad<Object,Object, Object, Object> quad : varComp) {
            RefType s = (RefType)quad.val0;
			String srckey = s.getClassName(); 
            RefType t = (RefType)quad.val3;
			String tgtkey = t.getClassName();
            String key = srckey+"@"+tgtkey;
            String revkey = tgtkey+"@"+srckey;
            if( (depMap.get(key) == null) && (depMap.get(revkey) == null) ){
            //if( (depMap.get(key) == null) ){
                depMap.put(key, 1);
            } else {
                if(depMap.get(key) != null) depMap.put(key, depMap.get(key)+1);
                if(depMap.get(revkey) != null) depMap.put(revkey, depMap.get(revkey)+1);
                //depMap.put(key, depMap.get(key)+1);
            }

        }

		relDepComp.close();
    }

	public void run()
	{
	    //output dotty.
		parsePermission();
        dump();
        compDepend();
        //genGraph();
	}
    
    public void genGraph() 
    {
      try {
 
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("graphml");
        rootElement.setAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns");

        //rootElement.add( new Namespace( "xmlns", "http://graphml.graphdrawing.org/xmlns" ) );
        doc.appendChild(rootElement);
 
        // graph elements
        Element graph = doc.createElement("graph");
        rootElement.appendChild(graph);
 
        // set attribute to graph element
        Attr attr = doc.createAttribute("id");
        attr.setValue("G");
        graph.setAttributeNode(attr);

        Attr direct = doc.createAttribute("edgedefault");
        direct.setValue("directed");
        graph.setAttributeNode(direct);

        //<key id="weight" for="edge" attr.name="weight" attr.type="int"/>
        Element keyw= doc.createElement("key");
        Attr wAttr = doc.createAttribute("id");
        wAttr.setValue("weight");
        keyw.setAttributeNode(wAttr);

        Attr forAttr = doc.createAttribute("for");
        forAttr.setValue("edge");
        keyw.setAttributeNode(forAttr);

        Attr nAttr = doc.createAttribute("attr.name");
        nAttr.setValue("weight");
        keyw.setAttributeNode(nAttr);

        Attr tAttr = doc.createAttribute("attr.type");
        tAttr.setValue("int");
        keyw.setAttributeNode(tAttr);

        graph.appendChild(keyw);

        //nodes.
        Iterator itSrc = components.entrySet().iterator();
		while (itSrc.hasNext()) {//k1: src
			Map.Entry srcEntry = (Map.Entry) itSrc.next();
			String srckey = (String)srcEntry.getKey();

            Element node = doc.createElement("node");
            Attr nodeAttr = doc.createAttribute("id");
            nodeAttr.setValue(parseStr(srckey));
            node.setAttributeNode(nodeAttr);

            Attr nodeLabAttr = doc.createAttribute("label");
            nodeLabAttr.setValue(parseStr(srckey));
            node.setAttributeNode(nodeLabAttr);

            graph.appendChild(node);
     
        }
        //edges.
	    Iterator itEdge = depMap.entrySet().iterator();
        int cnt = 1;
        while (itEdge.hasNext()) {//k1: src
			Map.Entry e = (Map.Entry) itEdge.next();
			String eKey = (String)e.getKey();
			int val = (Integer)e.getValue();
            String[] sa = eKey.split("@");
            //System.out.println(srckey.replace("$", "-->") + " Weight=" +val);

            Element edge = doc.createElement("edge");
            Attr idAttr = doc.createAttribute("id");
            idAttr.setValue("E"+String.valueOf(cnt));
            edge.setAttributeNode(idAttr);

            Attr srcAttr = doc.createAttribute("source");
            srcAttr.setValue(parseStr(sa[0]));
            edge.setAttributeNode(srcAttr);

            Attr tgtAttr = doc.createAttribute("target");
            tgtAttr.setValue(parseStr(sa[1]));
            edge.setAttributeNode(tgtAttr);

            Attr wtAttr = doc.createAttribute("weight");
            wtAttr.setValue(String.valueOf(val));
            edge.setAttributeNode(wtAttr);


            Element w = doc.createElement("data");
            Attr dataAttr = doc.createAttribute("key");
            dataAttr.setValue("weight");
            w.setAttributeNode(dataAttr);
            w.appendChild(doc.createTextNode(String.valueOf(val)));
            edge.appendChild(w);
 
            graph.appendChild(edge);
            cnt++;

        }
 
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(reportsFilePath));
        transformer.transform(source, result);
 
      } catch (ParserConfigurationException pce) {
        pce.printStackTrace();
      } catch (TransformerException tfe) {
        tfe.printStackTrace();
      }
    }

	public static final String reportsFilePath = System.getProperty("stamp.out.dir")+"/datadep.graphml";
	public static final String dotFilePath = "/iccg.dot";

    /* Read the permission into a map.*/
	void parsePermission() {
		String loc = System.getProperty("stamp.dir") + "/models/permissionmap401.txt";
		try {
			FileReader fileIn = new FileReader(loc);
			LineNumberReader in = new LineNumberReader(fileIn);
			String line = in.readLine();
			String currPer = line.substring(line.indexOf(":") + 1, line.length());
			do {
				if(line.contains("Permission:")) {
					String key = line.substring(line.indexOf(":") + 1, line.length());
					currPer = key;
				} else {
                    if(line.contains("Callers")) line = in.readLine();//filter out the title.
					String methSig = line.substring(0, line.indexOf(")>")+2 );
                    assert(methSig.length() > 2);
					//System.out.println(methSig);
					if (pMap.get(methSig) == null) {
						Set<String> newperSet = new HashSet<String>(); 
						newperSet.add(currPer);
						pMap.put(methSig, newperSet);
					} else {
						pMap.get(methSig).add(currPer);
					}
				}
				line = in.readLine();
			} while (line != null);
			in.close();
		}catch(Exception e){
			e.printStackTrace();

		}

	}

    
    private void plotFlow2Comp(ICCG ig)
    {
        final ProgramRel relCtxtFlows = (ProgramRel)ClassicProject.g().getTrgt("flow");
        final ProgramRel relCT = (ProgramRel)ClassicProject.g().getTrgt("CT");

        relCtxtFlows.load();
        relCT.load();
        Set<String> flowSet = new HashSet<String>();
        Iterable<Pair<Pair<String,Ctxt>,Pair<String,Ctxt>>> res = relCtxtFlows.getAry2ValTuples();
        for(Pair<Pair<String,Ctxt>,Pair<String,Ctxt>> pair : res) {
            String source = pair.val0.val0;
            Ctxt srcCtxt = pair.val0.val1;
            String sink = pair.val1.val0;
            Ctxt sinkCtxt = pair.val1.val1;
            //we should consider webview.
            if(source.contains("getExtras") || sink.contains("Activity") 
              || sink.contains("Broadcast") || sink.contains("LOG")) continue;

            //for each end-2-end flow.
            Iterable<Pair<Ctxt,Type>> resCT = relCT.getAry2ValTuples();
            for(Pair<Ctxt,Type> ctPair : resCT) {
                //assume that src&sink are in the same component.
                Ctxt ctxt = ctPair.val0;
                RefType comp = (RefType)ctPair.val1;
                //if(ctxt.equals(srcCtxt) || ctxt.equals(sinkCtxt))
                if(ctxt.equals(sinkCtxt))
                    flowSet.add(comp.getClassName()+"@"+source+"@"+sink);

            }
        
         }

        ig.setFlow(flowSet);
	    relCT.close();
	    relCtxtFlows.close();

    }

    private String parseStr(String str)
    {
        str = str.replaceAll("\\/", ".");
        if(str.contains("."))
            str= str.substring(str.lastIndexOf(".")+1,str.length());

        return str;
    }


}
