package org.apache.tomcat.util.digester;
import org.xml.sax.Attributes;
public class CallParamRule extends Rule {
    protected final String attributeName;
    protected final int paramIndex;
    protected final boolean fromStack;
    protected final int stackIndex;
    protected ArrayStack<String> bodyTextStack;
    public CallParamRule ( final int paramIndex ) {
        this ( paramIndex, null );
    }
    public CallParamRule ( final int paramIndex, final String attributeName ) {
        this ( attributeName, paramIndex, 0, false );
    }
    private CallParamRule ( final String attributeName, final int paramIndex, final int stackIndex, final boolean fromStack ) {
        this.attributeName = attributeName;
        this.paramIndex = paramIndex;
        this.stackIndex = stackIndex;
        this.fromStack = fromStack;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        Object param = null;
        if ( this.attributeName != null ) {
            param = attributes.getValue ( this.attributeName );
        } else if ( this.fromStack ) {
            param = this.digester.peek ( this.stackIndex );
            if ( this.digester.log.isDebugEnabled() ) {
                final StringBuilder sb = new StringBuilder ( "[CallParamRule]{" );
                sb.append ( this.digester.match );
                sb.append ( "} Save from stack; from stack?" ).append ( this.fromStack );
                sb.append ( "; object=" ).append ( param );
                this.digester.log.debug ( sb.toString() );
            }
        }
        if ( param != null ) {
            final Object[] parameters = ( Object[] ) this.digester.peekParams();
            parameters[this.paramIndex] = param;
        }
    }
    @Override
    public void body ( final String namespace, final String name, final String bodyText ) throws Exception {
        if ( this.attributeName == null && !this.fromStack ) {
            if ( this.bodyTextStack == null ) {
                this.bodyTextStack = new ArrayStack<String>();
            }
            this.bodyTextStack.push ( bodyText.trim() );
        }
    }
    @Override
    public void end ( final String namespace, final String name ) {
        if ( this.bodyTextStack != null && !this.bodyTextStack.empty() ) {
            final Object[] parameters = ( Object[] ) this.digester.peekParams();
            parameters[this.paramIndex] = this.bodyTextStack.pop();
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "CallParamRule[" );
        sb.append ( "paramIndex=" );
        sb.append ( this.paramIndex );
        sb.append ( ", attributeName=" );
        sb.append ( this.attributeName );
        sb.append ( ", from stack=" );
        sb.append ( this.fromStack );
        sb.append ( "]" );
        return sb.toString();
    }
}
