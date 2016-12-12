package org.apache.catalina.startup;
import org.apache.catalina.Container;
import org.apache.catalina.LifecycleListener;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.digester.Rule;
import org.xml.sax.Attributes;
public class LifecycleListenerRule extends Rule {
    public LifecycleListenerRule ( String listenerClass, String attributeName ) {
        this.listenerClass = listenerClass;
        this.attributeName = attributeName;
    }
    private final String attributeName;
    private final String listenerClass;
    @Override
    public void begin ( String namespace, String name, Attributes attributes )
    throws Exception {
        Container c = ( Container ) digester.peek();
        Container p = null;
        Object obj = digester.peek ( 1 );
        if ( obj instanceof Container ) {
            p = ( Container ) obj;
        }
        String className = null;
        if ( attributeName != null ) {
            String value = attributes.getValue ( attributeName );
            if ( value != null ) {
                className = value;
            }
        }
        if ( p != null && className == null ) {
            String configClass =
                ( String ) IntrospectionUtils.getProperty ( p, attributeName );
            if ( configClass != null && configClass.length() > 0 ) {
                className = configClass;
            }
        }
        if ( className == null ) {
            className = listenerClass;
        }
        Class<?> clazz = Class.forName ( className );
        LifecycleListener listener =
            ( LifecycleListener ) clazz.newInstance();
        c.addLifecycleListener ( listener );
    }
}
