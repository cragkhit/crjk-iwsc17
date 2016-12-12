package org.apache.catalina.connector;
import org.apache.catalina.TomcatPrincipal;
static final class Request$4 implements SpecialAttributeAdapter {
    @Override
    public Object get ( final Request request, final String name ) {
        if ( request.userPrincipal instanceof TomcatPrincipal ) {
            return ( ( TomcatPrincipal ) request.userPrincipal ).getGssCredential();
        }
        return null;
    }
    @Override
    public void set ( final Request request, final String name, final Object value ) {
    }
}
