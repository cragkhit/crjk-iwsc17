// 
// Decompiled by Procyon v0.5.29
// 

package jsp2.examples.simpletag;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class HelloWorldSimpleTag extends SimpleTagSupport
{
    public void doTag() throws JspException, IOException {
        this.getJspContext().getOut().write("Hello, world!");
    }
}
