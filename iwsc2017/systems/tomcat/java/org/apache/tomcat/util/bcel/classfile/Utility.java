package org.apache.tomcat.util.bcel.classfile;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import org.apache.tomcat.util.bcel.Const;
final class Utility {
    private Utility() {
    }
    static String compactClassName ( final String str ) {
        return str.replace ( '/', '.' );
    }
    static String getClassName ( final ConstantPool constant_pool, final int index ) {
        Constant c = constant_pool.getConstant ( index, Const.CONSTANT_Class );
        int i = ( ( ConstantClass ) c ).getNameIndex();
        c = constant_pool.getConstant ( i, Const.CONSTANT_Utf8 );
        String name = ( ( ConstantUtf8 ) c ).getBytes();
        return compactClassName ( name );
    }
    static void skipFully ( final DataInput file, final int length ) throws IOException {
        int total = file.skipBytes ( length );
        if ( total != length ) {
            throw new EOFException();
        }
    }
    static void swallowFieldOrMethod ( final DataInput file )
    throws IOException {
        skipFully ( file, 6 );
        int attributes_count = file.readUnsignedShort();
        for ( int i = 0; i < attributes_count; i++ ) {
            swallowAttribute ( file );
        }
    }
    static void swallowAttribute ( final DataInput file )
    throws IOException {
        skipFully ( file, 2 );
        int length = file.readInt();
        skipFully ( file, length );
    }
}
