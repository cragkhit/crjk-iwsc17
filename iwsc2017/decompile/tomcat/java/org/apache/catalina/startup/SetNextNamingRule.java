package org.apache.catalina.startup;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.Context;
import org.apache.tomcat.util.digester.Rule;
public class SetNextNamingRule extends Rule {
    protected final String methodName;
    protected final String paramType;
    public SetNextNamingRule ( final String methodName, final String paramType ) {
        this.methodName = methodName;
        this.paramType = paramType;
    }
    @Override
    public void end ( final String namespace, final String name ) throws Exception {
        final Object child = this.digester.peek ( 0 );
        final Object parent = this.digester.peek ( 1 );
        NamingResourcesImpl namingResources = null;
        if ( parent instanceof Context ) {
            namingResources = ( ( Context ) parent ).getNamingResources();
        } else {
            namingResources = ( NamingResourcesImpl ) parent;
        }
        IntrospectionUtils.callMethod1 ( namingResources, this.methodName, child, this.paramType, this.digester.getClassLoader() );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "SetNextRule[" );
        sb.append ( "methodName=" );
        sb.append ( this.methodName );
        sb.append ( ", paramType=" );
        sb.append ( this.paramType );
        sb.append ( "]" );
        return sb.toString();
    }
}
