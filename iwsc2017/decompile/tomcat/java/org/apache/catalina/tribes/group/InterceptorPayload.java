package org.apache.catalina.tribes.group;
import org.apache.catalina.tribes.ErrorHandler;
public class InterceptorPayload {
    private ErrorHandler errorHandler;
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }
    public void setErrorHandler ( final ErrorHandler errorHandler ) {
        this.errorHandler = errorHandler;
    }
}
