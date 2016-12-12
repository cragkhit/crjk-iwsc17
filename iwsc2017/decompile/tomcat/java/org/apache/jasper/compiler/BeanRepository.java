package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import java.util.HashMap;
public class BeanRepository {
    private final HashMap<String, String> beanTypes;
    private final ClassLoader loader;
    private final ErrorDispatcher errDispatcher;
    public BeanRepository ( final ClassLoader loader, final ErrorDispatcher err ) {
        this.loader = loader;
        this.errDispatcher = err;
        this.beanTypes = new HashMap<String, String>();
    }
    public void addBean ( final Node.UseBean n, final String s, final String type, final String scope ) throws JasperException {
        if ( scope != null && !scope.equals ( "page" ) && !scope.equals ( "request" ) && !scope.equals ( "session" ) && !scope.equals ( "application" ) ) {
            this.errDispatcher.jspError ( n, "jsp.error.usebean.badScope", new String[0] );
        }
        this.beanTypes.put ( s, type );
    }
    public Class<?> getBeanType ( final String bean ) throws JasperException {
        Class<?> clazz = null;
        try {
            clazz = this.loader.loadClass ( this.beanTypes.get ( bean ) );
        } catch ( ClassNotFoundException ex ) {
            throw new JasperException ( ex );
        }
        return clazz;
    }
    public boolean checkVariable ( final String bean ) {
        return this.beanTypes.containsKey ( bean );
    }
}
