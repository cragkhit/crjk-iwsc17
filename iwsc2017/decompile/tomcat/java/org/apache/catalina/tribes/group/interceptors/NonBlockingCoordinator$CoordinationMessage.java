package org.apache.catalina.tribes.group.interceptors;
import org.apache.catalina.tribes.membership.MemberImpl;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.io.XByteBuffer;
public static class CoordinationMessage {
    protected final XByteBuffer buf;
    protected Member leader;
    protected Member source;
    protected Member[] view;
    protected UniqueId id;
    protected byte[] type;
    public CoordinationMessage ( final XByteBuffer buf ) {
        this.buf = buf;
        this.parse();
    }
    public CoordinationMessage ( final Member leader, final Member source, final Member[] view, final UniqueId id, final byte[] type ) {
        this.buf = new XByteBuffer ( 4096, false );
        this.leader = leader;
        this.source = source;
        this.view = view;
        this.id = id;
        this.type = type;
        this.write();
    }
    public byte[] getHeader() {
        return NonBlockingCoordinator.COORD_HEADER;
    }
    public Member getLeader() {
        if ( this.leader == null ) {
            this.parse();
        }
        return this.leader;
    }
    public Member getSource() {
        if ( this.source == null ) {
            this.parse();
        }
        return this.source;
    }
    public UniqueId getId() {
        if ( this.id == null ) {
            this.parse();
        }
        return this.id;
    }
    public Member[] getMembers() {
        if ( this.view == null ) {
            this.parse();
        }
        return this.view;
    }
    public byte[] getType() {
        if ( this.type == null ) {
            this.parse();
        }
        return this.type;
    }
    public XByteBuffer getBuffer() {
        return this.buf;
    }
    public void parse() {
        int offset = 16;
        final int ldrLen = XByteBuffer.toInt ( this.buf.getBytesDirect(), offset );
        offset += 4;
        final byte[] ldr = new byte[ldrLen];
        System.arraycopy ( this.buf.getBytesDirect(), offset, ldr, 0, ldrLen );
        this.leader = MemberImpl.getMember ( ldr );
        offset += ldrLen;
        final int srcLen = XByteBuffer.toInt ( this.buf.getBytesDirect(), offset );
        offset += 4;
        final byte[] src = new byte[srcLen];
        System.arraycopy ( this.buf.getBytesDirect(), offset, src, 0, srcLen );
        this.source = MemberImpl.getMember ( src );
        offset += srcLen;
        final int mbrCount = XByteBuffer.toInt ( this.buf.getBytesDirect(), offset );
        offset += 4;
        this.view = new Member[mbrCount];
        for ( int i = 0; i < this.view.length; ++i ) {
            final int mbrLen = XByteBuffer.toInt ( this.buf.getBytesDirect(), offset );
            offset += 4;
            final byte[] mbr = new byte[mbrLen];
            System.arraycopy ( this.buf.getBytesDirect(), offset, mbr, 0, mbrLen );
            this.view[i] = MemberImpl.getMember ( mbr );
            offset += mbrLen;
        }
        this.id = new UniqueId ( this.buf.getBytesDirect(), offset, 16 );
        offset += 16;
        this.type = new byte[16];
        System.arraycopy ( this.buf.getBytesDirect(), offset, this.type, 0, this.type.length );
        offset += 16;
    }
    public void write() {
        this.buf.reset();
        this.buf.append ( NonBlockingCoordinator.COORD_HEADER, 0, NonBlockingCoordinator.COORD_HEADER.length );
        byte[] ldr = this.leader.getData ( false, false );
        this.buf.append ( ldr.length );
        this.buf.append ( ldr, 0, ldr.length );
        ldr = null;
        byte[] src = this.source.getData ( false, false );
        this.buf.append ( src.length );
        this.buf.append ( src, 0, src.length );
        src = null;
        this.buf.append ( this.view.length );
        for ( int i = 0; i < this.view.length; ++i ) {
            final byte[] mbr = this.view[i].getData ( false, false );
            this.buf.append ( mbr.length );
            this.buf.append ( mbr, 0, mbr.length );
        }
        this.buf.append ( this.id.getBytes(), 0, this.id.getBytes().length );
        this.buf.append ( this.type, 0, this.type.length );
    }
}
