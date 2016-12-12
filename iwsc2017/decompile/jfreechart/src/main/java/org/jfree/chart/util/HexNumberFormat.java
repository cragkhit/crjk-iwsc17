package org.jfree.chart.util;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.text.NumberFormat;
public class HexNumberFormat extends NumberFormat {
    public static final int BYTE = 2;
    public static final int WORD = 4;
    public static final int DWORD = 8;
    public static final int QWORD = 16;
    private int m_numDigits;
    public HexNumberFormat() {
        this ( 8 );
    }
    public HexNumberFormat ( final int digits ) {
        this.m_numDigits = 8;
        this.m_numDigits = digits;
    }
    public final int getNumberOfDigits() {
        return this.m_numDigits;
    }
    public void setNumberOfDigits ( final int digits ) {
        this.m_numDigits = digits;
    }
    @Override
    public StringBuffer format ( final double number, final StringBuffer toAppendTo, final FieldPosition pos ) {
        return this.format ( ( long ) number, toAppendTo, pos );
    }
    @Override
    public StringBuffer format ( final long number, final StringBuffer toAppendTo, final FieldPosition pos ) {
        final String l_hex = Long.toHexString ( number ).toUpperCase();
        int l_pad = this.m_numDigits - l_hex.length();
        l_pad = ( ( 0 < l_pad ) ? l_pad : 0 );
        final StringBuffer l_extended = new StringBuffer ( "0x" );
        for ( int i = 0; i < l_pad; ++i ) {
            l_extended.append ( 0 );
        }
        l_extended.append ( l_hex );
        return l_extended;
    }
    @Override
    public Number parse ( final String source, final ParsePosition parsePosition ) {
        return null;
    }
}
