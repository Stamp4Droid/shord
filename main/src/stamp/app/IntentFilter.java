package stamp.app;

import java.util.*;

public class IntentFilter
{
	public final Set<String> actions = new HashSet();
	public final Set<String> dataTypes = new HashSet();
	
	private int priority;

	public void setPriority(String p)
	{
		int pr = Integer.parseInt(p);
		this.priority = pr;
	}
	
	public void addAction(String action)
	{
		actions.add(action);
	}

	public void addDataType(String dt)
	{
		dataTypes.add(dt);
	}
	
	public boolean isMAIN()
	{
		for(String act : actions){
			if(act.equals("android.intent.action.MAIN"))
				return true;
		}
		return false;
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder("intent-filter: { ");
		builder.append("actions: {");
		int len = actions.size();
		int i = 0;
		for(String act : actions){
			builder.append(act);
			if(i < (len-1))
				builder.append(", ");
			i++;
		}
		builder.append("} ");

		builder.append("datatype: {");
		len = dataTypes.size();
		i = 0;
		for(String dt : dataTypes){
			builder.append(dt);
			if(i < (len-1))
				builder.append(", ");
			i++;
		}
		builder.append("} ");

		builder.append("} ");
		return builder.toString();
	}
}
