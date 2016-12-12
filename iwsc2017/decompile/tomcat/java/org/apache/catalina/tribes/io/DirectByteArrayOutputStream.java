package org.apache.catalina.tribes.io;
import java.io.IOException;
import java.io.OutputStream;
public class DirectByteArrayOutputStream extends OutputStream {
    private final XByteBuffer buffer;
    public DirectByteArrayOutputStream ( final int size ) {
        this.buffer = new XByteBuffer ( size, false );
    }
    @Override
    public void write ( final int b ) throws IOException {
        this.buffer.append ( ( byte ) b );
    }
    public int size() {
        return this.buffer.getLength();
    }
    public byte[] getArrayDirect() {
        return this.buffer.getBytesDirect();
    }
    public byte[] getArray() {
        return this.buffer.getBytes();
    }
}
