package org.apache.jasper.compiler;
import java.util.List;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
import java.util.Vector;
public static class PageDirective extends Node {
    private final Vector<String> imports;
    public PageDirective ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:directive.page", attrs, null, null, start, parent );
    }
    public PageDirective ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "directive.page", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        this.imports = new Vector<String>();
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public void addImport ( final String value ) {
        int start;
        int index;
        for ( start = 0; ( index = value.indexOf ( 44, start ) ) != -1; start = index + 1 ) {
            this.imports.add ( this.validateImport ( value.substring ( start, index ) ) );
        }
        if ( start == 0 ) {
            this.imports.add ( this.validateImport ( value ) );
        } else {
            this.imports.add ( this.validateImport ( value.substring ( start ) ) );
        }
    }
    public List<String> getImports() {
        return this.imports;
    }
    private String validateImport ( final String importEntry ) {
        if ( importEntry.indexOf ( 59 ) > -1 ) {
            throw new IllegalArgumentException ( Localizer.getMessage ( "jsp.error.page.invalid.import" ) );
        }
        return importEntry.trim();
    }
}
