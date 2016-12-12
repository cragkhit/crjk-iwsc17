package org.apache.tomcat.util.digester;
import org.xml.sax.Attributes;
public interface ObjectCreationFactory {
    Object createObject ( Attributes p0 ) throws Exception;
    Digester getDigester();
    void setDigester ( Digester p0 );
}
