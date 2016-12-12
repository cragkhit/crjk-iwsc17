package org.apache.tomcat.util.digester;
import java.util.List;
public interface Rules {
    public Digester getDigester();
    public void setDigester ( Digester digester );
    public String getNamespaceURI();
    public void setNamespaceURI ( String namespaceURI );
    public void add ( String pattern, Rule rule );
    public void clear();
    public List<Rule> match ( String namespaceURI, String pattern );
    public List<Rule> rules();
}
