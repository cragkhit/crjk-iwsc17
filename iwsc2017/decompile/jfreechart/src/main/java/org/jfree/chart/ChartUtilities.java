package org.jfree.chart;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.OverLIBToolTipTagFragmentGenerator;
import java.io.PrintWriter;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.util.ParamChecks;
import java.io.IOException;
import java.io.OutputStream;
public abstract class ChartUtilities {
    public static void applyCurrentTheme ( final JFreeChart chart ) {
        ChartFactory.getChartTheme().apply ( chart );
    }
    public static void writeChartAsPNG ( final OutputStream out, final JFreeChart chart, final int width, final int height ) throws IOException {
        writeChartAsPNG ( out, chart, width, height, null );
    }
    public static void writeChartAsPNG ( final OutputStream out, final JFreeChart chart, final int width, final int height, final boolean encodeAlpha, final int compression ) throws IOException {
        writeChartAsPNG ( out, chart, width, height, null, encodeAlpha, compression );
    }
    public static void writeChartAsPNG ( final OutputStream out, final JFreeChart chart, final int width, final int height, final ChartRenderingInfo info ) throws IOException {
        ParamChecks.nullNotPermitted ( chart, "chart" );
        final BufferedImage bufferedImage = chart.createBufferedImage ( width, height, info );
        EncoderUtil.writeBufferedImage ( bufferedImage, "png", out );
    }
    public static void writeChartAsPNG ( final OutputStream out, final JFreeChart chart, final int width, final int height, final ChartRenderingInfo info, final boolean encodeAlpha, final int compression ) throws IOException {
        ParamChecks.nullNotPermitted ( out, "out" );
        ParamChecks.nullNotPermitted ( chart, "chart" );
        final BufferedImage chartImage = chart.createBufferedImage ( width, height, 2, info );
        writeBufferedImageAsPNG ( out, chartImage, encodeAlpha, compression );
    }
    public static void writeScaledChartAsPNG ( final OutputStream out, final JFreeChart chart, final int width, final int height, final int widthScaleFactor, final int heightScaleFactor ) throws IOException {
        ParamChecks.nullNotPermitted ( out, "out" );
        ParamChecks.nullNotPermitted ( chart, "chart" );
        final double desiredWidth = width * widthScaleFactor;
        final double desiredHeight = height * heightScaleFactor;
        final double defaultWidth = width;
        final double defaultHeight = height;
        boolean scale = false;
        if ( widthScaleFactor != 1 || heightScaleFactor != 1 ) {
            scale = true;
        }
        final double scaleX = desiredWidth / defaultWidth;
        final double scaleY = desiredHeight / defaultHeight;
        final BufferedImage image = new BufferedImage ( ( int ) desiredWidth, ( int ) desiredHeight, 2 );
        final Graphics2D g2 = image.createGraphics();
        if ( scale ) {
            final AffineTransform saved = g2.getTransform();
            g2.transform ( AffineTransform.getScaleInstance ( scaleX, scaleY ) );
            chart.draw ( g2, new Rectangle2D.Double ( 0.0, 0.0, defaultWidth, defaultHeight ), null, null );
            g2.setTransform ( saved );
            g2.dispose();
        } else {
            chart.draw ( g2, new Rectangle2D.Double ( 0.0, 0.0, defaultWidth, defaultHeight ), null, null );
        }
        out.write ( encodeAsPNG ( image ) );
    }
    public static void saveChartAsPNG ( final File file, final JFreeChart chart, final int width, final int height ) throws IOException {
        saveChartAsPNG ( file, chart, width, height, null );
    }
    public static void saveChartAsPNG ( final File file, final JFreeChart chart, final int width, final int height, final ChartRenderingInfo info ) throws IOException {
        ParamChecks.nullNotPermitted ( file, "file" );
        final OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) );
        try {
            writeChartAsPNG ( out, chart, width, height, info );
        } finally {
            out.close();
        }
    }
    public static void saveChartAsPNG ( final File file, final JFreeChart chart, final int width, final int height, final ChartRenderingInfo info, final boolean encodeAlpha, final int compression ) throws IOException {
        ParamChecks.nullNotPermitted ( file, "file" );
        ParamChecks.nullNotPermitted ( chart, "chart" );
        final OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) );
        try {
            writeChartAsPNG ( out, chart, width, height, info, encodeAlpha, compression );
        } finally {
            out.close();
        }
    }
    public static void writeChartAsJPEG ( final OutputStream out, final JFreeChart chart, final int width, final int height ) throws IOException {
        writeChartAsJPEG ( out, chart, width, height, null );
    }
    public static void writeChartAsJPEG ( final OutputStream out, final float quality, final JFreeChart chart, final int width, final int height ) throws IOException {
        writeChartAsJPEG ( out, quality, chart, width, height, null );
    }
    public static void writeChartAsJPEG ( final OutputStream out, final JFreeChart chart, final int width, final int height, final ChartRenderingInfo info ) throws IOException {
        ParamChecks.nullNotPermitted ( out, "out" );
        ParamChecks.nullNotPermitted ( chart, "chart" );
        final BufferedImage image = chart.createBufferedImage ( width, height, 1, info );
        EncoderUtil.writeBufferedImage ( image, "jpeg", out );
    }
    public static void writeChartAsJPEG ( final OutputStream out, final float quality, final JFreeChart chart, final int width, final int height, final ChartRenderingInfo info ) throws IOException {
        ParamChecks.nullNotPermitted ( out, "out" );
        ParamChecks.nullNotPermitted ( chart, "chart" );
        final BufferedImage image = chart.createBufferedImage ( width, height, 1, info );
        EncoderUtil.writeBufferedImage ( image, "jpeg", out, quality );
    }
    public static void saveChartAsJPEG ( final File file, final JFreeChart chart, final int width, final int height ) throws IOException {
        saveChartAsJPEG ( file, chart, width, height, null );
    }
    public static void saveChartAsJPEG ( final File file, final float quality, final JFreeChart chart, final int width, final int height ) throws IOException {
        saveChartAsJPEG ( file, quality, chart, width, height, null );
    }
    public static void saveChartAsJPEG ( final File file, final JFreeChart chart, final int width, final int height, final ChartRenderingInfo info ) throws IOException {
        ParamChecks.nullNotPermitted ( file, "file" );
        ParamChecks.nullNotPermitted ( chart, "chart" );
        final OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) );
        try {
            writeChartAsJPEG ( out, chart, width, height, info );
        } finally {
            out.close();
        }
    }
    public static void saveChartAsJPEG ( final File file, final float quality, final JFreeChart chart, final int width, final int height, final ChartRenderingInfo info ) throws IOException {
        ParamChecks.nullNotPermitted ( file, "file" );
        ParamChecks.nullNotPermitted ( chart, "chart" );
        final OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) );
        try {
            writeChartAsJPEG ( out, quality, chart, width, height, info );
        } finally {
            out.close();
        }
    }
    public static void writeBufferedImageAsJPEG ( final OutputStream out, final BufferedImage image ) throws IOException {
        writeBufferedImageAsJPEG ( out, 0.75f, image );
    }
    public static void writeBufferedImageAsJPEG ( final OutputStream out, final float quality, final BufferedImage image ) throws IOException {
        EncoderUtil.writeBufferedImage ( image, "jpeg", out, quality );
    }
    public static void writeBufferedImageAsPNG ( final OutputStream out, final BufferedImage image ) throws IOException {
        EncoderUtil.writeBufferedImage ( image, "png", out );
    }
    public static void writeBufferedImageAsPNG ( final OutputStream out, final BufferedImage image, final boolean encodeAlpha, final int compression ) throws IOException {
        EncoderUtil.writeBufferedImage ( image, "png", out, compression, encodeAlpha );
    }
    public static byte[] encodeAsPNG ( final BufferedImage image ) throws IOException {
        return EncoderUtil.encode ( image, "png" );
    }
    public static byte[] encodeAsPNG ( final BufferedImage image, final boolean encodeAlpha, final int compression ) throws IOException {
        return EncoderUtil.encode ( image, "png", compression, encodeAlpha );
    }
    public static void writeImageMap ( final PrintWriter writer, final String name, final ChartRenderingInfo info, final boolean useOverLibForToolTips ) throws IOException {
        ToolTipTagFragmentGenerator toolTipTagFragmentGenerator;
        if ( useOverLibForToolTips ) {
            toolTipTagFragmentGenerator = new OverLIBToolTipTagFragmentGenerator();
        } else {
            toolTipTagFragmentGenerator = new StandardToolTipTagFragmentGenerator();
        }
        ImageMapUtilities.writeImageMap ( writer, name, info, toolTipTagFragmentGenerator, new StandardURLTagFragmentGenerator() );
    }
    public static void writeImageMap ( final PrintWriter writer, final String name, final ChartRenderingInfo info, final ToolTipTagFragmentGenerator toolTipTagFragmentGenerator, final URLTagFragmentGenerator urlTagFragmentGenerator ) throws IOException {
        writer.println ( ImageMapUtilities.getImageMap ( name, info, toolTipTagFragmentGenerator, urlTagFragmentGenerator ) );
    }
    public static String getImageMap ( final String name, final ChartRenderingInfo info ) {
        return ImageMapUtilities.getImageMap ( name, info, new StandardToolTipTagFragmentGenerator(), new StandardURLTagFragmentGenerator() );
    }
    public static String getImageMap ( final String name, final ChartRenderingInfo info, final ToolTipTagFragmentGenerator toolTipTagFragmentGenerator, final URLTagFragmentGenerator urlTagFragmentGenerator ) {
        return ImageMapUtilities.getImageMap ( name, info, toolTipTagFragmentGenerator, urlTagFragmentGenerator );
    }
}
