import soot.*;
import soot.jimple.*;

import java.util.*;

public class FixCode extends BodyTransformer
{
	Mapper mapper;

	public FixCode(String mappingFile)
	{
		this.mapper = new Mapper(mappingFile);
	}
	

	public static void main(String[] args)
	{
		String mappingFile = args[0];
		//String manifestFile = args[1];

		FixCode fc = new FixCode(mappingFile);

		PackManager.v().getPack("jtp").add(new Transform("jtp.fixCode", fc));
		soot.Main.main(args);
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
							ie.setArg(0, newArg);
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
		return s;
	}
	
}