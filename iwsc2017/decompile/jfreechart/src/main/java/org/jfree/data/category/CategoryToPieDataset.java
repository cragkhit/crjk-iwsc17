package org.jfree.data.category;
import org.jfree.data.general.DatasetChangeEvent;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.util.ParamChecks;
import org.jfree.util.TableOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.PieDataset;
import org.jfree.data.general.AbstractDataset;
public class CategoryToPieDataset extends AbstractDataset implements PieDataset, DatasetChangeListener {
    static final long serialVersionUID = 5516396319762189617L;
    private CategoryDataset source;
    private TableOrder extract;
    private int index;
    public CategoryToPieDataset ( final CategoryDataset source, final TableOrder extract, final int index ) {
        ParamChecks.nullNotPermitted ( extract, "extract" );
        this.source = source;
        if ( this.source != null ) {
            this.source.addChangeListener ( this );
        }
        this.extract = extract;
        this.index = index;
    }
    public CategoryDataset getUnderlyingDataset() {
        return this.source;
    }
    public TableOrder getExtractType() {
        return this.extract;
    }
    public int getExtractIndex() {
        return this.index;
    }
    @Override
    public int getItemCount() {
        int result = 0;
        if ( this.source != null ) {
            if ( this.extract == TableOrder.BY_ROW ) {
                result = this.source.getColumnCount();
            } else if ( this.extract == TableOrder.BY_COLUMN ) {
                result = this.source.getRowCount();
            }
        }
        return result;
    }
    @Override
    public Number getValue ( final int item ) {
        Number result = null;
        if ( item < 0 || item >= this.getItemCount() ) {
            throw new IndexOutOfBoundsException ( "The 'item' index is out of bounds." );
        }
        if ( this.extract == TableOrder.BY_ROW ) {
            result = this.source.getValue ( this.index, item );
        } else if ( this.extract == TableOrder.BY_COLUMN ) {
            result = this.source.getValue ( item, this.index );
        }
        return result;
    }
    @Override
    public Comparable getKey ( final int index ) {
        Comparable result = null;
        if ( index < 0 || index >= this.getItemCount() ) {
            throw new IndexOutOfBoundsException ( "Invalid 'index': " + index );
        }
        if ( this.extract == TableOrder.BY_ROW ) {
            result = this.source.getColumnKey ( index );
        } else if ( this.extract == TableOrder.BY_COLUMN ) {
            result = this.source.getRowKey ( index );
        }
        return result;
    }
    @Override
    public int getIndex ( final Comparable key ) {
        int result = -1;
        if ( this.source != null ) {
            if ( this.extract == TableOrder.BY_ROW ) {
                result = this.source.getColumnIndex ( key );
            } else if ( this.extract == TableOrder.BY_COLUMN ) {
                result = this.source.getRowIndex ( key );
            }
        }
        return result;
    }
    @Override
    public List getKeys() {
        List result = Collections.EMPTY_LIST;
        if ( this.source != null ) {
            if ( this.extract == TableOrder.BY_ROW ) {
                result = this.source.getColumnKeys();
            } else if ( this.extract == TableOrder.BY_COLUMN ) {
                result = this.source.getRowKeys();
            }
        }
        return result;
    }
    @Override
    public Number getValue ( final Comparable key ) {
        Number result = null;
        final int keyIndex = this.getIndex ( key );
        if ( keyIndex != -1 ) {
            if ( this.extract == TableOrder.BY_ROW ) {
                result = this.source.getValue ( this.index, keyIndex );
            } else if ( this.extract == TableOrder.BY_COLUMN ) {
                result = this.source.getValue ( keyIndex, this.index );
            }
        }
        return result;
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
        if ( ! ( obj instanceof PieDataset ) ) {
            return false;
        }
        final PieDataset that = ( PieDataset ) obj;
        final int count = this.getItemCount();
        if ( that.getItemCount() != count ) {
            return false;
        }
        for ( int i = 0; i < count; ++i ) {
            final Comparable k1 = this.getKey ( i );
            final Comparable k2 = that.getKey ( i );
            if ( !k1.equals ( k2 ) ) {
                return false;
            }
            final Number v1 = this.getValue ( i );
            final Number v2 = that.getValue ( i );
            if ( v1 == null ) {
                if ( v2 != null ) {
                    return false;
                }
            } else if ( !v1.equals ( v2 ) ) {
                return false;
            }
        }
        return true;
    }
}
