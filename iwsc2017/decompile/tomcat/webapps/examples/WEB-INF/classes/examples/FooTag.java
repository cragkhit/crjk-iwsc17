// 
// Decompiled by Procyon v0.5.29
// 

package examples;

import java.io.IOException;
import javax.servlet.jsp.JspTagException;
import java.io.Writer;
import javax.servlet.jsp.JspException;

public class FooTag extends ExampleTagBase
{
    private static final long serialVersionUID = 1L;
    private final String[] atts;
    int i;
    
    public FooTag() {
        this.atts = new String[3];
        this.i = 0;
    }
    
    private final void setAtt(final int index, final String value) {
        this.atts[index] = value;
    }
    
    public void setAtt1(final String value) {
        this.setAtt(0, value);
    }
    
    public void setAtt2(final String value) {
        this.setAtt(1, value);
    }
    
    public void setAtt3(final String value) {
        this.setAtt(2, value);
    }
    
    @Override
    public int doStartTag() throws JspException {
        this.i = 0;
        return 2;
    }
    
    @Override
    public void doInitBody() throws JspException {
        this.pageContext.setAttribute("member", (Object)this.atts[this.i]);
        ++this.i;
    }
    
    @Override
    public int doAfterBody() throws JspException {
        try {
            if (this.i == 3) {
                this.bodyOut.writeOut((Writer)this.bodyOut.getEnclosingWriter());
                return 0;
            }
            this.pageContext.setAttribute("member", (Object)this.atts[this.i]);
            ++this.i;
            return 2;
        }
        catch (IOException ex) {
            throw new JspTagException(ex.toString());
        }
    }
}
