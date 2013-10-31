package stamp.analyses.iccg;


public class ICCGEdge
{
    private ICCGNode source;
    private ICCGNode target;
    private String stmt;
    private boolean implicit;
    private boolean isAsynchronous;
    private String event;

    private String action = "";
    private String category = "";
    private String type = "";

    public ICCGNode getSrc() {
        return this.source;
    }

    public ICCGNode getTgt() {
        return this.target;
    }

    public void setSrc(ICCGNode node) {
        this.source = node;
    }

    public void setTgt(ICCGNode node) {
        this.target = node;
    }

    public boolean getImplicit() {
        return this.implicit;
    }

    public void setImplicit(boolean flag) {
        this.implicit = flag;
    }

    public boolean isAsynchronous() {
        return this.isAsynchronous;
    }

    public void setAsynchronous(boolean flag) {
        this.isAsynchronous= flag;
    }

    public String getEvent() {

        return event;
    }

    public void setEvent(String evt) {

        event = evt;
    }

    public String getAction() {

        return action;
    }

    public void setAction(String name) {

        action = name;
    }

    public String getCategory() {

        return category;
    }

    public void setCategory(String name) {

        category = name;
    }

    public String getType() {

        return type;
    }

    public void setType(String name) {

        type = name;
    }

    public boolean equals(ICCGEdge obj) {

        return (source.equals(obj.getSrc()) && target.equals(obj.getTgt()));
    }

    public String toString() {
        //com.foo.a$1 => a;
        /*String srcStr = "deadNode";
        String tgtStr = "deadNode";
        if(source != null) srcStr = source.toString();
        if(target != null) tgtStr = target.toString();

        if(isAsynchronous)
            return srcStr + " -> " + tgtStr + "[style=dashed]; ";
            //return srcStr + " -> " + tgtStr + "[label=\" "+ event + "\",style=dashed]; ";

        return srcStr + " -> " + tgtStr + "; "; */
        String ret = source.getId() + " -> " + target.getId();
        if(isAsynchronous) ret += "[style=dashed]; ";
        return ret;
    }

}
