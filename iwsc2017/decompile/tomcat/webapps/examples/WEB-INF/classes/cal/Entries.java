// 
// Decompiled by Procyon v0.5.29
// 

package cal;

import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;

public class Entries
{
    private final Hashtable<String, Entry> entries;
    private static final String[] time;
    public static final int rows = 12;
    
    public Entries() {
        this.entries = new Hashtable<String, Entry>(12);
        for (int i = 0; i < 12; ++i) {
            this.entries.put(Entries.time[i], new Entry(Entries.time[i]));
        }
    }
    
    public int getRows() {
        return 12;
    }
    
    public Entry getEntry(final int index) {
        return this.entries.get(Entries.time[index]);
    }
    
    public int getIndex(final String tm) {
        for (int i = 0; i < 12; ++i) {
            if (tm.equals(Entries.time[i])) {
                return i;
            }
        }
        return -1;
    }
    
    public void processRequest(final HttpServletRequest request, final String tm) {
        final int index = this.getIndex(tm);
        if (index >= 0) {
            final String descr = request.getParameter("description");
            this.entries.get(Entries.time[index]).setDescription(descr);
        }
    }
    
    static {
        time = new String[] { "8am", "9am", "10am", "11am", "12pm", "1pm", "2pm", "3pm", "4pm", "5pm", "6pm", "7pm", "8pm" };
    }
}
