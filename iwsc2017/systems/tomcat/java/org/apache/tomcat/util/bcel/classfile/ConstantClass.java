package org.apache.tomcat.util.bcel.classfile;
import java.io.DataInput;
import java.io.IOException;
import org.apache.tomcat.util.bcel.Const;
public final class ConstantClass extends Constant {
    private final int name_index;
    ConstantClass ( final DataInput file ) throws IOException {
        super ( Const.CONSTANT_Class );
        this.name_index = file.readUnsignedShort();
    }
    public final int getNameIndex() {
        return name_index;
    }
}
