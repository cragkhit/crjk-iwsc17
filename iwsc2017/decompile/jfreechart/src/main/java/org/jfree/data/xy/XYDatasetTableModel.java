package org.jfree.data.xy;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
public class XYDatasetTableModel extends AbstractTableModel implements TableModel, DatasetChangeListener {
    TableXYDataset model;
    public XYDatasetTableModel() {
        this.model = null;
    }
    public XYDatasetTableModel ( final TableXYDataset dataset ) {
        this();
        ( this.model = dataset ).addChangeListener ( this );
    }
    public void setModel ( final TableXYDataset dataset ) {
        ( this.model = dataset ).addChangeListener ( this );
        this.fireTableDataChanged();
    }
    @Override
    public int getRowCount() {
        if ( this.model == null ) {
            return 0;
        }
        return this.model.getItemCount();
    }
    @Override
    public int getColumnCount() {
        if ( this.model == null ) {
            return 0;
        }
        return this.model.getSeriesCount() + 1;
    }
    @Override
    public String getColumnName ( final int column ) {
        if ( this.model == null ) {
            return super.getColumnName ( column );
        }
        if ( column < 1 ) {
            return "X Value";
        }
        return this.model.getSeriesKey ( column - 1 ).toString();
    }
    @Override
    public Object getValueAt ( final int row, final int column ) {
        if ( this.model == null ) {
            return null;
        }
        if ( column < 1 ) {
            return this.model.getX ( 0, row );
        }
        return this.model.getY ( column - 1, row );
    }
    @Override
    public void datasetChanged ( final DatasetChangeEvent event ) {
        this.fireTableDataChanged();
    }
    @Override
    public boolean isCellEditable ( final int row, final int column ) {
        return false;
    }
    @Override
    public void setValueAt ( final Object value, final int row, final int column ) {
        if ( this.isCellEditable ( row, column ) ) {
            return;
        }
    }
}
