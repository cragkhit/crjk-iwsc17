package org.apache.catalina.tribes.membership;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.transport.SenderState;
import org.apache.catalina.tribes.util.StringManager;
public class MemberImpl implements Member, java.io.Externalizable {
    public static final boolean DO_DNS_LOOKUPS = Boolean.parseBoolean ( System.getProperty ( "org.apache.catalina.tribes.dns_lookups", "false" ) );
    public static final transient byte[] TRIBES_MBR_BEGIN = new byte[] {84, 82, 73, 66, 69, 83, 45, 66, 1, 0};
    public static final transient byte[] TRIBES_MBR_END   = new byte[] {84, 82, 73, 66, 69, 83, 45, 69, 1, 0};
    protected static final StringManager sm = StringManager.getManager ( Constants.Package );
    protected volatile byte[] host = new byte[0];
    protected transient volatile String hostname;
    protected volatile int port;
    protected volatile int udpPort = -1;
    protected volatile int securePort = -1;
    protected AtomicInteger msgCount = new AtomicInteger ( 0 );
    protected volatile long memberAliveTime = 0;
    protected transient long serviceStartTime;
    protected transient byte[] dataPkg = null;
    protected volatile byte[] uniqueId = new byte[16];
    protected volatile byte[] payload = new byte[0];
    protected volatile byte[] command = new byte[0];
    protected volatile byte[] domain = new byte[0];
    protected volatile boolean local = false;
    public MemberImpl() {
    }
    public MemberImpl ( String host,
                        int port,
                        long aliveTime ) throws IOException {
        setHostname ( host );
        this.port = port;
        this.memberAliveTime = aliveTime;
    }
    public MemberImpl ( String host,
                        int port,
                        long aliveTime,
                        byte[] payload ) throws IOException {
        this ( host, port, aliveTime );
        setPayload ( payload );
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
        msgCount.incrementAndGet();
    }
    public byte[] getData()  {
        return getData ( true );
    }
    @Override
    public byte[] getData ( boolean getalive )  {
        return getData ( getalive, false );
    }
    @Override
    public synchronized int getDataLength() {
        return TRIBES_MBR_BEGIN.length +
               4 +
               8 +
               4 +
               4 +
               4 +
               1 +
               host.length +
               4 +
               command.length +
               4 +
               domain.length +
               16 +
               4 +
               payload.length +
               TRIBES_MBR_END.length;
    }
    @Override
    public synchronized byte[] getData ( boolean getalive, boolean reset )  {
        if ( reset ) {
            dataPkg = null;
        }
        if ( dataPkg != null ) {
            if ( getalive ) {
                long alive = System.currentTimeMillis() - getServiceStartTime();
                byte[] result = dataPkg.clone();
                XByteBuffer.toBytes ( alive, result, TRIBES_MBR_BEGIN.length + 4 );
                dataPkg = result;
            }
            return dataPkg;
        }
        long alive = System.currentTimeMillis() - getServiceStartTime();
        byte[] data = new byte[getDataLength()];
        int bodylength = ( getDataLength() - TRIBES_MBR_BEGIN.length - TRIBES_MBR_END.length - 4 );
        int pos = 0;
        System.arraycopy ( TRIBES_MBR_BEGIN, 0, data, pos, TRIBES_MBR_BEGIN.length );
        pos += TRIBES_MBR_BEGIN.length;
        XByteBuffer.toBytes ( bodylength, data, pos );
        pos += 4;
        XByteBuffer.toBytes ( alive, data, pos );
        pos += 8;
        XByteBuffer.toBytes ( port, data, pos );
        pos += 4;
        XByteBuffer.toBytes ( securePort, data, pos );
        pos += 4;
        XByteBuffer.toBytes ( udpPort, data, pos );
        pos += 4;
        data[pos++] = ( byte ) host.length;
        System.arraycopy ( host, 0, data, pos, host.length );
        pos += host.length;
        XByteBuffer.toBytes ( command.length, data, pos );
        pos += 4;
        System.arraycopy ( command, 0, data, pos, command.length );
        pos += command.length;
        XByteBuffer.toBytes ( domain.length, data, pos );
        pos += 4;
        System.arraycopy ( domain, 0, data, pos, domain.length );
        pos += domain.length;
        System.arraycopy ( uniqueId, 0, data, pos, uniqueId.length );
        pos += uniqueId.length;
        XByteBuffer.toBytes ( payload.length, data, pos );
        pos += 4;
        System.arraycopy ( payload, 0, data, pos, payload.length );
        pos += payload.length;
        System.arraycopy ( TRIBES_MBR_END, 0, data, pos, TRIBES_MBR_END.length );
        pos += TRIBES_MBR_END.length;
        dataPkg = data;
        return data;
    }
    public static Member getMember ( byte[] data, MemberImpl member ) {
        return getMember ( data, 0, data.length, member );
    }
    public static Member getMember ( byte[] data, int offset, int length, MemberImpl member ) {
        int pos = offset;
        if ( XByteBuffer.firstIndexOf ( data, offset, TRIBES_MBR_BEGIN ) != pos ) {
            throw new IllegalArgumentException ( sm.getString ( "memberImpl.invalid.package.begin", org.apache.catalina.tribes.util.Arrays.toString ( TRIBES_MBR_BEGIN ) ) );
        }
        if ( length < ( TRIBES_MBR_BEGIN.length + 4 ) ) {
            throw new ArrayIndexOutOfBoundsException ( sm.getString ( "memberImpl.package.small" ) );
        }
        pos += TRIBES_MBR_BEGIN.length;
        int bodylength = XByteBuffer.toInt ( data, pos );
        pos += 4;
        if ( length < ( bodylength + 4 + TRIBES_MBR_BEGIN.length + TRIBES_MBR_END.length ) ) {
            throw new ArrayIndexOutOfBoundsException ( sm.getString ( "memberImpl.notEnough.bytes" ) );
        }
        int endpos = pos + bodylength;
        if ( XByteBuffer.firstIndexOf ( data, endpos, TRIBES_MBR_END ) != endpos ) {
            throw new IllegalArgumentException ( sm.getString ( "memberImpl.invalid.package.end", org.apache.catalina.tribes.util.Arrays.toString ( TRIBES_MBR_END ) ) );
        }
        byte[] alived = new byte[8];
        System.arraycopy ( data, pos, alived, 0, 8 );
        pos += 8;
        byte[] portd = new byte[4];
        System.arraycopy ( data, pos, portd, 0, 4 );
        pos += 4;
        byte[] sportd = new byte[4];
        System.arraycopy ( data, pos, sportd, 0, 4 );
        pos += 4;
        byte[] uportd = new byte[4];
        System.arraycopy ( data, pos, uportd, 0, 4 );
        pos += 4;
        byte hl = data[pos++];
        byte[] addr = new byte[hl];
        System.arraycopy ( data, pos, addr, 0, hl );
        pos += hl;
        int cl = XByteBuffer.toInt ( data, pos );
        pos += 4;
        byte[] command = new byte[cl];
        System.arraycopy ( data, pos, command, 0, command.length );
        pos += command.length;
        int dl = XByteBuffer.toInt ( data, pos );
        pos += 4;
        byte[] domain = new byte[dl];
        System.arraycopy ( data, pos, domain, 0, domain.length );
        pos += domain.length;
        byte[] uniqueId = new byte[16];
        System.arraycopy ( data, pos, uniqueId, 0, 16 );
        pos += 16;
        int pl = XByteBuffer.toInt ( data, pos );
        pos += 4;
        byte[] payload = new byte[pl];
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
        member.dataPkg = new byte[length];
        System.arraycopy ( data, offset, member.dataPkg, 0, length );
        return member;
    }
    public static Member getMember ( byte[] data ) {
        return getMember ( data, new MemberImpl() );
    }
    public static Member getMember ( byte[] data, int offset, int length ) {
        return getMember ( data, offset, length, new MemberImpl() );
    }
    @Override
    public String getName() {
        return "tcp://" + getHostname() + ":" + getPort();
    }
    @Override
    public int getPort()  {
        return this.port;
    }
    @Override
    public byte[] getHost()  {
        return host;
    }
    public String getHostname() {
        if ( this.hostname != null ) {
            return hostname;
        } else {
            try {
                byte[] host = this.host;
                if ( DO_DNS_LOOKUPS ) {
                    this.hostname = java.net.InetAddress.getByAddress ( host ).getHostName();
                } else {
                    this.hostname = org.apache.catalina.tribes.util.Arrays.toString ( host, 0, host.length, true );
                }
                return this.hostname;
            } catch ( IOException x ) {
                throw new RuntimeException ( sm.getString ( "memberImpl.unableParse.hostname" ), x );
            }
        }
    }
    public int getMsgCount() {
        return msgCount.get();
    }
    @Override
    public long getMemberAliveTime() {
        return memberAliveTime;
    }
    public long getServiceStartTime() {
        return serviceStartTime;
    }
    @Override
    public byte[] getUniqueId() {
        return uniqueId;
    }
    @Override
    public byte[] getPayload() {
        return payload;
    }
    @Override
    public byte[] getCommand() {
        return command;
    }
    @Override
    public byte[] getDomain() {
        return domain;
    }
    @Override
    public int getSecurePort() {
        return securePort;
    }
    @Override
    public int getUdpPort() {
        return udpPort;
    }
    @Override
    public void setMemberAliveTime ( long time ) {
        memberAliveTime = time;
    }
    @Override
    public String toString()  {
        StringBuilder buf = new StringBuilder ( getClass().getName() );
        buf.append ( "[" );
        buf.append ( getName() ).append ( "," );
        buf.append ( getHostname() ).append ( "," );
        buf.append ( port ).append ( ", alive=" );
        buf.append ( memberAliveTime ).append ( ", " );
        buf.append ( "securePort=" ).append ( securePort ).append ( ", " );
        buf.append ( "UDP Port=" ).append ( udpPort ).append ( ", " );
        buf.append ( "id=" ).append ( bToS ( this.uniqueId ) ).append ( ", " );
        buf.append ( "payload=" ).append ( bToS ( this.payload, 8 ) ).append ( ", " );
        buf.append ( "command=" ).append ( bToS ( this.command, 8 ) ).append ( ", " );
        buf.append ( "domain=" ).append ( bToS ( this.domain, 8 ) ).append ( ", " );
        buf.append ( "]" );
        return buf.toString();
    }
    public static String bToS ( byte[] data ) {
        return bToS ( data, data.length );
    }
    public static String bToS ( byte[] data, int max ) {
        StringBuilder buf = new StringBuilder ( 4 * 16 );
        buf.append ( "{" );
        for ( int i = 0; data != null && i < data.length; i++ ) {
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
        return getHost() [0] + getHost() [1] + getHost() [2] + getHost() [3];
    }
    @Override
    public boolean equals ( Object o ) {
        if ( o instanceof MemberImpl )    {
            return Arrays.equals ( this.getHost(), ( ( MemberImpl ) o ).getHost() ) &&
                   this.getPort() == ( ( MemberImpl ) o ).getPort() &&
                   Arrays.equals ( this.getUniqueId(), ( ( MemberImpl ) o ).getUniqueId() );
        } else {
            return false;
        }
    }
    public synchronized void setHost ( byte[] host ) {
        this.host = host;
    }
    public void setHostname ( String host ) throws IOException {
        hostname = host;
        synchronized ( this ) {
            this.host = java.net.InetAddress.getByName ( host ).getAddress();
        }
    }
    public void setMsgCount ( int msgCount ) {
        this.msgCount.set ( msgCount );
    }
    public synchronized void setPort ( int port ) {
        this.port = port;
        this.dataPkg = null;
    }
    public void setServiceStartTime ( long serviceStartTime ) {
        this.serviceStartTime = serviceStartTime;
    }
    public synchronized void setUniqueId ( byte[] uniqueId ) {
        this.uniqueId = uniqueId != null ? uniqueId : new byte[16];
        getData ( true, true );
    }
    @Override
    public synchronized void setPayload ( byte[] payload ) {
        long oldPayloadLength = 0;
        if ( this.payload != null ) {
            oldPayloadLength = this.payload.length;
        }
        long newPayloadLength = 0;
        if ( payload != null ) {
            newPayloadLength = payload.length;
        }
        if ( newPayloadLength > oldPayloadLength ) {
            if ( ( newPayloadLength - oldPayloadLength + getData ( false, false ).length ) >
                    McastServiceImpl.MAX_PACKET_SIZE ) {
                throw new IllegalArgumentException ( sm.getString ( "memberImpl.large.payload" ) );
            }
        }
        this.payload = payload != null ? payload : new byte[0];
        getData ( true, true );
    }
    @Override
    public synchronized void setCommand ( byte[] command ) {
        this.command = command != null ? command : new byte[0];
        getData ( true, true );
    }
    public synchronized void setDomain ( byte[] domain ) {
        this.domain = domain != null ? domain : new byte[0];
        getData ( true, true );
    }
    public synchronized void setSecurePort ( int securePort ) {
        this.securePort = securePort;
        this.dataPkg = null;
    }
    public synchronized void setUdpPort ( int port ) {
        this.udpPort = port;
        this.dataPkg = null;
    }
    @Override
    public boolean isLocal() {
        return local;
    }
    @Override
    public void setLocal ( boolean local ) {
        this.local = local;
    }
    @Override
    public void readExternal ( ObjectInput in ) throws IOException, ClassNotFoundException {
        int length = in.readInt();
        byte[] message = new byte[length];
        in.readFully ( message );
        getMember ( message, this );
    }
    @Override
    public void writeExternal ( ObjectOutput out ) throws IOException {
        byte[] data = this.getData();
        out.writeInt ( data.length );
        out.write ( data );
    }
}
