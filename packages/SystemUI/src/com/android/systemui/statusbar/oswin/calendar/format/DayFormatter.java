package com.android.systemui.statusbar.oswin.calendar.format;



import com.android.systemui.statusbar.oswin.calendar.CalendarDay;

import java.text.SimpleDateFormat;

/**
 * Supply labels for a given day. Default implementation is to format using a {@linkplain SimpleDateFormat}
 */
public interface DayFormatter {

    /**
     * Format a given day into a string
     *
     * @param day the day
     * @return a label for the day
     */
    
    String format( CalendarDay day);

    /**
     * Default implementation used by {@linkplain package com.android.systemui.statusbar.oswin.calendar.MaterialCalendarView}
     */
    public static final DayFormatter DEFAULT = new DateFormatDayFormatter();
}
