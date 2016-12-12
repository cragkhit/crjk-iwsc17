package org.apache.catalina.ha.session;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.Externalizable;
private static class AttributeInfo implements Externalizable {
    private String name;
    private Object value;
    private int action;
    private int type;
    public AttributeInfo() {
        this ( -1, -1, null, null );
    }
    public AttributeInfo ( final int type, final int action, final String name, final Object value ) {
        this.name = null;
        this.value = null;
        this.init ( type, action, name, value );
    }
    public void init ( final int type, final int action, final String name, final Object value ) {
        this.name = name;
        this.value = value;
        this.action = action;
        this.type = type;
    }
    public int getType() {
        return this.type;
    }
    public int getAction() {
        return this.action;
    }
    public Object getValue() {
        return this.value;
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    public String getName() {
        return this.name;
    }
    public void recycle() {
        this.name = null;
        this.value = null;
        this.type = -1;
        this.action = -1;
    }
    @Override
    public boolean equals ( final Object o ) {
        if ( ! ( o instanceof AttributeInfo ) ) {
            return false;
        }
        final AttributeInfo other = ( AttributeInfo ) o;
        return other.getName().equals ( this.getName() );
    }
    @Override
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.type = in.readInt();
        this.action = in.readInt();
        this.name = in.readUTF();
        final boolean hasValue = in.readBoolean();
        if ( hasValue ) {
            this.value = in.readObject();
        }
    }
    @Override
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeInt ( this.getType() );
        out.writeInt ( this.getAction() );
        out.writeUTF ( this.getName() );
        out.writeBoolean ( this.getValue() != null );
        if ( this.getValue() != null ) {
            out.writeObject ( this.getValue() );
        }
    }
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder ( "AttributeInfo[type=" );
        buf.append ( this.getType() ).append ( ", action=" ).append ( this.getAction() );
        buf.append ( ", name=" ).append ( this.getName() ).append ( ", value=" ).append ( this.getValue() );
        buf.append ( ", addr=" ).append ( super.toString() ).append ( "]" );
        return buf.toString();
    }
}
