package org.apache.juli;
import java.util.logging.Logger;
protected static class RootLogger extends Logger {
    public RootLogger() {
        super ( "", null );
    }
}
