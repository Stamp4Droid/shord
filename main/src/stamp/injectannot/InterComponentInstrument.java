package stamp.injectannot;

import java.io.File;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.jimple.toolkits.annotation.defs.ReachingDefsTagger;
import soot.jimple.internal.ImmediateBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JimpleLocalBox;
import soot.jimple.internal.VariableBox;
import soot.tagkit.LinkTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import shord.program.Program;
import java.util.*;
import edu.stanford.droidrecord.logreader.events.info.ParamInfo;

import stamp.droidrecord.DroidrecordProxy;
import stamp.droidrecord.StampCallArgumentValueAnalysis;

/**
 * Visitor class for instrumenting the app code befere they are fed to chord. 
 * Procedure: For each bundleId, inject getter/setter to Bundle.classs; 
 * For get/putString, replace to get/put_$bundleId;
 * Then build the connection between inter-components through static field "intent",
 * e.g, in source: call "$target.set_Intent(intent)";
 * Currently we only consider the following method invocations:
 * public Intent (String action, Uri uri, Context packageContext, Class<?> cls) 
 * public Intent setClass(Context packageContext, Class<?> cls) 
 * public Intent setClassName (Context packageContext, String className) 
 * public Intent setClassName (String packageName, String className) 
 * public Intent setComponent (ComponentName component)
 *
 * @author Yu Feng (yufeng@cs.wm.edu)
 * @date Jun 15, 2013
 */

public class InterComponentInstrument extends AnnotationInjector.Visitor
{

	private String intentClass = "android.content.Intent";
	
	private String bundleClass = "android.os.Bundle";

    private SootClass rootClass;
	
	//cache the temporary result from new ComponentName.
	private Map<Value, String> arg2CompnentName = new HashMap<Value, String>();
	
    private StampCallArgumentValueAnalysis cavAnalysis = null;

    public InterComponentInstrument()
    {
        DroidrecordProxy droidrecord = stamp.droidrecord.DroidrecordProxy.g();
        if(droidrecord.isAvailable()) {
            cavAnalysis = droidrecord.getCallArgumentValueAnalysis();
            cavAnalysis.run();
        }
    }
    
    private List<ParamInfo> queryArgumentValues(SootMethod caller, Stmt stmt, 
                                                int argNum) {
        if(cavAnalysis != null)
            return cavAnalysis.queryArgumentValues(caller, stmt, argNum);
        else
            return java.util.Collections.EMPTY_LIST;
    }
	
    protected void visit(SootClass klass)
    {
		//filter out android.support.v4.jar
		if (klass.getName().contains("android.support")) return;
		this.rootClass = klass;
		Collection<SootMethod> methodsCopy = new ArrayList(klass.getMethods());
		for(SootMethod method : methodsCopy)
			visitMethod(method);
    }
	
	/* 
	 * Reaching definition for each method body. Result will be put into tag
	 * field for each statement. 
	 * Currently cann't deal with e.g, setComponent(new ComponentName("pkgName", "className"))
	 */
	private void runReachingDef(Body body)
	{
		///reaching def begin.
		UnitGraph g = new ExceptionalUnitGraph(body);
		LocalDefs sld = new SmartLocalDefs(g, new SimpleLiveLocals(g));

		Iterator it = body.getUnits().iterator();
		while (it.hasNext()){
			Stmt s = (Stmt)it.next();
			//System.out.println("stmt: "+s);
			Iterator usesIt = s.getUseBoxes().iterator();
			while (usesIt.hasNext()){
				ValueBox vbox = (ValueBox)usesIt.next();
				if (vbox.getValue() instanceof Local) {
					Local l = (Local)vbox.getValue();
					//System.out.println("local: "+l);
					Iterator<Unit> rDefsIt = sld.getDefsOfAt(l, s).iterator();
					while (rDefsIt.hasNext()){
						Stmt next = (Stmt)rDefsIt.next();
						String info = l+" has reaching def: "+next.toString();
						s.addTag(new LinkTag(info, next, body.getMethod().getDeclaringClass().getName(), 
							     "Reaching Defs"));
					}
				}
			}
		}
		//end 
	}
	
    private void visitMethod(SootMethod method)
    {
		
		if(!method.isConcrete())
			return;
		
		Body body = method.retrieveActiveBody();
		
		//run reaching def for each body, should combine with dynamic analysis result.
        this.runReachingDef(body);
		
		Chain<Local> locals = body.getLocals();
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> uit = units.snapshotIterator();
		while(uit.hasNext()){
			Stmt stmt = (Stmt) uit.next();
	    
			//invocation statements
			if(stmt.containsInvokeExpr()){
				InvokeExpr ie = stmt.getInvokeExpr();
				
				String methodRefStr = ie.getMethodRef().toString();
				
				//For Intent: intent.putExtra(), include string, int, boolean, byte, etc.
				if (methodRefStr
				    .contains(this.intentClass+": "+this.intentClass+" putExtra(java.lang.String,")
				  || methodRefStr
				    .equals("<"+this.intentClass+": java.lang.String getStringExtra(java.lang.String)>")
			      || methodRefStr
			        .equals("<"+this.intentClass+": android.os.Parcelable getParcelableExtra(java.lang.String)>")
				                                                                                             ) {
					// System.out.println("So you are going to call getStringExtra......." + stmt);
					ImmediateBox bundleLoc = (ImmediateBox) ie.getUseBoxes().get(1);
					Value putStringArg = bundleLoc.getValue();
					StringConstant strVal = StringConstant.v("dummy");
					
					if (putStringArg instanceof StringConstant){
						strVal = (StringConstant) putStringArg;
					} else {
						// otherwise we have to ask for reaching def.
						for (Tag tagEntity : stmt.getTags()) {
							if(!(tagEntity instanceof LinkTag)) continue; 
							LinkTag ttg = (LinkTag) tagEntity;
							if ( !(ttg.getLink() instanceof JAssignStmt)) continue;
							JAssignStmt asst = (JAssignStmt) ttg.getLink();
							
							// FIXME:can not deal with inter-proc now!
							if (asst.getLeftOp().equals(putStringArg)) {
								// assert (asst.getRightOp() instanceof StringConstant);
								if (asst.getRightOp() instanceof StringConstant) {
									strVal = (StringConstant) asst.getRightOp();
								}
							}
						}
					}
					//should triggle bundle instrument
					String bundleKey = strVal.value;
					if (bundleKey.equals("dummy")) {
						reportUnknownRegister(stmt, putStringArg);
						continue; ///go to next stmt
					}

                    //need to add getter/setter of bundlekey to Bundle.class!
					instrumentBundle(bundleKey);
								
					JimpleLocalBox intentObj = (JimpleLocalBox) ie.getUseBoxes().get(0);
				    
					SootMethod getExtrasCall = Scene.v().getMethod(
						"<" + this.intentClass + ": "+this.bundleClass+" getExtras()>");
					
					//invoke intent.getExtras()
					VirtualInvokeExpr getExtrasExpr = 
						Jimple.v().newVirtualInvokeExpr(
							(Local) intentObj.getValue(),
								getExtrasCall.makeRef(), Arrays.asList(new Value[] {}));
					
					Local extrasLocal = Jimple.v().newLocal("r_Extras", RefType.v(this.bundleClass)); 
					body.getLocals().add(extrasLocal);
					AssignStmt assign2Extras = Jimple.v().newAssignStmt(extrasLocal, getExtrasExpr);
					units.insertBefore(assign2Extras, stmt);
					
					//invoke extra.put_deviceId()
					if (methodRefStr
					    .contains(this.intentClass+": "+this.intentClass+" putExtra(java.lang.String,") ) {
						SootMethod putExtrasCall = Scene.v().getMethod(
							"<" + this.bundleClass + ": void put_"
								+ bundleKey + "(java.lang.Object)>");
					
						InvokeStmt putExtraStmt = Jimple.v().newInvokeStmt(
							Jimple.v().newVirtualInvokeExpr(extrasLocal,
								putExtrasCall.makeRef(), ie.getArg(1)));
						units.insertAfter(putExtraStmt, stmt);
						//Remove this will reduce the false alarm like deviceId=>intent, but potentially buggy,
						//e.g, what if I assign current expr to an new intent object?
						units.remove(stmt);	
																										 
					}

					
					//for getter in bundle...
					//$r6.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>($r4)
					if (methodRefStr
						.equals("<"+this.intentClass+": java.lang.String getStringExtra(java.lang.String)>")
		  			 || methodRefStr
					    .equals("<"+this.intentClass+": android.os.Parcelable getParcelableExtra(java.lang.String)>")
					) {
						SootMethod getObjCall = Scene.v().getMethod(
							"<" + this.bundleClass
								+ ": java.lang.Object get_" + bundleKey
									+ "()>");

						VirtualInvokeExpr invokeGetStr = 
							Jimple.v().newVirtualInvokeExpr(
								extrasLocal, getObjCall.makeRef(), Arrays.asList(new Value[] {}));
					
						//FIXME: what if we have multiple defboxes?
						// assert (stmt.getDefBoxes().size > 0);
						if (stmt.getDefBoxes().size() == 0) {
							reportUnknownRegister(stmt, extrasLocal);
							continue;
						}
						
						VariableBox orgCallSite = (VariableBox)stmt.getDefBoxes().get(0);
						AssignStmt invokeAssign = Jimple.v().newAssignStmt(orgCallSite.getValue(), invokeGetStr);	
						units.insertAfter(invokeAssign, stmt);
						units.remove(stmt);	
						
					}
											
				}
				
				
				// For Bundle: Src putString -> put_id && getString -> 
				//$r9 = virtualinvoke $r8.<android.os.Bundle: 
				//java.lang.String getString(java.lang.String)>($r4)
				if (methodRefStr
						.contains(this.bundleClass + ": void putString")
						|| methodRefStr
								.equals("<"
										+ this.bundleClass
										+ ": java.lang.String getString(java.lang.String)>")
				        || methodRefStr
								.equals("<"
										+ this.bundleClass
										+ ": android.os.Parcelable getParcelable(java.lang.String)>")) {						
											
					ImmediateBox bundleLoc = (ImmediateBox) ie.getUseBoxes().get(1);
					Value putStringArg = bundleLoc.getValue();
					StringConstant strVal = StringConstant.v("dummy");
					if (putStringArg instanceof StringConstant){
						strVal = (StringConstant) putStringArg;
					} else {
						// otherwise we have to ask for reaching def.
						for (Tag tagEntity : stmt.getTags()) {
							if(!(tagEntity instanceof LinkTag)) continue; 
							LinkTag ttg = (LinkTag) tagEntity;
							if ( !(ttg.getLink() instanceof JAssignStmt)) continue;
							JAssignStmt asst = (JAssignStmt) ttg.getLink();
							
							// FIXME:can not deal with inter-proc now!
							if (asst.getLeftOp().equals(putStringArg)) {
								// assert (asst.getRightOp() instanceof StringConstant);
								if (asst.getRightOp() instanceof StringConstant) {
									strVal = (StringConstant) asst.getRightOp();
								}
							}
						}
					}
					//should triggle bundle instrument
					JimpleLocalBox bundleObj = (JimpleLocalBox) ie
							.getUseBoxes().get(0);
					String bundleKey = strVal.value;
					if (bundleKey.equals("dummy")) {
						reportUnknownRegister(stmt, putStringArg);
						continue; ///go to next stmt
					}

                    //need to add getter/setter of bundlekey to Bundle.class!
					instrumentBundle(bundleKey);
					
					// invoke
					SootMethod toCall;
					InvokeStmt invokeSetter;
					//for put_string
					if (methodRefStr
					    .contains(this.bundleClass + ": void putString")){
						toCall = Scene.v().getMethod(
							"<" + this.bundleClass + ": void put_"
								+ bundleKey + "(java.lang.Object)>");

						// System.out.println("tocall = " + toCall );
						invokeSetter = Jimple.v().newInvokeStmt(
							Jimple.v().newVirtualInvokeExpr(
								(Local) bundleObj.getValue(),
									toCall.makeRef(), ie.getArg(1)));
						units.insertAfter(invokeSetter, stmt);
					} else {
						// invoke
						toCall = Scene.v().getMethod(
							"<" + this.bundleClass
								+ ": java.lang.Object get_" + bundleKey
									+ "()>");

						VirtualInvokeExpr invoke = 
							Jimple.v().newVirtualInvokeExpr(
								(Local) bundleObj.getValue(),
									toCall.makeRef(), Arrays.asList(new Value[] {}));
						
						//FIXME: what if we have multiple defboxes?
						VariableBox orgCallSite = (VariableBox)stmt.getDefBoxes().get(0);
						AssignStmt invokeAssign = Jimple.v().newAssignStmt(orgCallSite.getValue(), invoke);
						
						units.insertAfter(invokeAssign, stmt);
					}			
					units.remove(stmt);
				}
				
				
				// multiple cases
				// 1. new Intent(context, class)
				if (methodRefStr.contains(intentClass
						+ ": void <init>(android.content.Context,java.lang.Class)")) {
					this.lookforTgtArgToInject(units, stmt, ie.getArg(1));							
					
				}

				// 2. intent.setClass(context, class)
				if (methodRefStr.contains(intentClass + ": " + intentClass
						+ " setClass(android.content.Context,java.lang.Class)")) {
					this.lookforTgtArgToInject(units, stmt, ie.getArg(1));							
				}
				
				// 3. intent.setClassName(context, String)
				/* e.g, $r4.<android.content.Intent: android.content.Intent 
				 *	setClassName(android.content.Context,java.lang.String)>($r0, $r7);
				 */
				if (methodRefStr.contains(intentClass + ": " + intentClass
						+ " setClassName(android.content.Context,java.lang.String)")) {
					this.lookforTgtArgToInject(units, stmt, ie.getArg(1));							
				}
				
				// 4. intent.setClassName(String, String)
				/* e.g, $r4.<android.content.Intent: android.content.Intent 
				 *	setClassName(java.lang.String,java.lang.String)>($r6, $r7) 
				 */
				if (methodRefStr.contains(intentClass + ": " + intentClass
						+ " setClassName(java.lang.String,java.lang.String)")) {
					this.lookforTgtArgToInject(units, stmt, ie.getArg(1));							
				}
				
				// 5. intent.setComponent(ComponentName)
				/* e.g, $r4.<android.content.Intent: android.content.Intent 
				 *	setComponent(android.content.ComponentName)>($r3); 
				 *  right now I can not capture inter-procedure call, so I have to combine 
				 *  both of them, ask for the following call:
				 *  <android.content.ComponentName: void <init>(java.lang.String,java.lang.String)>($r6, $r7)
				 *  Notice that it's still not precise.
				 */
				if (methodRefStr.contains("android.content.ComponentName: void <init>(java.lang.String,java.lang.String)")) {
                    String tgtComptName = "";
					StringConstant strCont;
					if (ie.getArg(1) instanceof StringConstant) {
						strCont = (StringConstant) ie.getArg(1);
						tgtComptName = strCont.value;
					}else {// FIXME: too ugly to grab result in this way
						//otherwise we have to ask for reaching def.
						for (Tag tagEntity : stmt.getTags()) {
							if(!(tagEntity instanceof LinkTag)) continue; 
							LinkTag ttg = (LinkTag) tagEntity;
							if ( !(ttg.getLink() instanceof JAssignStmt)) continue;
							JAssignStmt asst = (JAssignStmt) ttg.getLink();
							if (asst.getLeftOp().equals(ie.getArg(1))) {
								// assert (asst.getRightOp() instanceof ClassConstant);
                                if (asst.getRightOp() instanceof StringConstant) {
									strCont = (StringConstant) asst.getRightOp();
									tgtComptName = strCont.value;
								}

							}
						}

					}
					JimpleLocalBox newBox = (JimpleLocalBox)ie.getUseBoxes().get(0);
					this.arg2CompnentName.put(newBox.getValue(), tgtComptName);
				}
				
				if (methodRefStr.contains(intentClass + ": " + intentClass
						+ " setComponent(android.content.ComponentName)")) {
					//FIXME need more information on setComponent, improve current reachingDef.
					String tgtComptName = this.arg2CompnentName.get(ie.getArg(0));
					
					if (tgtComptName != null) 
						tgtComptName = tgtComptName.replace(File.separatorChar, '.');
					///Check whether exist such class.
					if ( !Program.g().scene().containsClass(tgtComptName)) {
						reportUnknownRegister(stmt, ie.getArg(0));
						return;
					}
				
					this.instruTgtCompt(tgtComptName);
		
					//begin to instument current invoke method of src.		
					JimpleLocalBox localBox = (JimpleLocalBox) ie
						.getUseBoxes().get(0);
					SootMethod toCall = Scene.v().getMethod(
						"<" + tgtComptName + ": void set_Intent("
							+ intentClass + ")>");
					Stmt invokeSetter = Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(
							toCall.makeRef(), localBox.getValue()));
					units.insertAfter(invokeSetter, stmt);
				}
				
			}
		}
    }
	
	
	/* Search for the target component based on reachingDef stored in tag. */	
	private void lookforTgtArgToInject(Chain<Unit> units, Stmt stmt, Value arg) {
		
		String tgtComptName = "";
		ClassConstant clazz = ClassConstant.v("dummy");
		StringConstant strCont = StringConstant.v("dummy");
		InvokeExpr ie = stmt.getInvokeExpr();
		
		if (arg instanceof ClassConstant) {
			clazz = (ClassConstant) arg;
			tgtComptName = clazz.value;
		} else if (arg instanceof StringConstant) {
			strCont = (StringConstant) arg;
			tgtComptName = strCont.value;
		}else {// FIXME: too ugly to grab result in this way
			//otherwise we have to ask for reaching def.
			for (Tag tagEntity : stmt.getTags()) {
				// String tagStr = arg.toString();
				if(!(tagEntity instanceof LinkTag)) continue; 
				LinkTag ttg = (LinkTag) tagEntity;
				if ( !(ttg.getLink() instanceof JAssignStmt)) continue;
				JAssignStmt asst = (JAssignStmt) ttg.getLink();
				if (asst.getLeftOp().equals(arg)) {
					// assert (asst.getRightOp() instanceof ClassConstant);
					if (asst.getRightOp() instanceof ClassConstant) {
						clazz = (ClassConstant) asst.getRightOp();		
						tgtComptName = clazz.value;	
					} else if (asst.getRightOp() instanceof StringConstant) {
						strCont = (StringConstant) asst.getRightOp();
						tgtComptName = strCont.value;
					}

				}
			}

		}
		
		if (tgtComptName.equals("")) {
			reportUnknownRegister(stmt, arg);
			return;
		}
		
		tgtComptName = tgtComptName.replace(File.separatorChar, '.');
		///Check whether exist such class.
		if ( !Program.g().scene().containsClass(tgtComptName)) {
            reportUnknownRegister(stmt, arg);
			return;
		}
				
		this.instruTgtCompt(tgtComptName);
		
		//begin to instument current invoke method of src.		
		JimpleLocalBox localBox = (JimpleLocalBox) ie
			.getUseBoxes().get(0);
		SootMethod toCall = Scene.v().getMethod(
			"<" + tgtComptName + ": void set_Intent("
				+ intentClass + ")>");
		Stmt invokeSetter = Jimple.v().newInvokeStmt(
			Jimple.v().newStaticInvokeExpr(
				toCall.makeRef(), localBox.getValue()));
		units.insertAfter(invokeSetter, stmt);
	}

    /* Inject get/set-Intent method bodies for target component.*/
	private void instruTgtCompt(String klassName) {
		String intentInstName = "intent";
		klassName = klassName.replace(File.separatorChar, '.');
		SootClass klass =  Program.g().scene().getSootClass(klassName);		
		
		//Check whether this class has already been instrumented
		if(klass.declaresFieldByName(intentInstName)) {
			System.out.println("WARN: class has already been instrumented.." + klassName);
			return;
		}
		
		Collection<SootMethod> methodsCopy = new ArrayList(klass.getMethods());
		Body body;
		Chain<Unit> units;

		// add public static Intent intent;
		// "private static android.content.Intent intent;"
		SootField intentField = new SootField(intentInstName,
				RefType.v(this.intentClass), Modifier.PRIVATE | Modifier.STATIC);
		klass.addField(intentField);

		// "public android.content.Intent getIntent() {return intent; }"
		SootMethod m_getter = new SootMethod("getIntent", Arrays.asList(),
				RefType.v(this.intentClass), Modifier.PUBLIC);
		// stmt
		body = Jimple.v().newBody(m_getter);
		m_getter.setActiveBody(body);
		units = body.getUnits();
		//add this ref!!!!, otherwise it will be static..
		ThisRef thisRef = new ThisRef(klass.getType());
		Local thisLocal = Jimple.v().newLocal("r0", thisRef.getType());   
		body.getLocals().add(thisLocal);

		units.add(Jimple.v().newIdentityStmt(thisLocal,
		            Jimple.v().newThisRef((RefType)thisRef.getType())));
      
		SootFieldRef sFieldRef = intentField.makeRef();
		Local new_obj = Jimple.v().newLocal("$r1", intentField.getType());

		body.getLocals().add(new_obj);
		Value v = Jimple.v().newStaticFieldRef(sFieldRef);
		soot.jimple.AssignStmt g_assign = soot.jimple.Jimple.v().newAssignStmt(
				new_obj, v);

		body.getUnits().add(g_assign);
		Stmt retStmt = soot.jimple.Jimple.v().newReturnStmt(new_obj);
		units.add(retStmt);
		klass.addMethod(m_getter);
		// System.out.println("targetBody====getter==" + body);

		// "public static void set_Intent(android.content.Intent i) {intent = i;}",
		SootMethod m_setter = new SootMethod("set_Intent",
				Arrays.asList(RefType.v(this.intentClass)), VoidType.v(),
				Modifier.PUBLIC | Modifier.STATIC);
		// stmt
		body = Jimple.v().newBody(m_setter);
		m_setter.setActiveBody(body);
		units = body.getUnits();
		Local paramLocal = Jimple.v().newLocal("r0",
				RefType.v(this.intentClass));
		body.getLocals().add(paramLocal);
		// add "r0 = @parameter0"
		soot.jimple.ParameterRef paramRef = soot.jimple.Jimple.v()
				.newParameterRef(RefType.v(this.intentClass), 0);
		soot.jimple.Stmt idStmt = soot.jimple.Jimple.v().newIdentityStmt(
				paramLocal, paramRef);
		body.getUnits().add(idStmt);

		soot.jimple.FieldRef fieldRef = soot.jimple.Jimple.v()
				.newStaticFieldRef(sFieldRef);

		soot.jimple.AssignStmt assign = soot.jimple.Jimple.v().newAssignStmt(
				fieldRef, paramLocal);
		body.getUnits().add(assign);

		units.add(Jimple.v().newReturnVoidStmt());

		klass.addMethod(m_setter);
		// System.out.println("targetBody====setter==" + body);
		// 
		// System.out.println("Target===field===>!!" + intentField);
		// System.out.println("Target===getIntent===>!!" + m_getter);
		// System.out.println("Target******add set_Intent===>!!" + m_setter);

	}
	
	/* Inject field + getter/setter to Bundle.class based on
	 * the bundleId collected from app. */
	private void instrumentBundle(String bundleKey) {
		//Look up for the Bundle sootclass at first. What if i can't get the bundle?
		SootClass klass = Program.g().scene().loadClassAndSupport(bundleClass);

		//already contain this bundleKey?
		if (klass.declaresFieldByName(bundleKey)) {
			System.out.println("already found the key**, return.");
			return;
		}
		// add key field, e.g, "public Object deviceId;"
		SootField keyField = new SootField(bundleKey,
			RefType.v("java.lang.Object"), Modifier.PRIVATE);

		klass.addField(keyField);

		keyField = klass.getFieldByName(bundleKey);
		// getter, e.g, "public Object get_deviceId() {return deviceId; }",
		SootMethod m_getter = new SootMethod("get_" + bundleKey,
			Arrays.asList(), RefType.v("java.lang.Object"),
				Modifier.PUBLIC);
		// stmt
		JimpleBody body = Jimple.v().newBody(m_getter);
		m_getter.setActiveBody(body);
		Chain units = body.getUnits();
		
		//
		ThisRef thisRef = new ThisRef(klass.getType());
		Local thisLocal = Jimple.v().newLocal("r0", thisRef.getType());   
		body.getLocals().add(thisLocal);

		units.add(Jimple.v().newIdentityStmt(thisLocal,
		            Jimple.v().newThisRef((RefType)thisRef.getType())));
       
		SootFieldRef sFieldRef = keyField.makeRef();
		Local returnLocal = Jimple.v().newLocal("r1", keyField.getType());
		body.getLocals().add(returnLocal);
			
		Value v = Jimple.v().newInstanceFieldRef(thisLocal, sFieldRef);
		soot.jimple.AssignStmt returnAssign = soot.jimple.Jimple.v().newAssignStmt(
			returnLocal, v);
		body.getUnits().add(returnAssign);
		Stmt retStmt = soot.jimple.Jimple.v().newReturnStmt(returnLocal);
		units.add(retStmt);
		klass.addMethod(m_getter);
		// System.out.println("inject field and methods into bundle....done: getter: " + body);
		

		// setter, e.g,  "public void put_deviceId(Object v) {deviceId = v; }",
		SootMethod m_setter = new SootMethod("put_" + bundleKey,
			Arrays.asList(RefType.v("java.lang.Object")), VoidType.v(),
				Modifier.PUBLIC);
		body = Jimple.v().newBody(m_setter);
		m_setter.setActiveBody(body);
		units = body.getUnits();
		
		//r0 should actually point to "this".
		ThisRef sThisRef = new ThisRef(klass.getType());
		Local sThisLocal = Jimple.v().newLocal("r0", sThisRef.getType());   
		body.getLocals().add(sThisLocal);

		units.add(Jimple.v().newIdentityStmt(sThisLocal,
			Jimple.v().newThisRef((RefType)sThisRef.getType())));

		Local paramLocal = Jimple.v().newLocal("r1",
			RefType.v("java.lang.Object"));
		body.getLocals().add(paramLocal);
		// add "l0 = @parameter0"
		soot.jimple.ParameterRef paramRef = soot.jimple.Jimple.v()
			.newParameterRef(RefType.v("java.lang.Object"), 0);
		soot.jimple.Stmt idStmt = soot.jimple.Jimple.v().newIdentityStmt(
			paramLocal, paramRef);
		body.getUnits().add(idStmt);

		soot.jimple.FieldRef instFieldRef = soot.jimple.Jimple.v()
			.newInstanceFieldRef(sThisLocal, sFieldRef);

		soot.jimple.AssignStmt assign = soot.jimple.Jimple.v()
			.newAssignStmt(instFieldRef, paramLocal);
		body.getUnits().add(assign);

		units.add(Jimple.v().newReturnVoidStmt());
		klass.addMethod(m_setter);
		// System.out.println("inject field and methods into bundle....done: setter: " + body);
		

	}
	
	/* Output the related information of the register which can't be handled by intro-proc reaching def. */
	private void reportUnknownRegister(Stmt stmt, Value v) {
		System.out.println("ERROR: Can not locate the value from reachingDef: " + v);
		System.out.println("ERROR:Current class: " + this.rootClass + " || Statement: " + stmt + 
			 "|| reachingDef: " + stmt.getTags());
	}
    
}