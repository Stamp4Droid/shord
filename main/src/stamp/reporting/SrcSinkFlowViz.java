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

		try {
			final ProgramRel relSrcSinkFlow = (ProgramRel)ClassicProject.g().getTrgt("flow");

			relSrcSinkFlow.load();

			System.out.println("SOLVERGENPATHS");

			int count = 0;
			for (Path p : PathsAdapter.getPaths()) {
				count += 1;
				String flowname = count + ") "+p.start + " --> " + p.end;
				Category mc = makeOrGetPkgCat(new SootClass(flowname.replace('.','_')));
				Set<String> seenLocs = new HashSet();

				for (Step s : p.steps) {
					if (s.target instanceof CtxtVarPoint) {
						String progress = "";
						Unit[] elems = ((CtxtObjPoint)s.target).ctxt.getElems();
						Category c = mc;
						System.out.println(s.target);
						for (int i = elems.length - 1; i >= 0; --i) {
							Stmt stm  = (Stmt)elems[i];
							SootMethod method = Program.containerMethod(stm);

							String sourceFileName = (method == null) ? "" : SourceInfo.filePath(method.getDeclaringClass());
							int methodLineNum = SourceInfo.methodLineNum(method);
							if (methodLineNum < 0) {
								methodLineNum = 0;
							}
							String methName = method.getName();
							System.out.println(methName);

							c = c.makeOrGetSubCat(method);

							progress += method.getNumber();
							System.out.println(progress);

							if (!seenLocs.contains(progress)) {
								c.addRawValue(methName, sourceFileName, ""+methodLineNum, "method", "");
								seenLocs.add(progress);
							}
						}

						if (s.target instanceof CtxtVarPoint) {
							VarNode v = ((CtxtVarPoint)s.target).var;
							SootMethod method = null;
							if (v instanceof LocalVarNode) {
								LocalVarNode localRegister = (LocalVarNode)v;
								method = localRegister.meth;
							} else if (v instanceof ThisVarNode) {
								ThisVarNode thisRegister = (ThisVarNode)v;
								method = thisRegister.method;
							} else if (v instanceof ParamVarNode) {
								ParamVarNode paramRegister = (ParamVarNode)v;
								method = paramRegister.method;
							} else if (v instanceof RetVarNode) {
								RetVarNode retRegister = (RetVarNode)v;
								method = retRegister.method;
							} 

							if (method == null)
								continue;
							String sourceFileName = SourceInfo.filePath(method.getDeclaringClass());
							int methodLineNum = SourceInfo.methodLineNum(method);
							if (methodLineNum < 0) {
								methodLineNum = 0;
							}
							String methName = method.getName();

							progress += method.getNumber();
							if (!seenLocs.contains(progress)) {
								c.newTuple().addRawValue(method.getName(), sourceFileName, ""+methodLineNum, "method", "")
									.addValue("Label: " + s.symbol);
								seenLocs.add(progress);
							}

						} /*else if (s.target instanceof CtxtObjPoint) {
							c.newTuple().addRawValue("Obj", "", "0", "method", "")
								.addValue("Label: " + "CtxtObj");

						}*/
					}
					//System.out.println((s.reverse ? "<-" : "--" ) + s.symbol +
					//				   (s.reverse ? "-- " : "-> " ) + s.target);
				//System.out.println();
				}
			}
		} catch (Exception e) {
			System.err.println("Problem prodicing FlowViz report");
			e.printStackTrace();
		}
	}
}
