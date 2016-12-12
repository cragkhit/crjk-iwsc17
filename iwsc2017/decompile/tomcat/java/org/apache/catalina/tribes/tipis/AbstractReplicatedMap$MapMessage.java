package org.apache.catalina.tribes.tipis;
import org.apache.catalina.tribes.io.XByteBuffer;
import java.io.IOException;
import org.apache.catalina.tribes.Member;
import java.io.Serializable;
public static class MapMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int MSG_BACKUP = 1;
    public static final int MSG_RETRIEVE_BACKUP = 2;
    public static final int MSG_PROXY = 3;
    public static final int MSG_REMOVE = 4;
    public static final int MSG_STATE = 5;
    public static final int MSG_START = 6;
    public static final int MSG_STOP = 7;
    public static final int MSG_INIT = 8;
    public static final int MSG_COPY = 9;
    public static final int MSG_STATE_COPY = 10;
    public static final int MSG_ACCESS = 11;
    public static final int MSG_NOTIFY_MAPMEMBER = 12;
    public static final int MSG_PING = 13;
    private final byte[] mapId;
    private final int msgtype;
    private final boolean diff;
    private transient Serializable key;
    private transient Serializable value;
    private byte[] valuedata;
    private byte[] keydata;
    private final byte[] diffvalue;
    private final Member[] nodes;
    private Member primary;
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder ( "MapMessage[context=" );
        buf.append ( new String ( this.mapId ) );
        buf.append ( "; type=" );
        buf.append ( this.getTypeDesc() );
        buf.append ( "; key=" );
        buf.append ( this.key );
        buf.append ( "; value=" );
        buf.append ( this.value );
        return buf.toString();
    }
    public String getTypeDesc() {
        switch ( this.msgtype ) {
        case 1: {
            return "MSG_BACKUP";
        }
        case 2: {
            return "MSG_RETRIEVE_BACKUP";
        }
        case 3: {
            return "MSG_PROXY";
        }
        case 4: {
            return "MSG_REMOVE";
        }
        case 5: {
            return "MSG_STATE";
        }
        case 6: {
            return "MSG_START";
        }
        case 7: {
            return "MSG_STOP";
        }
        case 8: {
            return "MSG_INIT";
        }
        case 10: {
            return "MSG_STATE_COPY";
        }
        case 9: {
            return "MSG_COPY";
        }
        case 11: {
            return "MSG_ACCESS";
        }
        case 12: {
            return "MSG_NOTIFY_MAPMEMBER";
        }
        case 13: {
            return "MSG_PING";
        }
        default: {
            return "UNKNOWN";
        }
        }
    }
    public MapMessage ( final byte[] mapId, final int msgtype, final boolean diff, final Serializable key, final Serializable value, final byte[] diffvalue, final Member primary, final Member[] nodes ) {
        this.mapId = mapId;
        this.msgtype = msgtype;
        this.diff = diff;
        this.key = key;
        this.value = value;
        this.diffvalue = diffvalue;
        this.nodes = nodes;
        this.primary = primary;
        this.setValue ( value );
        this.setKey ( key );
    }
    public void deserialize ( final ClassLoader[] cls ) throws IOException, ClassNotFoundException {
        this.key ( cls );
        this.value ( cls );
    }
    public int getMsgType() {
        return this.msgtype;
    }
    public boolean isDiff() {
        return this.diff;
    }
    public Serializable getKey() {
        try {
            return this.key ( null );
        } catch ( Exception x ) {
            throw new RuntimeException ( AbstractReplicatedMap.sm.getString ( "mapMessage.deserialize.error.key" ), x );
        }
    }
    public Serializable key ( final ClassLoader[] cls ) throws IOException, ClassNotFoundException {
        if ( this.key != null ) {
            return this.key;
        }
        if ( this.keydata == null || this.keydata.length == 0 ) {
            return null;
        }
        this.key = XByteBuffer.deserialize ( this.keydata, 0, this.keydata.length, cls );
        this.keydata = null;
        return this.key;
    }
    public byte[] getKeyData() {
        return this.keydata;
    }
    public Serializable getValue() {
        try {
            return this.value ( null );
        } catch ( Exception x ) {
            throw new RuntimeException ( AbstractReplicatedMap.sm.getString ( "mapMessage.deserialize.error.value" ), x );
        }
    }
    public Serializable value ( final ClassLoader[] cls ) throws IOException, ClassNotFoundException {
        if ( this.value != null ) {
            return this.value;
        }
        if ( this.valuedata == null || this.valuedata.length == 0 ) {
            return null;
        }
        this.value = XByteBuffer.deserialize ( this.valuedata, 0, this.valuedata.length, cls );
        this.valuedata = null;
        return this.value;
    }
    public byte[] getValueData() {
        return this.valuedata;
    }
    public byte[] getDiffValue() {
        return this.diffvalue;
    }
    public Member[] getBackupNodes() {
        return this.nodes;
    }
    public Member getPrimary() {
        return this.primary;
    }
    private void setPrimary ( final Member m ) {
        this.primary = m;
    }
    public byte[] getMapId() {
        return this.mapId;
    }
    public void setValue ( final Serializable value ) {
        try {
            if ( value != null ) {
                this.valuedata = XByteBuffer.serialize ( value );
            }
            this.value = value;
        } catch ( IOException x ) {
            throw new RuntimeException ( x );
        }
    }
    public void setKey ( final Serializable key ) {
        try {
            if ( key != null ) {
                this.keydata = XByteBuffer.serialize ( key );
            }
            this.key = key;
        } catch ( IOException x ) {
            throw new RuntimeException ( x );
        }
    }
    public Object clone() {
        final MapMessage msg = new MapMessage ( this.mapId, this.msgtype, this.diff, this.key, this.value, this.diffvalue, this.primary, this.nodes );
        msg.keydata = this.keydata;
        msg.valuedata = this.valuedata;
        return msg;
    }
}
