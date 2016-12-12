package org.junit.runner.notification;
import java.io.Serializable;
import org.junit.internal.Throwables;
import org.junit.runner.Description;
public class Failure implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Description fDescription;
    private final Throwable fThrownException;
    public Failure ( Description description, Throwable thrownException ) {
        this.fThrownException = thrownException;
        this.fDescription = description;
    }
    public String getTestHeader() {
        return fDescription.getDisplayName();
    }
    public Description getDescription() {
        return fDescription;
    }
    public Throwable getException() {
        return fThrownException;
    }
    @Override
    public String toString() {
        return getTestHeader() + ": " + fThrownException.getMessage();
    }
    public String getTrace() {
        return Throwables.getStacktrace ( getException() );
    }
    public String getMessage() {
        return getException().getMessage();
    }
}
