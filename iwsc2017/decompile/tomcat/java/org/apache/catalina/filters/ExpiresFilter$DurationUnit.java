package org.apache.catalina.filters;
protected enum DurationUnit {
    DAY ( 6 ),
    HOUR ( 10 ),
    MINUTE ( 12 ),
    MONTH ( 2 ),
    SECOND ( 13 ),
    WEEK ( 3 ),
    YEAR ( 1 );
    private final int calendardField;
    private DurationUnit ( final int calendardField ) {
        this.calendardField = calendardField;
    }
    public int getCalendardField() {
        return this.calendardField;
    }
}
