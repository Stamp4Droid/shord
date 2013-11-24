class Formatter
{

	@STAMP(flows = {@Flow(from="this",to="@return")})
    public  java.lang.String toString() { throw new RuntimeException("Stub!"); }

	@STAMP(flows = {@Flow(from="args",to="this")})
    public  java.util.Formatter format(java.lang.String format, java.lang.Object... args) { throw new RuntimeException("Stub!"); }

	@STAMP(flows = {@Flow(from="args",to="this")})
    public  java.util.Formatter format(java.util.Locale l, java.lang.String format, java.lang.Object... args) { throw new RuntimeException("Stub!"); }
}
