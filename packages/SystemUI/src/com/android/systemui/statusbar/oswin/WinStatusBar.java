/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.oswin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.annotation.NonNull;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.R;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.BaseStatusBar;

//import com.android.systemui.statusbar.phone.NavigationBarView;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.os.Messenger;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.Collection;
import java.util.Collections;
import java.io.File;
import java.util.List;
import android.widget.Toast;
import android.widget.TextView;
import android.provider.Settings;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import java.util.Map;


/*
 * Status bar implementation for "large screen" products that mostly present no on-screen nav
 */

public class WinStatusBar extends BaseStatusBar {
    static final String TAG = "WinStatusBar";
    public static final boolean DEBUG = BaseStatusBar.DEBUG;

    // tracking calls to View.setSystemUiVisibility()
    int mSystemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;

    WinStatusBarView mStatusBarView;
    private HandlerThread mHandlerThread;
    private WinNavigationBarView mWinNavigationBarView = null;
    private HorizontalListView mRecentListView;
    private WinMetroWindow mWinMetro;
    private WinRecentListViewAdapter  mRecentAdapter;
    private AppTaskManager mAppTaskManager;
    private ContentResolver mContentResolver;
    private MetroDataloader mMetroDataLoader;
	private SystemTime mSystemTime;


    private void notifyUiVisibilityChanged(int vis) {
        try {
            mWindowManagerService.statusBarVisibilityChanged(vis);
        } catch (RemoteException ex) {
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.v(TAG, "onReceive: " + intent);
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                Log.e(TAG, "onReceive action=" + action);
            }
            else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenOn = false;
                //notifyNavigationBarScreenOn(false);
                notifyHeadsUpScreenOn(false);
                //finishBarAnimations();
                //resetUserExpandedStates();
            }
            else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenOn = true;
                //notifyNavigationBarScreenOn(true);
            }
        }
    };

    private void addStatusBarWindow() {
        makeStatusBarView();
        //mStatusBarWindowManager = new StatusBarWindowManager(mContext);
        //mStatusBarWindowManager.add(mStatusBarWindow, getStatusBarHeight());
    }

    protected WinStatusBarView makeStatusBarView() {
        // Background thread for any controllers that need it.
        mHandlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        final Context context = mContext;
        mWinNavigationBarView =
            (WinNavigationBarView) View.inflate(context, R.layout.nav_bar_oswin, null);
        
        mWinNavigationBarView.setDisabledFlags(0);
        mWinNavigationBarView.setBar(this);

        mStatusBarView = null;

        return mStatusBarView;
    }

	
    @Override
    protected void updateSearchPanel() {
        super.updateSearchPanel();
        if (mWinNavigationBarView != null) {
            mWinNavigationBarView.setDelegateView(mSearchPanelView);
        }
    }


    //////////////////////////////////////////////////////////////////
    //Search Panel
    private Runnable mShowSearchPanel = new Runnable() {
        public void run() {
            showSearchPanel();
        }
    };

    @Override
    public void showSearchPanel() {
        super.showSearchPanel();
        mHandler.removeCallbacks(mShowSearchPanel);

        // we want to freeze the sysui state wherever it is
        mSearchPanelView.setSystemUiVisibility(mSystemUiVisibility);

        if (mWinNavigationBarView != null) {
            WindowManager.LayoutParams lp =
                (android.view.WindowManager.LayoutParams) mWinNavigationBarView.getLayoutParams();
            lp.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mWindowManager.updateViewLayout(mWinNavigationBarView, lp);
        }
    }

    @Override
    public void hideSearchPanel() {
        super.hideSearchPanel();
        if (mWinNavigationBarView != null) {
            WindowManager.LayoutParams lp =
                (android.view.WindowManager.LayoutParams) mWinNavigationBarView.getLayoutParams();
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mWindowManager.updateViewLayout(mWinNavigationBarView, lp);
        }
    }

    ////////////////////////////////////////////////////////////////
    //destroy()
    @Override
    public void destroy() {
        super.destroy();
        if (mStatusBarView != null) {
            mWindowManager.removeViewImmediate(mStatusBarView);
            mStatusBarView = null;
        }
        if (mWinNavigationBarView != null) {
            mWindowManager.removeViewImmediate(mWinNavigationBarView);
            mWinNavigationBarView = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    ////////////////////////////////////////////////////////////////
    //Navigation Bar
    // For small-screen devices (read: phones) that lack hardware navigation buttons
    private boolean mBarIsAdd = true;
    private void addNavigationBar() {
        if (DEBUG) Log.e(TAG, "addNavigationBar -> mWinNavigationBarView=" + mWinNavigationBarView);
        if (mWinNavigationBarView == null) return;

        prepareNavigationBarView();

        mWindowManager.addView(mWinNavigationBarView, getNavigationBarLayoutParams());
        mBarIsAdd = true;
    }

    private class RecentClickListener implements OnItemClickListener{
	    @Override
	    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){
	        mRecentAdapter.onClickItem(arg1);
	    }
    };

    private void prepareNavigationBarView() {
        mWinNavigationBarView.reorient();
        int height = mWinNavigationBarView.getHeight();

        //init recent listview
        mRecentListView =(HorizontalListView)mWinNavigationBarView.findViewById(R.id.recent_listview);  
        mAppTaskManager = new AppTaskManager(this.mContext);
        mRecentAdapter= new WinRecentListViewAdapter(this.mContext, mAppTaskManager);     
        mRecentAdapter.notifyDataSetChanged();  
        mRecentListView.setAdapter(mRecentAdapter); 
        mRecentListView.setOnItemClickListener(new RecentClickListener());

        //init recent button
        mWinNavigationBarView.getRecentsButton().setOnClickListener(mRecentsClickListener);

        //init SystemTime
		initSystemTime();

        //init winstart button
        View winStart = (View)mWinNavigationBarView.findViewById(R.id.win_start);
        
        
        winStart.setOnClickListener(mWinStartClickListener);
        mContentResolver =  mContext.getContentResolver(); 
        mMetroDataLoader = new MetroDataloader();
        mMetroDataLoader.addDefalutAppType(mContentResolver, mContext);
        mWinMetro = new WinMetroWindow(mContext, mAppTaskManager, mMetroDataLoader);
		
		mWinNavigationBarView.getHidebarButton().setOnTouchListener(mHideListener);

        //updateSearchPanel();
    }
	
    private View.OnTouchListener mHideListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            if(action == MotionEvent.ACTION_UP) {
                removeBar();
				return true;
            }
            return false;
        }
    };
	
    private void removeBar(){
        if (mBarIsAdd){
            if (mWinNavigationBarView != null)
                mWindowManager.removeViewImmediate(mWinNavigationBarView);
                       
            //if (mStatusBarWindow != null)
            //    mStatusBarWindow.setVisibility(View.GONE);
            //    mBarIsAdd = false;
            //}
        }
    }
	
    private BroadcastReceiver mIntentTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_TIME_TICK.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                    || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                updateSystemTime();
            }
        }
    };
	
	private void initSystemTime(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        mContext.registerReceiver(mIntentTimeReceiver, filter, null, null);
		mSystemTime = new SystemTime(mContext);
        updateSystemTime();
	}
	
	private void updateSystemTime(){
	    TextView tv = mWinNavigationBarView.getTimeView();
		if((null != tv)&&(null != mSystemTime)){
			mSystemTime.updateCalendar();
			tv.setText(mSystemTime.getFormatTime());
		}
		
	}

    private View.OnClickListener mRecentsClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            //awakenDreams();
            toggleRecentApps();
        }
    };

    private View.OnClickListener mWinStartClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            //awakenDreams();
            mWinMetro.show(null);
        }
    };

    private void removeNavigationBar(){
        if (mBarIsAdd){
            Log.d(TAG,"remove Bar");
            if (mWinNavigationBarView != null)
                mWindowManager.removeView(mWinNavigationBarView);
                mBarIsAdd = false;
                Toast.makeText(mContext, 
                    mContext.getResources().getString(R.string.hidebar_msg), 3000).show();
            }
    }

    private WindowManager.LayoutParams getNavigationBarLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR,
                    0
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);
        // this will allow the navbar to run in an overlay on devices that support this
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }

        lp.setTitle("NavigationBar");
        lp.windowAnimations = 0;
        return lp;
    }




    @Override
    public void addIcon(String slot, int index, int viewIndex, StatusBarIcon icon) {
    }

    @Override
    public void updateIcon(String slot, int index, int viewIndex, StatusBarIcon old,
            StatusBarIcon icon) {
    }

    @Override
    public void removeIcon(String slot, int index, int viewIndex) {
    }

    @Override
    public void addNotification(StatusBarNotification notification, RankingMap ranking) {
    }

    @Override
    protected void updateNotificationRanking(RankingMap ranking) {
    }

    @Override
    public void removeNotification(String key, RankingMap ranking) {
    }

    @Override
    public void disable(int state, boolean animate) {
    }

    @Override
    public void animateExpandNotificationsPanel() {
    }

    @Override
    public void animateCollapsePanels(int flags) {
    }

    @Override
    public void setSystemUiVisibility(int vis, int mask) {
        // ready to unhide
        if ((vis & View.STATUS_BAR_UNHIDE) != 0) {
            mSystemUiVisibility &= ~View.STATUS_BAR_UNHIDE;
        }
        if ((vis & View.NAVIGATION_BAR_UNHIDE) != 0) {
            mSystemUiVisibility &= ~View.NAVIGATION_BAR_UNHIDE;
        }
 
        // send updated sysui visibility to window manager
        notifyUiVisibilityChanged(mSystemUiVisibility);

    }

    @Override
    public void topAppWindowChanged(boolean visible) {
    }

    @Override
    public void setImeWindowStatus(IBinder token, int vis, int backDisposition,
            boolean showImeSwitcher) {
    }

    @Override
    public void showRecentApps(boolean triggeredFromAltTab) {
        int msg = MSG_SHOW_RECENT_APPS;
        mHandler.removeMessages(msg);
        mHandler.obtainMessage(msg, triggeredFromAltTab ? 1 : 0, 0).sendToTarget();
    }

    @Override
    public void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
        int msg = MSG_HIDE_RECENT_APPS;
        mHandler.removeMessages(msg);
        mHandler.obtainMessage(msg, triggeredFromAltTab ? 1 : 0,
                triggeredFromHomeKey ? 1 : 0).sendToTarget();
    }

    @Override
    public void toggleRecentApps() {
        int msg = MSG_TOGGLE_RECENTS_APPS;
        mHandler.removeMessages(msg);
        mHandler.sendEmptyMessage(msg);
    }

    @Override
    public void preloadRecentApps() {
        int msg = MSG_PRELOAD_RECENT_APPS;
        mHandler.removeMessages(msg);
        mHandler.sendEmptyMessage(msg);
    }

    @Override
    public void cancelPreloadRecentApps() {
        int msg = MSG_CANCEL_PRELOAD_RECENT_APPS;
        mHandler.removeMessages(msg);
        mHandler.sendEmptyMessage(msg);
    }


    @Override // CommandQueue
    public void setWindowState(int window, int state) {
    }

    @Override // CommandQueue
    public void buzzBeepBlinked() {
    }

    @Override // CommandQueue
    public void notificationLightOff() {
    }

    @Override // CommandQueue
    public void notificationLightPulse(int argb, int onMillis, int offMillis) {
    }
    
    @Override // CommandQueue
    public void addBar() {
        addNavigationBar();
    }

    @Override
    protected WindowManager.LayoutParams getSearchLayoutParams(
            LayoutParams layoutParams) {
        return null;
    }

    @Override
    protected void haltTicker() {
    }

    @Override
    protected void setAreThereNotifications() {
    }

    @Override
    protected void updateNotifications() {
    }

    @Override
    protected void tick(StatusBarNotification n, boolean firstTime) {
    }

    @Override
    protected void updateExpandedViewPos(int expandedPosition) {
    }

    @Override
    protected boolean shouldDisableNavbarGestures() {
        return true;
    }

    public View getStatusBarView() {
        return null;
    }

    @Override
    public void resetHeadsUpDecayTimer() {
    }

    @Override
    public void scheduleHeadsUpOpen() {
    }

    @Override
    public void scheduleHeadsUpEscalation() {
    }

    @Override
    public void scheduleHeadsUpClose() {
    }

    @Override
    protected int getMaxKeyguardNotifications() {
        return 0;
    }

    @Override
    public void animateExpandSettingsPanel() {
    }

    @Override
    protected void createAndAddWindows() {
        addStatusBarWindow();
    }

    @Override
    protected void refreshLayout(int layoutDirection) {
    }

    @Override
    public void onActivated(ActivatableNotificationView view) {
    }

    @Override
    public void onActivationReset(ActivatableNotificationView view) {
    }

    @Override
    public void showScreenPinningRequest() {
    }
	
    // add Dual SIM support
    @Override
    public void showSimSwitchUi(int type) {}

    @Override
    public void hideSimSwitchUi() {}
}

