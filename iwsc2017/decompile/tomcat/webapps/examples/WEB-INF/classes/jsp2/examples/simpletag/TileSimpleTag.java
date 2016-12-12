// 
// Decompiled by Procyon v0.5.29
// 

package jsp2.examples.simpletag;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class TileSimpleTag extends SimpleTagSupport
{
    private String color;
    private String label;
    
    public void doTag() throws JspException, IOException {
        this.getJspContext().getOut().write("<td width=\"32\" height=\"32\" bgcolor=\"" + this.color + "\"><font color=\"#ffffff\"><center>" + this.label + "</center></font></td>");
    }
    
    public void setColor(final String color) {
        this.color = color;
    }
    
    public void setLabel(final String label) {
        this.label = label;
    }
}
