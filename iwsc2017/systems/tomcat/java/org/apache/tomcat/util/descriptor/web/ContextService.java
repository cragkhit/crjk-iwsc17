package org.apache.tomcat.util.descriptor.web;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
public class ContextService extends ResourceBase {
    private static final long serialVersionUID = 1L;
    private String displayname = null;
    public String getDisplayname() {
        return ( this.displayname );
    }
    public void setDisplayname ( String displayname ) {
        this.displayname = displayname;
    }
    private String largeIcon = null;
    public String getLargeIcon() {
        return ( this.largeIcon );
    }
    public void setLargeIcon ( String largeIcon ) {
        this.largeIcon = largeIcon;
    }
    private String smallIcon = null;
    public String getSmallIcon() {
        return ( this.smallIcon );
    }
    public void setSmallIcon ( String smallIcon ) {
        this.smallIcon = smallIcon;
    }
    private String serviceInterface = null;
    public String getInterface() {
        return serviceInterface;
    }
    public void setInterface ( String serviceInterface ) {
        this.serviceInterface = serviceInterface;
    }
    private String wsdlfile = null;
    public String getWsdlfile() {
        return ( this.wsdlfile );
    }
    public void setWsdlfile ( String wsdlfile ) {
        this.wsdlfile = wsdlfile;
    }
    private String jaxrpcmappingfile = null;
    public String getJaxrpcmappingfile() {
        return ( this.jaxrpcmappingfile );
    }
    public void setJaxrpcmappingfile ( String jaxrpcmappingfile ) {
        this.jaxrpcmappingfile = jaxrpcmappingfile;
    }
    private String[] serviceqname = new String[2];
    public String[] getServiceqname() {
        return ( this.serviceqname );
    }
    public String getServiceqname ( int i ) {
        return this.serviceqname[i];
    }
    public String getServiceqnameNamespaceURI() {
        return this.serviceqname[0];
    }
    public String getServiceqnameLocalpart() {
        return this.serviceqname[1];
    }
    public void setServiceqname ( String[] serviceqname ) {
        this.serviceqname = serviceqname;
    }
    public void setServiceqname ( String serviceqname, int i ) {
        this.serviceqname[i] = serviceqname;
    }
    public void setServiceqnameNamespaceURI ( String namespaceuri ) {
        this.serviceqname[0] = namespaceuri;
    }
    public void setServiceqnameLocalpart ( String localpart ) {
        this.serviceqname[1] = localpart;
    }
    public Iterator<String> getServiceendpoints() {
        return this.listProperties();
    }
    public String getPortlink ( String serviceendpoint ) {
        return ( String ) this.getProperty ( serviceendpoint );
    }
    public void addPortcomponent ( String serviceendpoint, String portlink ) {
        if ( portlink == null ) {
            portlink = "";
        }
        this.setProperty ( serviceendpoint, portlink );
    }
    private final HashMap<String, ContextHandler> handlers = new HashMap<>();
    public Iterator<String> getHandlers() {
        return handlers.keySet().iterator();
    }
    public ContextHandler getHandler ( String handlername ) {
        return handlers.get ( handlername );
    }
    public void addHandler ( ContextHandler handler ) {
        handlers.put ( handler.getName(), handler );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "ContextService[" );
        sb.append ( "name=" );
        sb.append ( getName() );
        if ( getDescription() != null ) {
            sb.append ( ", description=" );
            sb.append ( getDescription() );
        }
        if ( getType() != null ) {
            sb.append ( ", type=" );
            sb.append ( getType() );
        }
        if ( displayname != null ) {
            sb.append ( ", displayname=" );
            sb.append ( displayname );
        }
        if ( largeIcon != null ) {
            sb.append ( ", largeIcon=" );
            sb.append ( largeIcon );
        }
        if ( smallIcon != null ) {
            sb.append ( ", smallIcon=" );
            sb.append ( smallIcon );
        }
        if ( wsdlfile != null ) {
            sb.append ( ", wsdl-file=" );
            sb.append ( wsdlfile );
        }
        if ( jaxrpcmappingfile != null ) {
            sb.append ( ", jaxrpc-mapping-file=" );
            sb.append ( jaxrpcmappingfile );
        }
        if ( serviceqname[0] != null ) {
            sb.append ( ", service-qname/namespaceURI=" );
            sb.append ( serviceqname[0] );
        }
        if ( serviceqname[1] != null ) {
            sb.append ( ", service-qname/localpart=" );
            sb.append ( serviceqname[1] );
        }
        if ( this.getServiceendpoints() != null ) {
            sb.append ( ", port-component/service-endpoint-interface=" );
            sb.append ( this.getServiceendpoints() );
        }
        if ( handlers != null ) {
            sb.append ( ", handler=" );
            sb.append ( handlers );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result +
                 ( ( displayname == null ) ? 0 : displayname.hashCode() );
        result = prime * result +
                 ( ( handlers == null ) ? 0 : handlers.hashCode() );
        result = prime *
                 result +
                 ( ( jaxrpcmappingfile == null ) ? 0 : jaxrpcmappingfile.hashCode() );
        result = prime * result +
                 ( ( largeIcon == null ) ? 0 : largeIcon.hashCode() );
        result = prime * result +
                 ( ( serviceInterface == null ) ? 0 : serviceInterface.hashCode() );
        result = prime * result + Arrays.hashCode ( serviceqname );
        result = prime * result +
                 ( ( smallIcon == null ) ? 0 : smallIcon.hashCode() );
        result = prime * result +
                 ( ( wsdlfile == null ) ? 0 : wsdlfile.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        ContextService other = ( ContextService ) obj;
        if ( displayname == null ) {
            if ( other.displayname != null ) {
                return false;
            }
        } else if ( !displayname.equals ( other.displayname ) ) {
            return false;
        }
        if ( handlers == null ) {
            if ( other.handlers != null ) {
                return false;
            }
        } else if ( !handlers.equals ( other.handlers ) ) {
            return false;
        }
        if ( jaxrpcmappingfile == null ) {
            if ( other.jaxrpcmappingfile != null ) {
                return false;
            }
        } else if ( !jaxrpcmappingfile.equals ( other.jaxrpcmappingfile ) ) {
            return false;
        }
        if ( largeIcon == null ) {
            if ( other.largeIcon != null ) {
                return false;
            }
        } else if ( !largeIcon.equals ( other.largeIcon ) ) {
            return false;
        }
        if ( serviceInterface == null ) {
            if ( other.serviceInterface != null ) {
                return false;
            }
        } else if ( !serviceInterface.equals ( other.serviceInterface ) ) {
            return false;
        }
        if ( !Arrays.equals ( serviceqname, other.serviceqname ) ) {
            return false;
        }
        if ( smallIcon == null ) {
            if ( other.smallIcon != null ) {
                return false;
            }
        } else if ( !smallIcon.equals ( other.smallIcon ) ) {
            return false;
        }
        if ( wsdlfile == null ) {
            if ( other.wsdlfile != null ) {
                return false;
            }
        } else if ( !wsdlfile.equals ( other.wsdlfile ) ) {
            return false;
        }
        return true;
    }
}
