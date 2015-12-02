package com.android.systemui.statusbar.oswin.calendar.decorators;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import java.util.Calendar;

import com.android.systemui.statusbar.oswin.calendar.CalendarDay;
import com.android.systemui.statusbar.oswin.calendar.DayViewDecorator;
import com.android.systemui.statusbar.oswin.calendar.DayViewFacade;

/**
 * Highlight Saturdays and Sundays with a background
 */
public class HighlightWeekendsDecorator implements DayViewDecorator {

    private final Calendar calendar = Calendar.getInstance();
    private final Drawable highlightDrawable;
    //private static final int color = Color.parseColor("#228BC34A");
    private static final int color = Color.parseColor("#00FFFFFF");

    public HighlightWeekendsDecorator() {
        highlightDrawable = new ColorDrawable(color);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        day.copyTo(calendar);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDay == Calendar.SATURDAY || weekDay == Calendar.SUNDAY;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(highlightDrawable);
    }
}
