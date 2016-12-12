package org.apache.catalina.ant.jmx;
import org.apache.tools.ant.BuildException;
public class JMXAccessorCondition extends JMXAccessorConditionBase {
    private String operation = "==" ;
    private String type = "long" ;
    private String unlessCondition;
    private String ifCondition;
    public String getOperation() {
        return operation;
    }
    public void setOperation ( String operation ) {
        this.operation = operation;
    }
    public String getType() {
        return type;
    }
    public void setType ( String type ) {
        this.type = type;
    }
    public String getIf() {
        return ifCondition;
    }
    public void setIf ( String c ) {
        ifCondition = c;
    }
    public String getUnless() {
        return unlessCondition;
    }
    public void setUnless ( String c ) {
        unlessCondition = c;
    }
    protected boolean testIfCondition() {
        if ( ifCondition == null || "".equals ( ifCondition ) ) {
            return true;
        }
        return getProject().getProperty ( ifCondition ) != null;
    }
    protected boolean testUnlessCondition() {
        if ( unlessCondition == null || "".equals ( unlessCondition ) ) {
            return true;
        }
        return getProject().getProperty ( unlessCondition ) == null;
    }
    @Override
    public boolean eval() {
        String value = getValue();
        if ( operation == null ) {
            throw new BuildException ( "operation attribute is not set" );
        }
        if ( value == null ) {
            throw new BuildException ( "value attribute is not set" );
        }
        if ( ( getName() == null || getAttribute() == null ) ) {
            throw new BuildException (
                "Must specify an MBean name and attribute for condition" );
        }
        if ( testIfCondition() && testUnlessCondition() ) {
            String jmxValue = accessJMXValue();
            if ( jmxValue != null ) {
                String op = getOperation();
                if ( "==".equals ( op ) ) {
                    return jmxValue.equals ( value );
                } else if ( "!=".equals ( op ) ) {
                    return !jmxValue.equals ( value );
                } else {
                    if ( "long".equals ( type ) ) {
                        long jvalue = Long.parseLong ( jmxValue );
                        long lvalue = Long.parseLong ( value );
                        if ( ">".equals ( op ) ) {
                            return jvalue > lvalue;
                        } else if ( ">=".equals ( op ) ) {
                            return jvalue >= lvalue;
                        } else if ( "<".equals ( op ) ) {
                            return jvalue < lvalue;
                        } else if ( "<=".equals ( op ) ) {
                            return jvalue <= lvalue;
                        }
                    } else if ( "double".equals ( type ) ) {
                        double jvalue = Double.parseDouble ( jmxValue );
                        double dvalue = Double.parseDouble ( value );
                        if ( ">".equals ( op ) ) {
                            return jvalue > dvalue;
                        } else if ( ">=".equals ( op ) ) {
                            return jvalue >= dvalue;
                        } else if ( "<".equals ( op ) ) {
                            return jvalue < dvalue;
                        } else if ( "<=".equals ( op ) ) {
                            return jvalue <= dvalue;
                        }
                    }
                }
            }
            return false;
        }
        return true;
    }
}
