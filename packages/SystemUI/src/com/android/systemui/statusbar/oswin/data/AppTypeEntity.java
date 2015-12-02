package com.android.systemui.statusbar.oswin.data;


import android.content.ContentValues;

public class AppTypeEntity {
	private int mAppTypeID;
	private String mAppTypeAtlas;
	
	public static final String KEY_ID = "_id"; 	
	public static final String KEY_TYPE_ID    ="type_id";
	public static final String KEY_TYPE_ATLAS ="type_atlas";
	public static final String TABLE_APP_TYPE = "tb_app_type";

	public static final int TABLE_TYPE_BASE   = 0x10;
    public static final int TABLE_TYPE_ALL  = TABLE_TYPE_BASE;
	public static final int TABLE_TYPE_SYSTEM = TABLE_TYPE_BASE+1;
	public static final int TABLE_TYPE_OFFICE = TABLE_TYPE_BASE+2;
	public static final int TABLE_TYPE_GAME   = TABLE_TYPE_BASE+3;
	public static final int TABLE_TYPE_MEDIA  = TABLE_TYPE_BASE+4;
    public static final int TABLE_TYPE_OTHER  = TABLE_TYPE_BASE+5;
	


	public static final String APP_TYPE_TABLE_CREATE = "create table "  
            + TABLE_APP_TYPE + " ("
			+ KEY_ID  + " integer primary key autoincrement, "
			+ KEY_TYPE_ID + " integer, "
            + KEY_TYPE_ATLAS + " TEXT" + ");"; 
	
	public AppTypeEntity(){
		mAppTypeID = 0;
		mAppTypeAtlas = null;
	}
	
	public ContentValues getConentValues(){
		ContentValues content = new ContentValues();
		content.put(KEY_TYPE_ID, mAppTypeID);
		content.put(KEY_TYPE_ATLAS, mAppTypeAtlas);

		return content;
	}
}
