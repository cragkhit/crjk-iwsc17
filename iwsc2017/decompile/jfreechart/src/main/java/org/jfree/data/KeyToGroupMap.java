package org.jfree.data;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import org.jfree.chart.util.ParamChecks;
import java.util.Map;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class KeyToGroupMap implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -2228169345475318082L;
    private Comparable defaultGroup;
    private List groups;
    private Map keyToGroupMap;
    public KeyToGroupMap() {
        this ( "Default Group" );
    }
    public KeyToGroupMap ( final Comparable defaultGroup ) {
        ParamChecks.nullNotPermitted ( defaultGroup, "defaultGroup" );
        this.defaultGroup = defaultGroup;
        this.groups = new ArrayList();
        this.keyToGroupMap = new HashMap();
    }
    public int getGroupCount() {
        return this.groups.size() + 1;
    }
    public List getGroups() {
        final List result = new ArrayList();
        result.add ( this.defaultGroup );
        for ( final Comparable group : this.groups ) {
            if ( !result.contains ( group ) ) {
                result.add ( group );
            }
        }
        return result;
    }
    public int getGroupIndex ( final Comparable group ) {
        int result = this.groups.indexOf ( group );
        if ( result < 0 ) {
            if ( this.defaultGroup.equals ( group ) ) {
                result = 0;
            }
        } else {
            ++result;
        }
        return result;
    }
    public Comparable getGroup ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        Comparable result = this.defaultGroup;
        final Comparable group = this.keyToGroupMap.get ( key );
        if ( group != null ) {
            result = group;
        }
        return result;
    }
    public void mapKeyToGroup ( final Comparable key, final Comparable group ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        final Comparable currentGroup = this.getGroup ( key );
        if ( !currentGroup.equals ( this.defaultGroup ) && !currentGroup.equals ( group ) ) {
            final int count = this.getKeyCount ( currentGroup );
            if ( count == 1 ) {
                this.groups.remove ( currentGroup );
            }
        }
        if ( group == null ) {
            this.keyToGroupMap.remove ( key );
        } else {
            if ( !this.groups.contains ( group ) && !this.defaultGroup.equals ( group ) ) {
                this.groups.add ( group );
            }
            this.keyToGroupMap.put ( key, group );
        }
    }
    public int getKeyCount ( final Comparable group ) {
        ParamChecks.nullNotPermitted ( group, "group" );
        int result = 0;
        for ( final Comparable g : this.keyToGroupMap.values() ) {
            if ( group.equals ( g ) ) {
                ++result;
            }
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof KeyToGroupMap ) ) {
            return false;
        }
        final KeyToGroupMap that = ( KeyToGroupMap ) obj;
        return ObjectUtilities.equal ( ( Object ) this.defaultGroup, ( Object ) that.defaultGroup ) && this.keyToGroupMap.equals ( that.keyToGroupMap );
    }
    public Object clone() throws CloneNotSupportedException {
        final KeyToGroupMap result = ( KeyToGroupMap ) super.clone();
        result.defaultGroup = ( Comparable ) clone ( this.defaultGroup );
        result.groups = ( List ) clone ( this.groups );
        result.keyToGroupMap = ( Map ) clone ( this.keyToGroupMap );
        return result;
    }
    private static Object clone ( final Object object ) {
        if ( object == null ) {
            return null;
        }
        final Class c = object.getClass();
        Object result = null;
        try {
            final Method m = c.getMethod ( "clone", ( Class[] ) null );
            if ( Modifier.isPublic ( m.getModifiers() ) ) {
                try {
                    result = m.invoke ( object, ( Object[] ) null );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        } catch ( NoSuchMethodException e2 ) {
            result = object;
        }
        return result;
    }
    private static Collection clone ( final Collection list ) throws CloneNotSupportedException {
        Collection result = null;
        if ( list != null ) {
            try {
                final List clone = ( List ) list.getClass().newInstance();
                final Iterator iterator = list.iterator();
                while ( iterator.hasNext() ) {
                    clone.add ( clone ( iterator.next() ) );
                }
                result = clone;
            } catch ( Exception e ) {
                throw new CloneNotSupportedException ( "Exception." );
            }
        }
        return result;
    }
}
