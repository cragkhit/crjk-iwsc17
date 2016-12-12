package org.apache.jasper.tagplugins.jstl;
import javax.servlet.WriteListener;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
class Util$ImportResponseWrapper$1 extends ServletOutputStream {
    public void write ( final int b ) throws IOException {
        ImportResponseWrapper.access$000 ( ImportResponseWrapper.this ).write ( b );
    }
    public boolean isReady() {
        return false;
    }
    public void setWriteListener ( final WriteListener listener ) {
        throw new UnsupportedOperationException();
    }
}
