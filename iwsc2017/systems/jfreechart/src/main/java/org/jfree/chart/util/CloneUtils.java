package org.jfree.chart.util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jfree.util.ObjectUtilities;
public class CloneUtils {
    public static List<?> cloneList ( List<?> source ) {
        ParamChecks.nullNotPermitted ( source, "source" );
        List result = new ArrayList();
        for ( Object obj : source ) {
            if ( obj != null ) {
                try {
                    result.add ( ObjectUtilities.clone ( obj ) );
                } catch ( CloneNotSupportedException ex ) {
                    throw new RuntimeException ( ex );
                }
            } else {
                result.add ( null );
            }
        }
        return result;
    }
    public static Map cloneMapValues ( Map source ) {
        ParamChecks.nullNotPermitted ( source, "source" );
        Map result = new HashMap();
        for ( Object key : source.keySet() ) {
            Object value = source.get ( key );
            if ( value != null ) {
                try {
                    result.put ( key, ObjectUtilities.clone ( value ) );
                } catch ( CloneNotSupportedException ex ) {
                    throw new RuntimeException ( ex );
                }
            } else {
                result.put ( key, null );
            }
        }
        return result;
    }
}
