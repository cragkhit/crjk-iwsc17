package org.apache.tomcat.util.digester;
import java.util.List;
public interface Rules {
    Digester getDigester();
    void setDigester ( Digester p0 );
    String getNamespaceURI();
    void setNamespaceURI ( String p0 );
    void add ( String p0, Rule p1 );
    void clear();
    List<Rule> match ( String p0, String p1 );
    List<Rule> rules();
}
