package stamp.app;

import java.util.*;

public class Layout
{
	public final List<Widget> widgets = new ArrayList();
	public final Set<String> callbacks = new HashSet();
	public final int id;
	public final String fileName;

	Layout(int id, String fileName)
	{
		this.id = id;
		this.fileName = fileName;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder("{");
		builder.append("\"file\": \""+fileName+"\", ");
		builder.append("\"id\": \""+id+"\", ");
		builder.append("\"widgets\": [");
		int len = widgets.size();
		for(int i = 0; i < len; i++){
			builder.append(widgets.get(i).toString());
			if(i < (len-1))
				builder.append(", ");
		}
		builder.append("], ");
		builder.append("\"callbacks\": [");
		len = callbacks.size();
		int i = 0;
		for(String cb : callbacks){
			builder.append("\""+cb+"\"");
			if(i < (len-1))
				builder.append(", ");
			i++;
		}
		builder.append("]");
		builder.append("}");
		return builder.toString();
	}
	
}