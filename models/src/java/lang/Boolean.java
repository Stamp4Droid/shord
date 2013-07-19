package java.lang;

class Boolean {

    @STAMP(flows = { @Flow(from = "this", to = "return") })
    public boolean booleanValue() {
        return true;
    }

    @STAMP(flows = { @Flow(from = "this", to = "return") })
    public int hashCode() {
        return 1231;
    }

    @STAMP(flows = { @Flow(from = "string", to = "@return") })
    public static java.lang.Boolean valueOf(java.lang.String string) {
        return new Boolean(true);
    }

    @STAMP(flows = { @Flow(from = "b", to = "@return") })
    public static java.lang.Boolean valueOf(boolean b) {
        return new Boolean(b);
    }
}

