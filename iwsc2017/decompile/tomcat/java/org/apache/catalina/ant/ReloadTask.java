package org.apache.catalina.ant;
import org.apache.tools.ant.BuildException;
public class ReloadTask extends AbstractCatalinaCommandTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        this.execute ( this.createQueryString ( "/reload" ).toString() );
    }
}
