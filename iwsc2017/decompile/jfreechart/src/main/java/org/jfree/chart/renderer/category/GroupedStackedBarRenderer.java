package org.jfree.chart.renderer.category;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.ValueAxis;
import java.awt.Graphics2D;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.KeyToGroupMap;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class GroupedStackedBarRenderer extends StackedBarRenderer implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -2725921399005922939L;
    private KeyToGroupMap seriesToGroupMap;
    public GroupedStackedBarRenderer() {
        this.seriesToGroupMap = new KeyToGroupMap();
    }
    public void setSeriesToGroupMap ( final KeyToGroupMap map ) {
        ParamChecks.nullNotPermitted ( map, "map" );
        this.seriesToGroupMap = map;
        this.fireChangeEvent();
    }
    @Override
    public Range findRangeBounds ( final CategoryDataset dataset ) {
        if ( dataset == null ) {
            return null;
        }
        final Range r = DatasetUtilities.findStackedRangeBounds ( dataset, this.seriesToGroupMap );
        return r;
    }
    @Override
    protected void calculateBarWidth ( final CategoryPlot plot, final Rectangle2D dataArea, final int rendererIndex, final CategoryItemRendererState state ) {
        final CategoryAxis xAxis = plot.getDomainAxisForDataset ( rendererIndex );
        final CategoryDataset data = plot.getDataset ( rendererIndex );
        if ( data != null ) {
            final PlotOrientation orientation = plot.getOrientation();
            double space = 0.0;
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                space = dataArea.getHeight();
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                space = dataArea.getWidth();
            }
            final double maxWidth = space * this.getMaximumBarWidth();
            final int groups = this.seriesToGroupMap.getGroupCount();
            final int categories = data.getColumnCount();
            final int columns = groups * categories;
            double categoryMargin = 0.0;
            double itemMargin = 0.0;
            if ( categories > 1 ) {
                categoryMargin = xAxis.getCategoryMargin();
            }
            if ( groups > 1 ) {
                itemMargin = this.getItemMargin();
            }
            final double used = space * ( 1.0 - xAxis.getLowerMargin() - xAxis.getUpperMargin() - categoryMargin - itemMargin );
            if ( columns > 0 ) {
                state.setBarWidth ( Math.min ( used / columns, maxWidth ) );
            } else {
                state.setBarWidth ( Math.min ( used, maxWidth ) );
            }
        }
    }
    protected double calculateBarW0 ( final CategoryPlot plot, final PlotOrientation orientation, final Rectangle2D dataArea, final CategoryAxis domainAxis, final CategoryItemRendererState state, final int row, final int column ) {
        double space;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            space = dataArea.getHeight();
        } else {
            space = dataArea.getWidth();
        }
        double barW0 = domainAxis.getCategoryStart ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
        final int groupCount = this.seriesToGroupMap.getGroupCount();
        final int groupIndex = this.seriesToGroupMap.getGroupIndex ( this.seriesToGroupMap.getGroup ( plot.getDataset ( plot.getIndexOf ( this ) ).getRowKey ( row ) ) );
        final int categoryCount = this.getColumnCount();
        if ( groupCount > 1 ) {
            final double groupGap = space * this.getItemMargin() / ( categoryCount * ( groupCount - 1 ) );
            final double groupW = this.calculateSeriesWidth ( space, domainAxis, categoryCount, groupCount );
            barW0 = barW0 + groupIndex * ( groupW + groupGap ) + groupW / 2.0 - state.getBarWidth() / 2.0;
        } else {
            barW0 = domainAxis.getCategoryMiddle ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() ) - state.getBarWidth() / 2.0;
        }
        return barW0;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        final Number dataValue = dataset.getValue ( row, column );
        if ( dataValue == null ) {
            return;
        }
        final double value = dataValue.doubleValue();
        final Comparable group = this.seriesToGroupMap.getGroup ( dataset.getRowKey ( row ) );
        final PlotOrientation orientation = plot.getOrientation();
        final double barW0 = this.calculateBarW0 ( plot, orientation, dataArea, domainAxis, state, row, column );
        double positiveBase = 0.0;
        double negativeBase = 0.0;
        for ( int i = 0; i < row; ++i ) {
            if ( group.equals ( this.seriesToGroupMap.getGroup ( dataset.getRowKey ( i ) ) ) ) {
                final Number v = dataset.getValue ( i, column );
                if ( v != null ) {
                    final double d = v.doubleValue();
                    if ( d > 0.0 ) {
                        positiveBase += d;
                    } else {
                        negativeBase += d;
                    }
                }
            }
        }
        final boolean positive = value > 0.0;
        final boolean inverted = rangeAxis.isInverted();
        RectangleEdge barBase;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            if ( ( positive && inverted ) || ( !positive && !inverted ) ) {
                barBase = RectangleEdge.RIGHT;
            } else {
                barBase = RectangleEdge.LEFT;
            }
        } else if ( ( positive && !inverted ) || ( !positive && inverted ) ) {
            barBase = RectangleEdge.BOTTOM;
        } else {
            barBase = RectangleEdge.TOP;
        }
        final RectangleEdge location = plot.getRangeAxisEdge();
        double translatedBase;
        double translatedValue;
        if ( value > 0.0 ) {
            translatedBase = rangeAxis.valueToJava2D ( positiveBase, dataArea, location );
            translatedValue = rangeAxis.valueToJava2D ( positiveBase + value, dataArea, location );
        } else {
            translatedBase = rangeAxis.valueToJava2D ( negativeBase, dataArea, location );
            translatedValue = rangeAxis.valueToJava2D ( negativeBase + value, dataArea, location );
        }
        final double barL0 = Math.min ( translatedBase, translatedValue );
        final double barLength = Math.max ( Math.abs ( translatedValue - translatedBase ), this.getMinimumBarLength() );
        Rectangle2D bar;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            bar = new Rectangle2D.Double ( barL0, barW0, barLength, state.getBarWidth() );
        } else {
            bar = new Rectangle2D.Double ( barW0, barL0, state.getBarWidth(), barLength );
        }
        this.getBarPainter().paintBar ( g2, this, row, column, bar, barBase );
        final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( row, column );
        if ( generator != null && this.isItemLabelVisible ( row, column ) ) {
            this.drawItemLabel ( g2, dataset, row, column, plot, generator, bar, value < 0.0 );
        }
        if ( state.getInfo() != null ) {
            final EntityCollection entities = state.getEntityCollection();
            if ( entities != null ) {
                this.addItemEntity ( entities, dataset, row, column, bar );
            }
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof GroupedStackedBarRenderer ) ) {
            return false;
        }
        final GroupedStackedBarRenderer that = ( GroupedStackedBarRenderer ) obj;
        return this.seriesToGroupMap.equals ( that.seriesToGroupMap ) && super.equals ( obj );
    }
}
