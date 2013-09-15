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
import chord.util.tuple.object.Pair;
import chord.util.tuple.object.Trio;
import chord.util.tuple.object.Quad;

/**
  * Search for implicit intent.
  **/

@Chord(name="post-java-iccg",
       consumes={ "CallerComp", "CICM" },
       namesOfTypes = { "M", "T", "C", "I"},
       types = { DomM.class, DomT.class, DomC.class, DomI.class},
       namesOfSigns = { "CalledComp", "CICM" },
       signs = { "M0,C0,T0:M0_C0_T0", "C0,I0,C1,M0:C0_I0_C1_M0" }
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
				iNode.setShape("circle");
			} else if("receiver".equals(val.getType())) {
				iNode.setShape("triangle");
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

		SootClass listener = Scene.v().getSootClass("android.view.View.OnClickListener");
		List<SootClass> listenerList = subTypesOf(listener);

		SootClass callback = Scene.v().getSootClass("edu.stanford.stamp.harness.Callback");
		List<SootClass> callbackList = subTypesOf(callback);

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

	private HashMap<SootClass,List<SootClass>> classToSubtypes = new HashMap();

	List<SootClass> subTypesOf(SootClass cl)
	{
		List<SootClass> subTypes = classToSubtypes.get(cl);
		if(subTypes != null)
			return subTypes;

		classToSubtypes.put(cl, subTypes = new ArrayList());

		subTypes.add(cl);

		LinkedList<SootClass> worklist = new LinkedList<SootClass>();
		HashSet<SootClass> workset = new HashSet<SootClass>();
		FastHierarchy fh = Program.g().scene().getOrMakeFastHierarchy();

		if(workset.add(cl)) worklist.add(cl);
		while(!worklist.isEmpty()) {
			cl = worklist.removeFirst();
			if(cl.isInterface()) {
				for(Iterator cIt = fh.getAllImplementersOfInterface(cl).iterator(); cIt.hasNext();) {
					final SootClass c = (SootClass) cIt.next();
					if(workset.add(c)) worklist.add(c);
				}
			} else {
				if(cl.isConcrete()) {
					subTypes.add(cl);
				}
				for(Iterator cIt = fh.getSubclassesOf(cl).iterator(); cIt.hasNext();) {
					final SootClass c = (SootClass) cIt.next();
					if(workset.add(c)) worklist.add(c);
				}
			}
		}
		return subTypes;
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

	public void run()
	{
		Program program = Program.g();
		parsePermission();
		program.buildCallGraph();
		fh = Program.g().scene().getOrMakeFastHierarchy();
		NumberedSet fklasses = frameworkClasses();
		openRels();
		for(SootClass klass: Program.g().getClasses()){
			if(fklasses.contains(klass)) continue;
			this.visit(klass);
		}

		//output dotty.
		System.out.println(iccg.getSignature());
		fh = null;

	}

}
