package org.apache.catalina.tribes.group;
import java.io.Serializable;
import org.apache.catalina.tribes.Member;
public class Response {
    private Member source;
    private Serializable message;
    public Response() {
    }
    public Response ( final Member source, final Serializable message ) {
        this.source = source;
        this.message = message;
    }
    public void setSource ( final Member source ) {
        this.source = source;
    }
    public void setMessage ( final Serializable message ) {
        this.message = message;
    }
    public Member getSource() {
        return this.source;
    }
    public Serializable getMessage() {
        return this.message;
    }
}
