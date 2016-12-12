package org.apache.tomcat.util.net;
import java.nio.channels.SelectionKey;
public static class KeyReference {
    SelectionKey key;
    public KeyReference() {
        this.key = null;
    }
    public void finalize() {
        if ( this.key != null && this.key.isValid() ) {
            NioBlockingSelector.access$000().warn ( "Possible key leak, cancelling key in the finalizer." );
            try {
                this.key.cancel();
            } catch ( Exception ex ) {}
        }
        this.key = null;
    }
}
