package org.apache.jasper.compiler;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
private static class FirstPassVisitor extends Node.Visitor implements TagConstants {
    private final Node.Root root;
    private final AttributesImpl rootAttrs;
    private final PageInfo pageInfo;
    private String jspIdPrefix;
    public FirstPassVisitor ( final Node.Root root, final PageInfo pageInfo ) {
        this.root = root;
        this.pageInfo = pageInfo;
        ( this.rootAttrs = new AttributesImpl() ).addAttribute ( "", "", "version", "CDATA", "2.0" );
        this.jspIdPrefix = "jsp";
    }
    @Override
    public void visit ( final Node.Root n ) throws JasperException {
        this.visitBody ( n );
        if ( n == this.root ) {
            if ( !"http://java.sun.com/JSP/Page".equals ( this.rootAttrs.getValue ( "xmlns:jsp" ) ) ) {
                this.rootAttrs.addAttribute ( "", "", "xmlns:jsp", "CDATA", "http://java.sun.com/JSP/Page" );
            }
            if ( this.pageInfo.isJspPrefixHijacked() ) {
                this.jspIdPrefix += "jsp";
                while ( this.pageInfo.containsPrefix ( this.jspIdPrefix ) ) {
                    this.jspIdPrefix += "jsp";
                }
                this.rootAttrs.addAttribute ( "", "", "xmlns:" + this.jspIdPrefix, "CDATA", "http://java.sun.com/JSP/Page" );
            }
            this.root.setAttributes ( this.rootAttrs );
        }
    }
    @Override
    public void visit ( final Node.JspRoot n ) throws JasperException {
        this.addAttributes ( n.getTaglibAttributes() );
        this.addAttributes ( n.getNonTaglibXmlnsAttributes() );
        this.addAttributes ( n.getAttributes() );
        this.visitBody ( n );
    }
    @Override
    public void visit ( final Node.TaglibDirective n ) throws JasperException {
        final Attributes attrs = n.getAttributes();
        if ( attrs != null ) {
            final String qName = "xmlns:" + attrs.getValue ( "prefix" );
            if ( this.rootAttrs.getIndex ( qName ) == -1 ) {
                String location = attrs.getValue ( "uri" );
                if ( location != null ) {
                    if ( location.startsWith ( "/" ) ) {
                        location = "urn:jsptld:" + location;
                    }
                    this.rootAttrs.addAttribute ( "", "", qName, "CDATA", location );
                } else {
                    location = attrs.getValue ( "tagdir" );
                    this.rootAttrs.addAttribute ( "", "", qName, "CDATA", "urn:jsptagdir:" + location );
                }
            }
        }
    }
    public String getJspIdPrefix() {
        return this.jspIdPrefix;
    }
    private void addAttributes ( final Attributes attrs ) {
        if ( attrs != null ) {
            for ( int len = attrs.getLength(), i = 0; i < len; ++i ) {
                final String qName = attrs.getQName ( i );
                if ( !"version".equals ( qName ) ) {
                    if ( this.rootAttrs.getIndex ( qName ) == -1 ) {
                        this.rootAttrs.addAttribute ( attrs.getURI ( i ), attrs.getLocalName ( i ), qName, attrs.getType ( i ), attrs.getValue ( i ) );
                    }
                }
            }
        }
    }
}
