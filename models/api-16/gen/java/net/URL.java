package java.net;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public final class URL implements java.io.Serializable {

    public static synchronized void setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory factory) {
        throw new RuntimeException("Stub!");
    }

    protected void set(java.lang.String protocol, java.lang.String host, int port, java.lang.String file, java.lang.String ref) {
        throw new RuntimeException("Stub!");
    }

    public boolean equals(java.lang.Object o) {
        throw new RuntimeException("Stub!");
    }

    public boolean sameFile(java.net.URL otherURL) {
        throw new RuntimeException("Stub!");
    }

    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    public final java.io.InputStream openStream() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public java.net.URI toURI() throws java.net.URISyntaxException {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String toString() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String toExternalForm() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getProtocol() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getAuthority() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getUserInfo() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getHost() {
        throw new RuntimeException("Stub!");
    }

    public int getPort() {
        throw new RuntimeException("Stub!");
    }

    public int getDefaultPort() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getFile() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getPath() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getQuery() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getRef() {
        throw new RuntimeException("Stub!");
    }

    protected void set(java.lang.String protocol, java.lang.String host, int port, java.lang.String authority, java.lang.String userInfo, java.lang.String path, java.lang.String query, java.lang.String ref) {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "spec", to = "this") })
    public URL(java.lang.String spec) throws java.net.MalformedURLException {
    }

    @STAMP(flows = { @Flow(from = "spec", to = "spec") })
    public URL(java.net.URL context, java.lang.String spec) throws java.net.MalformedURLException {
    }

    @STAMP(flows = { @Flow(from = "spec", to = "this") })
    public URL(java.net.URL context, java.lang.String spec, java.net.URLStreamHandler handler) throws java.net.MalformedURLException {
    }

    @STAMP(flows = { @Flow(from = "file", to = "this") })
    public URL(java.lang.String protocol, java.lang.String host, java.lang.String file) throws java.net.MalformedURLException {
    }

    @STAMP(flows = { @Flow(from = "file", to = "this") })
    public URL(java.lang.String protocol, java.lang.String host, int port, java.lang.String file) throws java.net.MalformedURLException {
    }

    @STAMP(flows = { @Flow(from = "file", to = "this") })
    public URL(java.lang.String protocol, java.lang.String host, int port, java.lang.String file, java.net.URLStreamHandler handler) throws java.net.MalformedURLException {
    }

    @STAMP(flows = { @Flow(from = "this", to = "!this"), @Flow(from = "this", to = "!INTERNET") })
    public java.net.URLConnection openConnection() throws java.io.IOException {
        return new StampURLConnection(this);
    }

    @STAMP(flows = { @Flow(from = "this", to = "!this"), @Flow(from = "this", to = "!INTERNET") })
    public java.net.URLConnection openConnection(java.net.Proxy proxy) throws java.io.IOException {
        return new StampURLConnection(this);
    }

    @STAMP(flows = { @Flow(from = "this", to = "!this"), @Flow(from = "this", to = "!INTERNET") })
    public final java.lang.Object getContent() throws java.io.IOException {
        return null;
    }

    @STAMP(flows = { @Flow(from = "this", to = "!this"), @Flow(from = "this", to = "!INTERNET") })
    public final java.lang.Object getContent(java.lang.Class[] types) throws java.io.IOException {
        return null;
    }
}

