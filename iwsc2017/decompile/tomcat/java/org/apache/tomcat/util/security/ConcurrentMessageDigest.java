package org.apache.tomcat.util.security;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.Queue;
import java.util.Map;
public class ConcurrentMessageDigest {
    private static final String MD5 = "MD5";
    private static final String SHA1 = "SHA-1";
    private static final Map<String, Queue<MessageDigest>> queues;
    public static byte[] digestMD5 ( final byte[]... input ) {
        return digest ( "MD5", input );
    }
    public static byte[] digestSHA1 ( final byte[]... input ) {
        return digest ( "SHA-1", input );
    }
    public static byte[] digest ( final String algorithm, final byte[]... input ) {
        return digest ( algorithm, 1, input );
    }
    public static byte[] digest ( final String algorithm, final int rounds, final byte[]... input ) {
        final Queue<MessageDigest> queue = ConcurrentMessageDigest.queues.get ( algorithm );
        if ( queue == null ) {
            throw new IllegalStateException ( "Must call init() first" );
        }
        MessageDigest md = queue.poll();
        if ( md == null ) {
            try {
                md = MessageDigest.getInstance ( algorithm );
            } catch ( NoSuchAlgorithmException e ) {
                throw new IllegalStateException ( "Must call init() first" );
            }
        }
        for ( final byte[] bytes : input ) {
            md.update ( bytes );
        }
        byte[] result = md.digest();
        if ( rounds > 1 ) {
            for ( int i = 1; i < rounds; ++i ) {
                md.update ( result );
                result = md.digest();
            }
        }
        queue.add ( md );
        return result;
    }
    public static void init ( final String algorithm ) throws NoSuchAlgorithmException {
        synchronized ( ConcurrentMessageDigest.queues ) {
            if ( !ConcurrentMessageDigest.queues.containsKey ( algorithm ) ) {
                final MessageDigest md = MessageDigest.getInstance ( algorithm );
                final Queue<MessageDigest> queue = new ConcurrentLinkedQueue<MessageDigest>();
                queue.add ( md );
                ConcurrentMessageDigest.queues.put ( algorithm, queue );
            }
        }
    }
    static {
        queues = new HashMap<String, Queue<MessageDigest>>();
        try {
            init ( "MD5" );
            init ( "SHA-1" );
        } catch ( NoSuchAlgorithmException e ) {
            throw new IllegalArgumentException ( e );
        }
    }
}
