

package org.jfree.chart.imagemap;


public class StandardToolTipTagFragmentGenerator
    implements ToolTipTagFragmentGenerator {


    public StandardToolTipTagFragmentGenerator() {
        super();
    }


    @Override
    public String generateToolTipFragment ( String toolTipText ) {
        return " title=\"" + ImageMapUtilities.htmlEscape ( toolTipText )
               + "\" alt=\"\"";
    }

}
