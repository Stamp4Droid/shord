class ArrayList<E>
{
    private E f;

	@STAMP(flows = {@Flow(from="object",to="this")})
    public E set(int index, E object) {
	    return null;
    }

	@STAMP(flows = {@Flow(from="object",to="this")})
    public  boolean add(E object)
    {
	return false;
    }

	@STAMP(flows = {@Flow(from="object",to="this")})
    public  void add(int index, E object)
	{
	}

	@STAMP(flows = {@Flow(from="this",to="@return")})
    public 	E get(int index)
    {
	return null;
    }

	@STAMP(flows = {@Flow(from="this",to="@return")})
    public 	E remove(int index)
    {
	return null;
    }
}
