package com.android.systemui.statusbar.oswin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class RecentAppManager {
	protected static final String TAG =  "WinStatusBar";
	private Context mContext;
	public ArrayList<HashMap<String, Object>> mAppList;

	public RecentAppManager(Context context){
		mContext = context;
		mAppList = new ArrayList<HashMap<String, Object>>();
		mAppList.clear();
	}
	
	public void sleepMoment(int time){
		try {
			 Thread.sleep(time);
		} catch (Exception ex) {
		}
	}
	
	public boolean updateRecentApp(String packName, boolean added){
		if(filterOutPackage(packName)){
			return false;
		}
		
		HashMap<String, Object> item = null;

		for(int i = 0; i < mAppList.size(); i++){
			item = mAppList.get(i);
			String packageName=(String) item.get("packagename");
			if(packageName.equals(packName)){
				if(added==false){//remove existed package
				    mAppList.remove(i);
				    Log.d(TAG, "updateRecentApp --> APP removed..packName="+packName);
				    return true;
				}
				Log.d(TAG, "updateRecentApp --> APP existed..packName="+packName);
				return false;
			}
		}
		
        if(added){
            item = new HashMap<String, Object>(); 
            Drawable icon = getTopAppIcon();
            String topName = getTopAppPackName();
            if((null!=icon)&&(null!=packName)){
                item.put(       "icon", icon);
                item.put("packagename", topName);//
                mAppList.add(item);
                Log.d(TAG, "updateRecentApp --> New App Opened..packName="+packName);
                return true;
            }
		}
		return false;
	}
	
	public int moveRecentToFront(String packName){
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = mContext.getApplicationContext().getPackageManager();
        List<ActivityManager.RecentTaskInfo> appTask = am.getRecentTasks(30,ActivityManager.RECENT_WITH_EXCLUDED|ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        if(!appTask.isEmpty()){
			for(int i = 0;i<appTask.size();i++){
			 	ActivityManager.RecentTaskInfo info = appTask.get(i);				
				
				Intent intent  = new Intent(info.baseIntent);
				if(info.origActivity != null) intent.setComponent(info.origActivity);
				ResolveInfo resolveInfo = pm.resolveActivity( intent,0);

	            if((resolveInfo != null)&&(packName.equals(resolveInfo.activityInfo.packageName))){
					Log.d(TAG, "moveRecentToFront --> packName:" + packName);
					am.moveTaskToFront(info.id,ActivityManager.MOVE_TASK_WITH_HOME,null);
					return info.id;
				}
		    }
        }
        return -1;
    }
	
	public void removeRecentApp(String packName){
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = mContext.getApplicationContext().getPackageManager();
        List<ActivityManager.RecentTaskInfo> appTask = am.getRecentTasks(50,ActivityManager.RECENT_WITH_EXCLUDED|ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        if(!appTask.isEmpty()){
            try {
                for(ActivityManager.RecentTaskInfo ra : appTask){
                    Intent intent = new Intent(ra.baseIntent);
                    if((isCurrentHomeActivity(intent.getComponent().getPackageName(), null))){ 
                        continue;
                    }

                    if(intent.getComponent().getPackageName().equals(packName)){
                        int persistentId = ra.persistentId; // pid 
                        Log.d(TAG, "removeRecentApp --> packName:" + packName);
                        am.removeTask(persistentId/*, ActivityManager.REMOVE_TASK_KILL_PROCESS*/);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
         }
	}
	
	private boolean filterOutPackage(String packName){
		if((null==packName)||"com.android.inputmethod.latin".equals(packName)
				||"com.android.packageinstaller".equals(packName)
				||isCurrentHomeActivity(packName,null)){
			return true;
		}
		return false;
	}
	
	public boolean isCurrentHomeActivity(String packName, ActivityInfo homeInfo) {
		if (homeInfo == null) {
			final PackageManager pm = mContext.getPackageManager();
			homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
				.resolveActivityInfo(pm, 0);
		}
		return homeInfo != null
			&& homeInfo.packageName.equals(packName);
    }
	
    public Drawable getTopAppIcon() {
        PackageManager pm = mContext.getApplicationContext().getPackageManager();
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RecentTaskInfo> appTask = am.getRecentTasks(Integer.MAX_VALUE, 1);
        Drawable icon ;
        ActivityInfo info = null;
        if (!appTask.isEmpty()) {
            try {
            info = pm.getActivityInfo(appTask.get(0).baseIntent.getComponent(), PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }  
            if(info!=null){
                icon = info.loadIcon(pm);
                return icon;
            }
        }
        return null;
    }
	
    public String getTopAppPackName(){
    	String packageName = null;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = mContext.getApplicationContext().getPackageManager();
        List<ActivityManager.RecentTaskInfo> appTask = am.getRecentTasks(1,ActivityManager.RECENT_WITH_EXCLUDED|ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        if(!appTask.isEmpty()){
			ActivityManager.RecentTaskInfo info = appTask.get(0);
				 
			if(info.topOfLauncher == 0) return null;
			Intent intent  = new Intent(info.baseIntent);
			if(info.origActivity != null) intent.setComponent(info.origActivity);
			ResolveInfo resolveInfo = pm.resolveActivity( intent,0);
            
            if(resolveInfo != null){
               packageName =  resolveInfo.activityInfo.packageName;
               
            }
         }
         Log.d(TAG, "getCurrentPackName packageName = "+packageName);
         return packageName;
	}
    
    public void clearRunningTasks(){
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RecentTaskInfo> run = am.getRecentTasks(512, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        PackageManager pm =mContext.getPackageManager();
        try {
            for(ActivityManager.RecentTaskInfo ra : run){
                Intent intent = new Intent(ra.baseIntent);
                if((isCurrentHomeActivity(intent.getComponent().getPackageName(), null))
                		||(intent.getComponent().getPackageName().equals("com.android.launcher"))
                		|| (intent.getComponent().getPackageName().equals("xxxx.xxxx.xxx"))){
                 continue;
                }


                int persistentId = ra.persistentId;
                am.removeTask(persistentId/*, ActivityManager.REMOVE_TASK_KILL_PROCESS*/);
 			 Toast.makeText(mContext, "Clear Recent APP", 500).show();
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    public boolean isServiceRunning(String SERVICE_NAME) {	
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_NAME.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
	
    public void ForceStopRunningApp(String packagename){
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        Log.d(TAG,"ForceStopRunningApp -->"+packagename);
        am.forceStopPackage(packagename);
    }
	
    public void startAppWithName(String packageName){
        PackageManager pm = mContext.getApplicationContext().getPackageManager();
        Intent intent=new Intent(); 
        intent = pm.getLaunchIntentForPackage(packageName);
        Log.d(TAG,"startAppWithName -->"+packageName);
        if(intent != null){
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP );
            mContext.startActivity(intent);
        }
    }
}
