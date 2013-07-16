package stamp.reporting;

import soot.SootClass;
import soot.Unit;
import soot.SootMethod;
import soot.jimple.Stmt;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.StringEscapeUtils;

import shord.program.Program;
import stamp.srcmap.SourceInfo;
import stamp.srcmap.Expr;

/*
 * @author Saswat Anand
**/
public class Tuple
{
	protected String str;
	protected String attrs;

	public Tuple addValue(SootClass klass)
	{
		String line = String.valueOf(SourceInfo.classLineNum(klass));
		addValue(klass.getName(), klass, line);
		return this;
	}

	public Tuple addValue(SootMethod meth)
	{
		return addValue(meth, false, null);
	}

	public Tuple addValue(SootMethod meth, boolean showClassName, String type)
	{
		String line = String.valueOf(SourceInfo.methodLineNum(meth));
		SootClass declKlass = meth.getDeclaringClass();
		addValueWithSig((showClassName ? declKlass.getName() + "." : "") + meth.getName(), 
						declKlass, 
						line,
						(type == null ? "method" : type),
						SourceInfo.chordSigFor(meth));
		return this;
	}
	
	public Tuple addValue(Unit quad)
	{
		SootMethod meth = Program.containerMethod((Stmt) quad);
		if(meth != null){
			String label = quad.toString();//meth.getDeclaringClass().getSourceFileName() + ":"+ quad.getLineNumber();
			addValue(label, meth.getDeclaringClass(), String.valueOf(SourceInfo.stmtLineNum((Stmt) quad)));
		}
		else
			addValue(quad.toString());
		return this;
	}
	
	public Tuple addValue(Object obj)
	{
		if(obj instanceof String)
			addValue((String) obj);
		else if(obj instanceof Unit)
			addValue((Unit) obj);
		else if(obj instanceof SootMethod)
			addValue((SootMethod) obj);
		else if(obj instanceof SootClass)
			addValue((SootClass) obj);
		else if(obj == null)
			return this;
		else
			throw new RuntimeException("Cannot add value of " + obj.getClass() + " type to tuple");
		return this;
	}
	
	public void write(PrintWriter writer)
	{
		writer.print("<tuple"+(attrs != null ? attrs : ""));
		if(str != null){
			writer.println(">");
			writer.println(str);
			writer.println("</tuple>");
		} else
			writer.println("/>");
	}

	public final Tuple setAttr(String key, String value)
	{
		String kvp = " "+key+"=\""+value+"\"";
		if(attrs != null)
			attrs += kvp;
		else
			attrs = kvp;
		return this;
	}

	public final Tuple addValue(String label)
	{
		str = (str != null ? str : "") +
			"\t<value>\n" +
			"\t\t<label><![CDATA["+label+"]]></label>\n" +
			"\t</value>\n";
		return this;
	}

	public final Tuple addValue(String label, SootClass klass, String lineNum)
	{
		return addValue(label, klass, lineNum, null);
	}

	public final Tuple addValue(String label, SootClass klass, String lineNum, String type)
	{
		return addValueWithSig(label, klass, lineNum, type, null);
 	}
	
	public final Tuple addValueWithSig(String label, SootClass klass, String lineNum, String type, String chordSig)
	{
		String srcFile = SourceInfo.filePath(klass);
		str = (str != null ? str : "") +
			"\t<value"+
			(srcFile == null ? "" : (" srcFile=\""+srcFile+"\" lineNum=\""+lineNum+"\"")) +
			(chordSig == null ? "" : (" chordsig=\""+StringEscapeUtils.escapeXml(chordSig)+"\""))+
			(type == null ? "" : (" type=\""+type+"\"")) +
			">\n" 
		    + "\t\t<label><![CDATA["+label+"]]></label>\n"
		    + "\t</value>\n";
		return this;
	}

	//TODO: remove? Added to facilitate hack for Flow Viz
	public final Tuple addRawValue(String label, String srcFile, String lineNum, String type, String chordSig) {
		str = "\t<value"+
			(srcFile == null ? "" : (" srcFile=\""+srcFile+"\" lineNum=\""+lineNum+"\"")) +
			(chordSig == null ? "" : (" chordsig=\""+StringEscapeUtils.escapeXml(chordSig)+"\""))+
			(type == null ? "" : (" type=\""+type+"\"")) +
			">\n" 
		    + "\t\t<label><![CDATA["+label+"]]></label>\n"
		    + "\t</value>\n" + (str != null ? str : "");
		return this;
	}

	public final Tuple addValueWithHighlight(SootClass klass, Expr e)
	{
		String srcFile = SourceInfo.filePath(klass);
		str = (str != null ? str : "") +
			"\t<value srcFile=\""+srcFile+
			"\" lineNum=\""+e.line()+"\""+
			" type=\"expr\""+
			">\n" +
			"\t\t<highlight key=\"taintedVariable\" startpos=\""+e.start()+"\" length=\""+e.length()+"\"/>\n" +
			"\t\t<label><![CDATA["+e.text()+"]]></label>\n" +
			"\t</value>\n";
		return this;
	}
		
	public int hashCode()
	{
		return str == null ? 0 : str.hashCode();
	}
	
	public boolean equals(Object other)
	{
		if(!(other instanceof Tuple))
			return false;
		Tuple o = (Tuple) other;
		if(str == null)
			return o.str == null;
		else
			return str.equals(o.str);
	}
	
	public String toString()
	{
		return str;
	}
}
