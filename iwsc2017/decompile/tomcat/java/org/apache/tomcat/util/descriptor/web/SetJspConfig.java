package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class SetJspConfig extends Rule {
    boolean isJspConfigSet;
    public SetJspConfig() {
        this.isJspConfigSet = false;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        if ( this.isJspConfigSet ) {
            throw new IllegalArgumentException ( "<jsp-config> element is limited to 1 occurrence" );
        }
        this.isJspConfigSet = true;
    }
}
