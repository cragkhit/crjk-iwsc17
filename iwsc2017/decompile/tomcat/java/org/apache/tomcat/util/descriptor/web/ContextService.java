package org.apache.tomcat.util.descriptor.web;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;
public class ContextService extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private String displayname;
    private String largeIcon;
    private String smallIcon;
    private String serviceInterface;
    private String wsdlfile;
    private String jaxrpcmappingfile;
    private String[] serviceqname;
    private final HashMap<String, ContextHandler> handlers;
    public ContextService() {
        this.displayname = null;
        this.largeIcon = null;
        this.smallIcon = null;
        this.serviceInterface = null;
        this.wsdlfile = null;
        this.jaxrpcmappingfile = null;
        this.serviceqname = new String[2];
        this.handlers = new HashMap<String, ContextHandler>();
    }
    public String getDisplayname() {
        return this.displayname;
    }
    public void setDisplayname ( final String displayname ) {
        this.displayname = displayname;
    }
    public String getLargeIcon() {
        return this.largeIcon;
    }
    public void setLargeIcon ( final String largeIcon ) {
        this.largeIcon = largeIcon;
    }
    public String getSmallIcon() {
        return this.smallIcon;
    }
    public void setSmallIcon ( final String smallIcon ) {
        this.smallIcon = smallIcon;
    }
    public String getInterface() {
        return this.serviceInterface;
    }
    public void setInterface ( final String serviceInterface ) {
        this.serviceInterface = serviceInterface;
    }
    public String getWsdlfile() {
        return this.wsdlfile;
    }
    public void setWsdlfile ( final String wsdlfile ) {
        this.wsdlfile = wsdlfile;
    }
    public String getJaxrpcmappingfile() {
        return this.jaxrpcmappingfile;
    }
    public void setJaxrpcmappingfile ( final String jaxrpcmappingfile ) {
        this.jaxrpcmappingfile = jaxrpcmappingfile;
    }
    public String[] getServiceqname() {
        return this.serviceqname;
    }
    public String getServiceqname ( final int i ) {
        return this.serviceqname[i];
    }
    public String getServiceqnameNamespaceURI() {
        return this.serviceqname[0];
    }
    public String getServiceqnameLocalpart() {
        return this.serviceqname[1];
    }
    public void setServiceqname ( final String[] serviceqname ) {
        this.serviceqname = serviceqname;
    }
    public void setServiceqname ( final String serviceqname, final int i ) {
        this.serviceqname[i] = serviceqname;
    }
    public void setServiceqnameNamespaceURI ( final String namespaceuri ) {
        this.serviceqname[0] = namespaceuri;
    }
    public void setServiceqnameLocalpart ( final String localpart ) {
        this.serviceqname[1] = localpart;
    }
    public Iterator<String> getServiceendpoints() {
        return this.listProperties();
    }
    public String getPortlink ( final String serviceendpoint ) {
        return ( String ) this.getProperty ( serviceendpoint );
    }
    public void addPortcomponent ( final String serviceendpoint, String portlink ) {
        if ( portlink == null ) {
            portlink = "";
        }
        this.setProperty ( serviceendpoint, portlink );
    }
    public Iterator<String> getHandlers() {
        return this.handlers.keySet().iterator();
    }
    public ContextHandler getHandler ( final String handlername ) {
        return this.handlers.get ( handlername );
    }
    public void addHandler ( final ContextHandler handler ) {
        this.handlers.put ( handler.getName(), handler );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ContextService[" );
        sb.append ( "name=" );
        sb.append ( this.getName() );
        if ( this.getDescription() != null ) {
            sb.append ( ", description=" );
            sb.append ( this.getDescription() );
        }
        if ( this.getType() != null ) {
            sb.append ( ", type=" );
            sb.append ( this.getType() );
        }
        if ( this.displayname != null ) {
            sb.append ( ", displayname=" );
            sb.append ( this.displayname );
        }
        if ( this.largeIcon != null ) {
            sb.append ( ", largeIcon=" );
            sb.append ( this.largeIcon );
        }
        if ( this.smallIcon != null ) {
            sb.append ( ", smallIcon=" );
            sb.append ( this.smallIcon );
        }
        if ( this.wsdlfile != null ) {
            sb.append ( ", wsdl-file=" );
            sb.append ( this.wsdlfile );
        }
        if ( this.jaxrpcmappingfile != null ) {
            sb.append ( ", jaxrpc-mapping-file=" );
            sb.append ( this.jaxrpcmappingfile );
        }
        if ( this.serviceqname[0] != null ) {
            sb.append ( ", service-qname/namespaceURI=" );
            sb.append ( this.serviceqname[0] );
        }
        if ( this.serviceqname[1] != null ) {
            sb.append ( ", service-qname/localpart=" );
            sb.append ( this.serviceqname[1] );
        }
        if ( this.getServiceendpoints() != null ) {
            sb.append ( ", port-component/service-endpoint-interface=" );
            sb.append ( this.getServiceendpoints() );
        }
        if ( this.handlers != null ) {
            sb.append ( ", handler=" );
            sb.append ( this.handlers );
        }
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = 31 * result + ( ( this.displayname == null ) ? 0 : this.displayname.hashCode() );
        result = 31 * result + ( ( this.handlers == null ) ? 0 : this.handlers.hashCode() );
        result = 31 * result + ( ( this.jaxrpcmappingfile == null ) ? 0 : this.jaxrpcmappingfile.hashCode() );
        result = 31 * result + ( ( this.largeIcon == null ) ? 0 : this.largeIcon.hashCode() );
        result = 31 * result + ( ( this.serviceInterface == null ) ? 0 : this.serviceInterface.hashCode() );
        result = 31 * result + Arrays.hashCode ( this.serviceqname );
        result = 31 * result + ( ( this.smallIcon == null ) ? 0 : this.smallIcon.hashCode() );
        result = 31 * result + ( ( this.wsdlfile == null ) ? 0 : this.wsdlfile.hashCode() );
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
        final ContextService other = ( ContextService ) obj;
        if ( this.displayname == null ) {
            if ( other.displayname != null ) {
                return false;
            }
        } else if ( !this.displayname.equals ( other.displayname ) ) {
            return false;
        }
        if ( this.handlers == null ) {
            if ( other.handlers != null ) {
                return false;
            }
        } else if ( !this.handlers.equals ( other.handlers ) ) {
            return false;
        }
        if ( this.jaxrpcmappingfile == null ) {
            if ( other.jaxrpcmappingfile != null ) {
                return false;
            }
        } else if ( !this.jaxrpcmappingfile.equals ( other.jaxrpcmappingfile ) ) {
            return false;
        }
        if ( this.largeIcon == null ) {
            if ( other.largeIcon != null ) {
                return false;
            }
        } else if ( !this.largeIcon.equals ( other.largeIcon ) ) {
            return false;
        }
        if ( this.serviceInterface == null ) {
            if ( other.serviceInterface != null ) {
                return false;
            }
        } else if ( !this.serviceInterface.equals ( other.serviceInterface ) ) {
            return false;
        }
        if ( !Arrays.equals ( this.serviceqname, other.serviceqname ) ) {
            return false;
        }
        if ( this.smallIcon == null ) {
            if ( other.smallIcon != null ) {
                return false;
            }
        } else if ( !this.smallIcon.equals ( other.smallIcon ) ) {
            return false;
        }
        if ( this.wsdlfile == null ) {
            if ( other.wsdlfile != null ) {
                return false;
            }
        } else if ( !this.wsdlfile.equals ( other.wsdlfile ) ) {
            return false;
        }
        return true;
    }
}
