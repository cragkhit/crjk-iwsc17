package org.apache.catalina.util;
import java.io.PrintWriter;
import java.io.Writer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class DOMWriter {
    private final PrintWriter out;
    private final boolean canonical;
    public DOMWriter ( Writer writer, boolean canonical ) {
        out = new PrintWriter ( writer );
        this.canonical = canonical;
    }
    public void print ( Node node ) {
        if ( node == null ) {
            return;
        }
        int type = node.getNodeType();
        switch ( type ) {
        case Node.DOCUMENT_NODE:
            if ( !canonical ) {
                out.println ( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
            }
            print ( ( ( Document ) node ).getDocumentElement() );
            out.flush();
            break;
        case Node.ELEMENT_NODE:
            out.print ( '<' );
            out.print ( node.getLocalName() );
            Attr attrs[] = sortAttributes ( node.getAttributes() );
            for ( int i = 0; i < attrs.length; i++ ) {
                Attr attr = attrs[i];
                out.print ( ' ' );
                out.print ( attr.getLocalName() );
                out.print ( "=\"" );
                out.print ( escape ( attr.getNodeValue() ) );
                out.print ( '"' );
            }
            out.print ( '>' );
            printChildren ( node );
            break;
        case Node.ENTITY_REFERENCE_NODE:
            if ( canonical ) {
                printChildren ( node );
            } else {
                out.print ( '&' );
                out.print ( node.getLocalName() );
                out.print ( ';' );
            }
            break;
        case Node.CDATA_SECTION_NODE:
            if ( canonical ) {
                out.print ( escape ( node.getNodeValue() ) );
            } else {
                out.print ( "<![CDATA[" );
                out.print ( node.getNodeValue() );
                out.print ( "]]>" );
            }
            break;
        case Node.TEXT_NODE:
            out.print ( escape ( node.getNodeValue() ) );
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            out.print ( "<?" );
            out.print ( node.getLocalName() );
            String data = node.getNodeValue();
            if ( data != null && data.length() > 0 ) {
                out.print ( ' ' );
                out.print ( data );
            }
            out.print ( "?>" );
            break;
        }
        if ( type == Node.ELEMENT_NODE ) {
            out.print ( "</" );
            out.print ( node.getLocalName() );
            out.print ( '>' );
        }
        out.flush();
    }
    private void printChildren ( Node node ) {
        NodeList children = node.getChildNodes();
        if ( children != null ) {
            int len = children.getLength();
            for ( int i = 0; i < len; i++ ) {
                print ( children.item ( i ) );
            }
        }
    }
    private Attr[] sortAttributes ( NamedNodeMap attrs ) {
        if ( attrs == null ) {
            return new Attr[0];
        }
        int len = attrs.getLength();
        Attr array[] = new Attr[len];
        for ( int i = 0; i < len; i++ ) {
            array[i] = ( Attr ) attrs.item ( i );
        }
        for ( int i = 0; i < len - 1; i++ ) {
            String name = null;
            name = array[i].getLocalName();
            int index = i;
            for ( int j = i + 1; j < len; j++ ) {
                String curName = null;
                curName = array[j].getLocalName();
                if ( curName.compareTo ( name ) < 0 ) {
                    name = curName;
                    index = j;
                }
            }
            if ( index != i ) {
                Attr temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }
        }
        return ( array );
    }
    private String escape ( String s ) {
        if ( s == null ) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        int len = s.length();
        for ( int i = 0; i < len; i++ ) {
            char ch = s.charAt ( i );
            switch ( ch ) {
            case '<':
                str.append ( "&lt;" );
                break;
            case '>':
                str.append ( "&gt;" );
                break;
            case '&':
                str.append ( "&amp;" );
                break;
            case '"':
                str.append ( "&quot;" );
                break;
            case '\r':
            case '\n':
                if ( canonical ) {
                    str.append ( "&#" );
                    str.append ( Integer.toString ( ch ) );
                    str.append ( ';' );
                    break;
                }
            default:
                str.append ( ch );
            }
        }
        return ( str.toString() );
    }
}
