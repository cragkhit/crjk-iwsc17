package org.apache.jasper.compiler;
import java.util.List;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
import java.util.Vector;
public static class TagDirective extends Node {
    private final Vector<String> imports;
    public TagDirective ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:directive.tag", attrs, null, null, start, parent );
    }
    public TagDirective ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "directive.tag", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        this.imports = new Vector<String>();
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public void addImport ( final String value ) {
        int start;
        int index;
        for ( start = 0; ( index = value.indexOf ( 44, start ) ) != -1; start = index + 1 ) {
            this.imports.add ( value.substring ( start, index ).trim() );
        }
        if ( start == 0 ) {
            this.imports.add ( value.trim() );
        } else {
            this.imports.add ( value.substring ( start ).trim() );
        }
    }
    public List<String> getImports() {
        return this.imports;
    }
}
