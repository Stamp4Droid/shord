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
		/*
		//This code is for Osbert's JCFL solver stuff. Will fail if JCFL doesn't come through.
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
		*/

		//This code should work with Manolis' solvergen stuff
		try {
			BufferedReader br = new BufferedReader(new FileReader("solvergen/solvergenpaths.out"));

			while ((line = br.readLine()) != null) {
				Category c = null;
				if (line.length() == 0) {
					//Empty line; delim between flows
					c = null;

				} else if (line.charAt(0) == '$') {
					//should indicate first line of new flow, format $SRC-->!SINK
					c = makeOrGetPkgCat(new SootClass(line));

				} else {
					/*
					--label2Ref-> [$r13 = virtualinvoke $r12.<android.telephony.TelephonyManager: java.lang.String getDeviceId()>()@<stamp.stanford.malware.Malware: void onCreate(android.os.Bundle)>,virtualinvoke $r1.<android.app.Activity: void onCreate(android.os.Bundle)>(null)@<android.app.Activity$1: void run()>]:return@<android.telephony.TelephonyManager: java.lang.String getDeviceId()>
					*/
					System.out.p
					//edge of the path
				}

			}

		} catch (Exception e) {
			System.err.println("Error reading solvergen/solvergenpaths.out");
			e.printStackTrace();
		}
	}
}
