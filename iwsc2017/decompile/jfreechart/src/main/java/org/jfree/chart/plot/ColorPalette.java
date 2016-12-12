package org.jfree.chart.plot;
import org.jfree.chart.axis.ValueTick;
import java.util.List;
import java.util.Arrays;
import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;
public abstract class ColorPalette implements Cloneable, Serializable {
    private static final long serialVersionUID = -9029901853079622051L;
    protected double minZ;
    protected double maxZ;
    protected int[] r;
    protected int[] g;
    protected int[] b;
    protected double[] tickValues;
    protected boolean logscale;
    protected boolean inverse;
    protected String paletteName;
    protected boolean stepped;
    protected static final double log10;
    public ColorPalette() {
        this.minZ = -1.0;
        this.maxZ = -1.0;
        this.tickValues = null;
        this.logscale = false;
        this.inverse = false;
        this.paletteName = null;
        this.stepped = false;
    }
    public Paint getColor ( final double value ) {
        final int izV = ( int ) ( 253.0 * ( value - this.minZ ) / ( this.maxZ - this.minZ ) ) + 2;
        return new Color ( this.r[izV], this.g[izV], this.b[izV] );
    }
    public Color getColor ( final int izV ) {
        return new Color ( this.r[izV], this.g[izV], this.b[izV] );
    }
    public Color getColorLinear ( double value ) {
        if ( this.stepped ) {
            int index = Arrays.binarySearch ( this.tickValues, value );
            if ( index < 0 ) {
                index = -1 * index - 2;
            }
            if ( index < 0 ) {
                value = this.minZ;
            } else {
                value = this.tickValues[index];
            }
        }
        int izV = ( int ) ( 253.0 * ( value - this.minZ ) / ( this.maxZ - this.minZ ) ) + 2;
        izV = Math.min ( izV, 255 );
        izV = Math.max ( izV, 2 );
        return this.getColor ( izV );
    }
    public Color getColorLog ( double value ) {
        final double minZtmp = this.minZ;
        final double maxZtmp = this.maxZ;
        if ( this.minZ <= 0.0 ) {
            this.maxZ = maxZtmp - minZtmp + 1.0;
            this.minZ = 1.0;
            value = value - minZtmp + 1.0;
        }
        final double minZlog = Math.log ( this.minZ ) / ColorPalette.log10;
        final double maxZlog = Math.log ( this.maxZ ) / ColorPalette.log10;
        value = Math.log ( value ) / ColorPalette.log10;
        int izV;
        if ( this.stepped ) {
            final int numSteps = this.tickValues.length;
            final int steps = 256 / ( numSteps - 1 );
            izV = steps * ( int ) ( numSteps * ( value - minZlog ) / ( maxZlog - minZlog ) ) + 2;
        } else {
            izV = ( int ) ( 253.0 * ( value - minZlog ) / ( maxZlog - minZlog ) ) + 2;
        }
        izV = Math.min ( izV, 255 );
        izV = Math.max ( izV, 2 );
        this.minZ = minZtmp;
        this.maxZ = maxZtmp;
        return this.getColor ( izV );
    }
    public double getMaxZ() {
        return this.maxZ;
    }
    public double getMinZ() {
        return this.minZ;
    }
    public Paint getPaint ( final double value ) {
        if ( this.isLogscale() ) {
            return this.getColorLog ( value );
        }
        return this.getColorLinear ( value );
    }
    public String getPaletteName() {
        return this.paletteName;
    }
    public double[] getTickValues() {
        return this.tickValues;
    }
    public abstract void initialize();
    public void invertPalette() {
        final int[] red = new int[256];
        final int[] green = new int[256];
        final int[] blue = new int[256];
        for ( int i = 0; i < 256; ++i ) {
            red[i] = this.r[i];
            green[i] = this.g[i];
            blue[i] = this.b[i];
        }
        for ( int i = 2; i < 256; ++i ) {
            this.r[i] = red[257 - i];
            this.g[i] = green[257 - i];
            this.b[i] = blue[257 - i];
        }
    }
    public boolean isInverse() {
        return this.inverse;
    }
    public boolean isLogscale() {
        return this.logscale;
    }
    public boolean isStepped() {
        return this.stepped;
    }
    public void setInverse ( final boolean inverse ) {
        this.inverse = inverse;
        this.initialize();
        if ( inverse ) {
            this.invertPalette();
        }
    }
    public void setLogscale ( final boolean logscale ) {
        this.logscale = logscale;
    }
    public void setMaxZ ( final double newMaxZ ) {
        this.maxZ = newMaxZ;
    }
    public void setMinZ ( final double newMinZ ) {
        this.minZ = newMinZ;
    }
    public void setPaletteName ( final String paletteName ) {
        this.paletteName = paletteName;
    }
    public void setStepped ( final boolean stepped ) {
        this.stepped = stepped;
    }
    public void setTickValues ( final double[] newTickValues ) {
        this.tickValues = newTickValues;
    }
    public void setTickValues ( final List ticks ) {
        this.tickValues = new double[ticks.size()];
        for ( int i = 0; i < this.tickValues.length; ++i ) {
            this.tickValues[i] = ticks.get ( i ).getValue();
        }
    }
    @Override
    public boolean equals ( final Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( ! ( o instanceof ColorPalette ) ) {
            return false;
        }
        final ColorPalette colorPalette = ( ColorPalette ) o;
        if ( this.inverse != colorPalette.inverse ) {
            return false;
        }
        if ( this.logscale != colorPalette.logscale ) {
            return false;
        }
        if ( this.maxZ != colorPalette.maxZ ) {
            return false;
        }
        if ( this.minZ != colorPalette.minZ ) {
            return false;
        }
        if ( this.stepped != colorPalette.stepped ) {
            return false;
        }
        if ( !Arrays.equals ( this.b, colorPalette.b ) ) {
            return false;
        }
        if ( !Arrays.equals ( this.g, colorPalette.g ) ) {
            return false;
        }
        if ( this.paletteName != null ) {
            if ( this.paletteName.equals ( colorPalette.paletteName ) ) {
                return Arrays.equals ( this.r, colorPalette.r ) && Arrays.equals ( this.tickValues, colorPalette.tickValues );
            }
        } else if ( colorPalette.paletteName == null ) {
            return Arrays.equals ( this.r, colorPalette.r ) && Arrays.equals ( this.tickValues, colorPalette.tickValues );
        }
        return false;
    }
    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits ( this.minZ );
        int result = ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.maxZ );
        result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        result = 29 * result + ( this.logscale ? 1 : 0 );
        result = 29 * result + ( this.inverse ? 1 : 0 );
        result = 29 * result + ( ( this.paletteName != null ) ? this.paletteName.hashCode() : 0 );
        result = 29 * result + ( this.stepped ? 1 : 0 );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        final ColorPalette clone = ( ColorPalette ) super.clone();
        return clone;
    }
    static {
        log10 = Math.log ( 10.0 );
    }
}
