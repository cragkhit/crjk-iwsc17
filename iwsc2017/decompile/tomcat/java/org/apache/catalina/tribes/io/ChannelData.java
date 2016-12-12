package org.apache.catalina.tribes.io;
import java.sql.Timestamp;
import java.util.Arrays;
import org.apache.catalina.tribes.membership.MemberImpl;
import org.apache.catalina.tribes.util.UUIDGenerator;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.ChannelMessage;
public class ChannelData implements ChannelMessage {
    private static final long serialVersionUID = 1L;
    public static final ChannelData[] EMPTY_DATA_ARRAY;
    public static volatile boolean USE_SECURE_RANDOM_FOR_UUID;
    private int options;
    private XByteBuffer message;
    private long timestamp;
    private byte[] uniqueId;
    private Member address;
    public ChannelData() {
        this ( true );
    }
    public ChannelData ( final boolean generateUUID ) {
        this.options = 0;
        if ( generateUUID ) {
            this.generateUUID();
        }
    }
    public ChannelData ( final byte[] uniqueId, final XByteBuffer message, final long timestamp ) {
        this.options = 0;
        this.uniqueId = uniqueId;
        this.message = message;
        this.timestamp = timestamp;
    }
    @Override
    public XByteBuffer getMessage() {
        return this.message;
    }
    @Override
    public void setMessage ( final XByteBuffer message ) {
        this.message = message;
    }
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }
    @Override
    public void setTimestamp ( final long timestamp ) {
        this.timestamp = timestamp;
    }
    @Override
    public byte[] getUniqueId() {
        return this.uniqueId;
    }
    public void setUniqueId ( final byte[] uniqueId ) {
        this.uniqueId = uniqueId;
    }
    @Override
    public int getOptions() {
        return this.options;
    }
    @Override
    public void setOptions ( final int options ) {
        this.options = options;
    }
    @Override
    public Member getAddress() {
        return this.address;
    }
    @Override
    public void setAddress ( final Member address ) {
        this.address = address;
    }
    public void generateUUID() {
        final byte[] data = new byte[16];
        UUIDGenerator.randomUUID ( ChannelData.USE_SECURE_RANDOM_FOR_UUID, data, 0 );
        this.setUniqueId ( data );
    }
    public int getDataPackageLength() {
        final int length = 16 + this.uniqueId.length + 4 + this.address.getDataLength() + 4 + this.message.getLength();
        return length;
    }
    public byte[] getDataPackage() {
        final int length = this.getDataPackageLength();
        final byte[] data = new byte[length];
        final int offset = 0;
        return this.getDataPackage ( data, offset );
    }
    public byte[] getDataPackage ( final byte[] data, int offset ) {
        final byte[] addr = this.address.getData ( false );
        XByteBuffer.toBytes ( this.options, data, offset );
        offset += 4;
        XByteBuffer.toBytes ( this.timestamp, data, offset );
        offset += 8;
        XByteBuffer.toBytes ( this.uniqueId.length, data, offset );
        offset += 4;
        System.arraycopy ( this.uniqueId, 0, data, offset, this.uniqueId.length );
        offset += this.uniqueId.length;
        XByteBuffer.toBytes ( addr.length, data, offset );
        offset += 4;
        System.arraycopy ( addr, 0, data, offset, addr.length );
        offset += addr.length;
        XByteBuffer.toBytes ( this.message.getLength(), data, offset );
        offset += 4;
        System.arraycopy ( this.message.getBytesDirect(), 0, data, offset, this.message.getLength() );
        offset += this.message.getLength();
        return data;
    }
    public static ChannelData getDataFromPackage ( final XByteBuffer xbuf ) {
        final ChannelData data = new ChannelData ( false );
        int offset = 0;
        data.setOptions ( XByteBuffer.toInt ( xbuf.getBytesDirect(), offset ) );
        offset += 4;
        data.setTimestamp ( XByteBuffer.toLong ( xbuf.getBytesDirect(), offset ) );
        offset += 8;
        data.uniqueId = new byte[XByteBuffer.toInt ( xbuf.getBytesDirect(), offset )];
        offset += 4;
        System.arraycopy ( xbuf.getBytesDirect(), offset, data.uniqueId, 0, data.uniqueId.length );
        offset += data.uniqueId.length;
        final int addrlen = XByteBuffer.toInt ( xbuf.getBytesDirect(), offset );
        offset += 4;
        data.setAddress ( MemberImpl.getMember ( xbuf.getBytesDirect(), offset, addrlen ) );
        offset += addrlen;
        final int xsize = XByteBuffer.toInt ( xbuf.getBytesDirect(), offset );
        offset += 4;
        System.arraycopy ( xbuf.getBytesDirect(), offset, xbuf.getBytesDirect(), 0, xsize );
        xbuf.setLength ( xsize );
        data.message = xbuf;
        return data;
    }
    public static ChannelData getDataFromPackage ( final byte[] b ) {
        final ChannelData data = new ChannelData ( false );
        int offset = 0;
        data.setOptions ( XByteBuffer.toInt ( b, offset ) );
        offset += 4;
        data.setTimestamp ( XByteBuffer.toLong ( b, offset ) );
        offset += 8;
        data.uniqueId = new byte[XByteBuffer.toInt ( b, offset )];
        offset += 4;
        System.arraycopy ( b, offset, data.uniqueId, 0, data.uniqueId.length );
        offset += data.uniqueId.length;
        final byte[] addr = new byte[XByteBuffer.toInt ( b, offset )];
        offset += 4;
        System.arraycopy ( b, offset, addr, 0, addr.length );
        data.setAddress ( MemberImpl.getMember ( addr ) );
        offset += addr.length;
        final int xsize = XByteBuffer.toInt ( b, offset );
        data.message = BufferPool.getBufferPool().getBuffer ( xsize, false );
        offset += 4;
        System.arraycopy ( b, offset, data.message.getBytesDirect(), 0, xsize );
        data.message.append ( b, offset, xsize );
        offset += xsize;
        return data;
    }
    @Override
    public int hashCode() {
        return XByteBuffer.toInt ( this.getUniqueId(), 0 );
    }
    @Override
    public boolean equals ( final Object o ) {
        return o instanceof ChannelData && Arrays.equals ( this.getUniqueId(), ( ( ChannelData ) o ).getUniqueId() );
    }
    @Override
    public Object clone() {
        final ChannelData clone = new ChannelData ( false );
        clone.options = this.options;
        clone.message = new XByteBuffer ( this.message.getBytesDirect(), false );
        clone.timestamp = this.timestamp;
        clone.uniqueId = this.uniqueId;
        clone.address = this.address;
        return clone;
    }
    @Override
    public Object deepclone() {
        final byte[] d = this.getDataPackage();
        return getDataFromPackage ( d );
    }
    public static boolean sendAckSync ( final int options ) {
        return ( 0x2 & options ) == 0x2 && ( 0x4 & options ) == 0x4;
    }
    public static boolean sendAckAsync ( final int options ) {
        return ( 0x2 & options ) == 0x2 && ( 0x4 & options ) != 0x4;
    }
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append ( "ClusterData[src=" );
        buf.append ( this.getAddress() ).append ( "; id=" );
        buf.append ( bToS ( this.getUniqueId() ) ).append ( "; sent=" );
        buf.append ( new Timestamp ( this.getTimestamp() ).toString() ).append ( "]" );
        return buf.toString();
    }
    public static String bToS ( final byte[] data ) {
        final StringBuilder buf = new StringBuilder ( 64 );
        buf.append ( "{" );
        for ( int i = 0; data != null && i < data.length; ++i ) {
            buf.append ( String.valueOf ( data[i] ) ).append ( " " );
        }
        buf.append ( "}" );
        return buf.toString();
    }
    static {
        EMPTY_DATA_ARRAY = new ChannelData[0];
        ChannelData.USE_SECURE_RANDOM_FOR_UUID = false;
    }
}
