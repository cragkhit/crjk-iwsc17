package org.jfree.chart.urls;
import org.jfree.util.ObjectUtilities;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
public class StandardCategoryURLGenerator implements CategoryURLGenerator, Cloneable, Serializable {
    private static final long serialVersionUID = 2276668053074881909L;
    private String prefix;
    private String seriesParameterName;
    private String categoryParameterName;
    public StandardCategoryURLGenerator() {
        this.prefix = "index.html";
        this.seriesParameterName = "series";
        this.categoryParameterName = "category";
    }
    public StandardCategoryURLGenerator ( final String prefix ) {
        this.prefix = "index.html";
        this.seriesParameterName = "series";
        this.categoryParameterName = "category";
        ParamChecks.nullNotPermitted ( prefix, "prefix" );
        this.prefix = prefix;
    }
    public StandardCategoryURLGenerator ( final String prefix, final String seriesParameterName, final String categoryParameterName ) {
        this.prefix = "index.html";
        this.seriesParameterName = "series";
        this.categoryParameterName = "category";
        ParamChecks.nullNotPermitted ( prefix, "prefix" );
        ParamChecks.nullNotPermitted ( seriesParameterName, "seriesParameterName" );
        ParamChecks.nullNotPermitted ( categoryParameterName, "categoryParameterName" );
        this.prefix = prefix;
        this.seriesParameterName = seriesParameterName;
        this.categoryParameterName = categoryParameterName;
    }
    @Override
    public String generateURL ( final CategoryDataset dataset, final int series, final int category ) {
        String url = this.prefix;
        final Comparable seriesKey = dataset.getRowKey ( series );
        final Comparable categoryKey = dataset.getColumnKey ( category );
        final boolean firstParameter = !url.contains ( "?" );
        url += ( firstParameter ? "?" : "&amp;" );
        try {
            url = url + this.seriesParameterName + "=" + URLEncoder.encode ( seriesKey.toString(), "UTF-8" );
            url = url + "&amp;" + this.categoryParameterName + "=" + URLEncoder.encode ( categoryKey.toString(), "UTF-8" );
        } catch ( UnsupportedEncodingException ex ) {
            throw new RuntimeException ( ex );
        }
        return url;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardCategoryURLGenerator ) ) {
            return false;
        }
        final StandardCategoryURLGenerator that = ( StandardCategoryURLGenerator ) obj;
        return ObjectUtilities.equal ( ( Object ) this.prefix, ( Object ) that.prefix ) && ObjectUtilities.equal ( ( Object ) this.seriesParameterName, ( Object ) that.seriesParameterName ) && ObjectUtilities.equal ( ( Object ) this.categoryParameterName, ( Object ) that.categoryParameterName );
    }
    @Override
    public int hashCode() {
        int result = ( this.prefix != null ) ? this.prefix.hashCode() : 0;
        result = 29 * result + ( ( this.seriesParameterName != null ) ? this.seriesParameterName.hashCode() : 0 );
        result = 29 * result + ( ( this.categoryParameterName != null ) ? this.categoryParameterName.hashCode() : 0 );
        return result;
    }
}
