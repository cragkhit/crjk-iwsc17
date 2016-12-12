package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.EOFException;
import java.io.DataInput;
final class Utility {
    static String compactClassName ( final String str ) {
        return str.replace ( '/', '.' );
    }
    static String getClassName ( final ConstantPool constant_pool, final int index ) {
        Constant c = constant_pool.getConstant ( index, ( byte ) 7 );
        final int i = ( ( ConstantClass ) c ).getNameIndex();
        c = constant_pool.getConstant ( i, ( byte ) 1 );
        final String name = ( ( ConstantUtf8 ) c ).getBytes();
        return compactClassName ( name );
    }
    static void skipFully ( final DataInput file, final int length ) throws IOException {
        final int total = file.skipBytes ( length );
        if ( total != length ) {
            throw new EOFException();
        }
    }
    static void swallowFieldOrMethod ( final DataInput file ) throws IOException {
        skipFully ( file, 6 );
        for ( int attributes_count = file.readUnsignedShort(), i = 0; i < attributes_count; ++i ) {
            swallowAttribute ( file );
        }
    }
    static void swallowAttribute ( final DataInput file ) throws IOException {
        skipFully ( file, 2 );
        final int length = file.readInt();
        skipFully ( file, length );
    }
}
