package stamp.reporting;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/*
 * @author Saswat Anand
**/
public abstract class XMLVizReport extends XMLReport
{
	protected XMLVizReport(String title) {
		super(title);
	}

	@Override
	public void write(PrintWriter writer)
	{
		writer.println("<root>");
		writer.println("<title>"+getTitle()+"</title>");
		for(Tuple t : tuples)
			t.write(writer);
		for(Category sc : subCategories.values())
			sc.writeInsertionOrder(writer);
		writer.println("</root>");
	}

}