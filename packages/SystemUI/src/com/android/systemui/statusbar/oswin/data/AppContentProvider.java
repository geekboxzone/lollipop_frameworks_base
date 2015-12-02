 package com.android.systemui.statusbar.oswin.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class AppContentProvider  extends ContentProvider {
	private static final String TAG = "ContentProvider";

    private static final String CONTENT_URI = "com.android.systemui.data.appprovider";
	
	private SQLiteHelper mHelper = null;
	private SQLiteDatabase mDataBase;
    private static final UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int APP_TYPE_TABLE  = 1;
    private static final int APP_TYPE_SINGLE = 2;
    private static final int APP_ITEM_TABLE  = 3;
    private static final int APP_ITEM_SINGLE = 4;
	
    static {  
    	mMatcher.addURI(CONTENT_URI, AppTypeEntity.TABLE_APP_TYPE, APP_TYPE_TABLE);  
    	mMatcher.addURI(CONTENT_URI, AppTypeEntity.TABLE_APP_TYPE+"/#", APP_TYPE_SINGLE);  
    	mMatcher.addURI(CONTENT_URI, AppItemEntity.TABLE_APP_ITEM, APP_ITEM_TABLE);  
    	mMatcher.addURI(CONTENT_URI, AppItemEntity.TABLE_APP_ITEM+"/#", APP_ITEM_SINGLE); 
    }     
 
    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }
 
    @Override
    public String getType(Uri arg0) {
        return null;
    }
 
    @Override
    public Uri insert(Uri uri, ContentValues values) {
    	long rowID = 0;
    	Log.e(TAG, "insert --> URI="+uri);
    	switch (mMatcher.match(uri)) {
    		case APP_TYPE_TABLE:
    			Log.e(TAG, "insert --> table="+AppTypeEntity.TABLE_APP_TYPE);
                rowID = mDataBase.insert(AppTypeEntity.TABLE_APP_TYPE, null, values);
    			break;
    		case APP_ITEM_TABLE:
    			Log.e(TAG, "insert --> table="+AppItemEntity.TABLE_APP_ITEM);
                rowID = mDataBase.insert(AppItemEntity.TABLE_APP_ITEM, null, values);   
                break;
    		default:
    			break;
    	}
    	if (rowID > 0) {  
    		Uri newUri = ContentUris.withAppendedId(uri, rowID);  
        	getContext().getContentResolver().notifyChange(newUri, null);  
        	return newUri;  
        }
    	throw new SQLException("Failed to insert row into " + uri);
    }
 
    @Override
    public boolean onCreate() {
        mHelper = new SQLiteHelper(this.getContext());
        mDataBase =  mHelper.getWritableDatabase();
        return true;
    }
 
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
    	Log.e(TAG, "query --> URI="+uri);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = null;
        switch (mMatcher.match(uri)) {
        	case APP_TYPE_TABLE:
        		Log.e(TAG, "query --> table="+AppTypeEntity.TABLE_APP_TYPE);
        		cursor = db.query(AppTypeEntity.TABLE_APP_TYPE, projection, selection, selectionArgs, null, null, null);
        		break;
        	case APP_TYPE_SINGLE:
        		break;
        	case APP_ITEM_TABLE:
        		Log.e(TAG, "query --> table="+AppItemEntity.TABLE_APP_ITEM);
        		cursor = db.query(AppItemEntity.TABLE_APP_ITEM, projection, selection, selectionArgs, null, null, null);
        		break;
        	case APP_ITEM_SINGLE:
        		break;
        	default:
        		break;
        }
        return cursor;
    }
 
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
		SQLiteDatabase db=mHelper.getWritableDatabase();
		int count=0;
        switch (mMatcher.match(uri)) {
	    	case APP_TYPE_TABLE:
	    		count=db.update(AppTypeEntity.TABLE_APP_TYPE, values, selection, selectionArgs);		
	    		break;
	    	case APP_TYPE_SINGLE:
	            long id = ContentUris.parseId(uri);  
	            String where = AppTypeEntity.KEY_TYPE_ID + "=" + id;  
	            if (selection != null && !"".equals(selection)) {  
	                where = selection + " and " + where;  
	            }  
	            count = db.update(AppTypeEntity.TABLE_APP_TYPE, values, where, selectionArgs); 
	    		break;
	    	case APP_ITEM_TABLE:
	    		count=db.update(AppItemEntity.TABLE_APP_ITEM, values, selection, selectionArgs);
	    		break;
	    	case APP_ITEM_SINGLE:
	    		break;
	    	default:
	    		throw new IllegalArgumentException("Unknown Uri:"+uri.toString());
        }
        if(count > 0){
        	getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
    
    public class SQLiteHelper extends SQLiteOpenHelper{
    	public static final String db_name = "app.db";
    	public static final int version = 1;
    	private static final String TAG = "SQLiteHelper";
    	 
    	public SQLiteHelper(Context context) {
    		super(context, db_name, null, version);
    	}
    	 
    	@Override
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL(AppTypeEntity.APP_TYPE_TABLE_CREATE);
    		db.execSQL(AppItemEntity.APP_ITEM_TABLE_CREATE);
    	}
    	 
    	@Override
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		db.execSQL("DROP TABLE IF EXISTS " + AppTypeEntity.TABLE_APP_TYPE);
    		db.execSQL("DROP TABLE IF EXISTS " + AppItemEntity.TABLE_APP_ITEM);
            onCreate(db);	 
    	}
    }
}
