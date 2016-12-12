package org.apache.coyote.http11.filters;
import java.nio.charset.StandardCharsets;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import java.io.IOException;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.http11.InputFilter;
public class VoidInputFilter implements InputFilter {
    protected static final String ENCODING_NAME = "void";
    protected static final ByteChunk ENCODING;
    @Override
    public int doRead ( final ApplicationBufferHandler handler ) throws IOException {
        return -1;
    }
    @Override
    public void setRequest ( final Request request ) {
    }
    @Override
    public void setBuffer ( final InputBuffer buffer ) {
    }
    @Override
    public void recycle() {
    }
    @Override
    public ByteChunk getEncodingName() {
        return VoidInputFilter.ENCODING;
    }
    @Override
    public long end() throws IOException {
        return 0L;
    }
    @Override
    public int available() {
        return 0;
    }
    @Override
    public boolean isFinished() {
        return true;
    }
    static {
        ( ENCODING = new ByteChunk() ).setBytes ( "void".getBytes ( StandardCharsets.ISO_8859_1 ), 0, "void".length() );
    }
}
