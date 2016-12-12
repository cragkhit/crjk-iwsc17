package org.apache.catalina.valves;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import java.util.concurrent.Semaphore;
public class SemaphoreValve extends ValveBase {
    protected Semaphore semaphore;
    protected int concurrency;
    protected boolean fairness;
    protected boolean block;
    protected boolean interruptible;
    public SemaphoreValve() {
        super ( true );
        this.semaphore = null;
        this.concurrency = 10;
        this.fairness = false;
        this.block = true;
        this.interruptible = false;
    }
    public int getConcurrency() {
        return this.concurrency;
    }
    public void setConcurrency ( final int concurrency ) {
        this.concurrency = concurrency;
    }
    public boolean getFairness() {
        return this.fairness;
    }
    public void setFairness ( final boolean fairness ) {
        this.fairness = fairness;
    }
    public boolean getBlock() {
        return this.block;
    }
    public void setBlock ( final boolean block ) {
        this.block = block;
    }
    public boolean getInterruptible() {
        return this.interruptible;
    }
    public void setInterruptible ( final boolean interruptible ) {
        this.interruptible = interruptible;
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        this.semaphore = new Semaphore ( this.concurrency, this.fairness );
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
        this.semaphore = null;
    }
    @Override
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        if ( this.controlConcurrency ( request, response ) ) {
            boolean shouldRelease = true;
            try {
                Label_0097: {
                    if ( this.block ) {
                        if ( this.interruptible ) {
                            try {
                                this.semaphore.acquire();
                                break Label_0097;
                            } catch ( InterruptedException e ) {
                                shouldRelease = false;
                                this.permitDenied ( request, response );
                                return;
                            }
                        }
                        this.semaphore.acquireUninterruptibly();
                    } else if ( !this.semaphore.tryAcquire() ) {
                        shouldRelease = false;
                        this.permitDenied ( request, response );
                        return;
                    }
                }
                this.getNext().invoke ( request, response );
            } finally {
                if ( shouldRelease ) {
                    this.semaphore.release();
                }
            }
        } else {
            this.getNext().invoke ( request, response );
        }
    }
    public boolean controlConcurrency ( final Request request, final Response response ) {
        return true;
    }
    public void permitDenied ( final Request request, final Response response ) throws IOException, ServletException {
    }
}
