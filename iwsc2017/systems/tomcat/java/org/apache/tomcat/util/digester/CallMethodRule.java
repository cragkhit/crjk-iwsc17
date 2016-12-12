package org.apache.tomcat.util.digester;
import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.Attributes;
public class CallMethodRule extends Rule {
    public CallMethodRule ( String methodName,
                            int paramCount ) {
        this ( 0, methodName, paramCount );
    }
    public CallMethodRule ( int targetOffset,
                            String methodName,
                            int paramCount ) {
        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;
        if ( paramCount == 0 ) {
            this.paramTypes = new Class[] { String.class };
        } else {
            this.paramTypes = new Class[paramCount];
            for ( int i = 0; i < this.paramTypes.length; i++ ) {
                this.paramTypes[i] = String.class;
            }
        }
        this.paramClassNames = null;
    }
    public CallMethodRule ( String methodName ) {
        this ( 0, methodName, 0, ( Class[] ) null );
    }
    public CallMethodRule ( int targetOffset,
                            String methodName,
                            int paramCount,
                            Class<?> paramTypes[] ) {
        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;
        if ( paramTypes == null ) {
            this.paramTypes = new Class[paramCount];
            for ( int i = 0; i < this.paramTypes.length; i++ ) {
                this.paramTypes[i] = String.class;
            }
        } else {
            this.paramTypes = new Class[paramTypes.length];
            for ( int i = 0; i < this.paramTypes.length; i++ ) {
                this.paramTypes[i] = paramTypes[i];
            }
        }
        this.paramClassNames = null;
    }
    protected String bodyText = null;
    protected final int targetOffset;
    protected final String methodName;
    protected final int paramCount;
    protected Class<?> paramTypes[] = null;
    protected final String paramClassNames[];
    protected boolean useExactMatch = false;
    public boolean getUseExactMatch() {
        return useExactMatch;
    }
    public void setUseExactMatch ( boolean useExactMatch ) {
        this.useExactMatch = useExactMatch;
    }
    @Override
    public void setDigester ( Digester digester ) {
        super.setDigester ( digester );
        if ( this.paramClassNames != null ) {
            this.paramTypes = new Class[paramClassNames.length];
            for ( int i = 0; i < this.paramClassNames.length; i++ ) {
                try {
                    this.paramTypes[i] =
                        digester.getClassLoader().loadClass ( this.paramClassNames[i] );
                } catch ( ClassNotFoundException e ) {
                    digester.getLogger().error ( "(CallMethodRule) Cannot load class " + this.paramClassNames[i], e );
                    this.paramTypes[i] = null;
                }
            }
        }
    }
    @Override
    public void begin ( String namespace, String name, Attributes attributes )
    throws Exception {
        if ( paramCount > 0 ) {
            Object parameters[] = new Object[paramCount];
            for ( int i = 0; i < parameters.length; i++ ) {
                parameters[i] = null;
            }
            digester.pushParams ( parameters );
        }
    }
    @Override
    public void body ( String namespace, String name, String bodyText )
    throws Exception {
        if ( paramCount == 0 ) {
            this.bodyText = bodyText.trim();
        }
    }
    @SuppressWarnings ( "null" )
    @Override
    public void end ( String namespace, String name ) throws Exception {
        Object parameters[] = null;
        if ( paramCount > 0 ) {
            parameters = ( Object[] ) digester.popParams();
            if ( digester.log.isTraceEnabled() ) {
                for ( int i = 0, size = parameters.length; i < size; i++ ) {
                    digester.log.trace ( "[CallMethodRule](" + i + ")" + parameters[i] ) ;
                }
            }
            if ( paramCount == 1 && parameters[0] == null ) {
                return;
            }
        } else if ( paramTypes != null && paramTypes.length != 0 ) {
            if ( bodyText == null ) {
                return;
            }
            parameters = new Object[1];
            parameters[0] = bodyText;
        }
        Object paramValues[] = new Object[paramTypes.length];
        for ( int i = 0; i < paramTypes.length; i++ ) {
            if (
                parameters[i] == null ||
                ( parameters[i] instanceof String &&
                  !String.class.isAssignableFrom ( paramTypes[i] ) ) ) {
                paramValues[i] =
                    IntrospectionUtils.convert ( ( String ) parameters[i], paramTypes[i] );
            } else {
                paramValues[i] = parameters[i];
            }
        }
        Object target;
        if ( targetOffset >= 0 ) {
            target = digester.peek ( targetOffset );
        } else {
            target = digester.peek ( digester.getCount() + targetOffset );
        }
        if ( target == null ) {
            StringBuilder sb = new StringBuilder();
            sb.append ( "[CallMethodRule]{" );
            sb.append ( digester.match );
            sb.append ( "} Call target is null (" );
            sb.append ( "targetOffset=" );
            sb.append ( targetOffset );
            sb.append ( ",stackdepth=" );
            sb.append ( digester.getCount() );
            sb.append ( ")" );
            throw new org.xml.sax.SAXException ( sb.toString() );
        }
        if ( digester.log.isDebugEnabled() ) {
            StringBuilder sb = new StringBuilder ( "[CallMethodRule]{" );
            sb.append ( digester.match );
            sb.append ( "} Call " );
            sb.append ( target.getClass().getName() );
            sb.append ( "." );
            sb.append ( methodName );
            sb.append ( "(" );
            for ( int i = 0; i < paramValues.length; i++ ) {
                if ( i > 0 ) {
                    sb.append ( "," );
                }
                if ( paramValues[i] == null ) {
                    sb.append ( "null" );
                } else {
                    sb.append ( paramValues[i].toString() );
                }
                sb.append ( "/" );
                if ( paramTypes[i] == null ) {
                    sb.append ( "null" );
                } else {
                    sb.append ( paramTypes[i].getName() );
                }
            }
            sb.append ( ")" );
            digester.log.debug ( sb.toString() );
        }
        Object result = IntrospectionUtils.callMethodN ( target, methodName,
                        paramValues, paramTypes );
        processMethodCallResult ( result );
    }
    @Override
    public void finish() throws Exception {
        bodyText = null;
    }
    protected void processMethodCallResult ( Object result ) {
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "CallMethodRule[" );
        sb.append ( "methodName=" );
        sb.append ( methodName );
        sb.append ( ", paramCount=" );
        sb.append ( paramCount );
        sb.append ( ", paramTypes={" );
        if ( paramTypes != null ) {
            for ( int i = 0; i < paramTypes.length; i++ ) {
                if ( i > 0 ) {
                    sb.append ( ", " );
                }
                sb.append ( paramTypes[i].getName() );
            }
        }
        sb.append ( "}" );
        sb.append ( "]" );
        return ( sb.toString() );
    }
}
