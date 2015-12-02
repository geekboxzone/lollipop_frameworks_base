package com.android.systemui.statusbar.oswin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import com.android.systemui.R;

public class SystemTime {
	private static int mYear;  
	private static int mMonth;  
	private static int mDay;  
	private static int mWeek; 
	private static int mHour;
	private static int mMinute;
	private ArrayList<String> mWeekNames;
	
	public SystemTime(Context ctx){
		mWeekNames = new ArrayList<String>();
		initWeeks(ctx);
		updateCalendar();
	}
	public String getFormatTime(){
		return String.format("%02d:%02d %s\n%04d/%02d/%02d",
		 mHour,mMinute, mWeekNames.get(mWeek),mYear,mMonth,mDay);
	}

	public void initWeeks(Context ctx){
		mWeekNames.add(ctx.getString(R.string.week_sunday));
		mWeekNames.add(ctx.getString(R.string.week_monday));
		mWeekNames.add(ctx.getString(R.string.week_tuesday));
		mWeekNames.add(ctx.getString(R.string.week_wednesday));
		mWeekNames.add(ctx.getString(R.string.week_thurday));
		mWeekNames.add(ctx.getString(R.string.week_friday));
		mWeekNames.add(ctx.getString(R.string.week_saturday));
	}
 
	      
	public void updateCalendar(){  
		final Calendar c = Calendar.getInstance();  
		c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));  
		mYear  = c.get(Calendar.YEAR); 
		mMonth = c.get(Calendar.MONTH) + 1;
		mDay   = c.get(Calendar.DAY_OF_MONTH);
		mWeek  = c.get(Calendar.DAY_OF_WEEK); 
		mHour  = c.get(Calendar.HOUR);
		mMinute= c.get(Calendar.MINUTE);
		mWeek--;  if((mWeek>= 7)||(mWeek<0)){mWeek = 0;}
	}  
}
