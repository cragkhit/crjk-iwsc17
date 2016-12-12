package org.apache.catalina.tribes;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
public class ByteMessage implements Externalizable {
    private byte[] message;
    public ByteMessage() {
    }
    public ByteMessage ( byte[] data ) {
        message = data;
    }
    public byte[] getMessage() {
        return message;
    }
    public void setMessage ( byte[] message ) {
        this.message = message;
    }
    @Override
    public void readExternal ( ObjectInput in ) throws IOException {
        int length = in.readInt();
        message = new byte[length];
        in.readFully ( message );
    }
    @Override
    public void writeExternal ( ObjectOutput out ) throws IOException {
        out.writeInt ( message != null ? message.length : 0 );
        if ( message != null ) {
            out.write ( message, 0, message.length );
        }
    }
}
