package org.apache.catalina.storeconfig;
import java.util.HashSet;
import java.io.IOException;
import java.io.File;
import java.beans.IntrospectionException;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.net.SocketProperties;
import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.catalina.connector.Connector;
import java.io.PrintWriter;
import java.util.Set;
import java.util.HashMap;
public class ConnectorStoreAppender extends StoreAppender {
    protected static final HashMap<String, String> replacements;
    protected static final Set<String> internalExecutorAttributes;
    @Override
    public void printAttributes ( final PrintWriter writer, final int indent, final boolean include, final Object bean, final StoreDescription desc ) throws Exception {
        if ( include && desc != null && !desc.isStandard() ) {
            writer.print ( " className=\"" );
            writer.print ( bean.getClass().getName() );
            writer.print ( "\"" );
        }
        final Connector connector = ( Connector ) bean;
        final String protocol = connector.getProtocol();
        final List<String> propertyKeys = this.getPropertyKeys ( connector );
        final Object bean2 = new Connector ( protocol );
        for ( final String key : propertyKeys ) {
            final Object value = IntrospectionUtils.getProperty ( bean, key );
            if ( desc.isTransientAttribute ( key ) ) {
                continue;
            }
            if ( value == null ) {
                continue;
            }
            if ( !this.isPersistable ( value.getClass() ) ) {
                continue;
            }
            final Object value2 = IntrospectionUtils.getProperty ( bean2, key );
            if ( value.equals ( value2 ) ) {
                continue;
            }
            if ( !this.isPrintValue ( bean, bean2, key, desc ) ) {
                continue;
            }
            this.printValue ( writer, indent, key, value );
        }
        if ( protocol != null && !"HTTP/1.1".equals ( protocol ) ) {
            super.printValue ( writer, indent, "protocol", protocol );
        }
        final String executorName = connector.getExecutorName();
        if ( !"Internal".equals ( executorName ) ) {
            super.printValue ( writer, indent, "executor", executorName );
        }
    }
    protected List<String> getPropertyKeys ( final Connector bean ) throws IntrospectionException {
        final ArrayList<String> propertyKeys = new ArrayList<String>();
        final ProtocolHandler protocolHandler = bean.getProtocolHandler();
        PropertyDescriptor[] descriptors = Introspector.getBeanInfo ( bean.getClass() ).getPropertyDescriptors();
        if ( descriptors == null ) {
            descriptors = new PropertyDescriptor[0];
        }
        for ( final PropertyDescriptor descriptor : descriptors ) {
            if ( ! ( descriptor instanceof IndexedPropertyDescriptor ) ) {
                if ( this.isPersistable ( descriptor.getPropertyType() ) && descriptor.getReadMethod() != null ) {
                    if ( descriptor.getWriteMethod() != null ) {
                        if ( !"protocol".equals ( descriptor.getName() ) ) {
                            if ( !"protocolHandlerClassName".equals ( descriptor.getName() ) ) {
                                propertyKeys.add ( descriptor.getName() );
                            }
                        }
                    }
                }
            }
        }
        descriptors = Introspector.getBeanInfo ( protocolHandler.getClass() ).getPropertyDescriptors();
        if ( descriptors == null ) {
            descriptors = new PropertyDescriptor[0];
        }
        for ( final PropertyDescriptor descriptor : descriptors ) {
            if ( ! ( descriptor instanceof IndexedPropertyDescriptor ) ) {
                if ( this.isPersistable ( descriptor.getPropertyType() ) && descriptor.getReadMethod() != null ) {
                    if ( descriptor.getWriteMethod() != null ) {
                        String key = descriptor.getName();
                        if ( "Internal".equals ( bean.getExecutorName() ) || !ConnectorStoreAppender.internalExecutorAttributes.contains ( key ) ) {
                            if ( ConnectorStoreAppender.replacements.get ( key ) != null ) {
                                key = ConnectorStoreAppender.replacements.get ( key );
                            }
                            if ( !propertyKeys.contains ( key ) ) {
                                propertyKeys.add ( key );
                            }
                        }
                    }
                }
            }
        }
        final String socketName = "socket.";
        descriptors = Introspector.getBeanInfo ( SocketProperties.class ).getPropertyDescriptors();
        if ( descriptors == null ) {
            descriptors = new PropertyDescriptor[0];
        }
        for ( final PropertyDescriptor descriptor2 : descriptors ) {
            if ( ! ( descriptor2 instanceof IndexedPropertyDescriptor ) ) {
                if ( this.isPersistable ( descriptor2.getPropertyType() ) && descriptor2.getReadMethod() != null ) {
                    if ( descriptor2.getWriteMethod() != null ) {
                        String key2 = descriptor2.getName();
                        if ( ConnectorStoreAppender.replacements.get ( key2 ) != null ) {
                            key2 = ConnectorStoreAppender.replacements.get ( key2 );
                        }
                        if ( !propertyKeys.contains ( key2 ) ) {
                            propertyKeys.add ( "socket." + descriptor2.getName() );
                        }
                    }
                }
            }
        }
        return propertyKeys;
    }
    protected void storeConnectorAttribtues ( final PrintWriter aWriter, final int indent, final Object bean, final StoreDescription aDesc ) throws Exception {
        if ( aDesc.isAttributes() ) {
            this.printAttributes ( aWriter, indent, false, bean, aDesc );
        }
    }
    @Override
    public void printOpenTag ( final PrintWriter aWriter, final int indent, final Object bean, final StoreDescription aDesc ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( aDesc.getTag() );
        this.storeConnectorAttribtues ( aWriter, indent, bean, aDesc );
        aWriter.println ( ">" );
    }
    @Override
    public void printTag ( final PrintWriter aWriter, final int indent, final Object bean, final StoreDescription aDesc ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( aDesc.getTag() );
        this.storeConnectorAttribtues ( aWriter, indent, bean, aDesc );
        aWriter.println ( "/>" );
    }
    @Override
    public void printValue ( final PrintWriter writer, final int indent, final String name, final Object value ) {
        String repl = name;
        if ( ConnectorStoreAppender.replacements.get ( name ) != null ) {
            repl = ConnectorStoreAppender.replacements.get ( name );
        }
        super.printValue ( writer, indent, repl, value );
    }
    @Override
    public boolean isPrintValue ( final Object bean, final Object bean2, final String attrName, final StoreDescription desc ) {
        boolean isPrint = super.isPrintValue ( bean, bean2, attrName, desc );
        if ( isPrint && "jkHome".equals ( attrName ) ) {
            final Connector connector = ( Connector ) bean;
            final File catalinaBase = this.getCatalinaBase();
            final File jkHomeBase = this.getJkHomeBase ( ( String ) connector.getProperty ( "jkHome" ), catalinaBase );
            isPrint = !catalinaBase.equals ( jkHomeBase );
        }
        return isPrint;
    }
    protected File getCatalinaBase() {
        File file = new File ( System.getProperty ( "catalina.base" ) );
        try {
            file = file.getCanonicalFile();
        } catch ( IOException ex ) {}
        return file;
    }
    protected File getJkHomeBase ( final String jkHome, final File appBase ) {
        File file = new File ( jkHome );
        if ( !file.isAbsolute() ) {
            file = new File ( appBase, jkHome );
        }
        File jkHomeBase;
        try {
            jkHomeBase = file.getCanonicalFile();
        } catch ( IOException e ) {
            jkHomeBase = file;
        }
        return jkHomeBase;
    }
    static {
        replacements = new HashMap<String, String>();
        internalExecutorAttributes = new HashSet<String>();
        ConnectorStoreAppender.replacements.put ( "timeout", "connectionUploadTimeout" );
        ConnectorStoreAppender.replacements.put ( "clientauth", "clientAuth" );
        ConnectorStoreAppender.replacements.put ( "keystore", "keystoreFile" );
        ConnectorStoreAppender.replacements.put ( "randomfile", "randomFile" );
        ConnectorStoreAppender.replacements.put ( "keypass", "keystorePass" );
        ConnectorStoreAppender.replacements.put ( "keytype", "keystoreType" );
        ConnectorStoreAppender.replacements.put ( "protocol", "sslProtocol" );
        ConnectorStoreAppender.replacements.put ( "protocols", "sslProtocols" );
        ConnectorStoreAppender.internalExecutorAttributes.add ( "maxThreads" );
        ConnectorStoreAppender.internalExecutorAttributes.add ( "minSpareThreads" );
        ConnectorStoreAppender.internalExecutorAttributes.add ( "threadPriority" );
    }
}
