package org.apache.catalina.tribes.group;
import org.apache.catalina.tribes.ChannelInterceptor;
import java.util.Iterator;
public static class InterceptorIterator implements Iterator<ChannelInterceptor> {
    private final ChannelInterceptor end;
    private ChannelInterceptor start;
    public InterceptorIterator ( final ChannelInterceptor start, final ChannelInterceptor end ) {
        this.end = end;
        this.start = start;
    }
    @Override
    public boolean hasNext() {
        return this.start != null && this.start != this.end;
    }
    @Override
    public ChannelInterceptor next() {
        ChannelInterceptor result = null;
        if ( this.hasNext() ) {
            result = this.start;
            this.start = this.start.getNext();
        }
        return result;
    }
    @Override
    public void remove() {
    }
}
