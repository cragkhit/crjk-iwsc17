package org.apache.tomcat.util.descriptor.web;
import org.apache.tomcat.util.digester.CallMethodRule;
final class LifecycleCallbackRule extends CallMethodRule {
    private final boolean postConstruct;
    public LifecycleCallbackRule ( final String methodName, final int paramCount, final boolean postConstruct ) {
        super ( methodName, paramCount );
        this.postConstruct = postConstruct;
    }
    @Override
    public void end ( final String namespace, final String name ) throws Exception {
        final Object[] params = ( Object[] ) this.digester.peekParams();
        if ( params != null && params.length == 2 ) {
            final WebXml webXml = ( WebXml ) this.digester.peek();
            if ( this.postConstruct ) {
                if ( webXml.getPostConstructMethods().containsKey ( params[0] ) ) {
                    throw new IllegalArgumentException ( WebRuleSet.sm.getString ( "webRuleSet.postconstruct.duplicate", params[0] ) );
                }
            } else if ( webXml.getPreDestroyMethods().containsKey ( params[0] ) ) {
                throw new IllegalArgumentException ( WebRuleSet.sm.getString ( "webRuleSet.predestroy.duplicate", params[0] ) );
            }
        }
        super.end ( namespace, name );
    }
}
