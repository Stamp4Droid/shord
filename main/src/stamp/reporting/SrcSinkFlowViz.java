package stamp.reporting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
		try {
			BufferedReader brdr = new BufferedReader(new FileReader("cfl/Src2SinkFiles.out"));

			while (brdr.ready()) {
				String filename = brdr.readLine();

				Set<String> seenLocs = new HashSet();
				Category c = makeOrGetPkgCat(new SootClass(filename));

				try {
					BufferedReader br = new BufferedReader(new FileReader("cfl/"+filename +".out"));
					String line;
					while ((line = br.readLine()) != null) {
						String[] tokens = line.split(" ");
						if (tokens[0].charAt(0) == '<') continue;

						Category mc = c;
						String[] context = tokens[3].split("@");
						String progress = "";
						for (String s : context) {
							String[] methTokens = s.split(":");
							assert methTokens.length == 3;
							mc = mc.makeOrGetSubCat(methTokens[2]);
							progress += s;
							if (!seenLocs.contains(progress)) {
								mc.addRawValue(methTokens[2], methTokens[0], methTokens[1], "method", "");
								seenLocs.add(progress);
							}
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
