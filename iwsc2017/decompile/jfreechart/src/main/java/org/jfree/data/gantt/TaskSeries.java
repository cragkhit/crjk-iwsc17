package org.jfree.data.gantt;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import java.util.Collections;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.general.Series;
public class TaskSeries extends Series {
    private List tasks;
    public TaskSeries ( final String name ) {
        super ( name );
        this.tasks = new ArrayList();
    }
    public void add ( final Task task ) {
        ParamChecks.nullNotPermitted ( task, "task" );
        this.tasks.add ( task );
        this.fireSeriesChanged();
    }
    public void remove ( final Task task ) {
        this.tasks.remove ( task );
        this.fireSeriesChanged();
    }
    public void removeAll() {
        this.tasks.clear();
        this.fireSeriesChanged();
    }
    @Override
    public int getItemCount() {
        return this.tasks.size();
    }
    public Task get ( final int index ) {
        return this.tasks.get ( index );
    }
    public Task get ( final String description ) {
        Task result = null;
        for ( int count = this.tasks.size(), i = 0; i < count; ++i ) {
            final Task t = this.tasks.get ( i );
            if ( t.getDescription().equals ( description ) ) {
                result = t;
                break;
            }
        }
        return result;
    }
    public List getTasks() {
        return Collections.unmodifiableList ( ( List<?> ) this.tasks );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TaskSeries ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final TaskSeries that = ( TaskSeries ) obj;
        return this.tasks.equals ( that.tasks );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final TaskSeries clone = ( TaskSeries ) super.clone();
        clone.tasks = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.tasks );
        return clone;
    }
}
