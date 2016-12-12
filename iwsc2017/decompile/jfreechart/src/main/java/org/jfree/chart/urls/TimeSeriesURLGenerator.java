package org.jfree.chart.urls;
import java.util.Date;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.util.ParamChecks;
import java.text.DateFormat;
import java.io.Serializable;
public class TimeSeriesURLGenerator implements XYURLGenerator, Serializable {
    private static final long serialVersionUID = -9122773175671182445L;
    private DateFormat dateFormat;
    private String prefix;
    private String seriesParameterName;
    private String itemParameterName;
    public TimeSeriesURLGenerator() {
        this.dateFormat = DateFormat.getInstance();
        this.prefix = "index.html";
        this.seriesParameterName = "series";
        this.itemParameterName = "item";
    }
    public TimeSeriesURLGenerator ( final DateFormat dateFormat, final String prefix, final String seriesParameterName, final String itemParameterName ) {
        this.dateFormat = DateFormat.getInstance();
        this.prefix = "index.html";
        this.seriesParameterName = "series";
        this.itemParameterName = "item";
        ParamChecks.nullNotPermitted ( dateFormat, "dateFormat" );
        ParamChecks.nullNotPermitted ( prefix, "prefix" );
        ParamChecks.nullNotPermitted ( seriesParameterName, "seriesParameterName" );
        ParamChecks.nullNotPermitted ( itemParameterName, "itemParameterName" );
        this.dateFormat = ( DateFormat ) dateFormat.clone();
        this.prefix = prefix;
        this.seriesParameterName = seriesParameterName;
        this.itemParameterName = itemParameterName;
    }
    public DateFormat getDateFormat() {
        return ( DateFormat ) this.dateFormat.clone();
    }
    public String getPrefix() {
        return this.prefix;
    }
    public String getSeriesParameterName() {
        return this.seriesParameterName;
    }
    public String getItemParameterName() {
        return this.itemParameterName;
    }
    @Override
    public String generateURL ( final XYDataset dataset, final int series, final int item ) {
        String result = this.prefix;
        boolean firstParameter = !result.contains ( "?" );
        final Comparable seriesKey = dataset.getSeriesKey ( series );
        if ( seriesKey != null ) {
            result += ( firstParameter ? "?" : "&amp;" );
            try {
                result = result + this.seriesParameterName + "=" + URLEncoder.encode ( seriesKey.toString(), "UTF-8" );
            } catch ( UnsupportedEncodingException ex ) {
                throw new RuntimeException ( ex );
            }
            firstParameter = false;
        }
        final long x = ( long ) dataset.getXValue ( series, item );
        final String xValue = this.dateFormat.format ( new Date ( x ) );
        result += ( firstParameter ? "?" : "&amp;" );
        try {
            result = result + this.itemParameterName + "=" + URLEncoder.encode ( xValue, "UTF-8" );
        } catch ( UnsupportedEncodingException ex2 ) {
            throw new RuntimeException ( ex2 );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TimeSeriesURLGenerator ) ) {
            return false;
        }
        final TimeSeriesURLGenerator that = ( TimeSeriesURLGenerator ) obj;
        return this.dateFormat.equals ( that.dateFormat ) && this.itemParameterName.equals ( that.itemParameterName ) && this.prefix.equals ( that.prefix ) && this.seriesParameterName.equals ( that.seriesParameterName );
    }
}
