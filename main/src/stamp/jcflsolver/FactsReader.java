package stamp.jcflsolver;

import java.io.*;
import java.util.*;

public class FactsReader {
	
	public static void read(Graph g, File inputDir, File outputDir) throws IOException {
		for(int k = 0; k < g.numKinds(); k++) {
			String symbol = g.kindToSymbol(k);
			String fileName = symbol+".dat";
			File f;
			if(g.isTerminal(k)){
				f = new File(inputDir, fileName);
				if(!f.exists()){
					System.out.println("Warning: .dat file for non-terminal "+symbol+" not found.");
					continue;
				}
			} else {
				//this could be a relation output from a prev analysis
				f = new File(outputDir, fileName);
				if(f.exists())
					System.out.println("Reading .dat file for non-terminal "+symbol+" generated from prev analysis.");
				else
					continue;
			} 
			BufferedReader reader = new BufferedReader(new FileReader(f));

			String line;
			short kindToWeight = g.kindToWeight(k);
			while((line = reader.readLine()) != null){
				//System.out.println(line);
				String[] tokens = line.split(" ");
				String fromName = tokens[0];
				String toName = tokens[1];
				String index = tokens[2];
				
				short weight = tokens.length >= 4 ? Short.parseShort(tokens[3]) : kindToWeight; 
				
				if(index.equals("*"))
					g.addWeightedInputEdge(fromName, toName, k, weight);
				else
					g.addWeightedInputEdge(fromName, toName, k, Integer.parseInt(index), weight);
			}
			
			reader.close();

			/*
			System.out.println("******* "+g.kindToSymbol(k));
			for(Node node : g.allNodes()){
				String fromName = node.name;
				for(Edge edge : node.getOutEdges(k)){
					if(edge instanceof LabeledEdge)
						System.out.printf("%s %d\n", fromName, edge.to.name, ((LabeledEdge) edge).label);
					else
						System.out.printf("%s %s *\n", fromName, edge.to.name);
				}
			}
			break;
			*/
		}
	}
}