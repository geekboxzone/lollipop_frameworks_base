package com.android.systemui.statusbar.oswin.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import com.android.systemui.R;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MetroLayout extends FrameLayout implements View.OnFocusChangeListener{
    public static final int Invalid    = -1; //square rectangle
    public static final int Vertical   = 0; //occupy two vertical cells
    public static final int Horizontal = 1; //occupy two horizontal cells
    public static final int Normal     = 2; //square rectangle
    private static final String TAG = "WinStartMetro";
    
    Context mContext;
    int[] rowOffset = new int[4];
    static  int DIVIDE_SIZE = 6;
    boolean mMirror = false;
    AnimatorSet mScaleAnimator;
    List<WeakReference<View>> mViewList = new ArrayList<WeakReference<View>>();

    View mLeftView;
    View mRightView;
    private View lastFocusedView;
	
    float mDensityScale = 1.0f;
    private static int ITEM_V_WIDTH  = -1;
    private static int ITEM_V_HEIGHT = -1;
    private static int ITEM_H_WIDTH  = -1;
    private static int ITEM_H_HEIGHT  = -1;
    private static int ITEM_NORMAL_SIZE = -1;

    //Highlight and Click Animation
    private Rect mTempRect = new Rect();
    private View mMotionTarget;
    private ScaleAnimation mScaleUp;
    private ScaleAnimation mScaleDown;


    public class Item{
        public Item( int type, int row){
            mType = type;
            mRow = row;
        }
        public int mType;
        public int mRow;
    }
	
    public MetroLayout(Context context) {
        super(context);
        mContext = context;
        init();
    }
	
    public MetroLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }
	
    private void init(){
        if(ITEM_V_WIDTH == -1){
            DIVIDE_SIZE   = getResources().getDimensionPixelSize(R.dimen.ITEM_DIVIDE_SIZE);
            ITEM_V_WIDTH  = getResources().getDimensionPixelSize(R.dimen.ITEM_V_WIDTH);
            ITEM_V_HEIGHT = getResources().getDimensionPixelSize(R.dimen.ITEM_V_HEIGHT);
            ITEM_H_WIDTH  = getResources().getDimensionPixelSize(R.dimen.ITEM_H_WIDTH);
            ITEM_H_HEIGHT = getResources().getDimensionPixelSize(R.dimen.ITEM_H_HEIGHT);
            ITEM_NORMAL_SIZE = getResources().getDimensionPixelSize(R.dimen.ITEM_NORMAL_SIZE);
        }

        mDensityScale = 1;//mContext.getResources().getDisplayMetrics().densityDpi/320.0f;
        setClipChildren(false);
        setClipToPadding(false);
    }

    public View getItemView(int index){
        if(index>=mViewList.size()) return null;
        return mViewList.get(index).get();
    }

    public View addItemView(View child, int celltype , int row){
        return addItemView(child, celltype , row, DIVIDE_SIZE);
    }

    public int startFadeout(Animation ani_alpha){
        View child = null;
        Log.e(TAG,"startFadeout");
        if(0 == mViewList.size()){
            ani_alpha.cancel();
            ani_alpha.reset();
            return -1;
        }
        for(int i = 0; i < mViewList.size(); i++){
            child = getItemView(i);
            if(null != child){
                child.setAlpha(1.0f);      
                child.startAnimation(ani_alpha);
            }
        }
        return 0;
    }

    public void clearItems(){
        removeAllViews();
        rowOffset[3]=rowOffset[2]=rowOffset[1]=rowOffset[0]=0;
        mViewList.clear();
        mLeftView = null;
        mRightView = null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        int action = ev.getAction();

        switch (action)
        {
        case MotionEvent.ACTION_DOWN:
            if(null != mMotionTarget){
                mMotionTarget.setScaleX(1.0f);
                mMotionTarget.setScaleY(1.0f);
            }
            checkScaleAnimation();
            mMotionTarget = findSelectedView(ev);
            if(null != mMotionTarget){
                mMotionTarget.startAnimation(mScaleUp);
            }
            break;
        case MotionEvent.ACTION_MOVE:
             break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            if(null != mMotionTarget){
                mMotionTarget.startAnimation(mScaleDown);
                mMotionTarget = null;
            }
            break;

        default:
            break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void checkScaleAnimation(){
        if(null==mScaleUp){
            mScaleUp = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        }
        if(null==mScaleDown){
            mScaleDown = new ScaleAnimation(1.1f, 1.0f, 1.1f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        }
        mScaleUp.reset();
        mScaleUp.setDuration(100);
        mScaleDown.reset();
        mScaleDown.setDuration(100);
    }

    private View findSelectedView(MotionEvent ev){
        final int count = this.getChildCount();
        final Rect frame = mTempRect;

        final int scrolledXInt = (int)ev.getX();
        final int scrolledYInt = (int)ev.getY();

        for (int i = count - 1; i >= 0; i--) {
            final View child = this.getChildAt(i);
            if ((child.getVisibility() == View.VISIBLE)
                        || child.getAnimation()!= null) {
                child.getHitRect(frame);
                if (frame.contains(scrolledXInt, scrolledYInt)) {
                    return child;
                }
            }
        }
        return null;
    }

    public View addItemView(View child, int celltype , int row, int padding){
        child.setAlpha(1.0f);  
        Log.e(TAG,"addItemView row = " + row);
        if(mLeftView==null){
            mLeftView = child;
        }
        if(row==0) {
            mRightView = child;
        }
        child.setFocusable(true);
        child.setOnFocusChangeListener(this);
        LayoutParams flp;

        child.setTag(mViewList.size());
        mViewList.add(new WeakReference<View>(child));
        View result = child;
        switch(celltype){
            case Vertical:
                flp = new LayoutParams(
                   (int)(ITEM_V_WIDTH*mDensityScale),
                   (int)(ITEM_V_HEIGHT*mDensityScale));
			child.setFocusable(true);
			child.setOnFocusChangeListener(this);
            child.setTag(R.integer.tag_view_postion, 0);
            flp.leftMargin = rowOffset[0];
    	    flp.topMargin = getPaddingTop();
    		flp.rightMargin = getPaddingRight();
			addView(child, flp);
			rowOffset[0]+=ITEM_V_WIDTH*mDensityScale+padding;
			rowOffset[1]=rowOffset[0];
			rowOffset[2]=rowOffset[0];
			rowOffset[3]=rowOffset[0];
			break;
		case Horizontal:
			flp = new LayoutParams((int)(ITEM_H_WIDTH*mDensityScale), (int)(ITEM_H_HEIGHT*mDensityScale));
			switch(row){
			case 0:
				flp.leftMargin = rowOffset[0];
				flp.topMargin = getPaddingTop();
				flp.rightMargin = getPaddingRight();
				child.setFocusable(true);
				child.setOnFocusChangeListener(this);
                child.setTag(R.integer.tag_view_postion, 0);
				addView(child,flp);
				rowOffset[0]+=ITEM_H_WIDTH*mDensityScale+padding;
				break;
			default:
				child.setFocusable(true);
				child.setOnFocusChangeListener(this);
                child.setTag(R.integer.tag_view_postion, 1);
				flp.leftMargin = rowOffset[row];
				flp.topMargin = getPaddingTop();
				flp.rightMargin = getPaddingRight();
				flp.topMargin += row*(ITEM_NORMAL_SIZE*mDensityScale+padding);
				addView(child,flp);
				rowOffset[row]+=ITEM_H_WIDTH*mDensityScale+padding;
				break;
			}
			break;
		case Normal:
			flp = new LayoutParams(
					(int)(ITEM_NORMAL_SIZE*mDensityScale),
					(int)(ITEM_NORMAL_SIZE*mDensityScale));
			switch(row){
			case 0:
				flp.leftMargin = rowOffset[0];
				child.setFocusable(true);
				child.setOnFocusChangeListener(this);
                child.setTag(R.integer.tag_view_postion, 0);
    			flp.topMargin = getPaddingTop();
    			flp.rightMargin = getPaddingRight();
				addView(child,flp);
				rowOffset[0]+=ITEM_NORMAL_SIZE*mDensityScale+padding;
				break;
			default:
				child.setFocusable(true);
                child.setTag(R.integer.tag_view_postion, 1);
			    child.setOnFocusChangeListener(this);
				flp.leftMargin = rowOffset[row];
				flp.topMargin = getPaddingTop();
				flp.rightMargin = getPaddingRight();
				flp.topMargin += row*(ITEM_NORMAL_SIZE*mDensityScale+padding);
				addView(child,flp);
				rowOffset[row]+=ITEM_NORMAL_SIZE*mDensityScale+padding;
				break;
			}
			break;
		}
		return result;
	}
    
    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (lastFocusedView!=null&&lastFocusedView.requestFocus(direction, previouslyFocusedRect)) {
            return true;
        }

        int index;
        int increment;
        int end;
        int count = this.getChildCount();
        if ((direction & FOCUS_FORWARD) != 0) {
            index = 0;
            increment = 1;
            end = count;
        } else {
            index = count - 1;
            increment = -1;
            end = -1;
        }

        for (int i = index; i != end; i += increment) {
            View child = this.getChildAt(i);
            {
                if (child.requestFocus(direction, previouslyFocusedRect)) {
                    return true;
                }
            }
        }
        return false;
    }
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child,focused);
    }
    
    public void onFocusChange(final View v, boolean hasFocus){
    	if(mScaleAnimator!=null) mScaleAnimator.end();
    	if(hasFocus){
    	    lastFocusedView = v;        
    	    bringChildToFront(v);
            invalidate();
    	    ObjectAnimator animX = ObjectAnimator.ofFloat(v, "ScaleX", 
            	new float[] { 1.0F, 1.1F }).setDuration(200);
    	    ObjectAnimator animY = ObjectAnimator.ofFloat(v, "ScaleY", 
            	new float[] { 1.0F, 1.1F }).setDuration(200);
    	    mScaleAnimator = new AnimatorSet();
    	    mScaleAnimator.playTogether(new Animator[] { animX, animY });
    	    mScaleAnimator.start();
    	}else{    	   
            v.setScaleX(1.0f);
    	    v.setScaleY(1.0f);
       } 
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Handle automatic focus changes.
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int direction = 0;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (event.hasNoModifiers()) {
                        direction = View.FOCUS_LEFT;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (event.hasNoModifiers()) {
                        direction = View.FOCUS_RIGHT;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (event.hasNoModifiers()) {
                        direction = View.FOCUS_UP;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (event.hasNoModifiers()) {
                        direction = View.FOCUS_DOWN;
                    }
                    break;
                case KeyEvent.KEYCODE_TAB:
                    if (event.hasNoModifiers()) {
                        direction = View.FOCUS_FORWARD;
                    } else if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
                        direction = View.FOCUS_BACKWARD;
                    }
                    break;
            }
            if (direction == View.FOCUS_DOWN || direction == View.FOCUS_UP) {
                View focused = findFocus();
                if (focused != null) {
                    View v = focused.focusSearch(direction);
                    if (v == null) {
                        //Utils.playKeySound(this, Utils.SOUND_ERROR_KEY);
                    }
                }
            }
        }
        boolean ret = super.dispatchKeyEvent(event);
        return ret;
    }

    public void focusMoveToLeft(){
        mLeftView.requestFocus();
    }

    public void focusMoveToRight(){
        mRightView.requestFocus();
    }

    public void focusMoveToPreFocused(){
        if(lastFocusedView!=null){
            lastFocusedView.requestFocus();
        }else {
            mLeftView.requestFocus();
        }
    }
}
