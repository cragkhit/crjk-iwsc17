package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
public static class Root extends Node {
    private final Root parentRoot;
    private final boolean isXmlSyntax;
    private String pageEnc;
    private String jspConfigPageEnc;
    private boolean isDefaultPageEncoding;
    private boolean isEncodingSpecifiedInProlog;
    private boolean isBomPresent;
    private int tempSequenceNumber;
    Root ( final Mark start, final Node parent, final boolean isXmlSyntax ) {
        super ( start, parent );
        this.tempSequenceNumber = 0;
        this.isXmlSyntax = isXmlSyntax;
        this.qName = "jsp:root";
        this.localName = "root";
        Node r;
        for ( r = parent; r != null && ! ( r instanceof Root ); r = r.getParent() ) {}
        this.parentRoot = ( Root ) r;
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public boolean isXmlSyntax() {
        return this.isXmlSyntax;
    }
    public void setJspConfigPageEncoding ( final String enc ) {
        this.jspConfigPageEnc = enc;
    }
    public String getJspConfigPageEncoding() {
        return this.jspConfigPageEnc;
    }
    public void setPageEncoding ( final String enc ) {
        this.pageEnc = enc;
    }
    public String getPageEncoding() {
        return this.pageEnc;
    }
    public void setIsDefaultPageEncoding ( final boolean isDefault ) {
        this.isDefaultPageEncoding = isDefault;
    }
    public boolean isDefaultPageEncoding() {
        return this.isDefaultPageEncoding;
    }
    public void setIsEncodingSpecifiedInProlog ( final boolean isSpecified ) {
        this.isEncodingSpecifiedInProlog = isSpecified;
    }
    public boolean isEncodingSpecifiedInProlog() {
        return this.isEncodingSpecifiedInProlog;
    }
    public void setIsBomPresent ( final boolean isBom ) {
        this.isBomPresent = isBom;
    }
    public boolean isBomPresent() {
        return this.isBomPresent;
    }
    public String nextTemporaryVariableName() {
        if ( this.parentRoot == null ) {
            return Constants.TEMP_VARIABLE_NAME_PREFIX + this.tempSequenceNumber++;
        }
        return this.parentRoot.nextTemporaryVariableName();
    }
}
