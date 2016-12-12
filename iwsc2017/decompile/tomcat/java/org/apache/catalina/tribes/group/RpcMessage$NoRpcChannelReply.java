package org.apache.catalina.tribes.group;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.Serializable;
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
