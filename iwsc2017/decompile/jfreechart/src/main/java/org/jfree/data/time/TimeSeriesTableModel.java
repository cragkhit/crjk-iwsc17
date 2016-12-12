package org.jfree.data.time;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import javax.swing.table.AbstractTableModel;
public class TimeSeriesTableModel extends AbstractTableModel implements SeriesChangeListener {
    private TimeSeries series;
    private boolean editable;
    private RegularTimePeriod newTimePeriod;
    private Number newValue;
    public TimeSeriesTableModel() {
        this ( new TimeSeries ( "Untitled" ) );
    }
    public TimeSeriesTableModel ( final TimeSeries series ) {
        this ( series, false );
    }
    public TimeSeriesTableModel ( final TimeSeries series, final boolean editable ) {
        ( this.series = series ).addChangeListener ( this );
        this.editable = editable;
    }
    @Override
    public int getColumnCount() {
        return 2;
    }
    @Override
    public Class getColumnClass ( final int column ) {
        if ( column == 0 ) {
            return String.class;
        }
        if ( column == 1 ) {
            return Double.class;
        }
        return null;
    }
    @Override
    public String getColumnName ( final int column ) {
        if ( column == 0 ) {
            return "Period:";
        }
        if ( column == 1 ) {
            return "Value:";
        }
        return null;
    }
    @Override
    public int getRowCount() {
        return this.series.getItemCount();
    }
    @Override
    public Object getValueAt ( final int row, final int column ) {
        if ( row < this.series.getItemCount() ) {
            if ( column == 0 ) {
                return this.series.getTimePeriod ( row );
            }
            if ( column == 1 ) {
                return this.series.getValue ( row );
            }
            return null;
        } else {
            if ( column == 0 ) {
                return this.newTimePeriod;
            }
            if ( column == 1 ) {
                return this.newValue;
            }
            return null;
        }
    }
    @Override
    public boolean isCellEditable ( final int row, final int column ) {
        return this.editable && ( column == 0 || column == 1 );
    }
    @Override
    public void setValueAt ( final Object value, final int row, final int column ) {
        if ( row < this.series.getItemCount() ) {
            if ( column == 1 ) {
                try {
                    final Double v = Double.valueOf ( value.toString() );
                    this.series.update ( row, v );
                } catch ( NumberFormatException nfe ) {
                    System.err.println ( "Number format exception" );
                }
            }
        } else if ( column == 0 ) {
            this.newTimePeriod = null;
        } else if ( column == 1 ) {
            this.newValue = Double.valueOf ( value.toString() );
        }
    }
    @Override
    public void seriesChanged ( final SeriesChangeEvent event ) {
        this.fireTableDataChanged();
    }
}
