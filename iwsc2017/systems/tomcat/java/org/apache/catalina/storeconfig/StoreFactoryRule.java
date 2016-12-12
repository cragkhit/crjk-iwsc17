package org.apache.catalina.storeconfig;
import org.apache.tomcat.util.digester.Rule;
import org.xml.sax.Attributes;
public class StoreFactoryRule extends Rule {
    public StoreFactoryRule ( String storeFactoryClass, String attributeName,
                              String storeAppenderClass, String appenderAttributeName ) {
        this.storeFactoryClass = storeFactoryClass;
        this.attributeName = attributeName;
        this.appenderAttributeName = appenderAttributeName;
        this.storeAppenderClass = storeAppenderClass;
    }
    private String attributeName;
    private String appenderAttributeName;
    private String storeFactoryClass;
    private String storeAppenderClass;
    @Override
    public void begin ( String namespace, String name, Attributes attributes )
    throws Exception {
        IStoreFactory factory = ( IStoreFactory ) newInstance ( attributeName,
                                storeFactoryClass, attributes );
        StoreAppender storeAppender = ( StoreAppender ) newInstance (
                                          appenderAttributeName, storeAppenderClass, attributes );
        factory.setStoreAppender ( storeAppender );
        StoreDescription desc = ( StoreDescription ) digester.peek ( 0 );
        StoreRegistry registry = ( StoreRegistry ) digester.peek ( 1 );
        factory.setRegistry ( registry );
        desc.setStoreFactory ( factory );
    }
    protected Object newInstance ( String attr, String defaultName,
                                   Attributes attributes ) throws ClassNotFoundException,
        InstantiationException, IllegalAccessException {
        String className = defaultName;
        if ( attr != null ) {
            String value = attributes.getValue ( attr );
            if ( value != null ) {
                className = value;
            }
        }
        Class<?> clazz = Class.forName ( className );
        return clazz.newInstance();
    }
}
