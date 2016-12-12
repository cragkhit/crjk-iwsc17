package org.apache.catalina.ant.jmx;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.catalina.ant.BaseRedirectorHelperTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public class JMXAccessorTask extends BaseRedirectorHelperTask {
    public static final String JMX_SERVICE_PREFIX = "service:jmx:rmi:///jndi/rmi://";
    public static final String JMX_SERVICE_SUFFIX = "/jmxrmi";
    private String name = null;
    private String resultproperty;
    private String url = null;
    private String host = "localhost";
    private String port = "8050";
    private String password = null;
    private String username = null;
    private String ref = "jmx.server";
    private boolean echo = false;
    private boolean separatearrayresults = true;
    private String delimiter;
    private String unlessCondition;
    private String ifCondition;
    private final Properties properties = new Properties();
    public String getName() {
        return ( this.name );
    }
    public void setName ( String objectName ) {
        this.name = objectName;
    }
    public String getResultproperty() {
        return resultproperty;
    }
    public void setResultproperty ( String propertyName ) {
        this.resultproperty = propertyName;
    }
    public String getDelimiter() {
        return delimiter;
    }
    public void setDelimiter ( String separator ) {
        this.delimiter = separator;
    }
    public boolean isEcho() {
        return echo;
    }
    public void setEcho ( boolean echo ) {
        this.echo = echo;
    }
    public boolean isSeparatearrayresults() {
        return separatearrayresults;
    }
    public void setSeparatearrayresults ( boolean separateArrayResults ) {
        this.separatearrayresults = separateArrayResults;
    }
    public String getPassword() {
        return ( this.password );
    }
    public void setPassword ( String password ) {
        this.password = password;
    }
    public String getUsername() {
        return ( this.username );
    }
    public void setUsername ( String username ) {
        this.username = username;
    }
    public String getUrl() {
        return ( this.url );
    }
    public void setUrl ( String url ) {
        this.url = url;
    }
    public String getHost() {
        return ( this.host );
    }
    public void setHost ( String host ) {
        this.host = host;
    }
    public String getPort() {
        return ( this.port );
    }
    public void setPort ( String port ) {
        this.port = port;
    }
    public boolean isUseRef() {
        return ref != null && !"".equals ( ref );
    }
    public String getRef() {
        return ref;
    }
    public void setRef ( String refId ) {
        this.ref = refId;
    }
    public String getIf() {
        return ifCondition;
    }
    public void setIf ( String c ) {
        ifCondition = c;
    }
    public String getUnless() {
        return unlessCondition;
    }
    public void setUnless ( String c ) {
        unlessCondition = c;
    }
    @Override
    public void execute() throws BuildException {
        if ( testIfCondition() && testUnlessCondition() ) {
            try {
                String error = null;
                MBeanServerConnection jmxServerConnection = getJMXConnection();
                error = jmxExecute ( jmxServerConnection );
                if ( error != null && isFailOnError() ) {
                    throw new BuildException ( error );
                }
            } catch ( Exception e ) {
                if ( isFailOnError() ) {
                    throw new BuildException ( e );
                } else {
                    handleErrorOutput ( e.getMessage() );
                }
            } finally {
                closeRedirector();
            }
        }
    }
    public static MBeanServerConnection createJMXConnection ( String url,
            String host, String port, String username, String password )
    throws MalformedURLException, IOException {
        String urlForJMX;
        if ( url != null ) {
            urlForJMX = url;
        } else
            urlForJMX = JMX_SERVICE_PREFIX + host + ":" + port
                        + JMX_SERVICE_SUFFIX;
        Map<String, String[]> environment = null;
        if ( username != null && password != null ) {
            String[] credentials = new String[2];
            credentials[0] = username;
            credentials[1] = password;
            environment = new HashMap<>();
            environment.put ( JMXConnector.CREDENTIALS, credentials );
        }
        return JMXConnectorFactory.connect ( new JMXServiceURL ( urlForJMX ),
                                             environment ).getMBeanServerConnection();
    }
    protected boolean testIfCondition() {
        if ( ifCondition == null || "".equals ( ifCondition ) ) {
            return true;
        }
        return getProperty ( ifCondition ) != null;
    }
    protected boolean testUnlessCondition() {
        if ( unlessCondition == null || "".equals ( unlessCondition ) ) {
            return true;
        }
        return getProperty ( unlessCondition ) == null;
    }
    @SuppressWarnings ( "null" )
    public static MBeanServerConnection accessJMXConnection ( Project project,
            String url, String host, String port, String username,
            String password, String refId ) throws MalformedURLException,
        IOException {
        MBeanServerConnection jmxServerConnection = null;
        boolean isRef = project != null && refId != null && refId.length() > 0;
        if ( isRef ) {
            Object pref = project.getReference ( refId );
            try {
                jmxServerConnection = ( MBeanServerConnection ) pref;
            } catch ( ClassCastException cce ) {
                project.log ( "wrong object reference " + refId + " - "
                              + pref.getClass() );
                return null;
            }
        }
        if ( jmxServerConnection == null ) {
            jmxServerConnection = createJMXConnection ( url, host, port,
                                  username, password );
        }
        if ( isRef && jmxServerConnection != null ) {
            project.addReference ( refId, jmxServerConnection );
        }
        return jmxServerConnection;
    }
    protected MBeanServerConnection getJMXConnection()
    throws MalformedURLException, IOException {
        MBeanServerConnection jmxServerConnection = null;
        if ( isUseRef() ) {
            Object pref = null ;
            if ( getProject() != null ) {
                pref = getProject().getReference ( getRef() );
                if ( pref != null ) {
                    try {
                        jmxServerConnection = ( MBeanServerConnection ) pref;
                    } catch ( ClassCastException cce ) {
                        getProject().log (
                            "Wrong object reference " + getRef() + " - "
                            + pref.getClass() );
                        return null;
                    }
                }
            }
            if ( jmxServerConnection == null ) {
                jmxServerConnection = accessJMXConnection ( getProject(),
                                      getUrl(), getHost(), getPort(), getUsername(),
                                      getPassword(), getRef() );
            }
        } else {
            jmxServerConnection = accessJMXConnection ( getProject(), getUrl(),
                                  getHost(), getPort(), getUsername(), getPassword(), null );
        }
        return jmxServerConnection;
    }
    public String jmxExecute ( MBeanServerConnection jmxServerConnection )
    throws Exception {
        if ( ( jmxServerConnection == null ) ) {
            throw new BuildException ( "Must open a connection!" );
        } else if ( isEcho() ) {
            handleOutput ( "JMX Connection ref=" + ref + " is open!" );
        }
        return null;
    }
    protected Object convertStringToType ( String value, String valueType ) {
        if ( "java.lang.String".equals ( valueType ) ) {
            return value;
        }
        Object convertValue = value;
        if ( "java.lang.Integer".equals ( valueType ) || "int".equals ( valueType ) ) {
            try {
                convertValue = Integer.valueOf ( value );
            } catch ( NumberFormatException ex ) {
                if ( isEcho() ) {
                    handleErrorOutput ( "Unable to convert to integer:" + value );
                }
            }
        } else if ( "java.lang.Long".equals ( valueType )
                    || "long".equals ( valueType ) ) {
            try {
                convertValue = Long.valueOf ( value );
            } catch ( NumberFormatException ex ) {
                if ( isEcho() ) {
                    handleErrorOutput ( "Unable to convert to long:" + value );
                }
            }
        } else if ( "java.lang.Boolean".equals ( valueType )
                    || "boolean".equals ( valueType ) ) {
            convertValue = Boolean.valueOf ( value );
        } else if ( "java.lang.Float".equals ( valueType )
                    || "float".equals ( valueType ) ) {
            try {
                convertValue = Float.valueOf ( value );
            } catch ( NumberFormatException ex ) {
                if ( isEcho() ) {
                    handleErrorOutput ( "Unable to convert to float:" + value );
                }
            }
        } else if ( "java.lang.Double".equals ( valueType )
                    || "double".equals ( valueType ) ) {
            try {
                convertValue = Double.valueOf ( value );
            } catch ( NumberFormatException ex ) {
                if ( isEcho() ) {
                    handleErrorOutput ( "Unable to convert to double:" + value );
                }
            }
        } else if ( "javax.management.ObjectName".equals ( valueType )
                    || "name".equals ( valueType ) ) {
            try {
                convertValue = new ObjectName ( value );
            } catch ( MalformedObjectNameException e ) {
                if ( isEcho() )
                    handleErrorOutput ( "Unable to convert to ObjectName:"
                                        + value );
            }
        } else if ( "java.net.InetAddress".equals ( valueType ) ) {
            try {
                convertValue = InetAddress.getByName ( value );
            } catch ( UnknownHostException exc ) {
                if ( isEcho() ) {
                    handleErrorOutput ( "Unable to resolve host name:" + value );
                }
            }
        }
        return convertValue;
    }
    protected void echoResult ( String name, Object result ) {
        if ( isEcho() ) {
            if ( result.getClass().isArray() ) {
                for ( int i = 0; i < Array.getLength ( result ); i++ ) {
                    handleOutput ( name + "." + i + "=" + Array.get ( result, i ) );
                }
            } else {
                handleOutput ( name + "=" + result );
            }
        }
    }
    protected void createProperty ( Object result ) {
        if ( resultproperty != null ) {
            createProperty ( resultproperty, result );
        }
    }
    protected void createProperty ( String propertyPrefix, Object result ) {
        if ( propertyPrefix == null ) {
            propertyPrefix = "";
        }
        if ( result instanceof CompositeDataSupport ) {
            CompositeDataSupport data = ( CompositeDataSupport ) result;
            CompositeType compositeType = data.getCompositeType();
            Set<String> keys = compositeType.keySet();
            for ( Iterator<String> iter = keys.iterator(); iter.hasNext(); ) {
                String key = iter.next();
                Object value = data.get ( key );
                OpenType<?> type = compositeType.getType ( key );
                if ( type instanceof SimpleType<?> ) {
                    setProperty ( propertyPrefix + "." + key, value );
                } else {
                    createProperty ( propertyPrefix + "." + key, value );
                }
            }
        } else if ( result instanceof TabularDataSupport ) {
            TabularDataSupport data = ( TabularDataSupport ) result;
            for ( Iterator<Object> iter = data.keySet().iterator(); iter.hasNext(); ) {
                Object key = iter.next();
                for ( Iterator<?> iter1 = ( ( List<?> ) key ).iterator(); iter1.hasNext(); ) {
                    Object key1 = iter1.next();
                    CompositeData valuedata = data.get ( new Object[] { key1 } );
                    Object value = valuedata.get ( "value" );
                    OpenType<?> type = valuedata.getCompositeType().getType (
                                           "value" );
                    if ( type instanceof SimpleType<?> ) {
                        setProperty ( propertyPrefix + "." + key1, value );
                    } else {
                        createProperty ( propertyPrefix + "." + key1, value );
                    }
                }
            }
        } else if ( result.getClass().isArray() ) {
            if ( isSeparatearrayresults() ) {
                int size = 0;
                for ( int i = 0; i < Array.getLength ( result ); i++ ) {
                    if ( setProperty ( propertyPrefix + "." + size, Array.get (
                                           result, i ) ) ) {
                        size++;
                    }
                }
                if ( size > 0 ) {
                    setProperty ( propertyPrefix + ".Length", Integer
                                  .toString ( size ) );
                }
            }
        } else {
            String delim = getDelimiter();
            if ( delim != null ) {
                StringTokenizer tokenizer = new StringTokenizer ( result
                        .toString(), delim );
                int size = 0;
                for ( ; tokenizer.hasMoreTokens(); ) {
                    String token = tokenizer.nextToken();
                    if ( setProperty ( propertyPrefix + "." + size, token ) ) {
                        size++;
                    }
                }
                if ( size > 0 )
                    setProperty ( propertyPrefix + ".Length", Integer
                                  .toString ( size ) );
            } else {
                setProperty ( propertyPrefix, result.toString() );
            }
        }
    }
    public String getProperty ( String property ) {
        Project currentProject = getProject();
        if ( currentProject != null ) {
            return currentProject.getProperty ( property );
        } else {
            return properties.getProperty ( property );
        }
    }
    public boolean setProperty ( String property, Object value ) {
        if ( property != null ) {
            if ( value == null ) {
                value = "";
            }
            if ( isEcho() ) {
                handleOutput ( property + "=" + value.toString() );
            }
            Project currentProject = getProject();
            if ( currentProject != null ) {
                currentProject.setNewProperty ( property, value.toString() );
            } else {
                properties.setProperty ( property, value.toString() );
            }
            return true;
        }
        return false;
    }
}
