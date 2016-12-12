package org.apache.catalina.tribes.group;
import org.apache.catalina.tribes.Member;
import java.io.Serializable;
public interface RpcCallback {
    Serializable replyRequest ( Serializable p0, Member p1 );
    void leftOver ( Serializable p0, Member p1 );
}
