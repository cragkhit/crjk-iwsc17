package org.apache.tomcat.util.descriptor.web;
import java.util.ArrayList;
import org.apache.tomcat.util.digester.CallParamRule;
final class CallParamMultiRule extends CallParamRule {
    public CallParamMultiRule ( final int paramIndex ) {
        super ( paramIndex );
    }
    @Override
    public void end ( final String namespace, final String name ) {
        if ( this.bodyTextStack != null && !this.bodyTextStack.empty() ) {
            final Object[] parameters = ( Object[] ) this.digester.peekParams();
            ArrayList<String> params = ( ArrayList<String> ) parameters[this.paramIndex];
            if ( params == null ) {
                params = new ArrayList<String>();
                parameters[this.paramIndex] = params;
            }
            params.add ( this.bodyTextStack.pop() );
        }
    }
}
