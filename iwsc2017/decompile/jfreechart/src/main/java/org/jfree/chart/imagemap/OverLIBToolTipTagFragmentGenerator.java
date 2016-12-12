package org.jfree.chart.imagemap;
public class OverLIBToolTipTagFragmentGenerator implements ToolTipTagFragmentGenerator {
    @Override
    public String generateToolTipFragment ( final String toolTipText ) {
        return " onMouseOver=\"return overlib('" + ImageMapUtilities.javascriptEscape ( toolTipText ) + "');\" onMouseOut=\"return nd();\"";
    }
}
