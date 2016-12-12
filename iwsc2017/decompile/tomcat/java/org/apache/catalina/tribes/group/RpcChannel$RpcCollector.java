package org.apache.catalina.tribes.group;
import org.apache.catalina.tribes.Member;
import java.io.Serializable;
import java.util.ArrayList;
public static class RpcCollector {
    public final ArrayList<Response> responses;
    public final RpcCollectorKey key;
    public final int options;
    public int destcnt;
    public RpcCollector ( final RpcCollectorKey key, final int options, final int destcnt ) {
        this.responses = new ArrayList<Response>();
        this.key = key;
        this.options = options;
        this.destcnt = destcnt;
    }
    public void addResponse ( final Serializable message, final Member sender ) {
        final Response resp = new Response ( sender, message );
        this.responses.add ( resp );
    }
    public boolean isComplete() {
        if ( this.destcnt <= 0 ) {
            return true;
        }
        switch ( this.options ) {
        case 3: {
            return this.destcnt == this.responses.size();
        }
        case 2: {
            final float perc = this.responses.size() / this.destcnt;
            return perc >= 0.5f;
        }
        case 1: {
            return this.responses.size() > 0;
        }
        default: {
            return false;
        }
        }
    }
    @Override
    public int hashCode() {
        return this.key.hashCode();
    }
    @Override
    public boolean equals ( final Object o ) {
        if ( o instanceof RpcCollector ) {
            final RpcCollector r = ( RpcCollector ) o;
            return r.key.equals ( this.key );
        }
        return false;
    }
    public Response[] getResponses() {
        return this.responses.toArray ( new Response[this.responses.size()] );
    }
}
