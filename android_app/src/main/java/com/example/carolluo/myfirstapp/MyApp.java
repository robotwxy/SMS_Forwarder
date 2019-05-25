package com.example.carolluo.myfirstapp;

import android.app.Application;

public class MyApp extends Application {
    public String lastSms;


    public String getLastSms() {
        return lastSms;
    }

    public void setlastSms(String lastSms) {
        this.lastSms = lastSms;

    }
}
