// 
// Decompiled by Procyon v0.5.29
// 

package examples;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

public abstract class ExampleTagBase extends BodyTagSupport
{
    private static final long serialVersionUID = 1L;
    protected BodyContent bodyOut;
    protected Tag parent;
    
    public void setParent(final Tag parent) {
        this.parent = parent;
    }
    
    public void setBodyContent(final BodyContent bodyOut) {
        this.bodyOut = bodyOut;
    }
    
    public Tag getParent() {
        return this.parent;
    }
    
    public int doStartTag() throws JspException {
        return 0;
    }
    
    public int doEndTag() throws JspException {
        return 6;
    }
    
    public void doInitBody() throws JspException {
    }
    
    public int doAfterBody() throws JspException {
        return 0;
    }
    
    public void release() {
        this.bodyOut = null;
        this.pageContext = null;
        this.parent = null;
    }
}
