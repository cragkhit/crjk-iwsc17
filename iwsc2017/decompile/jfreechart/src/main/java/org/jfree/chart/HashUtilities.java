package org.jfree.chart;
import org.jfree.util.StrokeList;
import org.jfree.util.PaintList;
import org.jfree.util.BooleanList;
import java.awt.Stroke;
import java.awt.GradientPaint;
import java.awt.Paint;
public class HashUtilities {
    public static int hashCodeForPaint ( final Paint p ) {
        if ( p == null ) {
            return 0;
        }
        int result;
        if ( p instanceof GradientPaint ) {
            final GradientPaint gp = ( GradientPaint ) p;
            result = 193;
            result = 37 * result + gp.getColor1().hashCode();
            result = 37 * result + gp.getPoint1().hashCode();
            result = 37 * result + gp.getColor2().hashCode();
            result = 37 * result + gp.getPoint2().hashCode();
        } else {
            result = p.hashCode();
        }
        return result;
    }
    public static int hashCodeForDoubleArray ( final double[] a ) {
        if ( a == null ) {
            return 0;
        }
        int result = 193;
        for ( int i = 0; i < a.length; ++i ) {
            final long temp = Double.doubleToLongBits ( a[i] );
            result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        }
        return result;
    }
    public static int hashCode ( final int pre, final boolean b ) {
        return 37 * pre + ( b ? 0 : 1 );
    }
    public static int hashCode ( final int pre, final int i ) {
        return 37 * pre + i;
    }
    public static int hashCode ( final int pre, final double d ) {
        final long l = Double.doubleToLongBits ( d );
        return 37 * pre + ( int ) ( l ^ l >>> 32 );
    }
    public static int hashCode ( final int pre, final Paint p ) {
        return 37 * pre + hashCodeForPaint ( p );
    }
    public static int hashCode ( final int pre, final Stroke s ) {
        final int h = ( s != null ) ? s.hashCode() : 0;
        return 37 * pre + h;
    }
    public static int hashCode ( final int pre, final String s ) {
        final int h = ( s != null ) ? s.hashCode() : 0;
        return 37 * pre + h;
    }
    public static int hashCode ( final int pre, final Comparable c ) {
        final int h = ( c != null ) ? c.hashCode() : 0;
        return 37 * pre + h;
    }
    public static int hashCode ( final int pre, final Object obj ) {
        final int h = ( obj != null ) ? obj.hashCode() : 0;
        return 37 * pre + h;
    }
    public static int hashCode ( final int pre, final BooleanList list ) {
        if ( list == null ) {
            return pre;
        }
        int result = 127;
        final int size = list.size();
        result = hashCode ( result, size );
        if ( size > 0 ) {
            result = hashCode ( result, list.getBoolean ( 0 ) );
            if ( size > 1 ) {
                result = hashCode ( result, list.getBoolean ( size - 1 ) );
                if ( size > 2 ) {
                    result = hashCode ( result, list.getBoolean ( size / 2 ) );
                }
            }
        }
        return 37 * pre + result;
    }
    public static int hashCode ( final int pre, final PaintList list ) {
        if ( list == null ) {
            return pre;
        }
        int result = 127;
        final int size = list.size();
        result = hashCode ( result, size );
        if ( size > 0 ) {
            result = hashCode ( result, list.getPaint ( 0 ) );
            if ( size > 1 ) {
                result = hashCode ( result, list.getPaint ( size - 1 ) );
                if ( size > 2 ) {
                    result = hashCode ( result, list.getPaint ( size / 2 ) );
                }
            }
        }
        return 37 * pre + result;
    }
    public static int hashCode ( final int pre, final StrokeList list ) {
        if ( list == null ) {
            return pre;
        }
        int result = 127;
        final int size = list.size();
        result = hashCode ( result, size );
        if ( size > 0 ) {
            result = hashCode ( result, list.getStroke ( 0 ) );
            if ( size > 1 ) {
                result = hashCode ( result, list.getStroke ( size - 1 ) );
                if ( size > 2 ) {
                    result = hashCode ( result, list.getStroke ( size / 2 ) );
                }
            }
        }
        return 37 * pre + result;
    }
}
