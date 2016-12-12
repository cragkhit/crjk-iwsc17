package javax.servlet.jsp.tagext;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
public class TagSupport implements IterationTag, Serializable {
    private static final long serialVersionUID = 1L;
    public static final Tag findAncestorWithClass ( Tag from,
            @SuppressWarnings ( "rawtypes" )
            Class klass ) {
        boolean isInterface = false;
        if ( from == null ||
                klass == null ||
                ( !Tag.class.isAssignableFrom ( klass ) &&
                  ! ( isInterface = klass.isInterface() ) ) ) {
            return null;
        }
        for ( ;; ) {
            Tag tag = from.getParent();
            if ( tag == null ) {
                return null;
            }
            if ( ( isInterface && klass.isInstance ( tag ) ) ||
                    ( ( Class<?> ) klass ).isAssignableFrom ( tag.getClass() ) ) {
                return tag;
            }
            from = tag;
        }
    }
    public TagSupport() {
    }
    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }
    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }
    @Override
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }
    @Override
    public void release() {
        parent = null;
        id = null;
        if ( values != null ) {
            values.clear();
        }
        values = null;
    }
    @Override
    public void setParent ( Tag t ) {
        parent = t;
    }
    @Override
    public Tag getParent() {
        return parent;
    }
    public void setId ( String id ) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    @Override
    public void setPageContext ( PageContext pageContext ) {
        this.pageContext = pageContext;
    }
    public void setValue ( String k, Object o ) {
        if ( values == null ) {
            values = new Hashtable<>();
        }
        values.put ( k, o );
    }
    public Object getValue ( String k ) {
        if ( values == null ) {
            return null;
        }
        return values.get ( k );
    }
    public void removeValue ( String k ) {
        if ( values != null ) {
            values.remove ( k );
        }
    }
    public Enumeration<String> getValues() {
        if ( values == null ) {
            return null;
        }
        return values.keys();
    }
    private   Tag         parent;
    private   Hashtable<String, Object>   values;
    protected String      id;
    protected transient PageContext pageContext;
}
