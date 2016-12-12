package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
class ParamVisitor extends Node.Visitor {
    private final boolean ie;
    ParamVisitor ( final boolean ie ) {
        this.ie = ie;
    }
    @Override
    public void visit ( final Node.ParamAction n ) throws JasperException {
        String name = n.getTextAttribute ( "name" );
        if ( name.equalsIgnoreCase ( "object" ) ) {
            name = "java_object";
        } else if ( name.equalsIgnoreCase ( "type" ) ) {
            name = "java_type";
        }
        n.setBeginJavaLine ( GenerateVisitor.access$300 ( GenerateVisitor.this ).getJavaLine() );
        if ( this.ie ) {
            GenerateVisitor.access$300 ( GenerateVisitor.this ).printil ( "out.write( \"<param name=\\\"" + Generator.escape ( name ) + "\\\" value=\\\"\" + " + GenerateVisitor.access$400 ( GenerateVisitor.this, n.getValue(), false, String.class ) + " + \"\\\">\" );" );
            GenerateVisitor.access$300 ( GenerateVisitor.this ).printil ( "out.write(\"\\n\");" );
        } else {
            GenerateVisitor.access$300 ( GenerateVisitor.this ).printil ( "out.write( \" " + Generator.escape ( name ) + "=\\\"\" + " + GenerateVisitor.access$400 ( GenerateVisitor.this, n.getValue(), false, String.class ) + " + \"\\\"\" );" );
        }
        n.setEndJavaLine ( GenerateVisitor.access$300 ( GenerateVisitor.this ).getJavaLine() );
    }
}
