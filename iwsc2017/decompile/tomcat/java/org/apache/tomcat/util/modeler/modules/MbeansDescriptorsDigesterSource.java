package org.apache.tomcat.util.modeler.modules;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import org.apache.tomcat.util.modeler.ManagedBean;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import org.apache.tomcat.util.digester.Digester;
import javax.management.ObjectName;
import java.util.List;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.juli.logging.Log;
public class MbeansDescriptorsDigesterSource extends ModelerSource {
    private static final Log log;
    private static final Object dLock;
    private Registry registry;
    private final List<ObjectName> mbeans;
    private static Digester digester;
    public MbeansDescriptorsDigesterSource() {
        this.mbeans = new ArrayList<ObjectName>();
    }
    private static Digester createDigester() {
        final Digester digester = new Digester();
        digester.setNamespaceAware ( false );
        digester.setValidating ( false );
        final URL url = Registry.getRegistry ( null, null ).getClass().getResource ( "/org/apache/tomcat/util/modeler/mbeans-descriptors.dtd" );
        digester.register ( "-//Apache Software Foundation//DTD Model MBeans Configuration File", url.toString() );
        digester.addObjectCreate ( "mbeans-descriptors/mbean", "org.apache.tomcat.util.modeler.ManagedBean" );
        digester.addSetProperties ( "mbeans-descriptors/mbean" );
        digester.addSetNext ( "mbeans-descriptors/mbean", "add", "java.lang.Object" );
        digester.addObjectCreate ( "mbeans-descriptors/mbean/attribute", "org.apache.tomcat.util.modeler.AttributeInfo" );
        digester.addSetProperties ( "mbeans-descriptors/mbean/attribute" );
        digester.addSetNext ( "mbeans-descriptors/mbean/attribute", "addAttribute", "org.apache.tomcat.util.modeler.AttributeInfo" );
        digester.addObjectCreate ( "mbeans-descriptors/mbean/notification", "org.apache.tomcat.util.modeler.NotificationInfo" );
        digester.addSetProperties ( "mbeans-descriptors/mbean/notification" );
        digester.addSetNext ( "mbeans-descriptors/mbean/notification", "addNotification", "org.apache.tomcat.util.modeler.NotificationInfo" );
        digester.addObjectCreate ( "mbeans-descriptors/mbean/notification/descriptor/field", "org.apache.tomcat.util.modeler.FieldInfo" );
        digester.addSetProperties ( "mbeans-descriptors/mbean/notification/descriptor/field" );
        digester.addSetNext ( "mbeans-descriptors/mbean/notification/descriptor/field", "addField", "org.apache.tomcat.util.modeler.FieldInfo" );
        digester.addCallMethod ( "mbeans-descriptors/mbean/notification/notification-type", "addNotifType", 0 );
        digester.addObjectCreate ( "mbeans-descriptors/mbean/operation", "org.apache.tomcat.util.modeler.OperationInfo" );
        digester.addSetProperties ( "mbeans-descriptors/mbean/operation" );
        digester.addSetNext ( "mbeans-descriptors/mbean/operation", "addOperation", "org.apache.tomcat.util.modeler.OperationInfo" );
        digester.addObjectCreate ( "mbeans-descriptors/mbean/operation/descriptor/field", "org.apache.tomcat.util.modeler.FieldInfo" );
        digester.addSetProperties ( "mbeans-descriptors/mbean/operation/descriptor/field" );
        digester.addSetNext ( "mbeans-descriptors/mbean/operation/descriptor/field", "addField", "org.apache.tomcat.util.modeler.FieldInfo" );
        digester.addObjectCreate ( "mbeans-descriptors/mbean/operation/parameter", "org.apache.tomcat.util.modeler.ParameterInfo" );
        digester.addSetProperties ( "mbeans-descriptors/mbean/operation/parameter" );
        digester.addSetNext ( "mbeans-descriptors/mbean/operation/parameter", "addParameter", "org.apache.tomcat.util.modeler.ParameterInfo" );
        return digester;
    }
    public void setRegistry ( final Registry reg ) {
        this.registry = reg;
    }
    public void setSource ( final Object source ) {
        this.source = source;
    }
    @Override
    public List<ObjectName> loadDescriptors ( final Registry registry, final String type, final Object source ) throws Exception {
        this.setRegistry ( registry );
        this.setSource ( source );
        this.execute();
        return this.mbeans;
    }
    public void execute() throws Exception {
        if ( this.registry == null ) {
            this.registry = Registry.getRegistry ( null, null );
        }
        final InputStream stream = ( InputStream ) this.source;
        final ArrayList<ManagedBean> loadedMbeans = new ArrayList<ManagedBean>();
        synchronized ( MbeansDescriptorsDigesterSource.dLock ) {
            if ( MbeansDescriptorsDigesterSource.digester == null ) {
                MbeansDescriptorsDigesterSource.digester = createDigester();
            }
            try {
                MbeansDescriptorsDigesterSource.digester.push ( loadedMbeans );
                MbeansDescriptorsDigesterSource.digester.parse ( stream );
            } catch ( Exception e ) {
                MbeansDescriptorsDigesterSource.log.error ( "Error digesting Registry data", e );
                throw e;
            } finally {
                MbeansDescriptorsDigesterSource.digester.reset();
            }
        }
        final Iterator<ManagedBean> iter = loadedMbeans.iterator();
        while ( iter.hasNext() ) {
            this.registry.addManagedBean ( iter.next() );
        }
    }
    static {
        log = LogFactory.getLog ( MbeansDescriptorsDigesterSource.class );
        dLock = new Object();
        MbeansDescriptorsDigesterSource.digester = null;
    }
}
