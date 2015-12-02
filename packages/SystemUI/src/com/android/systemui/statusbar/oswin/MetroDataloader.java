package com.android.systemui.statusbar.oswin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.statusbar.oswin.data.AppContentProvider;
import com.android.systemui.statusbar.oswin.data.AppItemEntity;
import com.android.systemui.statusbar.oswin.data.AppTypeEntity;

public class MetroDataloader {
	private static final String TAG = "WinMetroWindow";
	private final String mKey_icon = "key_icon";
	private final String mKey_name = "key_name";
    private static final String CONTENT_URI = "com.android.systemui.data.appprovider";
	private final Uri APP_TYPE_URI= Uri.parse("content://com.android.systemui.data.appprovider/tb_app_type");
	private final Uri APP_ITEM_URI= Uri.parse("content://com.android.systemui.data.appprovider/tb_app_item");

    public void addDefalutAppType(ContentResolver cr, Context context){
    	Cursor cursor = cr.query(APP_TYPE_URI,  
                null, null, null, null);
        if((cursor == null) || (cursor.getCount()==0)){
            insertAppType(cr, context, AppTypeEntity.TABLE_TYPE_ALL, R.string.app_type_all);
        	insertAppType(cr, context, AppTypeEntity.TABLE_TYPE_SYSTEM,R.string.app_type_system);
        	insertAppType(cr, context, AppTypeEntity.TABLE_TYPE_OFFICE,R.string.app_type_office);
        	insertAppType(cr, context, AppTypeEntity.TABLE_TYPE_GAME,  R.string.app_type_game);
        	insertAppType(cr, context, AppTypeEntity.TABLE_TYPE_MEDIA, R.string.app_type_media);
        	insertAppType(cr, context, AppTypeEntity.TABLE_TYPE_OTHER, R.string.app_type_other);
        }else{
            updateAppType(cr, context, AppTypeEntity.TABLE_TYPE_ALL, R.string.app_type_all);
        	updateAppType(cr, context, AppTypeEntity.TABLE_TYPE_SYSTEM,R.string.app_type_system);
        	updateAppType(cr, context, AppTypeEntity.TABLE_TYPE_OFFICE,R.string.app_type_office);
        	updateAppType(cr, context, AppTypeEntity.TABLE_TYPE_GAME,  R.string.app_type_game);
        	updateAppType(cr, context, AppTypeEntity.TABLE_TYPE_MEDIA, R.string.app_type_media);
        	updateAppType(cr, context, AppTypeEntity.TABLE_TYPE_OTHER, R.string.app_type_other);
        }
    }
    
    public void addDefalutSystemApp(ContentResolver cr, AppTaskManager manager){
    	Cursor cursor = cr.query(APP_ITEM_URI, null, null, null, null);
    	Log.d(TAG, "addDefalutSystemApp cursor.getCount()" + cursor.getCount());
        if((cursor == null) || (cursor.getCount()==0)){
        	List<PackInfo> mTaskList = manager.getInstalledApps_RK(0, 100);
        	for(int i = 0 ; i < mTaskList.size(); i++){
        		insertAppItem(cr, AppTypeEntity.TABLE_TYPE_SYSTEM,  mTaskList.get(i));
        	}
        }
    }

    public int queryAppItemCount(Context context, int type_id){
        Cursor cursor = null;
        String[] args = {String.valueOf(type_id)};
        if(AppTypeEntity.TABLE_TYPE_ALL != type_id){
            if(null != context){
    	        cursor = context.getContentResolver().query(APP_ITEM_URI, null, 
                        String.format("%s=?", AppItemEntity.KEY_APP_TYPE), args, null);
            }
        }else{
            if(null != context){
    	        cursor = context.getContentResolver().query(APP_ITEM_URI, null, 
                        null, null, null);
            }
        }
        if(null != cursor){
            return cursor.getCount();
        }
        return 0;
    }
    
    public void queryAppItemList(Context context, int type_id, List<PackInfo> itemList){
    	String[] args = {String.valueOf(type_id)};
    	Cursor cursor = context.getContentResolver().query(APP_ITEM_URI, null, 
    			String.format("%s=?", AppItemEntity.KEY_APP_TYPE), args, null); 
    	//Log.d(TAG, "addDefalutSystemApp cursor.getCount()" + cursor.getCount());
    	while((cursor!=null)&&cursor.moveToNext()){
    		PackInfo item = AppItemEntity.makePackInfoFromCursor(cursor);
    		if(null != item){
    			itemList.add(item);
    		}
    	}
    }

    /**
         * Query app items by page in Sqlite database with app type id
         * getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder)
         *  sortOrder:  asc(up),desc(down); <_id asc LIMIT 20 OFFSET 20>
         */
    public void queryAppItemByPage(Context context, int type_id, 
                    int page_index, int page_size, List<PackInfo> itemList){
        //"sort_key COLLATE LOCALIZED asc limit " + pageSize + " offset " + currentOffset
        Cursor cursor = null;
     	String[] args = {String.valueOf(type_id)};
        //Limit and offset is no effect, Why????????????
        //String limit = String.format("_id asc LIMIT %d OFFSET %d", page_size, (page_index-1)*page_size);
        //String limit = String.format("limit %d,%d",(page_index-1)*page_size,page_size);
        String limit = null;   
        if(AppTypeEntity.TABLE_TYPE_ALL != type_id){
    	    cursor = context.getContentResolver().query(APP_ITEM_URI, null, 
                        String.format("%s=?", AppItemEntity.KEY_APP_TYPE), args, limit);
        }else{
    	    cursor = context.getContentResolver().query(APP_ITEM_URI, null, 
                        null, null, limit);
        }

    	Log.d(TAG, "queryAppItemByPage cursor.getCount()=" + cursor.getCount());
        int index_start = (page_index-1)*page_size;
        int index_end   = index_start + page_size;
        int index = 0; 
        int count =0;
    	while((cursor!=null)&&cursor.moveToNext()){
            if((index>= index_start)&&(index< index_end)){
        		PackInfo item = AppItemEntity.makePackInfoFromCursor(cursor);
        		if(null != item){
        			itemList.add(item);
                    count++;
        		}
            }
            index++;
    	}
        Log.d(TAG, "queryAppItemByPage CountInPage = " + count);
    }
    
    public void queryAppTypeList(Context context, ArrayList<HashMap<String, Object>> typeList){
        Cursor cursor = context.getContentResolver().query(APP_TYPE_URI, null, null, null, null);  
        while((cursor!=null)&&cursor.moveToNext()){
            HashMap<String, Object> item = new HashMap<String, Object>(); 
            item.put(mKey_name, cursor.getString(cursor.getColumnIndex(AppTypeEntity.KEY_TYPE_ATLAS)));
            int index = cursor.getInt(cursor.getColumnIndex(AppTypeEntity.KEY_TYPE_ID));
            switch(index){
            case AppTypeEntity.TABLE_TYPE_ALL:
                item.put(mKey_icon, context.getResources().getDrawable(R.drawable.win_metro_type_allapp));
                break;
            case AppTypeEntity.TABLE_TYPE_SYSTEM:
                item.put(mKey_icon, context.getResources().getDrawable(R.drawable.win_metro_type_system));
                break;
            case AppTypeEntity.TABLE_TYPE_OFFICE:
                item.put(mKey_icon, context.getResources().getDrawable(R.drawable.win_metro_type_office));
                break;
            case AppTypeEntity.TABLE_TYPE_GAME:
                item.put(mKey_icon, context.getResources().getDrawable(R.drawable.win_metro_type_game));
    			break;
            case AppTypeEntity.TABLE_TYPE_MEDIA:
                item.put(mKey_icon, context.getResources().getDrawable(R.drawable.win_metro_type_social));
                break;
            case AppTypeEntity.TABLE_TYPE_OTHER:
                item.put(mKey_icon, context.getResources().getDrawable(R.drawable.win_metro_type_other));
                break;
    		default:
                item.put(mKey_icon, context.getResources().getDrawable(R.drawable.win_metro_type_other));
    		}
            item.put(AppTypeEntity.KEY_TYPE_ID, index);

            typeList.add(item);
        }	
    }
    
    public String queryAccountInfo(Context context){
    	AccountManager accountManager =  AccountManager.get(context); 
    	Account[] accounts = accountManager.getAccounts(); 
    	for(Account account:accounts){
    		return account.name;
    	}
    	return null;
    }
    
    public void queryCoreApp(Context context, ArrayList<HashMap<String, Object>> list){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(mKey_name, "FileExplor");
        map.put("mKey_pkg_name", "com.rockchip.fileexplorer");
        map.put("mKey_act_name", "com.rockchip.fileexplorer.RKFileExplorer");
        map.put(mKey_icon, context.getResources().getDrawable(R.drawable.win_metro_type_explorer));
        list.add(map);
     
        map = new HashMap<String, Object>();
        map.put(mKey_name, "Settings");
        map.put("mKey_pkg_name", "com.android.settings");
        map.put("mKey_act_name", "com.android.settings.Settings");
        map.put(mKey_icon, context.getResources().getDrawable(R.drawable.win_metro_type_setting));
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(mKey_name, "Power");
        map.put("mKey_pkg_name", "com.android.power");
        map.put("mKey_act_name", "com.android.power.poweropt");
        map.put(mKey_icon, context.getResources().getDrawable(R.drawable.win_metro_type_power));
        list.add(map);
    }

    private ContentValues makeAppType(int type_id, String atlas){
    	ContentValues cv = new ContentValues();
    	cv.put(AppTypeEntity.KEY_TYPE_ID, type_id);
    	cv.put(AppTypeEntity.KEY_TYPE_ATLAS, atlas);
    	return cv;
    }
    
    private ContentValues makeAppItem(int type_id, PackInfo info){
    	AppItemEntity entity = new AppItemEntity();
    	entity.setType(type_id);
    	entity.setAppName(info.getAppName());
    	entity.setPackageName(info.getPackageName());
    	entity.setActivityName(info.getActivityName());
    	entity.setIcon(info.getIcon());
    	return entity.getConentValues();
    }
    
    private void insertAppType(ContentResolver cr, Context context, int type_id,  int altas_id){
    	cr.insert(APP_TYPE_URI, makeAppType(type_id, context.getResources().getString(altas_id)));
    }
    
    public void updateAppType(ContentResolver cr, Context context, int type_id,  int altas_id){
    	cr.update(Uri.withAppendedPath(APP_TYPE_URI, String.valueOf(type_id)), 
    			makeAppType(type_id, context.getResources().getString(altas_id)),null, null);
    }
    
    private void insertAppItem(ContentResolver cr, int type_id,  PackInfo info){
    	cr.insert(APP_ITEM_URI, makeAppItem(type_id, info));
    }
    
    public void updateAppItem(ContentResolver cr, int type_id,  PackInfo info){
    	String[] args = {info.getPackageName()};
    	cr.update(APP_ITEM_URI, makeAppItem(type_id, info), 
    			String.format("%s=?", AppItemEntity.KEY_PKG_NAME), args);
    }
}
