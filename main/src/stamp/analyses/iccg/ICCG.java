package stamp.analyses.iccg;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class ICCG 
{
    //private HashSet<ICCGEdge> edges;
    private List<ICCGEdge> edges;
    private List<ICCGNode> nodes;

    ///All external or unknown nodes share 1 instance.
    private static ICCGNode unknownNode = new ICCGNode(); 

    public ICCG() {
        edges = new ArrayList<ICCGEdge>();
        nodes = new ArrayList<ICCGNode>();
    }

    public String getSignature() {

    //node2 [style=filled, fillcolor=red] 
	//first dump all the permission info.
        String sig = "digraph G {\n ";

        for (ICCGNode node : nodes) {
	    //if(!"unknown".equals(node.getComptName()) && !"targetNotFound".equals(node.getComptName()) ){
            String nodeName = node.toString();
            String pers = "";
            String flows = "";
            String extraStyle = "";

            for(String perm : node.getPermission())
                pers += " \\n " + perm;

            for(String flow : node.getFlow())
                flows += " \\n " + flow;

            if(!node.getMain())
                extraStyle += " ,style=filled, fillcolor=red";

            nodeName += pers;
            nodeName += flows;
            //sig += nodeName + "[shape=" + node.getShape()+"];";
            sig += '\n' + Integer.toString(node.getId()) + 
                "[label=\""+ nodeName + "\", shape=" + node.getShape() + extraStyle+"];";

        }

        for (ICCGEdge edge:edges) {
            sig += '\n' + edge.toString() ;
        }
        sig += "\n}";
        return sig;
    }

    /*public void setUnknown(ICCGNode node) {
        unknownNode = node;
    }*/

    public ICCGNode getUnknown() {
        return unknownNode;
    }

    public int size() {
        return 1;
    }

    public void addEdge(ICCGEdge eg) {
        //check if it's duplicated.
        if(eg.getSrc() == null || eg.getTgt() == null) {
            System.out.println("ERROR: adding null edge: " + eg.getSrc() + "|" + eg.getTgt());
            return;
        }

        boolean flag = true;
        for(ICCGEdge e:edges) {
            //if(e.getSrc().equals(eg.getSrc()) && e.getTgt().equals(eg.getTgt())) {
            if(e.equals(eg)) {
                System.out.println("duplicated!" + eg + edges);
                flag = false;
                break;
            }
        }
        if(flag) {
            System.out.println("current edges:" + eg + eg.getSrc() + eg.getTgt() + " || " + edges);
            edges.add(eg);
        }
    }

/*    private boolean isEqual(ICCGNode e1, ICCGNode e2) {

        return (e1.getSrc().equals(e2.getSrc()) && e1.getTgt().equals(e2.getTgt()));
    }*/

    public void addNode(ICCGNode n) {
        nodes.add(n);
    }

    public void setFlow(Set<String> flows) {
        for(String flow : flows){
            String[] fset = flow.split("@");
            String comp = fset[0];
            String src = fset[1];
            String sink = fset[2];
            ICCGNode node = getNode(comp);
            node.addFlow(src.replace("$", "\\$")+"->"+sink.replace("!", "\\!"));
        }
    }

    public ICCGNode getNode(String name) {
        for(ICCGNode node:nodes){
            if(node.getComptName().equals(name)) return node;
        }
        System.out.println("Searching..fail." + name + "||" + nodes);

        return null;
    }

    public void removeEdge(ICCGEdge e) {
        edges.remove(e);
    }
}
