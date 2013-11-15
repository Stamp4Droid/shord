package shord.analyses;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Iterator;


import gnu.trove.list.array.TIntArrayList;

import chord.util.Utils;
import chord.bddbddb.Rel.RelView;
import soot.Unit;
import soot.Type;
import soot.RefType;
import soot.jimple.Stmt;
import soot.SootMethod;
import soot.util.NumberedSet;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;

import shord.program.Program;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import chord.project.Config;
import chord.project.Chord;
import chord.project.Messages;
import chord.util.ArraySet;
import chord.util.graph.IGraph;
import chord.util.graph.MutableGraph;
import chord.util.tuple.object.Pair;

/**
 * Analysis for pre-computing abstract contexts.
 * @author Yu Feng (yufeng@cs.stanford.edu)
 */
@Chord(name = "ctxts-obj-java",
       consumes = { "MI", "VH", "MH", "ipt", "StatIM"},
       produces = { "C", "CC", "CH", "CI"},
       namesOfTypes = { "C" },
       types = { DomC.class }
)
public class CtxtsObjAnalysis extends JavaAnalysis {
    private static final Set<Ctxt> emptyCtxtSet = Collections.emptySet();
    private static final Set<SootMethod> emptyMethSet = Collections.emptySet();
    private static final Unit[] emptyElems = new Unit[0];

    // includes all methods in domain
    private Set<Ctxt>[] methToCtxts;
    
    private TIntArrayList[] methToRcvSites;  // ctxt kind is KOBJSEN
    private TIntArrayList[] methToClrSites;  // ctxt kind is KCfa, for static method
    
    private Map<SootMethod, ThisVarNode> methToThis = new HashMap<SootMethod, ThisVarNode>();

    private Set<SootMethod>[] methToClrMeths; // ctxt kind is CTXTCPY


    private Set<Ctxt> epsilonCtxtSet;

	public static int K = 2;

    private int[] ItoM;
    private int[] HtoM;
    private Unit[] ItoQ;
    private AllocNode[] HtoQ;

    private SootMethod mainMeth;

    private DomV domV;
    private DomM domM;
    private DomI domI;
    private DomH domH;
    private DomC domC;

    private ProgramRel relVH;
    private ProgramRel relCC;
    private ProgramRel relCH;
    private ProgramRel relCI;
    private ProgramRel relIpt;
    private ProgramRel relStatIM;
    


    public void run() {
        domV = (DomV) ClassicProject.g().getTrgt("V");
        domI = (DomI) ClassicProject.g().getTrgt("I");
        domM = (DomM) ClassicProject.g().getTrgt("M");
        domH = (DomH) ClassicProject.g().getTrgt("H");
        domC = (DomC) ClassicProject.g().getTrgt("C");

        relVH = (ProgramRel) ClassicProject.g().getTrgt("VH");
        relCC = (ProgramRel) ClassicProject.g().getTrgt("CC");
        relCH = (ProgramRel) ClassicProject.g().getTrgt("CH");
        relCI = (ProgramRel) ClassicProject.g().getTrgt("CI");
        relIpt = (ProgramRel) ClassicProject.g().getTrgt("ipt");
        relStatIM = (ProgramRel) ClassicProject.g().getTrgt("StatIM");

        mainMeth = Program.g().getMainMethod();

        int numV = domV.size();
        int numM = domM.size();
        int numA = domH.size();
        int numI = domI.size();

        for(VarNode vnode:domV){
            if(vnode instanceof ThisVarNode) {
                ThisVarNode thisVar = (ThisVarNode) vnode;
                methToThis.put(thisVar.getMethod(), thisVar); 
            }

        }

        ItoM = new int[numI];
        ItoQ = new Unit[numI];
        final ProgramRel relMI = (ProgramRel) ClassicProject.g().getTrgt("MI");		
        relMI.load();
        Iterable<Pair<SootMethod,Unit>> res = relMI.getAry2ValTuples();
        for(Pair<SootMethod,Unit> pair : res) {
            SootMethod meth = pair.val0;
            Unit invk = pair.val1;
            int mIdx = domM.indexOf(meth);
            int iIdx = domI.indexOf(invk);
            ItoM[iIdx] = mIdx;
            ItoQ[iIdx] = invk;
        }
        relMI.close();

        DomH domH = (DomH) ClassicProject.g().getTrgt("H");
        int numH = domH.size();
        HtoM = new int[numH];
        HtoQ = new AllocNode[numH];
        final ProgramRel relMH = (ProgramRel) ClassicProject.g().getTrgt("MH");		
        relMH.load();
        Iterable<Pair<SootMethod,AllocNode>> res1 = relMH.getAry2ValTuples();
        for(Pair<SootMethod,AllocNode> pair : res1) {
            SootMethod meth = pair.val0;
            AllocNode alloc = pair.val1;
            int mIdx = domM.indexOf(meth);
            int hIdx = domH.indexOf(alloc);
            HtoM[hIdx] = mIdx;
            HtoQ[hIdx] = alloc;
        }
        relMH.close();
        relVH.load();
        relIpt.load();
        relStatIM.load();

        Ctxt epsilon = domC.setCtxt(emptyElems);
        epsilonCtxtSet = new ArraySet<Ctxt>(1);
        epsilonCtxtSet.add(epsilon);

        methToCtxts = new Set[numM];

        methToRcvSites = new TIntArrayList[numM];
        methToClrSites = new TIntArrayList[numM];
        methToClrMeths = new Set[numM];

        // Do the heavy crunching
        doAnalysis();

        relVH.close();
        relIpt.close();
        relStatIM.close();

        // Populate domC
        for(int iIdx = 0; iIdx < ItoM.length; iIdx++){
            int mIdx = ItoM[iIdx];
            Unit invk = ItoQ[iIdx];
            Set<Ctxt> ctxts = methToCtxts[mIdx];
            for (Ctxt oldCtxt : ctxts) {
                Object[] oldElems = oldCtxt.getElems();
                Object[] newElems = combine(K, invk, oldElems);
                domC.setCtxt(newElems);
            }
        }

        for (int hIdx = 1; hIdx < numA; hIdx++) {
			int mIdx = HtoM[hIdx];
			AllocNode alloc = HtoQ[hIdx];

            Set<Ctxt> ctxts = methToCtxts[mIdx];
            assert(ctxts != null);
            if(ctxts == null) continue;
            for (Ctxt oldCtxt : ctxts) {
                Object[] oldElems = oldCtxt.getElems();
                Object[] newElems = combine(K, alloc, oldElems);
                domC.setCtxt(newElems);
            }
        }
        domC.save();

        int numC = domC.size();

        relCC.zero();
        relCI.zero();
        /*for (int iIdx = 0; iIdx < numI; iIdx++) {
            Quad invk = (Quad) domI.get(iIdx);
            SootMethod meth = invk.getMethod();
            Set<Ctxt> ctxts = methToCtxts[domM.indexOf(meth)];
            int k = kcfaValue[iIdx];
            for (Ctxt oldCtxt : ctxts) {
                Object[] oldElems = oldCtxt.getElems();
                Object[] newElems = combine(k, invk, oldElems);
                Ctxt newCtxt = domC.setCtxt(newElems);
                relCC.add(oldCtxt, newCtxt);
                relCI.add(newCtxt, invk);
            }
        }*/

        for(int iIdx = 0; iIdx < ItoM.length; iIdx++){
            int mIdx = ItoM[iIdx];
            Unit invk = ItoQ[iIdx];
            Set<Ctxt> ctxts = methToCtxts[mIdx];
            assert(ctxts != null);
            for (Ctxt oldCtxt : ctxts) {
                Object[] oldElems = oldCtxt.getElems();
                Object[] newElems = combine(K, invk, oldElems);
                Ctxt newCtxt = domC.setCtxt(newElems);
                relCC.add(oldCtxt, newCtxt);
                relCI.add(newCtxt, invk);
            }
        }

        relCI.save();

        assert (domC.size() == numC);
        ////CH
        relCH.zero();

        for (int hIdx = 0; hIdx < numA; hIdx++) {//why chord uses 1?
            int mIdx = HtoM[hIdx];
			AllocNode alloc = HtoQ[hIdx];

            //ignore the context of gString.
            if(alloc instanceof GlobalStringNode) continue;

            Set<Ctxt> ctxts = methToCtxts[mIdx];
            assert(ctxts != null);
            for (Ctxt oldCtxt : ctxts) {
                Object[] oldElems = oldCtxt.getElems();
                Object[] newElems = combine(K, alloc, oldElems);
                Ctxt newCtxt = domC.setCtxt(newElems);
                relCC.add(oldCtxt, newCtxt);
                relCH.add(newCtxt, alloc);
            }
        }
        relCH.save();

        assert (domC.size() == numC);

        relCC.save();
    }

    private void doAnalysis() {
        SootMethod mainMeth = Program.g().getMainMethod();
        Set<SootMethod> roots = new HashSet<SootMethod>();
        Map<SootMethod, Set<SootMethod>> methToPredsMap = new HashMap<SootMethod, Set<SootMethod>>();
		boolean ignoreStubs = PAGBuilder.ignoreStubs;
		NumberedSet stubs = PAGBuilder.stubMethods;
		Iterator mIt = Program.g().scene().getReachableMethods().listener();
		while(mIt.hasNext()){
			SootMethod meth = (SootMethod) mIt.next();
			if(ignoreStubs && stubs.contains(meth)) continue;

            int mIdx = domM.indexOf(meth);
			if (meth == mainMeth || meth.getName().equals("<clinit>")){
                roots.add(meth);
                methToPredsMap.put(meth, emptyMethSet);
                methToCtxts[mIdx] = epsilonCtxtSet;
			} else {
                Set<SootMethod> predMeths = new HashSet<SootMethod>();
                if(meth.isStatic()) {
                    //do the copyctxt for static method.
                    /*Iterable<Object> ivks = getStatIvk(meth);
                    for (Object ivk : ivks) {
                        int iIdx = domI.indexOf(ivk);
                        int mm = ItoM[iIdx];
                        predMeths.add(domM.get(mm));
                    }
                    methToPredsMap.put(meth, predMeths);
                    methToCtxts[mIdx] = emptyCtxtSet;
                    methToClrMeths[mIdx] = predMeths;*/

                    ///use callsite.
                    TIntArrayList clrSites = new TIntArrayList();
                    for (Unit invk : getStatIvk(meth)) {
                        int iIdx = domI.indexOf(invk);
                        predMeths.add(domM.get(ItoM[iIdx])); // Which method can point to this method...?
                        clrSites.add(iIdx); // sites that can call me
                    }
                    methToClrSites[mIdx] = clrSites;
                    methToPredsMap.put(meth, predMeths);
                    methToCtxts[mIdx] = emptyCtxtSet;
                } else {
                    TIntArrayList rcvSites = new TIntArrayList();
                    ThisVarNode thisVar = methToThis.get(meth);
                    Iterable<Object> pts = getPointsTo(thisVar);
                    for (Object inst : pts) {
                        int hIdx = domH.indexOf(inst);
                        predMeths.add(domM.get(HtoM[hIdx]));
                        rcvSites.add(hIdx);
                    }
                    methToRcvSites[mIdx] = rcvSites;
                    methToPredsMap.put(meth, predMeths);
                    methToCtxts[mIdx] = emptyCtxtSet;
                }

            }
        }
        process(roots, methToPredsMap);
    }

    // Compute all the contexts that each method can be called in
    private void process(Set<SootMethod> roots, Map<SootMethod, Set<SootMethod>> methToPredsMap) {
        IGraph<SootMethod> graph = new MutableGraph<SootMethod>(roots, methToPredsMap, null);
        List<Set<SootMethod>> sccList = graph.getTopSortedSCCs();
        int n = sccList.size();
        if (Config.verbose >= 2)
            System.out.println("numSCCs: " + n);
        for (int i = 0; i < n; i++) { // For each SCC...
            Set<SootMethod> scc = sccList.get(i);
            if (Config.verbose >= 2)
                System.out.println("Processing SCC #" + i + " of size: " + scc.size());
            if (scc.size() == 1) { // Singleton
                SootMethod cle = scc.iterator().next();
                if (roots.contains(cle))
                    continue;
                if (!graph.hasEdge(cle, cle)) {
                    int cleIdx = domM.indexOf(cle);
                    methToCtxts[cleIdx] = getNewCtxts(cleIdx);
                    continue;
                }
            }
            for (SootMethod cle : scc) {
                assert (!roots.contains(cle));
            }
            boolean changed = true;
            for (int count = 0; changed; count++) { // Iterate...
                if (Config.verbose >= 2)
                    System.out.println("\tIteration  #" + count);
                changed = false;
                for (SootMethod cle : scc) { // For each node (method) in SCC
                    int mIdx = domM.indexOf(cle);
                    Set<Ctxt> newCtxts = getNewCtxts(mIdx);
                    if (!changed) {
                        Set<Ctxt> oldCtxts = methToCtxts[mIdx];
                        if (newCtxts.size() > oldCtxts.size())
                            changed = true;
                        else {
                            for (Ctxt ctxt : newCtxts) {
                                if (!oldCtxts.contains(ctxt)) {
                                    changed = true;
                                    break;
                                }
                            }
                        }
                    }
                    methToCtxts[mIdx] = newCtxts;
                }
            }
        }
    }

    private Iterable<Object> getPointsTo(VarNode var) {
        //RelView view = relVH.getView();
        RelView view = relIpt.getView();
        view.selectAndDelete(0, var);
        return view.getAry1ValTuples();
    }

    private Iterable<Unit> getStatIvk(SootMethod var) {
        //RelView view = relVH.getView();
        RelView view = relStatIM.getView();
        view.selectAndDelete(1, var);
        return view.getAry1ValTuples();
    }

    private Object[] combine(int k, Object inst, Object[] elems) {
        int oldLen = elems.length;
        int newLen = Math.min(k - 1, oldLen) + 1;
        Object[] newElems = new Object[newLen];
        if (newLen > 0) newElems[0] = inst;
        if (newLen > 1)
            System.arraycopy(elems, 0, newElems, 1, newLen - 1);
        return newElems;
    }

    private Set<Ctxt> getNewCtxts(int cleIdx) { // Update contexts for this method (callee)
        final Set<Ctxt> newCtxts = new HashSet<Ctxt>();
        SootMethod meth = (SootMethod) domM.get(cleIdx);

        if(meth.isStatic()){//static?copy all the ctxts from its callers.
            /*Set<SootMethod> clrs = methToClrMeths[cleIdx];
            for (SootMethod clr : clrs) {
                int clrIdx = domM.indexOf(clr);
                Set<Ctxt> clrCtxts = methToCtxts[clrIdx];
                newCtxts.addAll(clrCtxts);
            }*/
            //instead of copy, we push the invk to the new ctxt.
            TIntArrayList invks = methToClrSites[cleIdx]; // which call sites point to me
            int n = invks.size();
            for (int i = 0; i < n; i++) {
                int iIdx = invks.get(i);
                Unit invk = ItoQ[iIdx];
                int clrIdx = ItoM[iIdx];
                Set<Ctxt> clrCtxts = methToCtxts[clrIdx]; // method of caller
                for (Ctxt oldCtxt : clrCtxts) {
                    Object[] oldElems = oldCtxt.getElems();
                    Object[] newElems = combine(K, invk, oldElems); // Append
                    Ctxt newCtxt = domC.setCtxt(newElems);
                    newCtxts.add(newCtxt);
                    //System.out.println("newCtxt: "+newCtxt);
                }
            }

        }else{
            TIntArrayList rcvs = methToRcvSites[cleIdx];
            int n = rcvs.size();
            for (int i = 0; i < n; i++) {
                int hIdx = rcvs.get(i);
                Object rcv = HtoQ[hIdx];
                int clrIdx = HtoM[hIdx];
                Set<Ctxt> rcvCtxts = methToCtxts[clrIdx];
                for (Ctxt oldCtxt : rcvCtxts) {
                    Object[] oldElems = oldCtxt.getElems();
                    Object[] newElems = combine(K, rcv, oldElems);
                    Ctxt newCtxt = domC.setCtxt(newElems);
                    newCtxts.add(newCtxt);
                }
            }
        }
        return newCtxts;
    }

}
