package org.apache.catalina.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
public class IOTools {
    protected static final int DEFAULT_BUFFER_SIZE = 4 * 1024;
    private IOTools() {
    }
    public static void flow ( Reader reader, Writer writer, char[] buf )
    throws IOException {
        int numRead;
        while ( ( numRead = reader.read ( buf ) ) >= 0 ) {
            writer.write ( buf, 0, numRead );
        }
    }
    public static void flow ( Reader reader, Writer writer )
    throws IOException {
        char[] buf = new char[DEFAULT_BUFFER_SIZE];
        flow ( reader, writer, buf );
    }
    public static void flow ( InputStream is, OutputStream os ) throws IOException {
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        int numRead;
        while ( ( numRead = is.read ( buf ) ) >= 0 ) {
            os.write ( buf, 0, numRead );
        }
    }
}
