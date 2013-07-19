package stamp.reporting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import shord.analyses.*;
import shord.program.Program;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import chord.util.tuple.object.Pair;

import soot.jimple.Stmt;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import stamp.paths.*;
import stamp.srcmap.SourceInfo;

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

		/*
		//This code should work with Manolis' solvergen stuff
		try {
			BufferedReader br = new BufferedReader(new FileReader("solvergen/solvergenpaths.out"));

			System.out.println("SOLVERGENPATHS");
			String line = null;
			while ((line = br.readLine()) != null) {
				Category c = null;
				if (line.length() == 0) {
					//Empty line; delim between flows
					c = null;

				} else if (line.charAt(0) == '$') {
					//should indicate first line of new flow, format $SRC-->!SINK
					c = makeOrGetPkgCat(new SootClass(line));

				} else {
					//edge of the path
					String[] tokens = line.split("^--|^<-|-> \\[|-- \\[|\\]:");
					System.out.println(Arrays.toString(tokens));
				}

			}
			System.out.println("SOLVERGENPATHSEND");

		} catch (Exception e) {
			System.err.println("Error reading solvergen/solvergenpaths.out");
			e.printStackTrace();
		}
		*/

		final ProgramRel relSrcSinkFlow = (ProgramRel)ClassicProject.g().getTrgt("flow");

		relSrcSinkFlow.load();

		System.out.println("SOLVERGENPATHS");

		for (Path p : PathsAdapter.getPaths()) {
			Category mc = makeOrGetPkgCat(new SootClass(p.start + " --> " + p.end));
			Set<String> seenLocs = new HashSet();
			for (Step s : p.steps) {
				Category c = mc;
				if (s.target instanceof CtxtVarPoint || s.target instanceof CtxtObjPoint) {
					String progress = "";
					for (Unit u : ((CtxtObjPoint)s.target).ctxt.getElems()) {
						Stmt stm  = (Stmt)u;
						SootMethod method = Program.containerMethod(stm);

						String sourceFileName = (method == null) ? "" : SourceInfo.filePath(method.getDeclaringClass());
						int methodLineNum = SourceInfo.methodLineNum(method);
						if (methodLineNum < 0) {
							methodLineNum = 0;
						}
						String methName = method.getName();

						c = c.makeOrGetSubCat(methName);

						progress += stm.toString();

						if (!seenLocs.contains(progress)) {
							c.addRawValue(methName, sourceFileName, ""+methodLineNum, "method", "");
							seenLocs.add(progress);
						}
					}
					if (s.target instanceof CtxtVarPoint) {
						VarNode v = ((CtxtVarPoint)s.target).var;
						Local local = null;
						SootMethod method = null;
						if(v instanceof LocalVarNode) {
							LocalVarNode localRegister = (LocalVarNode)v;
							local = localRegister.local;
							method = localRegister.meth;
						} else if(v instanceof ThisVarNode) {
							ThisVarNode thisRegister = (ThisVarNode)v;
							method = thisRegister.method;
						} else if(v instanceof ParamVarNode) {
							ParamVarNode paramRegister = (ParamVarNode)v;
							method = paramRegister.method;
						} else if(v instanceof RetVarNode) {
							RetVarNode retRegister = (RetVarNode)v;
							method = retRegister.method;
						} 

						// link the method
						if (method == null)
							continue;
						String sourceFileName = SourceInfo.filePath(method.getDeclaringClass());
						int methodLineNum = SourceInfo.methodLineNum(method);

						String methStr = sourceFileName + " " + methodLineNum + " " +method.getName() + " ";

						//tokens[2] is method name
						//tokens [1] is line # of method
						//tokens[0] is source file

						c.newTuple().addRawValue(method.getName(), sourceFileName, ""+methodLineNum, "method", "")
							.addValue("Label: " + s.symbol + " " + v.toString());

					} else if (s.target instanceof CtxtObjPoint) {
						c.newTuple().addRawValue("Obj", "", "0", "method", "")
							.addValue("Label: " + "CtxtObj");

					}
				}
				//System.out.println((s.reverse ? "<-" : "--" ) + s.symbol +
				//				   (s.reverse ? "-- " : "-> " ) + s.target);
			//System.out.println();
			}
		}
	}
}
