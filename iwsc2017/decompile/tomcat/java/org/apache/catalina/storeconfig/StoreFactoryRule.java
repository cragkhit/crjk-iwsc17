package org.apache.catalina.storeconfig;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
public class StoreFactoryRule extends Rule {
    private String attributeName;
    private String appenderAttributeName;
    private String storeFactoryClass;
    private String storeAppenderClass;
    public StoreFactoryRule ( final String storeFactoryClass, final String attributeName, final String storeAppenderClass, final String appenderAttributeName ) {
        this.storeFactoryClass = storeFactoryClass;
        this.attributeName = attributeName;
        this.appenderAttributeName = appenderAttributeName;
        this.storeAppenderClass = storeAppenderClass;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final IStoreFactory factory = ( IStoreFactory ) this.newInstance ( this.attributeName, this.storeFactoryClass, attributes );
        final StoreAppender storeAppender = ( StoreAppender ) this.newInstance ( this.appenderAttributeName, this.storeAppenderClass, attributes );
        factory.setStoreAppender ( storeAppender );
        final StoreDescription desc = ( StoreDescription ) this.digester.peek ( 0 );
        final StoreRegistry registry = ( StoreRegistry ) this.digester.peek ( 1 );
        factory.setRegistry ( registry );
        desc.setStoreFactory ( factory );
    }
    protected Object newInstance ( final String attr, final String defaultName, final Attributes attributes ) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String className = defaultName;
        if ( attr != null ) {
            final String value = attributes.getValue ( attr );
            if ( value != null ) {
                className = value;
            }
        }
        final Class<?> clazz = Class.forName ( className );
        return clazz.newInstance();
    }
}
