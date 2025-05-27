package com.example.newEcom.utils;// Tạo 1 class MyApp extends Application

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Map config = new HashMap();
        config.put("cloud_name", "dzi4yzwjk");
        config.put("api_key", "571788156588531");
        config.put("api_secret", "xIxbTMdaX0Pc4RzE3kAgC2kDjMk");
        config.put("secure", true);
        MediaManager.init(this, config);
    }
}
