package org.apache.catalina.authenticator;
import java.util.Iterator;
import java.util.LinkedHashMap;
public static class SpnegoTokenFixer {
    private final byte[] token;
    private int pos;
    public static void fix ( final byte[] token ) {
        final SpnegoTokenFixer fixer = new SpnegoTokenFixer ( token );
        fixer.fix();
    }
    private SpnegoTokenFixer ( final byte[] token ) {
        this.pos = 0;
        this.token = token;
    }
    private void fix() {
        if ( !this.tag ( 96 ) ) {
            return;
        }
        if ( !this.length() ) {
            return;
        }
        if ( !this.oid ( "1.3.6.1.5.5.2" ) ) {
            return;
        }
        if ( !this.tag ( 160 ) ) {
            return;
        }
        if ( !this.length() ) {
            return;
        }
        if ( !this.tag ( 48 ) ) {
            return;
        }
        if ( !this.length() ) {
            return;
        }
        if ( !this.tag ( 160 ) ) {
            return;
        }
        this.lengthAsInt();
        if ( !this.tag ( 48 ) ) {
            return;
        }
        final int mechTypesLen = this.lengthAsInt();
        final int mechTypesStart = this.pos;
        final LinkedHashMap<String, int[]> mechTypeEntries = new LinkedHashMap<String, int[]>();
        while ( this.pos < mechTypesStart + mechTypesLen ) {
            final int[] value = { this.pos, 0 };
            final String key = this.oidAsString();
            value[1] = this.pos - value[0];
            mechTypeEntries.put ( key, value );
        }
        final byte[] replacement = new byte[mechTypesLen];
        int replacementPos = 0;
        final int[] first = mechTypeEntries.remove ( "1.2.840.113554.1.2.2" );
        if ( first != null ) {
            System.arraycopy ( this.token, first[0], replacement, replacementPos, first[1] );
            replacementPos += first[1];
        }
        for ( final int[] markers : mechTypeEntries.values() ) {
            System.arraycopy ( this.token, markers[0], replacement, replacementPos, markers[1] );
            replacementPos += markers[1];
        }
        System.arraycopy ( replacement, 0, this.token, mechTypesStart, mechTypesLen );
    }
    private boolean tag ( final int expected ) {
        return ( this.token[this.pos++] & 0xFF ) == expected;
    }
    private boolean length() {
        final int len = this.lengthAsInt();
        return this.pos + len == this.token.length;
    }
    private int lengthAsInt() {
        int len = this.token[this.pos++] & 0xFF;
        if ( len > 127 ) {
            final int bytes = len - 128;
            len = 0;
            for ( int i = 0; i < bytes; ++i ) {
                len <<= 8;
                len += ( this.token[this.pos++] & 0xFF );
            }
        }
        return len;
    }
    private boolean oid ( final String expected ) {
        return expected.equals ( this.oidAsString() );
    }
    private String oidAsString() {
        if ( !this.tag ( 6 ) ) {
            return null;
        }
        final StringBuilder result = new StringBuilder();
        final int len = this.lengthAsInt();
        final int v = this.token[this.pos++] & 0xFF;
        int c2 = v % 40;
        c2 = ( v - c2 ) / 40;
        result.append ( c2 );
        result.append ( '.' );
        result.append ( c2 );
        int c3 = 0;
        boolean write = false;
        for ( int i = 1; i < len; ++i ) {
            int b = this.token[this.pos++] & 0xFF;
            if ( b > 127 ) {
                b -= 128;
            } else {
                write = true;
            }
            c3 <<= 7;
            c3 += b;
            if ( write ) {
                result.append ( '.' );
                result.append ( c3 );
                c3 = 0;
                write = false;
            }
        }
        return result.toString();
    }
}
