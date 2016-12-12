package org.apache.tomcat.util.bcel.classfile;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.DataInput;
public final class ClassParser {
    private static final int MAGIC = -889275714;
    private final DataInput dataInputStream;
    private String class_name;
    private String superclass_name;
    private int access_flags;
    private String[] interface_names;
    private ConstantPool constant_pool;
    private Annotations runtimeVisibleAnnotations;
    private static final int BUFSIZE = 8192;
    private static final String[] INTERFACES_EMPTY_ARRAY;
    public ClassParser ( final InputStream inputStream ) {
        this.dataInputStream = new DataInputStream ( new BufferedInputStream ( inputStream, 8192 ) );
    }
    public JavaClass parse() throws IOException, ClassFormatException {
        this.readID();
        this.readVersion();
        this.readConstantPool();
        this.readClassInfo();
        this.readInterfaces();
        this.readFields();
        this.readMethods();
        this.readAttributes();
        return new JavaClass ( this.class_name, this.superclass_name, this.access_flags, this.constant_pool, this.interface_names, this.runtimeVisibleAnnotations );
    }
    private void readAttributes() throws IOException, ClassFormatException {
        for ( int attributes_count = this.dataInputStream.readUnsignedShort(), i = 0; i < attributes_count; ++i ) {
            final int name_index = this.dataInputStream.readUnsignedShort();
            final ConstantUtf8 c = ( ConstantUtf8 ) this.constant_pool.getConstant ( name_index, ( byte ) 1 );
            final String name = c.getBytes();
            final int length = this.dataInputStream.readInt();
            if ( name.equals ( "RuntimeVisibleAnnotations" ) ) {
                if ( this.runtimeVisibleAnnotations != null ) {
                    throw new ClassFormatException ( "RuntimeVisibleAnnotations attribute is not allowed more than once in a class file" );
                }
                this.runtimeVisibleAnnotations = new Annotations ( this.dataInputStream, this.constant_pool );
            } else {
                Utility.skipFully ( this.dataInputStream, length );
            }
        }
    }
    private void readClassInfo() throws IOException, ClassFormatException {
        this.access_flags = this.dataInputStream.readUnsignedShort();
        if ( ( this.access_flags & 0x200 ) != 0x0 ) {
            this.access_flags |= 0x400;
        }
        if ( ( this.access_flags & 0x400 ) != 0x0 && ( this.access_flags & 0x10 ) != 0x0 ) {
            throw new ClassFormatException ( "Class can't be both final and abstract" );
        }
        final int class_name_index = this.dataInputStream.readUnsignedShort();
        this.class_name = Utility.getClassName ( this.constant_pool, class_name_index );
        final int superclass_name_index = this.dataInputStream.readUnsignedShort();
        if ( superclass_name_index > 0 ) {
            this.superclass_name = Utility.getClassName ( this.constant_pool, superclass_name_index );
        } else {
            this.superclass_name = "java.lang.Object";
        }
    }
    private void readConstantPool() throws IOException, ClassFormatException {
        this.constant_pool = new ConstantPool ( this.dataInputStream );
    }
    private void readFields() throws IOException, ClassFormatException {
        for ( int fields_count = this.dataInputStream.readUnsignedShort(), i = 0; i < fields_count; ++i ) {
            Utility.swallowFieldOrMethod ( this.dataInputStream );
        }
    }
    private void readID() throws IOException, ClassFormatException {
        if ( this.dataInputStream.readInt() != -889275714 ) {
            throw new ClassFormatException ( "It is not a Java .class file" );
        }
    }
    private void readInterfaces() throws IOException, ClassFormatException {
        final int interfaces_count = this.dataInputStream.readUnsignedShort();
        if ( interfaces_count > 0 ) {
            this.interface_names = new String[interfaces_count];
            for ( int i = 0; i < interfaces_count; ++i ) {
                final int index = this.dataInputStream.readUnsignedShort();
                this.interface_names[i] = Utility.getClassName ( this.constant_pool, index );
            }
        } else {
            this.interface_names = ClassParser.INTERFACES_EMPTY_ARRAY;
        }
    }
    private void readMethods() throws IOException, ClassFormatException {
        for ( int methods_count = this.dataInputStream.readUnsignedShort(), i = 0; i < methods_count; ++i ) {
            Utility.swallowFieldOrMethod ( this.dataInputStream );
        }
    }
    private void readVersion() throws IOException, ClassFormatException {
        Utility.skipFully ( this.dataInputStream, 4 );
    }
    static {
        INTERFACES_EMPTY_ARRAY = new String[0];
    }
}
