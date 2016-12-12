package org.jfree.chart.labels;
import org.jfree.data.category.CategoryDataset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.io.Serializable;
public class StandardCategoryToolTipGenerator extends AbstractCategoryItemLabelGenerator implements CategoryToolTipGenerator, Serializable {
    private static final long serialVersionUID = -6768806592218710764L;
    public static final String DEFAULT_TOOL_TIP_FORMAT_STRING = "({0}, {1}) = {2}";
    public StandardCategoryToolTipGenerator() {
        super ( "({0}, {1}) = {2}", NumberFormat.getInstance() );
    }
    public StandardCategoryToolTipGenerator ( final String labelFormat, final NumberFormat formatter ) {
        super ( labelFormat, formatter );
    }
    public StandardCategoryToolTipGenerator ( final String labelFormat, final DateFormat formatter ) {
        super ( labelFormat, formatter );
    }
    @Override
    public String generateToolTip ( final CategoryDataset dataset, final int row, final int column ) {
        return this.generateLabelString ( dataset, row, column );
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof StandardCategoryToolTipGenerator && super.equals ( obj ) );
    }
}
