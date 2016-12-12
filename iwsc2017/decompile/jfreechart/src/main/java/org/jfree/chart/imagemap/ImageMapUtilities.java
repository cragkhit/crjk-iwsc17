package org.jfree.chart.imagemap;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.util.StringUtils;
import java.io.IOException;
import org.jfree.chart.ChartRenderingInfo;
import java.io.PrintWriter;
public class ImageMapUtilities {
    public static void writeImageMap ( final PrintWriter writer, final String name, final ChartRenderingInfo info ) throws IOException {
        writeImageMap ( writer, name, info, new StandardToolTipTagFragmentGenerator(), new StandardURLTagFragmentGenerator() );
    }
    public static void writeImageMap ( final PrintWriter writer, final String name, final ChartRenderingInfo info, final boolean useOverLibForToolTips ) throws IOException {
        ToolTipTagFragmentGenerator toolTipTagFragmentGenerator;
        if ( useOverLibForToolTips ) {
            toolTipTagFragmentGenerator = new OverLIBToolTipTagFragmentGenerator();
        } else {
            toolTipTagFragmentGenerator = new StandardToolTipTagFragmentGenerator();
        }
        writeImageMap ( writer, name, info, toolTipTagFragmentGenerator, new StandardURLTagFragmentGenerator() );
    }
    public static void writeImageMap ( final PrintWriter writer, final String name, final ChartRenderingInfo info, final ToolTipTagFragmentGenerator toolTipTagFragmentGenerator, final URLTagFragmentGenerator urlTagFragmentGenerator ) throws IOException {
        writer.println ( getImageMap ( name, info, toolTipTagFragmentGenerator, urlTagFragmentGenerator ) );
    }
    public static String getImageMap ( final String name, final ChartRenderingInfo info ) {
        return getImageMap ( name, info, new StandardToolTipTagFragmentGenerator(), new StandardURLTagFragmentGenerator() );
    }
    public static String getImageMap ( final String name, final ChartRenderingInfo info, final ToolTipTagFragmentGenerator toolTipTagFragmentGenerator, final URLTagFragmentGenerator urlTagFragmentGenerator ) {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "<map id=\"" ).append ( htmlEscape ( name ) );
        sb.append ( "\" name=\"" ).append ( htmlEscape ( name ) ).append ( "\">" );
        sb.append ( StringUtils.getLineSeparator() );
        final EntityCollection entities = info.getEntityCollection();
        if ( entities != null ) {
            final int count = entities.getEntityCount();
            for ( int i = count - 1; i >= 0; --i ) {
                final ChartEntity entity = entities.getEntity ( i );
                if ( entity.getToolTipText() != null || entity.getURLText() != null ) {
                    final String area = entity.getImageMapAreaTag ( toolTipTagFragmentGenerator, urlTagFragmentGenerator );
                    if ( area.length() > 0 ) {
                        sb.append ( area );
                        sb.append ( StringUtils.getLineSeparator() );
                    }
                }
            }
        }
        sb.append ( "</map>" );
        return sb.toString();
    }
    public static String htmlEscape ( final String input ) {
        ParamChecks.nullNotPermitted ( input, "input" );
        final StringBuilder result = new StringBuilder();
        for ( int length = input.length(), i = 0; i < length; ++i ) {
            final char c = input.charAt ( i );
            if ( c == '&' ) {
                result.append ( "&amp;" );
            } else if ( c == '\"' ) {
                result.append ( "&quot;" );
            } else if ( c == '<' ) {
                result.append ( "&lt;" );
            } else if ( c == '>' ) {
                result.append ( "&gt;" );
            } else if ( c == '\'' ) {
                result.append ( "&#39;" );
            } else if ( c == '\\' ) {
                result.append ( "&#092;" );
            } else {
                result.append ( c );
            }
        }
        return result.toString();
    }
    public static String javascriptEscape ( final String input ) {
        ParamChecks.nullNotPermitted ( input, "input" );
        final StringBuilder result = new StringBuilder();
        for ( int length = input.length(), i = 0; i < length; ++i ) {
            final char c = input.charAt ( i );
            if ( c == '\"' ) {
                result.append ( "\\\"" );
            } else if ( c == '\'' ) {
                result.append ( "\\'" );
            } else if ( c == '\\' ) {
                result.append ( "\\\\" );
            } else {
                result.append ( c );
            }
        }
        return result.toString();
    }
}
