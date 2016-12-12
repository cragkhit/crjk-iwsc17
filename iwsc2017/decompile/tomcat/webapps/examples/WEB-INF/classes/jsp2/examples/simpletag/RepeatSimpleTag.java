// 
// Decompiled by Procyon v0.5.29
// 

package jsp2.examples.simpletag;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import java.io.Writer;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class RepeatSimpleTag extends SimpleTagSupport
{
    private int num;
    
    public void doTag() throws JspException, IOException {
        for (int i = 0; i < this.num; ++i) {
            this.getJspContext().setAttribute("count", (Object)String.valueOf(i + 1));
            this.getJspBody().invoke((Writer)null);
        }
    }
    
    public void setNum(final int num) {
        this.num = num;
    }
}
