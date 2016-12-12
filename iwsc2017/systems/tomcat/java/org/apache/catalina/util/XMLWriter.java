package org.apache.catalina.util;
import java.io.IOException;
import java.io.Writer;
public class XMLWriter {
    public static final int OPENING = 0;
    public static final int CLOSING = 1;
    public static final int NO_CONTENT = 2;
    protected StringBuilder buffer = new StringBuilder();
    protected final Writer writer;
    public XMLWriter() {
        this ( null );
    }
    public XMLWriter ( Writer writer ) {
        this.writer = writer;
    }
    @Override
    public String toString() {
        return buffer.toString();
    }
    public void writeProperty ( String namespace, String name, String value ) {
        writeElement ( namespace, name, OPENING );
        buffer.append ( value );
        writeElement ( namespace, name, CLOSING );
    }
    public void writeElement ( String namespace, String name, int type ) {
        writeElement ( namespace, null, name, type );
    }
    public void writeElement ( String namespace, String namespaceInfo,
                               String name, int type ) {
        if ( ( namespace != null ) && ( namespace.length() > 0 ) ) {
            switch ( type ) {
            case OPENING:
                if ( namespaceInfo != null ) {
                    buffer.append ( "<" + namespace + ":" + name + " xmlns:"
                                    + namespace + "=\""
                                    + namespaceInfo + "\">" );
                } else {
                    buffer.append ( "<" + namespace + ":" + name + ">" );
                }
                break;
            case CLOSING:
                buffer.append ( "</" + namespace + ":" + name + ">\n" );
                break;
            case NO_CONTENT:
            default:
                if ( namespaceInfo != null ) {
                    buffer.append ( "<" + namespace + ":" + name + " xmlns:"
                                    + namespace + "=\""
                                    + namespaceInfo + "\"/>" );
                } else {
                    buffer.append ( "<" + namespace + ":" + name + "/>" );
                }
                break;
            }
        } else {
            switch ( type ) {
            case OPENING:
                buffer.append ( "<" + name + ">" );
                break;
            case CLOSING:
                buffer.append ( "</" + name + ">\n" );
                break;
            case NO_CONTENT:
            default:
                buffer.append ( "<" + name + "/>" );
                break;
            }
        }
    }
    public void writeText ( String text ) {
        buffer.append ( text );
    }
    public void writeData ( String data ) {
        buffer.append ( "<![CDATA[" + data + "]]>" );
    }
    public void writeXMLHeader() {
        buffer.append ( "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" );
    }
    public void sendData()
    throws IOException {
        if ( writer != null ) {
            writer.write ( buffer.toString() );
            buffer = new StringBuilder();
        }
    }
}
