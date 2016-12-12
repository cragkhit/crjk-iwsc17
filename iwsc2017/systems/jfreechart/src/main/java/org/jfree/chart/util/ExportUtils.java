package org.jfree.chart.util;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;
import org.jfree.ui.Drawable;
public class ExportUtils {
    public static boolean isJFreeSVGAvailable() {
        Class<?> svgClass = null;
        try {
            svgClass = Class.forName ( "org.jfree.graphics2d.svg.SVGGraphics2D" );
        } catch ( ClassNotFoundException e ) {
        }
        return ( svgClass != null );
    }
    public static boolean isOrsonPDFAvailable() {
        Class<?> pdfDocumentClass = null;
        try {
            pdfDocumentClass = Class.forName ( "com.orsonpdf.PDFDocument" );
        } catch ( ClassNotFoundException e ) {
        }
        return ( pdfDocumentClass != null );
    }
    public static void writeAsSVG ( Drawable drawable, int w, int h,
                                    File file ) {
        if ( !ExportUtils.isJFreeSVGAvailable() ) {
            throw new IllegalStateException (
                "JFreeSVG is not present on the classpath." );
        }
        ParamChecks.nullNotPermitted ( drawable, "drawable" );
        ParamChecks.nullNotPermitted ( file, "file" );
        try {
            Class<?> svg2Class = Class.forName (
                                     "org.jfree.graphics2d.svg.SVGGraphics2D" );
            Constructor<?> c1 = svg2Class.getConstructor ( int.class, int.class );
            Graphics2D svg2 = ( Graphics2D ) c1.newInstance ( w, h );
            Rectangle2D drawArea = new Rectangle2D.Double ( 0, 0, w, h );
            drawable.draw ( svg2, drawArea );
            Class<?> svgUtilsClass = Class.forName (
                                         "org.jfree.graphics2d.svg.SVGUtils" );
            Method m1 = svg2Class.getMethod ( "getSVGElement", ( Class[] ) null );
            String element = ( String ) m1.invoke ( svg2, ( Object[] ) null );
            Method m2 = svgUtilsClass.getMethod ( "writeToSVG", File.class,
                                                  String.class );
            m2.invoke ( svgUtilsClass, file, element );
        } catch ( ClassNotFoundException ex ) {
            throw new RuntimeException ( ex );
        } catch ( InstantiationException ex ) {
            throw new RuntimeException ( ex );
        } catch ( IllegalAccessException ex ) {
            throw new RuntimeException ( ex );
        } catch ( NoSuchMethodException ex ) {
            throw new RuntimeException ( ex );
        } catch ( SecurityException ex ) {
            throw new RuntimeException ( ex );
        } catch ( IllegalArgumentException ex ) {
            throw new RuntimeException ( ex );
        } catch ( InvocationTargetException ex ) {
            throw new RuntimeException ( ex );
        }
    }
    public static final void writeAsPDF ( Drawable drawable,
                                          int w, int h, File file ) {
        if ( !ExportUtils.isOrsonPDFAvailable() ) {
            throw new IllegalStateException (
                "OrsonPDF is not present on the classpath." );
        }
        ParamChecks.nullNotPermitted ( drawable, "drawable" );
        ParamChecks.nullNotPermitted ( file, "file" );
        try {
            Class<?> pdfDocClass = Class.forName ( "com.orsonpdf.PDFDocument" );
            Object pdfDoc = pdfDocClass.newInstance();
            Method m = pdfDocClass.getMethod ( "createPage", Rectangle2D.class );
            Rectangle2D rect = new Rectangle ( w, h );
            Object page = m.invoke ( pdfDoc, rect );
            Method m2 = page.getClass().getMethod ( "getGraphics2D" );
            Graphics2D g2 = ( Graphics2D ) m2.invoke ( page );
            Rectangle2D drawArea = new Rectangle2D.Double ( 0, 0, w, h );
            drawable.draw ( g2, drawArea );
            Method m3 = pdfDocClass.getMethod ( "writeToFile", File.class );
            m3.invoke ( pdfDoc, file );
        } catch ( ClassNotFoundException ex ) {
            throw new RuntimeException ( ex );
        } catch ( InstantiationException ex ) {
            throw new RuntimeException ( ex );
        } catch ( IllegalAccessException ex ) {
            throw new RuntimeException ( ex );
        } catch ( NoSuchMethodException ex ) {
            throw new RuntimeException ( ex );
        } catch ( SecurityException ex ) {
            throw new RuntimeException ( ex );
        } catch ( IllegalArgumentException ex ) {
            throw new RuntimeException ( ex );
        } catch ( InvocationTargetException ex ) {
            throw new RuntimeException ( ex );
        }
    }
    public static void writeAsPNG ( Drawable drawable, int w, int h,
                                    File file ) throws FileNotFoundException, IOException {
        BufferedImage image = new BufferedImage ( w, h,
                BufferedImage.TYPE_INT_ARGB );
        Graphics2D g2 = image.createGraphics();
        drawable.draw ( g2, new Rectangle ( w, h ) );
        OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) );
        try {
            ImageIO.write ( image, "png", out );
        } finally {
            out.close();
        }
    }
    public static void writeAsJPEG ( Drawable drawable, int w, int h,
                                     File file ) throws FileNotFoundException, IOException {
        BufferedImage image = new BufferedImage ( w, h,
                BufferedImage.TYPE_INT_RGB );
        Graphics2D g2 = image.createGraphics();
        drawable.draw ( g2, new Rectangle ( w, h ) );
        OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) );
        try {
            ImageIO.write ( image, "jpg", out );
        } finally {
            out.close();
        }
    }
}
