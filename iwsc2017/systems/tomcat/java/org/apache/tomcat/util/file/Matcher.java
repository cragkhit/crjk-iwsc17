package org.apache.tomcat.util.file;
import java.util.Set;
public final class Matcher {
    public static boolean matchName ( Set<String> patternSet, String fileName ) {
        char[] fileNameArray = fileName.toCharArray();
        for ( String pattern : patternSet ) {
            if ( match ( pattern, fileNameArray, true ) ) {
                return true;
            }
        }
        return false;
    }
    public static boolean match ( String pattern, String str,
                                  boolean caseSensitive ) {
        return match ( pattern, str.toCharArray(), caseSensitive );
    }
    private static boolean match ( String pattern, char[] strArr,
                                   boolean caseSensitive ) {
        char[] patArr = pattern.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;
        boolean containsStar = false;
        for ( int i = 0; i < patArr.length; i++ ) {
            if ( patArr[i] == '*' ) {
                containsStar = true;
                break;
            }
        }
        if ( !containsStar ) {
            if ( patIdxEnd != strIdxEnd ) {
                return false;
            }
            for ( int i = 0; i <= patIdxEnd; i++ ) {
                ch = patArr[i];
                if ( ch != '?' ) {
                    if ( different ( caseSensitive, ch, strArr[i] ) ) {
                        return false;
                    }
                }
            }
            return true;
        }
        if ( patIdxEnd == 0 ) {
            return true;
        }
        while ( true ) {
            ch = patArr[patIdxStart];
            if ( ch == '*' || strIdxStart > strIdxEnd ) {
                break;
            }
            if ( ch != '?' ) {
                if ( different ( caseSensitive, ch, strArr[strIdxStart] ) ) {
                    return false;
                }
            }
            patIdxStart++;
            strIdxStart++;
        }
        if ( strIdxStart > strIdxEnd ) {
            return allStars ( patArr, patIdxStart, patIdxEnd );
        }
        while ( true ) {
            ch = patArr[patIdxEnd];
            if ( ch == '*' || strIdxStart > strIdxEnd ) {
                break;
            }
            if ( ch != '?' ) {
                if ( different ( caseSensitive, ch, strArr[strIdxEnd] ) ) {
                    return false;
                }
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if ( strIdxStart > strIdxEnd ) {
            return allStars ( patArr, patIdxStart, patIdxEnd );
        }
        while ( patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd ) {
            int patIdxTmp = -1;
            for ( int i = patIdxStart + 1; i <= patIdxEnd; i++ ) {
                if ( patArr[i] == '*' ) {
                    patIdxTmp = i;
                    break;
                }
            }
            if ( patIdxTmp == patIdxStart + 1 ) {
                patIdxStart++;
                continue;
            }
            int patLength = ( patIdxTmp - patIdxStart - 1 );
            int strLength = ( strIdxEnd - strIdxStart + 1 );
            int foundIdx = -1;
            strLoop:
            for ( int i = 0; i <= strLength - patLength; i++ ) {
                for ( int j = 0; j < patLength; j++ ) {
                    ch = patArr[patIdxStart + j + 1];
                    if ( ch != '?' ) {
                        if ( different ( caseSensitive, ch,
                                         strArr[strIdxStart + i + j] ) ) {
                            continue strLoop;
                        }
                    }
                }
                foundIdx = strIdxStart + i;
                break;
            }
            if ( foundIdx == -1 ) {
                return false;
            }
            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }
        return allStars ( patArr, patIdxStart, patIdxEnd );
    }
    private static boolean allStars ( char[] chars, int start, int end ) {
        for ( int i = start; i <= end; ++i ) {
            if ( chars[i] != '*' ) {
                return false;
            }
        }
        return true;
    }
    private static boolean different (
        boolean caseSensitive, char ch, char other ) {
        return caseSensitive
               ? ch != other
               : Character.toUpperCase ( ch ) != Character.toUpperCase ( other );
    }
}
