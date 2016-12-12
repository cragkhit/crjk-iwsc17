// 
// Decompiled by Procyon v0.5.29
// 

package cal;

import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;

public class TableBean
{
    final Hashtable<String, Entries> table;
    final JspCalendar JspCal;
    Entries entries;
    String date;
    String name;
    String email;
    boolean processError;
    
    public TableBean() {
        this.name = null;
        this.email = null;
        this.processError = false;
        this.table = new Hashtable<String, Entries>(10);
        this.JspCal = new JspCalendar();
        this.date = this.JspCal.getCurrentDate();
    }
    
    public void setName(final String nm) {
        this.name = nm;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setEmail(final String mail) {
        this.email = mail;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    public String getDate() {
        return this.date;
    }
    
    public Entries getEntries() {
        return this.entries;
    }
    
    public void processRequest(final HttpServletRequest request) {
        this.processError = false;
        if (this.name == null || this.name.equals("")) {
            this.setName(request.getParameter("name"));
        }
        if (this.email == null || this.email.equals("")) {
            this.setEmail(request.getParameter("email"));
        }
        if (this.name == null || this.email == null || this.name.equals("") || this.email.equals("")) {
            this.processError = true;
            return;
        }
        final String dateR = request.getParameter("date");
        if (dateR == null) {
            this.date = this.JspCal.getCurrentDate();
        }
        else if (dateR.equalsIgnoreCase("next")) {
            this.date = this.JspCal.getNextDate();
        }
        else if (dateR.equalsIgnoreCase("prev")) {
            this.date = this.JspCal.getPrevDate();
        }
        this.entries = this.table.get(this.date);
        if (this.entries == null) {
            this.entries = new Entries();
            this.table.put(this.date, this.entries);
        }
        final String time = request.getParameter("time");
        if (time != null) {
            this.entries.processRequest(request, time);
        }
    }
    
    public boolean getProcessError() {
        return this.processError;
    }
}
