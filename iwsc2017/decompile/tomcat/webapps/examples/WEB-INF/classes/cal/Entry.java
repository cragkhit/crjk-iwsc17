// 
// Decompiled by Procyon v0.5.29
// 

package cal;

public class Entry
{
    final String hour;
    String description;
    
    public Entry(final String hour) {
        this.hour = hour;
        this.description = "";
    }
    
    public String getHour() {
        return this.hour;
    }
    
    public String getColor() {
        if (this.description.equals("")) {
            return "lightblue";
        }
        return "red";
    }
    
    public String getDescription() {
        if (this.description.equals("")) {
            return "None";
        }
        return this.description;
    }
    
    public void setDescription(final String descr) {
        this.description = descr;
    }
}
