package edu.stanford.stamp.harness;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import android.app.Activity;
import android.os.Bundle;

public class ApplicationDriver {

    static List<Callback> callbacks = new ArrayList();

    private static ApplicationDriver instance = new ApplicationDriver();

    private ApplicationDriver() {
    }

    public static ApplicationDriver getInstance() {
        return instance;
    }

    public static void registerCallback(Callback cb) {
        callbacks.add(cb);
    }

    public static void callCallbacks() {
        callbacks.get(0).run();
    }
}

