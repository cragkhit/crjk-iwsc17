package org.jfree.chart.urls;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
public class URLUtilities {
    private static final Class[] STRING_ARGS_2;
    public static String encode ( final String s, final String encoding ) {
        final Class c = URLEncoder.class;
        String result = null;
        try {
            final Method m = c.getDeclaredMethod ( "encode", ( Class[] ) URLUtilities.STRING_ARGS_2 );
            try {
                result = ( String ) m.invoke ( null, s, encoding );
            } catch ( InvocationTargetException e ) {
                e.printStackTrace();
            } catch ( IllegalAccessException e2 ) {
                e2.printStackTrace();
            }
        } catch ( NoSuchMethodException e3 ) {
            result = URLEncoder.encode ( s );
        }
        return result;
    }
    static {
        STRING_ARGS_2 = new Class[] { String.class, String.class };
    }
}
