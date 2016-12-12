package org.apache.tomcat.util.descriptor.tld;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
private static class ScriptVariableRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        this.digester.push ( new Variable() );
    }
    @Override
    public void end ( final String namespace, final String name ) throws Exception {
        final Variable variable = ( Variable ) this.digester.pop();
        final TagXml tag = ( TagXml ) this.digester.peek();
        tag.getVariables().add ( variable.toTagVariableInfo() );
    }
}
