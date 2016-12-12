package org.apache.catalina.tribes.util;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.List;
import org.apache.catalina.tribes.membership.Membership;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.catalina.tribes.group.AbsoluteOrder;
import java.util.Arrays;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
public class Arrays {
    protected static final StringManager sm;
    public static boolean contains ( final byte[] source, final int srcoffset, final byte[] key, final int keyoffset, final int length ) {
        if ( srcoffset < 0 || srcoffset >= source.length ) {
            throw new ArrayIndexOutOfBoundsException ( Arrays.sm.getString ( "arrays.srcoffset.outOfBounds" ) );
        }
        if ( keyoffset < 0 || keyoffset >= key.length ) {
            throw new ArrayIndexOutOfBoundsException ( Arrays.sm.getString ( "arrays.keyoffset.outOfBounds" ) );
        }
        if ( length > key.length - keyoffset ) {
            throw new ArrayIndexOutOfBoundsException ( Arrays.sm.getString ( "arrays.length.outOfBounds" ) );
        }
        if ( length > source.length - srcoffset ) {
            return false;
        }
        boolean match = true;
        for ( int pos = keyoffset, i = srcoffset; match && i < length; match = ( source[i] == key[pos++] ), ++i ) {}
        return match;
    }
    public static String toString ( final byte[] data ) {
        return toString ( data, 0, ( data != null ) ? data.length : 0 );
    }
    public static String toString ( final byte[] data, final int offset, final int length ) {
        return toString ( data, offset, length, false );
    }
    public static String toString ( final byte[] data, final int offset, final int length, final boolean unsigned ) {
        final StringBuilder buf = new StringBuilder ( "{" );
        if ( data != null && length > 0 ) {
            int i = offset;
            if ( unsigned ) {
                buf.append ( data[i++] & 0xFF );
                while ( i < length ) {
                    buf.append ( ", " ).append ( data[i] & 0xFF );
                    ++i;
                }
            } else {
                buf.append ( data[i++] );
                while ( i < length ) {
                    buf.append ( ", " ).append ( data[i] );
                    ++i;
                }
            }
        }
        buf.append ( "}" );
        return buf.toString();
    }
    public static String toString ( final Object[] data ) {
        return toString ( data, 0, ( data != null ) ? data.length : 0 );
    }
    public static String toString ( final Object[] data, int offset, final int length ) {
        final StringBuilder buf = new StringBuilder ( "{" );
        if ( data != null && length > 0 ) {
            buf.append ( data[offset++] );
            for ( int i = offset; i < length; ++i ) {
                buf.append ( ", " ).append ( data[i] );
            }
        }
        buf.append ( "}" );
        return buf.toString();
    }
    public static String toNameString ( final Member[] data ) {
        return toNameString ( data, 0, ( data != null ) ? data.length : 0 );
    }
    public static String toNameString ( final Member[] data, int offset, final int length ) {
        final StringBuilder buf = new StringBuilder ( "{" );
        if ( data != null && length > 0 ) {
            buf.append ( data[offset++].getName() );
            for ( int i = offset; i < length; ++i ) {
                buf.append ( ", " ).append ( data[i].getName() );
            }
        }
        buf.append ( "}" );
        return buf.toString();
    }
    public static int add ( final int[] data ) {
        int result = 0;
        for ( int i = 0; i < data.length; ++i ) {
            result += data[i];
        }
        return result;
    }
    public static UniqueId getUniqudId ( final ChannelMessage msg ) {
        return new UniqueId ( msg.getUniqueId() );
    }
    public static UniqueId getUniqudId ( final byte[] data ) {
        return new UniqueId ( data );
    }
    public static boolean equals ( final byte[] o1, final byte[] o2 ) {
        return java.util.Arrays.equals ( o1, o2 );
    }
    public static boolean equals ( final Object[] o1, final Object[] o2 ) {
        boolean result = o1.length == o2.length;
        if ( result ) {
            for ( int i = 0; i < o1.length && result; result = o1[i].equals ( o2[i] ), ++i ) {}
        }
        return result;
    }
    public static boolean sameMembers ( final Member[] m1, final Member[] m2 ) {
        AbsoluteOrder.absoluteOrder ( m1 );
        AbsoluteOrder.absoluteOrder ( m2 );
        return equals ( m1, m2 );
    }
    public static Member[] merge ( final Member[] m1, final Member[] m2 ) {
        AbsoluteOrder.absoluteOrder ( m1 );
        AbsoluteOrder.absoluteOrder ( m2 );
        final ArrayList<Member> list = new ArrayList<Member> ( java.util.Arrays.asList ( m1 ) );
        for ( int i = 0; i < m2.length; ++i ) {
            if ( !list.contains ( m2[i] ) ) {
                list.add ( m2[i] );
            }
        }
        final Member[] result = new Member[list.size()];
        list.toArray ( result );
        AbsoluteOrder.absoluteOrder ( result );
        return result;
    }
    public static void fill ( final Membership mbrship, final Member[] m ) {
        for ( int i = 0; i < m.length; ++i ) {
            mbrship.addMember ( m[i] );
        }
    }
    public static Member[] diff ( final Membership complete, final Membership local, final Member ignore ) {
        final ArrayList<Member> result = new ArrayList<Member>();
        final Member[] comp = complete.getMembers();
        for ( int i = 0; i < comp.length; ++i ) {
            if ( ignore == null || !ignore.equals ( comp[i] ) ) {
                if ( local.getMember ( comp[i] ) == null ) {
                    result.add ( comp[i] );
                }
            }
        }
        return result.toArray ( new Member[result.size()] );
    }
    public static Member[] remove ( final Member[] all, final Member remove ) {
        return extract ( all, new Member[] { remove } );
    }
    public static Member[] extract ( final Member[] all, final Member[] remove ) {
        final List<Member> alist = java.util.Arrays.asList ( all );
        final ArrayList<Member> list = new ArrayList<Member> ( alist );
        for ( int i = 0; i < remove.length; ++i ) {
            list.remove ( remove[i] );
        }
        return list.toArray ( new Member[list.size()] );
    }
    public static int indexOf ( final Member member, final Member[] members ) {
        int result = -1;
        for ( int i = 0; result == -1 && i < members.length; ++i ) {
            if ( member.equals ( members[i] ) ) {
                result = i;
            }
        }
        return result;
    }
    public static int nextIndex ( final Member member, final Member[] members ) {
        int idx = indexOf ( member, members ) + 1;
        if ( idx >= members.length ) {
            idx = ( ( members.length > 0 ) ? 0 : -1 );
        }
        return idx;
    }
    public static int hashCode ( final byte[] a ) {
        if ( a == null ) {
            return 0;
        }
        int result = 1;
        for ( int i = 0; i < a.length; ++i ) {
            final byte element = a[i];
            result = 31 * result + element;
        }
        return result;
    }
    public static byte[] fromString ( final String value ) {
        if ( value == null ) {
            return null;
        }
        if ( !value.startsWith ( "{" ) ) {
            throw new RuntimeException ( Arrays.sm.getString ( "arrays.malformed.arrays" ) );
        }
        final StringTokenizer t = new StringTokenizer ( value, "{,}", false );
        final byte[] result = new byte[t.countTokens()];
        for ( int i = 0; i < result.length; ++i ) {
            result[i] = Byte.parseByte ( t.nextToken() );
        }
        return result;
    }
    public static byte[] convert ( final String s ) {
        return s.getBytes ( StandardCharsets.ISO_8859_1 );
    }
    static {
        sm = StringManager.getManager ( Arrays.class );
    }
}
