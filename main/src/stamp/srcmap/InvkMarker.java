package stamp.srcmap;

import java.util.*;

public class InvkMarker extends Marker
{
	String text;
	List<Expr> params;	

	public Expr getArg(int i)
	{ 
		if(params.size() == i){
			//it is the vararg-type param
			//and in the source code in the corresponding
			//invocation instr does not pass anything for this param
			return null;
		}
		return params.get(i); 
	}	
	
	public int argCount(){ return params.size(); }
	
	public String text(){ return text; }

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(line + ": " + chordSig + "(");
		if(params != null){
			for(Expr e : params){
				builder.append((e == null ? "null" : e.toString())+", ");
			}
		}
		builder.append(")");
		return builder.toString();
	}
}