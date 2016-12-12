package org.jfree.data.xml;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
public class CategoryDatasetHandler extends RootHandler implements DatasetTags {
    private DefaultCategoryDataset dataset;
    public CategoryDatasetHandler() {
        this.dataset = null;
    }
    public CategoryDataset getDataset() {
        return this.dataset;
    }
    public void addItem ( final Comparable rowKey, final Comparable columnKey, final Number value ) {
        this.dataset.addValue ( value, rowKey, columnKey );
    }
    @Override
    public void startElement ( final String namespaceURI, final String localName, final String qName, final Attributes atts ) throws SAXException {
        final DefaultHandler current = this.getCurrentHandler();
        if ( current != this ) {
            current.startElement ( namespaceURI, localName, qName, atts );
        } else if ( qName.equals ( "CategoryDataset" ) ) {
            this.dataset = new DefaultCategoryDataset();
        } else {
            if ( !qName.equals ( "Series" ) ) {
                throw new SAXException ( "Element not recognised: " + qName );
            }
            final CategorySeriesHandler subhandler = new CategorySeriesHandler ( this );
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
