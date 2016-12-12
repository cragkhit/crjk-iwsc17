package org.apache.catalina.startup;
import org.apache.catalina.Context;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.digester.Rule;
public class SetNextNamingRule extends Rule {
    public SetNextNamingRule ( String methodName,
                               String paramType ) {
        this.methodName = methodName;
        this.paramType = paramType;
    }
    protected final String methodName;
    protected final String paramType;
    @Override
    public void end ( String namespace, String name ) throws Exception {
        Object child = digester.peek ( 0 );
        Object parent = digester.peek ( 1 );
        NamingResourcesImpl namingResources = null;
        if ( parent instanceof Context ) {
            namingResources = ( ( Context ) parent ).getNamingResources();
        } else {
            namingResources = ( NamingResourcesImpl ) parent;
        }
        IntrospectionUtils.callMethod1 ( namingResources, methodName,
                                         child, paramType, digester.getClassLoader() );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "SetNextRule[" );
        sb.append ( "methodName=" );
        sb.append ( methodName );
        sb.append ( ", paramType=" );
        sb.append ( paramType );
        sb.append ( "]" );
        return ( sb.toString() );
    }
}
