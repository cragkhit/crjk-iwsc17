// 
// Decompiled by Procyon v0.5.29
// 

package jsp2.examples.simpletag;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.ArrayList;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class EchoAttributesTag extends SimpleTagSupport implements DynamicAttributes
{
    private final ArrayList<String> keys;
    private final ArrayList<Object> values;
    
    public EchoAttributesTag() {
        this.keys = new ArrayList<String>();
        this.values = new ArrayList<Object>();
    }
    
    public void doTag() throws JspException, IOException {
        final JspWriter out = this.getJspContext().getOut();
        for (int i = 0; i < this.keys.size(); ++i) {
            final String key = this.keys.get(i);
            final Object value = this.values.get(i);
            out.println("<li>" + key + " = " + value + "</li>");
        }
    }
    
    public void setDynamicAttribute(final String uri, final String localName, final Object value) throws JspException {
        this.keys.add(localName);
        this.values.add(value);
    }
}
