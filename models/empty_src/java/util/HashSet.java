class HashSet
{
    private E f;

	@STAMP(flows = {@Flow(from="object",to="this")})
    public boolean add(E object) {
	    return true;
	}

}
