package shord.project;

import java.util.ArrayList;
import java.util.Collection;

import shord.program.Program;
import shord.program.visitors.IAcqLockInstVisitor;
import shord.program.visitors.IClassVisitor;
import shord.program.visitors.IFieldVisitor;
import shord.program.visitors.IHeapInstVisitor;
import shord.program.visitors.IInstVisitor;
import shord.program.visitors.IInvokeInstVisitor;
import shord.program.visitors.IMethodVisitor;
import shord.program.visitors.IMoveInstVisitor;
import shord.program.visitors.INewInstVisitor;
import shord.program.visitors.IPhiInstVisitor;
import shord.program.visitors.IRelLockInstVisitor;
import shord.program.visitors.IReturnInstVisitor;
import shord.program.visitors.ICastInstVisitor;

import chord.project.ITask;
import chord.util.IndexSet;

import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Body;
import soot.Unit;
import soot.Value;
import soot.Local;
import soot.Immediate;
import soot.jimple.Stmt;
import soot.jimple.AssignStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.CastExpr;
import soot.jimple.AnyNewExpr;
import soot.jimple.toolkits.callgraph.ReachableMethods;

import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.Operator;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.Operator.ALoad;
import joeq.Compiler.Quad.Operator.AStore;
import joeq.Compiler.Quad.Operator.Getfield;
import joeq.Compiler.Quad.Operator.Getstatic;
import joeq.Compiler.Quad.Operator.Invoke;
import joeq.Compiler.Quad.Operator.Move;
import joeq.Compiler.Quad.Operator.CheckCast;
import joeq.Compiler.Quad.Operator.New;
import joeq.Compiler.Quad.Operator.NewArray;
import joeq.Compiler.Quad.Operator.Phi;
import joeq.Compiler.Quad.Operator.Putfield;
import joeq.Compiler.Quad.Operator.Putstatic;
import joeq.Compiler.Quad.Operator.Return;
import joeq.Compiler.Quad.Operator.Return.THROW_A;
import joeq.Compiler.Quad.Operator.Monitor.MONITORENTER;
import joeq.Compiler.Quad.Operator.Monitor.MONITOREXIT;

/**
 * Utility for registering and executing a set of tasks
 * as visitors over program representation.
 *
 * @author Mayur Naik (mayur.naik@intel.com)
 */
public class VisitorHandler {
    private final Collection<ITask> tasks;
    private Collection<IClassVisitor> cvs;
    private Collection<IFieldVisitor> fvs;
    private Collection<IMethodVisitor> mvs;
    private Collection<IHeapInstVisitor> hivs;
    private Collection<INewInstVisitor> nivs;
    private Collection<IInvokeInstVisitor> iivs;
    private Collection<IReturnInstVisitor> rivs;
    private Collection<IAcqLockInstVisitor> acqivs;
    private Collection<IRelLockInstVisitor> relivs;
    private Collection<IMoveInstVisitor> mivs;
    private Collection<ICastInstVisitor> civs;
    private Collection<IPhiInstVisitor> pivs;
    private Collection<IInstVisitor> ivs;

    public VisitorHandler(ITask task) {
        tasks = new ArrayList<ITask>(1);
        tasks.add(task);
    }

    public VisitorHandler(Collection<ITask> tasks) {
        this.tasks = tasks;
    }

    private void visitFields(SootClass c) {
        for (SootField f : c.getFields()) {
			for (IFieldVisitor fv : fvs)
				fv.visit(f);
		}
    }

    private void visitMethods(SootClass c, ReachableMethods reachableMethods) {
        for (SootMethod m : c.getMethods()) {
			if (!reachableMethods.contains(m))
				continue;
			for (IMethodVisitor mv : mvs) {
				mv.visit(m);
				if (!m.isConcrete())
					continue;
				visitInsts(m.retrieveActiveBody());
			}
		}
	}

    private void visitInsts(Body body) {
		for (Unit unit : body.getUnits()) {
			if (ivs != null) {
				for (IInstVisitor iv : ivs)
					iv.visit(unit);
			}
			Stmt stmt = (Stmt) unit;
			if (stmt.containsInvokeExpr()) {
				if (iivs != null) {
					for (IInvokeInstVisitor iiv : iivs)
						iiv.visitInvokeInst(unit);
				}
			} else if (stmt.containsFieldRef() || stmt.containsArrayRef()) {
				if (hivs != null) {
					for (IHeapInstVisitor hiv : hivs)
						hiv.visitHeapInst(unit);
				}
			} else if (stmt instanceof AssignStmt) {
				AssignStmt as = (AssignStmt) stmt;
				Value lhs = as.getLeftOp();
				Value rhs = as.getRightOp();
				if (rhs instanceof AnyNewExpr) {
					if (nivs != null) {
						for (INewInstVisitor niv : nivs)
							niv.visitNewInst(unit);
					}
				} else if (rhs instanceof Immediate) {
                    if (mivs != null) {
                        for (IMoveInstVisitor miv : mivs)
                            miv.visitMoveInst(unit);
                    }
                } else if (rhs instanceof CastExpr) {
                    if (civs != null) {
                        for (ICastInstVisitor civ : civs)
                            civ.visitCastInst(unit);
                    }
				}
			} else if (false/*op instanceof Phi*/) {  //TODO
				if (pivs != null) {
					for (IPhiInstVisitor piv : pivs)
						piv.visitPhiInst(unit);
				} 
			} else if (stmt instanceof ReturnStmt) {
					if (rivs != null) {
                        for (IReturnInstVisitor riv : rivs)
                            riv.visitReturnInst(unit);
                    }
			} else if (false/*op instanceof MONITORENTER*/) { //TODO
				if (acqivs != null) {
					for (IAcqLockInstVisitor acqiv : acqivs)
						acqiv.visitAcqLockInst(unit);
				}
			} else if (false/*op instanceof MONITOREXIT*/) { //TODO
				if (relivs != null) {
					for (IRelLockInstVisitor reliv : relivs)
						reliv.visitRelLockInst(unit);
				}
			}
		}
	}


    public void visitProgram() {
        for (ITask task : tasks) {
            if (task instanceof IClassVisitor) {
                if (cvs == null)
                    cvs = new ArrayList<IClassVisitor>();
                cvs.add((IClassVisitor) task);
            }
            if (task instanceof IFieldVisitor) {
                if (fvs == null)
                    fvs = new ArrayList<IFieldVisitor>();
                fvs.add((IFieldVisitor) task);
            }
            if (task instanceof IMethodVisitor) {
                if (mvs == null)
                    mvs = new ArrayList<IMethodVisitor>();
                mvs.add((IMethodVisitor) task);
            }
            if (task instanceof IInstVisitor) {
                if (ivs == null)
                    ivs = new ArrayList<IInstVisitor>();
                ivs.add((IInstVisitor) task);
            }
            if (task instanceof IHeapInstVisitor) {
                if (hivs == null)
                    hivs = new ArrayList<IHeapInstVisitor>();
                hivs.add((IHeapInstVisitor) task);
            }
            if (task instanceof IInvokeInstVisitor) {
                if (iivs == null)
                    iivs = new ArrayList<IInvokeInstVisitor>();
                iivs.add((IInvokeInstVisitor) task);
            }
            if (task instanceof INewInstVisitor) {
                if (nivs == null)
                    nivs = new ArrayList<INewInstVisitor>();
                nivs.add((INewInstVisitor) task);
            }
            if (task instanceof IMoveInstVisitor) {
                if (mivs == null)
                    mivs = new ArrayList<IMoveInstVisitor>();
                mivs.add((IMoveInstVisitor) task);
            }
            if (task instanceof ICastInstVisitor) {
                if (civs == null)
                    civs = new ArrayList<ICastInstVisitor>();
                civs.add((ICastInstVisitor) task);
            }
            if (task instanceof IPhiInstVisitor) {
                if (pivs == null)
                    pivs = new ArrayList<IPhiInstVisitor>();
                pivs.add((IPhiInstVisitor) task);
            }
            if (task instanceof IReturnInstVisitor) {
                if (rivs == null)
                    rivs = new ArrayList<IReturnInstVisitor>();
                rivs.add((IReturnInstVisitor) task);
            }
            if (task instanceof IAcqLockInstVisitor) {
                if (acqivs == null)
                    acqivs = new ArrayList<IAcqLockInstVisitor>();
                acqivs.add((IAcqLockInstVisitor) task);
            }
            if (task instanceof IRelLockInstVisitor) {
                if (relivs == null)
                    relivs = new ArrayList<IRelLockInstVisitor>();
                relivs.add((IRelLockInstVisitor) task);
            }
        }
        Program program = Program.g();
        if (cvs != null) {
			ReachableMethods reachableMethods = program.getMethods();
            for (SootClass c : program.getClasses()) {
                for (IClassVisitor cv : cvs)
                    cv.visit(c);
                if (fvs != null)
                    visitFields(c);
                if (mvs != null)
                    visitMethods(c, reachableMethods);
            }
        }
    }
}
