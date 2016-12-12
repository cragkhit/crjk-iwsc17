package org.apache.catalina.tribes.group;
import java.io.Serializable;
import org.apache.catalina.tribes.Member;
public interface ExtendedRpcCallback extends RpcCallback {
    public void replyFailed ( Serializable request, Serializable response, Member sender, Exception reason );
    public void replySucceeded ( Serializable request, Serializable response, Member sender );
}
