package stamp.harnessgen;

public class XmlNode {

    String name;
    String permission;
    boolean isMain;
    String type;
    String intentFilter;

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }
    public void setPermission(String s) {
        permission = s;
    }
    public String getPermission() {
        return permission;
    }

    public void setType(String s) {
        type = s;
    }

    public String getType() {
        return type;
    }

    public boolean getMain() {
        return isMain;
    }

    public void setMain(boolean s) {
        isMain = s;
    }

    public void setIntentFilter(String s) {
        intentFilter = s;
    }

    public String getIntentFilter() {
        return intentFilter;
    }
}
