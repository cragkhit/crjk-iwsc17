package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
import java.io.InputStream;
public class JKStatusUpdateTask extends AbstractCatalinaTask {
    private String worker;
    private String workerType;
    private int internalid;
    private Integer lbRetries;
    private Integer lbRecovertime;
    private Boolean lbStickySession;
    private Boolean lbForceSession;
    private Integer workerLoadFactor;
    private String workerRedirect;
    private String workerClusterDomain;
    private Boolean workerDisabled;
    private Boolean workerStopped;
    private boolean isLBMode;
    private String workerLb;
    public JKStatusUpdateTask() {
        this.worker = "lb";
        this.workerType = "lb";
        this.internalid = 0;
        this.lbStickySession = Boolean.TRUE;
        this.lbForceSession = Boolean.FALSE;
        this.workerDisabled = Boolean.FALSE;
        this.workerStopped = Boolean.FALSE;
        this.isLBMode = true;
        this.setUrl ( "http://localhost/status" );
    }
    public int getInternalid() {
        return this.internalid;
    }
    public void setInternalid ( final int internalid ) {
        this.internalid = internalid;
    }
    public Boolean getLbForceSession() {
        return this.lbForceSession;
    }
    public void setLbForceSession ( final Boolean lbForceSession ) {
        this.lbForceSession = lbForceSession;
    }
    public Integer getLbRecovertime() {
        return this.lbRecovertime;
    }
    public void setLbRecovertime ( final Integer lbRecovertime ) {
        this.lbRecovertime = lbRecovertime;
    }
    public Integer getLbRetries() {
        return this.lbRetries;
    }
    public void setLbRetries ( final Integer lbRetries ) {
        this.lbRetries = lbRetries;
    }
    public Boolean getLbStickySession() {
        return this.lbStickySession;
    }
    public void setLbStickySession ( final Boolean lbStickySession ) {
        this.lbStickySession = lbStickySession;
    }
    public String getWorker() {
        return this.worker;
    }
    public void setWorker ( final String worker ) {
        this.worker = worker;
    }
    public String getWorkerType() {
        return this.workerType;
    }
    public void setWorkerType ( final String workerType ) {
        this.workerType = workerType;
    }
    public String getWorkerLb() {
        return this.workerLb;
    }
    public void setWorkerLb ( final String workerLb ) {
        this.workerLb = workerLb;
    }
    public String getWorkerClusterDomain() {
        return this.workerClusterDomain;
    }
    public void setWorkerClusterDomain ( final String workerClusterDomain ) {
        this.workerClusterDomain = workerClusterDomain;
    }
    public Boolean getWorkerDisabled() {
        return this.workerDisabled;
    }
    public void setWorkerDisabled ( final Boolean workerDisabled ) {
        this.workerDisabled = workerDisabled;
    }
    public Boolean getWorkerStopped() {
        return this.workerStopped;
    }
    public void setWorkerStopped ( final Boolean workerStopped ) {
        this.workerStopped = workerStopped;
    }
    public Integer getWorkerLoadFactor() {
        return this.workerLoadFactor;
    }
    public void setWorkerLoadFactor ( final Integer workerLoadFactor ) {
        this.workerLoadFactor = workerLoadFactor;
    }
    public String getWorkerRedirect() {
        return this.workerRedirect;
    }
    public void setWorkerRedirect ( final String workerRedirect ) {
        this.workerRedirect = workerRedirect;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        this.checkParameter();
        final StringBuilder sb = this.createLink();
        this.execute ( sb.toString(), null, null, -1L );
    }
    private StringBuilder createLink() {
        final StringBuilder sb = new StringBuilder();
        try {
            sb.append ( "?cmd=update&mime=txt" );
            sb.append ( "&w=" );
            sb.append ( URLEncoder.encode ( this.worker, this.getCharset() ) );
            if ( this.isLBMode ) {
                if ( this.lbRetries != null ) {
                    sb.append ( "&lr=" );
                    sb.append ( this.lbRetries );
                }
                if ( this.lbRecovertime != null ) {
                    sb.append ( "&lt=" );
                    sb.append ( this.lbRecovertime );
                }
                if ( this.lbStickySession != null ) {
                    sb.append ( "&ls=" );
                    sb.append ( this.lbStickySession );
                }
                if ( this.lbForceSession != null ) {
                    sb.append ( "&lf=" );
                    sb.append ( this.lbForceSession );
                }
            } else {
                if ( this.workerLb != null ) {
                    sb.append ( "&l=" );
                    sb.append ( URLEncoder.encode ( this.workerLb, this.getCharset() ) );
                }
                if ( this.workerLoadFactor != null ) {
                    sb.append ( "&wf=" );
                    sb.append ( this.workerLoadFactor );
                }
                if ( this.workerDisabled != null ) {
                    sb.append ( "&wd=" );
                    sb.append ( this.workerDisabled );
                }
                if ( this.workerStopped != null ) {
                    sb.append ( "&ws=" );
                    sb.append ( this.workerStopped );
                }
                if ( this.workerRedirect != null ) {
                    sb.append ( "&wr=" );
                }
                if ( this.workerClusterDomain != null ) {
                    sb.append ( "&wc=" );
                    sb.append ( URLEncoder.encode ( this.workerClusterDomain, this.getCharset() ) );
                }
            }
        } catch ( UnsupportedEncodingException e ) {
            throw new BuildException ( "Invalid 'charset' attribute: " + this.getCharset() );
        }
        return sb;
    }
    protected void checkParameter() {
        if ( this.worker == null ) {
            throw new BuildException ( "Must specify 'worker' attribute" );
        }
        if ( this.workerType == null ) {
            throw new BuildException ( "Must specify 'workerType' attribute" );
        }
        if ( "lb".equals ( this.workerType ) ) {
            if ( this.lbRecovertime == null && this.lbRetries == null ) {
                throw new BuildException ( "Must specify at a lb worker either 'lbRecovertime' or'lbRetries' attribute" );
            }
            if ( this.lbStickySession == null || this.lbForceSession == null ) {
                throw new BuildException ( "Must specify at a lb worker either'lbStickySession' and 'lbForceSession' attribute" );
            }
            if ( null != this.lbRecovertime && 60 < this.lbRecovertime ) {
                throw new BuildException ( "The 'lbRecovertime' must be greater than 59" );
            }
            if ( null != this.lbRetries && 1 < this.lbRetries ) {
                throw new BuildException ( "The 'lbRetries' must be greater than 1" );
            }
            this.isLBMode = true;
        } else {
            if ( !"worker".equals ( this.workerType ) ) {
                throw new BuildException ( "Only 'lb' and 'worker' supported as workerType attribute" );
            }
            if ( this.workerDisabled == null ) {
                throw new BuildException ( "Must specify at a node worker 'workerDisabled' attribute" );
            }
            if ( this.workerStopped == null ) {
                throw new BuildException ( "Must specify at a node worker 'workerStopped' attribute" );
            }
            if ( this.workerLoadFactor == null ) {
                throw new BuildException ( "Must specify at a node worker 'workerLoadFactor' attribute" );
            }
            if ( this.workerClusterDomain == null ) {
                throw new BuildException ( "Must specify at a node worker 'workerClusterDomain' attribute" );
            }
            if ( this.workerRedirect == null ) {
                throw new BuildException ( "Must specify at a node worker 'workerRedirect' attribute" );
            }
            if ( this.workerLb == null ) {
                throw new BuildException ( "Must specify 'workerLb' attribute" );
            }
            if ( this.workerLoadFactor < 1 ) {
                throw new BuildException ( "The 'workerLoadFactor' must be greater or equal 1" );
            }
            this.isLBMode = false;
        }
    }
}
