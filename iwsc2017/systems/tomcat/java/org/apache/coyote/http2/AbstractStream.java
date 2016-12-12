package org.apache.coyote.http2;
import java.util.HashSet;
import java.util.Set;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
abstract class AbstractStream {
    private static final Log log = LogFactory.getLog ( AbstractStream.class );
    private static final StringManager sm = StringManager.getManager ( AbstractStream.class );
    private final Integer identifier;
    private volatile AbstractStream parentStream = null;
    private final Set<AbstractStream> childStreams = new HashSet<>();
    private long windowSize = ConnectionSettingsBase.DEFAULT_INITIAL_WINDOW_SIZE;
    final Integer getIdentifier() {
        return identifier;
    }
    AbstractStream ( Integer identifier ) {
        this.identifier = identifier;
    }
    final void detachFromParent() {
        if ( parentStream != null ) {
            parentStream.getChildStreams().remove ( this );
            parentStream = null;
        }
    }
    final void addChild ( AbstractStream child ) {
        child.setParentStream ( this );
        childStreams.add ( child );
    }
    final boolean isDescendant ( AbstractStream stream ) {
        if ( childStreams.contains ( stream ) ) {
            return true;
        }
        for ( AbstractStream child : childStreams ) {
            if ( child.isDescendant ( stream ) ) {
                return true;
            }
        }
        return false;
    }
    final AbstractStream getParentStream() {
        return parentStream;
    }
    final void setParentStream ( AbstractStream parentStream ) {
        this.parentStream = parentStream;
    }
    final Set<AbstractStream> getChildStreams() {
        return childStreams;
    }
    final synchronized void setWindowSize ( long windowSize ) {
        this.windowSize = windowSize;
    }
    final synchronized long getWindowSize() {
        return windowSize;
    }
    synchronized void incrementWindowSize ( int increment ) throws Http2Exception {
        windowSize += increment;
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "abstractStream.windowSizeInc", getConnectionId(),
                                       getIdentifier(), Integer.toString ( increment ), Long.toString ( windowSize ) ) );
        }
        if ( windowSize > ConnectionSettingsBase.MAX_WINDOW_SIZE ) {
            String msg = sm.getString ( "abstractStream.windowSizeTooBig", getConnectionId(), identifier,
                                        Integer.toString ( increment ), Long.toString ( windowSize ) );
            if ( identifier.intValue() == 0 ) {
                throw new ConnectionException ( msg, Http2Error.FLOW_CONTROL_ERROR );
            } else {
                throw new StreamException (
                    msg, Http2Error.FLOW_CONTROL_ERROR, identifier.intValue() );
            }
        }
    }
    final synchronized void decrementWindowSize ( int decrement ) {
        windowSize -= decrement;
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "abstractStream.windowSizeDec", getConnectionId(),
                                       getIdentifier(), Integer.toString ( decrement ), Long.toString ( windowSize ) ) );
        }
    }
    abstract String getConnectionId();
    abstract int getWeight();
}
