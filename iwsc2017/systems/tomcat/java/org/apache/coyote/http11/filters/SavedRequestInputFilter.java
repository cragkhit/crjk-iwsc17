package org.apache.coyote.http11.filters;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.http11.InputFilter;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
public class SavedRequestInputFilter implements InputFilter {
    protected ByteChunk input = null;
    public SavedRequestInputFilter ( ByteChunk input ) {
        this.input = input;
    }
    @Override
    public int doRead ( ApplicationBufferHandler handler ) throws IOException {
        if ( input.getOffset() >= input.getEnd() ) {
            return -1;
        }
        ByteBuffer byteBuffer = handler.getByteBuffer();
        byteBuffer.position ( byteBuffer.limit() ).limit ( byteBuffer.capacity() );
        input.substract ( byteBuffer );
        return byteBuffer.remaining();
    }
    @Override
    public void setRequest ( org.apache.coyote.Request request ) {
        request.setContentLength ( input.getLength() );
    }
    @Override
    public void recycle() {
        input = null;
    }
    @Override
    public ByteChunk getEncodingName() {
        return null;
    }
    @Override
    public void setBuffer ( InputBuffer buffer ) {
    }
    @Override
    public int available() {
        return input.getLength();
    }
    @Override
    public long end() throws IOException {
        return 0;
    }
    @Override
    public boolean isFinished() {
        return input.getOffset() >= input.getEnd();
    }
}
