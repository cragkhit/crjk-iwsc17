package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInput;
public class ElementValuePair {
    private final ElementValue elementValue;
    private final ConstantPool constantPool;
    private final int elementNameIndex;
    ElementValuePair ( final DataInput file, final ConstantPool constantPool ) throws IOException {
        this.constantPool = constantPool;
        this.elementNameIndex = file.readUnsignedShort();
        this.elementValue = ElementValue.readElementValue ( file, constantPool );
    }
    public String getNameString() {
        final ConstantUtf8 c = ( ConstantUtf8 ) this.constantPool.getConstant ( this.elementNameIndex, ( byte ) 1 );
        return c.getBytes();
    }
    public final ElementValue getValue() {
        return this.elementValue;
    }
}
