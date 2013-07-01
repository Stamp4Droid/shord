package android.view;
public class KeyCharacterMap
  implements android.os.Parcelable
{
@java.lang.Deprecated()
public static class KeyData
{
public  KeyData() { throw new RuntimeException("Stub!"); }
public static final int META_LENGTH = 4;
public char displayLabel;
public char number;
public char[] meta = null;
}
public static class UnavailableException
  extends android.util.AndroidRuntimeException
{
public  UnavailableException(java.lang.String msg) { throw new RuntimeException("Stub!"); }
}
KeyCharacterMap() { throw new RuntimeException("Stub!"); }
protected  void finalize() throws java.lang.Throwable { throw new RuntimeException("Stub!"); }
public static  android.view.KeyCharacterMap load(int deviceId) { throw new RuntimeException("Stub!"); }
public  int get(int keyCode, int metaState) { throw new RuntimeException("Stub!"); }
public  char getNumber(int keyCode) { throw new RuntimeException("Stub!"); }
public  char getMatch(int keyCode, char[] chars) { throw new RuntimeException("Stub!"); }
public  char getMatch(int keyCode, char[] chars, int metaState) { throw new RuntimeException("Stub!"); }
public  char getDisplayLabel(int keyCode) { throw new RuntimeException("Stub!"); }
public static  int getDeadChar(int accent, int c) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  boolean getKeyData(int keyCode, android.view.KeyCharacterMap.KeyData results) { throw new RuntimeException("Stub!"); }
public  android.view.KeyEvent[] getEvents(char[] chars) { throw new RuntimeException("Stub!"); }
public  boolean isPrintingKey(int keyCode) { throw new RuntimeException("Stub!"); }
public  int getKeyboardType() { throw new RuntimeException("Stub!"); }
public  int getModifierBehavior() { throw new RuntimeException("Stub!"); }
public static  boolean deviceHasKey(int keyCode) { throw new RuntimeException("Stub!"); }
public static  boolean[] deviceHasKeys(int[] keyCodes) { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel out, int flags) { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public static final int BUILT_IN_KEYBOARD = 0;
public static final int VIRTUAL_KEYBOARD = -1;
public static final int NUMERIC = 1;
public static final int PREDICTIVE = 2;
public static final int ALPHA = 3;
public static final int FULL = 4;
public static final int SPECIAL_FUNCTION = 5;
public static final char HEX_INPUT = 61184;
public static final char PICKER_DIALOG_INPUT = 61185;
public static final int MODIFIER_BEHAVIOR_CHORDED = 0;
public static final int MODIFIER_BEHAVIOR_CHORDED_OR_TOGGLED = 1;
public static final int COMBINING_ACCENT = -2147483648;
public static final int COMBINING_ACCENT_MASK = 2147483647;
public static final android.os.Parcelable.Creator<android.view.KeyCharacterMap> CREATOR;
static { CREATOR = null; }
}
