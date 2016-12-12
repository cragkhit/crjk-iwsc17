package org.apache.catalina.tribes.membership;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.net.InetAddress;
import org.apache.catalina.tribes.util.Arrays;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.transport.SenderState;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.catalina.tribes.util.StringManager;
import java.io.Externalizable;
import org.apache.catalina.tribes.Member;
public class MemberImpl implements Member, Externalizable {
    public static final boolean DO_DNS_LOOKUPS;
    public static final transient byte[] TRIBES_MBR_BEGIN;
    public static final transient byte[] TRIBES_MBR_END;
    protected static final StringManager sm;
    protected volatile byte[] host;
    protected transient volatile String hostname;
    protected volatile int port;
    protected volatile int udpPort;
    protected volatile int securePort;
    protected AtomicInteger msgCount;
    protected volatile long memberAliveTime;
    protected transient long serviceStartTime;
    protected transient byte[] dataPkg;
    protected volatile byte[] uniqueId;
    protected volatile byte[] payload;
    protected volatile byte[] command;
    protected volatile byte[] domain;
    protected volatile boolean local;
    public MemberImpl() {
        this.host = new byte[0];
        this.udpPort = -1;
        this.securePort = -1;
        this.msgCount = new AtomicInteger ( 0 );
        this.memberAliveTime = 0L;
        this.dataPkg = null;
        this.uniqueId = new byte[16];
        this.payload = new byte[0];
        this.command = new byte[0];
        this.domain = new byte[0];
        this.local = false;
    }
    public MemberImpl ( final String host, final int port, final long aliveTime ) throws IOException {
        this.host = new byte[0];
        this.udpPort = -1;
        this.securePort = -1;
        this.msgCount = new AtomicInteger ( 0 );
        this.memberAliveTime = 0L;
        this.dataPkg = null;
        this.uniqueId = new byte[16];
        this.payload = new byte[0];
        this.command = new byte[0];
        this.domain = new byte[0];
        this.local = false;
        this.setHostname ( host );
        this.port = port;
        this.memberAliveTime = aliveTime;
    }
    public MemberImpl ( final String host, final int port, final long aliveTime, final byte[] payload ) throws IOException {
        this ( host, port, aliveTime );
        this.setPayload ( payload );
    }
    @Override
    public boolean isReady() {
        return SenderState.getSenderState ( this ).isReady();
    }
    @Override
    public boolean isSuspect() {
        return SenderState.getSenderState ( this ).isSuspect();
    }
    @Override
    public boolean isFailing() {
        return SenderState.getSenderState ( this ).isFailing();
    }
    protected void inc() {
        this.msgCount.incrementAndGet();
    }
    public byte[] getData() {
        return this.getData ( true );
    }
    @Override
    public byte[] getData ( final boolean getalive ) {
        return this.getData ( getalive, false );
    }
    @Override
    public synchronized int getDataLength() {
        return MemberImpl.TRIBES_MBR_BEGIN.length + 4 + 8 + 4 + 4 + 4 + 1 + this.host.length + 4 + this.command.length + 4 + this.domain.length + 16 + 4 + this.payload.length + MemberImpl.TRIBES_MBR_END.length;
    }
    @Override
    public synchronized byte[] getData ( final boolean getalive, final boolean reset ) {
        if ( reset ) {
            this.dataPkg = null;
        }
        if ( this.dataPkg != null ) {
            if ( getalive ) {
                final long alive = System.currentTimeMillis() - this.getServiceStartTime();
                final byte[] result = this.dataPkg.clone();
                XByteBuffer.toBytes ( alive, result, MemberImpl.TRIBES_MBR_BEGIN.length + 4 );
                this.dataPkg = result;
            }
            return this.dataPkg;
        }
        final long alive = System.currentTimeMillis() - this.getServiceStartTime();
        final byte[] data = new byte[this.getDataLength()];
        final int bodylength = this.getDataLength() - MemberImpl.TRIBES_MBR_BEGIN.length - MemberImpl.TRIBES_MBR_END.length - 4;
        int pos = 0;
        System.arraycopy ( MemberImpl.TRIBES_MBR_BEGIN, 0, data, pos, MemberImpl.TRIBES_MBR_BEGIN.length );
        pos += MemberImpl.TRIBES_MBR_BEGIN.length;
        XByteBuffer.toBytes ( bodylength, data, pos );
        pos += 4;
        XByteBuffer.toBytes ( alive, data, pos );
        pos += 8;
        XByteBuffer.toBytes ( this.port, data, pos );
        pos += 4;
        XByteBuffer.toBytes ( this.securePort, data, pos );
        pos += 4;
        XByteBuffer.toBytes ( this.udpPort, data, pos );
        pos += 4;
        data[pos++] = ( byte ) this.host.length;
        System.arraycopy ( this.host, 0, data, pos, this.host.length );
        pos += this.host.length;
        XByteBuffer.toBytes ( this.command.length, data, pos );
        pos += 4;
        System.arraycopy ( this.command, 0, data, pos, this.command.length );
        pos += this.command.length;
        XByteBuffer.toBytes ( this.domain.length, data, pos );
        pos += 4;
        System.arraycopy ( this.domain, 0, data, pos, this.domain.length );
        pos += this.domain.length;
        System.arraycopy ( this.uniqueId, 0, data, pos, this.uniqueId.length );
        pos += this.uniqueId.length;
        XByteBuffer.toBytes ( this.payload.length, data, pos );
        pos += 4;
        System.arraycopy ( this.payload, 0, data, pos, this.payload.length );
        pos += this.payload.length;
        System.arraycopy ( MemberImpl.TRIBES_MBR_END, 0, data, pos, MemberImpl.TRIBES_MBR_END.length );
        pos += MemberImpl.TRIBES_MBR_END.length;
        return this.dataPkg = data;
    }
    public static Member getMember ( final byte[] data, final MemberImpl member ) {
        return getMember ( data, 0, data.length, member );
    }
    public static Member getMember ( final byte[] data, final int offset, final int length, final MemberImpl member ) {
        if ( XByteBuffer.firstIndexOf ( data, offset, MemberImpl.TRIBES_MBR_BEGIN ) != offset ) {
            throw new IllegalArgumentException ( MemberImpl.sm.getString ( "memberImpl.invalid.package.begin", Arrays.toString ( MemberImpl.TRIBES_MBR_BEGIN ) ) );
        }
        if ( length < MemberImpl.TRIBES_MBR_BEGIN.length + 4 ) {
            throw new ArrayIndexOutOfBoundsException ( MemberImpl.sm.getString ( "memberImpl.package.small" ) );
        }
        int pos = offset + MemberImpl.TRIBES_MBR_BEGIN.length;
        final int bodylength = XByteBuffer.toInt ( data, pos );
        pos += 4;
        if ( length < bodylength + 4 + MemberImpl.TRIBES_MBR_BEGIN.length + MemberImpl.TRIBES_MBR_END.length ) {
            throw new ArrayIndexOutOfBoundsException ( MemberImpl.sm.getString ( "memberImpl.notEnough.bytes" ) );
        }
        final int endpos = pos + bodylength;
        if ( XByteBuffer.firstIndexOf ( data, endpos, MemberImpl.TRIBES_MBR_END ) != endpos ) {
            throw new IllegalArgumentException ( MemberImpl.sm.getString ( "memberImpl.invalid.package.end", Arrays.toString ( MemberImpl.TRIBES_MBR_END ) ) );
        }
        final byte[] alived = new byte[8];
        System.arraycopy ( data, pos, alived, 0, 8 );
        pos += 8;
        final byte[] portd = new byte[4];
        System.arraycopy ( data, pos, portd, 0, 4 );
        pos += 4;
        final byte[] sportd = new byte[4];
        System.arraycopy ( data, pos, sportd, 0, 4 );
        pos += 4;
        final byte[] uportd = new byte[4];
        System.arraycopy ( data, pos, uportd, 0, 4 );
        pos += 4;
        final byte hl = data[pos++];
        final byte[] addr = new byte[hl];
        System.arraycopy ( data, pos, addr, 0, hl );
        pos += hl;
        final int cl = XByteBuffer.toInt ( data, pos );
        pos += 4;
        final byte[] command = new byte[cl];
        System.arraycopy ( data, pos, command, 0, command.length );
        pos += command.length;
        final int dl = XByteBuffer.toInt ( data, pos );
        pos += 4;
        final byte[] domain = new byte[dl];
        System.arraycopy ( data, pos, domain, 0, domain.length );
        pos += domain.length;
        final byte[] uniqueId = new byte[16];
        System.arraycopy ( data, pos, uniqueId, 0, 16 );
        pos += 16;
        final int pl = XByteBuffer.toInt ( data, pos );
        pos += 4;
        final byte[] payload = new byte[pl];
        System.arraycopy ( data, pos, payload, 0, payload.length );
        pos += payload.length;
        member.setHost ( addr );
        member.setPort ( XByteBuffer.toInt ( portd, 0 ) );
        member.setSecurePort ( XByteBuffer.toInt ( sportd, 0 ) );
        member.setUdpPort ( XByteBuffer.toInt ( uportd, 0 ) );
        member.setMemberAliveTime ( XByteBuffer.toLong ( alived, 0 ) );
        member.setUniqueId ( uniqueId );
        member.payload = payload;
        member.domain = domain;
        member.command = command;
        System.arraycopy ( data, offset, member.dataPkg = new byte[length], 0, length );
        return member;
    }
    public static Member getMember ( final byte[] data ) {
        return getMember ( data, new MemberImpl() );
    }
    public static Member getMember ( final byte[] data, final int offset, final int length ) {
        return getMember ( data, offset, length, new MemberImpl() );
    }
    @Override
    public String getName() {
        return "tcp://" + this.getHostname() + ":" + this.getPort();
    }
    @Override
    public int getPort() {
        return this.port;
    }
    @Override
    public byte[] getHost() {
        return this.host;
    }
    public String getHostname() {
        if ( this.hostname != null ) {
            return this.hostname;
        }
        try {
            final byte[] host = this.host;
            if ( MemberImpl.DO_DNS_LOOKUPS ) {
                this.hostname = InetAddress.getByAddress ( host ).getHostName();
            } else {
                this.hostname = Arrays.toString ( host, 0, host.length, true );
            }
            return this.hostname;
        } catch ( IOException x ) {
            throw new RuntimeException ( MemberImpl.sm.getString ( "memberImpl.unableParse.hostname" ), x );
        }
    }
    public int getMsgCount() {
        return this.msgCount.get();
    }
    @Override
    public long getMemberAliveTime() {
        return this.memberAliveTime;
    }
    public long getServiceStartTime() {
        return this.serviceStartTime;
    }
    @Override
    public byte[] getUniqueId() {
        return this.uniqueId;
    }
    @Override
    public byte[] getPayload() {
        return this.payload;
    }
    @Override
    public byte[] getCommand() {
        return this.command;
    }
    @Override
    public byte[] getDomain() {
        return this.domain;
    }
    @Override
    public int getSecurePort() {
        return this.securePort;
    }
    @Override
    public int getUdpPort() {
        return this.udpPort;
    }
    @Override
    public void setMemberAliveTime ( final long time ) {
        this.memberAliveTime = time;
    }
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder ( this.getClass().getName() );
        buf.append ( "[" );
        buf.append ( this.getName() ).append ( "," );
        buf.append ( this.getHostname() ).append ( "," );
        buf.append ( this.port ).append ( ", alive=" );
        buf.append ( this.memberAliveTime ).append ( ", " );
        buf.append ( "securePort=" ).append ( this.securePort ).append ( ", " );
        buf.append ( "UDP Port=" ).append ( this.udpPort ).append ( ", " );
        buf.append ( "id=" ).append ( bToS ( this.uniqueId ) ).append ( ", " );
        buf.append ( "payload=" ).append ( bToS ( this.payload, 8 ) ).append ( ", " );
        buf.append ( "command=" ).append ( bToS ( this.command, 8 ) ).append ( ", " );
        buf.append ( "domain=" ).append ( bToS ( this.domain, 8 ) ).append ( ", " );
        buf.append ( "]" );
        return buf.toString();
    }
    public static String bToS ( final byte[] data ) {
        return bToS ( data, data.length );
    }
    public static String bToS ( final byte[] data, final int max ) {
        final StringBuilder buf = new StringBuilder ( 64 );
        buf.append ( "{" );
        for ( int i = 0; data != null && i < data.length; ++i ) {
            buf.append ( String.valueOf ( data[i] ) ).append ( " " );
            if ( i == max ) {
                buf.append ( "...(" + data.length + ")" );
                break;
            }
        }
        buf.append ( "}" );
        return buf.toString();
    }
    @Override
    public int hashCode() {
        return this.getHost() [0] + this.getHost() [1] + this.getHost() [2] + this.getHost() [3];
    }
    @Override
    public boolean equals ( final Object o ) {
        return o instanceof MemberImpl && java.util.Arrays.equals ( this.getHost(), ( ( MemberImpl ) o ).getHost() ) && this.getPort() == ( ( MemberImpl ) o ).getPort() && java.util.Arrays.equals ( this.getUniqueId(), ( ( MemberImpl ) o ).getUniqueId() );
    }
    public synchronized void setHost ( final byte[] host ) {
        this.host = host;
    }
    public void setHostname ( final String host ) throws IOException {
        this.hostname = host;
        synchronized ( this ) {
            this.host = InetAddress.getByName ( host ).getAddress();
        }
    }
    public void setMsgCount ( final int msgCount ) {
        this.msgCount.set ( msgCount );
    }
    public synchronized void setPort ( final int port ) {
        this.port = port;
        this.dataPkg = null;
    }
    public void setServiceStartTime ( final long serviceStartTime ) {
        this.serviceStartTime = serviceStartTime;
    }
    public synchronized void setUniqueId ( final byte[] uniqueId ) {
        this.uniqueId = ( ( uniqueId != null ) ? uniqueId : new byte[16] );
        this.getData ( true, true );
    }
    @Override
    public synchronized void setPayload ( final byte[] payload ) {
        long oldPayloadLength = 0L;
        if ( this.payload != null ) {
            oldPayloadLength = this.payload.length;
        }
        long newPayloadLength = 0L;
        if ( payload != null ) {
            newPayloadLength = payload.length;
        }
        if ( newPayloadLength > oldPayloadLength && newPayloadLength - oldPayloadLength + this.getData ( false, false ).length > 65535L ) {
            throw new IllegalArgumentException ( MemberImpl.sm.getString ( "memberImpl.large.payload" ) );
        }
        this.payload = ( ( payload != null ) ? payload : new byte[0] );
        this.getData ( true, true );
    }
    @Override
    public synchronized void setCommand ( final byte[] command ) {
        this.command = ( ( command != null ) ? command : new byte[0] );
        this.getData ( true, true );
    }
    public synchronized void setDomain ( final byte[] domain ) {
        this.domain = ( ( domain != null ) ? domain : new byte[0] );
        this.getData ( true, true );
    }
    public synchronized void setSecurePort ( final int securePort ) {
        this.securePort = securePort;
        this.dataPkg = null;
    }
    public synchronized void setUdpPort ( final int port ) {
        this.udpPort = port;
        this.dataPkg = null;
    }
    @Override
    public boolean isLocal() {
        return this.local;
    }
    @Override
    public void setLocal ( final boolean local ) {
        this.local = local;
    }
    @Override
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        final int length = in.readInt();
        final byte[] message = new byte[length];
        in.readFully ( message );
        getMember ( message, this );
    }
    @Override
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        final byte[] data = this.getData();
        out.writeInt ( data.length );
        out.write ( data );
    }
    static {
        DO_DNS_LOOKUPS = Boolean.parseBoolean ( System.getProperty ( "org.apache.catalina.tribes.dns_lookups", "false" ) );
        TRIBES_MBR_BEGIN = new byte[] { 84, 82, 73, 66, 69, 83, 45, 66, 1, 0 };
        TRIBES_MBR_END = new byte[] { 84, 82, 73, 66, 69, 83, 45, 69, 1, 0 };
        sm = StringManager.getManager ( "org.apache.catalina.tribes.membership" );
    }
}
