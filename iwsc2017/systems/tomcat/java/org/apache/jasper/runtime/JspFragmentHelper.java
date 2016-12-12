package org.apache.jasper.runtime;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.JspTag;
public abstract class JspFragmentHelper extends JspFragment {
    protected final int discriminator;
    protected final JspContext jspContext;
    protected final PageContext _jspx_page_context;
    protected final JspTag parentTag;
    public JspFragmentHelper ( int discriminator, JspContext jspContext,
                               JspTag parentTag ) {
        this.discriminator = discriminator;
        this.jspContext = jspContext;
        if ( jspContext instanceof PageContext ) {
            _jspx_page_context = ( PageContext ) jspContext;
        } else {
            _jspx_page_context = null;
        }
        this.parentTag = parentTag;
    }
    @Override
    public JspContext getJspContext() {
        return this.jspContext;
    }
    public JspTag getParentTag() {
        return this.parentTag;
    }
}
