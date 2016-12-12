package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
public static class Visitor {
    public void visit ( final Root n ) throws JasperException {
        n.getExpression().visit ( this );
    }
    public void visit ( final Function n ) throws JasperException {
    }
    public void visit ( final Text n ) throws JasperException {
    }
    public void visit ( final ELText n ) throws JasperException {
    }
}
