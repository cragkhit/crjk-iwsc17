package org.apache.catalina.tribes.io;
import org.apache.juli.logging.LogFactory;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
public class XByteBuffer {
    private static final Log log;
    protected static final StringManager sm;
    private static final byte[] START_DATA;
    private static final byte[] END_DATA;
    protected byte[] buf;
    protected int bufSize;
    protected boolean discard;
    private static final AtomicInteger invokecount;
    public XByteBuffer ( final int size, final boolean discard ) {
        this.buf = null;
        this.bufSize = 0;
        this.discard = true;
        this.buf = new byte[size];
        this.discard = discard;
    }
    public XByteBuffer ( final byte[] data, final boolean discard ) {
        this ( data, data.length + 128, discard );
    }
    public XByteBuffer ( final byte[] data, final int size, final boolean discard ) {
        this.buf = null;
        this.bufSize = 0;
        this.discard = true;
        final int length = Math.max ( data.length, size );
        System.arraycopy ( data, 0, this.buf = new byte[length], 0, data.length );
        this.bufSize = data.length;
        this.discard = discard;
    }
    public int getLength() {
        return this.bufSize;
    }
    public void setLength ( final int size ) {
        if ( size > this.buf.length ) {
            throw new ArrayIndexOutOfBoundsException ( XByteBuffer.sm.getString ( "xByteBuffer.size.larger.buffer" ) );
        }
        this.bufSize = size;
    }
    public void trim ( final int length ) {
        if ( this.bufSize - length < 0 ) {
            throw new ArrayIndexOutOfBoundsException ( XByteBuffer.sm.getString ( "xByteBuffer.unableTrim", Integer.toString ( this.bufSize ), Integer.toString ( length ) ) );
        }
        this.bufSize -= length;
    }
    public void reset() {
        this.bufSize = 0;
    }
    public byte[] getBytesDirect() {
        return this.buf;
    }
    public byte[] getBytes() {
        final byte[] b = new byte[this.bufSize];
        System.arraycopy ( this.buf, 0, b, 0, this.bufSize );
        return b;
    }
    public void clear() {
        this.bufSize = 0;
    }
    public boolean append ( final ByteBuffer b, final int len ) {
        final int newcount = this.bufSize + len;
        if ( newcount > this.buf.length ) {
            this.expand ( newcount );
        }
        b.get ( this.buf, this.bufSize, len );
        this.bufSize = newcount;
        if ( this.discard && this.bufSize > XByteBuffer.START_DATA.length && firstIndexOf ( this.buf, 0, XByteBuffer.START_DATA ) == -1 ) {
            this.bufSize = 0;
            XByteBuffer.log.error ( XByteBuffer.sm.getString ( "xByteBuffer.discarded.invalidHeader" ) );
            return false;
        }
        return true;
    }
    public boolean append ( final byte i ) {
        final int newcount = this.bufSize + 1;
        if ( newcount > this.buf.length ) {
            this.expand ( newcount );
        }
        this.buf[this.bufSize] = i;
        this.bufSize = newcount;
        return true;
    }
    public boolean append ( final boolean i ) {
        final int newcount = this.bufSize + 1;
        if ( newcount > this.buf.length ) {
            this.expand ( newcount );
        }
        toBytes ( i, this.buf, this.bufSize );
        this.bufSize = newcount;
        return true;
    }
    public boolean append ( final long i ) {
        final int newcount = this.bufSize + 8;
        if ( newcount > this.buf.length ) {
            this.expand ( newcount );
        }
        toBytes ( i, this.buf, this.bufSize );
        this.bufSize = newcount;
        return true;
    }
    public boolean append ( final int i ) {
        final int newcount = this.bufSize + 4;
        if ( newcount > this.buf.length ) {
            this.expand ( newcount );
        }
        toBytes ( i, this.buf, this.bufSize );
        this.bufSize = newcount;
        return true;
    }
    public boolean append ( final byte[] b, final int off, final int len ) {
        if ( off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0 ) {
            throw new IndexOutOfBoundsException();
        }
        if ( len == 0 ) {
            return false;
        }
        final int newcount = this.bufSize + len;
        if ( newcount > this.buf.length ) {
            this.expand ( newcount );
        }
        System.arraycopy ( b, off, this.buf, this.bufSize, len );
        this.bufSize = newcount;
        if ( this.discard && this.bufSize > XByteBuffer.START_DATA.length && firstIndexOf ( this.buf, 0, XByteBuffer.START_DATA ) == -1 ) {
            this.bufSize = 0;
            XByteBuffer.log.error ( XByteBuffer.sm.getString ( "xByteBuffer.discarded.invalidHeader" ) );
            return false;
        }
        return true;
    }
    public void expand ( final int newcount ) {
        final byte[] newbuf = new byte[Math.max ( this.buf.length << 1, newcount )];
        System.arraycopy ( this.buf, 0, newbuf, 0, this.bufSize );
        this.buf = newbuf;
    }
    public int getCapacity() {
        return this.buf.length;
    }
    public int countPackages() {
        return this.countPackages ( false );
    }
    public int countPackages ( final boolean first ) {
        int cnt = 0;
        int pos = XByteBuffer.START_DATA.length;
        int start = 0;
        while ( start < this.bufSize ) {
            final int index = firstIndexOf ( this.buf, start, XByteBuffer.START_DATA );
            if ( index != start ) {
                break;
            }
            if ( this.bufSize - start < 14 ) {
                break;
            }
            final int size = toInt ( this.buf, pos );
            pos = start + XByteBuffer.START_DATA.length + 4 + size;
            if ( pos + XByteBuffer.END_DATA.length > this.bufSize ) {
                break;
            }
            final int newpos = firstIndexOf ( this.buf, pos, XByteBuffer.END_DATA );
            if ( newpos != pos ) {
                break;
            }
            ++cnt;
            start = pos + XByteBuffer.END_DATA.length;
            pos = start + XByteBuffer.START_DATA.length;
            if ( first ) {
                break;
            }
        }
        return cnt;
    }
    public boolean doesPackageExist() {
        return this.countPackages ( true ) > 0;
    }
    public XByteBuffer extractDataPackage ( final boolean clearFromBuffer ) {
        final int psize = this.countPackages ( true );
        if ( psize == 0 ) {
            throw new IllegalStateException ( XByteBuffer.sm.getString ( "xByteBuffer.no.package" ) );
        }
        final int size = toInt ( this.buf, XByteBuffer.START_DATA.length );
        final XByteBuffer xbuf = BufferPool.getBufferPool().getBuffer ( size, false );
        xbuf.setLength ( size );
        System.arraycopy ( this.buf, XByteBuffer.START_DATA.length + 4, xbuf.getBytesDirect(), 0, size );
        if ( clearFromBuffer ) {
            final int totalsize = XByteBuffer.START_DATA.length + 4 + size + XByteBuffer.END_DATA.length;
            this.bufSize -= totalsize;
            System.arraycopy ( this.buf, totalsize, this.buf, 0, this.bufSize );
        }
        return xbuf;
    }
    public ChannelData extractPackage ( final boolean clearFromBuffer ) {
        final XByteBuffer xbuf = this.extractDataPackage ( clearFromBuffer );
        final ChannelData cdata = ChannelData.getDataFromPackage ( xbuf );
        return cdata;
    }
    public static byte[] createDataPackage ( final ChannelData cdata ) {
        final int dlength = cdata.getDataPackageLength();
        final int length = getDataPackageLength ( dlength );
        final byte[] data = new byte[length];
        int offset = 0;
        System.arraycopy ( XByteBuffer.START_DATA, 0, data, offset, XByteBuffer.START_DATA.length );
        offset += XByteBuffer.START_DATA.length;
        toBytes ( dlength, data, XByteBuffer.START_DATA.length );
        offset += 4;
        cdata.getDataPackage ( data, offset );
        offset += dlength;
        System.arraycopy ( XByteBuffer.END_DATA, 0, data, offset, XByteBuffer.END_DATA.length );
        offset += XByteBuffer.END_DATA.length;
        return data;
    }
    public static byte[] createDataPackage ( final byte[] data, final int doff, final int dlength, final byte[] buffer, final int bufoff ) {
        if ( buffer.length - bufoff > getDataPackageLength ( dlength ) ) {
            throw new ArrayIndexOutOfBoundsException ( XByteBuffer.sm.getString ( "xByteBuffer.unableCreate" ) );
        }
        System.arraycopy ( XByteBuffer.START_DATA, 0, buffer, bufoff, XByteBuffer.START_DATA.length );
        toBytes ( data.length, buffer, bufoff + XByteBuffer.START_DATA.length );
        System.arraycopy ( data, doff, buffer, bufoff + XByteBuffer.START_DATA.length + 4, dlength );
        System.arraycopy ( XByteBuffer.END_DATA, 0, buffer, bufoff + XByteBuffer.START_DATA.length + 4 + data.length, XByteBuffer.END_DATA.length );
        return buffer;
    }
    public static int getDataPackageLength ( final int datalength ) {
        final int length = XByteBuffer.START_DATA.length + 4 + datalength + XByteBuffer.END_DATA.length;
        return length;
    }
    public static byte[] createDataPackage ( final byte[] data ) {
        final int length = getDataPackageLength ( data.length );
        final byte[] result = new byte[length];
        return createDataPackage ( data, 0, data.length, result, 0 );
    }
    public static int toInt ( final byte[] b, final int off ) {
        return ( b[off + 3] & 0xFF ) + ( ( b[off + 2] & 0xFF ) << 8 ) + ( ( b[off + 1] & 0xFF ) << 16 ) + ( ( b[off + 0] & 0xFF ) << 24 );
    }
    public static long toLong ( final byte[] b, final int off ) {
        return ( b[off + 7] & 0xFFL ) + ( ( b[off + 6] & 0xFFL ) << 8 ) + ( ( b[off + 5] & 0xFFL ) << 16 ) + ( ( b[off + 4] & 0xFFL ) << 24 ) + ( ( b[off + 3] & 0xFFL ) << 32 ) + ( ( b[off + 2] & 0xFFL ) << 40 ) + ( ( b[off + 1] & 0xFFL ) << 48 ) + ( ( b[off + 0] & 0xFFL ) << 56 );
    }
    public static byte[] toBytes ( final boolean bool, final byte[] data, final int offset ) {
        data[offset] = ( byte ) ( bool ? 1 : 0 );
        return data;
    }
    public static boolean toBoolean ( final byte[] b, final int offset ) {
        return b[offset] != 0;
    }
    public static byte[] toBytes ( int n, final byte[] b, final int offset ) {
        b[offset + 3] = ( byte ) n;
        n >>>= 8;
        b[offset + 2] = ( byte ) n;
        n >>>= 8;
        b[offset + 1] = ( byte ) n;
        n >>>= 8;
        b[offset + 0] = ( byte ) n;
        return b;
    }
    public static byte[] toBytes ( long n, final byte[] b, final int offset ) {
        b[offset + 7] = ( byte ) n;
        n >>>= 8;
        b[offset + 6] = ( byte ) n;
        n >>>= 8;
        b[offset + 5] = ( byte ) n;
        n >>>= 8;
        b[offset + 4] = ( byte ) n;
        n >>>= 8;
        b[offset + 3] = ( byte ) n;
        n >>>= 8;
        b[offset + 2] = ( byte ) n;
        n >>>= 8;
        b[offset + 1] = ( byte ) n;
        n >>>= 8;
        b[offset + 0] = ( byte ) n;
        return b;
    }
    public static int firstIndexOf ( final byte[] src, final int srcOff, final byte[] find ) {
        int result = -1;
        if ( find.length > src.length ) {
            return result;
        }
        if ( find.length == 0 || src.length == 0 ) {
            return result;
        }
        if ( srcOff >= src.length ) {
            throw new ArrayIndexOutOfBoundsException();
        }
        boolean found = false;
        final int srclen = src.length;
        final int findlen = find.length;
        final byte first = find[0];
        int pos = srcOff;
        while ( !found ) {
            while ( pos < srclen && first != src[pos] ) {
                ++pos;
            }
            if ( pos >= srclen ) {
                return -1;
            }
            if ( srclen - pos < findlen ) {
                return -1;
            }
            found = true;
            for ( int i = 1; i < findlen && found; found = ( find[i] == src[pos + i] ), ++i ) {}
            if ( found ) {
                result = pos;
            } else {
                if ( srclen - pos < findlen ) {
                    return -1;
                }
                ++pos;
            }
        }
        return result;
    }
    public static Serializable deserialize ( final byte[] data ) throws IOException, ClassNotFoundException, ClassCastException {
        return deserialize ( data, 0, data.length );
    }
    public static Serializable deserialize ( final byte[] data, final int offset, final int length ) throws IOException, ClassNotFoundException, ClassCastException {
        return deserialize ( data, offset, length, null );
    }
    public static Serializable deserialize ( final byte[] data, final int offset, final int length, ClassLoader[] cls ) throws IOException, ClassNotFoundException, ClassCastException {
        XByteBuffer.invokecount.addAndGet ( 1 );
        Object message = null;
        if ( cls == null ) {
            cls = new ClassLoader[0];
        }
        if ( data != null && length > 0 ) {
            final InputStream instream = new ByteArrayInputStream ( data, offset, length );
            ObjectInputStream stream = null;
            stream = ( ( cls.length > 0 ) ? new ReplicationStream ( instream, cls ) : new ObjectInputStream ( instream ) );
            message = stream.readObject();
            instream.close();
            stream.close();
        }
        if ( message == null ) {
            return null;
        }
        if ( message instanceof Serializable ) {
            return ( Serializable ) message;
        }
        throw new ClassCastException ( XByteBuffer.sm.getString ( "xByteBuffer.wrong.class", message.getClass().getName() ) );
    }
    public static byte[] serialize ( final Serializable msg ) throws IOException {
        final ByteArrayOutputStream outs = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream ( outs );
        out.writeObject ( msg );
        out.flush();
        final byte[] data = outs.toByteArray();
        return data;
    }
    public void setDiscard ( final boolean discard ) {
        this.discard = discard;
    }
    public boolean getDiscard() {
        return this.discard;
    }
    static {
        log = LogFactory.getLog ( XByteBuffer.class );
        sm = StringManager.getManager ( XByteBuffer.class );
        START_DATA = new byte[] { 70, 76, 84, 50, 48, 48, 50 };
        END_DATA = new byte[] { 84, 76, 70, 50, 48, 48, 51 };
        invokecount = new AtomicInteger ( 0 );
    }
}
