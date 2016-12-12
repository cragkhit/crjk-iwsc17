package org.jfree.chart.labels;
import org.jfree.data.category.CategoryDataset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardCategoryItemLabelGenerator extends AbstractCategoryItemLabelGenerator implements CategoryItemLabelGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 3499701401211412882L;
    public static final String DEFAULT_LABEL_FORMAT_STRING = "{2}";
    public StandardCategoryItemLabelGenerator() {
        super ( "{2}", NumberFormat.getInstance() );
    }
    public StandardCategoryItemLabelGenerator ( final String labelFormat, final NumberFormat formatter ) {
        super ( labelFormat, formatter );
    }
    public StandardCategoryItemLabelGenerator ( final String labelFormat, final NumberFormat formatter, final NumberFormat percentFormatter ) {
        super ( labelFormat, formatter, percentFormatter );
    }
    public StandardCategoryItemLabelGenerator ( final String labelFormat, final DateFormat formatter ) {
        super ( labelFormat, formatter );
    }
    @Override
    public String generateLabel ( final CategoryDataset dataset, final int row, final int column ) {
        return this.generateLabelString ( dataset, row, column );
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof StandardCategoryItemLabelGenerator && super.equals ( obj ) );
    }
}
