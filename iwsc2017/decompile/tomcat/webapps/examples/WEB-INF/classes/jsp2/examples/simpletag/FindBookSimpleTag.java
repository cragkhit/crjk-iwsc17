// 
// Decompiled by Procyon v0.5.29
// 

package jsp2.examples.simpletag;

import javax.servlet.jsp.JspException;
import jsp2.examples.BookBean;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class FindBookSimpleTag extends SimpleTagSupport
{
    private String var;
    private static final String BOOK_TITLE = "The Lord of the Rings";
    private static final String BOOK_AUTHOR = "J. R. R. Tolkein";
    private static final String BOOK_ISBN = "0618002251";
    
    public void doTag() throws JspException {
        final BookBean book = new BookBean("The Lord of the Rings", "J. R. R. Tolkein", "0618002251");
        this.getJspContext().setAttribute(this.var, (Object)book);
    }
    
    public void setVar(final String var) {
        this.var = var;
    }
}
