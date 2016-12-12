package org.apache.tomcat.dbcp.pool2.impl;
import java.util.Date;
import java.text.SimpleDateFormat;
static class AbandonedObjectCreatedException extends Exception {
    private static final long serialVersionUID = 7398692158058772916L;
    private static final SimpleDateFormat format;
    private final long _createdTime;
    public AbandonedObjectCreatedException() {
        this._createdTime = System.currentTimeMillis();
    }
    @Override
    public String getMessage() {
        final String msg;
        synchronized ( AbandonedObjectCreatedException.format ) {
            msg = AbandonedObjectCreatedException.format.format ( new Date ( this._createdTime ) );
        }
        return msg;
    }
    static {
        format = new SimpleDateFormat ( "'Pooled object created' yyyy-MM-dd HH:mm:ss Z 'by the following code has not been returned to the pool:'" );
    }
}
