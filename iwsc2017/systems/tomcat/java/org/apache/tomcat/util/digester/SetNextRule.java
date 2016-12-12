package org.apache.tomcat.util.digester;
import org.apache.tomcat.util.IntrospectionUtils;
public class SetNextRule extends Rule {
    public SetNextRule ( String methodName,
                         String paramType ) {
        this.methodName = methodName;
        this.paramType = paramType;
    }
    protected String methodName = null;
    protected String paramType = null;
    protected boolean useExactMatch = false;
    public boolean isExactMatch() {
        return useExactMatch;
    }
    public void setExactMatch ( boolean useExactMatch ) {
        this.useExactMatch = useExactMatch;
    }
    @Override
    public void end ( String namespace, String name ) throws Exception {
        Object child = digester.peek ( 0 );
        Object parent = digester.peek ( 1 );
        if ( digester.log.isDebugEnabled() ) {
            if ( parent == null ) {
                digester.log.debug ( "[SetNextRule]{" + digester.match +
                                     "} Call [NULL PARENT]." +
                                     methodName + "(" + child + ")" );
            } else {
                digester.log.debug ( "[SetNextRule]{" + digester.match +
                                     "} Call " + parent.getClass().getName() + "." +
                                     methodName + "(" + child + ")" );
            }
        }
        IntrospectionUtils.callMethod1 ( parent, methodName,
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
