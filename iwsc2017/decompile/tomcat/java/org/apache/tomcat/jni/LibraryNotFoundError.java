package org.apache.tomcat.jni;
public class LibraryNotFoundError extends UnsatisfiedLinkError {
    private static final long serialVersionUID = 1L;
    private final String libraryNames;
    public LibraryNotFoundError ( final String libraryNames, final String errors ) {
        super ( errors );
        this.libraryNames = libraryNames;
    }
    public String getLibraryNames() {
        return this.libraryNames;
    }
}
