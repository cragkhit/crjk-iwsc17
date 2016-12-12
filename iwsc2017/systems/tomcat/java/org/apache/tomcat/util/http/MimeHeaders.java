package org.apache.tomcat.util.http;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.res.StringManager;
public class MimeHeaders {
    public static final int DEFAULT_HEADER_SIZE = 8;
    private static final StringManager sm =
        StringManager.getManager ( "org.apache.tomcat.util.http" );
    private MimeHeaderField[] headers = new
    MimeHeaderField[DEFAULT_HEADER_SIZE];
    private int count;
    private int limit = -1;
    public MimeHeaders() {
    }
    public void setLimit ( int limit ) {
        this.limit = limit;
        if ( limit > 0 && headers.length > limit && count < limit ) {
            MimeHeaderField tmp[] = new MimeHeaderField[limit];
            System.arraycopy ( headers, 0, tmp, 0, count );
            headers = tmp;
        }
    }
    public void recycle() {
        clear();
    }
    public void clear() {
        for ( int i = 0; i < count; i++ ) {
            headers[i].recycle();
        }
        count = 0;
    }
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter ( sw );
        pw.println ( "=== MimeHeaders ===" );
        Enumeration<String> e = names();
        while ( e.hasMoreElements() ) {
            String n = e.nextElement();
            Enumeration<String> ev = values ( n );
            while ( ev.hasMoreElements() ) {
                pw.print ( n );
                pw.print ( " = " );
                pw.println ( ev.nextElement() );
            }
        }
        return sw.toString();
    }
    public void duplicate ( MimeHeaders source ) throws IOException {
        for ( int i = 0; i < source.size(); i++ ) {
            MimeHeaderField mhf = createHeader();
            mhf.getName().duplicate ( source.getName ( i ) );
            mhf.getValue().duplicate ( source.getValue ( i ) );
        }
    }
    public int size() {
        return count;
    }
    public MessageBytes getName ( int n ) {
        return n >= 0 && n < count ? headers[n].getName() : null;
    }
    public MessageBytes getValue ( int n ) {
        return n >= 0 && n < count ? headers[n].getValue() : null;
    }
    public int findHeader ( String name, int starting ) {
        for ( int i = starting; i < count; i++ ) {
            if ( headers[i].getName().equalsIgnoreCase ( name ) ) {
                return i;
            }
        }
        return -1;
    }
    public Enumeration<String> names() {
        return new NamesEnumerator ( this );
    }
    public Enumeration<String> values ( String name ) {
        return new ValuesEnumerator ( this, name );
    }
    private MimeHeaderField createHeader() {
        if ( limit > -1 && count >= limit ) {
            throw new IllegalStateException ( sm.getString (
                                                  "headers.maxCountFail", Integer.valueOf ( limit ) ) );
        }
        MimeHeaderField mh;
        int len = headers.length;
        if ( count >= len ) {
            int newLength = count * 2;
            if ( limit > 0 && newLength > limit ) {
                newLength = limit;
            }
            MimeHeaderField tmp[] = new MimeHeaderField[newLength];
            System.arraycopy ( headers, 0, tmp, 0, len );
            headers = tmp;
        }
        if ( ( mh = headers[count] ) == null ) {
            headers[count] = mh = new MimeHeaderField();
        }
        count++;
        return mh;
    }
    public MessageBytes addValue ( String name ) {
        MimeHeaderField mh = createHeader();
        mh.getName().setString ( name );
        return mh.getValue();
    }
    public MessageBytes addValue ( byte b[], int startN, int len ) {
        MimeHeaderField mhf = createHeader();
        mhf.getName().setBytes ( b, startN, len );
        return mhf.getValue();
    }
    public MessageBytes setValue ( String name ) {
        for ( int i = 0; i < count; i++ ) {
            if ( headers[i].getName().equalsIgnoreCase ( name ) ) {
                for ( int j = i + 1; j < count; j++ ) {
                    if ( headers[j].getName().equalsIgnoreCase ( name ) ) {
                        removeHeader ( j-- );
                    }
                }
                return headers[i].getValue();
            }
        }
        MimeHeaderField mh = createHeader();
        mh.getName().setString ( name );
        return mh.getValue();
    }
    public MessageBytes getValue ( String name ) {
        for ( int i = 0; i < count; i++ ) {
            if ( headers[i].getName().equalsIgnoreCase ( name ) ) {
                return headers[i].getValue();
            }
        }
        return null;
    }
    public MessageBytes getUniqueValue ( String name ) {
        MessageBytes result = null;
        for ( int i = 0; i < count; i++ ) {
            if ( headers[i].getName().equalsIgnoreCase ( name ) ) {
                if ( result == null ) {
                    result = headers[i].getValue();
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }
        return result;
    }
    public String getHeader ( String name ) {
        MessageBytes mh = getValue ( name );
        return mh != null ? mh.toString() : null;
    }
    public void removeHeader ( String name ) {
        for ( int i = 0; i < count; i++ ) {
            if ( headers[i].getName().equalsIgnoreCase ( name ) ) {
                removeHeader ( i-- );
            }
        }
    }
    private void removeHeader ( int idx ) {
        MimeHeaderField mh = headers[idx];
        mh.recycle();
        headers[idx] = headers[count - 1];
        headers[count - 1] = mh;
        count--;
    }
}
class NamesEnumerator implements Enumeration<String> {
    private int pos;
    private final int size;
    private String next;
    private final MimeHeaders headers;
    public NamesEnumerator ( MimeHeaders headers ) {
        this.headers = headers;
        pos = 0;
        size = headers.size();
        findNext();
    }
    private void findNext() {
        next = null;
        for ( ; pos < size; pos++ ) {
            next = headers.getName ( pos ).toString();
            for ( int j = 0; j < pos ; j++ ) {
                if ( headers.getName ( j ).equalsIgnoreCase ( next ) ) {
                    next = null;
                    break;
                }
            }
            if ( next != null ) {
                break;
            }
        }
        pos++;
    }
    @Override
    public boolean hasMoreElements() {
        return next != null;
    }
    @Override
    public String nextElement() {
        String current = next;
        findNext();
        return current;
    }
}
class ValuesEnumerator implements Enumeration<String> {
    private int pos;
    private final int size;
    private MessageBytes next;
    private final MimeHeaders headers;
    private final String name;
    ValuesEnumerator ( MimeHeaders headers, String name ) {
        this.name = name;
        this.headers = headers;
        pos = 0;
        size = headers.size();
        findNext();
    }
    private void findNext() {
        next = null;
        for ( ; pos < size; pos++ ) {
            MessageBytes n1 = headers.getName ( pos );
            if ( n1.equalsIgnoreCase ( name ) ) {
                next = headers.getValue ( pos );
                break;
            }
        }
        pos++;
    }
    @Override
    public boolean hasMoreElements() {
        return next != null;
    }
    @Override
    public String nextElement() {
        MessageBytes current = next;
        findNext();
        return current.toString();
    }
}
class MimeHeaderField {
    private final MessageBytes nameB = MessageBytes.newInstance();
    private final MessageBytes valueB = MessageBytes.newInstance();
    public MimeHeaderField() {
    }
    public void recycle() {
        nameB.recycle();
        valueB.recycle();
    }
    public MessageBytes getName() {
        return nameB;
    }
    public MessageBytes getValue() {
        return valueB;
    }
}
