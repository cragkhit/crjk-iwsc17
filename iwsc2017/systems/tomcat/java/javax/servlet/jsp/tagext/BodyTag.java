package javax.servlet.jsp.tagext;
import javax.servlet.jsp.JspException;
public interface BodyTag extends IterationTag {
    @SuppressWarnings ( "dep-ann" )
    public static final int EVAL_BODY_TAG = 2;
    public static final int EVAL_BODY_BUFFERED = 2;
    void setBodyContent ( BodyContent b );
    void doInitBody() throws JspException;
}
