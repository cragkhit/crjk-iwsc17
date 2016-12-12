package org.apache.catalina.core;
import java.io.IOException;
import javax.servlet.AsyncEvent;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.AsyncListener;
public class AsyncListenerWrapper {
    private AsyncListener listener;
    private ServletRequest servletRequest;
    private ServletResponse servletResponse;
    public AsyncListenerWrapper() {
        this.listener = null;
        this.servletRequest = null;
        this.servletResponse = null;
    }
    public void fireOnStartAsync ( final AsyncEvent event ) throws IOException {
        this.listener.onStartAsync ( this.customizeEvent ( event ) );
    }
    public void fireOnComplete ( final AsyncEvent event ) throws IOException {
        this.listener.onComplete ( this.customizeEvent ( event ) );
    }
    public void fireOnTimeout ( final AsyncEvent event ) throws IOException {
        this.listener.onTimeout ( this.customizeEvent ( event ) );
    }
    public void fireOnError ( final AsyncEvent event ) throws IOException {
        this.listener.onError ( this.customizeEvent ( event ) );
    }
    public AsyncListener getListener() {
        return this.listener;
    }
    public void setListener ( final AsyncListener listener ) {
        this.listener = listener;
    }
    public void setServletRequest ( final ServletRequest servletRequest ) {
        this.servletRequest = servletRequest;
    }
    public void setServletResponse ( final ServletResponse servletResponse ) {
        this.servletResponse = servletResponse;
    }
    private AsyncEvent customizeEvent ( final AsyncEvent event ) {
        if ( this.servletRequest != null && this.servletResponse != null ) {
            return new AsyncEvent ( event.getAsyncContext(), this.servletRequest, this.servletResponse, event.getThrowable() );
        }
        return event;
    }
}
