package org.apache.coyote.http11.filters;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Response;
import org.apache.coyote.http11.OutputFilter;
public class VoidOutputFilter implements OutputFilter {
    @Override
    public int doWrite ( ByteBuffer chunk ) throws IOException {
        return chunk.remaining();
    }
    @Override
    public long getBytesWritten() {
        return 0;
    }
    @Override
    public void setResponse ( Response response ) {
    }
    @Override
    public void setBuffer ( OutputBuffer buffer ) {
    }
    @Override
    public void recycle() {
    }
    @Override
    public long end()
    throws IOException {
        return 0;
    }
}
