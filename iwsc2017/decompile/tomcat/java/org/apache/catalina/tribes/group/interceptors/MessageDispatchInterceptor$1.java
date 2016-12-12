package org.apache.catalina.tribes.group.interceptors;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.ChannelMessage;
class MessageDispatchInterceptor$1 implements Runnable {
    final   ChannelMessage val$msg;
    final   Member[] val$destination;
    final   InterceptorPayload val$payload;
    @Override
    public void run() {
        MessageDispatchInterceptor.this.sendAsyncData ( this.val$msg, this.val$destination, this.val$payload );
    }
}
