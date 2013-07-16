package stamp.reporting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import chord.util.tuple.object.Pair;

import soot.SootClass;
import soot.SootMethod;

public class SrcSinkFlowViz extends XMLVizReport
{
	public SrcSinkFlowViz()
	{
		super("Flow Viz");
	}

    public void generate()
	{
		/*
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt("out_taintSrc");
		rel.load();

		Iterable<Pair<String,SootMethod>> res = rel.getAry2ValTuples();
		for(Pair<String,SootMethod> pair : res) {
			SootMethod srcMethod = pair.val1;
			makeOrGetSubCat(srcMethod.getDeclaringClass()).newTuple()
				.addValue(srcMethod)
				.addValue("Label: "+pair.val0);
		}

		rel.close();
		*/

		try {
			BufferedReader brdr = new BufferedReader(new FileReader("cfl/Src2SinkFiles.out"));

			while (brdr.ready()) {
				String filename = brdr.readLine();

				HashMap<String, SootClass> classes = new HashMap<String, SootClass>();
				Category c = makeOrGetPkgCat(new SootClass(filename));

				try {
					BufferedReader br = new BufferedReader(new FileReader("cfl/"+filename +".out"));
					String line;
					while ((line = br.readLine()) != null) {
						String[] tokens = line.split(" ");
						if (tokens[0].charAt(0) == '<') continue;

						Category mc = c;
						String[] context = tokens[3].split("\\.");
						for (String s : context) {
							mc = mc.makeOrGetSubCat(s);
							mc.addRawValue(s, "EMPTY", "EMPTY", "method", "");
						}
						mc.newTuple().addRawValue(tokens[2], tokens[0], tokens[1], "method", "")
							.addValue("Label: " + tokens[2]);
					}

				} catch (Exception e) {
					System.err.println("No Flow Viz Intermediate File Found");
				}
			}
		} catch (Exception e) {
			System.err.println("Failed to open cfl/Src2SinkFiles.out");
		}
	}
}
