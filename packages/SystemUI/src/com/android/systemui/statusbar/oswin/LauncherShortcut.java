package com.android.systemui.statusbar.oswin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

public class LauncherShortcut {
	public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
	public static final String ACTION_REMOVE_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";
    private static final String TAG = "TRACE: LauncherShortcut";
	
	public static void addShortcutForApp(Context context, String name, Bitmap icon,
			String packName, String actiName) {

		//if(hasInstallShortcut(context, name)){
		//	return ;
		//}
	    
		Intent scIntent = new Intent(ACTION_ADD_SHORTCUT);
		scIntent.putExtra("duplicate", false);
		scIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
	    
	    //set app icon
	    //Bitmap b = BitmapFactory.decodeByteArray(iconArray, 0, iconArray.length);
	    scIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
	    
	    //set related application
	    Intent launcherIntent = makeLaunchIntent(packName, actiName);
	    scIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
	    
	    //send broadcast
	    context.sendBroadcast(scIntent);
    }
	
    public static void addShortcutForFile(Context context, String name, Bitmap icon,
            String fullFilePath) {

        //if(hasInstallShortcut(context, name)){
        //    return ;
        //}
        
        Intent scIntent = new Intent(ACTION_ADD_SHORTCUT);
        scIntent.putExtra("duplicate", false);
        scIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        
        //set app icon
        //Bitmap b = BitmapFactory.decodeByteArray(iconArray, 0, iconArray.length);
        scIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        
        //set related application
        Intent launcherIntent = makeFileViewIntent(fullFilePath);
        scIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
        
        //send broadcast
        context.sendBroadcast(scIntent);
    }
	
	public void removeShortcut(Context context, String name, String packName, String actiName) {

        Intent intent = new Intent(ACTION_REMOVE_SHORTCUT);
        
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        
        Intent launcher = makeLaunchIntent(packName, actiName);

        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcher);

        context.sendBroadcast(intent);
    }
	
    public static Intent makeLaunchIntent(String packName, String actiName) {
        return new Intent(Intent.ACTION_MAIN)
             .addCategory(Intent.CATEGORY_LAUNCHER)
             .setComponent(new ComponentName(packName, actiName))
             .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }	
	
    public static Intent makeFileViewIntent(String fullFilePath) {
       return new Intent(Intent.ACTION_VIEW, null).setData(Uri.parse(String.format("file://%s", fullFilePath)));
    }
    
    public static void testWebIntent(String fullFilePath){
        Log.e(TAG, "testWebIntent path = " + fullFilePath);
        Uri uri = Uri.parse(String.format("file://%s", fullFilePath));
        Intent intent = makeFileViewIntent(fullFilePath);
        Log.e(TAG, "testWebIntent uri = " + uri.toString());
        Log.e(TAG, "testWebIntent intent.getData() = " + intent.getData());
        Log.e(TAG, "testWebIntent intent.getDataString() = " + intent.getDataString());
    }
	
	private static boolean hasInstallShortcut(Context context, String name) {

        boolean hasInstall = false;

        final String AUTHORITY = "com.android.launcher3.settings";
        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                + "/favorites?notify=true");

        Cursor cursor = context.getContentResolver().query(CONTENT_URI,
                new String[] { "title", "iconResource" }, "title=?",
                new String[] { name }, null);

        if (cursor != null && cursor.getCount() > 0) {
            hasInstall = true;
        }

        return hasInstall;
    }

}
