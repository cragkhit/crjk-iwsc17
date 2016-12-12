package org.apache.catalina.ant.jmx;
import javax.management.ObjectName;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.management.MBeanServerConnection;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.ProjectComponent;
public abstract class JMXAccessorConditionBase extends ProjectComponent implements Condition {
    private String url;
    private String host;
    private String port;
    private String password;
    private String username;
    private String name;
    private String attribute;
    private String value;
    private String ref;
    public JMXAccessorConditionBase() {
        this.url = null;
        this.host = "localhost";
        this.port = "8050";
        this.password = null;
        this.username = null;
        this.name = null;
        this.ref = "jmx.server";
    }
    public String getAttribute() {
        return this.attribute;
    }
    public void setAttribute ( final String attribute ) {
        this.attribute = attribute;
    }
    public String getHost() {
        return this.host;
    }
    public void setHost ( final String host ) {
        this.host = host;
    }
    public String getName() {
        return this.name;
    }
    public void setName ( final String objectName ) {
        this.name = objectName;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword ( final String password ) {
        this.password = password;
    }
    public String getPort() {
        return this.port;
    }
    public void setPort ( final String port ) {
        this.port = port;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl ( final String url ) {
        this.url = url;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername ( final String username ) {
        this.username = username;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue ( final String value ) {
        this.value = value;
    }
    public String getRef() {
        return this.ref;
    }
    public void setRef ( final String refId ) {
        this.ref = refId;
    }
    protected MBeanServerConnection getJMXConnection() throws MalformedURLException, IOException {
        return JMXAccessorTask.accessJMXConnection ( this.getProject(), this.getUrl(), this.getHost(), this.getPort(), this.getUsername(), this.getPassword(), this.ref );
    }
    protected String accessJMXValue() {
        try {
            final Object result = this.getJMXConnection().getAttribute ( new ObjectName ( this.name ), this.attribute );
            if ( result != null ) {
                return result.toString();
            }
        } catch ( Exception ex ) {}
        return null;
    }
}
