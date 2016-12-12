package org.jfree.data.xml;
import org.jfree.data.category.CategoryDataset;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import org.jfree.data.general.PieDataset;
import java.io.File;
public class DatasetReader {
    public static PieDataset readPieDatasetFromXML ( final File file ) throws IOException {
        final InputStream in = new FileInputStream ( file );
        return readPieDatasetFromXML ( in );
    }
    public static PieDataset readPieDatasetFromXML ( final InputStream in ) throws IOException {
        PieDataset result = null;
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            final SAXParser parser = factory.newSAXParser();
            final PieDatasetHandler handler = new PieDatasetHandler();
            parser.parse ( in, handler );
            result = handler.getDataset();
        } catch ( SAXException e ) {
            System.out.println ( e.getMessage() );
        } catch ( ParserConfigurationException e2 ) {
            System.out.println ( e2.getMessage() );
        }
        return result;
    }
    public static CategoryDataset readCategoryDatasetFromXML ( final File file ) throws IOException {
        final InputStream in = new FileInputStream ( file );
        return readCategoryDatasetFromXML ( in );
    }
    public static CategoryDataset readCategoryDatasetFromXML ( final InputStream in ) throws IOException {
        CategoryDataset result = null;
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            final SAXParser parser = factory.newSAXParser();
            final CategoryDatasetHandler handler = new CategoryDatasetHandler();
            parser.parse ( in, handler );
            result = handler.getDataset();
        } catch ( SAXException e ) {
            System.out.println ( e.getMessage() );
        } catch ( ParserConfigurationException e2 ) {
            System.out.println ( e2.getMessage() );
        }
        return result;
    }
}
