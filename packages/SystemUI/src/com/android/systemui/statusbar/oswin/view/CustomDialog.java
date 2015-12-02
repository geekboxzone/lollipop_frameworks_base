package com.android.systemui.statusbar.oswin.view;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;

public class CustomDialog extends Dialog{
	private onDispatchEventListener mDispatchListener;

	public CustomDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public CustomDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	protected CustomDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

    public interface onDispatchEventListener{
    	public boolean onDispatchEvent(MotionEvent event);
    }
    
    public void setOnDispatchEventListener(onDispatchEventListener del){
    	mDispatchListener = del;
    }

	@Override
	public boolean dispatchTouchEvent(MotionEvent event){
		switch(event.getAction()){
		    case MotionEvent.ACTION_UP:
				if((null != mDispatchListener)&&mDispatchListener.onDispatchEvent(event)){
					return true;
			    }
		        break;
		    default:
		    	break;
		}
		return super.dispatchTouchEvent(event);
	}
}
