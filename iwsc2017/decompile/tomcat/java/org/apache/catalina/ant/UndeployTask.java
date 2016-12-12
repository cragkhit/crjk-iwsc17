package org.apache.catalina.ant;
import org.apache.tools.ant.BuildException;
public class UndeployTask extends AbstractCatalinaCommandTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        this.execute ( this.createQueryString ( "/undeploy" ).toString() );
    }
}
