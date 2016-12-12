package org.jfree.chart.title;
import java.util.Date;
import java.text.DateFormat;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Font;
import java.util.Locale;
import java.io.Serializable;
public class DateTitle extends TextTitle implements Serializable {
    private static final long serialVersionUID = -465434812763159881L;
    public DateTitle() {
        this ( 1 );
    }
    public DateTitle ( final int style ) {
        this ( style, Locale.getDefault(), new Font ( "Dialog", 0, 12 ), Color.black );
    }
    public DateTitle ( final int style, final Locale locale, final Font font, final Paint paint ) {
        this ( style, locale, font, paint, RectangleEdge.BOTTOM, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER, Title.DEFAULT_PADDING );
    }
    public DateTitle ( final int style, final Locale locale, final Font font, final Paint paint, final RectangleEdge position, final HorizontalAlignment horizontalAlignment, final VerticalAlignment verticalAlignment, final RectangleInsets padding ) {
        super ( DateFormat.getDateInstance ( style, locale ).format ( new Date() ), font, paint, position, horizontalAlignment, verticalAlignment, padding );
    }
    public void setDateFormat ( final int style, final Locale locale ) {
        this.setText ( DateFormat.getDateInstance ( style, locale ).format ( new Date() ) );
    }
}
