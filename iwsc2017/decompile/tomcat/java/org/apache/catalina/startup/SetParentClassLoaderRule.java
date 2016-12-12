package org.apache.catalina.startup;
import org.apache.catalina.Container;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class SetParentClassLoaderRule extends Rule {
    ClassLoader parentClassLoader;
    public SetParentClassLoaderRule ( final ClassLoader parentClassLoader ) {
        this.parentClassLoader = null;
        this.parentClassLoader = parentClassLoader;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( "Setting parent class loader" );
        }
        final Container top = ( Container ) this.digester.peek();
        top.setParentClassLoader ( this.parentClassLoader );
    }
}
