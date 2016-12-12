package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class SetLoginConfig extends Rule {
    boolean isLoginConfigSet;
    public SetLoginConfig() {
        this.isLoginConfigSet = false;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        if ( this.isLoginConfigSet ) {
            throw new IllegalArgumentException ( "<login-config> element is limited to 1 occurrence" );
        }
        this.isLoginConfigSet = true;
    }
}
