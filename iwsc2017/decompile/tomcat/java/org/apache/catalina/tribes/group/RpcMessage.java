package org.apache.catalina.tribes.group;
import org.apache.catalina.tribes.util.Arrays;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.Serializable;
import java.io.Externalizable;
public class RpcMessage implements Externalizable {
    protected Serializable message;
    protected byte[] uuid;
    protected byte[] rpcId;
    protected boolean reply;
    public RpcMessage() {
        this.reply = false;
    }
    public RpcMessage ( final byte[] rpcId, final byte[] uuid, final Serializable message ) {
        this.reply = false;
        this.rpcId = rpcId;
        this.uuid = uuid;
        this.message = message;
    }
    @Override
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.reply = in.readBoolean();
        int length = in.readInt();
        in.readFully ( this.uuid = new byte[length] );
        length = in.readInt();
        in.readFully ( this.rpcId = new byte[length] );
        this.message = ( Serializable ) in.readObject();
    }
    @Override
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeBoolean ( this.reply );
        out.writeInt ( this.uuid.length );
        out.write ( this.uuid, 0, this.uuid.length );
        out.writeInt ( this.rpcId.length );
        out.write ( this.rpcId, 0, this.rpcId.length );
        out.writeObject ( this.message );
    }
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder ( "RpcMessage[" );
        buf.append ( super.toString() );
        buf.append ( "] rpcId=" );
        buf.append ( Arrays.toString ( this.rpcId ) );
        buf.append ( "; uuid=" );
        buf.append ( Arrays.toString ( this.uuid ) );
        buf.append ( "; msg=" );
        buf.append ( this.message );
        return buf.toString();
    }
    public static class NoRpcChannelReply extends RpcMessage {
        public NoRpcChannelReply() {
        }
        public NoRpcChannelReply ( final byte[] rpcid, final byte[] uuid ) {
            super ( rpcid, uuid, null );
            this.reply = true;
        }
        @Override
        public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
            this.reply = true;
            int length = in.readInt();
            in.readFully ( this.uuid = new byte[length] );
            length = in.readInt();
            in.readFully ( this.rpcId = new byte[length] );
        }
        @Override
        public void writeExternal ( final ObjectOutput out ) throws IOException {
            out.writeInt ( this.uuid.length );
            out.write ( this.uuid, 0, this.uuid.length );
            out.writeInt ( this.rpcId.length );
            out.write ( this.rpcId, 0, this.rpcId.length );
        }
    }
}
