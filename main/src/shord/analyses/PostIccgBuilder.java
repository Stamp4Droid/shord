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

@Chord(name="post-java-iccg",
       consumes={ "CallerComp", "flow"}
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

	public static final String dotFilePath = "/iccg.dot";

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

   	}

   	void genPermission(ICCG ig)
	{
		relCallerComp = (ProgramRel) ClassicProject.g().getTrgt("CallerComp");
		relCallerComp.load();

		Iterable<Trio<SootMethod,Ctxt,String>> res1 = relCallerComp.getAry3ValTuples();
		for(Trio<SootMethod,Ctxt,String> pair : res1) {
			SootMethod mm = pair.val0;
			String cc = pair.val2;
			Set<String> hs = component2Meths.get(cc);
			if(hs == null) {
				hs = new HashSet<String>();
				component2Meths.put(cc, hs);
			}
			hs.add(mm.getSignature());
			//add permission.
			ICCGNode iNode = ig.getNode(cc);
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
    public void dump() 
    {
	    ICCG iccg = new ICCG();
		String appName = System.getProperty("stamp.app");
        iccg.setAppName(appName);
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

		Iterable<Pair<String,String>> res2 = relICCG.getAry2ValTuples();
		for(Pair<String,String> pair : res2) {
			String s  = pair.val0;
			String t = pair.val1;
	        ICCGEdge iccgEdge = new ICCGEdge();
            ICCGNode srcNode = iccg.getNode(s);
            ICCGNode tgtNode = iccg.getNode(t);

            iccgEdge.setTgt(tgtNode);
            iccgEdge.setSrc(srcNode);
            iccg.addEdge(iccgEdge);
			iccgEdge.setAsynchronous(true);
		}


		relICCG.close();
        //findExtra(iccg);
        genPermission(iccg);
        getSpecCaller(iccg);
        getIntentFilter(iccg);
        //plotFlow2Comp(iccg);
        PrintWriter out = OutDirUtils.newPrintWriter(dotFilePath);
        out.println(iccg.getSignature());
        out.close();
        //store in DB.
        iccg.updateDB();
    }


	public void run()
	{
	    //output dotty.
		parsePermission();
        dump();
	}
    

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

    //e.g. abortBroadcast()
    private void getSpecCaller(ICCG ig)
    {
        final ProgramRel relSpecCaller = (ProgramRel)ClassicProject.g().getTrgt("SpecCallerComp");
        relSpecCaller.load();
        Iterable<Pair<SootMethod,String>> res = relSpecCaller.getAry2ValTuples();
        Set<String> specCallSet = new HashSet<String>();
        for(Pair<SootMethod,String> pair : res) {
            String meth = pair.val0.getSignature();
            String comp = pair.val1;
            specCallSet.add(comp+"@"+meth);
        }
        ig.setSpecCall(specCallSet);
        relSpecCaller.close();
    }

    
    private void getIntentFilter(ICCG ig)
    {
        Set<String> filterList = new HashSet<String>();
		String pkgName = ICCGBuilder.pkgName;
		Collection compNodes = ICCGBuilder.components.values();
        for(Object node : compNodes){
            XmlNode comp = (XmlNode) node;
            String nodeName = comp.getName();

            if(nodeName.indexOf(".") == 0) nodeName = pkgName +  nodeName;
            if(nodeName.indexOf(".") == -1) nodeName = pkgName + "." +  nodeName;

            //pick up the maximum priority.
            int max = 0;
            String action = "";
            for(String priority : comp.getFilterList()){
                int p = Integer.parseInt(priority);
                if(p > max) max = p;
            }

            //union all the action strings.
            for(String actionName : comp.getActionList()){
                action += actionName;
            }

            filterList.add(nodeName + "@" + action + "@" + String.valueOf(max));

        }

        System.out.println("Filterlist: " + filterList);
        ig.setFilterList(filterList);
    }

    private void plotFlow2Comp(ICCG ig)
    {
        final ProgramRel relCompFlows = (ProgramRel)ClassicProject.g().getTrgt("FlowComp");
        Set<String> cpFlowSet = new HashSet<String>();
        relCompFlows.load();
        Iterable<Quad<Type,Pair<String,Ctxt>,Type,Pair<String,Ctxt>>> cflows = relCompFlows.getAry4ValTuples();
        for(Quad<Type,Pair<String,Ctxt>,Type,Pair<String,Ctxt>> quad : cflows) {
            String srcComp = ((RefType)quad.val0).getClassName();
            String srctxt = quad.val1.val0;
            String tgtComp = ((RefType)quad.val2).getClassName();
            String sinktxt = quad.val3.val0;
            cpFlowSet.add(srcComp + "@" + srctxt + "@" + tgtComp + "@"+ sinktxt);
        }
        ig.setFlows(cpFlowSet);
        relCompFlows.close();
    }

    private String parseStr(String str)
    {
        str = str.replaceAll("\\/", ".");
        if(str.contains("."))
            str= str.substring(str.lastIndexOf(".")+1,str.length());

        return str;
    }


}
