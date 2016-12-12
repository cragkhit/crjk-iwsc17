package org.apache.tomcat.util.bcel.classfile;
public class ClassElementValue extends ElementValue {
    private final int idx;
    ClassElementValue ( final int type, final int idx, final ConstantPool cpool ) {
        super ( type, cpool );
        this.idx = idx;
    }
    @Override
    public String stringifyValue() {
        final ConstantUtf8 cu8 = ( ConstantUtf8 ) super.getConstantPool().getConstant ( this.idx, ( byte ) 1 );
        return cu8.getBytes();
    }
}
