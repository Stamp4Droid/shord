package stamp.analyses.iccg;

import java.util.Set;
import java.util.HashSet;

public class ICCGNode
{

    private String comptName = "unknown";
    private Set<String> permission = new HashSet<String>();
    private boolean isMain = false;
    private Set<String> keys;
    private String type = "";
    private String intentFilter = "";

    //box:unknown, diamond:main, ellipse:activity, circle:service, triangle:broadcaster
    private String shape = "box";

    public ICCGNode() {
        this.comptName = "unknown";
    }

    public ICCGNode(String cptName) {
        //need to get rid of "$"
        if(cptName.contains("$")) 
            cptName = cptName.substring(0, cptName.indexOf("$"));
        this.comptName = cptName; 
    }

    public String getComptName() {

        return comptName;
    }

    public String getIntentFilter() {

        return intentFilter;
    }

    public void setIntentFilter(String s) {

        intentFilter = s;
    }


    public void setComptName(String name) {
        if(name.contains("$")) 
            name = name.substring(0, name.indexOf("$"));
        comptName = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String name) {
        type = name;
    }


   public Set getPermission() {

        return permission;
    }

    //do union.
    public void setPermission(Set p) {
        permission.addAll(p);
    }

    public boolean getMain() {

        return isMain;
    }

    public void setMain(boolean name) {

        isMain= name;
    }

    public String getShape() {

        return shape;
    }

    public void setShape(String name) {

        shape = name;
    } 

    public Set<String> getKeys() {

        return keys;
    }

    public void setKeys(Set name) {

        keys = name;
    }

   /* public String toString() {
        return comptName + "(" + "action:" + action + "category:" 
              + category + "type:" + type + "keys:" + keys + ")";
    }*/

     public String toString() {
        String str = comptName;
        if("unknown".equals(comptName) || "targetNotFound".equals(comptName)) return comptName;

        System.out.println("nodepermission: " + comptName + permission);

        if (str.contains("$"))
            str = str.substring(0, str.indexOf("$"));

        str = str.replaceAll("\\/", ".");

        if(str.contains("."))
            str= str.substring(str.lastIndexOf(".")+1,str.length());

        if(type.equals("receiver") && !"".equals(intentFilter))
            return str + "_" + intentFilter.replaceAll("\\.","") +"_" + type; 

        //return comptName; 
        //System.out.println("supername: " + str + "(" + type + ")"); 
        //return str + "(" + type + ")"; 
        return str + "_" + type; 
    }

    public boolean equals(ICCGNode obj) 
    { 
        return (this.getComptName().equals(obj.getComptName()));
    } 



}