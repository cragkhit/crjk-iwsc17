package javax.servlet.jsp.tagext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
public class TagAdapter implements Tag {
    private final SimpleTag simpleTagAdaptee;
    private Tag parent;
    private boolean parentDetermined;
    public TagAdapter ( final SimpleTag adaptee ) {
        if ( adaptee == null ) {
            throw new IllegalArgumentException();
        }
        this.simpleTagAdaptee = adaptee;
    }
    @Override
    public void setPageContext ( final PageContext pc ) {
        throw new UnsupportedOperationException ( "Illegal to invoke setPageContext() on TagAdapter wrapper" );
    }
    @Override
    public void setParent ( final Tag parentTag ) {
        throw new UnsupportedOperationException ( "Illegal to invoke setParent() on TagAdapter wrapper" );
    }
    @Override
    public Tag getParent() {
        if ( !this.parentDetermined ) {
            final JspTag adapteeParent = this.simpleTagAdaptee.getParent();
            if ( adapteeParent != null ) {
                if ( adapteeParent instanceof Tag ) {
                    this.parent = ( Tag ) adapteeParent;
                } else {
                    this.parent = new TagAdapter ( ( SimpleTag ) adapteeParent );
                }
            }
            this.parentDetermined = true;
        }
        return this.parent;
    }
    public JspTag getAdaptee() {
        return this.simpleTagAdaptee;
    }
    @Override
    public int doStartTag() throws JspException {
        throw new UnsupportedOperationException ( "Illegal to invoke doStartTag() on TagAdapter wrapper" );
    }
    @Override
    public int doEndTag() throws JspException {
        throw new UnsupportedOperationException ( "Illegal to invoke doEndTag() on TagAdapter wrapper" );
    }
    @Override
    public void release() {
        throw new UnsupportedOperationException ( "Illegal to invoke release() on TagAdapter wrapper" );
    }
}
