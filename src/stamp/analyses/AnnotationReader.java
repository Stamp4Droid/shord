package stamp.analyses;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import soot.SootMethod;

/**
 * @author Saswat Anand
**/
@Chord(name = "annot-java",
	   consumes = { "M", "Z" },
	   produces = { "SRC", "SINK", 
					"DirectArgArgFlow", "DirectArgRetFlow", 
					"DirectArgSinkFlow", "DirectRetSinkFlow",
					"DirectSrcArgFlow", "DirectSrcRetFlow" },
	   namesOfTypes = { "SRC", "SINK" },
	   types = { DomSRC.class, DomSINK.class },
	   namesOfSigns = { "DirectArgArgFlow", "DirectArgRetFlow", 
						"DirectArgSinkFlow", "DirectRetSinkFlow",
						"DirectSrcArgFlow", "DirectSrcRetFlow" },
	   signs = { "M0,Z0,Z1:M0_Z0_Z1", "M0,Z0:M0_Z0", 
				 "M0,Z0,SINK0:SINK0_M0_Z0", "M0,SINK0:SINK0_M0",
				 "SRC0,M0,Z0:SRC0_M0_Z0", "SRC0,M0:SRC0_M0" }
	   )
public class AnnotationReader extends JavaAnalysis
{
	private static Map<String,List<Flow>> flowMap;

	private static void readAnnotations() throws IOException
	{
		assert flowMap == null;
		flowMap = new HashMap();

		BufferedReader reader = new BufferedReader(new FileReader(new File("stamp_annotations.txt")));
		String line = reader.readLine();
		while(line != null){
			final String[] tokens = line.split(" ");
			String chordMethodSig = tokens[0];
			String from = tokens[1];
			String to = tokens[2];

			List<Flow> flows = flowMap.get(chordMethodSig);
			if(flows == null){
				flows = new ArrayList();
				flowMap.put(chordMethodSig, flows);
			}

			Flow f = null;
			try{
				f = determineFlow(from, to);
				flows.add(f);
				//System.out.println("flow: " +chordMethodSig + " " + f);
			} catch(NumberFormatException e) {
				System.out.println("Unsupported annotation type "+line);
				//throw new Error(line, e);
			}
			line = reader.readLine();
		}
		reader.close();
	}

	private static Flow determineFlow(String from, String to) throws NumberFormatException
	{
		if(from.charAt(0) == '$') {
			if(to.equals("-1"))
				return new RelDirectSrcRetFlow.Tuple(from);
			return new RelDirectSrcArgFlow.Tuple(from, Integer.parseInt(to));
		} else {
			assert !(from.charAt(0) == '!');
			Integer fromArgIndex = Integer.parseInt(from);
			if(to.charAt(0) == '!'){
				if(from.equals("-1"))
					return new RelDirectRetSinkFlow.Tuple(to);
				else
					return new RelDirectArgSinkFlow.Tuple(fromArgIndex, to);
			}
			if(to.equals("-1"))
				return new RelDirectArgRetFlow.Tuple(fromArgIndex);
			Integer toArgIndex = Integer.parseInt(to);
			return new RelDirectArgArgFlow.Tuple(fromArgIndex, toArgIndex);
		}
	}

	static Iterable<Flow> flows(SootMethod meth)
	{
		if(flowMap == null) {
			try{
				readAnnotations();
			} catch(IOException e) {
				throw new Error(e);
			}
		}
		List<Flow> ret = flowMap.get(meth.toString());
		if(ret == null)
			ret = Collections.EMPTY_LIST;
		return ret;
	}

	static Iterable<String> srcs(SootMethod meth)
	{
		Iterable<Flow> flows = flows(meth);
		if(flows == null)
			return Collections.EMPTY_LIST;

		List<String> ret = new ArrayList();

		for(Flow flow : flows(meth)){
			String src = flow.src();
			if(src != null)
				ret.add(src);
		}
		return ret;
	}

	static Iterable<String> sinks(SootMethod meth)
	{
		Iterable<Flow> flows = flows(meth);
		if(flows == null)
			return Collections.EMPTY_LIST;

		List<String> ret = new ArrayList();

		for(Flow flow : flows(meth)){
			String sink = flow.sink();
			if(sink != null)
				ret.add(sink);
		}
		return ret;
	}
}
