package com.apposcopy.obfuscate;

import soot.*;
import soot.jimple.*;

import java.util.*;

public class StringConstantsTransformer extends BodyTransformer
{
	private Mapper mapper;
	private Set<String> actionStrings;

	public StringConstantsTransformer(String mappingFile, Set<String> actionStrings)
	{
		this.mapper = new Mapper(mappingFile);
		this.actionStrings = actionStrings;
	}

	protected void internalTransform(Body body, String phase, Map options)
	{
		for(Unit unit : body.getUnits()){
			Stmt stmt = (Stmt) unit;

			//invocation statements
			if(stmt.containsInvokeExpr()){
				InvokeExpr ie = stmt.getInvokeExpr();
				List args = ie.getArgs();
				int i = 0;
				for(Iterator ait = args.iterator(); ait.hasNext();){
					Immediate arg = (Immediate) ait.next();
					if(arg instanceof StringConstant){
						Immediate newArg = replaceIfNeeded((StringConstant) arg);
						if(newArg != null){
							ie.setArg(i, newArg);
						}
					}
					i++;
				}
			}
			else if(stmt instanceof AssignStmt){
				Value rhs = ((AssignStmt) stmt).getRightOp();
				if(rhs instanceof StringConstant){
					Immediate newRhs = replaceIfNeeded((StringConstant) rhs);
					if(newRhs != null)
						((AssignStmt) stmt).setRightOp(newRhs);
				}
			}
		}
	}
	
	private StringConstant replaceIfNeeded(StringConstant s)
	{
		String str = s.value;
		String newName = mapper.newName(str);
		if(newName == null) {
			if(str.equals("application/vnd.android.package-archive"))
				;
			else if(str.startsWith("content://"))
				;
			else if(actionStrings.contains(str))
				;
			else
				str = "obfuscated";
			return StringConstant.v(str);
		}
		else
			return StringConstant.v(newName);
	}

}