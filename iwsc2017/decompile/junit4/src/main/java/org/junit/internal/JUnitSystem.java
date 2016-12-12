package org.junit.internal;
import java.io.PrintStream;
public interface JUnitSystem {
    @Deprecated
    void exit ( int p0 );
    PrintStream out();
}
