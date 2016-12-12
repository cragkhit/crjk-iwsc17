package org.apache.tomcat.util.bcel.classfile;
import java.io.DataInput;
import java.io.IOException;
import org.apache.tomcat.util.bcel.Const;
public abstract class Constant {
    protected final byte tag;
    Constant ( final byte tag ) {
        this.tag = tag;
    }
    public final byte getTag() {
        return tag;
    }
    static Constant readConstant ( final DataInput input ) throws IOException,
        ClassFormatException {
        final byte b = input.readByte();
        int skipSize;
        switch ( b ) {
        case Const.CONSTANT_Class:
            return new ConstantClass ( input );
        case Const.CONSTANT_Integer:
            return new ConstantInteger ( input );
        case Const.CONSTANT_Float:
            return new ConstantFloat ( input );
        case Const.CONSTANT_Long:
            return new ConstantLong ( input );
        case Const.CONSTANT_Double:
            return new ConstantDouble ( input );
        case Const.CONSTANT_Utf8:
            return ConstantUtf8.getInstance ( input );
        case Const.CONSTANT_String:
        case Const.CONSTANT_MethodType:
            skipSize = 2;
            break;
        case Const.CONSTANT_MethodHandle:
            skipSize = 3;
            break;
        case Const.CONSTANT_Fieldref:
        case Const.CONSTANT_Methodref:
        case Const.CONSTANT_InterfaceMethodref:
        case Const.CONSTANT_NameAndType:
        case Const.CONSTANT_InvokeDynamic:
            skipSize = 4;
            break;
        default:
            throw new ClassFormatException ( "Invalid byte tag in constant pool: " + b );
        }
        Utility.skipFully ( input, skipSize );
        return null;
    }
    @Override
    public String toString() {
        return "[" + tag + "]";
    }
}
