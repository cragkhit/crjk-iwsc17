package org.apache.tomcat.util.descriptor.web;
import java.lang.reflect.Method;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class SetPublicIdRule extends Rule {
    private String method;
    public SetPublicIdRule ( final String method ) {
        this.method = null;
        this.method = method;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final Object top = this.digester.peek();
        final Class<?>[] paramClasses = ( Class<?>[] ) new Class[] { "String".getClass() };
        final String[] paramValues = { this.digester.getPublicId() };
        Method m = null;
        try {
            m = top.getClass().getMethod ( this.method, paramClasses );
        } catch ( NoSuchMethodException e ) {
            this.digester.getLogger().error ( "Can't find method " + this.method + " in " + top + " CLASS " + top.getClass() );
            return;
        }
        m.invoke ( top, ( Object[] ) paramValues );
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( "" + top.getClass().getName() + "." + this.method + "(" + paramValues[0] + ")" );
        }
    }
}
