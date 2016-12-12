package org.jfree.data.gantt;
import org.jfree.data.general.DatasetChangeEvent;
import java.util.Date;
import org.jfree.data.time.TimePeriod;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.AbstractXYDataset;
public class XYTaskDataset extends AbstractXYDataset implements IntervalXYDataset, DatasetChangeListener {
    private TaskSeriesCollection underlying;
    private double seriesWidth;
    private boolean transposed;
    public XYTaskDataset ( final TaskSeriesCollection tasks ) {
        ParamChecks.nullNotPermitted ( tasks, "tasks" );
        this.underlying = tasks;
        this.seriesWidth = 0.8;
        this.underlying.addChangeListener ( this );
    }
    public TaskSeriesCollection getTasks() {
        return this.underlying;
    }
    public double getSeriesWidth() {
        return this.seriesWidth;
    }
    public void setSeriesWidth ( final double w ) {
        if ( w <= 0.0 ) {
            throw new IllegalArgumentException ( "Requires 'w' > 0.0." );
        }
        this.seriesWidth = w;
        this.fireDatasetChanged();
    }
    public boolean isTransposed() {
        return this.transposed;
    }
    public void setTransposed ( final boolean transposed ) {
        this.transposed = transposed;
        this.fireDatasetChanged();
    }
    @Override
    public int getSeriesCount() {
        return this.underlying.getSeriesCount();
    }
    @Override
    public Comparable getSeriesKey ( final int series ) {
        return this.underlying.getSeriesKey ( series );
    }
    @Override
    public int getItemCount ( final int series ) {
        return this.underlying.getSeries ( series ).getItemCount();
    }
    @Override
    public double getXValue ( final int series, final int item ) {
        if ( !this.transposed ) {
            return this.getSeriesValue ( series );
        }
        return this.getItemValue ( series, item );
    }
    @Override
    public double getStartXValue ( final int series, final int item ) {
        if ( !this.transposed ) {
            return this.getSeriesStartValue ( series );
        }
        return this.getItemStartValue ( series, item );
    }
    @Override
    public double getEndXValue ( final int series, final int item ) {
        if ( !this.transposed ) {
            return this.getSeriesEndValue ( series );
        }
        return this.getItemEndValue ( series, item );
    }
    @Override
    public Number getX ( final int series, final int item ) {
        return new Double ( this.getXValue ( series, item ) );
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        return new Double ( this.getStartXValue ( series, item ) );
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        return new Double ( this.getEndXValue ( series, item ) );
    }
    @Override
    public double getYValue ( final int series, final int item ) {
        if ( !this.transposed ) {
            return this.getItemValue ( series, item );
        }
        return this.getSeriesValue ( series );
    }
    @Override
    public double getStartYValue ( final int series, final int item ) {
        if ( !this.transposed ) {
            return this.getItemStartValue ( series, item );
        }
        return this.getSeriesStartValue ( series );
    }
    @Override
    public double getEndYValue ( final int series, final int item ) {
        if ( !this.transposed ) {
            return this.getItemEndValue ( series, item );
        }
        return this.getSeriesEndValue ( series );
    }
    @Override
    public Number getY ( final int series, final int item ) {
        return new Double ( this.getYValue ( series, item ) );
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        return new Double ( this.getStartYValue ( series, item ) );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        return new Double ( this.getEndYValue ( series, item ) );
    }
    private double getSeriesValue ( final int series ) {
        return series;
    }
    private double getSeriesStartValue ( final int series ) {
        return series - this.seriesWidth / 2.0;
    }
    private double getSeriesEndValue ( final int series ) {
        return series + this.seriesWidth / 2.0;
    }
    private double getItemValue ( final int series, final int item ) {
        final TaskSeries s = this.underlying.getSeries ( series );
        final Task t = s.get ( item );
        final TimePeriod duration = t.getDuration();
        final Date start = duration.getStart();
        final Date end = duration.getEnd();
        return ( start.getTime() + end.getTime() ) / 2.0;
    }
    private double getItemStartValue ( final int series, final int item ) {
        final TaskSeries s = this.underlying.getSeries ( series );
        final Task t = s.get ( item );
        final TimePeriod duration = t.getDuration();
        final Date start = duration.getStart();
        return start.getTime();
    }
    private double getItemEndValue ( final int series, final int item ) {
        final TaskSeries s = this.underlying.getSeries ( series );
        final Task t = s.get ( item );
        final TimePeriod duration = t.getDuration();
        final Date end = duration.getEnd();
        return end.getTime();
    }
    @Override
    public void datasetChanged ( final DatasetChangeEvent event ) {
        this.fireDatasetChanged();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYTaskDataset ) ) {
            return false;
        }
        final XYTaskDataset that = ( XYTaskDataset ) obj;
        return this.seriesWidth == that.seriesWidth && this.transposed == that.transposed && this.underlying.equals ( that.underlying );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final XYTaskDataset clone = ( XYTaskDataset ) super.clone();
        clone.underlying = ( TaskSeriesCollection ) this.underlying.clone();
        return clone;
    }
}
