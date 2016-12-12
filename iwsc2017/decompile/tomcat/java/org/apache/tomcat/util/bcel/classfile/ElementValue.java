package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInput;
public abstract class ElementValue {
    private final int type;
    private final ConstantPool cpool;
    public static final byte STRING = 115;
    public static final byte ENUM_CONSTANT = 101;
    public static final byte CLASS = 99;
    public static final byte ANNOTATION = 64;
    public static final byte ARRAY = 91;
    public static final byte PRIMITIVE_INT = 73;
    public static final byte PRIMITIVE_BYTE = 66;
    public static final byte PRIMITIVE_CHAR = 67;
    public static final byte PRIMITIVE_DOUBLE = 68;
    public static final byte PRIMITIVE_FLOAT = 70;
    public static final byte PRIMITIVE_LONG = 74;
    public static final byte PRIMITIVE_SHORT = 83;
    public static final byte PRIMITIVE_BOOLEAN = 90;
    ElementValue ( final int type, final ConstantPool cpool ) {
        this.type = type;
        this.cpool = cpool;
    }
    public abstract String stringifyValue();
    public static ElementValue readElementValue ( final DataInput input, final ConstantPool cpool ) throws IOException {
        final byte type = input.readByte();
        switch ( type ) {
        case 66:
        case 67:
        case 68:
        case 70:
        case 73:
        case 74:
        case 83:
        case 90:
        case 115: {
            return new SimpleElementValue ( type, input.readUnsignedShort(), cpool );
        }
        case 101: {
            input.readUnsignedShort();
            return new EnumElementValue ( 101, input.readUnsignedShort(), cpool );
        }
        case 99: {
            return new ClassElementValue ( 99, input.readUnsignedShort(), cpool );
        }
        case 64: {
            return new AnnotationElementValue ( 64, new AnnotationEntry ( input, cpool ), cpool );
        }
        case 91: {
            final int numArrayVals = input.readUnsignedShort();
            final ElementValue[] evalues = new ElementValue[numArrayVals];
            for ( int j = 0; j < numArrayVals; ++j ) {
                evalues[j] = readElementValue ( input, cpool );
            }
            return new ArrayElementValue ( 91, evalues, cpool );
        }
        default: {
            throw new ClassFormatException ( "Unexpected element value kind in annotation: " + type );
        }
        }
    }
    final ConstantPool getConstantPool() {
        return this.cpool;
    }
    final int getType() {
        return this.type;
    }
}
