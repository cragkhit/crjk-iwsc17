package org.apache.tomcat.util.http;
import java.io.Serializable;
import org.apache.tomcat.util.buf.MessageBytes;
public class ServerCookie implements Serializable {
    private static final long serialVersionUID = 1L;
    private final MessageBytes name = MessageBytes.newInstance();
    private final MessageBytes value = MessageBytes.newInstance();
    private final MessageBytes path = MessageBytes.newInstance();
    private final MessageBytes domain = MessageBytes.newInstance();
    private final MessageBytes comment = MessageBytes.newInstance();
    private int version = 0;
    public ServerCookie() {
    }
    public void recycle() {
        name.recycle();
        value.recycle();
        comment.recycle();
        path.recycle();
        domain.recycle();
        version = 0;
    }
    public MessageBytes getComment() {
        return comment;
    }
    public MessageBytes getDomain() {
        return domain;
    }
    public MessageBytes getPath() {
        return path;
    }
    public MessageBytes getName() {
        return name;
    }
    public MessageBytes getValue() {
        return value;
    }
    public int getVersion() {
        return version;
    }
    public void setVersion ( int v ) {
        version = v;
    }
    @Override
    public String toString() {
        return "Cookie " + getName() + "=" + getValue() + " ; "
               + getVersion() + " " + getPath() + " " + getDomain();
    }
}
