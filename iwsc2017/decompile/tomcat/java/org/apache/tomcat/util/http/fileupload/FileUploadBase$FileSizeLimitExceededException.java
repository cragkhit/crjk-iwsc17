package org.apache.tomcat.util.http.fileupload;
public static class FileSizeLimitExceededException extends SizeException {
    private static final long serialVersionUID = 8150776562029630058L;
    private String fileName;
    private String fieldName;
    public FileSizeLimitExceededException ( final String message, final long actual, final long permitted ) {
        super ( message, actual, permitted );
    }
    public String getFileName() {
        return this.fileName;
    }
    public void setFileName ( final String pFileName ) {
        this.fileName = pFileName;
    }
    public String getFieldName() {
        return this.fieldName;
    }
    public void setFieldName ( final String pFieldName ) {
        this.fieldName = pFieldName;
    }
}
