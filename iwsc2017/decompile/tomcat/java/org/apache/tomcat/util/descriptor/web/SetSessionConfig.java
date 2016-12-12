package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class SetSessionConfig extends Rule {
    boolean isSessionConfigSet;
    public SetSessionConfig() {
        this.isSessionConfigSet = false;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        if ( this.isSessionConfigSet ) {
            throw new IllegalArgumentException ( "<session-config> element is limited to 1 occurrence" );
        }
        this.isSessionConfigSet = true;
    }
}
