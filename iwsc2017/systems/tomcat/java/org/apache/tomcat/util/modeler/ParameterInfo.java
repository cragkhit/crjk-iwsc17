package org.apache.tomcat.util.modeler;
import javax.management.MBeanParameterInfo;
public class ParameterInfo extends FeatureInfo {
    static final long serialVersionUID = 2222796006787664020L;
    public ParameterInfo() {
        super();
    }
    public MBeanParameterInfo createParameterInfo() {
        if ( info == null ) {
            info = new MBeanParameterInfo
            ( getName(), getType(), getDescription() );
        }
        return ( MBeanParameterInfo ) info;
    }
}
