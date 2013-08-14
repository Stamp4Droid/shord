package stamp.reporting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import shord.analyses.*;
import shord.program.Program;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import chord.util.tuple.object.Pair;

import soot.jimple.Stmt;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;

import stamp.paths.*;
import stamp.srcmap.SourceInfo;
import stamp.util.tree.*;

/**
 * Generates a an xml report that represents flows
 * as callstacks of depth K given in the analysis.
 * An attempt is made to (conservatively) link 
 * contexts together so the full callstack is shown.
 * Top-level methods that belong to the harness/framework
 * are not included. 
 *
 * @author brycecr
 */
public class SrcSinkFlowViz extends XMLVizReport
{
    protected enum StepActionType {
        SAME, // same parent context as last method added
        DROP, // new method called by last step's method
        POP, // new method at level of last context's caller
        BROKEN, // no context overlap with last; also initial step
        OTHER // self-explanatory
    }

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

            ArrayList<Tree<SootMethod>> flows = new ArrayList<Tree<SootMethod>>();
            Map<SootMethod, ArrayDeque<CallSite>> callSites = new HashMap<SootMethod, ArrayDeque<CallSite>>();

            int count = 0; //just counts for the sake of numbering. Will be phased out in future versions
            for (Path p : PathsAdapter.getPaths()) {
                count += 1;
                CtxtLabelPoint start = (CtxtLabelPoint)p.start;
                CtxtLabelPoint end = (CtxtLabelPoint)p.end;
                String startLabel = start.label;
                String endLabel = end.label;
                String flowname = count + ") "+ startLabel + " --> " + endLabel;

                Tree<SootMethod> t = new Tree<SootMethod>(flowname);
                Node<SootMethod> lastNode = t.getRoot();

                //TODO init?? Does this work?
                //Add ctxt for label
                //System.err.println(/* context */);

                for (Step s : p.steps) {
                    SootMethod method = getMethod(s);
                    logCallSites(s, callSites);
                    switch(getStepActionType(method, s, lastNode, t)) {
                        case SAME:
                            // could have consecutive exact callstack repeat
                            if (!lastNode.getData().equals(method)) {
                                lastNode = t.getParent(lastNode).addChild(new Node<SootMethod>(method));
                            }
                            break;
                        case DROP:
                            lastNode = lastNode.addChild(new Node<SootMethod>(method));
                            break;
                        case POP:
                            Node<SootMethod> grandFather = t.getParent(t.getParent(lastNode));
                            Node<SootMethod> greatGrandFather = t.getParent(grandFather);
                            if (greatGrandFather == t.getRoot()) {
                                greatGrandFather.replaceChild(getTopCtxtMethod(s));
                            }
                            //grandfather shouldn't be root or anything like that
                            //if stuff
                            lastNode = grandFather.addChild(method);
                            break;
                        case BROKEN:
                            lastNode = t.getRoot().addChild(getMethod(s));
                            break;
                        case OTHER:
                        default:
                            throw new Exception("Unrecognized StepActionType in SrcSinkFlowViz generate");
                    }
                }
                flows.add(t);
            }

        generateReport(flows, callSites);

        } catch (IllegalStateException ise) {
            // The hope is that this will be caught here if the error is simply that
            // no path solver was run. Try to provide some intelligable feeback...
            makeOrGetSubCat("Error: No Path Solver Found"); // TODO: undesireable b/c creates empty + drop-down
            System.out.println("No path solver found so no path visualization could be generated.");
            System.out.println("To visualize paths run with -Dstamp.backend=solvergen");

        } catch (Exception e) {
            //Something else went wrong...
            System.err.println("Problem producing FlowViz report");
            e.printStackTrace();
        }

    }

    /**
     * Frome the callgraph tree and map of methods to callsites
     * provided as parameters, generates the XML report
     */
    protected void generateReport(ArrayList<Tree<SootMethod>> flows, Map<SootMethod, ArrayDeque<CallSite>> callSites) {


        for (Tree<SootMethod> t : flows) {
            Category c = makeOrGetSubCat(t.getLabel());
            Tree<SootMethod>.TreeIterator itr = t.iterator();
            
            Deque<Category> stack = new ArrayDeque<Category>();
            while (itr.hasNext()) {
                int oldDepth = itr.getDepth();
                SootMethod meth = itr.next();
                int newDepth = itr.getDepth();
                if (oldDepth < newDepth) {
                    assert newDepth - oldDepth == 1;
                    c = c.makeOrGetSubCat(meth);
                    stack.push(c);
                } else if (oldDepth > newDepth) {
                    int del = oldDepth - newDepth;
                    for (; del > 0; del--) {
                        c = stack.pop();
                    }
                } else {
                    c.makeOrGetSubCat(meth);
                }
                //add classinfo data
            }
        }
    }

    /**
     * Returns the method object for the top (outermost)
     * context level associated with the parameter Step 
     */
    private SootMethod getTopCtxtMethod(Step s) {
        CtxtVarPoint point = (CtxtVarPoint)s.target;
        Unit[] ctxt = point.ctxt.getElems();

        if (ctxt.length > 1) {
            SootMethod method = getMethod((Stmt)ctxt[ctxt.length-1]);
            if (method == null) {
                return new SootMethod("No Method", Collections.<Type>emptyList(), VoidType.v()); // TODO check is this OK?
            }
        } else {
            // Ought to happen rarely or not at all...
            return getMethod(s);
        }
    }


    /**
     * Returns the "StepActionType" of the Step s.
     * In other words, returns the code for how the callgraph tree
     * will be modified by s. See @StepActionType for information
     * on return types.
     */
    private StepActionType getStepActionType(SootMethod method, Step s, Node<SootMethod> lastNode, Tree t) {
         
        // Throughout these we follow a minimal detection which, to my knowledge, ought to suffice.
        // However, it may be wiser in order to be certain we report the correct type and catch edge
        // cases to check that all conditions for each type apply
        
        if (lastNode.getData().equals(method)) {
            return StepActionType.DROP;

        } else if (t.getParent(lastNode).getData().equals(method)) {
            return StepActionType.SAME;

        } else if (t.getParent(t.getParent(lastNode)).getData().equals(method)) {
            return StepActionType.POP;

        } else if (t.isRoot(lastNode) /* other condition? */ ) {
            return StepActionType.BROKEN;
        }
        
        return StepActionType.OTHER;
    }

    /**
     * Save take variable and context information
     * find the associated source line, file, and class
     * (i.e. the CallSite object) and save that into
     * the map pamameter
     */
    private void logCallSites(Step s, Map<SootMethod, ArrayDeque<CallSite>> callSites) {

        CtxtVarPoint point = (CtxtVarPoint)s.target;
        Unit[] context = point.ctxt.getElems();
        SootMethod method = getMethod(s);
        Stmt stm = null;

        for (int i = -1; i < context.length; ++i) {
            // First iteration uses step target variable inited above
            if (i >= 0) {
                stm = (Stmt)context[i];
                method = getMethod(stm);
            }
            // We could filter methods here (i.e. framework), 
            // but might make sense to do it
            // instead while generating the XML report itself

            // Is this too general? Maybe we should handle these
            // statements differently based on their type?
            if (method == null) {
                continue;
            }

            // We add callsite for all but the topmost context
            // level, because we don't know the context for that
            if (i < context.length - 1) {
                CallSite cs = generateCallSite(method, (Stmt)context[i+1]);
                if (!callSites.containsKey(method)) {
                    callSites.put(method, new ArrayDeque<CallSite>());
                }
                callSites.get(method).addLast(cs);
            }
        }
    }


    private CallSite generateCallSite(SootMethod method, Stmt caller) {

        try {
            String locStr = SourceInfo.javaLocStr(caller);
            String[] locStrTokens = locStr.split(":");
            assert locStrTokens.length >= 1;

            // Create callsite 
            String methName = method.getName();
            int lineNumber = (locStrTokens.length > 1) ? Integer.parseInt(locStrTokens[1]) : 0;
            String className = SourceInfo.srcClassName(caller);
            String srcFilePath = locStrTokens[0];
            /* Some weird gui behavior associated with line number -1 so...
               ...This may be useful: if (methodLineNum < 0) {methodLineNum = 0;}
             */

            CallSite cs = new CallSite(methName, className, lineNumber, srcFilePath);
            return cs;

        } catch (NumberFormatException nfe) {
            System.err.println("Line number format was incorrect for callsite. Expecting "
                    + "[srcFilePath]:[line number] (no brackets)");
            nfe.printStackTrace();
        }
    }

    /**
     * Returns the method object associated with the 
     * CtxtVarNode of the parameter step
     */
    private SootMethod getMethod(Step s) {
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

        return method;
    }

    /**
     * Returns the method object associated with the 
     * method that contains the statement parameter
     */
    private SootMethod getMethod(Stmt stm) {
        return Program.containerMethod(stm);
    }

    /**
     * A class representing the data needed to represent a callsite
     * in terms of data necessary for the frontend to locate and highlight
     * the correct location in the correct source file.
     * The method name identifier may not be strictly necessary and is more
     * of an identifier. As far as the class is concerned, any parameter
     * to the contstructor may be null
     */
    class CallSite {
        String className;
        String srcFilePath;
        int lineNumber;
        String methodName;

        public CallSite(String methodName, String className, int lineNumber, String srcFilePath) {
            this.methodName = methodName;
            this.className = className;
            this.srcFilePath = srcFilePath;
            this.lineNumber = lineNumber;
        }
    }
}

/*
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

if (SourceInfo.isFrameworkClass(method.getDeclaringClass()) && c.equals(mc)) {
continue;
}

String methName = method.getName();
int methodLineNum = SourceInfo.methodLineNum(method);
if (methodLineNum < 0) {
methodLineNum = 0;
}

c = c.makeOrGetSubCat(method);

progress += method.getNumber();

if (i < elems.length-1) {
    stm = (Stmt)elems[i+1];
    method = Program.containerMethod(stm);
    methodLineNum = SourceInfo.stmtLineNum(stm);
}

String sourceFileName = (method == null) ? "" : SourceInfo.filePath(method.getDeclaringClass());


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
} catch (IllegalStateException ise) {
    // The hope is that this will be caught here if the error is simply that
    // no path solver was run. Try to provide some intelligable feeback...
    makeOrGetSubCat("Error: No Path Solver Found"); // TODO: slightly undesireable because creates empty + drop-down
    System.out.println("No path solver found so no path visualization could be generated.");
    System.out.println("To visualize paths run with -Dstamp.backend=solvergen");

} catch (Exception e) {
    //Something else went wrong...
    System.err.println("Problem producing FlowViz report");
    e.printStackTrace();
}
}
}
*/
