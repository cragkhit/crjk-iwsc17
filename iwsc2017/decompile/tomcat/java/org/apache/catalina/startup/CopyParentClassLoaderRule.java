package org.apache.catalina.startup;
import java.lang.reflect.Method;
import org.apache.catalina.Container;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
public class CopyParentClassLoaderRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( "Copying parent class loader" );
        }
        final Container child = ( Container ) this.digester.peek ( 0 );
        final Object parent = this.digester.peek ( 1 );
        final Method method = parent.getClass().getMethod ( "getParentClassLoader", ( Class<?>[] ) new Class[0] );
        final ClassLoader classLoader = ( ClassLoader ) method.invoke ( parent, new Object[0] );
        child.setParentClassLoader ( classLoader );
    }
}
