package org.apache.tomcat.util.descriptor.web;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.Serializable;
public class ResourceBase implements Serializable, Injectable {
    private static final long serialVersionUID = 1L;
    private String description;
    private String name;
    private String type;
    private final HashMap<String, Object> properties;
    private final List<InjectionTarget> injectionTargets;
    private NamingResources resources;
    public ResourceBase() {
        this.description = null;
        this.name = null;
        this.type = null;
        this.properties = new HashMap<String, Object>();
        this.injectionTargets = new ArrayList<InjectionTarget>();
        this.resources = null;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription ( final String description ) {
        this.description = description;
    }
    @Override
    public String getName() {
        return this.name;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public String getType() {
        return this.type;
    }
    public void setType ( final String type ) {
        this.type = type;
    }
    public Object getProperty ( final String name ) {
        return this.properties.get ( name );
    }
    public void setProperty ( final String name, final Object value ) {
        this.properties.put ( name, value );
    }
    public void removeProperty ( final String name ) {
        this.properties.remove ( name );
    }
    public Iterator<String> listProperties() {
        return this.properties.keySet().iterator();
    }
    @Override
    public void addInjectionTarget ( final String injectionTargetName, final String jndiName ) {
        final InjectionTarget target = new InjectionTarget ( injectionTargetName, jndiName );
        this.injectionTargets.add ( target );
    }
    @Override
    public List<InjectionTarget> getInjectionTargets() {
        return this.injectionTargets;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ( ( this.description == null ) ? 0 : this.description.hashCode() );
        result = 31 * result + ( ( this.injectionTargets == null ) ? 0 : this.injectionTargets.hashCode() );
        result = 31 * result + ( ( this.name == null ) ? 0 : this.name.hashCode() );
        result = 31 * result + ( ( this.properties == null ) ? 0 : this.properties.hashCode() );
        result = 31 * result + ( ( this.type == null ) ? 0 : this.type.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( this.getClass() != obj.getClass() ) {
            return false;
        }
        final ResourceBase other = ( ResourceBase ) obj;
        if ( this.description == null ) {
            if ( other.description != null ) {
                return false;
            }
        } else if ( !this.description.equals ( other.description ) ) {
            return false;
        }
        if ( this.injectionTargets == null ) {
            if ( other.injectionTargets != null ) {
                return false;
            }
        } else if ( !this.injectionTargets.equals ( other.injectionTargets ) ) {
            return false;
        }
        if ( this.name == null ) {
            if ( other.name != null ) {
                return false;
            }
        } else if ( !this.name.equals ( other.name ) ) {
            return false;
        }
        if ( this.properties == null ) {
            if ( other.properties != null ) {
                return false;
            }
        } else if ( !this.properties.equals ( other.properties ) ) {
            return false;
        }
        if ( this.type == null ) {
            if ( other.type != null ) {
                return false;
            }
        } else if ( !this.type.equals ( other.type ) ) {
            return false;
        }
        return true;
    }
    public NamingResources getNamingResources() {
        return this.resources;
    }
    public void setNamingResources ( final NamingResources resources ) {
        this.resources = resources;
    }
}
