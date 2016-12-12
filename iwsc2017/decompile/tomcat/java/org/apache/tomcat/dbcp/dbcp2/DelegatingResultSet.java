package org.apache.tomcat.dbcp.dbcp2;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.RowId;
import java.net.URL;
import java.util.Calendar;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Blob;
import java.sql.Ref;
import java.util.Map;
import java.io.Reader;
import java.sql.ResultSetMetaData;
import java.sql.SQLWarning;
import java.io.InputStream;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
public final class DelegatingResultSet extends AbandonedTrace implements ResultSet {
    private final ResultSet _res;
    private Statement _stmt;
    private Connection _conn;
    private DelegatingResultSet ( final Statement stmt, final ResultSet res ) {
        super ( ( AbandonedTrace ) stmt );
        this._stmt = stmt;
        this._res = res;
    }
    private DelegatingResultSet ( final Connection conn, final ResultSet res ) {
        super ( ( AbandonedTrace ) conn );
        this._conn = conn;
        this._res = res;
    }
    public static ResultSet wrapResultSet ( final Statement stmt, final ResultSet rset ) {
        if ( null == rset ) {
            return null;
        }
        return new DelegatingResultSet ( stmt, rset );
    }
    public static ResultSet wrapResultSet ( final Connection conn, final ResultSet rset ) {
        if ( null == rset ) {
            return null;
        }
        return new DelegatingResultSet ( conn, rset );
    }
    public ResultSet getDelegate() {
        return this._res;
    }
    public ResultSet getInnermostDelegate() {
        ResultSet r = this._res;
        while ( r != null && r instanceof DelegatingResultSet ) {
            r = ( ( DelegatingResultSet ) r ).getDelegate();
            if ( this == r ) {
                return null;
            }
        }
        return r;
    }
    @Override
    public Statement getStatement() throws SQLException {
        return this._stmt;
    }
    @Override
    public void close() throws SQLException {
        try {
            if ( this._stmt != null ) {
                ( ( AbandonedTrace ) this._stmt ).removeTrace ( this );
                this._stmt = null;
            }
            if ( this._conn != null ) {
                ( ( AbandonedTrace ) this._conn ).removeTrace ( this );
                this._conn = null;
            }
            this._res.close();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    protected void handleException ( final SQLException e ) throws SQLException {
        if ( this._stmt != null && this._stmt instanceof DelegatingStatement ) {
            ( ( DelegatingStatement ) this._stmt ).handleException ( e );
        } else {
            if ( this._conn == null || ! ( this._conn instanceof DelegatingConnection ) ) {
                throw e;
            }
            ( ( DelegatingConnection ) this._conn ).handleException ( e );
        }
    }
    @Override
    public boolean next() throws SQLException {
        try {
            return this._res.next();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean wasNull() throws SQLException {
        try {
            return this._res.wasNull();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public String getString ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getString ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public boolean getBoolean ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getBoolean ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public byte getByte ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getByte ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public short getShort ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getShort ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int getInt ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getInt ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public long getLong ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getLong ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0L;
        }
    }
    @Override
    public float getFloat ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getFloat ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0.0f;
        }
    }
    @Override
    public double getDouble ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getDouble ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0.0;
        }
    }
    @Deprecated
    @Override
    public BigDecimal getBigDecimal ( final int columnIndex, final int scale ) throws SQLException {
        try {
            return this._res.getBigDecimal ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public byte[] getBytes ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getBytes ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Date getDate ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getDate ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Time getTime ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getTime ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Timestamp getTimestamp ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getTimestamp ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public InputStream getAsciiStream ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getAsciiStream ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Deprecated
    @Override
    public InputStream getUnicodeStream ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getUnicodeStream ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public InputStream getBinaryStream ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getBinaryStream ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public String getString ( final String columnName ) throws SQLException {
        try {
            return this._res.getString ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public boolean getBoolean ( final String columnName ) throws SQLException {
        try {
            return this._res.getBoolean ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public byte getByte ( final String columnName ) throws SQLException {
        try {
            return this._res.getByte ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public short getShort ( final String columnName ) throws SQLException {
        try {
            return this._res.getShort ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int getInt ( final String columnName ) throws SQLException {
        try {
            return this._res.getInt ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public long getLong ( final String columnName ) throws SQLException {
        try {
            return this._res.getLong ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0L;
        }
    }
    @Override
    public float getFloat ( final String columnName ) throws SQLException {
        try {
            return this._res.getFloat ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0.0f;
        }
    }
    @Override
    public double getDouble ( final String columnName ) throws SQLException {
        try {
            return this._res.getDouble ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0.0;
        }
    }
    @Deprecated
    @Override
    public BigDecimal getBigDecimal ( final String columnName, final int scale ) throws SQLException {
        try {
            return this._res.getBigDecimal ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public byte[] getBytes ( final String columnName ) throws SQLException {
        try {
            return this._res.getBytes ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Date getDate ( final String columnName ) throws SQLException {
        try {
            return this._res.getDate ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Time getTime ( final String columnName ) throws SQLException {
        try {
            return this._res.getTime ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Timestamp getTimestamp ( final String columnName ) throws SQLException {
        try {
            return this._res.getTimestamp ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public InputStream getAsciiStream ( final String columnName ) throws SQLException {
        try {
            return this._res.getAsciiStream ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Deprecated
    @Override
    public InputStream getUnicodeStream ( final String columnName ) throws SQLException {
        try {
            return this._res.getUnicodeStream ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public InputStream getBinaryStream ( final String columnName ) throws SQLException {
        try {
            return this._res.getBinaryStream ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public SQLWarning getWarnings() throws SQLException {
        try {
            return this._res.getWarnings();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void clearWarnings() throws SQLException {
        try {
            this._res.clearWarnings();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public String getCursorName() throws SQLException {
        try {
            return this._res.getCursorName();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        try {
            return this._res.getMetaData();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Object getObject ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getObject ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Object getObject ( final String columnName ) throws SQLException {
        try {
            return this._res.getObject ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public int findColumn ( final String columnName ) throws SQLException {
        try {
            return this._res.findColumn ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public Reader getCharacterStream ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getCharacterStream ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Reader getCharacterStream ( final String columnName ) throws SQLException {
        try {
            return this._res.getCharacterStream ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public BigDecimal getBigDecimal ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getBigDecimal ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public BigDecimal getBigDecimal ( final String columnName ) throws SQLException {
        try {
            return this._res.getBigDecimal ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public boolean isBeforeFirst() throws SQLException {
        try {
            return this._res.isBeforeFirst();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean isAfterLast() throws SQLException {
        try {
            return this._res.isAfterLast();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean isFirst() throws SQLException {
        try {
            return this._res.isFirst();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean isLast() throws SQLException {
        try {
            return this._res.isLast();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public void beforeFirst() throws SQLException {
        try {
            this._res.beforeFirst();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void afterLast() throws SQLException {
        try {
            this._res.afterLast();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public boolean first() throws SQLException {
        try {
            return this._res.first();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean last() throws SQLException {
        try {
            return this._res.last();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public int getRow() throws SQLException {
        try {
            return this._res.getRow();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public boolean absolute ( final int row ) throws SQLException {
        try {
            return this._res.absolute ( row );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean relative ( final int rows ) throws SQLException {
        try {
            return this._res.relative ( rows );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean previous() throws SQLException {
        try {
            return this._res.previous();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public void setFetchDirection ( final int direction ) throws SQLException {
        try {
            this._res.setFetchDirection ( direction );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public int getFetchDirection() throws SQLException {
        try {
            return this._res.getFetchDirection();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public void setFetchSize ( final int rows ) throws SQLException {
        try {
            this._res.setFetchSize ( rows );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public int getFetchSize() throws SQLException {
        try {
            return this._res.getFetchSize();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int getType() throws SQLException {
        try {
            return this._res.getType();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int getConcurrency() throws SQLException {
        try {
            return this._res.getConcurrency();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public boolean rowUpdated() throws SQLException {
        try {
            return this._res.rowUpdated();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean rowInserted() throws SQLException {
        try {
            return this._res.rowInserted();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean rowDeleted() throws SQLException {
        try {
            return this._res.rowDeleted();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public void updateNull ( final int columnIndex ) throws SQLException {
        try {
            this._res.updateNull ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBoolean ( final int columnIndex, final boolean x ) throws SQLException {
        try {
            this._res.updateBoolean ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateByte ( final int columnIndex, final byte x ) throws SQLException {
        try {
            this._res.updateByte ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateShort ( final int columnIndex, final short x ) throws SQLException {
        try {
            this._res.updateShort ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateInt ( final int columnIndex, final int x ) throws SQLException {
        try {
            this._res.updateInt ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateLong ( final int columnIndex, final long x ) throws SQLException {
        try {
            this._res.updateLong ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateFloat ( final int columnIndex, final float x ) throws SQLException {
        try {
            this._res.updateFloat ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateDouble ( final int columnIndex, final double x ) throws SQLException {
        try {
            this._res.updateDouble ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBigDecimal ( final int columnIndex, final BigDecimal x ) throws SQLException {
        try {
            this._res.updateBigDecimal ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateString ( final int columnIndex, final String x ) throws SQLException {
        try {
            this._res.updateString ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBytes ( final int columnIndex, final byte[] x ) throws SQLException {
        try {
            this._res.updateBytes ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateDate ( final int columnIndex, final Date x ) throws SQLException {
        try {
            this._res.updateDate ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateTime ( final int columnIndex, final Time x ) throws SQLException {
        try {
            this._res.updateTime ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateTimestamp ( final int columnIndex, final Timestamp x ) throws SQLException {
        try {
            this._res.updateTimestamp ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateAsciiStream ( final int columnIndex, final InputStream x, final int length ) throws SQLException {
        try {
            this._res.updateAsciiStream ( columnIndex, x, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBinaryStream ( final int columnIndex, final InputStream x, final int length ) throws SQLException {
        try {
            this._res.updateBinaryStream ( columnIndex, x, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateCharacterStream ( final int columnIndex, final Reader x, final int length ) throws SQLException {
        try {
            this._res.updateCharacterStream ( columnIndex, x, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateObject ( final int columnIndex, final Object x, final int scale ) throws SQLException {
        try {
            this._res.updateObject ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateObject ( final int columnIndex, final Object x ) throws SQLException {
        try {
            this._res.updateObject ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNull ( final String columnName ) throws SQLException {
        try {
            this._res.updateNull ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBoolean ( final String columnName, final boolean x ) throws SQLException {
        try {
            this._res.updateBoolean ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateByte ( final String columnName, final byte x ) throws SQLException {
        try {
            this._res.updateByte ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateShort ( final String columnName, final short x ) throws SQLException {
        try {
            this._res.updateShort ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateInt ( final String columnName, final int x ) throws SQLException {
        try {
            this._res.updateInt ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateLong ( final String columnName, final long x ) throws SQLException {
        try {
            this._res.updateLong ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateFloat ( final String columnName, final float x ) throws SQLException {
        try {
            this._res.updateFloat ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateDouble ( final String columnName, final double x ) throws SQLException {
        try {
            this._res.updateDouble ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBigDecimal ( final String columnName, final BigDecimal x ) throws SQLException {
        try {
            this._res.updateBigDecimal ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateString ( final String columnName, final String x ) throws SQLException {
        try {
            this._res.updateString ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBytes ( final String columnName, final byte[] x ) throws SQLException {
        try {
            this._res.updateBytes ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateDate ( final String columnName, final Date x ) throws SQLException {
        try {
            this._res.updateDate ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateTime ( final String columnName, final Time x ) throws SQLException {
        try {
            this._res.updateTime ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateTimestamp ( final String columnName, final Timestamp x ) throws SQLException {
        try {
            this._res.updateTimestamp ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateAsciiStream ( final String columnName, final InputStream x, final int length ) throws SQLException {
        try {
            this._res.updateAsciiStream ( columnName, x, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBinaryStream ( final String columnName, final InputStream x, final int length ) throws SQLException {
        try {
            this._res.updateBinaryStream ( columnName, x, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateCharacterStream ( final String columnName, final Reader reader, final int length ) throws SQLException {
        try {
            this._res.updateCharacterStream ( columnName, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateObject ( final String columnName, final Object x, final int scale ) throws SQLException {
        try {
            this._res.updateObject ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateObject ( final String columnName, final Object x ) throws SQLException {
        try {
            this._res.updateObject ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void insertRow() throws SQLException {
        try {
            this._res.insertRow();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateRow() throws SQLException {
        try {
            this._res.updateRow();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void deleteRow() throws SQLException {
        try {
            this._res.deleteRow();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void refreshRow() throws SQLException {
        try {
            this._res.refreshRow();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void cancelRowUpdates() throws SQLException {
        try {
            this._res.cancelRowUpdates();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void moveToInsertRow() throws SQLException {
        try {
            this._res.moveToInsertRow();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void moveToCurrentRow() throws SQLException {
        try {
            this._res.moveToCurrentRow();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public Object getObject ( final int i, final Map<String, Class<?>> map ) throws SQLException {
        try {
            return this._res.getObject ( i, map );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Ref getRef ( final int i ) throws SQLException {
        try {
            return this._res.getRef ( i );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Blob getBlob ( final int i ) throws SQLException {
        try {
            return this._res.getBlob ( i );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Clob getClob ( final int i ) throws SQLException {
        try {
            return this._res.getClob ( i );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Array getArray ( final int i ) throws SQLException {
        try {
            return this._res.getArray ( i );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Object getObject ( final String colName, final Map<String, Class<?>> map ) throws SQLException {
        try {
            return this._res.getObject ( colName, map );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Ref getRef ( final String colName ) throws SQLException {
        try {
            return this._res.getRef ( colName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Blob getBlob ( final String colName ) throws SQLException {
        try {
            return this._res.getBlob ( colName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Clob getClob ( final String colName ) throws SQLException {
        try {
            return this._res.getClob ( colName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Array getArray ( final String colName ) throws SQLException {
        try {
            return this._res.getArray ( colName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Date getDate ( final int columnIndex, final Calendar cal ) throws SQLException {
        try {
            return this._res.getDate ( columnIndex, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Date getDate ( final String columnName, final Calendar cal ) throws SQLException {
        try {
            return this._res.getDate ( columnName, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Time getTime ( final int columnIndex, final Calendar cal ) throws SQLException {
        try {
            return this._res.getTime ( columnIndex, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Time getTime ( final String columnName, final Calendar cal ) throws SQLException {
        try {
            return this._res.getTime ( columnName, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Timestamp getTimestamp ( final int columnIndex, final Calendar cal ) throws SQLException {
        try {
            return this._res.getTimestamp ( columnIndex, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Timestamp getTimestamp ( final String columnName, final Calendar cal ) throws SQLException {
        try {
            return this._res.getTimestamp ( columnName, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public URL getURL ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getURL ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public URL getURL ( final String columnName ) throws SQLException {
        try {
            return this._res.getURL ( columnName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void updateRef ( final int columnIndex, final Ref x ) throws SQLException {
        try {
            this._res.updateRef ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateRef ( final String columnName, final Ref x ) throws SQLException {
        try {
            this._res.updateRef ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBlob ( final int columnIndex, final Blob x ) throws SQLException {
        try {
            this._res.updateBlob ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBlob ( final String columnName, final Blob x ) throws SQLException {
        try {
            this._res.updateBlob ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateClob ( final int columnIndex, final Clob x ) throws SQLException {
        try {
            this._res.updateClob ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateClob ( final String columnName, final Clob x ) throws SQLException {
        try {
            this._res.updateClob ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateArray ( final int columnIndex, final Array x ) throws SQLException {
        try {
            this._res.updateArray ( columnIndex, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateArray ( final String columnName, final Array x ) throws SQLException {
        try {
            this._res.updateArray ( columnName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public boolean isWrapperFor ( final Class<?> iface ) throws SQLException {
        return iface.isAssignableFrom ( this.getClass() ) || iface.isAssignableFrom ( this._res.getClass() ) || this._res.isWrapperFor ( iface );
    }
    @Override
    public <T> T unwrap ( final Class<T> iface ) throws SQLException {
        if ( iface.isAssignableFrom ( this.getClass() ) ) {
            return iface.cast ( this );
        }
        if ( iface.isAssignableFrom ( this._res.getClass() ) ) {
            return iface.cast ( this._res );
        }
        return this._res.unwrap ( iface );
    }
    @Override
    public RowId getRowId ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getRowId ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public RowId getRowId ( final String columnLabel ) throws SQLException {
        try {
            return this._res.getRowId ( columnLabel );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void updateRowId ( final int columnIndex, final RowId value ) throws SQLException {
        try {
            this._res.updateRowId ( columnIndex, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateRowId ( final String columnLabel, final RowId value ) throws SQLException {
        try {
            this._res.updateRowId ( columnLabel, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public int getHoldability() throws SQLException {
        try {
            return this._res.getHoldability();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public boolean isClosed() throws SQLException {
        try {
            return this._res.isClosed();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public void updateNString ( final int columnIndex, final String value ) throws SQLException {
        try {
            this._res.updateNString ( columnIndex, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNString ( final String columnLabel, final String value ) throws SQLException {
        try {
            this._res.updateNString ( columnLabel, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNClob ( final int columnIndex, final NClob value ) throws SQLException {
        try {
            this._res.updateNClob ( columnIndex, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNClob ( final String columnLabel, final NClob value ) throws SQLException {
        try {
            this._res.updateNClob ( columnLabel, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public NClob getNClob ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getNClob ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public NClob getNClob ( final String columnLabel ) throws SQLException {
        try {
            return this._res.getNClob ( columnLabel );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public SQLXML getSQLXML ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getSQLXML ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public SQLXML getSQLXML ( final String columnLabel ) throws SQLException {
        try {
            return this._res.getSQLXML ( columnLabel );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void updateSQLXML ( final int columnIndex, final SQLXML value ) throws SQLException {
        try {
            this._res.updateSQLXML ( columnIndex, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateSQLXML ( final String columnLabel, final SQLXML value ) throws SQLException {
        try {
            this._res.updateSQLXML ( columnLabel, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public String getNString ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getNString ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public String getNString ( final String columnLabel ) throws SQLException {
        try {
            return this._res.getNString ( columnLabel );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Reader getNCharacterStream ( final int columnIndex ) throws SQLException {
        try {
            return this._res.getNCharacterStream ( columnIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Reader getNCharacterStream ( final String columnLabel ) throws SQLException {
        try {
            return this._res.getNCharacterStream ( columnLabel );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void updateNCharacterStream ( final int columnIndex, final Reader reader, final long length ) throws SQLException {
        try {
            this._res.updateNCharacterStream ( columnIndex, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNCharacterStream ( final String columnLabel, final Reader reader, final long length ) throws SQLException {
        try {
            this._res.updateNCharacterStream ( columnLabel, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateAsciiStream ( final int columnIndex, final InputStream inputStream, final long length ) throws SQLException {
        try {
            this._res.updateAsciiStream ( columnIndex, inputStream, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBinaryStream ( final int columnIndex, final InputStream inputStream, final long length ) throws SQLException {
        try {
            this._res.updateBinaryStream ( columnIndex, inputStream, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateCharacterStream ( final int columnIndex, final Reader reader, final long length ) throws SQLException {
        try {
            this._res.updateCharacterStream ( columnIndex, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateAsciiStream ( final String columnLabel, final InputStream inputStream, final long length ) throws SQLException {
        try {
            this._res.updateAsciiStream ( columnLabel, inputStream, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBinaryStream ( final String columnLabel, final InputStream inputStream, final long length ) throws SQLException {
        try {
            this._res.updateBinaryStream ( columnLabel, inputStream, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateCharacterStream ( final String columnLabel, final Reader reader, final long length ) throws SQLException {
        try {
            this._res.updateCharacterStream ( columnLabel, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBlob ( final int columnIndex, final InputStream inputStream, final long length ) throws SQLException {
        try {
            this._res.updateBlob ( columnIndex, inputStream, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBlob ( final String columnLabel, final InputStream inputStream, final long length ) throws SQLException {
        try {
            this._res.updateBlob ( columnLabel, inputStream, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateClob ( final int columnIndex, final Reader reader, final long length ) throws SQLException {
        try {
            this._res.updateClob ( columnIndex, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateClob ( final String columnLabel, final Reader reader, final long length ) throws SQLException {
        try {
            this._res.updateClob ( columnLabel, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNClob ( final int columnIndex, final Reader reader, final long length ) throws SQLException {
        try {
            this._res.updateNClob ( columnIndex, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNClob ( final String columnLabel, final Reader reader, final long length ) throws SQLException {
        try {
            this._res.updateNClob ( columnLabel, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNCharacterStream ( final int columnIndex, final Reader reader ) throws SQLException {
        try {
            this._res.updateNCharacterStream ( columnIndex, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNCharacterStream ( final String columnLabel, final Reader reader ) throws SQLException {
        try {
            this._res.updateNCharacterStream ( columnLabel, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateAsciiStream ( final int columnIndex, final InputStream inputStream ) throws SQLException {
        try {
            this._res.updateAsciiStream ( columnIndex, inputStream );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBinaryStream ( final int columnIndex, final InputStream inputStream ) throws SQLException {
        try {
            this._res.updateBinaryStream ( columnIndex, inputStream );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateCharacterStream ( final int columnIndex, final Reader reader ) throws SQLException {
        try {
            this._res.updateCharacterStream ( columnIndex, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateAsciiStream ( final String columnLabel, final InputStream inputStream ) throws SQLException {
        try {
            this._res.updateAsciiStream ( columnLabel, inputStream );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBinaryStream ( final String columnLabel, final InputStream inputStream ) throws SQLException {
        try {
            this._res.updateBinaryStream ( columnLabel, inputStream );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateCharacterStream ( final String columnLabel, final Reader reader ) throws SQLException {
        try {
            this._res.updateCharacterStream ( columnLabel, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBlob ( final int columnIndex, final InputStream inputStream ) throws SQLException {
        try {
            this._res.updateBlob ( columnIndex, inputStream );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateBlob ( final String columnLabel, final InputStream inputStream ) throws SQLException {
        try {
            this._res.updateBlob ( columnLabel, inputStream );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateClob ( final int columnIndex, final Reader reader ) throws SQLException {
        try {
            this._res.updateClob ( columnIndex, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateClob ( final String columnLabel, final Reader reader ) throws SQLException {
        try {
            this._res.updateClob ( columnLabel, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNClob ( final int columnIndex, final Reader reader ) throws SQLException {
        try {
            this._res.updateNClob ( columnIndex, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void updateNClob ( final String columnLabel, final Reader reader ) throws SQLException {
        try {
            this._res.updateNClob ( columnLabel, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public <T> T getObject ( final int columnIndex, final Class<T> type ) throws SQLException {
        try {
            return this._res.getObject ( columnIndex, type );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public <T> T getObject ( final String columnLabel, final Class<T> type ) throws SQLException {
        try {
            return this._res.getObject ( columnLabel, type );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
}
