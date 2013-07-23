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
		super("Flow Path Vizualization");
	}

    public void generate()
	{
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

				SootMethod lastStackBtm = null;
				SootMethod lastStackTop = null;
				for (Step s : p.steps) {
					if (s.target instanceof CtxtVarPoint) {
						String progress = "";
						Unit[] elems = ((CtxtVarPoint)s.target).ctxt.getElems();
						Category c = mc;
						System.out.println(s.target);

						//NOTE TODO: CURRENTLY ASSUMES K = 2, not WLOG exactly...
						if (elems.length > 0 && Program.containerMethod((Stmt)elems[0]).equals(lastStackBtm)) {
							Stmt stm  = (Stmt)elems[elems.length-1];
							SootMethod method = Program.containerMethod(stm);

							if (SourceInfo.isFrameworkClass(method.getDeclaringClass()) && (c==null || c.equals(mc))) {
								continue;
							}
							
							c = c.makeOrGetSupCat(Program.containerMethod((Stmt)elems[0]), method);


							System.out.println("Adding SuperCat "+Program.containerMethod((Stmt)elems[0]).getName()
								+" "+method.getName());

							String sourceFileName = (method == null) ? "" : SourceInfo.filePath(method.getDeclaringClass());
							int methodLineNum = SourceInfo.methodLineNum(method);
							if (methodLineNum < 0) {
								methodLineNum = 0;
							}
							String methName = method.getName();


							if (c == null) {
								System.out.println("Found Empty Ctxt "+s.toString());

								lastStackBtm = null;
								lastStackTop = null;

								continue;
							}

							c.addRawValue(methName, sourceFileName, ""+methodLineNum, "method", "");
							seenLocs.add(progress);
						} else {
							if (elems.length >0) {
								c = c.findSubCat(Program.containerMethod((Stmt)elems[elems.length-1]));
								if (c == null) {
									c = mc;
								}
							}

							for (int i = elems.length - 1; i >= 0; --i) {
								Stmt stm  = (Stmt)elems[i];
								SootMethod method = Program.containerMethod(stm);

								String sourceFileName = (method == null) ? "" : SourceInfo.filePath(method.getDeclaringClass());
								int methodLineNum = SourceInfo.methodLineNum(method);
								if (methodLineNum < 0) {
									methodLineNum = 0;
								}
								String methName = method.getName();

								if (SourceInfo.isFrameworkClass(method.getDeclaringClass()) && c.equals(mc)) {
									continue;
								}

								c = c.makeOrGetSubCat(method);

								progress += method.getNumber();

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
									c = c.makeOrGetSubCat(method);
									c.addRawValue(method.getName(), sourceFileName, ""+methodLineNum, "method", "");
									seenLocs.add(progress);
								}

							} 
							/*else if (s.target instanceof CtxtObjPoint) {
								c.newTuple().addRawValue("Obj", "", "0", "method", "")
									.addValue("Label: " + "CtxtObj");

							}*/
						}
						if (elems.length > 0) {
							lastStackBtm = Program.containerMethod((Stmt)elems[elems.length-1]);
							lastStackTop = Program.containerMethod((Stmt)elems[0]);
						} else {
							lastStackBtm = null;
							lastStackTop = null;
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Problem producing FlowViz report");
			e.printStackTrace();
		}
	}
}
