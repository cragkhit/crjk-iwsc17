package org.apache.naming;
public class NamingEntry {
    public static final int ENTRY = 0;
    public static final int LINK_REF = 1;
    public static final int REFERENCE = 2;
    public static final int CONTEXT = 10;
    public int type;
    public final String name;
    public Object value;
    public NamingEntry ( final String name, final Object value, final int type ) {
        this.name = name;
        this.value = value;
        this.type = type;
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj instanceof NamingEntry && this.name.equals ( ( ( NamingEntry ) obj ).name );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
