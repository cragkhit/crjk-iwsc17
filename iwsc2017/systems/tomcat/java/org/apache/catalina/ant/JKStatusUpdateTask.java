package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
public class JKStatusUpdateTask extends AbstractCatalinaTask {
    private String worker = "lb";
    private String workerType = "lb";
    private int internalid = 0;
    private Integer lbRetries;
    private Integer lbRecovertime;
    private Boolean lbStickySession = Boolean.TRUE;
    private Boolean lbForceSession = Boolean.FALSE;
    private Integer workerLoadFactor;
    private String workerRedirect;
    private String workerClusterDomain;
    private Boolean workerDisabled = Boolean.FALSE;
    private Boolean workerStopped = Boolean.FALSE;
    private boolean isLBMode = true;
    private String workerLb;
    public JKStatusUpdateTask() {
        super();
        setUrl ( "http://localhost/status" );
    }
    public int getInternalid() {
        return internalid;
    }
    public void setInternalid ( int internalid ) {
        this.internalid = internalid;
    }
    public Boolean getLbForceSession() {
        return lbForceSession;
    }
    public void setLbForceSession ( Boolean lbForceSession ) {
        this.lbForceSession = lbForceSession;
    }
    public Integer getLbRecovertime() {
        return lbRecovertime;
    }
    public void setLbRecovertime ( Integer lbRecovertime ) {
        this.lbRecovertime = lbRecovertime;
    }
    public Integer getLbRetries() {
        return lbRetries;
    }
    public void setLbRetries ( Integer lbRetries ) {
        this.lbRetries = lbRetries;
    }
    public Boolean getLbStickySession() {
        return lbStickySession;
    }
    public void setLbStickySession ( Boolean lbStickySession ) {
        this.lbStickySession = lbStickySession;
    }
    public String getWorker() {
        return worker;
    }
    public void setWorker ( String worker ) {
        this.worker = worker;
    }
    public String getWorkerType() {
        return workerType;
    }
    public void setWorkerType ( String workerType ) {
        this.workerType = workerType;
    }
    public String getWorkerLb() {
        return workerLb;
    }
    public void setWorkerLb ( String workerLb ) {
        this.workerLb = workerLb;
    }
    public String getWorkerClusterDomain() {
        return workerClusterDomain;
    }
    public void setWorkerClusterDomain ( String workerClusterDomain ) {
        this.workerClusterDomain = workerClusterDomain;
    }
    public Boolean getWorkerDisabled() {
        return workerDisabled;
    }
    public void setWorkerDisabled ( Boolean workerDisabled ) {
        this.workerDisabled = workerDisabled;
    }
    public Boolean getWorkerStopped() {
        return workerStopped;
    }
    public void setWorkerStopped ( Boolean workerStopped ) {
        this.workerStopped = workerStopped;
    }
    public Integer getWorkerLoadFactor() {
        return workerLoadFactor;
    }
    public void setWorkerLoadFactor ( Integer workerLoadFactor ) {
        this.workerLoadFactor = workerLoadFactor;
    }
    public String getWorkerRedirect() {
        return workerRedirect;
    }
    public void setWorkerRedirect ( String workerRedirect ) {
        this.workerRedirect = workerRedirect;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        checkParameter();
        StringBuilder sb = createLink();
        execute ( sb.toString(), null, null, -1 );
    }
    private StringBuilder createLink() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append ( "?cmd=update&mime=txt" );
            sb.append ( "&w=" );
            sb.append ( URLEncoder.encode ( worker, getCharset() ) );
            if ( isLBMode ) {
                if ( ( lbRetries != null ) ) {
                    sb.append ( "&lr=" );
                    sb.append ( lbRetries );
                }
                if ( ( lbRecovertime != null ) ) {
                    sb.append ( "&lt=" );
                    sb.append ( lbRecovertime );
                }
                if ( ( lbStickySession != null ) ) {
                    sb.append ( "&ls=" );
                    sb.append ( lbStickySession );
                }
                if ( ( lbForceSession != null ) ) {
                    sb.append ( "&lf=" );
                    sb.append ( lbForceSession );
                }
            } else {
                if ( ( workerLb != null ) ) {
                    sb.append ( "&l=" );
                    sb.append ( URLEncoder.encode ( workerLb, getCharset() ) );
                }
                if ( ( workerLoadFactor != null ) ) {
                    sb.append ( "&wf=" );
                    sb.append ( workerLoadFactor );
                }
                if ( ( workerDisabled != null ) ) {
                    sb.append ( "&wd=" );
                    sb.append ( workerDisabled );
                }
                if ( ( workerStopped != null ) ) {
                    sb.append ( "&ws=" );
                    sb.append ( workerStopped );
                }
                if ( ( workerRedirect != null ) ) {
                    sb.append ( "&wr=" );
                }
                if ( ( workerClusterDomain != null ) ) {
                    sb.append ( "&wc=" );
                    sb.append ( URLEncoder.encode ( workerClusterDomain,
                                                    getCharset() ) );
                }
            }
        } catch ( UnsupportedEncodingException e ) {
            throw new BuildException ( "Invalid 'charset' attribute: "
                                       + getCharset() );
        }
        return sb;
    }
    protected void checkParameter() {
        if ( worker == null ) {
            throw new BuildException ( "Must specify 'worker' attribute" );
        }
        if ( workerType == null ) {
            throw new BuildException ( "Must specify 'workerType' attribute" );
        }
        if ( "lb".equals ( workerType ) ) {
            if ( lbRecovertime == null && lbRetries == null ) {
                throw new BuildException (
                    "Must specify at a lb worker either 'lbRecovertime' or"
                    + "'lbRetries' attribute" );
            }
            if ( lbStickySession == null || lbForceSession == null ) {
                throw new BuildException ( "Must specify at a lb worker either"
                                           + "'lbStickySession' and 'lbForceSession' attribute" );
            }
            if ( null != lbRecovertime && 60 < lbRecovertime.intValue() ) {
                throw new BuildException (
                    "The 'lbRecovertime' must be greater than 59" );
            }
            if ( null != lbRetries && 1 < lbRetries.intValue() ) {
                throw new BuildException (
                    "The 'lbRetries' must be greater than 1" );
            }
            isLBMode = true;
        } else if ( "worker".equals ( workerType ) ) {
            if ( workerDisabled == null ) {
                throw new BuildException (
                    "Must specify at a node worker 'workerDisabled' attribute" );
            }
            if ( workerStopped == null ) {
                throw new BuildException (
                    "Must specify at a node worker 'workerStopped' attribute" );
            }
            if ( workerLoadFactor == null ) {
                throw new BuildException (
                    "Must specify at a node worker 'workerLoadFactor' attribute" );
            }
            if ( workerClusterDomain == null ) {
                throw new BuildException (
                    "Must specify at a node worker 'workerClusterDomain' attribute" );
            }
            if ( workerRedirect == null ) {
                throw new BuildException (
                    "Must specify at a node worker 'workerRedirect' attribute" );
            }
            if ( workerLb == null ) {
                throw new BuildException ( "Must specify 'workerLb' attribute" );
            }
            if ( workerLoadFactor.intValue() < 1 ) {
                throw new BuildException (
                    "The 'workerLoadFactor' must be greater or equal 1" );
            }
            isLBMode = false;
        } else {
            throw new BuildException (
                "Only 'lb' and 'worker' supported as workerType attribute" );
        }
    }
}
