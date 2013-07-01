package android.provider;
public class VoicemailContract
{
public static final class Voicemails
  implements android.provider.BaseColumns, android.provider.OpenableColumns
{
Voicemails() { throw new RuntimeException("Stub!"); }
public static  android.net.Uri buildSourceUri(java.lang.String packageName) { throw new RuntimeException("Stub!"); }
public static final android.net.Uri CONTENT_URI;
public static final java.lang.String DIR_TYPE = "vnd.android.cursor.dir/voicemails";
public static final java.lang.String ITEM_TYPE = "vnd.android.cursor.item/voicemail";
public static final java.lang.String NUMBER = "number";
public static final java.lang.String DATE = "date";
public static final java.lang.String DURATION = "duration";
public static final java.lang.String IS_READ = "is_read";
public static final java.lang.String SOURCE_PACKAGE = "source_package";
public static final java.lang.String SOURCE_DATA = "source_data";
public static final java.lang.String HAS_CONTENT = "has_content";
public static final java.lang.String MIME_TYPE = "mime_type";
static { CONTENT_URI = null; }
}
public static final class Status
  implements android.provider.BaseColumns
{
Status() { throw new RuntimeException("Stub!"); }
public static  android.net.Uri buildSourceUri(java.lang.String packageName) { throw new RuntimeException("Stub!"); }
public static final android.net.Uri CONTENT_URI;
public static final java.lang.String DIR_TYPE = "vnd.android.cursor.dir/voicemail.source.status";
public static final java.lang.String ITEM_TYPE = "vnd.android.cursor.item/voicemail.source.status";
public static final java.lang.String SOURCE_PACKAGE = "source_package";
public static final java.lang.String SETTINGS_URI = "settings_uri";
public static final java.lang.String VOICEMAIL_ACCESS_URI = "voicemail_access_uri";
public static final java.lang.String CONFIGURATION_STATE = "configuration_state";
public static final int CONFIGURATION_STATE_OK = 0;
public static final int CONFIGURATION_STATE_NOT_CONFIGURED = 1;
public static final int CONFIGURATION_STATE_CAN_BE_CONFIGURED = 2;
public static final java.lang.String DATA_CHANNEL_STATE = "data_channel_state";
public static final int DATA_CHANNEL_STATE_OK = 0;
public static final int DATA_CHANNEL_STATE_NO_CONNECTION = 1;
public static final java.lang.String NOTIFICATION_CHANNEL_STATE = "notification_channel_state";
public static final int NOTIFICATION_CHANNEL_STATE_OK = 0;
public static final int NOTIFICATION_CHANNEL_STATE_NO_CONNECTION = 1;
public static final int NOTIFICATION_CHANNEL_STATE_MESSAGE_WAITING = 2;
static { CONTENT_URI = null; }
}
VoicemailContract() { throw new RuntimeException("Stub!"); }
public static final java.lang.String AUTHORITY = "com.android.voicemail";
public static final java.lang.String PARAM_KEY_SOURCE_PACKAGE = "source_package";
public static final java.lang.String ACTION_NEW_VOICEMAIL = "android.intent.action.NEW_VOICEMAIL";
public static final java.lang.String ACTION_FETCH_VOICEMAIL = "android.intent.action.FETCH_VOICEMAIL";
public static final java.lang.String EXTRA_SELF_CHANGE = "com.android.voicemail.extra.SELF_CHANGE";
}
