package org.apache.catalina.servlets;
import org.apache.catalina.util.XMLWriter;
import java.util.Enumeration;
import java.text.DateFormat;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import java.util.Date;
import java.util.Vector;
private class LockInfo {
    String path;
    String type;
    String scope;
    int depth;
    String owner;
    Vector<String> tokens;
    long expiresAt;
    Date creationDate;
    private LockInfo() {
        this.path = "/";
        this.type = "write";
        this.scope = "exclusive";
        this.depth = 0;
        this.owner = "";
        this.tokens = new Vector<String>();
        this.expiresAt = 0L;
        this.creationDate = new Date();
    }
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder ( "Type:" );
        result.append ( this.type );
        result.append ( "\nScope:" );
        result.append ( this.scope );
        result.append ( "\nDepth:" );
        result.append ( this.depth );
        result.append ( "\nOwner:" );
        result.append ( this.owner );
        result.append ( "\nExpiration:" );
        result.append ( FastHttpDateFormat.formatDate ( this.expiresAt, null ) );
        final Enumeration<String> tokensList = this.tokens.elements();
        while ( tokensList.hasMoreElements() ) {
            result.append ( "\nToken:" );
            result.append ( tokensList.nextElement() );
        }
        result.append ( "\n" );
        return result.toString();
    }
    public boolean hasExpired() {
        return System.currentTimeMillis() > this.expiresAt;
    }
    public boolean isExclusive() {
        return this.scope.equals ( "exclusive" );
    }
    public void toXML ( final XMLWriter generatedXML ) {
        generatedXML.writeElement ( "D", "activelock", 0 );
        generatedXML.writeElement ( "D", "locktype", 0 );
        generatedXML.writeElement ( "D", this.type, 2 );
        generatedXML.writeElement ( "D", "locktype", 1 );
        generatedXML.writeElement ( "D", "lockscope", 0 );
        generatedXML.writeElement ( "D", this.scope, 2 );
        generatedXML.writeElement ( "D", "lockscope", 1 );
        generatedXML.writeElement ( "D", "depth", 0 );
        if ( this.depth == WebdavServlet.access$100 ( WebdavServlet.this ) ) {
            generatedXML.writeText ( "Infinity" );
        } else {
            generatedXML.writeText ( "0" );
        }
        generatedXML.writeElement ( "D", "depth", 1 );
        generatedXML.writeElement ( "D", "owner", 0 );
        generatedXML.writeText ( this.owner );
        generatedXML.writeElement ( "D", "owner", 1 );
        generatedXML.writeElement ( "D", "timeout", 0 );
        final long timeout = ( this.expiresAt - System.currentTimeMillis() ) / 1000L;
        generatedXML.writeText ( "Second-" + timeout );
        generatedXML.writeElement ( "D", "timeout", 1 );
        generatedXML.writeElement ( "D", "locktoken", 0 );
        final Enumeration<String> tokensList = this.tokens.elements();
        while ( tokensList.hasMoreElements() ) {
            generatedXML.writeElement ( "D", "href", 0 );
            generatedXML.writeText ( "opaquelocktoken:" + tokensList.nextElement() );
            generatedXML.writeElement ( "D", "href", 1 );
        }
        generatedXML.writeElement ( "D", "locktoken", 1 );
        generatedXML.writeElement ( "D", "activelock", 1 );
    }
}
