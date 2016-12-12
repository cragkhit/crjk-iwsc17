package org.jfree.chart.entity;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import java.awt.Shape;
import org.jfree.data.general.PieDataset;
import java.io.Serializable;
public class PieSectionEntity extends ChartEntity implements Serializable {
    private static final long serialVersionUID = 9199892576531984162L;
    private PieDataset dataset;
    private int pieIndex;
    private int sectionIndex;
    private Comparable sectionKey;
    public PieSectionEntity ( final Shape area, final PieDataset dataset, final int pieIndex, final int sectionIndex, final Comparable sectionKey, final String toolTipText, final String urlText ) {
        super ( area, toolTipText, urlText );
        this.dataset = dataset;
        this.pieIndex = pieIndex;
        this.sectionIndex = sectionIndex;
        this.sectionKey = sectionKey;
    }
    public PieDataset getDataset() {
        return this.dataset;
    }
    public void setDataset ( final PieDataset dataset ) {
        this.dataset = dataset;
    }
    public int getPieIndex() {
        return this.pieIndex;
    }
    public void setPieIndex ( final int index ) {
        this.pieIndex = index;
    }
    public int getSectionIndex() {
        return this.sectionIndex;
    }
    public void setSectionIndex ( final int index ) {
        this.sectionIndex = index;
    }
    public Comparable getSectionKey() {
        return this.sectionKey;
    }
    public void setSectionKey ( final Comparable key ) {
        this.sectionKey = key;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof PieSectionEntity ) ) {
            return false;
        }
        final PieSectionEntity that = ( PieSectionEntity ) obj;
        return ObjectUtilities.equal ( ( Object ) this.dataset, ( Object ) that.dataset ) && this.pieIndex == that.pieIndex && this.sectionIndex == that.sectionIndex && ObjectUtilities.equal ( ( Object ) this.sectionKey, ( Object ) that.sectionKey ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = HashUtilities.hashCode ( result, this.pieIndex );
        result = HashUtilities.hashCode ( result, this.sectionIndex );
        return result;
    }
    @Override
    public String toString() {
        return "PieSection: " + this.pieIndex + ", " + this.sectionIndex + "(" + this.sectionKey.toString() + ")";
    }
}
