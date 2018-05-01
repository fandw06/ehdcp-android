package com.dawei.scdpm.context;

import android.database.Cursor;
import android.provider.CalendarContract;

public class CalendarEvent {

    public int id;
    public String title;
    public String description;
    public String location;
    public long begin;
    public long end;

    /**
     * Event items.
     */
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END
    };

    public CalendarEvent() {}

    /**
     * Build an event from cursor.
     * @param c
     */
    public CalendarEvent(Cursor c) {
        this.id = c.getInt(0);
        this.title = c.getString(1);
        this.description = c.getString(2);
        this.location = c.getString(3);
        this.begin = c.getLong(4);
        this.end = c.getLong(5);
    }

    @Override
    public String toString() {
        return "Event ID: " + id + " Title: " + title + " Description: " + description + " Location: " + location + " Begin: " + begin + " End: " + end;
    }
}
