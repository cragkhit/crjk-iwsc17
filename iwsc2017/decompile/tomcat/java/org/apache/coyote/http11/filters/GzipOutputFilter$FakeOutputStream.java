package org.apache.coyote.http11.filters;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.OutputStream;
protected class FakeOutputStream extends OutputStream {
    protected final ByteBuffer outputChunk;
    protected FakeOutputStream() {
        this.outputChunk = ByteBuffer.allocate ( 1 );
    }
    @Override
    public void write ( final int b ) throws IOException {
        this.outputChunk.put ( 0, ( byte ) ( b & 0xFF ) );
        GzipOutputFilter.this.buffer.doWrite ( this.outputChunk );
    }
    @Override
    public void write ( final byte[] b, final int off, final int len ) throws IOException {
        GzipOutputFilter.this.buffer.doWrite ( ByteBuffer.wrap ( b, off, len ) );
    }
    @Override
    public void flush() throws IOException {
    }
    @Override
    public void close() throws IOException {
    }
}
