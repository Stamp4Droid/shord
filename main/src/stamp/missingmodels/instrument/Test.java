package stamp.missingmodels.instrument;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.InvokeStmt;
import soot.options.Options;

public class Test {
	public static void main(String[] args) {
		//prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);

		//output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_dex);

		// resolve the PrintStream and System soot-classes
		Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);
		
		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {
			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
				final PatchingChain<Unit> units = b.getUnits();		
				//important to use snapshotIterator here
				for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
					final Unit u = iter.next();
					u.apply(new AbstractStmtSwitch() {

						public void caseInvokeStmt(InvokeStmt stmt) {
							//code here
						}

					});
				}
			}
		}));
	}
}
