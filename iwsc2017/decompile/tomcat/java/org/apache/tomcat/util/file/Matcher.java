package org.apache.tomcat.util.file;
import java.util.Iterator;
import java.util.Set;
public final class Matcher {
    public static boolean matchName ( final Set<String> patternSet, final String fileName ) {
        final char[] fileNameArray = fileName.toCharArray();
        for ( final String pattern : patternSet ) {
            if ( match ( pattern, fileNameArray, true ) ) {
                return true;
            }
        }
        return false;
    }
    public static boolean match ( final String pattern, final String str, final boolean caseSensitive ) {
        return match ( pattern, str.toCharArray(), caseSensitive );
    }
    private static boolean match ( final String pattern, final char[] strArr, final boolean caseSensitive ) {
        final char[] patArr = pattern.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        boolean containsStar = false;
        for ( int i = 0; i < patArr.length; ++i ) {
            if ( patArr[i] == '*' ) {
                containsStar = true;
                break;
            }
        }
        if ( !containsStar ) {
            if ( patIdxEnd != strIdxEnd ) {
                return false;
            }
            for ( int i = 0; i <= patIdxEnd; ++i ) {
                final char ch = patArr[i];
                if ( ch != '?' && different ( caseSensitive, ch, strArr[i] ) ) {
                    return false;
                }
            }
            return true;
        } else {
            if ( patIdxEnd == 0 ) {
                return true;
            }
            while ( true ) {
                char ch = patArr[patIdxStart];
                if ( ch != '*' && strIdxStart <= strIdxEnd ) {
                    if ( ch != '?' && different ( caseSensitive, ch, strArr[strIdxStart] ) ) {
                        return false;
                    }
                    ++patIdxStart;
                    ++strIdxStart;
                } else {
                    if ( strIdxStart > strIdxEnd ) {
                        return allStars ( patArr, patIdxStart, patIdxEnd );
                    }
                    while ( true ) {
                        ch = patArr[patIdxEnd];
                        if ( ch != '*' && strIdxStart <= strIdxEnd ) {
                            if ( ch != '?' && different ( caseSensitive, ch, strArr[strIdxEnd] ) ) {
                                return false;
                            }
                            --patIdxEnd;
                            --strIdxEnd;
                        } else {
                            if ( strIdxStart > strIdxEnd ) {
                                return allStars ( patArr, patIdxStart, patIdxEnd );
                            }
                            while ( patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd ) {
                                int patIdxTmp = -1;
                                for ( int j = patIdxStart + 1; j <= patIdxEnd; ++j ) {
                                    if ( patArr[j] == '*' ) {
                                        patIdxTmp = j;
                                        break;
                                    }
                                }
                                if ( patIdxTmp == patIdxStart + 1 ) {
                                    ++patIdxStart;
                                } else {
                                    final int patLength = patIdxTmp - patIdxStart - 1;
                                    final int strLength = strIdxEnd - strIdxStart + 1;
                                    int foundIdx = -1;
                                    int k = 0;
                                    Label_0355:
                                    while ( k <= strLength - patLength ) {
                                        for ( int l = 0; l < patLength; ++l ) {
                                            ch = patArr[patIdxStart + l + 1];
                                            if ( ch != '?' && different ( caseSensitive, ch, strArr[strIdxStart + k + l] ) ) {
                                                ++k;
                                                continue Label_0355;
                                            }
                                        }
                                        foundIdx = strIdxStart + k;
                                        break;
                                    }
                                    if ( foundIdx == -1 ) {
                                        return false;
                                    }
                                    patIdxStart = patIdxTmp;
                                    strIdxStart = foundIdx + patLength;
                                }
                            }
                            return allStars ( patArr, patIdxStart, patIdxEnd );
                        }
                    }
                }
            }
        }
    }
    private static boolean allStars ( final char[] chars, final int start, final int end ) {
        for ( int i = start; i <= end; ++i ) {
            if ( chars[i] != '*' ) {
                return false;
            }
        }
        return true;
    }
    private static boolean different ( final boolean caseSensitive, final char ch, final char other ) {
        return caseSensitive ? ( ch != other ) : ( Character.toUpperCase ( ch ) != Character.toUpperCase ( other ) );
    }
}
