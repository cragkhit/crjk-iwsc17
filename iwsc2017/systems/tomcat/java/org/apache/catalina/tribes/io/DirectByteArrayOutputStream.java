package org.apache.catalina.tribes.io;
import java.io.IOException;
import java.io.OutputStream;
public class DirectByteArrayOutputStream extends OutputStream {
    private final XByteBuffer buffer;
    public DirectByteArrayOutputStream ( int size ) {
        buffer = new XByteBuffer ( size, false );
    }
    @Override
    public void write ( int b ) throws IOException {
        buffer.append ( ( byte ) b );
    }
    public int size() {
        return buffer.getLength();
    }
    public byte[] getArrayDirect() {
        return buffer.getBytesDirect();
    }
    public byte[] getArray() {
        return buffer.getBytes();
    }
}
