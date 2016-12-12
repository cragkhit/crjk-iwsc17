package org.jfree.data.xml;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
public class ValueHandler extends DefaultHandler implements DatasetTags {
    private RootHandler rootHandler;
    private ItemHandler itemHandler;
    private StringBuffer currentText;
    public ValueHandler ( final RootHandler rootHandler, final ItemHandler itemHandler ) {
        this.rootHandler = rootHandler;
        this.itemHandler = itemHandler;
        this.currentText = new StringBuffer();
    }
    @Override
    public void startElement ( final String namespaceURI, final String localName, final String qName, final Attributes atts ) throws SAXException {
        if ( qName.equals ( "Value" ) ) {
            this.clearCurrentText();
            return;
        }
        throw new SAXException ( "Expecting <Value> but found " + qName );
    }
    @Override
    public void endElement ( final String namespaceURI, final String localName, final String qName ) throws SAXException {
        if ( qName.equals ( "Value" ) ) {
            Number value;
            try {
                value = Double.valueOf ( this.currentText.toString() );
                if ( ( ( Double ) value ).isNaN() ) {
                    value = null;
                }
            } catch ( NumberFormatException e1 ) {
                value = null;
            }
            this.itemHandler.setValue ( value );
            this.rootHandler.popSubHandler();
            return;
        }
        throw new SAXException ( "Expecting </Value> but found " + qName );
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
