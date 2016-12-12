package org.jfree.chart.axis;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.TextAnchor;
import java.util.ArrayList;
import org.jfree.data.time.Year;
import java.util.List;
import java.awt.FontMetrics;
import java.awt.font.LineMetrics;
import java.awt.font.FontRenderContext;
import java.awt.Font;
import org.jfree.ui.RectangleInsets;
import java.awt.Graphics2D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Month;
import java.util.Calendar;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.Range;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DateFormat;
import java.util.Date;
import org.jfree.data.time.DateRange;
import java.io.Serializable;
public class DateAxis extends ValueAxis implements Cloneable, Serializable {
    private static final long serialVersionUID = -1013460999649007604L;
    public static final DateRange DEFAULT_DATE_RANGE;
    public static final double DEFAULT_AUTO_RANGE_MINIMUM_SIZE_IN_MILLISECONDS = 2.0;
    public static final DateTickUnit DEFAULT_DATE_TICK_UNIT;
    public static final Date DEFAULT_ANCHOR_DATE;
    private DateTickUnit tickUnit;
    private DateFormat dateFormatOverride;
    private DateTickMarkPosition tickMarkPosition;
    private static final Timeline DEFAULT_TIMELINE;
    private TimeZone timeZone;
    private Locale locale;
    private Timeline timeline;
    public DateAxis() {
        this ( null );
    }
    public DateAxis ( final String label ) {
        this ( label, TimeZone.getDefault() );
    }
    public DateAxis ( final String label, final TimeZone zone ) {
        this ( label, zone, Locale.getDefault() );
    }
    public DateAxis ( final String label, final TimeZone zone, final Locale locale ) {
        super ( label, createStandardDateTickUnits ( zone, locale ) );
        this.tickMarkPosition = DateTickMarkPosition.START;
        this.tickUnit = new DateTickUnit ( DateTickUnitType.DAY, 1, new SimpleDateFormat() );
        this.setAutoRangeMinimumSize ( 2.0 );
        this.setRange ( DateAxis.DEFAULT_DATE_RANGE, false, false );
        this.dateFormatOverride = null;
        this.timeZone = zone;
        this.locale = locale;
        this.timeline = DateAxis.DEFAULT_TIMELINE;
    }
    public TimeZone getTimeZone() {
        return this.timeZone;
    }
    public void setTimeZone ( final TimeZone zone ) {
        ParamChecks.nullNotPermitted ( zone, "zone" );
        this.timeZone = zone;
        this.setStandardTickUnits ( createStandardDateTickUnits ( zone, this.locale ) );
        this.fireChangeEvent();
    }
    public Locale getLocale() {
        return this.locale;
    }
    public void setLocale ( final Locale locale ) {
        ParamChecks.nullNotPermitted ( locale, "locale" );
        this.locale = locale;
        this.setStandardTickUnits ( createStandardDateTickUnits ( this.timeZone, this.locale ) );
        this.fireChangeEvent();
    }
    public Timeline getTimeline() {
        return this.timeline;
    }
    public void setTimeline ( final Timeline timeline ) {
        if ( this.timeline != timeline ) {
            this.timeline = timeline;
            this.fireChangeEvent();
        }
    }
    public DateTickUnit getTickUnit() {
        return this.tickUnit;
    }
    public void setTickUnit ( final DateTickUnit unit ) {
        this.setTickUnit ( unit, true, true );
    }
    public void setTickUnit ( final DateTickUnit unit, final boolean notify, final boolean turnOffAutoSelection ) {
        this.tickUnit = unit;
        if ( turnOffAutoSelection ) {
            this.setAutoTickUnitSelection ( false, false );
        }
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public DateFormat getDateFormatOverride() {
        return this.dateFormatOverride;
    }
    public void setDateFormatOverride ( final DateFormat formatter ) {
        this.dateFormatOverride = formatter;
        this.fireChangeEvent();
    }
    @Override
    public void setRange ( final Range range ) {
        this.setRange ( range, true, true );
    }
    @Override
    public void setRange ( Range range, final boolean turnOffAutoRange, final boolean notify ) {
        ParamChecks.nullNotPermitted ( range, "range" );
        if ( ! ( range instanceof DateRange ) ) {
            range = new DateRange ( range );
        }
        super.setRange ( range, turnOffAutoRange, notify );
    }
    public void setRange ( final Date lower, final Date upper ) {
        if ( lower.getTime() >= upper.getTime() ) {
            throw new IllegalArgumentException ( "Requires 'lower' < 'upper'." );
        }
        this.setRange ( new DateRange ( lower, upper ) );
    }
    @Override
    public void setRange ( final double lower, final double upper ) {
        if ( lower >= upper ) {
            throw new IllegalArgumentException ( "Requires 'lower' < 'upper'." );
        }
        this.setRange ( new DateRange ( lower, upper ) );
    }
    public Date getMinimumDate() {
        final Range range = this.getRange();
        Date result;
        if ( range instanceof DateRange ) {
            final DateRange r = ( DateRange ) range;
            result = r.getLowerDate();
        } else {
            result = new Date ( ( long ) range.getLowerBound() );
        }
        return result;
    }
    public void setMinimumDate ( final Date date ) {
        ParamChecks.nullNotPermitted ( date, "date" );
        Date maxDate = this.getMaximumDate();
        final long maxMillis = maxDate.getTime();
        final long newMinMillis = date.getTime();
        if ( maxMillis <= newMinMillis ) {
            final Date oldMin = this.getMinimumDate();
            final long length = maxMillis - oldMin.getTime();
            maxDate = new Date ( newMinMillis + length );
        }
        this.setRange ( new DateRange ( date, maxDate ), true, false );
        this.fireChangeEvent();
    }
    public Date getMaximumDate() {
        final Range range = this.getRange();
        Date result;
        if ( range instanceof DateRange ) {
            final DateRange r = ( DateRange ) range;
            result = r.getUpperDate();
        } else {
            result = new Date ( ( long ) range.getUpperBound() );
        }
        return result;
    }
    public void setMaximumDate ( final Date maximumDate ) {
        ParamChecks.nullNotPermitted ( maximumDate, "maximumDate" );
        Date minDate = this.getMinimumDate();
        final long minMillis = minDate.getTime();
        final long newMaxMillis = maximumDate.getTime();
        if ( minMillis >= newMaxMillis ) {
            final Date oldMax = this.getMaximumDate();
            final long length = oldMax.getTime() - minMillis;
            minDate = new Date ( newMaxMillis - length );
        }
        this.setRange ( new DateRange ( minDate, maximumDate ), true, false );
        this.fireChangeEvent();
    }
    public DateTickMarkPosition getTickMarkPosition() {
        return this.tickMarkPosition;
    }
    public void setTickMarkPosition ( final DateTickMarkPosition position ) {
        ParamChecks.nullNotPermitted ( position, "position" );
        this.tickMarkPosition = position;
        this.fireChangeEvent();
    }
    @Override
    public void configure() {
        if ( this.isAutoRange() ) {
            this.autoAdjustRange();
        }
    }
    public boolean isHiddenValue ( final long millis ) {
        return !this.timeline.containsDomainValue ( new Date ( millis ) );
    }
    @Override
    public double valueToJava2D ( double value, final Rectangle2D area, final RectangleEdge edge ) {
        value = this.timeline.toTimelineValue ( ( long ) value );
        final DateRange range = ( DateRange ) this.getRange();
        final double axisMin = this.timeline.toTimelineValue ( range.getLowerMillis() );
        final double axisMax = this.timeline.toTimelineValue ( range.getUpperMillis() );
        double result = 0.0;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            final double minX = area.getX();
            final double maxX = area.getMaxX();
            if ( this.isInverted() ) {
                result = maxX + ( value - axisMin ) / ( axisMax - axisMin ) * ( minX - maxX );
            } else {
                result = minX + ( value - axisMin ) / ( axisMax - axisMin ) * ( maxX - minX );
            }
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            final double minY = area.getMinY();
            final double maxY = area.getMaxY();
            if ( this.isInverted() ) {
                result = minY + ( value - axisMin ) / ( axisMax - axisMin ) * ( maxY - minY );
            } else {
                result = maxY - ( value - axisMin ) / ( axisMax - axisMin ) * ( maxY - minY );
            }
        }
        return result;
    }
    public double dateToJava2D ( final Date date, final Rectangle2D area, final RectangleEdge edge ) {
        final double value = date.getTime();
        return this.valueToJava2D ( value, area, edge );
    }
    @Override
    public double java2DToValue ( final double java2DValue, final Rectangle2D area, final RectangleEdge edge ) {
        final DateRange range = ( DateRange ) this.getRange();
        final double axisMin = this.timeline.toTimelineValue ( range.getLowerMillis() );
        final double axisMax = this.timeline.toTimelineValue ( range.getUpperMillis() );
        double min = 0.0;
        double max = 0.0;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            min = area.getX();
            max = area.getMaxX();
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            min = area.getMaxY();
            max = area.getY();
        }
        double result;
        if ( this.isInverted() ) {
            result = axisMax - ( java2DValue - min ) / ( max - min ) * ( axisMax - axisMin );
        } else {
            result = axisMin + ( java2DValue - min ) / ( max - min ) * ( axisMax - axisMin );
        }
        return this.timeline.toMillisecond ( ( long ) result );
    }
    public Date calculateLowestVisibleTickValue ( final DateTickUnit unit ) {
        return this.nextStandardDate ( this.getMinimumDate(), unit );
    }
    public Date calculateHighestVisibleTickValue ( final DateTickUnit unit ) {
        return this.previousStandardDate ( this.getMaximumDate(), unit );
    }
    protected Date previousStandardDate ( final Date date, final DateTickUnit unit ) {
        final Calendar calendar = Calendar.getInstance ( this.timeZone, this.locale );
        calendar.setTime ( date );
        final int count = unit.getMultiple();
        final int current = calendar.get ( unit.getCalendarField() );
        int value = count * ( current / count );
        switch ( unit.getUnit() ) {
        case 6: {
            final int years = calendar.get ( 1 );
            final int months = calendar.get ( 2 );
            final int days = calendar.get ( 5 );
            final int hours = calendar.get ( 11 );
            final int minutes = calendar.get ( 12 );
            final int seconds = calendar.get ( 13 );
            calendar.set ( years, months, days, hours, minutes, seconds );
            calendar.set ( 14, value );
            Date mm = calendar.getTime();
            if ( mm.getTime() >= date.getTime() ) {
                calendar.set ( 14, value - count );
                mm = calendar.getTime();
            }
            return mm;
        }
        case 5: {
            final int years = calendar.get ( 1 );
            final int months = calendar.get ( 2 );
            final int days = calendar.get ( 5 );
            final int hours = calendar.get ( 11 );
            final int minutes = calendar.get ( 12 );
            int milliseconds;
            if ( this.tickMarkPosition == DateTickMarkPosition.START ) {
                milliseconds = 0;
            } else if ( this.tickMarkPosition == DateTickMarkPosition.MIDDLE ) {
                milliseconds = 500;
            } else {
                milliseconds = 999;
            }
            calendar.set ( 14, milliseconds );
            calendar.set ( years, months, days, hours, minutes, value );
            Date dd = calendar.getTime();
            if ( dd.getTime() >= date.getTime() ) {
                calendar.set ( 13, value - count );
                dd = calendar.getTime();
            }
            return dd;
        }
        case 4: {
            final int years = calendar.get ( 1 );
            final int months = calendar.get ( 2 );
            final int days = calendar.get ( 5 );
            final int hours = calendar.get ( 11 );
            int seconds;
            if ( this.tickMarkPosition == DateTickMarkPosition.START ) {
                seconds = 0;
            } else if ( this.tickMarkPosition == DateTickMarkPosition.MIDDLE ) {
                seconds = 30;
            } else {
                seconds = 59;
            }
            calendar.clear ( 14 );
            calendar.set ( years, months, days, hours, value, seconds );
            Date d0 = calendar.getTime();
            if ( d0.getTime() >= date.getTime() ) {
                calendar.set ( 12, value - count );
                d0 = calendar.getTime();
            }
            return d0;
        }
        case 3: {
            final int years = calendar.get ( 1 );
            final int months = calendar.get ( 2 );
            final int days = calendar.get ( 5 );
            int minutes;
            int seconds;
            if ( this.tickMarkPosition == DateTickMarkPosition.START ) {
                minutes = 0;
                seconds = 0;
            } else if ( this.tickMarkPosition == DateTickMarkPosition.MIDDLE ) {
                minutes = 30;
                seconds = 0;
            } else {
                minutes = 59;
                seconds = 59;
            }
            calendar.clear ( 14 );
            calendar.set ( years, months, days, value, minutes, seconds );
            Date d = calendar.getTime();
            if ( d.getTime() >= date.getTime() ) {
                calendar.set ( 11, value - count );
                d = calendar.getTime();
            }
            return d;
        }
        case 2: {
            final int years = calendar.get ( 1 );
            final int months = calendar.get ( 2 );
            int hours;
            if ( this.tickMarkPosition == DateTickMarkPosition.START ) {
                hours = 0;
            } else if ( this.tickMarkPosition == DateTickMarkPosition.MIDDLE ) {
                hours = 12;
            } else {
                hours = 23;
            }
            calendar.clear ( 14 );
            calendar.set ( years, months, value, hours, 0, 0 );
            Date d2 = calendar.getTime();
            if ( d2.getTime() >= date.getTime() ) {
                calendar.set ( 5, value - count );
                d2 = calendar.getTime();
            }
            return d2;
        }
        case 1: {
            value = count * ( ( current + 1 ) / count ) - 1;
            final int years = calendar.get ( 1 );
            calendar.clear ( 14 );
            calendar.set ( years, value, 1, 0, 0, 0 );
            Month month = new Month ( calendar.getTime(), this.timeZone, this.locale );
            Date standardDate = this.calculateDateForPosition ( month, this.tickMarkPosition );
            final long millis = standardDate.getTime();
            if ( millis >= date.getTime() ) {
                for ( int i = 0; i < count; ++i ) {
                    month = ( Month ) month.previous();
                }
                month.peg ( Calendar.getInstance ( this.timeZone ) );
                standardDate = this.calculateDateForPosition ( month, this.tickMarkPosition );
            }
            return standardDate;
        }
        case 0: {
            int months;
            int days;
            if ( this.tickMarkPosition == DateTickMarkPosition.START ) {
                months = 0;
                days = 1;
            } else if ( this.tickMarkPosition == DateTickMarkPosition.MIDDLE ) {
                months = 6;
                days = 1;
            } else {
                months = 11;
                days = 31;
            }
            calendar.clear ( 14 );
            calendar.set ( value, months, days, 0, 0, 0 );
            Date d3 = calendar.getTime();
            if ( d3.getTime() >= date.getTime() ) {
                calendar.set ( 1, value - count );
                d3 = calendar.getTime();
            }
            return d3;
        }
        default: {
            return null;
        }
        }
    }
    private Date calculateDateForPosition ( final RegularTimePeriod period, final DateTickMarkPosition position ) {
        ParamChecks.nullNotPermitted ( period, "period" );
        Date result = null;
        if ( position == DateTickMarkPosition.START ) {
            result = new Date ( period.getFirstMillisecond() );
        } else if ( position == DateTickMarkPosition.MIDDLE ) {
            result = new Date ( period.getMiddleMillisecond() );
        } else if ( position == DateTickMarkPosition.END ) {
            result = new Date ( period.getLastMillisecond() );
        }
        return result;
    }
    protected Date nextStandardDate ( final Date date, final DateTickUnit unit ) {
        final Date previous = this.previousStandardDate ( date, unit );
        final Calendar calendar = Calendar.getInstance ( this.timeZone, this.locale );
        calendar.setTime ( previous );
        calendar.add ( unit.getCalendarField(), unit.getMultiple() );
        return calendar.getTime();
    }
    public static TickUnitSource createStandardDateTickUnits() {
        return createStandardDateTickUnits ( TimeZone.getDefault(), Locale.getDefault() );
    }
    public static TickUnitSource createStandardDateTickUnits ( final TimeZone zone, final Locale locale ) {
        ParamChecks.nullNotPermitted ( zone, "zone" );
        ParamChecks.nullNotPermitted ( locale, "locale" );
        final TickUnits units = new TickUnits();
        final DateFormat f1 = new SimpleDateFormat ( "HH:mm:ss.SSS", locale );
        final DateFormat f2 = new SimpleDateFormat ( "HH:mm:ss", locale );
        final DateFormat f3 = new SimpleDateFormat ( "HH:mm", locale );
        final DateFormat f4 = new SimpleDateFormat ( "d-MMM, HH:mm", locale );
        final DateFormat f5 = new SimpleDateFormat ( "d-MMM", locale );
        final DateFormat f6 = new SimpleDateFormat ( "MMM-yyyy", locale );
        final DateFormat f7 = new SimpleDateFormat ( "yyyy", locale );
        f1.setTimeZone ( zone );
        f2.setTimeZone ( zone );
        f3.setTimeZone ( zone );
        f4.setTimeZone ( zone );
        f5.setTimeZone ( zone );
        f6.setTimeZone ( zone );
        f7.setTimeZone ( zone );
        units.add ( new DateTickUnit ( DateTickUnitType.MILLISECOND, 1, f1 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MILLISECOND, 5, DateTickUnitType.MILLISECOND, 1, f1 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MILLISECOND, 10, DateTickUnitType.MILLISECOND, 1, f1 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MILLISECOND, 25, DateTickUnitType.MILLISECOND, 5, f1 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MILLISECOND, 50, DateTickUnitType.MILLISECOND, 10, f1 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MILLISECOND, 100, DateTickUnitType.MILLISECOND, 10, f1 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MILLISECOND, 250, DateTickUnitType.MILLISECOND, 10, f1 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MILLISECOND, 500, DateTickUnitType.MILLISECOND, 50, f1 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.SECOND, 1, DateTickUnitType.MILLISECOND, 50, f2 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.SECOND, 5, DateTickUnitType.SECOND, 1, f2 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.SECOND, 10, DateTickUnitType.SECOND, 1, f2 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.SECOND, 30, DateTickUnitType.SECOND, 5, f2 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MINUTE, 1, DateTickUnitType.SECOND, 5, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MINUTE, 2, DateTickUnitType.SECOND, 10, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MINUTE, 5, DateTickUnitType.MINUTE, 1, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MINUTE, 10, DateTickUnitType.MINUTE, 1, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MINUTE, 15, DateTickUnitType.MINUTE, 5, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MINUTE, 20, DateTickUnitType.MINUTE, 5, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MINUTE, 30, DateTickUnitType.MINUTE, 5, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.HOUR, 1, DateTickUnitType.MINUTE, 5, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.HOUR, 2, DateTickUnitType.MINUTE, 10, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.HOUR, 4, DateTickUnitType.MINUTE, 30, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.HOUR, 6, DateTickUnitType.HOUR, 1, f3 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.HOUR, 12, DateTickUnitType.HOUR, 1, f4 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.DAY, 1, DateTickUnitType.HOUR, 1, f5 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.DAY, 2, DateTickUnitType.HOUR, 1, f5 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.DAY, 7, DateTickUnitType.DAY, 1, f5 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.DAY, 15, DateTickUnitType.DAY, 1, f5 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MONTH, 1, DateTickUnitType.DAY, 1, f6 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MONTH, 2, DateTickUnitType.DAY, 1, f6 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MONTH, 3, DateTickUnitType.MONTH, 1, f6 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MONTH, 4, DateTickUnitType.MONTH, 1, f6 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.MONTH, 6, DateTickUnitType.MONTH, 1, f6 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.YEAR, 1, DateTickUnitType.MONTH, 1, f7 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.YEAR, 2, DateTickUnitType.MONTH, 3, f7 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.YEAR, 5, DateTickUnitType.YEAR, 1, f7 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.YEAR, 10, DateTickUnitType.YEAR, 1, f7 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.YEAR, 25, DateTickUnitType.YEAR, 5, f7 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.YEAR, 50, DateTickUnitType.YEAR, 10, f7 ) );
        units.add ( new DateTickUnit ( DateTickUnitType.YEAR, 100, DateTickUnitType.YEAR, 20, f7 ) );
        return units;
    }
    @Override
    protected void autoAdjustRange() {
        final Plot plot = this.getPlot();
        if ( plot == null ) {
            return;
        }
        if ( plot instanceof ValueAxisPlot ) {
            final ValueAxisPlot vap = ( ValueAxisPlot ) plot;
            Range r = vap.getDataRange ( this );
            if ( r == null ) {
                if ( this.timeline instanceof SegmentedTimeline ) {
                    r = new DateRange ( ( ( SegmentedTimeline ) this.timeline ).getStartTime(), ( ( SegmentedTimeline ) this.timeline ).getStartTime() + 1L );
                } else {
                    r = new DateRange();
                }
            }
            long upper = this.timeline.toTimelineValue ( ( long ) r.getUpperBound() );
            final long fixedAutoRange = ( long ) this.getFixedAutoRange();
            long lower;
            if ( fixedAutoRange > 0.0 ) {
                lower = upper - fixedAutoRange;
            } else {
                lower = this.timeline.toTimelineValue ( ( long ) r.getLowerBound() );
                final double range = upper - lower;
                final long minRange = ( long ) this.getAutoRangeMinimumSize();
                if ( range < minRange ) {
                    final long expand = ( long ) ( minRange - range ) / 2L;
                    upper += expand;
                    lower -= expand;
                }
                upper += ( long ) ( range * this.getUpperMargin() );
                lower -= ( long ) ( range * this.getLowerMargin() );
            }
            upper = this.timeline.toMillisecond ( upper );
            lower = this.timeline.toMillisecond ( lower );
            final DateRange dr = new DateRange ( new Date ( lower ), new Date ( upper ) );
            this.setRange ( dr, false, false );
        }
    }
    protected void selectAutoTickUnit ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            this.selectHorizontalAutoTickUnit ( g2, dataArea, edge );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            this.selectVerticalAutoTickUnit ( g2, dataArea, edge );
        }
    }
    protected void selectHorizontalAutoTickUnit ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        long shift = 0L;
        if ( this.timeline instanceof SegmentedTimeline ) {
            shift = ( ( SegmentedTimeline ) this.timeline ).getStartTime();
        }
        final double zero = this.valueToJava2D ( shift + 0.0, dataArea, edge );
        double tickLabelWidth = this.estimateMaximumTickLabelWidth ( g2, this.getTickUnit() );
        final TickUnitSource tickUnits = this.getStandardTickUnits();
        final TickUnit unit1 = tickUnits.getCeilingTickUnit ( this.getTickUnit() );
        final double x1 = this.valueToJava2D ( shift + unit1.getSize(), dataArea, edge );
        final double unit1Width = Math.abs ( x1 - zero );
        final double guess = tickLabelWidth / unit1Width * unit1.getSize();
        DateTickUnit unit2 = ( DateTickUnit ) tickUnits.getCeilingTickUnit ( guess );
        final double x2 = this.valueToJava2D ( shift + unit2.getSize(), dataArea, edge );
        final double unit2Width = Math.abs ( x2 - zero );
        tickLabelWidth = this.estimateMaximumTickLabelWidth ( g2, unit2 );
        if ( tickLabelWidth > unit2Width ) {
            unit2 = ( DateTickUnit ) tickUnits.getLargerTickUnit ( unit2 );
        }
        this.setTickUnit ( unit2, false, false );
    }
    protected void selectVerticalAutoTickUnit ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final TickUnitSource tickUnits = this.getStandardTickUnits();
        final double zero = this.valueToJava2D ( 0.0, dataArea, edge );
        final double estimate1 = this.getRange().getLength() / 10.0;
        final DateTickUnit candidate1 = ( DateTickUnit ) tickUnits.getCeilingTickUnit ( estimate1 );
        final double labelHeight1 = this.estimateMaximumTickLabelHeight ( g2, candidate1 );
        final double y1 = this.valueToJava2D ( candidate1.getSize(), dataArea, edge );
        final double candidate1UnitHeight = Math.abs ( y1 - zero );
        final double estimate2 = labelHeight1 / candidate1UnitHeight * candidate1.getSize();
        final DateTickUnit candidate2 = ( DateTickUnit ) tickUnits.getCeilingTickUnit ( estimate2 );
        final double labelHeight2 = this.estimateMaximumTickLabelHeight ( g2, candidate2 );
        final double y2 = this.valueToJava2D ( candidate2.getSize(), dataArea, edge );
        final double unit2Height = Math.abs ( y2 - zero );
        DateTickUnit finalUnit;
        if ( labelHeight2 < unit2Height ) {
            finalUnit = candidate2;
        } else {
            finalUnit = ( DateTickUnit ) tickUnits.getLargerTickUnit ( candidate2 );
        }
        this.setTickUnit ( finalUnit, false, false );
    }
    private double estimateMaximumTickLabelWidth ( final Graphics2D g2, final DateTickUnit unit ) {
        final RectangleInsets tickLabelInsets = this.getTickLabelInsets();
        double result = tickLabelInsets.getLeft() + tickLabelInsets.getRight();
        final Font tickLabelFont = this.getTickLabelFont();
        final FontRenderContext frc = g2.getFontRenderContext();
        final LineMetrics lm = tickLabelFont.getLineMetrics ( "ABCxyz", frc );
        if ( this.isVerticalTickLabels() ) {
            result += lm.getHeight();
        } else {
            final DateRange range = ( DateRange ) this.getRange();
            final Date lower = range.getLowerDate();
            final Date upper = range.getUpperDate();
            final DateFormat formatter = this.getDateFormatOverride();
            String lowerStr;
            String upperStr;
            if ( formatter != null ) {
                lowerStr = formatter.format ( lower );
                upperStr = formatter.format ( upper );
            } else {
                lowerStr = unit.dateToString ( lower );
                upperStr = unit.dateToString ( upper );
            }
            final FontMetrics fm = g2.getFontMetrics ( tickLabelFont );
            final double w1 = fm.stringWidth ( lowerStr );
            final double w2 = fm.stringWidth ( upperStr );
            result += Math.max ( w1, w2 );
        }
        return result;
    }
    private double estimateMaximumTickLabelHeight ( final Graphics2D g2, final DateTickUnit unit ) {
        final RectangleInsets tickLabelInsets = this.getTickLabelInsets();
        double result = tickLabelInsets.getTop() + tickLabelInsets.getBottom();
        final Font tickLabelFont = this.getTickLabelFont();
        final FontRenderContext frc = g2.getFontRenderContext();
        final LineMetrics lm = tickLabelFont.getLineMetrics ( "ABCxyz", frc );
        if ( !this.isVerticalTickLabels() ) {
            result += lm.getHeight();
        } else {
            final DateRange range = ( DateRange ) this.getRange();
            final Date lower = range.getLowerDate();
            final Date upper = range.getUpperDate();
            final DateFormat formatter = this.getDateFormatOverride();
            String lowerStr;
            String upperStr;
            if ( formatter != null ) {
                lowerStr = formatter.format ( lower );
                upperStr = formatter.format ( upper );
            } else {
                lowerStr = unit.dateToString ( lower );
                upperStr = unit.dateToString ( upper );
            }
            final FontMetrics fm = g2.getFontMetrics ( tickLabelFont );
            final double w1 = fm.stringWidth ( lowerStr );
            final double w2 = fm.stringWidth ( upperStr );
            result += Math.max ( w1, w2 );
        }
        return result;
    }
    @Override
    public List refreshTicks ( final Graphics2D g2, final AxisState state, final Rectangle2D dataArea, final RectangleEdge edge ) {
        List result = null;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            result = this.refreshTicksHorizontal ( g2, dataArea, edge );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            result = this.refreshTicksVertical ( g2, dataArea, edge );
        }
        return result;
    }
    private Date correctTickDateForPosition ( final Date time, final DateTickUnit unit, final DateTickMarkPosition position ) {
        Date result = time;
        switch ( unit.getUnit() ) {
        case 1: {
            result = this.calculateDateForPosition ( new Month ( time, this.timeZone, this.locale ), position );
            break;
        }
        case 0: {
            result = this.calculateDateForPosition ( new Year ( time, this.timeZone, this.locale ), position );
            break;
        }
        }
        return result;
    }
    protected List refreshTicksHorizontal ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final List result = new ArrayList();
        final Font tickLabelFont = this.getTickLabelFont();
        g2.setFont ( tickLabelFont );
        if ( this.isAutoTickUnitSelection() ) {
            this.selectAutoTickUnit ( g2, dataArea, edge );
        }
        final DateTickUnit unit = this.getTickUnit();
        Date tickDate = this.calculateLowestVisibleTickValue ( unit );
        final Date upperDate = this.getMaximumDate();
        boolean hasRolled = false;
        while ( tickDate.before ( upperDate ) ) {
            if ( !hasRolled ) {
                tickDate = this.correctTickDateForPosition ( tickDate, unit, this.tickMarkPosition );
            }
            final long lowestTickTime = tickDate.getTime();
            final long distance = unit.addToDate ( tickDate, this.timeZone ).getTime() - lowestTickTime;
            int minorTickSpaces = this.getMinorTickCount();
            if ( minorTickSpaces <= 0 ) {
                minorTickSpaces = unit.getMinorTickCount();
            }
            for ( int minorTick = 1; minorTick < minorTickSpaces; ++minorTick ) {
                final long minorTickTime = lowestTickTime - distance * minorTick / minorTickSpaces;
                if ( minorTickTime > 0L && this.getRange().contains ( minorTickTime ) && !this.isHiddenValue ( minorTickTime ) ) {
                    result.add ( new DateTick ( TickType.MINOR, new Date ( minorTickTime ), "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0 ) );
                }
            }
            if ( !this.isHiddenValue ( tickDate.getTime() ) ) {
                final DateFormat formatter = this.getDateFormatOverride();
                String tickLabel;
                if ( formatter != null ) {
                    tickLabel = formatter.format ( tickDate );
                } else {
                    tickLabel = this.tickUnit.dateToString ( tickDate );
                }
                double angle = 0.0;
                TextAnchor anchor;
                TextAnchor rotationAnchor;
                if ( this.isVerticalTickLabels() ) {
                    anchor = TextAnchor.CENTER_RIGHT;
                    rotationAnchor = TextAnchor.CENTER_RIGHT;
                    if ( edge == RectangleEdge.TOP ) {
                        angle = 1.5707963267948966;
                    } else {
                        angle = -1.5707963267948966;
                    }
                } else if ( edge == RectangleEdge.TOP ) {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    rotationAnchor = TextAnchor.BOTTOM_CENTER;
                } else {
                    anchor = TextAnchor.TOP_CENTER;
                    rotationAnchor = TextAnchor.TOP_CENTER;
                }
                final Tick tick = new DateTick ( tickDate, tickLabel, anchor, rotationAnchor, angle );
                result.add ( tick );
                hasRolled = false;
                final long currentTickTime = tickDate.getTime();
                tickDate = unit.addToDate ( tickDate, this.timeZone );
                final long nextTickTime = tickDate.getTime();
                for ( int minorTick2 = 1; minorTick2 < minorTickSpaces; ++minorTick2 ) {
                    final long minorTickTime2 = currentTickTime + ( nextTickTime - currentTickTime ) * minorTick2 / minorTickSpaces;
                    if ( this.getRange().contains ( minorTickTime2 ) && !this.isHiddenValue ( minorTickTime2 ) ) {
                        result.add ( new DateTick ( TickType.MINOR, new Date ( minorTickTime2 ), "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0 ) );
                    }
                }
            } else {
                tickDate = unit.rollDate ( tickDate, this.timeZone );
                hasRolled = true;
            }
        }
        return result;
    }
    protected List refreshTicksVertical ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final List result = new ArrayList();
        final Font tickLabelFont = this.getTickLabelFont();
        g2.setFont ( tickLabelFont );
        if ( this.isAutoTickUnitSelection() ) {
            this.selectAutoTickUnit ( g2, dataArea, edge );
        }
        final DateTickUnit unit = this.getTickUnit();
        Date tickDate = this.calculateLowestVisibleTickValue ( unit );
        final Date upperDate = this.getMaximumDate();
        boolean hasRolled = false;
        while ( tickDate.before ( upperDate ) ) {
            if ( !hasRolled ) {
                tickDate = this.correctTickDateForPosition ( tickDate, unit, this.tickMarkPosition );
            }
            final long lowestTickTime = tickDate.getTime();
            final long distance = unit.addToDate ( tickDate, this.timeZone ).getTime() - lowestTickTime;
            int minorTickSpaces = this.getMinorTickCount();
            if ( minorTickSpaces <= 0 ) {
                minorTickSpaces = unit.getMinorTickCount();
            }
            for ( int minorTick = 1; minorTick < minorTickSpaces; ++minorTick ) {
                final long minorTickTime = lowestTickTime - distance * minorTick / minorTickSpaces;
                if ( minorTickTime > 0L && this.getRange().contains ( minorTickTime ) && !this.isHiddenValue ( minorTickTime ) ) {
                    result.add ( new DateTick ( TickType.MINOR, new Date ( minorTickTime ), "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0 ) );
                }
            }
            if ( !this.isHiddenValue ( tickDate.getTime() ) ) {
                final DateFormat formatter = this.getDateFormatOverride();
                String tickLabel;
                if ( formatter != null ) {
                    tickLabel = formatter.format ( tickDate );
                } else {
                    tickLabel = this.tickUnit.dateToString ( tickDate );
                }
                double angle = 0.0;
                TextAnchor anchor;
                TextAnchor rotationAnchor;
                if ( this.isVerticalTickLabels() ) {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    rotationAnchor = TextAnchor.BOTTOM_CENTER;
                    if ( edge == RectangleEdge.LEFT ) {
                        angle = -1.5707963267948966;
                    } else {
                        angle = 1.5707963267948966;
                    }
                } else if ( edge == RectangleEdge.LEFT ) {
                    anchor = TextAnchor.CENTER_RIGHT;
                    rotationAnchor = TextAnchor.CENTER_RIGHT;
                } else {
                    anchor = TextAnchor.CENTER_LEFT;
                    rotationAnchor = TextAnchor.CENTER_LEFT;
                }
                final Tick tick = new DateTick ( tickDate, tickLabel, anchor, rotationAnchor, angle );
                result.add ( tick );
                hasRolled = false;
                final long currentTickTime = tickDate.getTime();
                tickDate = unit.addToDate ( tickDate, this.timeZone );
                final long nextTickTime = tickDate.getTime();
                for ( int minorTick2 = 1; minorTick2 < minorTickSpaces; ++minorTick2 ) {
                    final long minorTickTime2 = currentTickTime + ( nextTickTime - currentTickTime ) * minorTick2 / minorTickSpaces;
                    if ( this.getRange().contains ( minorTickTime2 ) && !this.isHiddenValue ( minorTickTime2 ) ) {
                        result.add ( new DateTick ( TickType.MINOR, new Date ( minorTickTime2 ), "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0 ) );
                    }
                }
            } else {
                tickDate = unit.rollDate ( tickDate, this.timeZone );
                hasRolled = true;
            }
        }
        return result;
    }
    @Override
    public AxisState draw ( final Graphics2D g2, final double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState ) {
        if ( !this.isVisible() ) {
            final AxisState state = new AxisState ( cursor );
            final List ticks = this.refreshTicks ( g2, state, dataArea, edge );
            state.setTicks ( ticks );
            return state;
        }
        AxisState state = this.drawTickMarksAndLabels ( g2, cursor, plotArea, dataArea, edge );
        if ( this.getAttributedLabel() != null ) {
            state = this.drawAttributedLabel ( this.getAttributedLabel(), g2, plotArea, dataArea, edge, state );
        } else {
            state = this.drawLabel ( this.getLabel(), g2, plotArea, dataArea, edge, state );
        }
        this.createAndAddEntity ( cursor, state, dataArea, edge, plotState );
        return state;
    }
    @Override
    public void zoomRange ( final double lowerPercent, final double upperPercent ) {
        final double start = this.timeline.toTimelineValue ( ( long ) this.getRange().getLowerBound() );
        final double end = this.timeline.toTimelineValue ( ( long ) this.getRange().getUpperBound() );
        final double length = end - start;
        long adjStart;
        long adjEnd;
        if ( this.isInverted() ) {
            adjStart = ( long ) ( start + length * ( 1.0 - upperPercent ) );
            adjEnd = ( long ) ( start + length * ( 1.0 - lowerPercent ) );
        } else {
            adjStart = ( long ) ( start + length * lowerPercent );
            adjEnd = ( long ) ( start + length * upperPercent );
        }
        if ( adjEnd <= adjStart ) {
            adjEnd = adjStart + 1L;
        }
        final Range adjusted = new DateRange ( this.timeline.toMillisecond ( adjStart ), this.timeline.toMillisecond ( adjEnd ) );
        this.setRange ( adjusted );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DateAxis ) ) {
            return false;
        }
        final DateAxis that = ( DateAxis ) obj;
        return ObjectUtilities.equal ( ( Object ) this.timeZone, ( Object ) that.timeZone ) && ObjectUtilities.equal ( ( Object ) this.locale, ( Object ) that.locale ) && ObjectUtilities.equal ( ( Object ) this.tickUnit, ( Object ) that.tickUnit ) && ObjectUtilities.equal ( ( Object ) this.dateFormatOverride, ( Object ) that.dateFormatOverride ) && ObjectUtilities.equal ( ( Object ) this.tickMarkPosition, ( Object ) that.tickMarkPosition ) && ObjectUtilities.equal ( ( Object ) this.timeline, ( Object ) that.timeline ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final DateAxis clone = ( DateAxis ) super.clone();
        if ( this.dateFormatOverride != null ) {
            clone.dateFormatOverride = ( DateFormat ) this.dateFormatOverride.clone();
        }
        return clone;
    }
    public static TickUnitSource createStandardDateTickUnits ( final TimeZone zone ) {
        return createStandardDateTickUnits ( zone, Locale.getDefault() );
    }
    static {
        DEFAULT_DATE_RANGE = new DateRange();
        DEFAULT_DATE_TICK_UNIT = new DateTickUnit ( DateTickUnitType.DAY, 1, new SimpleDateFormat() );
        DEFAULT_ANCHOR_DATE = new Date();
        DEFAULT_TIMELINE = new DefaultTimeline();
    }
    private static class DefaultTimeline implements Timeline, Serializable {
        @Override
        public long toTimelineValue ( final long millisecond ) {
            return millisecond;
        }
        @Override
        public long toTimelineValue ( final Date date ) {
            return date.getTime();
        }
        @Override
        public long toMillisecond ( final long value ) {
            return value;
        }
        @Override
        public boolean containsDomainValue ( final long millisecond ) {
            return true;
        }
        @Override
        public boolean containsDomainValue ( final Date date ) {
            return true;
        }
        @Override
        public boolean containsDomainRange ( final long from, final long to ) {
            return true;
        }
        @Override
        public boolean containsDomainRange ( final Date from, final Date to ) {
            return true;
        }
        @Override
        public boolean equals ( final Object object ) {
            return object != null && ( object == this || object instanceof DefaultTimeline );
        }
    }
}
