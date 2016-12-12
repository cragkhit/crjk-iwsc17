package org.apache.catalina.tribes;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.Externalizable;
public class ByteMessage implements Externalizable {
    private byte[] message;
    public ByteMessage() {
    }
    public ByteMessage ( final byte[] data ) {
        this.message = data;
    }
    public byte[] getMessage() {
        return this.message;
    }
    public void setMessage ( final byte[] message ) {
        this.message = message;
    }
    @Override
    public void readExternal ( final ObjectInput in ) throws IOException {
        final int length = in.readInt();
        in.readFully ( this.message = new byte[length] );
    }
    @Override
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeInt ( ( this.message != null ) ? this.message.length : 0 );
        if ( this.message != null ) {
            out.write ( this.message, 0, this.message.length );
        }
    }
}
