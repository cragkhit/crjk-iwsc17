package org.apache.catalina.startup;
import java.lang.reflect.Method;
import org.apache.catalina.Container;
import org.apache.tomcat.util.digester.Rule;
import org.xml.sax.Attributes;
public class CopyParentClassLoaderRule extends Rule {
    public CopyParentClassLoaderRule() {
    }
    @Override
    public void begin ( String namespace, String name, Attributes attributes )
    throws Exception {
        if ( digester.getLogger().isDebugEnabled() ) {
            digester.getLogger().debug ( "Copying parent class loader" );
        }
        Container child = ( Container ) digester.peek ( 0 );
        Object parent = digester.peek ( 1 );
        Method method =
            parent.getClass().getMethod ( "getParentClassLoader", new Class[0] );
        ClassLoader classLoader =
            ( ClassLoader ) method.invoke ( parent, new Object[0] );
        child.setParentClassLoader ( classLoader );
    }
}
