package android.provider;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;
import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

@java.lang.SuppressWarnings(value = { "unused" })
public final class ContactsContract {

    public static final class Directory implements android.provider.BaseColumns {

        Directory() {
            throw new RuntimeException("Stub!");
        }

        public static void notifyDirectoryChange(android.content.ContentResolver resolver) {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/contact_directories";

        public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_directory";

        public static final long DEFAULT = 0L;

        public static final long LOCAL_INVISIBLE = 1L;

        public static final java.lang.String PACKAGE_NAME = "packageName";

        public static final java.lang.String TYPE_RESOURCE_ID = "typeResourceId";

        public static final java.lang.String DISPLAY_NAME = "displayName";

        public static final java.lang.String DIRECTORY_AUTHORITY = "authority";

        public static final java.lang.String ACCOUNT_TYPE = "accountType";

        public static final java.lang.String ACCOUNT_NAME = "accountName";

        public static final java.lang.String EXPORT_SUPPORT = "exportSupport";

        public static final int EXPORT_SUPPORT_NONE = 0;

        public static final int EXPORT_SUPPORT_SAME_ACCOUNT_ONLY = 1;

        public static final int EXPORT_SUPPORT_ANY_ACCOUNT = 2;

        public static final java.lang.String SHORTCUT_SUPPORT = "shortcutSupport";

        public static final int SHORTCUT_SUPPORT_NONE = 0;

        public static final int SHORTCUT_SUPPORT_DATA_ITEMS_ONLY = 1;

        public static final int SHORTCUT_SUPPORT_FULL = 2;

        public static final java.lang.String PHOTO_SUPPORT = "photoSupport";

        public static final int PHOTO_SUPPORT_NONE = 0;

        public static final int PHOTO_SUPPORT_THUMBNAIL_ONLY = 1;

        public static final int PHOTO_SUPPORT_FULL_SIZE_ONLY = 2;

        public static final int PHOTO_SUPPORT_FULL = 3;

        static {
            CONTENT_URI = null;
        }
    }

    public static final class SyncState implements android.provider.SyncStateContract.Columns {

        SyncState() {
            throw new RuntimeException("Stub!");
        }

        public static byte[] get(android.content.ContentProviderClient provider, android.accounts.Account account) throws android.os.RemoteException {
            throw new RuntimeException("Stub!");
        }

        public static android.util.Pair<android.net.Uri, byte[]> getWithUri(android.content.ContentProviderClient provider, android.accounts.Account account) throws android.os.RemoteException {
            throw new RuntimeException("Stub!");
        }

        public static void set(android.content.ContentProviderClient provider, android.accounts.Account account, byte[] data) throws android.os.RemoteException {
            throw new RuntimeException("Stub!");
        }

        public static android.content.ContentProviderOperation newSetOperation(android.accounts.Account account, byte[] data) {
            throw new RuntimeException("Stub!");
        }

        public static final java.lang.String CONTENT_DIRECTORY = "syncstate";

        public static final android.net.Uri CONTENT_URI;

        static {
            CONTENT_URI = taintedUri();
        }
    }

    public static final class ProfileSyncState implements android.provider.SyncStateContract.Columns {

        ProfileSyncState() {
            throw new RuntimeException("Stub!");
        }

        public static byte[] get(android.content.ContentProviderClient provider, android.accounts.Account account) throws android.os.RemoteException {
            throw new RuntimeException("Stub!");
        }

        public static android.util.Pair<android.net.Uri, byte[]> getWithUri(android.content.ContentProviderClient provider, android.accounts.Account account) throws android.os.RemoteException {
            throw new RuntimeException("Stub!");
        }

        public static void set(android.content.ContentProviderClient provider, android.accounts.Account account, byte[] data) throws android.os.RemoteException {
            throw new RuntimeException("Stub!");
        }

        public static android.content.ContentProviderOperation newSetOperation(android.accounts.Account account, byte[] data) {
            throw new RuntimeException("Stub!");
        }

        public static final java.lang.String CONTENT_DIRECTORY = "syncstate";

        public static final android.net.Uri CONTENT_URI;

        static {
            CONTENT_URI = taintedUri();
        }
    }

    protected static interface BaseSyncColumns {

        public static final java.lang.String SYNC1 = "sync1";

        public static final java.lang.String SYNC2 = "sync2";

        public static final java.lang.String SYNC3 = "sync3";

        public static final java.lang.String SYNC4 = "sync4";
    }

    protected static interface SyncColumns extends android.provider.ContactsContract.BaseSyncColumns {

        public static final java.lang.String ACCOUNT_NAME = "account_name";

        public static final java.lang.String ACCOUNT_TYPE = "account_type";

        public static final java.lang.String SOURCE_ID = "sourceid";

        public static final java.lang.String VERSION = "version";

        public static final java.lang.String DIRTY = "dirty";
    }

    protected static interface ContactOptionsColumns {

        public static final java.lang.String TIMES_CONTACTED = "times_contacted";

        public static final java.lang.String LAST_TIME_CONTACTED = "last_time_contacted";

        public static final java.lang.String STARRED = "starred";

        public static final java.lang.String CUSTOM_RINGTONE = "custom_ringtone";

        public static final java.lang.String SEND_TO_VOICEMAIL = "send_to_voicemail";
    }

    protected static interface ContactsColumns {

        public static final java.lang.String DISPLAY_NAME = "display_name";

        public static final java.lang.String PHOTO_ID = "photo_id";

        public static final java.lang.String PHOTO_FILE_ID = "photo_file_id";

        public static final java.lang.String PHOTO_URI = "photo_uri";

        public static final java.lang.String PHOTO_THUMBNAIL_URI = "photo_thumb_uri";

        public static final java.lang.String IN_VISIBLE_GROUP = "in_visible_group";

        public static final java.lang.String IS_USER_PROFILE = "is_user_profile";

        public static final java.lang.String HAS_PHONE_NUMBER = "has_phone_number";

        public static final java.lang.String LOOKUP_KEY = "lookup";
    }

    protected static interface ContactStatusColumns {

        public static final java.lang.String CONTACT_PRESENCE = "contact_presence";

        public static final java.lang.String CONTACT_CHAT_CAPABILITY = "contact_chat_capability";

        public static final java.lang.String CONTACT_STATUS = "contact_status";

        public static final java.lang.String CONTACT_STATUS_TIMESTAMP = "contact_status_ts";

        public static final java.lang.String CONTACT_STATUS_RES_PACKAGE = "contact_status_res_package";

        public static final java.lang.String CONTACT_STATUS_LABEL = "contact_status_label";

        public static final java.lang.String CONTACT_STATUS_ICON = "contact_status_icon";
    }

    public static interface FullNameStyle {

        public static final int UNDEFINED = 0;

        public static final int WESTERN = 1;

        public static final int CJK = 2;

        public static final int CHINESE = 3;

        public static final int JAPANESE = 4;

        public static final int KOREAN = 5;
    }

    public static interface PhoneticNameStyle {

        public static final int UNDEFINED = 0;

        public static final int PINYIN = 3;

        public static final int JAPANESE = 4;

        public static final int KOREAN = 5;
    }

    public static interface DisplayNameSources {

        public static final int UNDEFINED = 0;

        public static final int EMAIL = 10;

        public static final int PHONE = 20;

        public static final int ORGANIZATION = 30;

        public static final int NICKNAME = 35;

        public static final int STRUCTURED_NAME = 40;
    }

    protected static interface ContactNameColumns {

        public static final java.lang.String DISPLAY_NAME_SOURCE = "display_name_source";

        public static final java.lang.String DISPLAY_NAME_PRIMARY = "display_name";

        public static final java.lang.String DISPLAY_NAME_ALTERNATIVE = "display_name_alt";

        public static final java.lang.String PHONETIC_NAME_STYLE = "phonetic_name_style";

        public static final java.lang.String PHONETIC_NAME = "phonetic_name";

        public static final java.lang.String SORT_KEY_PRIMARY = "sort_key";

        public static final java.lang.String SORT_KEY_ALTERNATIVE = "sort_key_alt";
    }

    public static class Contacts implements android.provider.BaseColumns, android.provider.ContactsContract.ContactsColumns, android.provider.ContactsContract.ContactOptionsColumns, android.provider.ContactsContract.ContactNameColumns, android.provider.ContactsContract.ContactStatusColumns {

        public static final class Data implements android.provider.BaseColumns, android.provider.ContactsContract.DataColumns {

            Data() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_DIRECTORY = "data";
        }

        public static final class Entity implements android.provider.BaseColumns, android.provider.ContactsContract.ContactsColumns, android.provider.ContactsContract.ContactNameColumns, android.provider.ContactsContract.RawContactsColumns, android.provider.ContactsContract.BaseSyncColumns, android.provider.ContactsContract.SyncColumns, android.provider.ContactsContract.DataColumns, android.provider.ContactsContract.StatusColumns, android.provider.ContactsContract.ContactOptionsColumns, android.provider.ContactsContract.ContactStatusColumns {

            Entity() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_DIRECTORY = "entities";

            public static final java.lang.String RAW_CONTACT_ID = "raw_contact_id";

            public static final java.lang.String DATA_ID = "data_id";
        }

        public static final class StreamItems implements android.provider.ContactsContract.StreamItemsColumns {

            StreamItems() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_DIRECTORY = "stream_items";
        }

        public static final class AggregationSuggestions implements android.provider.BaseColumns, android.provider.ContactsContract.ContactsColumns, android.provider.ContactsContract.ContactOptionsColumns, android.provider.ContactsContract.ContactStatusColumns {

            AggregationSuggestions() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_DIRECTORY = "suggestions";
        }

        public static final class Photo implements android.provider.BaseColumns, android.provider.ContactsContract.DataColumnsWithJoins {

            Photo() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_DIRECTORY = "photo";

            public static final java.lang.String DISPLAY_PHOTO = "display_photo";

            public static final java.lang.String PHOTO_FILE_ID = "data14";

            public static final java.lang.String PHOTO = "data15";
        }

        Contacts() {
            throw new RuntimeException("Stub!");
        }

        public static android.net.Uri getLookupUri(android.content.ContentResolver resolver, android.net.Uri contactUri) {
            throw new RuntimeException("Stub!");
        }

        public static android.net.Uri getLookupUri(long contactId, java.lang.String lookupKey) {
            throw new RuntimeException("Stub!");
        }

        public static android.net.Uri lookupContact(android.content.ContentResolver resolver, android.net.Uri lookupUri) {
            throw new RuntimeException("Stub!");
        }

        @java.lang.Deprecated()
        public static void markAsContacted(android.content.ContentResolver resolver, long contactId) {
            throw new RuntimeException("Stub!");
        }

        public static java.io.InputStream openContactPhotoInputStream(android.content.ContentResolver cr, android.net.Uri contactUri, boolean preferHighres) {
            throw new RuntimeException("Stub!");
        }

        public static java.io.InputStream openContactPhotoInputStream(android.content.ContentResolver cr, android.net.Uri contactUri) {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final android.net.Uri CONTENT_LOOKUP_URI;

        public static final android.net.Uri CONTENT_VCARD_URI;

        public static final android.net.Uri CONTENT_FILTER_URI;

        public static final android.net.Uri CONTENT_STREQUENT_URI;

        public static final android.net.Uri CONTENT_STREQUENT_FILTER_URI;

        public static final android.net.Uri CONTENT_GROUP_URI;

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/contact";

        public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact";

        public static final java.lang.String CONTENT_VCARD_TYPE = "text/x-vcard";

        static {
            CONTENT_URI = taintedUri();
            CONTENT_LOOKUP_URI = taintedUri();
            CONTENT_VCARD_URI = taintedUri();
            CONTENT_FILTER_URI = taintedUri();
            CONTENT_STREQUENT_URI = taintedUri();
            CONTENT_STREQUENT_FILTER_URI = taintedUri();
            CONTENT_GROUP_URI = taintedUri();
        }
    }

    public static final class Profile implements android.provider.BaseColumns, android.provider.ContactsContract.ContactsColumns, android.provider.ContactsContract.ContactOptionsColumns, android.provider.ContactsContract.ContactNameColumns, android.provider.ContactsContract.ContactStatusColumns {

        Profile() {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final android.net.Uri CONTENT_VCARD_URI;

        public static final android.net.Uri CONTENT_RAW_CONTACTS_URI;

        public static final long MIN_ID = 9223372034707292160L;

        static {
            CONTENT_URI = taintedUri();
            CONTENT_VCARD_URI = taintedUri();
            CONTENT_RAW_CONTACTS_URI = taintedUri();
        }
    }

    protected static interface RawContactsColumns {

        public static final java.lang.String CONTACT_ID = "contact_id";

        public static final java.lang.String DATA_SET = "data_set";

        public static final java.lang.String AGGREGATION_MODE = "aggregation_mode";

        public static final java.lang.String DELETED = "deleted";

        public static final java.lang.String RAW_CONTACT_IS_READ_ONLY = "raw_contact_is_read_only";

        public static final java.lang.String RAW_CONTACT_IS_USER_PROFILE = "raw_contact_is_user_profile";
    }

    public static final class RawContacts implements android.provider.BaseColumns, android.provider.ContactsContract.RawContactsColumns, android.provider.ContactsContract.ContactOptionsColumns, android.provider.ContactsContract.ContactNameColumns, android.provider.ContactsContract.SyncColumns {

        public static final class Data implements android.provider.BaseColumns, android.provider.ContactsContract.DataColumns {

            Data() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_DIRECTORY = "data";
        }

        public static final class Entity implements android.provider.BaseColumns, android.provider.ContactsContract.DataColumns {

            Entity() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_DIRECTORY = "entity";

            public static final java.lang.String DATA_ID = "data_id";
        }

        public static final class StreamItems implements android.provider.BaseColumns, android.provider.ContactsContract.StreamItemsColumns {

            StreamItems() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_DIRECTORY = "stream_items";
        }

        public static final class DisplayPhoto {

            DisplayPhoto() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_DIRECTORY = "display_photo";
        }

        RawContacts() {
            throw new RuntimeException("Stub!");
        }

        public static android.net.Uri getContactLookupUri(android.content.ContentResolver resolver, android.net.Uri rawContactUri) {
            throw new RuntimeException("Stub!");
        }

        public static android.content.EntityIterator newEntityIterator(android.database.Cursor cursor) {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/raw_contact";

        public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/raw_contact";

        public static final int AGGREGATION_MODE_DEFAULT = 0;

        @java.lang.Deprecated()
        public static final int AGGREGATION_MODE_IMMEDIATE = 1;

        public static final int AGGREGATION_MODE_SUSPENDED = 2;

        public static final int AGGREGATION_MODE_DISABLED = 3;

        static {
            CONTENT_URI = taintedUri();
        }
    }

    protected static interface StatusColumns {

        public static final java.lang.String PRESENCE = "mode";

        @java.lang.Deprecated()
        public static final java.lang.String PRESENCE_STATUS = "mode";

        public static final int OFFLINE = 0;

        public static final int INVISIBLE = 1;

        public static final int AWAY = 2;

        public static final int IDLE = 3;

        public static final int DO_NOT_DISTURB = 4;

        public static final int AVAILABLE = 5;

        public static final java.lang.String STATUS = "status";

        @java.lang.Deprecated()
        public static final java.lang.String PRESENCE_CUSTOM_STATUS = "status";

        public static final java.lang.String STATUS_TIMESTAMP = "status_ts";

        public static final java.lang.String STATUS_RES_PACKAGE = "status_res_package";

        public static final java.lang.String STATUS_LABEL = "status_label";

        public static final java.lang.String STATUS_ICON = "status_icon";

        public static final java.lang.String CHAT_CAPABILITY = "chat_capability";

        public static final int CAPABILITY_HAS_VOICE = 1;

        public static final int CAPABILITY_HAS_VIDEO = 2;

        public static final int CAPABILITY_HAS_CAMERA = 4;
    }

    public static final class StreamItems implements android.provider.BaseColumns, android.provider.ContactsContract.StreamItemsColumns {

        public static final class StreamItemPhotos implements android.provider.BaseColumns, android.provider.ContactsContract.StreamItemPhotosColumns {

            StreamItemPhotos() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_DIRECTORY = "photo";

            public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/stream_item_photo";

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/stream_item_photo";
        }

        StreamItems() {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final android.net.Uri CONTENT_PHOTO_URI;

        public static final android.net.Uri CONTENT_LIMIT_URI;

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/stream_item";

        public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/stream_item";

        public static final java.lang.String MAX_ITEMS = "max_items";

        static {
            CONTENT_URI = taintedUri();
            CONTENT_PHOTO_URI = taintedUri();
            CONTENT_LIMIT_URI = taintedUri();
        }
    }

    protected static interface StreamItemsColumns {

        public static final java.lang.String CONTACT_ID = "contact_id";

        public static final java.lang.String CONTACT_LOOKUP_KEY = "contact_lookup";

        public static final java.lang.String RAW_CONTACT_ID = "raw_contact_id";

        public static final java.lang.String RES_PACKAGE = "res_package";

        public static final java.lang.String ACCOUNT_TYPE = "account_type";

        public static final java.lang.String ACCOUNT_NAME = "account_name";

        public static final java.lang.String DATA_SET = "data_set";

        public static final java.lang.String RAW_CONTACT_SOURCE_ID = "raw_contact_source_id";

        public static final java.lang.String RES_ICON = "icon";

        public static final java.lang.String RES_LABEL = "label";

        public static final java.lang.String TEXT = "text";

        public static final java.lang.String TIMESTAMP = "timestamp";

        public static final java.lang.String COMMENTS = "comments";

        public static final java.lang.String SYNC1 = "stream_item_sync1";

        public static final java.lang.String SYNC2 = "stream_item_sync2";

        public static final java.lang.String SYNC3 = "stream_item_sync3";

        public static final java.lang.String SYNC4 = "stream_item_sync4";
    }

    public static final class StreamItemPhotos implements android.provider.BaseColumns, android.provider.ContactsContract.StreamItemPhotosColumns {

        StreamItemPhotos() {
            throw new RuntimeException("Stub!");
        }

        public static final java.lang.String PHOTO = "photo";
    }

    protected static interface StreamItemPhotosColumns {

        public static final java.lang.String STREAM_ITEM_ID = "stream_item_id";

        public static final java.lang.String SORT_INDEX = "sort_index";

        public static final java.lang.String PHOTO_FILE_ID = "photo_file_id";

        public static final java.lang.String PHOTO_URI = "photo_uri";

        public static final java.lang.String SYNC1 = "stream_item_photo_sync1";

        public static final java.lang.String SYNC2 = "stream_item_photo_sync2";

        public static final java.lang.String SYNC3 = "stream_item_photo_sync3";

        public static final java.lang.String SYNC4 = "stream_item_photo_sync4";
    }

    protected static interface DataColumns {

        public static final java.lang.String MIMETYPE = "mimetype";

        public static final java.lang.String RAW_CONTACT_ID = "raw_contact_id";

        public static final java.lang.String IS_PRIMARY = "is_primary";

        public static final java.lang.String IS_SUPER_PRIMARY = "is_super_primary";

        public static final java.lang.String IS_READ_ONLY = "is_read_only";

        public static final java.lang.String DATA_VERSION = "data_version";

        public static final java.lang.String DATA1 = "data1";

        public static final java.lang.String DATA2 = "data2";

        public static final java.lang.String DATA3 = "data3";

        public static final java.lang.String DATA4 = "data4";

        public static final java.lang.String DATA5 = "data5";

        public static final java.lang.String DATA6 = "data6";

        public static final java.lang.String DATA7 = "data7";

        public static final java.lang.String DATA8 = "data8";

        public static final java.lang.String DATA9 = "data9";

        public static final java.lang.String DATA10 = "data10";

        public static final java.lang.String DATA11 = "data11";

        public static final java.lang.String DATA12 = "data12";

        public static final java.lang.String DATA13 = "data13";

        public static final java.lang.String DATA14 = "data14";

        public static final java.lang.String DATA15 = "data15";

        public static final java.lang.String SYNC1 = "data_sync1";

        public static final java.lang.String SYNC2 = "data_sync2";

        public static final java.lang.String SYNC3 = "data_sync3";

        public static final java.lang.String SYNC4 = "data_sync4";
    }

    protected static interface DataColumnsWithJoins extends android.provider.BaseColumns, android.provider.ContactsContract.DataColumns, android.provider.ContactsContract.StatusColumns, android.provider.ContactsContract.RawContactsColumns, android.provider.ContactsContract.ContactsColumns, android.provider.ContactsContract.ContactNameColumns, android.provider.ContactsContract.ContactOptionsColumns, android.provider.ContactsContract.ContactStatusColumns {
    }

    public static final class Data implements android.provider.ContactsContract.DataColumnsWithJoins {

        Data() {
            throw new RuntimeException("Stub!");
        }

        public static android.net.Uri getContactLookupUri(android.content.ContentResolver resolver, android.net.Uri dataUri) {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/data";

        static {
            CONTENT_URI = taintedUri();
        }
    }

    public static final class RawContactsEntity implements android.provider.BaseColumns, android.provider.ContactsContract.DataColumns, android.provider.ContactsContract.RawContactsColumns {

        RawContactsEntity() {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final android.net.Uri PROFILE_CONTENT_URI;

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/raw_contact_entity";

        public static final java.lang.String DATA_ID = "data_id";

        static {
            CONTENT_URI = taintedUri();
            PROFILE_CONTENT_URI = taintedUri();
        }
    }

    protected static interface PhoneLookupColumns {

        public static final java.lang.String NUMBER = "number";

        public static final java.lang.String TYPE = "type";

        public static final java.lang.String LABEL = "label";

        public static final java.lang.String NORMALIZED_NUMBER = "normalized_number";
    }

    public static final class PhoneLookup implements android.provider.BaseColumns, android.provider.ContactsContract.PhoneLookupColumns, android.provider.ContactsContract.ContactsColumns, android.provider.ContactsContract.ContactOptionsColumns {

        PhoneLookup() {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_FILTER_URI;

        static {
            CONTENT_FILTER_URI = taintedUri();
        }
    }

    protected static interface PresenceColumns {

        public static final java.lang.String DATA_ID = "presence_data_id";

        public static final java.lang.String PROTOCOL = "protocol";

        public static final java.lang.String CUSTOM_PROTOCOL = "custom_protocol";

        public static final java.lang.String IM_HANDLE = "im_handle";

        public static final java.lang.String IM_ACCOUNT = "im_account";
    }

    public static class StatusUpdates implements android.provider.ContactsContract.StatusColumns, android.provider.ContactsContract.PresenceColumns {

        StatusUpdates() {
            throw new RuntimeException("Stub!");
        }

        public static final int getPresenceIconResourceId(int status) {
            throw new RuntimeException("Stub!");
        }

        public static final int getPresencePrecedence(int status) {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final android.net.Uri PROFILE_CONTENT_URI;

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/status-update";

        public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/status-update";

        static {
            CONTENT_URI = taintedUri();
            PROFILE_CONTENT_URI = taintedUri();
        }
    }

    @java.lang.Deprecated()
    public static final class Presence extends android.provider.ContactsContract.StatusUpdates {

        public Presence() {
            throw new RuntimeException("Stub!");
        }
    }

    public static final class CommonDataKinds {

        public static interface BaseTypes {

            public static final int TYPE_CUSTOM = 0;
        }

        protected static interface CommonColumns extends android.provider.ContactsContract.CommonDataKinds.BaseTypes {

            public static final java.lang.String DATA = "data1";

            public static final java.lang.String TYPE = "data2";

            public static final java.lang.String LABEL = "data3";
        }

        public static final class StructuredName implements android.provider.ContactsContract.DataColumnsWithJoins {

            StructuredName() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/name";

            public static final java.lang.String DISPLAY_NAME = "data1";

            public static final java.lang.String GIVEN_NAME = "data2";

            public static final java.lang.String FAMILY_NAME = "data3";

            public static final java.lang.String PREFIX = "data4";

            public static final java.lang.String MIDDLE_NAME = "data5";

            public static final java.lang.String SUFFIX = "data6";

            public static final java.lang.String PHONETIC_GIVEN_NAME = "data7";

            public static final java.lang.String PHONETIC_MIDDLE_NAME = "data8";

            public static final java.lang.String PHONETIC_FAMILY_NAME = "data9";
        }

        public static final class Nickname implements android.provider.ContactsContract.DataColumnsWithJoins, android.provider.ContactsContract.CommonDataKinds.CommonColumns {

            Nickname() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/nickname";

            public static final int TYPE_DEFAULT = 1;

            public static final int TYPE_OTHER_NAME = 2;

            public static final int TYPE_MAIDEN_NAME = 3;

            @java.lang.Deprecated()
            public static final int TYPE_MAINDEN_NAME = 3;

            public static final int TYPE_SHORT_NAME = 4;

            public static final int TYPE_INITIALS = 5;

            public static final java.lang.String NAME = "data1";
        }

        public static final class Phone implements android.provider.ContactsContract.DataColumnsWithJoins, android.provider.ContactsContract.CommonDataKinds.CommonColumns {

            Phone() {
                throw new RuntimeException("Stub!");
            }

            public static final int getTypeLabelResource(int type) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.CharSequence getTypeLabel(android.content.res.Resources res, int type, java.lang.CharSequence label) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phone_v2";

            public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/phone_v2";

            public static final android.net.Uri CONTENT_URI;

            public static final android.net.Uri CONTENT_FILTER_URI;

            public static final java.lang.String SEARCH_DISPLAY_NAME_KEY = "search_display_name";

            public static final java.lang.String SEARCH_PHONE_NUMBER_KEY = "search_phone_number";

            public static final int TYPE_HOME = 1;

            public static final int TYPE_MOBILE = 2;

            public static final int TYPE_WORK = 3;

            public static final int TYPE_FAX_WORK = 4;

            public static final int TYPE_FAX_HOME = 5;

            public static final int TYPE_PAGER = 6;

            public static final int TYPE_OTHER = 7;

            public static final int TYPE_CALLBACK = 8;

            public static final int TYPE_CAR = 9;

            public static final int TYPE_COMPANY_MAIN = 10;

            public static final int TYPE_ISDN = 11;

            public static final int TYPE_MAIN = 12;

            public static final int TYPE_OTHER_FAX = 13;

            public static final int TYPE_RADIO = 14;

            public static final int TYPE_TELEX = 15;

            public static final int TYPE_TTY_TDD = 16;

            public static final int TYPE_WORK_MOBILE = 17;

            public static final int TYPE_WORK_PAGER = 18;

            public static final int TYPE_ASSISTANT = 19;

            public static final int TYPE_MMS = 20;

            public static final java.lang.String NUMBER = "data1";

            public static final java.lang.String NORMALIZED_NUMBER = "data4";

            static {
                CONTENT_URI = taintedUri();
                CONTENT_FILTER_URI = taintedUri();
            }
        }

        public static final class Email implements android.provider.ContactsContract.DataColumnsWithJoins, android.provider.ContactsContract.CommonDataKinds.CommonColumns {

            Email() {
                throw new RuntimeException("Stub!");
            }

            public static final int getTypeLabelResource(int type) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.CharSequence getTypeLabel(android.content.res.Resources res, int type, java.lang.CharSequence label) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/email_v2";

            public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/email_v2";

            public static final android.net.Uri CONTENT_URI;

            public static final android.net.Uri CONTENT_LOOKUP_URI;

            public static final android.net.Uri CONTENT_FILTER_URI;

            public static final java.lang.String ADDRESS = "data1";

            public static final int TYPE_HOME = 1;

            public static final int TYPE_WORK = 2;

            public static final int TYPE_OTHER = 3;

            public static final int TYPE_MOBILE = 4;

            public static final java.lang.String DISPLAY_NAME = "data4";

            static {
                CONTENT_URI = taintedUri();
                CONTENT_FILTER_URI = taintedUri();
                CONTENT_LOOKUP_URI = taintedUri();
            }
        }

        public static final class StructuredPostal implements android.provider.ContactsContract.DataColumnsWithJoins, android.provider.ContactsContract.CommonDataKinds.CommonColumns {

            StructuredPostal() {
                throw new RuntimeException("Stub!");
            }

            public static final int getTypeLabelResource(int type) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.CharSequence getTypeLabel(android.content.res.Resources res, int type, java.lang.CharSequence label) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/postal-address_v2";

            public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/postal-address_v2";

            public static final android.net.Uri CONTENT_URI;

            public static final int TYPE_HOME = 1;

            public static final int TYPE_WORK = 2;

            public static final int TYPE_OTHER = 3;

            public static final java.lang.String FORMATTED_ADDRESS = "data1";

            public static final java.lang.String STREET = "data4";

            public static final java.lang.String POBOX = "data5";

            public static final java.lang.String NEIGHBORHOOD = "data6";

            public static final java.lang.String CITY = "data7";

            public static final java.lang.String REGION = "data8";

            public static final java.lang.String POSTCODE = "data9";

            public static final java.lang.String COUNTRY = "data10";

            static {
                CONTENT_URI = taintedUri();
            }
        }

        public static final class Im implements android.provider.ContactsContract.DataColumnsWithJoins, android.provider.ContactsContract.CommonDataKinds.CommonColumns {

            Im() {
                throw new RuntimeException("Stub!");
            }

            public static final int getTypeLabelResource(int type) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.CharSequence getTypeLabel(android.content.res.Resources res, int type, java.lang.CharSequence label) {
                throw new RuntimeException("Stub!");
            }

            public static final int getProtocolLabelResource(int type) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.CharSequence getProtocolLabel(android.content.res.Resources res, int type, java.lang.CharSequence label) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/im";

            public static final int TYPE_HOME = 1;

            public static final int TYPE_WORK = 2;

            public static final int TYPE_OTHER = 3;

            public static final java.lang.String PROTOCOL = "data5";

            public static final java.lang.String CUSTOM_PROTOCOL = "data6";

            public static final int PROTOCOL_CUSTOM = -1;

            public static final int PROTOCOL_AIM = 0;

            public static final int PROTOCOL_MSN = 1;

            public static final int PROTOCOL_YAHOO = 2;

            public static final int PROTOCOL_SKYPE = 3;

            public static final int PROTOCOL_QQ = 4;

            public static final int PROTOCOL_GOOGLE_TALK = 5;

            public static final int PROTOCOL_ICQ = 6;

            public static final int PROTOCOL_JABBER = 7;

            public static final int PROTOCOL_NETMEETING = 8;
        }

        public static final class Organization implements android.provider.ContactsContract.DataColumnsWithJoins, android.provider.ContactsContract.CommonDataKinds.CommonColumns {

            Organization() {
                throw new RuntimeException("Stub!");
            }

            public static final int getTypeLabelResource(int type) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.CharSequence getTypeLabel(android.content.res.Resources res, int type, java.lang.CharSequence label) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/organization";

            public static final int TYPE_WORK = 1;

            public static final int TYPE_OTHER = 2;

            public static final java.lang.String COMPANY = "data1";

            public static final java.lang.String TITLE = "data4";

            public static final java.lang.String DEPARTMENT = "data5";

            public static final java.lang.String JOB_DESCRIPTION = "data6";

            public static final java.lang.String SYMBOL = "data7";

            public static final java.lang.String PHONETIC_NAME = "data8";

            public static final java.lang.String OFFICE_LOCATION = "data9";
        }

        public static final class Relation implements android.provider.ContactsContract.DataColumnsWithJoins, android.provider.ContactsContract.CommonDataKinds.CommonColumns {

            Relation() {
                throw new RuntimeException("Stub!");
            }

            public static final int getTypeLabelResource(int type) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.CharSequence getTypeLabel(android.content.res.Resources res, int type, java.lang.CharSequence label) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/relation";

            public static final int TYPE_ASSISTANT = 1;

            public static final int TYPE_BROTHER = 2;

            public static final int TYPE_CHILD = 3;

            public static final int TYPE_DOMESTIC_PARTNER = 4;

            public static final int TYPE_FATHER = 5;

            public static final int TYPE_FRIEND = 6;

            public static final int TYPE_MANAGER = 7;

            public static final int TYPE_MOTHER = 8;

            public static final int TYPE_PARENT = 9;

            public static final int TYPE_PARTNER = 10;

            public static final int TYPE_REFERRED_BY = 11;

            public static final int TYPE_RELATIVE = 12;

            public static final int TYPE_SISTER = 13;

            public static final int TYPE_SPOUSE = 14;

            public static final java.lang.String NAME = "data1";
        }

        public static final class Event implements android.provider.ContactsContract.DataColumnsWithJoins, android.provider.ContactsContract.CommonDataKinds.CommonColumns {

            Event() {
                throw new RuntimeException("Stub!");
            }

            public static int getTypeResource(java.lang.Integer type) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_event";

            public static final int TYPE_ANNIVERSARY = 1;

            public static final int TYPE_OTHER = 2;

            public static final int TYPE_BIRTHDAY = 3;

            public static final java.lang.String START_DATE = "data1";
        }

        public static final class Photo implements android.provider.ContactsContract.DataColumnsWithJoins {

            Photo() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/photo";

            public static final java.lang.String PHOTO_FILE_ID = "data14";

            public static final java.lang.String PHOTO = "data15";
        }

        public static final class Note implements android.provider.ContactsContract.DataColumnsWithJoins {

            Note() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/note";

            public static final java.lang.String NOTE = "data1";
        }

        public static final class GroupMembership implements android.provider.ContactsContract.DataColumnsWithJoins {

            GroupMembership() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/group_membership";

            public static final java.lang.String GROUP_ROW_ID = "data1";

            public static final java.lang.String GROUP_SOURCE_ID = "group_sourceid";
        }

        public static final class Website implements android.provider.ContactsContract.DataColumnsWithJoins, android.provider.ContactsContract.CommonDataKinds.CommonColumns {

            Website() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/website";

            public static final int TYPE_HOMEPAGE = 1;

            public static final int TYPE_BLOG = 2;

            public static final int TYPE_PROFILE = 3;

            public static final int TYPE_HOME = 4;

            public static final int TYPE_WORK = 5;

            public static final int TYPE_FTP = 6;

            public static final int TYPE_OTHER = 7;

            public static final java.lang.String URL = "data1";
        }

        public static final class SipAddress implements android.provider.ContactsContract.DataColumnsWithJoins, android.provider.ContactsContract.CommonDataKinds.CommonColumns {

            SipAddress() {
                throw new RuntimeException("Stub!");
            }

            public static final int getTypeLabelResource(int type) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.CharSequence getTypeLabel(android.content.res.Resources res, int type, java.lang.CharSequence label) {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/sip_address";

            public static final int TYPE_HOME = 1;

            public static final int TYPE_WORK = 2;

            public static final int TYPE_OTHER = 3;

            public static final java.lang.String SIP_ADDRESS = "data1";
        }

        public static final class Identity implements android.provider.ContactsContract.DataColumnsWithJoins {

            Identity() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/identity";

            public static final java.lang.String IDENTITY = "data1";

            public static final java.lang.String NAMESPACE = "data2";
        }

        CommonDataKinds() {
            throw new RuntimeException("Stub!");
        }
    }

    protected static interface GroupsColumns {

        public static final java.lang.String DATA_SET = "data_set";

        public static final java.lang.String TITLE = "title";

        public static final java.lang.String NOTES = "notes";

        public static final java.lang.String SYSTEM_ID = "system_id";

        public static final java.lang.String SUMMARY_COUNT = "summ_count";

        public static final java.lang.String SUMMARY_WITH_PHONES = "summ_phones";

        public static final java.lang.String GROUP_VISIBLE = "group_visible";

        public static final java.lang.String DELETED = "deleted";

        public static final java.lang.String SHOULD_SYNC = "should_sync";

        public static final java.lang.String AUTO_ADD = "auto_add";

        public static final java.lang.String FAVORITES = "favorites";

        public static final java.lang.String GROUP_IS_READ_ONLY = "group_is_read_only";
    }

    public static final class Groups implements android.provider.BaseColumns, android.provider.ContactsContract.GroupsColumns, android.provider.ContactsContract.SyncColumns {

        Groups() {
            throw new RuntimeException("Stub!");
        }

        public static android.content.EntityIterator newEntityIterator(android.database.Cursor cursor) {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final android.net.Uri CONTENT_SUMMARY_URI;

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/group";

        public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/group";

        static {
            CONTENT_URI = taintedUri();
            CONTENT_SUMMARY_URI = taintedUri();
        }
    }

    public static final class AggregationExceptions implements android.provider.BaseColumns {

        AggregationExceptions() {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/aggregation_exception";

        public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/aggregation_exception";

        public static final java.lang.String TYPE = "type";

        public static final int TYPE_AUTOMATIC = 0;

        public static final int TYPE_KEEP_TOGETHER = 1;

        public static final int TYPE_KEEP_SEPARATE = 2;

        public static final java.lang.String RAW_CONTACT_ID1 = "raw_contact_id1";

        public static final java.lang.String RAW_CONTACT_ID2 = "raw_contact_id2";

        static {
            CONTENT_URI = taintedUri();
        }
    }

    protected static interface SettingsColumns {

        public static final java.lang.String ACCOUNT_NAME = "account_name";

        public static final java.lang.String ACCOUNT_TYPE = "account_type";

        public static final java.lang.String DATA_SET = "data_set";

        public static final java.lang.String SHOULD_SYNC = "should_sync";

        public static final java.lang.String UNGROUPED_VISIBLE = "ungrouped_visible";

        public static final java.lang.String ANY_UNSYNCED = "any_unsynced";

        public static final java.lang.String UNGROUPED_COUNT = "summ_count";

        public static final java.lang.String UNGROUPED_WITH_PHONES = "summ_phones";
    }

    public static final class Settings implements android.provider.ContactsContract.SettingsColumns {

        Settings() {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/setting";

        public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/setting";

        static {
            CONTENT_URI = taintedUri();
        }
    }

    public static final class DataUsageFeedback {

        public DataUsageFeedback() {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri FEEDBACK_URI;

        public static final android.net.Uri DELETE_USAGE_URI;

        public static final java.lang.String USAGE_TYPE = "type";

        public static final java.lang.String USAGE_TYPE_CALL = "call";

        public static final java.lang.String USAGE_TYPE_LONG_TEXT = "long_text";

        public static final java.lang.String USAGE_TYPE_SHORT_TEXT = "short_text";

        static {
            FEEDBACK_URI = taintedUri();
        }
    }

    public static final class QuickContact {

        public QuickContact() {
            throw new RuntimeException("Stub!");
        }

        public static void showQuickContact(android.content.Context context, android.view.View target, android.net.Uri lookupUri, int mode, java.lang.String[] excludeMimes) {
            throw new RuntimeException("Stub!");
        }

        public static void showQuickContact(android.content.Context context, android.graphics.Rect target, android.net.Uri lookupUri, int mode, java.lang.String[] excludeMimes) {
            throw new RuntimeException("Stub!");
        }

        public static final int MODE_SMALL = 1;

        public static final int MODE_MEDIUM = 2;

        public static final int MODE_LARGE = 3;
    }

    public static final class DisplayPhoto {

        DisplayPhoto() {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final android.net.Uri CONTENT_MAX_DIMENSIONS_URI;

        public static final java.lang.String DISPLAY_MAX_DIM = "display_max_dim";

        public static final java.lang.String THUMBNAIL_MAX_DIM = "thumbnail_max_dim";

        static {
            CONTENT_URI = taintedUri();
            CONTENT_MAX_DIMENSIONS_URI = taintedUri();
        }
    }

    public static final class Intents {

        public static final class Insert {

            public Insert() {
                throw new RuntimeException("Stub!");
            }

            public static final java.lang.String ACTION = "android.intent.action.INSERT";

            public static final java.lang.String FULL_MODE = "full_mode";

            public static final java.lang.String NAME = "name";

            public static final java.lang.String PHONETIC_NAME = "phonetic_name";

            public static final java.lang.String COMPANY = "company";

            public static final java.lang.String JOB_TITLE = "job_title";

            public static final java.lang.String NOTES = "notes";

            public static final java.lang.String PHONE = "phone";

            public static final java.lang.String PHONE_TYPE = "phone_type";

            public static final java.lang.String PHONE_ISPRIMARY = "phone_isprimary";

            public static final java.lang.String SECONDARY_PHONE = "secondary_phone";

            public static final java.lang.String SECONDARY_PHONE_TYPE = "secondary_phone_type";

            public static final java.lang.String TERTIARY_PHONE = "tertiary_phone";

            public static final java.lang.String TERTIARY_PHONE_TYPE = "tertiary_phone_type";

            public static final java.lang.String EMAIL = "email";

            public static final java.lang.String EMAIL_TYPE = "email_type";

            public static final java.lang.String EMAIL_ISPRIMARY = "email_isprimary";

            public static final java.lang.String SECONDARY_EMAIL = "secondary_email";

            public static final java.lang.String SECONDARY_EMAIL_TYPE = "secondary_email_type";

            public static final java.lang.String TERTIARY_EMAIL = "tertiary_email";

            public static final java.lang.String TERTIARY_EMAIL_TYPE = "tertiary_email_type";

            public static final java.lang.String POSTAL = "postal";

            public static final java.lang.String POSTAL_TYPE = "postal_type";

            public static final java.lang.String POSTAL_ISPRIMARY = "postal_isprimary";

            public static final java.lang.String IM_HANDLE = "im_handle";

            public static final java.lang.String IM_PROTOCOL = "im_protocol";

            public static final java.lang.String IM_ISPRIMARY = "im_isprimary";

            public static final java.lang.String DATA = "data";
        }

        public Intents() {
            throw new RuntimeException("Stub!");
        }

        public static final java.lang.String SEARCH_SUGGESTION_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_CLICKED";

        public static final java.lang.String SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED";

        public static final java.lang.String SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED";

        public static final java.lang.String ATTACH_IMAGE = "com.android.contacts.action.ATTACH_IMAGE";

        public static final java.lang.String INVITE_CONTACT = "com.android.contacts.action.INVITE_CONTACT";

        public static final java.lang.String SHOW_OR_CREATE_CONTACT = "com.android.contacts.action.SHOW_OR_CREATE_CONTACT";

        public static final java.lang.String EXTRA_FORCE_CREATE = "com.android.contacts.action.FORCE_CREATE";

        public static final java.lang.String EXTRA_CREATE_DESCRIPTION = "com.android.contacts.action.CREATE_DESCRIPTION";
    }

    public ContactsContract() {
        throw new RuntimeException("Stub!");
    }

    public static boolean isProfileId(long id) {
        throw new RuntimeException("Stub!");
    }

    public static final java.lang.String AUTHORITY = "com.android.contacts";

    public static final android.net.Uri AUTHORITY_URI;

    public static final java.lang.String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";

    public static final java.lang.String DIRECTORY_PARAM_KEY = "directory";

    public static final java.lang.String LIMIT_PARAM_KEY = "limit";

    public static final java.lang.String PRIMARY_ACCOUNT_NAME = "name_for_primary_account";

    public static final java.lang.String PRIMARY_ACCOUNT_TYPE = "type_for_primary_account";

    static {
        AUTHORITY_URI = taintedUri();
    }

    @STAMP(flows = { @Flow(from = "$CONTACTS", to = "@return") })
    private static android.net.Uri taintedUri() {
        return new android.net.StampUri("");
    }
}

