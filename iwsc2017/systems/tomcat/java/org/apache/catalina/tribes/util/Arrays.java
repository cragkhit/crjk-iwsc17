package org.apache.catalina.tribes.util;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.group.AbsoluteOrder;
import org.apache.catalina.tribes.membership.Membership;
public class Arrays {
    protected static final StringManager sm = StringManager.getManager ( Arrays.class );
    public static boolean contains ( byte[] source, int srcoffset, byte[] key, int keyoffset, int length ) {
        if ( srcoffset < 0 || srcoffset >= source.length ) {
            throw new ArrayIndexOutOfBoundsException ( sm.getString ( "arrays.srcoffset.outOfBounds" ) );
        }
        if ( keyoffset < 0 || keyoffset >= key.length ) {
            throw new ArrayIndexOutOfBoundsException ( sm.getString ( "arrays.keyoffset.outOfBounds" ) );
        }
        if ( length > ( key.length - keyoffset ) ) {
            throw new ArrayIndexOutOfBoundsException ( sm.getString ( "arrays.length.outOfBounds" ) );
        }
        if ( length > ( source.length - srcoffset ) ) {
            return false;
        }
        boolean match = true;
        int pos = keyoffset;
        for ( int i = srcoffset; match && i < length; i++ ) {
            match = ( source[i] == key[pos++] );
        }
        return match;
    }
    public static String toString ( byte[] data ) {
        return toString ( data, 0, data != null ? data.length : 0 );
    }
    public static String toString ( byte[] data, int offset, int length ) {
        return toString ( data, offset, length, false );
    }
    public static String toString ( byte[] data, int offset, int length, boolean unsigned ) {
        StringBuilder buf = new StringBuilder ( "{" );
        if ( data != null && length > 0 ) {
            int i = offset;
            if ( unsigned ) {
                buf.append ( data[i++] & 0xff );
                for ( ; i < length; i++ ) {
                    buf.append ( ", " ).append ( data[i] & 0xff );
                }
            } else {
                buf.append ( data[i++] );
                for ( ; i < length; i++ ) {
                    buf.append ( ", " ).append ( data[i] );
                }
            }
        }
        buf.append ( "}" );
        return buf.toString();
    }
    public static String toString ( Object[] data ) {
        return toString ( data, 0, data != null ? data.length : 0 );
    }
    public static String toString ( Object[] data, int offset, int length ) {
        StringBuilder buf = new StringBuilder ( "{" );
        if ( data != null && length > 0 ) {
            buf.append ( data[offset++] );
            for ( int i = offset; i < length; i++ ) {
                buf.append ( ", " ).append ( data[i] );
            }
        }
        buf.append ( "}" );
        return buf.toString();
    }
    public static String toNameString ( Member[] data ) {
        return toNameString ( data, 0, data != null ? data.length : 0 );
    }
    public static String toNameString ( Member[] data, int offset, int length ) {
        StringBuilder buf = new StringBuilder ( "{" );
        if ( data != null && length > 0 ) {
            buf.append ( data[offset++].getName() );
            for ( int i = offset; i < length; i++ ) {
                buf.append ( ", " ).append ( data[i].getName() );
            }
        }
        buf.append ( "}" );
        return buf.toString();
    }
    public static int add ( int[] data ) {
        int result = 0;
        for ( int i = 0; i < data.length; i++ ) {
            result += data[i];
        }
        return result;
    }
    public static UniqueId getUniqudId ( ChannelMessage msg ) {
        return new UniqueId ( msg.getUniqueId() );
    }
    public static UniqueId getUniqudId ( byte[] data ) {
        return new UniqueId ( data );
    }
    public static boolean equals ( byte[] o1, byte[] o2 ) {
        return java.util.Arrays.equals ( o1, o2 );
    }
    public static boolean equals ( Object[] o1, Object[] o2 ) {
        boolean result = o1.length == o2.length;
        if ( result ) for ( int i = 0; i < o1.length && result; i++ ) {
                result = o1[i].equals ( o2[i] );
            }
        return result;
    }
    public static boolean sameMembers ( Member[] m1, Member[] m2 ) {
        AbsoluteOrder.absoluteOrder ( m1 );
        AbsoluteOrder.absoluteOrder ( m2 );
        return equals ( m1, m2 );
    }
    public static Member[] merge ( Member[] m1, Member[] m2 ) {
        AbsoluteOrder.absoluteOrder ( m1 );
        AbsoluteOrder.absoluteOrder ( m2 );
        ArrayList<Member> list = new ArrayList<> ( java.util.Arrays.asList ( m1 ) );
        for ( int i = 0; i < m2.length; i++ ) if ( !list.contains ( m2[i] ) ) {
                list.add ( m2[i] );
            }
        Member[] result = new Member[list.size()];
        list.toArray ( result );
        AbsoluteOrder.absoluteOrder ( result );
        return result;
    }
    public static void fill ( Membership mbrship, Member[] m ) {
        for ( int i = 0; i < m.length; i++ ) {
            mbrship.addMember ( m[i] );
        }
    }
    public static Member[] diff ( Membership complete, Membership local, Member ignore ) {
        ArrayList<Member> result = new ArrayList<>();
        Member[] comp = complete.getMembers();
        for ( int i = 0; i < comp.length; i++ ) {
            if ( ignore != null && ignore.equals ( comp[i] ) ) {
                continue;
            }
            if ( local.getMember ( comp[i] ) == null ) {
                result.add ( comp[i] );
            }
        }
        return result.toArray ( new Member[result.size()] );
    }
    public static Member[] remove ( Member[] all, Member remove ) {
        return extract ( all, new Member[] {remove} );
    }
    public static Member[] extract ( Member[] all, Member[] remove ) {
        List<Member> alist = java.util.Arrays.asList ( all );
        ArrayList<Member> list = new ArrayList<> ( alist );
        for ( int i = 0; i < remove.length; i++ ) {
            list.remove ( remove[i] );
        }
        return list.toArray ( new Member[list.size()] );
    }
    public static int indexOf ( Member member, Member[] members ) {
        int result = -1;
        for ( int i = 0; ( result == -1 ) && ( i < members.length ); i++ )
            if ( member.equals ( members[i] ) ) {
                result = i;
            }
        return result;
    }
    public static int nextIndex ( Member member, Member[] members ) {
        int idx = indexOf ( member, members ) + 1;
        if ( idx >= members.length ) {
            idx = ( ( members.length > 0 ) ? 0 : -1 );
        }
        return idx;
    }
    public static int hashCode ( byte a[] ) {
        if ( a == null ) {
            return 0;
        }
        int result = 1;
        for ( int i = 0; i < a.length; i++ ) {
            byte element = a[i];
            result = 31 * result + element;
        }
        return result;
    }
    public static byte[] fromString ( String value ) {
        if ( value == null ) {
            return null;
        }
        if ( !value.startsWith ( "{" ) ) {
            throw new RuntimeException ( sm.getString ( "arrays.malformed.arrays" ) );
        }
        StringTokenizer t = new StringTokenizer ( value, "{,}", false );
        byte[] result = new byte[t.countTokens()];
        for ( int i = 0; i < result.length; i++ ) {
            result[i] = Byte.parseByte ( t.nextToken() );
        }
        return result;
    }
    public static byte[] convert ( String s ) {
        return s.getBytes ( StandardCharsets.ISO_8859_1 );
    }
}