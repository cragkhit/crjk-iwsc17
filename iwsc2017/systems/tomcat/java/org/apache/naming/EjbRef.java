package org.apache.naming;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
public class EjbRef extends Reference {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_FACTORY =
        org.apache.naming.factory.Constants.DEFAULT_EJB_FACTORY;
    public static final String TYPE = "type";
    public static final String REMOTE = "remote";
    public static final String LINK = "link";
    public EjbRef ( String ejbType, String home, String remote, String link ) {
        this ( ejbType, home, remote, link, null, null );
    }
    public EjbRef ( String ejbType, String home, String remote, String link,
                    String factory, String factoryLocation ) {
        super ( home, factory, factoryLocation );
        StringRefAddr refAddr = null;
        if ( ejbType != null ) {
            refAddr = new StringRefAddr ( TYPE, ejbType );
            add ( refAddr );
        }
        if ( remote != null ) {
            refAddr = new StringRefAddr ( REMOTE, remote );
            add ( refAddr );
        }
        if ( link != null ) {
            refAddr = new StringRefAddr ( LINK, link );
            add ( refAddr );
        }
    }
    @Override
    public String getFactoryClassName() {
        String factory = super.getFactoryClassName();
        if ( factory != null ) {
            return factory;
        } else {
            factory = System.getProperty ( Context.OBJECT_FACTORIES );
            if ( factory != null ) {
                return null;
            } else {
                return DEFAULT_FACTORY;
            }
        }
    }
}
