package org.jfree.data.xml;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
public class ItemHandler extends DefaultHandler implements DatasetTags {
    private RootHandler root;
    private DefaultHandler parent;
    private Comparable key;
    private Number value;
    public ItemHandler ( final RootHandler root, final DefaultHandler parent ) {
        this.root = root;
        this.parent = parent;
        this.key = null;
        this.value = null;
    }
    public Comparable getKey() {
        return this.key;
    }
    public void setKey ( final Comparable key ) {
        this.key = key;
    }
    public Number getValue() {
        return this.value;
    }
    public void setValue ( final Number value ) {
        this.value = value;
    }
    @Override
    public void startElement ( final String namespaceURI, final String localName, final String qName, final Attributes atts ) throws SAXException {
        if ( qName.equals ( "Item" ) ) {
            final KeyHandler subhandler = new KeyHandler ( this.root, this );
            this.root.pushSubHandler ( subhandler );
        } else {
            if ( !qName.equals ( "Value" ) ) {
                throw new SAXException ( "Expected <Item> or <Value>...found " + qName );
            }
            final ValueHandler subhandler2 = new ValueHandler ( this.root, this );
            this.root.pushSubHandler ( subhandler2 );
        }
    }
    @Override
    public void endElement ( final String namespaceURI, final String localName, final String qName ) {
        if ( this.parent instanceof PieDatasetHandler ) {
            final PieDatasetHandler handler = ( PieDatasetHandler ) this.parent;
            handler.addItem ( this.key, this.value );
            this.root.popSubHandler();
        } else if ( this.parent instanceof CategorySeriesHandler ) {
            final CategorySeriesHandler handler2 = ( CategorySeriesHandler ) this.parent;
            handler2.addItem ( this.key, this.value );
            this.root.popSubHandler();
        }
    }
}
