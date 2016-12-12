package org.apache.catalina.util;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Writer;
import java.io.Reader;
public class IOTools {
    protected static final int DEFAULT_BUFFER_SIZE = 4096;
    public static void flow ( final Reader reader, final Writer writer, final char[] buf ) throws IOException {
        int numRead;
        while ( ( numRead = reader.read ( buf ) ) >= 0 ) {
            writer.write ( buf, 0, numRead );
        }
    }
    public static void flow ( final Reader reader, final Writer writer ) throws IOException {
        final char[] buf = new char[4096];
        flow ( reader, writer, buf );
    }
    public static void flow ( final InputStream is, final OutputStream os ) throws IOException {
        final byte[] buf = new byte[4096];
        int numRead;
        while ( ( numRead = is.read ( buf ) ) >= 0 ) {
            os.write ( buf, 0, numRead );
        }
    }
}
