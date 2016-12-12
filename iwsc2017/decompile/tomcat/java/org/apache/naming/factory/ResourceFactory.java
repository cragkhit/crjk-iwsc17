package org.apache.naming.factory;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;
import javax.naming.Reference;
import org.apache.naming.ResourceRef;
public class ResourceFactory extends FactoryBase {
    @Override
    protected boolean isReferenceTypeSupported ( final Object obj ) {
        return obj instanceof ResourceRef;
    }
    @Override
    protected ObjectFactory getDefaultFactory ( final Reference ref ) throws NamingException {
        ObjectFactory factory = null;
        if ( ref.getClassName().equals ( "javax.sql.DataSource" ) ) {
            final String javaxSqlDataSourceFactoryClassName = System.getProperty ( "javax.sql.DataSource.Factory", "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory" );
            try {
                factory = ( ObjectFactory ) Class.forName ( javaxSqlDataSourceFactoryClassName ).newInstance();
            } catch ( Exception e ) {
                final NamingException ex = new NamingException ( "Could not create resource factory instance" );
                ex.initCause ( e );
                throw ex;
            }
        } else if ( ref.getClassName().equals ( "javax.mail.Session" ) ) {
            final String javaxMailSessionFactoryClassName = System.getProperty ( "javax.mail.Session.Factory", "org.apache.naming.factory.MailSessionFactory" );
            try {
                factory = ( ObjectFactory ) Class.forName ( javaxMailSessionFactoryClassName ).newInstance();
            } catch ( Throwable t ) {
                if ( t instanceof NamingException ) {
                    throw ( NamingException ) t;
                }
                if ( t instanceof ThreadDeath ) {
                    throw ( ThreadDeath ) t;
                }
                if ( t instanceof VirtualMachineError ) {
                    throw ( VirtualMachineError ) t;
                }
                final NamingException ex = new NamingException ( "Could not create resource factory instance" );
                ex.initCause ( t );
                throw ex;
            }
        }
        return factory;
    }
    @Override
    protected Object getLinked ( final Reference ref ) {
        return null;
    }
}
