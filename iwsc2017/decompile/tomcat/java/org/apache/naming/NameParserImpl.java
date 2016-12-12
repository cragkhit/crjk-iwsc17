package org.apache.naming;
import javax.naming.NamingException;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameParser;
public class NameParserImpl implements NameParser {
    @Override
    public Name parse ( final String name ) throws NamingException {
        return new CompositeName ( name );
    }
}
