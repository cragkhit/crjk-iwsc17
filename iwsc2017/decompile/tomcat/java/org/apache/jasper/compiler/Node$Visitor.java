package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
public static class Visitor {
    protected void doVisit ( final Node n ) throws JasperException {
    }
    protected void visitBody ( final Node n ) throws JasperException {
        if ( n.getBody() != null ) {
            n.getBody().visit ( this );
        }
    }
    public void visit ( final Root n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final JspRoot n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final PageDirective n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final TagDirective n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final IncludeDirective n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final TaglibDirective n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final AttributeDirective n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final VariableDirective n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final Comment n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final Declaration n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final Expression n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final Scriptlet n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final ELExpression n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final IncludeAction n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final ForwardAction n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final GetProperty n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final SetProperty n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final ParamAction n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final ParamsAction n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final FallBackAction n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final UseBean n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final PlugIn n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final CustomTag n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final UninterpretedTag n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final JspElement n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final JspText n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final NamedAttribute n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final JspBody n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final InvokeAction n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final DoBodyAction n ) throws JasperException {
        this.doVisit ( n );
        this.visitBody ( n );
    }
    public void visit ( final TemplateText n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final JspOutput n ) throws JasperException {
        this.doVisit ( n );
    }
    public void visit ( final AttributeGenerator n ) throws JasperException {
        this.doVisit ( n );
    }
}
