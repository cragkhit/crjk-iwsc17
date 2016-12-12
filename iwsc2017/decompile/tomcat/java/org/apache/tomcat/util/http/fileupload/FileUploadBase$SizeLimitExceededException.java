package org.apache.tomcat.util.http.fileupload;
public static class SizeLimitExceededException extends SizeException {
    private static final long serialVersionUID = -2474893167098052828L;
    public SizeLimitExceededException ( final String message, final long actual, final long permitted ) {
        super ( message, actual, permitted );
    }
}
