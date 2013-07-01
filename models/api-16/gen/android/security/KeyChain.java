package android.security;
public final class KeyChain
{
public  KeyChain() { throw new RuntimeException("Stub!"); }
public static  android.content.Intent createInstallIntent() { throw new RuntimeException("Stub!"); }
public static  void choosePrivateKeyAlias(android.app.Activity activity, android.security.KeyChainAliasCallback response, java.lang.String[] keyTypes, java.security.Principal[] issuers, java.lang.String host, int port, java.lang.String alias) { throw new RuntimeException("Stub!"); }
public static  java.security.PrivateKey getPrivateKey(android.content.Context context, java.lang.String alias) throws android.security.KeyChainException, java.lang.InterruptedException { throw new RuntimeException("Stub!"); }
public static  java.security.cert.X509Certificate[] getCertificateChain(android.content.Context context, java.lang.String alias) throws android.security.KeyChainException, java.lang.InterruptedException { throw new RuntimeException("Stub!"); }
public static final java.lang.String EXTRA_NAME = "name";
public static final java.lang.String EXTRA_CERTIFICATE = "CERT";
public static final java.lang.String EXTRA_PKCS12 = "PKCS12";
public static final java.lang.String ACTION_STORAGE_CHANGED = "android.security.STORAGE_CHANGED";
}
