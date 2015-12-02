package com.android.systemui.statusbar.oswin.view;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.GradientDrawable;
import android.util.Log;


/*
 ** S for Square; R for rectangle
 ** (S-S-S-S)(R---R--)(S-R---S)(R---S-S)(S-S-R---)
 */


public class ItemStyleCreater {
	public static final String TAG = "ItemStyleCreater";
	private ColorPattern mColorPattern;
	private RowPattern   mRowPattern;
	public ItemStyleCreater(){
		mRowPattern   = new RowPattern();
		mColorPattern = new ColorPattern();	
	}
	
	public int[] getItemRandType(){
		return mRowPattern.getRandType();
	}
	
	public int getItemRandColor(){
		return mColorPattern.getRandColor();
	}
	
	private class ColorPattern{
		private int mRandBase  = 0;
		private List<GradientDrawable> mColors;
        private List<Integer> mColorTable;
 	    
		public ColorPattern(){
			mColors = new ArrayList<GradientDrawable>();
            mColorTable = new ArrayList<Integer>();
            mColorTable.add(0xff36dc65);
            mColorTable.add(0xffe8ca01);
            mColorTable.add(0xffe029ad);
            mColorTable.add(0xfff01e41);
            mColorTable.add(0xff01ccd9);
            mColorTable.add(0xff3766bf);
			mRandBase = (int)(Math.random()*mColorTable.size());
			//mColors.add(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xff36dc65, 0xff3bc261}));
			//mColors.add(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xffe8ca01, 0xffd3b802}));
			//mColors.add(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xffe029ad, 0xffc5219e}));
			//mColors.add(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xfff01e41, 0xffcf2541}));
            //mColors.add(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xff01ccd9, 0xff00b1bb}));
            //mColors.add(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xff3766bf, 0xff31559a}));
			//mRandBase = (int)(Math.random()*mColors.size());
		}

        public int getRandColor(){
 			mRandBase++;
			if(mRandBase >= mColors.size()){
				mRandBase = 0;
				reset();
			}
			return mColorTable.get(mRandBase);
        }
		/*public GradientDrawable getRandColor(){
			mRandBase++;
			if(mRandBase >= mColors.size()){
				mRandBase = 0;
				reset();
			}
			return mColors.get(mRandBase);
		}*/
		
		public void reset(){
			mRandBase = (int)(Math.random()*mColorTable.size());
		}
	}
	private class RowPattern{
		private int[][] mPatternArray;
		private int mRandBase = 0;
		private int mRandIndex = 0;
		private final int MAX_PATTERN = 4;
		public RowPattern(){
			mPatternArray = new int[MAX_PATTERN][5];
			mRandBase = (int)(Math.random()*MAX_PATTERN);
			//(S-S-S-S) pattern -> 1
			mPatternArray[0][0] = MetroLayout.Normal;
			mPatternArray[0][1] = MetroLayout.Normal;
			mPatternArray[0][2] = MetroLayout.Normal;
			mPatternArray[0][3] = MetroLayout.Normal;
			mPatternArray[0][4] = MetroLayout.Invalid;
			//(R---R--) pattern -> 2
			mPatternArray[1][0] = MetroLayout.Horizontal;
			mPatternArray[1][1] = MetroLayout.Horizontal;
			mPatternArray[1][2] = MetroLayout.Invalid;
			mPatternArray[1][3] = MetroLayout.Invalid;
			mPatternArray[1][4] = MetroLayout.Invalid;
			//(R---S-S) pattern -> 4
			mPatternArray[2][0] = MetroLayout.Horizontal;
			mPatternArray[2][1] = MetroLayout.Normal;
			mPatternArray[2][2] = MetroLayout.Normal;
			mPatternArray[2][3] = MetroLayout.Invalid;
			mPatternArray[2][4] = MetroLayout.Invalid;
			//(S-S-R--) pattern -> 5
			mPatternArray[3][0] = MetroLayout.Normal;
			mPatternArray[3][1] = MetroLayout.Normal;
			mPatternArray[3][2] = MetroLayout.Horizontal;
			mPatternArray[3][3] = MetroLayout.Invalid;
			mPatternArray[3][4] = MetroLayout.Invalid;
		}
		
		public int[] getRandType(){
			mRandBase++;
			if(mRandBase >= MAX_PATTERN){
				mRandBase = 0;
			}
			return mPatternArray[mRandBase];
		}
		
		public void reset(){
			mRandIndex = 0;
			mRandBase = (int)(Math.random()*MAX_PATTERN);
		}
	}

}
