package org.jfree.chart.title;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import java.awt.Shape;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.block.EntityBlockParams;
import org.jfree.chart.block.BlockResult;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.block.Arrangement;
import org.jfree.data.general.Dataset;
import org.jfree.chart.block.BlockContainer;
public class LegendItemBlockContainer extends BlockContainer {
    private Dataset dataset;
    private Comparable seriesKey;
    private int datasetIndex;
    private int series;
    private String toolTipText;
    private String urlText;
    public LegendItemBlockContainer ( final Arrangement arrangement, final int datasetIndex, final int series ) {
        super ( arrangement );
        this.datasetIndex = datasetIndex;
        this.series = series;
    }
    public LegendItemBlockContainer ( final Arrangement arrangement, final Dataset dataset, final Comparable seriesKey ) {
        super ( arrangement );
        this.dataset = dataset;
        this.seriesKey = seriesKey;
    }
    public Dataset getDataset() {
        return this.dataset;
    }
    public Comparable getSeriesKey() {
        return this.seriesKey;
    }
    public int getDatasetIndex() {
        return this.datasetIndex;
    }
    public int getSeriesIndex() {
        return this.series;
    }
    public String getToolTipText() {
        return this.toolTipText;
    }
    public void setToolTipText ( final String text ) {
        this.toolTipText = text;
    }
    public String getURLText() {
        return this.urlText;
    }
    public void setURLText ( final String text ) {
        this.urlText = text;
    }
    @Override
    public Object draw ( final Graphics2D g2, final Rectangle2D area, final Object params ) {
        super.draw ( g2, area, null );
        final BlockResult r = new BlockResult();
        if ( params instanceof EntityBlockParams ) {
            final EntityBlockParams ebp = ( EntityBlockParams ) params;
            if ( ebp.getGenerateEntities() ) {
                final EntityCollection ec = new StandardEntityCollection();
                final LegendItemEntity entity = new LegendItemEntity ( ( Shape ) area.clone() );
                entity.setSeriesIndex ( this.series );
                entity.setSeriesKey ( this.seriesKey );
                entity.setDataset ( this.dataset );
                entity.setToolTipText ( this.getToolTipText() );
                entity.setURLText ( this.getURLText() );
                ec.add ( entity );
                r.setEntityCollection ( ec );
            }
        }
        return r;
    }
}
