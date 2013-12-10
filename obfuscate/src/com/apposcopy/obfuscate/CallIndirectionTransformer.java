package com.apposcopy.obfuscate;

import soot.*;
import soot.jimple.*;
import soot.util.*;

import java.util.*;

public class CallIndirectionTransformer extends BodyTransformer
{
	public CallIndirectionTransformer()
	{
	}

	protected void internalTransform(Body body, String phase, Map options)
	{
		for(Unit unit : body.getUnits()){
			Stmt stmt = (Stmt) unit;

			//invocation statements
			if(stmt.containsInvokeExpr()){
				InvokeExpr ie = stmt.getInvokeExpr();
				if(ie instanceof SpecialInvokeExpr)
					continue;

				SootMethod callee = ie.getMethod();
				String calleeClassName = callee.getDeclaringClass().getName();
				if(calleeClassName.startsWith("android.")){
					SootMethod proxyMethod = proxyMethodFor(callee);

					List<Value> argsCopy = new ArrayList();
					argsCopy.addAll(ie.getArgs());

					if(ie instanceof InstanceInvokeExpr){
						Local base = (Local) ((InstanceInvokeExpr) ie).getBase();
						argsCopy.add(0, base);
					}
						
					InvokeExpr newie = Jimple.v().newStaticInvokeExpr(proxyMethod.makeRef(), argsCopy);

					if(stmt instanceof AssignStmt){
						((AssignStmt) stmt).setRightOp(newie);
					} else {
						stmt.getInvokeExprBox().setValue(newie);
					}
				}
			}
		}
	}

	
	private SootClass proxyClassFor(SootClass klass)
	{
		String proxyClassName = "apposcopy$".concat(klass.getName().replace('.','$'));
		SootClass proxyClass;
		if(Scene.v().containsClass(proxyClassName)){
			proxyClass = Scene.v().getSootClass(proxyClassName);
		} else {
			proxyClass = new SootClass(proxyClassName, Modifier.PUBLIC);
			proxyClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
			Scene.v().addClass(proxyClass);
			proxyClass.setApplicationClass();
		}
		return proxyClass;
	}
	
	private SootMethod proxyMethodFor(SootMethod method)
	{
		SootClass proxyClass = proxyClassFor(method.getDeclaringClass());
		
		int paramCount = method.getParameterCount();
		List<Type> paramTypes;
		if(method.isStatic()){
			paramTypes = method.getParameterTypes();
		} else {
			paramTypes = new ArrayList();
			paramTypes.add(method.getDeclaringClass().getType());
			for(int i = 0; i < paramCount; i++){
				paramTypes.add(method.getParameterType(i));
			}
		}
		
		Type retType = method.getReturnType();
		String retTypeStr = retType.toString().replace('.','$');

		String methodName = method.getName();
		methodName = methodName.concat("$$").concat(retTypeStr);
		
		SootMethod proxyMethod;
		if(proxyClass.declaresMethod(methodName, paramTypes)){
			proxyMethod = proxyClass.getMethod(methodName, paramTypes);
		} else {
			proxyMethod = new SootMethod(methodName, paramTypes, retType, Modifier.PUBLIC | Modifier.STATIC);
			proxyClass.addMethod(proxyMethod);
			
			Body body = Jimple.v().newBody(proxyMethod);
			proxyMethod.setActiveBody(body);
			Chain<Local> locals = body.getLocals();
			Chain<Unit> units = body.getUnits();
			int proxyParamCount = proxyMethod.getParameterCount();
			List<Local> params = new ArrayList();
			for(int i = 0; i < proxyParamCount; i++){
				Local param = Jimple.v().newLocal("l"+i, paramTypes.get(i));
				locals.add(param);
				params.add(param);
				units.add(Jimple.v().newIdentityStmt(param, Jimple.v().newParameterRef(paramTypes.get(i), i)));
			}
			
			InvokeExpr ie;
			if(method.isStatic()){
				ie = Jimple.v().newStaticInvokeExpr(method.makeRef(), params);
			} else {
				Local rcvr = params.remove(0);
				SootClass declKlass = method.getDeclaringClass();
				if(declKlass.isInterface())
					ie = Jimple.v().newInterfaceInvokeExpr(rcvr, method.makeRef(), params);
				else 
					ie = Jimple.v().newVirtualInvokeExpr(rcvr, method.makeRef(), params);
			}
			
			if(retType instanceof VoidType){
				units.add(Jimple.v().newInvokeStmt(ie));
				units.add(Jimple.v().newReturnVoidStmt());
			} else {
				Local retVar = Jimple.v().newLocal("r", retType);
				locals.add(retVar);
				units.add(Jimple.v().newAssignStmt(retVar, ie));
				units.add(Jimple.v().newReturnStmt(retVar));
			}
		}

		return proxyMethod;
	}
}