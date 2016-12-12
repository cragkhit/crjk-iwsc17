package org.jfree.chart.labels;
import java.text.MessageFormat;
import org.jfree.chart.plot.Crosshair;
import java.text.NumberFormat;
import java.io.Serializable;
public class StandardCrosshairLabelGenerator implements CrosshairLabelGenerator, Serializable {
    private String labelTemplate;
    private NumberFormat numberFormat;
    public StandardCrosshairLabelGenerator() {
        this ( "{0}", NumberFormat.getNumberInstance() );
    }
    public StandardCrosshairLabelGenerator ( final String labelTemplate, final NumberFormat numberFormat ) {
        if ( labelTemplate == null ) {
            throw new IllegalArgumentException ( "Null 'labelTemplate' argument." );
        }
        if ( numberFormat == null ) {
            throw new IllegalArgumentException ( "Null 'numberFormat' argument." );
        }
        this.labelTemplate = labelTemplate;
        this.numberFormat = numberFormat;
    }
    public String getLabelTemplate() {
        return this.labelTemplate;
    }
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }
    @Override
    public String generateLabel ( final Crosshair crosshair ) {
        final Object[] v = { this.numberFormat.format ( crosshair.getValue() ) };
        final String result = MessageFormat.format ( this.labelTemplate, v );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardCrosshairLabelGenerator ) ) {
            return false;
        }
        final StandardCrosshairLabelGenerator that = ( StandardCrosshairLabelGenerator ) obj;
        return this.labelTemplate.equals ( that.labelTemplate ) && this.numberFormat.equals ( that.numberFormat );
    }
    @Override
    public int hashCode() {
        return this.labelTemplate.hashCode();
    }
}
