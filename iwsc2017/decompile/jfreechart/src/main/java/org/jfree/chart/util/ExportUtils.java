package org.jfree.chart.util;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.io.File;
import org.jfree.ui.Drawable;
public class ExportUtils {
    public static boolean isJFreeSVGAvailable() {
        Class<?> svgClass = null;
        try {
            svgClass = Class.forName ( "org.jfree.graphics2d.svg.SVGGraphics2D" );
        } catch ( ClassNotFoundException ex ) {}
        return svgClass != null;
    }
    public static boolean isOrsonPDFAvailable() {
        Class<?> pdfDocumentClass = null;
        try {
            pdfDocumentClass = Class.forName ( "com.orsonpdf.PDFDocument" );
        } catch ( ClassNotFoundException ex ) {}
        return pdfDocumentClass != null;
    }
    public static void writeAsSVG ( final Drawable drawable, final int w, final int h, final File file ) {
        if ( !isJFreeSVGAvailable() ) {
            throw new IllegalStateException ( "JFreeSVG is not present on the classpath." );
        }
        ParamChecks.nullNotPermitted ( drawable, "drawable" );
        ParamChecks.nullNotPermitted ( file, "file" );
        try {
            final Class<?> svg2Class = Class.forName ( "org.jfree.graphics2d.svg.SVGGraphics2D" );
            final Constructor<?> c1 = svg2Class.getConstructor ( Integer.TYPE, Integer.TYPE );
            final Graphics2D svg2 = ( Graphics2D ) c1.newInstance ( w, h );
            final Rectangle2D drawArea = new Rectangle2D.Double ( 0.0, 0.0, w, h );
            drawable.draw ( svg2, drawArea );
            final Class<?> svgUtilsClass = Class.forName ( "org.jfree.graphics2d.svg.SVGUtils" );
            final Method m1 = svg2Class.getMethod ( "getSVGElement", ( Class<?>[] ) null );
            final String element = ( String ) m1.invoke ( svg2, ( Object[] ) null );
            final Method m2 = svgUtilsClass.getMethod ( "writeToSVG", File.class, String.class );
            m2.invoke ( svgUtilsClass, file, element );
        } catch ( ClassNotFoundException ex ) {
            throw new RuntimeException ( ex );
        } catch ( InstantiationException ex2 ) {
            throw new RuntimeException ( ex2 );
        } catch ( IllegalAccessException ex3 ) {
            throw new RuntimeException ( ex3 );
        } catch ( NoSuchMethodException ex4 ) {
            throw new RuntimeException ( ex4 );
        } catch ( SecurityException ex5 ) {
            throw new RuntimeException ( ex5 );
        } catch ( IllegalArgumentException ex6 ) {
            throw new RuntimeException ( ex6 );
        } catch ( InvocationTargetException ex7 ) {
            throw new RuntimeException ( ex7 );
        }
    }
    public static final void writeAsPDF ( final Drawable drawable, final int w, final int h, final File file ) {
        if ( !isOrsonPDFAvailable() ) {
            throw new IllegalStateException ( "OrsonPDF is not present on the classpath." );
        }
        ParamChecks.nullNotPermitted ( drawable, "drawable" );
        ParamChecks.nullNotPermitted ( file, "file" );
        try {
            final Class<?> pdfDocClass = Class.forName ( "com.orsonpdf.PDFDocument" );
            final Object pdfDoc = pdfDocClass.newInstance();
            final Method m = pdfDocClass.getMethod ( "createPage", Rectangle2D.class );
            final Rectangle2D rect = new Rectangle ( w, h );
            final Object page = m.invoke ( pdfDoc, rect );
            final Method m2 = page.getClass().getMethod ( "getGraphics2D", ( Class<?>[] ) new Class[0] );
            final Graphics2D g2 = ( Graphics2D ) m2.invoke ( page, new Object[0] );
            final Rectangle2D drawArea = new Rectangle2D.Double ( 0.0, 0.0, w, h );
            drawable.draw ( g2, drawArea );
            final Method m3 = pdfDocClass.getMethod ( "writeToFile", File.class );
            m3.invoke ( pdfDoc, file );
        } catch ( ClassNotFoundException ex ) {
            throw new RuntimeException ( ex );
        } catch ( InstantiationException ex2 ) {
            throw new RuntimeException ( ex2 );
        } catch ( IllegalAccessException ex3 ) {
            throw new RuntimeException ( ex3 );
        } catch ( NoSuchMethodException ex4 ) {
            throw new RuntimeException ( ex4 );
        } catch ( SecurityException ex5 ) {
            throw new RuntimeException ( ex5 );
        } catch ( IllegalArgumentException ex6 ) {
            throw new RuntimeException ( ex6 );
        } catch ( InvocationTargetException ex7 ) {
            throw new RuntimeException ( ex7 );
        }
    }
    public static void writeAsPNG ( final Drawable drawable, final int w, final int h, final File file ) throws FileNotFoundException, IOException {
        final BufferedImage image = new BufferedImage ( w, h, 2 );
        final Graphics2D g2 = image.createGraphics();
        drawable.draw ( g2, ( Rectangle2D ) new Rectangle ( w, h ) );
        final OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) );
        try {
            ImageIO.write ( image, "png", out );
        } finally {
            out.close();
        }
    }
    public static void writeAsJPEG ( final Drawable drawable, final int w, final int h, final File file ) throws FileNotFoundException, IOException {
        final BufferedImage image = new BufferedImage ( w, h, 1 );
        final Graphics2D g2 = image.createGraphics();
        drawable.draw ( g2, ( Rectangle2D ) new Rectangle ( w, h ) );
        final OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) );
        try {
            ImageIO.write ( image, "jpg", out );
        } finally {
            out.close();
        }
    }
}
