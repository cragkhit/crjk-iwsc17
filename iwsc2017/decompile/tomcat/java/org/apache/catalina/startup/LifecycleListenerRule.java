package org.apache.catalina.startup;
import org.apache.catalina.LifecycleListener;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.catalina.Container;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
public class LifecycleListenerRule extends Rule {
    private final String attributeName;
    private final String listenerClass;
    public LifecycleListenerRule ( final String listenerClass, final String attributeName ) {
        this.listenerClass = listenerClass;
        this.attributeName = attributeName;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final Container c = ( Container ) this.digester.peek();
        Container p = null;
        final Object obj = this.digester.peek ( 1 );
        if ( obj instanceof Container ) {
            p = ( Container ) obj;
        }
        String className = null;
        if ( this.attributeName != null ) {
            final String value = attributes.getValue ( this.attributeName );
            if ( value != null ) {
                className = value;
            }
        }
        if ( p != null && className == null ) {
            final String configClass = ( String ) IntrospectionUtils.getProperty ( p, this.attributeName );
            if ( configClass != null && configClass.length() > 0 ) {
                className = configClass;
            }
        }
        if ( className == null ) {
            className = this.listenerClass;
        }
        final Class<?> clazz = Class.forName ( className );
        final LifecycleListener listener = ( LifecycleListener ) clazz.newInstance();
        c.addLifecycleListener ( listener );
    }
}
