package org.apache.tomcat.util.digester;
import org.xml.sax.Attributes;
public class CallParamRule extends Rule {
    public CallParamRule ( int paramIndex ) {
        this ( paramIndex, null );
    }
    public CallParamRule ( int paramIndex,
                           String attributeName ) {
        this ( attributeName, paramIndex, 0, false );
    }
    private CallParamRule ( String attributeName, int paramIndex, int stackIndex,
                            boolean fromStack ) {
        this.attributeName = attributeName;
        this.paramIndex = paramIndex;
        this.stackIndex = stackIndex;
        this.fromStack = fromStack;
    }
    protected final String attributeName;
    protected final int paramIndex;
    protected final boolean fromStack;
    protected final int stackIndex;
    protected ArrayStack<String> bodyTextStack;
    @Override
    public void begin ( String namespace, String name, Attributes attributes )
    throws Exception {
        Object param = null;
        if ( attributeName != null ) {
            param = attributes.getValue ( attributeName );
        } else if ( fromStack ) {
            param = digester.peek ( stackIndex );
            if ( digester.log.isDebugEnabled() ) {
                StringBuilder sb = new StringBuilder ( "[CallParamRule]{" );
                sb.append ( digester.match );
                sb.append ( "} Save from stack; from stack?" ).append ( fromStack );
                sb.append ( "; object=" ).append ( param );
                digester.log.debug ( sb.toString() );
            }
        }
        if ( param != null ) {
            Object parameters[] = ( Object[] ) digester.peekParams();
            parameters[paramIndex] = param;
        }
    }
    @Override
    public void body ( String namespace, String name, String bodyText )
    throws Exception {
        if ( attributeName == null && !fromStack ) {
            if ( bodyTextStack == null ) {
                bodyTextStack = new ArrayStack<>();
            }
            bodyTextStack.push ( bodyText.trim() );
        }
    }
    @Override
    public void end ( String namespace, String name ) {
        if ( bodyTextStack != null && !bodyTextStack.empty() ) {
            Object parameters[] = ( Object[] ) digester.peekParams();
            parameters[paramIndex] = bodyTextStack.pop();
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "CallParamRule[" );
        sb.append ( "paramIndex=" );
        sb.append ( paramIndex );
        sb.append ( ", attributeName=" );
        sb.append ( attributeName );
        sb.append ( ", from stack=" );
        sb.append ( fromStack );
        sb.append ( "]" );
        return ( sb.toString() );
    }
}
