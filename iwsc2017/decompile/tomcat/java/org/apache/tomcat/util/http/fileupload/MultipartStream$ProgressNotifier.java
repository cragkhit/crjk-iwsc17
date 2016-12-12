package org.apache.tomcat.util.http.fileupload;
public static class ProgressNotifier {
    private final ProgressListener listener;
    private final long contentLength;
    private long bytesRead;
    private int items;
    ProgressNotifier ( final ProgressListener pListener, final long pContentLength ) {
        this.listener = pListener;
        this.contentLength = pContentLength;
    }
    void noteBytesRead ( final int pBytes ) {
        this.bytesRead += pBytes;
        this.notifyListener();
    }
    void noteItem() {
        ++this.items;
        this.notifyListener();
    }
    private void notifyListener() {
        if ( this.listener != null ) {
            this.listener.update ( this.bytesRead, this.contentLength, this.items );
        }
    }
}
