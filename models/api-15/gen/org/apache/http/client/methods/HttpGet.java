package org.apache.http.client.methods;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class HttpGet extends org.apache.http.client.methods.HttpRequestBase {

    public HttpGet() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getMethod() {
        throw new RuntimeException("Stub!");
    }

    public static final java.lang.String METHOD_NAME = "GET";

    @STAMP(flows = { @Flow(from = "uri", to = "this") })
    public HttpGet(java.net.URI uri) {
    }

    @STAMP(flows = { @Flow(from = "uri", to = "this") })
    public HttpGet(java.lang.String uri) {
    }
}

