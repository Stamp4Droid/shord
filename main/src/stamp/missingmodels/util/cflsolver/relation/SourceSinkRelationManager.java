package stamp.missingmodels.util.cflsolver.relation;

public class SourceSinkRelationManager extends TaintWithContextRelationManager {
	public SourceSinkRelationManager() {
		this.add(new IndexWithContextRelation("FrameworkSource", "V", 1, 0, "V", 1, 0, "src2Label", null, (short)1));
		this.add(new IndexWithContextRelation("FrameworkSource", "V", 1, 0, "V", 1, 0, "Label2RefT", null, (short)1));
		this.add(new IndexWithContextRelation("FrameworkPrimSource", "U", 1, 0, "U", 1, 0, "src2Label", null, (short)1));
		this.add(new IndexWithContextRelation("FrameworkPrimSource", "U", 1, 0, "U", 1, 0, "Label2PrimT", null, (short)1));
		this.add(new IndexWithContextRelation("FrameworkSink", "V", 1, 0, "V", 1, 0, "sink2Label", null, (short)1));
		this.add(new IndexWithContextRelation("FrameworkSink", "V", 1, 0, "V", 1, 0, "SinkF2RefF", null, (short)1));
		this.add(new IndexWithContextRelation("FrameworkPrimSink", "V", 1, 0, "V", 1, 0, "sink2Label", null, (short)1));
		this.add(new IndexWithContextRelation("FrameworkPrimSink", "V", 1, 0, "V", 1, 0, "SinkF2PrimF", null, (short)1));
	}	
}
