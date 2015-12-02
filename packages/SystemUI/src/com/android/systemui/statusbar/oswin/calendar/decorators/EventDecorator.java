package com.android.systemui.statusbar.oswin.calendar.decorators;

import java.util.Collection;
import java.util.HashSet;

import com.android.systemui.statusbar.oswin.calendar.CalendarDay;
import com.android.systemui.statusbar.oswin.calendar.DayViewDecorator;
import com.android.systemui.statusbar.oswin.calendar.DayViewFacade;

/**
 * Decorate several days with a dot
 */
public class EventDecorator implements DayViewDecorator {

    private int color;
    private HashSet<CalendarDay> dates;

    public EventDecorator(int color, Collection<CalendarDay> dates) {
        this.color = color;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(5, color));
    }
}
