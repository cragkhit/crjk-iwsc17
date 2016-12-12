package org.apache.el.lang;
import java.io.ObjectInput;
import java.io.IOException;
import org.apache.el.util.ReflectionUtil;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.io.Externalizable;
public static class Function implements Externalizable {
    protected transient Method m;
    protected String owner;
    protected String name;
    protected String[] types;
    protected String prefix;
    protected String localName;
    public Function ( final String prefix, final String localName, final Method m ) {
        if ( localName == null ) {
            throw new NullPointerException ( "LocalName cannot be null" );
        }
        if ( m == null ) {
            throw new NullPointerException ( "Method cannot be null" );
        }
        this.prefix = prefix;
        this.localName = localName;
        this.m = m;
    }
    public Function() {
    }
    @Override
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeUTF ( ( this.prefix != null ) ? this.prefix : "" );
        out.writeUTF ( this.localName );
        this.getMethod();
        out.writeUTF ( ( this.owner != null ) ? this.owner : this.m.getDeclaringClass().getName() );
        out.writeUTF ( ( this.name != null ) ? this.name : this.m.getName() );
        out.writeObject ( ( this.types != null ) ? this.types : ReflectionUtil.toTypeNameArray ( this.m.getParameterTypes() ) );
    }
    @Override
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.prefix = in.readUTF();
        if ( "".equals ( this.prefix ) ) {
            this.prefix = null;
        }
        this.localName = in.readUTF();
        this.owner = in.readUTF();
        this.name = in.readUTF();
        this.types = ( String[] ) in.readObject();
    }
    public Method getMethod() {
        if ( this.m == null ) {
            try {
                final Class<?> t = ReflectionUtil.forName ( this.owner );
                final Class<?>[] p = ReflectionUtil.toTypeArray ( this.types );
                this.m = t.getMethod ( this.name, p );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        return this.m;
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj instanceof Function && this.hashCode() == obj.hashCode();
    }
    @Override
    public int hashCode() {
        return ( this.prefix + this.localName ).hashCode();
    }
}
