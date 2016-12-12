package org.apache.tomcat.websocket.server;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import javax.websocket.DeploymentException;
import java.util.ArrayList;
import java.util.List;
import org.apache.tomcat.util.res.StringManager;
public class UriTemplate {
    private static final StringManager sm;
    private final String normalized;
    private final List<Segment> segments;
    private final boolean hasParameters;
    public UriTemplate ( final String path ) throws DeploymentException {
        this.segments = new ArrayList<Segment>();
        if ( path == null || path.length() == 0 || !path.startsWith ( "/" ) ) {
            throw new DeploymentException ( UriTemplate.sm.getString ( "uriTemplate.invalidPath", path ) );
        }
        final StringBuilder normalized = new StringBuilder ( path.length() );
        final Set<String> paramNames = new HashSet<String>();
        final String[] segments = path.split ( "/", -1 );
        int paramCount = 0;
        int segmentCount = 0;
        for ( int i = 0; i < segments.length; ++i ) {
            String segment = segments[i];
            if ( segment.length() == 0 ) {
                if ( i != 0 ) {
                    if ( i != segments.length - 1 || paramCount != 0 ) {
                        throw new IllegalArgumentException ( UriTemplate.sm.getString ( "uriTemplate.emptySegment", path ) );
                    }
                }
            } else {
                normalized.append ( '/' );
                int index = -1;
                if ( segment.startsWith ( "{" ) && segment.endsWith ( "}" ) ) {
                    index = segmentCount;
                    segment = segment.substring ( 1, segment.length() - 1 );
                    normalized.append ( '{' );
                    normalized.append ( paramCount++ );
                    normalized.append ( '}' );
                    if ( !paramNames.add ( segment ) ) {
                        throw new IllegalArgumentException ( UriTemplate.sm.getString ( "uriTemplate.duplicateParameter", segment ) );
                    }
                } else {
                    if ( segment.contains ( "{" ) || segment.contains ( "}" ) ) {
                        throw new IllegalArgumentException ( UriTemplate.sm.getString ( "uriTemplate.invalidSegment", segment, path ) );
                    }
                    normalized.append ( segment );
                }
                this.segments.add ( new Segment ( index, segment ) );
                ++segmentCount;
            }
        }
        this.normalized = normalized.toString();
        this.hasParameters = ( paramCount > 0 );
    }
    public Map<String, String> match ( final UriTemplate candidate ) {
        final Map<String, String> result = new HashMap<String, String>();
        if ( candidate.getSegmentCount() != this.getSegmentCount() ) {
            return null;
        }
        final Iterator<Segment> candidateSegments = candidate.getSegments().iterator();
        final Iterator<Segment> targetSegments = this.segments.iterator();
        while ( candidateSegments.hasNext() ) {
            final Segment candidateSegment = candidateSegments.next();
            final Segment targetSegment = targetSegments.next();
            if ( targetSegment.getParameterIndex() == -1 ) {
                if ( !targetSegment.getValue().equals ( candidateSegment.getValue() ) ) {
                    return null;
                }
                continue;
            } else {
                result.put ( targetSegment.getValue(), candidateSegment.getValue() );
            }
        }
        return result;
    }
    public boolean hasParameters() {
        return this.hasParameters;
    }
    public int getSegmentCount() {
        return this.segments.size();
    }
    public String getNormalizedPath() {
        return this.normalized;
    }
    private List<Segment> getSegments() {
        return this.segments;
    }
    static {
        sm = StringManager.getManager ( UriTemplate.class );
    }
    private static class Segment {
        private final int parameterIndex;
        private final String value;
        public Segment ( final int parameterIndex, final String value ) {
            this.parameterIndex = parameterIndex;
            this.value = value;
        }
        public int getParameterIndex() {
            return this.parameterIndex;
        }
        public String getValue() {
            return this.value;
        }
    }
}
