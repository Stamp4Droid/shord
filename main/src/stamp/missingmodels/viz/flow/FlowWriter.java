package stamp.missingmodels.viz.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import stamp.missingmodels.util.ConversionUtils;
import stamp.missingmodels.util.FileManager.FileType;
import stamp.missingmodels.util.FileManager.StampFile;
import stamp.missingmodels.util.StubLookup;
import stamp.missingmodels.util.StubLookup.StubLookupKey;
import stamp.missingmodels.util.StubLookup.StubLookupValue;
import stamp.missingmodels.util.Util.Counter;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.jcflsolver.Edge;
import stamp.missingmodels.util.jcflsolver.EdgeData;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.viz.flow.FlowObject.AliasCompressedFlowObject;

/*
 * Collection of StampFile generators to print various
 * outputs for the JCFLSolver analysis.
 * 
 * @author Osbert Bastani
 */
public class FlowWriter {
	/*
	 * Outputs a list of all the stubs used in a src2sink flow.
	 */
	public static class AllStubsFile implements StampFile {
		private final StubLookup s;

		public AllStubsFile(StubLookup s) {
			this.s = s;
		}

		@Override
		public String getName() {
			return "AllStubs.txt";
		}

		@Override
		public FileType getType() {
			return FileType.OUTPUT;
		}

		@Override
		public String getContent() {
			StringBuilder sb = new StringBuilder();
			Set<String> printedStubs = new HashSet<String>();
			for(Map.Entry<StubLookupKey,StubLookupValue> entry : this.s.entrySet()) {
				StubLookupKey key = entry.getKey();
				StubLookupValue info = entry.getValue();
				if(key.source.startsWith("M") && !printedStubs.contains(key.source)) {
					sb.append(key.source + " " + info.method.toString()).append("\n");
					printedStubs.add(key.source);
				}
				if(key.sink.startsWith("M") && !printedStubs.contains(key.sink)) {
					sb.append(key.sink + " " + info.method.toString()).append("\n");
					printedStubs.add(key.sink);
				}
			}
			return sb.toString();
		}
	}

	/*
	 * Output a list of all stub models included in the analysis.
	 */
	public static class StubModelsFile implements StampFile {
		private final StubLookup s;

		public StubModelsFile(StubLookup s) {
			this.s = s;
		}

		@Override
		public String getName() {
			return "StubModels.txt";
		}
		@Override
		public FileType getType() {
			return FileType.OUTPUT;
		}

		@Override
		public String getContent() {
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<StubLookupKey,StubLookupValue> entry : this.s.entrySet()) {
				StubLookupKey key = entry.getKey();
				StubLookupValue info = entry.getValue();
				sb.append(key.symbol + "," + key.source + "," + key.sink + " " + info.toString()).append("\n");
			}
			return sb.toString();
		}
	}

	/*
	 * Retrieves the positive weight edges from the src2sink edges.
	 */
	private static MultivalueMap<Edge,Pair<Edge,Boolean>> getPositiveWeightEdges(Graph g) {
		MultivalueMap<Edge,Pair<Edge,Boolean>> positiveWeightEdges = new MultivalueMap<Edge,Pair<Edge,Boolean>>();
		for(Edge edge : g.getEdges("Src2Sink")) {
			positiveWeightEdges.ensure(edge);
			List<Pair<Edge,Boolean>> path = g.getPath(edge);
			for(Pair<Edge,Boolean> pair : path) {
				if(pair.getX().weight > 0) {
					positiveWeightEdges.add(edge, pair);
				}
			}
		}
		return positiveWeightEdges;
	}

	/*
	 * For each src2sink edge in the graph, prints a list of all the
	 * stub models that are used in the edge.
	 */
	public static class StubInputsFile implements StampFile {
		private final Graph g;
		private final StubLookup s;

		public StubInputsFile(Graph g, StubLookup s) {
			this.g = g;
			this.s = s;
		}

		@Override
		public String getName() {
			return "StubInputs.txt";
		}

		@Override
		public FileType getType() {
			return FileType.OUTPUT;
		}

		@Override
		public String getContent() {
			StringBuilder sb = new StringBuilder();
			MultivalueMap<Edge,Pair<Edge,Boolean>> positiveWeightEdges = getPositiveWeightEdges(this.g);
			for(Edge edge : positiveWeightEdges.keySet()) {
				String source = ConversionUtils.getNodeInfoTokens(edge.from.getName())[1];
				String sink = ConversionUtils.getNodeInfoTokens(edge.to.getName())[1];
				sb.append(source + " -> " + sink).append("\n");

				for(Pair<Edge,Boolean> pair : positiveWeightEdges.get(edge)) {
					EdgeData data = pair.getX().getData(this.g);
					boolean forward = pair.getY();
					
					StubLookupValue info = s.get(new StubLookupKey(data.symbol, data.from, data.to));
					String line;
					if(info != null) {
						line = data.toString(forward) + ": " + info.toString();
					} else {
						line = data.toString(forward) + ": " + "NOT_FOUND";
					}
					sb.append(line).append("\n");
				}
			}
			sb.append("\n");
			return sb.toString();
		}
	}
	
	/*
	 * Prints a list of all stub models used in src2sink edges,
	 * including the number of times that they are used.
	 */
	public static class AllStubInputsFile implements StampFile {
		private final Graph g;
		private final StubLookup s;

		public AllStubInputsFile(Graph g, StubLookup s) {
			this.g = g;
			this.s = s;
		}

		@Override
		public String getName() {
			return "AllStubInputs.txt";
		}

		@Override
		public FileType getType() {
			return FileType.OUTPUT;
		}

		@Override
		public String getContent() {
			StringBuilder sb = new StringBuilder();
			Counter<String> keys = new Counter<String>();
			MultivalueMap<Edge,Pair<Edge,Boolean>> positiveWeightEdges = getPositiveWeightEdges(this.g);
			for(Edge edge : positiveWeightEdges.keySet()) {
				for(Pair<Edge,Boolean> pair : positiveWeightEdges.get(edge)) {
					EdgeData data = pair.getX().getData(this.g);
					boolean forward = pair.getY();
					
					StubLookupValue info = s.get(new StubLookupKey(data.symbol, data.from, data.to));
					String line;
					if(info != null) {
						line = data.toString(forward) + ": " + info.toString();
					} else {
						line = data.toString(forward) + ": " + "NOT_FOUND";
					}
					keys.increment(line);
				}
			}
			for(String key: keys.sortedKeySet()) {
				sb.append(key + " " + keys.getCount(key)).append("\n");
			}
			sb.append("\n");
			return sb.toString();
		}
	}
	
	/*
	 * Returns a dot file visualizing the flow graph.
	 */
	public static StampFile getFlowGraphViz(Graph g, StubLookup s) {
		List<Pair<String,String>> edges = new ArrayList<Pair<String,String>>();
		Set<String> sources = new HashSet<String>();
		Set<String> sinks = new HashSet<String>();
		for(Edge edge : g.getEdges("Src2Sink")) {
			List<Pair<Edge,Boolean>> path = g.getPath(edge);

			if(path.size() >= 2) {
				Pair<Edge,Boolean> source = path.get(0);
				EdgeData sourceData = source.getX().getData(g);
				StubLookupValue sourceInfo = s.get(new StubLookupKey(sourceData.symbol, sourceData.from, sourceData.to));

				Pair<Edge,Boolean> sink = path.get(path.size()-1);
				EdgeData sinkData = sink.getX().getData(g);
				StubLookupValue sinkInfo = s.get(new StubLookupKey(sinkData.symbol, sinkData.from, sinkData.to));

				edges.add(new Pair<String,String>(sourceData.from, sinkData.to));

				if(sourceData.symbol.equals("cs_srcRefFlowNew") || sourceData.symbol.equals("cs_srcPrimFlowNew")) {
					sources.add(sourceData.from);
				}
				if(sinkData.symbol.equals("cs_refSinkFlowNew") || sinkData.symbol.equals("cs_primSinkFlowNew")) {
					sinks.add(sinkData.to);
				}
				
				if(sourceInfo != null && sinkInfo != null) {
		    		edges.add(new Pair<String,String>(sourceInfo.toString(), sinkInfo.toString()));
				} else {
		    		System.out.println("ERROR: Src2Sink path length too short!");
				}
			}
		}
		return FlowGraphViz.viz.viz(new FlowGraphViz.FlowGraph(edges, sources, sinks));
	}

	/*
	 * Returns a list of files that are the HTML objects for the visualization.
	 */
	public static Set<StampFile> viz(final Graph g, final StubLookup s) {
		Set<StampFile> files = new HashSet<StampFile>();
		final Set<String> terminals = new HashSet<String>();
		terminals.add("FlowsTo");
		for(final Edge edge : g.getEdges("Src2Sink")) {
			String[] sourceTokens = ConversionUtils.getNodeInfoTokens(edge.from.getName());
			final String source = sourceTokens[1].substring(1);

			String[] sinkTokens = ConversionUtils.getNodeInfoTokens(edge.to.getName());
			final String sink = sinkTokens[1].substring(1);

			files.add(new StampFile() {
				@Override
				public String getName() {
					return source + "2" + sink + ".out";
				}

				@Override
				public FileType getType() {
					return FileType.OUTPUT;
				}

				@Override
				public String getContent() {
					//return new MethodCompressedFlowObject(g.getPath(edge), g, s).toString();
					return new AliasCompressedFlowObject(g.getPath(edge, terminals), g, s).toString();
				}
				
			});
		}
		return files;
	}
}
