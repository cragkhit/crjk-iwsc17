package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class NameRule extends Rule {
    boolean isNameSet;
    public NameRule() {
        this.isNameSet = false;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        if ( this.isNameSet ) {
            throw new IllegalArgumentException ( WebRuleSet.sm.getString ( "webRuleSet.nameCount" ) );
        }
        this.isNameSet = true;
    }
    @Override
    public void body ( final String namespace, final String name, final String text ) throws Exception {
        super.body ( namespace, name, text );
        ( ( WebXml ) this.digester.peek() ).setName ( text );
    }
}
