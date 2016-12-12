package org.apache.catalina.tribes.io;
import java.sql.Timestamp;
import java.util.Arrays;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.membership.MemberImpl;
import org.apache.catalina.tribes.util.UUIDGenerator;
public class ChannelData implements ChannelMessage {
    private static final long serialVersionUID = 1L;
    public static final ChannelData[] EMPTY_DATA_ARRAY = new ChannelData[0];
    public static volatile boolean USE_SECURE_RANDOM_FOR_UUID = false;
    private int options = 0 ;
    private XByteBuffer message ;
    private long timestamp ;
    private byte[] uniqueId ;
    private Member address;
    public ChannelData() {
        this ( true );
    }
    public ChannelData ( boolean generateUUID ) {
        if ( generateUUID ) {
            generateUUID();
        }
    }
    public ChannelData ( byte[] uniqueId, XByteBuffer message, long timestamp ) {
        this.uniqueId = uniqueId;
        this.message = message;
        this.timestamp = timestamp;
    }
    @Override
    public XByteBuffer getMessage() {
        return message;
    }
    @Override
    public void setMessage ( XByteBuffer message ) {
        this.message = message;
    }
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    @Override
    public void setTimestamp ( long timestamp ) {
        this.timestamp = timestamp;
    }
    @Override
    public byte[] getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId ( byte[] uniqueId ) {
        this.uniqueId = uniqueId;
    }
    @Override
    public int getOptions() {
        return options;
    }
    @Override
    public void setOptions ( int options ) {
        this.options = options;
    }
    @Override
    public Member getAddress() {
        return address;
    }
    @Override
    public void setAddress ( Member address ) {
        this.address = address;
    }
    public void generateUUID() {
        byte[] data = new byte[16];
        UUIDGenerator.randomUUID ( USE_SECURE_RANDOM_FOR_UUID, data, 0 );
        setUniqueId ( data );
    }
    public int getDataPackageLength() {
        int length =
            4 +
            8 +
            4 +
            uniqueId.length +
            4 +
            address.getDataLength() +
            4 +
            message.getLength();
        return length;
    }
    public byte[] getDataPackage()  {
        int length = getDataPackageLength();
        byte[] data = new byte[length];
        int offset = 0;
        return getDataPackage ( data, offset );
    }
    public byte[] getDataPackage ( byte[] data, int offset )  {
        byte[] addr = address.getData ( false );
        XByteBuffer.toBytes ( options, data, offset );
        offset += 4;
        XByteBuffer.toBytes ( timestamp, data, offset );
        offset += 8;
        XByteBuffer.toBytes ( uniqueId.length, data, offset );
        offset += 4;
        System.arraycopy ( uniqueId, 0, data, offset, uniqueId.length );
        offset += uniqueId.length;
        XByteBuffer.toBytes ( addr.length, data, offset );
        offset += 4;
        System.arraycopy ( addr, 0, data, offset, addr.length );
        offset += addr.length;
        XByteBuffer.toBytes ( message.getLength(), data, offset );
        offset += 4;
        System.arraycopy ( message.getBytesDirect(), 0, data, offset, message.getLength() );
        offset += message.getLength();
        return data;
    }
    public static ChannelData getDataFromPackage ( XByteBuffer xbuf )  {
        ChannelData data = new ChannelData ( false );
        int offset = 0;
        data.setOptions ( XByteBuffer.toInt ( xbuf.getBytesDirect(), offset ) );
        offset += 4;
        data.setTimestamp ( XByteBuffer.toLong ( xbuf.getBytesDirect(), offset ) );
        offset += 8;
        data.uniqueId = new byte[XByteBuffer.toInt ( xbuf.getBytesDirect(), offset )];
        offset += 4;
        System.arraycopy ( xbuf.getBytesDirect(), offset, data.uniqueId, 0, data.uniqueId.length );
        offset += data.uniqueId.length;
        int addrlen = XByteBuffer.toInt ( xbuf.getBytesDirect(), offset );
        offset += 4;
        data.setAddress ( MemberImpl.getMember ( xbuf.getBytesDirect(), offset, addrlen ) );
        offset += addrlen;
        int xsize = XByteBuffer.toInt ( xbuf.getBytesDirect(), offset );
        offset += 4;
        System.arraycopy ( xbuf.getBytesDirect(), offset, xbuf.getBytesDirect(), 0, xsize );
        xbuf.setLength ( xsize );
        data.message = xbuf;
        return data;
    }
    public static ChannelData getDataFromPackage ( byte[] b )  {
        ChannelData data = new ChannelData ( false );
        int offset = 0;
        data.setOptions ( XByteBuffer.toInt ( b, offset ) );
        offset += 4;
        data.setTimestamp ( XByteBuffer.toLong ( b, offset ) );
        offset += 8;
        data.uniqueId = new byte[XByteBuffer.toInt ( b, offset )];
        offset += 4;
        System.arraycopy ( b, offset, data.uniqueId, 0, data.uniqueId.length );
        offset += data.uniqueId.length;
        byte[] addr = new byte[XByteBuffer.toInt ( b, offset )];
        offset += 4;
        System.arraycopy ( b, offset, addr, 0, addr.length );
        data.setAddress ( MemberImpl.getMember ( addr ) );
        offset += addr.length;
        int xsize = XByteBuffer.toInt ( b, offset );
        data.message = BufferPool.getBufferPool().getBuffer ( xsize, false );
        offset += 4;
        System.arraycopy ( b, offset, data.message.getBytesDirect(), 0, xsize );
        data.message.append ( b, offset, xsize );
        offset += xsize;
        return data;
    }
    @Override
    public int hashCode() {
        return XByteBuffer.toInt ( getUniqueId(), 0 );
    }
    @Override
    public boolean equals ( Object o ) {
        if ( o instanceof ChannelData ) {
            return Arrays.equals ( getUniqueId(), ( ( ChannelData ) o ).getUniqueId() );
        } else {
            return false;
        }
    }
    @Override
    public Object clone() {
        ChannelData clone = new ChannelData ( false );
        clone.options = this.options;
        clone.message = new XByteBuffer ( this.message.getBytesDirect(), false );
        clone.timestamp = this.timestamp;
        clone.uniqueId = this.uniqueId;
        clone.address = this.address;
        return clone;
    }
    @Override
    public Object deepclone() {
        byte[] d = this.getDataPackage();
        return ChannelData.getDataFromPackage ( d );
    }
    public static boolean sendAckSync ( int options ) {
        return ( ( Channel.SEND_OPTIONS_USE_ACK & options ) == Channel.SEND_OPTIONS_USE_ACK ) &&
               ( ( Channel.SEND_OPTIONS_SYNCHRONIZED_ACK & options ) == Channel.SEND_OPTIONS_SYNCHRONIZED_ACK );
    }
    public static boolean sendAckAsync ( int options ) {
        return ( ( Channel.SEND_OPTIONS_USE_ACK & options ) == Channel.SEND_OPTIONS_USE_ACK ) &&
               ( ( Channel.SEND_OPTIONS_SYNCHRONIZED_ACK & options ) != Channel.SEND_OPTIONS_SYNCHRONIZED_ACK );
    }
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append ( "ClusterData[src=" );
        buf.append ( getAddress() ).append ( "; id=" );
        buf.append ( bToS ( getUniqueId() ) ).append ( "; sent=" );
        buf.append ( new Timestamp ( this.getTimestamp() ).toString() ).append ( "]" );
        return buf.toString();
    }
    public static String bToS ( byte[] data ) {
        StringBuilder buf = new StringBuilder ( 4 * 16 );
        buf.append ( "{" );
        for ( int i = 0; data != null && i < data.length; i++ ) {
            buf.append ( String.valueOf ( data[i] ) ).append ( " " );
        }
        buf.append ( "}" );
        return buf.toString();
    }
}
