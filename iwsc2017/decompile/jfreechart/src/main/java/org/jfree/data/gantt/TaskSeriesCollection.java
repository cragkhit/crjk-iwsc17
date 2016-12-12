package org.jfree.data.gantt;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.time.TimePeriod;
import java.util.Iterator;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
import org.jfree.data.general.AbstractSeriesDataset;
public class TaskSeriesCollection extends AbstractSeriesDataset implements GanttCategoryDataset, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -2065799050738449903L;
    private List keys;
    private List data;
    public TaskSeriesCollection() {
        this.keys = new ArrayList();
        this.data = new ArrayList();
    }
    public TaskSeries getSeries ( final Comparable key ) {
        if ( key == null ) {
            throw new NullPointerException ( "Null 'key' argument." );
        }
        TaskSeries result = null;
        final int index = this.getRowIndex ( key );
        if ( index >= 0 ) {
            result = this.getSeries ( index );
        }
        return result;
    }
    public TaskSeries getSeries ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        return this.data.get ( series );
    }
    @Override
    public int getSeriesCount() {
        return this.getRowCount();
    }
    @Override
    public Comparable getSeriesKey ( final int series ) {
        final TaskSeries ts = this.data.get ( series );
        return ts.getKey();
    }
    public int getRowCount() {
        return this.data.size();
    }
    public List getRowKeys() {
        return this.data;
    }
    public int getColumnCount() {
        return this.keys.size();
    }
    public List getColumnKeys() {
        return this.keys;
    }
    public Comparable getColumnKey ( final int index ) {
        return this.keys.get ( index );
    }
    public int getColumnIndex ( final Comparable columnKey ) {
        ParamChecks.nullNotPermitted ( columnKey, "columnKey" );
        return this.keys.indexOf ( columnKey );
    }
    public int getRowIndex ( final Comparable rowKey ) {
        int result = -1;
        for ( int count = this.data.size(), i = 0; i < count; ++i ) {
            final TaskSeries s = this.data.get ( i );
            if ( s.getKey().equals ( rowKey ) ) {
                result = i;
                break;
            }
        }
        return result;
    }
    public Comparable getRowKey ( final int index ) {
        final TaskSeries series = this.data.get ( index );
        return series.getKey();
    }
    public void add ( final TaskSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.add ( series );
        series.addChangeListener ( this );
        for ( final Task task : series.getTasks() ) {
            final String key = task.getDescription();
            final int index = this.keys.indexOf ( key );
            if ( index < 0 ) {
                this.keys.add ( key );
            }
        }
        this.fireDatasetChanged();
    }
    public void remove ( final TaskSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.data.contains ( series ) ) {
            series.removeChangeListener ( this );
            this.data.remove ( series );
            this.fireDatasetChanged();
        }
    }
    public void remove ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "TaskSeriesCollection.remove(): index outside valid range." );
        }
        final TaskSeries ts = this.data.get ( series );
        ts.removeChangeListener ( this );
        this.data.remove ( series );
        this.fireDatasetChanged();
    }
    public void removeAll() {
        for ( final TaskSeries series : this.data ) {
            series.removeChangeListener ( this );
        }
        this.data.clear();
        this.fireDatasetChanged();
    }
    public Number getValue ( final Comparable rowKey, final Comparable columnKey ) {
        return this.getStartValue ( rowKey, columnKey );
    }
    public Number getValue ( final int row, final int column ) {
        return this.getStartValue ( row, column );
    }
    public Number getStartValue ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final int row = this.getRowIndex ( rowKey );
        final TaskSeries series = this.data.get ( row );
        final Task task = series.get ( columnKey.toString() );
        if ( task != null ) {
            final TimePeriod duration = task.getDuration();
            if ( duration != null ) {
                result = new Long ( duration.getStart().getTime() );
            }
        }
        return result;
    }
    public Number getStartValue ( final int row, final int column ) {
        final Comparable rowKey = this.getRowKey ( row );
        final Comparable columnKey = this.getColumnKey ( column );
        return this.getStartValue ( rowKey, columnKey );
    }
    public Number getEndValue ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final int row = this.getRowIndex ( rowKey );
        final TaskSeries series = this.data.get ( row );
        final Task task = series.get ( columnKey.toString() );
        if ( task != null ) {
            final TimePeriod duration = task.getDuration();
            if ( duration != null ) {
                result = new Long ( duration.getEnd().getTime() );
            }
        }
        return result;
    }
    public Number getEndValue ( final int row, final int column ) {
        final Comparable rowKey = this.getRowKey ( row );
        final Comparable columnKey = this.getColumnKey ( column );
        return this.getEndValue ( rowKey, columnKey );
    }
    @Override
    public Number getPercentComplete ( final int row, final int column ) {
        final Comparable rowKey = this.getRowKey ( row );
        final Comparable columnKey = this.getColumnKey ( column );
        return this.getPercentComplete ( rowKey, columnKey );
    }
    @Override
    public Number getPercentComplete ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final int row = this.getRowIndex ( rowKey );
        final TaskSeries series = this.data.get ( row );
        final Task task = series.get ( columnKey.toString() );
        if ( task != null ) {
            result = task.getPercentComplete();
        }
        return result;
    }
    @Override
    public int getSubIntervalCount ( final int row, final int column ) {
        final Comparable rowKey = this.getRowKey ( row );
        final Comparable columnKey = this.getColumnKey ( column );
        return this.getSubIntervalCount ( rowKey, columnKey );
    }
    @Override
    public int getSubIntervalCount ( final Comparable rowKey, final Comparable columnKey ) {
        int result = 0;
        final int row = this.getRowIndex ( rowKey );
        final TaskSeries series = this.data.get ( row );
        final Task task = series.get ( columnKey.toString() );
        if ( task != null ) {
            result = task.getSubtaskCount();
        }
        return result;
    }
    @Override
    public Number getStartValue ( final int row, final int column, final int subinterval ) {
        final Comparable rowKey = this.getRowKey ( row );
        final Comparable columnKey = this.getColumnKey ( column );
        return this.getStartValue ( rowKey, columnKey, subinterval );
    }
    @Override
    public Number getStartValue ( final Comparable rowKey, final Comparable columnKey, final int subinterval ) {
        Number result = null;
        final int row = this.getRowIndex ( rowKey );
        final TaskSeries series = this.data.get ( row );
        final Task task = series.get ( columnKey.toString() );
        if ( task != null ) {
            final Task sub = task.getSubtask ( subinterval );
            if ( sub != null ) {
                final TimePeriod duration = sub.getDuration();
                result = new Long ( duration.getStart().getTime() );
            }
        }
        return result;
    }
    @Override
    public Number getEndValue ( final int row, final int column, final int subinterval ) {
        final Comparable rowKey = this.getRowKey ( row );
        final Comparable columnKey = this.getColumnKey ( column );
        return this.getEndValue ( rowKey, columnKey, subinterval );
    }
    @Override
    public Number getEndValue ( final Comparable rowKey, final Comparable columnKey, final int subinterval ) {
        Number result = null;
        final int row = this.getRowIndex ( rowKey );
        final TaskSeries series = this.data.get ( row );
        final Task task = series.get ( columnKey.toString() );
        if ( task != null ) {
            final Task sub = task.getSubtask ( subinterval );
            if ( sub != null ) {
                final TimePeriod duration = sub.getDuration();
                result = new Long ( duration.getEnd().getTime() );
            }
        }
        return result;
    }
    @Override
    public Number getPercentComplete ( final int row, final int column, final int subinterval ) {
        final Comparable rowKey = this.getRowKey ( row );
        final Comparable columnKey = this.getColumnKey ( column );
        return this.getPercentComplete ( rowKey, columnKey, subinterval );
    }
    @Override
    public Number getPercentComplete ( final Comparable rowKey, final Comparable columnKey, final int subinterval ) {
        Number result = null;
        final int row = this.getRowIndex ( rowKey );
        final TaskSeries series = this.data.get ( row );
        final Task task = series.get ( columnKey.toString() );
        if ( task != null ) {
            final Task sub = task.getSubtask ( subinterval );
            if ( sub != null ) {
                result = sub.getPercentComplete();
            }
        }
        return result;
    }
    @Override
    public void seriesChanged ( final SeriesChangeEvent event ) {
        this.refreshKeys();
        this.fireDatasetChanged();
    }
    private void refreshKeys() {
        this.keys.clear();
        for ( int i = 0; i < this.getSeriesCount(); ++i ) {
            final TaskSeries series = this.data.get ( i );
            for ( final Task task : series.getTasks() ) {
                final String key = task.getDescription();
                final int index = this.keys.indexOf ( key );
                if ( index < 0 ) {
                    this.keys.add ( key );
                }
            }
        }
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TaskSeriesCollection ) ) {
            return false;
        }
        final TaskSeriesCollection that = ( TaskSeriesCollection ) obj;
        return ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data );
    }
    public Object clone() throws CloneNotSupportedException {
        final TaskSeriesCollection clone = ( TaskSeriesCollection ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.data );
        clone.keys = new ArrayList ( this.keys );
        return clone;
    }
}
