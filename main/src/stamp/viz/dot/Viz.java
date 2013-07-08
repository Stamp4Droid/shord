package stamp.viz.dot;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class Viz<X> {
    private int vizCount = 0;
    private final String filename;
	
    public Viz(String filename) {
	this.filename = filename;
    }
	
    //public abstract JSONObject vizJSON(X x);
    public abstract DotObject vizDot(X x);

    public void viz(X x) {
	try {
	    String fullFilename = this.filename + (this.vizCount++);
	    //File file = File.createTempFile(fullFilename, ".dot");
	    File file = new File(fullFilename + ".dot");
	    //file.deleteOnExit();
			
	    PrintWriter pw = new PrintWriter(file);
	    pw.println(this.vizDot(x).toDotString());
	    pw.close();

	    String command = "dot -Tpdf -o" + fullFilename + ".pdf " + file.getCanonicalPath();
	    System.out.println("Executing command " + command + ".");
	    //Process process = Runtime.getRuntime().exec(command);
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }
}
