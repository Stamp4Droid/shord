package com.google.android.maps;

import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

public abstract class MapActivity extends android.app.Activity {

    protected void onCreate(android.os.Bundle param1) {
        throw new RuntimeException("Stub!");
    }

    public void onNewIntent(android.content.Intent param1) {
        throw new RuntimeException("Stub!");
    }

    protected void onResume() {
        throw new RuntimeException("Stub!");
    }

    protected void onPause() {
        throw new RuntimeException("Stub!");
    }

    protected void onDestroy() {
        throw new RuntimeException("Stub!");
    }

    protected abstract boolean isRouteDisplayed();

    protected boolean isLocationDisplayed() {
        throw new RuntimeException("Stub!");
    }

    protected int onGetMapDataSource() {
        throw new RuntimeException("Stub!");
    }

    public MapActivity() {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                MapActivity.this.onGetMapDataSource();
            }
        });
    }
}

