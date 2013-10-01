package stamp.missingmodels.analysis;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.FileManager.FileType;
import stamp.missingmodels.util.FileManager.StampOutputFile;

public class StampRelationOutputFile implements StampOutputFile {
	private final String relationName;
	
	public StampRelationOutputFile(String relationName) {
		this.relationName = relationName;
	}
	
	@Override
	public String getName() {
		return "relations/" + this.relationName + ".rel";
	}

	@Override
	public FileType getType() {
		return FileType.OUTPUT;
	}

	@Override
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(this.relationName);
		rel.load();
		Iterable<int[]> res = rel.getAryNIntTuples();
		
		for(int[] tuple : res) {
			for(int i=0; i<tuple.length; i++) {
				sb.append(tuple[i] + ",");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
		}
		
		return sb.toString();
	}

}
