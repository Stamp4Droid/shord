class System
{
	@STAMP(flows={@Flow(from="src",to="dst")})
	public static void arraycopy(java.lang.Object src, int srcPos, java.lang.Object dst, int dstPos, int length)
	{
	}


	@STAMP(flows = {@Flow(from="$MyTime",to="@return")}) 
    public static long currentTimeMillis() {
        return 1L;
    }
}
