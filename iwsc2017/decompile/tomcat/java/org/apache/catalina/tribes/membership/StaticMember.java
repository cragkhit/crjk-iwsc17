package org.apache.catalina.tribes.membership;
import org.apache.catalina.tribes.util.Arrays;
import java.io.IOException;
public class StaticMember extends MemberImpl {
    public StaticMember() {
    }
    public StaticMember ( final String host, final int port, final long aliveTime ) throws IOException {
        super ( host, port, aliveTime );
    }
    public StaticMember ( final String host, final int port, final long aliveTime, final byte[] payload ) throws IOException {
        super ( host, port, aliveTime, payload );
    }
    public void setHost ( final String host ) {
        if ( host == null ) {
            return;
        }
        if ( host.startsWith ( "{" ) ) {
            this.setHost ( Arrays.fromString ( host ) );
        } else {
            try {
                this.setHostname ( host );
            } catch ( IOException x ) {
                throw new RuntimeException ( x );
            }
        }
    }
    public void setDomain ( final String domain ) {
        if ( domain == null ) {
            return;
        }
        if ( domain.startsWith ( "{" ) ) {
            this.setDomain ( Arrays.fromString ( domain ) );
        } else {
            this.setDomain ( Arrays.convert ( domain ) );
        }
    }
    public void setUniqueId ( final String id ) {
        final byte[] uuid = Arrays.fromString ( id );
        if ( uuid == null || uuid.length != 16 ) {
            throw new RuntimeException ( StaticMember.sm.getString ( "staticMember.invalid.uuidLength", id ) );
        }
        this.setUniqueId ( uuid );
    }
}
