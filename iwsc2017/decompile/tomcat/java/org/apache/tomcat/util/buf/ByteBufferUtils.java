package org.apache.tomcat.util.buf;
import org.apache.juli.logging.LogFactory;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.lang.reflect.Method;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
public class ByteBufferUtils {
    private static final StringManager sm;
    private static final Log log;
    private static final Method cleanerMethod;
    private static final Method cleanMethod;
    public static ByteBuffer expand ( final ByteBuffer in, final int newSize ) {
        if ( in.capacity() >= newSize ) {
            return in;
        }
        boolean direct = false;
        ByteBuffer out;
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
    public static void cleanDirectBuffer ( final ByteBuffer buf ) {
        if ( ByteBufferUtils.cleanMethod != null ) {
            try {
                ByteBufferUtils.cleanMethod.invoke ( ByteBufferUtils.cleanerMethod.invoke ( buf, new Object[0] ), new Object[0] );
            } catch ( IllegalAccessException ) {}
            catch ( IllegalArgumentException ) {}
            catch ( InvocationTargetException ) {}
            catch ( SecurityException ex ) {}
        }
    }
    static {
        sm = StringManager.getManager ( ByteBufferUtils.class );
        log = LogFactory.getLog ( ByteBufferUtils.class );
        final ByteBuffer tempBuffer = ByteBuffer.allocateDirect ( 0 );
        Method cleanerMethodLocal = null;
        Method cleanMethodLocal = null;
        try {
            cleanerMethodLocal = tempBuffer.getClass().getMethod ( "cleaner", ( Class<?>[] ) new Class[0] );
            cleanerMethodLocal.setAccessible ( true );
            final Object cleanerObject = cleanerMethodLocal.invoke ( tempBuffer, new Object[0] );
            if ( cleanerObject instanceof Runnable ) {
                cleanMethodLocal = Runnable.class.getMethod ( "run", ( Class<?>[] ) new Class[0] );
            } else {
                cleanMethodLocal = cleanerObject.getClass().getMethod ( "clean", ( Class<?>[] ) new Class[0] );
            }
            cleanMethodLocal.invoke ( cleanerObject, new Object[0] );
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e ) {
            ByteBufferUtils.log.warn ( ByteBufferUtils.sm.getString ( "byteBufferUtils.cleaner" ), e );
            cleanerMethodLocal = null;
            cleanMethodLocal = null;
        }
        cleanerMethod = cleanerMethodLocal;
        cleanMethod = cleanMethodLocal;
    }
}
