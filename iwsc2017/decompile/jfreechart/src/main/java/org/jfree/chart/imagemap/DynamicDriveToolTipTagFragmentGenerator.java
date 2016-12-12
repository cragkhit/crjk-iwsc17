package org.jfree.chart.imagemap;
public class DynamicDriveToolTipTagFragmentGenerator implements ToolTipTagFragmentGenerator {
    protected String title;
    protected int style;
    public DynamicDriveToolTipTagFragmentGenerator() {
        this.title = "";
        this.style = 1;
    }
    public DynamicDriveToolTipTagFragmentGenerator ( final String title, final int style ) {
        this.title = "";
        this.style = 1;
        this.title = title;
        this.style = style;
    }
    @Override
    public String generateToolTipFragment ( final String toolTipText ) {
        return " onMouseOver=\"return stm(['" + ImageMapUtilities.javascriptEscape ( this.title ) + "','" + ImageMapUtilities.javascriptEscape ( toolTipText ) + "'],Style[" + this.style + "]);\" onMouseOut=\"return htm();\"";
    }
}
