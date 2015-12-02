package com.android.systemui.statusbar.oswin.calendar.decorators;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.android.systemui.R;
import com.android.systemui.statusbar.oswin.calendar.CalendarDay;
import com.android.systemui.statusbar.oswin.calendar.DayViewDecorator;
import com.android.systemui.statusbar.oswin.calendar.DayViewFacade;

/**
 * Use a custom selector
 */
public class MySelectorDecorator implements DayViewDecorator {

    private final Drawable drawable;

    public MySelectorDecorator(Activity context) {
        drawable = context.getResources().getDrawable(R.drawable.week_day_pointer);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return true;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
    }
}
