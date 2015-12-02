package com.android.systemui.statusbar.oswin;

import com.android.systemui.R;

import android.util.Log;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.Notification;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class NotificationCenterLite{
    private final String TAG  = "NotificationCenterLite";
    public LinearLayout mLayout;
    private ImageView mBtnClear;
    private View mRoot;
    private Dialog mNotiCenter;
    private Context mContext;
    private static NotificationManager mNotificationManager;

    public NotificationCenterLite(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mNotiCenter = onCreateLayout(context);
 
        mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void clearViews(){
        mLayout.removeAllViews();
    }
    
    public void addView(View view){
        mLayout.addView(view);
    }

    public void clearNotifications() {
        if(null != mNotificationManager){
            mNotificationManager.cancelAll();
            Toast.makeText(mContext, "Clear All Notifacations", Toast.LENGTH_SHORT).show();
        }
    }

    private void startClickAnimation(View view){
        /*
                Animation ani_alpha = new AlphaAnimation(1.0f, 0.7f);
                ani_alpha.setDuration(100);
                ani_alpha.setRepeatMode(Animation.REVERSE);
                ani_alpha.setRepeatCount(2);*/
         
     
        Animation ani_scale = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                      Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
     
        ani_scale.setDuration(100);
        ani_scale.setRepeatMode(Animation.REVERSE);
        ani_scale.setRepeatCount(2);
         
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);       
        view.startAnimation(ani_scale);
    }



     private View.OnTouchListener mOnTouchForClearAll = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            if(action == MotionEvent.ACTION_UP){
                startClickAnimation(v);
                clearNotifications();
                return true;
            }else if(action == MotionEvent.ACTION_DOWN){
                startClickAnimation(v);
            }
            return false;
        }
    };
    
    public Dialog onCreateLayout(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        mRoot = layoutInflater.inflate(R.layout.dialog_notify_center, null);  
        mLayout   = (LinearLayout)mRoot.findViewById(R.id.motification_list);
        mBtnClear = (ImageView)mRoot.findViewById(R.id.btn_clearall);
        mBtnClear.setOnTouchListener(mOnTouchForClearAll);
        
        int preferWidth  = (int)context.getResources().getDimension(R.dimen.sys_dialog_width);
        int preferHeight = (int)context.getResources().getDimension(R.dimen.sys_dialog_height); 
        
        Dialog center = new Dialog(mContext,R.style.dialog);
        WindowManager.LayoutParams params = center.getWindow().getAttributes();
        params.width  = preferWidth;     
        params.height = preferHeight; //WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = 1;
        center.setContentView(mRoot);
        center.getWindow().setAttributes(params);
        center.getWindow().setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        center.getWindow().setType(2002);
        center.setCanceledOnTouchOutside(true); 
  
        return center;
    }

    public void notifyVisibleChildrenChanged(){
        Log.d(TAG, "notifyVisibleChildrenChanged...");
    }

    //$_FENG_$_INFO_$_Notification_Center
    public void openCenter(){
        if(mNotiCenter.isShowing()){
            mNotiCenter.dismiss();
        }
		Log.d(TAG, "addNotifyCenterBarLite...");
        mNotiCenter.show();
    }
    
    public void closeCenter(){
        if(mNotiCenter.isShowing()){
            mNotiCenter.dismiss();
        }
    }
}
