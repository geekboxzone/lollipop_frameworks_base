package com.android.systemui.statusbar.oswin.view;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.systemui.R;

public class UserView extends FrameLayout{
	private FrameLayout mContainer;
	protected ImageView mLogo;
	protected TextView mItemTitleView;
	protected TextView mItemSummaryView;
    private ImageView mColorBg;

	public UserView(Context context) {
		this(context, null, 0);
	}

	public UserView(Context context, AttributeSet as) {
		this(context, as, 0);
	}

	public UserView(Context context, AttributeSet as, int uiStyle) {
		super(context, as, uiStyle);

		init(getContext());
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init(getContext());
	}

    @SuppressWarnings("deprecation")
	public void setBackground(Drawable background) {
        if(Build.VERSION.SDK_INT >= 16){
        	mContainer.setBackground(background);
        }
        mColorBg.setImageDrawable(background);
		//mColorBg.setBackground(background);
	}

	public void setBackground(int resid) {
		mContainer.setBackgroundColor(resid);
	}

	public void setLogo(int resid) {
		mLogo.setVisibility(View.VISIBLE);
		mLogo.setImageResource(resid);
	}

	public void setLogo(Drawable logo) {
		mLogo.setVisibility(View.VISIBLE);
		mLogo.setImageDrawable(logo);
	}
	
	public void setTile(String title) {
		mItemTitleView.setText(title);
	}
	
	public void setSummary(String summary) {
		mItemSummaryView.setText(summary);
	}

	private void init(Context context) {
		this.removeAllViews();

		mContainer = (FrameLayout)LayoutInflater.from(context).inflate(R.layout.metro_item, null);
		FrameLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
        mColorBg = (ImageView) mContainer.findViewById(R.id.metro_bg);
		mLogo = (ImageView) mContainer.findViewById(R.id.metro_item_logo);
		mItemTitleView = (TextView) mContainer.findViewById(R.id.metro_item_title);
		mItemSummaryView = (TextView) mContainer.findViewById(R.id.metro_item_summary);
        addView(mContainer, lp);
	}
}