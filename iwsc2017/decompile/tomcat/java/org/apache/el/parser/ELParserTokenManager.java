package org.apache.el.parser;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Deque;
public class ELParserTokenManager implements ELParserConstants {
    Deque<Integer> deque;
    public PrintStream debugStream;
    static final long[] jjbitVec0;
    static final long[] jjbitVec2;
    static final long[] jjbitVec3;
    static final long[] jjbitVec4;
    static final long[] jjbitVec5;
    static final long[] jjbitVec6;
    static final long[] jjbitVec7;
    static final long[] jjbitVec8;
    static final int[] jjnextStates;
    public static final String[] jjstrLiteralImages;
    public static final String[] lexStateNames;
    public static final int[] jjnewLexState;
    static final long[] jjtoToken;
    static final long[] jjtoSkip;
    protected SimpleCharStream input_stream;
    private final int[] jjrounds;
    private final int[] jjstateSet;
    private final StringBuilder jjimage;
    private StringBuilder image;
    private int jjimageLen;
    private int lengthOfMatch;
    protected char curChar;
    int curLexState;
    int defaultLexState;
    int jjnewStateCnt;
    int jjround;
    int jjmatchedPos;
    int jjmatchedKind;
    public void setDebugStream ( final PrintStream ds ) {
        this.debugStream = ds;
    }
    private final int jjStopStringLiteralDfa_0 ( final int pos, final long active0 ) {
        switch ( pos ) {
        case 0: {
            if ( ( active0 & 0xCL ) != 0x0L ) {
                this.jjmatchedKind = 1;
                return 5;
            }
            return -1;
        }
        default: {
            return -1;
        }
        }
    }
    private final int jjStartNfa_0 ( final int pos, final long active0 ) {
        return this.jjMoveNfa_0 ( this.jjStopStringLiteralDfa_0 ( pos, active0 ), pos + 1 );
    }
    private int jjStopAtPos ( final int pos, final int kind ) {
        this.jjmatchedKind = kind;
        return ( this.jjmatchedPos = pos ) + 1;
    }
    private int jjMoveStringLiteralDfa0_0() {
        switch ( this.curChar ) {
        case '#': {
            return this.jjMoveStringLiteralDfa1_0 ( 8L );
        }
        case '$': {
            return this.jjMoveStringLiteralDfa1_0 ( 4L );
        }
        default: {
            return this.jjMoveNfa_0 ( 7, 0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa1_0 ( final long active0 ) {
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_0 ( 0, active0 );
            return 1;
        }
        switch ( this.curChar ) {
        case '{': {
            if ( ( active0 & 0x4L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 2 );
            }
            if ( ( active0 & 0x8L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 3 );
            }
            break;
        }
        }
        return this.jjStartNfa_0 ( 0, active0 );
    }
    private int jjMoveNfa_0 ( final int startState, int curPos ) {
        int startsAt = 0;
        this.jjnewStateCnt = 8;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while ( true ) {
            if ( ++this.jjround == Integer.MAX_VALUE ) {
                this.ReInitRounds();
            }
            if ( this.curChar < '@' ) {
                final long l = 1L << this.curChar;
                do {
                    switch ( this.jjstateSet[--i] ) {
                    case 7: {
                        if ( ( 0xFFFFFFE7FFFFFFFFL & l ) != 0x0L ) {
                            if ( kind > 1 ) {
                                kind = 1;
                            }
                            this.jjCheckNAddStates ( 0, 4 );
                        } else if ( ( 0x1800000000L & l ) != 0x0L ) {
                            if ( kind > 1 ) {
                                kind = 1;
                            }
                            this.jjCheckNAdd ( 5 );
                        }
                        if ( ( 0xFFFFFFE7FFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddTwoStates ( 0, 1 );
                            continue;
                        }
                        continue;
                    }
                    case 0: {
                        if ( ( 0xFFFFFFE7FFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddTwoStates ( 0, 1 );
                            continue;
                        }
                        continue;
                    }
                    case 2: {
                        if ( ( 0xFFFFFFE7FFFFFFFFL & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 1 ) {
                            kind = 1;
                        }
                        this.jjCheckNAddStates ( 0, 4 );
                        continue;
                    }
                    case 3: {
                        if ( ( 0xFFFFFFE7FFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddTwoStates ( 3, 4 );
                            continue;
                        }
                        continue;
                    }
                    case 4: {
                        if ( ( 0x1800000000L & l ) != 0x0L ) {
                            this.jjCheckNAdd ( 5 );
                            continue;
                        }
                        continue;
                    }
                    case 5: {
                        if ( ( 0xFFFFFFE7FFFFFFFFL & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 1 ) {
                            kind = 1;
                        }
                        this.jjCheckNAddStates ( 5, 8 );
                        continue;
                    }
                    case 6: {
                        if ( ( 0x1800000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 1 ) {
                            kind = 1;
                        }
                        this.jjCheckNAddStates ( 9, 13 );
                        continue;
                    }
                    default: {
                        continue;
                    }
                    }
                } while ( i != startsAt );
            } else if ( this.curChar < '\u0080' ) {
                final long l = 1L << ( this.curChar & '?' );
                do {
                    switch ( this.jjstateSet[--i] ) {
                    case 7: {
                        if ( kind > 1 ) {
                            kind = 1;
                        }
                        this.jjCheckNAddStates ( 0, 4 );
                        if ( ( 0xFFFFFFFFEFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddTwoStates ( 0, 1 );
                            continue;
                        }
                        if ( this.curChar == '\\' ) {
                            if ( kind > 1 ) {
                                kind = 1;
                            }
                            this.jjCheckNAddStates ( 14, 17 );
                            continue;
                        }
                        continue;
                    }
                    case 0: {
                        if ( ( 0xFFFFFFFFEFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddTwoStates ( 0, 1 );
                            continue;
                        }
                        continue;
                    }
                    case 1: {
                        if ( this.curChar != '\\' ) {
                            continue;
                        }
                        if ( kind > 1 ) {
                            kind = 1;
                        }
                        this.jjCheckNAddStates ( 14, 17 );
                        continue;
                    }
                    case 5: {
                        if ( ( 0xF7FFFFFFEFFFFFFFL & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 1 ) {
                            kind = 1;
                        }
                        this.jjCheckNAddStates ( 5, 8 );
                        continue;
                    }
                    default: {
                        continue;
                    }
                    case 2: {
                        if ( kind > 1 ) {
                            kind = 1;
                        }
                        this.jjCheckNAddStates ( 0, 4 );
                        continue;
                    }
                    case 3: {
                        this.jjCheckNAddTwoStates ( 3, 4 );
                        continue;
                    }
                    }
                } while ( i != startsAt );
            } else {
                final int hiByte = this.curChar >> 8;
                final int i2 = hiByte >> 6;
                final long l2 = 1L << ( hiByte & 0x3F );
                final int i3 = ( this.curChar & '\u00ff' ) >> 6;
                final long l3 = 1L << ( this.curChar & '?' );
                do {
                    switch ( this.jjstateSet[--i] ) {
                    case 7: {
                        if ( jjCanMove_0 ( hiByte, i2, i3, l2, l3 ) ) {
                            this.jjCheckNAddTwoStates ( 0, 1 );
                        }
                        if ( jjCanMove_0 ( hiByte, i2, i3, l2, l3 ) ) {
                            if ( kind > 1 ) {
                                kind = 1;
                            }
                            this.jjCheckNAddStates ( 0, 4 );
                            continue;
                        }
                        continue;
                    }
                    case 0: {
                        if ( jjCanMove_0 ( hiByte, i2, i3, l2, l3 ) ) {
                            this.jjCheckNAddTwoStates ( 0, 1 );
                            continue;
                        }
                        continue;
                    }
                    case 2: {
                        if ( !jjCanMove_0 ( hiByte, i2, i3, l2, l3 ) ) {
                            continue;
                        }
                        if ( kind > 1 ) {
                            kind = 1;
                        }
                        this.jjCheckNAddStates ( 0, 4 );
                        continue;
                    }
                    case 3: {
                        if ( jjCanMove_0 ( hiByte, i2, i3, l2, l3 ) ) {
                            this.jjCheckNAddTwoStates ( 3, 4 );
                            continue;
                        }
                        continue;
                    }
                    case 5: {
                        if ( !jjCanMove_0 ( hiByte, i2, i3, l2, l3 ) ) {
                            continue;
                        }
                        if ( kind > 1 ) {
                            kind = 1;
                        }
                        this.jjCheckNAddStates ( 5, 8 );
                        continue;
                    }
                    default: {
                        continue;
                    }
                    }
                } while ( i != startsAt );
            }
            if ( kind != Integer.MAX_VALUE ) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            ++curPos;
            final int n = i = this.jjnewStateCnt;
            final int n2 = 8;
            final int jjnewStateCnt = startsAt;
            this.jjnewStateCnt = jjnewStateCnt;
            if ( n == ( startsAt = n2 - jjnewStateCnt ) ) {
                break;
            }
            try {
                this.curChar = this.input_stream.readChar();
            } catch ( IOException e ) {
                return curPos;
            }
        }
        return curPos;
    }
    private final int jjStopStringLiteralDfa_2 ( final int pos, final long active0 ) {
        switch ( pos ) {
        case 0: {
            if ( ( active0 & 0x20000L ) != 0x0L ) {
                return 1;
            }
            if ( ( active0 & 0x141D555401C000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                return 30;
            }
            return -1;
        }
        case 1: {
            if ( ( active0 & 0x41554000000L ) != 0x0L ) {
                return 30;
            }
            if ( ( active0 & 0x1419400001C000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 1;
                return 30;
            }
            return -1;
        }
        case 2: {
            if ( ( active0 & 0x14014000000000L ) != 0x0L ) {
                return 30;
            }
            if ( ( active0 & 0x18000001C000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 2;
                return 30;
            }
            return -1;
        }
        case 3: {
            if ( ( active0 & 0x14000L ) != 0x0L ) {
                return 30;
            }
            if ( ( active0 & 0x180000008000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 3;
                return 30;
            }
            return -1;
        }
        case 4: {
            if ( ( active0 & 0x80000008000L ) != 0x0L ) {
                return 30;
            }
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 4;
                return 30;
            }
            return -1;
        }
        case 5: {
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 5;
                return 30;
            }
            return -1;
        }
        case 6: {
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 6;
                return 30;
            }
            return -1;
        }
        case 7: {
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 7;
                return 30;
            }
            return -1;
        }
        case 8: {
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 8;
                return 30;
            }
            return -1;
        }
        default: {
            return -1;
        }
        }
    }
    private final int jjStartNfa_2 ( final int pos, final long active0 ) {
        return this.jjMoveNfa_2 ( this.jjStopStringLiteralDfa_2 ( pos, active0 ), pos + 1 );
    }
    private int jjMoveStringLiteralDfa0_2() {
        switch ( this.curChar ) {
        case '!': {
            this.jjmatchedKind = 37;
            return this.jjMoveStringLiteralDfa1_2 ( 34359738368L );
        }
        case '%': {
            return this.jjStopAtPos ( 0, 51 );
        }
        case '&': {
            return this.jjMoveStringLiteralDfa1_2 ( 549755813888L );
        }
        case '(': {
            return this.jjStopAtPos ( 0, 18 );
        }
        case ')': {
            return this.jjStopAtPos ( 0, 19 );
        }
        case '*': {
            return this.jjStopAtPos ( 0, 45 );
        }
        case '+': {
            this.jjmatchedKind = 46;
            return this.jjMoveStringLiteralDfa1_2 ( 9007199254740992L );
        }
        case ',': {
            return this.jjStopAtPos ( 0, 24 );
        }
        case '-': {
            this.jjmatchedKind = 47;
            return this.jjMoveStringLiteralDfa1_2 ( 36028797018963968L );
        }
        case '.': {
            return this.jjStartNfaWithStates_2 ( 0, 17, 1 );
        }
        case '/': {
            return this.jjStopAtPos ( 0, 49 );
        }
        case ':': {
            return this.jjStopAtPos ( 0, 22 );
        }
        case ';': {
            return this.jjStopAtPos ( 0, 23 );
        }
        case '<': {
            this.jjmatchedKind = 27;
            return this.jjMoveStringLiteralDfa1_2 ( 2147483648L );
        }
        case '=': {
            this.jjmatchedKind = 54;
            return this.jjMoveStringLiteralDfa1_2 ( 8589934592L );
        }
        case '>': {
            this.jjmatchedKind = 25;
            return this.jjMoveStringLiteralDfa1_2 ( 536870912L );
        }
        case '?': {
            return this.jjStopAtPos ( 0, 48 );
        }
        case '[': {
            return this.jjStopAtPos ( 0, 20 );
        }
        case ']': {
            return this.jjStopAtPos ( 0, 21 );
        }
        case 'a': {
            return this.jjMoveStringLiteralDfa1_2 ( 1099511627776L );
        }
        case 'd': {
            return this.jjMoveStringLiteralDfa1_2 ( 1125899906842624L );
        }
        case 'e': {
            return this.jjMoveStringLiteralDfa1_2 ( 8813272891392L );
        }
        case 'f': {
            return this.jjMoveStringLiteralDfa1_2 ( 32768L );
        }
        case 'g': {
            return this.jjMoveStringLiteralDfa1_2 ( 1140850688L );
        }
        case 'i': {
            return this.jjMoveStringLiteralDfa1_2 ( 17592186044416L );
        }
        case 'l': {
            return this.jjMoveStringLiteralDfa1_2 ( 4563402752L );
        }
        case 'm': {
            return this.jjMoveStringLiteralDfa1_2 ( 4503599627370496L );
        }
        case 'n': {
            return this.jjMoveStringLiteralDfa1_2 ( 343597449216L );
        }
        case 'o': {
            return this.jjMoveStringLiteralDfa1_2 ( 4398046511104L );
        }
        case 't': {
            return this.jjMoveStringLiteralDfa1_2 ( 16384L );
        }
        case '{': {
            return this.jjStopAtPos ( 0, 8 );
        }
        case '|': {
            return this.jjMoveStringLiteralDfa1_2 ( 2199023255552L );
        }
        case '}': {
            return this.jjStopAtPos ( 0, 9 );
        }
        default: {
            return this.jjMoveNfa_2 ( 0, 0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa1_2 ( final long active0 ) {
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_2 ( 0, active0 );
            return 1;
        }
        switch ( this.curChar ) {
        case '&': {
            if ( ( active0 & 0x8000000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 39 );
            }
            break;
        }
        case '=': {
            if ( ( active0 & 0x20000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 29 );
            }
            if ( ( active0 & 0x80000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 31 );
            }
            if ( ( active0 & 0x200000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 33 );
            }
            if ( ( active0 & 0x800000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 35 );
            }
            if ( ( active0 & 0x20000000000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 53 );
            }
            break;
        }
        case '>': {
            if ( ( active0 & 0x80000000000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 55 );
            }
            break;
        }
        case 'a': {
            return this.jjMoveStringLiteralDfa2_2 ( active0, 32768L );
        }
        case 'e': {
            if ( ( active0 & 0x40000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 1, 30, 30 );
            }
            if ( ( active0 & 0x100000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 1, 32, 30 );
            }
            if ( ( active0 & 0x1000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 1, 36, 30 );
            }
            break;
        }
        case 'i': {
            return this.jjMoveStringLiteralDfa2_2 ( active0, 1125899906842624L );
        }
        case 'm': {
            return this.jjMoveStringLiteralDfa2_2 ( active0, 8796093022208L );
        }
        case 'n': {
            return this.jjMoveStringLiteralDfa2_2 ( active0, 18691697672192L );
        }
        case 'o': {
            return this.jjMoveStringLiteralDfa2_2 ( active0, 4503874505277440L );
        }
        case 'q': {
            if ( ( active0 & 0x400000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 1, 34, 30 );
            }
            break;
        }
        case 'r': {
            if ( ( active0 & 0x40000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 1, 42, 30 );
            }
            return this.jjMoveStringLiteralDfa2_2 ( active0, 16384L );
        }
        case 't': {
            if ( ( active0 & 0x4000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 1, 26, 30 );
            }
            if ( ( active0 & 0x10000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 1, 28, 30 );
            }
            break;
        }
        case 'u': {
            return this.jjMoveStringLiteralDfa2_2 ( active0, 65536L );
        }
        case '|': {
            if ( ( active0 & 0x20000000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 41 );
            }
            break;
        }
        }
        return this.jjStartNfa_2 ( 0, active0 );
    }
    private int jjMoveStringLiteralDfa2_2 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_2 ( 0, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_2 ( 1, active0 );
            return 2;
        }
        switch ( this.curChar ) {
        case 'd': {
            if ( ( active0 & 0x10000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 2, 40, 30 );
            }
            if ( ( active0 & 0x10000000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 2, 52, 30 );
            }
            break;
        }
        case 'l': {
            return this.jjMoveStringLiteralDfa3_2 ( active0, 98304L );
        }
        case 'p': {
            return this.jjMoveStringLiteralDfa3_2 ( active0, 8796093022208L );
        }
        case 's': {
            return this.jjMoveStringLiteralDfa3_2 ( active0, 17592186044416L );
        }
        case 't': {
            if ( ( active0 & 0x4000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 2, 38, 30 );
            }
            break;
        }
        case 'u': {
            return this.jjMoveStringLiteralDfa3_2 ( active0, 16384L );
        }
        case 'v': {
            if ( ( active0 & 0x4000000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 2, 50, 30 );
            }
            break;
        }
        }
        return this.jjStartNfa_2 ( 1, active0 );
    }
    private int jjMoveStringLiteralDfa3_2 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_2 ( 1, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_2 ( 2, active0 );
            return 3;
        }
        switch ( this.curChar ) {
        case 'e': {
            if ( ( active0 & 0x4000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 3, 14, 30 );
            }
            break;
        }
        case 'l': {
            if ( ( active0 & 0x10000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 3, 16, 30 );
            }
            break;
        }
        case 's': {
            return this.jjMoveStringLiteralDfa4_2 ( active0, 32768L );
        }
        case 't': {
            return this.jjMoveStringLiteralDfa4_2 ( active0, 26388279066624L );
        }
        }
        return this.jjStartNfa_2 ( 2, active0 );
    }
    private int jjMoveStringLiteralDfa4_2 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_2 ( 2, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_2 ( 3, active0 );
            return 4;
        }
        switch ( this.curChar ) {
        case 'a': {
            return this.jjMoveStringLiteralDfa5_2 ( active0, 17592186044416L );
        }
        case 'e': {
            if ( ( active0 & 0x8000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 4, 15, 30 );
            }
            break;
        }
        case 'y': {
            if ( ( active0 & 0x80000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 4, 43, 30 );
            }
            break;
        }
        }
        return this.jjStartNfa_2 ( 3, active0 );
    }
    private int jjMoveStringLiteralDfa5_2 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_2 ( 3, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_2 ( 4, active0 );
            return 5;
        }
        switch ( this.curChar ) {
        case 'n': {
            return this.jjMoveStringLiteralDfa6_2 ( active0, 17592186044416L );
        }
        default: {
            return this.jjStartNfa_2 ( 4, active0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa6_2 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_2 ( 4, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_2 ( 5, active0 );
            return 6;
        }
        switch ( this.curChar ) {
        case 'c': {
            return this.jjMoveStringLiteralDfa7_2 ( active0, 17592186044416L );
        }
        default: {
            return this.jjStartNfa_2 ( 5, active0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa7_2 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_2 ( 5, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_2 ( 6, active0 );
            return 7;
        }
        switch ( this.curChar ) {
        case 'e': {
            return this.jjMoveStringLiteralDfa8_2 ( active0, 17592186044416L );
        }
        default: {
            return this.jjStartNfa_2 ( 6, active0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa8_2 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_2 ( 6, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_2 ( 7, active0 );
            return 8;
        }
        switch ( this.curChar ) {
        case 'o': {
            return this.jjMoveStringLiteralDfa9_2 ( active0, 17592186044416L );
        }
        default: {
            return this.jjStartNfa_2 ( 7, active0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa9_2 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_2 ( 7, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_2 ( 8, active0 );
            return 9;
        }
        switch ( this.curChar ) {
        case 'f': {
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_2 ( 9, 44, 30 );
            }
            break;
        }
        }
        return this.jjStartNfa_2 ( 8, active0 );
    }
    private int jjStartNfaWithStates_2 ( final int pos, final int kind, final int state ) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            return pos + 1;
        }
        return this.jjMoveNfa_2 ( state, pos + 1 );
    }
    private int jjMoveNfa_2 ( final int startState, int curPos ) {
        int startsAt = 0;
        this.jjnewStateCnt = 30;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while ( true ) {
            if ( ++this.jjround == Integer.MAX_VALUE ) {
                this.ReInitRounds();
            }
            if ( this.curChar < '@' ) {
                final long l = 1L << this.curChar;
                do {
                    switch ( this.jjstateSet[--i] ) {
                    case 0: {
                        if ( ( 0x3FF000000000000L & l ) != 0x0L ) {
                            if ( kind > 10 ) {
                                kind = 10;
                            }
                            this.jjCheckNAddStates ( 18, 22 );
                            continue;
                        }
                        if ( ( 0x1800000000L & l ) != 0x0L ) {
                            if ( kind > 56 ) {
                                kind = 56;
                            }
                            this.jjCheckNAddTwoStates ( 28, 29 );
                            continue;
                        }
                        if ( this.curChar == '\'' ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        if ( this.curChar == '\"' ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        if ( this.curChar == '.' ) {
                            this.jjCheckNAdd ( 1 );
                            continue;
                        }
                        continue;
                    }
                    case 30: {
                        if ( ( 0x3FF001000000000L & l ) != 0x0L ) {
                            if ( kind > 57 ) {
                                kind = 57;
                            }
                            this.jjCheckNAdd ( 29 );
                        }
                        if ( ( 0x3FF001000000000L & l ) != 0x0L ) {
                            if ( kind > 56 ) {
                                kind = 56;
                            }
                            this.jjCheckNAdd ( 28 );
                            continue;
                        }
                        continue;
                    }
                    case 1: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAddTwoStates ( 1, 2 );
                        continue;
                    }
                    case 3: {
                        if ( ( 0x280000000000L & l ) != 0x0L ) {
                            this.jjCheckNAdd ( 4 );
                            continue;
                        }
                        continue;
                    }
                    case 4: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAdd ( 4 );
                        continue;
                    }
                    case 5: {
                        if ( this.curChar == '\"' ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 6: {
                        if ( ( 0xFFFFFFFBFFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 8: {
                        if ( ( 0x8400000000L & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 9: {
                        if ( this.curChar == '\"' && kind > 13 ) {
                            kind = 13;
                            continue;
                        }
                        continue;
                    }
                    case 10: {
                        if ( this.curChar == '\'' ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 11: {
                        if ( ( 0xFFFFFF7FFFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 13: {
                        if ( ( 0x8400000000L & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 14: {
                        if ( this.curChar == '\'' && kind > 13 ) {
                            kind = 13;
                            continue;
                        }
                        continue;
                    }
                    case 15: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 10 ) {
                            kind = 10;
                        }
                        this.jjCheckNAddStates ( 18, 22 );
                        continue;
                    }
                    case 16: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 10 ) {
                            kind = 10;
                        }
                        this.jjCheckNAdd ( 16 );
                        continue;
                    }
                    case 17: {
                        if ( ( 0x3FF000000000000L & l ) != 0x0L ) {
                            this.jjCheckNAddTwoStates ( 17, 18 );
                            continue;
                        }
                        continue;
                    }
                    case 18: {
                        if ( this.curChar != '.' ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAddTwoStates ( 19, 20 );
                        continue;
                    }
                    case 19: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAddTwoStates ( 19, 20 );
                        continue;
                    }
                    case 21: {
                        if ( ( 0x280000000000L & l ) != 0x0L ) {
                            this.jjCheckNAdd ( 22 );
                            continue;
                        }
                        continue;
                    }
                    case 22: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAdd ( 22 );
                        continue;
                    }
                    case 23: {
                        if ( ( 0x3FF000000000000L & l ) != 0x0L ) {
                            this.jjCheckNAddTwoStates ( 23, 24 );
                            continue;
                        }
                        continue;
                    }
                    case 25: {
                        if ( ( 0x280000000000L & l ) != 0x0L ) {
                            this.jjCheckNAdd ( 26 );
                            continue;
                        }
                        continue;
                    }
                    case 26: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAdd ( 26 );
                        continue;
                    }
                    case 27: {
                        if ( ( 0x1800000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAddTwoStates ( 28, 29 );
                        continue;
                    }
                    case 28: {
                        if ( ( 0x3FF001000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAdd ( 28 );
                        continue;
                    }
                    case 29: {
                        if ( ( 0x3FF001000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 57 ) {
                            kind = 57;
                        }
                        this.jjCheckNAdd ( 29 );
                        continue;
                    }
                    default: {
                        continue;
                    }
                    }
                } while ( i != startsAt );
            } else if ( this.curChar < '\u0080' ) {
                final long l = 1L << ( this.curChar & '?' );
                do {
                    switch ( this.jjstateSet[--i] ) {
                    case 0: {
                        if ( ( 0x7FFFFFE87FFFFFEL & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAddTwoStates ( 28, 29 );
                        continue;
                    }
                    case 30: {
                        if ( ( 0x7FFFFFE87FFFFFEL & l ) != 0x0L ) {
                            if ( kind > 57 ) {
                                kind = 57;
                            }
                            this.jjCheckNAdd ( 29 );
                        }
                        if ( ( 0x7FFFFFE87FFFFFEL & l ) != 0x0L ) {
                            if ( kind > 56 ) {
                                kind = 56;
                            }
                            this.jjCheckNAdd ( 28 );
                            continue;
                        }
                        continue;
                    }
                    case 2: {
                        if ( ( 0x2000000020L & l ) != 0x0L ) {
                            this.jjAddStates ( 29, 30 );
                            continue;
                        }
                        continue;
                    }
                    case 6: {
                        if ( ( 0xFFFFFFFFEFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 7: {
                        if ( this.curChar == '\\' ) {
                            this.jjstateSet[this.jjnewStateCnt++] = 8;
                            continue;
                        }
                        continue;
                    }
                    case 8: {
                        if ( this.curChar == '\\' ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 11: {
                        if ( ( 0xFFFFFFFFEFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 12: {
                        if ( this.curChar == '\\' ) {
                            this.jjstateSet[this.jjnewStateCnt++] = 13;
                            continue;
                        }
                        continue;
                    }
                    case 13: {
                        if ( this.curChar == '\\' ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 20: {
                        if ( ( 0x2000000020L & l ) != 0x0L ) {
                            this.jjAddStates ( 31, 32 );
                            continue;
                        }
                        continue;
                    }
                    case 24: {
                        if ( ( 0x2000000020L & l ) != 0x0L ) {
                            this.jjAddStates ( 33, 34 );
                            continue;
                        }
                        continue;
                    }
                    case 28: {
                        if ( ( 0x7FFFFFE87FFFFFEL & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAdd ( 28 );
                        continue;
                    }
                    case 29: {
                        if ( ( 0x7FFFFFE87FFFFFEL & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 57 ) {
                            kind = 57;
                        }
                        this.jjCheckNAdd ( 29 );
                        continue;
                    }
                    default: {
                        continue;
                    }
                    }
                } while ( i != startsAt );
            } else {
                final int hiByte = this.curChar >> 8;
                final int i2 = hiByte >> 6;
                final long l2 = 1L << ( hiByte & 0x3F );
                final int i3 = ( this.curChar & '\u00ff' ) >> 6;
                final long l3 = 1L << ( this.curChar & '?' );
                do {
                    switch ( this.jjstateSet[--i] ) {
                    case 0: {
                        if ( !jjCanMove_1 ( hiByte, i2, i3, l2, l3 ) ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAddTwoStates ( 28, 29 );
                        continue;
                    }
                    case 30: {
                        if ( jjCanMove_1 ( hiByte, i2, i3, l2, l3 ) ) {
                            if ( kind > 56 ) {
                                kind = 56;
                            }
                            this.jjCheckNAdd ( 28 );
                        }
                        if ( jjCanMove_1 ( hiByte, i2, i3, l2, l3 ) ) {
                            if ( kind > 57 ) {
                                kind = 57;
                            }
                            this.jjCheckNAdd ( 29 );
                            continue;
                        }
                        continue;
                    }
                    case 6: {
                        if ( jjCanMove_0 ( hiByte, i2, i3, l2, l3 ) ) {
                            this.jjAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 11: {
                        if ( jjCanMove_0 ( hiByte, i2, i3, l2, l3 ) ) {
                            this.jjAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 28: {
                        if ( !jjCanMove_1 ( hiByte, i2, i3, l2, l3 ) ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAdd ( 28 );
                        continue;
                    }
                    case 29: {
                        if ( !jjCanMove_1 ( hiByte, i2, i3, l2, l3 ) ) {
                            continue;
                        }
                        if ( kind > 57 ) {
                            kind = 57;
                        }
                        this.jjCheckNAdd ( 29 );
                        continue;
                    }
                    default: {
                        continue;
                    }
                    }
                } while ( i != startsAt );
            }
            if ( kind != Integer.MAX_VALUE ) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            ++curPos;
            final int n = i = this.jjnewStateCnt;
            final int n2 = 30;
            final int jjnewStateCnt = startsAt;
            this.jjnewStateCnt = jjnewStateCnt;
            if ( n == ( startsAt = n2 - jjnewStateCnt ) ) {
                break;
            }
            try {
                this.curChar = this.input_stream.readChar();
            } catch ( IOException e ) {
                return curPos;
            }
        }
        return curPos;
    }
    private final int jjStopStringLiteralDfa_1 ( final int pos, final long active0 ) {
        switch ( pos ) {
        case 0: {
            if ( ( active0 & 0x20000L ) != 0x0L ) {
                return 1;
            }
            if ( ( active0 & 0x141D555401C000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                return 30;
            }
            return -1;
        }
        case 1: {
            if ( ( active0 & 0x41554000000L ) != 0x0L ) {
                return 30;
            }
            if ( ( active0 & 0x1419400001C000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 1;
                return 30;
            }
            return -1;
        }
        case 2: {
            if ( ( active0 & 0x14014000000000L ) != 0x0L ) {
                return 30;
            }
            if ( ( active0 & 0x18000001C000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 2;
                return 30;
            }
            return -1;
        }
        case 3: {
            if ( ( active0 & 0x14000L ) != 0x0L ) {
                return 30;
            }
            if ( ( active0 & 0x180000008000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 3;
                return 30;
            }
            return -1;
        }
        case 4: {
            if ( ( active0 & 0x80000008000L ) != 0x0L ) {
                return 30;
            }
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 4;
                return 30;
            }
            return -1;
        }
        case 5: {
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 5;
                return 30;
            }
            return -1;
        }
        case 6: {
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 6;
                return 30;
            }
            return -1;
        }
        case 7: {
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 7;
                return 30;
            }
            return -1;
        }
        case 8: {
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                this.jjmatchedKind = 56;
                this.jjmatchedPos = 8;
                return 30;
            }
            return -1;
        }
        default: {
            return -1;
        }
        }
    }
    private final int jjStartNfa_1 ( final int pos, final long active0 ) {
        return this.jjMoveNfa_1 ( this.jjStopStringLiteralDfa_1 ( pos, active0 ), pos + 1 );
    }
    private int jjMoveStringLiteralDfa0_1() {
        switch ( this.curChar ) {
        case '!': {
            this.jjmatchedKind = 37;
            return this.jjMoveStringLiteralDfa1_1 ( 34359738368L );
        }
        case '%': {
            return this.jjStopAtPos ( 0, 51 );
        }
        case '&': {
            return this.jjMoveStringLiteralDfa1_1 ( 549755813888L );
        }
        case '(': {
            return this.jjStopAtPos ( 0, 18 );
        }
        case ')': {
            return this.jjStopAtPos ( 0, 19 );
        }
        case '*': {
            return this.jjStopAtPos ( 0, 45 );
        }
        case '+': {
            this.jjmatchedKind = 46;
            return this.jjMoveStringLiteralDfa1_1 ( 9007199254740992L );
        }
        case ',': {
            return this.jjStopAtPos ( 0, 24 );
        }
        case '-': {
            this.jjmatchedKind = 47;
            return this.jjMoveStringLiteralDfa1_1 ( 36028797018963968L );
        }
        case '.': {
            return this.jjStartNfaWithStates_1 ( 0, 17, 1 );
        }
        case '/': {
            return this.jjStopAtPos ( 0, 49 );
        }
        case ':': {
            return this.jjStopAtPos ( 0, 22 );
        }
        case ';': {
            return this.jjStopAtPos ( 0, 23 );
        }
        case '<': {
            this.jjmatchedKind = 27;
            return this.jjMoveStringLiteralDfa1_1 ( 2147483648L );
        }
        case '=': {
            this.jjmatchedKind = 54;
            return this.jjMoveStringLiteralDfa1_1 ( 8589934592L );
        }
        case '>': {
            this.jjmatchedKind = 25;
            return this.jjMoveStringLiteralDfa1_1 ( 536870912L );
        }
        case '?': {
            return this.jjStopAtPos ( 0, 48 );
        }
        case '[': {
            return this.jjStopAtPos ( 0, 20 );
        }
        case ']': {
            return this.jjStopAtPos ( 0, 21 );
        }
        case 'a': {
            return this.jjMoveStringLiteralDfa1_1 ( 1099511627776L );
        }
        case 'd': {
            return this.jjMoveStringLiteralDfa1_1 ( 1125899906842624L );
        }
        case 'e': {
            return this.jjMoveStringLiteralDfa1_1 ( 8813272891392L );
        }
        case 'f': {
            return this.jjMoveStringLiteralDfa1_1 ( 32768L );
        }
        case 'g': {
            return this.jjMoveStringLiteralDfa1_1 ( 1140850688L );
        }
        case 'i': {
            return this.jjMoveStringLiteralDfa1_1 ( 17592186044416L );
        }
        case 'l': {
            return this.jjMoveStringLiteralDfa1_1 ( 4563402752L );
        }
        case 'm': {
            return this.jjMoveStringLiteralDfa1_1 ( 4503599627370496L );
        }
        case 'n': {
            return this.jjMoveStringLiteralDfa1_1 ( 343597449216L );
        }
        case 'o': {
            return this.jjMoveStringLiteralDfa1_1 ( 4398046511104L );
        }
        case 't': {
            return this.jjMoveStringLiteralDfa1_1 ( 16384L );
        }
        case '{': {
            return this.jjStopAtPos ( 0, 8 );
        }
        case '|': {
            return this.jjMoveStringLiteralDfa1_1 ( 2199023255552L );
        }
        case '}': {
            return this.jjStopAtPos ( 0, 9 );
        }
        default: {
            return this.jjMoveNfa_1 ( 0, 0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa1_1 ( final long active0 ) {
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_1 ( 0, active0 );
            return 1;
        }
        switch ( this.curChar ) {
        case '&': {
            if ( ( active0 & 0x8000000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 39 );
            }
            break;
        }
        case '=': {
            if ( ( active0 & 0x20000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 29 );
            }
            if ( ( active0 & 0x80000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 31 );
            }
            if ( ( active0 & 0x200000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 33 );
            }
            if ( ( active0 & 0x800000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 35 );
            }
            if ( ( active0 & 0x20000000000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 53 );
            }
            break;
        }
        case '>': {
            if ( ( active0 & 0x80000000000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 55 );
            }
            break;
        }
        case 'a': {
            return this.jjMoveStringLiteralDfa2_1 ( active0, 32768L );
        }
        case 'e': {
            if ( ( active0 & 0x40000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 1, 30, 30 );
            }
            if ( ( active0 & 0x100000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 1, 32, 30 );
            }
            if ( ( active0 & 0x1000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 1, 36, 30 );
            }
            break;
        }
        case 'i': {
            return this.jjMoveStringLiteralDfa2_1 ( active0, 1125899906842624L );
        }
        case 'm': {
            return this.jjMoveStringLiteralDfa2_1 ( active0, 8796093022208L );
        }
        case 'n': {
            return this.jjMoveStringLiteralDfa2_1 ( active0, 18691697672192L );
        }
        case 'o': {
            return this.jjMoveStringLiteralDfa2_1 ( active0, 4503874505277440L );
        }
        case 'q': {
            if ( ( active0 & 0x400000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 1, 34, 30 );
            }
            break;
        }
        case 'r': {
            if ( ( active0 & 0x40000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 1, 42, 30 );
            }
            return this.jjMoveStringLiteralDfa2_1 ( active0, 16384L );
        }
        case 't': {
            if ( ( active0 & 0x4000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 1, 26, 30 );
            }
            if ( ( active0 & 0x10000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 1, 28, 30 );
            }
            break;
        }
        case 'u': {
            return this.jjMoveStringLiteralDfa2_1 ( active0, 65536L );
        }
        case '|': {
            if ( ( active0 & 0x20000000000L ) != 0x0L ) {
                return this.jjStopAtPos ( 1, 41 );
            }
            break;
        }
        }
        return this.jjStartNfa_1 ( 0, active0 );
    }
    private int jjMoveStringLiteralDfa2_1 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_1 ( 0, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_1 ( 1, active0 );
            return 2;
        }
        switch ( this.curChar ) {
        case 'd': {
            if ( ( active0 & 0x10000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 2, 40, 30 );
            }
            if ( ( active0 & 0x10000000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 2, 52, 30 );
            }
            break;
        }
        case 'l': {
            return this.jjMoveStringLiteralDfa3_1 ( active0, 98304L );
        }
        case 'p': {
            return this.jjMoveStringLiteralDfa3_1 ( active0, 8796093022208L );
        }
        case 's': {
            return this.jjMoveStringLiteralDfa3_1 ( active0, 17592186044416L );
        }
        case 't': {
            if ( ( active0 & 0x4000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 2, 38, 30 );
            }
            break;
        }
        case 'u': {
            return this.jjMoveStringLiteralDfa3_1 ( active0, 16384L );
        }
        case 'v': {
            if ( ( active0 & 0x4000000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 2, 50, 30 );
            }
            break;
        }
        }
        return this.jjStartNfa_1 ( 1, active0 );
    }
    private int jjMoveStringLiteralDfa3_1 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_1 ( 1, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_1 ( 2, active0 );
            return 3;
        }
        switch ( this.curChar ) {
        case 'e': {
            if ( ( active0 & 0x4000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 3, 14, 30 );
            }
            break;
        }
        case 'l': {
            if ( ( active0 & 0x10000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 3, 16, 30 );
            }
            break;
        }
        case 's': {
            return this.jjMoveStringLiteralDfa4_1 ( active0, 32768L );
        }
        case 't': {
            return this.jjMoveStringLiteralDfa4_1 ( active0, 26388279066624L );
        }
        }
        return this.jjStartNfa_1 ( 2, active0 );
    }
    private int jjMoveStringLiteralDfa4_1 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_1 ( 2, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_1 ( 3, active0 );
            return 4;
        }
        switch ( this.curChar ) {
        case 'a': {
            return this.jjMoveStringLiteralDfa5_1 ( active0, 17592186044416L );
        }
        case 'e': {
            if ( ( active0 & 0x8000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 4, 15, 30 );
            }
            break;
        }
        case 'y': {
            if ( ( active0 & 0x80000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 4, 43, 30 );
            }
            break;
        }
        }
        return this.jjStartNfa_1 ( 3, active0 );
    }
    private int jjMoveStringLiteralDfa5_1 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_1 ( 3, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_1 ( 4, active0 );
            return 5;
        }
        switch ( this.curChar ) {
        case 'n': {
            return this.jjMoveStringLiteralDfa6_1 ( active0, 17592186044416L );
        }
        default: {
            return this.jjStartNfa_1 ( 4, active0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa6_1 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_1 ( 4, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_1 ( 5, active0 );
            return 6;
        }
        switch ( this.curChar ) {
        case 'c': {
            return this.jjMoveStringLiteralDfa7_1 ( active0, 17592186044416L );
        }
        default: {
            return this.jjStartNfa_1 ( 5, active0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa7_1 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_1 ( 5, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_1 ( 6, active0 );
            return 7;
        }
        switch ( this.curChar ) {
        case 'e': {
            return this.jjMoveStringLiteralDfa8_1 ( active0, 17592186044416L );
        }
        default: {
            return this.jjStartNfa_1 ( 6, active0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa8_1 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_1 ( 6, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_1 ( 7, active0 );
            return 8;
        }
        switch ( this.curChar ) {
        case 'o': {
            return this.jjMoveStringLiteralDfa9_1 ( active0, 17592186044416L );
        }
        default: {
            return this.jjStartNfa_1 ( 7, active0 );
        }
        }
    }
    private int jjMoveStringLiteralDfa9_1 ( final long old0, long active0 ) {
        if ( ( active0 &= old0 ) == 0x0L ) {
            return this.jjStartNfa_1 ( 7, old0 );
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            this.jjStopStringLiteralDfa_1 ( 8, active0 );
            return 9;
        }
        switch ( this.curChar ) {
        case 'f': {
            if ( ( active0 & 0x100000000000L ) != 0x0L ) {
                return this.jjStartNfaWithStates_1 ( 9, 44, 30 );
            }
            break;
        }
        }
        return this.jjStartNfa_1 ( 8, active0 );
    }
    private int jjStartNfaWithStates_1 ( final int pos, final int kind, final int state ) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        try {
            this.curChar = this.input_stream.readChar();
        } catch ( IOException e ) {
            return pos + 1;
        }
        return this.jjMoveNfa_1 ( state, pos + 1 );
    }
    private int jjMoveNfa_1 ( final int startState, int curPos ) {
        int startsAt = 0;
        this.jjnewStateCnt = 30;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while ( true ) {
            if ( ++this.jjround == Integer.MAX_VALUE ) {
                this.ReInitRounds();
            }
            if ( this.curChar < '@' ) {
                final long l = 1L << this.curChar;
                do {
                    switch ( this.jjstateSet[--i] ) {
                    case 0: {
                        if ( ( 0x3FF000000000000L & l ) != 0x0L ) {
                            if ( kind > 10 ) {
                                kind = 10;
                            }
                            this.jjCheckNAddStates ( 18, 22 );
                            continue;
                        }
                        if ( ( 0x1800000000L & l ) != 0x0L ) {
                            if ( kind > 56 ) {
                                kind = 56;
                            }
                            this.jjCheckNAddTwoStates ( 28, 29 );
                            continue;
                        }
                        if ( this.curChar == '\'' ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        if ( this.curChar == '\"' ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        if ( this.curChar == '.' ) {
                            this.jjCheckNAdd ( 1 );
                            continue;
                        }
                        continue;
                    }
                    case 30: {
                        if ( ( 0x3FF001000000000L & l ) != 0x0L ) {
                            if ( kind > 57 ) {
                                kind = 57;
                            }
                            this.jjCheckNAdd ( 29 );
                        }
                        if ( ( 0x3FF001000000000L & l ) != 0x0L ) {
                            if ( kind > 56 ) {
                                kind = 56;
                            }
                            this.jjCheckNAdd ( 28 );
                            continue;
                        }
                        continue;
                    }
                    case 1: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAddTwoStates ( 1, 2 );
                        continue;
                    }
                    case 3: {
                        if ( ( 0x280000000000L & l ) != 0x0L ) {
                            this.jjCheckNAdd ( 4 );
                            continue;
                        }
                        continue;
                    }
                    case 4: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAdd ( 4 );
                        continue;
                    }
                    case 5: {
                        if ( this.curChar == '\"' ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 6: {
                        if ( ( 0xFFFFFFFBFFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 8: {
                        if ( ( 0x8400000000L & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 9: {
                        if ( this.curChar == '\"' && kind > 13 ) {
                            kind = 13;
                            continue;
                        }
                        continue;
                    }
                    case 10: {
                        if ( this.curChar == '\'' ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 11: {
                        if ( ( 0xFFFFFF7FFFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 13: {
                        if ( ( 0x8400000000L & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 14: {
                        if ( this.curChar == '\'' && kind > 13 ) {
                            kind = 13;
                            continue;
                        }
                        continue;
                    }
                    case 15: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 10 ) {
                            kind = 10;
                        }
                        this.jjCheckNAddStates ( 18, 22 );
                        continue;
                    }
                    case 16: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 10 ) {
                            kind = 10;
                        }
                        this.jjCheckNAdd ( 16 );
                        continue;
                    }
                    case 17: {
                        if ( ( 0x3FF000000000000L & l ) != 0x0L ) {
                            this.jjCheckNAddTwoStates ( 17, 18 );
                            continue;
                        }
                        continue;
                    }
                    case 18: {
                        if ( this.curChar != '.' ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAddTwoStates ( 19, 20 );
                        continue;
                    }
                    case 19: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAddTwoStates ( 19, 20 );
                        continue;
                    }
                    case 21: {
                        if ( ( 0x280000000000L & l ) != 0x0L ) {
                            this.jjCheckNAdd ( 22 );
                            continue;
                        }
                        continue;
                    }
                    case 22: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAdd ( 22 );
                        continue;
                    }
                    case 23: {
                        if ( ( 0x3FF000000000000L & l ) != 0x0L ) {
                            this.jjCheckNAddTwoStates ( 23, 24 );
                            continue;
                        }
                        continue;
                    }
                    case 25: {
                        if ( ( 0x280000000000L & l ) != 0x0L ) {
                            this.jjCheckNAdd ( 26 );
                            continue;
                        }
                        continue;
                    }
                    case 26: {
                        if ( ( 0x3FF000000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 11 ) {
                            kind = 11;
                        }
                        this.jjCheckNAdd ( 26 );
                        continue;
                    }
                    case 27: {
                        if ( ( 0x1800000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAddTwoStates ( 28, 29 );
                        continue;
                    }
                    case 28: {
                        if ( ( 0x3FF001000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAdd ( 28 );
                        continue;
                    }
                    case 29: {
                        if ( ( 0x3FF001000000000L & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 57 ) {
                            kind = 57;
                        }
                        this.jjCheckNAdd ( 29 );
                        continue;
                    }
                    default: {
                        continue;
                    }
                    }
                } while ( i != startsAt );
            } else if ( this.curChar < '\u0080' ) {
                final long l = 1L << ( this.curChar & '?' );
                do {
                    switch ( this.jjstateSet[--i] ) {
                    case 0: {
                        if ( ( 0x7FFFFFE87FFFFFEL & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAddTwoStates ( 28, 29 );
                        continue;
                    }
                    case 30: {
                        if ( ( 0x7FFFFFE87FFFFFEL & l ) != 0x0L ) {
                            if ( kind > 57 ) {
                                kind = 57;
                            }
                            this.jjCheckNAdd ( 29 );
                        }
                        if ( ( 0x7FFFFFE87FFFFFEL & l ) != 0x0L ) {
                            if ( kind > 56 ) {
                                kind = 56;
                            }
                            this.jjCheckNAdd ( 28 );
                            continue;
                        }
                        continue;
                    }
                    case 2: {
                        if ( ( 0x2000000020L & l ) != 0x0L ) {
                            this.jjAddStates ( 29, 30 );
                            continue;
                        }
                        continue;
                    }
                    case 6: {
                        if ( ( 0xFFFFFFFFEFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 7: {
                        if ( this.curChar == '\\' ) {
                            this.jjstateSet[this.jjnewStateCnt++] = 8;
                            continue;
                        }
                        continue;
                    }
                    case 8: {
                        if ( this.curChar == '\\' ) {
                            this.jjCheckNAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 11: {
                        if ( ( 0xFFFFFFFFEFFFFFFFL & l ) != 0x0L ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 12: {
                        if ( this.curChar == '\\' ) {
                            this.jjstateSet[this.jjnewStateCnt++] = 13;
                            continue;
                        }
                        continue;
                    }
                    case 13: {
                        if ( this.curChar == '\\' ) {
                            this.jjCheckNAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 20: {
                        if ( ( 0x2000000020L & l ) != 0x0L ) {
                            this.jjAddStates ( 31, 32 );
                            continue;
                        }
                        continue;
                    }
                    case 24: {
                        if ( ( 0x2000000020L & l ) != 0x0L ) {
                            this.jjAddStates ( 33, 34 );
                            continue;
                        }
                        continue;
                    }
                    case 28: {
                        if ( ( 0x7FFFFFE87FFFFFEL & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAdd ( 28 );
                        continue;
                    }
                    case 29: {
                        if ( ( 0x7FFFFFE87FFFFFEL & l ) == 0x0L ) {
                            continue;
                        }
                        if ( kind > 57 ) {
                            kind = 57;
                        }
                        this.jjCheckNAdd ( 29 );
                        continue;
                    }
                    default: {
                        continue;
                    }
                    }
                } while ( i != startsAt );
            } else {
                final int hiByte = this.curChar >> 8;
                final int i2 = hiByte >> 6;
                final long l2 = 1L << ( hiByte & 0x3F );
                final int i3 = ( this.curChar & '\u00ff' ) >> 6;
                final long l3 = 1L << ( this.curChar & '?' );
                do {
                    switch ( this.jjstateSet[--i] ) {
                    case 0: {
                        if ( !jjCanMove_1 ( hiByte, i2, i3, l2, l3 ) ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAddTwoStates ( 28, 29 );
                        continue;
                    }
                    case 30: {
                        if ( jjCanMove_1 ( hiByte, i2, i3, l2, l3 ) ) {
                            if ( kind > 56 ) {
                                kind = 56;
                            }
                            this.jjCheckNAdd ( 28 );
                        }
                        if ( jjCanMove_1 ( hiByte, i2, i3, l2, l3 ) ) {
                            if ( kind > 57 ) {
                                kind = 57;
                            }
                            this.jjCheckNAdd ( 29 );
                            continue;
                        }
                        continue;
                    }
                    case 6: {
                        if ( jjCanMove_0 ( hiByte, i2, i3, l2, l3 ) ) {
                            this.jjAddStates ( 26, 28 );
                            continue;
                        }
                        continue;
                    }
                    case 11: {
                        if ( jjCanMove_0 ( hiByte, i2, i3, l2, l3 ) ) {
                            this.jjAddStates ( 23, 25 );
                            continue;
                        }
                        continue;
                    }
                    case 28: {
                        if ( !jjCanMove_1 ( hiByte, i2, i3, l2, l3 ) ) {
                            continue;
                        }
                        if ( kind > 56 ) {
                            kind = 56;
                        }
                        this.jjCheckNAdd ( 28 );
                        continue;
                    }
                    case 29: {
                        if ( !jjCanMove_1 ( hiByte, i2, i3, l2, l3 ) ) {
                            continue;
                        }
                        if ( kind > 57 ) {
                            kind = 57;
                        }
                        this.jjCheckNAdd ( 29 );
                        continue;
                    }
                    default: {
                        continue;
                    }
                    }
                } while ( i != startsAt );
            }
            if ( kind != Integer.MAX_VALUE ) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            ++curPos;
            final int n = i = this.jjnewStateCnt;
            final int n2 = 30;
            final int jjnewStateCnt = startsAt;
            this.jjnewStateCnt = jjnewStateCnt;
            if ( n == ( startsAt = n2 - jjnewStateCnt ) ) {
                break;
            }
            try {
                this.curChar = this.input_stream.readChar();
            } catch ( IOException e ) {
                return curPos;
            }
        }
        return curPos;
    }
    private static final boolean jjCanMove_0 ( final int hiByte, final int i1, final int i2, final long l1, final long l2 ) {
        switch ( hiByte ) {
        case 0: {
            return ( ELParserTokenManager.jjbitVec2[i2] & l2 ) != 0x0L;
        }
        default: {
            return ( ELParserTokenManager.jjbitVec0[i1] & l1 ) != 0x0L;
        }
        }
    }
    private static final boolean jjCanMove_1 ( final int hiByte, final int i1, final int i2, final long l1, final long l2 ) {
        switch ( hiByte ) {
        case 0: {
            return ( ELParserTokenManager.jjbitVec4[i2] & l2 ) != 0x0L;
        }
        case 48: {
            return ( ELParserTokenManager.jjbitVec5[i2] & l2 ) != 0x0L;
        }
        case 49: {
            return ( ELParserTokenManager.jjbitVec6[i2] & l2 ) != 0x0L;
        }
        case 51: {
            return ( ELParserTokenManager.jjbitVec7[i2] & l2 ) != 0x0L;
        }
        case 61: {
            return ( ELParserTokenManager.jjbitVec8[i2] & l2 ) != 0x0L;
        }
        default: {
            return ( ELParserTokenManager.jjbitVec3[i1] & l1 ) != 0x0L;
        }
        }
    }
    public ELParserTokenManager ( final SimpleCharStream stream ) {
        this.deque = new ArrayDeque<Integer>();
        this.debugStream = System.out;
        this.jjrounds = new int[30];
        this.jjstateSet = new int[60];
        this.jjimage = new StringBuilder();
        this.image = this.jjimage;
        this.curLexState = 0;
        this.defaultLexState = 0;
        this.input_stream = stream;
    }
    public ELParserTokenManager ( final SimpleCharStream stream, final int lexState ) {
        this ( stream );
        this.SwitchTo ( lexState );
    }
    public void ReInit ( final SimpleCharStream stream ) {
        final boolean b = false;
        this.jjnewStateCnt = ( b ? 1 : 0 );
        this.jjmatchedPos = ( b ? 1 : 0 );
        this.curLexState = this.defaultLexState;
        this.input_stream = stream;
        this.ReInitRounds();
    }
    private void ReInitRounds() {
        this.jjround = -2147483647;
        int i = 30;
        while ( i-- > 0 ) {
            this.jjrounds[i] = Integer.MIN_VALUE;
        }
    }
    public void ReInit ( final SimpleCharStream stream, final int lexState ) {
        this.ReInit ( stream );
        this.SwitchTo ( lexState );
    }
    public void SwitchTo ( final int lexState ) {
        if ( lexState >= 3 || lexState < 0 ) {
            throw new TokenMgrError ( "Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", 2 );
        }
        this.curLexState = lexState;
    }
    protected Token jjFillToken() {
        final String im = ELParserTokenManager.jjstrLiteralImages[this.jjmatchedKind];
        final String curTokenImage = ( im == null ) ? this.input_stream.GetImage() : im;
        final int beginLine = this.input_stream.getBeginLine();
        final int beginColumn = this.input_stream.getBeginColumn();
        final int endLine = this.input_stream.getEndLine();
        final int endColumn = this.input_stream.getEndColumn();
        final Token t = Token.newToken ( this.jjmatchedKind, curTokenImage );
        t.beginLine = beginLine;
        t.endLine = endLine;
        t.beginColumn = beginColumn;
        t.endColumn = endColumn;
        return t;
    }
    public Token getNextToken() {
        int curPos = 0;
        while ( true ) {
            try {
                this.curChar = this.input_stream.BeginToken();
            } catch ( IOException e ) {
                this.jjmatchedKind = 0;
                final Token matchedToken = this.jjFillToken();
                return matchedToken;
            }
            ( this.image = this.jjimage ).setLength ( 0 );
            this.jjimageLen = 0;
            switch ( this.curLexState ) {
            case 0: {
                this.jjmatchedKind = Integer.MAX_VALUE;
                this.jjmatchedPos = 0;
                curPos = this.jjMoveStringLiteralDfa0_0();
                break;
            }
            case 1: {
                try {
                    this.input_stream.backup ( 0 );
                    while ( this.curChar <= ' ' && ( 0x100002600L & 1L << this.curChar ) != 0x0L ) {
                        this.curChar = this.input_stream.BeginToken();
                    }
                } catch ( IOException e2 ) {
                    continue;
                }
                this.jjmatchedKind = Integer.MAX_VALUE;
                this.jjmatchedPos = 0;
                curPos = this.jjMoveStringLiteralDfa0_1();
                if ( this.jjmatchedPos == 0 && this.jjmatchedKind > 61 ) {
                    this.jjmatchedKind = 61;
                    break;
                }
                break;
            }
            case 2: {
                try {
                    this.input_stream.backup ( 0 );
                    while ( this.curChar <= ' ' && ( 0x100002600L & 1L << this.curChar ) != 0x0L ) {
                        this.curChar = this.input_stream.BeginToken();
                    }
                } catch ( IOException e2 ) {
                    continue;
                }
                this.jjmatchedKind = Integer.MAX_VALUE;
                this.jjmatchedPos = 0;
                curPos = this.jjMoveStringLiteralDfa0_2();
                if ( this.jjmatchedPos == 0 && this.jjmatchedKind > 61 ) {
                    this.jjmatchedKind = 61;
                    break;
                }
                break;
            }
            }
            if ( this.jjmatchedKind == Integer.MAX_VALUE ) {
                int error_line = this.input_stream.getEndLine();
                int error_column = this.input_stream.getEndColumn();
                String error_after = null;
                boolean EOFSeen = false;
                try {
                    this.input_stream.readChar();
                    this.input_stream.backup ( 1 );
                } catch ( IOException e3 ) {
                    EOFSeen = true;
                    error_after = ( ( curPos <= 1 ) ? "" : this.input_stream.GetImage() );
                    if ( this.curChar == '\n' || this.curChar == '\r' ) {
                        ++error_line;
                        error_column = 0;
                    } else {
                        ++error_column;
                    }
                }
                if ( !EOFSeen ) {
                    this.input_stream.backup ( 1 );
                    error_after = ( ( curPos <= 1 ) ? "" : this.input_stream.GetImage() );
                }
                throw new TokenMgrError ( EOFSeen, this.curLexState, error_line, error_column, error_after, this.curChar, 0 );
            }
            if ( this.jjmatchedPos + 1 < curPos ) {
                this.input_stream.backup ( curPos - this.jjmatchedPos - 1 );
            }
            if ( ( ELParserTokenManager.jjtoToken[this.jjmatchedKind >> 6] & 1L << ( this.jjmatchedKind & 0x3F ) ) != 0x0L ) {
                final Token matchedToken = this.jjFillToken();
                this.TokenLexicalActions ( matchedToken );
                if ( ELParserTokenManager.jjnewLexState[this.jjmatchedKind] != -1 ) {
                    this.curLexState = ELParserTokenManager.jjnewLexState[this.jjmatchedKind];
                }
                return matchedToken;
            }
            if ( ELParserTokenManager.jjnewLexState[this.jjmatchedKind] == -1 ) {
                continue;
            }
            this.curLexState = ELParserTokenManager.jjnewLexState[this.jjmatchedKind];
        }
    }
    void TokenLexicalActions ( final Token matchedToken ) {
        switch ( this.jjmatchedKind ) {
        case 2: {
            this.image.append ( ELParserTokenManager.jjstrLiteralImages[2] );
            this.lengthOfMatch = ELParserTokenManager.jjstrLiteralImages[2].length();
            this.deque.push ( 0 );
            break;
        }
        case 3: {
            this.image.append ( ELParserTokenManager.jjstrLiteralImages[3] );
            this.lengthOfMatch = ELParserTokenManager.jjstrLiteralImages[3].length();
            this.deque.push ( 0 );
            break;
        }
        case 8: {
            this.image.append ( ELParserTokenManager.jjstrLiteralImages[8] );
            this.lengthOfMatch = ELParserTokenManager.jjstrLiteralImages[8].length();
            this.deque.push ( this.curLexState );
            break;
        }
        case 9: {
            this.image.append ( ELParserTokenManager.jjstrLiteralImages[9] );
            this.lengthOfMatch = ELParserTokenManager.jjstrLiteralImages[9].length();
            this.SwitchTo ( this.deque.pop() );
            break;
        }
        }
    }
    private void jjCheckNAdd ( final int state ) {
        if ( this.jjrounds[state] != this.jjround ) {
            this.jjstateSet[this.jjnewStateCnt++] = state;
            this.jjrounds[state] = this.jjround;
        }
    }
    private void jjAddStates ( int start, final int end ) {
        do {
            this.jjstateSet[this.jjnewStateCnt++] = ELParserTokenManager.jjnextStates[start];
        } while ( start++ != end );
    }
    private void jjCheckNAddTwoStates ( final int state1, final int state2 ) {
        this.jjCheckNAdd ( state1 );
        this.jjCheckNAdd ( state2 );
    }
    private void jjCheckNAddStates ( int start, final int end ) {
        do {
            this.jjCheckNAdd ( ELParserTokenManager.jjnextStates[start] );
        } while ( start++ != end );
    }
    static {
        jjbitVec0 = new long[] { -2L, -1L, -1L, -1L };
        jjbitVec2 = new long[] { 0L, 0L, -1L, -1L };
        jjbitVec3 = new long[] { 2301339413881290750L, -16384L, 4294967295L, 432345564227567616L };
        jjbitVec4 = new long[] { 0L, 0L, 0L, -36028797027352577L };
        jjbitVec5 = new long[] { 0L, -1L, -1L, -1L };
        jjbitVec6 = new long[] { -1L, -1L, 65535L, 0L };
        jjbitVec7 = new long[] { -1L, -1L, 0L, 0L };
        jjbitVec8 = new long[] { 70368744177663L, 0L, 0L, 0L };
        jjnextStates = new int[] { 0, 1, 3, 4, 2, 0, 1, 4, 2, 0, 1, 4, 5, 2, 0, 1, 2, 6, 16, 17, 18, 23, 24, 11, 12, 14, 6, 7, 9, 3, 4, 21, 22, 25, 26 };
        jjstrLiteralImages = new String[] { "", null, "${", "#{", null, null, null, null, "{", "}", null, null, null, null, "true", "false", "null", ".", "(", ")", "[", "]", ":", ";", ",", ">", "gt", "<", "lt", ">=", "ge", "<=", "le", "==", "eq", "!=", "ne", "!", "not", "&&", "and", "||", "or", "empty", "instanceof", "*", "+", "-", "?", "/", "div", "%", "mod", "+=", "=", "->", null, null, null, null, null, null };
        lexStateNames = new String[] { "DEFAULT", "IN_EXPRESSION", "IN_SET_OR_MAP" };
        jjnewLexState = new int[] { -1, -1, 1, 1, -1, -1, -1, -1, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
        jjtoToken = new long[] { 2594073385365401359L };
        jjtoSkip = new long[] { 240L };
    }
}
