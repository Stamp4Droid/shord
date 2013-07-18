package stamp.missingmodels.util.viz.jcflsolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import stamp.missingmodels.util.FileManager;
import stamp.missingmodels.util.FileManager.FileType;
import stamp.missingmodels.util.FileManager.StampFile;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.jcflsolver.Edge;
import stamp.missingmodels.util.jcflsolver.EdgeData;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.util.jcflsolver.LabeledEdge;
import stamp.missingmodels.util.jcflsolver.Node;

public class JCFLRelationFile implements StampFile {
	private final Graph g;
	private final String symbol;
	private final FileType fileType;
	private final boolean printPaths;
	private final Set<String> terminals = new HashSet<String>();

	public JCFLRelationFile(FileType fileType, Graph g, String symbol) {
		this(fileType, g, symbol, false, null); 
	}

	public JCFLRelationFile(FileType fileType, Graph g, String symbol, boolean printPaths) {
		this(fileType, g, symbol, printPaths, null);
	}

	public JCFLRelationFile(FileType fileType, Graph g, String symbol, boolean printPaths, Set<String> terminals) {
		this.g = g;
		this.symbol = symbol;
		this.fileType = fileType;
		this.printPaths = printPaths;
		if(terminals != null) {
			this.terminals.addAll(terminals);
		}
	}

	@Override
	public String getName() {
		return this.symbol + ".dat";
	}

	@Override
	public FileType getType() {
		return this.fileType;
	}

	@Override
	public String getContent() {
		int kind;
		try {
			kind = this.g.symbolToKind(this.symbol);
		} catch(RuntimeException e) {
			return "";
		}

		System.out.println("Writing " + this.getName());

		StringBuilder sb = new StringBuilder();
		for(Node node : this.g.allNodes()) {
			String fromName = node.getName();
			for(Edge edge : node.getOutEdges(kind)){
				String path = "";
				if(this.printPaths) {
					StringBuilder sbPath = new StringBuilder();
					sbPath.append(" ");
					for(Pair<Edge,Boolean> pair : g.getPath(edge, terminals)) {
						EdgeData e = pair.getX().getData(g);
						sbPath.append(e.symbol + (pair.getY() ? "" : "Bar") + "," + e.from + "," + e.to + (e.hasLabel() ? "," + e.label : "") + ";");
					}
					path = sbPath.toString();
				}

				if(edge instanceof LabeledEdge) {
					sb.append(fromName + " " + edge.to.getName() + " " + ((LabeledEdge) edge).label + " " + edge.weight + path);
				} else {
					sb.append(fromName + " " + edge.to.getName() + " * " + edge.weight + path);
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public void read(FileManager manager) throws IOException {
		int kind;
		try {
			kind = this.g.symbolToKind(this.symbol);
		} catch(RuntimeException e) {
			return;
		}

		File f = manager.getFile(this);
		if(this.g.isTerminal(kind)){
			if(!f.exists()){
				System.out.println("Warning: .dat file for non-terminal " + this.symbol + " not found.");
				return;
			}
		} else {
			if(f.exists()) {
				System.out.println("Reading .dat file for non-terminal " + this.symbol + " generated from prev analysis.");
			} else {
				return;
			}
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(f));

		String line;
		short kindToWeight = this.g.kindToWeight(kind);
		while((line = reader.readLine()) != null) {
			String[] tokens = line.split(" ");
			String fromName = tokens[0];
			String toName = tokens[1];
			String index = tokens[2];

			short weight = tokens.length >= 4 ? Short.parseShort(tokens[3]) : kindToWeight; 

			if(index.equals("*")) {
				this.g.addWeightedInputEdge(fromName, toName, kind, weight);
			} else {
				this.g.addWeightedInputEdge(fromName, toName, kind, Integer.parseInt(index), weight);
			}
		}

		reader.close();
	}
}
