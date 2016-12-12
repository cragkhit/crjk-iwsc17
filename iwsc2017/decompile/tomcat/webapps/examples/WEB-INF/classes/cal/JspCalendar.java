// 
// Decompiled by Procyon v0.5.29
// 

package cal;

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
    
    public String getCurrentDate() {
        final Date dt = new Date();
        this.calendar.setTime(dt);
        return this.getMonthInt() + "/" + this.getDayOfMonth() + "/" + this.getYear();
    }
    
    public String getNextDate() {
        this.calendar.set(5, this.getDayOfMonth() + 1);
        return this.getDate();
    }
    
    public String getPrevDate() {
        this.calendar.set(5, this.getDayOfMonth() - 1);
        return this.getDate();
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
