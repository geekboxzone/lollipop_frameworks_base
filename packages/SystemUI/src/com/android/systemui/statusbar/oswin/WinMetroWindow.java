package com.android.systemui.statusbar.oswin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.systemui.R;
import com.android.systemui.statusbar.oswin.data.AppTypeEntity;
import com.android.systemui.statusbar.oswin.data.LoaderManager;
import com.android.systemui.statusbar.oswin.data.LoaderManagerImpl;
import com.android.systemui.statusbar.oswin.view.CustomDialog;
import com.android.systemui.statusbar.oswin.view.ItemStyleCreater;
import com.android.systemui.statusbar.oswin.view.PopupMenu;
import com.android.systemui.statusbar.oswin.view.MenuItem;
import com.android.systemui.statusbar.oswin.view.MetroLayout;
import com.android.systemui.statusbar.oswin.view.UserView;
import com.android.systemui.statusbar.oswin.view.UserViewFactory;

import com.android.systemui.screenshot.blur.*;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.CursorLoader;
import android.content.pm.PackageManager;
import android.content.Loader;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class WinMetroWindow implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "WinMetroWindow";
    Context mContext; 
    DialogCallback mDialogcallback; 
    CustomDialog mDialog; 
    private AppTaskManager mTaskManager;
    private ListView mTypeListView;
    private ListView mCoreListView;
    private ListView mPopupMenu;
    private SimpleAdapter mTaskAdapter = null;
    public  MetroLayout mMetroLayout;
    private ItemStyleCreater mItemStyleCreater = null;
    private AnimatorSet mScaleAnimator;
    private View mOldFocusedView = null;
    private ImageButton mMetroPageLeft  = null;
    private ImageButton mMetroPageRight = null;
    private TextView    mPageIndicator = null;
	
    private ArrayList<HashMap<String, Object>> mTypeList= null;
    private ArrayList<HashMap<String, Object>> mCoreList= null;
    private final String mKey_icon = "key_icon";
    private final String mKey_name = "key_name";
	
    private int mRowIndex = 0;
    private int mCurCategory = AppTypeEntity.TABLE_TYPE_ALL;
	
    private static final int LOADER_ID = 1;
    private LoaderManager mLoaderManager;
    private LinearLayout mRootView;
    private MetroDataloader mDataLoader;
    private ImageView mAccountLogo;
    private ImageView mDragView;
    private MetroDragEventListener mDragEventListener;
    private TextView  mAccountName;
    private View mOldOpeView = null;
    private List<PackInfo> mTaskList;
    private int mMetroPageIndex = 0;
    private int mMetroItemCount = 0;

    private final static int POWER_SUB_CLOSE  = 0;
    private final static int POWER_SUB_SLEEP  = 1;
    private final static int POWER_SUB_REBOOT = 2;
    private final static int MAX_PAGE_SIZE = 12;
    private final static int DIRECTION_LFET  = 0;
    private final static int DIRECTION_RIGHT = 1;
    private final static int FADE_DURATION_TIME = 250;

    public WinMetroWindow(Context context, AppTaskManager taskManager, MetroDataloader dataLoader) { 
        this.mContext = context; 
        this.mDialog = new CustomDialog(mContext, R.style.dialog); 
		
        mRootView = (LinearLayout) LayoutInflater.from(mContext).inflate(
                R.layout.win_metro, null, false);
        this.mDialog.setContentView(mRootView);
        //this.mDialog.setContentView(R.layout.win_metro); 
        this.mDialog.setCancelable(true);
        this.mDialog.setCanceledOnTouchOutside(true);
		
        Window window = mDialog.getWindow();
        window.setGravity(Gravity.LEFT|Gravity.BOTTOM);
        WindowManager.LayoutParams params = window.getAttributes();
        params.type = 2002;       

        /*
           params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
           params.format = PixelFormat.TRANSLUCENT;
           params.flags |= 8;
           params.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
           params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
           params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        */
        params.width  = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT | Gravity.BOTTOM;
        params.x = 0;
        params.y = 0;
        window.setAttributes(params);
        this.mDialog.setCancelable(true);
        this.mDialog.setCanceledOnTouchOutside(true);

        this.mDialog.setOnDispatchEventListener(new CustomDialog.onDispatchEventListener(){
            @Override
            public boolean onDispatchEvent(MotionEvent event){
                if((null!=mPopupMenu)&&(mPopupMenu.getVisibility()== View.VISIBLE)){
                    mPopupMenu.setVisibility(View.INVISIBLE);
                    return true;
                }
                if((null!=mOldOpeView)&&(mOldOpeView.getVisibility()== View.VISIBLE)){
                    mOldOpeView.setVisibility(View.INVISIBLE);
                    return true;
                }
                return false;
            }
        });

        mTaskManager = taskManager;
        mDataLoader  = dataLoader;
        mItemStyleCreater = new ItemStyleCreater();
        mMetroLayout = (MetroLayout)mDialog.findViewById(R.id.metrolayout);
        mAccountLogo = (ImageView)mDialog.findViewById(R.id.account_logo);
        mAccountName = (TextView)mDialog.findViewById(R.id.account_name);
        mTypeListView  = (ListView)mDialog.findViewById(R.id.mListType);
        mCoreListView  = (ListView)mDialog.findViewById(R.id.mListSystem);

        mPageIndicator = (TextView)mDialog.findViewById(R.id.metro_page_indicator);
        mMetroPageLeft = (ImageButton)mDialog.findViewById(R.id.btn_metro_left);
        mMetroPageRight= (ImageButton)mDialog.findViewById(R.id.btn_metro_right);
        mMetroPageLeft.setOnClickListener(mOnBtnClick);
        mMetroPageRight.setOnClickListener(mOnBtnClick);
        
        //Typeface face = Typeface.createFromAsset(mContext.getAssets(), "fonts/simple.otf");
        //mAccountName.setTypeface(face);
        initTypeList();
        initCoreList();
		
        mTaskList = new ArrayList<PackInfo>();
        setMetroAppType(AppTypeEntity.TABLE_TYPE_ALL);
        mDragEventListener = new MetroDragEventListener();
        mDialog.getWindow().getDecorView().setOnDragListener(mDragEventListener);
    } 
	
    private void initLoadManager(){
        mLoaderManager = new LoaderManagerImpl(TAG, mDialog, true);
        mLoaderManager.initLoader(LOADER_ID, null, this);
    }

	private OnClickListener mOnBtnClick = new OnClickListener(){
		@Override
		public void onClick(View button) {
			// TODO Auto-generated method stub
			switch (button.getId()) {
			case R.id.btn_metro_left:
                turnMetroPage(DIRECTION_LFET);
				break;
            case R.id.btn_metro_right:
                turnMetroPage(DIRECTION_RIGHT);
                break;
			default:
				break;
			}
		}
	};

    

    public interface DialogCallback { 
        public void update(String string); 
    }
	
    public void setmDialogCallback(DialogCallback callback) { 
        this.mDialogcallback = callback; 
    } 

    public void show(Bitmap blurBg) {
        if((null != mRootView)&&(null != blurBg)){
            //int statusbarHeight = mContext.getResources().getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
            int navibarHeight = mContext.getResources().getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
            int w_left = mContext.getResources().getDimensionPixelSize(R.dimen.win_app_list_width);
            int w_right = mContext.getResources().getDimensionPixelSize(R.dimen.ITEM_NORMAL_SIZE);
            int height = mContext.getResources().getDimensionPixelSize(R.dimen.win_metro_height);
            Bitmap winStart = ImageUtil.CropImageForWinstart(blurBg, (w_left+w_right*4), height, navibarHeight);
            blurBg.recycle();
            mRootView.setBackground(new BitmapDrawable(winStart));
        }
        mDialog.show();
    } 
	
    public void hide() { 
        mDialog.hide(); 
    }

    public void dismiss() { 
        mDialog.dismiss(); 
    }

    public boolean isShowing(){
        return mDialog.isShowing();
    }
	
    private void initTypeList(){
    	mTypeList= new ArrayList<HashMap<String, Object>>();
    	String account_name = mDataLoader.queryAccountInfo(mContext);
    	if(account_name != null){
    		mAccountName.setText(account_name);
    	}
    	mDataLoader.queryAppTypeList(mContext,mTypeList);
    	
    	mTaskAdapter=new SimpleAdapter(mContext, mTypeList, R.layout.app_list_item, new String[]{mKey_name,mKey_icon}, new int []{R.id.tv_name,R.id.tv_icon});
    	
    	ViewBinder viewBinder = new ViewBinder(){
    		@Override
            public boolean setViewValue(View view, Object data,  String textRepresentation) { 
				if(view instanceof ImageView && data instanceof Drawable){ 
					ImageView iv=(ImageView)view; 
					iv.setImageDrawable((Drawable)data); 
					return true; 
				} 
				return false;
			} 
        }; 
    	mTaskAdapter.setViewBinder(viewBinder);
    	mTypeListView.setAdapter(mTaskAdapter);
    	mTypeListView.setClickable(true);
    	
    	mTypeListView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onItemClick(TypeList) index =" + arg2);
                HashMap<String, Object> meta = (HashMap<String, Object>)parent.getAdapter().getItem(arg2);
                if(null != meta){
                    Integer typeID = (Integer)meta.get(AppTypeEntity.KEY_TYPE_ID);
                    Log.d(TAG, "onItemClick(TypeList) index =" + typeID);
                    setMetroAppType(Integer.valueOf(typeID));
                }
             }
    	});
    	
        mTypeListView.setOnItemLongClickListener(new OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				if(arg1 instanceof FrameLayout){
					View ope = arg1.findViewById(R.id.item_operate);
					ope.setVisibility(View.VISIBLE);
					mOldOpeView = ope;
				}

				return false;
			}
		});

    }

    private void startAnimation(int typeid){        
        //ScaleAnimation animation =new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
        //                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        Animation fadeIn  = new AlphaAnimation(0.5f, 1.0f);
        Animation fadeOut = new AlphaAnimation(1.0f, 0.5f);
        AnimationSet aniset = new AnimationSet(true);
        aniset.addAnimation(fadeOut);
        aniset.addAnimation(fadeIn);
        aniset.setDuration(500);
        aniset.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        mMetroLayout.startAnimation(aniset);
    }

    private void showPopupWindow(View view){
    }
    
    public void hideCloudMenu() {
        mTaskAdapter.notifyDataSetChanged();
    }
	
    private void initCoreList(){
    	mCoreList = new ArrayList<HashMap<String, Object>>();
    	mDataLoader.queryCoreApp(mContext, mCoreList);
    	
    	mTaskAdapter=new SimpleAdapter(mContext, mCoreList, R.layout.app_list_item, new String[]{mKey_name,mKey_icon}, new int []{R.id.tv_name,R.id.tv_icon});
    	ViewBinder viewBinder = new ViewBinder(){
    		@Override
            public boolean setViewValue(View view, Object data,  String textRepresentation) {  
				if(view instanceof ImageView && data instanceof Drawable){ 
					ImageView iv=(ImageView)view; 
					iv.setImageDrawable((Drawable)data); 
					return true; 
				} 
				return false;
			} 
        }; 
    	mTaskAdapter.setViewBinder(viewBinder);
    	mCoreListView.setAdapter(mTaskAdapter);

      	mCoreListView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int index, long arg3) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onItemClick index =" + index);
                HashMap<String, Object> meta = (HashMap<String, Object>)parent.getAdapter().getItem(index);
                String pkg_name = (String)meta.get("mKey_pkg_name");
                if("com.android.power".equals(pkg_name)){
                    onPowerClicked(arg1);
                    return ;
                }
                try{
                    startAppWithName(pkg_name);
                }catch(Exception ev){
                    String msg = mContext.getResources().getString(R.string.fail_open_app);
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                }
            }
    	});
    }

    public void onPowerClicked(View view){
        mPopupMenu = (ListView)mDialog.findViewById(R.id.power_menu);
        PopupMenu menu = new PopupMenu(mContext, mPopupMenu);
        //menu.setHeaderTitle("TitleTitleTitleTitleTitleTitle");
        menu.setOnItemSelectedListener(mOnMenuItemSelectedListener);
        //menu.add(POWER_SUB_CLOSE, R.string.power_sub_close).setIcon(null);
        menu.add(POWER_SUB_CLOSE,  R.string.power_sub_close);
        menu.add(POWER_SUB_SLEEP,  R.string.power_sub_sleep);
        menu.add(POWER_SUB_REBOOT, R.string.power_sub_reboot);
        menu.show();
    }

    PopupMenu.OnItemSelectedListener mOnMenuItemSelectedListener = new PopupMenu.OnItemSelectedListener(){
        @Override
        public void onItemSelected(MenuItem item){
            Intent intent = null;
            PowerManager power = null;
            
            switch(item.getItemId()) {
                case POWER_SUB_CLOSE:
                    Log.d(TAG, "onItemSelected POWER_SUB_CLOSE");
                    intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
                    intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    break;
                case POWER_SUB_SLEEP:
                    Log.d(TAG, "onItemSelected POWER_SUB_SLEEP");
                    power = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                    if(null != power){
                        power.goToSleep(10);
                    }
                    break;
                case POWER_SUB_REBOOT:
                    Log.d(TAG, "onItemSelected POWER_SUB_REBOOT");
                    power = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                    if(null!=power){
                        power.reboot("POWER_SUB_REBOOT");
                    }else{
                        intent = new Intent(Intent.ACTION_REBOOT);
                        intent.putExtra("nowait", 1);
                        intent.putExtra("interval", 1);
                        intent.putExtra("window", 0);
                        mContext.sendBroadcast(intent);
                    }
                    break;
               default:
                    break;
            }
        }
    };
   
    public View addView(View view, int celltype, int row){	
    	return mMetroLayout.addItemView(view, celltype, row);
    }

    public View addView(View view, int celltype, int row, int padding){
        return mMetroLayout.addItemView(view, celltype, row, padding);
    }

    public ArrayList<View> createUserView(){
        return UserViewFactory.getInstance().createUserView(mContext);
    }

    public void updateMetroPage(List<PackInfo> appList){
     	int count = 0;
    	mRowIndex = 0;
    	int itemIdx = 0;

    	ArrayList<View> views = createUserView();
    	mMetroLayout.clearItems();
    	mMetroLayout.invalidate();

    	int[] rowPattern = mItemStyleCreater.getItemRandType();
    	for(PackInfo task: appList){
    		View item = views.get(count);
    		if(rowPattern[itemIdx]==MetroLayout.Invalid){
    			rowPattern = mItemStyleCreater.getItemRandType();
    			mRowIndex++;
    			itemIdx = 0;
    		}
    		if(mRowIndex==4){break;}
    		if((item instanceof UserView)){
    			UserView uv = (UserView)item;
    			uv.setLogo(task.getIcon());
    			uv.setTile(task.getAppName());
    			//item.setBackground(mItemStyleCreater.getItemRandColor());
    			if(rowPattern[itemIdx] == MetroLayout.Horizontal){
    				if(task.getActivityName().startsWith("com.android.music")){
    					item.setBackgroundResource(R.drawable.app_bg_music);
    				}
    				if(task.getActivityName().startsWith("com.android.gallery3d")){
    					item.setBackgroundResource(R.drawable.app_bg_photo);
    				}
    			}
        	    addView(item,  rowPattern[itemIdx], mRowIndex, UserViewFactory.getInstance().getPadding(mContext));
                uv.setBackground(mItemStyleCreater.getItemRandColor());
        		uv.setClickable(true);
        		uv.setTag(Integer.valueOf(count));
        		uv.setOnClickListener(mMetroItemClickListener);
        		uv.setOnLongClickListener(new OnLongClickListener(){
					@Override
					public boolean onLongClick(View item) {
						// TODO Auto-generated method stub
						return onDragStart(item);
					}
        			
        		});
        		itemIdx++;
        		count++;
    		}
    	}
    }

    private void updateMetroPageIndicator(int type_id, int page_index){
        // TODO Auto-generated method stub
        int resid = 0;
        switch(type_id){
            case AppTypeEntity.TABLE_TYPE_ALL:
                resid = R.string.app_type_all;
                break;
            case AppTypeEntity.TABLE_TYPE_SYSTEM:
                resid = R.string.app_type_system;
                break;
            case AppTypeEntity.TABLE_TYPE_OFFICE:
                resid = R.string.app_type_office;
                break;
            case AppTypeEntity.TABLE_TYPE_GAME:
                resid = R.string.app_type_game;
                break;
            case AppTypeEntity.TABLE_TYPE_MEDIA:
                resid = R.string.app_type_media;
                break;
            case AppTypeEntity.TABLE_TYPE_OTHER:
                resid = R.string.app_type_other;
                break;
        }
        String typename = "Unkown"; 
        if(resid != 0){
            typename = mContext.getResources().getString(resid);
        }
        int max_page = mMetroItemCount/MAX_PAGE_SIZE + 1;
        if(page_index > max_page){page_index =max_page;}
        if(mMetroPageIndex!=0){
            mPageIndicator.setText(typename + String.format("(%d/%d)", page_index,max_page));
        }else{
            mPageIndicator.setText(typename);
        }
    }

    public void loadMetroPage(int type_id, int page_index){
    	int count = 0;
    	mRowIndex = 0;
    	int itemIdx = 0;
    	if((type_id-AppTypeEntity.TABLE_TYPE_BASE)<0){
    		return ;
    	}
    	
        mTaskList.clear();
        if(page_index > 0){
            mDataLoader.queryAppItemByPage(mContext, type_id, page_index, MAX_PAGE_SIZE, mTaskList); 
        } 
        Log.d(TAG, "loadMetroPage page_index = " + page_index);
        Log.d(TAG, "loadMetroPage type_id    = " + type_id);
        updateMetroPage(mTaskList);
        updateMetroPageIndicator(type_id, page_index);
    }

    public void setMetroAppType(final int type_id){
        mMetroItemCount = mDataLoader.queryAppItemCount(mContext, type_id);
        mMetroPageIndex = 0;
        if(mMetroItemCount>0){
            mMetroPageIndex = 1;
        }
        mCurCategory = type_id;

        //DumpPage Info
        Log.d(TAG, "setMetroAppType mMetroItemCount=" + mMetroItemCount);
        Log.d(TAG, "setMetroAppType mMetroPageIndex=" + mMetroPageIndex);
        Log.d(TAG, "setMetroAppType mCurCategory   =" + mCurCategory);
        
        //FadeOut and FadeIn Animation
        Animation fadeOut = new AlphaAnimation(1.0f, 0.4f);
        final Animation fadeIn  = new AlphaAnimation(0.4f, 1.0f);
        fadeOut.setDuration(FADE_DURATION_TIME);
        fadeIn.setDuration(FADE_DURATION_TIME);
        if(mMetroLayout.startFadeout(fadeOut)==0){
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    fadeIn.reset();
                    mMetroLayout.startAnimation(fadeIn);
                    loadMetroPage(type_id, mMetroPageIndex);
                }
            });
        }else{
            if(mMetroPageIndex>0){
                fadeIn.reset();
                mMetroLayout.startAnimation(fadeIn);
            }
            loadMetroPage(type_id, mMetroPageIndex);
       }
    }

 
    private void startFadeAnimation(final int typeid){        
        //ScaleAnimation animation =new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
        //                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        Animation fadeOut = new AlphaAnimation(1.0f, 0.4f);
        fadeOut.setDuration(FADE_DURATION_TIME);
        mMetroLayout.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation fadeIn  = new AlphaAnimation(0.4f, 1.0f);
                fadeIn.setDuration(FADE_DURATION_TIME);
                mMetroLayout.startAnimation(fadeIn);
                Log.d(TAG, "setMetroAppType index =" + typeid);
                setMetroAppType(typeid);
            }
        });
    }

    public int turnMetroPage(int direction){
        if(mMetroItemCount == 0){
            mMetroPageIndex = 0;
            return -1;
        }
        int max_page = mMetroItemCount/MAX_PAGE_SIZE + 1;
        int new_index = mMetroPageIndex;
        if(direction == DIRECTION_LFET){
            if(new_index>1){
                new_index--;
            }
        }
        if(direction == DIRECTION_RIGHT){
            if(new_index < max_page){
                new_index++;
            }
        }
        Log.d(TAG, "turnMetroPage " + String.format("new_index=%d, old_index=%d",new_index, mMetroPageIndex));
        if(new_index != mMetroPageIndex){
            mMetroPageIndex = new_index;
            loadMetroPage(mCurCategory, mMetroPageIndex);
            return 0;
        }
        
        return -1;
    }
    
    public OnClickListener mMetroItemClickListener = new OnClickListener(){
		@Override
		public void onClick(View item) {
			// TODO Auto-generated method stub
			item.requestFocus();
			
	    	if(mScaleAnimator!=null) mScaleAnimator.end();
	    	if(mOldFocusedView != null){
	    		mOldFocusedView.setScaleX(1.0f);
	    		mOldFocusedView.setScaleY(1.0f);	    		
	    	}

	    	mOldFocusedView = item;        
	    	mMetroLayout.bringChildToFront(item);
	    	mMetroLayout.invalidate();
	    	ObjectAnimator animX = ObjectAnimator.ofFloat(item, "ScaleX", 
	            			new float[] { 1.0F, 1.1F }).setDuration(100);
	    	ObjectAnimator animY = ObjectAnimator.ofFloat(item, "ScaleY", 
	            			new float[] { 1.0F, 1.1F }).setDuration(100);
	    	ObjectAnimator animX_back = ObjectAnimator.ofFloat(item, "ScaleX", 
        			        new float[] { 1.1F, 1.0F }).setDuration(100);
	    	ObjectAnimator animY_back = ObjectAnimator.ofFloat(item, "ScaleY", 
        			        new float[] { 1.1F, 1.0F }).setDuration(100);
	    	mScaleAnimator = new AnimatorSet();
	    	mScaleAnimator.playTogether(new Animator[] { animX, animY,animX_back,animY_back});
	    	mScaleAnimator.start();
	    	
	    	int index = (int)item.getTag();
            if((index>=0)&&(index<mTaskList.size())){
                startAppWithName(mTaskList.get(index).getPackageName());
            }
		}
	};

    public void startAppWithName(String packageName){
        dismiss();
        PackageManager pm = mContext.getApplicationContext().getPackageManager();
        Intent intent=new Intent(); 
        intent =pm.getLaunchIntentForPackage(packageName);
        if(intent != null){
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP );
            mContext.startActivity(intent);
        }
    }
	
    private int mDragItemIndex = -1;
	private boolean onDragStart(View view){
        Log.d(TAG, "onDragStart index=" + (Integer)view.getTag());		
		for(int index = 0; index < mTypeListView.getCount(); index++){
			mTypeListView.getChildAt(index).setOnDragListener(mDragEventListener);
			mTypeListView.getChildAt(index).setTag(Integer.valueOf(index));
		}
		
		ClipData.Item item = new ClipData.Item((CharSequence)view.getTag().toString());
		String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
		
		mDragItemIndex = (Integer)view.getTag();
           
		ClipData dragData = new ClipData(view.getTag().toString(),mimeTypes, item);
		View.DragShadowBuilder myShadow = new MetroItemDragShadowBuilder(view);
           
		view.startDrag(dragData,myShadow,null,0);
		return true;		
	}
	
    @SuppressLint("NewApi")
    private static class MetroItemDragShadowBuilder extends View.DragShadowBuilder {
        private static Drawable mShadow;
        //
        public MetroItemDragShadowBuilder(View item) {
            super(item);
            ImageView logo = (ImageView)item.findViewById(R.id.metro_item_logo);
            mShadow = logo.getDrawable();
            //mShadow = new ColorDrawable(Color.YELLOW);
        }

        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
        	if(null != mShadow){
	            int width  = mShadow.getIntrinsicWidth();
	            int height = mShadow.getIntrinsicHeight();
	            mShadow.setBounds(0, 0, width, height);
	            size.set(width, height);
	            touch.set(width, height);
        	}
        }


        @Override
        public void onDrawShadow(Canvas canvas) {
        	mShadow.draw(canvas);
        }
    }
	
    private void onAppItemDrop(int position){
        PackInfo detail = mTaskList.get(position);
        Bitmap bitmap = ((BitmapDrawable)detail.getIcon()).getBitmap();
        if(null != bitmap){
            LauncherShortcut.addShortcutForApp(mContext, detail.getAppName(), bitmap,
			detail.getPackageName(), detail.getActivityName());
            String format = mContext.getResources().getString(R.string.drag_and_drop_ended);
            Toast.makeText(mContext, String.format(format, detail.getAppName()), Toast.LENGTH_SHORT).show();
        }
    }

    private void setViewColor(View view, int color){
        if(view.getHeight() < 200){
            view.setBackgroundColor(color);
        }
    }
    
	private float mOldDragPosx = 1.0f;
    private int mOldDragStart,mOldDragDrop;
    @SuppressLint("NewApi")
    protected class MetroDragEventListener implements OnDragListener {
        @SuppressLint("ShowToast")
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();
            HashMap<String, Object> meta = null;
            int diff = 0;

            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    mOldDragStart = (int)event.getX();
                    mOldDragDrop = -1;
                	//Toast.makeText(mContext, "DragEvent.ACTION_DRAG_STARTED", Toast.LENGTH_SHORT).show();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    setViewColor(v, Color.YELLOW);
                	//Toast.makeText(mContext, "DragEvent.ACTION_DRAG_ENTERED", Toast.LENGTH_SHORT).show();
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    mOldDragDrop = (int)event.getX();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    mOldDragDrop = (int)event.getX();
                    setViewColor(v, Color.TRANSPARENT);
                	//Toast.makeText(mContext, "DragEvent.ACTION_DRAG_EXITED", Toast.LENGTH_SHORT).show();
                    return true;
                case DragEvent.ACTION_DROP:
                    setViewColor(v, Color.TRANSPARENT);
                    mOldDragDrop = (int)event.getX();
                    if(null == v.getTag()){
                        return false;
                    }
                	meta = (HashMap<String, Object>)mTypeListView.getAdapter().getItem((Integer)v.getTag());
    				Integer typeID = (Integer)meta.get(AppTypeEntity.KEY_TYPE_ID);
                	updateCategoryForApp(mDragItemIndex, typeID);
                	setMetroAppType(mCurCategory);
                	//Toast.makeText(mContext, "DragEvent.ACTION_DROP"+mDragItemIndex, Toast.LENGTH_SHORT).show();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    setViewColor(v, Color.TRANSPARENT);
                    diff = mOldDragDrop-mOldDragStart;
                    mOldDragDrop = mOldDragStart = -1;
                    if(diff > 300){
                        onAppItemDrop(mDragItemIndex);
                        return true;                       
                    }
                	//Toast.makeText(mContext, "DragEvent.ACTION_DRAG_ENDED", Toast.LENGTH_SHORT).show();
                    return true;
                default:
                	//Toast.makeText(mContext, "Unknown Action", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        };
    }
    
    public void updateCategoryForApp(int appID, int category){
    	mDataLoader.updateAppItem(mContext.getContentResolver(), category, mTaskList.get(appID));
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		
	}
}
