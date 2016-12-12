package org.apache.catalina.tribes.group;
import java.io.Serializable;
import org.apache.catalina.tribes.Member;
public interface RpcCallback {
    public Serializable replyRequest ( Serializable msg, Member sender );
    public void leftOver ( Serializable msg, Member sender );
}
