package org.apache.catalina.storeconfig;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.net.SocketProperties;
public class ConnectorStoreAppender extends StoreAppender {
    protected static final HashMap<String, String> replacements = new HashMap<>();
    protected static final Set<String> internalExecutorAttributes = new HashSet<>();
    static {
        replacements.put ( "timeout", "connectionUploadTimeout" );
        replacements.put ( "clientauth", "clientAuth" );
        replacements.put ( "keystore", "keystoreFile" );
        replacements.put ( "randomfile", "randomFile" );
        replacements.put ( "keypass", "keystorePass" );
        replacements.put ( "keytype", "keystoreType" );
        replacements.put ( "protocol", "sslProtocol" );
        replacements.put ( "protocols", "sslProtocols" );
        internalExecutorAttributes.add ( "maxThreads" );
        internalExecutorAttributes.add ( "minSpareThreads" );
        internalExecutorAttributes.add ( "threadPriority" );
    }
    @Override
    public void printAttributes ( PrintWriter writer, int indent,
                                  boolean include, Object bean, StoreDescription desc )
    throws Exception {
        if ( include && desc != null && !desc.isStandard() ) {
            writer.print ( " className=\"" );
            writer.print ( bean.getClass().getName() );
            writer.print ( "\"" );
        }
        Connector connector = ( Connector ) bean;
        String protocol = connector.getProtocol();
        List<String> propertyKeys = getPropertyKeys ( connector );
        Object bean2 = new Connector ( protocol );
        Iterator<String> propertyIterator = propertyKeys.iterator();
        while ( propertyIterator.hasNext() ) {
            String key = propertyIterator.next();
            Object value = IntrospectionUtils.getProperty ( bean, key );
            if ( desc.isTransientAttribute ( key ) ) {
                continue;
            }
            if ( value == null ) {
                continue;
            }
            if ( !isPersistable ( value.getClass() ) ) {
                continue;
            }
            Object value2 = IntrospectionUtils.getProperty ( bean2, key );
            if ( value.equals ( value2 ) ) {
                continue;
            }
            if ( isPrintValue ( bean, bean2, key, desc ) ) {
                printValue ( writer, indent, key, value );
            }
        }
        if ( protocol != null && !"HTTP/1.1".equals ( protocol ) ) {
            super.printValue ( writer, indent, "protocol", protocol );
        }
        String executorName = connector.getExecutorName();
        if ( !Connector.INTERNAL_EXECUTOR_NAME.equals ( executorName ) ) {
            super.printValue ( writer, indent, "executor", executorName );
        }
    }
    protected List<String> getPropertyKeys ( Connector bean )
    throws IntrospectionException {
        ArrayList<String> propertyKeys = new ArrayList<>();
        ProtocolHandler protocolHandler = bean.getProtocolHandler();
        PropertyDescriptor descriptors[] = Introspector.getBeanInfo (
                                               bean.getClass() ).getPropertyDescriptors();
        if ( descriptors == null ) {
            descriptors = new PropertyDescriptor[0];
        }
        for ( PropertyDescriptor descriptor : descriptors ) {
            if ( descriptor instanceof IndexedPropertyDescriptor ) {
                continue;
            }
            if ( !isPersistable ( descriptor.getPropertyType() )
                    || ( descriptor.getReadMethod() == null )
                    || ( descriptor.getWriteMethod() == null ) ) {
                continue;
            }
            if ( "protocol".equals ( descriptor.getName() )
                    || "protocolHandlerClassName".equals ( descriptor
                            .getName() ) ) {
                continue;
            }
            propertyKeys.add ( descriptor.getName() );
        }
        descriptors = Introspector.getBeanInfo (
                          protocolHandler.getClass() ).getPropertyDescriptors();
        if ( descriptors == null ) {
            descriptors = new PropertyDescriptor[0];
        }
        for ( PropertyDescriptor descriptor : descriptors ) {
            if ( descriptor instanceof IndexedPropertyDescriptor ) {
                continue;
            }
            if ( !isPersistable ( descriptor.getPropertyType() )
                    || ( descriptor.getReadMethod() == null )
                    || ( descriptor.getWriteMethod() == null ) ) {
                continue;
            }
            String key = descriptor.getName();
            if ( !Connector.INTERNAL_EXECUTOR_NAME.equals ( bean.getExecutorName() ) &&
                    internalExecutorAttributes.contains ( key ) ) {
                continue;
            }
            if ( replacements.get ( key ) != null ) {
                key = replacements.get ( key );
            }
            if ( !propertyKeys.contains ( key ) ) {
                propertyKeys.add ( key );
            }
        }
        final String socketName = "socket.";
        descriptors = Introspector.getBeanInfo (
                          SocketProperties.class ).getPropertyDescriptors();
        if ( descriptors == null ) {
            descriptors = new PropertyDescriptor[0];
        }
        for ( PropertyDescriptor descriptor : descriptors ) {
            if ( descriptor instanceof IndexedPropertyDescriptor ) {
                continue;
            }
            if ( !isPersistable ( descriptor.getPropertyType() )
                    || ( descriptor.getReadMethod() == null )
                    || ( descriptor.getWriteMethod() == null ) ) {
                continue;
            }
            String key = descriptor.getName();
            if ( replacements.get ( key ) != null ) {
                key = replacements.get ( key );
            }
            if ( !propertyKeys.contains ( key ) ) {
                propertyKeys.add ( socketName + descriptor.getName() );
            }
        }
        return propertyKeys;
    }
    protected void storeConnectorAttribtues ( PrintWriter aWriter, int indent,
            Object bean, StoreDescription aDesc ) throws Exception {
        if ( aDesc.isAttributes() ) {
            printAttributes ( aWriter, indent, false, bean, aDesc );
        }
    }
    @Override
    public void printOpenTag ( PrintWriter aWriter, int indent, Object bean,
                               StoreDescription aDesc ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( aDesc.getTag() );
        storeConnectorAttribtues ( aWriter, indent, bean, aDesc );
        aWriter.println ( ">" );
    }
    @Override
    public void printTag ( PrintWriter aWriter, int indent, Object bean,
                           StoreDescription aDesc ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( aDesc.getTag() );
        storeConnectorAttribtues ( aWriter, indent, bean, aDesc );
        aWriter.println ( "/>" );
    }
    @Override
    public void printValue ( PrintWriter writer, int indent, String name,
                             Object value ) {
        String repl = name;
        if ( replacements.get ( name ) != null ) {
            repl = replacements.get ( name );
        }
        super.printValue ( writer, indent, repl, value );
    }
    @Override
    public boolean isPrintValue ( Object bean, Object bean2, String attrName,
                                  StoreDescription desc ) {
        boolean isPrint = super.isPrintValue ( bean, bean2, attrName, desc );
        if ( isPrint ) {
            if ( "jkHome".equals ( attrName ) ) {
                Connector connector = ( ( Connector ) bean );
                File catalinaBase = getCatalinaBase();
                File jkHomeBase = getJkHomeBase ( ( String ) connector
                                                  .getProperty ( "jkHome" ), catalinaBase );
                isPrint = !catalinaBase.equals ( jkHomeBase );
            }
        }
        return isPrint;
    }
    protected File getCatalinaBase() {
        File file = new File ( System.getProperty ( "catalina.base" ) );
        try {
            file = file.getCanonicalFile();
        } catch ( IOException e ) {
        }
        return ( file );
    }
    protected File getJkHomeBase ( String jkHome, File appBase ) {
        File jkHomeBase;
        File file = new File ( jkHome );
        if ( !file.isAbsolute() ) {
            file = new File ( appBase, jkHome );
        }
        try {
            jkHomeBase = file.getCanonicalFile();
        } catch ( IOException e ) {
            jkHomeBase = file;
        }
        return ( jkHomeBase );
    }
}
