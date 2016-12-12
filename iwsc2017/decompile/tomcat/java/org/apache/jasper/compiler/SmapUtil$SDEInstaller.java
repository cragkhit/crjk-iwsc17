package org.apache.jasper.compiler;
import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import java.io.File;
import org.apache.juli.logging.Log;
private static class SDEInstaller {
    private final Log log;
    static final String nameSDE = "SourceDebugExtension";
    byte[] orig;
    byte[] sdeAttr;
    byte[] gen;
    int origPos;
    int genPos;
    int sdeIndex;
    static void install ( final File classFile, final byte[] smap ) throws IOException {
        final File tmpFile = new File ( classFile.getPath() + "tmp" );
        final SDEInstaller installer = new SDEInstaller ( classFile, smap );
        installer.install ( tmpFile );
        if ( !classFile.delete() ) {
            throw new IOException ( "classFile.delete() failed" );
        }
        if ( !tmpFile.renameTo ( classFile ) ) {
            throw new IOException ( "tmpFile.renameTo(classFile) failed" );
        }
    }
    SDEInstaller ( final File inClassFile, final byte[] sdeAttr ) throws IOException {
        this.log = LogFactory.getLog ( SDEInstaller.class );
        this.origPos = 0;
        this.genPos = 0;
        if ( !inClassFile.exists() ) {
            throw new FileNotFoundException ( "no such file: " + inClassFile );
        }
        this.sdeAttr = sdeAttr;
        this.orig = readWhole ( inClassFile );
        this.gen = new byte[this.orig.length + sdeAttr.length + 100];
    }
    void install ( final File outClassFile ) throws IOException {
        this.addSDE();
        try ( final FileOutputStream outStream = new FileOutputStream ( outClassFile ) ) {
            outStream.write ( this.gen, 0, this.genPos );
        }
    }
    static byte[] readWhole ( final File input ) throws IOException {
        final int len = ( int ) input.length();
        final byte[] bytes = new byte[len];
        try ( final FileInputStream inStream = new FileInputStream ( input ) ) {
            if ( inStream.read ( bytes, 0, len ) != len ) {
                throw new IOException ( "expected size: " + len );
            }
        }
        return bytes;
    }
    void addSDE() throws UnsupportedEncodingException, IOException {
        this.copy ( 8 );
        final int constantPoolCountPos = this.genPos;
        int constantPoolCount = this.readU2();
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "constant pool count: " + constantPoolCount );
        }
        this.writeU2 ( constantPoolCount );
        this.sdeIndex = this.copyConstantPool ( constantPoolCount );
        if ( this.sdeIndex < 0 ) {
            this.writeUtf8ForSDE();
            this.sdeIndex = constantPoolCount;
            ++constantPoolCount;
            this.randomAccessWriteU2 ( constantPoolCountPos, constantPoolCount );
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "SourceDebugExtension not found, installed at: " + this.sdeIndex );
            }
        } else if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "SourceDebugExtension found at: " + this.sdeIndex );
        }
        this.copy ( 6 );
        final int interfaceCount = this.readU2();
        this.writeU2 ( interfaceCount );
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "interfaceCount: " + interfaceCount );
        }
        this.copy ( interfaceCount * 2 );
        this.copyMembers();
        this.copyMembers();
        final int attrCountPos = this.genPos;
        int attrCount = this.readU2();
        this.writeU2 ( attrCount );
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "class attrCount: " + attrCount );
        }
        if ( !this.copyAttrs ( attrCount ) ) {
            ++attrCount;
            this.randomAccessWriteU2 ( attrCountPos, attrCount );
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "class attrCount incremented" );
            }
        }
        this.writeAttrForSDE ( this.sdeIndex );
    }
    void copyMembers() {
        final int count = this.readU2();
        this.writeU2 ( count );
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "members count: " + count );
        }
        for ( int i = 0; i < count; ++i ) {
            this.copy ( 6 );
            final int attrCount = this.readU2();
            this.writeU2 ( attrCount );
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "member attr count: " + attrCount );
            }
            this.copyAttrs ( attrCount );
        }
    }
    boolean copyAttrs ( final int attrCount ) {
        boolean sdeFound = false;
        for ( int i = 0; i < attrCount; ++i ) {
            final int nameIndex = this.readU2();
            if ( nameIndex == this.sdeIndex ) {
                sdeFound = true;
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "SDE attr found" );
                }
            } else {
                this.writeU2 ( nameIndex );
                final int len = this.readU4();
                this.writeU4 ( len );
                this.copy ( len );
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "attr len: " + len );
                }
            }
        }
        return sdeFound;
    }
    void writeAttrForSDE ( final int index ) {
        this.writeU2 ( index );
        this.writeU4 ( this.sdeAttr.length );
        for ( int i = 0; i < this.sdeAttr.length; ++i ) {
            this.writeU1 ( this.sdeAttr[i] );
        }
    }
    void randomAccessWriteU2 ( final int pos, final int val ) {
        final int savePos = this.genPos;
        this.genPos = pos;
        this.writeU2 ( val );
        this.genPos = savePos;
    }
    int readU1() {
        return this.orig[this.origPos++] & 0xFF;
    }
    int readU2() {
        final int res = this.readU1();
        return ( res << 8 ) + this.readU1();
    }
    int readU4() {
        final int res = this.readU2();
        return ( res << 16 ) + this.readU2();
    }
    void writeU1 ( final int val ) {
        this.gen[this.genPos++] = ( byte ) val;
    }
    void writeU2 ( final int val ) {
        this.writeU1 ( val >> 8 );
        this.writeU1 ( val & 0xFF );
    }
    void writeU4 ( final int val ) {
        this.writeU2 ( val >> 16 );
        this.writeU2 ( val & 0xFFFF );
    }
    void copy ( final int count ) {
        for ( int i = 0; i < count; ++i ) {
            this.gen[this.genPos++] = this.orig[this.origPos++];
        }
    }
    byte[] readBytes ( final int count ) {
        final byte[] bytes = new byte[count];
        for ( int i = 0; i < count; ++i ) {
            bytes[i] = this.orig[this.origPos++];
        }
        return bytes;
    }
    void writeBytes ( final byte[] bytes ) {
        for ( int i = 0; i < bytes.length; ++i ) {
            this.gen[this.genPos++] = bytes[i];
        }
    }
    int copyConstantPool ( final int constantPoolCount ) throws UnsupportedEncodingException, IOException {
        int sdeIndex = -1;
        for ( int i = 1; i < constantPoolCount; ++i ) {
            final int tag = this.readU1();
            this.writeU1 ( tag );
            switch ( tag ) {
            case 7:
            case 8:
            case 16: {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( i + " copying 2 bytes" );
                }
                this.copy ( 2 );
                break;
            }
            case 15: {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( i + " copying 3 bytes" );
                }
                this.copy ( 3 );
                break;
            }
            case 3:
            case 4:
            case 9:
            case 10:
            case 11:
            case 12:
            case 18: {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( i + " copying 4 bytes" );
                }
                this.copy ( 4 );
                break;
            }
            case 5:
            case 6: {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( i + " copying 8 bytes" );
                }
                this.copy ( 8 );
                ++i;
                break;
            }
            case 1: {
                final int len = this.readU2();
                this.writeU2 ( len );
                final byte[] utf8 = this.readBytes ( len );
                final String str = new String ( utf8, "UTF-8" );
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( i + " read class attr -- '" + str + "'" );
                }
                if ( str.equals ( "SourceDebugExtension" ) ) {
                    sdeIndex = i;
                }
                this.writeBytes ( utf8 );
                break;
            }
            default: {
                throw new IOException ( "unexpected tag: " + tag );
            }
            }
        }
        return sdeIndex;
    }
    void writeUtf8ForSDE() {
        final int len = "SourceDebugExtension".length();
        this.writeU1 ( 1 );
        this.writeU2 ( len );
        for ( int i = 0; i < len; ++i ) {
            this.writeU1 ( "SourceDebugExtension".charAt ( i ) );
        }
    }
}
