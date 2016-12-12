package org.apache.tomcat.util.log;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
class CaptureLog {
    protected CaptureLog() {
        baos = new ByteArrayOutputStream();
        ps = new PrintStream ( baos );
    }
    private final ByteArrayOutputStream baos;
    private final PrintStream ps;
    protected PrintStream getStream() {
        return ps;
    }
    protected void reset() {
        baos.reset();
    }
    protected String getCapture() {
        return baos.toString();
    }
}
