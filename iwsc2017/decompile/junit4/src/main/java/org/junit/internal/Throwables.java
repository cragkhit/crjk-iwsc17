package org.junit.internal;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
public final class Throwables {
    public static Exception rethrowAsException ( final Throwable e ) throws Exception {
        rethrow ( e );
        return null;
    }
    private static <T extends Throwable> void rethrow ( final Throwable e ) throws T, Throwable {
        throw e;
    }
    public static String getStacktrace ( final Throwable exception ) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter ( stringWriter );
        exception.printStackTrace ( writer );
        return stringWriter.toString();
    }
}
