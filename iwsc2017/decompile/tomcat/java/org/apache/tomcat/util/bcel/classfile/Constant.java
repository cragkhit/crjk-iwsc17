package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInput;
public abstract class Constant {
    protected final byte tag;
    Constant ( final byte tag ) {
        this.tag = tag;
    }
    public final byte getTag() {
        return this.tag;
    }
    static Constant readConstant ( final DataInput input ) throws IOException, ClassFormatException {
        final byte b = input.readByte();
        int skipSize = 0;
        switch ( b ) {
        case 7: {
            return new ConstantClass ( input );
        }
        case 3: {
            return new ConstantInteger ( input );
        }
        case 4: {
            return new ConstantFloat ( input );
        }
        case 5: {
            return new ConstantLong ( input );
        }
        case 6: {
            return new ConstantDouble ( input );
        }
        case 1: {
            return ConstantUtf8.getInstance ( input );
        }
        case 8:
        case 16: {
            skipSize = 2;
            break;
        }
        case 15: {
            skipSize = 3;
            break;
        }
        case 9:
        case 10:
        case 11:
        case 12:
        case 18: {
            skipSize = 4;
            break;
        }
        default: {
            throw new ClassFormatException ( "Invalid byte tag in constant pool: " + b );
        }
        }
        Utility.skipFully ( input, skipSize );
        return null;
    }
    @Override
    public String toString() {
        return "[" + this.tag + "]";
    }
}
