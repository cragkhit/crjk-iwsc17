package org.jfree.data.gantt;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.time.SimpleTimePeriod;
import java.util.Date;
import java.util.ArrayList;
import org.jfree.chart.util.ParamChecks;
import java.util.List;
import org.jfree.data.time.TimePeriod;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class Task implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 1094303785346988894L;
    private String description;
    private TimePeriod duration;
    private Double percentComplete;
    private List subtasks;
    public Task ( final String description, final TimePeriod duration ) {
        ParamChecks.nullNotPermitted ( description, "description" );
        this.description = description;
        this.duration = duration;
        this.percentComplete = null;
        this.subtasks = new ArrayList();
    }
    public Task ( final String description, final Date start, final Date end ) {
        this ( description, new SimpleTimePeriod ( start, end ) );
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription ( final String description ) {
        ParamChecks.nullNotPermitted ( description, "description" );
        this.description = description;
    }
    public TimePeriod getDuration() {
        return this.duration;
    }
    public void setDuration ( final TimePeriod duration ) {
        this.duration = duration;
    }
    public Double getPercentComplete() {
        return this.percentComplete;
    }
    public void setPercentComplete ( final Double percent ) {
        this.percentComplete = percent;
    }
    public void setPercentComplete ( final double percent ) {
        this.setPercentComplete ( new Double ( percent ) );
    }
    public void addSubtask ( final Task subtask ) {
        ParamChecks.nullNotPermitted ( subtask, "subtask" );
        this.subtasks.add ( subtask );
    }
    public void removeSubtask ( final Task subtask ) {
        this.subtasks.remove ( subtask );
    }
    public int getSubtaskCount() {
        return this.subtasks.size();
    }
    public Task getSubtask ( final int index ) {
        return this.subtasks.get ( index );
    }
    @Override
    public boolean equals ( final Object object ) {
        if ( object == this ) {
            return true;
        }
        if ( ! ( object instanceof Task ) ) {
            return false;
        }
        final Task that = ( Task ) object;
        return ObjectUtilities.equal ( ( Object ) this.description, ( Object ) that.description ) && ObjectUtilities.equal ( ( Object ) this.duration, ( Object ) that.duration ) && ObjectUtilities.equal ( ( Object ) this.percentComplete, ( Object ) that.percentComplete ) && ObjectUtilities.equal ( ( Object ) this.subtasks, ( Object ) that.subtasks );
    }
    public Object clone() throws CloneNotSupportedException {
        final Task clone = ( Task ) super.clone();
        return clone;
    }
}
