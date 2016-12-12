package org.apache.coyote.http2;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
abstract class AbstractStream {
    private static final Log log;
    private static final StringManager sm;
    private final Integer identifier;
    private volatile AbstractStream parentStream;
    private final Set<AbstractStream> childStreams;
    private long windowSize;
    final Integer getIdentifier() {
        return this.identifier;
    }
    AbstractStream ( final Integer identifier ) {
        this.parentStream = null;
        this.childStreams = new HashSet<AbstractStream>();
        this.windowSize = 65535L;
        this.identifier = identifier;
    }
    final void detachFromParent() {
        if ( this.parentStream != null ) {
            this.parentStream.getChildStreams().remove ( this );
            this.parentStream = null;
        }
    }
    final void addChild ( final AbstractStream child ) {
        child.setParentStream ( this );
        this.childStreams.add ( child );
    }
    final boolean isDescendant ( final AbstractStream stream ) {
        if ( this.childStreams.contains ( stream ) ) {
            return true;
        }
        for ( final AbstractStream child : this.childStreams ) {
            if ( child.isDescendant ( stream ) ) {
                return true;
            }
        }
        return false;
    }
    final AbstractStream getParentStream() {
        return this.parentStream;
    }
    final void setParentStream ( final AbstractStream parentStream ) {
        this.parentStream = parentStream;
    }
    final Set<AbstractStream> getChildStreams() {
        return this.childStreams;
    }
    final synchronized void setWindowSize ( final long windowSize ) {
        this.windowSize = windowSize;
    }
    final synchronized long getWindowSize() {
        return this.windowSize;
    }
    synchronized void incrementWindowSize ( final int increment ) throws Http2Exception {
        this.windowSize += increment;
        if ( AbstractStream.log.isDebugEnabled() ) {
            AbstractStream.log.debug ( AbstractStream.sm.getString ( "abstractStream.windowSizeInc", this.getConnectionId(), this.getIdentifier(), Integer.toString ( increment ), Long.toString ( this.windowSize ) ) );
        }
        if ( this.windowSize <= 2147483647L ) {
            return;
        }
        final String msg = AbstractStream.sm.getString ( "abstractStream.windowSizeTooBig", this.getConnectionId(), this.identifier, Integer.toString ( increment ), Long.toString ( this.windowSize ) );
        if ( this.identifier == 0 ) {
            throw new ConnectionException ( msg, Http2Error.FLOW_CONTROL_ERROR );
        }
        throw new StreamException ( msg, Http2Error.FLOW_CONTROL_ERROR, this.identifier );
    }
    final synchronized void decrementWindowSize ( final int decrement ) {
        this.windowSize -= decrement;
        if ( AbstractStream.log.isDebugEnabled() ) {
            AbstractStream.log.debug ( AbstractStream.sm.getString ( "abstractStream.windowSizeDec", this.getConnectionId(), this.getIdentifier(), Integer.toString ( decrement ), Long.toString ( this.windowSize ) ) );
        }
    }
    abstract String getConnectionId();
    abstract int getWeight();
    static {
        log = LogFactory.getLog ( AbstractStream.class );
        sm = StringManager.getManager ( AbstractStream.class );
    }
}
