package org.junit.runner.notification;
import org.junit.internal.Throwables;
import org.junit.runner.Description;
import java.io.Serializable;
public class Failure implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Description fDescription;
    private final Throwable fThrownException;
    public Failure ( final Description description, final Throwable thrownException ) {
        this.fThrownException = thrownException;
        this.fDescription = description;
    }
    public String getTestHeader() {
        return this.fDescription.getDisplayName();
    }
    public Description getDescription() {
        return this.fDescription;
    }
    public Throwable getException() {
        return this.fThrownException;
    }
    public String toString() {
        return this.getTestHeader() + ": " + this.fThrownException.getMessage();
    }
    public String getTrace() {
        return Throwables.getStacktrace ( this.getException() );
    }
    public String getMessage() {
        return this.getException().getMessage();
    }
}
