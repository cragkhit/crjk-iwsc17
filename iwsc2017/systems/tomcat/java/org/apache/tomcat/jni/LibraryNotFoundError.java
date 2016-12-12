package org.apache.tomcat.jni;
public class LibraryNotFoundError extends UnsatisfiedLinkError {
    private static final long serialVersionUID = 1L;
    private final String libraryNames;
    public LibraryNotFoundError ( String libraryNames, String errors ) {
        super ( errors );
        this.libraryNames = libraryNames;
    }
    public String getLibraryNames() {
        return libraryNames;
    }
}
