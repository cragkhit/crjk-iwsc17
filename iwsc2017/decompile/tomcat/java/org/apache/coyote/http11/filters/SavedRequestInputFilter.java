package org.apache.coyote.http11.filters;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.http11.InputFilter;
public class SavedRequestInputFilter implements InputFilter {
    protected ByteChunk input;
    public SavedRequestInputFilter ( final ByteChunk input ) {
        this.input = null;
        this.input = input;
    }
    @Override
    public int doRead ( final ApplicationBufferHandler handler ) throws IOException {
        if ( this.input.getOffset() >= this.input.getEnd() ) {
            return -1;
        }
        final ByteBuffer byteBuffer = handler.getByteBuffer();
        byteBuffer.position ( byteBuffer.limit() ).limit ( byteBuffer.capacity() );
        this.input.substract ( byteBuffer );
        return byteBuffer.remaining();
    }
    @Override
    public void setRequest ( final Request request ) {
        request.setContentLength ( this.input.getLength() );
    }
    @Override
    public void recycle() {
        this.input = null;
    }
    @Override
    public ByteChunk getEncodingName() {
        return null;
    }
    @Override
    public void setBuffer ( final InputBuffer buffer ) {
    }
    @Override
    public int available() {
        return this.input.getLength();
    }
    @Override
    public long end() throws IOException {
        return 0L;
    }
    @Override
    public boolean isFinished() {
        return this.input.getOffset() >= this.input.getEnd();
    }
}
