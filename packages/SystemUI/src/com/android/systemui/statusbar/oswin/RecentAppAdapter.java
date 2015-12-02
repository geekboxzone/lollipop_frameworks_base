package com.android.systemui.statusbar.oswin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.systemui.R;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.util.Log;
import android.view.LayoutInflater;  
import android.view.View;
import android.view.ViewGroup;  
import android.widget.BaseAdapter;  
import android.widget.ImageView;  
import android.widget.TextView;

public class RecentAppAdapter extends BaseAdapter{
    public static final String TAG = "WinStatusBar";
    private Context mContext;
    private LayoutInflater mInflater;
    ArrayList<HashMap<String, Object>> mAppList;
    
    public RecentAppAdapter(Context context, ArrayList<HashMap<String, Object>> applist){  
        mInflater = LayoutInflater.from(context);
        mContext  = context;
        mAppList  = applist;
    }

    public void onClickItem(View arg){
        // Bring an active task to the foreground
        ViewHolder vh=(ViewHolder)arg.getTag();
    }
	
    public void clearRunningApps(){
    	for(int i = 0 ; i < mAppList.size(); i++){
    		HashMap<String, Object> data = (HashMap<String, Object>)mAppList.get(i);
    		Drawable image = (Drawable)data.get("icon");
    		image.setCallback(null);
    		((BitmapDrawable)image).getBitmap().recycle();
    	}
    	mAppList.clear();
    	this.notifyDataSetChanged();
    }

    @Override  
    public int getCount() {  
        return mAppList.size();  
    } 
    
    @Override  
    public Object getItem(int position) {  
        return position;  
    }
    
    private static class ViewHolder {
        private TextView mName ;  
        private ImageView mIcon;
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
            //convertView = mInflater.inflate(R.layout.gridview_recent_item, null);  
            //holder.mIcon=(ImageView)convertView.findViewById(R.id.app_icon);   
            //holder.mName=(TextView)convertView.findViewById(R.id.app_name);
            convertView = mInflater.inflate(R.layout.recent_list_item, null);  
            holder.mIcon=(ImageView)convertView.findViewById(R.id.tv_icon);   
            holder.mName=(TextView)convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
            holder.mIcon.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();  
        }

        holder.mIcon.setClickable(true);
        holder.mIcon.setImageDrawable((Drawable)mAppList.get(position).get("icon"));

        return convertView;  
    }
}  

