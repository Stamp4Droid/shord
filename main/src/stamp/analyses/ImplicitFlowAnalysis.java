package stamp.analyses;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import chord.project.Chord;

/**
 * @author obastani
 */
/*
@Chord(name = "implicit-flow-java",
consumes = { "M", "Z" },
produces = { "RefRefImp", "RefPrimImp", "PrimRefImp", "PrimPrimImp" },
namesOfTypes = {},
types = {},
namesOfSigns = { "RefRefImp", "RefPrimImp", "PrimRefImp", "PrimPrimImp" },
signs = { "V0,V1:V0xV1", "V0,U0:V0_U0", "U0,V0:V0_U0", "U0,U0:U0xU1" })
*/
public class ImplicitFlowAnalysis extends JavaAnalysis {
	/*
	private ProgramRel relRefRefImp;
	private ProgramRel relRefPrimImp;
	private ProgramRel relPrimRefImp;
	private ProgramRel relPrimPrimImp;

	public void run() {
		this.relRefRefImp = (ProgramRel)ClassicProject.g().getTrgt("InLabelArg");
		this.relRefPrimImp = (ProgramRel)ClassicProject.g().getTrgt("InLabelRet");
		this.relPrimRefImp = (ProgramRel)ClassicProject.g().getTrgt("OutLabelArg");
		this.relPrimPrimImp = (ProgramRel)ClassicProject.g().getTrgt("OutLabelRet");
		
		this.relRefRefImp.zero();
		this.relRefPrimImp.zero();
		this.relPrimRefImp.zero();
		this.relPrimPrimImp.zero();
		
		this.relRefRefImp.save();
		this.relRefPrimImp.save();
		this.relPrimRefImp.save();
		this.relPrimPrimImp.save();
	}
	*/
}
