package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
class ParamVisitor extends Node.Visitor {
    private String separator;
    ParamVisitor ( final String separator ) {
        this.separator = separator;
    }
    @Override
    public void visit ( final Node.ParamAction n ) throws JasperException {
        GenerateVisitor.access$300 ( GenerateVisitor.this ).print ( " + " );
        GenerateVisitor.access$300 ( GenerateVisitor.this ).print ( this.separator );
        GenerateVisitor.access$300 ( GenerateVisitor.this ).print ( " + " );
        GenerateVisitor.access$300 ( GenerateVisitor.this ).print ( "org.apache.jasper.runtime.JspRuntimeLibrary.URLEncode(" + Generator.quote ( n.getTextAttribute ( "name" ) ) + ", request.getCharacterEncoding())" );
        GenerateVisitor.access$300 ( GenerateVisitor.this ).print ( "+ \"=\" + " );
        GenerateVisitor.access$300 ( GenerateVisitor.this ).print ( GenerateVisitor.access$400 ( GenerateVisitor.this, n.getValue(), true, String.class ) );
        this.separator = "\"&\"";
    }
}
