package android.app.backup;

import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;
import java.io.IOException;

public abstract class BackupAgent extends android.content.ContextWrapper {

    public void onCreate() {
        throw new RuntimeException("Stub!");
    }

    public void onDestroy() {
        throw new RuntimeException("Stub!");
    }

    public abstract void onBackup(android.os.ParcelFileDescriptor oldState, android.app.backup.BackupDataOutput data, android.os.ParcelFileDescriptor newState) throws java.io.IOException;

    public abstract void onRestore(android.app.backup.BackupDataInput data, int appVersionCode, android.os.ParcelFileDescriptor newState) throws java.io.IOException;

    public void onFullBackup(android.app.backup.FullBackupDataOutput data) throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public final void fullBackupFile(java.io.File file, android.app.backup.FullBackupDataOutput output) {
        throw new RuntimeException("Stub!");
    }

    public void onRestoreFile(android.os.ParcelFileDescriptor data, long size, java.io.File destination, int type, long mode, long mtime) throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public static final int TYPE_FILE = 1;

    public static final int TYPE_DIRECTORY = 2;

    public BackupAgent() {
        super((android.content.Context) null);
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                BackupAgent.this.onCreate();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                BackupAgent.this.onDestroy();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                try {
                    BackupAgent.this.onBackup(null, null, null);
                } catch (IOException e) {
                }
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                try {
                    BackupAgent.this.onRestore(null, 0, null);
                } catch (IOException e) {
                }
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                try {
                    BackupAgent.this.onFullBackup(null);
                } catch (IOException e) {
                }
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                try {
                    BackupAgent.this.onRestoreFile(null, 0L, null, 0, 0L, 0L);
                } catch (IOException e) {
                }
            }
        });
    }
}

