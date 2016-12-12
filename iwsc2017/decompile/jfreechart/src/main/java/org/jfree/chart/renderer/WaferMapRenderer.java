package org.jfree.chart.renderer;
import java.awt.Stroke;
import java.awt.Shape;
import java.util.HashSet;
import org.jfree.chart.LegendItem;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.LegendItemCollection;
import java.util.Iterator;
import java.util.Set;
import org.jfree.data.general.WaferMapDataset;
import java.awt.Paint;
import org.jfree.chart.plot.DrawingSupplier;
import java.util.HashMap;
import org.jfree.chart.plot.WaferMapPlot;
import java.util.Map;
public class WaferMapRenderer extends AbstractRenderer {
    private Map paintIndex;
    private WaferMapPlot plot;
    private int paintLimit;
    private static final int DEFAULT_PAINT_LIMIT = 35;
    public static final int POSITION_INDEX = 0;
    public static final int VALUE_INDEX = 1;
    private int paintIndexMethod;
    public WaferMapRenderer() {
        this ( null, null );
    }
    public WaferMapRenderer ( final int paintLimit, final int paintIndexMethod ) {
        this ( new Integer ( paintLimit ), new Integer ( paintIndexMethod ) );
    }
    public WaferMapRenderer ( final Integer paintLimit, final Integer paintIndexMethod ) {
        this.paintIndex = new HashMap();
        if ( paintLimit == null ) {
            this.paintLimit = 35;
        } else {
            this.paintLimit = paintLimit;
        }
        this.paintIndexMethod = 1;
        if ( paintIndexMethod != null && this.isMethodValid ( paintIndexMethod ) ) {
            this.paintIndexMethod = paintIndexMethod;
        }
    }
    private boolean isMethodValid ( final int method ) {
        switch ( method ) {
        case 0: {
            return true;
        }
        case 1: {
            return true;
        }
        default: {
            return false;
        }
        }
    }
    @Override
    public DrawingSupplier getDrawingSupplier() {
        DrawingSupplier result = null;
        final WaferMapPlot p = this.getPlot();
        if ( p != null ) {
            result = p.getDrawingSupplier();
        }
        return result;
    }
    public WaferMapPlot getPlot() {
        return this.plot;
    }
    public void setPlot ( final WaferMapPlot plot ) {
        this.plot = plot;
        this.makePaintIndex();
    }
    public Paint getChipColor ( final Number value ) {
        return this.getSeriesPaint ( this.getPaintIndex ( value ) );
    }
    private int getPaintIndex ( final Number value ) {
        return this.paintIndex.get ( value );
    }
    private void makePaintIndex() {
        if ( this.plot == null ) {
            return;
        }
        final WaferMapDataset data = this.plot.getDataset();
        final Number dataMin = data.getMinValue();
        final Number dataMax = data.getMaxValue();
        final Set uniqueValues = data.getUniqueValues();
        if ( uniqueValues.size() <= this.paintLimit ) {
            int count = 0;
            final Iterator i = uniqueValues.iterator();
            while ( i.hasNext() ) {
                this.paintIndex.put ( i.next(), new Integer ( count++ ) );
            }
        } else {
            switch ( this.paintIndexMethod ) {
            case 0: {
                this.makePositionIndex ( uniqueValues );
                break;
            }
            case 1: {
                this.makeValueIndex ( dataMax, dataMin, uniqueValues );
                break;
            }
            }
        }
    }
    private void makePositionIndex ( final Set uniqueValues ) {
        final int valuesPerColor = ( int ) Math.ceil ( uniqueValues.size() / this.paintLimit );
        int count = 0;
        int paint = 0;
        final Iterator i = uniqueValues.iterator();
        while ( i.hasNext() ) {
            this.paintIndex.put ( i.next(), new Integer ( paint ) );
            if ( ++count % valuesPerColor == 0 ) {
                ++paint;
            }
            if ( paint > this.paintLimit ) {
                paint = this.paintLimit;
            }
        }
    }
    private void makeValueIndex ( final Number max, final Number min, final Set uniqueValues ) {
        final double valueRange = max.doubleValue() - min.doubleValue();
        final double valueStep = valueRange / this.paintLimit;
        int paint = 0;
        double cutPoint = min.doubleValue() + valueStep;
        for ( final Number value : uniqueValues ) {
            while ( value.doubleValue() > cutPoint ) {
                cutPoint += valueStep;
                if ( ++paint > this.paintLimit ) {
                    paint = this.paintLimit;
                }
            }
            this.paintIndex.put ( value, new Integer ( paint ) );
        }
    }
    public LegendItemCollection getLegendCollection() {
        final LegendItemCollection result = new LegendItemCollection();
        if ( this.paintIndex != null && this.paintIndex.size() > 0 ) {
            if ( this.paintIndex.size() <= this.paintLimit ) {
                for ( final Map.Entry entry : this.paintIndex.entrySet() ) {
                    final String description;
                    final String label = description = entry.getKey().toString();
                    final Shape shape = new Rectangle2D.Double ( 1.0, 1.0, 1.0, 1.0 );
                    final Paint paint = this.lookupSeriesPaint ( entry.getValue() );
                    final Paint outlinePaint = Color.black;
                    final Stroke outlineStroke = WaferMapRenderer.DEFAULT_STROKE;
                    result.add ( new LegendItem ( label, description, null, null, shape, paint, outlineStroke, outlinePaint ) );
                }
            } else {
                final Set unique = new HashSet();
                for ( final Map.Entry entry2 : this.paintIndex.entrySet() ) {
                    if ( unique.add ( entry2.getValue() ) ) {
                        final String description2;
                        final String label2 = description2 = this.getMinPaintValue ( entry2.getValue() ).toString() + " - " + this.getMaxPaintValue ( entry2.getValue() ).toString();
                        final Shape shape2 = new Rectangle2D.Double ( 1.0, 1.0, 1.0, 1.0 );
                        final Paint paint2 = this.getSeriesPaint ( entry2.getValue() );
                        final Paint outlinePaint2 = Color.black;
                        final Stroke outlineStroke2 = WaferMapRenderer.DEFAULT_STROKE;
                        result.add ( new LegendItem ( label2, description2, null, null, shape2, paint2, outlineStroke2, outlinePaint2 ) );
                    }
                }
            }
        }
        return result;
    }
    private Number getMinPaintValue ( final Integer index ) {
        double minValue = Double.POSITIVE_INFINITY;
        for ( final Map.Entry entry : this.paintIndex.entrySet() ) {
            if ( entry.getValue().equals ( index ) && entry.getKey().doubleValue() < minValue ) {
                minValue = entry.getKey().doubleValue();
            }
        }
        return new Double ( minValue );
    }
    private Number getMaxPaintValue ( final Integer index ) {
        double maxValue = Double.NEGATIVE_INFINITY;
        for ( final Map.Entry entry : this.paintIndex.entrySet() ) {
            if ( entry.getValue().equals ( index ) && entry.getKey().doubleValue() > maxValue ) {
                maxValue = entry.getKey().doubleValue();
            }
        }
        return new Double ( maxValue );
    }
}
