package java.security;
public abstract class Permission
  implements java.security.Guard, java.io.Serializable
{
public  Permission(java.lang.String name) { throw new RuntimeException("Stub!"); }
public final  java.lang.String getName() { throw new RuntimeException("Stub!"); }
public  void checkGuard(java.lang.Object obj) throws java.lang.SecurityException { throw new RuntimeException("Stub!"); }
public  java.security.PermissionCollection newPermissionCollection() { throw new RuntimeException("Stub!"); }
public abstract  java.lang.String getActions();
public abstract  boolean implies(java.security.Permission permission);
}
