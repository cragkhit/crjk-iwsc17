package org.apache.catalina.tribes.group;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.Member;
import java.io.Serializable;
import org.apache.catalina.tribes.ErrorHandler;
class RpcChannel$1 implements ErrorHandler {
    final   ExtendedRpcCallback val$excallback;
    final   Serializable val$request;
    final   Serializable val$response;
    final   Member val$fsender;
    @Override
    public void handleError ( final ChannelException x, final UniqueId id ) {
        this.val$excallback.replyFailed ( this.val$request, this.val$response, this.val$fsender, x );
    }
    @Override
    public void handleCompletion ( final UniqueId id ) {
        this.val$excallback.replySucceeded ( this.val$request, this.val$response, this.val$fsender );
    }
}
