package com.android.systemui.statusbar.oswin;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class PackInfo { 
	// app name
	private String mAppName = null;
	// package name
	private String mPackageName = null;
	// main activity
	private String mActivityName = null;
	// app icon
	private Drawable mIcon = null;
	// version name
	private String mVersionName = null;
	// version code
	private int mVersionCode = 0;
	
	public int mAppType = 0;
	
	private Intent mIntent;
	
	public int createIntent(String packageName, String activityName){
		if((null==packageName) || (null==activityName)){
			mIntent = null;
			return -1;
		}

		mIntent = new Intent(Intent.ACTION_MAIN);
		mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		ComponentName componentName = new ComponentName(packageName, activityName);
		mIntent.setComponent(componentName);
		return 0;
	}
	
	public void setAppName(String name){
		mAppName = name;
	}
	
	public String getAppName(){
		return mAppName;
	}
	
	public void setPackageName(String packageName){
		mPackageName = packageName;
	}
	
	public String getPackageName(){
		return mPackageName;
	}
	
	public void setActivityName(String name){
		mActivityName = name;
	}
	
	public String getActivityName(){
		return mActivityName;
	}
	
	public void setIcon(Drawable drawable){
		mIcon = drawable;
	}

	public Drawable getIcon(){
		return mIcon;
	}
	
	public void setVersionName(String name){
		mVersionName = name;
	}
	
	public String getVersionName(){
		return mVersionName;
	}

	public void setVersionCode(int code){
		mVersionCode = code;
	}

	public int getVersionCode(){
		return mVersionCode;
	}
	
	public String toString(){
		return "name=" + mAppName + "; acvitity=" + mActivityName;
	}
} 