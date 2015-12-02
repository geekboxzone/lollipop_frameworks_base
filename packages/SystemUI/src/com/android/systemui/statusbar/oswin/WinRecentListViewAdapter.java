package com.android.systemui.statusbar.oswin;

import java.util.ArrayList;  
import java.util.List; 
  
import com.android.systemui.R;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.ActivityNotFoundException;

import android.util.Log;
import android.view.LayoutInflater;  
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;  
import android.widget.BaseAdapter;  
import android.widget.ImageView;  
import android.widget.TextView;

import com.android.systemui.recents.model.*;
import com.android.systemui.recents.AlternateRecentsComponent;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.SystemServicesProxy;

public class WinRecentListViewAdapter extends BaseAdapter{
    public static final String TAG = "WinRecentListViewAdapter";
    private RecentsConfiguration mConfig;
    private Context mContext;
    private AppTaskManager mAppTaskManager;
    private List<PackInfo> mListRecentTask = null;
    
    public WinRecentListViewAdapter(Context context, AppTaskManager taskManager){  
        mInflater=LayoutInflater.from(context);
        mContext = context;
        mAppTaskManager = taskManager;

        mListRecentTask = mAppTaskManager.getRunningApps(0, 10);

        mAppTaskManager.addUpdateCallback(new AppTaskManager.UpdateCallback(){
            @Override
            public void onUpdateList(){
                mListRecentTask.clear();
                mListRecentTask = mAppTaskManager.getRunningApps(0, 10);
            }
        });
    }

    public void onClickItem(View arg){
        // Bring an active task to the foreground
        ViewHolder vh=(ViewHolder)arg.getTag();
    }

    @Override  
    public int getCount() {  
        return mListRecentTask.size();  
    }
    
    private LayoutInflater mInflater; 
    
    @Override  
    public Object getItem(int position) {  
        return position;  
    }
    
    private static class ViewHolder {
        private TextView mName ;  
        private ImageView mIcon;
        private PackInfo mTaskInfo;
    }
    
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.recent_list_item, null);  
            holder.mIcon=(ImageView)convertView.findViewById(R.id.tv_icon);   
            holder.mName=(TextView)convertView.findViewById(R.id.tv_name);
            holder.mTaskInfo = mListRecentTask.get(position);
            convertView.setTag(holder);
            holder.mIcon.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();  
        }

        holder.mIcon.setClickable(true);
        holder.mIcon.setBackground(holder.mTaskInfo.getIcon());
        holder.mIcon.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View host) {
				// TODO Auto-generated method stub
				ViewHolder holder=(ViewHolder)host.getTag();
                lunchTask(holder.mTaskInfo);
			}
        });

        return convertView;  
    }

    private void lunchTask(PackInfo pack){
        try{
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            ComponentName componentName = new ComponentName(pack.getPackageName(),pack.getActivityName());
            intent.setComponent(componentName);
            Log.e(TAG, "lunchTask -->Task = " + pack.toString());
            mContext.startActivity(intent);
        }
        catch (ActivityNotFoundException e){
            e.printStackTrace();
        } 
        catch (SecurityException e) {
            e.printStackTrace();
        }
    }

}  

