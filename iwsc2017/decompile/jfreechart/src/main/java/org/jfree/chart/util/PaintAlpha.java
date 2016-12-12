package org.jfree.chart.util;
import java.util.Hashtable;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.awt.TexturePaint;
import java.awt.RadialGradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.GradientPaint;
import java.awt.Color;
import java.awt.Paint;
public class PaintAlpha {
    private static final double FACTOR = 0.7;
    private static boolean legacyAlpha;
    public static boolean setLegacyAlpha ( final boolean legacyAlpha ) {
        final boolean old = PaintAlpha.legacyAlpha;
        PaintAlpha.legacyAlpha = legacyAlpha;
        return old;
    }
    public static Paint darker ( final Paint paint ) {
        if ( paint instanceof Color ) {
            return darker ( ( Color ) paint );
        }
        if ( PaintAlpha.legacyAlpha ) {
            return paint;
        }
        if ( paint instanceof GradientPaint ) {
            return darker ( ( GradientPaint ) paint );
        }
        if ( paint instanceof LinearGradientPaint ) {
            return darkerLinearGradientPaint ( ( LinearGradientPaint ) paint );
        }
        if ( paint instanceof RadialGradientPaint ) {
            return darkerRadialGradientPaint ( ( RadialGradientPaint ) paint );
        }
        if ( paint instanceof TexturePaint ) {
            try {
                return darkerTexturePaint ( ( TexturePaint ) paint );
            } catch ( Exception e ) {
                return paint;
            }
        }
        return paint;
    }
    private static Color darker ( final Color paint ) {
        return new Color ( ( int ) ( paint.getRed() * 0.7 ), ( int ) ( paint.getGreen() * 0.7 ), ( int ) ( paint.getBlue() * 0.7 ), paint.getAlpha() );
    }
    private static GradientPaint darker ( final GradientPaint paint ) {
        return new GradientPaint ( paint.getPoint1(), darker ( paint.getColor1() ), paint.getPoint2(), darker ( paint.getColor2() ), paint.isCyclic() );
    }
    private static Paint darkerLinearGradientPaint ( final LinearGradientPaint paint ) {
        final Color[] paintColors = paint.getColors();
        for ( int i = 0; i < paintColors.length; ++i ) {
            paintColors[i] = darker ( paintColors[i] );
        }
        return new LinearGradientPaint ( paint.getStartPoint(), paint.getEndPoint(), paint.getFractions(), paintColors, paint.getCycleMethod(), paint.getColorSpace(), paint.getTransform() );
    }
    private static Paint darkerRadialGradientPaint ( final RadialGradientPaint paint ) {
        final Color[] paintColors = paint.getColors();
        for ( int i = 0; i < paintColors.length; ++i ) {
            paintColors[i] = darker ( paintColors[i] );
        }
        return new RadialGradientPaint ( paint.getCenterPoint(), paint.getRadius(), paint.getFocusPoint(), paint.getFractions(), paintColors, paint.getCycleMethod(), paint.getColorSpace(), paint.getTransform() );
    }
    private static TexturePaint darkerTexturePaint ( final TexturePaint paint ) {
        if ( !paint.getImage().getColorModel().isAlphaPremultiplied() ) {}
        final BufferedImage img = cloneImage ( paint.getImage() );
        final WritableRaster ras = img.copyData ( null );
        final int miX = ras.getMinX();
        final int miY = ras.getMinY();
        final int maY = ras.getMinY() + ras.getHeight();
        final int wid = ras.getWidth();
        int[] pix = new int[wid * img.getSampleModel().getNumBands()];
        if ( img.getColorModel() instanceof IndexColorModel ) {
            int[] nco = new int[4];
            for ( int y = miY; y < maY; ++y ) {
                pix = ras.getPixels ( miX, y, wid, 1, pix );
                for ( int p = 0; p < pix.length; ++p ) {
                    final int[] components;
                    nco = ( components = img.getColorModel().getComponents ( pix[p], nco, 0 ) );
                    final int n = 0;
                    components[n] *= ( int ) 0.7;
                    final int[] array = nco;
                    final int n2 = 1;
                    array[n2] *= ( int ) 0.7;
                    final int[] array2 = nco;
                    final int n3 = 2;
                    array2[n3] *= ( int ) 0.7;
                    pix[p] = img.getColorModel().getDataElement ( nco, 0 );
                }
                ras.setPixels ( miX, y, wid, 1, pix );
            }
            img.setData ( ras );
            return new TexturePaint ( img, paint.getAnchorRect() );
        }
        if ( img.getSampleModel().getNumBands() == 4 ) {
            for ( int y2 = miY; y2 < maY; ++y2 ) {
                pix = ras.getPixels ( miX, y2, wid, 1, pix );
                for ( int p2 = 0; p2 < pix.length; pix[p2] = ( int ) ( pix[p2++] * 0.7 ), pix[p2] = ( int ) ( pix[p2++] * 0.7 ), pix[p2] = ( int ) ( pix[p2++] * 0.7 ), ++p2 ) {}
                ras.setPixels ( miX, y2, wid, 1, pix );
            }
            img.setData ( ras );
            return new TexturePaint ( img, paint.getAnchorRect() );
        }
        for ( int y2 = miY; y2 < maY; ++y2 ) {
            pix = ras.getPixels ( miX, y2, wid, 1, pix );
            for ( int p2 = 0; p2 < pix.length; ++p2 ) {
                pix[p2] *= ( int ) 0.7;
            }
            ras.setPixels ( miX, y2, wid, 1, pix );
        }
        img.setData ( ras );
        return new TexturePaint ( img, paint.getAnchorRect() );
    }
    public static BufferedImage cloneImage ( final BufferedImage image ) {
        final WritableRaster rin = image.getRaster();
        final WritableRaster ras = rin.createCompatibleWritableRaster();
        ras.setRect ( rin );
        Hashtable props = null;
        final String[] propNames = image.getPropertyNames();
        if ( propNames != null ) {
            props = new Hashtable();
            for ( int i = 0; i < propNames.length; ++i ) {
                props.put ( propNames[i], image.getProperty ( propNames[i] ) );
            }
        }
        return new BufferedImage ( image.getColorModel(), ras, image.isAlphaPremultiplied(), props );
    }
    static {
        PaintAlpha.legacyAlpha = false;
    }
}
