package com.android.systemui.statusbar.oswin.data;

import java.io.ByteArrayOutputStream;

import com.android.systemui.statusbar.oswin.PackInfo;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class AppItemEntity { 
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
	// version code
	private int mAppType = 0;
	
	private Intent mIntent;
	public static final String KEY_ID = "_id"; 
	public static String KEY_APP_TYPE = "app_type";
	public static String KEY_APP_NAME = "app_name";
	public static String KEY_PKG_NAME = "pkg_name"; //PackageName
	public static String KEY_ACT_NAME = "act_name"; //ActivityName
	public static String KEY_ICON= "app_icon"; //ActivityName
	public static final String TABLE_APP_ITEM = "tb_app_item";
	
	public static final String APP_ITEM_TABLE_CREATE = "create table "  
            + TABLE_APP_ITEM + " ("
			+ KEY_ID  + " integer primary key autoincrement, " 
            + KEY_APP_TYPE + " TEXT, "  
            + KEY_APP_NAME + " TEXT, " 
            + KEY_PKG_NAME + " TEXT, "  
            + KEY_ACT_NAME + " TEXT, "  
            + KEY_ICON + " BLOB" + ");"; 
	
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
	
	public void setType(int type){
		mAppType = type;
	}

	public int getType(){
		return mAppType;
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
		return "name=" + mPackageName + "; mActivityName=" + mActivityName;
	}
	
	public ContentValues getConentValues(){
		ContentValues content = new ContentValues();
		content.put(KEY_APP_TYPE, mAppType);
		content.put(KEY_APP_NAME, mAppName);
		content.put(KEY_PKG_NAME, mPackageName);
		content.put(KEY_ACT_NAME, mActivityName);
		if(mIcon != null){
			Bitmap bmp = (((BitmapDrawable)mIcon).getBitmap());
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
			content.put(KEY_ICON, stream.toByteArray());
		}
		return content;
	}
	
	public static PackInfo makePackInfoFromCursor(Cursor cursor){
		PackInfo itemInfo = null;

		if((null!=cursor)&&(cursor.getCount()!=0)){
			itemInfo = new PackInfo();
			
			String appType = cursor.getString(cursor.getColumnIndex(KEY_APP_TYPE));
			itemInfo.mAppType = Integer.valueOf(appType);
			itemInfo.setAppName(cursor.getString(cursor.getColumnIndex(KEY_APP_NAME)));
			itemInfo.setPackageName(cursor.getString(cursor.getColumnIndex(KEY_PKG_NAME)));
			itemInfo.setActivityName(cursor.getString(cursor.getColumnIndex(KEY_ACT_NAME)));
	
			byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_ICON));
			if(null != blob){
				Bitmap bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);
				itemInfo.setIcon(new BitmapDrawable(bmp));
			}
			//itemInfo.setIcon(null);
		}
		
		return itemInfo;
	}
} 