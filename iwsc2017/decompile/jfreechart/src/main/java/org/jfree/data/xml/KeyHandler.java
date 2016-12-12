package org.jfree.data.xml;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
public class KeyHandler extends DefaultHandler implements DatasetTags {
    private RootHandler rootHandler;
    private ItemHandler itemHandler;
    private StringBuffer currentText;
    public KeyHandler ( final RootHandler rootHandler, final ItemHandler itemHandler ) {
        this.rootHandler = rootHandler;
        this.itemHandler = itemHandler;
        this.currentText = new StringBuffer();
    }
    @Override
    public void startElement ( final String namespaceURI, final String localName, final String qName, final Attributes atts ) throws SAXException {
        if ( qName.equals ( "Key" ) ) {
            this.clearCurrentText();
            return;
        }
        throw new SAXException ( "Expecting <Key> but found " + qName );
    }
    @Override
    public void endElement ( final String namespaceURI, final String localName, final String qName ) throws SAXException {
        if ( qName.equals ( "Key" ) ) {
            this.itemHandler.setKey ( this.getCurrentText() );
            this.rootHandler.popSubHandler();
            this.rootHandler.pushSubHandler ( new ValueHandler ( this.rootHandler, this.itemHandler ) );
            return;
        }
        throw new SAXException ( "Expecting </Key> but found " + qName );
    }
    @Override
    public void characters ( final char[] ch, final int start, final int length ) {
        if ( this.currentText != null ) {
            this.currentText.append ( String.copyValueOf ( ch, start, length ) );
        }
    }
    protected String getCurrentText() {
        return this.currentText.toString();
    }
    protected void clearCurrentText() {
        this.currentText.delete ( 0, this.currentText.length() );
    }
}
