package org.apache.tomcat.util.modeler;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
public class OperationInfo extends FeatureInfo {
    static final long serialVersionUID = 4418342922072614875L;
    public OperationInfo() {
        super();
    }
    protected String impact = "UNKNOWN";
    protected String role = "operation";
    protected final ReadWriteLock parametersLock = new ReentrantReadWriteLock();
    protected ParameterInfo parameters[] = new ParameterInfo[0];
    public String getImpact() {
        return this.impact;
    }
    public void setImpact ( String impact ) {
        if ( impact == null ) {
            this.impact = null;
        } else {
            this.impact = impact.toUpperCase ( Locale.ENGLISH );
        }
    }
    public String getRole() {
        return this.role;
    }
    public void setRole ( String role ) {
        this.role = role;
    }
    public String getReturnType() {
        if ( type == null ) {
            type = "void";
        }
        return type;
    }
    public void setReturnType ( String returnType ) {
        this.type = returnType;
    }
    public ParameterInfo[] getSignature() {
        Lock readLock = parametersLock.readLock();
        readLock.lock();
        try {
            return this.parameters;
        } finally {
            readLock.unlock();
        }
    }
    public void addParameter ( ParameterInfo parameter ) {
        Lock writeLock = parametersLock.writeLock();
        writeLock.lock();
        try {
            ParameterInfo results[] = new ParameterInfo[parameters.length + 1];
            System.arraycopy ( parameters, 0, results, 0, parameters.length );
            results[parameters.length] = parameter;
            parameters = results;
            this.info = null;
        } finally {
            writeLock.unlock();
        }
    }
    MBeanOperationInfo createOperationInfo() {
        if ( info == null ) {
            int impact = MBeanOperationInfo.UNKNOWN;
            if ( "ACTION".equals ( getImpact() ) ) {
                impact = MBeanOperationInfo.ACTION;
            } else if ( "ACTION_INFO".equals ( getImpact() ) ) {
                impact = MBeanOperationInfo.ACTION_INFO;
            } else if ( "INFO".equals ( getImpact() ) ) {
                impact = MBeanOperationInfo.INFO;
            }
            info = new MBeanOperationInfo ( getName(), getDescription(),
                                            getMBeanParameterInfo(),
                                            getReturnType(), impact );
        }
        return ( MBeanOperationInfo ) info;
    }
    protected MBeanParameterInfo[] getMBeanParameterInfo() {
        ParameterInfo params[] = getSignature();
        MBeanParameterInfo parameters[] =
            new MBeanParameterInfo[params.length];
        for ( int i = 0; i < params.length; i++ ) {
            parameters[i] = params[i].createParameterInfo();
        }
        return parameters;
    }
}
