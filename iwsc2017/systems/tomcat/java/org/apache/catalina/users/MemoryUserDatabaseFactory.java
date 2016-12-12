package org.apache.catalina.users;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
public class MemoryUserDatabaseFactory implements ObjectFactory {
    @Override
    public Object getObjectInstance ( Object obj, Name name, Context nameCtx,
                                      Hashtable<?, ?> environment )
    throws Exception {
        if ( ( obj == null ) || ! ( obj instanceof Reference ) ) {
            return ( null );
        }
        Reference ref = ( Reference ) obj;
        if ( !"org.apache.catalina.UserDatabase".equals ( ref.getClassName() ) ) {
            return ( null );
        }
        MemoryUserDatabase database = new MemoryUserDatabase ( name.toString() );
        RefAddr ra = null;
        ra = ref.get ( "pathname" );
        if ( ra != null ) {
            database.setPathname ( ra.getContent().toString() );
        }
        ra = ref.get ( "readonly" );
        if ( ra != null ) {
            database.setReadonly ( Boolean.parseBoolean ( ra.getContent().toString() ) );
        }
        database.open();
        if ( !database.getReadonly() ) {
            database.save();
        }
        return ( database );
    }
}
