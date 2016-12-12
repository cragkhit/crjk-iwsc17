

package org.jfree.chart.encoders;

import java.util.HashMap;
import java.util.Map;


public class ImageEncoderFactory {


    private static Map encoders = null;

    static {
        init();
    }


    private static void init() {
        encoders = new HashMap();
        encoders.put ( "jpeg", "org.jfree.chart.encoders.SunJPEGEncoderAdapter" );
        encoders.put ( "png", "org.jfree.chart.encoders.SunPNGEncoderAdapter" );
    }


    public static void setImageEncoder ( String format,
                                         String imageEncoderClassName ) {
        encoders.put ( format, imageEncoderClassName );
    }


    public static ImageEncoder newInstance ( String format ) {
        ImageEncoder imageEncoder = null;
        String className = ( String ) encoders.get ( format );
        if ( className == null ) {
            throw new IllegalArgumentException ( "Unsupported image format - "
                                                 + format );
        }
        try {
            Class imageEncoderClass = Class.forName ( className );
            imageEncoder = ( ImageEncoder ) imageEncoderClass.newInstance();
        } catch ( Exception e ) {
            throw new IllegalArgumentException ( e.toString() );
        }
        return imageEncoder;
    }


    public static ImageEncoder newInstance ( String format, float quality ) {
        ImageEncoder imageEncoder = newInstance ( format );
        imageEncoder.setQuality ( quality );
        return imageEncoder;
    }


    public static ImageEncoder newInstance ( String format,
            boolean encodingAlpha ) {
        ImageEncoder imageEncoder = newInstance ( format );
        imageEncoder.setEncodingAlpha ( encodingAlpha );
        return imageEncoder;
    }


    public static ImageEncoder newInstance ( String format, float quality,
            boolean encodingAlpha ) {
        ImageEncoder imageEncoder = newInstance ( format );
        imageEncoder.setQuality ( quality );
        imageEncoder.setEncodingAlpha ( encodingAlpha );
        return imageEncoder;
    }

}
