package org.apache.http.message;

public class BasicNameValuePair implements org.apache.http.NameValuePair, java.lang.Cloneable {

    public boolean equals(java.lang.Object object) {
        throw new RuntimeException("Stub!");
    }

    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.Object clone() throws java.lang.CloneNotSupportedException {
        throw new RuntimeException("Stub!");
    }

    private String name;

    private String value;

    public BasicNameValuePair(java.lang.String name, java.lang.String value) {
        this.name = name;
        this.value = value;
    }

    public java.lang.String getName() {
        return name;
    }

    public java.lang.String getValue() {
        return value;
    }

    public java.lang.String toString() {
        return name + value;
    }
}

