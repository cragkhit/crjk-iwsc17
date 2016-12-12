package org.apache.coyote;
import java.util.concurrent.atomic.AtomicBoolean;
public class PushToken {
    private final AtomicBoolean result;
    private final Request pushTarget;
    public PushToken ( final Request pushTarget ) {
        this.result = new AtomicBoolean ( false );
        this.pushTarget = pushTarget;
    }
    public Request getPushTarget() {
        return this.pushTarget;
    }
    public void setResult ( final boolean result ) {
        this.result.set ( result );
    }
    public boolean getResult() {
        return this.result.get();
    }
}
