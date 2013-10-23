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
       consumes={ "CallerComp", "CICM", "depComp" },
       namesOfTypes = { "M", "T", "C", "I"},
       types = { DomM.class, DomT.class, DomC.class, DomI.class},
       namesOfSigns = { "CalledComp", "CICM", "ICCG", "ICCGImp" },
       signs = { "M0,C0,T0:M0_C0_T0", "C0,I0,C1,M0:C0_I0_C1_M0", "T0,H0:T0_H0", "T0,T1:T0_T1" }
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
					String methSig = line.substring(0, line.indexOf(")>")+2 );
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


	void openRels()
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
			ICCGNode iNode = iccg.getNode(cc.getClassName());
			if(iNode!=null) {
				if(pMap.get(mm.getSignature()) != null) {
					//do union.
					iNode.setPermission(pMap.get(mm.getSignature()));
				}
			}

		}

		relCallerComp.close();
	}

	protected void visit(SootClass klass)
	{
		this.klass = klass;
		Collection<SootMethod> methodsCopy = new ArrayList(klass.getMethods());
		for(SootMethod method : methodsCopy)
			visitMethod(method);
	}

	private void visitMethod(SootMethod method)
	{
	if(!method.isConcrete()) return;

	Body body = method.retrieveActiveBody();
	UnitGraph g = new ExceptionalUnitGraph(body);
	ImplicitIntentDef sld = new ImplicitIntentDef(g, new SimpleLiveLocals(g));
	//Running transitive reaching def to grep the values of intent filter.
	ReachingDefsAnalysis.runReachingDef(body);
	ICCGNode srcNode = new ICCGNode();
	ICCGNode tgtNode = new ICCGNode();
	ICCGEdge iccgEdge = new ICCGEdge();

	Chain<Local> locals = body.getLocals();
	Chain<Unit> units = body.getUnits();
	Iterator<Unit> uit = units.snapshotIterator();
	while(uit.hasNext()){
	    Stmt stmt = (Stmt) uit.next();

	    if(stmt.containsInvokeExpr()){
		InvokeExpr ie = stmt.getInvokeExpr();
		String methSig = ie.getMethod().getSignature();
		//Entry point. new intent()...
		///explicit target?
		if (
		    methSig.equals("<android.content.Intent: void <init>(java.lang.String,android.net.Uri,android.content.Context,java.lang.Class)>")
		||  methSig.equals("<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>")
		||  methSig.equals("<android.content.Intent: android.content.Intent setClass(android.content.Context,java.lang.Class)>")
		||  methSig.equals("<android.content.Intent: android.content.Intent setClassName(android.content.Context,java.lang.String)>")
		||  methSig.equals("<android.content.Intent: android.content.Intent setClassName(java.lang.String,java.lang.String)>")
		||  methSig.equals("<android.content.ComponentName: void <init>(java.lang.String,java.lang.String)>")
		){
		    ArrayList<String> keyList = new ArrayList<String>(); 
		    String tgtName = "";
		    if (methSig.equals("<android.content.Intent: void <init>(java.lang.String,android.net.Uri,android.content.Context,java.lang.Class)>"))
			keyList = readKeysFromTag(stmt, ie.getArg(3));
		    else 
			keyList = readKeysFromTag(stmt, ie.getArg(1));

		    if(keyList.size() > 0) {
			tgtName = keyList.get(0);
			if(tgtName.contains("/")) tgtName = tgtName.replaceAll("/", ".");
			if(!tgtName.contains(".")) tgtName = pkgName + "." + tgtName;
		    } else {
			tgtName = "targetNotFound";
			System.out.println("targetnotfound:" + stmt + " " + stmt.getTags() + method + this.klass);
			//can we do better?
		    }

		    if(components.get(method.getDeclaringClass().getName()) == null) {
			//any other component can reach this method? 
			Iterator iter = component2Meths.entrySet().iterator();
			while (iter.hasNext()) { 
			    if(!klass.getName().contains(pkgName)) break;
			    Map.Entry entry = (Map.Entry) iter.next(); 
			    String key = (String)entry.getKey(); 
			    Set val = (Set)entry.getValue(); 
			    if(val.contains(method.getSignature())) {

				ICCGNode srcNode1 = iccg.getNode(key);
				ICCGEdge iccgEdge2 = new ICCGEdge();
				ICCGNode myTgt1 = iccg.getNode(tgtName);
				iccgEdge2.setTgt(myTgt1);
				iccgEdge2.setAsynchronous(true);
				String subSig = method.getSubSignature();
				iccgEdge2.setEvent(subSig.substring(subSig.indexOf(" "), subSig.indexOf("(")));
				iccgEdge2.setSrc(srcNode1);
				iccg.addEdge(iccgEdge2);
			    }
			} 
			
		    } else {
			ICCGEdge iccgEdge1 = new ICCGEdge();
			srcNode = iccg.getNode(klass.getName());
			ICCGNode myTgt = iccg.getNode(tgtName);

			iccgEdge1.setTgt(myTgt);
			iccgEdge1.setSrc(srcNode);
			iccg.addEdge(iccgEdge1);
		    }
		    continue;

		} else {//may be implicit
		   tgtNode = iccg.getUnknown();  
		}

		//Intent filter info, if any.
		///param values for setAction, setCategory and those related APIs.
		if (
		    methSig.equals("<android.content.Intent: void <init>(java.lang.String,android.net.Uri,android.content.Context,java.lang.Class)>")
		    || methSig.equals("<android.content.Intent: android.content.Intent setAction(java.lang.String)>")
		    || methSig.equals("<android.content.Intent: void <init>(java.lang.String,android.net.Uri)>")
		    || methSig.equals("<android.content.Intent: void <init>(java.lang.String)>")
		    ) {
		    //Output intent filter, target info.
		    System.out.println("Intent filter(action) = " + " Class:" + this.klass + " Method:" + method 
					+ " Stmt:" + stmt + " Defs:" + stmt.getTags() + "\n");

		    ArrayList<String> keyList = readKeysFromTag(stmt, ie.getArg(0));
		    if(keyList.size() > 0) iccgEdge.setAction(keyList.get(0));
		   tgtNode = iccg.getUnknown();  
		}

		if (methSig.equals("<android.content.Intent: android.content.Intent addCategory(java.lang.String)>")) {
		    //Output intent filter, target info.
		    System.out.println("Intent filter(category) = " + " Class:" + this.klass + " Method:" + method 
					+ " Stmt:" + stmt + " Defs:" + stmt.getTags() + "\n");
		    ArrayList<String> keyList = readKeysFromTag(stmt, ie.getArg(0));
		    if(keyList.size() > 0) iccgEdge.setCategory(keyList.get(0));
		}

	       if (methSig.equals("<android.content.Intent: android.content.Intent setType(java.lang.String)>")
		    || methSig.equals("<android.content.Intent: android.content.Intent setDataAndType(android.net.Uri,java.lang.String)>")
		    ) {
		    //Output intent filter, target info.
		    System.out.println("Intent filter(type) = " + " Class:" + this.klass + " Method:" + method 
					+ " Stmt:" + stmt + " Defs:" + stmt.getTags() + "\n");
		    ArrayList<String> keyList = readKeysFromTag(stmt, ie.getArg(0));
		    if(keyList.size() > 0) iccgEdge.setType(keyList.get(0));
		}

		//Bundle key info, if any.
		if (
		    (methSig.matches("^<android.content.Intent: .* get.*Extra.*") && !methSig.contains("getExtras()>"))
		||  (methSig.matches("^<android.os.Bundle: .* get.*"))
		||  (methSig.matches("^<android.content.Intent: android.content.Intent put.*"))
		||  (methSig.matches("^<android.os.Bundle: void put.*"))
		){

		    if (ie.getUseBoxes().size() < 2) continue;
		    ImmediateBox bundleLoc = (ImmediateBox) ie.getUseBoxes().get(1);
		    Value putStringArg = bundleLoc.getValue();
		    ArrayList<String> bundleKeyList = readKeysFromTag(stmt, putStringArg);
		    //we don't need bundle key right now.
		    //if(bundleKeyList.size() > 0) tgtNode.setKeys(new HashSet(bundleKeyList));

		}

		///exit point.
		if (
		    methSig.equals("<android.app.Activity: void startActivity(android.content.Intent)>")
		    || methSig.equals("<android.content.ContextWrapper: void sendBroadcast(android.content.Intent)>")
		    //shall we mark bindservice?|| methSig.equals("<android.content.ContextWrapper: boolean bindService(android.content.Intent,android.content.ServiceConnection,int)>") 
		    || methSig.equals("<android.content.ContextWrapper: android.content.ComponentName startService(android.content.Intent)>")
		    || methSig.equals("<android.content.ContextWrapper: void sendBroadcast(android.content.Intent,java.lang.String)>")
		    || methSig.equals("<android.content.ContextWrapper: void sendStickyBroadcast(android.content.Intent)>")
		    || methSig.equals("<android.content.ContextWrapper: void sendOrderedBroadcast(android.content.Intent,java.lang.String)>")
		    || methSig.equals("<android.content.ContextWrapper: void sendOrderedBroadcast(android.content.Intent,java.lang.String,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)>")
		    || methSig.equals("<android.content.ContextWrapper: void sendStickyOrderedBroadcast(android.content.Intent,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)>")

		    || methSig.equals("<android.content.Context: void sendBroadcast(android.content.Intent)>")
		    || methSig.equals("<android.content.Context: android.content.ComponentName startService(android.content.Intent)>")
		    || methSig.equals("<android.content.Context: void sendBroadcast(android.content.Intent,java.lang.String)>")
		    || methSig.equals("<android.content.Context: void sendStickyBroadcast(android.content.Intent)>")
		    || methSig.equals("<android.content.Context: void sendOrderedBroadcast(android.content.Intent,java.lang.String)>")
		    || methSig.equals("<android.content.Context: void sendOrderedBroadcast(android.content.Intent,java.lang.String,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)>")
		    || methSig.equals("<android.content.Context: void sendStickyOrderedBroadcast(android.content.Intent,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)>")

		    || methSig.equals("<android.app.Activity: void startActivities(android.content.Intent[])>")
		    || methSig.equals("<android.app.Activity: void startIntentSender(android.content.IntentSender,android.content.Intent,int,int,int)>")
		    || methSig.equals("<android.app.Activity: void startActivityForResult(android.content.Intent,int)>")
		    || methSig.equals("<android.app.Activity: boolean startActivityIfNeeded(android.content.Intent,int)>")
		    || methSig.equals("<android.app.Activity: boolean startNextMatchingActivity(android.content.Intent)>")
		    || methSig.equals("<android.app.Activity: void startActivityFromChild(android.app.Activity,android.content.Intent,int)>")
		    || methSig.equals("<android.app.Activity: void startActivityFromFragment(android.app.Fragment,android.content.Intent,int)>")
		    || methSig.equals("<android.app.Activity: void startIntentSenderForResult(android.content.IntentSender,int,android.content.Intent,int,int,int)>")
		    || methSig.equals("<android.app.Activity: void startIntentSenderFromChild(android.app.Activity,android.content.IntentSender,int,android.content.Intent,int,int,int)>")
		){

		    boolean isImplicit = sld.checkImplicit(stmt);
		    //Output intent results.
		    System.out.println("Implicit intent = " + isImplicit + "# Class: " + this.klass + "# Method: " 
					+ method + "# Stmt: " + stmt + "\n");
		    if (isImplicit) {
			//iccgEdge.setImplicit(isImplicit);
			ICCGNode myTgt3 = iccg.getNode("unknown");
			
			if(components.get(method.getDeclaringClass().getName()) == null) {
			    Iterator iter = component2Meths.entrySet().iterator();
			    while (iter.hasNext()) { 
				if(!klass.getName().contains(pkgName)) break;
				Map.Entry entry = (Map.Entry) iter.next(); 
				String key = (String)entry.getKey(); 
				Set val = (Set)entry.getValue(); 
				if(val.contains(method.getSignature())) {
				    ICCGNode srcNode3 = iccg.getNode(key);
				    ICCGEdge iccgEdge3 = new ICCGEdge();
				    iccgEdge3.setTgt(myTgt3);
				    iccgEdge3.setSrc(srcNode3);

				    iccgEdge3.setAsynchronous(true);
				    String subSig = method.getSubSignature();
				    iccgEdge3.setEvent(subSig.substring(subSig.indexOf(" "), subSig.indexOf("(")));

				    iccg.addEdge(iccgEdge3);
				}
			    } 
			    
			}else{
			    ICCGEdge iccgEdge4 = new ICCGEdge();
			    srcNode = iccg.getNode(klass.getName());
			    iccgEdge4.setTgt(myTgt3);
			    iccgEdge4.setSrc(srcNode);
			    iccg.addEdge(iccgEdge4);

			}

		    }
		}
	    }
	}
	}

	/* Read the reaching def values of the regester.*/
	private ArrayList<String> readKeysFromTag(Stmt stmt, Value arg) 
	{
		ArrayList<String> reachingDef = new ArrayList<String>();
		// Value putStringArg = bundleLoc.getValue();
		StringConstant strVal = StringConstant.v("dummy");

		if (arg instanceof StringConstant){
			strVal = (StringConstant) arg;
			reachingDef.add(strVal.value);
		} else {
			// otherwise we have to ask for reaching def.
			for (Tag tagEntity : stmt.getTags()) {
				if(!(tagEntity instanceof LinkTag)) continue;
				LinkTag ttg = (LinkTag) tagEntity;
				if ( !(ttg.getLink() instanceof JAssignStmt)) continue;
				JAssignStmt asst = (JAssignStmt) ttg.getLink();

				// FIXME:can not deal with inter-proc now!
				if (asst.getLeftOp().equals(arg)) {
					if (asst.getRightOp() instanceof StringConstant) {
						strVal = (StringConstant) asst.getRightOp();
						String bundleKey = strVal.value;
						//bundleKey = bundleKey.replaceAll("[\\s]+", "_");
						reachingDef.add(bundleKey);
					} else if (asst.getRightOp() instanceof ClassConstant) {
						ClassConstant clazz = (ClassConstant) asst.getRightOp();
						reachingDef.add(clazz.value);
					}
				}
			}
		}

		return reachingDef;
	}

	NumberedSet frameworkClasses()
	{
		Scene scene = Scene.v();
		NumberedSet frameworkClasses = new NumberedSet(scene.getClassNumberer());
		String androidJar = System.getProperty("stamp.android.jar");
		JarFile archive;
		try{
			archive = new JarFile(androidJar);
		}catch(IOException e){
			throw new Error(e);
		}
		for (Enumeration entries = archive.entries(); entries.hasMoreElements();) {
			JarEntry entry = (JarEntry) entries.nextElement();
			String entryName = entry.getName();
			int extensionIndex = entryName.lastIndexOf('.');
			if (extensionIndex >= 0) {
				String entryExtension = entryName.substring(extensionIndex);
				if (".class".equals(entryExtension)) {
					entryName = entryName.substring(0, extensionIndex);
					entryName = entryName.replace('/', '.');
					if(scene.containsClass(entryName))
						frameworkClasses.add(scene.getSootClass(entryName));
				}
			}
		}
		return frameworkClasses;
	}

	public SootMethod querySrc(Iterator<Edge> edgeIt, SootMethod method) 
	{

		while(edgeIt.hasNext()){
			Edge edge = edgeIt.next();
			if(!edge.isExplicit() && !edge.isThreadRunCall())
				continue;
			Stmt callStmt = edge.srcStmt();
			SootMethod tgt = (SootMethod) edge.tgt();
			SootMethod src = (SootMethod) edge.src();

			if(method.equals(tgt))
				return src; 
			if(tgt.isAbstract())
				assert false : "tgt = "+tgt +" "+tgt.isAbstract();
			if(tgt.isPhantom())
				continue;
			if(ignoreStubs){
				if(stubMethods.contains(tgt) || (src != null && stubMethods.contains(src)))
				    continue;
			}
		}

		return null;

	}

	//Return the name of first framework super class.
	public String getSuperFramework(SootClass klazz) 
	{
		NumberedSet fklasses = frameworkClasses();
		boolean flag = true;
		while(flag){
			if(klazz == null) break;
			if(fklasses.contains(klazz)) break;
			klazz = (klazz.hasSuperclass() ? klazz.getSuperclass() : null);
		}

		if(klazz == null) return ""; 
		else return klazz.getName(); 
	}

    //dump out ICCG.
    public void dump() {
	    ICCG iccg = new ICCG();

        Iterator iter = components.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String)entry.getKey();
			XmlNode val = (XmlNode)entry.getValue();
			String nodeName = val.getName();

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

        ProgramRel relICCG = (ProgramRel) ClassicProject.g().getTrgt("ICCG");
		relICCG.load();
        ProgramRel relICCGImp = (ProgramRel) ClassicProject.g().getTrgt("ICCGImp");
		relICCGImp.load();

		Iterable<Pair<Object,Object>> res1 = relICCG.getAry2ValTuples();
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

		}

		Iterable<Pair<Object,Object>> res2 = relICCGImp.getAry2ValTuples();
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
		relICCGImp.close();

		System.out.println(iccg.getSignature());
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

        /*System.out.println("Weight:...." + depMap);

        Iterator itSrc = depMap.entrySet().iterator();
        while (itSrc.hasNext()) {//k1: src
			Map.Entry srcEntry = (Map.Entry) itSrc.next();
			String srckey = (String)srcEntry.getKey();
			int val = (Integer)srcEntry.getValue();
            System.out.println(srckey.replace("$", "-->") + " Weight=" +val);

        }*/

        /*ProgramRel relVarComp = (ProgramRel) ClassicProject.g().getTrgt("varComp");
		relVarComp.load();
        ProgramRel relPtComp = (ProgramRel) ClassicProject.g().getTrgt("ptComp");
		relPtComp.load();

        Iterator itSrc = components.entrySet().iterator();
		while (itSrc.hasNext()) {//k1: src
			Map.Entry srcEntry = (Map.Entry) itSrc.next();
			String srckey = (String)srcEntry.getKey();
            Iterator itTgt = components.entrySet().iterator();
		    while (itTgt.hasNext()) {//k2:tgt
			    Map.Entry tgtEntry = (Map.Entry) itTgt.next();
			    String tgtkey = (String)tgtEntry.getKey();
                if(tgtkey.equals(srckey)) continue;

                int weight = 0;
                //k1:varComp
                Iterable<Trio<Object,Object,Object>> varComp = relVarComp.getAry3ValTuples();
                for(Trio<Object,Object, Object> trioVar : varComp) {
                    RefType s  = (RefType)trioVar.val2;
                    if(!s.getClassName().equals(srckey)) continue;
                        //System.out.println("srcccc: ..." + s.getClassName());

                    //k2:ptComp
                    Iterable<Trio<Object,Object,Object>> ptComp = relPtComp.getAry3ValTuples();
                    for(Trio<Object,Object, Object> trioPt : ptComp) {
                        RefType t  = (RefType)trioPt.val2;
                        if(!t.getClassName().equals(tgtkey)) continue;
                            //System.out.println("tgtttt: ..." + t.getClassName());
                        if(Utils.areEqual(trioVar.val0, trioPt.val0) &&
                           Utils.areEqual(trioVar.val1, trioPt.val1) ) 
                            weight++;
                    }
                }
                System.out.println("Depend....." + srckey + "---->(" + weight +")" + tgtkey);


            }


        }
		
		relVarComp.close();
		relPtComp.close();*/
    }

	public void run()
	{
		/*parsePermission();
		NumberedSet fklasses = frameworkClasses();
		openRels();
		for(SootClass klass: Program.g().getClasses()){
			if(fklasses.contains(klass)) continue;
			this.visit(klass);
		}*/

		//output dotty.
        dump();
        compDepend();
        genGraph();
		//System.out.println(iccg.getSignature());

	}
    
    /*class IccgNamespaceContext implements NamespaceContext
    {
        public String getNamespaceURI(String prefix) {
            return "http://schemas.android.com/apk/res/android";
	}
	

    }*/

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
        StreamResult result = new StreamResult(new File("/afs/cs.stanford.edu/u/yufeng/file.graphml"));
 
        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);
 
        transformer.transform(source, result);
 
        System.out.println("File saved!");
 
      } catch (ParserConfigurationException pce) {
        pce.printStackTrace();
      } catch (TransformerException tfe) {
        tfe.printStackTrace();
      }
    }

    private String parseStr(String str)
    {
        //if (str.contains("$"))
         //   str = str.substring(0, str.indexOf("$"));

        str = str.replaceAll("\\/", ".");

        if(str.contains("."))
            str= str.substring(str.lastIndexOf(".")+1,str.length());

        return str;
    }



}
