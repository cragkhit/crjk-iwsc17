package org.apache.coyote.http11;
import java.io.IOException;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Response;
public interface OutputFilter extends OutputBuffer {
    public void setResponse ( Response response );
    public void recycle();
    public void setBuffer ( OutputBuffer buffer );
    public long end() throws IOException;
}
