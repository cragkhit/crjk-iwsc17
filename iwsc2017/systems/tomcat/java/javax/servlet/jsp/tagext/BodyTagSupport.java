package javax.servlet.jsp.tagext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
public class BodyTagSupport extends TagSupport implements BodyTag {
    private static final long serialVersionUID = -7235752615580319833L;
    public BodyTagSupport() {
        super();
    }
    @Override
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
    @Override
    public int doEndTag() throws JspException {
        return super.doEndTag();
    }
    @Override
    public void setBodyContent ( BodyContent b ) {
        this.bodyContent = b;
    }
    @Override
    public void doInitBody() throws JspException {
    }
    @Override
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }
    @Override
    public void release() {
        bodyContent = null;
        super.release();
    }
    public BodyContent getBodyContent() {
        return bodyContent;
    }
    public JspWriter getPreviousOut() {
        return bodyContent.getEnclosingWriter();
    }
    protected transient BodyContent bodyContent;
}
