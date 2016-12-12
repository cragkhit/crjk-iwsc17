package org.apache.tomcat.util.http.fileupload;
public abstract static class SizeException extends FileUploadException {
    private static final long serialVersionUID = -8776225574705254126L;
    private final long actual;
    private final long permitted;
    protected SizeException ( final String message, final long actual, final long permitted ) {
        super ( message );
        this.actual = actual;
        this.permitted = permitted;
    }
    public long getActualSize() {
        return this.actual;
    }
    public long getPermittedSize() {
        return this.permitted;
    }
}
