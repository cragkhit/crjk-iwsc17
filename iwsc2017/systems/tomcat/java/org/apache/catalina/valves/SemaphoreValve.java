package org.apache.catalina.valves;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
public class SemaphoreValve extends ValveBase {
    public SemaphoreValve() {
        super ( true );
    }
    protected Semaphore semaphore = null;
    protected int concurrency = 10;
    public int getConcurrency() {
        return concurrency;
    }
    public void setConcurrency ( int concurrency ) {
        this.concurrency = concurrency;
    }
    protected boolean fairness = false;
    public boolean getFairness() {
        return fairness;
    }
    public void setFairness ( boolean fairness ) {
        this.fairness = fairness;
    }
    protected boolean block = true;
    public boolean getBlock() {
        return block;
    }
    public void setBlock ( boolean block ) {
        this.block = block;
    }
    protected boolean interruptible = false;
    public boolean getInterruptible() {
        return interruptible;
    }
    public void setInterruptible ( boolean interruptible ) {
        this.interruptible = interruptible;
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        semaphore = new Semaphore ( concurrency, fairness );
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
        semaphore = null;
    }
    @Override
    public void invoke ( Request request, Response response )
    throws IOException, ServletException {
        if ( controlConcurrency ( request, response ) ) {
            boolean shouldRelease = true;
            try {
                if ( block ) {
                    if ( interruptible ) {
                        try {
                            semaphore.acquire();
                        } catch ( InterruptedException e ) {
                            shouldRelease = false;
                            permitDenied ( request, response );
                            return;
                        }
                    } else {
                        semaphore.acquireUninterruptibly();
                    }
                } else {
                    if ( !semaphore.tryAcquire() ) {
                        shouldRelease = false;
                        permitDenied ( request, response );
                        return;
                    }
                }
                getNext().invoke ( request, response );
            } finally {
                if ( shouldRelease ) {
                    semaphore.release();
                }
            }
        } else {
            getNext().invoke ( request, response );
        }
    }
    public boolean controlConcurrency ( Request request, Response response ) {
        return true;
    }
    public void permitDenied ( Request request, Response response )
    throws IOException, ServletException {
    }
}
