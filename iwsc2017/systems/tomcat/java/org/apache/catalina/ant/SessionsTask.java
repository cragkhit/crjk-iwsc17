package org.apache.catalina.ant;
import org.apache.tools.ant.BuildException;
public class SessionsTask extends AbstractCatalinaCommandTask {
    protected String idle = null;
    public String getIdle() {
        return this.idle;
    }
    public void setIdle ( String idle ) {
        this.idle = idle;
    }
    @Override
    public StringBuilder createQueryString ( String command ) {
        StringBuilder buffer = super.createQueryString ( command );
        if ( path != null && idle != null ) {
            buffer.append ( "&idle=" );
            buffer.append ( this.idle );
        }
        return buffer;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        execute ( createQueryString ( "/sessions" ).toString() );
    }
}
