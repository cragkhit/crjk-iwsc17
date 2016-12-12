package org.apache.catalina.tribes;
public interface InterceptorEvent {
    int getEventType();
    String getEventTypeDesc();
    ChannelInterceptor getInterceptor();
}
