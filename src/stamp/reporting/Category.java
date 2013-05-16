package stamp.reporting;

import java.io.*;
import java.util.*;

import soot.SootClass;
import soot.SootMethod;

/*
 * @author Saswat Anand
**/
public class Category extends Tuple
{
	protected Map<Object,Category> subCategories = new HashMap();
	protected List<Tuple> tuples = new ArrayList();
	protected String type;

	public Category()
	{
		this(null);
	}

	public Category(Object key)
	{
		addValue(key);
		if(key instanceof SootClass)
			type = "class";
		else if(key instanceof SootMethod)
			type = "method";
	}

	public Category makeOrGetSubCat(Object key)
	{
		Category sc = subCategories.get(key);
		if(sc == null){
			sc = new Category(key);
			subCategories.put(key, sc);
		}
		return sc;
	}
	
	public void write(PrintWriter writer)
	{
		if(type != null)
			writer.println("<category type=\""+type+"\">");
		else
			writer.println("<category>");
		writer.println(str);
		for(Tuple t : tuples)
			t.write(writer);
		
		for(Category c : sortSubCats())
			c.write(writer);

		writer.println("</category>");
	}
	
	public Tuple newTuple()
	{
		Tuple tuple = new Tuple();
		return addTuple(tuple);
	}
	
	public Tuple addTuple(Tuple t)
	{
		tuples.add(t);
		return t;
	}

	public Category makeOrGetPkgCat(SootClass klass)
	{
		String name = klass.getName();
		return makeOrGetPkgCat(name, klass);
	}

	public Category makeOrGetPkgCat(SootMethod method)
	{
		SootClass klass = method.getDeclaringClass();
		return makeOrGetPkgCat(klass).makeOrGetSubCat(method);
	}
	
	private Category makeOrGetPkgCat(String pkg, SootClass klass)
	{
		int index = pkg.indexOf('.');
		Category ret;
		if(index < 0)
			ret = makeOrGetSubCat(klass);
		else {
			ret = makeOrGetSubCat(pkg.substring(0,index));
			ret = ret.makeOrGetPkgCat(pkg.substring(index+1), klass);
		}
		return ret;
	}
	
	protected List<Category> sortSubCats()
	{
		//sort
		Map<String,Category> strToSubcat = new HashMap();
		List<String> strs = new ArrayList();
		for(Category sc : subCategories.values()){
			strs.add(sc.str);
			strToSubcat.put(sc.str, sc);
		}
		Collections.sort(strs);
		
		List<Category> ret = new ArrayList();
		for(String str : strs){
			Category c = strToSubcat.get(str);
			ret.add(c);
		}
		return ret;
	}
}