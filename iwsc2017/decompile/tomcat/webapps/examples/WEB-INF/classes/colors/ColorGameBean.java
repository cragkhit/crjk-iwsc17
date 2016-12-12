// 
// Decompiled by Procyon v0.5.29
// 

package colors;

public class ColorGameBean
{
    private String background;
    private String foreground;
    private String color1;
    private String color2;
    private String hint;
    private int attempts;
    private int intval;
    private boolean tookHints;
    
    public ColorGameBean() {
        this.background = "yellow";
        this.foreground = "red";
        this.color1 = this.foreground;
        this.color2 = this.background;
        this.hint = "no";
        this.attempts = 0;
        this.intval = 0;
        this.tookHints = false;
    }
    
    public void processRequest() {
        if (!this.color1.equals(this.foreground) && (this.color1.equalsIgnoreCase("black") || this.color1.equalsIgnoreCase("cyan"))) {
            this.background = this.color1;
        }
        if (!this.color2.equals(this.background) && (this.color2.equalsIgnoreCase("black") || this.color2.equalsIgnoreCase("cyan"))) {
            this.foreground = this.color2;
        }
        ++this.attempts;
    }
    
    public void setColor2(final String x) {
        this.color2 = x;
    }
    
    public void setColor1(final String x) {
        this.color1 = x;
    }
    
    public void setAction(final String x) {
        if (!this.tookHints) {
            this.tookHints = x.equalsIgnoreCase("Hint");
        }
        this.hint = x;
    }
    
    public String getColor2() {
        return this.background;
    }
    
    public String getColor1() {
        return this.foreground;
    }
    
    public int getAttempts() {
        return this.attempts;
    }
    
    public boolean getHint() {
        return this.hint.equalsIgnoreCase("Hint");
    }
    
    public boolean getSuccess() {
        return (this.background.equalsIgnoreCase("black") || this.background.equalsIgnoreCase("cyan")) && (this.foreground.equalsIgnoreCase("black") || this.foreground.equalsIgnoreCase("cyan"));
    }
    
    public boolean getHintTaken() {
        return this.tookHints;
    }
    
    public void reset() {
        this.foreground = "red";
        this.background = "yellow";
    }
    
    public void setIntval(final int value) {
        this.intval = value;
    }
    
    public int getIntval() {
        return this.intval;
    }
}
