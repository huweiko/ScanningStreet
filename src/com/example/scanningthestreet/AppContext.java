package com.example.scanningthestreet;

import android.app.Application;
import android.content.Intent;

import com.baidu.mapapi.SDKInitializer;

public class AppContext extends Application {
	public Intent GuiJiServiceIntent;
	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(this);
	}

}