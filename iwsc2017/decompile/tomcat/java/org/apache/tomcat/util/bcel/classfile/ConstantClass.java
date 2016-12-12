package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInput;
public final class ConstantClass extends Constant {
    private final int name_index;
    ConstantClass ( final DataInput file ) throws IOException {
        super ( ( byte ) 7 );
        this.name_index = file.readUnsignedShort();
    }
    public final int getNameIndex() {
        return this.name_index;
    }
}
