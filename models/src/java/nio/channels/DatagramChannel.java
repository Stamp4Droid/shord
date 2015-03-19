class DatagramChannel
{
    public static java.nio.channels.DatagramChannel open() { return new java.nio.channels.StampDatagramChannel(); }

    @STAMP(flows = {@Flow(from="!SOCKET",to="this")})
	protected DatagramChannel(java.nio.channels.spi.SelectorProvider provider) { super(provider); }

    @STAMP(flows = {@Flow(from="buffer",to="!this")})
	public abstract int send(java.nio.ByteBuffer buffer, java.net.SocketAddress addr) throws java.io.IOException;
}

