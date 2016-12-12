package org.apache.tomcat.util.digester;
import org.xml.sax.Attributes;
public interface ObjectCreationFactory {
    public Object createObject ( Attributes attributes ) throws Exception;
    public Digester getDigester();
    public void setDigester ( Digester digester );
}
