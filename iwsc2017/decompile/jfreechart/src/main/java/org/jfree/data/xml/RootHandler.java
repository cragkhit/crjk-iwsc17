package org.jfree.data.xml;
import org.xml.sax.SAXException;
import java.util.Stack;
import org.xml.sax.helpers.DefaultHandler;
public class RootHandler extends DefaultHandler implements DatasetTags {
    private Stack subHandlers;
    public RootHandler() {
        this.subHandlers = new Stack();
    }
    public Stack getSubHandlers() {
        return this.subHandlers;
    }
    @Override
    public void characters ( final char[] ch, final int start, final int length ) throws SAXException {
        final DefaultHandler handler = this.getCurrentHandler();
        if ( handler != this ) {
            handler.characters ( ch, start, length );
        }
    }
    public DefaultHandler getCurrentHandler() {
        DefaultHandler result = this;
        if ( this.subHandlers != null && this.subHandlers.size() > 0 ) {
            final Object top = this.subHandlers.peek();
            if ( top != null ) {
                result = ( DefaultHandler ) top;
            }
        }
        return result;
    }
    public void pushSubHandler ( final DefaultHandler subhandler ) {
        this.subHandlers.push ( subhandler );
    }
    public DefaultHandler popSubHandler() {
        return this.subHandlers.pop();
    }
}
