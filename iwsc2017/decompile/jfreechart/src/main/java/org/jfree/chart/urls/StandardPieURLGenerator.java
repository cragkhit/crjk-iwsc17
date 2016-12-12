package org.jfree.chart.urls;
import org.jfree.util.ObjectUtilities;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.jfree.data.general.PieDataset;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
public class StandardPieURLGenerator implements PieURLGenerator, Serializable {
    private static final long serialVersionUID = 1626966402065883419L;
    private String prefix;
    private String categoryParamName;
    private String indexParamName;
    public StandardPieURLGenerator() {
        this ( "index.html" );
    }
    public StandardPieURLGenerator ( final String prefix ) {
        this ( prefix, "category" );
    }
    public StandardPieURLGenerator ( final String prefix, final String categoryParamName ) {
        this ( prefix, categoryParamName, "pieIndex" );
    }
    public StandardPieURLGenerator ( final String prefix, final String categoryParamName, final String indexParamName ) {
        this.prefix = "index.html";
        this.categoryParamName = "category";
        this.indexParamName = "pieIndex";
        ParamChecks.nullNotPermitted ( prefix, "prefix" );
        ParamChecks.nullNotPermitted ( categoryParamName, "categoryParamName" );
        this.prefix = prefix;
        this.categoryParamName = categoryParamName;
        this.indexParamName = indexParamName;
    }
    @Override
    public String generateURL ( final PieDataset dataset, final Comparable key, final int pieIndex ) {
        String url = this.prefix;
        try {
            if ( url.contains ( "?" ) ) {
                url = url + "&amp;" + this.categoryParamName + "=" + URLEncoder.encode ( key.toString(), "UTF-8" );
            } else {
                url = url + "?" + this.categoryParamName + "=" + URLEncoder.encode ( key.toString(), "UTF-8" );
            }
            if ( this.indexParamName != null ) {
                url = url + "&amp;" + this.indexParamName + "=" + pieIndex;
            }
        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException ( e );
        }
        return url;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardPieURLGenerator ) ) {
            return false;
        }
        final StandardPieURLGenerator that = ( StandardPieURLGenerator ) obj;
        return this.prefix.equals ( that.prefix ) && this.categoryParamName.equals ( that.categoryParamName ) && ObjectUtilities.equal ( ( Object ) this.indexParamName, ( Object ) that.indexParamName );
    }
}
