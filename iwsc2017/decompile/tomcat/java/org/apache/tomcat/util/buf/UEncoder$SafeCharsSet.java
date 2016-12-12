package org.apache.tomcat.util.buf;
import java.util.BitSet;
public enum SafeCharsSet {
    WITH_SLASH ( "/" ),
    DEFAULT ( "" );
    private final BitSet safeChars;
    private BitSet getSafeChars() {
        return this.safeChars;
    }
    private SafeCharsSet ( final String additionalSafeChars ) {
        this.safeChars = UEncoder.access$000();
        for ( final char c : additionalSafeChars.toCharArray() ) {
            this.safeChars.set ( c );
        }
    }
}
