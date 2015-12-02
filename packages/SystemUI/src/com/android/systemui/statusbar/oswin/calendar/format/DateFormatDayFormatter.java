package com.android.systemui.statusbar.oswin.calendar.format;



import com.android.systemui.statusbar.oswin.calendar.CalendarDay;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Format using a {@linkplain DateFormat} instance.
 */
public class DateFormatDayFormatter implements DayFormatter {

    private final DateFormat dateFormat;

    /**
     * Format using a default format
     */
    public DateFormatDayFormatter() {
        this.dateFormat = new SimpleDateFormat("d", Locale.getDefault());
    }

    /**
     * Format using a specific {@linkplain DateFormat}
     *
     * @param format the format to use
     */
    public DateFormatDayFormatter( DateFormat format) {
        this.dateFormat = format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    
    public String format( CalendarDay day) {
        return dateFormat.format(day.getDate());
    }
}
