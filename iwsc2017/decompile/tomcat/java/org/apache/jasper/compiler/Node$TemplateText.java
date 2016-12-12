package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
import java.util.ArrayList;
public static class TemplateText extends Node {
    private ArrayList<Integer> extraSmap;
    public TemplateText ( final String text, final Mark start, final Node parent ) {
        super ( null, null, text, start, parent );
        this.extraSmap = null;
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public void ltrim() {
        int index;
        for ( index = 0; index < this.text.length() && this.text.charAt ( index ) <= ' '; ++index ) {}
        this.text = this.text.substring ( index );
    }
    public void setText ( final String text ) {
        this.text = text;
    }
    public void rtrim() {
        int index;
        for ( index = this.text.length(); index > 0 && this.text.charAt ( index - 1 ) <= ' '; --index ) {}
        this.text = this.text.substring ( 0, index );
    }
    public boolean isAllSpace() {
        boolean isAllSpace = true;
        for ( int i = 0; i < this.text.length(); ++i ) {
            if ( !Character.isWhitespace ( this.text.charAt ( i ) ) ) {
                isAllSpace = false;
                break;
            }
        }
        return isAllSpace;
    }
    public void addSmap ( final int srcLine ) {
        if ( this.extraSmap == null ) {
            this.extraSmap = new ArrayList<Integer>();
        }
        this.extraSmap.add ( srcLine );
    }
    public ArrayList<Integer> getExtraSmap() {
        return this.extraSmap;
    }
}
