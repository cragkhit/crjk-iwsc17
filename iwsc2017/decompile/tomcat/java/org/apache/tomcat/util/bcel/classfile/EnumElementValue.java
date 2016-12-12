package org.apache.tomcat.util.bcel.classfile;
public class EnumElementValue extends ElementValue {
    private final int valueIdx;
    EnumElementValue ( final int type, final int valueIdx, final ConstantPool cpool ) {
        super ( type, cpool );
        if ( type != 101 ) {
            throw new RuntimeException ( "Only element values of type enum can be built with this ctor - type specified: " + type );
        }
        this.valueIdx = valueIdx;
    }
    @Override
    public String stringifyValue() {
        final ConstantUtf8 cu8 = ( ConstantUtf8 ) super.getConstantPool().getConstant ( this.valueIdx, ( byte ) 1 );
        return cu8.getBytes();
    }
}
