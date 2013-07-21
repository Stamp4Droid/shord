package java.lang;

class Byte {

    @STAMP(flows = { @Flow(from = "value", to = "this") })
    public Byte(byte value) {
    }

    @STAMP(flows = { @Flow(from = "string", to = "this") })
    public Byte(java.lang.String string) throws java.lang.NumberFormatException {
    }

    @STAMP(flows = { @Flow(from = "string", to = "@return") })
    public static byte parseByte(java.lang.String string) throws java.lang.NumberFormatException {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "string", to = "@return") })
    public static byte parseByte(java.lang.String string, int radix) throws java.lang.NumberFormatException {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "string", to = "@return") })
    public static java.lang.Byte valueOf(java.lang.String string) throws java.lang.NumberFormatException {
        return new Byte((byte)0);
    }

    @STAMP(flows = { @Flow(from = "string", to = "@return") })
    public static java.lang.Byte valueOf(java.lang.String string, int radix) throws java.lang.NumberFormatException {
        return new Byte((byte)0);
    }

    @STAMP(flows = { @Flow(from = "b", to = "@return") })
    public static java.lang.Byte valueOf(byte b) {
        return new Byte((byte)0);
    }
}

