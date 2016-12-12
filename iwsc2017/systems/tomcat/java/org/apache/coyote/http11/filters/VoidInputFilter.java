package org.apache.coyote.http11.filters;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.http11.InputFilter;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
public class VoidInputFilter implements InputFilter {
    protected static final String ENCODING_NAME = "void";
    protected static final ByteChunk ENCODING = new ByteChunk();
    static {
        ENCODING.setBytes ( ENCODING_NAME.getBytes ( StandardCharsets.ISO_8859_1 ),
                            0, ENCODING_NAME.length() );
    }
    @Override
    public int doRead ( ApplicationBufferHandler handler ) throws IOException {
        return -1;
    }
    @Override
    public void setRequest ( Request request ) {
    }
    @Override
    public void setBuffer ( InputBuffer buffer ) {
    }
    @Override
    public void recycle() {
    }
    @Override
    public ByteChunk getEncodingName() {
        return ENCODING;
    }
    @Override
    public long end() throws IOException {
        return 0;
    }
    @Override
    public int available() {
        return 0;
    }
    @Override
    public boolean isFinished() {
        return true;
    }
}
