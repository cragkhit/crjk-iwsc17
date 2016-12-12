package org.apache.coyote;
import java.util.concurrent.atomic.AtomicBoolean;
public class PushToken {
    private final AtomicBoolean result = new AtomicBoolean ( false );
    private final Request pushTarget;
    public PushToken ( Request pushTarget ) {
        this.pushTarget = pushTarget;
    }
    public Request getPushTarget() {
        return pushTarget;
    }
    public void setResult ( boolean result ) {
        this.result.set ( result );
    }
    public boolean getResult() {
        return result.get();
    }
}
