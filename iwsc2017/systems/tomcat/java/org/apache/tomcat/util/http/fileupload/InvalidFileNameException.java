package org.apache.tomcat.util.http.fileupload;
public class InvalidFileNameException extends RuntimeException {
    private static final long serialVersionUID = 7922042602454350470L;
    private final String name;
    public InvalidFileNameException ( String pName, String pMessage ) {
        super ( pMessage );
        name = pName;
    }
    public String getName() {
        return name;
    }
}
