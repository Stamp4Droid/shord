package stamp.analyses;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import shord.analyses.LocalVarNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import chord.util.tuple.object.Pair;

public class LocalsToVarNodeMap {

	// Helper for the next method
	private static void getVarNodesInHelper(Map<Local,LocalVarNode> localToVarNodeMap, Value value, Collection<LocalVarNode> result) {
		List<ValueBox> boxes = value.getUseBoxes();
		for(ValueBox box : boxes) {
			Value childValue = box.getValue();
			//System.out.println("VALUE recurse: " + childValue);
			if(value instanceof Local) {
				LocalVarNode varNode = localToVarNodeMap.get((Local)value);
				if(varNode != null) {
					result.add(varNode);
				} else {
					System.out.println("ERROR: No varnode found for local " + value);
				}
			}
			getVarNodesInHelper(localToVarNodeMap, childValue, result);
			// TODO: any other cases?
			// TODO: e.g. box instanceof CastExpr
		}
	}

	// This iterates over the unit and returns the VarNodes corresponding to the (use or def) Locals in the unit
	public static Collection<LocalVarNode> getVarNodesIn(Map<Local,LocalVarNode> localToVarNodeMap, Unit unit, boolean isDef) {
		Collection<LocalVarNode> result = new HashSet<LocalVarNode>();
		List<ValueBox> boxes = isDef ? unit.getDefBoxes() : unit.getUseBoxes();
		for(ValueBox box : boxes) {
			Value value = box.getValue();
			//System.out.println("VALUE " + isDef + ": " + value);
			if(value instanceof Local) {
				LocalVarNode varNode = localToVarNodeMap.get((Local)value);
				if(varNode != null) {
					result.add(varNode);
				} else {
					System.out.println("ERROR: No varnode found for local " + value);
				}
			}
			if(!isDef) {
				getVarNodesInHelper(localToVarNodeMap, value, result);
			}
			// TODO: any other cases?
			// TODO: e.g. box instanceof CastExpr
		}
		return result;
	}
	
	// This constructs a map from Locals to VarNodes for each (reachable) method in the program
	private Map<SootMethod,Map<Local,LocalVarNode>> localToVarNodeMaps = null;
	private void constructLocalToVarNodeMaps() {
		this.localToVarNodeMaps = new HashMap<SootMethod,Map<Local,LocalVarNode>>();
		ProgramRel relMV = (ProgramRel)ClassicProject.g().getTrgt("MV");
		relMV.load();
		for(Pair<Object,Object> pair : relMV.getAry2ValTuples()) {
			VarNode varNode = (VarNode)pair.val1;
			if(varNode instanceof LocalVarNode) {
				SootMethod method = (SootMethod)pair.val0;
				Map<Local,LocalVarNode> localToVarNodeMap = this.localToVarNodeMaps.get(method);
				if(localToVarNodeMap == null) {
					localToVarNodeMap = new HashMap<Local,LocalVarNode>();
					this.localToVarNodeMaps.put(method, localToVarNodeMap);
				}

				LocalVarNode localVarNode = (LocalVarNode)varNode;
				localToVarNodeMap.put(localVarNode.local, localVarNode);

				//System.out.println("ADDED VAR V " + localVarNode.local + " TO METHOD " + method);
			}
		}
		relMV.close();

		ProgramRel relMU = (ProgramRel)ClassicProject.g().getTrgt("MU");
		relMU.load();
		for(Pair<Object,Object> pair : relMU.getAry2ValTuples()) {
			VarNode varNode = (VarNode)pair.val1;
			if(varNode instanceof LocalVarNode) {
				SootMethod method = (SootMethod)pair.val0;
				Map<Local,LocalVarNode> localToVarNodeMap = this.localToVarNodeMaps.get(method);
				if(localToVarNodeMap == null) {
					localToVarNodeMap = new HashMap<Local,LocalVarNode>();
					this.localToVarNodeMaps.put(method, localToVarNodeMap);
				}

				LocalVarNode localVarNode = (LocalVarNode)varNode;
				localToVarNodeMap.put(localVarNode.local, localVarNode);

				//System.out.println("ADDED VAR U " + localVarNode.local + " TO METHOD " + method);
			}
		}
		relMU.close();
	}
	
	private static LocalsToVarNodeMap maps = new LocalsToVarNodeMap();
	// Constructs (using the above method) and returns Locals -> VarNodes map
	public static Map<Local,LocalVarNode> getLocalToVarNodeMap(SootMethod method) {
		if(maps.localToVarNodeMaps == null) {
			maps.constructLocalToVarNodeMaps();
		}
		return maps.localToVarNodeMaps.get(method);		
	}
	
	private static Map<SootMethod,Map<LocalVarNode,Local>> inverseMaps = new HashMap<SootMethod,Map<LocalVarNode,Local>>();
	public static Map<LocalVarNode,Local> getVarNodeToLocalMap(SootMethod method) {
		Map<LocalVarNode,Local> inverseMap = inverseMaps.get(method);
		if(inverseMap == null) {
			inverseMap = new HashMap<LocalVarNode,Local>();
			Map<Local,LocalVarNode> map = getLocalToVarNodeMap(method);
			for(Map.Entry<Local,LocalVarNode> entry : map.entrySet()) {
				if(inverseMap.get(entry.getValue()) != null) {
					throw new RuntimeException("Duplicate local var: " + entry.getKey() + " and " + inverseMap.get(entry.getValue()));
				}
				inverseMap.put(entry.getValue(), entry.getKey());
			}
			inverseMaps.put(method, inverseMap);
		}
		return inverseMap;
	}
}
