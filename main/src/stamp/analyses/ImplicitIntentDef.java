package stamp.analyses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JimpleLocalBox;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.LiveLocals;
import soot.toolkits.scalar.LocalDefs;

public class ImplicitIntentDef implements LocalDefs{
	
    private final Map<Unit, HashSet<Object>> answer;

	private ImplicitIntentDefAnalysis analysis;
	
    private Map<Unit, HashSet> unitToMask;
    
    private final UnitGraph graph;
    
    private final Map<Local, HashSet<Unit>> localToDefs; // for each local, set of units

    public ImplicitIntentDef(UnitGraph g, LiveLocals live) {
        this.graph = g;

        localToDefs = new HashMap<Local, HashSet<Unit>>();
        unitToMask = new HashMap<Unit, HashSet>();
        for( Iterator uIt = g.iterator(); uIt.hasNext(); ) {
            final Unit u = (Unit) uIt.next();
            Local l = localDef(u);
            if( l == null ) continue;
            HashSet<Unit> s = defsOf(l);
            s.add(u);
        }

        for( Iterator uIt = g.iterator(); uIt.hasNext(); ) {
            final Unit u = (Unit) uIt.next();
            unitToMask.put(u, new HashSet(live.getLiveLocalsAfter(u)));
        }
        
        analysis = new ImplicitIntentDefAnalysis(g);
        
        answer = new HashMap<Unit, HashSet<Object>>();
        for( Iterator uIt = g.iterator(); uIt.hasNext(); ) {
            final Unit u = (Unit) uIt.next();
            HashSet analysisResult = (HashSet) analysis.getFlowBefore(u);
            //System.out.println("implicit set...." + u + "||" +analysisResult);
            answer.put(u, analysisResult);
        }
    }
    
    public boolean checkImplicit(Object var) {
    	boolean implicitIntent = false;
    	Stmt stmt = (Stmt) var;
		if (stmt.containsInvokeExpr()) {
			InvokeExpr ie = stmt.getInvokeExpr();
			HashSet intentSet = answer.get(stmt);

		    Iterator it = intentSet.iterator();
	        while(it.hasNext()) {
                Unit inU = (Unit) it.next();

	        	if(inU instanceof JIdentityStmt)
	        		continue;
				AssignStmt localStmt = (AssignStmt)inU;
				if(localStmt.getLeftOp().equals(ie.getArg(0))) 
					implicitIntent = true;
	        }
		}
    	return implicitIntent;
    }
       
    public List<Unit> getDefsOfAt(Local l, Unit s)
    {
        return new ArrayList();
    }
    
    private Local localDef(Unit u) {
        List defBoxes = u.getDefBoxes();
		int size = defBoxes.size();
        if( size == 0 ) return null;
        if( size != 1 ) throw new RuntimeException();
        ValueBox vb = (ValueBox) defBoxes.get(0);
        Value v = vb.getValue();
        if( !(v instanceof Local) ) return null;
        return (Local) v;
    }
    
    private HashSet<Unit> defsOf( Local l ) {
        HashSet<Unit> ret = localToDefs.get(l);
        if( ret == null ) localToDefs.put( l, ret = new HashSet<Unit>() );
        return ret;
    }
    
	class ImplicitIntentDefAnalysis extends ForwardFlowAnalysis {
		

		ImplicitIntentDefAnalysis(UnitGraph g) {
            super(g);
            doAnalysis();
        }
        protected void merge(Object inoutO, Object inO) {
            HashSet inout = (HashSet) inoutO;
            HashSet in = (HashSet) inO;

            inout.addAll(in);
        }
        protected void merge(Object in1, Object in2, Object out) {
            HashSet inSet1 = (HashSet) in1;
            HashSet inSet2 = (HashSet) in2;
            HashSet outSet = (HashSet) out;

            outSet.clear();
            outSet.addAll(inSet1);
            outSet.addAll(inSet2);
        }
		
        protected void flowThrough(Object inValue, Object unit, Object outValue) {
            Unit u = (Unit) unit;
            HashSet in = (HashSet) inValue;
            HashSet<Unit> out = (HashSet<Unit>) outValue;
            out.clear();
            Set mask = unitToMask.get(u);
            Local l = localDef(u);
			HashSet<Unit> allDefUnits = null;
			if (l == null) {//add all units contained in mask
	            for( Iterator inUIt = in.iterator(); inUIt.hasNext(); ) {
	                final Unit inU = (Unit) inUIt.next();
	                if( mask.contains(localDef(inU)) )
					{
	                	//kill implicit....
	        			if ( (u instanceof JInvokeStmt) && (inU instanceof JAssignStmt) ) {
	        				JAssignStmt assign = (JAssignStmt) inU;
	        				JInvokeStmt invokeStmt = (JInvokeStmt) u;
                            String invokeMethSig = invokeStmt.getInvokeExpr().getMethod().getSignature();
        					ValueBox callsite = (ValueBox)invokeStmt.getUseBoxes().get(0);

	        				if ( (invokeMethSig.equals("<android.content.Intent: android.content.Intent setClass(android.content.Context,java.lang.Class)>") 
                            || invokeMethSig.equals("<android.content.Intent: android.content.Intent setClassName(android.content.Context,java.lang.String)>")
                            || invokeMethSig.equals("<android.content.Intent: android.content.Intent setClassName(java.lang.String,java.lang.String)>")
                            || invokeMethSig.equals("<android.content.Intent: android.content.Intent setComponent(android.content.ComponentName)>") 
                            || invokeMethSig.equals("<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>")
                            || invokeMethSig.equals("<android.content.Intent: void <init>(java.lang.String,android.net.Uri,android.content.Context,java.lang.Class)>") )
	        				&& assign.getLeftOp().equals(callsite.getValue()) ) {

                                System.out.println("kill implicit def:" + assign.getLeftOp() + "@" + u);
	                            continue;
	        				}

	        			}
						out.add(inU);
					}
	            }
			}
			else
			{//check unit whether contained in allDefUnits before add into out set.
				allDefUnits = defsOf(l);
				
	            for( Iterator inUIt = in.iterator(); inUIt.hasNext(); ) {
	                final Unit inU = (Unit) inUIt.next();
    	            if( mask.contains(localDef(inU)) )
					{//only add unit not contained in allDefUnits
						if ( allDefUnits.contains(inU)){
							out.remove(inU);
						} else {
    						out.add(inU);
						}
					}
    	        }
   	            out.removeAll(allDefUnits);
   	            if(mask.contains(l)) out.add(u);
			}
        }

    
        protected void copy(Object source, Object dest) {
            HashSet sourceSet = (HashSet) source;
            HashSet<Object> destSet   = (HashSet<Object>) dest;
              
			//retain all the elements contained by sourceSet
			if (destSet.size() > 0)
				destSet.retainAll(sourceSet);
			
			//add the elements not contained by destSet
			if (sourceSet.size() > 0)
			{
				for( Iterator its = sourceSet.iterator(); its.hasNext(); ) {
					Object o = its.next();
					if (!destSet.contains(o))
					{//need add this element.
						destSet.add(o);
					}
				}
			}

        }

        protected Object newInitialFlow() {
            return new HashSet();
        }

        protected Object entryInitialFlow() {
            return new HashSet();
        }
    
	}
}
