package org.jfree.data.io;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.CategoryDataset;
import java.io.Reader;
public class CSV {
    private char fieldDelimiter;
    private char textDelimiter;
    public CSV() {
        this ( ',', '\"' );
    }
    public CSV ( final char fieldDelimiter, final char textDelimiter ) {
        this.fieldDelimiter = fieldDelimiter;
        this.textDelimiter = textDelimiter;
    }
    public CategoryDataset readCategoryDataset ( final Reader in ) throws IOException {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final BufferedReader reader = new BufferedReader ( in );
        List columnKeys = null;
        int lineIndex = 0;
        for ( String line = reader.readLine(); line != null; line = reader.readLine(), ++lineIndex ) {
            if ( lineIndex == 0 ) {
                columnKeys = this.extractColumnKeys ( line );
            } else {
                this.extractRowKeyAndData ( line, dataset, columnKeys );
            }
        }
        return dataset;
    }
    private List extractColumnKeys ( final String line ) {
        final List keys = new ArrayList();
        int fieldIndex = 0;
        int start = 0;
        for ( int i = 0; i < line.length(); ++i ) {
            if ( line.charAt ( i ) == this.fieldDelimiter ) {
                if ( fieldIndex > 0 ) {
                    final String key = line.substring ( start, i );
                    keys.add ( this.removeStringDelimiters ( key ) );
                }
                start = i + 1;
                ++fieldIndex;
            }
        }
        final String key2 = line.substring ( start, line.length() );
        keys.add ( this.removeStringDelimiters ( key2 ) );
        return keys;
    }
    private void extractRowKeyAndData ( final String line, final DefaultCategoryDataset dataset, final List columnKeys ) {
        Comparable rowKey = null;
        int fieldIndex = 0;
        int start = 0;
        for ( int i = 0; i < line.length(); ++i ) {
            if ( line.charAt ( i ) == this.fieldDelimiter ) {
                if ( fieldIndex == 0 ) {
                    final String key = line.substring ( start, i );
                    rowKey = this.removeStringDelimiters ( key );
                } else {
                    final Double value = Double.valueOf ( this.removeStringDelimiters ( line.substring ( start, i ) ) );
                    dataset.addValue ( value, rowKey, columnKeys.get ( fieldIndex - 1 ) );
                }
                start = i + 1;
                ++fieldIndex;
            }
        }
        final Double value2 = Double.valueOf ( this.removeStringDelimiters ( line.substring ( start, line.length() ) ) );
        dataset.addValue ( value2, rowKey, columnKeys.get ( fieldIndex - 1 ) );
    }
    private String removeStringDelimiters ( final String key ) {
        String k = key.trim();
        if ( k.charAt ( 0 ) == this.textDelimiter ) {
            k = k.substring ( 1 );
        }
        if ( k.charAt ( k.length() - 1 ) == this.textDelimiter ) {
            k = k.substring ( 0, k.length() - 1 );
        }
        return k;
    }
}
