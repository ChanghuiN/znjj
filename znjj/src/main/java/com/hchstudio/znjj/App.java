package com.hchstudio.znjj;

import android.app.Application;
import android.util.Log;

import com.hchstudio.znjj.EMB.NodePresent;

import java.util.ArrayList;

/**
 * Created by hch on 16-5-15.
 */
public class App extends Application {
    private static final String TAG = "App";

    public static ArrayList<NodePresent> nodePresents;

    @Override
    public void onCreate() {
        nodePresents = new ArrayList<NodePresent>();
        super.onCreate();
    }
}
