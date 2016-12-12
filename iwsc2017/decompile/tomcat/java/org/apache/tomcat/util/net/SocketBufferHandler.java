package org.apache.tomcat.util.net;
import org.apache.tomcat.util.buf.ByteBufferUtils;
import java.nio.ByteBuffer;
public class SocketBufferHandler {
    private volatile boolean readBufferConfiguredForWrite;
    private volatile ByteBuffer readBuffer;
    private volatile boolean writeBufferConfiguredForWrite;
    private volatile ByteBuffer writeBuffer;
    private final boolean direct;
    public SocketBufferHandler ( final int readBufferSize, final int writeBufferSize, final boolean direct ) {
        this.readBufferConfiguredForWrite = true;
        this.writeBufferConfiguredForWrite = true;
        this.direct = direct;
        if ( direct ) {
            this.readBuffer = ByteBuffer.allocateDirect ( readBufferSize );
            this.writeBuffer = ByteBuffer.allocateDirect ( writeBufferSize );
        } else {
            this.readBuffer = ByteBuffer.allocate ( readBufferSize );
            this.writeBuffer = ByteBuffer.allocate ( writeBufferSize );
        }
    }
    public void configureReadBufferForWrite() {
        this.setReadBufferConfiguredForWrite ( true );
    }
    public void configureReadBufferForRead() {
        this.setReadBufferConfiguredForWrite ( false );
    }
    private void setReadBufferConfiguredForWrite ( final boolean readBufferConFiguredForWrite ) {
        if ( this.readBufferConfiguredForWrite != readBufferConFiguredForWrite ) {
            if ( readBufferConFiguredForWrite ) {
                final int remaining = this.readBuffer.remaining();
                if ( remaining == 0 ) {
                    this.readBuffer.clear();
                } else {
                    this.readBuffer.compact();
                }
            } else {
                this.readBuffer.flip();
            }
            this.readBufferConfiguredForWrite = readBufferConFiguredForWrite;
        }
    }
    public ByteBuffer getReadBuffer() {
        return this.readBuffer;
    }
    public boolean isReadBufferEmpty() {
        if ( this.readBufferConfiguredForWrite ) {
            return this.readBuffer.position() == 0;
        }
        return this.readBuffer.remaining() == 0;
    }
    public void configureWriteBufferForWrite() {
        this.setWriteBufferConfiguredForWrite ( true );
    }
    public void configureWriteBufferForRead() {
        this.setWriteBufferConfiguredForWrite ( false );
    }
    private void setWriteBufferConfiguredForWrite ( final boolean writeBufferConfiguredForWrite ) {
        if ( this.writeBufferConfiguredForWrite != writeBufferConfiguredForWrite ) {
            if ( writeBufferConfiguredForWrite ) {
                final int remaining = this.writeBuffer.remaining();
                if ( remaining == 0 ) {
                    this.writeBuffer.clear();
                } else {
                    this.writeBuffer.compact();
                    this.writeBuffer.position ( remaining );
                    this.writeBuffer.limit ( this.writeBuffer.capacity() );
                }
            } else {
                this.writeBuffer.flip();
            }
            this.writeBufferConfiguredForWrite = writeBufferConfiguredForWrite;
        }
    }
    public boolean isWriteBufferWritable() {
        if ( this.writeBufferConfiguredForWrite ) {
            return this.writeBuffer.hasRemaining();
        }
        return this.writeBuffer.remaining() == 0;
    }
    public ByteBuffer getWriteBuffer() {
        return this.writeBuffer;
    }
    public boolean isWriteBufferEmpty() {
        if ( this.writeBufferConfiguredForWrite ) {
            return this.writeBuffer.position() == 0;
        }
        return this.writeBuffer.remaining() == 0;
    }
    public void reset() {
        this.readBuffer.clear();
        this.readBufferConfiguredForWrite = true;
        this.writeBuffer.clear();
        this.writeBufferConfiguredForWrite = true;
    }
    public void expand ( final int newSize ) {
        this.configureReadBufferForWrite();
        this.readBuffer = ByteBufferUtils.expand ( this.readBuffer, newSize );
        this.configureWriteBufferForWrite();
        this.writeBuffer = ByteBufferUtils.expand ( this.writeBuffer, newSize );
    }
    public void free() {
        if ( this.direct ) {
            ByteBufferUtils.cleanDirectBuffer ( this.readBuffer );
            ByteBufferUtils.cleanDirectBuffer ( this.writeBuffer );
        }
    }
}
