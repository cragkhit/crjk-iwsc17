package org.apache.catalina.tribes;
import org.apache.catalina.tribes.util.Arrays;
import java.io.Serializable;
public final class UniqueId implements Serializable {
    private static final long serialVersionUID = 1L;
    final byte[] id;
    public UniqueId() {
        this ( null );
    }
    public UniqueId ( final byte[] id ) {
        this.id = id;
    }
    public UniqueId ( final byte[] id, final int offset, final int length ) {
        System.arraycopy ( id, offset, this.id = new byte[length], 0, length );
    }
    @Override
    public int hashCode() {
        if ( this.id == null ) {
            return 0;
        }
        return Arrays.hashCode ( this.id );
    }
    @Override
    public boolean equals ( final Object other ) {
        boolean result = other instanceof UniqueId;
        if ( result ) {
            final UniqueId uid = ( UniqueId ) other;
            result = ( ( this.id == null && uid.id == null ) || ( ( this.id != null || uid.id == null ) && ( this.id == null || uid.id != null ) && Arrays.equals ( this.id, uid.id ) ) );
        }
        return result;
    }
    public byte[] getBytes() {
        return this.id;
    }
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder ( "UniqueId" );
        buf.append ( Arrays.toString ( this.id ) );
        return buf.toString();
    }
}
