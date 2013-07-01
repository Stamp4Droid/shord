package com.google.android.maps;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;
import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

public abstract class ItemizedOverlay<Item extends OverlayItem> extends com.google.android.maps.Overlay implements com.google.android.maps.Overlay.Snappable {

    protected abstract Item createItem(int param1);

    public abstract int size();

    protected int getIndexToDraw(int param1) {
        throw new RuntimeException("Stub!");
    }

    protected void populate() {
        throw new RuntimeException("Stub!");
    }

    protected void setLastFocusedIndex(int param1) {
        throw new RuntimeException("Stub!");
    }

    public void setFocus(Item param1) {
        throw new RuntimeException("Stub!");
    }

    public int getLastFocusedIndex() {
        throw new RuntimeException("Stub!");
    }

    public boolean onTap(com.google.android.maps.GeoPoint param1, com.google.android.maps.MapView param2) {
        throw new RuntimeException("Stub!");
    }

    public boolean onSnapToItem(int param1, int param2, android.graphics.Point param3, com.google.android.maps.MapView param4) {
        throw new RuntimeException("Stub!");
    }

    public boolean onTrackballEvent(android.view.MotionEvent param1, com.google.android.maps.MapView param2) {
        throw new RuntimeException("Stub!");
    }

    public boolean onKeyUp(int param1, android.view.KeyEvent param2, com.google.android.maps.MapView param3) {
        throw new RuntimeException("Stub!");
    }

    public boolean onTouchEvent(android.view.MotionEvent param1, com.google.android.maps.MapView param2) {
        throw new RuntimeException("Stub!");
    }

    protected boolean hitTest(Item param1, android.graphics.drawable.Drawable param2, int param3, int param4) {
        throw new RuntimeException("Stub!");
    }

    public void setDrawFocusedItem(boolean param1) {
        throw new RuntimeException("Stub!");
    }

    protected boolean onTap(int param1) {
        throw new RuntimeException("Stub!");
    }

    public interface OnFocusChangeListener {

        public abstract void onFocusChanged(com.google.android.maps.ItemizedOverlay param1, com.google.android.maps.OverlayItem param2);
    }

    @STAMP(flows = { @Flow(from = "param1", to = "this") })
    public ItemizedOverlay(android.graphics.drawable.Drawable param1) {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                ItemizedOverlay.this.onTap(null, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                ItemizedOverlay.this.onSnapToItem(0, 0, null, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                ItemizedOverlay.this.onTrackballEvent(null, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                ItemizedOverlay.this.onKeyUp(0, null, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                ItemizedOverlay.this.onTouchEvent(null, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                ItemizedOverlay.this.onTap(0);
            }
        });
    }

    @STAMP(flows = { @Flow(from = "param1", to = "@return") })
    protected static android.graphics.drawable.Drawable boundCenterBottom(android.graphics.drawable.Drawable param1) {
        return param1;
    }

    @STAMP(flows = { @Flow(from = "param1", to = "@return") })
    protected static android.graphics.drawable.Drawable boundCenter(android.graphics.drawable.Drawable param1) {
        return param1;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public com.google.android.maps.GeoPoint getCenter() {
        return new GeoPoint(0, 0);
    }

    @STAMP(flows = { @Flow(from = "this", to = "param1") })
    public void draw(android.graphics.Canvas param1, com.google.android.maps.MapView param2, boolean param3) {
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int getLatSpanE6() {
        return 13000000;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int getLonSpanE6() {
        return 13000000;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public com.google.android.maps.OverlayItem getFocus() {
        return null;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public Item getItem(int param1) {
        return null;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public Item nextFocus(boolean param1) {
        return null;
    }

    public void setOnFocusChangeListener(final com.google.android.maps.ItemizedOverlay.OnFocusChangeListener listener) {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                listener.onFocusChanged(ItemizedOverlay.this, null);
            }
        });
    }
}

