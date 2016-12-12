package org.jfree.chart.imagemap;
public class StandardToolTipTagFragmentGenerator implements ToolTipTagFragmentGenerator {
    @Override
    public String generateToolTipFragment ( final String toolTipText ) {
        return " title=\"" + ImageMapUtilities.htmlEscape ( toolTipText ) + "\" alt=\"\"";
    }
}
