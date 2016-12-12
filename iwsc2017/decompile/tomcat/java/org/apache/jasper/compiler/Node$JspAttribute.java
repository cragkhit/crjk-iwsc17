package org.apache.jasper.compiler;
import javax.el.ELException;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.servlet.jsp.tagext.TagAttributeInfo;
public static class JspAttribute {
    private final String qName;
    private final String uri;
    private final String localName;
    private final String value;
    private final boolean expression;
    private final boolean dynamic;
    private final ELNode.Nodes el;
    private final TagAttributeInfo tai;
    private final boolean namedAttribute;
    private final NamedAttribute namedAttributeNode;
    JspAttribute ( final TagAttributeInfo tai, final String qName, final String uri, final String localName, final String value, final boolean expr, final ELNode.Nodes el, final boolean dyn ) {
        this.qName = qName;
        this.uri = uri;
        this.localName = localName;
        this.value = value;
        this.namedAttributeNode = null;
        this.expression = expr;
        this.el = el;
        this.dynamic = dyn;
        this.namedAttribute = false;
        this.tai = tai;
    }
    public void validateEL ( final ExpressionFactory ef, final ELContext ctx ) throws ELException {
        if ( this.el != null ) {
            ef.createValueExpression ( ctx, this.value, ( Class ) String.class );
        }
    }
    JspAttribute ( final NamedAttribute na, final TagAttributeInfo tai, final boolean dyn ) {
        this.qName = na.getName();
        this.localName = na.getLocalName();
        this.value = null;
        this.namedAttributeNode = na;
        this.expression = false;
        this.el = null;
        this.dynamic = dyn;
        this.namedAttribute = true;
        this.tai = tai;
        this.uri = null;
    }
    public String getName() {
        return this.qName;
    }
    public String getLocalName() {
        return this.localName;
    }
    public String getURI() {
        return this.uri;
    }
    public TagAttributeInfo getTagAttributeInfo() {
        return this.tai;
    }
    public boolean isDeferredInput() {
        return this.tai != null && this.tai.isDeferredValue();
    }
    public boolean isDeferredMethodInput() {
        return this.tai != null && this.tai.isDeferredMethod();
    }
    public String getExpectedTypeName() {
        if ( this.tai != null ) {
            if ( this.isDeferredInput() ) {
                return this.tai.getExpectedTypeName();
            }
            if ( this.isDeferredMethodInput() ) {
                final String m = this.tai.getMethodSignature();
                if ( m != null ) {
                    final int rti = m.trim().indexOf ( 32 );
                    if ( rti > 0 ) {
                        return m.substring ( 0, rti ).trim();
                    }
                }
            }
        }
        return "java.lang.Object";
    }
    public String[] getParameterTypeNames() {
        if ( this.tai != null && this.isDeferredMethodInput() ) {
            String m = this.tai.getMethodSignature();
            if ( m != null ) {
                m = m.trim();
                m = m.substring ( m.indexOf ( 40 ) + 1 );
                m = m.substring ( 0, m.length() - 1 );
                if ( m.trim().length() > 0 ) {
                    final String[] p = m.split ( "," );
                    for ( int i = 0; i < p.length; ++i ) {
                        p[i] = p[i].trim();
                    }
                    return p;
                }
            }
        }
        return new String[0];
    }
    public String getValue() {
        return this.value;
    }
    public NamedAttribute getNamedAttributeNode() {
        return this.namedAttributeNode;
    }
    public boolean isExpression() {
        return this.expression;
    }
    public boolean isNamedAttribute() {
        return this.namedAttribute;
    }
    public boolean isELInterpreterInput() {
        return this.el != null || this.isDeferredInput() || this.isDeferredMethodInput();
    }
    public boolean isLiteral() {
        return !this.expression && this.el == null && !this.namedAttribute;
    }
    public boolean isDynamic() {
        return this.dynamic;
    }
    public ELNode.Nodes getEL() {
        return this.el;
    }
}
