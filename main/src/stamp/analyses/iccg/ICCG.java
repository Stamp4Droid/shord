package stamp.analyses.iccg;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ICCG 
{
    //private HashSet<ICCGEdge> edges;
    String appName;
    private List<ICCGEdge> edges;
    private List<ICCGNode> nodes;
    Set<String> cpFlows = new HashSet<String>();
    Set<String> specCallSet = new HashSet<String>();
    Set<String> filterList = new HashSet<String>();

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
            String nodeName = node.toString().replace("$", "\\$");
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

    public void setSpecCall(Set<String> spec)
    {
        specCallSet = spec;
    }

    public void setFilterList(Set<String> filters)
    {
        filterList = filters;
    }

    public void setAppName(String name)
    {
        if(name.contains(".apk")){
            String[] arr = name.split("/");
            name = arr[arr.length-1];
        }
            
        appName = name;
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

    public void setFlows(Set<String> flows) {
        cpFlows = flows;
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

    private int getRowid(Statement statement)
    {
        ResultSet rs;
        try{
            rs = statement.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }catch(SQLException e){
            e.printStackTrace();
            return -1;
        }
    }


    ///insert iccg into database.
    public void updateDB() 
    {
		String dbLoc = "jdbc:sqlite:" + System.getProperty("stamp.dir") + "/iccg_scheme.sqlite";
        // load the sqlite-JDBC driver using the current class loader
        //assert(!appName.contains("/"));
        try{
            Class.forName("org.sqlite.JDBC");
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }

        Connection connection = null;
        ResultSet rs;
        try {

            // create a database connection
            connection = DriverManager.getConnection(dbLoc);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            //insert iccg 
            statement.executeUpdate("insert into iccg values(?, '"+appName+"', -1, -1)");
            int iccgId = getRowid(statement);

            //insert nodes
            for(ICCGNode nd : nodes ){
                statement.executeUpdate("insert into node values(?, '"
                        +nd.getComptName()+"', '"+nd.getType()+"', "+iccgId+")");
                int nodeId = getRowid(statement);
                nd.setRowid(nodeId);
                //insert permission
                for(String per: nd.getPermission()){
                    statement.executeUpdate("insert into permission values(?, '" 
                            + per +"', "+nodeId+ ","+iccgId+")");
                }
            }

            //insert src-sink
            for(String srcSink : cpFlows){
                String[] flowSet = srcSink.split("@");
                //remove "$" and "!".
                int srcId = getNode(flowSet[0]).getRowid();
                String src = flowSet[1];
                int tgtId = getNode(flowSet[2]).getRowid();
                String sink = flowSet[3];
                System.out.println("MYflow:" + src + "||"+srcId);
                statement.executeUpdate("insert into flow values(?, '" 
                    +src +"', '"+sink+"', "+srcId+", "+tgtId+","+iccgId+")");
            }

            //insert callerCamp. 
            for(String pair: specCallSet){
                String[] callSet = pair.split("@");
                int compId = getNode(callSet[0]).getRowid();
                String meth = callSet[1];
                statement.executeUpdate("insert into callerComp values(?, "+iccgId+", "+compId+", '"+meth+"')");
            }
            //insert intent filter. TBD
            for(String filter : filterList){
                String[] filterSet = filter.split("@");
                int compId = getNode(filterSet[0]).getRowid();
                String action = filterSet[1];
                int priority = Integer.parseInt(filterSet[2]);

                statement.executeUpdate("insert into intentFilter values(?, '"+action+"', "+priority+", "+compId+", "+iccgId+")");
            }

            //insert edges
            for(ICCGEdge ed : edges){
                int srcId = ed.getSrc().getRowid(); 
                int tgtId = ed.getTgt().getRowid(); 
                statement.executeUpdate("insert into edge values(?, "+srcId+", "+tgtId+", "+iccgId+")");
            }

            /*rs = statement.executeQuery("select * from iccg");
            while (rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("app_name"));
                System.out.println("id = " + rs.getInt("id"));
            }*/

        } catch (SQLException e) {

        // if the error message is "out of memory",
        // it probably means no database file is found
            System.err.println(e.getMessage());

        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e);
            }
        }
    }

}
