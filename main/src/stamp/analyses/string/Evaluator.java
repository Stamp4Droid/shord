package stamp.analyses.string;

import soot.Local;
import soot.Immediate;
import soot.jimple.StringConstant;

import java.util.*;

public class Evaluator
{
	private static final String UNKNOWN = "$stamp$UNKNOWN$stamp$";
	private static final String UNKNOWN_UNKNOWN = UNKNOWN.concat(UNKNOWN);

	Map<Local,Set<String>> localToVals = new HashMap();

	public Evaluator()
	{
	}

	public Set<String> evaluate(Set<Statement> slice, Local local)
	{
		List<Statement> list = new LinkedList();
		for(Statement stmt : slice){
			list.add(0, stmt);
		}
		for(Statement stmt : list){
			Local left = null;
			if(stmt instanceof Assign){
				Immediate right = ((Assign) stmt).right;
				left = ((Assign) stmt).left;
				addVals(left, getVals(right));
			} else if(stmt instanceof Havoc){
				left = ((Havoc) stmt).local;
				Set<String> newVals = new HashSet();
				newVals.add(UNKNOWN);
				addVals(left, newVals);
			} else if(stmt instanceof Concat){
				left = ((Concat) stmt).left;
				concat((Concat) stmt);
			}
			Set<String> newVals = localToVals.get(left);
			if(newVals == null)
				System.out.println("{  }");
			else {
				System.out.print("{ ");
				for(String val : newVals)
					System.out.print(val+" ");
				System.out.println(" }");
			}
		}
		return getVals(local);
	}
	
	/*
	public Set<String> evaluate(Set<Statement> slice, Local local)
	{
		init(slice);

		boolean changed = true;
		while(changed){
			changed = false;
			for(Statement stmt : slice){
				if(stmt instanceof Assign){
					if(assign((Assign) stmt)){
						System.out.print("changed "+ ((Assign) stmt).left + " ");
						for(String val : localToVals.get(((Assign) stmt).left))
							System.out.print(val+" ");
						System.out.println("");
						changed = true;
					}
				} else if(stmt instanceof Concat){
					if(concat((Concat) stmt)){
						System.out.print("changed "+ ((Concat) stmt).left + " ");
						for(String val : localToVals.get(((Concat) stmt).left))
							System.out.print(val+" ");
						System.out.println("");
						changed = true;
					}
				}
			}
		}
		return getVals(local);
	}

	private void init(Set<Statement> slice)
	{
		for(Statement stmt : slice){
			if(stmt instanceof Assign){
				Immediate right = ((Assign) stmt).right;
				Local left = ((Assign) stmt).left;
				addVals(left, getVals(right));
			} else if(stmt instanceof Havoc){
				Local l = ((Havoc) stmt).local;
				Set<String> newVals = new HashSet();
				newVals.add(UNKNOWN);
				addVals(l, newVals);
			}
		}
	}
	*/
	private boolean assign(Assign stmt)
	{
		Immediate right = stmt.right;
		if(!(right instanceof Local))
			return false;
		Local left = stmt.left;
		return addVals(left, getVals(right));
	}

	private boolean concat(Concat stmt)
	{
		Immediate right1 = stmt.right1;
		Immediate right2 = stmt.right2;
		Local left = stmt.left;
		
		Set<String> vals1 = getVals(right1);
		Set<String> vals2 = getVals(right2);
				
		if(vals1 == null || vals2 == null)
			return false;
		
		Set<String> newVals = new HashSet();
		for(String v1 : vals1){
			for(String v2 : vals2){
				String newV;
				newV = v1.concat(v2);
				newV = newV.replace(UNKNOWN_UNKNOWN, UNKNOWN);
				if(newV.length() < 50)
					newVals.add(newV);
			}
		}
		return addVals(left, newVals);
	}

	private Set<String> getVals(Immediate i)
	{
		Set<String> ret;
		if(i instanceof StringConstant){
			ret = new HashSet();
			ret.add(((StringConstant) i).value);
		} else {
			ret = localToVals.get((Local) i);
		}
		return ret;
	}

	private boolean addVals(Local local, Set<String> newVals)
	{
		if(newVals == null)
			return false;
		Set<String> vals = localToVals.get(local);
		if(vals == null){
			vals = new HashSet();
			localToVals.put(local, vals);
		}
		return vals.addAll(newVals);
	}
}