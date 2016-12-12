package org.apache.catalina.tribes.util;
import org.apache.juli.logging.LogFactory;
import java.util.Random;
import java.security.SecureRandom;
import org.apache.juli.logging.Log;
public class UUIDGenerator {
    private static final Log log;
    protected static final StringManager sm;
    public static final int UUID_LENGTH = 16;
    public static final int UUID_VERSION = 4;
    public static final int BYTES_PER_INT = 4;
    public static final int BITS_PER_BYTE = 8;
    protected static final SecureRandom secrand;
    protected static final Random rand;
    public static byte[] randomUUID ( final boolean secure ) {
        final byte[] result = new byte[16];
        return randomUUID ( secure, result, 0 );
    }
    public static byte[] randomUUID ( final boolean secure, final byte[] into, final int offset ) {
        if ( offset + 16 > into.length ) {
            throw new ArrayIndexOutOfBoundsException ( UUIDGenerator.sm.getString ( "uuidGenerator.unable.fit", Integer.toString ( 16 ), Integer.toString ( into.length ), Integer.toString ( offset + 16 ) ) );
        }
        final Random r = ( secure && UUIDGenerator.secrand != null ) ? UUIDGenerator.secrand : UUIDGenerator.rand;
        nextBytes ( into, offset, 16, r );
        final int n = 6 + offset;
        into[n] &= 0xF;
        final int n2 = 6 + offset;
        into[n2] |= 0x40;
        final int n3 = 8 + offset;
        into[n3] &= 0x3F;
        final int n4 = 8 + offset;
        into[n4] |= ( byte ) 128;
        return into;
    }
    public static void nextBytes ( final byte[] into, final int offset, final int length, final Random r ) {
        int numGot = 0;
        int rnd = 0;
        Block_2:
        while ( true ) {
            for ( int i = 0; i < 4; ++i ) {
                if ( numGot == length ) {
                    break Block_2;
                }
                rnd = ( ( i == 0 ) ? r.nextInt() : ( rnd >> 8 ) );
                into[offset + numGot] = ( byte ) rnd;
                ++numGot;
            }
        }
    }
    static {
        log = LogFactory.getLog ( UUIDGenerator.class );
        sm = StringManager.getManager ( "org.apache.catalina.tribes.util" );
        rand = new Random();
        final long start = System.currentTimeMillis();
        ( secrand = new SecureRandom() ).nextInt();
        final long time = System.currentTimeMillis() - start;
        if ( time > 100L ) {
            UUIDGenerator.log.info ( UUIDGenerator.sm.getString ( "uuidGenerator.createRandom", UUIDGenerator.secrand.getAlgorithm(), time ) );
        }
    }
}
