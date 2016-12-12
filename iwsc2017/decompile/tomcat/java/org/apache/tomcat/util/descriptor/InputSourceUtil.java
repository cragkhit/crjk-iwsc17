package org.apache.tomcat.util.descriptor;
import java.io.InputStream;
import org.apache.tomcat.util.ExceptionUtils;
import org.xml.sax.InputSource;
public final class InputSourceUtil {
    public static void close ( final InputSource inputSource ) {
        if ( inputSource == null ) {
            return;
        }
        final InputStream is = inputSource.getByteStream();
        if ( is != null ) {
            try {
                is.close();
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
            }
        }
    }
}
