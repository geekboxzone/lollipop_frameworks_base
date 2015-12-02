package com.android.systemui.statusbar.oswin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class AppTaskManager extends BroadcastReceiver{
	private static final String TAG = "AppTaskManager";
	private Context mContext;
	private ActivityManager mAm;
	private PackageManager  mPm;
	private List<ResolveInfo> mListTask = null;
	private List<UpdateCallback> mListCallback = null;
	
	public interface UpdateCallback{
		public void onUpdateList();
    }
	public AppTaskManager(Context conext){
		mContext = conext;
		mAm = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE); 
		mPm = mContext.getPackageManager();
	    registerBroadcast();
	    mListCallback = new ArrayList<UpdateCallback>();
	    mListCallback.clear();
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onReceive Intent.name=" + arg1.getAction());
        for(UpdateCallback callback : mListCallback){ 
        	callback.onUpdateList();
        }
	}
	
	private void registerBroadcast(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_INSTALL);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		filter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
		mContext.registerReceiver(this, filter);
	}
	
    public void releaseManager(){
    	mContext.unregisterReceiver(this);
    	mListTask.clear();
    }
    
    public void addUpdateCallback(UpdateCallback callback){
    	if(mListCallback.contains(callback) == false){
    		mListCallback.add(callback);
    	}
    }
	
	private ResolveInfo getAppInfo(String name){ 
        if(name == null){ 
            return null; 
        } 
        for(ResolveInfo task : mListTask){ 
            if(name.equals(task.activityInfo.packageName)){ 
                return task; 
            } 
        } 
        return null; 
    }
	
    public Drawable getActivityIcon(PackageManager pm, ComponentName cn){
    	ActivityInfo info = null;
        try {
        	info = pm.getActivityInfo(cn, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        return info.loadIcon(pm);
    }
    
    /** Returns the activity label */
    public String getActivityName(PackageManager pm, ComponentName cn) {
    	ActivityInfo info = null;
        try {
        	info = pm.getActivityInfo(cn, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

        return info.loadLabel(pm).toString();
    }
	
    public List<PackInfo> getRunningApps(int start, int pagesize){
    	int index = 0;
        List<PackInfo> list = new ArrayList<PackInfo>();
        list.clear();
        
    	//Query Running Applications
        List<RunningAppProcessInfo> listRun = mAm.getRunningAppProcesses();      
        //Query Installed Applications
		Intent intent = new Intent(Intent.ACTION_MAIN,null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		mListTask = mPm.queryIntentActivities(intent,0);
		Collections.sort(mListTask, new ResolveInfo.DisplayNameComparator(mPm));

		for(int i = 0; i < listRun.size(); i++){
            //Filter Out System Applications
			RunningAppProcessInfo ra = listRun.get(i);
            if(filterRunningApp(ra.processName)){
            	continue;
            }
            if((index>=start) && (index<(start+pagesize))){
            	ResolveInfo rsi= getAppInfo(ra.processName);
                if(null != rsi){
	                PackInfo pack = new PackInfo(); 
                    pack.setAppName(rsi.loadLabel(mPm).toString());
                    pack.setPackageName(rsi.activityInfo.packageName);
                    pack.setIcon(rsi.loadIcon(mPm));
                    pack.setActivityName(rsi.activityInfo.name);
                    Log.e(TAG,"getRunningApps --> " + pack.toString());
	                list.add(pack);
	                index++; 
                }
            }
		}
        
        mListTask.clear();
        listRun.clear();
        return list; 
    }
    
    private boolean filterRunningApp(String name){
    	if( name.startsWith("com.android")||
    	    name.startsWith("android")||
    	    name.startsWith("system")||
    	    name.startsWith("com.svox.pico")||
    	    name.startsWith("com.rockchip.itvbox")||
    	    name.startsWith("com.cghs.stresstest")	){
    		return true;
    	}
    	return false;
   }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP) 
    public List<PackInfo> getRecentApps(int start, int pagesize){
    	int index = 0;
        List<PackInfo> listRecent = new ArrayList<PackInfo>();
        listRecent.clear();
    	
    	//Query Recent Applications
        if(Build.VERSION.SDK_INT >= 21){
        	//for LOLLIPOP
        	List<AppTask> listApp = mAm.getAppTasks();
        	
        	for (AppTask recent : listApp) {
        		//recent.moveToFront();
        		RecentTaskInfo taskInfo = recent.getTaskInfo();
        		ComponentName cn = taskInfo.baseIntent.getComponent();
        		if((index>=start) && (index<(start+pagesize))&&(null!=cn)){
	                PackInfo pack = new PackInfo();
	                pack.setPackageName(cn.getPackageName());
	                pack.setActivityName(cn.getClassName());
	                pack.setAppName(this.getActivityName(mPm,cn));
					pack.setIcon(this.getActivityIcon(mPm, cn));
	                Log.e(TAG,"getRecentApps --> " + pack.toString());
	                listRecent.add(pack);
	                index++;
        		}
        		//Log.e(TAG,recent.getTaskInfo().origActivity.toString());
        	}
        	listApp.clear();
       }else{
    	    //for JellyBean
	       	List<RecentTaskInfo> listTask = mAm.getRecentTasks(pagesize, 1);
	       	for (RecentTaskInfo recent : listTask) {
        		ComponentName cn = recent.baseIntent.getComponent();
        		if((index>=start) && (index<(start+pagesize))&&(null != cn)){
	                PackInfo pack = new PackInfo();
	                pack.setPackageName(cn.getPackageName());
	                pack.setActivityName(cn.getClassName());
	                pack.setAppName(this.getActivityName(mPm,cn));
	                pack.setIcon(this.getActivityIcon(mPm, cn));
	                Log.e(TAG,"getRecentTasks --> " + pack.toString());
	                listRecent.add(pack);
	                index++;
        		}
	       	}
	       	listTask.clear();
       }
        return listRecent;
    }
    
    public  List<RecentTaskInfo> getRecentFromUser(int start, int pagesize){
        // Remove home/recents/excluded tasks
        List<RecentTaskInfo> tasks = mAm.getRecentTasksForUser(pagesize,
                ActivityManager.RECENT_IGNORE_HOME_STACK_TASKS |
                ActivityManager.RECENT_IGNORE_UNAVAILABLE |
                ActivityManager.RECENT_INCLUDE_PROFILES |
                ActivityManager.RECENT_WITH_EXCLUDED, UserHandle.CURRENT.getIdentifier());

        // Break early if we can't get a valid set of tasks
        if (tasks == null) {
            return new ArrayList<RecentTaskInfo>();
        }

        boolean isFirstValidTask = true;
        Iterator<RecentTaskInfo> iter = tasks.iterator();
        while (iter.hasNext()) {
            RecentTaskInfo t = iter.next();
            boolean isExcluded = (t.baseIntent.getFlags() & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    == Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
            if (isExcluded && (!isFirstValidTask)) {
                iter.remove();
                continue;
            }
            isFirstValidTask = false;
        }

        return tasks.subList(0, Math.min(tasks.size(), pagesize));    	
    }
    
    public List<PackInfo> getInstalledApps_RK(int start, int pagesize){
    	int index = 0;
    	List<PackInfo> list = new ArrayList<PackInfo>();
    	list.clear();
    	
		Intent intent = new Intent(Intent.ACTION_MAIN,null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolve = mPm.queryIntentActivities(intent,0);
		Collections.sort(resolve, new ResolveInfo.DisplayNameComparator(mPm));

		for(int i = 0; i < resolve.size(); i++){
			ResolveInfo app_info = resolve.get(i);

			String packageName = app_info.activityInfo.packageName;
			if(filterApk(packageName))
				continue;
			
			if((index>=start) && (index<(start+pagesize))){
				PackInfo pi = new PackInfo();
				pi.setAppName(app_info.loadLabel(mPm).toString());
				pi.setPackageName(packageName);
				pi.setIcon(app_info.loadIcon(mPm));
				pi.setActivityName(app_info.activityInfo.name);
				
				try {
					PackageInfo info = mPm.getPackageInfo(packageName, 0);
					pi.setVersionCode(info.versionCode);
					pi.setVersionName(info.versionName);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				list.add(pi);
				index++;
			}
		}
		
		return list;
    }
    
	public boolean filterApk(String packagenName){
		 if((packagenName.compareTo("com.android.browser") == 0) ||
			(packagenName.compareTo("com.android.calculator2") == 0) ||
			(packagenName.compareTo("com.android.calendar") == 0) ||
			(packagenName.compareTo("com.android.videoeditor") == 0) ||
			(packagenName.compareTo("com.android.deskclock") == 0) ||
			(packagenName.compareTo("com.android.development") == 0) ||
			(packagenName.compareTo("com.android.providers.downloads.ui") == 0) ||
			(packagenName.compareTo("com.cooliris.media") == 0) ||
			(packagenName.compareTo("com.android.music") == 0) ||
			(packagenName.compareTo("com.android.quicksearchbox") == 0) ||
			(packagenName.compareTo("com.android.camera") == 0) ||
			(packagenName.compareTo("com.android.spare_parts") == 0)  ||
			(packagenName.compareTo("com.android.speechrecorder") == 0) ||
			(packagenName.compareTo("com.appside.android.VpadMonitor") == 0) ||
			(packagenName.compareTo("com.rk.youtube") == 0) ||
			(packagenName.compareTo("com.android.soundrecorder") == 0))
		{
			return true;
		}
		return false;
	}
}
