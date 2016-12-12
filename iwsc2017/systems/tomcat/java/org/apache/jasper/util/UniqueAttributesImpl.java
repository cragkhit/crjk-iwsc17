package org.apache.jasper.util;
import java.util.HashSet;
import java.util.Set;
import org.apache.jasper.compiler.Localizer;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
public class UniqueAttributesImpl extends AttributesImpl {
    private static final String IMPORT = "import";
    private static final String PAGE_ENCODING = "pageEncoding";
    private final boolean pageDirective;
    private final Set<String> qNames = new HashSet<>();
    public UniqueAttributesImpl() {
        this.pageDirective = false;
    }
    public UniqueAttributesImpl ( boolean pageDirective ) {
        this.pageDirective = pageDirective;
    }
    @Override
    public void clear() {
        qNames.clear();
        super.clear();
    }
    @Override
    public void setAttributes ( Attributes atts ) {
        for ( int i = 0; i < atts.getLength(); i++ ) {
            if ( !qNames.add ( atts.getQName ( i ) ) ) {
                handleDuplicate ( atts.getQName ( i ), atts.getValue ( i ) );
            }
        }
        super.setAttributes ( atts );
    }
    @Override
    public void addAttribute ( String uri, String localName, String qName,
                               String type, String value ) {
        if ( qNames.add ( qName ) ) {
            super.addAttribute ( uri, localName, qName, type, value );
        } else {
            handleDuplicate ( qName, value );
        }
    }
    @Override
    public void setAttribute ( int index, String uri, String localName,
                               String qName, String type, String value ) {
        qNames.remove ( super.getQName ( index ) );
        if ( qNames.add ( qName ) ) {
            super.setAttribute ( index, uri, localName, qName, type, value );
        } else {
            handleDuplicate ( qName, value );
        }
    }
    @Override
    public void removeAttribute ( int index ) {
        qNames.remove ( super.getQName ( index ) );
        super.removeAttribute ( index );
    }
    @Override
    public void setQName ( int index, String qName ) {
        qNames.remove ( super.getQName ( index ) );
        super.setQName ( index, qName );
    }
    private void handleDuplicate ( String qName, String value ) {
        if ( pageDirective ) {
            if ( IMPORT.equalsIgnoreCase ( qName ) ) {
                int i = super.getIndex ( IMPORT );
                String v = super.getValue ( i );
                super.setValue ( i, v + "," + value );
                return;
            } else if ( PAGE_ENCODING.equalsIgnoreCase ( qName ) ) {
            } else {
                String v = super.getValue ( qName );
                if ( v.equals ( value ) ) {
                    return;
                }
            }
        }
        throw new IllegalArgumentException (
            Localizer.getMessage ( "jsp.error.duplicateqname", qName ) );
    }
}
