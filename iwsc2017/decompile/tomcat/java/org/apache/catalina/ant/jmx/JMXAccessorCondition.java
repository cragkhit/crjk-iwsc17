package org.apache.catalina.ant.jmx;
import org.apache.tools.ant.BuildException;
public class JMXAccessorCondition extends JMXAccessorConditionBase {
    private String operation;
    private String type;
    private String unlessCondition;
    private String ifCondition;
    public JMXAccessorCondition() {
        this.operation = "==";
        this.type = "long";
    }
    public String getOperation() {
        return this.operation;
    }
    public void setOperation ( final String operation ) {
        this.operation = operation;
    }
    public String getType() {
        return this.type;
    }
    public void setType ( final String type ) {
        this.type = type;
    }
    public String getIf() {
        return this.ifCondition;
    }
    public void setIf ( final String c ) {
        this.ifCondition = c;
    }
    public String getUnless() {
        return this.unlessCondition;
    }
    public void setUnless ( final String c ) {
        this.unlessCondition = c;
    }
    protected boolean testIfCondition() {
        return this.ifCondition == null || "".equals ( this.ifCondition ) || this.getProject().getProperty ( this.ifCondition ) != null;
    }
    protected boolean testUnlessCondition() {
        return this.unlessCondition == null || "".equals ( this.unlessCondition ) || this.getProject().getProperty ( this.unlessCondition ) == null;
    }
    public boolean eval() {
        final String value = this.getValue();
        if ( this.operation == null ) {
            throw new BuildException ( "operation attribute is not set" );
        }
        if ( value == null ) {
            throw new BuildException ( "value attribute is not set" );
        }
        if ( this.getName() == null || this.getAttribute() == null ) {
            throw new BuildException ( "Must specify an MBean name and attribute for condition" );
        }
        if ( this.testIfCondition() && this.testUnlessCondition() ) {
            final String jmxValue = this.accessJMXValue();
            if ( jmxValue != null ) {
                final String op = this.getOperation();
                if ( "==".equals ( op ) ) {
                    return jmxValue.equals ( value );
                }
                if ( "!=".equals ( op ) ) {
                    return !jmxValue.equals ( value );
                }
                if ( "long".equals ( this.type ) ) {
                    final long jvalue = Long.parseLong ( jmxValue );
                    final long lvalue = Long.parseLong ( value );
                    if ( ">".equals ( op ) ) {
                        return jvalue > lvalue;
                    }
                    if ( ">=".equals ( op ) ) {
                        return jvalue >= lvalue;
                    }
                    if ( "<".equals ( op ) ) {
                        return jvalue < lvalue;
                    }
                    if ( "<=".equals ( op ) ) {
                        return jvalue <= lvalue;
                    }
                } else if ( "double".equals ( this.type ) ) {
                    final double jvalue2 = Double.parseDouble ( jmxValue );
                    final double dvalue = Double.parseDouble ( value );
                    if ( ">".equals ( op ) ) {
                        return jvalue2 > dvalue;
                    }
                    if ( ">=".equals ( op ) ) {
                        return jvalue2 >= dvalue;
                    }
                    if ( "<".equals ( op ) ) {
                        return jvalue2 < dvalue;
                    }
                    if ( "<=".equals ( op ) ) {
                        return jvalue2 <= dvalue;
                    }
                }
            }
            return false;
        }
        return true;
    }
}
