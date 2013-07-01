package android.app;

import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

public abstract class IntentService extends android.app.Service {

    public void setIntentRedelivery(boolean enabled) {
        throw new RuntimeException("Stub!");
    }

    public void onCreate() {
        throw new RuntimeException("Stub!");
    }

    public void onStart(android.content.Intent intent, int startId) {
        throw new RuntimeException("Stub!");
    }

    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        throw new RuntimeException("Stub!");
    }

    public void onDestroy() {
        throw new RuntimeException("Stub!");
    }

    public android.os.IBinder onBind(android.content.Intent intent) {
        throw new RuntimeException("Stub!");
    }

    protected abstract void onHandleIntent(android.content.Intent intent);

    public IntentService(java.lang.String name) {
        super();
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                IntentService.this.onHandleIntent(new android.content.Intent());
            }
        });
    }
}

