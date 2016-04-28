package com.me202.jaredostdiek.smartbikepart1;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by jaredostdiek on 4/25/16.
 */
public class MyApp extends Application{
    @Override
    public void onCreate(){
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}