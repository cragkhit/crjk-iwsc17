package org.apache.catalina.tribes.group;
import org.apache.catalina.tribes.Member;
import java.io.Serializable;
public interface ExtendedRpcCallback extends RpcCallback {
    void replyFailed ( Serializable p0, Serializable p1, Member p2, Exception p3 );
    void replySucceeded ( Serializable p0, Serializable p1, Member p2 );
}
