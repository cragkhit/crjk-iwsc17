package org.apache.catalina.util;
public class StandardSessionIdGenerator extends SessionIdGeneratorBase {
    @Override
    public String generateSessionId ( final String route ) {
        final byte[] random = new byte[16];
        final int sessionIdLength = this.getSessionIdLength();
        final StringBuilder buffer = new StringBuilder ( 2 * sessionIdLength + 20 );
        int resultLenBytes = 0;
        while ( resultLenBytes < sessionIdLength ) {
            this.getRandomBytes ( random );
            for ( int j = 0; j < random.length && resultLenBytes < sessionIdLength; ++resultLenBytes, ++j ) {
                final byte b1 = ( byte ) ( ( random[j] & 0xF0 ) >> 4 );
                final byte b2 = ( byte ) ( random[j] & 0xF );
                if ( b1 < 10 ) {
                    buffer.append ( ( char ) ( 48 + b1 ) );
                } else {
                    buffer.append ( ( char ) ( 65 + ( b1 - 10 ) ) );
                }
                if ( b2 < 10 ) {
                    buffer.append ( ( char ) ( 48 + b2 ) );
                } else {
                    buffer.append ( ( char ) ( 65 + ( b2 - 10 ) ) );
                }
            }
        }
        if ( route != null && route.length() > 0 ) {
            buffer.append ( '.' ).append ( route );
        } else {
            final String jvmRoute = this.getJvmRoute();
            if ( jvmRoute != null && jvmRoute.length() > 0 ) {
                buffer.append ( '.' ).append ( jvmRoute );
            }
        }
        return buffer.toString();
    }
}
