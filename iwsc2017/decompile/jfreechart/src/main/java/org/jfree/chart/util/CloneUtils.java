package org.jfree.chart.util;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import org.jfree.util.ObjectUtilities;
import java.util.ArrayList;
import java.util.List;
public class CloneUtils {
    public static List<?> cloneList ( final List<?> source ) {
        ParamChecks.nullNotPermitted ( source, "source" );
        final List result = new ArrayList();
        for ( final Object obj : source ) {
            if ( obj != null ) {
                try {
                    result.add ( ObjectUtilities.clone ( obj ) );
                    continue;
                } catch ( CloneNotSupportedException ex ) {
                    throw new RuntimeException ( ex );
                }
            }
            result.add ( null );
        }
        return ( List<?> ) result;
    }
    public static Map cloneMapValues ( final Map source ) {
        ParamChecks.nullNotPermitted ( source, "source" );
        final Map result = new HashMap();
        for ( final Object key : source.keySet() ) {
            final Object value = source.get ( key );
            if ( value != null ) {
                try {
                    result.put ( key, ObjectUtilities.clone ( value ) );
                    continue;
                } catch ( CloneNotSupportedException ex ) {
                    throw new RuntimeException ( ex );
                }
            }
            result.put ( key, null );
        }
        return result;
    }
}
