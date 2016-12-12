package org.junit.internal.runners.model;
import java.util.List;
import org.junit.runners.model.MultipleFailureException;
@Deprecated
public class MultipleFailureException extends org.junit.runners.model.MultipleFailureException {
    private static final long serialVersionUID = 1L;
    public MultipleFailureException ( final List<Throwable> errors ) {
        super ( errors );
    }
}
