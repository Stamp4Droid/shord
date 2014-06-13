package stamp.missingmodels.util.cflsolver.util;

import shord.analyses.CastVarNode;
import shord.analyses.DomM;
import shord.analyses.DomU;
import shord.analyses.DomV;
import shord.analyses.LocalVarNode;
import shord.analyses.ParamVarNode;
import shord.analyses.RetVarNode;
import shord.analyses.StringConstantVarNode;
import shord.analyses.ThisVarNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import soot.SootMethod;
import chord.bddbddb.Dom;

public class ConversionUtils {
	public static String toStringShord(String name) {
		Dom<?> dom = (Dom<?>)ClassicProject.g().getTrgt(name.substring(0,1));
		if(name.contains("_")) {
			String newName = name.split("_")[0];
			return dom.get(Integer.parseInt(newName.substring(1))).toString();
		} else {
			return dom.get(Integer.parseInt(name.substring(1))).toString();
		}
	}
	
	public static String getMethodSig(String name) {
		VarNode v;
		if(name.startsWith("V")) {
			DomV dom = (DomV)ClassicProject.g().getTrgt(name.substring(0,1).toUpperCase());
			v = dom.get(Integer.parseInt(name.substring(1)));
		} else if(name.startsWith("U")) {
			DomU dom = (DomU)ClassicProject.g().getTrgt(name.substring(0,1).toUpperCase());
			v = dom.get(Integer.parseInt(name.substring(1)));
		} else if(name.startsWith("M")) {
			DomM dom = (DomM)ClassicProject.g().getTrgt(name.substring(0,1).toUpperCase());
			return dom.get(Integer.parseInt(name.substring(1))).toString();
		} else {
			throw new RuntimeException("Unrecognized vertex: " + name);
		}
		return getMethodForVar(v).toString();
	}

	public static SootMethod getMethodForVar(VarNode v) {
		if(v instanceof ParamVarNode) {
			return ((ParamVarNode)v).method;
		} else if(v instanceof RetVarNode) {
			return ((RetVarNode)v).method;
		} else if(v instanceof ThisVarNode) {
			return ((ThisVarNode)v).method;
		} else if(v instanceof LocalVarNode) {
			return ((LocalVarNode)v).meth;
		} else if(v instanceof CastVarNode) {
			return ((CastVarNode)v).method;
		} else if(v instanceof StringConstantVarNode) {
			return ((StringConstantVarNode)v).method;
		} else {
			throw new RuntimeException("Unrecognized variable: " + v);
		}
	}
}
