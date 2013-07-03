package stamp.jcflsolver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.*;

import stamp.jcflsolver.Util.Pair;

public class FactsWriter {
    public static void write(Graph g, File outputDir) throws IOException {
	write(g, outputDir, false);
    }
	
    public static void write(Graph g, File outputDir, boolean printPaths) throws IOException {
	write(g, outputDir, printPaths, new HashSet<String>());
    }
	
    public static void write(Graph g, File outputDir, boolean printPaths, Set<String> terminals) throws IOException
    {
	for(String outputRel : g.outputRels()){
	    int k = g.symbolToKind(outputRel);

	    if (g.isTerminal(k)) {
		System.out.println("Not outputing "+outputRel+" because it is a terminal symbol.");
	    }

	    String fileName = outputRel+".dat";
	    System.out.println("Writing "+fileName);
	    PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputDir, fileName))));

	    for(Node node : g.allNodes()) {
		String fromName = node.name;
		for(Edge edge : node.getOutEdges(k)){
		    String positiveWeightEdges = "";
					
		    if(printPaths) {
			StringBuilder sb = new StringBuilder();
			//sb.append("[");
			sb.append(" ");
			//for(Edge e : g.getPositiveWeightInputs(edge)) {
			for(Pair<Edge,Boolean> pair : g.getPath(edge, terminals)) {
			    EdgeData e = pair.getX().getData(g);
			    sb.append(e.symbol + (pair.getY() ? "" : "Bar") + "," + e.from + "," + e.to + (e.hasLabel() ? "," + e.label : "") + ";");
			}
			//sb.append("]");
			positiveWeightEdges = sb.toString();
		    }
						
		    if(edge instanceof LabeledEdge)
			writer.println(fromName + " " + edge.to.name + " " + ((LabeledEdge) edge).label + " " + edge.weight + positiveWeightEdges);
		    else
			writer.println(fromName + " " + edge.to.name + " * " + edge.weight + positiveWeightEdges);
		}
	    }
			
	    writer.close();
	}
    }
}