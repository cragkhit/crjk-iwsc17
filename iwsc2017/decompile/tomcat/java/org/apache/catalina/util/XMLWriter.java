package org.apache.catalina.util;
import java.io.IOException;
import java.io.Writer;
public class XMLWriter {
    public static final int OPENING = 0;
    public static final int CLOSING = 1;
    public static final int NO_CONTENT = 2;
    protected StringBuilder buffer;
    protected final Writer writer;
    public XMLWriter() {
        this ( null );
    }
    public XMLWriter ( final Writer writer ) {
        this.buffer = new StringBuilder();
        this.writer = writer;
    }
    @Override
    public String toString() {
        return this.buffer.toString();
    }
    public void writeProperty ( final String namespace, final String name, final String value ) {
        this.writeElement ( namespace, name, 0 );
        this.buffer.append ( value );
        this.writeElement ( namespace, name, 1 );
    }
    public void writeElement ( final String namespace, final String name, final int type ) {
        this.writeElement ( namespace, null, name, type );
    }
    public void writeElement ( final String namespace, final String namespaceInfo, final String name, final int type ) {
        if ( namespace != null && namespace.length() > 0 ) {
            switch ( type ) {
            case 0: {
                if ( namespaceInfo != null ) {
                    this.buffer.append ( "<" + namespace + ":" + name + " xmlns:" + namespace + "=\"" + namespaceInfo + "\">" );
                    break;
                }
                this.buffer.append ( "<" + namespace + ":" + name + ">" );
                break;
            }
            case 1: {
                this.buffer.append ( "</" + namespace + ":" + name + ">\n" );
                break;
            }
            default: {
                if ( namespaceInfo != null ) {
                    this.buffer.append ( "<" + namespace + ":" + name + " xmlns:" + namespace + "=\"" + namespaceInfo + "\"/>" );
                    break;
                }
                this.buffer.append ( "<" + namespace + ":" + name + "/>" );
                break;
            }
            }
        } else {
            switch ( type ) {
            case 0: {
                this.buffer.append ( "<" + name + ">" );
                break;
            }
            case 1: {
                this.buffer.append ( "</" + name + ">\n" );
                break;
            }
            default: {
                this.buffer.append ( "<" + name + "/>" );
                break;
            }
            }
        }
    }
    public void writeText ( final String text ) {
        this.buffer.append ( text );
    }
    public void writeData ( final String data ) {
        this.buffer.append ( "<![CDATA[" + data + "]]>" );
    }
    public void writeXMLHeader() {
        this.buffer.append ( "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" );
    }
    public void sendData() throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( this.buffer.toString() );
            this.buffer = new StringBuilder();
        }
    }
}
