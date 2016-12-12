package org.apache.naming.factory;
import javax.naming.RefAddr;
import javax.naming.InitialContext;
import java.util.Properties;
import javax.naming.Reference;
import org.apache.naming.EjbRef;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
public class OpenEjbFactory implements ObjectFactory {
    protected static final String DEFAULT_OPENEJB_FACTORY = "org.openejb.client.LocalInitialContextFactory";
    @Override
    public Object getObjectInstance ( final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment ) throws Exception {
        Object beanObj = null;
        if ( obj instanceof EjbRef ) {
            final Reference ref = ( Reference ) obj;
            String factory = "org.openejb.client.LocalInitialContextFactory";
            final RefAddr factoryRefAddr = ref.get ( "openejb.factory" );
            if ( factoryRefAddr != null ) {
                factory = factoryRefAddr.getContent().toString();
            }
            final Properties env = new Properties();
            ( ( Hashtable<String, String> ) env ).put ( "java.naming.factory.initial", factory );
            final RefAddr linkRefAddr = ref.get ( "openejb.link" );
            if ( linkRefAddr != null ) {
                final String ejbLink = linkRefAddr.getContent().toString();
                beanObj = new InitialContext ( env ).lookup ( ejbLink );
            }
        }
        return beanObj;
    }
}
