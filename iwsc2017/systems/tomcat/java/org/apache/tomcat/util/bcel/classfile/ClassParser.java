package org.apache.tomcat.util.bcel.classfile;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tomcat.util.bcel.Const;
public final class ClassParser {
    private static final int MAGIC = 0xCAFEBABE;
    private final DataInput dataInputStream;
    private String class_name, superclass_name;
    private int access_flags;
    private String[] interface_names;
    private ConstantPool constant_pool;
    private Annotations runtimeVisibleAnnotations;
    private static final int BUFSIZE = 8192;
    private static final String[] INTERFACES_EMPTY_ARRAY = new String[0];
    public ClassParser ( final InputStream inputStream ) {
        this.dataInputStream = new DataInputStream ( new BufferedInputStream ( inputStream, BUFSIZE ) );
    }
    public JavaClass parse() throws IOException, ClassFormatException {
        readID();
        readVersion();
        readConstantPool();
        readClassInfo();
        readInterfaces();
        readFields();
        readMethods();
        readAttributes();
        return new JavaClass ( class_name, superclass_name,
                               access_flags, constant_pool, interface_names,
                               runtimeVisibleAnnotations );
    }
    private void readAttributes() throws IOException, ClassFormatException {
        final int attributes_count = dataInputStream.readUnsignedShort();
        for ( int i = 0; i < attributes_count; i++ ) {
            ConstantUtf8 c;
            String name;
            int name_index;
            int length;
            name_index = dataInputStream.readUnsignedShort();
            c = ( ConstantUtf8 ) constant_pool.getConstant ( name_index,
                    Const.CONSTANT_Utf8 );
            name = c.getBytes();
            length = dataInputStream.readInt();
            if ( name.equals ( "RuntimeVisibleAnnotations" ) ) {
                if ( runtimeVisibleAnnotations != null ) {
                    throw new ClassFormatException (
                        "RuntimeVisibleAnnotations attribute is not allowed more than once in a class file" );
                }
                runtimeVisibleAnnotations = new Annotations ( dataInputStream, constant_pool );
            } else {
                Utility.skipFully ( dataInputStream, length );
            }
        }
    }
    private void readClassInfo() throws IOException, ClassFormatException {
        access_flags = dataInputStream.readUnsignedShort();
        if ( ( access_flags & Const.ACC_INTERFACE ) != 0 ) {
            access_flags |= Const.ACC_ABSTRACT;
        }
        if ( ( ( access_flags & Const.ACC_ABSTRACT ) != 0 )
                && ( ( access_flags & Const.ACC_FINAL ) != 0 ) ) {
            throw new ClassFormatException ( "Class can't be both final and abstract" );
        }
        int class_name_index = dataInputStream.readUnsignedShort();
        class_name = Utility.getClassName ( constant_pool, class_name_index );
        int superclass_name_index = dataInputStream.readUnsignedShort();
        if ( superclass_name_index > 0 ) {
            superclass_name = Utility.getClassName ( constant_pool, superclass_name_index );
        } else {
            superclass_name = "java.lang.Object";
        }
    }
    private void readConstantPool() throws IOException, ClassFormatException {
        constant_pool = new ConstantPool ( dataInputStream );
    }
    private void readFields() throws IOException, ClassFormatException {
        final int fields_count = dataInputStream.readUnsignedShort();
        for ( int i = 0; i < fields_count; i++ ) {
            Utility.swallowFieldOrMethod ( dataInputStream );
        }
    }
    private void readID() throws IOException, ClassFormatException {
        if ( dataInputStream.readInt() != MAGIC ) {
            throw new ClassFormatException ( "It is not a Java .class file" );
        }
    }
    private void readInterfaces() throws IOException, ClassFormatException {
        final int interfaces_count = dataInputStream.readUnsignedShort();
        if ( interfaces_count > 0 ) {
            interface_names = new String[interfaces_count];
            for ( int i = 0; i < interfaces_count; i++ ) {
                int index = dataInputStream.readUnsignedShort();
                interface_names[i] = Utility.getClassName ( constant_pool, index );
            }
        } else {
            interface_names = INTERFACES_EMPTY_ARRAY;
        }
    }
    private void readMethods() throws IOException, ClassFormatException {
        final int methods_count = dataInputStream.readUnsignedShort();
        for ( int i = 0; i < methods_count; i++ ) {
            Utility.swallowFieldOrMethod ( dataInputStream );
        }
    }
    private void readVersion() throws IOException, ClassFormatException {
        Utility.skipFully ( dataInputStream, 4 );
    }
}
