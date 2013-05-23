class ObjectOutputStream
{
 	private java.io.OutputStream wrappedStream;

 	public ObjectOutputStream(java.io.OutputStream output) throws java.io.IOException { this.wrappedStream = output; }

	public final void writeObject(java.lang.Object object) throws java.io.IOException {
 		wrappedStream.write(transfer(object));
 	}

	@STAMP(flows={@Flow(from="obj",to="@return")})
 	private static byte[] transfer(java.lang.Object obj) {
 		return new byte[0];
 	}
}
