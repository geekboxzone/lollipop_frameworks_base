package com.android.systemui.statusbar.oswin.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import com.android.systemui.R;
import com.android.systemui.statusbar.oswin.calendar.decorators.EventDecorator;
import com.android.systemui.statusbar.oswin.calendar.decorators.HighlightWeekendsDecorator;
import com.android.systemui.statusbar.oswin.calendar.decorators.MySelectorDecorator;
import com.android.systemui.statusbar.oswin.calendar.decorators.OneDayDecorator;
import com.android.systemui.statusbar.oswin.calendar.format.ArrayWeekDayFormatter;
import com.android.systemui.statusbar.oswin.calendar.format.DateFormatTitleFormatter;
import com.android.systemui.statusbar.oswin.calendar.format.MonthArrayTitleFormatter;
import com.android.systemui.statusbar.oswin.calendar.format.TitleFormatter;
import com.android.systemui.statusbar.oswin.calendar.format.WeekDayFormatter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class CalendarDialog implements OnDateSelectedListener{
    protected static final String TAG =  CalendarDialog.class.getName();
    private Context mContext;
    private Dialog mDateCenter;
    private View mRoot;
     
    private MaterialCalendarView  mCalendar;
    private TextView mTimeWeekday;
    private TextView mTimeDate;
    private TextView mTimeNow;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private final int MSG_TIME_TICK = 10;

    public CalendarDialog(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mDateCenter = onCreateLayout(context);
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if(msg.what == MSG_TIME_TICK){
                if(mDateCenter.isShowing()){
                    updateSystemTime();
                    mHandler.sendEmptyMessageDelayed(MSG_TIME_TICK, 8000);
                }
            }
        }
    };
    
    public Dialog onCreateLayout(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        mRoot = layoutInflater.inflate(R.layout.dialog_calendar, null);  
        mCalendar = (MaterialCalendarView)mRoot.findViewById(R.id.calendarView);
        mTimeWeekday = (TextView)mRoot.findViewById(R.id.time_weekday);
        mTimeDate    = (TextView)mRoot.findViewById(R.id.time_date);
        mTimeNow     = (TextView)mRoot.findViewById(R.id.time_now);
        //mCalendar.setShowWeekNumber(false);
        
        mCalendar.setOnDateChangedListener(this);
        mCalendar.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        //mCalendar.setSelectionColor(mContext.getResources().getColor(R.color.win_grey_white));
        mCalendar.setHeaderTextAppearance(R.style.CustomDayTextAppearance);
        mCalendar.setWeekDayTextAppearance(R.style.CustomDayTextAppearance);
        mCalendar.setDateTextAppearance(R.style.CustomDayTextAppearance);
        //mCalendar.setTitleFormatter(new MonthArrayTitleFormatter(mContext.getResources().getTextArray(R.array.custom_months)));
        mCalendar.setWeekDayFormatter(new ArrayWeekDayFormatter(mContext.getResources().getTextArray(R.array.custom_weekdays)));
        //mCalendar.setTileSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, 
        //                              mContext.getResources().getDisplayMetrics()));
        
        Calendar calendar = Calendar.getInstance();
        mCalendar.setSelectedDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1);
        mCalendar.setMinimumDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);
        mCalendar.setMaximumDate(calendar.getTime());

        mCalendar.addDecorators(
                //new MySelectorDecorator(this),
                new HighlightWeekendsDecorator(),
                oneDayDecorator
        );

        //new ApiSimulator().executeOnExecutor(Executors.newSingleThreadExecutor());
        
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

    @Override
    public void onDateSelected( MaterialCalendarView widget, CalendarDay date, boolean selected) {
        //If you change a decorate, you need to invalidate decorators
        oneDayDecorator.setDate(date.getDate());
        widget.invalidateDecorators();
    }

    private void updateSystemTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00")); 
        
        String weekday = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());     
        mTimeWeekday.setText(weekday);

        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());        
        mTimeDate.setText(month + " " + calendar.get(Calendar.DAY_OF_MONTH));

        String am_pm = calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault());
        String time_now = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        mTimeNow.setText(am_pm + " " + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE));
    }

    //$_FENG_$_INFO_$_Notification_Center
    public void openCalendar(){
        if(mDateCenter.isShowing()){
            mDateCenter.dismiss();
        }
		Log.d(TAG, "openCalendar...");
        updateSystemTime();
        mHandler.sendEmptyMessageDelayed(MSG_TIME_TICK, 3000);
        mDateCenter.show();
    }
    
    public void closeCalendar(){
        if(mDateCenter.isShowing()){
            mDateCenter.dismiss();
        }
        Log.d(TAG, "closeCalendar...");
    }
  
    public boolean isCalendarShow(){
        return mDateCenter.isShowing();
    }
    /**
     * Simulate an API call to show how to add decorators
     */
    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        @Override
        protected List<CalendarDay> doInBackground( Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -2);
            ArrayList<CalendarDay> dates = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                CalendarDay day = CalendarDay.from(calendar);
                dates.add(day);
                calendar.add(Calendar.DATE, 5);
            }

            return dates;
        }

        @Override
        protected void onPostExecute(List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            mCalendar.addDecorator(new EventDecorator(Color.RED, calendarDays));
        }
    }

}

