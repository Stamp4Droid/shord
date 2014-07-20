package chord.analyses.provenance.kcfa;

import shord.analyses.DomH;
import shord.analyses.DomI;
import shord.analyses.DomK;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import chord.project.Chord;

@Chord(name="test-cfa-java",
	consumes = { "I", "H", "K" },
	produces = { "IK", "HK", "OK" }
)
public class TestAnalysis extends JavaAnalysis {
	private ProgramRel relHK;
	private ProgramRel relIK;
	private ProgramRel relOK;
	private DomH domH;
	private DomI domI;
	private DomK domK;
	
	@Override
	public void run() {
	/*	domH = (DomH) ClassicProject.g().getTrgt("H");
		domI = (DomI) ClassicProject.g().getTrgt("I");
		domK = (DomK) ClassicProject.g().getTrgt("K");
        relHK = (ProgramRel) ClassicProject.g().getTrgt("HK");
        relIK = (ProgramRel) ClassicProject.g().getTrgt("IK");
        relOK = (ProgramRel) ClassicProject.g().getTrgt("OK");
        
        relHK.zero();
        relOK.zero();
        for (int hIdx = 0; hIdx < domH.size(); hIdx++) {
        	relHK.add(hIdx, 1);
        	relOK.add(hIdx, 0);
        }
        relHK.save();
        relOK.save();
        
        relIK.zero();
        for (int iIdx = 0; iIdx < domI.size(); iIdx++) {
        	relIK.add(iIdx, domK.getOrAdd(new Integer(1)));
        }
        relIK.save();
 */       
		ClassicProject.g().runTask("HIDumper-java");
		ClassicProject.g().runTask("cipa-0cfa-dlog");
		//populateRel();
		ClassicProject.g().runTask("simple-pro-ctxts-java");
		ClassicProject.g().runTask("kobj-bit-init-dlog");
		ClassicProject.g().runTask("pro-argCopy-dlog");
		ClassicProject.g().runTask("pro-cspa-kobj-dlog");

	}
	
 /*   public void populateRel(){
    	List<Pair<Quad,Quad>> allQueries = new ArrayList<Pair<Quad,Quad>>();
    	ProgramRel relAllQueries= (ProgramRel) ClassicProject.g().getTrgt("allTypestateQueries");
    	ClassicProject.g().runTask(relAllQueries);
    	relAllQueries.load();
    	Iterable<Pair<Quad,Quad>> tuples = relAllQueries.getAry2ValTuples();
    	for(Pair<Quad,Quad> p : tuples){
    		allQueries.add(p);
    	}
    	relAllQueries.close();
    	
    	Collections.shuffle(allQueries);
    	int numQueries = Integer.getInteger("chord.provenance.typestateQueries", 500);
    	ProgramRel relCurrentQueries = (ProgramRel) ClassicProject.g().getTrgt("currentQueries");
    	relCurrentQueries.zero();
    	for(int i = 0; i < numQueries && i < allQueries.size(); i++){
    		Pair<Quad,Quad> chosenQuery = allQueries.get(i);
    		relCurrentQueries.add(chosenQuery.val0,chosenQuery.val1);
    	}
    	relCurrentQueries.save();
    }
 */
}

