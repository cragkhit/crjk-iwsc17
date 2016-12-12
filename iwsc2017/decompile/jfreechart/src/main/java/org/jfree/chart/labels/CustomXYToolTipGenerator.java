package org.jfree.chart.labels;
import java.util.Collection;
import org.jfree.data.xy.XYDataset;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class CustomXYToolTipGenerator implements XYToolTipGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 8636030004670141362L;
    private List toolTipSeries;
    public CustomXYToolTipGenerator() {
        this.toolTipSeries = new ArrayList();
    }
    public int getListCount() {
        return this.toolTipSeries.size();
    }
    public int getToolTipCount ( final int list ) {
        int result = 0;
        final List tooltips = this.toolTipSeries.get ( list );
        if ( tooltips != null ) {
            result = tooltips.size();
        }
        return result;
    }
    public String getToolTipText ( final int series, final int item ) {
        String result = null;
        if ( series < this.getListCount() ) {
            final List tooltips = this.toolTipSeries.get ( series );
            if ( tooltips != null && item < tooltips.size() ) {
                result = tooltips.get ( item );
            }
        }
        return result;
    }
    public void addToolTipSeries ( final List toolTips ) {
        this.toolTipSeries.add ( toolTips );
    }
    @Override
    public String generateToolTip ( final XYDataset data, final int series, final int item ) {
        return this.getToolTipText ( series, item );
    }
    public Object clone() throws CloneNotSupportedException {
        final CustomXYToolTipGenerator clone = ( CustomXYToolTipGenerator ) super.clone();
        if ( this.toolTipSeries != null ) {
            clone.toolTipSeries = new ArrayList ( this.toolTipSeries );
        }
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( obj instanceof CustomXYToolTipGenerator ) {
            final CustomXYToolTipGenerator generator = ( CustomXYToolTipGenerator ) obj;
            boolean result = true;
            for ( int series = 0; series < this.getListCount(); ++series ) {
                for ( int item = 0; item < this.getToolTipCount ( series ); ++item ) {
                    final String t1 = this.getToolTipText ( series, item );
                    final String t2 = generator.getToolTipText ( series, item );
                    if ( t1 != null ) {
                        result = ( result && t1.equals ( t2 ) );
                    } else {
                        result = ( result && t2 == null );
                    }
                }
            }
            return result;
        }
        return false;
    }
}
