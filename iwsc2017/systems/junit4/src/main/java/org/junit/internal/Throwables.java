package org.junit.internal;
import java.io.PrintWriter;
import java.io.StringWriter;
public final class Throwables {
    private Throwables() {
    }
    public static Exception rethrowAsException ( Throwable e ) throws Exception {
        Throwables.<Exception>rethrow ( e );
        return null;
    }
    @SuppressWarnings ( "unchecked" )
    private static <T extends Throwable> void rethrow ( Throwable e ) throws T {
        throw ( T ) e;
    }
    public static String getStacktrace ( Throwable exception ) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter ( stringWriter );
        exception.printStackTrace ( writer );
        return stringWriter.toString();
    }
}
