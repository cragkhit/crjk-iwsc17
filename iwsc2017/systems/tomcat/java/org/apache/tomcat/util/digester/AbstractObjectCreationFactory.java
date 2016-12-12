package org.apache.tomcat.util.digester;
import org.xml.sax.Attributes;
public abstract class AbstractObjectCreationFactory
    implements ObjectCreationFactory {
    private Digester digester = null;
    @Override
    public abstract Object createObject ( Attributes attributes ) throws Exception;
    @Override
    public Digester getDigester() {
        return ( this.digester );
    }
    @Override
    public void setDigester ( Digester digester ) {
        this.digester = digester;
    }
}
