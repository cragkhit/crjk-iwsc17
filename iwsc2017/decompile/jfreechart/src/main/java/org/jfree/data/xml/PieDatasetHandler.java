package org.jfree.data.xml;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.jfree.data.general.PieDataset;
import org.jfree.data.general.DefaultPieDataset;
public class PieDatasetHandler extends RootHandler implements DatasetTags {
    private DefaultPieDataset dataset;
    public PieDatasetHandler() {
        this.dataset = null;
    }
    public PieDataset getDataset() {
        return this.dataset;
    }
    public void addItem ( final Comparable key, final Number value ) {
        this.dataset.setValue ( key, value );
    }
    @Override
    public void startElement ( final String namespaceURI, final String localName, final String qName, final Attributes atts ) throws SAXException {
        final DefaultHandler current = this.getCurrentHandler();
        if ( current != this ) {
            current.startElement ( namespaceURI, localName, qName, atts );
        } else if ( qName.equals ( "PieDataset" ) ) {
            this.dataset = new DefaultPieDataset();
        } else if ( qName.equals ( "Item" ) ) {
            final ItemHandler subhandler = new ItemHandler ( this, this );
            this.getSubHandlers().push ( subhandler );
            subhandler.startElement ( namespaceURI, localName, qName, atts );
        }
    }
    @Override
    public void endElement ( final String namespaceURI, final String localName, final String qName ) throws SAXException {
        final DefaultHandler current = this.getCurrentHandler();
        if ( current != this ) {
            current.endElement ( namespaceURI, localName, qName );
        }
    }
}
