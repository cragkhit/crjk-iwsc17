package org.apache.naming;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
public class NameParserImpl
    implements NameParser {
    @Override
    public Name parse ( String name )
    throws NamingException {
        return new CompositeName ( name );
    }
}
