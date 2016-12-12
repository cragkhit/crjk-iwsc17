// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.io.Serializable;

public static class InterceptorProperty implements Serializable
{
    private static final long serialVersionUID = 1L;
    String name;
    String value;
    
    public InterceptorProperty(final String name, final String value) {
        assert name != null;
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public boolean getValueAsBoolean(final boolean def) {
        if (this.value == null) {
            return def;
        }
        return "true".equals(this.value) || (!"false".equals(this.value) && def);
    }
    
    public int getValueAsInt(final int def) {
        if (this.value == null) {
            return def;
        }
        try {
            final int v = Integer.parseInt(this.value);
            return v;
        }
        catch (NumberFormatException nfe) {
            return def;
        }
    }
    
    public long getValueAsLong(final long def) {
        if (this.value == null) {
            return def;
        }
        try {
            return Long.parseLong(this.value);
        }
        catch (NumberFormatException nfe) {
            return def;
        }
    }
    
    public byte getValueAsByte(final byte def) {
        if (this.value == null) {
            return def;
        }
        try {
            return Byte.parseByte(this.value);
        }
        catch (NumberFormatException nfe) {
            return def;
        }
    }
    
    public short getValueAsShort(final short def) {
        if (this.value == null) {
            return def;
        }
        try {
            return Short.parseShort(this.value);
        }
        catch (NumberFormatException nfe) {
            return def;
        }
    }
    
    public float getValueAsFloat(final float def) {
        if (this.value == null) {
            return def;
        }
        try {
            return Float.parseFloat(this.value);
        }
        catch (NumberFormatException nfe) {
            return def;
        }
    }
    
    public double getValueAsDouble(final double def) {
        if (this.value == null) {
            return def;
        }
        try {
            return Double.parseDouble(this.value);
        }
        catch (NumberFormatException nfe) {
            return def;
        }
    }
    
    public char getValueAschar(final char def) {
        if (this.value == null) {
            return def;
        }
        try {
            return this.value.charAt(0);
        }
        catch (StringIndexOutOfBoundsException nfe) {
            return def;
        }
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof InterceptorProperty) {
            final InterceptorProperty other = (InterceptorProperty)o;
            return other.name.equals(this.name);
        }
        return false;
    }
}
