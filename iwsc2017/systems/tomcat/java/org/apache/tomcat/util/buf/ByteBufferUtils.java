package org.apache.tomcat.util.buf;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class ByteBufferUtils {
    private static final StringManager sm = StringManager.getManager ( ByteBufferUtils.class );
    private static final Log log = LogFactory.getLog ( ByteBufferUtils.class );
    private static final Method cleanerMethod;
    private static final Method cleanMethod;
    static {
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect ( 0 );
        Method cleanerMethodLocal = null;
        Method cleanMethodLocal = null;
        try {
            cleanerMethodLocal = tempBuffer.getClass().getMethod ( "cleaner" );
            cleanerMethodLocal.setAccessible ( true );
            Object cleanerObject = cleanerMethodLocal.invoke ( tempBuffer );
            if ( cleanerObject instanceof Runnable ) {
                cleanMethodLocal = Runnable.class.getMethod ( "run" );
            } else {
                cleanMethodLocal = cleanerObject.getClass().getMethod ( "clean" );
            }
            cleanMethodLocal.invoke ( cleanerObject );
        } catch ( IllegalAccessException | IllegalArgumentException
                      | InvocationTargetException | NoSuchMethodException | SecurityException e ) {
            log.warn ( sm.getString ( "byteBufferUtils.cleaner" ), e );
            cleanerMethodLocal = null;
            cleanMethodLocal = null;
        }
        cleanerMethod = cleanerMethodLocal;
        cleanMethod = cleanMethodLocal;
    }
    private ByteBufferUtils() {
    }
    public static ByteBuffer expand ( ByteBuffer in, int newSize ) {
        if ( in.capacity() >= newSize ) {
            return in;
        }
        ByteBuffer out;
        boolean direct = false;
        if ( in.isDirect() ) {
            out = ByteBuffer.allocateDirect ( newSize );
            direct = true;
        } else {
            out = ByteBuffer.allocate ( newSize );
        }
        in.flip();
        out.put ( in );
        if ( direct ) {
            cleanDirectBuffer ( in );
        }
        return out;
    }
    public static void cleanDirectBuffer ( ByteBuffer buf ) {
        if ( cleanMethod != null ) {
            try {
                cleanMethod.invoke ( cleanerMethod.invoke ( buf ) );
            } catch ( IllegalAccessException | IllegalArgumentException
                          | InvocationTargetException | SecurityException e ) {
            }
        }
    }
}
