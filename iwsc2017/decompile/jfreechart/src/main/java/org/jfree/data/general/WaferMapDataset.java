package org.jfree.data.general;
import java.util.TreeSet;
import java.util.Set;
import org.jfree.data.DefaultKeyedValues2D;
public class WaferMapDataset extends AbstractDataset {
    private DefaultKeyedValues2D data;
    private int maxChipX;
    private int maxChipY;
    private double chipSpace;
    private Double maxValue;
    private Double minValue;
    private static final double DEFAULT_CHIP_SPACE = 1.0;
    public WaferMapDataset ( final int maxChipX, final int maxChipY ) {
        this ( maxChipX, maxChipY, null );
    }
    public WaferMapDataset ( final int maxChipX, final int maxChipY, final Number chipSpace ) {
        this.maxValue = new Double ( Double.NEGATIVE_INFINITY );
        this.minValue = new Double ( Double.POSITIVE_INFINITY );
        this.data = new DefaultKeyedValues2D();
        this.maxChipX = maxChipX;
        this.maxChipY = maxChipY;
        if ( chipSpace == null ) {
            this.chipSpace = 1.0;
        } else {
            this.chipSpace = chipSpace.doubleValue();
        }
    }
    public void addValue ( final Number value, final Comparable chipx, final Comparable chipy ) {
        this.setValue ( value, chipx, chipy );
    }
    public void addValue ( final int v, final int x, final int y ) {
        this.setValue ( new Double ( v ), new Integer ( x ), new Integer ( y ) );
    }
    public void setValue ( final Number value, final Comparable chipx, final Comparable chipy ) {
        this.data.setValue ( value, chipx, chipy );
        if ( this.isMaxValue ( value ) ) {
            this.maxValue = ( Double ) value;
        }
        if ( this.isMinValue ( value ) ) {
            this.minValue = ( Double ) value;
        }
    }
    public int getUniqueValueCount() {
        return this.getUniqueValues().size();
    }
    public Set getUniqueValues() {
        final Set unique = new TreeSet();
        for ( int r = 0; r < this.data.getRowCount(); ++r ) {
            for ( int c = 0; c < this.data.getColumnCount(); ++c ) {
                final Number value = this.data.getValue ( r, c );
                if ( value != null ) {
                    unique.add ( value );
                }
            }
        }
        return unique;
    }
    public Number getChipValue ( final int chipx, final int chipy ) {
        return this.getChipValue ( new Integer ( chipx ), new Integer ( chipy ) );
    }
    public Number getChipValue ( final Comparable chipx, final Comparable chipy ) {
        final int rowIndex = this.data.getRowIndex ( chipx );
        if ( rowIndex < 0 ) {
            return null;
        }
        final int colIndex = this.data.getColumnIndex ( chipy );
        if ( colIndex < 0 ) {
            return null;
        }
        return this.data.getValue ( rowIndex, colIndex );
    }
    public boolean isMaxValue ( final Number check ) {
        return check.doubleValue() > this.maxValue;
    }
    public boolean isMinValue ( final Number check ) {
        return check.doubleValue() < this.minValue;
    }
    public Number getMaxValue() {
        return this.maxValue;
    }
    public Number getMinValue() {
        return this.minValue;
    }
    public int getMaxChipX() {
        return this.maxChipX;
    }
    public void setMaxChipX ( final int maxChipX ) {
        this.maxChipX = maxChipX;
    }
    public int getMaxChipY() {
        return this.maxChipY;
    }
    public void setMaxChipY ( final int maxChipY ) {
        this.maxChipY = maxChipY;
    }
    public double getChipSpace() {
        return this.chipSpace;
    }
    public void setChipSpace ( final double space ) {
        this.chipSpace = space;
    }
}
