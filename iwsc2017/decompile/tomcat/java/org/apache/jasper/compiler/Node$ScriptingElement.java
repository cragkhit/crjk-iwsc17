package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
public abstract static class ScriptingElement extends Node {
    public ScriptingElement ( final String qName, final String localName, final String text, final Mark start, final Node parent ) {
        super ( qName, localName, text, start, parent );
    }
    public ScriptingElement ( final String qName, final String localName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, localName, null, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    @Override
    public String getText() {
        String ret = this.text;
        if ( ret == null ) {
            if ( this.body != null ) {
                final StringBuilder buf = new StringBuilder();
                for ( int i = 0; i < this.body.size(); ++i ) {
                    buf.append ( this.body.getNode ( i ).getText() );
                }
                ret = buf.toString();
            } else {
                ret = "";
            }
        }
        return ret;
    }
    @Override
    public Mark getStart() {
        if ( this.text == null && this.body != null && this.body.size() > 0 ) {
            return this.body.getNode ( 0 ).getStart();
        }
        return super.getStart();
    }
}
