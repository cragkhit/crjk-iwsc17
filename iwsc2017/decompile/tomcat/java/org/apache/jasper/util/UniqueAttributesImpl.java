package org.apache.jasper.util;
import org.apache.jasper.compiler.Localizer;
import org.xml.sax.Attributes;
import java.util.HashSet;
import java.util.Set;
import org.xml.sax.helpers.AttributesImpl;
public class UniqueAttributesImpl extends AttributesImpl {
    private static final String IMPORT = "import";
    private static final String PAGE_ENCODING = "pageEncoding";
    private final boolean pageDirective;
    private final Set<String> qNames;
    public UniqueAttributesImpl() {
        this.qNames = new HashSet<String>();
        this.pageDirective = false;
    }
    public UniqueAttributesImpl ( final boolean pageDirective ) {
        this.qNames = new HashSet<String>();
        this.pageDirective = pageDirective;
    }
    @Override
    public void clear() {
        this.qNames.clear();
        super.clear();
    }
    @Override
    public void setAttributes ( final Attributes atts ) {
        for ( int i = 0; i < atts.getLength(); ++i ) {
            if ( !this.qNames.add ( atts.getQName ( i ) ) ) {
                this.handleDuplicate ( atts.getQName ( i ), atts.getValue ( i ) );
            }
        }
        super.setAttributes ( atts );
    }
    @Override
    public void addAttribute ( final String uri, final String localName, final String qName, final String type, final String value ) {
        if ( this.qNames.add ( qName ) ) {
            super.addAttribute ( uri, localName, qName, type, value );
        } else {
            this.handleDuplicate ( qName, value );
        }
    }
    @Override
    public void setAttribute ( final int index, final String uri, final String localName, final String qName, final String type, final String value ) {
        this.qNames.remove ( super.getQName ( index ) );
        if ( this.qNames.add ( qName ) ) {
            super.setAttribute ( index, uri, localName, qName, type, value );
        } else {
            this.handleDuplicate ( qName, value );
        }
    }
    @Override
    public void removeAttribute ( final int index ) {
        this.qNames.remove ( super.getQName ( index ) );
        super.removeAttribute ( index );
    }
    @Override
    public void setQName ( final int index, final String qName ) {
        this.qNames.remove ( super.getQName ( index ) );
        super.setQName ( index, qName );
    }
    private void handleDuplicate ( final String qName, final String value ) {
        if ( this.pageDirective ) {
            if ( "import".equalsIgnoreCase ( qName ) ) {
                final int i = super.getIndex ( "import" );
                final String v = super.getValue ( i );
                super.setValue ( i, v + "," + value );
                return;
            }
            if ( !"pageEncoding".equalsIgnoreCase ( qName ) ) {
                final String v2 = super.getValue ( qName );
                if ( v2.equals ( value ) ) {
                    return;
                }
            }
        }
        throw new IllegalArgumentException ( Localizer.getMessage ( "jsp.error.duplicateqname", qName ) );
    }
}
