package android.app.admin;
public class DevicePolicyManager
{
DevicePolicyManager() { throw new RuntimeException("Stub!"); }
public  boolean isAdminActive(android.content.ComponentName who) { throw new RuntimeException("Stub!"); }
public  java.util.List<android.content.ComponentName> getActiveAdmins() { throw new RuntimeException("Stub!"); }
public  void removeActiveAdmin(android.content.ComponentName who) { throw new RuntimeException("Stub!"); }
public  boolean hasGrantedPolicy(android.content.ComponentName admin, int usesPolicy) { throw new RuntimeException("Stub!"); }
public  void setPasswordQuality(android.content.ComponentName admin, int quality) { throw new RuntimeException("Stub!"); }
public  int getPasswordQuality(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  void setPasswordMinimumLength(android.content.ComponentName admin, int length) { throw new RuntimeException("Stub!"); }
public  int getPasswordMinimumLength(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  void setPasswordMinimumUpperCase(android.content.ComponentName admin, int length) { throw new RuntimeException("Stub!"); }
public  int getPasswordMinimumUpperCase(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  void setPasswordMinimumLowerCase(android.content.ComponentName admin, int length) { throw new RuntimeException("Stub!"); }
public  int getPasswordMinimumLowerCase(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  void setPasswordMinimumLetters(android.content.ComponentName admin, int length) { throw new RuntimeException("Stub!"); }
public  int getPasswordMinimumLetters(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  void setPasswordMinimumNumeric(android.content.ComponentName admin, int length) { throw new RuntimeException("Stub!"); }
public  int getPasswordMinimumNumeric(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  void setPasswordMinimumSymbols(android.content.ComponentName admin, int length) { throw new RuntimeException("Stub!"); }
public  int getPasswordMinimumSymbols(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  void setPasswordMinimumNonLetter(android.content.ComponentName admin, int length) { throw new RuntimeException("Stub!"); }
public  int getPasswordMinimumNonLetter(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  void setPasswordHistoryLength(android.content.ComponentName admin, int length) { throw new RuntimeException("Stub!"); }
public  void setPasswordExpirationTimeout(android.content.ComponentName admin, long timeout) { throw new RuntimeException("Stub!"); }
public  long getPasswordExpirationTimeout(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  long getPasswordExpiration(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  int getPasswordHistoryLength(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  int getPasswordMaximumLength(int quality) { throw new RuntimeException("Stub!"); }
public  boolean isActivePasswordSufficient() { throw new RuntimeException("Stub!"); }
public  int getCurrentFailedPasswordAttempts() { throw new RuntimeException("Stub!"); }
public  void setMaximumFailedPasswordsForWipe(android.content.ComponentName admin, int num) { throw new RuntimeException("Stub!"); }
public  int getMaximumFailedPasswordsForWipe(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  boolean resetPassword(java.lang.String password, int flags) { throw new RuntimeException("Stub!"); }
public  void setMaximumTimeToLock(android.content.ComponentName admin, long timeMs) { throw new RuntimeException("Stub!"); }
public  long getMaximumTimeToLock(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  void lockNow() { throw new RuntimeException("Stub!"); }
public  void wipeData(int flags) { throw new RuntimeException("Stub!"); }
public  int setStorageEncryption(android.content.ComponentName admin, boolean encrypt) { throw new RuntimeException("Stub!"); }
public  boolean getStorageEncryption(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public  int getStorageEncryptionStatus() { throw new RuntimeException("Stub!"); }
public  void setCameraDisabled(android.content.ComponentName admin, boolean disabled) { throw new RuntimeException("Stub!"); }
public  boolean getCameraDisabled(android.content.ComponentName admin) { throw new RuntimeException("Stub!"); }
public static final java.lang.String ACTION_ADD_DEVICE_ADMIN = "android.app.action.ADD_DEVICE_ADMIN";
public static final java.lang.String EXTRA_DEVICE_ADMIN = "android.app.extra.DEVICE_ADMIN";
public static final java.lang.String EXTRA_ADD_EXPLANATION = "android.app.extra.ADD_EXPLANATION";
public static final java.lang.String ACTION_SET_NEW_PASSWORD = "android.app.action.SET_NEW_PASSWORD";
public static final int PASSWORD_QUALITY_UNSPECIFIED = 0;
public static final int PASSWORD_QUALITY_BIOMETRIC_WEAK = 32768;
public static final int PASSWORD_QUALITY_SOMETHING = 65536;
public static final int PASSWORD_QUALITY_NUMERIC = 131072;
public static final int PASSWORD_QUALITY_ALPHABETIC = 262144;
public static final int PASSWORD_QUALITY_ALPHANUMERIC = 327680;
public static final int PASSWORD_QUALITY_COMPLEX = 393216;
public static final int RESET_PASSWORD_REQUIRE_ENTRY = 1;
public static final int WIPE_EXTERNAL_STORAGE = 1;
public static final int ENCRYPTION_STATUS_UNSUPPORTED = 0;
public static final int ENCRYPTION_STATUS_INACTIVE = 1;
public static final int ENCRYPTION_STATUS_ACTIVATING = 2;
public static final int ENCRYPTION_STATUS_ACTIVE = 3;
public static final java.lang.String ACTION_START_ENCRYPTION = "android.app.action.START_ENCRYPTION";
}
