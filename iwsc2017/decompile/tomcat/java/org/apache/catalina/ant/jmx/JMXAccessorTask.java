package org.apache.catalina.ant.jmx;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;
import java.util.Iterator;
import java.util.Set;
import javax.management.openmbean.CompositeType;
import java.util.StringTokenizer;
import java.util.List;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.CompositeDataSupport;
import java.lang.reflect.Array;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.net.UnknownHostException;
import java.net.InetAddress;
import org.apache.tools.ant.Project;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import javax.management.MBeanServerConnection;
import org.apache.tools.ant.BuildException;
import java.util.Properties;
import org.apache.catalina.ant.BaseRedirectorHelperTask;
public class JMXAccessorTask extends BaseRedirectorHelperTask {
    public static final String JMX_SERVICE_PREFIX = "service:jmx:rmi:///jndi/rmi://";
    public static final String JMX_SERVICE_SUFFIX = "/jmxrmi";
    private String name;
    private String resultproperty;
    private String url;
    private String host;
    private String port;
    private String password;
    private String username;
    private String ref;
    private boolean echo;
    private boolean separatearrayresults;
    private String delimiter;
    private String unlessCondition;
    private String ifCondition;
    private final Properties properties;
    public JMXAccessorTask() {
        this.name = null;
        this.url = null;
        this.host = "localhost";
        this.port = "8050";
        this.password = null;
        this.username = null;
        this.ref = "jmx.server";
        this.echo = false;
        this.separatearrayresults = true;
        this.properties = new Properties();
    }
    public String getName() {
        return this.name;
    }
    public void setName ( final String objectName ) {
        this.name = objectName;
    }
    public String getResultproperty() {
        return this.resultproperty;
    }
    public void setResultproperty ( final String propertyName ) {
        this.resultproperty = propertyName;
    }
    public String getDelimiter() {
        return this.delimiter;
    }
    public void setDelimiter ( final String separator ) {
        this.delimiter = separator;
    }
    public boolean isEcho() {
        return this.echo;
    }
    public void setEcho ( final boolean echo ) {
        this.echo = echo;
    }
    public boolean isSeparatearrayresults() {
        return this.separatearrayresults;
    }
    public void setSeparatearrayresults ( final boolean separateArrayResults ) {
        this.separatearrayresults = separateArrayResults;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword ( final String password ) {
        this.password = password;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername ( final String username ) {
        this.username = username;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl ( final String url ) {
        this.url = url;
    }
    public String getHost() {
        return this.host;
    }
    public void setHost ( final String host ) {
        this.host = host;
    }
    public String getPort() {
        return this.port;
    }
    public void setPort ( final String port ) {
        this.port = port;
    }
    public boolean isUseRef() {
        return this.ref != null && !"".equals ( this.ref );
    }
    public String getRef() {
        return this.ref;
    }
    public void setRef ( final String refId ) {
        this.ref = refId;
    }
    public String getIf() {
        return this.ifCondition;
    }
    public void setIf ( final String c ) {
        this.ifCondition = c;
    }
    public String getUnless() {
        return this.unlessCondition;
    }
    public void setUnless ( final String c ) {
        this.unlessCondition = c;
    }
    public void execute() throws BuildException {
        if ( this.testIfCondition() && this.testUnlessCondition() ) {
            try {
                String error = null;
                final MBeanServerConnection jmxServerConnection = this.getJMXConnection();
                error = this.jmxExecute ( jmxServerConnection );
                if ( error != null && this.isFailOnError() ) {
                    throw new BuildException ( error );
                }
            } catch ( Exception e ) {
                if ( this.isFailOnError() ) {
                    throw new BuildException ( ( Throwable ) e );
                }
                this.handleErrorOutput ( e.getMessage() );
            } finally {
                this.closeRedirector();
            }
        }
    }
    public static MBeanServerConnection createJMXConnection ( final String url, final String host, final String port, final String username, final String password ) throws MalformedURLException, IOException {
        String urlForJMX;
        if ( url != null ) {
            urlForJMX = url;
        } else {
            urlForJMX = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
        }
        Map<String, String[]> environment = null;
        if ( username != null && password != null ) {
            final String[] credentials = { username, password };
            environment = new HashMap<String, String[]>();
            environment.put ( "jmx.remote.credentials", credentials );
        }
        return JMXConnectorFactory.connect ( new JMXServiceURL ( urlForJMX ), environment ).getMBeanServerConnection();
    }
    protected boolean testIfCondition() {
        return this.ifCondition == null || "".equals ( this.ifCondition ) || this.getProperty ( this.ifCondition ) != null;
    }
    protected boolean testUnlessCondition() {
        return this.unlessCondition == null || "".equals ( this.unlessCondition ) || this.getProperty ( this.unlessCondition ) == null;
    }
    public static MBeanServerConnection accessJMXConnection ( final Project project, final String url, final String host, final String port, final String username, final String password, final String refId ) throws MalformedURLException, IOException {
        MBeanServerConnection jmxServerConnection = null;
        final boolean isRef = project != null && refId != null && refId.length() > 0;
        if ( isRef ) {
            final Object pref = project.getReference ( refId );
            try {
                jmxServerConnection = ( MBeanServerConnection ) pref;
            } catch ( ClassCastException cce ) {
                project.log ( "wrong object reference " + refId + " - " + pref.getClass() );
                return null;
            }
        }
        if ( jmxServerConnection == null ) {
            jmxServerConnection = createJMXConnection ( url, host, port, username, password );
        }
        if ( isRef && jmxServerConnection != null ) {
            project.addReference ( refId, ( Object ) jmxServerConnection );
        }
        return jmxServerConnection;
    }
    protected MBeanServerConnection getJMXConnection() throws MalformedURLException, IOException {
        MBeanServerConnection jmxServerConnection = null;
        if ( this.isUseRef() ) {
            Object pref = null;
            if ( this.getProject() != null ) {
                pref = this.getProject().getReference ( this.getRef() );
                if ( pref != null ) {
                    try {
                        jmxServerConnection = ( MBeanServerConnection ) pref;
                    } catch ( ClassCastException cce ) {
                        this.getProject().log ( "Wrong object reference " + this.getRef() + " - " + pref.getClass() );
                        return null;
                    }
                }
            }
            if ( jmxServerConnection == null ) {
                jmxServerConnection = accessJMXConnection ( this.getProject(), this.getUrl(), this.getHost(), this.getPort(), this.getUsername(), this.getPassword(), this.getRef() );
            }
        } else {
            jmxServerConnection = accessJMXConnection ( this.getProject(), this.getUrl(), this.getHost(), this.getPort(), this.getUsername(), this.getPassword(), null );
        }
        return jmxServerConnection;
    }
    public String jmxExecute ( final MBeanServerConnection jmxServerConnection ) throws Exception {
        if ( jmxServerConnection == null ) {
            throw new BuildException ( "Must open a connection!" );
        }
        if ( this.isEcho() ) {
            this.handleOutput ( "JMX Connection ref=" + this.ref + " is open!" );
        }
        return null;
    }
    protected Object convertStringToType ( final String value, final String valueType ) {
        if ( "java.lang.String".equals ( valueType ) ) {
            return value;
        }
        Object convertValue = value;
        if ( !"java.lang.Integer".equals ( valueType ) ) {
            if ( !"int".equals ( valueType ) ) {
                if ( !"java.lang.Long".equals ( valueType ) ) {
                    if ( !"long".equals ( valueType ) ) {
                        if ( "java.lang.Boolean".equals ( valueType ) || "boolean".equals ( valueType ) ) {
                            convertValue = Boolean.valueOf ( value );
                            return convertValue;
                        }
                        if ( !"java.lang.Float".equals ( valueType ) ) {
                            if ( !"float".equals ( valueType ) ) {
                                if ( !"java.lang.Double".equals ( valueType ) ) {
                                    if ( !"double".equals ( valueType ) ) {
                                        if ( !"javax.management.ObjectName".equals ( valueType ) ) {
                                            if ( !"name".equals ( valueType ) ) {
                                                if ( "java.net.InetAddress".equals ( valueType ) ) {
                                                    try {
                                                        convertValue = InetAddress.getByName ( value );
                                                    } catch ( UnknownHostException exc ) {
                                                        if ( this.isEcho() ) {
                                                            this.handleErrorOutput ( "Unable to resolve host name:" + value );
                                                        }
                                                    }
                                                    return convertValue;
                                                }
                                                return convertValue;
                                            }
                                        }
                                        try {
                                            convertValue = new ObjectName ( value );
                                        } catch ( MalformedObjectNameException e ) {
                                            if ( this.isEcho() ) {
                                                this.handleErrorOutput ( "Unable to convert to ObjectName:" + value );
                                            }
                                        }
                                        return convertValue;
                                    }
                                }
                                try {
                                    convertValue = Double.valueOf ( value );
                                } catch ( NumberFormatException ex ) {
                                    if ( this.isEcho() ) {
                                        this.handleErrorOutput ( "Unable to convert to double:" + value );
                                    }
                                }
                                return convertValue;
                            }
                        }
                        try {
                            convertValue = Float.valueOf ( value );
                        } catch ( NumberFormatException ex ) {
                            if ( this.isEcho() ) {
                                this.handleErrorOutput ( "Unable to convert to float:" + value );
                            }
                        }
                        return convertValue;
                    }
                }
                try {
                    convertValue = Long.valueOf ( value );
                } catch ( NumberFormatException ex ) {
                    if ( this.isEcho() ) {
                        this.handleErrorOutput ( "Unable to convert to long:" + value );
                    }
                }
                return convertValue;
            }
        }
        try {
            convertValue = Integer.valueOf ( value );
        } catch ( NumberFormatException ex ) {
            if ( this.isEcho() ) {
                this.handleErrorOutput ( "Unable to convert to integer:" + value );
            }
        }
        return convertValue;
    }
    protected void echoResult ( final String name, final Object result ) {
        if ( this.isEcho() ) {
            if ( result.getClass().isArray() ) {
                for ( int i = 0; i < Array.getLength ( result ); ++i ) {
                    this.handleOutput ( name + "." + i + "=" + Array.get ( result, i ) );
                }
            } else {
                this.handleOutput ( name + "=" + result );
            }
        }
    }
    protected void createProperty ( final Object result ) {
        if ( this.resultproperty != null ) {
            this.createProperty ( this.resultproperty, result );
        }
    }
    protected void createProperty ( String propertyPrefix, final Object result ) {
        if ( propertyPrefix == null ) {
            propertyPrefix = "";
        }
        if ( result instanceof CompositeDataSupport ) {
            final CompositeDataSupport data = ( CompositeDataSupport ) result;
            final CompositeType compositeType = data.getCompositeType();
            final Set<String> keys = compositeType.keySet();
            for ( final String key : keys ) {
                final Object value = data.get ( key );
                final OpenType<?> type = compositeType.getType ( key );
                if ( type instanceof SimpleType ) {
                    this.setProperty ( propertyPrefix + "." + key, value );
                } else {
                    this.createProperty ( propertyPrefix + "." + key, value );
                }
            }
        } else if ( result instanceof TabularDataSupport ) {
            final TabularDataSupport data2 = ( TabularDataSupport ) result;
            for ( final Object key2 : data2.keySet() ) {
                for ( final Object key3 : ( List ) key2 ) {
                    final CompositeData valuedata = data2.get ( new Object[] { key3 } );
                    final Object value2 = valuedata.get ( "value" );
                    final OpenType<?> type2 = valuedata.getCompositeType().getType ( "value" );
                    if ( type2 instanceof SimpleType ) {
                        this.setProperty ( propertyPrefix + "." + key3, value2 );
                    } else {
                        this.createProperty ( propertyPrefix + "." + key3, value2 );
                    }
                }
            }
        } else if ( result.getClass().isArray() ) {
            if ( this.isSeparatearrayresults() ) {
                int size = 0;
                for ( int i = 0; i < Array.getLength ( result ); ++i ) {
                    if ( this.setProperty ( propertyPrefix + "." + size, Array.get ( result, i ) ) ) {
                        ++size;
                    }
                }
                if ( size > 0 ) {
                    this.setProperty ( propertyPrefix + ".Length", Integer.toString ( size ) );
                }
            }
        } else {
            final String delim = this.getDelimiter();
            if ( delim != null ) {
                final StringTokenizer tokenizer = new StringTokenizer ( result.toString(), delim );
                int size2 = 0;
                while ( tokenizer.hasMoreTokens() ) {
                    final String token = tokenizer.nextToken();
                    if ( this.setProperty ( propertyPrefix + "." + size2, token ) ) {
                        ++size2;
                    }
                }
                if ( size2 > 0 ) {
                    this.setProperty ( propertyPrefix + ".Length", Integer.toString ( size2 ) );
                }
            } else {
                this.setProperty ( propertyPrefix, result.toString() );
            }
        }
    }
    public String getProperty ( final String property ) {
        final Project currentProject = this.getProject();
        if ( currentProject != null ) {
            return currentProject.getProperty ( property );
        }
        return this.properties.getProperty ( property );
    }
    public boolean setProperty ( final String property, Object value ) {
        if ( property != null ) {
            if ( value == null ) {
                value = "";
            }
            if ( this.isEcho() ) {
                this.handleOutput ( property + "=" + value.toString() );
            }
            final Project currentProject = this.getProject();
            if ( currentProject != null ) {
                currentProject.setNewProperty ( property, value.toString() );
            } else {
                this.properties.setProperty ( property, value.toString() );
            }
            return true;
        }
        return false;
    }
}
