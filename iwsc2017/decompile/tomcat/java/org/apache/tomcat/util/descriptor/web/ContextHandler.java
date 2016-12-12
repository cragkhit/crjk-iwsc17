package org.apache.tomcat.util.descriptor.web;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
public class ContextHandler extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private String handlerclass;
    private final HashMap<String, String> soapHeaders;
    private final ArrayList<String> soapRoles;
    private final ArrayList<String> portNames;
    public ContextHandler() {
        this.handlerclass = null;
        this.soapHeaders = new HashMap<String, String>();
        this.soapRoles = new ArrayList<String>();
        this.portNames = new ArrayList<String>();
    }
    public String getHandlerclass() {
        return this.handlerclass;
    }
    public void setHandlerclass ( final String handlerclass ) {
        this.handlerclass = handlerclass;
    }
    public Iterator<String> getLocalparts() {
        return this.soapHeaders.keySet().iterator();
    }
    public String getNamespaceuri ( final String localpart ) {
        return this.soapHeaders.get ( localpart );
    }
    public void addSoapHeaders ( final String localpart, final String namespaceuri ) {
        this.soapHeaders.put ( localpart, namespaceuri );
    }
    public void setProperty ( final String name, final String value ) {
        this.setProperty ( name, value );
    }
    public String getSoapRole ( final int i ) {
        return this.soapRoles.get ( i );
    }
    public int getSoapRolesSize() {
        return this.soapRoles.size();
    }
    public void addSoapRole ( final String soapRole ) {
        this.soapRoles.add ( soapRole );
    }
    public String getPortName ( final int i ) {
        return this.portNames.get ( i );
    }
    public int getPortNamesSize() {
        return this.portNames.size();
    }
    public void addPortName ( final String portName ) {
        this.portNames.add ( portName );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ContextHandler[" );
        sb.append ( "name=" );
        sb.append ( this.getName() );
        if ( this.handlerclass != null ) {
            sb.append ( ", class=" );
            sb.append ( this.handlerclass );
        }
        if ( this.soapHeaders != null ) {
            sb.append ( ", soap-headers=" );
            sb.append ( this.soapHeaders );
        }
        if ( this.getSoapRolesSize() > 0 ) {
            sb.append ( ", soap-roles=" );
            sb.append ( this.soapRoles );
        }
        if ( this.getPortNamesSize() > 0 ) {
            sb.append ( ", port-name=" );
            sb.append ( this.portNames );
        }
        if ( this.listProperties() != null ) {
            sb.append ( ", init-param=" );
            sb.append ( this.listProperties() );
        }
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = 31 * result + ( ( this.handlerclass == null ) ? 0 : this.handlerclass.hashCode() );
        result = 31 * result + ( ( this.portNames == null ) ? 0 : this.portNames.hashCode() );
        result = 31 * result + ( ( this.soapHeaders == null ) ? 0 : this.soapHeaders.hashCode() );
        result = 31 * result + ( ( this.soapRoles == null ) ? 0 : this.soapRoles.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( this.getClass() != obj.getClass() ) {
            return false;
        }
        final ContextHandler other = ( ContextHandler ) obj;
        if ( this.handlerclass == null ) {
            if ( other.handlerclass != null ) {
                return false;
            }
        } else if ( !this.handlerclass.equals ( other.handlerclass ) ) {
            return false;
        }
        if ( this.portNames == null ) {
            if ( other.portNames != null ) {
                return false;
            }
        } else if ( !this.portNames.equals ( other.portNames ) ) {
            return false;
        }
        if ( this.soapHeaders == null ) {
            if ( other.soapHeaders != null ) {
                return false;
            }
        } else if ( !this.soapHeaders.equals ( other.soapHeaders ) ) {
            return false;
        }
        if ( this.soapRoles == null ) {
            if ( other.soapRoles != null ) {
                return false;
            }
        } else if ( !this.soapRoles.equals ( other.soapRoles ) ) {
            return false;
        }
        return true;
    }
}
