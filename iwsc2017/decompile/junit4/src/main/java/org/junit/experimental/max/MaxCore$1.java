package org.junit.experimental.max;
import org.junit.runners.model.InitializationError;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runners.Suite;
import org.junit.runner.Runner;
import java.util.List;
import org.junit.runner.Request;
class MaxCore$1 extends Request {
    final   List val$runners;
    public Runner getRunner() {
        try {
            return new Suite ( ( Class ) null, this.val$runners ) {};
        } catch ( InitializationError e ) {
            return new ErrorReportingRunner ( null, e );
        }
    }
}
