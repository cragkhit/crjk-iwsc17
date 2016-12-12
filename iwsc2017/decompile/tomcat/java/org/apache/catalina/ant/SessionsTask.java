package org.apache.catalina.ant;
import org.apache.tools.ant.BuildException;
public class SessionsTask extends AbstractCatalinaCommandTask {
    protected String idle;
    public SessionsTask() {
        this.idle = null;
    }
    public String getIdle() {
        return this.idle;
    }
    public void setIdle ( final String idle ) {
        this.idle = idle;
    }
    @Override
    public StringBuilder createQueryString ( final String command ) {
        final StringBuilder buffer = super.createQueryString ( command );
        if ( this.path != null && this.idle != null ) {
            buffer.append ( "&idle=" );
            buffer.append ( this.idle );
        }
        return buffer;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        this.execute ( this.createQueryString ( "/sessions" ).toString() );
    }
}
