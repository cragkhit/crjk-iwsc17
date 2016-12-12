// 
// Decompiled by Procyon v0.5.29
// 

package dates;

import java.util.Date;
import java.util.Calendar;

public class JspCalendar
{
    final Calendar calendar;
    
    public JspCalendar() {
        this.calendar = Calendar.getInstance();
        final Date trialTime = new Date();
        this.calendar.setTime(trialTime);
    }
    
    public int getYear() {
        return this.calendar.get(1);
    }
    
    public String getMonth() {
        final int m = this.getMonthInt();
        final String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
        if (m > 12) {
            return "Unknown to Man";
        }
        return months[m - 1];
    }
    
    public String getDay() {
        final int x = this.getDayOfWeek();
        final String[] days = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        if (x > 7) {
            return "Unknown to Man";
        }
        return days[x - 1];
    }
    
    public int getMonthInt() {
        return 1 + this.calendar.get(2);
    }
    
    public String getDate() {
        return this.getMonthInt() + "/" + this.getDayOfMonth() + "/" + this.getYear();
    }
    
    public String getTime() {
        return this.getHour() + ":" + this.getMinute() + ":" + this.getSecond();
    }
    
    public int getDayOfMonth() {
        return this.calendar.get(5);
    }
    
    public int getDayOfYear() {
        return this.calendar.get(6);
    }
    
    public int getWeekOfYear() {
        return this.calendar.get(3);
    }
    
    public int getWeekOfMonth() {
        return this.calendar.get(4);
    }
    
    public int getDayOfWeek() {
        return this.calendar.get(7);
    }
    
    public int getHour() {
        return this.calendar.get(11);
    }
    
    public int getMinute() {
        return this.calendar.get(12);
    }
    
    public int getSecond() {
        return this.calendar.get(13);
    }
    
    public static void main(final String[] args) {
        final JspCalendar db = new JspCalendar();
        p("date: " + db.getDayOfMonth());
        p("year: " + db.getYear());
        p("month: " + db.getMonth());
        p("time: " + db.getTime());
        p("date: " + db.getDate());
        p("Day: " + db.getDay());
        p("DayOfYear: " + db.getDayOfYear());
        p("WeekOfYear: " + db.getWeekOfYear());
        p("era: " + db.getEra());
        p("ampm: " + db.getAMPM());
        p("DST: " + db.getDSTOffset());
        p("ZONE Offset: " + db.getZoneOffset());
        p("TIMEZONE: " + db.getUSTimeZone());
    }
    
    private static void p(final String x) {
        System.out.println(x);
    }
    
    public int getEra() {
        return this.calendar.get(0);
    }
    
    public String getUSTimeZone() {
        final String[] zones = { "Hawaii", "Alaskan", "Pacific", "Mountain", "Central", "Eastern" };
        return zones[10 + this.getZoneOffset()];
    }
    
    public int getZoneOffset() {
        return this.calendar.get(15) / 3600000;
    }
    
    public int getDSTOffset() {
        return this.calendar.get(16) / 3600000;
    }
    
    public int getAMPM() {
        return this.calendar.get(9);
    }
}
