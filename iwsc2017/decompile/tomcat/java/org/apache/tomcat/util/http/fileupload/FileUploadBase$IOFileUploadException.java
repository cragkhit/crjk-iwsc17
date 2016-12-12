package org.apache.tomcat.util.http.fileupload;
public static class IOFileUploadException extends FileUploadException {
    private static final long serialVersionUID = -5858565745868986701L;
    public IOFileUploadException() {
    }
    public IOFileUploadException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
    public IOFileUploadException ( final String message ) {
        super ( message );
    }
    public IOFileUploadException ( final Throwable cause ) {
        super ( cause );
    }
}
