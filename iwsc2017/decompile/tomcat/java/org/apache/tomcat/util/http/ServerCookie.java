package org.apache.tomcat.util.http;
import org.apache.tomcat.util.buf.MessageBytes;
import java.io.Serializable;
public class ServerCookie implements Serializable {
    private static final long serialVersionUID = 1L;
    private final MessageBytes name;
    private final MessageBytes value;
    private final MessageBytes path;
    private final MessageBytes domain;
    private final MessageBytes comment;
    private int version;
    public ServerCookie() {
        this.name = MessageBytes.newInstance();
        this.value = MessageBytes.newInstance();
        this.path = MessageBytes.newInstance();
        this.domain = MessageBytes.newInstance();
        this.comment = MessageBytes.newInstance();
        this.version = 0;
    }
    public void recycle() {
        this.name.recycle();
        this.value.recycle();
        this.comment.recycle();
        this.path.recycle();
        this.domain.recycle();
        this.version = 0;
    }
    public MessageBytes getComment() {
        return this.comment;
    }
    public MessageBytes getDomain() {
        return this.domain;
    }
    public MessageBytes getPath() {
        return this.path;
    }
    public MessageBytes getName() {
        return this.name;
    }
    public MessageBytes getValue() {
        return this.value;
    }
    public int getVersion() {
        return this.version;
    }
    public void setVersion ( final int v ) {
        this.version = v;
    }
    @Override
    public String toString() {
        return "Cookie " + this.getName() + "=" + this.getValue() + " ; " + this.getVersion() + " " + this.getPath() + " " + this.getDomain();
    }
}
