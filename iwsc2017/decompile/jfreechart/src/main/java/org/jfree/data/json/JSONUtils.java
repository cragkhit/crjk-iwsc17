package org.jfree.data.json;
import java.util.List;
import org.jfree.data.KeyedValues2D;
import java.util.Iterator;
import org.jfree.data.json.impl.JSONValue;
import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.KeyedValues;
public class JSONUtils {
    public static String writeKeyedValues ( final KeyedValues data ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        final StringWriter sw = new StringWriter();
        try {
            writeKeyedValues ( data, sw );
        } catch ( IOException ex ) {
            throw new RuntimeException ( ex );
        }
        return sw.toString();
    }
    public static void writeKeyedValues ( final KeyedValues data, final Writer writer ) throws IOException {
        ParamChecks.nullNotPermitted ( data, "data" );
        ParamChecks.nullNotPermitted ( writer, "writer" );
        writer.write ( "[" );
        boolean first = true;
        for ( final Comparable key : data.getKeys() ) {
            if ( !first ) {
                writer.write ( ", " );
            } else {
                first = false;
            }
            writer.write ( "[" );
            writer.write ( JSONValue.toJSONString ( key.toString() ) );
            writer.write ( ", " );
            writer.write ( JSONValue.toJSONString ( data.getValue ( key ) ) );
            writer.write ( "]" );
        }
        writer.write ( "]" );
    }
    public static String writeKeyedValues2D ( final KeyedValues2D data ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        final StringWriter sw = new StringWriter();
        try {
            writeKeyedValues2D ( data, sw );
        } catch ( IOException ex ) {
            throw new RuntimeException ( ex );
        }
        return sw.toString();
    }
    public static void writeKeyedValues2D ( final KeyedValues2D data, final Writer writer ) throws IOException {
        ParamChecks.nullNotPermitted ( data, "data" );
        ParamChecks.nullNotPermitted ( writer, "writer" );
        final List<Comparable<?>> columnKeys = ( List<Comparable<?>> ) data.getColumnKeys();
        final List<Comparable<?>> rowKeys = ( List<Comparable<?>> ) data.getRowKeys();
        writer.write ( "{" );
        if ( !columnKeys.isEmpty() ) {
            writer.write ( "\"columnKeys\": [" );
            boolean first = true;
            for ( final Comparable<?> columnKey : columnKeys ) {
                if ( !first ) {
                    writer.write ( ", " );
                } else {
                    first = false;
                }
                writer.write ( JSONValue.toJSONString ( columnKey.toString() ) );
            }
            writer.write ( "]" );
        }
        if ( !rowKeys.isEmpty() ) {
            writer.write ( ", \"rows\": [" );
            boolean firstRow = true;
            for ( final Comparable<?> rowKey : rowKeys ) {
                if ( !firstRow ) {
                    writer.write ( ", [" );
                } else {
                    writer.write ( "[" );
                    firstRow = false;
                }
                writer.write ( JSONValue.toJSONString ( rowKey.toString() ) );
                writer.write ( ", [" );
                boolean first2 = true;
                for ( final Comparable<?> columnKey2 : columnKeys ) {
                    if ( !first2 ) {
                        writer.write ( ", " );
                    } else {
                        first2 = false;
                    }
                    writer.write ( JSONValue.toJSONString ( data.getValue ( rowKey, columnKey2 ) ) );
                }
                writer.write ( "]]" );
            }
            writer.write ( "]" );
        }
        writer.write ( "}" );
    }
}
