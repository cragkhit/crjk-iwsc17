package org.apache.tomcat.dbcp.dbcp2;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.RowId;
import java.io.Reader;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Blob;
import java.sql.Ref;
import java.util.Map;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
public class DelegatingCallableStatement extends DelegatingPreparedStatement implements CallableStatement {
    public DelegatingCallableStatement ( final DelegatingConnection<?> c, final CallableStatement s ) {
        super ( c, s );
    }
    @Override
    public void registerOutParameter ( final int parameterIndex, final int sqlType ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).registerOutParameter ( parameterIndex, sqlType );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void registerOutParameter ( final int parameterIndex, final int sqlType, final int scale ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).registerOutParameter ( parameterIndex, sqlType, scale );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public boolean wasNull() throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).wasNull();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public String getString ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getString ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public boolean getBoolean ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getBoolean ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public byte getByte ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getByte ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public short getShort ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getShort ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int getInt ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getInt ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public long getLong ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getLong ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0L;
        }
    }
    @Override
    public float getFloat ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getFloat ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0.0f;
        }
    }
    @Override
    public double getDouble ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getDouble ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0.0;
        }
    }
    @Deprecated
    @Override
    public BigDecimal getBigDecimal ( final int parameterIndex, final int scale ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getBigDecimal ( parameterIndex, scale );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public byte[] getBytes ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getBytes ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Date getDate ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getDate ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Time getTime ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getTime ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Timestamp getTimestamp ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getTimestamp ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Object getObject ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getObject ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public BigDecimal getBigDecimal ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getBigDecimal ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Object getObject ( final int i, final Map<String, Class<?>> map ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getObject ( i, map );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Ref getRef ( final int i ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getRef ( i );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Blob getBlob ( final int i ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getBlob ( i );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Clob getClob ( final int i ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getClob ( i );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Array getArray ( final int i ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getArray ( i );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Date getDate ( final int parameterIndex, final Calendar cal ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getDate ( parameterIndex, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Time getTime ( final int parameterIndex, final Calendar cal ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getTime ( parameterIndex, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Timestamp getTimestamp ( final int parameterIndex, final Calendar cal ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getTimestamp ( parameterIndex, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void registerOutParameter ( final int paramIndex, final int sqlType, final String typeName ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).registerOutParameter ( paramIndex, sqlType, typeName );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void registerOutParameter ( final String parameterName, final int sqlType ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).registerOutParameter ( parameterName, sqlType );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void registerOutParameter ( final String parameterName, final int sqlType, final int scale ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).registerOutParameter ( parameterName, sqlType, scale );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void registerOutParameter ( final String parameterName, final int sqlType, final String typeName ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).registerOutParameter ( parameterName, sqlType, typeName );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public URL getURL ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getURL ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void setURL ( final String parameterName, final URL val ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setURL ( parameterName, val );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setNull ( final String parameterName, final int sqlType ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setNull ( parameterName, sqlType );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setBoolean ( final String parameterName, final boolean x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setBoolean ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setByte ( final String parameterName, final byte x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setByte ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setShort ( final String parameterName, final short x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setShort ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setInt ( final String parameterName, final int x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setInt ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setLong ( final String parameterName, final long x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setLong ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setFloat ( final String parameterName, final float x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setFloat ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setDouble ( final String parameterName, final double x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setDouble ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setBigDecimal ( final String parameterName, final BigDecimal x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setBigDecimal ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setString ( final String parameterName, final String x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setString ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setBytes ( final String parameterName, final byte[] x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setBytes ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setDate ( final String parameterName, final Date x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setDate ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setTime ( final String parameterName, final Time x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setTime ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setTimestamp ( final String parameterName, final Timestamp x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setTimestamp ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setAsciiStream ( final String parameterName, final InputStream x, final int length ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setAsciiStream ( parameterName, x, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setBinaryStream ( final String parameterName, final InputStream x, final int length ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setBinaryStream ( parameterName, x, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setObject ( final String parameterName, final Object x, final int targetSqlType, final int scale ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setObject ( parameterName, x, targetSqlType, scale );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setObject ( final String parameterName, final Object x, final int targetSqlType ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setObject ( parameterName, x, targetSqlType );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setObject ( final String parameterName, final Object x ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setObject ( parameterName, x );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setCharacterStream ( final String parameterName, final Reader reader, final int length ) throws SQLException {
        this.checkOpen();
        ( ( CallableStatement ) this.getDelegate() ).setCharacterStream ( parameterName, reader, length );
    }
    @Override
    public void setDate ( final String parameterName, final Date x, final Calendar cal ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setDate ( parameterName, x, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setTime ( final String parameterName, final Time x, final Calendar cal ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setTime ( parameterName, x, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setTimestamp ( final String parameterName, final Timestamp x, final Calendar cal ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setTimestamp ( parameterName, x, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setNull ( final String parameterName, final int sqlType, final String typeName ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setNull ( parameterName, sqlType, typeName );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public String getString ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getString ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public boolean getBoolean ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getBoolean ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public byte getByte ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getByte ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public short getShort ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getShort ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int getInt ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getInt ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public long getLong ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getLong ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0L;
        }
    }
    @Override
    public float getFloat ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getFloat ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0.0f;
        }
    }
    @Override
    public double getDouble ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getDouble ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0.0;
        }
    }
    @Override
    public byte[] getBytes ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getBytes ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Date getDate ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getDate ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Time getTime ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getTime ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Timestamp getTimestamp ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getTimestamp ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Object getObject ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getObject ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public BigDecimal getBigDecimal ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getBigDecimal ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Object getObject ( final String parameterName, final Map<String, Class<?>> map ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getObject ( parameterName, map );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Ref getRef ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getRef ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Blob getBlob ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getBlob ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Clob getClob ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getClob ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Array getArray ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getArray ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Date getDate ( final String parameterName, final Calendar cal ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getDate ( parameterName, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Time getTime ( final String parameterName, final Calendar cal ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getTime ( parameterName, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Timestamp getTimestamp ( final String parameterName, final Calendar cal ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getTimestamp ( parameterName, cal );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public URL getURL ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getURL ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public RowId getRowId ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getRowId ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public RowId getRowId ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getRowId ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void setRowId ( final String parameterName, final RowId value ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setRowId ( parameterName, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setNString ( final String parameterName, final String value ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setNString ( parameterName, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setNCharacterStream ( final String parameterName, final Reader reader, final long length ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setNCharacterStream ( parameterName, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setNClob ( final String parameterName, final NClob value ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setNClob ( parameterName, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setClob ( final String parameterName, final Reader reader, final long length ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setClob ( parameterName, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setBlob ( final String parameterName, final InputStream inputStream, final long length ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setBlob ( parameterName, inputStream, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setNClob ( final String parameterName, final Reader reader, final long length ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setNClob ( parameterName, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public NClob getNClob ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getNClob ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public NClob getNClob ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getNClob ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void setSQLXML ( final String parameterName, final SQLXML value ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setSQLXML ( parameterName, value );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public SQLXML getSQLXML ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getSQLXML ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public SQLXML getSQLXML ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getSQLXML ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public String getNString ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getNString ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public String getNString ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getNString ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Reader getNCharacterStream ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getNCharacterStream ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Reader getNCharacterStream ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getNCharacterStream ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Reader getCharacterStream ( final int parameterIndex ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getCharacterStream ( parameterIndex );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Reader getCharacterStream ( final String parameterName ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getCharacterStream ( parameterName );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void setBlob ( final String parameterName, final Blob blob ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setBlob ( parameterName, blob );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setClob ( final String parameterName, final Clob clob ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setClob ( parameterName, clob );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setAsciiStream ( final String parameterName, final InputStream inputStream, final long length ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setAsciiStream ( parameterName, inputStream, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setBinaryStream ( final String parameterName, final InputStream inputStream, final long length ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setBinaryStream ( parameterName, inputStream, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setCharacterStream ( final String parameterName, final Reader reader, final long length ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setCharacterStream ( parameterName, reader, length );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setAsciiStream ( final String parameterName, final InputStream inputStream ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setAsciiStream ( parameterName, inputStream );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setBinaryStream ( final String parameterName, final InputStream inputStream ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setBinaryStream ( parameterName, inputStream );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setCharacterStream ( final String parameterName, final Reader reader ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setCharacterStream ( parameterName, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setNCharacterStream ( final String parameterName, final Reader reader ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setNCharacterStream ( parameterName, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setClob ( final String parameterName, final Reader reader ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setClob ( parameterName, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setBlob ( final String parameterName, final InputStream inputStream ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setBlob ( parameterName, inputStream );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setNClob ( final String parameterName, final Reader reader ) throws SQLException {
        this.checkOpen();
        try {
            ( ( CallableStatement ) this.getDelegate() ).setNClob ( parameterName, reader );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public <T> T getObject ( final int parameterIndex, final Class<T> type ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getObject ( parameterIndex, type );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public <T> T getObject ( final String parameterName, final Class<T> type ) throws SQLException {
        this.checkOpen();
        try {
            return ( ( CallableStatement ) this.getDelegate() ).getObject ( parameterName, type );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
}
